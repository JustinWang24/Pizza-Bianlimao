package com.example.smartbroecommerce.main.product;

import android.graphics.Color;
import android.util.Log;
import android.view.View;

import com.example.smartbro.ui.recycler.DataConvertor;
import com.example.smartbro.ui.recycler.MultipleFields;
import com.example.smartbro.ui.recycler.MultipleItemEntity;
import com.example.smartbro.ui.recycler.MultipleRecyclerAdaptor;
import com.example.smartbro.ui.recycler.MultipleViewHolder;

import java.util.List;
import com.example.smarbro.R;
import com.example.smartbroecommerce.database.Product;

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

        this.productListDelegate.getHolders().add(holder);

        /**
         * 处理点击事件: 当产品被点击后，显示高亮即可
         */
        itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                // 先把所有的产品item的UI复原
                for (MultipleViewHolder theHolder: productListDelegate.getHolders()){
                    theHolder.itemView.setBackgroundResource(R.drawable.product_new_item_bg);
                    theHolder.setTextColor(R.id.tv_product_name_multiple, Color.DKGRAY);
                    theHolder.setTextColor(R.id.tv_product_desc_multiple, Color.DKGRAY);
                }

                final int position = holder.getAdapterPosition();
                final long productId = getData().get(position).getField(MultipleFields.ID);

                /**
                 * 获取当前选定的产品
                 */
                final Product product = Product.find(productId);
                productListDelegate.setSelectedProduct(product);

                // 只标记一个被选中的状态即可
                itemView.setBackgroundResource(R.drawable.product_new_item_bg_highlight);
                holder.setTextColor(R.id.tv_product_name_multiple, Color.WHITE);
                holder.setTextColor(R.id.tv_product_desc_multiple, Color.WHITE);
            }
        });
    }
}
