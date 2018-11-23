package com.example.smartbroecommerce.main.product;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.smartbro.delegates.SmartbroDelegate;
import com.example.smartbro.ui.recycler.ItemType;
import com.example.smartbro.ui.recycler.MultipleFields;
import com.example.smartbro.ui.recycler.MultipleItemEntity;
import com.example.smartbroecommerce.R;
import com.example.smartbroecommerce.R2;

import java.util.ArrayList;

import butterknife.BindView;

/**
 * Created by Justin Wang from SmartBro on 14/12/17.
 * 用来在tab中显示图片
 */

public class ImageDelegate extends SmartbroDelegate {

    private static final String ARG_PICTURES = "ARG_PICTURES";

    @BindView(R2.id.rv_image_container)
    RecyclerView imageContainer = null;

    @Override
    public Object setLayout() {
        return R.layout.delegate_image;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * 简单的工厂方法
     * @param imagesUrlList
     * @return ImageDelegate
     */
    public static ImageDelegate create(ArrayList<String> imagesUrlList){
        final Bundle args = new Bundle();
        args.putStringArrayList(ARG_PICTURES, imagesUrlList);
        final ImageDelegate delegate = new ImageDelegate();
        delegate.setArguments(args);
        return delegate;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        final LinearLayoutManager manager = new LinearLayoutManager(getContext());
        this.imageContainer.setLayoutManager(manager);
        this.initImages();
    }

    private void initImages(){
        final ArrayList<String> pictures = getArguments().getStringArrayList(ARG_PICTURES);
        final ArrayList<MultipleItemEntity> entities = new ArrayList<>();
        final int size;
        if(pictures != null){
            size = pictures.size();
            for (int i = 0; i < size; i++) {
                final String imageUrl = pictures.get(i);
                final MultipleItemEntity entity = MultipleItemEntity.builder()
                        .setItemType(ItemType.SINGLE_BIG_IMAGE)
                        .setField(MultipleFields.IMAGE_URL, imageUrl)
                        .build();
                entities.add(entity);
            }
            // 创建Adapter, 然后设置它
            final RecyclerImageAdapter adapter = new RecyclerImageAdapter(entities);
            this.imageContainer.setAdapter(adapter);
        }
    }
}
