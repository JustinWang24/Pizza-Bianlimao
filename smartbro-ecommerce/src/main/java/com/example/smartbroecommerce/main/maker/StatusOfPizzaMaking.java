package com.example.smartbroecommerce.main.maker;

/**
 * Created by Justin Wang from SmartBro on 19/12/17.
 * 定义披萨制作期间的各种状态, 通过message.what 来发送
 */

public class StatusOfPizzaMaking {

    public static final int IN_PROGRESS = 100;
    public static final int DONE = 101;
    public static final int NEED_NEW_TASK = 5151; // 要求启动一个新的任务
}
