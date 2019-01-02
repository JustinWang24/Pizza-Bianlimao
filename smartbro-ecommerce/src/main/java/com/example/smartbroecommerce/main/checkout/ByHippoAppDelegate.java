package com.example.smartbroecommerce.main.checkout;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.smartbro.delegates.SmartbroDelegate;
import com.example.smartbro.net.RestfulClient;
import com.example.smartbro.net.callback.IFailure;
import com.example.smartbro.net.callback.ISuccess;
import com.example.smartbro.utils.timer.BaseTimerTask;
import com.example.smartbro.utils.timer.ITimerListener;
import com.example.smartbroecommerce.R;
import com.example.smartbroecommerce.R2;
import com.example.smartbroecommerce.database.MachineProfile;
import com.example.smartbroecommerce.database.Product;
import com.example.smartbroecommerce.main.maker.ProcessingDelegate;
import com.example.smartbroecommerce.main.product.ListDelegate;
import com.example.smartbroecommerce.utils.BetterToast;

import butterknife.BindView;
import butterknife.OnClick;
import android.support.v7.widget.AppCompatTextView;

import java.util.Timer;

/**
 * 使用河马生鲜App付款页面
 * Created by Justin Wang from SmartBro on 26/12/18.
 */
public class ByHippoAppDelegate extends SmartbroDelegate implements ITimerListener {
    private Product product = null;

    private String appId = null;
    private String assetId = null;

    /**
     * 从服务器获取的新生成的订单的ID
     */
    private String orderNo = null;
    /**
     * 需要支付的实际金额，以 分 为单位
     */
    private int actualFee = 0;
    /**
     * 从用户处扫描到的支付码
     */
    private String customerPaymentCode = null;
    /**
     * 扫描的定时器
     */
    private Timer scanCustomerPaymentCodeTimer = null;

    private final int maxWaitSeconds = 60;  // 最多等待一分钟的时间
    private int scanCounter = 0;            // 当前第几次扫描
    private boolean isPayOrderApiCalled = false; // 表示是否已经调用过 反扫支付接口
    private boolean isOrderHasBeenPaid = false;  // 表示订单是否已经被支付了

    @BindView(R2.id.tv_toolbar_cancel_checkout_text)
    AppCompatTextView cancelCheckoutText;
    @BindView(R2.id.tv_product_name_in_checkout)
    AppCompatTextView productNameText;
    @BindView(R2.id.tv_product_price_in_checkout)
    AppCompatTextView productPriceText;

    @OnClick(R2.id.tv_toolbar_cancel_checkout_text)
    void onCancelCheckoutClicked(){
        this.backToProductList();
    }

    @Override
    public Object setLayout() {
        return R.layout.delegate_hippo_app_checkout;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        Bundle args = getArguments();
        this.product = Product.find(args.getLong("productId"));
        this.scanCounter = 0;
        if(this.product != null){
            this.productNameText.setText(getString(R.string.text_product_you_select) + this.product.getName());

            MachineProfile machineProfile = MachineProfile.getInstance();
            this.appId = machineProfile.getHippoAppId();
            this.assetId = machineProfile.getSerialName();

            final int priceInCents = (int) this.product.getPrice() * 100;

            // 开始扫描
            this.startScanning();

            // 通知服务器，以便创建一个新的订单
            RestfulClient.builder()
                    .url("hippo/create-order")
                    .params("appId", this.appId)
                    .params("assetId",this.assetId)
                    .params("itemId",this.product.getItemId())
                    .params("itemName",this.product.getName())
                    .params("price",Integer.toString(priceInCents))   // 以分为单位
                    .success(new ISuccess() {
                        @Override
                        public void onSuccess(String response) {
                            onCreateOrderCallSuccess(response);
                        }
                    })
                    .failure(new IFailure() {
                        @Override
                        public void onFailure() {
                            BetterToast.getInstance()
                                    .showText(getProxyActivity(),"系统维护中 无法创建订单 请稍候再试试");
                        }
                    })
                    .build().post();
        }
    }

    /**
     * 在页面跳转前把本地使用的变量重新设置一下
     */
    private void _resetLocalVariables(){
        Log.i("Info", " _resetLocalVariables 执行了 ");
        if(this.scanCustomerPaymentCodeTimer != null){
            this.scanCustomerPaymentCodeTimer.cancel();
            this.scanCustomerPaymentCodeTimer = null;
        }
        this.orderNo = null;
        this.product = null;
        this.actualFee = 0;
        this.customerPaymentCode = null;
        this.isOrderHasBeenPaid = false;
        this.isPayOrderApiCalled = false;
    }

    /**
     * 返回产品列表页面
     */
    protected void backToProductList(){
        this._resetLocalVariables();
        startWithPop(new ListDelegate());
    }

    /**
     * 反扫支付接口
     */
    private void payOrderNow(){
        RestfulClient.builder()
                .url("hippo/pay-order")
                .params("orderNo", this.orderNo)
                .params("totalPrice", Integer.toString(this.actualFee))
                .params("tempcode", this.customerPaymentCode)
                .params("appId", this.appId)
                .params("assetId", this.assetId)
                .success(new ISuccess() {
                    @Override
                    public void onSuccess(String response) {
                        onPayOrderCallSuccess(response);
                    }
                })
                .failure(new IFailure() {
                    @Override
                    public void onFailure() {

                    }
                })
                .build().post();
    }

    private void checkIsOrderHasBeenPaidNow(){
        // 查询叮当是否已经被支付
        RestfulClient.builder()
                .url("hippo/query-payment")
                .params("orderNo", this.orderNo)
                .params("appId", this.appId)
                .params("assetId", this.assetId)
                .success(new ISuccess() {
                    @Override
                    public void onSuccess(String response) {
                        onCheckOrderCallSuccess(response);
                    }
                })
                .failure(new IFailure() {
                    @Override
                    public void onFailure() {

                    }
                })
                .build().post();
    }

    /**
     * 跳转到烤饼的页面
     */
    protected void goToNextView(){
        if(this.scanCustomerPaymentCodeTimer != null){
            this.scanCustomerPaymentCodeTimer.cancel();
            this.scanCustomerPaymentCodeTimer = null;
        }

        SmartbroDelegate delegate = new ProcessingDelegate();
        this._redirectToDelegate(delegate);
        this._resetLocalVariables();
    }

    /**
     * 用于本view的快捷的跳转方法
     * @param delegate
     */
    private void _redirectToDelegate(SmartbroDelegate delegate){
        Bundle args = new Bundle();
        args.putString("orderNo",this.orderNo);
        args.putInt("totalPrice",this.actualFee);
        args.putString("tempcode", this.customerPaymentCode);
        args.putString("appId", this.appId);
        args.putString("assetId", this.assetId);
        args.putString("productName", this.product.getName());
        args.putString("productItemId", this.product.getItemId());
        args.putLong("productId", this.product.getId());
        delegate.setArguments(args);
        startWithPop(delegate);
    }

    /**
     * 开始扫描的具体方法
     */
    private void startScanning(){
        if(this.scanCustomerPaymentCodeTimer == null){
            this.scanCustomerPaymentCodeTimer = new Timer();
        }
        BaseTimerTask task = new BaseTimerTask(this);
        // 每隔 2 秒钟读取一下串口 或者查询一下服务器
        this.scanCustomerPaymentCodeTimer.schedule(task,2000,1000);
    }

    @Override
    public void onTimer() {
        getProxyActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i("Info","读取串口 " + Integer.toString(scanCounter));
                if(scanCounter <= maxWaitSeconds){
                    scanCounter++;

                    if(scanCounter > 10){
                        goToNextView();
                    }
                    if(customerPaymentCode == null){
                        // Todo 在这里打开扫码枪的扫描端口 开始循环读取扫描信息
                    }else {
                        // 已经取得了用户的付款码 那么准备跳转
                        if(isOrderHasBeenPaid){
                            // 通过网络查询，发现已经支付了
                            goToNextView();
                        }else{
                            if(!isPayOrderApiCalled){
                                // 还没有支付 调用 反扫支付接口 去进行支付
                                isPayOrderApiCalled = true;
                                payOrderNow();
                            }else{
                                // 支付了 通过 查询支付结果接口 去检查支付结果
                                checkIsOrderHasBeenPaidNow();
                            }
                        }
                    }
                }else{
                    // 超出最长扫描的等待时间
                    backToProductList();
                }
            }
        });
    }

    /**
     * 当创建订单接口被创建之后的处理
     * @param response
     */
    private void onCreateOrderCallSuccess(String response){
        final JSONObject resJson =
                JSON.parseObject(response);
        final int errorNo = resJson.getInteger("error_no");

        if(errorNo == RestfulClient.NO_ERROR){
            // 表示创建订单成功
            this.orderNo = resJson.getString("oid");
            final JSONObject data = resJson.getJSONObject("data");

            // 应付金额(最后付款的时候以这个金额为准)
            final double fee = data.getDouble("actual_fee");
            this.productPriceText.setText("¥ " + Double.toString(fee));
            this.actualFee = (int) (fee * 100); // 转换 从元变成 分
        }else{
            // 服务器返回了错误的数据
            BetterToast.getInstance()
                    .showText(getProxyActivity(),"系统繁忙 无法创建订单 请稍候再试试");
        }
    }

    /**
     * 当反扫支付接口的返回数据的处理
     * @param response
     */
    private void onPayOrderCallSuccess(String response){
        final JSONObject resJson =
                JSON.parseObject(response);
        final int errorNo = resJson.getInteger("error_no");
        final String nextActionString = resJson.getString("next");
        this.isPayOrderApiCalled = true;

        if(errorNo != RestfulClient.NO_ERROR){
            // 这次付款失败
            this._redirectToDelegate(new HippoPaymentFailedDelegate());
        }else{
            // 查询的结果表示调用成功，查看下一步如何操作
            if(RestfulClient.ACTION_GO_NEXT.equals(nextActionString)){
                // 表示支付已经确认，可以去烤饼了
                this.isOrderHasBeenPaid = true;
                Log.i("Info","表示支付已经确认，可以去烤饼了");
            }else if(RestfulClient.ACTION_KEEP_CHECKING.equals(nextActionString)){
                // 表示需要继续查询订单的支付状态，这个时候，什么也不用做，继续等待即可
                Log.i("Info","需要继续查询订单的支付状态");
            }else if(RestfulClient.ACTION_ORDER_CLOSE.equals(nextActionString)){
                // Todo 表示订单处于关闭的状态，该如何处理写在这里
                Log.i("Info","表示订单处于关闭的状态");
            }
        }
    }

    /**
     * 当查询支付结果接口的返回数据的处理
     * @param response
     */
    private void onCheckOrderCallSuccess(String response){
        final JSONObject resJson =
                JSON.parseObject(response);
        final int errorNo = resJson.getInteger("error_no");
        final String nextActionString = resJson.getString("next");

        if(errorNo != RestfulClient.NO_ERROR){
            // 这次付款失败
            this._redirectToDelegate(new HippoPaymentFailedDelegate());
        }else{
            // 查询的结果表示调用成功，查看下一步如何操作
            if(RestfulClient.ACTION_GO_NEXT.equals(nextActionString)){
                // 表示支付已经确认，可以去烤饼了
                this.isOrderHasBeenPaid = true;
                Log.i("Info","表示支付已经确认，可以去烤饼了");
            }else if(RestfulClient.ACTION_KEEP_CHECKING.equals(nextActionString)){
                // 表示需要继续查询订单的支付状态，这个时候，什么也不用做，继续等待即可
                Log.i("Info","需要继续查询订单的支付状态");
            }else if(RestfulClient.ACTION_ORDER_CLOSE.equals(nextActionString)){
                // Todo 表示订单处于关闭的状态，该如何处理写在这里
                Log.i("Info","表示订单处于关闭的状态");
            }
        }
    }

    @Override
    public void onDestroyView() {
        this._resetLocalVariables();
        super.onDestroyView();
    }
}
