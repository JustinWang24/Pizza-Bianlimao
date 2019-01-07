package com.example.th2.pizza;

import android.app.Application;
import com.example.smartbro.app.Smartbro;
import com.example.smartbroecommerce.database.DatabaseManager;
import com.example.smartbroecommerce.icon.FontSmartBroModule;
//import com.facebook.stetho.Stetho;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
/**
 * Created by th2 on 2017/6/28.
 */

public class MyApp extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        /*
         * 初始化并加载系统的配置信息
         */
        Smartbro.init(this)
//                .withApiHost("http://boss.htlc-kj.com/api/")
                .withApiHost("http://htlc.smartbro.com.au/api/")
//                .withApiHost("http://rap.taobao.org/mockjsdata/20889/api/")
                .withIcon(new FontAwesomeModule())
                .withIcon(new FontSmartBroModule())
                .withLoaderDelayed(1200)
                .withVersion(BuildConfig.VERSION_NAME)
//                .withInterceptor(new DebugInterceptor("index", R.raw.test))
                .configDone();
        DatabaseManager.getInstance().init(this);
//        this.initStetho();
    }

//    private void initStetho(){
//        Stetho.initializeWithDefaults(this);
//    }
}
