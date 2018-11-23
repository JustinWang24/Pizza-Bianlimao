package com.taihua.pishamachine;

import android.os.Message;

import com.taihua.pishamachine.command.Cashier;
import com.taihua.pishamachine.command.CommandHelper;
import com.taihua.pishamachine.command.CommandSender;

import android.os.Handler;
import android.util.Log;

import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Justin Wang from SmartBro on 1/1/18.
 */

public class CashierManager {
    private static CashierManager CASHIER_MANAGER_INSTANCE = null;
    public static boolean STOP_READING = false;

    // 和串口相关
    private String path;
    private int rate;
    private Handler handler;
    private SerialPortHelper serialPortHelper = null;

    // 和读取金额的进程相关
    private Thread readingCashierValueThread = null;
    private int lastValue = 0;  // 最近一次读取到的金额值
    private int orderTotal = 0;

    //
    private Timer timer = null;
    private TimerTask timerTask = null;

    private CashierManager(String path, int rate){
        this.path = path;
        this.rate = rate;
    }

    public static CashierManager getInstance(){
        if (CASHIER_MANAGER_INSTANCE == null){
            CASHIER_MANAGER_INSTANCE = new CashierManager(
                    "/dev/ttymxc2", 9600
            );
        }
        STOP_READING = false;
        return CASHIER_MANAGER_INSTANCE;
    }

    /**
     * 初始化方法, 必须被调用
     * @param handler
     */
    public void init(Handler handler){
        this.handler = handler;
        this.lastValue = 0;     // 最后一次读取到的金额设置为0
        if(this.serialPortHelper == null){
            this.serialPortHelper = new SerialPortHelper(
                    this.path,
                    this.rate,
                    0,
                    'N'
            );
            this.serialPortHelper.init();
            LogUtil.LogInfo("打开串口, 并进行初始化工作, 为读取投币器做准备");
        }
        STOP_READING = false; // 现在就停止标志位重置为false

        /**
         * 开始读取投币器金额的进程
         */
//        this.timer = new Timer(true);
//        TimerTask task = new TimerTask() {
//            @Override
//            public void run() {
//                Log.i("task","running");
//            }
//        };
//        this.timer.schedule(task,500,500);



//        this.readingCashierValueThread = new NumberReader();
//        this.readingCashierValueThread.start();

        // 使能投币器
//        this.enableCashier();
    }

    /**
     * 初始化方法, 必须被调用
     * @param handler    消息处理
     * @param orderTotal 订单总金额
     */
    public void init(Handler handler, int orderTotal){
        this.orderTotal = orderTotal;
        this.init(handler);
    }

    /**
     * 停止读取投币器
     */
    public void stopTimerTask(){
        STOP_READING = true;

        if(this.timerTask != null){
            this.timerTask.cancel();
            this.timerTask = null;
        }
        if(this.timer != null){
            this.timer.cancel();
            this.timer = null;
        }
    }

    /**
     * 开始读取投币器
     */
    public void startTimerTask(){
        boolean done = this.enableCashier();
        STOP_READING = !done;
        if(!STOP_READING){
            /*
             * 开始读取投币器金额的进程
             */
            this.timer = new Timer(true);
            this.timerTask = new TimerTask() {
                @Override
                public void run() {
                    if(STOP_READING || timer == null){
                        if(timer != null){
                            timer.cancel();
                            timer = null;
                            LogUtil.LogInfo("让 timer 为 null");
                            return;
                        }
                        LogUtil.LogInfo("收到停止读取的信号");
                        return;
                    }
                    try {
                        final int result = readValue();
//                    final int result = CashierMessage.NOTES_LATEST_VALUE;
                        switch (result){
                            case CashierMessage.NOTES_LATEST_VALUE:
                                // 读到了有效的值并且该值和上次保留的不一样, 才返回这个消息. 这个时候要去通知 Handler
                                final Message cashValueChangedMessage = new Message();
                                cashValueChangedMessage.what = CashierMessage.NOTES_LATEST_VALUE;
                                cashValueChangedMessage.arg1 = lastValue;
                                LogUtil.LogInfo("读取到变化的金额: " + Integer.toString(lastValue) + ", 发送通知");
                                handler.sendMessage(cashValueChangedMessage);
                                break;
                            case CashierMessage.EQUIPMENT_DISABLED:
                                // The serial port is closed
                                final Message serialPortClosedMessage = new Message();
                                serialPortClosedMessage.what = CashierMessage.EQUIPMENT_DISABLED;
                                serialPortClosedMessage.arg1 = 0;
                                LogUtil.LogInfo("串口已经关闭了, 发送通知");
                                handler.sendMessage(serialPortClosedMessage);
                            default:
                                break;
                        }
                    }catch (Exception e){
                        stopTimerTask();
                        LogUtil.LogStackTrace(e,"发生异常 停止读取投币器:" + Integer.toString(lastValue));
                    }
                }
            };
            this.timer.schedule(this.timerTask,0,200);
        }
    }

    /**
     * 真正读取投币金额的方法
     * @return 返回读取的结果
     */
    private int readValue(){
        int result = -1;
        if (this.serialPortHelper == null){
            // 串口已经被关闭
            result = CashierMessage.EQUIPMENT_DISABLED;
            return result;
        }

        // 串口还没有被关闭

        final byte[] command = Cashier.getReadingCommand(2);
        final byte[] resultBuffer = new byte[12];

        try {
            final Date before = new Date();
            LogUtil.LogInfo(Long.toString(before.getTime()));

            final int readSize = CommandSender.send(
                    command, resultBuffer,this.serialPortHelper
            );

            final Date after = new Date();
            LogUtil.LogInfo(Long.toString(after.getTime()));

            if(readSize > 0){
                // 读取正常, 可以解析 1, 3, 4, 0, 35, 0, 0, 11, -7
                if(resultBuffer[0] == 0x01 && resultBuffer[1] == 0x03){
                    int newValue = -1;
                    switch (resultBuffer[2]){
                        case 0x02:
                            // 返回2字节长度的数据
                            newValue = resultBuffer[4] & 0xFF;
                            break;
                        case 0x04:
                            // 返回4字节长度的数据
                            newValue = resultBuffer[4] & 0xFF;
                            break;
                        default:
                            break;
                    }
//                    if(newValue != -1 && newValue != this.lastValue){
                    if(newValue != -1){
                        // 读到了有效的值并且该值和上次保留的不一样, 才返回这个消息
                        this.lastValue = newValue;
                        result = CashierMessage.NOTES_LATEST_VALUE;
                    }
                }else if(resultBuffer[0] == 0x01 && resultBuffer[1] == 0x04){
                    int newValue = resultBuffer[3] & 0xFF;
                    // [1, 4, 0, 读取到的金额值, 0, 0, 107, -16, 0, 0, 0, 0]
                    if(newValue > 0){
                        // 读到了有效的值并且该值和上次保留的不一样, 才返回这个消息
                        this.lastValue = newValue;
                        result = CashierMessage.NOTES_LATEST_VALUE;
                    }
                }else  {
                    LogUtil.LogInfo("投币器读取的数据无法解析: " + Arrays.toString(resultBuffer));
                }
            }else {
                LogUtil.LogInfo("投币器读取失败");
            }
        }catch (Exception e){
            result = CashierMessage.RESULT_READING_EXCEPTION;
            LogUtil.LogStackTrace(e,"202020202020");
        }

        return result;
    }

    /**
     * 给客户找零的操作
     * @param coinsCount 找零硬币的数量
     * @return 找零操作是否成功
     */
    public boolean giveCustomerChanges(int coinsCount){
        final boolean result = this.giveCustomerChangeAndOnly(coinsCount);

        final Message giveCustomerChangesActionDoneMessage = new Message();
        giveCustomerChangesActionDoneMessage.what = CashierMessage.CASH_PROCESS_END;  // 可以烤饼了
        this.handler.sendMessage(giveCustomerChangesActionDoneMessage);
        LogUtil.LogInfo("给客户找零的操作完成，投币器和找零器全部禁能, 发布消息给Handler");

        return result;
    }

    /**
     * 仅仅执行找零操作的方法. 只要给用户一找钱，就停止扫描投币器的金额
     * @param coinsCount 找零硬币的数量
     * @return 找零操作是否成功
     */
    public boolean giveCustomerChangeAndOnly(int coinsCount){
        this.stopTimerTask();

        // 首先不需要再读取收款的金额了
        STOP_READING = true;
        boolean result = true;

        try {
            if(coinsCount > 0){
                if(this.serialPortHelper != null){
                    // 找零
                    byte[] command = Cashier.getGiveCustomerChangesOneTimeCommand(coinsCount);
                    byte[] outBuffer = new byte[12];
                    CommandSender.send(
                            command,
                            outBuffer,
                            this.serialPortHelper
                    );
                    LogUtil.LogInfo("一次性找零命令: " + Arrays.toString(outBuffer));
                }else{
                    LogUtil.LogInfo("this.serialPortHelper 是 null, 无法给客户找零了");
                }
            }else{
                LogUtil.LogInfo("找零金额为0, 无需操作");
            }
        }catch (Exception e){
            result = false;
            LogUtil.LogStackTrace(e, "3030303030");
        }

        // 禁能投币器
        try {
            Thread.sleep(50); // 等100毫秒
            this.disableCashier();

            Thread.sleep(100); // 等100毫秒
            // 关闭串口
            this.serialPortHelper.close();
            Thread.sleep(100); // 等100毫秒
            this.serialPortHelper = null;
            LogUtil.LogInfo("关闭串口, 并把 this.serialPortHelper 设置为 NULL");
        } catch (InterruptedException e) {
            LogUtil.LogStackTrace(e, "40404040440");
            result = false;
        }

        return result;
    }

    /**
     * 禁能投币器, 同时停止读取的扫描
     */
    private void disableCashier(){
        STOP_READING = true;
        try {
            if(this.serialPortHelper != null){
                byte[] command = Cashier.getDisableCommand();
                byte[] outBuffer = new byte[12];
                CommandSender.send(
                        command,
                        outBuffer,
                        this.serialPortHelper
                );

                // [1, 5, -4, 39, -1, 0, 12, 97, 0, 0, 0, 0]
                // 01 05  FC  27  FF 00  00  00

                LogUtil.LogInfo("禁能投币器: " + Arrays.toString(outBuffer));
            }
        }catch (Exception e){
            LogUtil.LogStackTrace(e, "50505050505050");
        }
    }

    /**
     * 使能投币器的方法
     * @return boolean
     */
    private boolean enableCashier(){
        try {
            if(this.serialPortHelper != null){
                byte[] command = Cashier.getEnableCommand();
                byte[] outBuffer = new byte[12];
                CommandSender.send(
                        command,
                        outBuffer,
                        this.serialPortHelper
                );
                // [1, 5, -4, 39, 0, 0, 77, -111, 0, 0, 0, 0]
                //  01 05 FC 27 00 00 00 00

                // [2, 0, 0, -71, -4, 1, 3, 2, 0, 0, -71, -4]
                LogUtil.LogInfo("投币器使能: " + Arrays.toString(outBuffer));
                return true;
            }
        }catch (Exception e){
            LogUtil.LogStackTrace(e, "60606060660");
        }
        return false;
    }
}
