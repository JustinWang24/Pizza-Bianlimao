package com.example.smartbroecommerce.database;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.example.smartbro.app.AccountManager;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

/**
 * Created by Justin Wang from SmartBro on 17/12/17.
 */

public class Order {

    private int id = -1;
    private int status = -1;
    private PaymentMethod paymentMethod = null;
    private ShoppingCart shoppingCart = null;
    private List<Position> positions =null;
    private List<Position> completedPositions = null;

    // 找零金额
    private double change = 0;
    // 总支付金额
    private double totalPaid = 0;
    // 订单总价
    @JSONField(name = "total")
    private double totolAmount = 0;
    @JSONField(name = "pgid")
    private long currentPaymentMethodId=0;
    @JSONField(name = "transactions")
    private String shoppingCartItemsForJson = null;

    // 当前正在制作的饼的位置的索引, 这个值永远是0
    private static final int CURRENT_POSITION_INDEX = 0;

    /**
     * 构造订单对象
     * @param id
     * @param paymentMethod
     */
    public Order(int id, PaymentMethod paymentMethod) {
        this.id = id;
        this.paymentMethod = paymentMethod;
        this.status = OrderStatus.PENDING;
        this.shoppingCart = ShoppingCart.getInstance();
        this.positions = new ArrayList<>();
        this.completedPositions = new ArrayList<>();
        this.totolAmount = this.shoppingCart.getTotal();

        /**
         * 把需要的取饼的位置保存起来
         */
        for(ShoppingCartItem item : this.shoppingCart.getShoppingCartItems()){
            // 加上下面的一句，是为了提交的时候，数据的结构是正确的
//            item.subtotal = item.getQuantity() * this.paymentMethod.getPrice();
            item.setSubtotal(item.getQuantity() * item.getProductPrice());
            
            final List<Position> itemPositions = item.getPositions();
            for (Position position : itemPositions){
                this.positions.add(position);
            }
        }
    }

    /**
     * 为了提交准备数据的方法
     */
    public void prepareShoppingCartData(){
        List<ShoppingCartItem> items = this.getShoppingCart().getShoppingCartItems();
        final int size = items.size();
        for (int i = 0; i < size; i++) {
            items.get(i).getPositionsIndex();
        }
    }

    /**
     * 获取订单提交的url
     * @return String
     */
    public String getOrderUrl(){
        String url = null;
        final int paymentMethodType = this.paymentMethod.getType();
        switch (paymentMethodType){
            case PaymentMethod.WECHAT:
                url = "make_order_wechat";
                break;
            case PaymentMethod.ALIPAY:
                url = "make_order_alipay";
                break;
            case PaymentMethod.APPLE_PAY:
                url = "make_order_apple";
                break;
            case PaymentMethod.CASH:
                url = "make_order_cash";
                break;
            case PaymentMethod.CREDIT_CARD:
                url = "make_order_cash";
                break;
            default:
                break;
        }
        return url;
    }

    /**
     * 获取下一张的位置
     * @return Position
     */
    public Position getNextPosition(){
        // 先把当前的拿出来放到已完成的里面
        this.completedPositions.add(this.positions.get(CURRENT_POSITION_INDEX));
        this.positions.remove(CURRENT_POSITION_INDEX);

        if(this.positions.size()>0){
            return this.positions.get(CURRENT_POSITION_INDEX);
        }else {
            return null;
        }
    }


    /**
     * 检查订单是否已经支付的方法
     * @return boolean
     */
    public boolean isPaid(){
        return this.status == OrderStatus.PAID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public ShoppingCart getShoppingCart() {
        return shoppingCart;
    }

    public void setShoppingCart(ShoppingCart shoppingCart) {
        this.shoppingCart = shoppingCart;
    }

    public double getTotolAmount(){
        return this.totolAmount;
    }
}
