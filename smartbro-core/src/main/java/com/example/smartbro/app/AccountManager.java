package com.example.smartbro.app;

import com.example.smartbro.utils.storage.LattePreference;

/**
 * Created by Justin Wang from SmartBro on 7/12/17.
 */

public class AccountManager {

    // 是否登录的标记
    private enum SignTag {
        SIGN_TAG,
        MACHINE_ID
    }

    /**
     * 设定是否登录标记位的方法 登录后调用
     * @param state 状态
     */
    public static void setSignState(boolean state){
        LattePreference.setAppFlag(SignTag.SIGN_TAG.name(), state);
    }

    /**
     * 保存设备的ID
     * @param machineId
     */
    public static void setMachineId(long machineId){
        LattePreference.setMachineId(SignTag.MACHINE_ID.name(), machineId);
    }

    /**
     * 获取设备的ID
     * @return long
     */
    public static long getMachineId(){
        return LattePreference.getMachineId(SignTag.MACHINE_ID.name());
    }

    /**
     * 获取是否登录状态的方法
     * @return boolean
     */
    public static boolean getSignState(){
        return LattePreference.getAppFlag(SignTag.SIGN_TAG.name());
    }

    /**
     * 检查是否登录标志位
     * @return boolean
     */
    private static boolean isSignIn(){
        return LattePreference.getAppFlag(SignTag.SIGN_TAG.name());
    }

    /**
     * 检查是否登录的方法
     * @param checker IUserChecker的实现类对象
     */
    public static void checkAccount(IUserChecker checker){
        if(isSignIn()){
            checker.onSignIn();
        }else {
            checker.onNotSign();
        }
    }
}
