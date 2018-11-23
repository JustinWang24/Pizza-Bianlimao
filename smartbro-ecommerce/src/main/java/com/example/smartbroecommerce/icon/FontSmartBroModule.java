package com.example.smartbroecommerce.icon;

import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.IconFontDescriptor;

/**
 * Created by Justin Wang from SmartBro on 17/12/17.
 */

public class FontSmartBroModule implements IconFontDescriptor{
    @Override
    public String ttfFileName() {
        // Assets 中ttf文件的名字
        return "iconfont.ttf";
    }

    @Override
    public Icon[] characters() {
        return SmartBroIcons.values();
    }
}
