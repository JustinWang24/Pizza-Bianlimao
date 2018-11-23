package com.example.smartbro.ui.launcher;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.view.View;

import com.bigkoo.convenientbanner.holder.Holder;

/**
 * Created by Justin Wang from SmartBro on 4/12/17.
 */

public class LauncherHolder implements Holder<Integer> {

    private AppCompatImageView mImageView = null;

    @Override
    public View createView(Context context) {
        this.mImageView = new AppCompatImageView(context);
        return this.mImageView;
    }

    @Override
    public void UpdateUI(Context context, int position, Integer data) {
        // 每次滑动是更新的操作
        this.mImageView.setBackgroundResource(data);
    }
}
