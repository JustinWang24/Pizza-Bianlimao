package com.taihua.pishamachine.command;

import com.taihua.pishamachine.CRC16;

/**
 * Created by Justin Wang from SmartBro on 1/1/18.
 * 生成找零投币相关的操作的命令的已包括CRC的字节数组工具类
 */

public class Cashier {
    /**
     * 获取投币器使能命令
     * @return byte[]
     */
    public static byte[] getEnableCommand(){
        final byte[] input =  CommandHelper.hexStringToBytes("0105FC2700000000");
        return appendCRC(input);
    }

    public static byte[] getGiveCustomerChangesOneTimeCommand(int total){
        String totalHexString = Integer.toHexString(total);
        if(totalHexString.length() == 1){
            totalHexString = "0" + totalHexString;
        }
        final byte[] input =  CommandHelper.hexStringToBytes("0106000700" + totalHexString + "0000");
        return appendCRC(input);
    }

    /**
     * 获取投币器禁能命令
     * @return byte[]
     */
    public static byte[] getDisableCommand(){
        final byte[] input =  CommandHelper.hexStringToBytes("0105FC27FF000000");
        return appendCRC(input);
    }

    /**
     * 获取读取投币器金额命令
     * @return byte[]
     */
    public static byte[] getReadingCommand(int dataLength){
        // 读取总金额，而不是分别读取纸币和硬币两个点
        final byte[] input = CommandHelper.hexStringToBytes(
            "0103005D000" + Integer.toString(dataLength) + "0000"
        );
        return appendCRC(input);
    }

    /**
     * 获取找零器使能并吐出一个硬币的命令
     * @return byte[]
     */
    public static byte[] getChargeEnableCommand(){
        final byte[] input =  CommandHelper.hexStringToBytes("0105FC25FF000000");
        return appendCRC(input);
    }

    /**
     * 获取找零器禁能命令
     * @return byte[]
     */
    public static byte[] getChargeDisableCommand(){
        final byte[] input =  CommandHelper.hexStringToBytes("0105FC2500000000");
        return appendCRC(input);
    }

    /**
     * 为指定的输入字节数组添加CRC校验码到尾部
     * @param input
     * @return byte[]
     */
    private static byte[] appendCRC(byte[] input){
        return CRC16.getSendBuf(input);
    }
}
