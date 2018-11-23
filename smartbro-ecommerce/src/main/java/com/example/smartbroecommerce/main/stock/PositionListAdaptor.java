package com.example.smartbroecommerce.main.stock;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.View;

import com.example.smartbro.ui.recycler.DataConvertor;
import com.example.smartbro.ui.recycler.ItemType;
import com.example.smartbro.ui.recycler.MultipleFields;
import com.example.smartbro.ui.recycler.MultipleItemEntity;
import com.example.smartbro.ui.recycler.MultipleRecyclerAdaptor;
import com.example.smartbro.ui.recycler.MultipleViewHolder;
import com.example.smartbroecommerce.R;
import com.example.smartbroecommerce.database.Position;

import java.util.List;

/**
 * Created by Justin Wang from SmartBro on 29/12/17.
 */

public class PositionListAdaptor extends MultipleRecyclerAdaptor {
    private StockManagerDelegate delegate;
    /**
     * 构造函数
     *
     * @param data
     */
    protected PositionListAdaptor(List<MultipleItemEntity> data) {
        super(data);
        addItemType(ItemType.POSITION_TYPE, R.layout.item_position);
    }

    public static PositionListAdaptor create(DataConvertor converter){
        return new PositionListAdaptor(converter.convert());
    }

    public void setOwnDelegate(StockManagerDelegate delegate){
        this.delegate = delegate;
    }

    @Override
    protected void convert(MultipleViewHolder holder, MultipleItemEntity entity) {
        super.convert(holder, entity);

        // 取出控件
        LinearLayoutCompat wrap = holder.getView(R.id.simple_position_container);
        AppCompatTextView tvIndex = holder.getView(R.id.tv_position_index_multiple);

        // 取出值
        final int index = entity.getField(MultipleFields.POSITION_INDEX);
        final String productName = entity.getField(MultipleFields.POSITION_PRODUCT_NAME);
        final boolean positionStatus =
                entity.getField(MultipleFields.POSITION_STATUS);

        tvIndex.setText(Integer.toString(index) + ":" + productName);
//        tvProductName.setText(productName);

        if(positionStatus){
            wrap.setBackgroundColor(ContextCompat.getColor(mContext, R.color.weChatGreen));
        }

        wrap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delegate.setStockUpdated(true);
                final Position position = Position.findByIndex(index);
                if(position != null){
                    if(position.isAvailable()){
                        // 有效变为无效
                        position.disable();
                        v.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorRed));
                        delegate.updateChangedPositions(Integer.toString(index), false);
                    }else {
                        // 无效变为有效, 前提是必须有产品
                        if(position.getProductId() > 0){
                            position.enable();
                            v.setBackgroundColor(ContextCompat.getColor(mContext, R.color.weChatGreen));
                            delegate.updateChangedPositions(Integer.toString(index), true);
                        }
                    }
                }
            }
        });
    }
}
