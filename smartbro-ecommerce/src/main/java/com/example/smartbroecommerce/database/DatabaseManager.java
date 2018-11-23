package com.example.smartbroecommerce.database;

import android.content.Context;

import org.greenrobot.greendao.database.Database;

/**
 * Created by Justin Wang from SmartBro on 5/12/17.
 * 在应用程序的开始， 使用 DatabaseManager.getInstance().init(this) 即可
 */

public class DatabaseManager {
    private DaoSession mDaoSession = null;
    private UserProfileDao mDao = null;
    // 和设备相关的
    private DaoSession machineSession = null;
    private MachineProfileDao machineProfileDao = null;
    // 和支付相关的
    private DaoSession paymentMethodSession = null;
    private PaymentMethodDao paymentMethodDao = null;
    // 和产品相关的
    private DaoSession productSession = null;
    private ProductDao productDao = null;
    // 和购物相关的
    private DaoSession shoppingCartItemSession = null;
    private ShoppingCartItemDao shoppingCartItemDao = null;
    // 和货品保存位置相关
    private DaoSession positionSession = null;
    private PositionDao positionDao = null;

    /**
     * 私有构造函数，实现单例模式
     */
    private DatabaseManager(){

    }

    /**
     * 初始化方法
     * @param context 上下文
     * @return DatabaseManager
     */
    public DatabaseManager init(Context context){
        initDao(context);
        initMachineDao(context);
        initPaymentMethodDao(context);
        initProductDao(context);
        initShoppingCartItemDao(context);
        initPositionDao(context);
        return this;
    }

    /**
     * 获取产品位置dao对象
     * @return PositionDao
     */
    public final PositionDao getPositionDao(){
        return this.positionDao;
    }

    /**
     * 获取dao对象
     * @return UserProfileDao
     */
    public final UserProfileDao getDao(){
        return this.mDao;
    }

    /**
     * 获取 Machine dao对象
     * @return MachineProfileDao
     */
    public final MachineProfileDao getMachineProfileDao(){
        return this.machineProfileDao;
    }

    /**
     * 获取 Payment dao对象
     * @return PaymentMethodDao
     */
    public final PaymentMethodDao getPaymentMethodDao(){
        return this.paymentMethodDao;
    }

    /**
     * 获取 Product dao对象
     * @return ProductDao
     */
    public final ProductDao getProductDao(){
        return this.productDao;
    }

    /**
     * 获取 Shopping Cart dao对象
     * @return ShoppingCartItemDao
     */
    public final ShoppingCartItemDao getShoppingCartItemDao(){
        return this.shoppingCartItemDao;
    }

    /**
     * 获取 DatabaseManager 的单例对象
     * @return DatabaseManager
     */
    public static DatabaseManager getInstance(){
        return Holder.INSTANCE;
    }

    /**
     * 静态内部类，实现单例的holder
     */
    private static final class Holder{
        private static final DatabaseManager INSTANCE = new DatabaseManager();
    }

    /**
     * 初始化Dao对象
     * @param context 上下文
     */
    private void initDao(Context context){
        // 创建helper，传入上下文和数据库的名称
        final ReleaseOpenHelper helper = new ReleaseOpenHelper(context, "pizza_box.db");
        final Database db = helper.getWritableDb();
        this.mDaoSession = new DaoMaster(db).newSession();

        this.mDao = this.mDaoSession.getUserProfileDao();
    }

    private void initMachineDao(Context context){
        // 创建helper，传入上下文和数据库的名称
        final ReleaseOpenHelper helper = new ReleaseOpenHelper(context, "pizza_box.db");
        final Database db = helper.getWritableDb();
        this.machineSession = new DaoMaster(db).newSession();

        // 创建Machine的Dao
        this.machineProfileDao = this.machineSession.getMachineProfileDao();
    }

    private void initPaymentMethodDao(Context context){
        // 创建helper，传入上下文和数据库的名称
        final ReleaseOpenHelper helper = new ReleaseOpenHelper(context, "pizza_box.db");
        final Database db = helper.getWritableDb();
        this.paymentMethodSession = new DaoMaster(db).newSession();

        // 创建Machine的Dao
        this.paymentMethodDao = this.paymentMethodSession.getPaymentMethodDao();
    }

    private void initProductDao(Context context){
        // 创建helper，传入上下文和数据库的名称
        final ReleaseOpenHelper helper = new ReleaseOpenHelper(context, "pizza_box.db");
        final Database db = helper.getWritableDb();
        this.productSession = new DaoMaster(db).newSession();

        // 创建Machine的Dao
        this.productDao = this.productSession.getProductDao();
    }

    private void initShoppingCartItemDao(Context context){
        final ReleaseOpenHelper helper = new ReleaseOpenHelper(context, "pizza_box.db");
        final Database db = helper.getWritableDb();
        this.shoppingCartItemSession = new DaoMaster(db).newSession();
        this.shoppingCartItemDao = this.shoppingCartItemSession.getShoppingCartItemDao();
    }

    /**
     * 初始化位置dao对象
     * @param context
     */
    private void initPositionDao(Context context){
        final ReleaseOpenHelper helper = new ReleaseOpenHelper(context, "pizza_box.db");
        final Database db = helper.getWritableDb();
        this.positionSession = new DaoMaster(db).newSession();
        this.positionDao = this.positionSession.getPositionDao();
    }
}
