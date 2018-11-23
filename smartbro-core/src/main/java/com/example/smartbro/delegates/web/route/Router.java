package com.example.smartbro.delegates.web.route;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.webkit.URLUtil;
import android.webkit.WebView;

import com.example.smartbro.app.Smartbro;
import com.example.smartbro.delegates.SmartbroDelegate;
import com.example.smartbro.delegates.web.WebDelegate;
import com.example.smartbro.delegates.web.WebDelegateImpl;

/**
 * Created by Justin Wang from SmartBro on 14/12/17.
 * 线程安全的惰性单例设计
 */

public class Router {

    public Router(){

    }

    private static class Holder{
        private static final Router INSTANCE = new Router();
    }

    public static Router getInstance(){
        return Holder.INSTANCE;
    }

    /**
     * 拦截所有的a标签的跳转拦截
     * @param delegate
     * @param url
     * @return
     */
    public final boolean handleWebUrl(WebDelegate delegate, String url){

        // 如果javascript中tel, 表示为拨打电话
        if(url.contains("tel:")){
            this.callPhoneNumber(delegate.getContext(), url);
            return true;
        }

        // 如果不是拨打电话
        final SmartbroDelegate parentDelegate = delegate.getTopDelegate();
        final WebDelegateImpl webDelegate = WebDelegateImpl.create(url);
        if(parentDelegate == null){
            // 如果没有父一级的Delegate, 那么在本层跳转
            delegate.start(webDelegate);
        }else {
            // 有父一级的delegate
            parentDelegate.start(webDelegate);
        }

        // 表示我们来接管url的处理了
        return true;
    }

    /**
     * 真正加载web页面的方法
     * @param webView
     * @param url
     */
    private void loadWebPage(WebView webView, String url){
        if(webView != null){
            webView.loadUrl(url);
        }else {
            throw new NullPointerException("WebView is Null in Router!");
        }
    }

    /**
     * 加载本地页面
     * 本地页面，指的是在app 中的 assets 资源文件夹中的 html, css, js 资源文档， 称为本地page
     * @param webView
     * @param url
     */
    private void loadLocalPage(WebView webView, String url){
        // 通过加入这一的前缀，完成本地页面的加载
        this.loadWebPage(webView, "file:///android_asset/" + url);
    }

    private void loadPage(WebView webView, String url){
        if(URLUtil.isNetworkUrl(url) || URLUtil.isAssetUrl(url)){
            this.loadWebPage(webView,url);
        }else {
            this.loadLocalPage(webView, url);
        }
    }

    /**
     * 共外部调用的加载Page的方法
     * @param delegate
     * @param url
     */
    public void loadPage(WebDelegate delegate, String url){
        this.loadPage(delegate.getWebView(), url);
    }

    /**
     * 执行拨打电话的方法
     * @param context
     * @param url
     */
    private void callPhoneNumber(Context context, String url){
        // 询问用户是否拨打电话
        final Intent intent = new Intent(Intent.ACTION_DIAL);

        final Uri data = Uri.parse(url);
        intent.setData(data);

        // 这种处理方式更加谨慎
        ContextCompat.startActivity(context, intent, null);
    }
}
