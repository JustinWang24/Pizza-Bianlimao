package com.example.smartbroecommerce.main.product;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.example.smartbro.delegates.SmartbroDelegate;
import com.example.smartbro.ui.recycler.DataConvertor;
import com.example.smartbro.ui.recycler.MultipleViewHolder;
import com.example.smartbro.utils.FastClickProtector;
import com.example.smartbro.utils.timer.BaseTimerTask;
import com.example.smartbro.utils.timer.ITimerListener;
import com.example.smartbroecommerce.R;
import com.example.smartbroecommerce.R2;
import com.example.smartbroecommerce.database.DatabaseManager;
import com.example.smartbroecommerce.database.Position;
import com.example.smartbroecommerce.database.Product;
import com.example.smartbroecommerce.database.ProductDao;
import com.example.smartbroecommerce.database.ShoppingCart;
import com.example.smartbroecommerce.machine.HomeDelegate;
import com.example.smartbroecommerce.main.checkout.ByHippoAppDelegate;
import com.example.smartbroecommerce.main.converters.ProductsListDataConverter;
import com.example.smartbroecommerce.main.pages.DeliveryCodeDelegate;
import com.example.smartbroecommerce.main.pages.StopWorkingDelegate;
import com.example.smartbroecommerce.main.pages.UnlockScreenDelegate;

import com.joanzapata.iconify.widget.IconTextView;
import com.taihua.pishamachine.LogUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Justin Wang from SmartBro on 12/12/17.
 * 产品列表页的 Delegate, 应用程序首页的轮播，点击任意一个, 都会跳转到此
 */

public class ListDelegate extends SmartbroDelegate
        implements BaseQuickAdapter.RequestLoadMoreListener, ITimerListener{
    // 页面的显示控件开始
    @BindView(R2.id.ll_product_listing_wrap)
    RelativeLayout wrapper = null;
    @BindView(R2.id.tv_page_title)
    AppCompatTextView tvPageTitle = null;   // 顶部栏

    @BindView(R2.id.rl_shop_cart_in_product_list)
    RelativeLayout shoppingCartLayout = null;   // 底部栏: 购物车部分按钮
    @BindView(R2.id.icon_shop_cart_in_product_list)
    IconTextView shoppingCartIcon = null;       // 底部栏: 购物车图标
    @BindView(R2.id.tv_shopping_cart_amount_in_product_list)
    CircleTextView circleTextView = null;       // 购物车中当前数量的圆形
    @BindView(R2.id.tv_shopping_cart_text_in_product_list)
    AppCompatTextView shoppingCartText = null;  // 购物车中文字
    // 页面的显示控件结束

    @BindView(R2.id.srl_products_listing)
    SwipeRefreshLayout refreshLayout;
    @BindView(R2.id.rv_products_listing)
    RecyclerView recyclerView;

    // 底部
    @BindView(R2.id.rl_help_wrap)
    RelativeLayout helpLink;
    @BindView(R2.id.rl_help_section_text)
    AppCompatTextView cartContentText;

    @BindView(R2.id.rl_delivery_code)
    RelativeLayout deliveryCodeBtn;

    private DataConvertor convertor = null;
    private ProductListAdaptor adaptor = null;

    // 上一次点击的时间戳
    private Timer mTimer = null;
    private int unlockBtnClickedCount = 0;
    private long lastTimeUnlockBtnClicked = new Date().getTime();

    private boolean cartEmptyMessageShowed = false;

    /**
     * 保存所有产品的Holder
     */
    private List<MultipleViewHolder> holders = new ArrayList<>();

    /**
     * 被选中的产品
     */
    private Product selectedProduct = null;

    @OnClick(R2.id.tv_page_title)
    void unlockScreen(){
        this.unlockBtnClickedCount++;
        this.lastTimeUnlockBtnClicked = new Date().getTime();

        if(this.unlockBtnClickedCount > 6){
            startWithPop(new UnlockScreenDelegate());
        }
    }

    @OnClick(R2.id.rl_delivery_code)
    void onDeliveryCodeBtnClick(){
        if(FastClickProtector.isFastDoubleClick()){
            return;
        }
        startWithPop(new DeliveryCodeDelegate());
    }

    public List<MultipleViewHolder> getHolders(){
        return this.holders;
    }

    public void setSelectedProduct(Product product){
        this.selectedProduct = product;
    }

    /*
     * 当帮助文字被点击时
     */
//    @OnClick(R2.id.rl_help_wrap)
//    void onHelpTextClicked(){
//        if(!this.isHelpButtonClicked && !FastClickProtector.isFastDoubleClick()){
//            this.isHelpButtonClicked = true;
//            startWithPop(new DiscoverDelegate());
//        }
        // 原来的帮助文字， 现在已经改成显示购物车中的物品，所有没有任何点击的响应了
//    }

    /**
     * 当购物车图标被点击时
     */
    @OnClick(R2.id.rl_shop_cart_in_product_list)
    void onShoppingCartClicked(){
        if(FastClickProtector.isFastDoubleClick()){
            return;
        }

        if(this.selectedProduct == null){
            // 没有选择产品
            this.cartEmptyMessageShowed = true;
            Toast.makeText(this.getContext(),getString(R.string.text_cart_is_empty), Toast.LENGTH_SHORT).show();
            // 这也算一次点击， 应该从新计时
            this.updateLastClickActionTimeStamp();
        }else {
            // 跳转到支付界面
            Bundle args = new Bundle();
            args.putLong("productId",this.selectedProduct.getId());
            ByHippoAppDelegate delegate = new ByHippoAppDelegate();
            delegate.setArguments(args);

            startWithPop(delegate);
        }
    }

    @Override
    public Object setLayout() {
        return R.layout.delegate_product_list_new;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        // Todo 页面下拉时候的处理
//        this.mRefreshHandler = RefreshHandler
//            .create(
//                    this.refreshLayout,this.recyclerView,new ProductsListDataConverter(),null
//            );
    }

    /**
     * 从本地数据库中加载产品
     */
    private void reloadProducts(){
        // 处理数据
        this.convertor = new ProductsListDataConverter();
        final ProductDao productDao = DatabaseManager.getInstance().getProductDao();
        final List<Product> products = productDao.queryBuilder().list();
        final List<Product> availableProducts = new ArrayList<Product>();
        for (Product p : products) {
            final Position position = Position.getPositionByProduct(p);
            if(position != null){
                // 说明产品还有至少一个有效位置, 可以显示
                availableProducts.add(p);
            }
        }

        this.adaptor = ProductListAdaptor.
                create(this.convertor.setObjectData(availableProducts));

        // 将delegate 传给 adapter, 以便实现监听
        this.adaptor.setOwnerDelegate(this);

        // 将适配器类与recycler view进行绑定
        this.recyclerView.setAdapter(this.adaptor);

        this.initRecyclerView();
        this.initRefreshLayout();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    /**
     * 界面可见，但是还不能和用户交互, 主要做准备数据的工作
     */
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        int errorValue = 0;
        Bundle args = getArguments();
        errorValue = args.getInt("errorCode");

        if(errorValue == 0){
            // 表示没有读取到任何错误 PLC
            this.reloadProducts();  // 重新加载产品，确保没有库存的产品下架
            // 初始化上一次点击的时间戳
            this.updateLastClickActionTimeStamp();
            // 开始跑起Timer
            final BaseTimerTask task = new BaseTimerTask(this);
            this.mTimer = new Timer(true);
            this.mTimer.schedule(task,1000, 5000);
        }else {
            LogUtil.LogInfo("产品列表页面中的错误检查: " + Integer.toString(errorValue));
            if(errorValue > 0){
                // 表示发生了错误, 需要上报服务器, 同时然后跳转到等待页面
                startWithPop(new StopWorkingDelegate());
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        this.updateLastClickActionTimeStamp();
        if(this.mTimer != null){
            this.mTimer.cancel();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /**
     * 显示产品的详情页, 这个方法是从adaptor里面的监听来调用的
     * @param productId 产品ID
     */
    public void showProductDetail(long productId){
        startWithPop(DetailDelegate.newInstance(productId));
    }

    /**
     * 在懒加载中处理数据. 对远程的数据处理也可以写在这里
     * @param savedInstanceState Bundle
     */
    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
        this.initRecyclerView();
        this.initRefreshLayout();

        if(ShoppingCart.getInstance().isEmpty()){
            this.circleTextView.setVisibility(View.GONE);
            this.cartContentText.setText(getString(R.string.text_cart_empty_text));
        }else {
            this.circleTextView.setText(Integer.toString(ShoppingCart.getInstance().getProductsCount()));
            this.circleTextView.setVisibility(View.VISIBLE);

            // Todo 显示里面的产品名称
            final List<Product> products = ShoppingCart.getInstance().getProducts();
            if(products.size() > 0){
                final Product product = products.get(0);
                this.cartContentText.setText(
                        getString(R.string.text_product_you_selected)
                        + product.getName()
                        + getString(R.string.text_product_you_selected_after)
                );
            }
        }
    }

    private void initRefreshLayout(){
        this.refreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
        this.refreshLayout.setProgressViewOffset(true, 120, 300);
    }

    private void initRecyclerView(){
        final GridLayoutManager manager = new GridLayoutManager(getContext(), 2); // 最多一行2个
        this.recyclerView.setLayoutManager(manager);
        // todo 添加适当的分割线
//        this.recyclerView.addItemDecoration(
//                BaseDecoration.create(
//                        ContextCompat.getColor(getContext(), R.color.helpbg),
//                        1
//                )
//        );
    }

    @Override
    public void onLoadMoreRequested() {

    }

    private void backToHome(){
        startWithPop(new HomeDelegate());
    }

    /**
     * 定时任务，如果2分钟没有任何操作，则执行下面的方法
     */
    @Override
    public void onTimer() {
        final long now = new Date().getTime();
        getProxyActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mTimer != null) {
                    if(now - lastClickActionTimeStamp > 120000){
                        mTimer.cancel();
                        mTimer = null;
//                        backToHome();
                    }
                }
            }
        });

        // 如果解锁按钮10秒钟没有被点击, 那么就重置
        if(now - this.lastTimeUnlockBtnClicked >= 10000){
            this.unlockBtnClickedCount = 0;
        }
    }
}
