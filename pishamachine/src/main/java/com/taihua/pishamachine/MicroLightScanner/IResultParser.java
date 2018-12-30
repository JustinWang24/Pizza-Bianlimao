package com.taihua.pishamachine.MicroLightScanner;

/**
 * Created by Justin Wang from SmartBro on 30/12/18.
 */
public interface IResultParser {
    /**
     * 获取解析结果的方法
     * @param resultBuffer 返回结果的字节数组
     * @return String
     */
    String go(byte[] resultBuffer);
}
