package com.example.smartbro.utils;

import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.example.smartbro.app.Smartbro;

/**
 * Created by Justin Wang from SmartBro on 3/12/17.
 * 工具类 包含了一些和屏幕相关的方法
 */

public class DimenUtil {

    /**
     * 获取屏幕宽度的值（单位: 像素）
     * @return
     */
    public static int getScreenWidth(){
        final Resources resources = Smartbro.getApplication().getResources();
        final DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        return displayMetrics.widthPixels;
    }

    /**
     * 获取屏幕高度的值（单位: 像素）
     * @return
     */
    public static int getScreenHeight(){
        final Resources resources = Smartbro.getApplication().getResources();
        final DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        return displayMetrics.heightPixels;
    }
}
