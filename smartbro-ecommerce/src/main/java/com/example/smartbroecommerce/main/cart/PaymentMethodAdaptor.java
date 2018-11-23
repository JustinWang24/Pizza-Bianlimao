package com.example.smartbroecommerce.main.cart;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.View;

import com.example.smartbro.app.Smartbro;
import com.example.smartbro.delegates.SmartbroDelegate;
import com.example.smartbro.ui.recycler.ItemType;
import com.example.smartbro.ui.recycler.MultipleFields;
import com.example.smartbro.ui.recycler.MultipleItemEntity;
import com.example.smartbro.ui.recycler.MultipleRecyclerAdaptor;
import com.example.smartbro.ui.recycler.MultipleViewHolder;
import com.example.smartbroecommerce.R;
import com.example.smartbroecommerce.container.ContainerConfig;
import com.example.smartbroecommerce.database.MachineProfile;
import com.example.smartbroecommerce.main.checkout.ByCashDelegate;
import com.example.smartbroecommerce.main.checkout.ByCreditCard;
import com.example.smartbroecommerce.main.checkout.CouponFormDelegate;
import com.example.smartbroecommerce.main.checkout.ScanQrCodeDelegate;
import com.example.smartbroecommerce.main.maker.ProcessingDelegate;
import com.example.smartbroecommerce.main.pages.StopWorkingDelegate;
import com.joanzapata.iconify.widget.IconTextView;
import com.example.smartbroecommerce.database.PaymentMethod;
import com.taihua.pishamachine.DoorManager;

import java.util.List;

/**
 * Created by Justin Wang from SmartBro on 17/12/17.
 */

public class PaymentMethodAdaptor extends MultipleRecyclerAdaptor {

    private ShopCartDelegate shopCartDelegate = null;

    /**
     * 构造函数
     *
     * @param data
     */
    protected PaymentMethodAdaptor(List<MultipleItemEntity> data) {
        super(data);
        addItemType(ItemType.PAYMENT_METHOD, R.layout.item_payment_method);
    }

    @Override
    protected void convert(MultipleViewHolder holder, MultipleItemEntity entity) {
        super.convert(holder, entity);

        final long paymentMethodId = entity.getField(MultipleFields.ID);
        final String name = entity.getField(MultipleFields.NAME);
        final String priceText = entity.getField(MultipleFields.PRICE_TEXT);
        final double price = entity.getField(MultipleFields.PRICE);
        final int type = entity.getField(MultipleFields.TAG);

        // 取出控件
        LinearLayoutCompat wrap = holder.getView(R.id.llc_payment_methods_wrap);
        IconTextView iconTextView = holder.getView(R.id.icon_payment_method);
        AppCompatTextView textViewPaymentMethod = holder.getView(R.id.tv_payment_method);

        // 支付图标
        String iconText = null;
        switch (type){
            case PaymentMethod.WECHAT:
                iconText = "{icon-weixin}";
                iconTextView.setTextColor(ContextCompat.getColor(mContext,R.color.weChatGreen));
                iconTextView.setText(iconText);
                // 支付方式的文字
                textViewPaymentMethod.setText(Smartbro.getApplication().getString(R.string.text_pay_weChat));
                wrap.setTag("wechat");
                break;
            case PaymentMethod.ALIPAY:
                iconText = "{icon-zhifubao}";
                iconTextView.setTextColor(ContextCompat.getColor(mContext,R.color.aliPayBlue));
                iconTextView.setText(iconText);
                textViewPaymentMethod.setText(Smartbro.getApplication().getString(R.string.text_pay_Ali));
                wrap.setTag("alipay");
                break;
            case PaymentMethod.CREDIT_CARD:
                iconText = "{fa-credit-card}";
                iconTextView.setTextColor(ContextCompat.getColor(mContext,R.color.colorAccent));
                iconTextView.setText(iconText);
                textViewPaymentMethod.setText(Smartbro.getApplication().getString(R.string.text_pay_credit_card));
                wrap.setTag("credit");
                break;
            case PaymentMethod.APPLE_PAY:
                iconText = "{fa-apple}";
                iconTextView.setTextColor(ContextCompat.getColor(mContext,R.color.black));
                iconTextView.setText(iconText);
                textViewPaymentMethod.setText(Smartbro.getApplication().getString(R.string.text_pay_Apple));
                wrap.setTag("apple");
                break;
            default:
                iconText = "{fa-money}";
                iconTextView.setTextColor(ContextCompat.getColor(mContext,R.color.helpbg));
                iconTextView.setText(iconText);
                textViewPaymentMethod.setText(Smartbro.getApplication().getString(R.string.text_pay_cash));
                wrap.setTag("cash");
                break;
        }


        wrap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.i("支付方式")
                final String methed = v.getTag().toString();

                shopCartDelegate.getProxyActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SmartbroDelegate delegate;

//                        BasicCheckoutDelegate delegate;

                        // 先检查是否有盒子，然后再决定是否跳转
                        final DoorManager doorManager =
                                DoorManager.getInstance(ContainerConfig.PATH, ContainerConfig.BAUD_RATE);

                        // 如果第一次开机， 或者盒子就位
                        if(ProcessingDelegate.IS_JUST_POWER_ON || doorManager.isPackpingBoxInPosition()){
                            Bundle args = new Bundle();
                            // 把Payment method的ID作为参数传过去
                            args.putLong(PaymentMethodFields.PAYMENT.name(), paymentMethodId);
                            // 把选定的支付方式传过去
                            args.putString("method",methed);

                            if(MachineProfile.getInstance().getSupportCoupon()){
                                // 表示现在设备支持使用优惠券
                                delegate = new CouponFormDelegate();
                            }else {
                                // 表示现在设备不支持使用优惠券了
                                if("wechat".equals(methed) || "alipay".equals(methed)){
                                    delegate = new ScanQrCodeDelegate();
                                }else if("credit".equals(methed)){
                                    delegate = new ByCreditCard();
                                } else {
                                    delegate = new ByCashDelegate();
                                }
                                // 给一个空的优惠券
                                args.putString("coupon","");
                            }
                            delegate.setArguments(args);
                        }else{
                            // 没有发现盒子
                            delegate = new StopWorkingDelegate();
                        }

                        shopCartDelegate.startWithPop(delegate);
                    }
                });
            }
        });
    }

    public void setShopCartDelegate(ShopCartDelegate delegate){
        this.shopCartDelegate = delegate;
    }

}
