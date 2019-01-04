package com.example.smartbroecommerce.main.checkout;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.smartbro.net.RestfulClient;
import com.example.smartbro.net.callback.IError;
import com.example.smartbro.net.callback.IFailure;
import com.example.smartbro.net.callback.ISuccess;
import com.example.smartbro.utils.timer.BaseTimerTask;
import com.example.smartbroecommerce.R;
import com.example.smartbroecommerce.R2;
import com.example.smartbroecommerce.database.MachineProfile;
import com.example.smartbroecommerce.database.Order;
import com.example.smartbroecommerce.database.PaymentMethod;
import com.example.smartbroecommerce.main.cart.PaymentMethodFields;
import com.example.smartbroecommerce.main.cart.ShopCartDelegate;

import java.util.Timer;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Justin Wang from SmartBro on 15/1/18.
 * 显示 Coupon 的表格信息
 */

public class CouponFormDelegate extends BasicCheckoutDelegate {

    private static final RequestOptions OPTIONS = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .centerCrop()
            .dontAnimate();

    @BindView(R2.id.iv_qrcode_get_coupon)
    AppCompatImageView ivQRCodeForCoupon = null;

    @BindView(R2.id.et_coupon_code)
    TextView deliveryCode = null;

    // 键盘相关
    @BindView(R2.id.btn_key0)
    Button btnKey0 = null;
    @BindView(R2.id.btn_key1)
    Button btnKey1 = null;
    @BindView(R2.id.btn_key2)
    Button btnKey2 = null;
    @BindView(R2.id.btn_key3)
    Button btnKey3 = null;
    @BindView(R2.id.btn_key4)
    Button btnKey4 = null;
    @BindView(R2.id.btn_key5)
    Button btnKey5 = null;
    @BindView(R2.id.btn_key6)
    Button btnKey6 = null;
    @BindView(R2.id.btn_key7)
    Button btnKey7 = null;
    @BindView(R2.id.btn_key8)
    Button btnKey8 = null;
    @BindView(R2.id.btn_key9)
    Button btnKey9 = null;
    @BindView(R2.id.btn_key_confirm)
    Button btnKeyConfirm = null;
    @BindView(R2.id.btn_key_delete)
    Button btnKeyDelete = null;
    // 键盘相关结束

    @BindView(R2.id.btn_key_cancel_delivery)
    Button btnKeyCancel = null;

    private String methodName = null;
    private int totalTimeoutCount = 120; // 2分钟后超时
    private Timer mTimer = null;

    // 通过键盘输入的coupon的长度
    private int enteredCouponCodeLength = 0;

//    @OnClick(R2.id.btn_coupon_form_cancel)
//    void onCouponFormCancelButtonClick(){
//        if (this.mTimer != null){
//            this.mTimer.cancel();
//            this.mTimer = null;
//        }
//        this.totalTimeoutCount = 120;
//        startWithPop(new ShopCartDelegate());
//    }

    @Override
    public Object setLayout() {
        return R.layout.delegate_coupon_form;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {

        final Bundle args = getArguments();
        this.paymentMethod = PaymentMethod.findById(args.getLong(PaymentMethodFields.PAYMENT.name()));
        this.methodName = args.getString("method");

        RestfulClient.builder()
                .url("show-request-coupon-qrcode")
                .success(new ISuccess() {
                    @Override
                    public void onSuccess(String response) {
                        final int errorNumber =
                                JSON.parseObject(response).getInteger("error_no");
                        if(errorNumber == RestfulClient.NO_ERROR){
                            // 表示获取了二维码
                            final String url = JSON.parseObject(response).getString("imageUrl");
                            initQrCodeImage(url);
                        }
                    }
                })
                .build()
                .get();

        // 倒计时
        this.mTimer = new Timer(true);
        BaseTimerTask task = new BaseTimerTask(this);
        this.mTimer.schedule(task,500, 1000);
    }

    /**
     * 初始化订单的支付二维码
     * 一旦二维码渲染完成，开始项服务器查询订单的支付状态
     * @param payUrl
     */
    private void initQrCodeImage(String payUrl){
        Glide.with(this)
                .load(payUrl)
                .apply(OPTIONS)
                .into(this.ivQRCodeForCoupon);
    }

    @Override
    public void onTimer() {
        getProxyActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                totalTimeoutCount--;
                if(totalTimeoutCount <= 0){
                    mTimer.cancel();
                    mTimer = null;
                    startWithPop(new ShopCartDelegate());
                }
            }
        });
    }

    @Override
    public Order createOrder() {
        return null;
    }

    @Override
    public void cancelCheckout() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void _checkout(String paymentMethod, String coupon, int couponValue){
        BasicCheckoutDelegate delegate;

        if("wechat".equals(paymentMethod) || "alipay".equals(paymentMethod)){
            delegate = new ScanQrCodeDelegate();
        }else if("credit".equals(paymentMethod)){
            delegate = new ByCreditCard();
        } else {
            delegate = new ByCashDelegate();
        }
        Bundle args = new Bundle();
        // 把Payment method的ID作为参数传过去
        args.putLong(PaymentMethodFields.PAYMENT.name(), this.paymentMethod.getId());
        // 把选定的支付方式传过去
        args.putString("method",methodName);
        // 把优惠券传过去
        args.putString("coupon",coupon);
        // 把优惠券的金额传过去
        args.putInt("discount",couponValue);

        delegate.setArguments(args);
        startWithPop(delegate);
    }

    /**
     * 土豪按钮被点击
     */
    @OnClick(R2.id.et_coupon_code)
    void checkoutDirectly(){
        if (this.enteredCouponCodeLength != 0){
            this.enteredCouponCodeLength = 0;
            this.deliveryCode.setText(getString(R.string.text_dont_use_coupon));
            return;
        }
        // 直接进行结账
        _checkout(this.methodName,"",0);
    }

    /**
     * 确认按钮被点击
     */
    @OnClick(R2.id.btn_key_confirm)
    void onConfirmClick(){
        if (this.mTimer != null){
            this.mTimer.cancel();
            this.mTimer = null;
        }
        this.totalTimeoutCount = 120;
        // 验证优惠码
        final String coupon = this.deliveryCode.getText().toString();
        final String muuid = MachineProfile.getInstance().getUuid();

        RestfulClient.builder()
                .url("verify-coupon-code")
                .params("muuid", muuid)
                .params("coupon",coupon)
                .success(new ISuccess() {
                    @Override
                    public void onSuccess(String response) {
                        final int errorNumber =
                                JSON.parseObject(response).getInteger("error_no");

                        if(errorNumber == RestfulClient.NO_ERROR){
                            final int couponValue = JSON.parseObject(response).getInteger("cv");
                            // 获取到优惠码的折扣金额, 开始结账
                            _checkout(methodName,coupon,couponValue);
                        }else {
                            // 验证失败
                            _checkout(methodName,coupon,0);
                        }
                    }
                })
                .error(new IError() {
                    @Override
                    public void onError(int code, String msg) {

                    }
                })
                .failure(new IFailure() {
                    @Override
                    public void onFailure() {

                    }
                })
                .build()
                .post();
    }

    /**
     * 取消按钮被点击
     */
    @OnClick(R2.id.btn_key_cancel_delivery)
    void onCancelClick(){
        if (this.mTimer != null){
            this.mTimer.cancel();
            this.mTimer = null;
        }
        this.totalTimeoutCount = 120;
        startWithPop(new ShopCartDelegate());
    }

    /**
     * 当删除按钮被点击
     */
    @OnClick(R2.id.btn_key_delete)
    void onDeleteClick(){
        if(this.enteredCouponCodeLength == 0){
            return;
        }

        String code = this.deliveryCode.getText().toString();

        if(code.length()>0){
            if(code.length() == 1){
                // 只有一个字符了，则恢复土豪的文字
                this.deliveryCode.setText(getString(R.string.text_dont_use_coupon));
            }else {
                final String sub = code.substring(0,code.length()-1);
                this.deliveryCode.setText(sub);
            }
            this.enteredCouponCodeLength = code.length()-1;
        }
    }

    /**
     * 处理按钮的点击
     * @param button
     */
    private void handleClick(Button button){
        if(this.enteredCouponCodeLength>5){
            return;
        }

        if(this.enteredCouponCodeLength == 0){
            // 如果还没有输入过coupon, 那么先把文字清空
            this.deliveryCode.setText("");
        }
        this.enteredCouponCodeLength++;
        final String code = this.deliveryCode.getText().toString();
        this.deliveryCode.setText(code + button.getText().toString());
    }

    @OnClick(R2.id.btn_key0)
    void onKey0Click(){
        this.handleClick(this.btnKey0);
    }
    @OnClick(R2.id.btn_key1)
    void onKey1Click(){
        this.handleClick(this.btnKey1);
    }
    @OnClick(R2.id.btn_key2)
    void onKey2Click(){
        this.handleClick(this.btnKey2);
    }
    @OnClick(R2.id.btn_key3)
    void onKey3Click(){
        this.handleClick(this.btnKey3);
    }
    @OnClick(R2.id.btn_key4)
    void onKey4Click(){
        this.handleClick(this.btnKey4);
    }
    @OnClick(R2.id.btn_key5)
    void onKey5Click(){
        this.handleClick(this.btnKey5);
    }
    @OnClick(R2.id.btn_key6)
    void onKey6Click(){
        this.handleClick(this.btnKey6);
    }
    @OnClick(R2.id.btn_key7)
    void onKey7Click(){
        this.handleClick(this.btnKey7);
    }
    @OnClick(R2.id.btn_key8)
    void onKey8Click(){
        this.handleClick(this.btnKey8);
    }
    @OnClick(R2.id.btn_key9)
    void onKey9Click(){
        this.handleClick(this.btnKey9);
    }
}
