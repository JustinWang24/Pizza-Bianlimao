package com.example.smartbroecommerce.main.checkout;

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
 * Created by Justin Wang from SmartBro on 27/12/18.
 */
public class HippoIsPayingDelegate extends SmartbroDelegate {
    private Product product = null;

    @BindView(R2.id.tv_product_name_in_paying)
    AppCompatTextView productNameText;
    @BindView(R2.id.tv_product_price_in_paying)
    AppCompatTextView productPriceText;

    @Override
    public Object setLayout() {
        return R.layout.delegate_hippo_is_paying;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        Bundle args = getArguments();
        this.product = Product.find(args.getLong("productId"));
        if(this.product != null){
            this.productNameText.setText(getString(R.string.text_product_you_select) + this.product.getName());
            this.productPriceText.setText("¥ " + this.product.getPrice());
        }
    }
}
