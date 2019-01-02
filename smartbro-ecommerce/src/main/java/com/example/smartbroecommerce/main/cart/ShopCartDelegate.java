package com.example.smartbroecommerce.main.cart;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.support.v7.widget.AppCompatTextView;

import com.example.smartbro.delegates.SmartbroDelegate;
import com.example.smartbro.ui.recycler.MultipleItemEntity;
import com.example.smartbro.utils.timer.BaseTimerTask;
import com.example.smartbro.utils.timer.ITimerListener;
import com.example.smartbroecommerce.R;
import com.example.smartbroecommerce.R2;
import com.example.smartbroecommerce.database.MachineProfile;
import com.example.smartbroecommerce.database.PaymentMethod;
import com.example.smartbroecommerce.database.ShoppingCart;
import com.example.smartbroecommerce.main.product.ListDelegate;
import com.joanzapata.iconify.widget.IconTextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Justin Wang from SmartBro on 14/12/17.
 * 购物车界面
 */

public class ShopCartDelegate extends SmartbroDelegate implements ITimerListener{

    @BindView(R2.id.rv_shop_cart)
    RecyclerView shoppingCartItemsView = null;
    private ShopCartItemAdaptor shopCartItemAdaptor = null;

    // 支付方式列表
    @BindView(R2.id.rv_payment_methods)
    RecyclerView paymentMethodsView = null;
    private PaymentMethodAdaptor paymentMethodAdaptor = null;

    // 上一次点击的时间戳
    private Timer mTimer = null;

    // 全选部分
    @BindView(R2.id.ll_select_all_cart_items_wrap)
    LinearLayoutCompat selectAllTrigger = null;
    @BindView(R2.id.icon_shop_cart_select_all)
    IconTextView iconSelectedAll = null;
    @BindView(R2.id.tv_shop_cart_select_all)
    AppCompatTextView mtvSelectedAll = null;

    // 删除选中的
    @BindView(R2.id.tv_top_shop_cart_remove_selected)
    AppCompatTextView mtvDeleteSelectedItemsTrigger = null;
    @BindView(R2.id.tv_top_shop_cart_clear)
    AppCompatTextView emptyCartTrigger = null;

    // 金额总计
    @BindView(R2.id.tv_shop_cart_total_price)
    AppCompatTextView mtvTotalText;
    @BindView(R2.id.tv_continue_shopping)
    AppCompatTextView mtvContinueShopping = null;

    @BindView(R2.id.toolbar_top_shop_cart_clear)
    Toolbar toolbar = null;

    @BindView(R2.id.tv_shopping_cart_wrap)
    LinearLayoutCompat wrap = null;

    /**
     * 更新购物车总额的方法
     */
    public void updateCartTotal(){
        this.mtvTotalText.setText(getString(R.string.text_currency_symbol) + Double.toString(ShoppingCart.getInstance().getTotal()));
    }

    /**
     * 清空购物车的事件响应
     */
    @OnClick(R2.id.tv_top_shop_cart_clear)
    void onEmptyCartTriggerClicked(){
        this.shopCartItemAdaptor.getData().clear();
        this.shopCartItemAdaptor.notifyDataSetChanged();
        ShoppingCart.getInstance().clear();
        this.backToProductList();
    }

    /**
     * 点击继续购物，返回产品列表页
     */
    @OnClick(R2.id.tv_continue_shopping)
    void onContinueShoppingClicked(){
        this.backToProductList();
    }

    /**
     * 移除选择的项目.
     * 现在采用直接清除购物车的方式
     */
    @OnClick(R2.id.tv_top_shop_cart_remove_selected)
    void onDeleteSelectedItemsTriggerClicked(){
        this.onEmptyCartTriggerClicked();
        // 首先取出所有的数据
//        final List<MultipleItemEntity> data = this.shopCartItemAdaptor.getData();

        /*
         * 找到要删除的数据
         * 采用这样的循环方式, 是为了避免数组越界的情况. 如果数组的大小被保存为一个整数值，则会在执行过程中越界
         */
//        for (int i = 0; i < this.shopCartItemAdaptor.getData().size(); i++) {
//            final boolean isSelected = data.get(i).getField(ShoppingCartItemFields.IS_SELECTED);
//            if(isSelected){
//                long cartItemId = data.get(i).getField(MultipleFields.ID);
//                // 从购物车和数据库中删除
//                ShoppingCart.getInstance().removeCartItem(cartItemId);
//                // 更新购物车总价
//                this.updateCartTotal();
//
//                // 从RecyclerView中删除
//                this.shopCartItemAdaptor.remove(i);
//                // 通知 RecyclerView 的视图发生变化
//                this.shopCartItemAdaptor.notifyItemRangeChanged(i, this.shopCartItemAdaptor.getItemCount());
//            }
//        }
    }

    /**
     * 全选和取消全选功能
     */
    @OnClick(R2.id.ll_select_all_cart_items_wrap)
    void onSelectAllTriggerClicked(){
        this.updateLastClickActionTimeStamp();

        final int tag = (int) this.selectAllTrigger.getTag();
        if(tag == 0){
            // 没有选中
            this.selectAllTrigger.setBackgroundColor(
                    ContextCompat.getColor(getContext(), R.color.green)
            );
            this.mtvSelectedAll.setText(getString(R.string.text_general_deselect_all));
            this.mtvSelectedAll.setTextColor(Color.WHITE);
            this.iconSelectedAll.setTextColor(Color.WHITE);

            this.selectAllTrigger.setTag(1); // 表示已经选择了. Tab是原生android自带的
            this.shopCartItemAdaptor.setIsSelectAll(true); // 去切换成全选状态

            // 主动的通过这个方法: 通知RecyclerView更新里面的内容
//            this.shopCartItemAdaptor.notifyDataSetChanged();
            this.shopCartItemAdaptor.notifyItemRangeChanged(0,this.shopCartItemAdaptor.getItemCount());
        }else {
            this.selectAllTrigger.setBackgroundColor(
                    ContextCompat.getColor(getContext(), R.color.lightGray)
            );
            this.mtvSelectedAll.setText(getString(R.string.text_general_select_all));
            this.mtvSelectedAll.setTextColor(Color.DKGRAY);
            this.iconSelectedAll.setTextColor(Color.DKGRAY);

            this.selectAllTrigger.setTag(0); // 表示已经选择了. Tab是原生android自带的
            this.shopCartItemAdaptor.setIsSelectAll(false); // 去切换成全不选状态

            // 主动的通过这个方法: 通知RecyclerView更新里面的内容
//            this.shopCartItemAdaptor.notifyDataSetChanged();
            this.shopCartItemAdaptor.notifyItemRangeChanged(0,this.shopCartItemAdaptor.getItemCount());
        }
    }



    @Override
    public Object setLayout() {
        return R.layout.delegate_shop_cart;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        // 为了全选功能的判别所监听控件的状态，通过tag进行， 那么在这里先设置一个初始值
        this.selectAllTrigger.setTag(0);
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);

        final ArrayList<MultipleItemEntity> data =
                new ShopCartDataConverter()
                        .setObjectData(ShoppingCart.getInstance().getShoppingCartItems())
                        .convert();
        this.shopCartItemAdaptor = new ShopCartItemAdaptor(data);
        this.shopCartItemAdaptor.setShopCartDelegateInstance(this);

        final LinearLayoutManager manager = new LinearLayoutManager(getContext());
        this.shoppingCartItemsView.setLayoutManager(manager);
        this.shoppingCartItemsView.setAdapter(this.shopCartItemAdaptor);
        // 计算购物车总价
        this.updateCartTotal();

        // 展示支付方式
        final ArrayList<MultipleItemEntity> paymentMethodsData =
                new PaymentMethodDataConverter()
                    .setObjectData(PaymentMethod.findAll())
                    .convert();
        this.paymentMethodAdaptor = new PaymentMethodAdaptor(paymentMethodsData);
        this.paymentMethodAdaptor.setShopCartDelegate(this);
        final LinearLayoutManager paymentManager = new LinearLayoutManager(getContext());
        this.paymentMethodsView.setLayoutManager(paymentManager);
        this.paymentMethodsView.setAdapter(this.paymentMethodAdaptor);

        /*
         * 中英语音播放的切换
         */
        MediaPlayer player = null;
        if("cn".equals(MachineProfile.getInstance().getLanguage())){
            // 中文不播放语音
        }else {
            player = MediaPlayer.create(getActivity(),R.raw.pay_notice_en);
        }
        if(player != null){
            player.start();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // 初始化上一次点击的时间戳
        this.updateLastClickActionTimeStamp();
        // 开始跑起Timer
        final BaseTimerTask task = new BaseTimerTask(this);
        this.mTimer = new Timer(true);
        this.mTimer.schedule(task,1000, 5000);
    }

    @Override
    public void onPause() {
        super.onPause();
        this.updateLastClickActionTimeStamp();
        if(this.mTimer != null){
            this.mTimer.cancel();
        }
    }

    protected void backToProductList(){
        startWithPop(new ListDelegate());
    }

    @Override
    public void onTimer() {
        getProxyActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mTimer != null) {
                    final long now = new Date().getTime();
                    if(now - lastClickActionTimeStamp > 120000){
                        mTimer.cancel();
                        mTimer = null;
                        ShoppingCart.getInstance().clear();
                        backToProductList();
                    }
                }
            }
        });
    }
}
