package com.example.smartbroecommerce.main.pages;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.smartbro.delegates.SmartbroDelegate;
import com.example.smartbroecommerce.R;
import com.example.smartbroecommerce.R2;
import com.example.smartbroecommerce.database.Product;
import com.example.smartbroecommerce.database.ShoppingCart;
import com.example.smartbroecommerce.main.maker.ProcessingDelegate;
import com.example.smartbroecommerce.main.product.ListDelegate;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Justin Wang from SmartBro on 28/12/18.
 */
public class DeliveryCodeSuccessDelegate extends SmartbroDelegate {
    private Product product = null;     // 准备要烤的产品对象
    private String deliveryCode = null; // 传递过来的 自提码

    @BindView(R2.id.tv_toolbar_cancel_checkout_text)
    AppCompatTextView toolbarCancelDeliveryButton;
    @BindView(R2.id.btn_key_confirm_delivery_success)
    Button confirmDeliverySuccess;

    /**
     * 被选择的产品的信息
     */
    @BindView(R2.id.image_product_multiple_round_corner)
    ImageView productImage; // 尝试声明为 RoundedImageView 类型但是不知为何无法导入该类，只好根据lib的源码，声明为ImageView类型
    @BindView(R2.id.tv_product_name_multiple)
    AppCompatTextView productNameTextView;
    @BindView(R2.id.tv_product_price_text_multiple)
    AppCompatTextView productPriceTextView;
    @BindView(R2.id.tv_product_desc_multiple)
    AppCompatTextView productDescTextView;
    /**
     * 被选择的产品的信息 结束
     */

    @OnClick(R2.id.tv_toolbar_cancel_checkout_text)
    void onCancelButtonClicked(){
        startWithPop(new ListDelegate());
    }

    @OnClick(R2.id.btn_key_confirm_delivery_success)
    void onDeliveryConfirmButtonClicked(){
        // 点击确认取货按钮后的处理
        Bundle args = new Bundle();
        args.putString("itemId", this.product.getItemId());
        args.putString("deliveryCode", this.deliveryCode);

        //
        args.putInt("orderId",1);
        args.putBoolean("needCallBakingCmd",true);

        //
        SmartbroDelegate delegate = new ProcessingDelegate();
        delegate.setArguments(args);

        // 往购物车中添加一个产品
        ShoppingCart.getInstance().addProduct(this.product, delegate);

        // 加载视图
        startWithPop(delegate);
    }

    @Override
    public Object setLayout() {
        return R.layout.delegate_delivery_code_success;
    }

    /**
     * 根据传入的 itemId 显示产品的信息
     * @param savedInstanceState
     * @param rootView
     */
    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        Bundle args = getArguments();
        this.product = Product.find(args.getString("itemId"));
        this.deliveryCode = args.getString("deliveryCode");

        if(this.product != null){
            this.productNameTextView.setText(this.product.getName());
            this.productPriceTextView.setText("¥ " + this.product.getPrice());
            this.productDescTextView.setText(this.product.getSummary());

            final RequestOptions options = new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate()
                    .centerCrop();
            Glide.with(getProxyActivity())
                    .load(this.product.getMainImageUrl())
                    .apply(options)
                    .into(this.productImage);
        }
    }
}
