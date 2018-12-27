package com.example.smartbroecommerce.main.checkout;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import com.example.smartbro.delegates.SmartbroDelegate;
import com.example.smartbroecommerce.R;
import com.example.smartbroecommerce.R2;
import com.example.smartbroecommerce.database.Product;
import com.example.smartbroecommerce.main.product.ListDelegate;
import butterknife.BindView;
import butterknife.OnClick;
import android.support.v7.widget.AppCompatTextView;

/**
 * 使用河马生鲜App付款页面
 * Created by Justin Wang from SmartBro on 26/12/18.
 */
public class ByHippoAppDelegate extends SmartbroDelegate {

    private Product product = null;

    @BindView(R2.id.tv_toolbar_cancel_checkout_text)
    AppCompatTextView cancelCheckoutText;
    @BindView(R2.id.tv_product_name_in_checkout)
    AppCompatTextView productNameText;
    @BindView(R2.id.tv_product_price_in_checkout)
    AppCompatTextView productPriceText;

    @OnClick(R2.id.tv_toolbar_cancel_checkout_text)
    void onCancelCheckoutClicked(){
        this.backToProductList();
    }

    @Override
    public Object setLayout() {
        return R.layout.delegate_hippo_app_checkout;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        Bundle args = getArguments();
        this.product = Product.find(args.getLong("productId"));
        if(this.product != null){
            this.productNameText.setText("您选择的产品: " + this.product.getName());
            this.productPriceText.setText("¥ " + this.product.getPrice());
        }
    }

    protected void backToProductList(){
        startWithPop(new ListDelegate());
    }
}
