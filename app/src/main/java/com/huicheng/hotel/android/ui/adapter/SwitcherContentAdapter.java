package com.huicheng.hotel.android.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.huicheng.hotel.android.ui.fragment.FragmentSwitcherHotel;
import com.huicheng.hotel.android.ui.fragment.FragmentSwitcherOrder;
import com.huicheng.hotel.android.ui.fragment.FragmentSwitcherPlane;
import com.huicheng.hotel.android.ui.listener.MainScreenCallback;

/**
 * @author kborid
 * @date 2018/2/9 0009.
 */

public class SwitcherContentAdapter extends FragmentPagerAdapter {

    private MainScreenCallback callback = null;
    private String[] title;

    public SwitcherContentAdapter(FragmentManager fm, String[] title, MainScreenCallback callback) {
        super(fm);
        this.title = title;
        this.callback = callback;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return FragmentSwitcherHotel.newInstance(callback);
            case 1:
                return FragmentSwitcherPlane.newInstance();
            case 2:
                return FragmentSwitcherOrder.newInstance();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return title.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return title[position];
    }
}
