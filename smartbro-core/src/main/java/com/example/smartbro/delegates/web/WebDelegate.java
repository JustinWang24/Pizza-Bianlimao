package com.example.smartbro.delegates.web;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.webkit.WebView;

import com.example.smartbro.app.Smartbro;
import com.example.smartbro.delegates.SmartbroDelegate;
import com.example.smartbro.delegates.web.route.RouteKeys;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 * Created by Justin Wang from SmartBro on 14/12/17.
 * Web View的基础类
 */

public abstract class WebDelegate extends SmartbroDelegate implements IWebViewInitializer{

    private WebView webView = null;
    private final ReferenceQueue<WebView> WEB_VIEW_QUEUE = new ReferenceQueue<>();
    private String url = null;
    private boolean isWebViewAvaliable = false;
    private SmartbroDelegate topDelegate = null;

    public WebDelegate(){

    }

    /**
     * 定义一个抽象方法，强制子类继承的时候，实现这个初始化方法
     */
    public abstract IWebViewInitializer setInitializer();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        this.url = args.getString(RouteKeys.URL.name());
        this.initWebView();
    }

    @SuppressLint("JavascriptInterface")
    private void initWebView(){
        if(this.webView != null){
            this.webView.removeAllViews();
            this.webView.destroy();
//            this.isWebViewAvaliable = false;
        }else {
            final IWebViewInitializer initializer = setInitializer();
            if(initializer != null){
                final WeakReference<WebView> webViewWeakReference =
                        new WeakReference<>(new WebView(Smartbro.getApplication()), WEB_VIEW_QUEUE);
                this.webView = webViewWeakReference.get();
                this.webView = initializer.initWebView(this.webView);
                this.webView.setWebViewClient(initializer.initWebViewClient());
                this.webView.setWebChromeClient(initializer.initWebChromeClient());
                this.webView.addJavascriptInterface(SmartbroWebInterface.create(this),"smartbro");
                this.isWebViewAvaliable = true;
            }else {
                // 初始化错误，抛出异常
                throw new NullPointerException("WebDelegate initializer is NULL!");
            }
        }
    }

    public WebView getWebView(){
        if(this.webView == null){
            throw new NullPointerException("WebView is NULL!");
        }
        return this.webView;
    }

    public String getUrl(){
        if(this.url == null){
            throw new NullPointerException("WebView URL is NULL!");
        }
        return this.url;
    }

    /**
     * 设置上一级或者顶级的delegate
     * @param delegate
     */
    public void setTopDelegate(SmartbroDelegate delegate) {
        this.topDelegate = delegate;
    }

    /**
     * 获取上一级或者顶级的delegate
     * @return
     */
    public SmartbroDelegate getTopDelegate(){
        if(this.topDelegate != null){
            return this.topDelegate;
        }
        return this;
    }

    @Override
    public void onPause() {
        super.onPause();
        if(this.webView != null){
            this.webView.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(this.webView != null){
            this.webView.onResume();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(this.webView != null){
            this.isWebViewAvaliable = false;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(this.webView != null){
            this.webView.removeAllViews();
            this.webView.destroy();
            this.webView = null;
        }
    }
}
