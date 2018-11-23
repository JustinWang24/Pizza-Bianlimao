package com.example.smartbro.ui;

import android.content.Context;
import android.support.v7.app.AppCompatDialog;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.example.smarbro.R;
import com.example.smartbro.utils.DimenUtil;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;

/**
 * Created by Justin Wang from SmartBro on 3/12/17.
 */

public class SmartbroLoader {

    private static final int LOADER_SIZE_SCALE = 8;
    private static final int LOADER_OFFSET_SCALE = 10;

    private static final ArrayList<AppCompatDialog> LOADERS = new ArrayList<>();

    private static final String DEFAULT_LOADER = LoaderStyle.BallPulseIndicator.name();

    /**
     * 显示应用程序处于Loading状态的方法
     * @param context
     * @param style
     */
    public static void showLoading(Context context, Enum<LoaderStyle> style){
        showLoading(context, style.name());
    }

    /**
     * 显示应用程序处于Loading状态的方法
     * @param context
     * @param type
     */
    public static void showLoading(Context context, String type){
        final AppCompatDialog dialog = new AppCompatDialog(context, R.style.fullScreenLoaderDialog);
        final AVLoadingIndicatorView avLoadingIndicatorView = LoaderCreator.create(type,context);

        dialog.setContentView(avLoadingIndicatorView);

        int deviceWidth = DimenUtil.getScreenWidth();
        int deviceHeight = DimenUtil.getScreenHeight();

        final Window dialogWindow = dialog.getWindow();

        if(dialogWindow != null){
            WindowManager.LayoutParams layoutParams = dialogWindow.getAttributes();
            layoutParams.width = deviceWidth / LOADER_SIZE_SCALE;
            layoutParams.height = deviceHeight / LOADER_SIZE_SCALE;
            layoutParams.height = layoutParams.height + deviceHeight/LOADER_OFFSET_SCALE;
            layoutParams.gravity = Gravity.CENTER;
        }

        LOADERS.add(dialog);
        dialog.show();
    }

    /**
     * 显示应用程序处于Loading状态的方法，使用默认的Loader Icon
     * @param context
     */
    public static void showLoading(Context context){
        showLoading(context,DEFAULT_LOADER);
    }

    /**
     * 停止Loader的显示
     */
    public static void stopLoading(){
        for(AppCompatDialog dialog : LOADERS){
            if(dialog != null){
                if(dialog.isShowing()){
                    dialog.cancel(); // 用cancel的方式隐藏Loader, 方便未来执行可能的回调方法
//                    dialog.dismiss();
                }
            }
        }
    }
}
