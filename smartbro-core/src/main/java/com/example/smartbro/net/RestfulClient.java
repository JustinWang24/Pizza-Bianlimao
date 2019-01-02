package com.example.smartbro.net;

import android.content.Context;

import com.example.smartbro.app.ConfigType;
import com.example.smartbro.app.Smartbro;
import com.example.smartbro.net.callback.IError;
import com.example.smartbro.net.callback.IFailure;
import com.example.smartbro.net.callback.IRequest;
import com.example.smartbro.net.callback.ISuccess;
import com.example.smartbro.net.callback.RequestCallbacks;
import com.example.smartbro.net.download.DownloadHandler;
import com.example.smartbro.ui.LoaderStyle;
import com.example.smartbro.ui.SmartbroLoader;

import java.io.File;
import java.util.WeakHashMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by Justin Wang from SmartBro on 3/12/17.
 * 使用 Android 简化的建造者模式来实现
 */

public class RestfulClient {

    public static final int NO_ERROR = 100;
    public static final String STATUS_OK = "ok";
    public static final String HIPPO_APP_VERSION = "1.0";
    public static final String ACTION_GO_NEXT       = "done";   // 可以执行下一步操作了
    public static final String ACTION_KEEP_CHECKING = "keep";   // 继续查询
    public static final String ACTION_PAY_FAILED    = "fail";   // 执行支付失败的处理
    public static final String ACTION_ORDER_CLOSE   = "close";  // 执行订单已经关闭的处理

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
    private final RequestBody BODY; // 提交的原始数据格式的内容
    private final File FILE;        // 文件上传的时候使用
    private Context CONTEXT;
    private LoaderStyle LOADER_STYLE;

    public RestfulClient(String url, WeakHashMap<String, Object> params, IRequest request,
                         String downloadDir, String downloadFileExtension, String downloadFileName,
                         ISuccess success, IFailure failure,
                         IError error, RequestBody body, File file,
                         Context context,
                         LoaderStyle loaderStyle) {
        URL = url;
        PARAMS.putAll(params);
        REQUEST = request;
        SUCCESS = success;
        FAILURE = failure;
        ERROR = error;
        BODY = body;
        this.FILE = file;
        this.CONTEXT = context;
        this.LOADER_STYLE = loaderStyle;

        this.DOWNLOAD_DIR = downloadDir;
        this.EXTENSION = downloadFileExtension;
        this.NAME = downloadFileName;
    }

    /**
     * 返回 Restful的 Client的builder, 同时在参数中加入版本号; 还有便利猫App的version
     * @return RestfulClientBuilder
     */
    public static RestfulClientBuilder builder() {
        return new RestfulClientBuilder().params(
                "_version",
                (String) Smartbro.getConfigurationsMap().get(ConfigType.VERSION.name())
        ).params(
                "appVersion",
                HIPPO_APP_VERSION
        );
    }

    /**
     * 实现 GET 网络请求
     */
    public final void get() {
        request(HttpMethod.GET);
    }

    /**
     * 实现 POST 网络请求
     */
    public final void post() {
        if(BODY == null){
            request(HttpMethod.POST);
        }else {
            if(!PARAMS.isEmpty()){
                throw new RuntimeException("http request params must be null!");
            }
            request(HttpMethod.POST_RAW);
        }
    }


    /**
     * 实现 PUT 网络请求
     */
    public final void put() {

        if(BODY == null){
            request(HttpMethod.PUT);
        }else {
            if(!PARAMS.isEmpty()){
                throw new RuntimeException("http request params must be null!");
            }
            request(HttpMethod.PUT_RAW);
        }
    }

    /**
     * 实现 DELETE 网络请求
     */
    public final void delete() {
        request(HttpMethod.DELETE);
    }

    /**
     * 处理 Download 的方法
     */
    public final void download(){
        DownloadHandler downloadHandler = new DownloadHandler(
                URL,REQUEST,DOWNLOAD_DIR,EXTENSION,NAME,SUCCESS,FAILURE,ERROR);
        downloadHandler.handle();
    }

    /**
     * 真正实现各种网络请求的方法
     * @param method 网络请求方法 post get put ...
     */
    private void request(HttpMethod method) {
        final RestfulService restfulService = RestfulCreator.getRestfulService();

        Call<String> call = null;

        if (REQUEST != null) {
            REQUEST.onRequestStart();
        }

        if(LOADER_STYLE != null){
            // 如果指定了Loader，那么就显示它
            SmartbroLoader.showLoading(this.CONTEXT);
        }

        switch (method) {
            case GET:
                call = restfulService.get(URL, PARAMS);
                break;
            case PUT:
                call = restfulService.put(URL, PARAMS);
                break;
            case PUT_RAW:
                call = restfulService.putRaw(URL,BODY);
                break;
            case POST:
                call = restfulService.post(URL, PARAMS);
                break;
            case POST_RAW:
                call = restfulService.postRaw(URL, BODY);
                break;
            case DELETE:
                call = restfulService.delete(URL, PARAMS);
                break;
            case UPLOAD:
                // OkHttp3 的官方文档中查看具体细节
                final RequestBody requestBody = RequestBody.create(
                        MediaType.parse(MultipartBody.FORM.toString()), // "multipart/form-data"
                        FILE
                );
                final MultipartBody.Part body =
                        MultipartBody.Part.createFormData("file",FILE.getName(), requestBody);
                call = restfulService.upload(URL,body);
                break;
            default:
                break;
        }

        if (call != null) {
            call.enqueue(getRequestCallback());
        }
    }

    private Callback<String> getRequestCallback() {
        return new RequestCallbacks(
                REQUEST,
                SUCCESS,
                FAILURE,
                ERROR,
                this.LOADER_STYLE
        );
    }
}
