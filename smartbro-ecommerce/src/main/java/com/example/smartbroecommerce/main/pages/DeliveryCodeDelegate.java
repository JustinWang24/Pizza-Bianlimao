package com.example.smartbroecommerce.main.pages;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bigkoo.convenientbanner.ConvenientBanner;
import com.example.smartbro.delegates.SmartbroDelegate;
import com.example.smartbro.net.RestfulClient;
import com.example.smartbro.net.callback.IFailure;
import com.example.smartbro.net.callback.ISuccess;
import com.example.smartbro.ui.banner.BannerCreator;
import com.example.smartbro.utils.timer.BaseTimerTask;
import com.example.smartbro.utils.timer.ITimerListener;
import com.example.smartbroecommerce.R;
import com.example.smartbroecommerce.R2;
import com.example.smartbroecommerce.database.MachineProfile;
import com.example.smartbroecommerce.database.Product;
import com.example.smartbroecommerce.database.ShoppingCart;
import com.example.smartbroecommerce.machine.InitDelegate;
import com.example.smartbroecommerce.main.checkout.HippoPaymentFailedDelegate;
import com.example.smartbroecommerce.main.product.ListDelegate;
import com.example.smartbroecommerce.main.stock.StockManagerDelegate;
import com.example.smartbroecommerce.utils.BannerTool;
import com.example.smartbroecommerce.utils.BetterToast;

import java.util.Date;
import java.util.Timer;
import butterknife.BindView;
import butterknife.OnClick;
import com.bigkoo.convenientbanner.listener.OnItemClickListener;
import com.taihua.pishamachine.MicroLightScanner.CommandExecuteResult;
import com.taihua.pishamachine.MicroLightScanner.Tx200Client;

/**
 * Created by Justin Wang from SmartBro on 10/1/18.
 * 取货码页面
 */

public class DeliveryCodeDelegate extends SmartbroDelegate implements ITimerListener, OnItemClickListener {

    @BindView(R2.id.et_delivery_code)
    TextView deliveryCode = null;
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
    @BindView(R2.id.btn_key_hash)
    Button btnKeyHash = null;

    @BindView(R2.id.btn_key_confirm)
    Button btnKeyConfirm = null;
    @BindView(R2.id.btn_key_delete)
    Button btnKeyDelete = null;

    @BindView(R2.id.btn_key_cancel_delivery)
    Button btnKeyCancel = null;

    @BindView(R2.id.ads_banner)
    ConvenientBanner<String> convenientBanner;

    private Timer timer = null;
    private BaseTimerTask baseTimerTask = null;
    private long openTime = 0;

    private String hippoAppId = null;
    private String assetId = null;

    /**
     * 自提码: 用户只有在付款之后才会得到的代码，一旦认证成功就直接出饼
     */
    @OnClick(R2.id.btn_key_confirm)
    void onConfirmClick(){
        // 先取得设备的id
        this.hippoAppId = MachineProfile.getInstance().getHippoAppId();
        this.assetId = MachineProfile.getInstance().getSerialName();

        // 判断是否为特殊的维护码
        final String codeString = this.deliveryCode.getText().toString();
        if ( "#111".equals(codeString) ){
            // 上货密码
            startWithPop(new StockManagerDelegate());
        }else if("#222".equals(codeString)){
            // 暂停设备运行密码
            startWithPop(new StopWorkingDelegate());
        }else if("#333".equals(codeString)){
            // 暂停设备运行密码
            startWithPop(new InitDelegate());
        }else if("#666".equals(codeString)){
            final long productId = 5;
            ShoppingCart.getInstance().addProduct(productId,this);
            final SmartbroDelegate delegate = new DeliveryCodeSuccessDelegate();
            Bundle args = new Bundle();
            args.putString("appId", this.hippoAppId);
            args.putString("assetId", this.assetId);
            args.putString("deliveryCode", "666");
            args.putString("msg", "测试");
            args.putString("itemId", "jkdd001");
            args.putDouble("productPrice", 1.0);
            delegate.setArguments(args);
            startWithPop(delegate);
        }else if(codeString.length() > 3){
            // 输入自提码
            this.deliveryCode.setText(getString(R.string.text_network_communication));
            _checkCode(codeString);
        }
    }

    private void _checkCode(final String codeString){
        // 发送提货请求到服务器
        RestfulClient.builder()
                .url("hippo/check-code") // 锁定要提货的商品接口
                .params("hippoAppId",this.hippoAppId)
                .params("assetId",this.assetId)
                .params("tempcode",codeString)
                .success(new ISuccess() {
                    @Override
                    public void onSuccess(String response) {
                        // 网络通信成功
                        onCheckCodeSuccess(response, codeString);
                    }
                })
                .failure(new IFailure() {
                    @Override
                    public void onFailure() {
                        onCheckCodeFailed();
                    }
                }).build().post();
    }

    /**
     * 检查自提码的网络调用成功
     * @param response 服务器返回的字符串形式的处理结果
     * @param deliveryCode 服务器返回的字符串形式的处理结果
     */
    private void onCheckCodeSuccess(String response, String deliveryCode){
        final JSONObject res = JSON.parseObject(response);
        final int errorNo = res.getInteger("error_no");
        final String itemId = res.getString("p");
        final String msg = res.getString("msg");
        final double itemPrice = res.getFloat("price");

        if(errorNo == RestfulClient.NO_ERROR){
            /*
            这个方法实际会执行一共2个接口的调用，在验证 自提码 的准确性之后，
            进行锁定商品接口的调用，然后把被锁定的商品的 itemId回送给上位机程序
             */
            final Product product = Product.find(itemId);
            if(product != null){
                // 跳转到确认支付页面
                Bundle args = new Bundle();
                args.putString("appId",this.hippoAppId);        // appId
                args.putString("assetId",this.assetId);         // assetId
                args.putString("itemId",itemId);                // 产品的 itemId
                args.putString("deliveryCode", deliveryCode);   // 自提码
                args.putString("msg", msg);   // 自提码

                // 和产品相关的信息
                args.putDouble("productPrice",itemPrice);

                SmartbroDelegate delegate = new DeliveryCodeSuccessDelegate();
                _redirectToDelegate(delegate, args);
            }else{
                BetterToast.getInstance().showText(
                        getActivity(),
                        "自提码指定的产品已经售完"
                );
                try {
                    Thread.sleep(1000);
                    _startTimer();
                }catch (Exception e){
                    sentryCapture(e);
                }
            }
        }else {
            // 清除已经输入的付款码
            this.deliveryCode.setText(msg);
            // 失败的调用
            BetterToast.getInstance().showText(
                    getActivity(),
                    msg
            );
            try {
                Thread.sleep(1000);
                _startTimer();
            }catch (Exception e){
                sentryCapture(e);
            }
        }
    }

    /**
     * 检查自提码的网络调用失败
     */
    private void onCheckCodeFailed(){
        BetterToast.getInstance().showText(
                getActivity(),
                "互联网连接异常, 请稍后再试"
        );
    }

    @OnClick(R2.id.btn_key_cancel_delivery)
    void onCancelClick(){
        startWithPop(new ListDelegate());
    }

    @OnClick(R2.id.btn_key_delete)
    void onDeleteClick(){
        String code = this.deliveryCode.getText().toString();
        if(code.length()>0){
            if(code.length() == 1){
                this.deliveryCode.setText("");
            }else {
                final String sub = code.substring(0,code.length()-1);
                this.deliveryCode.setText(sub);
            }
        }
    }

    private void handleClick(Button button){
        final String code = this.deliveryCode.getText().toString();
        this.deliveryCode.setText(code + button.getText().toString());
    }

    @Override
    public Object setLayout() {
        return R.layout.delegate_delivery_code_new;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        BannerCreator.setDefault(
                this.convenientBanner,
                BannerTool.GetInstance().getBannerImages(),
                this
        );
    }

    @Override
    public void onItemClick(int position) {

    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            // 1: 发送启用 QR 识别 命令
            Tx200Client.getClientInstance()
                    .activateQrReader();
        }catch (Exception e){
            Log.i("Info", e.getMessage());
        }

        // 2: 启动定时器
        _startTimer();
    }

    @Override
    public void onTimer() {
        getProxyActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final long now = new Date().getTime();
                if(now - openTime > 60000){
                    _stopTimer();
                    Bundle args = new Bundle();
                    _redirectToDelegate(new ListDelegate(), args);
                }else {
                    try {
                        final CommandExecuteResult commandExecuteResult = Tx200Client.getClientInstance().scan();
                        final String QrCodeString = commandExecuteResult.getResult();
                        if(!CommandExecuteResult.KEEP_WAITING.equals(QrCodeString)){
                            _checkCode(QrCodeString);

                            // 停止计时器
                            _stopTimer();

                            // 读取到之后，进行清空操作
                            Tx200Client.getClientInstance().clearCode();
                            Thread.sleep(200);
                            // 再启动扫码枪
                            Tx200Client.getClientInstance().activateQrReader();
                        }
                    }catch (Exception e){
                        sentryCapture(e);
                    }
                }
            }
        });
    }

    /**
     * 跳转到指定的view
     * @param delegate
     * @param args
     */
    private void _redirectToDelegate(SmartbroDelegate delegate, Bundle args){
        try{
            _stopTimer();
            Tx200Client.getClientInstance().clearCode();
            Thread.sleep(300);
            delegate.setArguments(args);
            startWithPop(delegate);
        }catch (Exception e){

        }
    }

    /**
     * 停止计时器
     */
    private void _stopTimer(){
        if(this.timer != null){
            this.timer.cancel();
            this.timer = null;
        }
        if (this.baseTimerTask != null){
            this.baseTimerTask.cancel();
            this.baseTimerTask = null;
        }
    }

    /**
     * 启动计时器
     */
    private void _startTimer(){
        this.openTime = new Date().getTime();
        if(this.timer == null){
            this.timer = new Timer(false);
            this.baseTimerTask = new BaseTimerTask(this);
            this.timer.schedule(this.baseTimerTask, 1000, 1000);
        }
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
    @OnClick(R2.id.btn_key_hash)
    void onKeyHashClick(){
        this.handleClick(this.btnKeyHash);
    }
}
