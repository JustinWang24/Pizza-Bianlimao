package com.example.th2.pizza.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.widget.Toast;

import com.example.smartbro.activities.ProxyActivity;
import com.example.smartbro.app.AccountManager;
import com.example.smartbro.delegates.SmartbroDelegate;
import com.example.smartbro.ui.launcher.ILauncherListener;
import com.example.smartbro.ui.launcher.OnLauncherFinishTag;
import com.example.smartbroecommerce.Auth.IAuthListener;
import com.example.smartbroecommerce.machine.InitDelegate;
import com.example.smartbroecommerce.main.pages.HippoStopWorkingDelegate;
import com.example.smartbroecommerce.main.stock.StockManagerDelegate;

/**
 * Created by Justin Wang from SmartBro on 3/12/17.
 * 这个是应用程序的唯一Activity
 */

public class ExampleActivity extends ProxyActivity implements IAuthListener, ILauncherListener{

    @Override
    public void onCreate(@Nullable Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        /*
         * 隐藏系统的任务条
         */
        final ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.hide();
        }

//        MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.rest);
//        mediaPlayer.start();

//        startService(new Intent(this, BasicService.class));
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Override
    public SmartbroDelegate setRootDelegate() {
        if(AccountManager.getSignState()){
            // 已经初始化了, 加载库存管理界面
            // 这个时候需要去服务器申请一下最新的数据
            StockManagerDelegate delegate = new StockManagerDelegate();
            Bundle args = new Bundle();
            args.putBoolean("refreshMachineData",true);
            delegate.setArguments(args);

            return delegate;
//            return new BottomDelegate();
        }else {
            // 加载初始化页面
            return new InitDelegate();
        }
    }

    @Override
    public void onError() {
        Toast.makeText(this, "初始化失败，请联系厂家", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSignInSuccess() {

    }

    /**
     * 当设备初始化第一次成功时
     */
    @Override
    public void onSignUpSuccess() {
        Toast.makeText(this, "Init Done", Toast.LENGTH_LONG).show();
    }

    /**
     * 判断设备已经处于初始化状态时, 应用程序被打开后的回调处理
     * @param tag 设备当前的状态
     */
    @Override
    public void onLaunchFinish(OnLauncherFinishTag tag) {
        switch (tag){
            case INITED:
                // 加载应用程序首页
                StockManagerDelegate delegate = new StockManagerDelegate();
                Bundle args = new Bundle();
                args.putBoolean("refreshMachineData",false);
                delegate.setArguments(args);
                startWithPop(delegate);
                break;
            case NOT_INITED:
                startWithPop(new HippoStopWorkingDelegate());
                break;
            default:
                break;
        }
    }
}
