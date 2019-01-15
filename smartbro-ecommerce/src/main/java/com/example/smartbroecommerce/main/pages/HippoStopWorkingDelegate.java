package com.example.smartbroecommerce.main.pages;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.listener.OnItemClickListener;
import com.example.smartbro.delegates.SmartbroDelegate;
import com.example.smartbro.ui.banner.BannerCreator;
import com.example.smartbroecommerce.R;
import com.example.smartbroecommerce.R2;
import com.example.smartbroecommerce.utils.BannerTool;

import butterknife.BindView;

/**
 * Created by Justin Wang from SmartBro on 28/12/18.
 */
public class HippoStopWorkingDelegate extends SmartbroDelegate {

    @BindView(R2.id.ads_banner)
    ConvenientBanner<String> convenientBanner;

    @Override
    public Object setLayout() {
        return R.layout.delegate_stop_working_new;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {

        BannerCreator.setDefault(
                this.convenientBanner,
                BannerTool.GetInstance().getBannerImages(),
                new OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {

                    }
                }
        );
    }
}
