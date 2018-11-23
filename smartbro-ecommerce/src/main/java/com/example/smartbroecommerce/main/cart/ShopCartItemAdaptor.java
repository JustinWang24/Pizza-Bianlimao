package com.example.smartbroecommerce.main.cart;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.smartbro.app.Smartbro;
import com.example.smartbro.ui.recycler.ItemType;
import com.example.smartbro.ui.recycler.MultipleFields;
import com.example.smartbro.ui.recycler.MultipleItemEntity;
import com.example.smartbro.ui.recycler.MultipleRecyclerAdaptor;
import com.example.smartbro.ui.recycler.MultipleViewHolder;
import com.example.smartbroecommerce.R;
import com.example.smartbroecommerce.database.DatabaseManager;
import com.example.smartbroecommerce.database.MachineProfile;
import com.example.smartbroecommerce.database.MachineProfileDao;
import com.example.smartbroecommerce.database.ShoppingCart;
import com.joanzapata.iconify.widget.IconTextView;

import java.util.List;

/**
 * Created by Justin Wang from SmartBro on 16/12/17.
 */

public class ShopCartItemAdaptor extends MultipleRecyclerAdaptor {

    private static final RequestOptions OPTIONS = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .dontAnimate()
            .centerCrop();

    // 表示是否所有的item都被选择了
    private boolean isAllItemsBeSelected = false;

    private ShopCartDelegate shopCartDelegateInstance = null;

    /**
     * 构造函数
     *
     * @param data
     */
    protected ShopCartItemAdaptor(List<MultipleItemEntity> data) {
        super(data);
        addItemType(ItemType.SHOPPING_CART_ITEM, R.layout.item_shop_cart);
    }

    @Override
    protected void convert(MultipleViewHolder holder, final MultipleItemEntity entity) {
        super.convert(holder, entity);
        final long cartItemId = entity.getField(MultipleFields.ID);
        final long productId = entity.getField(MultipleFields.TAG);
        final String productName = entity.getField(MultipleFields.NAME);
        final String productImage = entity.getField(MultipleFields.IMAGE_URL);
        final int quantity = entity.getField(MultipleFields.QUANTITY);
        final double price = entity.getField(MultipleFields.PRICE);
        double subtotal = price * quantity;


        MachineProfileDao dao = DatabaseManager.getInstance().getMachineProfileDao();
        List<MachineProfile> list = dao.queryBuilder().list();
        String currencySymbol = "$";
        for (MachineProfile machineProfile:list){
            currencySymbol = machineProfile.getCurrencySymbol();
        }

        // 取出控件
        final AppCompatImageView imageThumb = holder.getView(R.id.image_item_shop_cart);
        final AppCompatTextView productNameView = holder.getView(R.id.tv_item_shop_cart_title);
        final AppCompatTextView priceView = holder.getView(R.id.tv_item_shop_cart_price);
        final AppCompatTextView subtotalView = holder.getView(R.id.tv_item_subtotal);
        final AppCompatTextView quantityView = holder.getView(R.id.tv_item_shop_cart_count);
        final IconTextView isItemSelectedView = holder.getView(R.id.icon_item_shop_cart);
        final LinearLayoutCompat theRowWrapper = holder.getView(R.id.ll_cart_item_wrap);


        // 给控件赋值
        productNameView.setText(productName);
        priceView.setText(mContext.getString(R.string.text_price) + currencySymbol + Double.toString(price));
        quantityView.setText(Integer.toString(quantity));
        subtotalView.setText(mContext.getString(R.string.text_subtotal) + mContext.getString(R.string.text_currency_symbol) + Double.toString(subtotal));

        Glide.with(mContext)
                .load(productImage)
                .into(imageThumb);

        //
        entity.setField(ShoppingCartItemFields.IS_SELECTED, this.isAllItemsBeSelected);
        final boolean isItemSelected = entity.getField(ShoppingCartItemFields.IS_SELECTED);

        // icon select 勾勾控件事件的监听
        if(isItemSelected){
            isItemSelectedView.setTextColor(
                    ContextCompat.getColor(Smartbro.getApplication(),R.color.colorRed)
            );
        }else {
            isItemSelectedView.setTextColor(
                    ContextCompat.getColor(Smartbro.getApplication(),R.color.lightGray)
            );
        }
        theRowWrapper.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                final boolean currentSelected = entity.getField(ShoppingCartItemFields.IS_SELECTED);
                if(currentSelected){
                    isItemSelectedView.setTextColor(
                            ContextCompat.getColor(Smartbro.getApplication(),R.color.lightGray)
                    );
                    entity.setField(ShoppingCartItemFields.IS_SELECTED, false);
                }else {
                    isItemSelectedView.setTextColor(
                            ContextCompat.getColor(Smartbro.getApplication(),R.color.colorRed)
                    );
                    entity.setField(ShoppingCartItemFields.IS_SELECTED, true);
                }
            }
        });

        // 修改Item数量的操作
        // 监听减少的事件
//        final IconTextView iconMinus = holder.getView(R.id.icon_item_minus);
//        iconMinus.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final int currentQuantity = entity.getField(MultipleFields.QUANTITY);
//                if(currentQuantity > 0){
//                    try {
//                        ShoppingCart.getInstance().removeProductFrom(cartItemId);
//                        entity.setField(MultipleFields.QUANTITY, currentQuantity -1);
//                        quantityView.setText(Integer.toString(currentQuantity - 1));
//                        // 需要更新小计
//                        subtotalView.setText(
//                                mContext.getString(R.string.text_subtotal) +
//                                        mContext.getString(R.string.text_currency_symbol) +
//                                        Double.toString(price * (currentQuantity - 1)));
//                    }catch (Exception e){
//                        Toast.makeText(mContext,e.getMessage(),Toast.LENGTH_LONG).show();
//                    }
//                }else {
//                    // Todo 自动删除item, 当quantity是0的时候
//                    isItemSelectedView.setTextColor(
//                            ContextCompat.getColor(Smartbro.getApplication(),R.color.green)
//                    );
//                    entity.setField(ShoppingCartItemFields.IS_SELECTED, true);
//                    shopCartDelegateInstance.onDeleteSelectedItemsTriggerClicked();
//                }
//                shopCartDelegateInstance.updateCartTotal();
//            }
//        });


//        final IconTextView iconPlus = holder.getView(R.id.icon_item_plus);
//        iconPlus.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final int currentQuantity = entity.getField(MultipleFields.QUANTITY);
//                final int cartProductCountTotal = ShoppingCart.getInstance().getProductsCount();
//                // 保证最多的添加数量小于系统后台设置的
//                final int maxPizzaNumber = MachineProfile.getInstance().getMaxProductsToSellOneTime();
//                if(cartProductCountTotal < maxPizzaNumber){
//                    // 添加一个产品进去 并返回添加后的值
//                    try {
//                        final int latestProductsCountTotal = ShoppingCart.getInstance().addProduct(productId, null);
//                        if(latestProductsCountTotal > cartProductCountTotal){
//                            // 表示添加成功了
//                            entity.setField(MultipleFields.QUANTITY, currentQuantity + 1);
//                            quantityView.setText(Integer.toString(currentQuantity + 1));
//                            // 需要更新小计
//                            subtotalView.setText(
//                                    mContext.getString(R.string.text_subtotal) +
//                                            mContext.getString(R.string.text_currency_symbol) +
//                                            Double.toString(price * (currentQuantity + 1)));
//                            // Todo 需要去更新总计
//                        }else {
//                            Toast.makeText(
//                                    mContext,
//                                    productName + " " + mContext.getString(R.string.msg_product_out_of_stock),
//                                    Toast.LENGTH_LONG
//                            ).show();
//                        }
//                    }catch (Exception e){
//                        Toast.makeText(
//                                mContext,
//                                e.getMessage(),
//                                Toast.LENGTH_LONG
//                        ).show();
//                    }
//                }else {
//                    Toast.makeText(
//                            Smartbro.getApplication(),
//                            Smartbro.getApplication().getString(R.string.msg_cart_is_full),
//                            Toast.LENGTH_LONG
//                    ).show();
//                }
//                shopCartDelegateInstance.updateCartTotal();
//            }
//        });
    }

    public void setIsSelectAll(boolean allSelected){
        this.isAllItemsBeSelected = allSelected;
    }

    /**
     * 设置一个Delegate的引用
     * @param delegateInstance
     */
    public void setShopCartDelegateInstance(ShopCartDelegate delegateInstance){
        this.shopCartDelegateInstance = delegateInstance;
    }
}
