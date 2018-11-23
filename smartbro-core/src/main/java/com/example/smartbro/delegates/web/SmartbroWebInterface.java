package com.example.smartbro.delegates.web;

import android.webkit.JavascriptInterface;

import com.alibaba.fastjson.JSON;

/**
 * Created by Justin Wang from SmartBro on 14/12/17.
 * 用来和原生的WebView进行交互的
 */

public class SmartbroWebInterface {
    private final WebDelegate DELEGATE;

    private SmartbroWebInterface(WebDelegate delegate){
        this.DELEGATE = delegate;
    }

    public static SmartbroWebInterface create(WebDelegate delegate){
        return new SmartbroWebInterface(delegate);
    }

    /**
     * Javascript的执行接口
     * @param params
     * @return String 就是Javascript执行后的返回值
     */
    @JavascriptInterface
    public String event(String params){
        final String action = JSON.parseObject(params).getString("action");
        return null;
    }
}
