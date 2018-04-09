package com.huicheng.hotel.android.ui.activity.plane;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.requestbuilder.bean.AirCompanyInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneOrderDetailInfoBean;
import com.huicheng.hotel.android.ui.activity.MainSwitcherActivity;
import com.huicheng.hotel.android.ui.activity.UcOrdersActivity;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.huicheng.hotel.android.ui.glide.CustomReqURLFormatModelImpl;
import com.prj.sdk.util.BitmapUtils;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.StringUtil;

public class PlaneOrderPaySuccessActivity extends BaseAppActivity {

    private ImageView iv_pay_success;
    private LinearLayout order_info_lay;
    private TextView tv_gohome, tv_myorder;

    private View orderLayout;
    private ImageView iv_plane_icon, iv_plane_icon_back;
    private TextView tv_plane_name, tv_plane_name_back;
    private TextView tv_plane_code, tv_plane_code_back;
    private TextView tv_plane_date, tv_plane_date_back;

    private PlaneNewOrderActivity.FlightDetailInfo goFlightDetailInfo = null, backFlightDetailInfo = null;
    private PlaneOrderDetailInfoBean.TripInfo goTripInfo = null, backTripInfo = null;

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_plane_paysuccess_layout);
    }

    @Override
    public void initViews() {
        super.initViews();
        iv_pay_success = (ImageView) findViewById(R.id.iv_pay_success);
        order_info_lay = (LinearLayout) findViewById(R.id.order_info_lay);
        tv_gohome = (TextView) findViewById(R.id.tv_gohome);
        tv_myorder = (TextView) findViewById(R.id.tv_myorder);
    }

    @Override
    protected void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            goFlightDetailInfo = (PlaneNewOrderActivity.FlightDetailInfo) bundle.getSerializable("goFlightDetailInfo");
            backFlightDetailInfo = (PlaneNewOrderActivity.FlightDetailInfo) bundle.getSerializable("backFlightDetailInfo");
            goTripInfo = (PlaneOrderDetailInfoBean.TripInfo) bundle.getSerializable("goTripInfo");
            backTripInfo = (PlaneOrderDetailInfoBean.TripInfo) bundle.getSerializable("backTripInfo");
        }
    }

    @Override
    public void initParams() {
        super.initParams();
        findViewById(R.id.comm_title_rl).setBackgroundColor(getResources().getColor(R.color.white));
        tv_center_title.setText("订单成功");
        setRightButtonResource("完成", getResources().getColor(R.color.plane_mainColor));

        iv_pay_success.setImageBitmap(BitmapUtils.getAlphaBitmap(iv_pay_success.getDrawable(), getResources().getColor(R.color.plane_mainColor)));
        order_info_lay.removeAllViews();
        if (backTripInfo != null || backFlightDetailInfo != null) {
            orderLayout = LayoutInflater.from(this).inflate(R.layout.layout_goback_order_item, null);
            iv_plane_icon_back = (ImageView) orderLayout.findViewById(R.id.iv_plane_icon_back);
            tv_plane_name_back = (TextView) orderLayout.findViewById(R.id.tv_plane_name_back);
            tv_plane_code_back = (TextView) orderLayout.findViewById(R.id.tv_plane_code_back);
            tv_plane_date_back = (TextView) orderLayout.findViewById(R.id.tv_plane_date_back);
            String flightCode = null != backTripInfo ? backTripInfo.airco : backFlightDetailInfo.flightInfo.carrier;
            if (StringUtil.notEmpty(flightCode)) {
                if (SessionContext.getAirCompanyMap().size() > 0
                        && SessionContext.getAirCompanyMap().containsKey(flightCode)) {
                    AirCompanyInfoBean companyInfoBean = SessionContext.getAirCompanyMap().get(flightCode);
                    tv_plane_name_back.setText(companyInfoBean.company);
                    iv_plane_icon_back.setVisibility(View.VISIBLE);
                    Glide.with(this)
                            .load(new CustomReqURLFormatModelImpl(companyInfoBean.logourl))
                            .fitCenter()
                            .into(iv_plane_icon_back);
                } else {
                    tv_plane_name_back.setText(flightCode);
                    iv_plane_icon_back.setVisibility(View.GONE);
                }
            }
            tv_plane_code_back.setText(null != backTripInfo ? backTripInfo.flightNo : backFlightDetailInfo.flightInfo.flightNum);
            String date = (null != backTripInfo)
                    ? (DateUtil.getDay("MM月dd日", backTripInfo.sDate) + " " + backTripInfo.sTime)
                    : (DateUtil.getDay("MM月dd日", DateUtil.str2Date(backFlightDetailInfo.ticketInfo.date, "yyyy-MM-dd").getTime()) + " " + backFlightDetailInfo.ticketInfo.btime);
            tv_plane_date_back.setText(date);
        } else {
            orderLayout = LayoutInflater.from(this).inflate(R.layout.layout_gosingle_order_item, null);
        }
        iv_plane_icon = (ImageView) orderLayout.findViewById(R.id.iv_plane_icon);
        tv_plane_name = (TextView) orderLayout.findViewById(R.id.tv_plane_name);
        tv_plane_code = (TextView) orderLayout.findViewById(R.id.tv_plane_code);
        tv_plane_date = (TextView) orderLayout.findViewById(R.id.tv_plane_date);
        order_info_lay.addView(orderLayout);
        String flightCode = null != goTripInfo ? goTripInfo.airco : goFlightDetailInfo.flightInfo.carrier;
        if (StringUtil.notEmpty(flightCode)) {
            if (SessionContext.getAirCompanyMap().size() > 0
                    && SessionContext.getAirCompanyMap().containsKey(flightCode)) {
                AirCompanyInfoBean companyInfoBean = SessionContext.getAirCompanyMap().get(flightCode);
                tv_plane_name.setText(companyInfoBean.company);
                iv_plane_icon.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(new CustomReqURLFormatModelImpl(companyInfoBean.logourl))
                        .fitCenter()
                        .into(iv_plane_icon);
            } else {
                tv_plane_name.setText(flightCode);
                iv_plane_icon.setVisibility(View.GONE);
            }
        }
        tv_plane_code.setText(null != goTripInfo ? goTripInfo.flightNo : goFlightDetailInfo.flightInfo.flightNum);
        String date = (null != goTripInfo)
                ? (DateUtil.getDay("MM月dd日", goTripInfo.sDate) + " " + goTripInfo.sTime)
                : (DateUtil.getDay("MM月dd日", DateUtil.str2Date(goFlightDetailInfo.ticketInfo.date, "yyyy-MM-dd").getTime()) + " " + goFlightDetailInfo.ticketInfo.btime);
        tv_plane_date.setText(date);
    }

    @Override
    public void initListeners() {
        super.initListeners();
        tv_gohome.setOnClickListener(this);
        tv_myorder.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_back:
            case R.id.tv_right:
            case R.id.tv_gohome: {
                startActivity(new Intent(this, MainSwitcherActivity.class));
                break;
            }
            case R.id.tv_myorder: {
                startActivity(new Intent(this, MainSwitcherActivity.class));
                startActivity(new Intent(this, UcOrdersActivity.class));
                break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            iv_back.performClick();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
