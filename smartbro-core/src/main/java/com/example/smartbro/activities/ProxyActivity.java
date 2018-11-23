package com.example.smartbro.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.ContentFrameLayout;

import com.example.smarbro.R;
import com.example.smartbro.delegates.SmartbroDelegate;

import me.yokeyword.fragmentation.SupportActivity;

/**
 * Created by Justin Wang from SmartBro on 2/12/17.
 * 这个类是实现整个单Activity接口的基础类
 * 需要使用的时候，在主程序中实现定义的抽象方法即可
 */

public abstract class ProxyActivity extends SupportActivity {

    @SuppressWarnings("SpellCheckingInspection")
    public abstract SmartbroDelegate setRootDelegate();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initContainer(savedInstanceState);
    }

    /**
     * 初始化容器
     * @param savedInstanceState
     */
    private void initContainer(@Nullable Bundle savedInstanceState){
        final ContentFrameLayout container = new ContentFrameLayout(this);


        container.setId(R.id.delegate_container);
        setContentView(container);

        if(savedInstanceState == null){
            loadRootFragment(R.id.delegate_container, setRootDelegate());
        }
    }

    /**
     * 由于是单Activity的应用，那么当这个Activity退出的时候，实际上应用也退出了. 因此在这里可以做垃圾回收
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 执行垃圾回收
        System.gc();
        System.runFinalization();
    }
}
