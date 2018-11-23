package com.example.smartbro.net;

import com.example.smartbro.app.ConfigType;
import com.example.smartbro.app.Smartbro;

import java.util.ArrayList;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by Justin Wang from SmartBro on 3/12/17.
 */

public class RestfulCreator {
    public static RestfulService getRestfulService(){
        return RestfulServiceHolder.RESTFUL_SERVICE;
    }

    /**
     * 惰性的加载网络请求中保存的各种参数值的Map
     * @return WeakHashMap<String, Object>
     */
    public static WeakHashMap<String, Object> getParams(){
        return ParamsHolder.PARAMS;
    }

    private static final class ParamsHolder{
        private static final WeakHashMap<String, Object> PARAMS =
                new WeakHashMap<>();
    }

    private static final class RetrofitHolder{
        private static final String BASE_URL =
                (String) Smartbro.getConfigurationsMap().get(ConfigType.API_HOST.name());
        private static final Retrofit RETROFIT_CLIENT =
                new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(OKHttpHolder.OK_HTTP_CLIENT)  // 使用OKHttp的Client作为客户端, 同时创建加载所有的拦截器
                        .addConverterFactory(ScalarsConverterFactory.create())
                        .build();
    }

    /**
     * 私有静态内部类， 用来保存唯一的 OKHttp Client 实例
     */
    private static final class OKHttpHolder{
        private static final int TIME_OUT = 60;
        private static OkHttpClient.Builder BUILDER = new OkHttpClient.Builder();

        /**
         * 获取配置好的拦截器列表
         */
        private static final ArrayList<Interceptor> INTERCEPTORS =
                (ArrayList<Interceptor>) Smartbro.getConfigurationsMap().get(ConfigType.INTERCEPTOR);

        // Todo 通过循环的方式传入的OKHttp中
        private static OkHttpClient.Builder addInterceptor(){
            if(INTERCEPTORS != null && !INTERCEPTORS.isEmpty()){
                for (Interceptor interceptor:INTERCEPTORS){
                    BUILDER.addInterceptor(interceptor);
                }
            }
            return BUILDER;
        }


        private static final OkHttpClient OK_HTTP_CLIENT = addInterceptor()
                .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
                .build();
    }

    private static final class RestfulServiceHolder{
        private static final RestfulService RESTFUL_SERVICE =
                RetrofitHolder.RETROFIT_CLIENT.create(RestfulService.class);
    }
}
