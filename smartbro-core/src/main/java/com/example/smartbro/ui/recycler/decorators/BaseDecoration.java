package com.example.smartbro.ui.recycler.decorators;

import android.support.annotation.ColorInt;

import com.choices.divider.DividerItemDecoration;

/**
 * Created by Justin Wang from SmartBro on 13/12/17.
 * 基础的分割线类
 */

public class BaseDecoration extends DividerItemDecoration {

    private BaseDecoration(@ColorInt int color, int size){
        setDividerLookup(new DividerLookupImpl(color, size));
    }

    /**
     * 简单工厂方法
     * @param color
     * @param size
     * @return
     */
    public static BaseDecoration create(@ColorInt int color, int size){
        return new BaseDecoration(color,size);
    }
}
