package com.example.smartbroecommerce.main.converters;

import android.util.Log;

import com.example.smartbro.ui.recycler.DataConvertor;
import com.example.smartbro.ui.recycler.ItemType;
import com.example.smartbro.ui.recycler.MultipleFields;
import com.example.smartbro.ui.recycler.MultipleItemEntity;
import com.example.smartbroecommerce.R;
import com.example.smartbroecommerce.database.Position;
import com.example.smartbroecommerce.database.Product;
import com.example.smartbroecommerce.main.stock.StockManagerDelegate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Justin Wang from SmartBro on 29/12/17.
 */

public class PositionsListDataConverter extends DataConvertor {
    private StockManagerDelegate delegate = null;

    public PositionsListDataConverter(StockManagerDelegate delegate){
        super();
        this.delegate = delegate;
    }

    @Override
    public ArrayList<MultipleItemEntity> convert() {
        List<Position> positions = (List<Position>) this.getObjectData();

        for (Object positionObject : positions){
            Position position = (Position) positionObject;

            Product product = position.getProduct();
            if(product != null){
                final MultipleItemEntity entity = MultipleItemEntity.builder()
                        .setItemType(ItemType.POSITION_TYPE)
                        .setField(MultipleFields.POSITION_INDEX, position.getIndex())
                        .setField(MultipleFields.POSITION_STATUS, position.isAvailable())
                        .setField(MultipleFields.POSITION_PRODUCT_NAME, position.getProduct().getName())
                        .setField(MultipleFields.SPAN_SIZE,6)
                        .build();
                ENTITIES.add(entity);
            }else{
                final MultipleItemEntity entity = MultipleItemEntity.builder()
                        .setItemType(ItemType.POSITION_TYPE)
                        .setField(MultipleFields.POSITION_INDEX, position.getIndex())
                        .setField(MultipleFields.POSITION_STATUS, false)
                        .setField(MultipleFields.POSITION_PRODUCT_NAME, this.delegate.getString(R.string.text_na))
                        .setField(MultipleFields.SPAN_SIZE,6)
                        .build();
                ENTITIES.add(entity);
            }
        }
        return ENTITIES;
    }
}
