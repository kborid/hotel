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
import com.alibaba.fastjson.JSONObject;
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

import java.io.Serializable;
import java.util.LinkedHashMap;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author kborid
 * @date 2017/3/10 0010
 */
public class PlaneOrderDetailActivity extends BaseAppActivity {

    private static final int BACK_TICKET = 0x003;
    private static final int CHANGE_TICKET = 0x004;
    private static final int BACK_TRIP = 1;
    private static final int GO_TRIP = 2;

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

            PlaneCommDef.TicketOrderStatus ticketStatus = PlaneCommDef.TicketOrderStatus.statusCodeOf(Integer.valueOf(orderDetailInfoBean.orderStatus));
            refreshBtnActionStatus(ticketStatus);

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
                        tv_price.setText(String.format(getString(R.string.rmbStr2), goTripInfo.shouldPayAmount));
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

                                tv_build_price.setText(String.format(getString(R.string.rmbStr2), goTripInfo.buildAndFuelPrice));
                                tv_yanwu_price.setText(String.format(getString(R.string.rmbStr2), goTripInfo.delayMoney));
                                tv_yiwai_price.setText(String.format(getString(R.string.rmbStr2), goTripInfo.accidentMoney));

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
                                boolean canBack = false, canChange = false;
                                for (int i = 0; i < goTripInfo.passengerList.size(); i++) {
                                    View v = LayoutInflater.from(this).inflate(R.layout.layout_plane_orderdetail_personinfo_item, null);
                                    goPassengerLayout.addView(v);
                                    TextView tv_name = (TextView) v.findViewById(R.id.tv_name);
                                    TextView tv_status = (TextView) v.findViewById(R.id.tv_status);
                                    TextView tv_cardType = (TextView) v.findViewById(R.id.tv_cardType);
                                    TextView tv_cardId = (TextView) v.findViewById(R.id.tv_cardId);
                                    tv_name.setText(goTripInfo.passengerList.get(i).name);
                                    PlaneCommDef.PassengerStatus status = PlaneCommDef.PassengerStatus.statusCodeOf(goTripInfo.passengerList.get(i).status);
                                    if (ticketStatus == PlaneCommDef.TicketOrderStatus.TICKET_CANCELED
                                            || status != PlaneCommDef.PassengerStatus.Canceled) {
                                        tv_status.setVisibility(View.VISIBLE);
                                        tv_status.setText(status.getStatusMsg());
                                    }
                                    tv_cardType.setText(PlaneCommDef.CardType.valueOf(goTripInfo.passengerList.get(i).cardType).getValueName());
                                    tv_cardId.setText(goTripInfo.passengerList.get(i).cardNo);
                                    canBack = canBack || canBackTicket(status);
                                    canChange = canChange || canChangeTicket(status);
                                }
                                TextView tv_back = (TextView) goPassengerActionLayout.findViewById(R.id.tv_back);
                                TextView tv_change = (TextView) goPassengerActionLayout.findViewById(R.id.tv_change);
                                tv_back.setEnabled(canBack);
                                tv_change.setEnabled(canChange);
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
                            tv_price.setText(String.format(getString(R.string.rmbStr2), backTripInfo.shouldPayAmount));
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

                                    tv_build_price.setText(String.format(getString(R.string.rmbStr2), backTripInfo.buildAndFuelPrice));
                                    tv_yanwu_price.setText(String.format(getString(R.string.rmbStr2), backTripInfo.delayMoney));
                                    tv_yiwai_price.setText(String.format(getString(R.string.rmbStr2), backTripInfo.accidentMoney));

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
                                    boolean canBack = false, canChange = false;
                                    for (int i = 0; i < backTripInfo.passengerList.size(); i++) {
                                        View v = LayoutInflater.from(this).inflate(R.layout.layout_plane_orderdetail_personinfo_item, null);
                                        backPassengerLayout.addView(v);

                                        TextView tv_name = (TextView) v.findViewById(R.id.tv_name);
                                        TextView tv_status = (TextView) v.findViewById(R.id.tv_status);
                                        TextView tv_cardType = (TextView) v.findViewById(R.id.tv_cardType);
                                        TextView tv_cardId = (TextView) v.findViewById(R.id.tv_cardId);

                                        tv_name.setText(backTripInfo.passengerList.get(i).name);
                                        PlaneCommDef.PassengerStatus status = PlaneCommDef.PassengerStatus.statusCodeOf(backTripInfo.passengerList.get(i).status);
                                        if (ticketStatus == PlaneCommDef.TicketOrderStatus.TICKET_CANCELED
                                                || status != PlaneCommDef.PassengerStatus.Canceled) {
                                            tv_status.setVisibility(View.VISIBLE);
                                            tv_status.setText(status.getStatusMsg());
                                        }
                                        tv_cardType.setText(PlaneCommDef.CardType.valueOf(backTripInfo.passengerList.get(i).cardType).getValueName());
                                        tv_cardId.setText(backTripInfo.passengerList.get(i).cardNo);
                                        canBack = canBack || canBackTicket(status);
                                        canChange = canChange || canChangeTicket(status);
                                    }
                                    TextView tv_back = (TextView) backPassengerActionLayout.findViewById(R.id.tv_back);
                                    TextView tv_change = (TextView) backPassengerActionLayout.findViewById(R.id.tv_change);
                                    tv_back.setEnabled(canBack);
                                    tv_change.setEnabled(canChange);
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

    private void refreshBtnActionStatus(PlaneCommDef.TicketOrderStatus status) {
        switch (status) {
            case TICKET_CANCELED:
            case TICKET_FINISHED:
            case TICKET_OUTED_COMP:
            case TICKET_BACKED_COMP:
            case TICKET_CHANGED_COMP:
            case TICKET_PAID:
                tvPay.setEnabled(false);
                tvCancel.setEnabled(false);
                break;
            case TICKET_WAIT_PAY:
                tvPay.setEnabled(true);
                tvCancel.setEnabled(true);
                break;
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

    private void requestPlaneOrderCancel(String orderId) {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("orderNum", orderId);
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.PLANE_ORDER_CANCEL;
        d.flag = AppConst.PLANE_ORDER_CANCEL;

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
            } else if (request.flag == AppConst.PLANE_ORDER_CANCEL) {
                removeProgressDialog();
                LogUtil.i(response.body.toString());
                JSONObject mJson = JSON.parseObject(response.body.toString());
                if (mJson.containsKey("SUCCESS")) {
                    boolean isSuccess = mJson.getBoolean("SUCCESS");
                    String msg = mJson.containsKey("MSG") ? mJson.getString("MSG") : "";
                    if (StringUtil.isEmpty(msg)) {
                        msg = isSuccess ? getString(R.string.order_cancel_success) : getString(R.string.order_cancel_fail);
                    }
                    CustomToast.show(msg, CustomToast.LENGTH_SHORT);
                } else {
                    CustomToast.show(getString(R.string.order_cancel_fail), CustomToast.LENGTH_SHORT);
                }
                setResult(RESULT_OK);
                finish();
            }
        }
    }

    @OnClick({R.id.tv_pay, R.id.tv_cancel, R.id.tv_booking})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_pay:
                Intent payIntent = new Intent(this, PlaneOrderPayActivity.class);
                payIntent.putExtra("orderNo", orderDetailInfoBean.orderId);
                payIntent.putExtra("amount", orderDetailInfoBean.shouldPayAmount);
                payIntent.putExtra("tripInfoList", (Serializable) orderDetailInfoBean.tripList);
                startActivity(payIntent);
                break;
            case R.id.tv_cancel:
                requestPlaneOrderCancel(orderNo);
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
        intent.putExtra("tripId", tripInfo.tripId);
        intent.putExtra("plane_action", 0);
        startActivityForResult(intent, BACK_TICKET);
    }

    private void changePlaneTicketAction(PlaneOrderDetailInfoBean.TripInfo tripInfo) {
        System.out.println("changePlaneTicketAction()");
        Intent intent = new Intent(this, PlaneBackTicketActivity.class);
        intent.putExtra("tripId", tripInfo.tripId);
        intent.putExtra("plane_action", 1);
        startActivityForResult(intent, CHANGE_TICKET);
    }

    private boolean canBackTicket(PlaneCommDef.PassengerStatus status) {
        boolean tmpFlag = false;
        switch (status) {
            case Outed:
            case Changed_Failed:
            case Changed_Success:
                tmpFlag = true;
        }
        return tmpFlag;
    }

    private boolean canChangeTicket(PlaneCommDef.PassengerStatus status) {
        boolean tmpFlag = false;
        switch (status) {
            case Outed:
            case Backed_Failed:
                tmpFlag = true;
        }
        return tmpFlag;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK != resultCode) {
            return;
        }
        if (requestCode == BACK_TICKET || requestCode == CHANGE_TICKET) {
            requestPlaneOrderDetailInfo(orderNo);
        }
    }
}
