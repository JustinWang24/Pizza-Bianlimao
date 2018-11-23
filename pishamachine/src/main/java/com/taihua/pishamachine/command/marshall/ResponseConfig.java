package com.taihua.pishamachine.command.marshall;

/**
 * Created by Justin Wang from SmartBro on 5/1/18.
 * FirmwareInfo的响应结果
 */

public class ResponseConfig {
    private static ResponseConfig INSTANCE = null;

    private ResponseConfig(){
    }

    public static ResponseConfig getInstance(){
        if(INSTANCE == null){
            INSTANCE = new ResponseConfig();
        }
        return INSTANCE;
    }

    public  ResponseConfig parse(byte[] response){

        return INSTANCE;
    }
}
