package com.example.smartbro.ui.recycler;

/**
 * Created by Justin Wang from SmartBro on 9/12/17.
 * 定义了在app中显示的列表的item中的一些数据的类型
 */

public enum MultipleFields {
    ITEM_TYPE,
    TEXT,
    IMAGE_URL,
    BANNERS,
    SPAN_SIZE,      // item的显示宽度，可以有服务器传递过来
    ID,
    NAME,
    PRICE,          // 价格
    PRICE_TEXT,     // 价格的显示文字
    QUANTITY,       // 购物车项中的产品数量
    TAG,             // 备用的，不一定非要使用

    // 和Position相关
    POSITION_INDEX,
    POSITION_PRODUCT_NAME,
    POSITION_STATUS
}
