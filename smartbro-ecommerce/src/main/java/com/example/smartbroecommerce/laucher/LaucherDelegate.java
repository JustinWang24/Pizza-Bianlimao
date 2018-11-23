package com.example.smartbroecommerce.laucher;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;

import com.example.smartbro.delegates.SmartbroDelegate;
import com.example.smartbro.ui.launcher.ScrollLauncherTag;
import com.example.smartbro.utils.storage.LattePreference;
import com.example.smartbro.utils.timer.BaseTimerTask;
import com.example.smartbro.utils.timer.ITimerListener;
import com.example.smartbroecommerce.R;
import com.example.smartbroecommerce.R2;

import java.text.MessageFormat;
import java.util.Timer;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Justin Wang from SmartBro on 4/12/17.
 * 应用程序的启动页
 */

public class LaucherDelegate extends SmartbroDelegate implements ITimerListener {

    private Timer mTimer = null;
    private int mCount = 5;

    // 声明在当前 e-commerce 包中的资源的方式
    @BindView(R2.id.tv_launcher_timer)
    AppCompatTextView mTvTimer = null;

    @OnClick(R2.id.tv_launcher_timer)
    void onClickTimerView() {
        if (mCount < 0 && mTimer != null) {
            mTimer.cancel();
            mTimer = null;

            // timer结束了，检查是否展示 scroller 还是登录等...
            isScrollerLauncherShowedBefore();
        }
    }

    private void initTimer() {
        this.mTimer = new Timer();
        final BaseTimerTask baseTimerTask = new BaseTimerTask(this);
        mTimer.schedule(baseTimerTask, 0, 1000);
    }

    private void isScrollerLauncherShowedBefore() {
        if (!LattePreference.getAppFlag(ScrollLauncherTag.APP_ALREADY_LAUNCHED.name())) {
            // 如果没有标记过scroll launcher展示过，那么启动它
            start(new LauncherScrollDelegate(), SINGLETASK);
        } else {
            // Todo 展示过 scroller launcher 了， 那么检查是否用户已经登录
        }
    }

    @Override
    public Object setLayout() {
        return R.layout.delegate_laucher;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        initTimer();
    }

    @Override
    public void onTimer() {
        getProxyActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mTvTimer != null) {
                    // 显示倒计时
                    mTvTimer.setText(MessageFormat.format("Skip\n{0}s", mCount));
                    mCount--;

                    if (mCount < 0 && mTimer != null) {
                        mTimer.cancel();
                        mTimer = null;

                        // timer结束了，检查是否展示 scroller 还是登录等...
                        isScrollerLauncherShowedBefore();
                    }
                }
            }
        });
    }
}
