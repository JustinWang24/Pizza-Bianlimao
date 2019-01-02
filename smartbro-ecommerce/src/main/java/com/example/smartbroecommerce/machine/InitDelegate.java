package com.example.smartbroecommerce.machine;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartbro.app.AccountManager;
import com.example.smartbro.delegates.SmartbroDelegate;
import com.example.smartbro.net.RestfulClient;
import com.example.smartbro.net.callback.IFailure;
import com.example.smartbro.net.callback.ISuccess;
import com.example.smartbro.ui.launcher.ILauncherListener;
import com.example.smartbro.ui.launcher.OnLauncherFinishTag;
import com.example.smartbro.utils.timer.BaseTimerTask;
import com.example.smartbro.utils.timer.ITimerListener;
import com.example.smartbro.utils.validators.FormValidator;
import com.example.smartbroecommerce.Auth.IAuthListener;
import com.example.smartbroecommerce.Auth.MachineInitHandler;
import com.example.smartbroecommerce.R;
import com.example.smartbroecommerce.R2;
import com.example.smartbroecommerce.container.ContainerConfig;
import com.example.smartbroecommerce.main.maker.PizzaMakerHandler;
import com.taihua.pishamachine.CashierManager;
import com.taihua.pishamachine.CashierMessage;
import com.taihua.pishamachine.MicroLightScanner.CommandExecuteResult;
import com.taihua.pishamachine.MicroLightScanner.ParserImpl.QrCodeParserImpl;
import com.taihua.pishamachine.MicroLightScanner.ScannerCommand;
import com.taihua.pishamachine.MicroLightScanner.Tx200Client;
import com.taihua.pishamachine.PishaMachineManager;
import com.taihua.pishamachine.command.CommandHelper;

import java.util.Date;
import java.util.Timer;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Justin Wang from SmartBro on 4/12/17.
 * 设备初始化的 delegate
 */

public class InitDelegate extends SmartbroDelegate implements FormValidator, ITimerListener{
    // 获取数据
    @BindView(R2.id.edittext)
    TextInputEditText machineSerialNumber = null;

    // 测试功能的按钮面板
    @BindView(R2.id.btn_open_serial_port_one)
    AppCompatButton testPort1Button = null;
    @BindView(R2.id.btn_open_serial_port_two)
    AppCompatButton testPort2Button = null;
    @BindView(R2.id.btn_open_plc)
    AppCompatButton testPLCButton = null;

    // 读卡器测试
    @BindView(R2.id.btn_open_reader_keep_alive)
    AppCompatButton openAndKeepAliveBtn = null; // 使能扫描枪
    @BindView(R2.id.btn_reader_close)
    AppCompatButton closeReaderButton = null;   // 禁能扫描枪

    @BindView(R2.id.tv_console)
    TextView console = null;

    private PishaMachineManager pishaMachineManager = null;

    /**
     * 扫描的定时器
     */
    private Timer scanCustomerPaymentCodeTimer = null;
    private BaseTimerTask timerTask = null;

    /**
     * 需要一个Android的handler来在Loader消失的时候加上一些延时
     * Handler声明为 static 类型， 避免内存泄漏
     */
    // 测试相关声明结束

    private IAuthListener iAuthListener = null;
    private ILauncherListener iLauncherListener = null;
    private Activity activity = null;

    // 测试相关功能
    private boolean cashierEnabled = false;
    private CashierManager cashierManager = null;

    //
    private boolean openAndKeepAliveBtnJustClicked = false;

    // 测试功能相关函数集合

    @OnClick(R2.id.btn_open_plc)
    void onTestPlcClicked(){
        if(this.pishaMachineManager == null){
            this.pishaMachineManager = PishaMachineManager.getInstance();
        }
        final PizzaMakerHandler handler = PizzaMakerHandler.getInstance(null,String.valueOf(AccountManager.getMachineId()));
        this.pishaMachineManager.init(
                ContainerConfig.PATH,
                ContainerConfig.BAUD_RATE,
                handler
        );
        this.pishaMachineManager.printInitPlcCode();
    }

    @OnClick(R2.id.btn_open_reader_keep_alive)
    void openAndKeepAliveClicked(){
        try {
            final CommandExecuteResult result = Tx200Client.getClientInstance().connect();
            echo("扫码工作模式模式: 间隔模式(2s); "+result.getResult() + " : " + result.getRealResult(), true);
        }catch (Exception e){
            echo("执行 启动命令 错误: " + e.getMessage(), false);
        }
        this.startScanning();
    }

    @OnClick(R2.id.btn_reader_close)
    void closeReaderBtnClicked(){
        this.stopScanning();
        String resultString = "";
        try{
            resultString = Tx200Client.getClientInstance().disconnect().getRealResult();
        }catch (Exception e){
            resultString = "停止工作发生错误: " + e.getMessage();
        }
        echo("扫描枪暂停工作", false);
        echo(resultString,false);
        echo("扫描枪停止工作操作成功",false);
        // 检查解析QR
//        final byte[] readBuffer = {
//                0x55, (byte)0xAA, 0x30,
//                0x00,       // 标识字:一字节， 0x00则代表成功应答，其它失败或错误
//                0x22, 0x00, // 两字节，指明本条命令从长度字后面开始到校验字的字节数(不含效验字)，低位在前
//                // 数据域开始
//                0x30, 0x31,0x44,0x47,0x35,
//                0x30, 0x4B,0x58,0x59,0x41,
//                0x56, 0x51,0x45,0x46,0x44,
//                0x67, 0x4D,0x47,0x44,0x41,
//                0x45, 0x2F,0x37,0x6B,0x47,
//                0x46, 0x4A,0x74,0x6F,0x31,
//                0x78, 0x69,0x61,0x72,
//                // 数据域结束
//                (byte)0x9C  // BCC校验字
//        };
//        final String expecting = "01DG50KXYAVQEFDgMGDAE/7kGFJto1xiar";
//        final String QrCode = QrCodeParserImpl.bytesToAsciiString(readBuffer).toString();
//        Log.i("Info", "二维码: " + QrCode);
//        Log.i("Info", "期待结果: " + expecting);
//        Log.i("Info", Boolean.toString(expecting.equals(QrCode)));
    }

    /**
     * 开始扫码的定时任务
     */
    private void startScanning(){
        if(this.scanCustomerPaymentCodeTimer == null){
            this.scanCustomerPaymentCodeTimer = new Timer(true);
            this.timerTask = new BaseTimerTask(this);
        }
        this.scanCustomerPaymentCodeTimer.scheduleAtFixedRate(
                this.timerTask,
                1000,
                2000
        );
    }

    /**
     * 停止扫描器的定时任务
     */
    private void stopScanning(){
        if(this.scanCustomerPaymentCodeTimer != null){
            this.scanCustomerPaymentCodeTimer.cancel();
            this.scanCustomerPaymentCodeTimer = null;
            this.timerTask.cancel();
            this.timerTask = null;
        }
    }

    @Override
    public void onTimer() {
        getProxyActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final CommandExecuteResult commandExecuteResult = Tx200Client.getClientInstance().scan();
                    final String QrCodeString = commandExecuteResult.getResult();
                    echo("读取到结果 REAL: " + commandExecuteResult.getRealResult(), false);

                    if(!CommandExecuteResult.KEEP_WAITING.equals(QrCodeString)){
                        echo("读取到结果: " + QrCodeString, false);
                        stopScanning();
                    }

                }catch (Exception e){
                    echo("扫描中 发生错误: " + e.getMessage(), false);
                    stopScanning();
                }

            }
        });
    }

    /**
     * 测试串口2: 投币器使能禁能
     */
    @OnClick(R2.id.btn_open_serial_port_one)
    void setTestPort1ButtonClicked(){
//        HANDLER1.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                echo("PLC串口打开正常: " + ContainerConfig.PATH + ", 速率:" + ContainerConfig.BAUD_RATE ,false);
//            }
//        },1000);
//
//        PishaMachineManager
//            .getInstance()
//            .init(
//                ContainerConfig.PATH,
//                ContainerConfig.BAUD_RATE,
//                HANDLER1
//            );
        final MsgHandler msgHandler = new MsgHandler();
        if(this.cashierManager == null){
            this.cashierManager = CashierManager.getInstance();
            this.cashierManager.init(msgHandler);
        }

        if(this.cashierEnabled){
            echo("投币器禁能: ", true);
            // 投币器已经使能， 执行禁能操作
            this.cashierEnabled = false;
//            final boolean result = this.cashierManager.disableCashier();
            this.cashierManager.stopTimerTask();

        }else {
            echo("投币器使能: ", false);
            this.cashierEnabled = true;
//            this.cashierManager.enableCashier();
            this.cashierManager.startTimerTask();
        }
        // 测试注册一个事件
//        EventBus.getDefault().post("setTestPort1ButtonClicked", "do_something_tag");
    }

    private class MsgHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if(msg.what == CashierMessage.NOTES_LATEST_VALUE){
                final Date date = new Date();
                echo("读取到金额: " + Integer.toString(msg.arg1) + ": " + date.toString(), true);
            }

            if(msg.what == CashierMessage.EQUIPMENT_DISABLED){
                echo("投币器读取进程结束了" + Integer.toString(msg.arg1), false);
            }
        }
    }

    /**
     *
     */
    @OnClick(R2.id.btn_open_serial_port_two)
    void setTestPort2ButtonClicked(){
        final MsgHandler msgHandler = new MsgHandler();
        if(this.cashierManager == null){
            this.cashierManager = CashierManager.getInstance();
            this.cashierManager.init(msgHandler);
        }

        final boolean result = this.cashierManager.giveCustomerChangeAndOnly(Integer.valueOf(this.machineSerialNumber.getText().toString()));
        echo("找零完毕" + this.machineSerialNumber.getText().toString() + ": " + Boolean.toString(result), false);
    }

    /**
     * 简单的打印输出
     * @param line
     */
    @Override
    public void echo(String line, boolean clear){
        if(clear){
            this.console.setText(getString(R.string.testing_console_title) + "\n\n");
            this.console.append(line + "\n");
        }else {
            this.console.append(line + "\n");
        }

    }

    // 测试相关功能函数集合 结束

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);

        // 保存宿主的 Activity 实例
        this.activity = this.getProxyActivity();


        if(activity instanceof IAuthListener){
            this.iAuthListener = (IAuthListener) activity;
        }

        if(activity instanceof ILauncherListener){
            this.iLauncherListener = (ILauncherListener) activity;
        }

        // 检查是否设备已经初始化过了
//        this.checkIsMachineInitialized();
    }

    // 当初始化确认按钮被点击
    @OnClick(R2.id.btn_confirm_init)
    void onClickInitMachine(){
        if(this.validate()){
//            getProxyActivity().deleteDatabase("pizza_box.db");
            // 如果输入的设备串号符合要求, 那么就提交给服务器进行验证
            RestfulClient.builder()
                .url("machines/init")
                .params("serial_number",this.machineSerialNumber.getText().toString())
                .success(new ISuccess() {
                    @Override
                    public void onSuccess(String response) {
                        // 设备初始化成功, 把服务器返回结果与认证的监听类对象传给handler去处理
                        MachineInitHandler.onInitDone(response, iAuthListener);
                        checkIsMachineInitialized();
                    }
                })
                .failure(new IFailure() {
                    @Override
                    public void onFailure() {
                        // 设备初始化失败了, 显示提示信息
                        Toast.makeText(
                            getContext(),
                            getString(R.string.err_text_init_failed),
                            Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .build()
                .post();
        }
    }

    @Override
    public Object setLayout() {

        return R.layout.delegate_machine_init_new;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {

//        Log.i("***Info 70" , Arrays.toString(this.hexStringToByteArray("70")));
//        Log.i("***Info 87" , Arrays.toString(this.hexStringToByteArray("87")));

//        final byte[] resp = new byte[]{
//                0x09,0x00,0x01,0x1C,
//                0x01,0x53,0x00,0x38,
//                0x07
//        };
//
//        byte[] result = new byte[resp.length + 2];
//
//        String data = "123456789";
//        String data2 = "0900011C0153003807";
//        String crcString = CRCUtil.getCRC16CCITT(CRC16.getBufHexStr(resp),0x1021,0x0000,true);
//
//        char[] chars = crcString.toCharArray();
//        String crcHigh = String.valueOf(chars[2]) + String.valueOf(chars[3]);
//        String crcLow = String.valueOf(chars[0]) + String.valueOf(chars[1]);
//
//        Log.i("info CRC",crcHigh);
//        Log.i("info CRC",crcLow);
//
//
//        Log.i("info CRC", Arrays.toString(this.hexStringToByteArray(crcHigh)));
//        Log.i("info CRC", Arrays.toString(this.hexStringToByteArray(crcLow)));

//        byte[] d = MarshallProtocol.getInstance().getConfigureCommand(
//                "0", "0", "0"
//        );
////
//        echo("Config Info " + Arrays.toString(d),false);
//
//        byte[] input = new byte[]{
//                0x12, 0x00, 0x00,0x00,
//                0x01, 0x53, 0x00,0x00,
//                0x05, 0x00, 0x0B,0x04,
//                0x01, 0x00, 0x00,0x30, 0x30, 0x30, (byte) 0xC6, (byte)0x9C
//        };
//
//        Log.i("crcString", Arrays.toString(input));
//        Log.i("crcString", Arrays.toString(d));


//        Log.i("Vend Request", Arrays.toString(MarshallProtocol.getInstance().getVendRequestCommand(20,2)));

//        byte[] input = new byte[]{
//                0x4B, 0x00, 0x00,0x00, 0x01, 0x53, 0x00,0x00, 0x05,  // Packet Header
//                0x00, 0x0B, // Protocol version
//                0x04,       // Peripheral Type
//                0x01,       // Peripheral Sub Type
//                0x00, 0x00, // Peripheral capabilities
//                //Peripheral Model
//                0x30,
//                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
//                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
//
//                //Peripheral serial number
//                0x30,
//                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
//                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
//                0x30,
//                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
//                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
//                (byte) 0x84, (byte)0x33
//        };

//        byte[] input = new byte[]{
//                0x4B, 0x00, 0x00,0x00, 0x01, 0x53, 0x00,0x00, 0x05,  // Packet Header
//                0x00, 0x0B, // Protocol version
//                0x04,       // Peripheral Type
//                0x01,       // Peripheral Sub Type
//                0x00, 0x00, // Peripheral capabilities
//                //Peripheral Model
//                0x4D, 0x6F, 0x64, 0x65, 0x6C, 0x5F, 0x36, 0x35, 0x30, 0x30,
//                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
//
//                //Peripheral serial number
//                0x53, 0x65, 0x72, 0x69, 0x61, 0x6C, 0x5F, 0x00, 0x00, 0x00,
//                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
//
//                0x53, 0x69, 0x6D, 0x75, 0x6C, 0x61, 0x74, 0x6F, 0x72,0x5F,
//                0x56, 0x65, 0x72, 0x5F, 0x30, 0x31, 0x5F, 0x30, 0x31,0x00,
//                0x07, 0x71
//        };
//
//        Log.i("Info", "packed leng " + Integer.toString(input.length));
//        Log.i("Info", "packed leng " + Arrays.toString(input));
//        String bodyString = CRC16.getBufHexStr(input); // 把长度和packet的内容合成一个字节数组之后再计算CRC
//        String crcString = CRCUtil.getCRC16CCITT(bodyString,0x1021,0x0000,true);
//
//        Log.i("CRC string", crcString);
//
//        char[] chars = crcString.toCharArray();
//        String crcHigh = String.valueOf(chars[2]) + String.valueOf(chars[3]);
//        String crcLow = String.valueOf(chars[0]) + String.valueOf(chars[1]);
//
//        // 第5步
//        Log.i("info", Arrays.toString(hexStringToByteArray(crcHigh)));
//        Log.i("info", Arrays.toString(hexStringToByteArray(crcLow)));
        // 第6步

//        int i = 1;
//        byte[] bs = new byte[]{(byte)i};
//        Log.i("Info", "Vend Request " + Arrays.toString(MarshallProtocol.getInstance().getVendRequestCommand(12,1)));

//        String dateString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(new Date());
//        Log.i("Info", dateString);
////        byte[] input = MarshallProtocol.getInstance().getKeepAliveCommand();
//
//        byte[] input = new byte[]{0x4B, 0x00, 0x00,0x00, 0x01, 0x53, 0x00,0x00, 0x05};
//        Log.i("Info", Arrays.toString(input));
//
//        Log.i("Info", byteArrayToHexString(input));

//        Log.i("Hex",byteArrayToHexString(Cashier.getWriteOrderTotalCommand(8)));


        // 读卡器程序切换到 Fake 模式
//        CardReaderFactory.switchToFakeMode();

//        byte[] resp = new byte[]{
//                0,5,2,0,0x38,
//                1,0x53,0x0A,3,1,
//                1,4,1,4,7,
//                4,0x39,0x3B,(byte)0xC7,
//                (byte)0xDE,1,8,(byte)0xFA,0x24,
//                (byte)0x86,(byte)0xBD,0,0,0,
//                0,9,1,0,0x35,
//                (byte) 0xBE,0x22
//        };
//        byte[] id = this.parseTransactionId(resp);
//        Log.i("txn id", byteArrayToHexString(id));
//        echo("未开始连接读卡器", false);
        try{
            final boolean mode = false;
            final String modeString = mode ? "命令模式" : "主动上报模式";
            final CommandExecuteResult commandExecuteResult = Tx200Client.getClientInstance().setMode(mode);
            echo("设置扫描结果上报模式: " + modeString + "; " + commandExecuteResult.getRealResult() + ":" + commandExecuteResult.getRealResult(), true);
        }catch (Exception e){
            echo("串口链接失败, " + e.getMessage(), true);
        }
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);

    }

    @Override
    public boolean validate() {
        final String sn = this.machineSerialNumber.getText().toString();
        boolean validate = true;
        if(sn.isEmpty()){
            // 设备的序列号不可以为空字符串
            this.machineSerialNumber.setError(getString(R.string.err_text_sn));
            validate = false;
        }

        return validate;
    }

    /**
     * 检查是否设备已经初始化过了
     */
    private void checkIsMachineInitialized(){
        if(AccountManager.getSignState()){
            // Todo 表示设备已经初始化了 该如何操作
            if(this.iLauncherListener != null){
                // 执行初始化完成时的回调
                this.iLauncherListener.onLaunchFinish(OnLauncherFinishTag.INITED);
            }
        }
    }
}
