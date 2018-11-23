package com.example.smartbroecommerce.main.checkout;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.View;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.smartbro.app.AccountManager;
import com.example.smartbro.net.RestfulClient;
import com.example.smartbro.net.callback.IError;
import com.example.smartbro.net.callback.IFailure;
import com.example.smartbro.net.callback.ISuccess;
import com.example.smartbro.utils.timer.BaseTimerTask;
import com.example.smartbroecommerce.R;
import com.example.smartbroecommerce.R2;
import com.example.smartbroecommerce.database.MachineProfile;
import com.example.smartbroecommerce.database.Order;
import com.example.smartbroecommerce.database.OrderStatus;
import com.example.smartbroecommerce.database.PaymentMethod;
import com.example.smartbroecommerce.main.cart.PaymentMethodFields;
import com.example.smartbroecommerce.main.cart.ShopCartDelegate;
import com.example.smartbroecommerce.main.checkout.device.CreditCardReaderAction;
import com.example.smartbroecommerce.main.checkout.device.IReaderAction;
import com.example.smartbroecommerce.main.checkout.device.NayaxCardReader;
import com.example.smartbroecommerce.main.maker.ProcessingDelegate;
import com.joanzapata.iconify.widget.IconTextView;
import com.taihua.pishamachine.LogUtil;

import java.util.Date;
import java.util.Timer;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Justin Wang from SmartBro on 19/1/18.
 * 信用卡支付页面
 */

public class ByCreditCard extends BasicCheckoutDelegate {

    @BindView(R2.id.icon_payment_method)
    IconTextView iconTextView = null;

    @BindView(R2.id.tv_order_total)
    AppCompatTextView tvOrderTotal = null;
    @BindView(R2.id.tv_card_reader_process)
    AppCompatTextView tvReaderProcess = null;

    @BindView(R2.id.btn_order_cancel)
    AppCompatButton btnCancel = null;

    @BindView(R2.id.by_credit_checkout_layout_wrap)
    LinearLayoutCompat wrap = null;
    @BindView(R2.id.by_credit_checkout_layout_title)
    LinearLayout titleBar = null;

    private int orderTotalAmount = 0;
    private Order order = null;
    private Timer timer = null;
    private BaseTimerTask timerTask = null;

    // 新版读卡器程序
    private NayaxCardReader cardReader = null;
    private IReaderAction readerAction = null;

    // 状态控制

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

    @Override
    public Object setLayout() {
        return R.layout.delegate_by_credit_card_checkout;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        if("en".equals(MachineProfile.getInstance().getLanguage())){
            this.titleBar.setBackgroundColor(Color.BLACK);
            this.wrap.setBackground(getResources().getDrawable(R.mipmap.au_pizza_ninja_bg));
        }

        // 显示订单总金额
        this.orderTotalAmount = (int) this.createOrder().getTotolAmount();
        this.tvOrderTotal.setText(
                getString(R.string.text_total) + " $" + Integer.toString(this.orderTotalAmount)
        );

        // 获取传递来的参数， 取得支付方式对象
        final Bundle args = getArguments();
        this.paymentMethod = PaymentMethod.findById(
                args.getLong(PaymentMethodFields.PAYMENT.name())
        );

        this.initPaymentMethodIcon();
    }

    /**
     * 设置代表支付方式的Icon: 只有现金一种
     */
    private void initPaymentMethodIcon() {
        final String iconText = "{fa-credit-card}";
        iconTextView.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
        iconTextView.setText(iconText);
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
        this.tvReaderProcess.setText("Please wait ...");
    }

    @Override
    public void onResume() {
        super.onResume();

        final Bundle args = getArguments();

        /*
         * 产生订单
         */
        if(args != null){
            if(this.paymentMethod != null){
                this.createOrder();
                this.order.prepareShoppingCartData();
                final String orderInJson = JSON.toJSONString(this.order);
                RestfulClient.builder()
                        .url(this.order.getOrderUrl())
                        .loader(getContext())
                        .params("muuid", Long.toString(AccountManager.getMachineId()))
                        .params("order",orderInJson)
                        .success(new ISuccess() {
                            @Override
                            public void onSuccess(String response) {
                                // 订单已经产生, 让用户进行支付
                                // {"error_no":100,"msg":"","data":{"payUrl":"http:\/\/htlc.smartbro.com.au\/storage\/tmp\/qr\/729669-7.png","order_no":1007}}
                                final int errorNumber =
                                        JSON.parseObject(response).getInteger("error_no");
                                if(errorNumber == RestfulClient.NO_ERROR){
                                    // 表示订单正确的生成了
                                    final JSONObject data =
                                            JSON.parseObject(response).getJSONObject("data");
                                    // 保存订单ID
                                    order.setId(data.getInteger("order_no"));
                                    _checkCardReader();
                                }
                            }
                        })
                        .error(new IError() {
                            @Override
                            public void onError(int code, String msg) {
                                LogUtil.LogInfo("订单生成错误: " +msg + ", " + new Date().toString());
                            }
                        })
                        .failure(new IFailure() {
                            @Override
                            public void onFailure() {
                                LogUtil.LogInfo("订单生成失败: " + new Date().toString());
                            }
                        })
                        .build()
                        .post();
            }else {
                throw new NullPointerException("支付时，支付的方式对象不可以为空");
            }
        }
    }

    /**
     * 新版
     */
    private void _checkCardReader(){
        // 初始化读卡器
        this.cardReader = new NayaxCardReader();
        this.readerAction = new CreditCardReaderAction(this.cardReader);
        this.readerAction.setProductInfo(1,this.orderTotalAmount,1);
        this.cardReader.getInstance().start(this.readerAction,100);
        // 开始监听
        // 监听消息
        if(this.timer == null){
            this.timer = new Timer(true);
        }
        this.timerTask = new BaseTimerTask(this);
        this.timer.schedule(this.timerTask,0,500);
    }

    @Override
    public Order createOrder() {
        this.order = new Order(-1, this.paymentMethod);
        return this.order;
    }

    @Override
    public void cancelCheckout() {

        try {
            if(this.timer != null){
                this.timer.cancel();
                this.timer = null;
                this.timerTask = null;
            }
            // 等待200毫秒
            Thread.sleep(510);
        } catch (InterruptedException e) {
            LogUtil.LogException(e);
        } catch (NullPointerException e){
            this.timer = null;
            this.timerTask = null;
        } finally {
            startWithPop(new ShopCartDelegate());
        }
    }

    @Override
    public void onTimer() {
        getProxyActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(cardReader.getIsReadyToDeliveryProduct()){
                    // 获取到可以制作产品的标志位为真了
                    if(timer != null){
                        timer.cancel();
                        timerTask.cancel();
                        timerTask = null;
                        timer = null;
                    }

                    // 可以烤饼了
                    // 对订单的状态进行更新
                    order.setStatus(OrderStatus.PAID);
                    // 进入下一个制作的环节, 展示Pizza制作过程
                    ProcessingDelegate delegate = new ProcessingDelegate();
                    Bundle args = new Bundle();
                    // Todo 测试信用卡烤饼， 临时用了假的订单ID, 一定要删除
                    //                                    args.putInt("orderId",2);   // 订单ID
                    args.putInt("orderId",order.getId());   // 订单ID
                    args.putInt("changes", 0);           // 找零金额
                    args.putBoolean("needCallBakingCmd", true);
                    delegate.setArguments(args);
                    startWithPop(delegate);
                }
            }
        });
    }

    /**
     * 简单的打印输出
     * @param line
     */
    @Override
    public void echo(String line, boolean clear){
        this.tvReaderProcess.setText(line);
    }
}
