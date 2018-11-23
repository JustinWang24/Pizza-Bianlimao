package com.example.smartbro.ui.recycler;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;

/**
 * Created by Justin Wang from SmartBro on 9/12/17.
 */

public class MultipleItemEntity implements MultiItemEntity {

    /*
        数据转换层
        Todo 研究 ReferenceQueue, SoftReference的概念等
     */
    private final ReferenceQueue<LinkedHashMap<Object,Object>> ITEM_QUEUE
            = new ReferenceQueue<>();
    private final LinkedHashMap<Object, Object> MULTIPLE_FIELDS
            = new LinkedHashMap<>();
    private final SoftReference<LinkedHashMap<Object,Object>> FIELD_REFERENCE
            = new SoftReference<LinkedHashMap<Object,Object>>(MULTIPLE_FIELDS, ITEM_QUEUE);

    /**
     * 构造函数
     * @param fields
     */
    public MultipleItemEntity(LinkedHashMap<Object, Object> fields) {
        FIELD_REFERENCE.get().putAll(fields);
    }

    public static MultipleItemEntityBuilder builder(){
        return new MultipleItemEntityBuilder();
    }

    /**
     * 插入数据的方法, 可链式调用
     * @param key
     * @param value
     * @return MultipleItemEntity
     */
    public final MultipleItemEntity setField(Object key, Object value){
        FIELD_REFERENCE.get().put(key, value);
        return this;
    }

    /**
     * 根据key获取field
     * @param key
     * @param <T>
     * @return <T> T
     */
    @SuppressWarnings("unchecked")
    public final <T> T getField(Object key){
        return (T) getFields().get(key);
    }

    /**
     * 获取所有的fields
     * @return
     */
    public final LinkedHashMap<?,?> getFields(){
        return FIELD_REFERENCE.get();
    }

    /**
     * 为了控制每个Item的样式和表现特征
     * @return int
     */
    @Override
    public int getItemType() {
        return (int) FIELD_REFERENCE.get()      // 这个 get() 实际上是返回了 MULTIPLE_FIELDS
                .get(MultipleFields.ITEM_TYPE);
    }
}
