package com.taihua.pishamachine.MicroLightScanner;

import com.taihua.pishamachine.MicroLightScanner.ParserImpl.QrCodeParserImpl;
import com.taihua.pishamachine.MicroLightScanner.ParserImpl.ReportModeParserImpl;
import com.taihua.pishamachine.MicroLightScanner.ParserImpl.ScannerControlParserImpl;
import com.taihua.pishamachine.SerialPortHelper;
import com.taihua.pishamachine.command.CommandHelper;

/**
 * Created by Justin Wang from SmartBro on 29/12/18.
 */
public class Tx200Client {
    /**
     * 扫描枪串口的配置
     */
    private static final int RS232_FLAG = 1;
    private static final int RS232_RATE = 115200;
    private static final char RS232_N_EVENT = 'N';
    private static final String RS232_PORT = "/dev/ttymxc3";

    private static Tx200Client CLIENT_INSTANCE = null;
    private SerialPortHelper serialPortHelper;

    /**
     * 私有构造函数，实现简单单例模式
     * @param path
     * @param rate
     */
    private Tx200Client(String path, int rate){
        this.serialPortHelper = new SerialPortHelper(
                path,
                rate,
                RS232_FLAG,
                RS232_N_EVENT
        );
        this.serialPortHelper.init();
    }

    /**
     * 获取扫描枪客户端类实例对象的方法
     * @return Tx200Client
     */
    public static Tx200Client getClientInstance(){
        if(CLIENT_INSTANCE == null){
            CLIENT_INSTANCE = new Tx200Client(RS232_PORT,RS232_RATE);
        }
        return CLIENT_INSTANCE;
    }

    /**
     * 连接扫码枪
     * @param passiveMode
     * @return
     */
    public CommandExecuteResult connect(boolean passiveMode){
        final byte[] command;                   // 需要发送的命令
        final byte[] resultBuffer = new byte[20]; // 收信的字节缓冲区

        if(passiveMode){
            // 命令模式
            command = ScannerCommand.GetSetPassiveModeCmd();
        }else {
            // 主动上报模式
            command = ScannerCommand.GetSetPositiveModeCmd();
        }

        // 发送命令
        final int readSize = this.serialPortHelper.sentData(command, resultBuffer, 200);
        return new CommandExecuteResult(readSize, resultBuffer, new ReportModeParserImpl());
    }

    /**
     * 扫描二维码 返回扫描到的二维码
     * @return String
     */
    public CommandExecuteResult scan(){
        final byte[] command = ScannerCommand.GetReadQrCodeCommand();
        final byte[] resultBuffer = new byte[100]; // 收信的字节缓冲区
        final int readSize = this.serialPortHelper.sentData(command, resultBuffer, 300);
        return new CommandExecuteResult(readSize, resultBuffer, new QrCodeParserImpl());
    }

    /**
     * 清空已经扫到的二维码的值，相当于断开扫码枪的连接
     * @return CommandExecuteResult
     */
    public CommandExecuteResult disconnect(){
        final byte[] command = ScannerCommand.GetClearCodeCmd();
        final byte[] resultBuffer = new byte[20]; // 收信的字节缓冲区
        final int readSize = this.serialPortHelper.sentData(command, resultBuffer, 200);
        return new CommandExecuteResult(readSize, resultBuffer, new ScannerControlParserImpl());
    }

    /**
     * 关闭串口
     */
    private void _terminateSerialPort(){
        this.serialPortHelper.close();
        this.serialPortHelper = null;
        CLIENT_INSTANCE = null;
    }
}
