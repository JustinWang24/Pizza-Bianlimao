package com.taihua.pishamachine.CardReaderModule;

/**
 * Created by Justin Wang from SmartBro on 22/1/18.
 * 和读卡器相关的消息
 */

public class CardReaderMessage {
    // 和读卡器相关的消息
    public static final int CARD_READER_PAIR_FAIL       = 911;
    public static final int CARD_READER_PAIR_SUCCESS    = 912;
    public static final int CARD_READER_LOST            = 913; // 丢失与Amit的通信了
    public static final int CARD_READER_CONFIG_FAILED   = 914;  // 初始化失败
    public static final int CARD_READER_CONFIG_SUCCESS  = 915;  // 初始化成功
    public static final int CARD_READER_NOT_ALIVE       = 916;
    public static final int CARD_READER_IS_ALIVE        = 917;
    public static final int CARD_READER_WAITING_FOR_BEGIN_SESSION  = 918;  // 进入等待用户划卡或者取消状态
    public static final int CARD_READER_SEND_VEND_REQUEST  = 988;  // 进入等待用户划卡或者取消状态
    public static final int CARD_READER_TRANSFER_DATA     = 919;  // Amit 发来的支付确认信息已经得到

    public static final int CARD_READER_VEND_CANCEL     = 922;  // 回话结束了
    public static final int CARD_READER_VEND_DENIED     = 923;  // 回话结束了


    public static final int CARD_READER_IDLE            = 924;  // 读卡器处于空闲状态
    public static final int CARD_READER_RESET           = 925;  // 读卡器处于空闲状态
    public static final int IS_BEGIN_SESSION            = 926;  // 读卡器处于空闲状态
    public static final int CARD_READER_VEND_APPROVED   = 920;  // Amit vend approve
    public static final int IS_END_SESSION              = 927;  // Amit vend approve
    public static final int ENABLE_OK                   = 928;  // Enable ok
    public static final int NEED_KEEP_ALIVE_ONLY        = 929;  // Enable ok
    public static final int NEED_SEND_VEND_SUCCESS      = 930;  // 可以发送Vend Success
    public static final int CARD_READER_SESSION_COMPLETE     = 921;  // 回话结束了
    public static final int WAITING_END_SESSION         = 931;  // 等待 Amit 结束
    public static final int KEEP_SILENCE                = 932;  // 等待 Amit 结束

    public static final int VEND_CANCEL  = 2;
    public static final int VEND_APPROVE = 1;

    public static String explain(int msg){
        String result = "未知消息";

        switch (msg){
            case CARD_READER_RESET:
                result = "等待Reset成功";
                break;
            case CARD_READER_CONFIG_SUCCESS:
                result = "收到Config成功";
                break;
            case NEED_KEEP_ALIVE_ONLY:
                result = "Keep Alive";
                break;
            case ENABLE_OK:
                result = "使能成功";
                break;
            case IS_BEGIN_SESSION:
                result = "收到 Begin Session";
                break;
            case CARD_READER_VEND_APPROVED:
                result = "收到 Vend Approved";
                break;
            case NEED_SEND_VEND_SUCCESS:
                result = "收到可以发送 vend success 消息";
                break;
            case CARD_READER_SESSION_COMPLETE:
                result = "收到可以发送 SESSION_COMPLETE 消息";
                break;
            case IS_END_SESSION:
                result = "收到 End Session 消息";
                break;
            case KEEP_SILENCE:
                result = "收到保持安静消息";
                break;
            case WAITING_END_SESSION:
                result = "等待 End Session 消息";
                break;
            default:
                break;
        }

        return result;
    }
}
