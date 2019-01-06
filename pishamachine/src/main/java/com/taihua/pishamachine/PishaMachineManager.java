/**
 * 披萨机类 与串口通讯 使用modbus协议
 * Created by Ash on 2016-10-30.
 */
package com.taihua.pishamachine;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import com.taihua.pishamachine.command.PlcDevice;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.taihua.pishamachine.utils.bytesToHexString;


public class PishaMachineManager {

    private static PishaMachineManager mInstance;
    private SerialPortHelper mSerialPortHelper = null;
    private GetStatusThread mGetStatusThread = null;

    private static final String TAG = "PishaMachineManager";
    private int mErrorTime = 0;
    private Handler handler;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // 烤饼过程的对象
    private CookingProcess cookingProcess = null;
    private final int waitingForPlcResetTimeoutMax = 2; // 最多等待两个状态查询循环周期
    private int waitingForPlcResetTimeout = 0;     //  没200毫秒查询一次设备状态，这个timeout相当于2.4秒
    private int time;
    private int nIndex;
    private boolean isLastOne = false;  // 表示本次烤的是否为最后一张
    public static boolean IS_FORCED_TO_STOP_ALL = false;   // 是否强制停止

    public int consequentNoBoxCount = 0;

    // 特殊的处理
    private int countNoBoxError = 0;

    private PishaMachineManager() {
    }


    //单例模式类
    public static PishaMachineManager getInstance() {

        if (mInstance == null) mInstance = new PishaMachineManager();
        return mInstance;
    }


//    public void sendMessage(Message msg){
//        mEventContext.Event(msg.what, msg.getData());
//    }

    //初始化
    public void init(String path, int baudrate, Handler handler) {
        this.handler = handler;
        this.countNoBoxError = 0;  // 空盒子发生故障的计数器
        // 只要一初始化，全部停止的标志就要置位到FALSE
        IS_FORCED_TO_STOP_ALL = false;
        // 初始化烤饼过程对象
        if(this.cookingProcess == null){
            this.cookingProcess = new CookingProcess();
        }
        else {
            this.cookingProcess.reset();
        }

        // 重置无盒子读取错误寄存器
        this.consequentNoBoxCount = 0;

        this.initSerialPortHelper(path, baudrate);

        // 消息处理handler对象不为空的时候, 启动状态查询线程
        if(this.handler != null){
            //初始化状态查询线程
            this.mGetStatusThread = new GetStatusThread();
            this.mGetStatusThread.start();
        }
    }

    /**
     * 专门用来初始化串口管理器的方法
     * @param path
     * @param baudrate
     */
    public void initSerialPortHelper(String path, int baudrate){
        // 重置无盒子读取错误寄存器
        this.consequentNoBoxCount = 0;

        if (mSerialPortHelper == null) {
            try{
                mSerialPortHelper = new SerialPortHelper(path, baudrate, 0, 'N');
                // 显示的初始化串口，给定读取时延为50毫秒
                mSerialPortHelper.init(50);
            }catch (Exception e){
                LogUtil.LogStackTrace(e, "串口初始化失败");
            }
        }
    }

    /**
     * 关闭串口的端口
     */
    public void closeSerialPort(){
        if(this.mSerialPortHelper != null){
            this.mSerialPortHelper.close();
        }
        this.mSerialPortHelper = null;
    }

    /**
     * 烤饼的程序
     * @param nIndex 饼所在的位置 1 - 40 之间
     * @param time   烤的是第几张饼
     * @param isLast 表示当前烤的是不是最后一张
     * @return
     */
    public int baking(final int nIndex,int time, boolean isLast) {
        // 设置PLC复位最长等待时间
        this.waitingForPlcResetTimeout = this.waitingForPlcResetTimeoutMax;

        this.time = time;
        this.nIndex = nIndex;
        this.isLastOne = isLast;

        // 初始化烤饼过程对象
        if(this.cookingProcess == null){
            this.cookingProcess = new CookingProcess();
        }
        else {
            this.cookingProcess.reset();
        }

        if(time == 1){
            // 只有烤第一张的时候才发送这个命令

            byte[] buffer = {0x01, 0x10, 0x00, 0x00, 0x00, 0x05, 0x0A, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            byte[] sd = intToBytes(nIndex);

            buffer[7] = sd[0];

            buffer[8] = sd[1];

            byte[] outBuffer = new byte[8];

            int readSize = sent(CRC16.getSendBuf(buffer), outBuffer);

            if (readSize > 0) {

                byte[] okBufferUnsinged = {0x01, 0x10, 0x00, 0x00, 0x00, 0x04, 0x00, 0x00};
                byte[] okBuffer = CRC16.getSendBuf(okBufferUnsinged);
                //01 10 00 00 00 04 C1 CA
                if (Arrays.equals(okBuffer, outBuffer)) {
                    // 这里表示烤饼命令发送跟正常
                    mErrorTime = 0;
                } else {
                    return -1;
                }
            } else {
                mErrorTime++;
                if (mErrorTime < 3) {
                    //重新发送请求
                    LogUtil.LogInfo("烤饼状态" + Integer.toString(mErrorTime) + ":错误烤饼位置" + nIndex +
                            "  " + format.format(new Date(System.currentTimeMillis())));
                    baking(nIndex,1, isLast);
                }
            }
        }

        return 0;
    }

    /**
     * 设备复位的操作, Justin
     * @return int
     */
    public int setDone() {
        // 复位指令
        byte[] buffer = {0x01, 0x10, 0x00, 0x00, 0x00, 0x05, 0x0A, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] outBuffer = new byte[8];
        final int readSize = this.sent(CRC16.getSendBuf(buffer), outBuffer);
        if (readSize > 0) {
            byte[] okBufferUnsinged = {0x01, 0x10, 0x00, 0x00, 0x00, 0x04, 0x00, 0x00};
            byte[] okBuffer = CRC16.getSendBuf(okBufferUnsinged);
            if (Arrays.equals(okBuffer, outBuffer)) {
                mErrorTime = 0;
                return MachineStatusOfMakingPizza.MACHINE_RESET_OK;
            } else {
                return MachineStatusOfMakingPizza.MACHINE_RESET_ERROR;
            }
        } else {
            mErrorTime++;
            return MachineStatusOfMakingPizza.MACHINE_RESET_ERROR;
        }
    }

    /**
     * 临时的方法, 打印一下PLC初始的状态
     */
    public void printInitPlcCode(){
        byte[] buffer = {0x01, 0x10, 0x00, 0x00, 0x00, 0x05, 0x0A, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] outBuffer = new byte[8];
        final int readSize = this.sent(CRC16.getSendBuf(buffer), outBuffer);
        if (readSize > 0) {
            byte[] okBufferUnsinged = {0x01, 0x10, 0x00, 0x00, 0x00, 0x04, 0x00, 0x00};
            byte[] okBuffer = CRC16.getSendBuf(okBufferUnsinged);
            LogUtil.LogInfo("复位Data:"+bytesToHexString(outBuffer,outBuffer.length));

        } else {
            mErrorTime++;
            LogUtil.LogInfo("复位错误");
        }
    }

    //复位
    public int reset() {
        if(this.cookingProcess != null){
            this.cookingProcess.reset();
        }
        return 0;
    }

    /**
     * 中断检查状态的进程
     */
    public void stopStatusChecking(){
        IS_FORCED_TO_STOP_ALL = true;
    }

    /**
     * 开始状态监测进程
     */
    public void startStatusChecking(){
        if(this.mGetStatusThread == null){
            this.mGetStatusThread = new GetStatusThread();
        }
        if(this.mGetStatusThread.isInterrupted()){
            this.mGetStatusThread.start();
        }
    }

    //一直发送获取状态请求
    private class GetStatusThread extends Thread {
        private volatile boolean workIsDone = false;

        @Override
        public void run() {
            super.run();
            this.startMe();
            //后两位为校验码
            final byte[] buffer = {0x01, 0x03, 0x00, 0x00, 0x00, 0x06, 0x00, 0x00};
            while (!this.workIsDone && !IS_FORCED_TO_STOP_ALL) {
//            while (!interrupted()) {
                try {
                    final byte[] outBuffer = new byte[17];
                    final int readSize = sent(CRC16.getSendBuf(buffer), outBuffer);

                    if (readSize > 0) {
                        //收到的话 ，重置错误计数
                        mErrorTime = 0;
                        // Todo 解析PLC回送的设备当前状态数据并通过 Message 发送给 PizzaMakerHandler
                        final int decodeStatus = statusDataDecode(outBuffer);
                        switch (decodeStatus){
                            case MachineStatusOfMakingPizza.PLC_STATUS_DECODE_ERROR:
                                LogUtil.LogInfo("数据解析 + 数据校验失败, 得到一个 -1： MachineStatusOfMakingPizza.PLC_STATUS_DECODE_ERROR");
                                break;
                            case MachineStatusOfMakingPizza.WAITING_FOR_PLC_RESET:
                                LogUtil.LogInfo("一张饼已经烤完, 等待PLC重置成功" );
                                break;
                            case MachineStatusOfMakingPizza.PLC_RESET_OK_FINAL:
                                LogUtil.LogInfo("PLC重置成功终于" );
                                break;
                            case MachineStatusOfMakingPizza.MACHINE_STATUS_DECODE_SHOULD_INTERRUPT:
                                // 终止本进程
                                reset();
                                LogUtil.LogInfo("所有饼已经烤完" );
                                this.stopMe();
                                break;
                            case MachineStatusOfMakingPizza.INFORM_ERROR_HAPPENED_IN_PROGRESS:
                                // 状态解析发现设备出现了故障
                                LogUtil.LogInfo("状态解析发现设备出现了故障" );
                                break;
                            default:
                                break;
                        }
                    } else {
                        mErrorTime++;
                    }

                    //5次无消息返回，报通讯故障
                    if (mErrorTime >= 10) {
                        mErrorTime = 0;
                        Message errorMessage = new Message();
                        errorMessage.what = MachineStatusOfMakingPizza.ERROR_COMMUNICATION;  //  通知外界发生了什么
                        errorMessage.arg1 = 1;  //  6666故障
                        errorMessage.arg2 = 6002;                //  6002故障, 通信故障
                        handler.sendMessage(errorMessage);
                    }

                    Thread.sleep(200);
                }catch (InterruptedException ie){
                    LogUtil.LogInfo("线程中断异常被捕获: " + ie.getMessage());
                    break;
                }catch (Exception e) {
                    LogUtil.LogInfo("发送读取状态命令报异常: " + e.getMessage());
                }
            }

            if(this.workIsDone){
//                LogUtil.LogInfo("状态查询线程正常终止了" );
            }

            if(IS_FORCED_TO_STOP_ALL){
//                LogUtil.LogInfo("强迫状态查询线程终止" );
            }
        }

        /**
         * 表示工作已完成
         */
        private void stopMe(){
            this.workIsDone = true;
        }

        /**
         * 表示工作未完成
         */
        private void startMe(){
            this.workIsDone = false;
        }
    }

    /**
     * 专门的读取PLC是否发生错误的寄存器的方法
     */
    public int readingPlcErrorCode(){
        int value = 0; // 读取到的错误数据状态
        final int responseBufferSize = 7;
        final byte[] outBuffer = new byte[responseBufferSize];
        final int readSize = sent(PlcDevice.getReadErrorCode(), outBuffer);
        if (readSize > 0) {
            if(outBuffer[0] == 0x01 && outBuffer[1] == 0x01 && outBuffer[2] == 0x02){

                LogUtil.LogInfo("读取错误寄存器返回值: " + Arrays.toString(outBuffer));

                // 表示数据是可以解析成功的
                value = bytesToInt(new byte[]{outBuffer[4],outBuffer[3]});
                if(value > 0){
                    // 表示设备处故障了
                    LogUtil.LogInfo("读取到错误, 值为" + Integer.toString(value));
                }
            }else {
//                LogUtil.LogInfo("读错误进程返回数据无法解析");
            }
        }else {
            LogUtil.LogInfo("读错误进程返回无");
        }
        return value;
    }

    /**
     * 专门的读取PLC是否 发生无盒子错误的方法
     */
    public int readingPlcErrorCodeFoxBox(){
        int value = 0; // 读取到的错误数据状态
        final int responseBufferSize = 7;
        final byte[] outBuffer = new byte[responseBufferSize];
        final int readSize = sent(PlcDevice.getReadErrorCode(), outBuffer);
        if (readSize > 0) {
            if(outBuffer[0] == 0x01 && outBuffer[1] == 0x01 && outBuffer[2] == 0x02){

                LogUtil.LogInfo("读取发生无盒子错误寄存器返回值: " + Arrays.toString(outBuffer));

                // 表示数据是可以解析成功的
                value = bytesToInt(new byte[]{outBuffer[4],outBuffer[3]});
                if(value > 0){
                    // 表示设备处故障了
                    LogUtil.LogInfo("读取到 无盒子错误, 值为" + Integer.toString(value));

                    if(outBuffer[4] == 0x00 && outBuffer[3] == 0x01){
                        // 这个是明确的无盒子错误 01, 01, 02, 01, 00, B8, 6C. 十进制为 (1, 1, 2, 1, 0, -72, 108)
                        // 错误计数累加 1
                        this.consequentNoBoxCount++;
                    }

                }else {
                    // 重置无盒子读取错误寄存器
                    this.consequentNoBoxCount = 0;
                }
            }else {
//                LogUtil.LogInfo("读错误进程返回数据无法解析");
            }
        }else {
            LogUtil.LogInfo("读错误进程返回无");
        }
        return value;
    }


    /**
     * Justin 向PLC发送指令
     * @param sendBuffer
     * @param readBuffer
     * @return 返回 -1 表示通讯错误
     */
    private int sent(byte[] sendBuffer, byte[] readBuffer) {
        int readSize;
        final int timeout = 1000;

        if (mSerialPortHelper == null) {
            readSize = -1;
//            throw new NullPointerException("mSerialPortHelper 对象不可以为空");
        }else {
            byte[] sentData = CRC16.getSendBuf(sendBuffer);
            readSize = mSerialPortHelper.sentData(sentData, readBuffer, timeout);
        }
        return readSize;
    }

    //将int 转2位 byte数组
    private static byte[] intToBytes(int iSource) {
        return
                new byte[]{
                        (byte) ((iSource >> 8) & 0xFF),
                        (byte) (iSource & 0xFF)
                };
    }

    //将两个字节的byte转成int
    private static int bytesToInt(byte[] bytes) {
        int addr = 0;
        if (bytes.length == 1) {
            addr = bytes[0] & 0xFF;
        } else {
            addr = bytes[0] & 0xFF;
            addr = (addr << 8) | (bytes[1] & 0xff);
        }
        return addr;
    }

    //将List<Integer> 转成int[]
    private static int[] convertIntegers(List<Integer> integers) {
        int[] ret = new int[integers.size()];
        Iterator<Integer> iterator = integers.iterator();
        for (int i = 0; i < ret.length; i++) {
            ret[i] = iterator.next().intValue();
        }
        return ret;
    }

    //获取int的二进制位是否为1  只适合2字节的int 最大 65535
    private static boolean GetbitValue(int value, int index) {
        byte[] btData = intToBytes(value);
        byte byte1;
        int nLocation;
        if (index < 8) {
            byte1 = btData[1];
            nLocation = index;
        } else {
            byte1 = btData[0];
            nLocation = index - 8;
        }
        return (byte1 >> (nLocation - 1) & 1) == 1;

    }

    //状态数据解析 17字节

    /**
     * 烤饼的过程中, 不停的调用这个方法, 以获取设备的状态, 每500毫秒一次
     * @param buffer 从PLC读取到的状态
     * @return int
     */
    private int statusDataDecode(byte[] buffer) {
        final int bufferSize = buffer.length;
        boolean bufferIsWrong = false;

        // 烤饼过程中发生一般型故障时, 把故障代码都放到这里
        final ArrayList<Integer> alertList  = new ArrayList<>();

        // 数据解析 + 数据校验 必须要执行，才能进行下面的粘包判断
        if (buffer.length <= 3 || !CRC16.checkBuf(buffer)) {
            // 处理一下数据错乱的可能性
            bufferIsWrong = true; // 标识数据出错
            LogUtil.LogInfo("解析失败的: " + Arrays.toString(buffer));
        }

        // 检查粘包的问题
        if(bufferIsWrong && buffer.length == 17){
            if(buffer[16] == 0x01){
                // 说明粘包了, 从新组织一下数据, 把最后一位挪到第一个来
                final byte[] newBuffer = new byte[17];
                newBuffer[0] = buffer[16];
                for (int i = 0; i < 16; i++) {
                    newBuffer[i+1] = buffer[i];
                }
                buffer = newBuffer;
                bufferIsWrong = false;
            }else if(buffer[0] == 0x00  && buffer[1] == 0x03 && buffer[2] == 0x0c ){
                // 说明第一位发生了错误, 把它修复过来
                buffer[0] = 0x01;
                bufferIsWrong = false;
            }else if(buffer[0] == 0x01 && buffer[1] == 0x0c){
                // 说明粘包了, 从新组织一下数据, 把最后一位挪到第一个来
                // 1, 12, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, -82, -80, 0
                // 1, 3, 12, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, -82, -80

                // 1, 12, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, -125, -80, 0
                // 1, 3, 12, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, -125, -80
                final byte[] newBuffer = new byte[17];
                newBuffer[0] = 0x01;
                newBuffer[1] = 0x03;
                newBuffer[2] = 0x0c;
                for (int i = 3; i < 17; i++) {
                    newBuffer[i] = buffer[i-1];
                }
                buffer = newBuffer;
                bufferIsWrong = false;
            }
        }
//        LogUtil.LogInfo("重构之后的: " + Arrays.toString(buffer));

        if(bufferIsWrong){
            // 发生了解码错误，并且还无法恢复，那么返回解码错误
            return MachineStatusOfMakingPizza.MACHINE_STATUS_DECODE_ERROR;
        }

        if (this.cookingProcess == null){
            return MachineStatusOfMakingPizza.MACHINE_STATUS_DECODE_OK;
        }

        //解析数据
        if(buffer[0] == 0x01 && buffer[1] == 0x03 && buffer[2] == 0x0c) {

            // Todo 读取来的串口二进制数据解析
            // 01 03 0c 00 00 00 00 00 00 00 00 00 00 00 00 93 70
            // b0 b1 b2 b3 b4 b5 b6 b7 b8 b9 bA bB bC bD bE CRC
            //          d0    d1    d2    d3    d4    d5    CRC

            // d0 为取饼的位置 index 1 - 40
            //int d0 = bytesToInt(new byte[]{buffer[3], buffer[4]});

            // d1 为取饼命令 数值范围0-1 该值由0变1瞬间 下发取饼命令
//             int d1 = bytesToInt(new byte[]{buffer[5], buffer[6]});

            // d2复位 设置1 复位
//            final int d2 = bytesToInt(new byte[]{buffer[7], buffer[8]});

            // D3 为有无纸盒，数值范围0-1，有纸盒为1无纸盒为0
            final int d3 = bytesToInt(new byte[]{buffer[9], buffer[10]});

//            final int d4 = bytesToInt(new byte[]{buffer[11], buffer[12]});
//            final int d5 = bytesToInt(new byte[]{buffer[13], buffer[14]});


//            final int errorValue = this.readingPlcErrorCode();
//            if(errorValue > 0){
//                // 表示错误发生了
//                if(this.cookingProcess.isWaitingForPlcResetStage() || this.cookingProcess.complete()){
//                    // 如果不是在等待推盒子的过程中，那么就报警
//                    final Message errorMessage;
//                    errorMessage = new Message();
//                    errorMessage.what = MachineStatusOfMakingPizza.INFORM_ERROR_HAPPENED_IN_PROGRESS;
//                    errorMessage.arg1 = errorValue;
//                    handler.sendMessage(errorMessage);
//
//                    return MachineStatusOfMakingPizza.INFORM_ERROR_HAPPENED_IN_PROGRESS;
//                }else {
//                    if(errorValue != 1){
//                        // 是在推盒子的过程中， 如果告警不是 "没有盒子", 那么也要发布
//                        final Message errorMessage;
//                        errorMessage = new Message();
//                        errorMessage.what = MachineStatusOfMakingPizza.INFORM_ERROR_HAPPENED_IN_PROGRESS;
//                        errorMessage.arg1 = errorValue;
//                        handler.sendMessage(errorMessage);
//
//                        return MachineStatusOfMakingPizza.INFORM_ERROR_HAPPENED_IN_PROGRESS;
//                    }
//                }
//            }

//            if(errorValue > 0){
//                final Message errorMessage;
//                errorMessage = new Message();
//                errorMessage.what = MachineStatusOfMakingPizza.INFORM_ERROR_HAPPENED_IN_PROGRESS;
//                errorMessage.arg1 = errorValue;
//                handler.sendMessage(errorMessage);
//            }

            // D3 的值是关键的参考点
            if(this.cookingProcess.isMaking()){
                if (d3 == 1) {
                    // Todo 烤至过程中， 发现饼烤完了，可以让用户取饼了
                    this.cookingProcess.switchToWaitingForTakenStage();

                    int resetDoneStatus = MachineStatusOfMakingPizza.MACHINE_RESET_ERROR;
                    final int maxRetryTimes = 5;
                    int resetRetryCount = 1;
                    while (resetDoneStatus == MachineStatusOfMakingPizza.MACHINE_RESET_ERROR){
                        // 在持续的无法得到确认的reset成功的情况下, 不停的执行 resetDone 方法，直到成功为止
                        resetDoneStatus = setDone();
                        if(resetRetryCount <= maxRetryTimes){
                            resetRetryCount ++;
                        }else {
                            resetDoneStatus = MachineStatusOfMakingPizza.MACHINE_RESET_GIVE_UP;
                        }
                    }

                    this.cookingProcess.updateSetDoneActionValue(resetDoneStatus);

                    final Message message1 = new Message();
                    message1.what = MachineStatusOfMakingPizza.INFORM_TO_TAKE_PIZZA_READY;  // 通知客人可以取饼了
                    message1.arg1 = resetDoneStatus;    // 携带是否 setDone 成功的消息: 成功或者放弃

                    handler.sendMessage(message1);
                }
                return MachineStatusOfMakingPizza.MACHINE_STATUS_DECODE_OK;
            }

            // 烤制过程中,以下的代码是不会被执行的
            // 等待客户把饼拿走区间: 开始
            if (this.cookingProcess.waitingForTaken()){
                // Todo 表示处于等待客户取饼的状态中
                if(d3 == 1){
                    // 表示客户还没有把饼拿走，继续等待吧
//                    LogUtil.LogInfo("表示客户还没有把饼拿走，继续等待吧");
                }
                if(d3 == 0){
                    // 表示客户把饼取走了, 等待新的饼的盒子的推送
//                    this.cookingProcess.switchToCompleteStage();
                    this.cookingProcess.switchToWaitingForResetStage(); // 切换到推盒子的状态

                    final Message waitingForPlcResetMessage = new Message();
                    waitingForPlcResetMessage.what = MachineStatusOfMakingPizza.WAITING_FOR_PLC_RESET;  // 饼已经取走，新的盒子还没到位. 这个状态会让上位机向服务器发送确认卖出的消息
                    handler.sendMessage(waitingForPlcResetMessage);
                }

                return MachineStatusOfMakingPizza.MACHINE_STATUS_DECODE_OK;
            }
            // 等待客户把饼拿走 结束


            // 等待推盒子
            if(this.cookingProcess.isWaitingForPlcResetStage()){
                // 推盒子的流程，需要监控 d2 这个位
                final int d2 = bytesToInt(new byte[]{buffer[7], buffer[8]});

                final int errorValue = this.readingPlcErrorCodeFoxBox();

                if(d2 == 1){
//                if(errorValue > 0){
                    // 推盒子完成了， 这个时候，还不知道是否有故障, 因此需要等待一段时间才行: 400毫秒
                    if(this.waitingForPlcResetTimeout == 0){
                        // 等待了约 2.4秒, 这个时候，应该已经检测到是否有错误了
                        // 检查是否有错误
                        if(this.consequentNoBoxCount > 2){
                            // 明确的检测到发生错误了没有盒子的错误
                            LogUtil.LogInfo("明确的检测到发生错误了没有盒子的错误 并且至少连续3次");
                            final Message errorMessage;
                            errorMessage = new Message();
                            errorMessage.what = MachineStatusOfMakingPizza.INFORM_ERROR_NO_BOX_AT_END;
                            errorMessage.arg1 = 1;  // 没有盒子的故障代码
                            handler.sendMessage(errorMessage);
                        }
                        else{
                            // 没有错误
                            final Message readyForNextMessage = new Message();
                            readyForNextMessage.what = MachineStatusOfMakingPizza.SUCCESS_READY_FOR_NEXT;  // 饼已经取走，盒子到位
                            readyForNextMessage.arg1 = this.cookingProcess.getSetDoneActionValue();        // 携带是否 setDone 成功的消息: 成功或者放弃

                            handler.sendMessage(readyForNextMessage);
                        }

                        this.cookingProcess.switchToCompleteStage();
                    }else {
                        this.waitingForPlcResetTimeout--;
                    }
                }
                return MachineStatusOfMakingPizza.MACHINE_STATUS_DECODE_OK;
            }
            // 推盒子完成

            // 等待客户把饼拿走期间，下面的代码是不会执行的
            if(this.cookingProcess.complete()){
                // 本次烤饼已经完成， 并且确认为最后一张
                // Todo 用户已经把饼拿走了, 接下来做什么. 这个会在 SUCCESS_READY_FOR_NEXT 消息发布之后 200毫秒后的下一个检查周期才得到执行
                // Todo 这个时候, 应该停止继续读取状态了，如果是只要烤一张饼
                this.cookingProcess.reset();
                if(this.isLastOne){
//                    LogUtil.LogInfo("已经是最后一张了, 中断PLC状态读取进程");
                    return MachineStatusOfMakingPizza.MACHINE_STATUS_DECODE_SHOULD_INTERRUPT;
                }else {
                    // 假定已经复位成功了
                    // 通知Handler
                    final Message machineResetComplete = new Message();
                    machineResetComplete.what = MachineStatusOfMakingPizza.PLC_RESET_OK_FINAL;
                    machineResetComplete.arg1 = this.nIndex; // 已经完成的烤的饼的位置
                    handler.sendMessage(machineResetComplete);
                    // 通知状态读取进程
                    return MachineStatusOfMakingPizza.PLC_RESET_OK_FINAL;
                }
            }
        }

        // Todo 串口读取的二进制数据解码发生错误
        return MachineStatusOfMakingPizza.MACHINE_STATUS_DECODE_ERROR;
    }
}
