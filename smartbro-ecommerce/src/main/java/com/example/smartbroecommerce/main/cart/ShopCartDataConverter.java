package com.example.smartbroecommerce.main.cart;

import com.example.smartbro.ui.recycler.DataConvertor;
import com.example.smartbro.ui.recycler.ItemType;
import com.example.smartbro.ui.recycler.MultipleFields;
import com.example.smartbro.ui.recycler.MultipleItemEntity;
import com.example.smartbroecommerce.database.ShoppingCart;
import com.example.smartbroecommerce.database.ShoppingCartItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Justin Wang from SmartBro on 16/12/17.
 */

public class ShopCartDataConverter extends DataConvertor {
    @Override
    public ArrayList<MultipleItemEntity> convert() {

        List<ShoppingCartItem> items = ShoppingCart.getInstance().getShoppingCartItems();

        for (ShoppingCartItem item : items){
            final long cartItemId = item.getId();
            final long productId = item.getProductId();
            final String productName = item.getProductName();
            final String productImageUrl = item.getProductImage();
            final double price = item.getProductPrice();
            final int quantity = item.getQuantity();

            final MultipleItemEntity entity = MultipleItemEntity.builder()
                    .setField(MultipleFields.ITEM_TYPE, ItemType.SHOPPING_CART_ITEM)
                    .setField(MultipleFields.ID, cartItemId)
                    .setField(MultipleFields.NAME, productName)
                    .setField(MultipleFields.IMAGE_URL, productImageUrl)
                    .setField(MultipleFields.PRICE, price)
                    .setField(MultipleFields.QUANTITY, quantity)
                    .setField(MultipleFields.TAG, productId)
                    .setField(ShoppingCartItemFields.IS_SELECTED, false) // 默认该项没有被选择
                    .build();
            ENTITIES.add(entity);
        }

        return ENTITIES;
    }
}
