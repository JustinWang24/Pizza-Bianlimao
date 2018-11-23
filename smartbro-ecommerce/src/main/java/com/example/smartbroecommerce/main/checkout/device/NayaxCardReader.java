package com.example.smartbroecommerce.main.checkout.device;

import android.util.Log;

import com.example.smartbroecommerce.main.checkout.device.TheReader;
import com.taihua.pishamachine.LogUtil;
import com.taihua.pishamachine.SerialPortHelper;
import com.taihua.pishamachine.command.marshall.MarshallProtocol;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Justin Wang from SmartBro on 4/2/18.
 */

public class NayaxCardReader extends TheReader {
    // 硬件相关
    static SerialPortHelper serialPortHelper = null;

    public NayaxCardReader(){

    }

    @Override
    int writeSerialPort(byte[] input, byte[] response, int timeout) {
        final int length =  serialPortHelper.sentData(input,response,timeout);
        this._printLog("Send - Rev: " + Integer.toString(length),input,response);
        return length;
    }

    @Override
    int readSerialPort() {
        final byte[] resp = new byte[70];
        try {
            final int dataLength = serialPortHelper.readInputStreamWithTimeout(resp,50);
            return MarshallPacketParser.getInstanceOnly().parse(resp, dataLength);
        } catch (IOException e) {
            LogUtil.LogInfo("初始化串口失败");
        }
        return -1;
    }

    @Override
    public TheReader getInstance() {
        if(theReader == null){
            theReader = new NayaxCardReader();
        }
        return theReader;
    }

    @Override
    void openSerialPort() {
        if(TESTING_MODE){
            Log.i("Info","初始化串口");
            return;
        }
        if(serialPortHelper == null){
            serialPortHelper = new SerialPortHelper(
                    "/dev/ttymxc1",     // 读三号串口
                    115200,           // 速率
                    0,
                    'N'
            );

            // 设置串口读取的时延为10毫秒
            serialPortHelper.init(10);
        }
    }

    @Override
    void closeSerialPort() {
        if(TESTING_MODE){
            Log.i("Info","关闭串口");
            return;
        }
        serialPortHelper.close();
    }

    private void _printLog(String processName, byte[] input, byte[] output){
        if(true){
            String dateString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(new Date());
            LogUtil.LogInfo(processName + ": " + dateString);
            if(input != null){
                LogUtil.LogInfo("Snd: " + MarshallProtocol.byteArrayToHexString(input));
            }

            if(output != null){
                LogUtil.LogInfo("Rvd: " + MarshallProtocol.byteArrayToHexString(output));
            }
            if(input == null && output == null){
                LogUtil.LogInfo("\n");
            }
        }else{
            Log.i("Info Parse", processName);
        }
    }
}
