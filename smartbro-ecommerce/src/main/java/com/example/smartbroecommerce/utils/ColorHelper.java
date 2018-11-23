package com.example.smartbroecommerce.utils;

import android.content.res.Resources;
import com.example.smartbroecommerce.R;

/**
 * Created by Justin Wang from SmartBro on 21/10/18.
 */
public class ColorHelper {
    public static final String BLACK = "black";
    public static final String WHITE = "white";

    /**
     * Get color int value by given name
     * @param colorName Color name string
     * @return
     */
    public static int GetColorIntValueByName(String colorName, Resources resources){
        int result = 0;
        colorName = colorName.toLowerCase();    // 必须转换为小写字母
        switch (colorName){
            case BLACK:
                result = resources.getColor(R.color.black);
                break;
            case WHITE:
                result = resources.getColor(R.color.white);
                break;
            default:
                break;
        }
        return result;
    }
}
