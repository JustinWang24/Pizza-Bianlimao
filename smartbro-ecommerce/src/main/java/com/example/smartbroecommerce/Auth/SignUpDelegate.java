package com.example.smartbroecommerce.Auth;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.smartbro.delegates.SmartbroDelegate;
import com.example.smartbro.net.RestfulClient;
import com.example.smartbro.net.callback.IFailure;
import com.example.smartbro.net.callback.ISuccess;
import com.example.smartbro.utils.validators.FormValidator;
import com.example.smartbroecommerce.R;
import com.example.smartbroecommerce.R2;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Justin Wang from SmartBro on 4/12/17.
 * 注册的逻辑
 */

public class SignUpDelegate extends SmartbroDelegate implements FormValidator {


    @BindView(R2.id.edit_sign_up_name)
    TextInputEditText mName = null;

    @BindView(R2.id.edit_sign_up_email)
    TextInputEditText mEmail = null;

    @BindView(R2.id.edit_sign_up_phone)
    TextInputEditText mPhone = null;

    @BindView(R2.id.edit_sign_up_password)
    TextInputEditText mPassword = null;

    @BindView(R2.id.edit_sign_up_password_confirm)
    TextInputEditText mPasswordConfirm = null;

    private IAuthListener iAuthListener = null;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        if(activity instanceof IAuthListener){
            this.iAuthListener = (IAuthListener) activity;
        }
    }

    /**
     * 当注册按钮被点击时
     */
    @OnClick(R2.id.btn_sign_up)
    void onClickSignUp(){

        // 验证表单
        if(this.validate()){
            // todo 向服务器提供信息
            RestfulClient.builder()
                    .url("machines/init")
                    .params("name",this.mName.getText().toString())
                    .success(new ISuccess() {
                        @Override
                        public void onSuccess(String response) {
                            // Todo 注册成功的处理
                            MachineInitHandler.onInitDone(response,iAuthListener);
                        }
                    })
                    .failure(new IFailure() {
                        @Override
                        public void onFailure() {
                            // Todo 注册失败的处理
                        }
                    })
                    .build()
                    .post();
        }
        Toast.makeText(getContext(), "Sign up Success", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R2.id.tv_link_sign_in)
    void onSignInLinkClick(){
        // 当用户有账户，点击sign in 链接的时候
        getSupportDelegate().start(new SignInDelegate());
    }

    @Override
    public Object setLayout() {

        return R.layout.delegate_sign_up;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {

    }

    /**
     * 表单的验证
     *
     * @return boolean
     */
    @Override
    public boolean validate() {
        final String name = this.mName.getText().toString();
        final String email = this.mEmail.getText().toString();
        final String phone = this.mPhone.getText().toString();
        final String password = this.mPassword.getText().toString();
        final String passwordConfirm = this.mPasswordConfirm.getText().toString();

        boolean validated = true;

        if (name.isEmpty()) {
            this.mName.setError(getString(R.string.err_text_name));
            validated = false;
        } else {
            this.mName.setError(null);
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            this.mEmail.setError(getString(R.string.err_text_email));
            validated = false;
        } else {
            this.mEmail.setError(null);
        }

        if (phone.isEmpty() || phone.length() < 5) {
            this.mPhone.setError(getString(R.string.err_text_phone));
            validated = false;
        } else {
            this.mPhone.setError(null);
        }

        if (password.isEmpty()) {
            this.mPassword.setError(getString(R.string.err_text_password));
            validated = false;
        } else {
            this.mPassword.setError(null);
        }

        if (!password.equals(passwordConfirm)) {
            this.mPasswordConfirm.setError(getString(R.string.err_text_password_not_match));
            validated = false;
        } else {
            this.mPasswordConfirm.setError(null);
        }

        return validated;
    }
}
