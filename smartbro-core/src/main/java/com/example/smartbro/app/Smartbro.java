package com.example.smartbro.app;

import android.content.Context;
import android.os.Handler;

import java.util.HashMap;

/**
 * Created by justinwang on 2/12/17.
 *
 * App控制类 final 不允许其他的程序继承
 * 主要用来保存全局的各种信息
 */
@SuppressWarnings("SpellCheckingInspection")
public final class Smartbro {

    /**
     * 初始化配置类
     * @param context
     * @return Configurator
     */
    public static Configurator init(Context context){
        getConfigurationsMap().put(
                ConfigType.APPLICATION_CONTEXT.name(),
                context.getApplicationContext()
        );

        /*
         * 初始化的是否，放进一个Handler对象，以便以后使用
         */
        getConfigurationsMap().put(
                ConfigType.HANDLER.name(),
                new Handler()
        );
        return Configurator.getInstance();
    }

    /**
     * 获取配置工具类中的配置项Map
     * @return HashMap<Object, Object>
     */
    public static HashMap<Object, Object> getConfigurationsMap(){
        return Configurator.getInstance().getConfigsMap();
    }

    /**
     * 获取配置类对象实例
     * @return Configurator
     */
    public static Configurator getConfigurator(){
        return Configurator.getInstance();
    }

    /**
     * 根据给定的key获取配置中的值
     * @return <T> T
     */
    public static <T> T getConfiguration(Object key){
        return getConfigurator().getConfiguration((Enum<ConfigType>) key);
    }


    /**
     * 获取全局配置中的上下文对象的方法
     * @return Context
     */
    public static Context getApplication(){
        return (Context) getConfigurationsMap().get(ConfigType.APPLICATION_CONTEXT.name());
    }

    /**
     * 获取handler对象
     * @return Handler
     */
    public static Handler getHandler(){
        return (Handler) getConfigurationsMap().get(ConfigType.HANDLER.name());
    }
}
