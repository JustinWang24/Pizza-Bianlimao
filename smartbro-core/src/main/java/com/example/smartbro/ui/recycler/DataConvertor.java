package com.example.smartbro.ui.recycler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Justin Wang from SmartBro on 9/12/17.
 * 数据转换的约束
 */

public abstract class DataConvertor {
    protected final ArrayList<MultipleItemEntity> ENTITIES = new ArrayList<>();

    // 保存Json类型的数据
    private String mJsonData = null;

    // 保存非Json类型的数据
    private List<?> mObjectData = null;

    public abstract ArrayList<MultipleItemEntity> convert();

    public DataConvertor setJsonData(String jsonData){
        this.mJsonData = jsonData;
        return this;
    }

    public String getJsonData(){
        if(this.mJsonData == null || this.mJsonData.isEmpty()){
            throw new NullPointerException("DataConvertor Class -> JSON data is null");
        }
        return this.mJsonData;
    }

    // 以下是配合对于非json类型的数据的 Getter 和 Setter
    public DataConvertor setObjectData(List<?> data){
        this.mObjectData = data;
        return this;
    }

    public List<?> getObjectData(){
        return this.mObjectData;
    }
}
