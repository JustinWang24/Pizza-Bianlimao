package com.example.smartbroecommerce.icon;

import com.joanzapata.iconify.Icon;

/**
 * Created by Justin Wang from SmartBro on 17/12/17.
 * 自定义的两个图标
 */

public enum SmartBroIcons implements Icon {
    icon_scan('\ue602'),
    icon_alipay('\ue606'),
    icon_cancel('\ue501'),
    icon_zhifubaoBig('\ue676'),
    icon_weixinBig('\ue603'),
    icon_zhifubao('\ue60a'),
    icon_weixin('\ue509');

    private char character;

    SmartBroIcons(char c){
        this.character = c;
    }

    @Override
    public String key() {
        return name().replace('_', '-');
    }

    @Override
    public char character() {
        return this.character;
    }
}
