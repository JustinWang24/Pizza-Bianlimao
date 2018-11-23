package com.example.smartbroecommerce.main.checkout.device;

import android.os.Message;

/**
 * Created by Justin Wang from SmartBro on 4/2/18.
 */

public interface IReaderAction {

    public void onResetSuccess(Message message);
    public void onResetFail(Message message);

    public void onConfig(Message message);
    public void onConfigFailed(Message message);

    public void onKeepAliveOk(Message message);
    public void onKeepAliveFailed(Message message);

    public void onEnableOk(Message message);
    public void onEnableFailed(Message message);

    public void onBeginSession(Message message);

    public void onVendRequest(Message message);

    public void onVendApprove(Message message);

    public void onVendSuccess(Message message);

    public void onSessionComplete(Message message);

    public void onEndSession(Message message);

    public void onDisableCard(Message message);

    public void onIdleState(Message message);

    public void onKeepAliveOnly();
    public void onKeepSilence();

    public void setProductInfo(int productId, int productPrice, int productQuantity);
}
