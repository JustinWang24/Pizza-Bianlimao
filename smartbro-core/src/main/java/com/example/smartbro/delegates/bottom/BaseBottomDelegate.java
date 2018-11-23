package com.example.smartbro.delegates.bottom;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.smarbro.R;
import com.example.smarbro.R2;
import com.example.smartbro.delegates.SmartbroDelegate;
import com.joanzapata.iconify.widget.IconTextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import butterknife.BindView;
import me.yokeyword.fragmentation.SupportFragment;

/**
 * Created by Justin Wang from SmartBro on 9/12/17.
 * 1: 需要 Bean 保存每个 bottom button 的信息
 */

public abstract class BaseBottomDelegate extends SmartbroDelegate
    implements View.OnClickListener{

    // 保存所有底部导航按钮相关联的子Delegate的列表
    private final ArrayList<BottomTabBean> TAB_BEANS = new ArrayList<>();
    private final ArrayList<BottomItemDelegate> ITEM_DELEGATES = new ArrayList<>();

    // 保存底部按钮和delegate的关系
    private final LinkedHashMap<BottomTabBean, BottomItemDelegate> ITEMS = new LinkedHashMap<>();

    @BindView(R2.id.bottom_bar)
    LinearLayoutCompat mBottomBar = null;

    /*
        以下两个变量主要决定了在进入程序的时候，显示的是哪个Tab及对应的Delegate
     */
    private int mCurrentDelegate = 0;
    private int mIndexDelegate = 0;

    private int mClickedItemColor = Color.RED;

    public abstract LinkedHashMap<BottomTabBean, BottomItemDelegate> setItems(ItemBuilder itemBuilder);

    public abstract int setIndexDelegate();
    @ColorInt
    public abstract int setClickedColor();

    /**
     * 设置布局文件
     * @return
     */
    @Override
    public Object setLayout() {
        return R.layout.delegate_bottom;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCurrentDelegate = setIndexDelegate();
        if(setClickedColor() != 0){
            mClickedItemColor = setClickedColor();
        }

        final ItemBuilder builder = ItemBuilder.builder();
        final LinkedHashMap<BottomTabBean,BottomItemDelegate> items = setItems(builder);
        ITEMS.putAll(items);

        /*
         *  循环处理map
         */
        for (Map.Entry<BottomTabBean, BottomItemDelegate> item: ITEMS.entrySet()){
            final BottomTabBean key = item.getKey();
            final BottomItemDelegate value = item.getValue();

            TAB_BEANS.add(key);
            ITEM_DELEGATES.add(value);
        }
    }

    /**
     * 处理: 如何把delegate放到界面中
     * @param savedInstanceState
     * @param rootView
     */
    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        final int size = ITEMS.size();
        for (int i = 0; i < size; i++) {
            LayoutInflater.from(getContext())
                    .inflate(
                            R.layout.bottom_item_icon_text_layout,
                            mBottomBar
                    );

            final RelativeLayout item = (RelativeLayout) mBottomBar.getChildAt(i);

            // 设置每个item的点击事件
            item.setTag(i);
            item.setOnClickListener(this);

            final IconTextView icon = (IconTextView)item.getChildAt(0);
            final AppCompatTextView title = (AppCompatTextView) item.getChildAt(1);

            final BottomTabBean bean = TAB_BEANS.get(i);

            // 初始化数据
            icon.setText(bean.getIcon());
            title.setText(bean.getTitle());

            if(i == mIndexDelegate){
                // 如果角标是需要当前高亮的, 那么设定icon和title的颜色为高亮的颜色
                icon.setTextColor(mClickedItemColor);
                title.setTextColor(mClickedItemColor);
            }
        }

        final SupportFragment[] delegateArray = ITEM_DELEGATES.toArray(new SupportFragment[size]);

        loadMultipleRootFragment(R.id.bottom_bar_delegate_container, mIndexDelegate, delegateArray);
    }

    /**
     * 重置底部所有按钮的颜色
     * 当前函数中指定了灰色 Color.GRAY
     */
    private void resetColor(@ColorInt int color){
        final int count = mBottomBar.getChildCount();
        final int defaultColor = Color.GRAY;
        if(color == -1){
            // 如果传入了 -1, 表示使用框架默认的颜色
            color = defaultColor;
        }

        for (int i = 0; i <count; i++) {
            final RelativeLayout item = (RelativeLayout)mBottomBar.getChildAt(i);
            this.setItemColor(item,color);
        }
    }

    /**
     * 将指定的 item的 图标icon和文字颜色变成指定的color
     * @param item
     * @param color
     */
    private void setItemColor(RelativeLayout item,@ColorInt int color){
        final IconTextView icon = (IconTextView)item.getChildAt(0);
        icon.setTextColor(color);

        final AppCompatTextView title = (AppCompatTextView) item.getChildAt(1);
        title.setTextColor(color);
    }

    /**
     * 底部按钮点击事件的监听.
     * 当点击后，首先所有颜色重置，然后更改被点击的按钮的颜色, 最后切换Fragment
     * @param view
     */
    @Override
    public void onClick(View view) {
        final int tag = (int) view.getTag();
        // 切换所有的按钮颜色为默认的颜色
        resetColor(-1);

        this.setItemColor((RelativeLayout) view, mClickedItemColor);

        // 关键点: 切换Fragment
        showHideFragment(
                ITEM_DELEGATES.get(tag),                // 需要显示的Fragment
                ITEM_DELEGATES.get(mCurrentDelegate)    // 需要隐藏的Fragment
        );

        // 把当前的Delegate的tag值变成被点击的
        mCurrentDelegate = tag;
    }
}
