package com.example.smartbro.ui.recycler;

import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.listener.OnItemClickListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.example.smarbro.R;
import com.example.smartbro.ui.banner.BannerCreator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Justin Wang from SmartBro on 11/12/17.
 * 渲染列表的UI的基础类, 和 MultipleViewHolder 结合使用
 */

public class MultipleRecyclerAdaptor extends
        BaseMultiItemQuickAdapter<MultipleItemEntity, MultipleViewHolder>
    implements BaseQuickAdapter.SpanSizeLookup, OnItemClickListener{


    private boolean isBannerInitialized = false; // 保存是否Banner的slider已经被初始化了. 确保初始化一次，避免重复加载

    @Override
    public void onItemClick(int i) {
        // Todo Banner的图片被点击时的处理
        Log.i("Info","Banner的图片被点击了. MultipleRecyclerAdaptor->onItemClick");
    }

    /**
     * 构造函数
     * @param data
     */
    protected MultipleRecyclerAdaptor(List<MultipleItemEntity> data) {
        super(data);
        // 进行初始化
        init();
    }

    /**
     * 静态方法: 直接把数据传进来后 调用构造函数 进行赋值
     * @param data
     * @return
     */
    public static MultipleRecyclerAdaptor create(List<MultipleItemEntity> data){
        return new MultipleRecyclerAdaptor(data);
    }

    /**
     * 静态函数: 把DataConverter传进来, 然后从 converter取得数据后, 调用构造函数 进行赋值
     * @param dataConvertor
     * @return
     */
    public static MultipleRecyclerAdaptor create(DataConvertor dataConvertor){
        return new MultipleRecyclerAdaptor(dataConvertor.convert());
    }

    /**
     * 这里实现什么样的数据会转换成什么样的视图
     * @param holder
     * @param entity
     */
    @Override
    protected void convert(MultipleViewHolder holder, MultipleItemEntity entity) {
        // 取出数据
        final String text;
        final String imageUrl;
        final ArrayList<String> bannerImages;
        switch (holder.getItemViewType()){
            case ItemType.TEXT_ONLY:
                text = entity.getField(MultipleFields.TEXT);
                this.setTextToView(text, R.id.text_single, holder);
                break;
            case ItemType.IMAGE_ONLY:
                imageUrl = entity.getField(MultipleFields.IMAGE_URL);
                this.setImageToView(imageUrl, R.id.image_single, holder);
                break;
            case ItemType.TEXT_PLUS_IMAGE:
                text = entity.getField(MultipleFields.TEXT);
                imageUrl = entity.getField(MultipleFields.IMAGE_URL);
                this.setTextToView(text, R.id.tv_multiple, holder);
                this.setImageToView(imageUrl, R.id.image_multiple,holder);
                break;
            case ItemType.BANNER:
                if(!this.isBannerInitialized){
                    // 如果banner还没有被加载
                    bannerImages = entity.getField(MultipleFields.BANNERS);
                    final ConvenientBanner<String> convenientBanner = holder.getView(R.id.banner_recycler_item);
                    BannerCreator.setDefault(convenientBanner,bannerImages,this);
                    this.isBannerInitialized = true;
                }
                break;
            case ItemType.PRODUCT:
                // 对于产品类型的数据
                text = entity.getField(MultipleFields.TEXT);
                imageUrl = entity.getField(MultipleFields.IMAGE_URL);
                final String priceText = entity.getField(MultipleFields.PRICE_TEXT);
                this.setTextToView(text, R.id.tv_product_name_multiple, holder);
                this.setTextToView(priceText, R.id.tv_product_price_text_multiple, holder);
                this.setImageToView(imageUrl, R.id.image_product_multiple,holder);
                break;
            case ItemType.PAYMENT_METHOD:

                break;
            default:
                break;
        }
    }

    /**
     * 绑定文字与特定的view
     * @param text
     * @param viewId
     * @param holder
     */
    private void setTextToView(String text, int viewId, MultipleViewHolder holder){
        holder.setText(viewId, text);
    }

    /**
     * 绑定图片与特定的view
     * @param imageUrl
     * @param viewId
     * @param holder
     */
    private void setImageToView(String imageUrl, int viewId, MultipleViewHolder holder){
        final RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontAnimate()
                .centerCrop();

        Glide.with(mContext)
            .load(imageUrl)
            .apply(options)
            .into((ImageView) holder.getView(viewId));
    }

    /**
     * 初始化： 针对不同的数据类型, 加载布局
     */
    private void init(){
        // 设置不同的布局
        addItemType(ItemType.TEXT_ONLY, R.layout.item_multiple_text);
        addItemType(ItemType.IMAGE_ONLY, R.layout.item_multiple_image);
        addItemType(ItemType.TEXT_PLUS_IMAGE, R.layout.item_multiple_text_plus_image);
        addItemType(ItemType.BANNER, R.layout.item_multiple_banner);
        addItemType(ItemType.PRODUCT, R.layout.item_multiple_simple_product);       // 对于产品的布局
        addItemType(ItemType.PAYMENT_METHOD, R.layout.item_multiple_payment_method_hori);        // 对于支付方式的布局

        // 设置宽度的监听
        setSpanSizeLookup(this);
        openLoadAnimation();
        // 多次执行动画
        isFirstOnly(false);
    }

    @Override
    protected MultipleViewHolder createBaseViewHolder(View view) {
        return MultipleViewHolder.create(view);
    }

    @Override
    public int getSpanSize(GridLayoutManager gridLayoutManager, int position) {
        return getData().get(position).getField(MultipleFields.SPAN_SIZE);
    }
}