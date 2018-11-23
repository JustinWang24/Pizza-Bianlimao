package com.taihua.pishamachine;

import java.util.ArrayList;

/**
 * Created by Justin Wang from SmartBro on 19/12/17.
 */

public class MachineStatusOfMakingPizza {
    // 消息通知用的变量
    public static final int INFORM_ERROR_NO_BOX_AT_END        = 6665;   // 通知设备最后没有正常的推出盒子
    public static final int INFORM_ERROR_HAPPENED_IN_PROGRESS = 6666;   // 通知设备故障
    public static final int INFORM_TO_PLAY_VOICE_MESSAGE      = 6667;   // 通知播放语音
    public static final int INFORM_ALL_DONE                   = 6669;   // 通知全部完成
    public static final int INFORM_TO_TAKE_PIZZA_READY        = 6668;   // 通知可以取饼
    public static final int WAITING_FOR_PLC_RESET             = 6671;   // 等待PLC重置成功
    public static final int PLC_RESET_OK_FINAL                = 6672;   // PLC重置成功
    public static final int INFORM_TO_MAKE_NEXT_PIZZA         = 6670;   // 上一张饼取走了，通知上位机去考下一张饼
    public static final int ERROR_COMMUNICATION = 6002;
    public static final int SUCCESS_READY_FOR_NEXT = 6004;  // 准备好取下一张饼

    // 表示一张饼已经考完了，通知处理器类, 但是不代表全部烤完。是否全部烤完，处理器类自己去判断
    public static final int INFORM_ONE_DONE_READY_TO_TAKE     = 6611;   //

    // 设备的状态值
    public static final int MACHINE_RESET_OK    = 0;
    public static final int MACHINE_RESET_ERROR = -1;
    public static final int MACHINE_RESET_GIVE_UP = 88;

    public static final int PLC_STATUS_DECODE_ERROR = -1;
    public static final int MACHINE_STATUS_DECODE_ERROR = -1;
    public static final int MACHINE_STATUS_DECODE_OK    = 22;
    public static final int MACHINE_STATUS_DECODE_SHOULD_INTERRUPT    = 44; // 读取设备状态码应该结束

    public static final int MACHINE_STATUS_ALL_GOOD = 66;   // 设备整体状态正常
    public static final int MACHINE_ERROR_RS232_2 = 2322;   // 2号串口无法读取
    public static final int MACHINE_ERROR_RS232_3 = 2323;   // 3号串口无法读取


    // 设备的一般性故障描述
    public static final String[] GENERAL_ERRORS = new String[]{
            "有无纸盒报警",
            "没有盒子",
            "推盒左限位不到位",
            "翻盒开关不到位",
            "无此错误 4",
            "拨饼开关不到位",
            "无此错误 6",
            "无此错误 7",
            "烤箱交换饼叉右限位不到位",
            "无此错误 9",
            "烤箱提升不到位",
            "烤箱烤饼叉后限位不到位",
            "12 无此错误",
            "13 无此错误",
            "14 无此错误",
            "15 无此错误",
            "冷柜电机右限位不到位",
            "17 无此错误",
            "18 无此错误",
            "冷柜电机后限位不到位",
            "冷柜电机下限位不到位",
            "21 无此错误"
    };
}
