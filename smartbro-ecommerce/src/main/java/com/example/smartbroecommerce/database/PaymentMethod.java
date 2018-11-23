package com.example.smartbroecommerce.database;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

import java.util.List;

/**
 * Created by Justin Wang from SmartBro on 6/12/17.
 */

@Entity(nameInDb = "payment_profile")
public class PaymentMethod {
    @Id
    private long id = 0;                   // 支付方式在系统中的ID
    private int type = 0;                  // 支付类型
    private String name = null;            // 支付方式的名称
    private String priceText = null;       // 价格的标签
    private double price = 0;               // 价格的真实浮点值

    // 定义的几种支付类型
    public static final int WECHAT      = 3;
    public static final int ALIPAY      = 4;
    public static final int CASH        = 1;
    public static final int CREDIT_CARD = 2;
    public static final int APPLE_PAY   = 5;

    @Generated(hash = 269361818)
    public PaymentMethod(long id, int type, String name, String priceText, double price) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.priceText = priceText;
        this.price = price;
    }

    @Generated(hash = 173538308)
    public PaymentMethod() {
    }

    /**
     * 清空已有数据表的方法
     */
    public static void flush(){
        PaymentMethodDao dao = DatabaseManager.getInstance().getPaymentMethodDao();
        List<PaymentMethod> list = findAll();
        for(PaymentMethod paymentMethod : list){
            dao.delete(paymentMethod);
        }
    }

    /**
     * 获取所有的支付方式对象
     * @return List<PaymentMethod>
     */
    public static List<PaymentMethod> findAll(){
        PaymentMethodDao dao = DatabaseManager.getInstance().getPaymentMethodDao();
        return dao.queryBuilder().list();
    }

    /**
     * 获取所有的支付方式对象
     * @return List<PaymentMethod>
     */
    public static PaymentMethod findById(long id){
        PaymentMethodDao dao = DatabaseManager.getInstance().getPaymentMethodDao();
        List<PaymentMethod> list =  dao.queryBuilder().where(PaymentMethodDao.Properties.Id.eq(id)).list();
        if(list.size() > 0){
            return list.get(0);
        }
        return null;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPriceText() {
        return this.priceText;
    }

    public void setPriceText(String priceText) {
        this.priceText = priceText;
    }

    public double getPrice() {
        return this.price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
