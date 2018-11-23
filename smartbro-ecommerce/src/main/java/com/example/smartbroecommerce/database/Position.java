package com.example.smartbroecommerce.database;

import android.text.format.DateUtils;
import android.util.Log;

import com.alibaba.fastjson.annotation.JSONField;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.greenrobot.greendao.annotation.Generated;
/**
 * Created by Justin Wang from SmartBro on 14/12/17.
 * 设备存储Pizza的位置的对象
 */
@Entity(nameInDb = "positions")
public class Position {
    @Id
    private long id = 0;
    private long productId = 0;     // 该位置存放的什么产品
    @JSONField(name = "index")
    private int index = 1;              // 该位置的编号, 实际的值为 1 - 40. 面对机器，右下为1， 左上为40
    private boolean status = false; // 表示该位置当前是否有产品
    private Date expiredAt = null;  // 表示该位置的产品的过期时间

    @Generated(hash = 1159311007)
    public Position(long id, long productId, int index, boolean status,
            Date expiredAt) {
        this.id = id;
        this.productId = productId;
        this.index = index;
        this.status = status;
        this.expiredAt = expiredAt;
    }

    @Generated(hash = 958937587)
    public Position() {
    }

    /**
     * 清空位置表
     */
    public static void flush(){
        PositionDao dao = DatabaseManager.getInstance().getPositionDao();
        List<Position> list = dao.queryBuilder().list();
        for (Position position : list){
            dao.delete(position);
        }
    }

    /**
     * 根据index获取position对象
     * @param targetIndex
     * @return
     */
    public static Position findByIndex(int targetIndex){
        PositionDao dao = DatabaseManager.getInstance().getPositionDao();
        List<Position> list = dao.queryBuilder()
                .where(PositionDao.Properties.Index.eq(targetIndex))
                .limit(1)
                .list();
        Position result = null;
        if(list.size() > 0){
            result = list.get(0);
        }
        return result;
    }

    /**
     * 根据给定的产品, 返回一个最有利的position
     * 一定要返回一个还没有过期的位置.
     * 如果没有符合条件的位置, 那么返回 null
     * @param product
     * @return Position
     */
    public static Position getPositionByProduct(Product product){
        PositionDao dao = DatabaseManager.getInstance().getPositionDao();
        Position result = null;

        List<Position> list = dao.queryBuilder()
                .where(PositionDao.Properties.ProductId.eq(product.getId()))
                .where(PositionDao.Properties.ExpiredAt.ge(new Date()))
                .where(PositionDao.Properties.Status.eq(true))
                .orderAsc(PositionDao.Properties.ExpiredAt)
                .limit(1)
                .list();
        if(list.size() > 0){
            result = list.get(0);
        }
        return result;
    }

    public static boolean isOutOfStock(){
        PositionDao dao = DatabaseManager.getInstance().getPositionDao();
        List<Position> list = dao.queryBuilder()
                .where(PositionDao.Properties.ExpiredAt.ge(new Date()))
                .where(PositionDao.Properties.Status.eq(true))
                .orderAsc(PositionDao.Properties.ExpiredAt)
                .list();
        return list.size() == 0;
    }

    /**
     * 使位置失效的方法
     */
    public void disable(){
        this.setStatus(false);
        PositionDao dao = DatabaseManager.getInstance().getPositionDao();
        dao.update(this);
    }

    /**
     * 使位置有效的方法, 一旦使位置有效，则加入48小时的有效期
     */
    public void enable(){
        this.setStatus(true);
        PositionDao dao = DatabaseManager.getInstance().getPositionDao();
        dao.update(this);
    }

    /**
     * 是位置有效并给他48小时的有效期
     */
    public void enableAndGive48Hours(){
        this.setStatus(true);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR_OF_DAY, 48);
        this.setExpiredAt(calendar.getTime());

        Log.i("Info",calendar.getTime().toString());
        PositionDao dao = DatabaseManager.getInstance().getPositionDao();
        dao.update(this);
    }

    /**
     * 获取位置关联的产品对象
     * @return Product
     */
    public Product getProduct(){
        Product product = Product.find(this.getProductId());
        return product;
    }

    /**
     * 判断某个位置是否有效的方法, 根据status和expired date两个条件
     * @return
     */
    public boolean isAvailable(){
        final Date now = new Date();
//        Log.i("now",now.toString());
//        Log.i("expire",this.expiredAt.toString());
//        Log.i("info",Boolean.toString(now.before(this.getExpiredAt())));
//        Log.i("info","***********");
        return this.getStatus() && now.before(this.getExpiredAt());
    }

    @Override
    public String toString() {

//        Log.i("Position", "***************** 对象信息输出开始 ********************");
//        Log.i("添加的位置", Integer.toString(this.getIndex()));
//        Log.i("添加的位置过期时间", this.getExpiredAt().toString());
//        Log.i("添加的位置的状态", Boolean.toString(this.getStatus()));
//        Log.i("添加的位置的产品ID", Long.toString(this.getProductId()));
//        Log.i("Position", "***************** 对象信息输出结束 ********************");
        return super.toString();
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getProductId() {
        return this.productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean getStatus() {
        return this.status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Date getExpiredAt() {
        return this.expiredAt;
    }

    public void setExpiredAt(Date expiredAt) {
        this.expiredAt = expiredAt;
    }

    
}
