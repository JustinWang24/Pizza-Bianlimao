package com.example.smartbroecommerce.main.stock;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.smartbro.delegates.SmartbroDelegate;
import com.example.smartbro.net.RestfulClient;
import com.example.smartbro.net.callback.IError;
import com.example.smartbro.net.callback.IFailure;
import com.example.smartbro.net.callback.ISuccess;
import com.example.smartbro.ui.recycler.DataConvertor;
import com.example.smartbro.ui.recycler.decorators.BaseDecoration;
import com.example.smartbroecommerce.Auth.MachineInitHandler;
import com.example.smartbroecommerce.R;
import com.example.smartbroecommerce.R2;
import com.example.smartbroecommerce.database.DatabaseManager;
import com.example.smartbroecommerce.database.MachineProfile;
import com.example.smartbroecommerce.database.Position;
import com.example.smartbroecommerce.database.PositionDao;
import com.example.smartbroecommerce.main.converters.PositionsListDataConverter;
import com.example.smartbroecommerce.main.product.ListDelegate;
import com.example.smartbroecommerce.utils.BetterToast;
import com.taihua.pishamachine.MicroLightScanner.Tx200Client;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Justin Wang from SmartBro on 29/12/17.
 */

public class StockManagerDelegate extends SmartbroDelegate {
    private boolean isStockUpdated = false;
    private WeakHashMap<String,Object> changedPositions = new WeakHashMap<String,Object>();

    private DataConvertor converter = null;
    private PositionListAdaptor adaptor = null;

    /**
     * 是否为开发模式
     */
    private boolean isDevMode = false;

    @BindView(R2.id.rv_positions_listing)
    RecyclerView positionsRecyclerView = null;
    @BindView(R2.id.rl_start_button_wrap)
    RelativeLayout btnStart = null;
    @BindView(R2.id.rl_save_button_wrap)
    RelativeLayout btnSyncStock = null;

    /**
     * 跳转到应用程序的入口
     */
    @OnClick(R2.id.rl_start_button_wrap)
    public void onBtnStartClick(){
        if(this.isStockUpdated && this.changedPositions.size() > 0){
            BetterToast.getInstance().showText(getActivity(),getString(R.string.msg_need_upload_positions));
        }else {
            // 使能扫描枪
            try {
                Tx200Client.getClientInstance().setMode(false);
                Tx200Client.getClientInstance().connect();
                startWithPop(new ListDelegate());
            }catch (Exception e){
                BetterToast.getInstance().showText(
                        getProxyActivity(),
                        "设备串口连接失败"
                );
                if(this.isDevMode){
                    startWithPop(new ListDelegate());
                }
            }
        }
    }

    /**
     * 如果在上位机上修改了库存, 则同步到服务器
     */
    @OnClick(R2.id.rl_save_button_wrap)
    public void onBtnSyncClick(){
        if(this.isStockUpdated && this.changedPositions.size() > 0){
            StringBuilder positions = new StringBuilder();

            for (Map.Entry entry : changedPositions.entrySet()){
                Boolean status = (Boolean) entry.getValue();
                positions.append(entry.getKey())
                        .append(":")
                        .append(Boolean.toString(status))
                        .append(",");
            }

            // 更新到服务器
            RestfulClient.builder()
                    .url("machines/update_stock")
                    .params("muuid",MachineProfile.getInstance().getUuid())
                    .params("positions", positions.toString())
                    .success(new ISuccess() {
                        @Override
                        public void onSuccess(String response) {

                            for (Map.Entry entry : changedPositions.entrySet()){
                                boolean status = (boolean) entry.getValue();
                                Position p = Position.findByIndex(Integer.parseInt(entry.getKey().toString()));
                                if(status){
                                    p.enableAndGive48Hours();
                                }else {
                                    p.disable();
                                }
                            }
                            isStockUpdated = false;
                            changedPositions.clear();
                            BetterToast.getInstance().showText(getActivity(),getString(R.string.msg_done_upload_positions));
                        }
                    })
                    .error(new IError() {
                        @Override
                        public void onError(int code, String msg) {
                            BetterToast.getInstance().showText(getActivity(),msg);
                        }
                    })
                    .failure(new IFailure() {
                        @Override
                        public void onFailure() {
                            Log.i("here","failure");
                        }
                    })
                    .build().post();
        }else {
            this.changedPositions.clear();
            this.isStockUpdated = false;
        }
    }

    @Override
    public Object setLayout() {
        return R.layout.delegate_stock_manager;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        // 强制收起软键盘
        final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(rootView.getWindowToken(),0);

        final Bundle args = getArguments();


        if(args.getBoolean("refreshMachineData")){
            // 需要强制的去服务器进行数据采集与更新
            final String machineSerialNumber = MachineProfile.getInstance().getSerialName();

            RestfulClient.builder()
                    .url("machines/init")
                    .params("serial_number",machineSerialNumber)
                    .success(new ISuccess() {
                        @Override
                        public void onSuccess(String response) {
                            // 设备初始化成功, 把服务器返回结果与认证的监听类对象传给handler去处理
                            MachineInitHandler.initMachine(response);
                            renderView();
                        }
                    })
                    .failure(new IFailure() {
                        @Override
                        public void onFailure() {
                            // 设备初始化失败了, 显示提示信息
                            Toast.makeText(
                                    getContext(),
                                    getString(R.string.err_text_init_failed),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    })
                    .build()
                    .post();
        }else {
            // 不需要联系服务器, 这种情况，只有在刚刚进行完设备初始化以后才会出现
            renderView();
        }
    }

    private void renderView(){
        this.converter = new PositionsListDataConverter(this);
        final PositionDao positionDao = DatabaseManager.getInstance().getPositionDao();
        final List<Position> positions = positionDao.queryBuilder().list();

        this.adaptor = PositionListAdaptor.
                create(this.converter.setObjectData(positions));
        this.adaptor.setOwnDelegate(this);
        this.positionsRecyclerView.setAdapter(this.adaptor);

        final GridLayoutManager manager = new GridLayoutManager(getContext(), 6);
        this.positionsRecyclerView.setLayoutManager(manager);
        // 添加适当的分割线
        this.positionsRecyclerView.addItemDecoration(
                BaseDecoration.create(
                        ContextCompat.getColor(getContext(), R.color.helpbg),
                        1
                )
        );

    }

    /**
     * 设置是否库存被更改的状态
     * @param status
     */
    public void setStockUpdated(boolean status){
        this.isStockUpdated = status;
    }

    /**
     * 把所有的修改的位置信息进行保存
     * @param index
     * @param newStatus
     */
    public void updateChangedPositions(String index, Boolean newStatus){
        this.changedPositions.put(index, newStatus);
    }
}
