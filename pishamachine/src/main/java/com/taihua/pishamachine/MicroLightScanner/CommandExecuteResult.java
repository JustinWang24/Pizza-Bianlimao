package com.taihua.pishamachine.MicroLightScanner;

import com.taihua.pishamachine.MicroLightScanner.ParserImpl.QrCodeParserImpl;

/**
 * 这个类是装载扫描枪的串口命令执行结果的类
 */
public class CommandExecuteResult {
    public static final String OK = "OK";
    public static final String NOT_OK = "NOK";
    public static final String KEEP_WAITING = "KEEP_WAITING";

    /**
     * 返回命令执行结果的长度
     */
    private int size;

    /**
     * 返回命令执行结果的缓冲区
     */
    private byte[] resultBuffer;
    private IResultParser parser;

    /**
     * 构造函数
     * @param size
     * @param resultBuffer
     */
    public CommandExecuteResult(int size, byte[] resultBuffer, IResultParser parser){
        this.size = size;
        this.resultBuffer = resultBuffer;
        this.parser = parser;
    }

    /**
     * 返回串口命令执行基本结果的方法， 首先是返回的读取size必须大于零
     * @return boolean
     */
    public String getResult(){
        if(this.parser instanceof QrCodeParserImpl){
            // 如果是扫描二维码的命令执行结果
            return this.parser.go(this.resultBuffer);
        }else{
            return this.size > 0 ? OK : NOT_OK;
        }
    }
}
