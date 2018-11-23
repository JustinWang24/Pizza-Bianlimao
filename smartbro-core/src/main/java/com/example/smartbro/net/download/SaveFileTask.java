package com.example.smartbro.net.download;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import com.example.smartbro.app.ConfigType;
import com.example.smartbro.app.Smartbro;
import com.example.smartbro.net.callback.IRequest;
import com.example.smartbro.net.callback.ISuccess;
import com.example.smartbro.utils.file.FileUtil;

import java.io.File;
import java.io.InputStream;

import okhttp3.ResponseBody;

/**
 * Created by Justin Wang from SmartBro on 3/12/17.
 */

public class SaveFileTask extends AsyncTask<Object, Void, File> {

    private final IRequest REQUEST;
    private final ISuccess SUCCESS;

    public SaveFileTask(IRequest request, ISuccess success) {
        REQUEST = request;
        SUCCESS = success;
    }


    @Override
    protected File doInBackground(Object... objects) {

        // 下载的保存目录为第一个参数
        String downloadDir = (String) objects[0];
        // 文件扩展名为第二个参数
        String extension = (String) objects[1];
        // 请求体为第三个参数
        final ResponseBody body = (ResponseBody) objects[2];
        // 文件名为第四个参数
        String fileName = (String) objects[3];

        final InputStream inputStream = body.byteStream();

        if(downloadDir == null || downloadDir.equals("")){
            // 设置一个默认的下载路径
            downloadDir = (String) Smartbro.getConfigurationsMap().get(ConfigType.DEFAULT_DOWNLOAD_ROOT_DIR.name());
        }

        if(extension == null || extension.equals("")){
            // 设置一个默认的文件扩展名
            extension = "";
        }

        if(fileName == null || fileName.equals("")){
            // 返回一个默认生成的文件
            return FileUtil.writeToDisk(inputStream,downloadDir,extension.toUpperCase(),extension);
        }else{
            // 返回指定了名字的文件
            return FileUtil.writeToDisk(inputStream,downloadDir,fileName);
        }
    }

    @Override
    protected void onPostExecute(File file) {
        super.onPostExecute(file);

        if(SUCCESS != null){
            SUCCESS.onSuccess(file.getPath());
        }

        if(REQUEST != null){
            REQUEST.onRequestEnd();
        }

        // 自动安装 APK 文件
        autoInstallApk(file);
    }

    /**
     * 安装APK文件的处理方法
     * @param file
     */
    private void autoInstallApk(File file){
        if(FileUtil.getExtension(file.getPath()).equals("apk")){
            // 如果确认下载的文件名是apk
            final Intent install = new Intent();
            install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            install.setAction(Intent.ACTION_VIEW);
            install.setDataAndType(Uri.fromFile(file),"application/vnd.android.package-archive");
            Smartbro.getApplication().startActivity(install);
        }
    }
}
