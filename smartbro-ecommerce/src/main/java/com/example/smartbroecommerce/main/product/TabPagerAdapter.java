package com.example.smartbroecommerce.main.product;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.smartbroecommerce.database.Product;

import java.util.ArrayList;

/**
 * Created by Justin Wang from SmartBro on 14/12/17.
 * 继承 FragmentStatePagerAdapter 的原因是在商品详情销毁之后，这个pager也会被销毁，适用pizza机的情况
 */

public class TabPagerAdapter extends FragmentStatePagerAdapter {

    private final ArrayList<String> TAB_TITLES = new ArrayList<>();
    private final ArrayList<ArrayList<String>> PICTURES = new ArrayList<>();

    public TabPagerAdapter(FragmentManager fm, Product product) {
        super(fm);
        ArrayList<String> tabNames = product.getTabNames();
        ArrayList<ArrayList<Object>> content = product.getTabContent();

        if(tabNames != null){
            int size = tabNames.size();
            for (int i = 0; i < size; i++) {
                TAB_TITLES.add(tabNames.get(i));
                ArrayList<String> tabContent = new ArrayList<>();
                for (Object object: content.get(i)) {
                    tabContent.add((String) object);
                }
                PICTURES.add(tabContent);
            }
        }
    }

    @Override
    public Fragment getItem(int position) {
        // 在这里使用 ImageDelegate 显示tab中的信息（实际就是一些图片)
        if(PICTURES.size() > 0){
            return ImageDelegate.create(PICTURES.get(position));
        }
        return null;
    }

    /**
     * 返回有几个tab
     * @return int
     */
    @Override
    public int getCount() {
        // 如果真的有tab的时候，必须确保这里传回去正确的值
        return TAB_TITLES.size();
    }

    /**
     * 这个方法返回的是Tab的title
     * @param position
     * @return CharSequence
     */
    @Override
    public CharSequence getPageTitle(int position) {
        if(TAB_TITLES.size() > 0){
            return TAB_TITLES.get(position);
        }
        return "";
    }
}
