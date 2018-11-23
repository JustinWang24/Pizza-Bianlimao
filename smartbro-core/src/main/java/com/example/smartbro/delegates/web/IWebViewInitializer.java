package com.example.smartbro.delegates.web;

import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by Justin Wang from SmartBro on 14/12/17.
 */

public interface IWebViewInitializer {

    WebView initWebView(WebView webView);
    WebViewClient initWebViewClient();

    // 针对页面的控制
    WebChromeClient initWebChromeClient();
}
