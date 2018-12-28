package com.example.smartbroecommerce.main.pages;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.smartbro.delegates.SmartbroDelegate;
import com.example.smartbro.utils.timer.BaseTimerTask;
import com.example.smartbro.utils.timer.ITimerListener;
import com.example.smartbroecommerce.R;
import com.example.smartbroecommerce.R2;
import com.example.smartbroecommerce.main.product.ListDelegate;

import java.util.Date;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Justin Wang from SmartBro on 10/1/18.
 * 取货码页面
 */

public class DeliveryCodeDelegate extends SmartbroDelegate implements ITimerListener {

    @BindView(R2.id.et_delivery_code)
    TextView deliveryCode = null;
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

    private Timer timer = null;
    private long openTime = 0;


    @OnClick(R2.id.btn_key_confirm)
    void onConfirmClick(){
        startWithPop(new DeliveryCodeSuccessDelegate());
    }

    @OnClick(R2.id.btn_key_cancel_delivery)
    void onCancelClick(){
        startWithPop(new ListDelegate());
    }

    @OnClick(R2.id.btn_key_delete)
    void onDeleteClick(){
        String code = this.deliveryCode.getText().toString();
        if(code.length()>0){
            if(code.length() == 1){
                this.deliveryCode.setText("");
            }else {
                final String sub = code.substring(0,code.length()-1);
                this.deliveryCode.setText(sub);
            }
        }
    }

    private void handleClick(Button button){
        final String code = this.deliveryCode.getText().toString();
        this.deliveryCode.setText(code + button.getText().toString());
    }

    @Override
    public Object setLayout() {
        return R.layout.delegate_delivery_code_new;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {

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
