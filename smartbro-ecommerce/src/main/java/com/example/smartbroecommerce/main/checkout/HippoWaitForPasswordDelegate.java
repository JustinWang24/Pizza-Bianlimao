package com.example.smartbroecommerce.main.checkout;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.example.smartbro.delegates.SmartbroDelegate;
import com.example.smartbroecommerce.R;

/**
 * Created by Justin Wang from SmartBro on 27/12/18.
 */
public class HippoWaitForPasswordDelegate extends SmartbroDelegate {
    @Override
    public Object setLayout() {
        return R.layout.delegate_hippo_wait_for_password;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {

    }
}
