package com.example.smartbro.delegates;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.smartbro.activities.ProxyActivity;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment;

/**
 * Created by Justin Wang from SmartBro on 2/12/17.
 */

public abstract class BaseDelegate extends SwipeBackFragment{

    // 强制子类实现的方法
    public abstract Object setLayout();
    public abstract void onBindView(@Nullable Bundle savedInstanceState, View rootView);

    // 使用ButterKnife的解绑
    @SuppressWarnings("SpellCheckingInspection")
    private Unbinder mUnbinder = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView;

        if(setLayout() instanceof Integer){
            rootView = inflater.inflate(
                    (Integer)setLayout(),
                    container,
                    false   // 没有父布局
            );
        }else if (setLayout() instanceof View){
            rootView = (View) setLayout();
        }else{
            throw new ClassCastException("BaseDelegate onCreateView setLayout() type must be int or View!");
        }

        if(rootView != null){
            mUnbinder = ButterKnife.bind(this, rootView);
            onBindView(savedInstanceState,rootView);
        }

        return rootView;
    }

    /**
     * 获取Proxy Activity的实例
     * @return ProxyActivity
     */
    public final ProxyActivity getProxyActivity(){
        return (ProxyActivity) _mActivity;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if(mUnbinder != null){
            mUnbinder.unbind();
        }
    }
}
