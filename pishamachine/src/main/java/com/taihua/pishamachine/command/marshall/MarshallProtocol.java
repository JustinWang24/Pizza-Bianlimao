package com.taihua.pishamachine.command.marshall;

import android.util.Log;

import com.taihua.pishamachine.CRC16;
import com.taihua.pishamachine.command.CommandHelper;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Created by Justin Wang from SmartBro on 4/1/18.
 */

public class MarshallProtocol{
    private static MarshallProtocol marshallProtocol = null;
    public static byte AMIT_ID     = 0x00;
    public static byte AMIT_DESTINATION = 0x00;
    public static byte AMIT_DESTINATION_LSB = 0x38;

    private byte[] packetLength     = new byte[2];
    private byte[] packetOptions    = new byte[1];
    private byte[] packetID         = new byte[1];
    private byte[] source           = new byte[1];
    private byte[] sourceLSB        = new byte[1];
    private byte[] destination      = new byte[1];
    private byte[] destinationLSB   = new byte[1];
    private byte[] functionCode     = new byte[1];
    public byte[] data              = null;
    private byte[] CRCField         = new byte[]{0x00,0x00};    // 初始的CRC值

    private int packetIdCounter = 0;
    private int maxKeepAlivePacketId = 255;

    private MarshallProtocol(){
//        super(PacketType.MARSHALL);
    }

    public static MarshallProtocol getInstance(){
        if(marshallProtocol == null){
            marshallProtocol = new MarshallProtocol();
        }
        return marshallProtocol;
    }

    /**
     * 获取配置信息的发送命令, 即 Firmware Info
     * @param modelInAscii
     * @param serialNumberInAscii
     * @param applicationSoftwareVersionInAscii
     * @return
     */
    public byte[] getConfigureCommand(String modelInAscii, String serialNumberInAscii, String applicationSoftwareVersionInAscii){

        this.setPacketOptions(CommandHelper.hexStringToBytes("00"));
        this.setPacketID(CommandHelper.hexStringToBytes("00"));
        this.setSource(CommandHelper.hexStringToBytes("01"));
        this.setSourceLSB(CommandHelper.hexStringToBytes("53"));
        this.setDestination(CommandHelper.hexStringToBytes("00"));
        this.setDestinationLSB(CommandHelper.hexStringToBytes("00"));
        this.setFunctionCode(CommandHelper.hexStringToBytes("05"));

        byte[] params = new byte[]{
            0x00, 0x0B,
            0x04,
            0x01,
            0x00,0x00
        };

        final String dataString = modelInAscii + serialNumberInAscii + applicationSoftwareVersionInAscii;
        byte[] machineData = dataString.getBytes(StandardCharsets.US_ASCII);
        this.setData(mergeBytes(params, machineData));

        return this.packetGenerator();
    }

    /**
     * 获取Firmware Info 命令的方法
     * @param packetId
     * @return
     */
    public byte[] getFirmwareInfoCommand(byte packetId){
        byte[] input = new byte[]{
                0x4B, 0x00, 0x00,packetId, 0x01, 0x53, 0x00,0x00, 0x05,  // Packet Header
                0x00, 0x0B, // Protocol version
                0x04,       // Peripheral Type
                0x01,       // Peripheral Sub Type
                0x00, 0x00, // Peripheral capabilities
                //Peripheral Model
                0x4D, 0x6F, 0x64, 0x65, 0x6C, 0x5F, 0x36, 0x35, 0x30, 0x30,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,

                //Peripheral serial number
                0x53, 0x65, 0x72, 0x69, 0x61, 0x6C, 0x5F, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,

                0x53, 0x69, 0x6D, 0x75, 0x6C, 0x61, 0x74, 0x6F, 0x72,0x5F,
                0x56, 0x65, 0x72, 0x5F, 0x30, 0x31, 0x5F, 0x30, 0x31,0x00
//                0x07, 0x71
        };

        // 保持 Packet id 处于递增状态
        String body = CRC16.getBufHexStr(input);
        String crcString = CRCUtil.getCRC16CCITT(body,0x1021,0x0000,true);
        char[] chars = crcString.toCharArray();
        String crcHigh = String.valueOf(chars[2]) + String.valueOf(chars[3]);
        String crcLow = String.valueOf(chars[0]) + String.valueOf(chars[1]);

        // 第5步
        input = mergeBytes(
                input,
                MarshallProtocol.getInstance().hexStringToByteArray(crcHigh));
        // 第6步
        input = mergeBytes(
                input,
                MarshallProtocol.getInstance().hexStringToByteArray(crcLow));

        return input;
    }

    /**
     * 获取keep Alive命令
     * @return
     */
    public byte[] getKeepAliveCommand(){

        this._setForPeripheral();

        this.setFunctionCode(new byte[]{0x07});
        this.setData(null);

        return this.packetGenerator();
    }

    /**
     * 获取 使能 刷卡器的命令
     * @return
     */
    public byte[] getEnableReaderCommand(){
        // 0B 00 01 2F 01 53 00 30 80 14 01 CD 5D
        this._setForPeripheral();
        this.setFunctionCode(new byte[]{(byte) 0x80}); // 表示是MDB command
        final byte[] enableData = new byte[]{0x14,0x01};
        this.setData(enableData);
//        this.setPacketID(new byte[]{0x1D});

        return this.packetGenerator();
    }

    /**
     * 获取 使能 刷卡器的命令
     * @return
     */
    public byte[] getDisableReaderCommand(){
        // 0B 00 01 2F 01 53 00 30 80 14 01 CD 5D
        this._setForPeripheral();
        this.setFunctionCode(new byte[]{(byte) 0x80}); // 表示是MDB command
        final byte[] enableData = new byte[]{0x14,0x00}; // 表示禁能读卡器
        this.setData(enableData);
//        this.setPacketID(new byte[]{0x1D});

        return this.packetGenerator();
    }

    /**
     * 获取Vend Success命令
     * @return
     */
    public byte[] getVendSuccessCommand(){
        this._setForPeripheral();
        this.setFunctionCode(new byte[]{(byte) 0x80}); // 表示是MDB command
        this.setData(new byte[]{0x13,0x02,0x01,0x00});
        return this.packetGenerator();
    }

    /**
     * 获取 Session Complete 命令
     * @return
     */
    public byte[] getSessionCompleteCommand(){
        this._setForPeripheral();
        this.setFunctionCode(new byte[]{(byte) 0x80}); // 表示是MDB command
        this.setData(new byte[]{0x13,0x04});
        return this.packetGenerator();
    }

    /**
     * 获取 transfer data命令， 发送订单的数据给 amit
     * @return
     */
    public byte[] getTransferTransactionCommand(){
        this._setForPeripheral();
        this.setFunctionCode(new byte[]{0x0A}); // 表示是 transfer Data
        byte[] params = new byte[22];
        params[0] = 0x01;
        params[1] = 0x14;
        params[2] = 0x02;
        for (int i = 0; i < 19; i++) {
            params[i+3] = 0x00;
        }
        this.setData(params);
        return this.packetGenerator();
    }


    /**
     * 获取 Vend Request 命令
     * @param price
     * @param quantity
     * @return
     */
    public byte[] getVendRequestCommand(int price, int quantity){
        this._setForPeripheral();
        this.setFunctionCode(new byte[]{(byte) 0x80}); // 表示是MDB command

        final byte[] mdbCommandType = new byte[]{0x13, 0x00};   // 表示是MDB command type : vend request
        final byte[] priceData = this.intToPacketLength(price*quantity);
//        final byte[] priceData = new byte[]{0x0C,0x00};
        final byte[] quantityData = this.intToPacketLength(quantity*2);
//        final byte[] quantityData = new byte[]{0x02,0x00};

        byte[] data = mergeBytes(mdbCommandType,priceData);
        data = mergeBytes(data,quantityData);

        this.setData(data);

        return this.packetGenerator();
    }

    /**
     * 获取发送 ACK 的命令， 这个命令不重置PacketID，而是来什么就返回什么
     * @param packetIdByte
     * @return
     */
    public byte[] getAckCommand(byte packetIdByte, boolean packedIdPlusOne){
        this.setPacketOptions(CommandHelper.hexStringToBytes("00"));
        this.setPacketID( new byte[]{packetIdByte} );

        // 发送 ACK, 但是 Keep Alive 的Packet ID一样要加1
        if(packedIdPlusOne){
            this.packetIdCounter++;
        }

        this.setSource(CommandHelper.hexStringToBytes("01"));
        this.setSourceLSB(CommandHelper.hexStringToBytes("53"));

        this.setDestination(new byte[]{AMIT_DESTINATION});
        this.setDestinationLSB(new byte[]{AMIT_DESTINATION_LSB});
        this.setFunctionCode(new byte[]{0x00});
        this.setData(new byte[]{0x00});
        return this.packetGenerator();
    }



    private void _setForPeripheral(){
        this.setPacketOptions(CommandHelper.hexStringToBytes("01"));
//        this.setPacketID(CommandHelper.hexStringToBytes("01")); // Todo 这里的值，在实际中似乎是个自增的整数

        // PacketId 在这里的值，在实际中似乎是个自增的整数
        if(this.packetIdCounter < this.maxKeepAlivePacketId){
            this.packetIdCounter++;
        }else {
            this.packetIdCounter = 0;
        }

//        String s = String.valueOf(this.packetIdCounter);
        this.setPacketID(new byte[]{(byte)this.packetIdCounter});

        this.setSource(CommandHelper.hexStringToBytes("01"));
        this.setSourceLSB(CommandHelper.hexStringToBytes("53"));

        //在获取config数据的时候， 实际上会得到这两个值。所有发往amit的数据， destination都要这么设置
        this.setDestination(new byte[]{AMIT_DESTINATION});
        this.setDestinationLSB(new byte[]{AMIT_DESTINATION_LSB});
    }

    /**
     * 依据传入的Data, 合成一个 Marshall 协议支持的Packet
     * @return
     */
    public byte[] packetGenerator(){
        byte[] tmp1 = mergeBytes(this.packetOptions,this.packetID);
        byte[] tmp2 = mergeBytes(tmp1,this.source);
        byte[] tmp3 = mergeBytes(tmp2,this.sourceLSB);
        byte[] tmp4 = mergeBytes(tmp3,this.destination);
        byte[] tmp5 = mergeBytes(tmp4,this.destinationLSB);
        byte[] packetHeader = mergeBytes(tmp5,this.functionCode);
//        byte[] body;
//
//        if(data != null && data.length>0){
//            byte[] tmp6 = mergeBytes(packetHeader,data);
//            body = mergeBytes(tmp6,this.CRCField);
//        }else {
//            body = mergeBytes(packetHeader, this.CRCField);
//        }

        byte[] body;

        if(data != null && data.length>0){
            body = mergeBytes(packetHeader,data);
//            body = mergeBytes(tmp6,this.CRCField);
        }else {
            body = packetHeader;
        }

        /**
         * 生成 packet 字节数组的方式
         * 1: 先生成头部的2字节的 packet length: 值为 body的长度 + 2
         * 2: 组合 packet Length 和 body 成为新的body
         * 3: 根据新的body 计算 crc 字符串, 算法是 ccitt 方式
         * 4: crc字符串的 高低换位
         * 5: 合并新body 和 crc 字符高位
         * 6: 合并新body 和 crc 字符低位
         * 7: 返回新body
         */

        // 第一步
        this.packetLength = intToPacketLength(body.length + 2);  // 需要把crc的长度也计算进去
        // 第2步
        body = mergeBytes(this.packetLength,body);
        // 第3步
        String bodyString = CRC16.getBufHexStr(body); // 把长度和packet的内容合成一个字节数组之后再计算CRC
        String crcString = CRCUtil.getCRC16CCITT(bodyString,0x1021,0x0000,true);

        // 第4步
        char[] chars = crcString.toCharArray();
        String crcHigh = String.valueOf(chars[2]) + String.valueOf(chars[3]);
        String crcLow = String.valueOf(chars[0]) + String.valueOf(chars[1]);

        // 第5步
        body = mergeBytes(body,this.hexStringToByteArray(crcHigh));
        // 第6步
        body = mergeBytes(body,this.hexStringToByteArray(crcLow));

        // 第7步
        return body;
    }

    /**
     * 把 hex 的字符串转换成字符数组
     * @param s
     * @return
     */
    public byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }

        return data;
    }

    /**
     * 合并两个数组, 然后返回新的合并后的数组
     * @param data1
     * @param data2
     * @return
     */
    public byte[] mergeBytes(byte[] data1, byte[] data2){
        byte[] data3 = new byte[data1.length + data2.length];
        System.arraycopy(data1, 0, data3, 0, data1.length);
        System.arraycopy(data2, 0, data3, data1.length, data2.length);
        return data3;
    }

    /**
     * 将int 转2位 byte数组
     * @param iSource
     * @return
     */
    byte[] intToPacketLength(int iSource) {
//        final String lenHigh = Integer.toHexString(iSource);
//        final byte[] lenLow = new byte[]{0x00};
//        byte[] lenHighByte = hexStringToByteArray(lenHigh);
//        byte[] result = mergeBytes(lenHighByte, lenLow);
//        return result;

        return
                new byte[]{
                        (byte) (iSource & 0xFF),
                        (byte) ((iSource >> 8) & 0xFF),
                };
    }

    /**
     * 将int 转2位 byte数组
     * @param iSource
     * @return
     */
    byte[] intToBytes(int iSource) {
        return
                new byte[]{
                        (byte) ((iSource >> 8) & 0xFF),
                        (byte) (iSource & 0xFF)
                };
    }

    @Override
    public String toString() {
        return "MarshallProtocol{" +
                "packetLength=" + Arrays.toString(packetLength) +
                ", packetOptions=" + Arrays.toString(packetOptions) +
                ", packetID=" + Arrays.toString(packetID) +
                ", source=" + Arrays.toString(source) +
                ", sourceLSB=" + Arrays.toString(sourceLSB) +
                ", destination=" + Arrays.toString(destination) +
                ", destinationLSB=" + Arrays.toString(destinationLSB) +
                ", functionCode=" + Arrays.toString(functionCode) +
                ", data=" + Arrays.toString(data) +
                ", CRCField=" + Arrays.toString(CRCField) +
//                ", isParsedOK=" + isParsedOK() +
                '}';
    }

    public static String byteArrToString(byte[] arr, int offset, int max_len)
    {
        int str_length = max_len;

        for (int i = offset; i < offset + str_length; i++)
        {
            if (arr[i] == 0 || arr[i] == -1)
            {
                str_length = i;
                break;
            }
        }
        return new String(arr, offset, str_length);
    }

    /**
     * 字符数组转hex demical的方法
     * @param arr
     * @return
     */
    public static String byteArrayToHexString(byte[] arr){
        StringBuilder sb = new StringBuilder(arr.length *2);
        sb.append("[");
        for (byte b : arr){
            int bi = 0xFF & b;
            String toAppend = Integer.toHexString(bi);
            sb.append(toAppend).append(",");
        }
        sb.setLength(sb.length() -1);
        return sb.append("]").toString().toUpperCase();
    }

    public byte[] getPacketLength() {
        return packetLength;
    }

    public void setPacketLength(byte[] packetLength) {
        this.packetLength = packetLength;
    }

    public byte[] getPacketOptions() {
        return packetOptions;
    }

    public void setPacketOptions(byte[] packetOptions) {
        this.packetOptions = packetOptions;
    }

    public byte[] getPacketID() {
        return packetID;
    }

    public void setPacketID(byte[] packetID) {
        this.packetID = packetID;
    }

    public byte[] getSource() {
        return source;
    }

    public void setSource(byte[] source) {
        this.source = source;
    }

    public byte[] getSourceLSB() {
        return sourceLSB;
    }

    public void setSourceLSB(byte[] sourceLSB) {
        this.sourceLSB = sourceLSB;
    }

    public byte[] getDestination() {
        return destination;
    }

    public void setDestination(byte[] destination) {
        this.destination = destination;
    }

    public byte[] getDestinationLSB() {
        return destinationLSB;
    }

    public void setDestinationLSB(byte[] destinationLSB) {
        this.destinationLSB = destinationLSB;
    }

    public byte[] getFunctionCode() {
        return functionCode;
    }

    public void setFunctionCode(byte[] functionCode) {
        this.functionCode = functionCode;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
