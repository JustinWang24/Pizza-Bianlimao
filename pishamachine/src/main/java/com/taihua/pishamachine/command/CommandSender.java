package com.taihua.pishamachine.command;

import com.taihua.pishamachine.SerialPortHelper;

/**
 * Created by Justin Wang from SmartBro on 1/1/18.
 * 发送串口命令的方法
 */

public class CommandSender {

    /**
     * 从给定的串口帮助对象发送命令
     * @param command
     * @param returnBuffer
     * @param helper
     * @return
     */
    public static int send(byte[] command, byte[] returnBuffer, SerialPortHelper helper){
        int readSize = -1;
        try {
            if(helper != null){
                readSize = helper.sentData(
                        command,
                        returnBuffer,
                        1000
                );
            }
            return readSize;
        }catch (Exception e){
            return readSize;
        }
    }
}
