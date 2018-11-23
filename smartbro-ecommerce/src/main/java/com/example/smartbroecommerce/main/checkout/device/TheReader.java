package com.example.smartbroecommerce.main.checkout.device;

import android.os.Message;
import android.util.Log;

import com.taihua.pishamachine.CardReaderModule.CardReaderMessage;
import com.taihua.pishamachine.LogUtil;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Justin Wang from SmartBro on 4/2/18.
 */

public abstract class TheReader {
    static final boolean TESTING_MODE = false;
    public static boolean isReadyToDeliveryProduct = false;

    static TheReader theReader = null;

    volatile boolean stopTransceiver = false;

    IReaderAction readerAction = null;
    private long interval = 100;

    ScheduledExecutorService executorService = null;

    /**
     * 表示当前的状态值
     */
    volatile int receivedMessageType = -1;

    /**
     * 最新消息
     */
    volatile ScheduledFuture<Message> future = null;

//    private volatile boolean isReadyToDeliveryProduct = false;

    private Message latestMessage;

    /**
     * 获取读卡器对象实例
     * @return
     */
    public abstract TheReader getInstance();

    /**
     * 打开串口
     */
    abstract void openSerialPort();

    /**
     * 关闭串口
     */
    abstract void closeSerialPort();

    // 读串口数据
    abstract int readSerialPort();
    // 写串口数据
    abstract int writeSerialPort(byte[] input, byte[] response, int timeout);

    /**
     * 开始执行读取操作
     */
    public void start(IReaderAction action, long interval){
        this.openSerialPort();

        this.readerAction = action;
        this.interval = interval;
        stopTransceiver = false;
        isReadyToDeliveryProduct = false;

        this.latestMessage = new Message();

        if(this.executorService == null){
            Log.i("info", "开始读卡器");
            this.executorService = Executors.newSingleThreadScheduledExecutor();
            this.executorService.scheduleWithFixedDelay(
                    new CallableTransceiver(),
                    100,
                    this.interval,
                    TimeUnit.MILLISECONDS
            );

//            this.future = (ScheduledFuture<Message>) this.executorService.scheduleWithFixedDelay(
//                    new CallableTransceiver(),
//                    100,
//                    this.interval,
//                    TimeUnit.MILLISECONDS
//            );
        }
    }

    public void stop(){
        // 用来结束当前的进程
        this.closeSerialPort();
        this.stopTransceiver = true;
    }

    /**
     * 通知外界可以获取产品了。但是为了保证只通知一次。在为真的情况下，一旦被获取，就置位为false
     * @return
     */
    public boolean getIsReadyToDeliveryProduct(){
        LogUtil.LogInfo("Delegate 询问是否可以烤饼的答案: " + Boolean.toString(isReadyToDeliveryProduct));
        return isReadyToDeliveryProduct;
    }

    public Message getLatestMessage(){
        return this.latestMessage;
    }

    /**
     * 获取串口读取线程的消息
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
//    public Message getFutureMessage() throws ExecutionException, InterruptedException {
//        if(this.future != null){
//            return this.future.get();
//        }
//        return null;
//    }

    private class CallableTransceiver implements Runnable{

//        @Override
//        public Message call() throws Exception {
//            if (!stopTransceiver){
//                // 读取串口, 获取到当前的消息
//                receivedMessageType = readSerialPort();
//
//                LogUtil.LogInfo("消息类型: " + CardReaderMessage.explain(receivedMessageType));
//
//                latestMessage.what = receivedMessageType;
//
//                switch (receivedMessageType){
//                    case CardReaderMessage.KEEP_SILENCE:
//                        // 在读卡器还有没有Reset的状态下，打开串口可能会读到全是0的数据，这个时候，什么也不做，等下一个周期
//                        readerAction.onKeepSilence();
//                        break;
//                    case CardReaderMessage.CARD_READER_RESET:
//                        // 要求读卡器开始工作, 在此种情况下, 读到了 reset
//                        readerAction.onResetSuccess(latestMessage);
//                        break;
//                    case CardReaderMessage.CARD_READER_CONFIG_SUCCESS:
//                        // 读取到Config成功信息
//                        readerAction.onConfig(latestMessage);
//                        break;
//                    case CardReaderMessage.ENABLE_OK:
//                        // 当读取到读卡器被激活
//                        readerAction.onEnableOk(latestMessage);
//                        break;
//                    case CardReaderMessage.IS_BEGIN_SESSION:
//                        readerAction.onBeginSession(latestMessage);
//                        break;
//                    case CardReaderMessage.CARD_READER_VEND_APPROVED:
//                        isReadyToDeliveryProduct = true;        // Todo 通知外界可以制作商品了
//                        readerAction.onVendApprove(latestMessage);
//                        break;
//                    case CardReaderMessage.NEED_SEND_VEND_SUCCESS:
//                        readerAction.onVendSuccess(latestMessage);
//                        break;
//                    case CardReaderMessage.CARD_READER_SESSION_COMPLETE:
//                        readerAction.onSessionComplete(latestMessage);
//                        break;
//                    case CardReaderMessage.WAITING_END_SESSION:
//                        // 如果是等待session结束, 那么只发送keep alive 即可
//                        readerAction.onKeepAliveOnly();
//                        break;
//                    case CardReaderMessage.IS_END_SESSION:
//                        readerAction.onEndSession(latestMessage);
//                        stopTransceiver = true;
//                        break;
//                    case CardReaderMessage.NEED_KEEP_ALIVE_ONLY:
//                        readerAction.onKeepAliveOnly();
//                        break;
//                    default:
//                        // 没有收到命令，那么就发送 Keep Alive
//                        readerAction.onIdleState(latestMessage);
//                        break;
//                }
//
//                return latestMessage;
//            }else {
//                latestMessage.what = CardReaderMessage.CARD_READER_IDLE;
//                isReadyToDeliveryProduct = false;
//            }
//
//            readerAction.onIdleState(latestMessage);
//            return latestMessage;
//        }

        private void handler(){
            if (!stopTransceiver){
                // 读取串口, 获取到当前的消息
                receivedMessageType = readSerialPort();

                LogUtil.LogInfo("消息类型: " + CardReaderMessage.explain(receivedMessageType));


                latestMessage.what = receivedMessageType;

                switch (receivedMessageType){
                    case CardReaderMessage.KEEP_SILENCE:
                        // 在读卡器还有没有Reset的状态下，打开串口可能会读到全是0的数据，这个时候，什么也不做，等下一个周期
                        readerAction.onKeepSilence();
                        break;
                    case CardReaderMessage.CARD_READER_RESET:
                        // 要求读卡器开始工作, 在此种情况下, 读到了 reset
                        readerAction.onResetSuccess(latestMessage);
                        break;
                    case CardReaderMessage.CARD_READER_CONFIG_SUCCESS:
                        // 读取到Config成功信息
                        readerAction.onConfig(latestMessage);
                        break;
                    case CardReaderMessage.ENABLE_OK:
                        // 当读取到读卡器被激活
                        readerAction.onEnableOk(latestMessage);
                        break;
                    case CardReaderMessage.IS_BEGIN_SESSION:
                        readerAction.onBeginSession(latestMessage);
                        break;
                    case CardReaderMessage.CARD_READER_VEND_APPROVED:
                        TheReader.isReadyToDeliveryProduct = true;        // Todo 通知外界可以制作商品了
                        readerAction.onVendApprove(latestMessage);
                        break;
                    case CardReaderMessage.NEED_SEND_VEND_SUCCESS:
                        readerAction.onVendSuccess(latestMessage);
                        break;
                    case CardReaderMessage.CARD_READER_SESSION_COMPLETE:
                        readerAction.onSessionComplete(latestMessage);
                        break;
                    case CardReaderMessage.WAITING_END_SESSION:
                        // 如果是等待session结束, 那么只发送keep alive 即可
                        readerAction.onKeepAliveOnly();
                        break;
                    case CardReaderMessage.IS_END_SESSION:
                        readerAction.onEndSession(latestMessage);
                        stopTransceiver = true;
                        break;
                    case CardReaderMessage.NEED_KEEP_ALIVE_ONLY:
                        readerAction.onKeepAliveOnly();
                        break;
                    default:
                        // 没有收到命令，那么就发送 Keep Alive
                        readerAction.onIdleState(latestMessage);
                        break;
                }
                LogUtil.LogInfo("是否可以烤饼: " + Boolean.toString(TheReader.isReadyToDeliveryProduct));
                return;
            }else {
                latestMessage.what = CardReaderMessage.CARD_READER_IDLE;
                isReadyToDeliveryProduct = false;
                readerAction.onIdleState(latestMessage);
            }
        }

        @Override
        public void run() {
            try {
                this.handler();
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }
    }
}
