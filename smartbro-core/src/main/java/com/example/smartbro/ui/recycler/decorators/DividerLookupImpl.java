package com.example.smartbro.ui.recycler.decorators;

import android.support.annotation.ColorInt;

import com.choices.divider.Divider;
import com.choices.divider.DividerItemDecoration;

/**
 * Created by Justin Wang from SmartBro on 13/12/17.
 */

public class DividerLookupImpl implements DividerItemDecoration.DividerLookup {

    private final int COLOR;
    private final int SIZE;

    public DividerLookupImpl(@ColorInt int color, int size) {
        this.COLOR = color;
        this.SIZE = size;
    }

    @Override
    public Divider getVerticalDivider(int i) {
        return new Divider.Builder().size(SIZE).color(COLOR).build();
    }

    @Override
    public Divider getHorizontalDivider(int i) {
        return new Divider.Builder().size(SIZE).color(COLOR).build();
    }
}
