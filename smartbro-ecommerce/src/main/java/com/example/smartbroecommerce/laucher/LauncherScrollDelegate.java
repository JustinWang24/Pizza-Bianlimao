package com.example.smartbroecommerce.laucher;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.listener.OnItemClickListener;
import com.example.smartbro.delegates.SmartbroDelegate;
import com.example.smartbro.ui.launcher.LauncherHolderCreator;
import com.example.smartbro.ui.launcher.ScrollLauncherTag;
import com.example.smartbro.utils.storage.LattePreference;
import com.example.smartbroecommerce.R;

import java.util.ArrayList;

/**
 * Created by Justin Wang from SmartBro on 4/12/17.
 * 启动界面 内含轮播图的启动方式
 */

public class LauncherScrollDelegate extends SmartbroDelegate implements OnItemClickListener{

    // banner里面是资源文件图片，所以泛型的类型是Integer
    private ConvenientBanner<Integer> convenientBanner = null;

    private static final ArrayList<Integer> INTEGERS = new ArrayList<>();

    private void initBanner(){
//        INTEGERS.add(R.mipmap.launcher_10);
        INTEGERS.add(R.mipmap.mainbg);
        INTEGERS.add(R.mipmap.mainbg);
//        INTEGERS.add(R.mipmap.launcher_13);

        this.convenientBanner.setPages(
                new LauncherHolderCreator(),
                INTEGERS
        )
        .setPageIndicator(new int[]{R.drawable.dot_normal, R.drawable.dot_focus})
        .setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.CENTER_HORIZONTAL)
        .setOnItemClickListener(this)
        .setCanLoop(false);
    }

    @Override
    public Object setLayout() {
        this.convenientBanner = new ConvenientBanner<>(getContext());
        return this.convenientBanner;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        initBanner();
    }

    @Override
    public void onItemClick(int position) {
        // 当幻灯片中的 如果最后一页被点击过, 那么在preference中标记一下，表明app已经被启动过了
        if(position == INTEGERS.size() -1){
            LattePreference.setAppFlag(ScrollLauncherTag.APP_ALREADY_LAUNCHED.name(), true);

            // todo 检查用户是否已经登录了
        }
    }
}
