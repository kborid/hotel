package com.huicheng.hotel.android.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.net.bean.CouponInfoBean;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.CommonAssessStarsLayout;
import com.huicheng.hotel.android.ui.custom.RoundedAllImageView;
import com.huicheng.hotel.android.ui.glide.CustomReqURLFormatModelImpl;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kborid
 * @date 2016/12/8 0008
 * @modify 2017/02/20
 */
public class MyDiscountCouponActivity extends BaseActivity {

    private LinearLayout noDiscountLayout, hasDiscountLayout, active_lay;
    private TextView tv_no_coupon_note, tv_no_coupon_time;
    private TextView tv_summary;

    private RelativeLayout viewPagerContainer;
    private ViewPager viewPager;
    private LinearLayout indicator_lay;
    private int positionIndex = 0;
    private CouponInfoBean couponInfoBean = null;
    private Button btn_use;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_mydiscount_layout);
        initViews();
        initParams();
        initListeners();
        if (null == savedInstanceState) {
            requestCouponInfo();
        }
    }

    @Override
    public void initViews() {
        super.initViews();
        noDiscountLayout = (LinearLayout) findViewById(R.id.no_discount_lay);
        tv_no_coupon_note = (TextView) findViewById(R.id.tv_no_coupon_note);
        active_lay = (LinearLayout) findViewById(R.id.active_lay);
        tv_no_coupon_time = (TextView) findViewById(R.id.tv_no_coupon_time);
        hasDiscountLayout = (LinearLayout) findViewById(R.id.has_discount_lay);
        tv_summary = (TextView) findViewById(R.id.tv_summary);

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPagerContainer = (RelativeLayout) findViewById(R.id.pager_layout);
        viewPager.setPageMargin(Utils.dip2px(10));
        viewPager.addOnPageChangeListener(new MyOnPageChangeListener());

        indicator_lay = (LinearLayout) findViewById(R.id.indicator_lay);

        btn_use = (Button) findViewById(R.id.btn_use);
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText("我的优惠券");
    }

    private void requestCouponInfo() {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.YHQ_COUPON;
        d.flag = AppConst.YHQ_COUPON;
        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void refreshCouponInfo() {
        if (couponInfoBean != null) {
            if (couponInfoBean.coupon != null && couponInfoBean.coupon.size() > 0) {
                noDiscountLayout.setVisibility(View.GONE);
                hasDiscountLayout.setVisibility(View.VISIBLE);

                viewPager.setAdapter(new MyPagerAdapter(this, couponInfoBean.coupon));
                // to cache all page, or we will see the right item delayed
                int count = couponInfoBean.coupon.size();
                if (count > 0) {
                    viewPager.setOffscreenPageLimit(count);
                }
                initIndicatorLay(count);
                String hotelName = couponInfoBean.coupon.get(0).cityName + couponInfoBean.coupon.get(0).hotelName;
                String date = DateUtil.getDay("yyyy/MM/dd", couponInfoBean.coupon.get(0).activeTime);
                String note = String.format(getString(R.string.coupon_note), hotelName, date);

                int index[] = new int[2];
                index[0] = note.indexOf(hotelName);
                index[1] = note.indexOf(date);

                SpannableStringBuilder style = new SpannableStringBuilder(note);
                style.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.mainColor)), index[0], index[0] + hotelName.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                style.setSpan(new StyleSpan(Typeface.BOLD), index[0], index[0] + hotelName.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                style.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.mainColor)), index[1], index[1] + date.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                style.setSpan(new StyleSpan(Typeface.BOLD), index[1], index[1] + date.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                tv_summary.setText(style);
            } else {
                noDiscountLayout.setVisibility(View.VISIBLE);
                hasDiscountLayout.setVisibility(View.GONE);

                if (couponInfoBean.activity != null) {
                    active_lay.setVisibility(View.VISIBLE);
                    tv_no_coupon_note.setText(String.format(getString(R.string.no_coupon_note), couponInfoBean.activity.roomCnt));
                    tv_no_coupon_time.setText(DateUtil.getDay("yyyy/MM/dd", couponInfoBean.activity.createTime));
                } else {
                    active_lay.setVisibility(View.GONE);
                }
            }
        }
    }


    private void initIndicatorLay(int count) {
        indicator_lay.removeAllViews();
        if (count > 1) {
            for (int i = 0; i < count; i++) {
                View view = new View(this);
                view.setBackgroundResource(R.drawable.indicator_sel);
                view.setEnabled(false);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Utils.dip2px(7), Utils.dip2px(7));
                if (i > 0) {
                    lp.leftMargin = Utils.dip2px(7);
                }
                view.setLayoutParams(lp);
                indicator_lay.addView(view);
            }
            indicator_lay.getChildAt(positionIndex).setEnabled(true);
        }
    }

    @Override
    public void initListeners() {
        super.initListeners();
        viewPagerContainer.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // dispatch the events to the ViewPager, to solve the problem that we can swipe only the middle view.
                return viewPager.dispatchTouchEvent(event);
            }
        });
        btn_use.setOnClickListener(this);
        active_lay.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btn_use:
                HotelOrderManager.getInstance().reset();
                HotelOrderManager.getInstance().setCouponInfoBean(couponInfoBean.coupon.get(positionIndex));
                Intent intent = new Intent(this, HotelCalendarChooseActivity.class);
                intent.putExtra("isForbidTitleClick", true);
                intent.putExtra("isCouponBooking", true);
                startActivity(intent);
                break;
            case R.id.active_lay:
                Intent intent1 = new Intent(this, Hotel0YuanHomeActivity.class);
                startActivity(intent1);
                break;
            default:
                break;
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
            if (request.flag == AppConst.YHQ_COUPON) {
                removeProgressDialog();
                LogUtil.i(TAG, "yhq json = " + response.body.toString());
                couponInfoBean = JSON.parseObject(response.body.toString(), CouponInfoBean.class);
                if (null != couponInfoBean) {
                    refreshCouponInfo();
                }
            }
        }
    }

    /**
     * this is a example fragment, just a imageview, u can replace it with your needs
     *
     * @author Trinea 2013-04-03
     */
    class MyPagerAdapter extends PagerAdapter {
        private Context context;
        private List<CouponInfoBean.CouponInfo> list = new ArrayList<>();

        public MyPagerAdapter(Context context, List<CouponInfoBean.CouponInfo> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = LayoutInflater.from(context).inflate(R.layout.vp_mydiscount_item, null);
            final RoundedAllImageView iv_background = (RoundedAllImageView) view.findViewById(R.id.iv_background);
            String url = list.get(position).featurePicPath;
            Glide.with(context)
                    .load(new CustomReqURLFormatModelImpl(url))
                    .placeholder(R.drawable.def_coupon)
                    .crossFade()
                    .centerCrop()
                    .override(800, 480)
                    .into(iv_background);
            CommonAssessStarsLayout start_lay = (CommonAssessStarsLayout) view.findViewById(R.id.start_lay);
            int grade = 0;
            try {
                grade = (int) Float.parseFloat(list.get(position).grade);
            } catch (Exception e) {
                e.printStackTrace();
            }
            start_lay.setColorStars(grade);
            TextView tv_name = (TextView) view.findViewById(R.id.tv_name);
            tv_name.setText(list.get(position).hotelName);
            TextView tv_valid_time = (TextView) view.findViewById(R.id.tv_valid_time);
            tv_valid_time.setText("有效期：" + DateUtil.getDay("yyyy/MM/dd", list.get(position).activeTime));
            container.addView(view, position);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageSelected(int position) {
            indicator_lay.getChildAt(position).setEnabled(true);
            indicator_lay.getChildAt(positionIndex).setEnabled(false);
            positionIndex = position;
            String hotelName = couponInfoBean.coupon.get(position).cityName + couponInfoBean.coupon.get(position).hotelName;
            String date = DateUtil.getDay("yyyy/MM/dd", couponInfoBean.coupon.get(position).activeTime);
            String note = String.format(getString(R.string.coupon_note), hotelName, date);

            int index[] = new int[2];
            index[0] = note.indexOf(hotelName);
            index[1] = note.indexOf(date);

            SpannableStringBuilder style = new SpannableStringBuilder(note);
            style.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.mainColor)), index[0], index[0] + hotelName.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            style.setSpan(new StyleSpan(Typeface.BOLD), index[0], index[0] + hotelName.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            style.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.mainColor)), index[1], index[1] + date.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            style.setSpan(new StyleSpan(Typeface.BOLD), index[1], index[1] + date.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            tv_summary.setText(style);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (viewPagerContainer != null) {
                viewPagerContainer.invalidate();
            }
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }
}
