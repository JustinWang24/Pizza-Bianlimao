package com.example.smartbroecommerce.main.checkout;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.smartbro.app.AccountManager;
import com.example.smartbro.net.RestfulClient;
import com.example.smartbro.net.callback.IError;
import com.example.smartbro.net.callback.IFailure;
import com.example.smartbro.net.callback.ISuccess;
import com.example.smartbroecommerce.R;
import com.example.smartbroecommerce.R2;
import com.example.smartbroecommerce.database.MachineProfile;
import com.example.smartbroecommerce.database.Order;
import com.example.smartbroecommerce.database.OrderStatus;
import com.example.smartbroecommerce.database.PaymentMethod;
import com.example.smartbroecommerce.database.ShoppingCart;
import com.example.smartbroecommerce.main.cart.PaymentMethodFields;
import com.example.smartbroecommerce.main.maker.ProcessingDelegate;
import com.example.smartbroecommerce.main.pages.StopWorkingDelegate;
import com.example.smartbroecommerce.main.product.ListDelegate;
import com.joanzapata.iconify.widget.IconTextView;
import com.taihua.pishamachine.LogUtil;

import java.util.Date;
import java.util.Timer;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Justin Wang from SmartBro on 17/12/17.R
 * 通过扫描方式进行支付的页面
 */

public class ScanQrCodeDelegate extends BasicCheckoutDelegate{

    private static final RequestOptions OPTIONS = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .centerCrop()
            .dontAnimate();

    @BindView(R2.id.icon_payment_method)
    IconTextView iconTextView = null;
    @BindView(R2.id.tv_order_total)
    AppCompatTextView tvOrderTotal = null;
    @BindView(R2.id.iv_qrcode_image)
    AppCompatImageView ivQRCodeImage = null;
    @BindView(R2.id.btn_order_cancel)           // 取消按钮
    LinearLayoutCompat btnCancelOrder = null;
    @BindView(R2.id.tv_order_cancel_icon)
    IconTextView tvOrderCancelBtnIcon = null;
    @BindView(R2.id.tv_order_cancel)
    AppCompatTextView tvOrderCancelBtn = null;  // 取消按钮的文字
    @BindView(R2.id.tv_page_title)
    AppCompatTextView tvPageTitle = null;

    private int totalTimeoutCount = 120;

    /**
     * 当取消支付被点击时
     */
    @OnClick(R2.id.btn_order_cancel)
    void onCancelOrderButtonClicked(){
        this.cancelCheckout();
    }

    @Override
    public void onStart() {
        super.onStart();
        this.checkOrderStatusTimer = new Timer(true);
        this.updateLastClickActionTimeStamp();
    }

    /**
     * 服务器返回的扫描二维码的图片URL
     */

    @Override
    public Object setLayout() {
        return R.layout.delegate_qrcode_checkout;
    }

    /**
     * 查询订单支付状态, 整个支付停留时间为2分钟
     */
    @Override
    public void onTimer() {
        getProxyActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
            if (checkOrderStatusTimer != null) {
                totalTimeoutCount--;
                tvOrderCancelBtn.setText(
                        getString(R.string.text_cancel) + "("
                        + getString(R.string.text_count_down)
                        + ": "
                        + Integer.toString(totalTimeoutCount)
                        + ")"
                );

                final long now = new Date().getTime();
                if(now - lastClickActionTimeStamp > 120000){
                    // 等待超时了
                    checkOrderStatusTimer.cancel();
                    checkOrderStatusTimer = null;
                    cancelCheckout();
                }else {
                    // 等待没有超时, 查询订单的状态
                    if(totalTimeoutCount< 110 && totalTimeoutCount%3 == 1){
                        // 前10秒基本不用检查, 且没两秒钟检查一次
                        RestfulClient.builder()
                                .url("check_order_status")
                                .params("oi",Integer.toString(order.getId()))   // 提交order的ID
                                .success(new ISuccess() {
                                    @Override
                                    public void onSuccess(String response) {
                                        try{
                                            final int errorNumber =
                                                    JSON.parseObject(response).getInteger("error_no");
                                            final String status =
                                                    JSON.parseObject(response).getString("status");
                                            if(errorNumber == RestfulClient.NO_ERROR && RestfulClient.STATUS_OK.equals(status)){
                                                // 表示订单已经支付成功
                                                checkOrderStatusTimer.cancel();
                                                checkOrderStatusTimer = null;
                                                // 对订单的状态进行更新
                                                order.setStatus(OrderStatus.PAID);
                                                // 进入下一个制作的环节, 展示Pizza制作过程
                                                ProcessingDelegate delegate = new ProcessingDelegate();
                                                Bundle args = new Bundle();
                                                args.putInt("orderId",order.getId());   // 订单ID
                                                args.putDouble("changes", 0);           // 找零金额
                                                args.putBoolean("needCallBakingCmd", true);
                                                delegate.setArguments(args);
                                                startWithPop(delegate);
                                            }
                                        }catch (Exception e){
                                            LogUtil.LogException(e);
                                        }
                                    }
                                })
                                .error(new IError() {
                                    @Override
                                    public void onError(int code, String msg) {
                                        LogUtil.LogInfoForce(msg);
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
                }
            }
            }
        });
    }

    @Override
    public Order createOrder() {
        this.order = new Order(-1, this.paymentMethod);
        return this.order;
    }

    @Override
    public void cancelCheckout() {
        if(this.checkOrderStatusTimer != null){
            this.checkOrderStatusTimer.cancel();
            this.checkOrderStatusTimer = null;
        }
        ShoppingCart.getInstance().clear();
        startWithPop(new ListDelegate());
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        this.tvPageTitle.setText(MachineProfile.getInstance().getMachineName());

        // 获取传递来的参数， 取得支付方式对象
        final Bundle args = getArguments();
        this.paymentMethod = PaymentMethod.findById(args.getLong(PaymentMethodFields.PAYMENT.name()));
        this.initPaymentMethodIcon(this.paymentMethod.getType());
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
                .into(this.ivQRCodeImage);
        // 开始项服务器查询订单的支付状态
        this.checkOrderStatus();
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        this.totalTimeoutCount = 120;
//        this.tv_order_cancel_icon
        this.tvOrderCancelBtnIcon.setText("{icon-cancel}");
        super.onLazyInitView(savedInstanceState);
        final Bundle args = getArguments();

        final int discount = args.getInt("discount");
        final double orderTotal = this.createOrder().getTotolAmount();
        final double orderTotalFinal = orderTotal - discount;

        if(discount>0){
            this.tvOrderTotal.setText(
                    getString(R.string.text_total) + Double.toString(orderTotalFinal) + "(优惠" + Integer.toString(discount) + "元)"
            );
        }else{
            this.tvOrderTotal.setText(
                    getString(R.string.text_total) + Double.toString(orderTotalFinal)
            );
        }


        if(args != null){
            if(this.paymentMethod != null){
                this.createOrder();
                this.order.prepareShoppingCartData();
                final String orderInJson = JSON.toJSONString(this.order);
                final String coupon = args.getString("coupon"); // 获取优惠码: 如果不支持优惠码，那么将得到一个 ""

                RestfulClient.builder()
                        .url(this.order.getOrderUrl())
                        .loader(getContext())
                        .params("muuid", Long.toString(AccountManager.getMachineId()))
                        .params("order",orderInJson)
                        .params("coupon",coupon)
                        .params("discount",Integer.toString(discount))
                        .success(new ISuccess() {
                            @Override
                            public void onSuccess(String response) {
                                // 订单已经产生, 让用户进行支付
                                // {"error_no":100,"msg":"","data":{"payUrl":"http:\/\/htlc.smartbro.com.au\/storage\/tmp\/qr\/729669-7.png","order_no":1007}}
                                Log.i("Info",response);
                            try{
                                final int errorNumber =
                                        JSON.parseObject(response).getInteger("error_no");
                                final int forceShutDown =
                                        JSON.parseObject(response).getInteger("sd");
                                if(forceShutDown == 1){
                                    // 强制设备不工作
                                    forceShutDown();
                                }else {
                                    if(errorNumber == RestfulClient.NO_ERROR){
                                        // 表示订单正确的生成了
                                        final JSONObject data =
                                                JSON.parseObject(response).getJSONObject("data");
                                        final String payUrl = data.getString("payUrl");
                                        initQrCodeImage(payUrl);
                                        // 保存订单ID
                                        order.setId(data.getInteger("order_no"));
                                    } else {

                                    }
                                }
                            }catch (Exception e){
                                LogUtil.LogException(e);
                            }

                            }
                        })
                        .error(new IError() {
                            @Override
                            public void onError(int code, String msg) {
                                LogUtil.LogInfoForce("订单生成错误: " +msg + ", " + new Date().toString());
                            }
                        })
                        .failure(new IFailure() {
                            @Override
                            public void onFailure() {
                                LogUtil.LogInfoForce("订单生成失败: " + new Date().toString());
                            }
                        })
                        .build()
                        .post();
            }else {
                throw new NullPointerException("支付时，支付的方式对象不可以为空");
            }
        }
    }

    private void forceShutDown(){
        getProxyActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startWithPop(new StopWorkingDelegate());
            }
        });
    }

    /**
     * 设置代表支付方式的Icon
     * @param type
     */
    private void initPaymentMethodIcon(int type) {
        String iconText;
        switch (type){
            case PaymentMethod.WECHAT:
                iconText = "{icon-weixinBig}";
                iconTextView.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
                iconTextView.setText(iconText);
                // 支付方式的文字
                break;
            case PaymentMethod.ALIPAY:
                iconText = "{icon-zhifubaoBig}";
                iconTextView.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
                iconTextView.setText(iconText);
                break;
            case PaymentMethod.CREDIT_CARD:
                iconText = "{fa-credit-card}";
                iconTextView.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
                iconTextView.setText(iconText);
                break;
            case PaymentMethod.APPLE_PAY:
                iconText = "{fa-apple}";
                iconTextView.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
                iconTextView.setText(iconText);
                break;
            default:
                iconText = "{fa-money}";
                iconTextView.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
                iconTextView.setText(iconText);
                break;
        }
    }
}
