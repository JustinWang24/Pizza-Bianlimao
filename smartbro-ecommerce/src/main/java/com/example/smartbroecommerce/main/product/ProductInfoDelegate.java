package com.example.smartbroecommerce.main.product;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;

import com.example.smartbro.delegates.SmartbroDelegate;
import com.example.smartbroecommerce.R;
import com.example.smartbroecommerce.R2;
import com.example.smartbroecommerce.database.Product;

import butterknife.BindView;

/**
 * Created by Justin Wang from SmartBro on 13/12/17.
 * 用来显示一些商品额外信息的类，和商品详情类组合使用
 */

public class ProductInfoDelegate extends SmartbroDelegate {
    @BindView(R2.id.tv_goods_info_title)
    AppCompatTextView productTitleText;
    @BindView(R2.id.tv_goods_info_desc)
    AppCompatTextView productSummaryText;
    @BindView(R2.id.tv_goods_info_price)
    AppCompatTextView productPriceText;

    private Product product = null;

    @Override
    public Object setLayout() {
        return R.layout.layout_product_info;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 取参数
//        final Bundle args = getArguments();
//        final String productInfo = args.getString(ARG_GOODS_INFO);
//        final String productPriceText = args.getString(ARG_GOODS_PRICE_TEXT);
    }

    public static ProductInfoDelegate create(Product product){
//        final Bundle args = new Bundle();
//        args.putString(ARG_GOODS_INFO, product.getSummary());
//        args.putString(ARG_GOODS_PRICE_TEXT, product.getPriceText());

        final ProductInfoDelegate delegate = new ProductInfoDelegate();
        // 把产品对象保存起来
        delegate.product = product;
//        delegate.setArguments(args);
        return delegate;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        this.productTitleText.setText(this.product.getName());
        this.productSummaryText.setText(this.product.getSummary());
        this.productPriceText.setText(this.product.getPriceText());
        this.productSummaryText.setBackgroundColor(Color.TRANSPARENT);
        this.initPager();
    }

    /**
     * 就是加入类似 产品规格、性能参数等等附加的信息. 通过点击tab来切换并显示对应的内容
     * 在 Pizza 机暂时还不用，但是以后可以添加更多内容进来
     * 这里叫做Pager，配合TabPagerAdapter一起使用
     */
    private void initPager(){

    }
}
