package com.example.smartbroecommerce.main.maker;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.View;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.smartbro.app.AccountManager;
import com.example.smartbro.delegates.SmartbroDelegate;
import com.example.smartbro.net.RestfulClient;
import com.example.smartbro.net.callback.IFailure;
import com.example.smartbro.net.callback.ISuccess;
import com.example.smartbro.utils.timer.BaseTimerTask;
import com.example.smartbro.utils.timer.ITimerListener;
import com.example.smartbroecommerce.R;
import com.example.smartbroecommerce.R2;
import com.example.smartbroecommerce.container.ContainerConfig;
import com.example.smartbroecommerce.database.Position;
import com.example.smartbroecommerce.database.ShoppingCart;
import com.example.smartbroecommerce.main.product.ListDelegate;
import com.example.smartbroecommerce.utils.UrlTool;
import com.taihua.pishamachine.LogUtil;
import com.taihua.pishamachine.MachineStatusOfMakingPizza;
import com.taihua.pishamachine.PishaMachineManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import butterknife.BindView;

/**
 * Created by Justin Wang from SmartBro on 19/12/17.
 */

public class ProcessingDelegate extends SmartbroDelegate
    implements ITimerListener{

    // 表示是否刚刚开机
    public static boolean IS_JUST_POWER_ON = false;

    // 和网络通信相关的属性
    private int orderId = -1;
    private String orderNo = null;
    private String appId = null;
    private String assetId = null;

    // 和制作Pizza相关的类
    private List<Position> positions = null;
    private ArrayList<Integer> positionsIndex = null;
    private Timer mTimer = null;
    private BaseTimerTask baseTimerTask = null;
    private PizzaMakerHandler pizzaMakerHandler = null;
    private PishaMachineManager pishaMachineManager = null;

    private boolean pizzaMachineResetDone = false;
    private boolean needCallBakingCmd = true;

    // 播放声音的相关属性
    private boolean isPlayingAudio = false;
    private MediaPlayer mediaPlayer = null;
    private int nextAudioRaw = -1;
    private boolean pizzaDoneAudioHasBeenPlayed = false;    // pizza制作完成语音已经被播放过了

    @BindView(R2.id.tv_which_product_is_making)
    AppCompatTextView mtvWhichProductIsMaking = null;
    @BindView(R2.id.making_progress_layout_wrap)
    LinearLayoutCompat wrap = null;
    @BindView(R2.id.making_pizza_animation_image)
    ImageView makingPizzaAnimationImage = null;

    @Override
    public Object setLayout() {
        return R.layout.delegate_maker_progress_new;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        Bundle args = getArguments();
        this.orderId = args.getInt("orderIntegerId");
        this.orderNo = args.getString("orderNo");
        this.appId = args.getString("appId");
        this.assetId = args.getString("assetId");

        // 指示是否需要向PLC发送烤饼的命令
        this.needCallBakingCmd = args.getBoolean("needCallBakingCmd");

        // 获取订单相关的数据
        this.positions = ShoppingCart.getInstance().getPositions();

        this.positionsIndex = new ArrayList<>();

        // 获取烤饼的具体位置
        for (Position position : this.positions) {
            this.positionsIndex.add(position.getIndex());
            // 只要一开始烤，就把饼置位为无效
            position.disable();
            LogUtil.LogInfo("确认饼的位置: " + Integer.toString(position.getIndex()) + "会被制作");
        }

        // 设置屏幕上显示的制作进度
//        this.echo(getString(R.string.text_default_making_progress), false);
    }

    /**
     * 用于回显信息到UI
     * @param line 文字内容
     * @param isError 是否为错误信息
     */
    @Override
    public void echo(String line, boolean isError){
        this.mtvWhichProductIsMaking.setText(line);
        if(isError){
            this.mtvWhichProductIsMaking.setTextColor(ContextCompat.getColor(getContext(),R.color.colorRed));
        }
    }

    /**
     * 跳转到指定的Delegate, 但是要做好必要的数据清理工作
     * @param delegate
     */
    @Override
    public void redirectToDelegate(SmartbroDelegate delegate) {
        // 显示再见文本
        this.echo(getString(R.string.text_all_good), false);

        for (Position position : this.positions) {
            position.disable();
        }
        // 订单完成了, 清空购物车
        ShoppingCart.getInstance().allDone();
        super.redirectToDelegate(delegate);
    }

    @Override
    public void onResume() {
        super.onResume();
        // 初始化handler

        this.makePizza(0);

        /* 开始监听 */
        this.baseTimerTask = new BaseTimerTask(this);
        this.mTimer = new Timer(true);
        this.mTimer.scheduleAtFixedRate(this.baseTimerTask, 2000, 1000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 跳转到其他的delegate前, 停止设备状态的查询
        this.pishaMachineManager.stopStatusChecking();
    }

    private void makePizza(int taskIndex){
        // 能够执行到这里，表示肯定不是刚刚开机了
        IS_JUST_POWER_ON = false;

        try {
            if(positionsIndex.size() > 0){
                /* 必须有指定的位置 */
                pizzaMakerHandler = PizzaMakerHandler
                        .getInstance(this, String.valueOf(AccountManager.getMachineId()));
                // 设置handler的起始状态与位置
                LogUtil.LogInfo("Process Delegate 烤饼开始 :" + Integer.toString(taskIndex+1));

                pizzaMakerHandler.init(this.orderId, positionsIndex.size());
                final int position = positionsIndex.get(taskIndex);

                this.pizzaMakerHandler.setCurrentPositionIndex(position, taskIndex);

                if(this.pishaMachineManager == null){
                    this.pishaMachineManager  = PishaMachineManager.getInstance();
                }
                this.pishaMachineManager.init(
                        ContainerConfig.PATH,
                        ContainerConfig.BAUD_RATE,
                        this.pizzaMakerHandler
                );
                final boolean isLastOne = (taskIndex == positionsIndex.size()-1);
                if(this.needCallBakingCmd){
                    pishaMachineManager.baking(position,1, isLastOne);
                }else{
                    // 不需要发送烤饼的命令, 那么传送一个 -1
                    pishaMachineManager.baking(position,-1, isLastOne);
                }

                // 烤饼开始了，在重置位设置成false
                this.setPizzaMachineResetDone(false);
            }
        }catch (Exception e){
            UrlTool.reportMachineStatus(
                    String.valueOf(AccountManager.getMachineId()),
                    MachineStatusOfMakingPizza.MACHINE_ERROR_RS232_2,
                    e.getMessage(),
                    orderId
            );

            LogUtil.LogStackTrace(e, "250250250");
        }
    }

    /**
     * 设置设备是否重置成功的方法
     * @param status
     */
    public void setPizzaMachineResetDone(boolean status){
        this.pizzaMachineResetDone = status;
    }

    /**
     * 每一秒钟检查一次handler处理后的结果，如果不是预期的结果，就继续调用读取的方法看PLC的状态
     */
    @Override
    public void onTimer() {
        getProxyActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 检查是否收到了消息
                final int status = pizzaMakerHandler.getProcessStatus();
                LogUtil.LogInfo("消息循环 :" + Integer.toString(status));
                switch (status){
                    case MachineStatusOfMakingPizza.INFORM_TO_TAKE_PIZZA_READY:
                        // 只要已得到可以取饼的消息，显示第几张饼已经烤好
                        _showTakePizzaAnimation(); // 显示可以取饼的动画
                        if(!isPlayingAudio && !pizzaDoneAudioHasBeenPlayed){
                            pizzaDoneAudioHasBeenPlayed = true;
                            // 如果没有播放提示语音
                            playAudio(R.raw.pizzadone);
                        }
                        break;
                    case MachineStatusOfMakingPizza.ERROR_COMMUNICATION:
                        // PLC 链接中断了
                        if(mTimer != null){
                            mTimer.cancel();
                            mTimer = null;
                        }
                        if(baseTimerTask != null){
                            baseTimerTask.cancel();
                            baseTimerTask = null;
                        }
                        startWithPop(new ErrorHappendDuringMakingDelegate());
                        break;
                    case MachineStatusOfMakingPizza.INFORM_ERROR_HAPPENED_IN_PROGRESS:
                        // Todo 检查到设备故障，但是现在并不需要处理
                        startWithPop(new ErrorHappendDuringMakingDelegate());
                        break;
                    case MachineStatusOfMakingPizza.SUCCESS_READY_FOR_NEXT:
                        if(pizzaMakerHandler.isLastOneDone()){
                            _showWaitingForPlateAnimation();    // 显示等待装盘的动画
                            if(mTimer != null){
                                mTimer.cancel();
                                mTimer = null;
                            }
                            if(baseTimerTask != null){
                                baseTimerTask.cancel();
                                baseTimerTask = null;
                            }
                            // 这个时候，还没有把盒子推出来，因此需要检查
                            waitThenRedirect();
                        }
                        break;
                    case MachineStatusOfMakingPizza.INFORM_ERROR_NO_BOX_AT_END:
                        /*
                         * 发现没有盒子
                         */
                        waitThenRedirect();
                        break;
                    case MachineStatusOfMakingPizza.PLC_RESET_OK_FINAL:
                        // 检测到这个状态，表示具备了烤下一张饼的条件
                        final int newTaskIndex = pizzaMakerHandler.getCurrentTaskIndex() + 1; // 取当前的taskIndex的下一个
                        if(newTaskIndex > -1 && newTaskIndex < positionsIndex.size()){
                            makePizza(newTaskIndex);
                        }
                        break;
                    default:
                        // 没有收到期待的消息, 那么继续的获取数据
                        break;
                }
            }
        });
    }

    /**
     * 显示等待装盘的动画
     */
    private void _showWaitingForPlateAnimation(){
        this.makingPizzaAnimationImage.setBackgroundResource(R.mipmap.putting_pizza_in_plate);
    }

    /**
     * 显示可以取饼的动画
     */
    private void _showTakePizzaAnimation(){
        this.makingPizzaAnimationImage.setBackgroundResource(R.mipmap.please_take_pizza);

        // 可以取饼了，则通知服务器
        RestfulClient.builder()
                .url("hippo/sync-delivery-info")
                .params("appId",this.appId)
                .params("assetId",this.assetId)
                .params("orderNo",this.orderNo)
                .success(new ISuccess() {
                    @Override
                    public void onSuccess(String response) {
                        final JSONObject resJson =
                                JSON.parseObject(response);
                        final int errorNo = resJson.getIntValue("error_no");
                        if(errorNo == RestfulClient.NO_ERROR){
                            LogUtil.LogInfoForce("订单: " + orderNo + " 完成");
                        }else {
                            LogUtil.LogInfoForce("订单: " + orderNo + "失败, " + resJson.getString("msg"));
                        }
                    }
                })
                .failure(new IFailure() {
                    @Override
                    public void onFailure() {
                        LogUtil.LogInfoForce("订单: " + orderNo + " 同步发货状态接口失败");
                    }
                })
                .build().post();
    }

    /**
     * 总是带着错误代码返回产品列表页面
     */
    private void waitThenRedirect(){
        final int errorCode = this.pizzaMakerHandler.getCurrentMachineErrorCode();

        // 关闭串口
        this.pishaMachineManager.closeSerialPort();

        // 关闭音频播放器
        if (this.mediaPlayer != null){
            this.mediaPlayer.release();
        }

        Bundle args = new Bundle();
        args.putInt("errorCode",errorCode);   // 订单ID

        ListDelegate delegate = new ListDelegate();
        delegate.setArguments(args);
        redirectToDelegate(delegate);
    }

    /**
     * 播放音频文件的方法
     * @param what 要播放的音频文件的ID
     */
    private void playAudio(int what){
        if(this.mediaPlayer == null){
            this.mediaPlayer = MediaPlayer.create(getActivity(),what);
        }

        // 如果没有播放声音文件, 并且收到烤饼已经完成的消息
        if(!this.isPlayingAudio){
            this.isPlayingAudio = true;
            this.mediaPlayer = MediaPlayer.create(getActivity(),what); // 播放叮当的声音
            this.mediaPlayer.start();

            if(what==R.raw.pizzadone ){
                this.nextAudioRaw = R.raw.pizzadone;
            }
        }

        this.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // 监听播放音频, 如果播放结束
                if(nextAudioRaw != -1){
                    mediaPlayer = MediaPlayer.create(getActivity(),nextAudioRaw);
                    mediaPlayer.start();
                    // 表示没有继续要播放的音频了
                    nextAudioRaw = -1;
                }else {
                    isPlayingAudio = false;
                }
            }
        });
    }
}
