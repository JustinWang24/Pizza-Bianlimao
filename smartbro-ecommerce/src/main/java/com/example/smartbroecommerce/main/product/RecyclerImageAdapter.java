package com.example.smartbroecommerce.main.product;

import android.support.v7.widget.AppCompatImageView;

import com.bumptech.glide.Glide;
import com.example.smartbro.ui.recycler.ItemType;
import com.example.smartbro.ui.recycler.MultipleFields;
import com.example.smartbro.ui.recycler.MultipleItemEntity;
import com.example.smartbro.ui.recycler.MultipleRecyclerAdaptor;
import com.example.smartbro.ui.recycler.MultipleViewHolder;
import com.example.smartbroecommerce.R;

import java.util.List;

/**
 * Created by Justin Wang from SmartBro on 14/12/17.
 */

public class RecyclerImageAdapter extends MultipleRecyclerAdaptor {

    // todo 检查Glide升级之后的 RequestOptions

    /**
     * 构造函数
     *
     * @param data
     */
    protected RecyclerImageAdapter(List<MultipleItemEntity> data) {
        super(data);
        this.addItemType(ItemType.SINGLE_BIG_IMAGE, R.layout.item_image);
    }

    @Override
    protected void convert(MultipleViewHolder holder, MultipleItemEntity entity) {
        super.convert(holder, entity);
        final int type = holder.getItemViewType();
        switch (type){
            case ItemType.SINGLE_BIG_IMAGE:
                final AppCompatImageView imageView = holder.getView(R.id.image_rv_item);
                final String url = entity.getField(MultipleFields.IMAGE_URL);
                Glide.with(mContext)
                        .load(url)
                        .into(imageView);
                break;
            default:
                break;
        }
    }
}
