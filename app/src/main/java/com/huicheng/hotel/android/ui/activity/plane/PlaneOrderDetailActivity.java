package com.huicheng.hotel.android.ui.activity.plane;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.PlaneCommDef;
import com.huicheng.hotel.android.common.PlaneOrderManager;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.requestbuilder.bean.AirCompanyInfoBean;
import com.huicheng.hotel.android.ui.activity.MainSwitcherActivity;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.huicheng.hotel.android.ui.glide.CustomReqURLFormatModelImpl;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;

import java.util.Date;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author kborid
 * @date 2017/3/10 0010
 */
public class PlaneOrderDetailActivity extends BaseAppActivity {

    @BindView(R.id.flight_layout)
    LinearLayout flightLayout;
    @BindView(R.id.tv_total_price)
    TextView tvTotalPrice;
    @BindView(R.id.tv_pay)
    TextView tvPay;
    @BindView(R.id.tv_cancel)
    TextView tvCancel;
    @BindView(R.id.tv_booking)
    TextView tvBooking;

    private PlaneNewOrderActivity.FlightDetailInfo goFlightDetailInfo;
    private PlaneNewOrderActivity.FlightDetailInfo backFlightDetailInfo;

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_plane_orderdetail);
    }

    @Override
    protected void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            goFlightDetailInfo = (PlaneNewOrderActivity.FlightDetailInfo) bundle.getSerializable("goFlightDetailInfo");
            backFlightDetailInfo = (PlaneNewOrderActivity.FlightDetailInfo) bundle.getSerializable("backFlightDetailInfo");
        }
    }

    @Override
    protected void initParams() {
        super.initParams();
        findViewById(R.id.comm_title_rl).setBackgroundColor(getResources().getColor(R.color.white));
        tv_center_title.setText("订单详情");
        if (null != goFlightDetailInfo) {
            flightLayout.removeAllViews();
            {
                //去程信息
                {
                    View goFlightView = LayoutInflater.from(this).inflate(R.layout.layout_plane_orderdetail_flightinfo_item, null);
                    flightLayout.addView(goFlightView);
                    TextView tv_flag = (TextView) goFlightView.findViewById(R.id.tv_flag);
                    TextView tv_date = (TextView) goFlightView.findViewById(R.id.tv_date);
                    TextView tv_price = (TextView) goFlightView.findViewById(R.id.tv_price);
                    TextView tv_off_time = (TextView) goFlightView.findViewById(R.id.tv_off_time);
                    TextView tv_on_time = (TextView) goFlightView.findViewById(R.id.tv_on_time);
                    TextView tv_during = (TextView) goFlightView.findViewById(R.id.tv_during);
                    TextView tv_off_airport = (TextView) goFlightView.findViewById(R.id.tv_off_airport);
                    TextView tv_on_airport = (TextView) goFlightView.findViewById(R.id.tv_on_airport);
                    TextView tv_name = (TextView) goFlightView.findViewById(R.id.tv_name);
                    TextView tv_code = (TextView) goFlightView.findViewById(R.id.tv_code);
                    ImageView iv_flight_icon = (ImageView) goFlightView.findViewById(R.id.iv_flight_icon);

                    goFlightView.findViewById(R.id.ticket_count_layout).setVisibility(View.VISIBLE);
                    TextView tv_ticket_count = (TextView) goFlightView.findViewById(R.id.tv_ticket_count);
                    TextView tv_jjry_count = (TextView) goFlightView.findViewById(R.id.tv_jjry_count);
                    tv_jjry_count.setVisibility(View.VISIBLE);
                    TextView tv_jjry_price = (TextView) goFlightView.findViewById(R.id.tv_jjry_price);
                    goFlightView.findViewById(R.id.baoxian_layout).setVisibility(View.VISIBLE);
                    TextView tv_baoxian_count = (TextView) goFlightView.findViewById(R.id.tv_baoxian_count);
                    tv_baoxian_count.setVisibility(View.VISIBLE);

                    tv_flag.setText("去程");
                    Date date = DateUtil.str2Date(goFlightDetailInfo.ticketInfo.date, "yyyy-MM-dd");
                    tv_date.setText(DateUtil.getDay("MM月dd日", date.getTime()) + " " + goFlightDetailInfo.ticketInfo.btime);
                    tv_price.setText(String.valueOf(goFlightDetailInfo.vendorInfo.barePrice));
                    tv_off_time.setText(goFlightDetailInfo.flightInfo.dptTime);
                    tv_on_time.setText(goFlightDetailInfo.flightInfo.arrTime);
                    tv_during.setText(goFlightDetailInfo.flightInfo.flightTimes);
                    tv_off_airport.setText(goFlightDetailInfo.ticketInfo.depAirport + goFlightDetailInfo.ticketInfo.depTerminal);
                    tv_on_airport.setText(goFlightDetailInfo.ticketInfo.arrAirport + goFlightDetailInfo.ticketInfo.arrTerminal);
                    tv_code.setText(goFlightDetailInfo.flightInfo.flightNum);
                    if (StringUtil.notEmpty(goFlightDetailInfo.flightInfo.carrier)) {
                        if (SessionContext.getAirCompanyMap().size() > 0
                                && SessionContext.getAirCompanyMap().containsKey(goFlightDetailInfo.flightInfo.carrier)) {
                            AirCompanyInfoBean companyInfoBean = SessionContext.getAirCompanyMap().get(goFlightDetailInfo.flightInfo.carrier);
                            tv_name.setText(companyInfoBean.company);
                            iv_flight_icon.setVisibility(View.VISIBLE);
                            Glide.with(this)
                                    .load(new CustomReqURLFormatModelImpl(companyInfoBean.logourl))
                                    .fitCenter()
                                    .override(Utils.dp2px(15), Utils.dp2px(15))
                                    .into(iv_flight_icon);
                        } else {
                            tv_name.setText(goFlightDetailInfo.flightInfo.carrier);
                            iv_flight_icon.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        iv_flight_icon.setVisibility(View.INVISIBLE);
                    }
                    tv_jjry_price.setText(String.valueOf(goFlightDetailInfo.flightInfo.arf + goFlightDetailInfo.flightInfo.tof));

                    //去程乘机人信息
                    {
                        View goPassengerActionLayout = LayoutInflater.from(this).inflate(R.layout.layout_plane_orderdetail_passenger_action_item, null);
                        flightLayout.addView(goPassengerActionLayout);
                        LinearLayout goPassengerLayout = (LinearLayout) goPassengerActionLayout.findViewById(R.id.passenger_lay);
                        goPassengerLayout.removeAllViews();
                        View goPassenger1 = LayoutInflater.from(this).inflate(R.layout.layout_plane_orderdetail_personinfo_item, null);
                        View goPassenger2 = LayoutInflater.from(this).inflate(R.layout.layout_plane_orderdetail_personinfo_item, null);
                        goPassengerLayout.addView(goPassenger1);
                        goPassengerLayout.addView(goPassenger2);

                        TextView tv_back = (TextView) goPassengerActionLayout.findViewById(R.id.tv_back);
                        tv_back.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                backPlaneTicketAction(PlaneCommDef.STATUS_GO);
                            }
                        });
                        TextView tv_change = (TextView) goPassengerActionLayout.findViewById(R.id.tv_change);
                        tv_change.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                changePlaneTicketAction(PlaneCommDef.STATUS_GO);
                            }
                        });
                    }
                }


                //返程信息
                {
                    if (PlaneOrderManager.instance.isFlightGoBack() && null != backFlightDetailInfo) {
                        View backFlightView = LayoutInflater.from(this).inflate(R.layout.layout_plane_orderdetail_flightinfo_item, null);
                        flightLayout.addView(backFlightView);
                        TextView tv_flag = (TextView) backFlightView.findViewById(R.id.tv_flag);
                        TextView tv_date = (TextView) backFlightView.findViewById(R.id.tv_date);
                        TextView tv_price = (TextView) backFlightView.findViewById(R.id.tv_price);
                        TextView tv_off_time = (TextView) backFlightView.findViewById(R.id.tv_off_time);
                        TextView tv_on_time = (TextView) backFlightView.findViewById(R.id.tv_on_time);
                        TextView tv_during = (TextView) backFlightView.findViewById(R.id.tv_during);
                        TextView tv_off_airport = (TextView) backFlightView.findViewById(R.id.tv_off_airport);
                        TextView tv_on_airport = (TextView) backFlightView.findViewById(R.id.tv_on_airport);
                        TextView tv_name = (TextView) backFlightView.findViewById(R.id.tv_name);
                        TextView tv_code = (TextView) backFlightView.findViewById(R.id.tv_code);
                        ImageView iv_flight_icon = (ImageView) backFlightView.findViewById(R.id.iv_flight_icon);

                        backFlightView.findViewById(R.id.ticket_count_layout).setVisibility(View.VISIBLE);
                        TextView tv_ticket_count = (TextView) backFlightView.findViewById(R.id.tv_ticket_count);
                        TextView tv_jjry_count = (TextView) backFlightView.findViewById(R.id.tv_jjry_count);
                        tv_jjry_count.setVisibility(View.VISIBLE);
                        TextView tv_jjry_price = (TextView) backFlightView.findViewById(R.id.tv_jjry_price);
                        backFlightView.findViewById(R.id.baoxian_layout).setVisibility(View.VISIBLE);
                        TextView tv_baoxian_count = (TextView) backFlightView.findViewById(R.id.tv_baoxian_count);
                        tv_baoxian_count.setVisibility(View.VISIBLE);

                        tv_flag.setText("返程");
                        Date date = DateUtil.str2Date(backFlightDetailInfo.ticketInfo.date, "yyyy-MM-dd");
                        tv_date.setText(DateUtil.getDay("MM月dd日", date.getTime()) + " " + backFlightDetailInfo.ticketInfo.btime);
                        tv_price.setText(String.valueOf(backFlightDetailInfo.vendorInfo.barePrice));
                        tv_off_time.setText(backFlightDetailInfo.flightInfo.dptTime);
                        tv_on_time.setText(backFlightDetailInfo.flightInfo.arrTime);
                        tv_during.setText(backFlightDetailInfo.flightInfo.flightTimes);
                        tv_off_airport.setText(backFlightDetailInfo.ticketInfo.depAirport + backFlightDetailInfo.ticketInfo.depTerminal);
                        tv_on_airport.setText(backFlightDetailInfo.ticketInfo.arrAirport + backFlightDetailInfo.ticketInfo.arrTerminal);
                        tv_code.setText(backFlightDetailInfo.flightInfo.flightNum);
                        if (StringUtil.notEmpty(backFlightDetailInfo.flightInfo.carrier)) {
                            if (SessionContext.getAirCompanyMap().size() > 0
                                    && SessionContext.getAirCompanyMap().containsKey(backFlightDetailInfo.flightInfo.carrier)) {
                                AirCompanyInfoBean companyInfoBean = SessionContext.getAirCompanyMap().get(backFlightDetailInfo.flightInfo.carrier);
                                tv_name.setText(companyInfoBean.company);
                                iv_flight_icon.setVisibility(View.VISIBLE);
                                Glide.with(this)
                                        .load(new CustomReqURLFormatModelImpl(companyInfoBean.logourl))
                                        .fitCenter()
                                        .override(Utils.dp2px(15), Utils.dp2px(15))
                                        .into(iv_flight_icon);
                            } else {
                                tv_name.setText(backFlightDetailInfo.flightInfo.carrier);
                                iv_flight_icon.setVisibility(View.INVISIBLE);
                            }
                        } else {
                            iv_flight_icon.setVisibility(View.INVISIBLE);
                        }
                        tv_jjry_price.setText(String.valueOf(goFlightDetailInfo.flightInfo.arf + goFlightDetailInfo.flightInfo.tof));

                        //返程乘机人信息
                        {
                            View backPassengerActionLayout = LayoutInflater.from(this).inflate(R.layout.layout_plane_orderdetail_passenger_action_item, null);
                            flightLayout.addView(backPassengerActionLayout);
                            LinearLayout backPassengerLayout = (LinearLayout) backPassengerActionLayout.findViewById(R.id.passenger_lay);
                            backPassengerLayout.removeAllViews();
                            View backPassenger1 = LayoutInflater.from(this).inflate(R.layout.layout_plane_orderdetail_personinfo_item, null);
                            View backPassenger2 = LayoutInflater.from(this).inflate(R.layout.layout_plane_orderdetail_personinfo_item, null);
                            backPassengerLayout.addView(backPassenger1);
                            backPassengerLayout.addView(backPassenger2);

                            TextView tv_back = (TextView) backPassengerActionLayout.findViewById(R.id.tv_back);
                            tv_back.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    backPlaneTicketAction(PlaneCommDef.STATUS_BACK);
                                }
                            });
                            TextView tv_change = (TextView) backPassengerActionLayout.findViewById(R.id.tv_change);
                            tv_change.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    changePlaneTicketAction(PlaneCommDef.STATUS_BACK);
                                }
                            });
                        }
                    }
                }
            }
        }
    }

    @OnClick({R.id.tv_pay, R.id.tv_cancel, R.id.tv_booking})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_pay:
                break;
            case R.id.tv_cancel:
                break;
            case R.id.tv_booking:
                Intent rebookIntent = new Intent(this, MainSwitcherActivity.class);
                rebookIntent.putExtra("index", 1);
                startActivity(rebookIntent);
                break;
        }
    }

    private void backPlaneTicketAction(int flightType) {
        System.out.println("backPlaneTicketAction flightType = " + flightType);
        startActivity(new Intent(this, PlaneBackTicketActivity.class));
    }

    private void changePlaneTicketAction(int flightType) {
        System.out.println("changePlaneTicketAction flightType = " + flightType);
    }
}
