package com.example.smartbroecommerce.main.checkout;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
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
import com.example.smartbroecommerce.main.maker.ProcessingDelegate;
import com.joanzapata.iconify.widget.IconTextView;
import com.taihua.pishamachine.LogUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Justin Wang from SmartBro on 1/1/18.
 */

public class ByCashDelegate extends BasicCheckoutDelegate {

    // 收到的现金金额
    private int totalCashReceived = 0;
    private int orderTotalAmount = 0;
    private CashMessageHandler cashMessageHandler = null;
    private Timer checkCashier;
    private boolean orderCompleteReported = false;
    private boolean isStartToRedirectToOtherDelegate = false;
    private String dollarSign = "$";

    @BindView(R2.id.icon_payment_method)
    IconTextView iconTextView = null;
    @BindView(R2.id.tv_order_total)
    AppCompatTextView tvOrderTotal = null;
    @BindView(R2.id.tv_received_total)
    AppCompatTextView tvReceivedTotal = null;
    @BindView(R2.id.btn_order_cancel)
    AppCompatButton btnCancelOrder = null;

    @BindView(R2.id.by_cash_layout_title)
    LinearLayout titleBar = null;
    @BindView(R2.id.by_cash_layout_wrap)
    LinearLayoutCompat wrap = null;

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
        // 不需要通过网络来检查订单状态
        this.stopCheckingOrderStatusThroughNetwork = true;

        // 设置是否开始向其他Delegate跳转的标志位
        this.isStartToRedirectToOtherDelegate = false;

        this.updateLastClickActionTimeStamp();

        if("cn".equals(MachineProfile.getInstance().getLanguage())){
            this.dollarSign = "¥";
        }
    }

    @Override
    public Object setLayout() {
        return R.layout.delegate_by_cash_checkout;
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
                getString(R.string.text_total) + ": " + this.dollarSign + Integer.toString(this.orderTotalAmount)
        );

        // 显示已经收到的金额
        this.tvReceivedTotal.setText(
                "$" +Integer.toString(this.totalCashReceived)
        );
        // 获取传递来的参数， 取得支付方式对象
        final Bundle args = getArguments();
        this.paymentMethod = PaymentMethod.findById(
                args.getLong(PaymentMethodFields.PAYMENT.name())
        );
        this.initPaymentMethodIcon();

        // 设置是否开始向其他Delegate跳转的标志位
        this.isStartToRedirectToOtherDelegate = false;
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
        final Bundle args = getArguments();

        this.orderCompleteReported = false;
        // 设置是否开始向其他Delegate跳转的标志位
        this.isStartToRedirectToOtherDelegate = false;

        LogUtil.LogInfo("开始执行现金订单的处理任务0: " + new Date().toString());

        if(args != null){
            LogUtil.LogInfo("开始执行现金订单的处理任务1: ");
            if(this.paymentMethod != null){
                LogUtil.LogInfo("开始执行现金订单的处理任务2: ");
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

                                LogUtil.LogInfo("开始执行现金订单的处理任务3: error no = " + Integer.toString(errorNumber));

                                if(errorNumber == RestfulClient.NO_ERROR){
                                    // 表示订单正确的生成了
                                    final JSONObject data =
                                            JSON.parseObject(response).getJSONObject("data");
                                    // 保存订单ID
                                    order.setId(data.getInteger("order_no"));
                                    checkCashCollector();
                                    LogUtil.LogInfo("开始执行现金订单的处理任务4: 打开投币器开始检查");
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
                NullPointerException e = new NullPointerException("支付时，支付的方式对象不可以为空");
                LogUtil.LogStackTrace(e,"8080808080808080");
                throw e;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // 设置是否开始向其他Delegate跳转的标志位
        this.isStartToRedirectToOtherDelegate = false;
        LogUtil.LogInfo("现金支付页面 resume");
    }

    @Override
    public Order createOrder() {
        this.order = new Order(-1, this.paymentMethod);
        return this.order;
    }

    /**
     * 检查投币器的金额，然后更新显示器的上的数额
     */
    private void checkCashCollector(){
        // Todo 初始化串口管理器以及设置handler
        this.cashMessageHandler = CashMessageHandler.getInstance();
        this.cashMessageHandler.init(0, this.orderTotalAmount);

        final BaseTimerTask timerTask = new BaseTimerTask(this);
        this.checkCashier = new Timer(true);
        this.checkCashier.schedule(timerTask, 100, 300);
    }

    @Override
    public void onTimer() {
        getProxyActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                if(orderTotalAmount > 0 && cashMessageHandler.getLatestCashReceivedValue() >= orderTotalAmount){
//                    // 表示订单金额有效, 且已经被支付了足够的金额
//
//                    final Date now = new Date();
//                    if(now.getTime() - receivedNumberLastUpdateTime.getTime() >= 2000){
//                        // 如果已经超过1500毫秒都没有更新，表示已经投入了足够的钱，并且不会再投钱了,那么就可以开始烤饼和找零了
//                        cashMessageHandler.giveCustomerChanges(totalCashReceived - orderTotalAmount); // 找零
//                    }
//                }
                // 更新收到的钱数金额
                if(cashMessageHandler != null && totalCashReceived != cashMessageHandler.getLatestCashReceivedValue()){
                    totalCashReceived = cashMessageHandler.getLatestCashReceivedValue();
                    // 收到的钱数发生了变化
                    tvReceivedTotal.setText(
                            dollarSign + Integer.toString(totalCashReceived)
                    );
                    // 重置自动返回以前页面的计数器
                    updateLastClickActionTimeStamp();
                }

                if(cashMessageHandler != null && cashMessageHandler.readyToMakePizza() && !orderCompleteReported){
                    orderCompleteReported = true;
                    // 如果找零已经完成，更新订单的状态并跳转
                    RestfulClient.builder()
                            .url("switch_order_to_complete")
                            .params("order_id",Integer.toString(order.getId()))   // 提交order的ID
                            .success(new ISuccess() {
                                @Override
                                public void onSuccess(String response) {

                                    final int errorNumber =
                                            JSON.parseObject(response).getInteger("error_no");
                                    if(errorNumber == RestfulClient.NO_ERROR){
                                        // 表示订单状态切换成功
                                        // 当前的Timer可以取消了
                                        if(checkCashier != null){
                                            checkCashier.cancel();
                                            checkCashier = null;
                                        }

                                        // 对订单的状态进行更新
                                        order.setStatus(OrderStatus.PAID);
                                        // 进入下一个制作的环节, 展示Pizza制作过程
                                        ProcessingDelegate delegate = new ProcessingDelegate();
                                        Bundle args = new Bundle();
                                        args.putInt("orderId",order.getId());   // 订单ID
                                        args.putInt("changes", totalCashReceived - orderTotalAmount);           // 找零金额
                                        args.putBoolean("needCallBakingCmd", true);
                                        delegate.setArguments(args);

                                        LogUtil.LogInfo("order id" + Integer.toString(order.getId()));
                                        LogUtil.LogInfo("找零金额" + Integer.toString(totalCashReceived - orderTotalAmount));

                                        // 设置是否开始向其他Delegate跳转的标志位, 表示已经开始跳转了
                                        if(!isStartToRedirectToOtherDelegate){
                                            isStartToRedirectToOtherDelegate = true;
                                            // 执行跳转操作
                                            startWithPop(delegate);
                                        }
                                    }
                                }
                            })
                            .build()
                            .get();
                }

                if(cashMessageHandler == null){
                    LogUtil.LogInfo("cashMessageHandler 为 null 了");
                    if(checkCashier != null){
                        checkCashier.cancel();
                        checkCashier = null;
                    }
                    // 设置是否开始向其他Delegate跳转的标志位, 表示已经开始跳转了
                    if(!isStartToRedirectToOtherDelegate){
                        isStartToRedirectToOtherDelegate = true;
                        // 执行跳转操作
                        startWithPop(new ShopCartDelegate());
                    }
                }
            }
        });
    }

    @Override
    public void cancelCheckout() {
        // Todo 1: 关闭投币器
        // Todo 2: 获取收款的金额, 如果大于0, 那么就发出收款金额相同数量的找零脉冲

        // 下面这个方法都会完成上面的2步
        this.cashMessageHandler.giveCustomerChangesOnly(true);
        // 推到上个界面
//        startWithPop(new ShopCartDelegate());
        this.cashMessageHandler = null;
    }

    /**
     * 设置代表支付方式的Icon: 只有现金一种
     */
    private void initPaymentMethodIcon() {
        final String iconText = "{fa-money}";
        iconTextView.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
        iconTextView.setText(iconText);
    }
}
