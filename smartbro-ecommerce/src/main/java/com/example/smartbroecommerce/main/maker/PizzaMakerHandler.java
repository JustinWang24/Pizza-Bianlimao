package com.example.smartbroecommerce.main.maker;

import com.example.smartbro.net.RestfulClient;
import com.example.smartbro.net.callback.IError;
import com.example.smartbro.net.callback.IFailure;
import com.example.smartbro.net.callback.ISuccess;
import com.example.smartbroecommerce.utils.UrlTool;
import com.taihua.pishamachine.LogUtil;
import com.taihua.pishamachine.MachineStatusOfMakingPizza;

import android.os.Handler;
import android.os.Message;

/**
 * Created by Justin Wang from SmartBro on 19/12/17.
 * 披萨制作处理器类
 */

public class PizzaMakerHandler extends Handler {
    /**
     * 单例工厂模式
     */
    private static PizzaMakerHandler INSTANCE = null;

    private int currentPositionIndex = -1;

    /**
     * 当前正在处理的订单 ID
     */
    private int currentOrderId = -1;
    /**
     * 本次会处理几张披萨的制作
     */
    private static int PIZZA_COUNT      = 1;

    private static boolean GENERAL_ERRORS_NOT_REPORTED = true;

    /**
     * 关联的视图 Delegate 对象
     */
    private ProcessingDelegate delegate = null;

    // 设备的UUID
    private String machineUuid = null;

    // 指示当前执行的任务的序号
    private int currentTaskIndex = 0;

    // 是否烤完最后一张饼
    private boolean isLastPizzaDone = false;
    private int processStatus = -1;     // 随便给一个无效的状态值整数 -1

    // 当前收到的故障码
    private int currentMachineErrorCode = 0; // 0表示没有错误

    private PizzaMakerHandler(){
    }

    /**
     * 静态工程方法
     * @param delegate
     * @return
     */
    public static PizzaMakerHandler getInstance(ProcessingDelegate delegate, String machineUuid){
        if(INSTANCE == null){
            INSTANCE = new PizzaMakerHandler();
            INSTANCE.delegate = delegate;
            INSTANCE.machineUuid = machineUuid;
        }
        return INSTANCE;
    }

    /**
     * 设置最新的取饼的位置信息和第几张饼的序号
     * @param index     取饼的位置信息
     * @param taskIndex 第几张饼的序号
     */
    public void setCurrentPositionIndex(int index, int taskIndex){
        this.isLastPizzaDone = false;
        this.processStatus = -1;
        this.currentPositionIndex = index;
        this.currentTaskIndex = taskIndex;
    }

    /**
     * 初始化静态属性: 本次处理的订单ID， 本次处理的饼的个数
     * @param orderId    表示本次制作的orderID
     * @param pizzaCount 表示本次会制作几张饼
     */
    public PizzaMakerHandler init(int orderId, int pizzaCount){
        this.currentOrderId = orderId;
        PIZZA_COUNT      = pizzaCount;
        return INSTANCE;
    }

    /**
     * 是否为最后一个了
     * @return boolean
     */
    boolean isLastOneDone(){
//        LogUtil.LogInfo("判断是否最后一张饼 index:" + Integer.toString(this.currentTaskIndex) + " 任务数: " + Integer.toString(PIZZA_COUNT));
        this.isLastPizzaDone = this.currentTaskIndex == PIZZA_COUNT -1;
        return this.isLastPizzaDone;
    }

    int getCurrentTaskIndex(){
        if(this.isLastOneDone()){
            return -1;
        }else {
            return this.currentTaskIndex;
        }
    }

    /**
     * 获取烤饼过程的状态值
     * @return
     */
    int getProcessStatus(){
        return this.processStatus;
    }

    /**
     * 设置烤饼过程的状态值
     * @param status
     */
    private void setProcessStatus(int status){
        if(this.processStatus != MachineStatusOfMakingPizza.SUCCESS_READY_FOR_NEXT){
            this.processStatus = status;
        }
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if(msg != null){
            switch (msg.what){
                case MachineStatusOfMakingPizza.INFORM_TO_TAKE_PIZZA_READY:
                    // 烤至过程中， 发现饼烤完了，可以让用户取饼了
                    this.setProcessStatus(MachineStatusOfMakingPizza.INFORM_TO_TAKE_PIZZA_READY);
                    break;
                case MachineStatusOfMakingPizza.SUCCESS_READY_FOR_NEXT:
                    // 饼已经取走，盒子也推到位了. 从这之后，handler处于就位烤下一张的状态
                    // 通知服务器, 某个位置的饼已经出炉, ProcessingDelegate会不停的检查这个状态
                    this.setProcessStatus(MachineStatusOfMakingPizza.SUCCESS_READY_FOR_NEXT);
                    // 检查是否已经是最后的一张饼了
                    if(this.isLastOneDone()){
                        // 表示已经烤完了, 执行订单完成需要的工作
                        this.orderComplete();
                    }else {
                        // 还不是最后一张饼, 把工作指示的序号加一
                        LogUtil.LogInfo("转移到烤第" + Integer.toString(this.currentTaskIndex+1) + "张饼");
                        delegate.setPizzaMachineResetDone(true);
                    }
                    break;
                case MachineStatusOfMakingPizza.WAITING_FOR_PLC_RESET:
                    // 等待PLC执行推新盒子的命令
                    LogUtil.LogInfo("等待新的盒子推到位; 通知服务器某个饼的存储位置已经是空的了");
                    // 通知服务器更新一个饼的位置为false
                    this.confirmPositionIsEmptyToServer();
                    break;
                case MachineStatusOfMakingPizza.PLC_RESET_OK_FINAL:
                    // 只有在烤多张饼的时候，才会收到这个消息, 表示当前的饼已经完全烤完，可以烤新的一张了
                    this.setProcessStatus(MachineStatusOfMakingPizza.PLC_RESET_OK_FINAL);
                    LogUtil.LogInfo("存储位置"+Integer.toString(msg.arg1)+"已经是空的了, 准备烤下一张饼了");
                    break;
                case MachineStatusOfMakingPizza.ERROR_COMMUNICATION:
                    // 与PLC的通讯故障
                    if(MachineStatusOfMakingPizza.ERROR_COMMUNICATION != this.getCurrentMachineErrorCode()){
                        this.setCurrentMachineErrorCode(MachineStatusOfMakingPizza.ERROR_COMMUNICATION);
                        this.generalErrorHandler(msg);
                        LogUtil.LogInfo("收到一个 MachineStatusOfMakingPizza.ERROR_COMMUNICATION 消息");
                    }
                    this.setProcessStatus(MachineStatusOfMakingPizza.ERROR_COMMUNICATION);
                    break;
                case MachineStatusOfMakingPizza.INFORM_ERROR_HAPPENED_IN_PROGRESS:
                    // 烤饼过程中发生了某个故障
                    if(msg.arg1 != this.getCurrentMachineErrorCode()){
                        this.setCurrentMachineErrorCode(msg.arg1);
                        this.generalErrorHandler(msg);
                        LogUtil.LogInfo("收到一个 MachineStatusOfMakingPizza.INFORM_ERROR_HAPPENED_IN_PROGRESS 消息: " + Integer.toString(msg.arg1));
                    }
                    this.setProcessStatus(MachineStatusOfMakingPizza.INFORM_ERROR_HAPPENED_IN_PROGRESS);
                    break;

                case MachineStatusOfMakingPizza.INFORM_ERROR_NO_BOX_AT_END:
                    // 烤饼过程完成之后没有推出盒子
                    if(msg.arg1 != this.getCurrentMachineErrorCode()){
                        this.setCurrentMachineErrorCode(msg.arg1);
                        this.generalErrorHandler(msg);
                        LogUtil.LogInfo("收到一个 MachineStatusOfMakingPizza.INFORM_ERROR_NO_BOX_AT_END 消息: " + Integer.toString(msg.arg1));
                    }
                    this.setProcessStatus(MachineStatusOfMakingPizza.INFORM_ERROR_NO_BOX_AT_END);
                    break;
                default:
                    LogUtil.LogInfo("收到一个不知道是啥的消息" + Integer.toString(msg.what));
                    break;
            }
        }else {
            LogUtil.LogInfo("收到一个不知道是啥的消息 NULL");
        }
    }

    /**
     * 向服务器发送确认某个位置已经卖出的消息
     */
    private void confirmPositionIsEmptyToServer(){
//        RestfulClient.builder()
//                .url("machines/update_stock")
//                .params("muuid", this.machineUuid)
//                .params("positions", Integer.toString(this.currentPositionIndex) + ":false")
//                .success(new ISuccess() {
//                    @Override
//                    public void onSuccess(String response) {
//                        LogUtil.LogInfo("向服务器发送确认已经卖出的消息, 位置: " + Integer.toString(currentPositionIndex));
//                    }
//                })
//                .error(new IError() {
//                    @Override
//                    public void onError(int code, String msg) {
//                        LogUtil.LogInfo("向服务器发送确认已经卖出的消息发生错误, 位置: " + Integer.toString(currentPositionIndex) + ", " + msg);
//                    }
//                })
//                .failure(new IFailure() {
//                    @Override
//                    public void onFailure() {
//                        LogUtil.LogInfo("向服务器发送确认已经卖出的消息失败, 位置: " + Integer.toString(currentPositionIndex));
//                    }
//                })
//                .build()
//                .post();
    }

    /**
     * 通知Delegate
     */
    private void informDelegate(){
        // Todo 烤饼期间出现了故障, 通知Delegate做下一步的处理. 同时做好这个处理器的善后工作，包括清理定时器, 无用的进程等
    }

    /**
     * 订单处理完成的处理
     */
    private void orderComplete(){
        this.isLastPizzaDone = true;
        UrlTool.reportOrderComplete(this.currentOrderId);
    }

    /**
     * 烤饼过程中的一般故障处理
     *
     * @param msg
     */
    private void generalErrorHandler(Message msg){
        if(GENERAL_ERRORS_NOT_REPORTED){
            // 如果错误信息还没有上报服务器
            GENERAL_ERRORS_NOT_REPORTED = true;

            // 故障信息都收集完了
            // 上报故障给服务器
            UrlTool.reportMachineStatus(
                    this.machineUuid,
                    this.getCurrentMachineErrorCode(),
                    "",
                    this.currentOrderId
            );
        }
        this.informDelegate();
    }

    public int getCurrentMachineErrorCode() {
        return currentMachineErrorCode;
    }

    public void setCurrentMachineErrorCode(int currentMachineErrorCode) {
        this.currentMachineErrorCode = currentMachineErrorCode;
    }
}
