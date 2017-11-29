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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.CouponInfoBean;
import com.huicheng.hotel.android.ui.activity.hotel.Hotel0YuanHomeActivity;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.RoundedAllImageView;
import com.huicheng.hotel.android.ui.glide.CustomReqURLFormatModelImpl;
import com.prj.sdk.app.AppConst;
import com.prj.sdk.app.NetURL;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
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
public class UcCouponsActivity extends BaseActivity implements ViewPager.OnPageChangeListener {

    private static final int FREE_COUPON = 1;
    private static final int UNION_COUPON = 2;

    private LinearLayout noDiscountLayout, hasDiscountLayout, active_lay;
    private TextView tv_no_coupon_note, tv_no_coupon_time;
    private TextView tv_summary;

    private ViewPager viewPager;
    private LinearLayout indicator_lay;
    private int positionIndex = 0;
    private CouponInfoBean couponInfoBean = null;
    private Button btn_use;

    private boolean isShowAll = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_coupons_layout);
        initViews();
        initParams();
        initListeners();
        if (null == savedInstanceState) {
            if (isShowAll) {
                requestAllCoupons();
            } else {
                requestUsefulCoupons();
            }
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
        viewPager.addOnPageChangeListener(this);
        indicator_lay = (LinearLayout) findViewById(R.id.indicator_lay);
        btn_use = (Button) findViewById(R.id.btn_use);
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText("我的优惠券");
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            boolean showUsefulCoupon = bundle.getBoolean("showUsefulCoupon");
            isShowAll = !showUsefulCoupon;
        }
    }

    private void requestAllCoupons() {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.COUPON_ALL;
        d.flag = AppConst.COUPON_ALL;
        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void requestUsefulCoupons() {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("beginDate", String.valueOf(HotelOrderManager.getInstance().getBeginTime()));
        b.addBody("endDate", String.valueOf(HotelOrderManager.getInstance().getEndTime()));
        b.addBody("hotelid", String.valueOf(HotelOrderManager.getInstance().getHotelDetailInfo().id));

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.COUPON_USEFUL_LIST;
        d.flag = AppConst.COUPON_USEFUL_LIST;
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

                viewPager.setPageMargin(Utils.dip2px(10));
                viewPager.setAdapter(new MyPagerAdapter(this, couponInfoBean.coupon));
                initIndicatorLay(couponInfoBean.coupon.size());
                viewPager.setOffscreenPageLimit(couponInfoBean.coupon.size());
                refreshCouponInfoAndStatus(0);
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
        btn_use.setOnClickListener(this);
        active_lay.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btn_use:
                CouponInfoBean.CouponInfo info = couponInfoBean.coupon.get(positionIndex);
                if (info.type == 2) {
                    Intent data = new Intent();
                    data.putExtra("coupon", info);
                    setResult(RESULT_OK, data);
                    finish();
                } else {
                    HotelOrderManager.getInstance().reset();
                    HotelOrderManager.getInstance().setCouponInfoBean(info);
                    Intent intent = new Intent(this, CalendarChooseActivity.class);
                    intent.putExtra("isCouponBooking", true);
                    startActivity(intent);
                }
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
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.COUPON_ALL
                    || request.flag == AppConst.COUPON_USEFUL_LIST) {
                removeProgressDialog();
                LogUtil.i(TAG, "show coupon json = " + response.body.toString());
                couponInfoBean = JSON.parseObject(response.body.toString(), CouponInfoBean.class);
                if (null != couponInfoBean) {
                    refreshCouponInfo();
                }
            }
        }
    }

    private void refreshCouponInfoAndStatus(int position) {
        CouponInfoBean.CouponInfo info = couponInfoBean.coupon.get(position);
        if (isShowAll) {
            btn_use.setVisibility(View.GONE);
        } else {
            btn_use.setVisibility(View.VISIBLE);
        }

        if (info.type == UNION_COUPON) {
            btn_use.setEnabled(true);
            tv_summary.setText(getString(R.string.coupon_tips2));
            tv_summary.append(getString(R.string.coupon_tips_support));
            if (info.eventHotel != null && info.eventHotel.size() > 0) {
                for (int i = 0; i < info.eventHotel.size(); i++) {
                    tv_summary.append(info.eventHotel.get(i));
                    if (i != info.eventHotel.size() - 1) {
                        tv_summary.append("，");
                    }
                }
            } else {
                tv_summary.append("无");
            }
        } else {
            btn_use.setEnabled(false);
            String name = info.hotelName;
            String date = DateUtil.getDay("yyyy/MM/dd", couponInfoBean.coupon.get(position).activeTime);
            String note = String.format(getString(R.string.coupon_tips1), name, date);
            int index[] = new int[2];
            index[0] = note.indexOf(name);
            index[1] = note.indexOf(date);

            SpannableStringBuilder style = new SpannableStringBuilder(note);
            style.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.mainColor)), index[0], index[0] + name.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            style.setSpan(new StyleSpan(Typeface.BOLD), index[0], index[0] + name.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            style.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.mainColor)), index[1], index[1] + date.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            style.setSpan(new StyleSpan(Typeface.BOLD), index[1], index[1] + date.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            tv_summary.setText(style);
        }
    }

    @Override
    public void onPageSelected(int position) {
        indicator_lay.getChildAt(position).setEnabled(true);
        indicator_lay.getChildAt(positionIndex).setEnabled(false);
        positionIndex = position;
        refreshCouponInfoAndStatus(position);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
    }

    /**
     * this is a example fragment, just a imageview, u can replace it with your needs
     *
     * @author Trinea 2013-04-03
     */
    private class MyPagerAdapter extends PagerAdapter {
        private Context context;
        private List<CouponInfoBean.CouponInfo> list = new ArrayList<>();

        MyPagerAdapter(Context context, List<CouponInfoBean.CouponInfo> list) {
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
            RoundedAllImageView iv_background = (RoundedAllImageView) view.findViewById(R.id.iv_background);
            TextView tv_name = (TextView) view.findViewById(R.id.tv_name);
            TextView tv_limit = (TextView) view.findViewById(R.id.tv_limit);
            TextView tv_id_num = (TextView) view.findViewById(R.id.tv_id_num);
            TextView tv_valid_time = (TextView) view.findViewById(R.id.tv_valid_time);

            CouponInfoBean.CouponInfo info = list.get(position);
            String url = info.featurePicPath;
            Glide.with(context)
                    .load(new CustomReqURLFormatModelImpl(url))
                    .placeholder(R.drawable.def_coupon)
                    .crossFade()
                    .centerCrop()
                    .override(800, 480)
                    .into(iv_background);

            String name;
            if (info.type == UNION_COUPON) {
                name = info.name;
                tv_limit.setVisibility(View.VISIBLE);
            } else {
                name = info.hotelName;
                tv_limit.setVisibility(View.GONE);
            }
            tv_name.setText(name);
            tv_id_num.setText("凭证号：" + info.code);
            tv_valid_time.setText("有效期：" + DateUtil.getDay("yyyy/MM/dd", info.createTime) + "-" + DateUtil.getDay("yyyy/MM/dd", info.activeTime));

            container.addView(view, position);

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
