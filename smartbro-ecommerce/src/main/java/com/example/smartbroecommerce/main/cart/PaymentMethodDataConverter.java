package com.example.smartbroecommerce.main.cart;

import com.example.smartbro.ui.recycler.DataConvertor;
import com.example.smartbro.ui.recycler.ItemType;
import com.example.smartbro.ui.recycler.MultipleFields;
import com.example.smartbro.ui.recycler.MultipleItemEntity;
import com.example.smartbroecommerce.database.PaymentMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Justin Wang from SmartBro on 17/12/17.
 * 支付方式的数据转换
 */

public class PaymentMethodDataConverter extends DataConvertor {
    @Override
    public ArrayList<MultipleItemEntity> convert() {

        List<PaymentMethod> methods = PaymentMethod.findAll();

        for (PaymentMethod paymentMethod:methods) {
            final long id = paymentMethod.getId();
            final String name = paymentMethod.getName();
            final String priceText = paymentMethod.getPriceText();
            final double price = paymentMethod.getPrice();
            final int type = paymentMethod.getType();   // 支付方式种类的标识

            final MultipleItemEntity entity = MultipleItemEntity.builder()
                    .setItemType(ItemType.PAYMENT_METHOD)
                    .setField(MultipleFields.ITEM_TYPE, ItemType.PAYMENT_METHOD)
                    .setField(MultipleFields.ID, id)
                    .setField(MultipleFields.NAME, name)
                    .setField(MultipleFields.PRICE, price)
                    .setField(MultipleFields.PRICE_TEXT, priceText)
                    .setField(MultipleFields.TAG, type)
                    .setField(PaymentMethodFields.IS_SELECTED, false) // 默认该项没有被选择
                    .setField(MultipleFields.SPAN_SIZE,2)
                    .build();
            ENTITIES.add(entity);
        }

        return ENTITIES;
    }
}
