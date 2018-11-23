package com.example.smartbro.ui.banner;

import android.widget.AdapterView;

import com.ToxicBakery.viewpager.transforms.DefaultTransformer;
import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.listener.OnItemClickListener;
import com.example.smarbro.R;

import java.util.ArrayList;

/**
 * Created by Justin Wang from SmartBro on 12/12/17.
 * 创建Banner轮播图的类, 只有基本功能
 */

public class BannerCreator {

    public static void setDefault(
            ConvenientBanner<String> convenientBanner,
            ArrayList<String> banners,
            OnItemClickListener clickListener)
    {
        convenientBanner
                .setPages(new HolderCreator(), banners)
                .setPageIndicator(new int[]{R.drawable.dot_normal, R.drawable.dot_focus})
                .setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.CENTER_HORIZONTAL)
                .setOnItemClickListener(clickListener)
                .setPageTransformer(new DefaultTransformer())
                .startTurning(8000)
                .setCanLoop(true);
    }
}
