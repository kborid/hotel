package com.huicheng.hotel.android.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.huicheng.hotel.android.ui.fragment.FragmentSwitcherHotel;
import com.huicheng.hotel.android.ui.fragment.FragmentSwitcherPlane;

/**
 * @author kborid
 * @date 2018/2/9 0009.
 */

public class SwitcherContentAdapter extends FragmentPagerAdapter {

    private String[] mTitle;

    public SwitcherContentAdapter(FragmentManager fm, String[] title) {
        super(fm);
        mTitle = title;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return FragmentSwitcherHotel.newInstance();
            case 1:
                return FragmentSwitcherPlane.newInstance();
            case 2:
                return FragmentSwitcherPlane.newInstance();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mTitle.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitle[position];
    }
}
