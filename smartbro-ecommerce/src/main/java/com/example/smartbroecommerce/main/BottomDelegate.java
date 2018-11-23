package com.example.smartbroecommerce.main;

import android.graphics.Color;

import com.example.smartbro.delegates.bottom.BaseBottomDelegate;
import com.example.smartbro.delegates.bottom.BottomItemDelegate;
import com.example.smartbro.delegates.bottom.BottomTabBean;
import com.example.smartbro.delegates.bottom.ItemBuilder;
import com.example.smartbroecommerce.main.pages.*;

import java.util.LinkedHashMap;

/**
 * Created by Justin Wang from SmartBro on 9/12/17.
 */

public class BottomDelegate extends BaseBottomDelegate {
    @Override
    public LinkedHashMap<BottomTabBean, BottomItemDelegate> setItems(ItemBuilder itemBuilder) {
        final LinkedHashMap<BottomTabBean, BottomItemDelegate> items
                = new LinkedHashMap<>();
        items.put(new BottomTabBean("{fa-home}","主页"), new IndexDelegate());
        items.put(new BottomTabBean("{fa-sort}","分类"), new SortDelegate());
        items.put(new BottomTabBean("{fa-compass}","发现"), new IndexDelegate());
        items.put(new BottomTabBean("{fa-shopping-cart}","购物车"), new IndexDelegate());
        items.put(new BottomTabBean("{fa-user}","我的"), new IndexDelegate());

        return itemBuilder.addItems(items).build();
    }

    @Override
    public int setIndexDelegate() {
        return 0;
    }

    @Override
    public int setClickedColor() {
        return Color.parseColor("#ffff8800");
    }
}
