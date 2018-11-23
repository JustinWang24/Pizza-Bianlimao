package com.taihua.pishamachine;

import android.util.Log;

import com.taihua.pishamachine.command.CommandSender;
import com.taihua.pishamachine.command.PlcDevice;

import java.util.Arrays;

/**
 * Created by Justin Wang from SmartBro on 10/1/18.
 * 和开门相关的
 */

public class DoorManager {
    private static DoorManager DOOR_MANAGER_INSTANCE = null;
    // 和串口相关
    private SerialPortHelper serialPortHelper = null;

    private DoorManager(String path, int rate){
        this.serialPortHelper = new SerialPortHelper(
                path, rate, 0, 'N'
        );
        this.serialPortHelper.init();
    }

    public static DoorManager getInstance(){
        if(DOOR_MANAGER_INSTANCE == null){
            DOOR_MANAGER_INSTANCE = new DoorManager("/dev/ttymxc2", 9600);
        }
        return DOOR_MANAGER_INSTANCE;
    }

    /**
     * 工厂方法, 获取实例
     * @param path
     * @param rate
     * @return
     */
    public static DoorManager getInstance(String path, int rate){
        if(DOOR_MANAGER_INSTANCE == null){
            DOOR_MANAGER_INSTANCE = new DoorManager(path, rate);
        }
        return DOOR_MANAGER_INSTANCE;
    }

    /**
     * 开门的方法
     */
    public void unlockDoor(){
        final int responseBufferSize = 20;
        final byte[] outBuffer = new byte[responseBufferSize];
        final int respSize = CommandSender.send(PlcDevice.getUnlockDoorCommand(), outBuffer, this.serialPortHelper);

        if(respSize > 0){
            try {
                Thread.sleep(2000);
                CommandSender.send(
                        PlcDevice.getUnlockDoorStep2Command(),
                        outBuffer,
                        this.serialPortHelper);
                Thread.sleep(2000);

                // 两秒后关闭串口
                this._terminateSerialPort();
            } catch (InterruptedException e) {
                LogUtil.LogInfo("第二条关门命令异常");
            }
        }
    }

    /**
     * 是否盒子已经就位的方法
     * @return
     */
    public boolean isPackpingBoxInPosition(){

        boolean result = true;
        final int responseBufferSize = 16;
        final byte[] outBuffer = new byte[responseBufferSize];

        try {
            Thread.sleep(100);
            final int readSize = CommandSender.send(PlcDevice.getReadErrorCode(), outBuffer, this.serialPortHelper);

            if(readSize > 0){
//                LogUtil.LogInfo("检测是否有盒子的方法读取返回值: " + Arrays.toString(outBuffer));
                Log.d("packpingBoxInPosition1",Arrays.toString(outBuffer));
                // 第 9 个byte如果是 0x00, 表示盒子正常. 因为返回值高位字节和地位自己是反着的
//                result = outBuffer[8] == 0x00;

                // 检查是否有 01 01 02 01 00 CRC
                result = this._match(outBuffer);

                Log.d("有盒子1",Boolean.toString(result));
            }

            if(!result){
                // 第二次检查
                Thread.sleep(100);
                final int readSize2 = CommandSender.send(PlcDevice.getReadErrorCode(), outBuffer, this.serialPortHelper);

                if(readSize2 > 0){
//                LogUtil.LogInfo("检测是否有盒子的方法读取返回值: " + Arrays.toString(outBuffer));
                    Log.d("packpingBoxInPosition2",Arrays.toString(outBuffer));
                    Log.d("有盒子2",Boolean.toString(result));
                    result = this._match(outBuffer);
                }

                if(!result){
                    // 第三次检查
                    Thread.sleep(100);
                    final int readSize3 = CommandSender.send(PlcDevice.getReadErrorCode(), outBuffer, this.serialPortHelper);

                    if(readSize3 > 0){
//                LogUtil.LogInfo("检测是否有盒子的方法读取返回值: " + Arrays.toString(outBuffer));
                        Log.d("packpingBoxInPosition3",Arrays.toString(outBuffer));
                        Log.d("有盒子3",Boolean.toString(result));
                        result = this._match(outBuffer);
                    }
                }
            }

            this._terminateSerialPort();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }


    /**
     * 关闭串口
     */
    private void _terminateSerialPort(){
        this.serialPortHelper.close();
        this.serialPortHelper = null;
        DOOR_MANAGER_INSTANCE = null;
    }

    /**
     * 检查传入的buffer中， 是否包含 01 01 02 01 00 这样的连续字符。 如果包含，表示盒子归位正常
     * @param buffer
     * @return
     */
    private boolean _match(byte[] buffer){
        boolean found = false;
        // 检查是否有 01 01 02 01 00 CRC
        final int max = buffer.length - 5;
        for (int idx = 0; idx < max; idx++){
            if(
                    buffer[idx] == 0x01 &&
                    buffer[idx+1] == 0x01 &&
                    buffer[idx+2] == 0x02 &&
                    buffer[idx+3] == 0x00 &&
                    buffer[idx+4] == 0x00
                ){
                found = true;
                break;
            }
        }
        return found;
    }
}
