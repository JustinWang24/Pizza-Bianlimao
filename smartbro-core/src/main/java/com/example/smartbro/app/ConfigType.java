package com.example.smartbro.app;

/**
 * Created by Justin Wang from SmartBro on 2/12/17.
 *
 * 这个是唯一的单例， 且只能被初始化一次。 在多线程环境下，的线程安全的懒汉模式
 *
 * 配置的类型
 */

public enum ConfigType {
    /**
     * API的服务接口URL地址
     */
    API_HOST,

    /**
     * 应用程序的全局上下文
     */
    APPLICATION_CONTEXT,

    /**
     * 配置信息是否加载完成的指示位
     */
    CONFIG_READY,

    /**
     * 配置默认的文件下载根目录
     */
    DEFAULT_DOWNLOAD_ROOT_DIR,

    /**
     * 配置网络通信时的 loader icon的渐变消失的延时
     */
    LOADER_DELAYED,

    /**
     * 配置拦截器
     */
    INTERCEPTOR,

    /**
     * 设置本软件当前的版本号
     */
    VERSION,

    /**
     * 应用的各种帮助类
     */
    HANDLER,

    /**
     * 用到的icons
     */
    ICON,

    /**
     * 设置便利猫的APP ID
     */
    HIPPO_APP_ID
}
