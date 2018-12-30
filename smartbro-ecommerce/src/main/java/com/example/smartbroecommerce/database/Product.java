package com.example.smartbroecommerce.database;

import android.util.Log;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.OrderBy;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.ArrayList;
import java.util.List;
import org.greenrobot.greendao.DaoException;

/**
 * Created by Justin Wang from SmartBro on 6/12/17.
 * 产品类
 */
@Entity(nameInDb = "products")
public class Product {
    @Id
    private long id = 0;                   // 在系统中的ID
    private String itemId = null;          // 产品的 itemId
    private String name = null;            // 产品的名称
    private String summary = null;         // 产品的简述
    private String mainImageUrl = null;    // 产品图片的URL
    private double price = 0;              // 产品的最新价格
    private double listPrice = 0;          // 产品的原始价格

    // 一个产品对应了多个保存的位置
    @ToMany(referencedJoinProperty = "productId")
    @OrderBy("expiredAt ASC")
    private List<Position> positions;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 694336451)
    private transient ProductDao myDao;


    @Generated(hash = 2097743201)
    public Product(long id, String itemId, String name, String summary, String mainImageUrl,
            double price, double listPrice) {
        this.id = id;
        this.itemId = itemId;
        this.name = name;
        this.summary = summary;
        this.mainImageUrl = mainImageUrl;
        this.price = price;
        this.listPrice = listPrice;
    }

    @Generated(hash = 1890278724)
    public Product() {
    }


    /**
     * 清空产品表
     */
    public static void flush(){
        ProductDao dao = DatabaseManager.getInstance().getProductDao();
        List<Product> list = dao.queryBuilder().list();
        for (Product product : list){
            dao.delete(product);
        }
    }

    /**
     * 根据给定的产品id返回产品
     * @param id
     * @return
     */
    public static Product find(long id){
        ProductDao dao = DatabaseManager.getInstance().getProductDao();
        List<Product> list = dao.queryBuilder()
                .where(ProductDao.Properties.Id.eq(id))
                .list();
        if(list.size() > 0){
            return list.get(0);
        }
        return null;
    }

    /**
     * 根据给定的产品 itemId 返回产品
     * @param itemId 产品的item id，是便利猫提供的
     * @return
     */
    public static Product find(String itemId){
        ProductDao dao = DatabaseManager.getInstance().getProductDao();
        List<Product> list = dao.queryBuilder()
                .where(ProductDao.Properties.ItemId.eq(itemId))
                .list();
        if(list.size() > 0){
            return list.get(0);
        }
        return null;
    }

    /**
     * 获取和产品关联的价格信息
     * @return float
     */
    public double getPrice(){
        return this.price;
    }

    /**
     * 获取价格的文字表述
     * @return String
     */
    public String getPriceText(){
        String priceText = "";
        MachineProfileDao dao = DatabaseManager.getInstance().getMachineProfileDao();
        List<MachineProfile> list = dao.queryBuilder().list();
        for (MachineProfile machineProfile:list){
            priceText += machineProfile.getCurrencySymbol() + Double.toString(this.getPrice());
        }
        return priceText;
    }

    @Override
    public String toString() {
        Log.i("Product", "***************** 对象信息输出开始 ********************");
        Log.i("产品名称", this.getName());
        Log.i("产品ID", Long.toString(this.getId()));
        Log.i("Product", "***************** 对象信息输出结束 ********************");
        return super.toString();
    }

    /**
     * 获取产品信息中，需要以tab方式显示的那些 tab的名字，比如 产品规格、评论 ...
     * @return ArrayList<String>
     */
    public ArrayList<String> getTabNames(){
        return null;
    }

    /**
     * 获取产品信息中，需要以tab方式显示的那些 tab的中的内容，由于不确定有那些内容，就是每个tab的内容都是 ArrayList<Object> 类型
     * 以适用于更多的情况。根据具体的进行改写
     * @return ArrayList<ArrayList<Object>>
     */
    public ArrayList<ArrayList<Object>> getTabContent(){
        return null;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSummary() {
        return this.summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getMainImageUrl() {
        return this.mainImageUrl;
    }

    public void setMainImageUrl(String mainImageUrl) {
        this.mainImageUrl = mainImageUrl;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 2116009110)
    public List<Position> getPositions() {
        if (positions == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            PositionDao targetDao = daoSession.getPositionDao();
            List<Position> positionsNew = targetDao._queryProduct_Positions(id);
            synchronized (this) {
                if (positions == null) {
                    positions = positionsNew;
                }
            }
        }
        return positions;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1228876919)
    public synchronized void resetPositions() {
        positions = null;
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    public String getItemId() {
        return this.itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public double getListPrice() {
        return this.listPrice;
    }

    public void setListPrice(double listPrice) {
        this.listPrice = listPrice;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1171535257)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getProductDao() : null;
    }

    
}
