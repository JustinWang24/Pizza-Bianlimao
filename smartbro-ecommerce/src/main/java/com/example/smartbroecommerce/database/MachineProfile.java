package com.example.smartbroecommerce.database;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

import java.util.List;

/**
 * Created by Justin Wang from SmartBro on 6/12/17.
 */

@Entity(nameInDb = "machine_profile")
public class MachineProfile {
    @Id
    private long id = 0;                        // ID
    private String uuid = null;                 // 设备的UUID
    private String machineName = null;          // 设备的名称
    private String machinePhone = null;         // 设备的维护电话
    private String serialName = null;           // 设备的序列号
    private String operatorName = null;         // 那个经销商的名字
    private String operatorId = null;           // 那个经销商的ID
    private String currencySymbol = null;       // 设备的货币符号
    private String language = null;             // 设备运行的语言 （在pizza机没用）
    private int maxProductsToSellOneTime = 1;   // 一次最多可以卖几个产品
    private int multiple = 1;                   // 投币器的放大倍数，默认为1 不放大
    private boolean supportCoupon = false;      // 是否本机支持优惠码

    @Generated(hash = 1584803101)
    public MachineProfile(long id, String uuid, String machineName, String machinePhone,
            String serialName, String operatorName, String operatorId, String currencySymbol,
            String language, int maxProductsToSellOneTime, int multiple,
            boolean supportCoupon) {
        this.id = id;
        this.uuid = uuid;
        this.machineName = machineName;
        this.machinePhone = machinePhone;
        this.serialName = serialName;
        this.operatorName = operatorName;
        this.operatorId = operatorId;
        this.currencySymbol = currencySymbol;
        this.language = language;
        this.maxProductsToSellOneTime = maxProductsToSellOneTime;
        this.multiple = multiple;
        this.supportCoupon = supportCoupon;
    }

    @Generated(hash = 894943870)
    public MachineProfile() {
    }

    /**
     * 清空数据表的方法
     */
    public static void flush(){
        MachineProfileDao dao = DatabaseManager.getInstance().getMachineProfileDao();
        List<MachineProfile> list = dao.queryBuilder().list();
        for (MachineProfile mp:list) {
            dao.delete(mp);
        }
    }

    /**
     * 获取machine的唯一一条记录
     * @return
     */
    public static MachineProfile getInstance(){
        MachineProfileDao dao = DatabaseManager.getInstance().getMachineProfileDao();
        List<MachineProfile> list = dao.queryBuilder().list();
        MachineProfile machine = null;
        for (MachineProfile mp:list) {
            machine = mp;
            break;
        }
        return machine;
    }

    /**
     * 获取本设备最多能一次烤几张饼
     * @return int
     */
    public static int getShoppingCartVolumn(){
        MachineProfileDao dao = DatabaseManager.getInstance().getMachineProfileDao();
        List<MachineProfile> list = dao.queryBuilder().list();
        int volume = 0;

        if(list.size() == 0){
            throw new NullPointerException("Machine init data missing!");
        }else {
            for (MachineProfile mp:list) {
                volume = mp.getMaxProductsToSellOneTime();
                break;
            }
        }
        return volume;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getMachineName() {
        return this.machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public String getMachinePhone() {
        return this.machinePhone;
    }

    public void setMachinePhone(String machinePhone) {
        this.machinePhone = machinePhone;
    }

    public String getSerialName() {
        return this.serialName;
    }

    public void setSerialName(String serialName) {
        this.serialName = serialName;
    }

    public String getOperatorName() {
        return this.operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getOperatorId() {
        return this.operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getCurrencySymbol() {
        return this.currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public String getLanguage() {
        return this.language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getMaxProductsToSellOneTime() {
        return this.maxProductsToSellOneTime;
    }

    public void setMaxProductsToSellOneTime(int maxProductsToSellOneTime) {
        this.maxProductsToSellOneTime = maxProductsToSellOneTime;
    }

    public int getMultiple() {
        return this.multiple;
    }

    public void setMultiple(int multiple) {
        this.multiple = multiple;
    }

    public boolean getSupportCoupon() {
        return this.supportCoupon;
    }

    public void setSupportCoupon(boolean supportCoupon) {
        this.supportCoupon = supportCoupon;
    }

    
}
