package com.example.smartbro.ui.banner;

import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;

/**
 * Created by Justin Wang from SmartBro on 12/12/17.
 */

public class HolderCreator implements CBViewHolderCreator<BannerImageHolder> {
    @Override
    public BannerImageHolder createHolder() {
        return new BannerImageHolder();
    }
}
