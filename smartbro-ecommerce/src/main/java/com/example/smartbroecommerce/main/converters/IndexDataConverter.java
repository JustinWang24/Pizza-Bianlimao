package com.example.smartbroecommerce.main.converters;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.smartbro.ui.recycler.DataConvertor;
import com.example.smartbro.ui.recycler.ItemType;
import com.example.smartbro.ui.recycler.MultipleFields;
import com.example.smartbro.ui.recycler.MultipleItemEntity;
import com.example.smartbroecommerce.database.DatabaseManager;
import com.example.smartbroecommerce.database.MachineProfile;
import com.example.smartbroecommerce.database.PaymentMethod;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Justin Wang from SmartBro on 9/12/17.
 */

public class IndexDataConverter extends DataConvertor {

    @Override
    public ArrayList<MultipleItemEntity> convert() {
        // 获取数据
        final JSONObject machineJson =
                JSON.parseObject(getJsonData()).getJSONObject("data");

        final long machineId = machineJson.getLong("machineId");
        final String uuid = machineJson.getString("uuid");
        final String machineName = machineJson.getString("machineName");
        final String machinePhone = machineJson.getString("machinePhone");
        final String serialName = machineJson.getString("serialName");
        final String operatorName = machineJson.getString("operatorName");
        final String operatorId = machineJson.getString("operatorId");
        final String currencySymbol = machineJson.getString("currencySymbol");
        final String language = machineJson.getString("language");
        final int maxProductsToSellOneTime = machineJson.getInteger("max_cups_one_time");
        final int multiple = machineJson.getInteger("multiple");

//        final MachineProfile machineProfile = new MachineProfile(
//                machineId,uuid,machineName,machinePhone,serialName,
//                operatorName,operatorId,currencySymbol,language,maxProductsToSellOneTime, multiple
//        );

        // 解析json数组
        final JSONArray paymentMethodsJson = machineJson.getJSONArray("paymentMethods");
        for (Iterator iterator = paymentMethodsJson.iterator(); iterator.hasNext();){
            JSONObject paymentMethodJson = (JSONObject)iterator.next();
            long id = paymentMethodJson.getLong("id");
            String name = paymentMethodJson.getString("name");
            String priceText = paymentMethodJson.getString("priceText");
            float finalPrice = paymentMethodJson.getFloat("finalPrice");
            int type = paymentMethodJson.getInteger("type");

            // Todo 持久化保存支付信息
//            PaymentMethod pm = new PaymentMethod(
//                    id,type,name,priceText,finalPrice
//            );
//            DatabaseManager.getInstance().getPaymentMethodDao().insert(pm);
        }

        // 解析Products 并持久化, 表示机器是默认支持同时售卖多种产品的
        final JSONArray productsJson = machineJson.getJSONArray("products");
        for (Iterator iterator = productsJson.iterator(); iterator.hasNext();){
            JSONObject productJson = (JSONObject)iterator.next();

            long id = productJson.getLong("id");
            String name = productJson.getString("name");
            String mainImageUrl = productJson.getString("mainImageUrl");
            String summary = productJson.getString("summary");

            // 判断传递回来的产品信息的类型, 其实都是图文类型
            int productItemType = ItemType.TEXT_PLUS_IMAGE;

            final MultipleItemEntity entity = MultipleItemEntity.builder()
                    .setItemType(productItemType)
                    .setField(MultipleFields.ID,id)
                    .setField(MultipleFields.NAME, name)
                    .setField(MultipleFields.IMAGE_URL, mainImageUrl)
                    .setField(MultipleFields.TEXT, summary)
                    .build();

            ENTITIES.add(entity);

            // Todo 持久化保存产品信息
//            Product product = new Product(id,name,summary,mainImageUrl);
//            DatabaseManager.getInstance().getProductDao().insert(product);
        }

        return ENTITIES;
    }
}
