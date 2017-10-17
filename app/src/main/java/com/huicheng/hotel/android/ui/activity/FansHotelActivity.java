package com.huicheng.hotel.android.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.net.bean.FansHotelInfoBean;
import com.huicheng.hotel.android.tools.CityParseUtils;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.CustomCardStackViewPager;
import com.huicheng.hotel.android.ui.custom.RoundedAllImageView;
import com.huicheng.hotel.android.ui.custom.VerticalStackTransformer;
import com.huicheng.hotel.android.ui.glide.CustomReqURLFormatModelImpl;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.Utils;

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
    private TextView tv_count;
    private List<FansHotelInfoBean> fanHotelList = new ArrayList<>();
    private int positionIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_fanshotel_layout);
        initViews();
        initParams();
        initListeners();
        if (null == savedInstanceState) {
            requestVipHotelInfo();
        }
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
        tv_center_title.setText(getString(R.string.side_fans));
        viewPager.setOrientation(CustomCardStackViewPager.Orientation.VERTICAL);
        viewPager.setPageTransformer(true, new VerticalStackTransformer(this));

        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        llp.height = Utils.mScreenHeight - Utils.dip2px(45) - Utils.dip2px(150) - Utils.dip2px(40);
        llp.width = (int) ((float) llp.height / 7 * 5.5f);
        viewPager.setLayoutParams(llp);
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
                int newPosition = position % fanHotelList.size();
                tv_count.setText(newPosition + 1 + " / " + fanHotelList.size());
                if (positionIndex != newPosition) {
                    positionIndex = newPosition;
                }
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
                HotelOrderManager.getInstance().reset();
                Intent intent = new Intent(FansHotelActivity.this, MainActivity.class);
                intent.putExtra("isClosed", true);
                intent.putExtra("index", 0);
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
            viewPager.setAdapter(new MyPageAdapter(this, fanHotelList));
            tv_count.setText(1 + " / " + fanHotelList.size());

            //viewPager一个假的无限循环，初始位置是viewPager count的100倍
            viewPager.setCurrentItem(fanHotelList.size() * 100);
            viewPager.setOffscreenPageLimit(fanHotelList.size());
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

    @Override
    public void onNotifyError(ResponseData request) {
        super.onNotifyError(request);
        removeProgressDialog();
        refreshFansHotelLayout();
    }

    private class MyPageAdapter extends PagerAdapter {
        private Context context;
        private List<FansHotelInfoBean> fansList;

        public MyPageAdapter(Context context, List<FansHotelInfoBean> fansList) {
            this.context = context;
            this.fansList = fansList;
        }

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }

        @Override
        public Object instantiateItem(final ViewGroup container, int position) {
            position %= fansList.size();
            if (position < 0) {
                position = fansList.size() + position;
            }
            final FansHotelInfoBean bean = fansList.get(position);
            final View view = LayoutInflater.from(context).inflate(R.layout.vp_fanshotel_item, null);
            final RoundedAllImageView iv_background = (RoundedAllImageView) view.findViewById(R.id.iv_background);
            final TextView tv_name = (TextView) view.findViewById(R.id.tv_name);
            tv_name.getPaint().setFakeBoldText(true);
            final TextView tv_loc = (TextView) view.findViewById(R.id.tv_loc);
            final TextView tv_order = (TextView) view.findViewById(R.id.tv_order);
            tv_order.getPaint().setFakeBoldText(true);
            Glide.with(context)
                    .load(new CustomReqURLFormatModelImpl(bean.featurePicPath))
                    .placeholder(R.drawable.def_fans)
                    .crossFade()
                    .override(500, 700)
                    .into(iv_background);
            tv_name.setText(bean.name);
            tv_loc.setText(CityParseUtils.getProvinceCityString(bean.provinceName, bean.cityName, " "));
            tv_order.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HotelOrderManager.getInstance().reset();
                    HotelOrderManager.getInstance().setFansHotelInfoBean(bean);
                    HotelOrderManager.getInstance().setCityStr(CityParseUtils.getProvinceCityString(bean.provinceName, bean.cityName, "-"));
                    Intent intent = new Intent(context, HotelCalendarChooseActivity.class);
                    intent.putExtra("isForbidTitleClick", true);
                    intent.putExtra("isFansBooking", true);
                    startActivity(intent);
                }
            });

            //如果View已经在之前添加到了一个父组件，则必须先remove，否则会抛出IllegalStateException。
            ViewParent vp = view.getParent();
            if (vp != null) {
                ViewGroup parent = (ViewGroup) vp;
                parent.removeView(view);
            }
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
        }
    }
}
