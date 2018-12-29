package com.example.smartbroecommerce.Auth;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.smartbro.app.AccountManager;
import com.example.smartbroecommerce.database.DatabaseManager;
import com.example.smartbroecommerce.database.MachineProfile;
import com.example.smartbroecommerce.database.PaymentMethod;
import com.example.smartbroecommerce.database.Position;
import com.example.smartbroecommerce.database.Product;
import com.example.smartbroecommerce.database.ShoppingCartItem;
import java.util.Iterator;

/**
 * Created by Justin Wang from SmartBro on 6/12/17.
 */

public class MachineInitHandler {

    public static void onInitDone(String responseStringInJson, IAuthListener iAuthListener){
        final long machineId = initMachine(responseStringInJson);

        // Todo 保存设备初始化的状态, 或用户登录状态
        AccountManager.setSignState(true);
        AccountManager.setMachineId(machineId);
        // 执行注册成功的回调方法
        iAuthListener.onSignUpSuccess();
    }

    /**
     * 专门用来解析并初始化设备本地数据库的方法
     * @param responseStringInJson
     * @return
     */
    public static long initMachine(String responseStringInJson){

        final JSONObject machineJson =
                JSON.parseObject(responseStringInJson).getJSONObject("data");

        /*
         * 先清空已经有的数据
         */
        MachineProfile.flush();
        /*
         * 把网络的数据写入到数据库中
         */
        final long machineId = machineJson.getLong("machineId");
        final String uuid = machineJson.getString("uuid");
        final String machineName = machineJson.getString("machineName");
        final String machinePhone = machineJson.getString("machinePhone");
        final String serialName = machineJson.getString("serialName");
        final String operatorName = machineJson.getString("operatorName");
        final String operatorId = machineJson.getString("operatorId");
        final String currencySymbol = machineJson.getString("currencySymbol");
        final String language = machineJson.getString("language");
        final String hippoAppId = machineJson.getString("hippoAppId");
        final String hippoApiVersion = machineJson.getString("hippoApiVersion");
        final boolean supportCoupon = machineJson.getBoolean("support_coupon");

        final int maxProductsToSellOneTime = machineJson.getInteger("max_cups_one_time");
        final int multiple = machineJson.getInteger("multiple");

        final MachineProfile machineProfile = new MachineProfile(
                machineId,uuid,machineName,machinePhone,serialName,
                operatorName,operatorId,currencySymbol,language,hippoAppId,hippoApiVersion,maxProductsToSellOneTime, multiple,supportCoupon
        );

        // 持久化设备的数据表
        DatabaseManager.getInstance().getMachineProfileDao()
                .insert(machineProfile);

        // 解析json数组, 以便初始化Payment Methods
        PaymentMethod.flush();
        final JSONArray paymentMethodsJson = machineJson.getJSONArray("paymentMethods");
        for (Iterator iterator = paymentMethodsJson.iterator(); iterator.hasNext();){
            JSONObject paymentMethodJson = (JSONObject)iterator.next();
            long id = paymentMethodJson.getLong("id");
            String name = paymentMethodJson.getString("name");
            String priceText = paymentMethodJson.getString("priceText");
            float finalPrice = paymentMethodJson.getFloat("finalPrice");
            int type = paymentMethodJson.getInteger("type");
            PaymentMethod pm = new PaymentMethod(
                    id,type,name,priceText,finalPrice
            );
            DatabaseManager.getInstance().getPaymentMethodDao().insert(pm);
        }

//        Map<String, Long> productsId = new HashMap<String, Long>();

        // 解析Products 并持久化, 表示机器是默认支持同时售卖多种产品的
        Product.flush();
        final JSONArray productsJson = machineJson.getJSONArray("products");
        for (Iterator iterator = productsJson.iterator(); iterator.hasNext();){
            JSONObject productJson = (JSONObject)iterator.next();

            long id = productJson.getLong("id");
            String itemId = productJson.getString("itemId");
            String name = productJson.getString("name");
            String mainImageUrl = productJson.getString("mainImageUrl");
            String summary = productJson.getString("summary");
            double price = productJson.getDouble("price");
            double listPrice = productJson.getDouble("listPrice");

            Product product = new Product(id,itemId,name,summary,mainImageUrl,price,listPrice);
            DatabaseManager.getInstance().getProductDao().insert(product);
        }

        /**
         * 初始化设备的存储空间
         */
        Position.flush();
        final JSONArray positions = machineJson.getJSONArray("positions");

        for (Iterator iterator = positions.iterator(); iterator.hasNext();){
            JSONObject positionJson = (JSONObject)iterator.next();

            final Position position = new Position();
            position.setId(positionJson.getLong("id"));
            position.setProductId(positionJson.getLong("product_id"));
            position.setIndex(positionJson.getInteger("index"));
            position.setExpiredAt(positionJson.getDate("expiredAt"));
            position.setStatus(positionJson.getBoolean("status"));

            DatabaseManager.getInstance().getPositionDao().insert(position);
        }

        // CartItem也要清空
        ShoppingCartItem.flush();

        return machineId;
    }
}
