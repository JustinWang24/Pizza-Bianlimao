package com.example.smartbro.net.download;

import android.os.AsyncTask;

import com.example.smartbro.net.RestfulCreator;
import com.example.smartbro.net.callback.IError;
import com.example.smartbro.net.callback.IFailure;
import com.example.smartbro.net.callback.IRequest;
import com.example.smartbro.net.callback.ISuccess;

import java.util.WeakHashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Justin Wang from SmartBro on 3/12/17.
 */

public class DownloadHandler {
    private final String URL;
    private static final WeakHashMap<String, Object> PARAMS = RestfulCreator.getParams();
    private final IRequest REQUEST;

    // 和下载相关的属性
    private final String DOWNLOAD_DIR;
    private final String EXTENSION;
    private final String NAME;
    // 和下载相关的属性结束

    private final ISuccess SUCCESS;
    private final IFailure FAILURE;
    private final IError ERROR;

    public DownloadHandler(String url, IRequest request, String download_dir,
               String extension, String name, ISuccess success,
               IFailure failure, IError error) {
        URL = url;
        REQUEST = request;
        DOWNLOAD_DIR = download_dir;
        EXTENSION = extension;
        NAME = name;
        SUCCESS = success;
        FAILURE = failure;
        ERROR = error;
    }

    public final void handle(){
        if(REQUEST != null){
            REQUEST.onRequestStart();
            // 开始下载了
        }

        RestfulCreator.getRestfulService().download(URL, PARAMS)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        // 判断 Async Task 是否结束，否则文件下载不全
                        if(response.isSuccessful()){
                            final SaveFileTask saveFileTask = new SaveFileTask(REQUEST, SUCCESS);

                            final ResponseBody body = response.body();

                            /**
                             * 开始下载任务， 注意这里
                             * 1: 以线程池的方式运行
                             * 2: 其他参数传递的顺序, 要和task中的取值顺序一样
                             */
                            saveFileTask.executeOnExecutor(
                                    AsyncTask.THREAD_POOL_EXECUTOR,
                                    DOWNLOAD_DIR,
                                    EXTENSION,
                                    response,
                                    NAME
                            );

                            if(saveFileTask.isCancelled()){
                                if(REQUEST != null){
                                    REQUEST.onRequestEnd();
                                }
                            } else {
                                if(ERROR != null){
                                    ERROR.onError(response.code(), response.message());
                                }
                            }
                        }

                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                        // 下载失败
                        if(FAILURE != null){
                            FAILURE.onFailure();
                        }
                    }
                });
    }
}
