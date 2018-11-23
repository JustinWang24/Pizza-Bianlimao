package com.example.smartbroecommerce.Auth;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.util.Patterns;
import android.view.View;

import com.example.smartbro.delegates.SmartbroDelegate;
import com.example.smartbro.utils.validators.FormValidator;
import com.example.smartbroecommerce.R;
import com.example.smartbroecommerce.R2;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Justin Wang from SmartBro on 5/12/17.
 * 用户登录用的 Delegate
 */

public class SignInDelegate extends SmartbroDelegate implements FormValidator{

    @BindView(R2.id.edit_sign_in_email)
    TextInputEditText mEmail = null;

    @BindView(R2.id.edit_sign_in_password)
    TextInputEditText mPassword = null;

    @OnClick(R2.id.btn_sign_in)
    void onSignInClick(){
        if (this.validate()){
            // Todo 用户登录的实际处理
        }
    }

    @OnClick(R2.id.icon_sign_in_we_chat)
    void onWeChatIconClick(){
        // Todo 微信登录的处理
    }

    @OnClick(R2.id.tv_link_sign_up)
    void onSignUpLinkClick(){
        // 当用户点击没有账号，去注册一个时候，跳转到 sign up delegate
        start(new SignUpDelegate());
    }

    @Override
    public Object setLayout() {
        return R.layout.delegate_sign_in;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {

    }

    @Override
    public boolean validate() {
        final String email = this.mEmail.getText().toString();
        final String password = this.mPassword.getText().toString();

        boolean validated = true;

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            this.mEmail.setError(getString(R.string.err_text_email));
            validated = false;
        } else {
            this.mEmail.setError(null);
        }

        if (password.isEmpty()) {
            this.mPassword.setError(getString(R.string.err_text_password));
            validated = false;
        } else {
            this.mPassword.setError(null);
        }

        return validated;
    }
}
