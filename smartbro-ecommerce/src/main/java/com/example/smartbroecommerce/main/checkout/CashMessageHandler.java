package com.example.smartbroecommerce.main.checkout;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.taihua.pishamachine.CashierManager;
import com.taihua.pishamachine.CashierMessage;
import com.taihua.pishamachine.LogUtil;

/**
 * Created by Justin Wang from SmartBro on 1/1/18.
 * 现金支付期间的消息处理
 */

public class CashMessageHandler extends Handler {

    private static CashMessageHandler INSTANCE = null;
    private int latestCashReceivedValue = 0;
    private int orderTotal = 0;
    private boolean isChangesActionDone = false;
    private boolean isPaymentProcessCancelled = false;  // 现金支付流程被取消了
    private CashierManager cashierManager = null;
    private final int maxWaitingCount = 2;
    private int waitingCount = 2;

    // 设置找零操作已经开始的标志
    private boolean changesActionCalled = false;

    private static int FORCE_RESET_CODE = -2;

    private CashMessageHandler(){

    }

    /**
     * 简单单例方法
     * @return 返回对象
     */
    public static CashMessageHandler getInstance(){
        if(INSTANCE == null){
            INSTANCE = new CashMessageHandler();
        }
        return INSTANCE;
    }

    private void _forceReset(){
        this.isChangesActionDone        = false;
        this.isPaymentProcessCancelled  = false;
        this.changesActionCalled        = false;
        this.waitingCount               = this.maxWaitingCount;
    }

    /**
     * 初始化投币器: 可以给定一个非零的金额, 比如优惠 1元
     * @param startValue    投币器金额起始价
     * @param total         订单总价
     */
    public void init(int startValue, int total){
        this.latestCashReceivedValue = startValue;  // 初始的金额
        this.orderTotal = total;                    // 订单的总额

        this._forceReset();

        if(this.cashierManager == null){
//            Log.i("Init","初始化cashierManager");
        }
        // 初始化投币器管理模块
        this.cashierManager = CashierManager.getInstance();
        this.cashierManager.init(this, this.orderTotal);

        // 开始扫描投币器的工作
        this.cashierManager.startTimerTask();
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what){
            case CashierMessage.NOTES_LATEST_VALUE:
                // 读取到了最新的收款金额, 去更新一下
                this.setLatestCashReceivedValue(msg.arg1);
                LogUtil.LogInfo("捕捉到金额被读取的消息: " + Integer.toString(msg.arg1));
                break;
            case CashierMessage.CASH_PROCESS_END:
                LogUtil.LogInfo("捕捉到可以烤饼的通知了 ************************************");
                this.isChangesActionDone = true;
                break;
            case CashierMessage.EQUIPMENT_DISABLED:
                // 投币器禁能
                this.setLatestCashReceivedValue(FORCE_RESET_CODE);
                LogUtil.LogInfo("捕捉到 投币器禁能 的消息: " + Integer.toString(msg.arg1));
                break;
            case CashierMessage.EQUIPMENT_ENABLED:
                // 投币器使能
                break;

            default:
                break;
        }
    }

    /**
     * 可以开始烤饼了, delegate调用它来决定是否烤
     * @return boolean
     */
    boolean readyToMakePizza(){
        return this.isChangesActionDone;
    }

    /**
     * 判断是否支付过程被取消了
     * @return boolean
     */
    boolean paymentProcessCancelled(){
        return this.isPaymentProcessCancelled;
    }

    /**
     * 在读取到的金额大于0的情况下, 这个方法，只有在金额变化的情况下才会被调用
     * @param value 读取到的金额
     */
    private void setLatestCashReceivedValue(int value){
        if(value == FORCE_RESET_CODE){
            // 强制 reset
            this._forceReset();
        }

        if(value > 0){
            LogUtil.LogInfo("投入现金: " + Integer.toString(this.latestCashReceivedValue) + ", 总价:" + this.orderTotal);

            if(this.latestCashReceivedValue == value){
                // 表示最新读入的投币总金额没有发生变化
                if(this.isPaidEnough()){
                    // 如果已经投入了足够的现金
                    if(this.waitingCount > 0){
                        LogUtil.LogInfo("投入了足够的现金, 再等一会儿");
                        this.waitingCount--;
                    }else {
                        // 已经在投入了足够金额的前提下等待了2.4秒, 投入的钱还没有变化, 表示支付完成，开始找钱
                        LogUtil.LogInfo("投入了足够的现金, 可以找钱了");
                        if(!this.isChangesActionDone){
                            this.giveCustomerChanges();
                            // 找钱完毕, 通知delegate
                            this.isChangesActionDone = true;
                        }
                    }
                }
            }else {
                // 表示投入的钱的金额发生了变化
                this.latestCashReceivedValue = value;
//                this.waitingCount = this.maxWaitingCount;
            }
        }
    }

    private boolean isPaidEnough(){
        return this.latestCashReceivedValue >= this.orderTotal;
    }

    int getLatestCashReceivedValue(){
        return this.latestCashReceivedValue;
    }

    /**
     * 给客户找零的方法
     */
    void giveCustomerChanges(){
        if(!this.isChangesActionDone){
            boolean done = this.cashierManager.giveCustomerChanges(this.latestCashReceivedValue - this.orderTotal);
            if (done){
                this.setLatestCashReceivedValue(FORCE_RESET_CODE);
            }
        }
    }

    /**
     * 取消支付的时候调用
     * 仅仅用于找零
     * @param isCancelButtonClicked 表示是否为用户手动取消现金支付
     */
    void giveCustomerChangesOnly(boolean isCancelButtonClicked){
        if(!this.isChangesActionDone){
            boolean done = this.cashierManager
                    .giveCustomerChangeAndOnly(this.latestCashReceivedValue - this.orderTotal);
            if (done){
                //
                this.setLatestCashReceivedValue(FORCE_RESET_CODE);
                this.isPaymentProcessCancelled = isCancelButtonClicked;
            }
        }
    }
}
