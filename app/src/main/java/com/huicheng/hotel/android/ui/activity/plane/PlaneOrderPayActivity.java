package com.huicheng.hotel.android.ui.activity.plane;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.broadcast.PayResultReceiver;
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.common.PlaneOrderManager;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.content.AppConst;
import com.huicheng.hotel.android.content.NetURL;
import com.huicheng.hotel.android.pay.PayCommDef;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.AirCompanyInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneBookingInfo;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneBookingInfo_FlightInfo;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneOrderDetailInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.PlanePaySuccessFlightInfo;
import com.huicheng.hotel.android.ui.activity.MainSwitcherActivity;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.huicheng.hotel.android.ui.custom.CommonPayChannelLayout;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.util.ActivityTack;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @auth kborid
 * @date 2017/12/7 0007.
 */

public class PlaneOrderPayActivity extends BaseAppActivity {

    private static final int GO_TRIP = 1;
    private static final int BACK_TRIP = 2;

    @BindView(R.id.tv_amount)
    TextView tvAmount;
    @BindView(R.id.payChannelLay)
    CommonPayChannelLayout payChannelLay;
    @BindView(R.id.tv_comment)
    TextView tvComment;
    @BindView(R.id.flight_layout)
    LinearLayout flightLayout;

    private PayResultReceiver mPayReceiver = new PayResultReceiver();

    private int mAmount = 0;
    private String mOrderNo = "";
    private List<PlaneBookingInfo> bookingInfoList = null;
    private PlaneBookingInfo goBookingInfo = null, backBookingInfo = null;
    private List<PlaneOrderDetailInfoBean.TripInfo> tripInfoList = null;
    private PlaneOrderDetailInfoBean.TripInfo goTripInfo = null, backTripInfo = null;

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_plane_orderpay_layout);
    }

    @Override
    protected void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            mOrderNo = bundle.getString("orderNo");
            mAmount = bundle.getInt("amount");
            bookingInfoList = (List<PlaneBookingInfo>) bundle.getSerializable("bookingInfoList");
            if (null != bookingInfoList) {
                goBookingInfo = bookingInfoList.get(0);
                PlaneOrderManager.instance.setGoPlanePaySuccessFlightInfo(new PlanePaySuccessFlightInfo(
                        goBookingInfo.flightInfo.get(0).carrier,
                        DateUtil.getDay("MM月dd日", DateUtil.str2Date(goBookingInfo.flightInfo.get(0).dptDate, "yyyy-MM-dd").getTime()) + " " + goBookingInfo.flightInfo.get(0).dptTime,
                        goBookingInfo.flightInfo.get(0).flightNum));
                if (bookingInfoList.size() > 1) {
                    backBookingInfo = bookingInfoList.get(1);
                    PlaneOrderManager.instance.setBackPlanePaySuccessFlightInfo(new PlanePaySuccessFlightInfo(
                            backBookingInfo.flightInfo.get(0).carrier,
                            DateUtil.getDay("MM月dd日", DateUtil.str2Date(backBookingInfo.flightInfo.get(0).dptDate, "yyyy-MM-dd").getTime()) + " " + backBookingInfo.flightInfo.get(0).dptTime,
                            backBookingInfo.flightInfo.get(0).flightNum));
                }
            }
            tripInfoList = (List<PlaneOrderDetailInfoBean.TripInfo>) bundle.getSerializable("tripInfoList");
            if (null != tripInfoList) {
                for (PlaneOrderDetailInfoBean.TripInfo tripInfo : tripInfoList) {
                    if (tripInfo.tripType == GO_TRIP) { //去程信息
                        goTripInfo = tripInfo;
                        PlaneOrderManager.instance.setGoPlanePaySuccessFlightInfo(new PlanePaySuccessFlightInfo(
                                goTripInfo.airco,
                                DateUtil.getDay("MM月dd日", tripInfo.sDate) + " " + DateUtil.dateToWeek2(new Date(tripInfo.sDate)),
                                goTripInfo.flightNo));
                    } else if (tripInfo.tripType == BACK_TRIP) { //返程信息
                        backTripInfo = tripInfo;
                        PlaneOrderManager.instance.setBackPlanePaySuccessFlightInfo(new PlanePaySuccessFlightInfo(
                                backTripInfo.airco,
                                DateUtil.getDay("MM月dd日", tripInfo.sDate) + " " + DateUtil.dateToWeek2(new Date(tripInfo.sDate)),
                                backTripInfo.flightNo));
                    }
                }
            }
        }
    }

    @Override
    public void initParams() {
        super.initParams();
        findViewById(R.id.comm_title_rl).setBackgroundColor(getResources().getColor(R.color.white));
        tv_center_title.setText("支付方式");
        tvAmount.setText(String.format(getString(R.string.rmbStr2), mAmount));
        refreshFlightLayoutInfo();

        // 动态注册支付广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadCastConst.ACTION_PAY_STATUS);
        LocalBroadcastManager.getInstance(this).registerReceiver(mPayReceiver, intentFilter);
    }

    private void refreshFlightLayoutInfo() {
        flightLayout.removeAllViews();
        if (null != tripInfoList) {
            if (null != goTripInfo) {
                View goFlightView = LayoutInflater.from(this).inflate(R.layout.layout_plane_pay_item, null);
                flightLayout.addView(goFlightView);
                setFlightViewInfo(goFlightView, goTripInfo);
                if (null != backTripInfo) {
                    View line = new View(this);
                    line.setBackgroundColor(Color.parseColor("#cccccc"));
                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dp2px(0.5f));
                    flightLayout.addView(line, llp);
                    View backFlightView = LayoutInflater.from(this).inflate(R.layout.layout_plane_pay_item, null);
                    flightLayout.addView(backFlightView);
                    setFlightViewInfo(backFlightView, backTripInfo);
                }
            }
        } else {
            if (null != bookingInfoList) {
                if (null != goBookingInfo) {
                    View goFlightView = LayoutInflater.from(this).inflate(R.layout.layout_plane_pay_item, null);
                    flightLayout.addView(goFlightView);
                    setFlightViewInfo(goFlightView, goBookingInfo, GO_TRIP);
                    if (null != backBookingInfo) {
                        View line = new View(this);
                        line.setBackgroundColor(Color.parseColor("#cccccc"));
                        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dp2px(0.5f));
                        flightLayout.addView(line, llp);
                        View backFlightView = LayoutInflater.from(this).inflate(R.layout.layout_plane_pay_item, null);
                        flightLayout.addView(backFlightView);
                        setFlightViewInfo(backFlightView, backBookingInfo, BACK_TRIP);
                    }
                }
            }
        }
    }

    private void requestOrderPayInfo(String orderNo) {
        LogUtil.i(TAG, "requestOrderPayInfo()");
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("orderNo", orderNo);
        b.addBody("tradeType", "02"); // 01 酒店业务，02 机票业务
        b.addBody("payChannel", payChannelLay.getPayChannel());

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.PAY;
        d.flag = AppConst.PAY;

        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            iv_back.performClick();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_back:
                CustomDialog dialog = new CustomDialog(this);
                dialog.setTitle("温馨提示");
                dialog.setMessage("您将离开，该订单请在15分钟内完成支付，否则订单自动取消。\n\n离开之后，您可以在\n【个人中心】→【我的订单】中继续支付。");
                dialog.setPositiveButton("继续支付", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.setNegativeButton("确认离开", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (ActivityTack.getInstanse().isExitActivity(PlaneTicketListActivity.class)) {
                            Intent intent = new Intent(PlaneOrderPayActivity.this, MainSwitcherActivity.class);
                            intent.putExtra("isClosed", true);
                            startActivity(intent);
                        } else {
                            finish();
                        }
                    }
                });
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                break;
        }
    }

    @OnClick({R.id.tv_detail, R.id.btn_pay})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_pay:
                if (StringUtil.notEmpty(mOrderNo)) {
                    requestOrderPayInfo(mOrderNo);
                }
                break;
            case R.id.tv_detail:
                if (StringUtil.notEmpty(mOrderNo)) {
                    requestOrderPayInfoDetail(mOrderNo);
                }
                break;
        }
    }

    private void requestOrderPayInfoDetail(String orderNo) {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("orderNum", orderNo);
        ResponseData d = b.syncRequest(b);
        d.flag = AppConst.PLANE_ORDER_INFO;
        d.path = NetURL.PLANE_ORDER_INFO;
        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    @Override
    protected void onNotifyMessage(ResponseData request, ResponseData response) {
        super.onNotifyMessage(request, response);
        if (response != null && response.body != null) {
            if (request.flag == AppConst.PAY) {
                LogUtil.i("json = " + response.body.toString());
                removeProgressDialog();
                if (StringUtil.notEmpty(response.body.toString()) && !"{}".equals(response.body.toString())) {
                    JSONObject json = JSONObject.parseObject(response.body.toString());
                    if (json.containsKey("status") && json.getString("status").equals("noneedpay")) {
                        LocalBroadcastManager.getInstance(this).sendBroadcast(
                                new Intent(BroadCastConst.ACTION_PAY_STATUS)
                                        .putExtra("type", "noneedpay")
                                        .putExtra("info", "noneedpay")
                                        .putExtra("module", PayResultReceiver.MODULE_PLANE));
                    } else {
                        startPay(json);
                    }
                } else {
                    LocalBroadcastManager.getInstance(this).sendBroadcast(
                            new Intent(BroadCastConst.ACTION_PAY_STATUS)
                                    .putExtra("type", PayCommDef.CUSTOMPAY)
                                    .putExtra("module", PayResultReceiver.MODULE_PLANE));
                }
            } else if (request.flag == AppConst.PLANE_ORDER_INFO) {
                removeProgressDialog();
                LogUtil.i("json = " + response.body.toString());
                showPlaneOrderDetailInfo(response.body.toString());
            }
        }
    }

    private void startPay(JSONObject mJson) {
        if (mJson.containsKey(HotelCommDef.ALIPAY)) {
        } else if (mJson.containsKey(HotelCommDef.WXPAY)) {
        } else if (mJson.containsKey(HotelCommDef.UNIONPAY)) {
        } else {
            CustomToast.show("支付失败", CustomToast.LENGTH_SHORT);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mPayReceiver) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mPayReceiver);
        }
    }

    private void setFlightViewInfo(View v, PlaneBookingInfo detailInfo, int tripType) {
        PlaneBookingInfo_FlightInfo flightInfo = detailInfo.flightInfo.get(0);
        Date date = DateUtil.str2Date(flightInfo.dptDate, "yyyy-MM-dd");
        ((TextView) v.findViewById(R.id.tv_flight_date)).setText(DateUtil.getDay("MM月dd日", date.getTime()) + " " + DateUtil.dateToWeek2(date));
        String city = "";
        if (GO_TRIP == tripType) {
            city = PlaneOrderManager.instance.getGoFlightOffAirportInfo().cityname + " - " + PlaneOrderManager.instance.getGoFlightOnAirportInfo().cityname;
        } else if (BACK_TRIP == tripType) {
            city = PlaneOrderManager.instance.getBackFlightOffAirportInfo().cityname + " - " + PlaneOrderManager.instance.getBackFlightOnAirportInfo().cityname;
        }
        ((TextView) v.findViewById(R.id.tv_flight_city)).setText(city);
        ((TextView) v.findViewById(R.id.tv_flight_cabin)).setText(flightInfo.ccbcn);
        ((TextView) v.findViewById(R.id.tv_flight_off_time)).setText(flightInfo.dptTime);
        ((TextView) v.findViewById(R.id.tv_flight_off_airport)).setText(flightInfo.dptAirport + flightInfo.dptTerminal);
        ((TextView) v.findViewById(R.id.tv_flight_on_time)).setText(flightInfo.arrTime);
        ((TextView) v.findViewById(R.id.tv_flight_on_airport)).setText(flightInfo.arrAirport + flightInfo.arrTerminal);
        LinearLayout stopLayout = (LinearLayout) v.findViewById(R.id.stopover_lay);
        if (flightInfo.stops <= 0) {
            stopLayout.setVisibility(View.GONE);
        } else {
            stopLayout.setVisibility(View.VISIBLE);
            ((TextView) v.findViewById(R.id.tv_flight_stopover)).setText("经停" + flightInfo.stopCity);
        }
        ((TextView) v.findViewById(R.id.tv_flight_during)).setText(flightInfo.flightTimes);

        //航班基本信息
        LinearLayout flightBaseInfoLay = (LinearLayout) v.findViewById(R.id.flight_base_info_lay);
        flightBaseInfoLay.removeAllViews();
        ArrayList<String> items = new ArrayList<>();
        //航空公司、航班号
        if (StringUtil.notEmpty(flightInfo.carrier)) {
            String item = flightInfo.carrier;
            if (SessionContext.getAirCompanyMap().size() > 0
                    && SessionContext.getAirCompanyMap().containsKey(flightInfo.carrier)) {
                AirCompanyInfoBean companyInfoBean = SessionContext.getAirCompanyMap().get(flightInfo.carrier);
                item = companyInfoBean.company + " " + flightInfo.flightNum;
            }
            items.add(item);
        }
        //航班型号
        //准点率
        //有无餐食

        for (int i = 0; i < items.size(); i++) {
            TextView tv_item = new TextView(this);
            tv_item.setTextColor(Color.parseColor("#666666"));
            tv_item.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            tv_item.setText(items.get(i));
            flightBaseInfoLay.addView(tv_item);
            if (i < items.size() - 1) {
                View line = new View(this);
                line.setBackgroundColor(Color.parseColor("#666666"));
                LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(Utils.dp2px(0.5f), ViewGroup.LayoutParams.MATCH_PARENT);
                llp.setMargins(Utils.dp2px(5), Utils.dp2px(2), Utils.dp2px(5), Utils.dp2px(2));
                flightBaseInfoLay.addView(line, llp);
            }
        }
    }

    private void setFlightViewInfo(View v, PlaneOrderDetailInfoBean.TripInfo tripInfo) {
        ((TextView) v.findViewById(R.id.tv_flight_date)).setText(DateUtil.getDay("MM月dd日", tripInfo.sDate) + " " + DateUtil.dateToWeek2(new Date(tripInfo.sDate)));
        ((TextView) v.findViewById(R.id.tv_flight_city)).setText(tripInfo.scity + " - " + tripInfo.ecity);
        ((TextView) v.findViewById(R.id.tv_flight_cabin)).setText(tripInfo.airCabin);
        ((TextView) v.findViewById(R.id.tv_flight_off_time)).setText(tripInfo.sTime);
        ((TextView) v.findViewById(R.id.tv_flight_off_airport)).setText(tripInfo.sAirport + tripInfo.sTerminal);
        ((TextView) v.findViewById(R.id.tv_flight_on_time)).setText(tripInfo.eTime);
        ((TextView) v.findViewById(R.id.tv_flight_on_airport)).setText(tripInfo.eAirport + tripInfo.eTerminal);
        ((TextView) v.findViewById(R.id.tv_flight_during)).setText(tripInfo.flyTime);
        LinearLayout stopLayout = (LinearLayout) v.findViewById(R.id.stopover_lay);
        if (tripInfo.stops <= 0) {
            stopLayout.setVisibility(View.GONE);
        } else {
            stopLayout.setVisibility(View.VISIBLE);
            ((TextView) v.findViewById(R.id.tv_flight_stopover)).setText("经停" + tripInfo.stopCity);
        }

        //航班基本信息
        LinearLayout flightBaseInfoLay = (LinearLayout) v.findViewById(R.id.flight_base_info_lay);
        flightBaseInfoLay.removeAllViews();
        ArrayList<String> items = new ArrayList<>();
        //航空公司、航班号
        if (StringUtil.notEmpty(tripInfo.airco)) {
            String item = tripInfo.airco;
            if (SessionContext.getAirCompanyMap().size() > 0
                    && SessionContext.getAirCompanyMap().containsKey(tripInfo.airco)) {
                AirCompanyInfoBean companyInfoBean = SessionContext.getAirCompanyMap().get(tripInfo.airco);
                item = companyInfoBean.company + " " + tripInfo.flightNo;
            }
            items.add(item);
        }
        //航班型号
        //准点率
        //有无餐食

        for (int i = 0; i < items.size(); i++) {
            TextView tv_item = new TextView(this);
            tv_item.setTextColor(Color.parseColor("#666666"));
            tv_item.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            tv_item.setText(items.get(i));
            flightBaseInfoLay.addView(tv_item);
            if (i < items.size() - 1) {
                View line = new View(this);
                line.setBackgroundColor(Color.parseColor("#666666"));
                LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(Utils.dp2px(0.5f), ViewGroup.LayoutParams.MATCH_PARENT);
                llp.setMargins(Utils.dp2px(5), Utils.dp2px(2), Utils.dp2px(5), Utils.dp2px(2));
                flightBaseInfoLay.addView(line, llp);
            }
        }
    }

    private void showPlaneOrderDetailInfo(String jsonStr) {
        if (StringUtil.notEmpty(jsonStr)) {
            final CustomDialog dialog = new CustomDialog(this);
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_order_detail_layout, null);
            dialog.addView(view);
            LinearLayout service_lay = (LinearLayout) view.findViewById(R.id.service_lay);
            service_lay.removeAllViews();
            JSONObject mJson = JSON.parseObject(jsonStr);
            if (mJson.containsKey("priceInfos")) {
                List<JSONObject> jsonArr = JSON.parseArray(mJson.getString("priceInfos"), JSONObject.class);
                for (JSONObject json : jsonArr) {
                    View commView = LayoutInflater.from(this).inflate(R.layout.dialog_order_detail_item, null);
                    ((TextView) commView.findViewById(R.id.tv_title)).setText(json.getString("name"));
                    ((TextView) commView.findViewById(R.id.tv_price)).setText(String.format(getString(R.string.rmbStr2), json.getInteger("price")));
                    service_lay.addView(commView);
                }
            }
            dialog.setNegativeButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        }
    }
}
