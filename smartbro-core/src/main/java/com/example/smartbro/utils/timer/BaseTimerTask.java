package com.example.smartbro.utils.timer;

import java.util.TimerTask;

/**
 * Created by Justin Wang from SmartBro on 4/12/17.
 */

public class BaseTimerTask extends TimerTask {

    private ITimerListener iTimerListener = null;

    public BaseTimerTask(ITimerListener iTimerListener){
        this.iTimerListener = iTimerListener;
    }

    @Override
    public void run() {
        if(iTimerListener != null){
            iTimerListener.onTimer();
        }
    }
}
