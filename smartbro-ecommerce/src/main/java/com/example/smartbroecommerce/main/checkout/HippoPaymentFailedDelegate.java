package com.example.smartbroecommerce.main.checkout;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;

import com.example.smartbro.delegates.SmartbroDelegate;
import com.example.smartbroecommerce.R;
import com.example.smartbroecommerce.R2;
import com.example.smartbroecommerce.database.Product;
import com.example.smartbroecommerce.main.product.ListDelegate;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Justin Wang from SmartBro on 27/12/18.
 */
public class HippoPaymentFailedDelegate extends SmartbroDelegate{
    private long productId;

    @BindView(R2.id.tv_product_name_in_payment_failed)
    AppCompatTextView productNameText;

    /**
     * 重新购买按钮
     */
    @BindView(R2.id.tv_back_to_products_list_in_payment_failed)
    AppCompatTextView backToProductList;

    @OnClick(R2.id.tv_back_to_products_list_in_payment_failed)
    void onBackToProductListClicked(){
        this.backToProductList();
    }

    @Override
    public Object setLayout() {
        return R.layout.delegate_hippo_payment_failed;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        Bundle args = getArguments();
        this.productNameText.setText(getString(R.string.text_product_you_select) + args.getString("productName"));
        this.productId = args.getLong("productId");
    }

    protected void backToProductList(){
        startWithPop(new ListDelegate());
    }
}
