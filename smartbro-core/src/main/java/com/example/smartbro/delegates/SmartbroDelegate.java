package com.example.smartbro.delegates;
import com.example.smartbro.activities.ProxyActivity;
import java.util.Date;

import io.sentry.Sentry;
import io.sentry.android.AndroidSentryClientFactory;
import io.sentry.event.Breadcrumb;
import io.sentry.event.BreadcrumbBuilder;
import io.sentry.event.UserBuilder;

/**
 * Created by Justin Wang from SmartBro on 3/12/17.
 */

public abstract class SmartbroDelegate extends PermissionCheckerDelegate {

    private final static String sentryDSN = "https://54afde81fc484432a91c2ef3da2f8f7c@sentry.io/1365642";
    protected long lastClickActionTimeStamp = 0;

    protected void updateLastClickActionTimeStamp(){
        this.lastClickActionTimeStamp = new Date().getTime();
    }

    /**
     * 输出错误信息到主UI的方法, 该方法什么也不做, 由具体的实现类去复写, 以便完成相应的功能
     * @param info
     */
    public void echo(String info, boolean clear){

    }

    /**
     * 跳转到指定的 Delegate, , 子类去复写这个方法, 来实现特定的跳转
     */
    public void redirectToDelegate(SmartbroDelegate delegate){
        startWithPop(delegate);
    }

    /**
     * 跳转到指定的Activity, 子类去复写这个方法, 来实现特定的跳转
     * @param activity 指定的 Activity
     */
    public void redirectToActivity(ProxyActivity activity){

    }

    public int getColorIntByName(String colorName){
        int result = 0;
        return result;
    }

    public void sentryCapture(String s){
        Sentry.init(sentryDSN, new AndroidSentryClientFactory(getContext()));
        Sentry.getContext().recordBreadcrumb(
                new BreadcrumbBuilder().setMessage("First test").build()
        );
        Sentry.getContext().setUser(
                new UserBuilder().setEmail("justinwang24@yahoo.com.au").build()
        );
        Sentry.capture(s);
    }

    public void sentryCapture(Exception e){
        Sentry.init(sentryDSN, new AndroidSentryClientFactory(getContext()));
        Sentry.getContext().recordBreadcrumb(
                new BreadcrumbBuilder().setMessage("First test").build()
        );
        Sentry.getContext().setUser(
                new UserBuilder().setEmail("justinwang24@yahoo.com.au").build()
        );
        Sentry.capture(e);
    }
}
