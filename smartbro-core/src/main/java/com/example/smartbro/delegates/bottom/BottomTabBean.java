package com.example.smartbro.delegates.bottom;

/**
 * Created by Justin Wang from SmartBro on 9/12/17.
 */

public final class BottomTabBean {

    private final CharSequence ICON;    // 保存底部tab按键的图标
    private final CharSequence TITLE;   // 保存底部tab按键的文字

    public BottomTabBean(CharSequence icon, CharSequence title){
        this.ICON = icon;
        this.TITLE = title;
    }

    public CharSequence getIcon(){
        return this.ICON;
    }

    public CharSequence getTitle(){
        return this.TITLE;
    }
}
