package com.example.smartbro.app;

/**
 * Created by Justin Wang from SmartBro on 7/12/17.
 * 这个接口定义了如何检查当前程序的登录状态
 */

public interface IUserChecker {

    // 当登录信息被检测到的时候的回调函数
    void onSignIn();

    // 当检测到没有登录时的回调函数
    void onNotSign();
}
