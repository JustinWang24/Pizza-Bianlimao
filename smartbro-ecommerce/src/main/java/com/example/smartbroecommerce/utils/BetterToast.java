package com.example.smartbroecommerce.utils;

import android.app.Activity;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.view.View;

import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.github.johnpersano.supertoasts.library.utils.PaletteUtils;

/**
 * Created by Justin Wang from SmartBro on 16/12/17.
 * 显示更好看的Toast的快捷方法类
 */

public class BetterToast {

    public static BetterToast getInstance(){
        return Holder.INSTANCE;
    }

    private static class Holder{
        private static final BetterToast INSTANCE = new BetterToast();
    }

    /**
     * 显示好看一些的文字Toast
     * @param activity
     * @param message
     */
    public void showText(@NonNull Activity activity, String message){
        SuperActivityToast.create(activity, new Style(),Style.ANIMATIONS_FLY)
                .setText(message)
                .setDuration(Style.DURATION_MEDIUM)
                .setFrame(Style.FRAME_LOLLIPOP)
                .setColor(PaletteUtils.getSolidColor(PaletteUtils.MATERIAL_INDIGO))
                .setAnimations(Style.ANIMATIONS_FLY)
                .show();
    }

    public void showButton(@NonNull Activity activity, String message){
        SuperActivityToast.create(activity, new Style(),Style.ANIMATIONS_FLY)
                .setButtonText(message)
                .setOnButtonClickListener(message, null, new SuperActivityToast.OnButtonClickListener() {
                    @Override
                    public void onClick(View view, Parcelable parcelable) {
                        // Todo 实现在 Super Activity Toast为Button类型时的点击事件
                    }
                })
                .setDuration(Style.DURATION_MEDIUM)
                .setFrame(Style.FRAME_LOLLIPOP)
                .setColor(PaletteUtils.getSolidColor(PaletteUtils.MATERIAL_PURPLE))
                .setAnimations(Style.ANIMATIONS_POP)
                .show();
    }
}
