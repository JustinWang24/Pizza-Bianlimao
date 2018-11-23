package com.example.smartbro.ui.refresh;

import android.support.v4.widget.SwipeRefreshLayout;

import com.example.smartbro.app.Smartbro;
import com.example.smartbro.ui.recycler.DataConvertor;
import com.example.smartbro.ui.recycler.MultipleRecyclerAdaptor;

import android.os.Handler;
import android.support.v7.widget.RecyclerView;

/**
 * Created by Justin Wang from SmartBro on 9/12/17.
 * 在页面刷新的时候，处理刷新效果的类
 * converter
 */

public class RefreshHandler implements SwipeRefreshLayout.OnRefreshListener{

    private final SwipeRefreshLayout REFRESH_LAYOUT;
    private final PagingBean PAGINATION_BEAN;
    private final RecyclerView RECYCLER_VIEW;
    private MultipleRecyclerAdaptor multipleRecyclerAdaptor = null;
    private final DataConvertor CONVERTOR;

    private RefreshHandler(
            SwipeRefreshLayout refreshLayout, RecyclerView recyclerView, DataConvertor dataConvertor, PagingBean pagingBean) {
        REFRESH_LAYOUT = refreshLayout;
        this.PAGINATION_BEAN = pagingBean;
        this.RECYCLER_VIEW = recyclerView;
        this.CONVERTOR = dataConvertor;
        REFRESH_LAYOUT.setOnRefreshListener(this);
    }

    /**
     * 简单的工厂方法
     * @param refreshLayout
     * @param recyclerView
     * @param dataConvertor
     * @param pagingBean
     * @return
     */
    public static RefreshHandler create(
            SwipeRefreshLayout refreshLayout,
            RecyclerView recyclerView,
            DataConvertor dataConvertor,
            PagingBean pagingBean){
        return new RefreshHandler(refreshLayout,recyclerView,dataConvertor,pagingBean);
    }

    /**
     * 真正实现refresh效果的方法
     * Todo 具体的效果，再仔细研究
     */
    private void refresh(){
        REFRESH_LAYOUT.setRefreshing(true);
        final Handler handler = Smartbro.getHandler();

        if(handler != null){
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Todo 进行需要的网络请求
                    REFRESH_LAYOUT.setRefreshing(false);
                }
            }, 2000);
        }
    }

    @Override
    public void onRefresh() {
        this.refresh();
    }
}
