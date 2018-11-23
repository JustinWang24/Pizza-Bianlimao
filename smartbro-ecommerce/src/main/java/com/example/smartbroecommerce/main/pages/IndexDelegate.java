package com.example.smartbroecommerce.main.pages;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.example.smartbro.delegates.bottom.BottomItemDelegate;
import com.example.smartbro.net.RestfulClient;
import com.example.smartbro.net.callback.ISuccess;
import com.example.smartbro.ui.recycler.MultipleFields;
import com.example.smartbro.ui.recycler.MultipleItemEntity;
import com.example.smartbro.ui.refresh.PagingBean;
import com.example.smartbro.ui.refresh.RefreshHandler;
import com.example.smartbroecommerce.R;
import com.example.smartbroecommerce.R2;
import com.example.smartbroecommerce.main.converters.IndexDataConverter;
import com.joanzapata.iconify.widget.IconTextView;

import java.util.ArrayList;
import butterknife.BindView;

/**
 * Created by Justin Wang from SmartBro on 9/12/17.
 */

public class IndexDelegate extends BottomItemDelegate {

    @BindView(R2.id.rv_index)
    RecyclerView recyclerView = null;
    @BindView(R2.id.srl_index)
    SwipeRefreshLayout mRefreshLayout = null;
    @BindView(R2.id.tb_index)
    Toolbar toolbar = null;
    @BindView(R2.id.icon_index_scan)
    IconTextView mIconScan = null;
    @BindView(R2.id.et_search_view)
    AppCompatEditText mSearchView = null;

    private RefreshHandler mRefreshHandler = null;



    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
        this.initRefreshLayout();
        this.initRecyclerView();
    }

    @Override
    public Object setLayout() {
        return R.layout.delegate_page_index;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        this.mRefreshHandler = RefreshHandler.create(
                mRefreshLayout,
                recyclerView,
                new IndexDataConverter(),
                new PagingBean()
        );

        // 测试
        RestfulClient.builder()
            .url("http://rap.taobao.org/mockjsdata/20889/api/machines/init")
            .success(new ISuccess() {
                @Override
                public void onSuccess(String response) {
                    final IndexDataConverter converter = new IndexDataConverter();
                    converter.setJsonData(response);
                    final ArrayList<MultipleItemEntity> entities = converter.convert();
                    final String url = entities.get(0).getField(MultipleFields.IMAGE_URL);
                    Toast.makeText(getContext(),url,Toast.LENGTH_SHORT).show();
                }
            })
            .build()
            .get();
    }

    /**
     * 初始化 RefreshLayout 布局
     */
    private void initRefreshLayout(){
        this.mRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );

        // Todo 这个是定义刷新的效果的，需要研究一下
        this.mRefreshLayout.setProgressViewOffset(true, 120, 300);
    }

    /**
     * 初始化 RecyclerView
     */
    private void initRecyclerView(){
        final GridLayoutManager manager = new GridLayoutManager(getContext(), 4);
        this.recyclerView.setLayoutManager(manager);
    }
}
