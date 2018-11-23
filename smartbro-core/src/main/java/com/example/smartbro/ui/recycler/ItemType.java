package com.example.smartbro.ui.recycler;

/**
 * Created by Justin Wang from SmartBro on 9/12/17.
 * 定义返回的数据的类型
 */

public class ItemType {

    public static final int TEXT_ONLY       = 1 ;   // 只是文字
    public static final int IMAGE_ONLY      = 2 ;   // 只是图片
    public static final int TEXT_PLUS_IMAGE = 3 ;   // 图文并茂
    public static final int BANNER          = 4 ;   // banner广告
    public static final int PRODUCT         = 5 ;   // 产品信息
    public static final int SINGLE_BIG_IMAGE= 6 ;   // 单独的一张大图片
    public static final int SHOPPING_CART_ITEM = 7; // 购物车中的购物项类型
    public static final int SHOPPING_CART_ITEM_QUANTITY = 8; // 购物车中的购物项中的产品数量
    public static final int SHOPPING_CART_ITEM_PRICE_TEXT = 9; // 购物车中的购物项中的价格文字信息
    public static final int PAYMENT_METHOD = 10;    // 支付方式
    public static final int POSITION_TYPE  = 11;    // 位置信息
}
