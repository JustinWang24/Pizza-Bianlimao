package com.example.smartbroecommerce.Auth;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * Created by Justin Wang from SmartBro on 6/12/17.
 */

public class AuthHandler {
    public static void onSignUp(String responseStringInJson){
        // 用 fastjson 解析返回的字符串
        final JSONObject profileJson = JSON.parseObject(responseStringInJson).getJSONObject("data");
    }
}
