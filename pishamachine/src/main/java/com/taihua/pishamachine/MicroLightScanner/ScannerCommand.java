package com.taihua.pishamachine.MicroLightScanner;

import com.taihua.pishamachine.CRC16;
import com.taihua.pishamachine.command.CommandHelper;

/**
 * Created by Justin Wang from SmartBro on 29/12/18.
 */
public class ScannerCommand {

    private static final byte[] CMD_HEAD = { 0x55, (byte)0xAA };

    /**
     * 获取清空码值的命令
     * @return byte[]
     */
    public static byte[] GetClearCodeCmd(){
        final byte[] cmd              = { 0x21 };
        final byte[] dataLength       = { 0x01, 0x00};
        final byte[] data             = { 0x00 };
        return _BuildCommand(cmd, dataLength, data);
    }

    /**
     * 扫描结果上报模式设置: 设为被动模式
     * @return byte[]
     */
    public static byte[] GetSetPassiveModeCmd(){
        final byte[] cmd        = { 0x31 };
        final byte[] dataLength = { 0x01, 0x00};
        final byte[] data       = { 0x00 };
        return _BuildCommand(cmd,dataLength,data);
    }

    /**
     * 扫描结果上报模式设置: 设为主动上报模式
     * @return byte[]
     */
    public static byte[] GetSetPositiveModeCmd(){
        final byte[] cmd        = { 0x31 };
        final byte[] dataLength = { 0x01, 0x00};
        final byte[] data       = { 0x01 };
        return _BuildCommand(cmd,dataLength,data);
    }

    /**
     * 命令模式下获取扫描结果
     * @return byte[]
     */
    public static byte[] GetReadQrCodeCommand(){
        final byte[] cmd              = { 0x30 };
        final byte[] dataLength       = { 0x00, 0x00};
        final byte[] data = {};
        return _BuildCommand(cmd,dataLength,data);
    }

    /**
     * 生成正确的命令字符串的方法
     * @param cmd           需要发送的命令 1个字节长度/2位长度字符串
     * @param dataLength    长度字:2字节/4位长度字符串，指明本条命令从长度字后面开始到校验字的 85 字节数(不含效验字)，低位 在前
     * @param data          此项可以为空 数据位
     * @return
     */
    private static byte[] _BuildCommand(
            byte[] cmd,
            byte[] dataLength,
            byte[] data
    ){
        final int totalLength = CMD_HEAD.length + cmd.length + dataLength.length + data.length;

        final byte[] commandBytes = new byte[totalLength];

        int offset = 0;

        for (int i=0;i<CMD_HEAD.length;i++){
            commandBytes[i] = CMD_HEAD[i];
            offset++;
        }

        for (int i=0;i<cmd.length;i++){
            commandBytes[offset] = cmd[i];
            offset++;
        }

        for (int i=0;i<dataLength.length;i++){
            commandBytes[offset] = dataLength[i];
            offset++;
        }

        if(data.length > 0){
            for (int i=0;i<data.length;i++){
                commandBytes[offset] = data[i];
                offset++;
            }
        }

        return _AppendVerificationCode_BCC(commandBytes);
    }

    /**
     * BCC 生成校验字 1 字节，从命令头到数据域最后一字节的逐字节异或值
     * @param commandBytes
     * @return
     */
    public static byte[] _AppendVerificationCode_BCC(byte[] commandBytes){
        final byte[] result = new byte[commandBytes.length + 1];
        // 保存校验结果
        final byte BCC[]= new byte[1];
        // 循环检测后得到校验字
        for (int i=0;i<commandBytes.length;i++){
            BCC[0] = (byte) (BCC[0]^commandBytes[i]);
        }
        // 整理校验字
        String hex = Integer.toHexString(BCC[0] & 0xFF);
        if(hex.length() == 1){
            hex = '0' + hex;
        }
        hex = hex.toUpperCase();

        // 把校验字转成字节数组，该数组只有1位长度才正确
        final byte[] bccBytes = CommandHelper.hexStringToBytes(hex);
        if(bccBytes.length == 1){
            // 把校验字放到最终结果的最后一位
            for (int i=0;i<commandBytes.length;i++){
                result[i] = commandBytes[i];
            }
            result[result.length-1] = bccBytes[0];
        }
        return result;
    }
}
