package com.example.smartbroecommerce.Auth;

/**
 * Created by Justin Wang from SmartBro on 7/12/17.
 */

public interface IAuthListener {

    /**
     * 登录成功的回调
     */
    void onSignInSuccess();

    /**
     * 注册成功的回调
     */
    void onSignUpSuccess();
}
