package com.example.smartbro.delegates.bottom;

import java.util.LinkedHashMap;

/**
 * Created by Justin Wang from SmartBro on 9/12/17.
 */

public final class ItemBuilder {

    // 建立映射关系
    private final LinkedHashMap<BottomTabBean, BottomItemDelegate> ITEMS
            = new LinkedHashMap<>();

    static ItemBuilder builder(){
        return new ItemBuilder();
    }

    public final ItemBuilder addItem(BottomTabBean bean, BottomItemDelegate bottomItemDelegate){
        ITEMS.put(bean, bottomItemDelegate);
        return this;
    }

    public final ItemBuilder addItems(LinkedHashMap<BottomTabBean, BottomItemDelegate> items){
        ITEMS.putAll(items);
        return this;
    }

    public final LinkedHashMap<BottomTabBean,BottomItemDelegate> build(){
        return ITEMS;
    }
}
