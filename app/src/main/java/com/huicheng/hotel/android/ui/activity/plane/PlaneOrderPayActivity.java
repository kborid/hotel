package com.huicheng.hotel.android.ui.activity.plane;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.broadcast.PayResultReceiver;
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.content.AppConst;
import com.huicheng.hotel.android.content.NetURL;
import com.huicheng.hotel.android.pay.PayCommDef;
import com.huicheng.hotel.android.pay.alipay.AlipayUtil;
import com.huicheng.hotel.android.pay.unionpay.UnionPayUtil;
import com.huicheng.hotel.android.pay.wxpay.WXPayUtils;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneOrderDetailInfoBean;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.huicheng.hotel.android.ui.custom.CommonPayChannelLayout;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @auth kborid
 * @date 2017/12/7 0007.
 */

public class PlaneOrderPayActivity extends BaseAppActivity {

    @BindView(R.id.tv_amount)
    TextView tvAmount;
    @BindView(R.id.payChannelLay)
    CommonPayChannelLayout payChannelLay;
    @BindView(R.id.tv_comment)
    TextView tvComment;
    @BindView(R.id.flight_layout)
    LinearLayout flightLayout;
    //<include layout="@layout/layout_plane_pay_item" />

    private PayResultReceiver mPayReceiver = new PayResultReceiver();
    private AlipayUtil alipay = null;
    private WXPayUtils wxpay = null;
    private UnionPayUtil unionpay = null;

    private int mAmount = 0;
    private String mOrderNo = "";
    private PlaneNewOrderActivity.FlightDetailInfo goFlightDetailInfo = null;
    private PlaneNewOrderActivity.FlightDetailInfo backFlightDetailInfo = null;
    private List<PlaneOrderDetailInfoBean.TripInfo> tripInfoList = null;

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
            goFlightDetailInfo = (PlaneNewOrderActivity.FlightDetailInfo) bundle.getSerializable("goFlightDetailInfo");
            backFlightDetailInfo = (PlaneNewOrderActivity.FlightDetailInfo) bundle.getSerializable("backFlightDetailInfo");
            tripInfoList = (List<PlaneOrderDetailInfoBean.TripInfo>) bundle.getSerializable("tripInfoList");
        }
    }

    @Override
    public void initParams() {
        super.initParams();
        findViewById(R.id.comm_title_rl).setBackgroundColor(getResources().getColor(R.color.white));
        tv_center_title.setText("支付方式");
        tvAmount.setText(String.format(getString(R.string.rmbStr2), mAmount));
        System.out.println("tripInfoList = " + tripInfoList);
        System.out.println("goFlightDetailInfo = " + goFlightDetailInfo);
        System.out.println("backFlightDetailInfo = " + backFlightDetailInfo);
        refreshFlightLayoutInfo();

        alipay = new AlipayUtil(this);
        wxpay = new WXPayUtils(this);
        unionpay = new UnionPayUtil(this);
        // 动态注册支付广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadCastConst.ACTION_PAY_STATUS);
        LocalBroadcastManager.getInstance(this).registerReceiver(mPayReceiver, intentFilter);
    }

    private void refreshFlightLayoutInfo() {
        flightLayout.removeAllViews();
        if (null != tripInfoList && tripInfoList.size() > 0) {
            PlaneOrderDetailInfoBean.TripInfo goTripInfo = null, backTripInfo = null;
            for (int i = 0; i < tripInfoList.size(); i++) {
                PlaneOrderDetailInfoBean.TripInfo tripInfo = tripInfoList.get(i);
                if (tripInfo.tripType == 1) { //去程信息
                    goTripInfo = tripInfo;
                } else if (tripInfo.tripType == 2) { //返程信息
                    backTripInfo = tripInfo;
                }
            }
            if (null != goTripInfo) {
                View goFlightView = LayoutInflater.from(this).inflate(R.layout.layout_plane_pay_item, null);
                flightLayout.addView(goFlightView);
                if (null != backTripInfo) {
                    View line = new View(this);
                    line.setBackgroundColor(Color.parseColor("#cccccc"));
                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dp2px(0.5f));
                    flightLayout.addView(line, llp);
                    View backFlightView = LayoutInflater.from(this).inflate(R.layout.layout_plane_pay_item, null);
                    flightLayout.addView(backFlightView);
                }
            }
        } else {
            if (null != goFlightDetailInfo) {
                View goFlightView = LayoutInflater.from(this).inflate(R.layout.layout_plane_pay_item, null);
                flightLayout.addView(goFlightView);
                if (null != backFlightDetailInfo) {
                    View line = new View(this);
                    line.setBackgroundColor(Color.parseColor("#cccccc"));
                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dp2px(0.5f));
                    flightLayout.addView(line, llp);
                    View backFlightView = LayoutInflater.from(this).inflate(R.layout.layout_plane_pay_item, null);
                    flightLayout.addView(backFlightView);
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
//                startActivity(new Intent(this, MainSwitcherActivity.class));
                CustomToast.show("要支付！", CustomToast.LENGTH_LONG);
//                if (ActivityTack.getInstanse().isExitActivity(PlaneTicketListActivity.class)) {
//                    Intent intent = new Intent(PlaneOrderPayActivity.this, MainSwitcherActivity.class);
//                    intent.putExtra("isClosed", true);
//                    startActivity(intent);
//                } else {
//                    finish();
//                }
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
                startActivity(new Intent(this, PlaneOrderPaySuccessActivity.class));
                break;
        }
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
                                        .putExtra("info", "noneedpay"));
                    } else {
                        startPay(json);
                    }
                } else {
                    LocalBroadcastManager.getInstance(this).sendBroadcast(
                            new Intent(BroadCastConst.ACTION_PAY_STATUS)
                                    .putExtra("type", PayCommDef.CUSTOMPAY));
                }
            }
        }
    }

    private void startPay(JSONObject mJson) {
        if (mJson.containsKey(HotelCommDef.ALIPAY)) {
            //支付宝第三方支付
            alipay.pay(mJson.getString(HotelCommDef.ALIPAY));
        } else if (mJson.containsKey(HotelCommDef.WXPAY)) {
            //微信第三方支付
            JSONObject mmJson = mJson.getJSONObject(HotelCommDef.WXPAY);
            wxpay.sendPayReq(
                    mmJson.getString("package"),
                    mmJson.getString("appid"),
                    mmJson.getString("sign"),
                    mmJson.getString("partnerid"),
                    mmJson.getString("prepayid"),
                    mmJson.getString("noncestr"),
                    mmJson.getString("timestamp"));
        } else if (mJson.containsKey(HotelCommDef.UNIONPAY)) {
            //银联第三方支付
            JSONObject mmJson = mJson.getJSONObject(HotelCommDef.UNIONPAY);
            unionpay.setUnionPayServerMode(UnionPayUtil.RELEASE_MODE);
            unionpay.unionStartPay(mmJson.getString("tn"));
        } else {
            CustomToast.show("支付失败", CustomToast.LENGTH_SHORT);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wxpay = null;
        alipay = null;
        unionpay = null;
        if (null != mPayReceiver) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mPayReceiver);
        }
    }
}
