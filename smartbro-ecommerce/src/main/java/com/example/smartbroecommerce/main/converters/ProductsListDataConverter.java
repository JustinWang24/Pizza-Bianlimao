package com.example.smartbroecommerce.main.converters;

import com.example.smartbro.ui.recycler.DataConvertor;
import com.example.smartbro.ui.recycler.ItemType;
import com.example.smartbro.ui.recycler.MultipleFields;
import com.example.smartbro.ui.recycler.MultipleItemEntity;
import com.example.smartbroecommerce.database.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Justin Wang from SmartBro on 12/12/17.
 * 从本地的数据库中加载产品数据，然后放到 entities 中准备渲染
 */

public class ProductsListDataConverter extends DataConvertor {

    /**
     * 从本地的数据库中加载产品数据，然后放到 entities 中准备渲染
     * @return ArrayList<MultipleItemEntity>
     */
    @Override
    public ArrayList<MultipleItemEntity> convert() {
        // 获取数据
        List<Product> products = (List<Product>) this.getObjectData();

        for (Object productObject:products){
            // 取得数据并强转为Product类型
            Product product = (Product) productObject;

            // 产品都是图文型的结构
            int productItemType = ItemType.PRODUCT;

            final MultipleItemEntity entity = MultipleItemEntity.builder()
                    .setItemType(productItemType)
                    .setField(MultipleFields.ID, product.getId())
                    .setField(MultipleFields.IMAGE_URL, product.getMainImageUrl())
                    .setField(MultipleFields.TEXT, product.getName())
                    .setField(MultipleFields.PRICE, product.getPrice())
                    .setField(MultipleFields.PRICE_TEXT, product.getPriceText())
                    .setField(MultipleFields.SPAN_SIZE,2)
                    .build();

            ENTITIES.add(entity);
        }

        return ENTITIES;
    }
}
