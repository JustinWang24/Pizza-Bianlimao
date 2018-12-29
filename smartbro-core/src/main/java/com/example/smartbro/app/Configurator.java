package com.example.smartbro.app;

import android.os.Handler;
import com.joanzapata.iconify.IconFontDescriptor;
import com.joanzapata.iconify.Iconify;

import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Interceptor;

/**
 * Created by Justin Wang from SmartBro on 2/12/17.
 *
 * 完成配置文件存储与获取的功能
 */

public class Configurator {

    private static final HashMap<Object, Object> SMARTBRO_CONFIGS = new HashMap<>();
    private static final ArrayList<IconFontDescriptor> ICONS = new ArrayList<>();

    /**
     * 网络请求中可能会使用的拦截器列表
     */
    private static final ArrayList<Interceptor> INTERCEPTORS = new ArrayList<>();

    private static final Handler HANDLER = new Handler();

    private Configurator(){
        // 开始配置信息的加载，刚开始，指示完成位是 false
        SMARTBRO_CONFIGS.put(ConfigType.CONFIG_READY.name(), false);
    }

    /**
     * 获取配置类实例的方法
     * @return Configurator
     */
    static Configurator getInstance(){
        return Holder.INSTANCE;
    }

    /**
     * 静态内部类，实现懒汉模式
     */
    private static class Holder{
        private static final Configurator INSTANCE = new Configurator();
    }

    final HashMap<Object, Object> getConfigsMap(){
        return SMARTBRO_CONFIGS;
    }

    /**
     * 初始化常用的图标
     * 设置配置项全部完成的指示位为true
     */
    public final void configDone(){
        SMARTBRO_CONFIGS.put(ConfigType.CONFIG_READY.name(), true);
        // 默认就加载图标库共应用程序使用
        initIcons();

        // 检查是否配置了默认的下载路径， 如果没有配置，则指定一个默认保存的目录名
        if(SMARTBRO_CONFIGS.get(ConfigType.DEFAULT_DOWNLOAD_ROOT_DIR.name()) == null){
            this.withDownloadRootDir("/Downloads");
        }
    }

    /**
     * 配置Api Host的方法
     * @param host API的URL地址
     * @return Configurator
     */
    public final Configurator withApiHost(String host){
        SMARTBRO_CONFIGS.put(ConfigType.API_HOST.name(), host);
        return this;
    }

    /**
     * 配置本软件当前的版本号
     * @param version 当前的版本号
     * @return Configurator
     */
    public final Configurator withVersion(String version){
        SMARTBRO_CONFIGS.put(ConfigType.VERSION.name(), version);
        return this;
    }

    /**
     * 配置本机的便利猫 APP ID
     * @param hippoAppId 本机的便利猫 APP ID
     * @return Configurator
     */
    public final Configurator withHippoAppId(String hippoAppId){
        SMARTBRO_CONFIGS.put(ConfigType.HIPPO_APP_ID.name(), hippoAppId);
        return this;
    }

    /**
     * 配置默认的下载路径的方法
     * @param path 下载文件的保存路径
     * @return Configurator
     */
    public final Configurator withDownloadRootDir(String path){
        SMARTBRO_CONFIGS.put(ConfigType.DEFAULT_DOWNLOAD_ROOT_DIR.name(),path);
        return this;
    }

    /**
     * 配置网络通信时的 loader icon的渐变消失的延时
     * @param milliseconds 延时的毫秒数
     * @return Configurator
     */
    public final Configurator withLoaderDelayed(int milliseconds){
        SMARTBRO_CONFIGS.put(ConfigType.LOADER_DELAYED.name(),milliseconds);
        return this;
    }

    /**
     * 加入图标
     * @param descriptor 图标的描述符对象
     * @return Configurator
     */
    public final Configurator withIcon(IconFontDescriptor descriptor){
        ICONS.add(descriptor);
        return this;
    }

    /**
     * 初始化图标
     */
    private void initIcons(){
        if(ICONS.size() > 0){
            final Iconify.IconifyInitializer initializer = Iconify.with(ICONS.get(0));
            for (int i = 1; i < ICONS.size(); i++) {
                initializer.with(ICONS.get(i));
            }
        }
    }

    /**
     * 检查配置是否完成
     */
    private void checkConfiguration(){
        final boolean isReady = (boolean) SMARTBRO_CONFIGS.get(ConfigType.CONFIG_READY.name());

        if(!isReady){
            throw new RuntimeException("Configuration is not ready, call function: Configurator -> configDone();");
        }
    }

    @SuppressWarnings("unchecked")
    final <T> T getConfiguration(Enum<ConfigType> key){
        checkConfiguration();
        return (T) SMARTBRO_CONFIGS.get(key.name());
    }

    /**
     * 加入一个拦截器
     * @param interceptor 单个的拦截器对象
     * @return Configurator
     */
    public final Configurator withInterceptor(Interceptor interceptor){
        INTERCEPTORS.add(interceptor);
        SMARTBRO_CONFIGS.put(ConfigType.INTERCEPTOR, INTERCEPTORS);
        return this;
    }

    /**
     * 加入多个拦截器
     * @param interceptors 拦截器的列表
     * @return Configurator
     */
    public final Configurator withInterceptors(ArrayList<Interceptor> interceptors){
        INTERCEPTORS.addAll(interceptors);
        SMARTBRO_CONFIGS.put(ConfigType.INTERCEPTOR, INTERCEPTORS);
        return this;
    }
}
