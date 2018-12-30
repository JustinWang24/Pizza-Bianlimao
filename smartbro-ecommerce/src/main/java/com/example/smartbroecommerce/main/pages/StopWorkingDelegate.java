package com.example.smartbroecommerce.main.pages;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;

import com.example.smartbro.delegates.SmartbroDelegate;
import com.example.smartbro.utils.timer.ITimerListener;
import com.example.smartbroecommerce.R;
import com.example.smartbroecommerce.R2;
import com.example.smartbroecommerce.database.MachineProfile;
import com.example.smartbroecommerce.utils.UrlTool;

import java.util.Date;
import java.util.Timer;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Justin Wang from SmartBro on 12/1/18.
 */

public class StopWorkingDelegate extends SmartbroDelegate implements ITimerListener {

    private int unlockBtnClickedCount = 0;
    private long lastTimeUnlockBtnClicked = 0;
    private Timer mTimer = null;

    @Override
    public Object setLayout() {
        return R.layout.delegate_stop_working_new;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        this.lastTimeUnlockBtnClicked = 0;
    }

    @Override
    public void onResume() {
        super.onResume();
        this.lastTimeUnlockBtnClicked = 0;
        // 通知服务器本机已经out of service了
        UrlTool.reportMachineStatus(MachineProfile.getInstance().getUuid(),1,"",0);
    }

    @Override
    public void onTimer() {
        final long now = new Date().getTime();
        // 如果解锁按钮10秒钟没有被点击, 那么就重置
        if(now - this.lastTimeUnlockBtnClicked >= 10000){
            this.unlockBtnClickedCount = 0;
        }
    }
}
