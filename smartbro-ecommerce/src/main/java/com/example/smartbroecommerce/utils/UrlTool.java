package com.example.smartbroecommerce.utils;

import com.example.smartbro.net.RestfulClient;
import com.example.smartbro.net.callback.IError;
import com.example.smartbro.net.callback.IFailure;
import com.example.smartbro.net.callback.ISuccess;
import com.example.smartbroecommerce.database.ShoppingCart;
import com.example.smartbroecommerce.database.ShoppingCartItem;

import java.util.List;

/**
 * Created by Justin Wang from SmartBro on 19/12/17.
 */

public class UrlTool {
    private static final String API_REPORT_MACHINE_STATUS = "machines/report_status";
    private static final String API_REPORT_ORDER_COMPLETE = "switch_order_to_complete";

    /**
     * 上报设备故障的静态方法
     * @param uuid
     * @param errorCode
     * @param orderId
     * @param notes
     */
    public static void reportMachineStatus(String uuid, int errorCode, String notes, Integer orderId){
        if( orderId <= 0){
            // 设备运行期间出现的故障
            RestfulClient.builder()
                .url(API_REPORT_MACHINE_STATUS)
                .params("uuid", uuid)
                .params("status_code",Integer.toString(errorCode))  // 把第一个故障的故障码传过去
                .params("notes",notes)
                .success(new ISuccess() {
                    @Override
                    public void onSuccess(String response) {
                        // 上报成功之后的处理
                        // 把购物车清空
//                            clearShoppingCart();
                    }
                })
                .error(new IError() {
                    @Override
                    public void onError(int code, String msg) {

                    }
                })
                .failure(new IFailure() {
                    @Override
                    public void onFailure() {

                    }
                })
                .build()
                .post();
        }else {
            // 烤披萨期间出现的故障
            RestfulClient.builder()
                .url(API_REPORT_MACHINE_STATUS)
                .params("order_id",Integer.toString(orderId))
                .params("uuid", uuid)
                .params("status_code",Integer.toString(errorCode))  // 把第一个故障的故障码传过去
                .params("notes",notes)
                .success(new ISuccess() {
                    @Override
                    public void onSuccess(String response) {
                        // 上报成功之后的处理
                        // 把购物车清空
//                            clearShoppingCart();
                    }
                })
                .error(new IError() {
                    @Override
                    public void onError(int code, String msg) {

                    }
                })
                .failure(new IFailure() {
                    @Override
                    public void onFailure() {

                    }
                })
                .build()
                .post();
        }
    }

    public static void clearShoppingCart(){
        final List<ShoppingCartItem> items = ShoppingCart.getInstance().getShoppingCartItems();
        if(items != null){
            final int size = items.size();
            for (int i = 0; i < size; i++) {
                ShoppingCart.getInstance().removeCartItem(i);
            }
        }
    }

    /**
     * 上报订单顺利完成的方法
     * 无论是否上报成功, 都需要把本地的数据清空
     * @param orderId
     */
    public static void reportOrderComplete(int orderId){
        // 把购物车清空
        ShoppingCart.getInstance().clear();
        if( orderId > 0){
            RestfulClient.builder()
                    .url(API_REPORT_ORDER_COMPLETE)
                    .params("order_id",Integer.toString(orderId))
                    .success(new ISuccess() {
                        @Override
                        public void onSuccess(String response) {

                        }
                    })
                    .error(new IError() {
                        @Override
                        public void onError(int code, String msg) {

                        }
                    })
                    .failure(new IFailure() {
                        @Override
                        public void onFailure() {

                        }
                    })
                    .build()
                    .get();
        }
    }
}
