package com.example.smartbro.net.interceptors;

import java.io.IOException;
import java.util.LinkedHashMap;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Justin Wang from SmartBro on 3/12/17.
 */

public abstract class BaseInterceptor implements Interceptor {

    // 要求子类自己去实现
    @Override
    public abstract Response intercept(Chain chain) throws IOException;

    /**
     * 获取 GET 请求url中的所有参数
     * @param chain 拦截器对象
     * @return LinkedHashMap 所有参数的  name => value 键值对
     */
    protected LinkedHashMap<String,String> getUrlParameters(Chain chain){

        final HttpUrl url = chain.request().url();
        int size = url.querySize();

        final LinkedHashMap<String,String> params =
                new LinkedHashMap<>();

        for (int i = 0; i < size; i++) {
            params.put(
                    url.queryParameterName(i),  // 根据角标获取 url中第i个参数的名字
                    url.queryParameterValue(i)  // 根据角标获取 url 中第i个参数的值
            );
        }

        // 所有提交的url中的参数都已获取并按照 name => value 键值对的方式放到了params中， 然后返回
        return params;
    }

    /**
     * 获取 GET 请求url中某个特定名称的参数的值
     * @param chain 拦截器对象
     * @param key 参数名
     * @return String 参数值
     */
    protected String getUrlParameters(Chain chain, String key){
        final Request request = chain.request();
        return request.url().queryParameter(key);
    }

    /**
     * 获取 POST 请求中的参数
     * @param chain 拦截器对象
     * @return LinkedHashMap 所有参数的  name => value 键值对
     */
    protected LinkedHashMap<String, String> getBodyParameters(Chain chain){
        final FormBody formBody = (FormBody) chain.request().body();
        final LinkedHashMap<String,String> params =
                new LinkedHashMap<>();
        int size = formBody.size();

        for (int i = 0; i < size; i++) {
            params.put(
                    formBody.name(i),  // 根据角标获取 url中第i个参数的名字
                    formBody.value(i)  // 根据角标获取 url 中第i个参数的值
            );
        }
        return params;
    }

    /**
     * 获取 POST 请求中某个特定名称的参数的值
     * @param chain 拦截器对象
     * @param key 参数名
     * @return String 参数值
     */
    protected String getBodyParameters(Chain chain, String key){
        return getUrlParameters(chain).get(key);
    }
}
