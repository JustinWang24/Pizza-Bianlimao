package com.taihua.pishamachine.command;

/**
 * Created by Justin Wang from SmartBro on 5/1/18.
 * 通讯协议的基类
 */

public abstract class Packet {
    // 协议包类的名字: 对应的可能是Marshall，或者MDB协议
    private PacketType tag;

    // 结果是否被成功的解析了
    private boolean isParsedOK = false;

    /**
     * 解析结果用的方法
     * @param receivedBuffer
     * @param packetSent
     */
    public abstract void parseResponse(byte[] receivedBuffer, Packet packetSent);

    /**
     * 获取Packet对象的工程方法
     * @return
     */
    public abstract Packet build();

    /**
     * 生成字节数组以便发送的方法
     * @return 带CRC的字节数据
     */
    public abstract byte[] packetGenerator();

    public Packet(PacketType tag){
        this.tag = tag;
    }

    public PacketType getTag() {
        return tag;
    }

    public boolean isParsedOK() {
        return isParsedOK;
    }

    public void setParsedOK(boolean parsedOK) {
        isParsedOK = parsedOK;
    }
}
