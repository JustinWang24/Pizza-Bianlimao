package com.example.smartbroecommerce.main.product;

import android.util.Log;
import android.view.View;

import com.example.smartbro.ui.recycler.DataConvertor;
import com.example.smartbro.ui.recycler.MultipleFields;
import com.example.smartbro.ui.recycler.MultipleItemEntity;
import com.example.smartbro.ui.recycler.MultipleRecyclerAdaptor;
import com.example.smartbro.ui.recycler.MultipleViewHolder;

import java.util.List;
import com.example.smarbro.R;

/**
 * Created by Justin Wang from SmartBro on 13/12/17.
 */

public class ProductListAdaptor extends MultipleRecyclerAdaptor {

    private ListDelegate productListDelegate;

    /**
     * @param data
     */
    protected ProductListAdaptor(List<MultipleItemEntity> data) {
        super(data);
    }

    /**
     * 静态函数: 把DataConverter传进来, 然后从 converter取得数据后, 调用构造函数 进行赋值
     * @param dataConvertor
     * @return
     */
    public static ProductListAdaptor create(DataConvertor dataConvertor){
        return new ProductListAdaptor(dataConvertor.convert());
    }

    public void setOwnerDelegate(ListDelegate delegate){
        this.productListDelegate = delegate;
    }

    @Override
    protected void convert(final MultipleViewHolder holder, MultipleItemEntity entity) {
        super.convert(holder, entity);
        // 这里只处理产品的列表
        final View itemView = holder.itemView;

        /**
         * 处理点击事件: 当产品被点击后，显示高亮即可
         */
        itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                final int position = holder.getAdapterPosition();
                final long productId = getData().get(position).getField(MultipleFields.ID);
                // 加载产品详情页
                productListDelegate.showProductDetail(productId);
            }
        });
    }
}
