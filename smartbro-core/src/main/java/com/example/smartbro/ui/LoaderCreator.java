package com.example.smartbro.ui;

import android.content.Context;
import android.util.Log;

import java.util.WeakHashMap;

import com.wang.avi.AVLoadingIndicatorView;
import com.wang.avi.Indicator;
import com.wang.avi.indicators.BallPulseIndicator;

/**
 * Created by Justin Wang from SmartBro on 3/12/17.
 * 通过将loader的对象缓存到hash map中，提高加载Loader的性能
 */

public class LoaderCreator {

    /**
     * 保存所有加载的可用Loader icon的map
     */
    private static final WeakHashMap<String, Indicator> LOADING_MAP =
            new WeakHashMap<>();

    /**
     * 创建Loader Icon对象
     * @param type Loader Icon的名字
     * @param context 应用的context
     * @return AVLoadingIndicatorView
     */
    static AVLoadingIndicatorView create(String type, Context context){
        final AVLoadingIndicatorView avLoadingIndicatorView =
                new AVLoadingIndicatorView(context);

        if(LOADING_MAP.get(type) == null){
            final Indicator indicator = getIndicator(type);
            LOADING_MAP.put(type, indicator);
        }

        avLoadingIndicatorView.setIndicator(LOADING_MAP.get(type));
        return avLoadingIndicatorView;
    }

    /**
     * 根据给定的Loader Icon的名字，返回对应的Loader对象
     * @param name Loader Icon的名字
     * @return Indicator
     */
    private static Indicator getIndicator(String name){
        if(name == null || name.isEmpty()){
            return null;
        }

        final StringBuilder drawableClassName = new StringBuilder();

        // 如果name没有包含 . , 说明传入的不是个类名, 需要构建成类名
        if(!name.contains(".")){
            final String defaultPackageName = AVLoadingIndicatorView.class.getPackage().getName();
            drawableClassName.append(defaultPackageName)
                    .append(".indicators")
                    .append(".");
        }
        drawableClassName.append(name);

        try {
            final Class<?> drawableClass = Class.forName(drawableClassName.toString());
            return (Indicator) drawableClass.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null;
        }
    }
}
