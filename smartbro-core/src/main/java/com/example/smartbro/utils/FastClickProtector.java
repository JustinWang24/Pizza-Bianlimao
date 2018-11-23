package com.example.smartbro.utils;

/**
 * Created by Justin Wang from SmartBro on 5/2/18.
 * 防止被快速连续点击的工具类
 */

public class FastClickProtector {
    private static long lastClickTime;

    public static boolean isFastDoubleClick(){
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if(0<timeD && timeD < 1000){
            return true;
        }
        lastClickTime = time;
        return false;
    }
}
