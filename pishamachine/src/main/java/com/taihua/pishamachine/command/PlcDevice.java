package com.taihua.pishamachine.command;

import com.taihua.pishamachine.CRC16;

/**
 * Created by Justin Wang from SmartBro on 9/1/18.
 */

public class PlcDevice {

    /**
     * 读取错误状态存储寄存器的命令
     * @return byte[]
     */
    public static byte[] getReadErrorCode(){
        final byte[] input =  CommandHelper.hexStringToBytes("01010096000A0000");
        return CRC16.getSendBuf(input);
    }

    public static byte[] getUnlockDoorCommand(){
        final byte[] input =  CommandHelper.hexStringToBytes("0105FC15FF000000");
        return CRC16.getSendBuf(input);
    }

    public static byte[] getUnlockDoorStep2Command(){
        final byte[] input =  CommandHelper.hexStringToBytes("0105FC1500000000");
        return CRC16.getSendBuf(input);
    }
}
