package com.example.smartbroecommerce.main.checkout.device;

import android.util.Log;

import com.taihua.pishamachine.CardReaderModule.CardReaderMessage;
import com.taihua.pishamachine.LogUtil;
import com.taihua.pishamachine.command.marshall.MarshallProtocol;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Justin Wang from SmartBro on 4/2/18.
 */

public class MarshallPacketParser {
    private static MarshallPacketParser packetParser = null;
    private static boolean isResetHandled               = false;
    private static boolean isWaitingForConfig           = true;     // 初始值为真
    private static boolean isReadyToEnableCard          = false;
    private static boolean isSessionAlreadyBegan        = false;
    private static boolean isVendNotApproved            = true;     // 初始值为真
    private static boolean isReadyToSendVendSuccess     = false;
    private static boolean isReadyToSendSessionComplete = false;
    private static boolean startWaitingEndSession       = false;
    private static boolean isReaderInIdleState          = false;

    private byte lastAmitPacketId = 0x01;

    private MarshallPacketParser(){}

    public static MarshallPacketParser getInstance(){
        if (packetParser == null){
            packetParser = new MarshallPacketParser();
        }
        return packetParser;
    }

    /**
     * 所有状态复位
     */
    private void _goingToIdleState(){
//        isResetHandled = false;
//        isWaitingForConfig = true;
//        isReadyToEnableCard = false;
        // 表示是否已经开始了 Session
        isSessionAlreadyBegan = false;
        isVendNotApproved = true;
        isReadyToSendVendSuccess = false;
        isReadyToSendSessionComplete = false;
        startWaitingEndSession = false;

        // 标识目前为 Idle 状态
//        isReaderInIdleState = false;
    }

    /**
     * 只获取实例，不改变任何的状态
     * @return
     */
    public static MarshallPacketParser getInstanceOnly(){
        if (packetParser == null){
            packetParser = new MarshallPacketParser();
        }
        return packetParser;
    }

    /**
     * 设置可以发送 Session Success 了.
     */
    public static void readyToSendVenRequest(){
        isReadyToSendVendSuccess = true;
        isVendNotApproved = false;
    }

    /**
     * 设置可以发送 Session Complete 了.
     */
    public static void readyToSendSessionComplete(){
        isReadyToSendSessionComplete = true;
    }

    public static void waitingEndSessionCommand(){
        startWaitingEndSession = true;
    }

    /**
     * 获取保存的 Amit 发送的Packet ID
     * @return
     */
    public byte getLastAmitPacketId(){
        return this.lastAmitPacketId;
    }

    /**
     * 分析读卡器的返回值
     * @param resp  包含Packet的字节数组
     * @param dataLength 有效数据长度
     * @return
     */
    public int parse(byte[] resp, int dataLength){
        this._printLog("\nParsing Data: isResetHandled=" + Boolean.toString(isResetHandled), null, resp);

        int result = CardReaderMessage.NEED_KEEP_ALIVE_ONLY;

        if(
            isReaderInIdleState &&              // 如果现在读卡器处于空闲的状态
            !isReadyToSendSessionComplete &&    // 并且没有准备发送session complete
            !startWaitingEndSession             // 并且没有 等待End session 那么应该直接进入发送Enable的流程
        ){
            isReadyToEnableCard = true;
            isWaitingForConfig = false;
        }

        if(!isResetHandled && !isReaderInIdleState && this._isAllZero(resp)){
            // 如果还没有初始化 却受到了全零的数据, 返回一个什么也不做， 保持安静的消息
            return CardReaderMessage.KEEP_SILENCE;
        }

        if(this.isResetPacket(resp)){
            // 是 reset 数据包, 标记已经reset, 然后发送 reset 成功过得消息
            result = CardReaderMessage.CARD_READER_RESET;
            isResetHandled = true;
            isWaitingForConfig = true;
            return result;
        }

        // Todo 这个步骤在 下面的Config之后
        if(isReadyToEnableCard){
            // 假定 Enable 必定成功, 因此 重置 ready 的标记为，返回enable 成功消息
            isReadyToEnableCard = false;
            // 一旦Enable, 就不在是 Idle 状态了
            isReaderInIdleState = false;
            return CardReaderMessage.CARD_READER_CONFIG_SUCCESS;
        }

        // Todo 这个步骤在 上面 Enable 之前
        if(isResetHandled && isWaitingForConfig){
            // 在reset之后, 肯定到这里已经发送了 Firmware Info 和 收到了 Config, 这种情况下，直接返回一个 Keep Alive
            isWaitingForConfig = false;

            // 设定请求 enable的 标记为，下一个周期即可 使能读卡器
            isReadyToEnableCard = true;

            return result;
        }

        // 以上三种情况都处理以后，读卡器进入发送 Begin Session 的状态

        if(!isSessionAlreadyBegan && this.isRightAckInfo(resp)){
            // 检查Enable是否成功: 注意 第9位必须是0, 表示没有失去同步
            // 同时这个也检查是检查了Keep Alive的返回值
            result = CardReaderMessage.ENABLE_OK;

            // 确认现在不是空闲状态了
            isReaderInIdleState = false;
            return result;
        }

        if(!isSessionAlreadyBegan && this.isBeginSession(resp)){
            isSessionAlreadyBegan = true;
            result = CardReaderMessage.IS_BEGIN_SESSION;
            return result;
        }

        if(isVendNotApproved && this.isVendApprove(resp) && !isReaderInIdleState){
            // 已经收到了Vend Approve 命令
            isVendNotApproved = false;
            result = CardReaderMessage.CARD_READER_VEND_APPROVED;
            this._printLog("发送可以烤饼的消息",null, null);
        }

        if(isSessionAlreadyBegan && isVendNotApproved){
            // Begin session 已经完成， 等待 Vend Approve 的过程中
            result = CardReaderMessage.NEED_KEEP_ALIVE_ONLY;
        }

        if(isReadyToSendVendSuccess){
            // 通知外界可以发布 Vend Success 了
            result = CardReaderMessage.NEED_SEND_VEND_SUCCESS;
            // 只发布一次即可，所以这个进行重置
            isReadyToSendVendSuccess = false;
        }

        if(isReadyToSendSessionComplete){
            isReadyToSendSessionComplete = false;
            // 在这个时候设置为Idle, 避免end session 之后多处理一次 vend approve.
            isReaderInIdleState = true;
            result = CardReaderMessage.CARD_READER_SESSION_COMPLETE;
        }

        if(startWaitingEndSession){
            result = CardReaderMessage.WAITING_END_SESSION;
            if(this.isEndSessionCommand(resp)){
                // 如果收到了 Send Session 命令
                result = CardReaderMessage.IS_END_SESSION;

                // Todo 把所有标志重置位
                _goingToIdleState();
            }
        }

        return result;
    }

    /**
     * 判断是否为全零数据. 根据发送规律，最少也会出现3个零
     * @param resp
     * @return
     */
    private boolean _isAllZero(byte[] resp){
        boolean result = true;
        int zeroCount = 0;
        for (int i = 0; i < resp.length; i++) {
            if(resp[i] != 0x00){
                zeroCount++;
            }
            if(zeroCount > 3){
                result = false;
                break;
            }
        }
        return result;
    }

    /**
     * 是否Amit的通用反馈，用来检测是否为正常的keep alive 和 card enable 返回值
     * @param resp
     * @return
     */
    private boolean isRightAckInfo(byte[] resp){
        boolean result = resp[0] == 0x0A && resp[1] == 0x00 && resp[2] == 0x00 && resp[9] == 0x00;
        this._printLog("Amit ACK: " + Boolean.toString(result), null, null);
        return result;
    }

    /**
     * 是否 End Session 命令
     * @param resp
     * @return
     */
    private boolean isEndSessionCommand(byte[] resp){
        boolean result = false;

        for (int i = 0; i < resp.length-2; i++) {
            if(resp[i] == (byte) 0x80 && resp[i+1] == 0x07){
                result = true;
                break;
            }
        }

        this._printLog("Vend Approve: " + Boolean.toString(result), null, null);
        return result;
    }

    /**
     * 是否 Vend Approve 命令
     * @param resp
     * @return
     */
    private boolean isVendApprove(byte[] resp){
        boolean result = false;

        // Todo : 读卡器发来的 transfer Data, 需要解析. 只要有连续的 80 05

        for (int i = 0; i < resp.length-3; i++) {
            if(resp[i] == (byte) 0x80 && resp[i+1] == 0x05 && resp[i+2] != 0x00){
                if(i>4){
                    this.lastAmitPacketId = resp[i - 5];
                }
                result = true;
                break;
            }
        }

        this._printLog("Vend Approve: " + Boolean.toString(result), null, null);
        return result;
    }

    /**
     * 是否是Begin Session的数据包
     * @param resp
     * @return
     */
    private boolean isBeginSession(byte[] resp){
        boolean result = resp[8] == (byte) 0x80 && resp[9] == 0x03;

        if(!result){
            for (int i = 0; i < resp.length-2; i++) {
                if(resp[i] == (byte) 0x80 && resp[i+1] == 0x03){
                    result = true;
                    break;
                }
            }
        }

        this._printLog("isBeginSession: " + Boolean.toString(result), null, null);
        return result;
    }

    /**
     * 是否为Config 数据. 这个不需要验证，似乎每次都是可以成功的
     * @param resp
     * @return
     */
    private boolean isConfigData(byte[] resp){
        return true;
//        boolean result = resp[0] == 0x2A && resp[1] == 0x00;
//
//        if(!result){
//            // 如果不是，检查一下可能的错位
//            for (int i = 0; i < resp.length-6; i++) {
//                if(resp[i] == (byte) 0x2A && resp[i+1] == 0x00 && resp[i+2] == 0x00 && resp[i+3] == 0x00 && resp[i+4] == 0x00 && resp[i+5] == 0x38){
//                    result = true;
//                    break;
//                }
//            }
//        }
//
//        this._printLog("is Config: " + Boolean.toString(result), null, null);
//        return result;
    }

    /**
     * 检查是否为reset 数据
     * @param responseBuffer
     * @return
     */
    private boolean isResetPacket(byte[] responseBuffer){
        /**
         * 配对必须要完全匹配
         */
        boolean result = false;
        for (int i = 0; i < 8; i++) {
            if(
                    responseBuffer[i] == (byte) 0xFF &&
                    responseBuffer[i+1] == (byte) 0xFF &&
                    responseBuffer[i+2] == (byte) 0xFF &&
                    responseBuffer[i+3] == (byte) 0xFF &&
                    responseBuffer[i+4] == (byte) 0x01
            ){
                result = true;
                break;
            }
        }

        this._printLog("isResetPacket: " + Boolean.toString(result), null, null);

        return result;
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
