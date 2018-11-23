package com.example.smartbro.net;


import android.content.Context;

import com.example.smartbro.net.callback.IError;
import com.example.smartbro.net.callback.IFailure;
import com.example.smartbro.net.callback.IRequest;
import com.example.smartbro.net.callback.ISuccess;
import com.example.smartbro.ui.LoaderStyle;

import java.io.File;
import java.util.WeakHashMap;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Created by Justin Wang from SmartBro on 3/12/17.
 */

public class RestfulClientBuilder {

    private  String mUrl = null;
    private static WeakHashMap<String, Object> mParams = RestfulCreator.getParams();
    private  IRequest mIRequest = null;

    // 和文件下载相关
    private String mDownloadDir = null;
    private String mFileExtension = null;
    private String mFileName = null;
    // 和文件下载相关结束

    private  ISuccess mISuccess = null;
    private  IFailure mIFailure = null;
    private  IError mIError = null;
    private  RequestBody mBody = null;
    private File mFile = null;
    private Context mContext = null;
    private LoaderStyle mLoaderStyle = null;

    RestfulClientBuilder(){

    }

    public final RestfulClient build(){
        return new RestfulClient(
                mUrl,
                mParams,
                mIRequest,
                mDownloadDir,
                mFileExtension,
                mFileName,
                mISuccess,
                mIFailure,
                mIError,
                mBody,
                mFile,
                mContext,
                mLoaderStyle
        );
    }

    /**
     * 设置需要使用的Loader
     * @param context 上下文
     * @param loaderStyle Loader的名字
     * @return RestfulClientBuilder
     */
    public final RestfulClientBuilder loader(Context context, LoaderStyle loaderStyle){
        this.mContext = context;
        this.mLoaderStyle = loaderStyle;
        return this;
    }

    /**
     * 设置使用默认的Loader
     * @param context 上下文
     * @return RestfulClientBuilder
     */
    public final RestfulClientBuilder loader(Context context){
        loader(context, LoaderStyle.BallPulseIndicator);
        return this;
    }

    /**
     * 设置URL
     * @param url 请求的url
     * @return RestfulClientBuilder
     */
    public final RestfulClientBuilder url(String url){
        mUrl = url;
        return this;
    }

    public final RestfulClientBuilder request(IRequest iRequest){
        mIRequest = iRequest;
        return this;
    }

    public final RestfulClientBuilder success(ISuccess iSuccess){
        mISuccess = iSuccess;
        return this;
    }

    public final RestfulClientBuilder error(IError iError){
        mIError = iError;
        return this;
    }

    public final RestfulClientBuilder failure(IFailure iFailure){
        mIFailure = iFailure;
        return this;
    }


    /**
     * 传入原始的JSON字符串作为参数
     * @param raw 原始的JSON字符串
     * @return RestfulClientBuilder
     */
    public final RestfulClientBuilder raw(String raw){
        this.mBody = RequestBody.create(MediaType.parse("application/json;charset=UTF-8"),raw);
        return this;
    }

    public final RestfulClientBuilder params(WeakHashMap<String, Object> params){
        mParams.putAll(params);
        return this;
    }

    /**
     * 重载: 也可以传入键/值对
     * @param key 键
     * @param value 值
     * @return RestfulClientBuilder
     */
    public final RestfulClientBuilder params(String key, String value){
        mParams.put(key, value);
        return this;
    }

    /**
     * 指定上传的文件
     * @param file 文件对象
     * @return RestfulClientBuilder
     */
    public final RestfulClientBuilder file(File file){
        this.mFile = file;
        return this;
    }

    /**
     * 重载: 指定上传的文件, 给定文件的路径
     * @param filePath 文件的路径
     * @return RestfulClientBuilder
     */
    public final RestfulClientBuilder file(String filePath){
        this.mFile = new File(filePath);
        return this;
    }

    /**
     * 文件下载相关
     * @param downloadDir 下载目录
     * @param downloadFileExtension 下载文件后保存时的扩展名
     * @param downloadFileName 下载后保存的文件的名字
     * @return RestfulClientBuilder
     */
    public final RestfulClientBuilder downloadFile(String downloadDir, String downloadFileExtension, String downloadFileName){
        this.mDownloadDir = downloadDir;
        this.mFileExtension = downloadFileExtension;
        this.mFileName = downloadFileName;
        return this;
    }
}
