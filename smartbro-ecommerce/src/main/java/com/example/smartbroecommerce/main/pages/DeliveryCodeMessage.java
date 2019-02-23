package com.example.smartbroecommerce.main.pages;

/**
 * Created by Justin Wang from SmartBro on 23/2/19.
 */
public class DeliveryCodeMessage {
    // 获取到了扫码枪的码 也到服务器进行了验证 验证无效的时候 用这个值表示
    public static final int CODE_VERIFY_FAILED = -29;

    // 获取到了扫码枪的码 也到服务器进行了验证 验证成功 却发现没有产品 用这个值表示
    public static final int CODE_VERIFY_OK_BUT_NO_PRODUCT = -28;
    public static final String CODE_VERIFY_OK_BUT_NO_PRODUCT_TEXT = "自提码指定的产品已经售完";

    public static final int CODE_VERIFY_FAILED_INTERNET_ISSUE = -27;
    public static final String CODE_VERIFY_FAILED_INTERNET_ISSUE_TEXT = "互联网连接异常, 请稍后再试";
}
