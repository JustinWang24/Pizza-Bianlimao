package com.example.smartbro.delegates.web;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.smartbro.delegates.web.client.WebViewClientImpl;
import com.example.smartbro.delegates.web.route.RouteKeys;
import com.example.smartbro.delegates.web.route.Router;

/**
 * Created by Justin Wang from SmartBro on 14/12/17.
 */

public class WebDelegateImpl extends WebDelegate {

    public static WebDelegateImpl create(String url){
        final Bundle args = new Bundle();
        args.putString(RouteKeys.URL.name(), url);
        final WebDelegateImpl delegate = new WebDelegateImpl();
        delegate.setArguments(args);
        return delegate;
    }

    @Override
    public Object setLayout() {
        return getWebView();
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        if(getUrl() != null){
            // 采用原生的方法跳转, 模拟Web的跳转并进行页面加载
            Router.getInstance().loadPage(this, getUrl());
        }
    }

    @Override
    public IWebViewInitializer setInitializer() {
        return this;
    }

    // 以下开始实现 IWebViewInitializer 接口
    /**
     *
     * @param webView
     * @return
     */
    @Override
    public WebView initWebView(WebView webView) {
        // 初始化webview的各种配置项
        return new WebViewInitializer().createWebView(webView);
    }

    @Override
    public WebViewClient initWebViewClient() {
        final WebViewClient client = new WebViewClientImpl(this);
        return client;
    }

    @Override
    public WebChromeClient initWebChromeClient() {
        return null;
    }
}
