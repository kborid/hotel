package com.huicheng.hotel.android.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.net.bean.FansHotelInfoBean;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.CustomCardStackViewPager;
import com.huicheng.hotel.android.ui.custom.VerticalStackTransformer;
import com.huicheng.hotel.android.ui.fragment.CardFragment;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kborid
 * @date 2016/12/20 0020
 */
public class FansHotelActivity extends BaseActivity {

    private LinearLayout no_fans_lay, has_fans_lay;
    private Button btn_booking;
    private CustomCardStackViewPager viewPager;
    private List<Fragment> fragments = new ArrayList<>();
    private TextView tv_count;
    private List<FansHotelInfoBean> fanHotelList = new ArrayList<>();
    private MyFragmentPageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_fanshotel_layout);

        initViews();
        initParams();
        initListeners();
        requestVipHotelInfo();
    }

    @Override
    public void initViews() {
        super.initViews();
        no_fans_lay = (LinearLayout) findViewById(R.id.no_fans_hotel);
        btn_booking = (Button) findViewById(R.id.btn_booking);
        has_fans_lay = (LinearLayout) findViewById(R.id.has_fans_hotel);
        viewPager = (CustomCardStackViewPager) findViewById(R.id.view_pager);
        tv_count = (TextView) findViewById(R.id.tv_count);
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText("粉丝酒店");
        viewPager.setOrientation(CustomCardStackViewPager.Orientation.VERTICAL);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setPageTransformer(true, new VerticalStackTransformer(this));
        adapter = new MyFragmentPageAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
    }

    @Override
    public void initListeners() {
        super.initListeners();
        btn_booking.setOnClickListener(this);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tv_count.setText(position + 1 + " / " + fragments.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btn_booking:
                Intent intent = new Intent(FansHotelActivity.this, HotelCalendarChooseActivity.class);
                HotelOrderManager.getInstance().reset();
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    private void requestVipHotelInfo() {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.VIP_HOTEL;
        d.flag = AppConst.VIP_HOTEL;
        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void refreshFansHotelLayout() {
        if (fanHotelList != null && fanHotelList.size() > 0) {
            no_fans_lay.setVisibility(View.GONE);
            has_fans_lay.setVisibility(View.VISIBLE);
            for (int i = 0; i < fanHotelList.size(); i++) {
                fragments.add(CardFragment.newInstance(i, fanHotelList.get(i)));
            }
            tv_count.setText(1 + " / " + fanHotelList.size());
            adapter.notifyDataSetChanged();
        } else {
            no_fans_lay.setVisibility(View.VISIBLE);
            has_fans_lay.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void preExecute(ResponseData request) {

    }

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.VIP_HOTEL) {
                removeProgressDialog();
                List<FansHotelInfoBean> temp = JSON.parseArray(response.body.toString(), FansHotelInfoBean.class);
                if (temp != null && temp.size() > 0) {
                    fanHotelList.clear();
                    fanHotelList.addAll(temp);
                }
                refreshFansHotelLayout();
            }
        }
    }

    private class MyFragmentPageAdapter extends FragmentPagerAdapter {

        public MyFragmentPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int arg0) {
            return fragments.get(arg0);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }
}
