package com.example.smartbroecommerce.main.pages;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bigkoo.convenientbanner.ConvenientBanner;
import com.example.smartbro.delegates.SmartbroDelegate;
import com.example.smartbro.net.RestfulClient;
import com.example.smartbro.net.callback.IError;
import com.example.smartbro.net.callback.IFailure;
import com.example.smartbro.net.callback.ISuccess;
import com.example.smartbro.ui.banner.BannerCreator;
import com.example.smartbro.utils.timer.BaseTimerTask;
import com.example.smartbro.utils.timer.ITimerListener;
import com.example.smartbroecommerce.R;
import com.example.smartbroecommerce.R2;
import com.example.smartbroecommerce.database.MachineProfile;
import com.example.smartbroecommerce.database.Product;
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
import com.taihua.pishamachine.DoorManager;
import com.taihua.pishamachine.LogUtil;
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

    private String userRealInput = "";

    @Override
    public Object setLayout() {
        // Step 1
        return R.layout.delegate_delivery_code_new;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        // Step 2
        BannerCreator.setDefault(
                this.convenientBanner,
                BannerTool.GetInstance().getBannerImages(),
                this
        );
    }

    @Override
    public void onResume() {
        // Step 3
        super.onResume();
        try {
            // 1: 发送启用 QR 识别 命令
            Tx200Client.getClientInstance().activateQrReader();
            // 2: 启动定时器
            _startTimer();
        }catch (Exception e){
            LogUtil.LogException(e);
        }
    }

    @Override
    public void onDestroyView(){
        // Step 4： 清空扫码器的缓冲区 然后销毁
        Tx200Client.getClientInstance().clearCode();
        // 清除已经输入的付款码
        this.deliveryCode.setText("");
        super.onDestroyView();
    }

    /**
     * 自提码: 用户只有在付款之后才会得到的代码，一旦认证成功就直接出饼
     */
    @OnClick(R2.id.btn_key_confirm)
    void onConfirmClick(){
        // 先取得设备的id
        this.hippoAppId = MachineProfile.getInstance().getHippoAppId();
        this.assetId = MachineProfile.getInstance().getSerialName();

        String codeString = this.deliveryCode.getText().toString();
        if (this.isFirstLetterHash(codeString)){
            // 如果第一个字母是# 那么从 userRealInput 获取真正的输入
            codeString = "#" + this.userRealInput;
        }

        // 判断是否为特殊的维护码
        if ( "#859280".equals(codeString) ){
            // 暂停设备运行密码 #859280
            Bundle args = new Bundle();
            this._redirectToDelegate(new StopWorkingDelegate(),args);
        }else if("#351486".equals(codeString)){
            // 上货密码 #351486
            this.unlockTheDoor();
        }
//        else if("#666".equals(codeString)){
//            final long productId = 5;
//            ShoppingCart.getInstance().addProduct(productId,this);
//            final SmartbroDelegate delegate = new DeliveryCodeSuccessDelegate();
//            Bundle args = new Bundle();
//            args.putString("appId", this.hippoAppId);
//            args.putString("assetId", this.assetId);
//            args.putString("deliveryCode", "666");
//            args.putString("msg", "测试");
//            args.putString("itemId", "jkdd001");
//            args.putDouble("productPrice", 1.0);
//            delegate.setArguments(args);
//            startWithPop(delegate);
//        }
        else if(codeString.length() > 3){
            // 输入自提码
            this.deliveryCode.setText(getString(R.string.text_network_communication));
            _checkCode(codeString);
        }
    }

    /**
     * 开门上货的功能
     */
    private void unlockTheDoor(){
        _stopTimer();
        try {
            Thread.sleep(300);
            final DoorManager doorManager = DoorManager.getInstance();
            doorManager.unlockDoor();
            Thread.sleep(300);
            startWithPop(new StockManagerDelegate());
        }catch (Exception e){
//            this.sentryCapture(e);
            LogUtil.LogException(e);
        }
    }

    /**
     * 检查提货码
     * @param codeString
     */
    private void _checkCode(final String codeString){
        if(this.hippoAppId == null){
            this.hippoAppId = MachineProfile.getInstance().getHippoAppId();
            this.assetId = MachineProfile.getInstance().getSerialName();
        }

        if (this.isFirstLetterHash(codeString)){
            // 是特殊代码
            RestfulClient.builder()
                .url("hippo/check-code") // 锁定要提货的商品接口
                .params("hippoAppId",this.hippoAppId)
                .params("assetId",this.assetId)
                .params("tempcode",codeString)
                .success(new ISuccess() {
                    @Override
                    public void onSuccess(String response) {
                    // 网络通信成功
                    onCheckSpecialCodeSuccess(response);
                    }
                })
                .failure(new IFailure() {
                    @Override
                    public void onFailure() {
                    onCheckSpecialCodeFailed();
                    }
                }).build().post();
        }else {
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
                }).error(new IError() {
                    @Override
                    public void onError(int code, String msg) {
                        BetterToast.getInstance()
                                .showText(getProxyActivity(),msg + " - " + Integer.toString(code));
                    }
                }).build().post();
        }
    }

    /**
     * 当特殊码验证请求执行成功
     * @param response
     */
    private void onCheckSpecialCodeSuccess(String response){
        final JSONObject res = JSON.parseObject(response);
        final int errorNo = res.getInteger("error_no");
        this.userRealInput = "";
        this.deliveryCode.setText("");
        if(errorNo == RestfulClient.NO_ERROR){
            // 跳转到加饼的界面，同时打开柜门
            this.unlockTheDoor();
        }else {
            BetterToast.getInstance().showText(getProxyActivity(),"您输入的提货码无效");
        }
    }

    /**
     * 当特殊码验证请求执行失败
     */
    private void onCheckSpecialCodeFailed(){
        this.userRealInput = "";
        this.deliveryCode.setText("");
        BetterToast.getInstance().showText(getProxyActivity(),"您输入的提货码无效");
    }

    /**
     * 检查自提码的网络调用成功
     * @param response 服务器返回的字符串形式的处理结果
     * @param deliveryCode 服务器返回的字符串形式的处理结果
     */
    private void onCheckCodeSuccess(String response, String deliveryCode){
        final JSONObject res = JSON.parseObject(response);
        final int errorNo = res.getInteger("error_no");

        if(errorNo == RestfulClient.NO_ERROR){
            final String itemId = res.getString("p");
            final String msg = res.getString("msg");
            final double itemPrice = res.getFloat("price");
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
                args.putString("msg", msg);                     // 服务器返回的消息

                // 和产品相关的信息
                args.putDouble("productPrice",itemPrice);
                SmartbroDelegate delegate = new DeliveryCodeSuccessDelegate();
                _redirectToDelegate(delegate, args);
            }else{
                BetterToast.getInstance().showText(
                        getActivity(),
                        DeliveryCodeMessage.CODE_VERIFY_OK_BUT_NO_PRODUCT_TEXT
                );
                SmartbroDelegate delegate = new ListDelegate();
                Bundle args = new Bundle();
                args.putInt("errorCode",DeliveryCodeMessage.CODE_VERIFY_OK_BUT_NO_PRODUCT);
                _redirectToDelegate(delegate,args);
            }
        }else {
            final String msg = res.getString("msg");
            // 失败的调用
            BetterToast.getInstance().showText(
                    getActivity(),
                    msg
            );
            // 验证取货码失败 返回产品列表页面
            SmartbroDelegate delegate = new ListDelegate();
            Bundle args = new Bundle();
            args.putInt("errorCode",DeliveryCodeMessage.CODE_VERIFY_FAILED);
            _redirectToDelegate(delegate,args);
        }
    }

    /**
     * 检查自提码的网络调用失败
     */
    private void onCheckCodeFailed(){
        BetterToast.getInstance().showText(
                getActivity(),
                DeliveryCodeMessage.CODE_VERIFY_FAILED_INTERNET_ISSUE_TEXT
        );
        Bundle args = new Bundle();
        args.putInt("errorCode",DeliveryCodeMessage.CODE_VERIFY_FAILED_INTERNET_ISSUE);
        this._redirectToDelegate(new HippoPaymentFailedDelegate(),args);
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
        if(this.isFirstLetterHash(code)){
            this.userRealInput += button.getText().toString();
            this.deliveryCode.setText(code + "#");
        }else{
            this.deliveryCode.setText(code + button.getText().toString());
        }
    }

    private boolean isFirstLetterHash(String input){
        if(input.length() > 0){
            char first = input.charAt(0);
            return first == '#';
        }else{
            return false;
        }
    }

    @Override
    public void onItemClick(int position) {

    }

    @Override
    public void onTimer() {
        getProxyActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final long now = new Date().getTime();
                final long timePassedAway = now - openTime;

                if(timePassedAway > 60000){
                    // 超过1分钟没有操作 返回产品列表页面
                    Bundle args = new Bundle();
                    args.putInt("errorCode",0);
                    _redirectToDelegate(new ListDelegate(), args);
                }else {
                    // 还没有超时，检查扫码器是否有返回的数据
                    try {
                        final CommandExecuteResult commandExecuteResult =
                                Tx200Client.getClientInstance().scan();
                        final String QrCodeString = commandExecuteResult.getResult();
                        if(
                                CommandExecuteResult.NOT_OK.equals(QrCodeString) ||
                                CommandExecuteResult.NOTHING.equals(QrCodeString) ||
                                CommandExecuteResult.KEEP_WAITING.equals(QrCodeString)
                        ){
                            // 都是没有意义的返回值，那么继续等待即可
                        }
                        else{
                            _startCodeVerifying(QrCodeString);
                        }
                    }catch (Exception e){
                        LogUtil.LogException(e);
                    }
                }
            }
        });
    }

    /**
     * 读取到了有意义的码 到服务器进行验证 如果失败 那么应该跳转回产品列表页面
     * @param qrCode 有意义的码
     */
    private void _startCodeVerifying(String qrCode){
        // 停止计时器
        this._stopTimer();

        // 验证二维码
        this._checkCode(qrCode);
    }

    /**
     * 读取超时以后 跳转到指定的view
     * @param delegate
     * @param args
     */
    private void _redirectToDelegate(SmartbroDelegate delegate, Bundle args){
        try{
            // 定时器停止
            this._stopTimer();
            delegate.setArguments(args);
            startWithPop(delegate);
        }catch (Exception e){
            LogUtil.LogException(e);
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
            this.timer = new Timer();
            this.baseTimerTask = new BaseTimerTask(this);
        }
        this.timer.schedule(this.baseTimerTask, 1000, 1000);
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
