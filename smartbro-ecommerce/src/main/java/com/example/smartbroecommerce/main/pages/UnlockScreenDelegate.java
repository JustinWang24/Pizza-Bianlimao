package com.example.smartbroecommerce.main.pages;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.example.smartbro.app.AccountManager;
import com.example.smartbro.delegates.SmartbroDelegate;
import com.example.smartbro.net.RestfulClient;
import com.example.smartbro.net.callback.IError;
import com.example.smartbro.net.callback.IFailure;
import com.example.smartbro.net.callback.ISuccess;
import com.example.smartbro.utils.timer.BaseTimerTask;
import com.example.smartbro.utils.timer.ITimerListener;
import com.example.smartbroecommerce.R;
import com.example.smartbroecommerce.R2;
import com.example.smartbroecommerce.main.product.ListDelegate;
import com.example.smartbroecommerce.main.stock.StockManagerDelegate;
import com.example.smartbroecommerce.utils.BetterToast;
import com.taihua.pishamachine.DoorManager;

import java.util.Date;
import java.util.Timer;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Justin Wang from SmartBro on 7/1/18.
 */

public class UnlockScreenDelegate extends SmartbroDelegate implements ITimerListener {

    private Timer timer = null;
    private long openTime = 0;

    /**
     * 保存的真实密码
     */
    private String realPassword = "";

//    @BindView(R2.id.slide_to_unlock)
//    CustomSlideToUnlockView customSlideToUnlockView = null;
    @BindView(R2.id.et_delivery_code)
    TextView password = null;
    @BindView(R2.id.btn_key0)
    Button btnKey0 = null;
    @BindView(R2.id.btn_key1)
    Button btnKey1 = null;
    @BindView(R2.id.btn_key2)
    Button btnKey2 = null;
    @BindView(R2.id.btn_key3)
    Button btnKey3 = null;
    @BindView(R2.id.btn_key4)
    Button btnKey4 = null;
    @BindView(R2.id.btn_key5)
    Button btnKey5 = null;
    @BindView(R2.id.btn_key6)
    Button btnKey6 = null;
    @BindView(R2.id.btn_key7)
    Button btnKey7 = null;
    @BindView(R2.id.btn_key8)
    Button btnKey8 = null;
    @BindView(R2.id.btn_key9)
    Button btnKey9 = null;

    @BindView(R2.id.btn_key_confirm)
    Button btnKeyConfirm = null;
    @BindView(R2.id.btn_key_delete)
    Button btnKeyDelete = null;

    @BindView(R2.id.btn_key_cancel_delivery)
    Button btnKeyCancel = null;

    @OnClick(R2.id.btn_key_cancel_delivery)
    void onCancelClicked(){
        startWithPop(new ListDelegate());
    }

    @OnClick(R2.id.btn_key_delete)
    void onDeleteClick(){
        String code = this.password.getText().toString();
        if(code.length()>0){
            if(code.length() == 1){
                this.password.setText("");
                this.realPassword = "";
            }else {
                final String sub = code.substring(0,code.length()-1);
                this.password.setText(sub);

                this.realPassword = this.realPassword.substring(0,code.length() - 1);
            }
        }
    }

    @OnClick(R2.id.btn_key_confirm)
    void confirmBtnClicked(){
        if(this.realPassword.length() == 0){
            return;
        }

        RestfulClient.builder()
            .url("compare_machine_password")
            .params("password", this.realPassword)
//            .params("password",password.getText().toString())
            .params("muuid", String.valueOf(AccountManager.getMachineId()))
            .success(new ISuccess() {
                @Override
                public void onSuccess(String response) {
                    final int errorNumber =
                            JSON.parseObject(response).getInteger("error_no");

                    if(errorNumber == RestfulClient.NO_ERROR){
                        final DoorManager doorManager = DoorManager.getInstance();
                        doorManager.unlockDoor();
                        startWithPop(new StockManagerDelegate());
                    }else {
                        BetterToast.getInstance().showText(_mActivity, getString(R.string.err_text_password_not_match));
                    }
                }
            })
            .error(new IError() {
                @Override
                public void onError(int code, String msg) {
                }
            })
            .failure(new IFailure() {
                @Override
                public void onFailure() {
                }
            })
            .build()
            .post();
    }

    @Override
    public Object setLayout() {
        return R.layout.delegate_unlock;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        // 只有1分钟操作时间
//        CustomSlideToUnlockView.CallBack callBack = new CustomSlideToUnlockView.CallBack(){
//            @Override
//            public void onSlide(int distance) {
////                tv_text.setText("slide distance:"+distance);
//            }
//
//            @Override
//            public void onUnlocked() {
//                RestfulClient.builder()
//                        .url("compare_machine_password")
//                        .params("password",password.getText().toString())
//                        .params("muuid", String.valueOf(AccountManager.getMachineId()))
//                        .success(new ISuccess() {
//                            @Override
//                            public void onSuccess(String response) {
//                                customSlideToUnlockView.resetView();
//                                final int errorNumber =
//                                        JSON.parseObject(response).getInteger("error_no");
//
//                                if(errorNumber == RestfulClient.NO_ERROR){
//                                    final DoorManager doorManager = DoorManager.getInstance();
//                                    doorManager.unlockDoor();
//                                    startWithPop(new StockManagerDelegate());
//                                }else {
//                                    BetterToast.getInstance().showText(_mActivity, getString(R.string.err_text_password_not_match));
//                                }
//                            }
//                        })
//                        .error(new IError() {
//                            @Override
//                            public void onError(int code, String msg) {
//                                customSlideToUnlockView.resetView();
//                            }
//                        })
//                        .failure(new IFailure() {
//                            @Override
//                            public void onFailure() {
//                                customSlideToUnlockView.resetView();
//                            }
//                        })
//                        .build()
//                        .post();
//            }
//        };
//
//        this.customSlideToUnlockView.setmCallBack(callBack);
    }

    @Override
    public void onResume() {
        super.onResume();

        this.openTime = new Date().getTime();

        final BaseTimerTask task = new BaseTimerTask(this);
        this.timer = new Timer(true);
        this.timer.schedule(task, 1000, 1000);
    }

    @Override
    public void onTimer() {
        getProxyActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final long now = new Date().getTime();
                if(now - openTime > 60000){
                    timer.cancel();
                    timer = null;
                    startWithPop(new ListDelegate());
                }
            }
        });
    }

    private void handleClick(Button button){
        final String code = this.password.getText().toString();
        this.password.setText(code + "*");

        // 保存真实输入的密码
        this.realPassword = this.realPassword + button.getText().toString();
    }

    @OnClick(R2.id.btn_key0)
    void onKey0Click(){
        this.handleClick(this.btnKey0);
    }
    @OnClick(R2.id.btn_key1)
    void onKey1Click(){
        this.handleClick(this.btnKey1);
    }
    @OnClick(R2.id.btn_key2)
    void onKey2Click(){
        this.handleClick(this.btnKey2);
    }
    @OnClick(R2.id.btn_key3)
    void onKey3Click(){
        this.handleClick(this.btnKey3);
    }
    @OnClick(R2.id.btn_key4)
    void onKey4Click(){
        this.handleClick(this.btnKey4);
    }
    @OnClick(R2.id.btn_key5)
    void onKey5Click(){
        this.handleClick(this.btnKey5);
    }
    @OnClick(R2.id.btn_key6)
    void onKey6Click(){
        this.handleClick(this.btnKey6);
    }
    @OnClick(R2.id.btn_key7)
    void onKey7Click(){
        this.handleClick(this.btnKey7);
    }
    @OnClick(R2.id.btn_key8)
    void onKey8Click(){
        this.handleClick(this.btnKey8);
    }
    @OnClick(R2.id.btn_key9)
    void onKey9Click(){
        this.handleClick(this.btnKey9);
    }
}
