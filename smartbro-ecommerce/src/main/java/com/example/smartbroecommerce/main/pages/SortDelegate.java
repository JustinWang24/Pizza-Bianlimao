package com.example.smartbroecommerce.main.pages;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.example.smartbro.delegates.bottom.BottomItemDelegate;
import com.example.smartbroecommerce.R;

/**
 * Created by Justin Wang from SmartBro on 9/12/17.
 */

public class SortDelegate extends BottomItemDelegate {
    @Override
    public Object setLayout() {
        return R.layout.delegate_page_sort;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {

    }
}
