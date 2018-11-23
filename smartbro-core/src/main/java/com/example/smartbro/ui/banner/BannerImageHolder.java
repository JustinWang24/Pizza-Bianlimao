package com.example.smartbro.ui.banner;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.view.View;

import com.bigkoo.convenientbanner.holder.Holder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

/**
 * Created by Justin Wang from SmartBro on 12/12/17.
 */

public class BannerImageHolder implements Holder{
    private AppCompatImageView imageView = null;

    @Override
    public View createView(Context context) {
        this.imageView = new AppCompatImageView(context);
        return this.imageView;
    }

    @Override
    public void UpdateUI(Context context, int position, Object imageUrl) {
//        Glide.with(context)
//            .load(imageUrl)
//            .diskCacheStrategy(DiskCacheStrategy.ALL)
//            .dontAnimate()
//            .centerCrop()
//            .into(this.imageView);
        final RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontAnimate()
                .centerCrop();

        Glide.with(context)
                .load(imageUrl)
                .apply(options)
                .into(this.imageView);
    }
}
