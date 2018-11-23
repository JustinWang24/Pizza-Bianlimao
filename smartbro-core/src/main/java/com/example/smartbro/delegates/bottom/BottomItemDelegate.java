package com.example.smartbro.delegates.bottom;

import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.example.smartbro.delegates.SmartbroDelegate;

/**
 * Created by Justin Wang from SmartBro on 9/12/17.
 * 需要有保留底部导航按钮的bean
 */

public abstract class BottomItemDelegate extends SmartbroDelegate
    implements View.OnKeyListener{

    private long mExitTime = 0;
    private static final int EXIT_TIME = 2000;  // 表示连续两次点击的时间间隔为2秒

    /**
     * 在程序从后台被唤醒的时候，需要重新注册一下，才能恢复继续监听的功能，这个是Fragment的问题，需要注意
     */
    @Override
    public void onResume() {
        super.onResume();
        final View rootView = getView();
        if(rootView != null){
            rootView.setFocusableInTouchMode(true);
            rootView.requestFocus();
            rootView.setOnKeyListener(this);
        }
    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {

        if(keyCode == keyEvent.KEYCODE_BACK && keyEvent.getAction() == keyEvent.ACTION_DOWN){
            // 点击的Back按键
            if((System.currentTimeMillis() - mExitTime) > EXIT_TIME){
                Toast.makeText(getContext(), "双击退出" , Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            }else{
                // 点击很快的话，小于2秒，那么可以判定为直接退出应用
                _mActivity.finish();
                if(mExitTime != 0){
                    mExitTime = 0;
                }
            }
            return true;
        }

        return false;
    }
}
