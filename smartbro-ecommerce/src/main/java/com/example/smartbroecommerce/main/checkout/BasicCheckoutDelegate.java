package com.example.smartbroecommerce.main.checkout;

import com.example.smartbro.delegates.SmartbroDelegate;
import com.example.smartbro.utils.timer.BaseTimerTask;
import com.example.smartbro.utils.timer.ITimerListener;
import com.example.smartbroecommerce.database.Order;
import com.example.smartbroecommerce.database.PaymentMethod;

import java.util.Timer;

/**
 * Created by Justin Wang from SmartBro on 17/12/17.
 */

public abstract class BasicCheckoutDelegate extends SmartbroDelegate implements ITimerListener{
    // 本次购买的订单
    protected Order order = null;
    protected PaymentMethod paymentMethod = null;
    protected Timer checkOrderStatusTimer = null;

    public boolean stopCheckingOrderStatusThroughNetwork = false;

    // 每个子类必须完成创建订单的方法
    public abstract Order createOrder();

    // 每个子类必须实现取消本次支付的方法
    public abstract void cancelCheckout();

    /**
     * 检查服务器上订单当前的支付状态的方法
     */
    protected void checkOrderStatus(){
        if(!this.stopCheckingOrderStatusThroughNetwork){
            // 需要通过网络来检查服务器上的订单状态
            if(this.checkOrderStatusTimer != null){
                this.checkOrderStatusTimer = new Timer(true);
            }
            BaseTimerTask checkOrderStatusTask = new BaseTimerTask(this);
            // 每隔离2秒钟检查一下订单的状态
            this.checkOrderStatusTimer.schedule(checkOrderStatusTask, 1000, 1000);
        }
    }
}
