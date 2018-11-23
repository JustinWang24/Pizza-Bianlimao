package com.example.smartbroecommerce.main.web;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.ContentFrameLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.example.smartbro.delegates.SmartbroDelegate;
import com.example.smartbro.delegates.web.WebDelegate;
import com.example.smartbro.delegates.web.WebDelegateImpl;
import com.example.smartbro.utils.timer.BaseTimerTask;
import com.example.smartbro.utils.timer.ITimerListener;
import com.example.smartbroecommerce.R;
import com.example.smartbroecommerce.R2;
import com.example.smartbroecommerce.database.MachineProfile;
import com.example.smartbroecommerce.main.product.ListDelegate;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Justin Wang from SmartBro on 14/12/17.
 * 显示帮助信息
 */

public class DiscoverDelegate extends SmartbroDelegate implements ITimerListener{

    @BindView(R2.id.toolbar_back_to_product_list)
    Toolbar backToProductListToolbar;
    @BindView(R2.id.web_discovery_container)
    ContentFrameLayout webFrame;

    private Timer timer;
    private BaseTimerTask timerTask;
    private long timeOnEnter;

    @OnClick(R2.id.toolbar_back_to_product_list)
    void onClickBack(){
        if(System.currentTimeMillis() - timeOnEnter > 2000){
            // 2秒后才能返回, 避免连续双击帮助按钮
            startWithPop(new ListDelegate());
        }
    }

    @OnClick(R2.id.web_discovery_container)
    void onClickWebFrame(){
//        startWithPop(new ListDelegate());
    }

    @Override
    public Object setLayout() {
        return R.layout.delegate_web_discover;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {

    }

    /**
     * 把WebView塞进去
     * @param savedInstanceState
     */
    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
        // 加载本地的page的方法, 在 assets 目录中
//        final WebDelegate delegate = WebDelegateImpl.create("index.html");

        // 加载远程的page
        final WebDelegate delegate =
                WebDelegateImpl.
                        create("http://boss.htlc-kj.com/help?machine=" + MachineProfile.getInstance().getUuid());
        loadRootFragment(R.id.web_discovery_container, delegate);


        // 开始计时
        this.timeOnEnter = System.currentTimeMillis();
        this.timer = new Timer(true);
        this.timerTask = new BaseTimerTask(this);
        this.timer.schedule(this.timerTask,1000, 1000);
    }

    @Override
    public void onTimer() {
        getProxyActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
            // 两分钟后自动跳回
            if(System.currentTimeMillis() - timeOnEnter > 120000){
                if(timer != null){
                    timer.cancel();
                    timer = null;
                }
                startWithPop(new ListDelegate());
            }
            }
        });
    }
}
