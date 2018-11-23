package com.taihua.pishamachine.command;

import com.taihua.pishamachine.CRC16;
import com.taihua.pishamachine.command.marshall.MarshallProtocol;
import com.taihua.pishamachine.command.marshall.PacketFirmwareInfo;

/**
 * Created by Justin Wang from SmartBro on 1/1/18.
 * 信用卡读卡器的命令管理
 */

public class CreditCardReader {

    public static int READER_PAIRED_OK      = 1000;      // 配对成功
    public static int READER_PAIRED_ERROR   = 1001;   // 配对失败
    public static int KEEP_ALIVE_DONE       = 1002;      // 设置Keep Alive 完成
    public static int KEEP_ALIVE_FAILED     = 1003;      // 设置Keep Alive 失败



    public static byte[] getEnableCommand(){
        /**
         *  0B 00 01 2F 01 53 00 30 80 14 01 CD 5D
            1. Function code = MDB Command
            2. MDB Command type = reader enable
         */
        final String cmd = "0B00012F015300308014010000"; // 最后总是加上 00 00 给CRC
        return CRC16.getSendBuf(cmd);
    }

    /**
     * 在通电或者收到Reset包之后, 需要这个命令来进行配对操作
     * Contains specific FW details of Peripheral. 
     * Sent at Power Up / Reset.
     * @return
     */
    public static MarshallProtocol packetFirmwareInfo(){
        MarshallProtocol packet = MarshallProtocol.getInstance();
        return packet;
    }

    /**
     * 获取配对命令
     * @return
     */
    public static byte[] getPairCommand(){
        final byte[] cmd = new byte[]{
                0x4B, 0x00, 0x00, 0x00, 0x02, 0x53, 0x00, 0x00, 0x05,   // 1: packet header
                0x00, 0x0A,                                             // 2: Protocol version
                0x04,                                                   // 3: Peripheral Type
                0x02,                                                   // 4: Peripheral Sub-Type
                0x00, 0x00,                                             // 5: Peripheral capabilities
                0x4D, 0x6F, 0x64, 0x65, 0x6C, 0x5F, 0x31, 0x32, 0x38,   // 6: Peripheral Model (ASCII string, Max size - 20 Bytes)
                0x30, 0x35, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                // 7: Peripheral serial number (ASCII string, Max size - 20 Bytes)
                0x00, 0x53, 0x65, 0x72, 0x69, 0x61, 0x6C, 0x5F, 0x31, 0x33,
                0x34, 0x37, 0x38, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x53,
                // 8: Peripheral SW version (ASCII string, Max size - 20 Bytes)
                0x69, 0x6D, 0x75, 0x6C, 0x61, 0x74, 0x6F, 0x72, 0x5F, 0x56,
                0x65, 0x72, 0x5F, 0x30, 0x30, 0x5F, 0x31, 0x35, 0x00,
                // 9: CRC
                0x00, 0x00
        };
        return CRC16.getSendBuf(cmd);
    }

    /**
     * 检验配对命令的方法
     * @param response 返回结果, 是有CRC的
     * @return
     */
    public static boolean isPairedDone(byte[] response){
        // 这个是没有CRC的期待结果的字符串形式
        final String expect = "2A0000000030015306000A01E803000001CC030000000002303435323331303030303030303030300002";
        final byte[] expectation = CommandHelper.hexStringToBytes(expect);
        return isMatch(response, expectation);
    }

    /**
     * 在配对成功之后的keep alive 命令
     * @return
     */
    public static byte[] getKeepAliveCommand(){
        // 09 00 01 01 01 53 00 30 07 D9 0E
        final byte[] cmd = new byte[]{
                0x09, 0x00, // 字节长度
                0x01,       // Packet Options
                0x01,       // Packet ID
                0x01,       // Source:
                0x53, 0x00, 0x30, 0x07, 0x00, 0x00
        };
        return CRC16.getSendBuf(cmd);
    }

    /**
     * 是否Keep Alive状态完成
     * @param response
     * @return
     */
    public static boolean isKeepAliveDone(byte[] response){
        // 0A 00 00 01 00 30 01 53 00 00 64 51
        final byte[] expectation = CommandHelper.hexStringToBytes("0A000001003001530000");
        return isMatch(response, expectation);
    }

    /**
     * 比较给定的返回值是否符合预期
     * @param response    真实的返回值， 有CRC
     * @param expectation 期待的结果, 无CRC
     * @return
     */
    private static boolean isMatch(byte[] response, byte[] expectation){
        boolean notMatch = false;
        if(response.length > expectation.length){
            for (int i = 0; i < expectation.length; i++) {
                if(expectation[i] != response[i]){
                    notMatch = true;
                    break;
                }
            }
        }
        return !notMatch;
    }

    /**
     * 从Amit设备发来的响应的解析
     * @param response
     * @return
     */
    public static int parse(byte[] response){
        return 1;
    }
}
