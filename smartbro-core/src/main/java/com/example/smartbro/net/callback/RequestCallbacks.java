package com.example.smartbro.net.callback;

import android.os.Handler;

import com.example.smartbro.app.ConfigType;
import com.example.smartbro.app.Smartbro;
import com.example.smartbro.ui.LoaderStyle;
import com.example.smartbro.ui.SmartbroLoader;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Justin Wang from SmartBro on 3/12/17.
 */

public class RequestCallbacks implements Callback<String> {
    private final IRequest REQUEST;
    private final ISuccess SUCCESS;
    private final IFailure FAILURE;
    private final IError ERROR;
    private final LoaderStyle LOADER_STYLE;

    // Loader Icon的延时消失时间
    private final int LOADER_GONE_IN;

    /**
     * 需要一个Android的handler来在Loader消失的时候加上一些延时
     * Handler声明为 static 类型， 避免内存泄漏
     */
    private static final Handler HANDLER = new Handler();

    /**
     * 构造函数
     * @param request   请求对象
     * @param success   成功回调对象
     * @param failure   失败回调对象
     * @param error     错误回调对象
     * @param loaderStyle   加载的 Loader 名称
     */
    public RequestCallbacks(IRequest request, ISuccess success, IFailure failure, IError error, LoaderStyle loaderStyle) {
        REQUEST = request;
        SUCCESS = success;
        FAILURE = failure;
        ERROR = error;
        this.LOADER_STYLE = loaderStyle;

        /**
         * 给加载的 Loader 一个消失效果延时
         */
        final int configDelay =
                (int) Smartbro.getConfigurationsMap().get(ConfigType.LOADER_DELAYED.name());
        if(configDelay > 0){
            this.LOADER_GONE_IN = configDelay;
        }else{
            this.LOADER_GONE_IN = 1000;
        }
    }


    @Override
    public void onResponse(Call<String> call, Response<String> response) {
        if(response.isSuccessful()){
            if(call.isExecuted()){
                if(SUCCESS != null){
                    SUCCESS.onSuccess(response.body());
                }
            }
        }else {
            if (ERROR != null){
                ERROR.onError(response.code(), response.message());
            }
        }

        if(LOADER_STYLE != null){
            // 表示有 Loader, 获取默认的延时时间
            HANDLER.postDelayed(new Runnable() {
                @Override
                public void run() {
                    SmartbroLoader.stopLoading();
                }
            }, this.LOADER_GONE_IN);
        }
    }

    @Override
    public void onFailure(Call<String> call, Throwable throwable) {
        if(FAILURE != null){
            FAILURE.onFailure();
        }

        if(REQUEST != null){
            REQUEST.onRequestEnd();
        }
    }
}
