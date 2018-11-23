package com.taihua.pishamachine.command.marshall;

import com.taihua.pishamachine.command.Packet;

/**
 * Created by Justin Wang from SmartBro on 5/1/18.
 * Contains specific FW details of Peripheral. 
 * Sent at Power Up / Reset.
 */

public class PacketFirmwareInfo {

    private byte[] protocolVersion = new byte[]{0x00, 0x0B};
    private byte[] peripheralType  = new byte[]{0x04};      // 类型是2: Card Reader
    private byte[] peripheralSubType  = new byte[]{0x01};      // 这个其实已经没有用了
    private byte[] peripheralCapabilities   = new byte[]{0x00,0x00};      //

//    @Override
//    public void parseResponse(byte[] receivedBuffer, Packet packetSent) {
//        ResponseConfig.getInstance().parse(receivedBuffer);
//    }
//
//    @Override
//    public MarshallProtocol build() {
//        final byte[] functionCode = new byte[]{0x05};
//        this.setFunctionCode(functionCode);
//
//        this.data = mergeBytes(this.protocolVersion,this.peripheralType);
//        this.data = mergeBytes(this.data, this.peripheralSubType);
//        this.data = mergeBytes(this.data,this.peripheralCapabilities);
//
//        return this;
//    }
    
}
