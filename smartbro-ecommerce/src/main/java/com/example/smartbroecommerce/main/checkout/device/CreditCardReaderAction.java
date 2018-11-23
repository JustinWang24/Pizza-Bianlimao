package com.example.smartbroecommerce.main.checkout.device;

import android.os.Message;
import android.util.Log;

import com.taihua.pishamachine.LogUtil;
import com.taihua.pishamachine.command.marshall.MarshallProtocol;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Justin Wang from SmartBro on 4/2/18.
 */

public class CreditCardReaderAction implements IReaderAction {
    private TheReader cardReader = null;
    // 最后读取到的 Packet ID 的值
    private int lastPacketId = 0;

    private boolean isVendRequestSent = false;

    private int productId;
    private int productPrice;
    private int productQuantity;

    public CreditCardReaderAction(TheReader reader){
        this.cardReader = reader;
        this.isVendRequestSent = false;
    }

    @Override
    public void setProductInfo(int productId, int productPrice, int productQuantity) {
        this.productId = productId;
        this.productPrice = productPrice;
        this.productQuantity = productQuantity;
    }

    @Override
    public void onKeepSilence() {

    }

    private boolean _sendKeepAlive(){
        byte[] outBuffer = new byte[12];
        byte[] input = MarshallProtocol.getInstance().getKeepAliveCommand();
        this.cardReader.writeSerialPort(
                input,
                outBuffer,
                30
        );
        return outBuffer[0] == 0x0A && outBuffer[9] == 0x00;
    }

    @Override
    public void onKeepAliveOnly() {
        this._sendKeepAlive();
    }

    @Override
    public void onResetSuccess(Message message) {
        // 表示成功的收到了Reset数据包, 那么接下来要去发送 Firmware Info 数据
        byte[] resp = new byte[100];
        byte packetId = 0x00;
        this.cardReader.writeSerialPort(
                MarshallProtocol.getInstance().getFirmwareInfoCommand(packetId),
                resp,
                30
        );
    }

    @Override
    public void onResetFail(Message message) {

    }

    @Override
    public void onConfig(Message message) {
        // 当收到了成功的config配置数据时执行: 发送 Enable 命令
        final byte[] input = MarshallProtocol.getInstance().getEnableReaderCommand();
        final byte[] outBuffer = new byte[12];
        this.cardReader.writeSerialPort(
                input,
                outBuffer,
                30
        );
    }

    @Override
    public void onConfigFailed(Message message) {

    }

    @Override
    public void onKeepAliveOk(Message message) {

    }

    @Override
    public void onKeepAliveFailed(Message message) {

    }

    @Override
    public void onEnableOk(Message message) {
        // 卡被激活, 可以什么都不做
    }

    @Override
    public void onEnableFailed(Message message) {
        this._sendKeepAlive();
    }

    @Override
    public void onBeginSession(Message message) {
        // Session 已经开始, 发送Vend Request
        if(!this.isVendRequestSent){
            this.isVendRequestSent = true;
            final byte[] resp = new byte[70];
            final byte[] input = MarshallProtocol.getInstance().
                    getVendRequestCommand(this.productPrice,this.productQuantity);
            this.cardReader.writeSerialPort(
                    input, resp, 50
            );
        }
    }

    @Override
    public void onVendRequest(Message message) {

    }

    @Override
    public void onVendApprove(Message message) {
        // 要会送一个ACK
        final byte[] ackCmd = MarshallProtocol.
                getInstance().
                getAckCommand(
                        MarshallPacketParser.getInstanceOnly().getLastAmitPacketId(),
                        false);

        final byte[] ackResp = new byte[70];
        this.cardReader.writeSerialPort(
                ackCmd,
                ackResp,
                50
        );
        MarshallPacketParser.readyToSendVenRequest();
    }

    @Override
    public void onVendSuccess(Message message) {
        // 发送 Vend Success 和 Vend Complete
        final byte[] outBuffer = new byte[12];
        final byte[] input = MarshallProtocol.getInstance().getVendSuccessCommand();

        this.cardReader.writeSerialPort(
                input,
                outBuffer,
                50
        );
        // 准备好发送 session complete
        MarshallPacketParser.readyToSendSessionComplete();
    }

    @Override
    public void onSessionComplete(Message message) {
        final byte[] outBuffer = new byte[70];
        final byte[] input = MarshallProtocol.getInstance().getSessionCompleteCommand();
        this.cardReader.writeSerialPort(
                input,
                outBuffer,
                50
        );
        // 开始等待 Amit 结束流程
        MarshallPacketParser.waitingEndSessionCommand();
    }

    @Override
    public void onEndSession(Message message) {

    }

    @Override
    public void onDisableCard(Message message) {

    }

    @Override
    public void onIdleState(Message message) {
        this._printLog("读卡器处于空闲状态",null,null);
        this._sendKeepAlive();
    }

    private void _printLog(String processName, byte[] input, byte[] output){
        if(true){
            String dateString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(new Date());
            LogUtil.LogInfo(processName + ": " + dateString);
            if(input != null){
                LogUtil.LogInfo("Snd: " + MarshallProtocol.byteArrayToHexString(input));
            }

            if(output != null){
                LogUtil.LogInfo("Rvd: " + MarshallProtocol.byteArrayToHexString(output));
            }
            if(input == null && output == null){
                LogUtil.LogInfo("\n");
            }
        }else{
            Log.i("Info Parse", processName);
        }
    }
}
