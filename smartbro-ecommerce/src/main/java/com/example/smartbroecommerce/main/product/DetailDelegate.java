package com.example.smartbroecommerce.main.product;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.widget.RelativeLayout;
import android.support.v7.widget.Toolbar;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.daimajia.androidanimations.library.YoYo;
import com.example.smartbro.app.Smartbro;
import com.example.smartbro.delegates.SmartbroDelegate;
import com.example.smartbro.ui.banner.HolderCreator;
import com.example.smartbro.utils.FastClickProtector;
import com.example.smartbro.utils.timer.BaseTimerTask;
import com.example.smartbro.utils.timer.ITimerListener;
import com.example.smartbroecommerce.R;
import com.example.smartbroecommerce.R2;
import com.example.smartbroecommerce.animation.BezierAnimation;
import com.example.smartbroecommerce.animation.BezierUtil;
import com.example.smartbroecommerce.database.MachineProfile;
import com.example.smartbroecommerce.database.Product;
import com.example.smartbroecommerce.database.ShoppingCart;
import com.example.smartbroecommerce.main.cart.ShopCartDelegate;
import com.example.smartbroecommerce.utils.BetterToast;
import com.example.smartbroecommerce.utils.ColorHelper;
import com.joanzapata.iconify.widget.IconTextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;

import butterknife.BindView;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import me.yokeyword.fragmentation.anim.DefaultHorizontalAnimator;
import me.yokeyword.fragmentation.anim.FragmentAnimator;

/**
 * Created by Justin Wang from SmartBro on 13/12/17.
 * 产品的详情信息
 */

public class DetailDelegate extends SmartbroDelegate
        implements AppBarLayout.OnOffsetChangedListener, BezierUtil.AnimationListener, ITimerListener{
    @BindView(R2.id.rl_product_detail_wrap)
    RelativeLayout wrapper = null;

    @BindView(R2.id.goods_detail_toolbar)
    Toolbar toolbar = null;
    @BindView(R2.id.tab_layout)
    TabLayout tabLayout = null;
    @BindView(R2.id.view_pager)
    ViewPager viewPager = null;
    @BindView(R2.id.detail_banner)
    ConvenientBanner<String> banner = null;
    @BindView(R2.id.collapsing_toolbar_detail)
    CollapsingToolbarLayout collapsingToolbarLayout = null;
    @BindView((R2.id.app_bar_detail))
    AppBarLayout appBar = null;

    @BindView(R2.id.tv_page_title)
    AppCompatTextView tvPageTitle = null;

    // 顶部的文字
    @BindView(R2.id.tv_detail_title_text)
    AppCompatTextView titleTextView = null;
    @BindView(R2.id.icon_goods_back)
    IconTextView backToProductsListingIcon = null;

    // 底部
    @BindView(R2.id.rl_shop_cart)
    RelativeLayout cartButtonLayout = null;
    @BindView(R2.id.rl_favor)
    RelativeLayout iconBackToListWrap = null; // 包含返回按钮的那个layout, 它用来感知点击事件
    @BindView(R2.id.icon_favor)
    IconTextView iconBackToList = null; // 返回前一页按钮
    @BindView(R2.id.tv_shopping_cart_amount)
    CircleTextView circleTextView = null; // 购物车中当前数量的圆形
    @BindView(R2.id.tv_shopping_cart_text)
    AppCompatTextView shoppingCartText = null;

    @BindView(R2.id.rl_add_shop_cart)
    RelativeLayout addToShoppingCart = null;    // 加入购物车按钮
    @BindView(R2.id.rl_add_shop_cart_text)
    AppCompatTextView addToShoppingCartText = null; // 加入购物车按钮文字

    @BindView(R2.id.rl_quick_checkout)
    RelativeLayout quickCheckoutBtn = null;     // 立刻购买按钮
    @BindView(R2.id.icon_shop_cart)
    IconTextView shoppingCart = null;

    // 传递参数
    private static final String ARG_PRODUCT_ID = "PRODUCT_ID";
    private long productId = -1;

    private Product product = null;

    // 为了实现加入购物车飞入动画
    private String productThumbnailUrl = null;
    private int shoppingCartItemCount = 0;
    private static final RequestOptions OPTIONS = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
            .dontAnimate()
            .override(100, 100);

    private Timer mTimer = null;

    // 购物车动画已经显示过了
    private boolean alarmShowed = false;

    /**
     * 返回产品列表页
     */
    @OnClick(R2.id.rl_favor)
    void onClickBackToProductListing(){
        this.backToProductListingDelegate();
    }

    @OnClick(R2.id.icon_goods_back)
    void onIconBackToProductListingClick(){
        this.backToProductListingDelegate();
    }

    /**
     * 返回商品列表页
     */
    private void backToProductListingDelegate(){
        startWithPop(new ListDelegate());
    }

    /**
     * 打开购物车页
     */
    @OnClick(R2.id.rl_shop_cart)
    void onClickShoppingCart(){
        if(FastClickProtector.isFastDoubleClick()){
            return;
        }

        if (ShoppingCart.getInstance().isEmpty()){
            // 购物车为空
            BetterToast.getInstance().showText(_mActivity,getString(R.string.text_cart_is_empty));
        }else{
            startWithPop(new ShopCartDelegate());
        }
    }

    /**
     * 加入购物车的点击监听
     */
    @OnClick(R2.id.rl_add_shop_cart)
    void onClickAddToCart(){
        if(FastClickProtector.isFastDoubleClick()){
            return;
        }

        if(this.shoppingCartItemCount < 1){
            if(!this.alarmShowed){
                final CircleImageView view = new CircleImageView(Smartbro.getApplication());
                Glide.with(this)
                        .load(this.productThumbnailUrl)
                        .apply(OPTIONS)
                        .into(view);
                // 执行动画
                BezierAnimation.addCart(this,this.addToShoppingCart,this.shoppingCart,view,this);
            }
        }else {
            if(!this.alarmShowed){
                this.alarmShowed = true;
                // 已经有足够产品了，购物车不能再装了
                BetterToast.getInstance().showText(
                        _mActivity,
                        getString(R.string.msg_cart_is_full)
                );
            }
        }
        // 更新最后一次点击的时间
        this.updateLastClickActionTimeStamp();
    }

    /**
     * 立刻下单按钮被点击后的响应方法
     */
    @OnClick(R2.id.rl_quick_checkout)
    void onClickQuickCheckoutButton(){
        if(FastClickProtector.isFastDoubleClick()){
            return;
        }

        if(this.shoppingCartItemCount == 0){
            this.shoppingCartItemCount = ShoppingCart.getInstance().addProduct(this.product, this);
        }

        if(this.mTimer != null){
            this.mTimer.cancel();
            this.mTimer = null;
        }
        startWithPop(new ShopCartDelegate());
    }

    /**
     * 初始化购物车的数量小圆点儿
     */
    private void setShoppingCartItemCount(){
        this.productThumbnailUrl = this.product.getMainImageUrl();
        this.shoppingCartItemCount = ShoppingCart.getInstance().getProductsCount();
        if(this.shoppingCartItemCount == 0){
            this.circleTextView.setVisibility(View.GONE);
        }else{
            this.circleTextView.setText(Integer.toString(this.shoppingCartItemCount));
            this.circleTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        if(args != null){
            this.productId = args.getLong(ARG_PRODUCT_ID);
        }
    }

    public static DetailDelegate newInstance(@NonNull long productId){
        final Bundle args = new Bundle();
        args.putLong(ARG_PRODUCT_ID, productId);
        final DetailDelegate detailDelegate = new DetailDelegate();
        detailDelegate.setArguments(args);
        return detailDelegate;
    }
    // 传递参数 完毕

    @Override
    public Object setLayout() {
        return R.layout.delegate_product_detail;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        // 更新页面控件的显示属性
        if("en".equals(MachineProfile.getInstance().getLanguage())){
            this._updateUIDisplayAttributes();
        }
        this.tvPageTitle.setText(MachineProfile.getInstance().getMachineName());

        this.loadProduct();
        if(this.product != null){
            // 产品加载成功
            this.titleTextView.setText("");
            this.collapsingToolbarLayout.setContentScrimColor(Color.WHITE);
            this.appBar.addOnOffsetChangedListener(this);
            this.initBannerSlider();    // 产品的图片动起来
            this.initProductInfo();     // 初始化产品的基本信息
            this.initTabLayout();
            this.initPager();
        }
    }

    private void _updateUIDisplayAttributes(){
        this.wrapper.setBackground(getResources().getDrawable(R.mipmap.au_pizza_ninja_bg));
        this.tvPageTitle.setBackgroundColor(ColorHelper.GetColorIntValueByName("black",getResources()));

        this.cartButtonLayout.setBackgroundColor(Color.TRANSPARENT);
        this.shoppingCart.setTextColor(Color.TRANSPARENT);
        this.shoppingCartText.setTextColor(Color.TRANSPARENT);

        this.addToShoppingCart.setBackgroundColor(Color.TRANSPARENT);
        this.addToShoppingCartText.setTextColor(Color.TRANSPARENT);

        this.circleTextView.setTextColor(Color.TRANSPARENT);
        this.circleTextView.setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
        this.setShoppingCartItemCount();    // 获取购物车中的产品数量
        if("cn".equals(MachineProfile.getInstance().getLanguage())){
            this.circleTextView.setBackgroundColor(Color.TRANSPARENT);
            this.circleTextView.setCircleBackground(Color.RED);
        }else{
            this.circleTextView.setBackgroundColor(Color.TRANSPARENT);
            this.circleTextView.setTextColor(Color.TRANSPARENT);
            this.circleTextView.setCircleBackground(Color.TRANSPARENT);
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

        if("en".equals(MachineProfile.getInstance().getLanguage())){
            MediaPlayer player = MediaPlayer.create(getActivity(), R.raw.productsingle_en);
            player.start();
        }

        this.alarmShowed = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        if(this.mTimer != null){
            this.mTimer.cancel();
            this.mTimer = null;
        }
    }

    private void initPager(){
        final PagerAdapter adapter = new TabPagerAdapter(getFragmentManager(),this.product);
        this.viewPager.setAdapter(adapter);
    }

    /**
     * 初始化Tab layout
     * 这个layout的作用， 其实就是加入类似 产品规格、性能参数等等附加的信息. 通过点击tab来切换并显示对应的内容
     * 在 Pizza 机暂时还不用，但是以后可以添加更多内容进来
     * 更多的内容，都在 ProductInfoDelegate 里面处理
     */
    private void initTabLayout(){
        this.tabLayout.setTabMode(TabLayout.MODE_FIXED);
        this.tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(getContext(), R.color.machinebg));
        // 设置文字颜色
        this.tabLayout.setTabTextColors(ColorStateList.valueOf(Color.BLACK));
        // 设置背景颜色
        this.tabLayout.setBackgroundColor(Color.WHITE);
        this.tabLayout.setupWithViewPager(this.viewPager);
    }

    /**
     * 展示商品的价格，使用的原料等等信息
     */
    private void initProductInfo(){
        getSupportDelegate().
            loadRootFragment(
                R.id.frame_goods_info,
                ProductInfoDelegate.create(this.product)
            );
    }

    /**
     * 初始化产品详情页幻灯片
     */
    private void initBannerSlider(){
        final List<String> images = new ArrayList<>();
        // todo 产品详情页中, 目前只有一张图片, 也可以是多张图片
        images.add(this.product.getMainImageUrl());

        this.banner.setPages(new HolderCreator(), images)
                .setPageIndicator(new int[]{R.drawable.dot_normal, R.drawable.dot_focus})
                .setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.CENTER_HORIZONTAL)
//                .setPageTransformer(new DefaultTransformer())
                .startTurning(3000)
                .setCanLoop(true);
    }

    /**
     * 加载产品信息的方法
     */
    private void loadProduct(){
        this.product = Product.find(this.productId);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        // 当app bar滑动时

    }

    @Override
    public FragmentAnimator onCreateFragmentAnimator() {
        return new DefaultHorizontalAnimator();
    }

    /**
     * 当Bez 曲线动画完成后的回调, 完成产品加入购物车的操作
     */
    @Override
    public void onAnimationEnd() {
        YoYo.with(new ScaleUpAnimator())
                .duration(500)
                .playOn(this.shoppingCart);

        // todo 完成真正的更新购物车的操作: 最多3张饼
        this.shoppingCartItemCount = ShoppingCart.getInstance().addProduct(this.product, this);
        this.circleTextView.setVisibility(View.VISIBLE);
        this.circleTextView.setText(Integer.toString(this.shoppingCartItemCount));
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
                        backToProductListingDelegate();
                    }
                }
            }
        });
    }
}
