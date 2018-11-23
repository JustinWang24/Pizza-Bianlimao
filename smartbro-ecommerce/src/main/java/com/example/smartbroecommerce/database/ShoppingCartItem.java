package com.example.smartbroecommerce.database;

import com.alibaba.fastjson.annotation.JSONField;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.JoinProperty;
import org.greenrobot.greendao.annotation.OrderBy;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.ArrayList;
import java.util.List;
import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Transient;

/**
 * Created by Justin Wang from SmartBro on 12/12/17.
 * 购物车中购物项
 */

@Entity(nameInDb = "cart_items")
public class ShoppingCartItem {

    // 这个不是数据库表的字段，也没有关联关系
    private static List<Position> productPositions = new ArrayList<>();

    @JSONField(name="subtotal")
    @Transient
    public double subtotal = 0;  // 这个不是数据库的字段，而是专门为了提交数据准备的

    @JSONField(name="positionsIndex")
    @Transient
    public String positionsIndex = "";  // 这个不是数据库的字段，而是专门为了提交数据准备的

    @Id
    private long id = 0;   // 在系统中的ID
    @JSONField(name="productId")
    private long productId = 0;
    private String productName = null;
    private String productImage = null;

    @JSONField(name="quantity")
    private int quantity = 1;

    @JSONField(name="price")
    private double productPrice = 0;


    @Generated(hash = 151240552)
    public ShoppingCartItem(long id, long productId, String productName, String productImage,
            int quantity, double productPrice) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.productImage = productImage;
        this.quantity = quantity;
        this.productPrice = productPrice;
    }

    @Generated(hash = 526600932)
    public ShoppingCartItem() {
    }


    /**
     * 清空数据库购物车项目表的方法
     */
    public static void flush(){
        ShoppingCartItemDao dao = DatabaseManager.getInstance().getShoppingCartItemDao();
        List<ShoppingCartItem> items = dao.queryBuilder().list();

        if(items != null){
            for (ShoppingCartItem item : items){
                dao.delete(item);
            }
        }

        productPositions = null;
        productPositions = new ArrayList<>();
    }

    /**
     * 根据给定的ID删除购物车项目的方法
     * @param id
     */
    public static void terminate(long id){
        ShoppingCartItemDao dao = DatabaseManager.getInstance().getShoppingCartItemDao();
        List<ShoppingCartItem> items = dao.queryBuilder()
                .where(ShoppingCartItemDao.Properties.Id.eq(id))
                .limit(1)
                .list();
        if(items.size() > 0){
            final ShoppingCartItem item = items.get(0);
            // 把所有的占用位置释放
            List<Position> positions = item.getPositions();
            for (Position position: positions) {
                position.enable();
            }
            //
            item.setPositions(new ArrayList<Position>());
            dao.delete(item);
        }
    }

    /**
     * 根据给定的ID删除购物车项目的方法, 但是不回复位置, 在烤饼完成之后的清除操作
     * @param id
     */
    public static void terminateCartAndPosition(long id){
        ShoppingCartItemDao dao = DatabaseManager.getInstance().getShoppingCartItemDao();
        List<ShoppingCartItem> items = dao.queryBuilder()
                .where(ShoppingCartItemDao.Properties.Id.eq(id))
                .limit(1)
                .list();
        if(items.size() > 0){
            final ShoppingCartItem item = items.get(0);
            // 把所有的占用位置释放
            item.setPositions(new ArrayList<Position>());
            dao.delete(item);
        }
    }

    /**
     * 根据给定的ID删除购物车项目的方法
     * @param id cartItem的ID
     */
    public static ShoppingCartItem findById(long id){
        ShoppingCartItemDao dao = DatabaseManager.getInstance().getShoppingCartItemDao();
        List<ShoppingCartItem> items = dao.queryBuilder()
                .where(ShoppingCartItemDao.Properties.Id.eq(id))
                .limit(1)
                .list();
        if(items.size() > 0){
            return items.get(0);
        }
        return null;
    }

    /**
     * 添加一个新的position
     * @param position 对应占用的位置对象
     */
    public void takePosition(Position position){
        // 把新位置加入到列表中
        productPositions.add(position);
        // 标记给定的position已经不可用了
        position.disable();
    }

    /**
     * 释放一个已被占用的位置
     */
    public void releaseAPosition(){
        final int size = productPositions.size();
        if(size > 0){
            // 取得第一个
            Position position = productPositions.get(0);
            position.enable();
            productPositions.remove(0);
        }
    }

    /**
     * 获取position index的数组
     * @return
     */
    public String getPositionsIndex(){
        List<Position> positions = this.getPositions();

        for (Position pos :
                positions) {
            this.positionsIndex += Integer.toString(pos.getIndex()) + ",";
        }

        return this.positionsIndex;
    }

    /**
     * 获取关联的产品的位置
     * @return List<Position>
     */
    public List<Position> getPositions(){
        return productPositions;
    }

    /**
     * 设置positions
     * @param ps
     */
    public void setPositions(List<Position> ps){
        productPositions = ps;
    }

    /**
     * 不是自动生成的，获取小计金额
     * @return
     */
    public double getSubtotal() {
        return this.subtotal;
    }

    /**
     * 不是自动生成的， 设置小计金额
     * @param subtotal
     */
    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
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

    public String getProductName() {
        return this.productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductImage() {
        return this.productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getProductPrice() {
        return this.productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }
}
