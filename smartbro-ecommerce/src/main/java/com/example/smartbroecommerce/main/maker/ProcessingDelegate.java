package com.example.smartbroecommerce.main.maker;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.View;

import com.example.smartbro.app.AccountManager;
import com.example.smartbro.delegates.SmartbroDelegate;
import com.example.smartbro.utils.timer.BaseTimerTask;
import com.example.smartbro.utils.timer.ITimerListener;
import com.example.smartbroecommerce.R;
import com.example.smartbroecommerce.R2;
import com.example.smartbroecommerce.container.ContainerConfig;
import com.example.smartbroecommerce.database.MachineProfile;
import com.example.smartbroecommerce.database.Position;
import com.example.smartbroecommerce.database.ShoppingCart;
import com.example.smartbroecommerce.main.pages.StopWorkingDelegate;
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

//    private int orderId = -1;
//    private List<Position> positions = new ArrayList<>();

    // 表示是否刚刚开机
    public static boolean IS_JUST_POWER_ON = false;

    // 和制作Pizza相关的类
    private int orderId = -1;
    private List<Position> positions = null;
    private ArrayList<Integer> positionsIndex = null;
    private Timer mTimer = null;
    private PizzaMakerHandler pizzaMakerHandler = null;
    private PishaMachineManager pishaMachineManager = null;
    private int counter = 0;
    private int finishedPizzasNumber = 0; // 已经制作完成的披萨饼的数量
    private boolean pizzaMachineResetDone = false;
    private boolean needCallBakingCmd = true;

    // 播放声音的相关属性
    private boolean isPlayingAudio = false;
    private MediaPlayer mediaPlayer = null;
    private int nextAudioRaw = -1;

    @BindView(R2.id.tv_which_product_is_making)
    AppCompatTextView mtvWhichProductIsMaking = null;
    @BindView(R2.id.tv_change_number_text)
    AppCompatTextView mtvChangeNumberText = null;
    @BindView(R2.id.tv_service_phone_number)
    AppCompatTextView mtvServicePhone = null;
    @BindView(R2.id.making_progress_layout_wrap)
    LinearLayoutCompat wrap = null;

    @Override
    public Object setLayout() {
        return R.layout.delegate_maker_progress;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {

        if("en".equals(MachineProfile.getInstance().getLanguage())){
            this.wrap.setBackground(getResources().getDrawable(R.mipmap.au_pizza_ninja_bg));
        }

        Bundle args = getArguments();
        this.orderId = args.getInt("orderId");

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
            LogUtil.LogInfoForce("确认饼的位置: " + Integer.toString(position.getIndex()) + "会被制作");
        }

        // 设置屏幕上显示的找零金额

        if (args.getDouble("changes") > 0){
            this.mtvChangeNumberText.append(Integer.toString(args.getInt("changes")));
        }
        else{
            this.mtvChangeNumberText.setText("");
        }

        // 显示联系电话
        this.mtvServicePhone.append(MachineProfile.getInstance().getMachinePhone());
        // 设置屏幕上显示的制作进度
        this.echo(getString(R.string.text_default_making_progress), false);
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
        final BaseTimerTask task = new BaseTimerTask(this);
        this.mTimer = new Timer(true);
        this.mTimer.schedule(task, 2000, 1000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 跳转到其他的delegate前, 停止设备状态的查询
        this.pishaMachineManager.stopStatusChecking();
    }

    private void makePizza(int taskIndex){
        LogUtil.LogInfo("Process Delegate 烤饼开始 :" + Integer.toString(taskIndex+1));

        // 能够执行到这里，表示肯定不是刚刚开机了
        IS_JUST_POWER_ON = false;

        try {
            if(positionsIndex.size() > 0){
                /* 必须有指定的位置 */
                pizzaMakerHandler = PizzaMakerHandler
                        .getInstance(this, String.valueOf(AccountManager.getMachineId()));
                // 设置handler的起始状态与位置
                pizzaMakerHandler.init(orderId, positionsIndex.size());

                final int position = positionsIndex.get(taskIndex);
                this.pizzaMakerHandler.setCurrentPositionIndex(position, taskIndex);

                if(this.pishaMachineManager == null){
                    this.pishaMachineManager  = PishaMachineManager.getInstance();
                }
                this.pishaMachineManager.init(
                        ContainerConfig.PATH,
                        ContainerConfig.BAUD_RATE,
                        pizzaMakerHandler
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

                if("en".equals(MachineProfile.getInstance().getLanguage())){
                    this.playAudio(R.raw.make_first_one_en);
                }else {
                    this.playAudio(R.raw.make_first_one);
                }
            }
        }catch (Exception e){
            UrlTool.reportMachineStatus(
                    String.valueOf(AccountManager.getMachineId()),
                    MachineStatusOfMakingPizza.MACHINE_ERROR_RS232_2,
                    e.getMessage(),
                    orderId
            );

            if("en".equals(MachineProfile.getInstance().getLanguage())){
                this.playAudio(R.raw.on_error_en);
            }else {
                this.playAudio(R.raw.on_error);
            }
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
                switch (status){
                    case MachineStatusOfMakingPizza.INFORM_TO_TAKE_PIZZA_READY:
                        // 只要已得到可以取饼的消息，显示第几张饼已经烤好
                        final int currentTaskIndex = pizzaMakerHandler.getCurrentTaskIndex() +1;
                        if(!isPlayingAudio){
                            // 如果没有播放提示语音
                            playAudio(R.raw.dingding);
                            if("cn".equals(MachineProfile.getInstance().getLanguage())){
                                echo(getString(R.string.text_please_take_pizza1)
                                        + Integer.toString(currentTaskIndex+1) + " "
                                        + getString(R.string.text_please_take_pizza2), false);
                            }else {
                                echo(getString(R.string.text_please_take_pizza1) + "\n" + getString(R.string.text_please_take_pizza2), false);
                            }
                        }
                        break;
                    case MachineStatusOfMakingPizza.ERROR_COMMUNICATION:
                        // PLC 链接中断了
                        if("en".equals(MachineProfile.getInstance().getLanguage())){
                            playAudio(R.raw.on_error_en);
                        }else {
                            playAudio(R.raw.on_error);
                        }
                        mTimer.cancel();
                        mTimer = null;
                        startWithPop(new StopWorkingDelegate());
                        break;
                    case MachineStatusOfMakingPizza.INFORM_ERROR_HAPPENED_IN_PROGRESS:
                        // Todo 检查到设备故障，但是现在并不需要处理
                        startWithPop(new StopWorkingDelegate());
                        break;
                    case MachineStatusOfMakingPizza.SUCCESS_READY_FOR_NEXT:
                        if(pizzaMakerHandler.isLastOneDone()){
                            echo(getString(R.string.text_all_good), false); // 显示并已经烤完了
                            mTimer.cancel();
                            mTimer = null;
//                            PishaMachineManager.IS_FORCED_TO_STOP_ALL = true;
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

            if(what==R.raw.dingding){
                if("cn".equals(MachineProfile.getInstance().getLanguage()))
                    this.nextAudioRaw = R.raw.pizzadone;
                else
                    this.nextAudioRaw = R.raw.pizzadone_en;
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