package com.example.smartbroecommerce.main.maker;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.example.smartbro.delegates.SmartbroDelegate;
import com.example.smartbroecommerce.R;

/**
 * Created by Justin Wang from SmartBro on 30/12/18.
 */
public class ErrorHappendDuringMakingDelegate extends SmartbroDelegate {

    private String productItemId = null;
    private String deliveryCode = null;

    @Override
    public Object setLayout() {
        return R.layout.delegate_error_happened_during_making;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {

    }
}
