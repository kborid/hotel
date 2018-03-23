package com.huicheng.hotel.android.ui.activity.plane;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.PlaneCommDef;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.content.AppConst;
import com.huicheng.hotel.android.content.NetURL;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.AirCompanyInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneOrderDetailInfoBean;
import com.huicheng.hotel.android.ui.activity.MainSwitcherActivity;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.huicheng.hotel.android.ui.glide.CustomReqURLFormatModelImpl;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;

import java.util.LinkedHashMap;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author kborid
 * @date 2017/3/10 0010
 */
public class PlaneOrderDetailActivity extends BaseAppActivity {

    private static final int GO_TRIP = 1;
    private static final int BACK_TRIP = 2;

    @BindView(R.id.scroll_view)
    ScrollView scrollView;
    @BindView(R.id.flight_layout)
    LinearLayout flightLayout;
    @BindView(R.id.tv_total_price)
    TextView tvTotalPrice;
    @BindView(R.id.tv_contacts_name)
    TextView tvContactsName;
    @BindView(R.id.tv_contacts_mobile)
    TextView tvContactsMobile;
    @BindView(R.id.order_timeinfo_lay)
    LinearLayout orderTimeInfoLay;

    @BindView(R.id.tv_pay)
    TextView tvPay;
    @BindView(R.id.tv_cancel)
    TextView tvCancel;
    @BindView(R.id.tv_booking)
    TextView tvBooking;

    private PlaneOrderDetailInfoBean orderDetailInfoBean = null;
    private String orderNo = "";
    private int orderType = 1;
    private int backViewTop = 0;

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_plane_orderdetail);
    }

    @Override
    protected void requestData() {
        super.requestData();
        requestPlaneOrderDetailInfo(orderNo);
    }

    @Override
    protected void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            orderNo = bundle.getString("planeOrderNo");
            orderType = bundle.getInt("planeOrderType");
        }
    }

    @Override
    protected void initParams() {
        super.initParams();
        findViewById(R.id.comm_title_rl).setBackgroundColor(getResources().getColor(R.color.white));
        tv_center_title.setText("订单详情");
        scrollView.setLayoutAnimation(getAnimationController());
    }

    private void refreshDetailInfo() {
        if (null != orderDetailInfoBean) {
            //订单信息
            tvTotalPrice.setText(String.valueOf(orderDetailInfoBean.shouldPayAmount));
            tvContactsName.setText(orderDetailInfoBean.contactor);
            tvContactsMobile.setText(orderDetailInfoBean.mobile);

            LinkedHashMap<String, String> orderTimeInfo = new LinkedHashMap<>();
            orderTimeInfo.put("订单编号：", orderDetailInfoBean.orderId);
            orderTimeInfo.put("创建日期：", DateUtil.getDay("yyyy年MM月dd日HH时mm分", orderDetailInfoBean.createTime));
            orderTimeInfo.put("支付时间：", 0 != orderDetailInfoBean.paidTime ? DateUtil.getDay("yyyy年MM月dd日HH时mm分", orderDetailInfoBean.paidTime) : "");
//            orderTimeInfo.put("出票日期：", DateUtil.getDay("yyyy年MM月dd日HH时mm分", orderDetailInfoBean.createTime));
//            orderTimeInfo.put("改签时间：", DateUtil.getDay("yyyy年MM月dd日HH时mm分", orderDetailInfoBean.createTime));
//            orderTimeInfo.put("改签成功时间：", DateUtil.getDay("yyyy年MM月dd日HH时mm分", orderDetailInfoBean.createTime));
            orderTimeInfoLay.removeAllViews();
            for (String key : orderTimeInfo.keySet()) {
                if (StringUtil.notEmpty(orderTimeInfo.get(key))) {
                    View v = LayoutInflater.from(this).inflate(R.layout.layout_plane_orderdetail_orderinfo_item, null);
                    orderTimeInfoLay.addView(v);
                    ((TextView) v.findViewById(R.id.tv_item_name)).setText(key);
                    ((TextView) v.findViewById(R.id.tv_item_content)).setText(orderTimeInfo.get(key));
                }
            }

            //机票信息
            PlaneOrderDetailInfoBean.TripInfo goTripInfo = null;
            PlaneOrderDetailInfoBean.TripInfo backTripInfo = null;
            for (int i = 0; i < orderDetailInfoBean.tripList.size(); i++) {
                PlaneOrderDetailInfoBean.TripInfo tripInfo = orderDetailInfoBean.tripList.get(i);
                if (tripInfo.tripType == GO_TRIP) {
                    goTripInfo = tripInfo;
                } else if (tripInfo.tripType == BACK_TRIP) {
                    backTripInfo = tripInfo;
                }
            }
            if (null != goTripInfo) {
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
                        TextView tv_company = (TextView) goFlightView.findViewById(R.id.tv_company);
                        TextView tv_code = (TextView) goFlightView.findViewById(R.id.tv_code);
                        ImageView iv_flight_icon = (ImageView) goFlightView.findViewById(R.id.iv_flight_icon);

                        tv_flag.setText("去程");
                        tv_date.setText(DateUtil.getDay("MM月dd日", goTripInfo.sDate) + " " + goTripInfo.sTime);
                        tv_price.setText(String.valueOf(goTripInfo.shouldPayAmount));
                        tv_off_time.setText(goTripInfo.sTime);
                        tv_on_time.setText(goTripInfo.eTime);
                        tv_during.setText(goTripInfo.flyTime);
                        tv_off_airport.setText(goTripInfo.sAirport + goTripInfo.sTerminal);
                        tv_on_airport.setText(goTripInfo.eAirport + goTripInfo.eTerminal);
                        tv_code.setText(goTripInfo.flightNo);
                        if (StringUtil.notEmpty(goTripInfo.airco)) {
                            if (SessionContext.getAirCompanyMap().size() > 0
                                    && SessionContext.getAirCompanyMap().containsKey(goTripInfo.airco)) {
                                AirCompanyInfoBean companyInfoBean = SessionContext.getAirCompanyMap().get(goTripInfo.airco);
                                System.out.println(companyInfoBean.toString());
                                tv_company.setText(companyInfoBean.company);
                                iv_flight_icon.setVisibility(View.VISIBLE);
                                Glide.with(this)
                                        .load(new CustomReqURLFormatModelImpl(companyInfoBean.logourl))
                                        .fitCenter()
                                        .override(Utils.dp2px(15), Utils.dp2px(15))
                                        .into(iv_flight_icon);
                            } else {
                                tv_company.setText(goTripInfo.airco);
                                iv_flight_icon.setVisibility(View.INVISIBLE);
                            }
                        } else {
                            iv_flight_icon.setVisibility(View.INVISIBLE);
                        }

                        //去程乘机人信息
                        {
                            if (goTripInfo.passengerList != null) {
                                goFlightView.findViewById(R.id.ticket_count_layout).setVisibility(View.VISIBLE);
                                TextView tv_ticket_count = (TextView) goFlightView.findViewById(R.id.tv_ticket_count);
                                TextView tv_build_count = (TextView) goFlightView.findViewById(R.id.tv_build_count);
                                TextView tv_build_price = (TextView) goFlightView.findViewById(R.id.tv_build_price);
                                TextView tv_build_detail = (TextView) goFlightView.findViewById(R.id.tv_build_detail);
                                TextView tv_yanwu_count = (TextView) goFlightView.findViewById(R.id.tv_yanwu_count);
                                TextView tv_yanwu_price = (TextView) goFlightView.findViewById(R.id.tv_yanwu_price);
                                TextView tv_yanwu_detail = (TextView) goFlightView.findViewById(R.id.tv_yanwu_detail);
                                TextView tv_yiwai_count = (TextView) goFlightView.findViewById(R.id.tv_yiwai_count);
                                TextView tv_yiwai_price = (TextView) goFlightView.findViewById(R.id.tv_yiwai_price);
                                TextView tv_yiwai_detail = (TextView) goFlightView.findViewById(R.id.tv_yiwai_detail);

                                tv_build_price.setText(String.valueOf(goTripInfo.buildAndFuelPrice));
                                tv_yanwu_price.setText(String.valueOf(goTripInfo.delayMoney));
                                tv_yiwai_price.setText(String.valueOf(goTripInfo.accidentMoney));

                                if (goTripInfo.delayCount > 0) {
                                    goFlightView.findViewById(R.id.yanwu_layout).setVisibility(View.VISIBLE);
                                }
                                if (goTripInfo.accidentCount > 0) {
                                    goFlightView.findViewById(R.id.yiwai_layout).setVisibility(View.VISIBLE);
                                }

                                int passengerCount = goTripInfo.passengerList.size();
                                if (passengerCount > 1) {
                                    tv_ticket_count.setVisibility(View.VISIBLE);
                                    tv_ticket_count.setText(String.format(getString(R.string.multipleSign) + "%1$d", passengerCount));
                                    tv_build_count.setVisibility(View.VISIBLE);
                                    tv_build_count.setText(String.format(getString(R.string.multipleSign) + "%1$d", passengerCount));
                                    tv_yanwu_count.setVisibility(View.VISIBLE);
                                    tv_yanwu_count.setText(String.format(getString(R.string.multipleSign) + "%1$d", passengerCount));
                                    tv_yiwai_count.setVisibility(View.VISIBLE);
                                    tv_yiwai_count.setText(String.format(getString(R.string.multipleSign) + "%1$d", passengerCount));
                                }

                                View goPassengerActionLayout = LayoutInflater.from(this).inflate(R.layout.layout_plane_orderdetail_passenger_action_item, null);
                                flightLayout.addView(goPassengerActionLayout);
                                LinearLayout goPassengerLayout = (LinearLayout) goPassengerActionLayout.findViewById(R.id.passenger_lay);
                                goPassengerLayout.removeAllViews();
                                for (int i = 0; i < goTripInfo.passengerList.size(); i++) {
                                    View v = LayoutInflater.from(this).inflate(R.layout.layout_plane_orderdetail_personinfo_item, null);
                                    goPassengerLayout.addView(v);
                                    TextView tv_name = (TextView) v.findViewById(R.id.tv_name);
                                    TextView tv_status = (TextView) v.findViewById(R.id.tv_status);
                                    TextView tv_cardType = (TextView) v.findViewById(R.id.tv_cardType);
                                    TextView tv_cardId = (TextView) v.findViewById(R.id.tv_cardId);
                                    tv_name.setText(goTripInfo.passengerList.get(i).name);
                                    tv_status.setText(PlaneCommDef.TicketStatus.statusCodeOf(goTripInfo.passengerList.get(i).status).getStatusMsg());
                                    tv_cardType.setText(PlaneCommDef.CardType.valueOf(goTripInfo.passengerList.get(i).cardType).getValueName());
                                    tv_cardId.setText(goTripInfo.passengerList.get(i).cardNo);
                                }
                                TextView tv_back = (TextView) goPassengerActionLayout.findViewById(R.id.tv_back);
                                TextView tv_change = (TextView) goPassengerActionLayout.findViewById(R.id.tv_change);
                                final PlaneOrderDetailInfoBean.TripInfo finalGoTripInfo = goTripInfo;
                                tv_back.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        backPlaneTicketAction(finalGoTripInfo);
                                    }
                                });

                                tv_change.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        changePlaneTicketAction(finalGoTripInfo);
                                    }
                                });
                            }
                        }
                    }


                    //返程信息
                    {
                        if (null != backTripInfo) {
                            final View backFlightView = LayoutInflater.from(this).inflate(R.layout.layout_plane_orderdetail_flightinfo_item, null);
                            flightLayout.addView(backFlightView);
                            TextView tv_flag = (TextView) backFlightView.findViewById(R.id.tv_flag);
                            TextView tv_date = (TextView) backFlightView.findViewById(R.id.tv_date);
                            TextView tv_price = (TextView) backFlightView.findViewById(R.id.tv_price);
                            TextView tv_off_time = (TextView) backFlightView.findViewById(R.id.tv_off_time);
                            TextView tv_on_time = (TextView) backFlightView.findViewById(R.id.tv_on_time);
                            TextView tv_during = (TextView) backFlightView.findViewById(R.id.tv_during);
                            TextView tv_off_airport = (TextView) backFlightView.findViewById(R.id.tv_off_airport);
                            TextView tv_on_airport = (TextView) backFlightView.findViewById(R.id.tv_on_airport);
                            TextView tv_company = (TextView) backFlightView.findViewById(R.id.tv_company);
                            TextView tv_code = (TextView) backFlightView.findViewById(R.id.tv_code);
                            ImageView iv_flight_icon = (ImageView) backFlightView.findViewById(R.id.iv_flight_icon);

                            tv_flag.setText("返程");
                            tv_date.setText(DateUtil.getDay("MM月dd日", backTripInfo.sDate) + " " + backTripInfo.sTime);
                            tv_price.setText(String.valueOf(backTripInfo.shouldPayAmount));
                            tv_off_time.setText(backTripInfo.sTime);
                            tv_on_time.setText(backTripInfo.eTime);
                            tv_during.setText(backTripInfo.flyTime);
                            tv_off_airport.setText(backTripInfo.sAirport + backTripInfo.sTerminal);
                            tv_on_airport.setText(backTripInfo.eAirport + backTripInfo.eTerminal);
                            tv_code.setText(backTripInfo.flightNo);
                            if (StringUtil.notEmpty(backTripInfo.airco)) {
                                if (SessionContext.getAirCompanyMap().size() > 0
                                        && SessionContext.getAirCompanyMap().containsKey(backTripInfo.airco)) {
                                    AirCompanyInfoBean companyInfoBean = SessionContext.getAirCompanyMap().get(backTripInfo.airco);
                                    tv_company.setText(companyInfoBean.company);
                                    iv_flight_icon.setVisibility(View.VISIBLE);
                                    Glide.with(this)
                                            .load(new CustomReqURLFormatModelImpl(companyInfoBean.logourl))
                                            .fitCenter()
                                            .override(Utils.dp2px(15), Utils.dp2px(15))
                                            .into(iv_flight_icon);
                                } else {
                                    tv_company.setText(backTripInfo.airco);
                                    iv_flight_icon.setVisibility(View.INVISIBLE);
                                }
                            } else {
                                iv_flight_icon.setVisibility(View.INVISIBLE);
                            }

                            //返程乘机人信息
                            {
                                if (backTripInfo.passengerList != null) {

                                    backFlightView.findViewById(R.id.ticket_count_layout).setVisibility(View.VISIBLE);
                                    TextView tv_ticket_count = (TextView) backFlightView.findViewById(R.id.tv_ticket_count);
                                    TextView tv_build_count = (TextView) backFlightView.findViewById(R.id.tv_build_count);
                                    TextView tv_build_price = (TextView) backFlightView.findViewById(R.id.tv_build_price);
                                    TextView tv_build_detail = (TextView) backFlightView.findViewById(R.id.tv_build_detail);
                                    TextView tv_yanwu_count = (TextView) backFlightView.findViewById(R.id.tv_yanwu_count);
                                    TextView tv_yanwu_price = (TextView) backFlightView.findViewById(R.id.tv_yanwu_price);
                                    TextView tv_yanwu_detail = (TextView) backFlightView.findViewById(R.id.tv_yanwu_detail);
                                    TextView tv_yiwai_count = (TextView) backFlightView.findViewById(R.id.tv_yiwai_count);
                                    TextView tv_yiwai_price = (TextView) backFlightView.findViewById(R.id.tv_yiwai_price);
                                    TextView tv_yiwai_detail = (TextView) backFlightView.findViewById(R.id.tv_yiwai_detail);

                                    tv_build_price.setText(String.valueOf(backTripInfo.buildAndFuelPrice));
                                    tv_yanwu_price.setText(String.valueOf(backTripInfo.delayMoney));
                                    tv_yiwai_price.setText(String.valueOf(backTripInfo.accidentMoney));

                                    if (backTripInfo.delayCount > 0) {
                                        backFlightView.findViewById(R.id.yanwu_layout).setVisibility(View.VISIBLE);
                                    }
                                    if (backTripInfo.accidentCount > 0) {
                                        backFlightView.findViewById(R.id.yiwai_layout).setVisibility(View.VISIBLE);
                                    }

                                    int passengerCount = backTripInfo.passengerList.size();
                                    if (passengerCount > 1) {
                                        tv_ticket_count.setVisibility(View.VISIBLE);
                                        tv_ticket_count.setText(String.format(getString(R.string.multipleSign) + "%1$d", passengerCount));
                                        tv_build_count.setVisibility(View.VISIBLE);
                                        tv_build_count.setText(String.format(getString(R.string.multipleSign) + "%1$d", passengerCount));
                                        tv_yanwu_count.setVisibility(View.VISIBLE);
                                        tv_yanwu_count.setText(String.format(getString(R.string.multipleSign) + "%1$d", passengerCount));
                                        tv_yiwai_count.setVisibility(View.VISIBLE);
                                        tv_yiwai_count.setText(String.format(getString(R.string.multipleSign) + "%1$d", passengerCount));
                                    }

                                    View backPassengerActionLayout = LayoutInflater.from(this).inflate(R.layout.layout_plane_orderdetail_passenger_action_item, null);
                                    flightLayout.addView(backPassengerActionLayout);
                                    LinearLayout backPassengerLayout = (LinearLayout) backPassengerActionLayout.findViewById(R.id.passenger_lay);
                                    backPassengerLayout.removeAllViews();
                                    for (int i = 0; i < backTripInfo.passengerList.size(); i++) {
                                        View v = LayoutInflater.from(this).inflate(R.layout.layout_plane_orderdetail_personinfo_item, null);
                                        backPassengerLayout.addView(v);

                                        TextView tv_name = (TextView) v.findViewById(R.id.tv_name);
                                        TextView tv_status = (TextView) v.findViewById(R.id.tv_status);
                                        TextView tv_cardType = (TextView) v.findViewById(R.id.tv_cardType);
                                        TextView tv_cardId = (TextView) v.findViewById(R.id.tv_cardId);

                                        tv_name.setText(backTripInfo.passengerList.get(i).name);
                                        tv_status.setText(PlaneCommDef.TicketStatus.statusCodeOf(backTripInfo.passengerList.get(i).status).getStatusMsg());
                                        tv_cardType.setText(PlaneCommDef.CardType.valueOf(backTripInfo.passengerList.get(i).cardType).getValueName());
                                        tv_cardId.setText(backTripInfo.passengerList.get(i).cardNo);
                                    }
                                    TextView tv_back = (TextView) backPassengerActionLayout.findViewById(R.id.tv_back);
                                    TextView tv_change = (TextView) backPassengerActionLayout.findViewById(R.id.tv_change);
                                    final PlaneOrderDetailInfoBean.TripInfo finalBackTripInfo = backTripInfo;
                                    tv_back.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            backPlaneTicketAction(finalBackTripInfo);
                                        }
                                    });

                                    tv_change.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            changePlaneTicketAction(finalBackTripInfo);
                                        }
                                    });
                                }
                            }

                            backFlightView.post(new Runnable() {
                                @Override
                                public void run() {
                                    backViewTop = backFlightView.getTop();
                                    System.out.println("backViewTop = " + backViewTop);
                                }
                            });
                        }

                        System.out.println("orderType = " + orderType);
                        if (orderType == BACK_TRIP) {
                            scrollView.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    scrollView.scrollTo(0, backViewTop + Utils.dp2px(1));
                                }
                            }, 10);
                        }
                    }
                }
            }
            scrollView.setVisibility(View.VISIBLE);
        } else {
            scrollView.setVisibility(View.GONE);
        }
    }

    private void requestPlaneOrderDetailInfo(String orderId) {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("orderNum", orderId);
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.PLANE_ORDER_DETAIL;
        d.flag = AppConst.PLANE_ORDER_DETAIL;

        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    @Override
    protected void onNotifyMessage(ResponseData request, ResponseData response) {
        super.onNotifyMessage(request, response);
        if (response != null && response.body != null) {
            if (request.flag == AppConst.PLANE_ORDER_DETAIL) {
                removeProgressDialog();
                LogUtil.i(response.body.toString());
                orderDetailInfoBean = JSON.parseObject(response.body.toString(), PlaneOrderDetailInfoBean.class);
                refreshDetailInfo();
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
                rebookIntent.putExtra("rebookPlane", true);
                rebookIntent.putExtra("isClosed", true);
                startActivity(rebookIntent);
                break;
        }
    }

    private void backPlaneTicketAction(PlaneOrderDetailInfoBean.TripInfo tripInfo) {
        System.out.println("backPlaneTicketAction()");
        Intent intent = new Intent(this, PlaneBackTicketActivity.class);
        intent.putExtra("tripInfo", tripInfo);
        startActivity(intent);
    }

    private void changePlaneTicketAction(PlaneOrderDetailInfoBean.TripInfo tripInfo) {
        System.out.println("changePlaneTicketAction()");
        CustomToast.show("翻滚吧，青年！现在还不能改签。", CustomToast.LENGTH_LONG);
    }
}
