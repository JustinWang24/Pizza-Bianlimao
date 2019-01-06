package com.example.smartbroecommerce.utils;

import java.util.ArrayList;

/**
 * Created by Justin Wang from SmartBro on 6/1/19.
 */
public class BannerTool {
    private final static ArrayList<String> bannerImages = new ArrayList<>();

    private static class Holder{
        private static final BannerTool INSTANCE = new BannerTool();
    }

    public static BannerTool GetInstance(){
        return Holder.INSTANCE;
    }

    public ArrayList<String> getBannerImages(){
        return bannerImages;
    }
}
