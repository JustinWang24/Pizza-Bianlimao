package com.example.smartbro.net.interceptors;

import android.support.annotation.RawRes;
import android.util.Log;

import com.example.smartbro.utils.file.FileUtil;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by Justin Wang from SmartBro on 3/12/17.
 * 调试用的拦截器，它负责拦截所有的请求，不经过网络，而是直接提取raw中的某个json文件，作为结果返回给调用者
 */

public class DebugInterceptor extends BaseInterceptor {

    private final String DEBUG_URL;
    private final int DEBUG_RAW_ID; // 为了调试简单, 使用RAW

    public DebugInterceptor(String debug_url, int debug_raw_id) {
        DEBUG_URL = debug_url;
        DEBUG_RAW_ID = debug_raw_id;
    }

    /**
     * 根据给定的json内容，拦截后返回一个json类型的response给调用者
     * @param chain 拦截器
     * @param json json文本
     * @return Response
     */
    private Response getResponse(Chain chain, String json){
        return new Response.Builder()
                .code(200)
                .addHeader("Content-Type","application/json")
                .body(ResponseBody.create(MediaType.parse("application/json"),json))
                .message("OK")
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .build();
    }

    /**
     * 根据给定的 raw id 内容，拦截后返回一个json类型的response给调用者
     * @param chain 拦截器链
     * @param rawId 调试用的模拟返回的json数据文件的Raw ID
     * @return
     */
    private Response debugResponse(Chain chain, @RawRes int rawId){
        final String json = FileUtil.getRawFile(rawId);

        return  getResponse(chain, json);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        final String url = chain.request().url().toString();
        // 拦截后检查url是否为调试的URL地址, 如果是，则返回自定义的JSON数据
        if(url.contains(DEBUG_URL)){
            return debugResponse(chain, DEBUG_RAW_ID);
        }

        // 非调试 向下继续传递
        return chain.proceed(chain.request());
    }
}
