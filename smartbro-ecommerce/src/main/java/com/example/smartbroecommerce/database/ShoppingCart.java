package com.example.smartbroecommerce.database;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.smartbro.app.Smartbro;
import com.example.smartbro.delegates.BaseDelegate;
import com.example.smartbroecommerce.R;
import com.example.smartbroecommerce.utils.BetterToast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Justin Wang from SmartBro on 12/12/17.
 * 购物车类
 */

public class ShoppingCart {

    private List<ShoppingCartItem> shoppingCartItems = null;
    // 操作cart item的dao
    private static final ShoppingCartItemDao SHOPPING_CART_ITEM_DAO = DatabaseManager.getInstance().getShoppingCartItemDao();
    private static final ProductDao PRODUCT_DAO = DatabaseManager.getInstance().getProductDao();
    // 购物车的产品容量
    private static final int VOLUME = MachineProfile.getShoppingCartVolumn();

    private ShoppingCart(){
        // 加载购物车的数据
        this.loadCartItems();
    }

    /**
     * 简单工厂方法, 创建一个特定容量的购物车. 容量是指产品的数量，也是连续烤Pizza的最大值
     * @return ShoppingCart
     */
    public static ShoppingCart getInstance(){
        return Holder.INSTANCE;
    }

    /**
     * 静态内部类，实现懒汉模式
     */
    private static class Holder{
        private static final ShoppingCart INSTANCE = new ShoppingCart();
    }

    /**
     * 加载所有的购物车项目
     */
    private void loadCartItems(){
        // 把所有当前的购物项都取出来
        final List<ShoppingCartItem> list = SHOPPING_CART_ITEM_DAO.loadAll();
        if(list.size() > 0){
            this.shoppingCartItems = list;
        }else{
            this.shoppingCartItems = new ArrayList<>();
        }
    }

    /**
     * 获取当前有多少个Items
     * @return int
     */
    private int getItemsCount(){
        if(this.shoppingCartItems == null){
            this.shoppingCartItems = new ArrayList<>();
        }
        return this.shoppingCartItems.size();
    }

    /**
     * 获取当前购物车中有多少产品
     * @return int
     */
    public int getProductsCount(){
        int count = 0;
        if(this.shoppingCartItems != null){
            for (ShoppingCartItem item : this.shoppingCartItems){
                count = count + item.getQuantity();
            }
        }
        return count;
    }

    /**
     * 检查购物车是不是已经满了
     * @return boolean
     */
    public boolean hasMoreSpace(){
        return this.getProductsCount() < VOLUME;
    }

    /**
     * 检查购物车是否为空
     * @return boolean
     */
    public boolean isEmpty(){
        return this.getItemsCount() == 0 || this.getProductsCount() == 0;
    }

    /**
     * 清空购物车的方法
     */
    public void clear(){
        // 数据库表格要清理
        // 1: 把购物车中的position全部先恢复为有效
        if(shoppingCartItems != null){
            for (ShoppingCartItem item: shoppingCartItems) {
                if(item.getPositions() != null){
                    for (Position position: item.getPositions()) {
                        position.enable();
                    }
                }
            }
        }

        // 2：清空cart items
        ShoppingCartItem.flush();
        this.shoppingCartItems = null;
    }

    /**
     * 订单完成之后, 清空购物车的方法
     */
    public void allDone(){
        // 数据库表格要清理
        // 1: 把购物车中的position全部先恢复为有效
        if(shoppingCartItems != null){
            for (ShoppingCartItem item: shoppingCartItems) {
                if(item.getPositions() != null){
                    for (Position position: item.getPositions()) {
                        position.disable();
                    }
                }
            }
        }
        // 2：清空cart items
        ShoppingCartItem.flush();
        this.shoppingCartItems = null;
    }

    /**
     * 计算购物车总计金额
     * @return
     */
    public double getTotal(){
        double total = 0;
        for (ShoppingCartItem item: this.shoppingCartItems) {
            total += item.getProductPrice() * item.getQuantity();
        }
        return total;
    }

    /**
     * 获取当前购物车中所有的 item 列表
     * @return List<ShoppingCartItem>
     */
    public List<ShoppingCartItem> getShoppingCartItems(){
        return shoppingCartItems;
    }

    /**
     * 获取购物车中的产品
     * @return List<Product>
     */
    public List<Product> getProducts(){
        final int size = this.getItemsCount();
        final List<Product> products = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            final ShoppingCartItem item = this.getShoppingCartItems().get(i);
            products.add(Product.find(item.getProductId()));
        }

        return products;
    }

    /**
     * 获取购物车中的位置信息
     * @return
     */
    public List<Position> getPositions(){
        final int size = this.getItemsCount();
        final List<Position> positions = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            final ShoppingCartItem item = this.getShoppingCartItems().get(i);

            final List<Position> ps = item.getPositions();
            for (Position p: ps ) {
                // 确认Position没有被重复添加
                if(!this.isPositionExist(p, positions)){
                    positions.add(p);
                }
            }
        }
        return positions;
    }

    /**
     * 检查给定的position是否在给定的positions中存在
     * @param p
     * @param ps
     * @return
     */
    private boolean isPositionExist(Position p, List<Position> ps){
        boolean exist = false;
        for (Position pos : ps) {
            if(pos.getIndex() == p.getIndex()){
                exist = true;
                break;
            }
        }
        return exist;
    }

    /**
     * 将给定的产品添加到购物车中
     * @param product   添加的产品对象
     * @param delegate  所归属的Fragment
     * @return int 当前购物车中产品的数量(注意，不是Item的数量）
     */
    public int addProduct(@NonNull Product product, BaseDelegate delegate){
        // 先检查是否购物车已经满了
        if(this.hasMoreSpace()){
            // 在检查是否有库存
            Position position = Position.getPositionByProduct(product);
            if(position != null){
                // 表示找到一个库存
                final int index = this.isProductExist(product);
                if(this.isEmpty() || index < 0){
                    // 购物车是空的, 或者给定产品不在购物车中
                    this.shoppingCartItems.add(this.saveProductInNewCartItem(product, position));
                }else{
                    // 购物车不是空的, 并且该产品已经在购物车中
                    this.updateCartItemQuantity(index, 1, position);
                }
            }else {
                // 已经没有库存了, 提示一下用户
                if(delegate != null){
                    BetterToast.getInstance().showText(
                            delegate.getActivity(),
                            product.getName()+ " " + Smartbro.getApplication().getString(R.string.msg_product_out_of_stock)
                    );
                }
            }
        }else {
            // 购物车已经满了, 提示一下用户
            if(delegate != null){
                BetterToast.getInstance().showText(
                        delegate.getActivity(),
                        Smartbro.getApplication().getString(R.string.msg_cart_is_full));
            }
        }
        return this.getProductsCount();
    }

    /**
     * 从购物项里释放一个空间位置 Position, 同时也更新了item的当前quantity
     * @param cartItemId
     */
    public void removeProductFrom(long cartItemId){
        final ShoppingCartItem item = ShoppingCartItem.findById(cartItemId);
        if(item != null){
            // 购物项找到了
            item.releaseAPosition();
            item.setQuantity(item.getQuantity() -1 );
            SHOPPING_CART_ITEM_DAO.update(item);
        }
        else {
            throw new NullPointerException("无法从空的购物项里移除产品");
        }
    }

    /**
     * 将给定的产品添加到购物车中
     * @param productId 产品的ID
     * @param delegate 所显示的Fragment, 为了显示提示信息用
     */
    public int addProduct(long productId, BaseDelegate delegate){
        // 先检查产品是否存在
        Product product = Product.find(productId);
        if(product != null){
            return this.addProduct(product, delegate);
        }else {
            throw new NullPointerException("视图往购物车里加一个空的产品");
        }
    }

    /**
     * 更新给定index的cart item的数量, 变化值为quantity, 可正可负.
     * @param index 购物项在购物车中的索引
     * @param quantity 新添的数量增量
     * @param position 所在的位置
     */
    public void updateCartItemQuantity(int index, int quantity,@NonNull Position position){
        ShoppingCartItem existedItem = this.shoppingCartItems.get(index);
        existedItem.setQuantity(existedItem.getQuantity() + quantity);
        SHOPPING_CART_ITEM_DAO.update(existedItem);
        // 占用给定的位置
        existedItem.takePosition(position);
    }

    /**
     * 将给定的产品保存到一个新的Item中
     * @param product 需要添加的产品
     * @return ShoppingCartItem
     */
    private ShoppingCartItem saveProductInNewCartItem(Product product,@NonNull Position position){
        final ShoppingCartItem newItem = new ShoppingCartItem();
        newItem.setProductId(product.getId());
        newItem.setProductImage(product.getMainImageUrl());
        newItem.setProductName(product.getName());
        newItem.setProductPrice(product.getPrice());
        newItem.setQuantity(1);
        // 使用时间戳毫秒数作为item的id
        newItem.setId(new Date().getTime());
        SHOPPING_CART_ITEM_DAO.insert(newItem);

        // 占用给定的位置
        newItem.takePosition(position);
        return newItem;
    }

    /**
     * 检查是否该产品已经在购物车中, 如果存在，返回item的index; 否则返回 -1
     * @param product
     * @return int
     */
    private int isProductExist(Product product){
        int itemIndex = -1;
        final int size = this.getItemsCount();
//        Log.i("Cart Items count", Integer.toString(size));
//        Log.i("Product Id", Long.toString(product.getId()));

        for (int i = 0; i < size; i++) {
            final ShoppingCartItem entity = this.shoppingCartItems.get(i);
//            Log.i("Entity Id", Long.toString(entity.getProductId()));
//            Log.i("Result", Boolean.toString(entity.getProductId() == product.getId()));
            if(entity.getProductId() == product.getId()){
                // 给定的产品已经在购物车中
                itemIndex = i;
                break;
            }
        }
//        Log.i("Cart Items count", Integer.toString(itemIndex));
        return itemIndex;
    }

    /**
     * 从购物车中移除整个购物项
     * @param cartItemId
     */
    public void removeCartItem(long cartItemId){
        final int size = this.getItemsCount();
        int indexToBeRemoved = -1;
        for (int i = 0; i < size; i++) {
            final ShoppingCartItem item = this.shoppingCartItems.get(i);
            if(item.getId() == cartItemId){
                indexToBeRemoved = i;
                break;
            }
        }
        if(indexToBeRemoved > -1){
            this.shoppingCartItems.remove(indexToBeRemoved);
            ShoppingCartItem.terminate(cartItemId);
        }
    }

    /**
     * 从购物车中移除整个购物项
     * @param cartItemId
     */
    public void removeCartItemFinal(long cartItemId){
        final int size = this.getItemsCount();
        int indexToBeRemoved = -1;
        for (int i = 0; i < size; i++) {
            final ShoppingCartItem item = this.shoppingCartItems.get(i);
            if(item.getId() == cartItemId){
                indexToBeRemoved = i;
                break;
            }
        }
        if(indexToBeRemoved > -1){
            this.shoppingCartItems.remove(indexToBeRemoved);
            ShoppingCartItem.terminateCartAndPosition(cartItemId);
        }
    }
}
