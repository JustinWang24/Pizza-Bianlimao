package com.example.smartbroecommerce.machine;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.listener.OnItemClickListener;
import com.example.smartbro.delegates.SmartbroDelegate;
import com.example.smartbro.net.RestfulClient;
import com.example.smartbro.net.callback.IError;
import com.example.smartbro.net.callback.IFailure;
import com.example.smartbro.net.callback.ISuccess;
import com.example.smartbro.ui.banner.BannerCreator;
import com.example.smartbro.utils.timer.BaseTimerTask;
import com.example.smartbro.utils.timer.ITimerListener;
import com.example.smartbroecommerce.Auth.MachineInitHandler;
import com.example.smartbroecommerce.R;
import com.example.smartbroecommerce.R2;
import com.example.smartbroecommerce.database.MachineProfile;
import com.example.smartbroecommerce.database.Position;
import com.example.smartbroecommerce.database.ShoppingCart;
import com.example.smartbroecommerce.main.product.ListDelegate;
import com.example.smartbroecommerce.utils.BetterToast;
import com.taihua.pishamachine.LogUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;

import butterknife.BindView;

/**
 * Created by Justin Wang from SmartBro on 12/12/17.
 * 这个是程序的首页，在无人操作的时候，就是显示这个页面。本页面的主要功能如下
 * 1: 检查如果没有产品的数据，或者产品数据已经过期了，那么就重新加载产品的最新数据。这个操作在后台进行，本次依然可以使用过期的产品图片
 * 2: 显示产品图片的幻灯片
 * 3: 显示一个按钮"开始使用 Start"
 * 4: 如果购物车中有商品，则清空购物车
 */

public class HomeDelegate extends SmartbroDelegate implements OnItemClickListener, ITimerListener{

    private static ArrayList<String> bannerImages = new ArrayList<>();
    private static boolean isBannerCreated = false;

    private Timer mTimer = null;
    private static Boolean isProductReloaded = false; // 表示今天的产品/库存更新了没有
    private static int CURRENT_HOUR_NUMBER = -100;

    @BindView(R2.id.banner_home)
    ConvenientBanner<String> convenientBanner;

    @Override
    public Object setLayout() {
        // 指定Layout
        return R.layout.delegate_machine_home;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(CURRENT_HOUR_NUMBER == -100){
            final Calendar today = Calendar.getInstance();
            final int hour = today.get(Calendar.HOUR_OF_DAY);
            if(hour == 0){
                CURRENT_HOUR_NUMBER = 23;
            }else {
                CURRENT_HOUR_NUMBER = hour -1;
            }
        }
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        // 初始化轮播图
        this.initMachineAdsBanner();
    }

    private void initMachineAdsBanner(){
        if(!isBannerCreated){
            final HomeDelegate that = this;
            RestfulClient.builder()
                    .url("machines/get_images")
                    .loader(getContext())
                    .params("uuid", MachineProfile.getInstance().getUuid())
                    .success(new ISuccess() {
                        @Override
                        public void onSuccess(String response) {
                            final JSONArray machineImages =
                                    JSON.parseObject(response).getJSONArray("data");
                            final int size = machineImages.size();
                            for (int i = 0; i < size; i++) {
                                final String imgUrl = machineImages.getString(i);
                                bannerImages.add(imgUrl);
                            }
                            BannerCreator.setDefault(
                                    convenientBanner,
                                    bannerImages,
                                    that
                            );
                            isBannerCreated = true;
                        }
                    })
                    .error(new IError() {
                        @Override
                        public void onError(int code, String msg) {
                            // 加载数据发生错误的时候
                            Log.i("HomeDelegate 初始化轮播图", msg);
                        }
                    })
                    .failure(new IFailure() {
                        @Override
                        public void onFailure() {
                            // 失败的时候
                            Log.i("HomeDelegate 初始化轮播图", "彻底失败");
                        }
                    })
                    .build()
                    .post();
        }else {
            // 已经创建了
            BannerCreator.setDefault(
                    convenientBanner,
                    bannerImages,
                    this
            );
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // 只要这个页面一显示，就要清空购物车
        ShoppingCart.getInstance().clear();

        // 检查产品数据是否已经过期
        if(this.isProductDataExpired()){
            // 再次初始化轮播图
            this.initMachineAdsBanner();
        }
    }

    @Override
    public void onItemClick(int i) {
        if(Position.isOutOfStock()){
            // 没有存货了
            BetterToast.getInstance().showText(_mActivity,getString(R.string.err_text_out_of_stock));
        }else {
            startWithPop(new ListDelegate());
        }
    }

    // Todo 创建判定本地产品数据是否过期的方法
    private boolean isProductDataExpired(){
        return false;
    }

    @Override
    public void onDestroy() {
        // 注销总线的注册
        super.onDestroy();
    }

    @Override
    public void onResume(){
        super.onResume();
        final BaseTimerTask task = new BaseTimerTask(this);
        this.mTimer = new Timer(true);
        // 每十秒执行一次
        this.mTimer.schedule(task,1000, 10000);
    }

    @Override
    public void onPause(){
        super.onPause();
        if(this.mTimer != null){
            this.mTimer.cancel();
        }
    }

    @Override
    public void onTimer() {
        // 检查当前的时间，如果在某个特定的时间段内，执行更新库存数据的操作
        final Calendar today = Calendar.getInstance();
        final int hour = today.get(Calendar.HOUR_OF_DAY);
        if(hour != CURRENT_HOUR_NUMBER){
            // 如果现在是产品更新时间, 已经过了一个小时了
            if(!isProductReloaded){
                // 如果产品还没有更新
                final String machineSerialNumber = MachineProfile.getInstance().getSerialName();
                RestfulClient.builder()
                        .url("machines/init")
                        .params("serial_number",machineSerialNumber)
                        .success(new ISuccess() {
                            @Override
                            public void onSuccess(String response) {
                                // 设备初始化成功, 把服务器返回结果与认证的监听类对象传给handler去处理
                                MachineInitHandler.initMachine(response);
                                isProductReloaded = true;
                                CURRENT_HOUR_NUMBER = hour;
                                Date date = new Date();
                                LogUtil.LogInfoForce("从服务器同步产品数据成功: " + date.toString());
                            }
                        })
                        .error(new IError() {
                            @Override
                            public void onError(int code, String msg) {
                                Date date = new Date();
                                LogUtil.LogInfoForce("自动同步产品数据发生错误: " + date.toString());
                            }
                        })
                        .failure(new IFailure() {
                            @Override
                            public void onFailure() {
                                Date date = new Date();
                                LogUtil.LogInfoForce("自动同步产品数据失败: " + date.toString());
                            }
                        })
                        .build()
                        .post();

            }
        }else{
            // 不在产品自动更新时间内，因此标记为没有自动更新
            isProductReloaded = false;
        }
    }
}
