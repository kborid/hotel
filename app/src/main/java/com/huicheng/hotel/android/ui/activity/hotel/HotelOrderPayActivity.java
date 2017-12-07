package com.huicheng.hotel.android.ui.activity.hotel;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.broadcast.PayResultReceiver;
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.pay.alipay.AlipayUtil;
import com.huicheng.hotel.android.pay.qmf.QmfPayHelper;
import com.huicheng.hotel.android.pay.unionpay.UnionPayUtil;
import com.huicheng.hotel.android.pay.wxpay.WXPayUtils;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.OrderPayDetailInfoBean;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.CommonPayChannelLayout;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.prj.sdk.app.AppConst;
import com.prj.sdk.app.NetURL;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.util.ActivityTack;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.Utils;

import java.util.Date;

/**
 * @author kborid
 * @date 2017/3/7 0007
 */
public class HotelOrderPayActivity extends BaseActivity {

    private PayResultReceiver mPayReceiver = new PayResultReceiver();
    private OrderPayDetailInfoBean orderPayDetailInfoBean = null;
    private String orderId, orderType;
    private int during = 0;

    private LinearLayout root_lay;
    private TextView tv_address, tv_date, tv_room_name, tv_total_price, tv_detail, tv_comment;
    private TextView tv_room_count, tv_during;
    private Button btn_pay;

    private CommonPayChannelLayout payChannelLay;
    private AlipayUtil alipay = null;
    private WXPayUtils wxpay = null;
    private UnionPayUtil unionpay = null;
    //qmf pay
    private QmfPayHelper qmfPayHelper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_hotel_orderpay_layout);
        initViews();
        initParams();
        initListeners();
        if (null == savedInstanceState) {
            requestOrderDetailInfo();
        }
    }

    @Override
    public void initViews() {
        super.initViews();
        root_lay = (LinearLayout) findViewById(R.id.root_lay);
        root_lay.setVisibility(View.GONE);
        root_lay.setLayoutAnimation(getAnimationController());
        tv_address = (TextView) findViewById(R.id.tv_address);
        tv_room_name = (TextView) findViewById(R.id.tv_room_name);
        tv_date = (TextView) findViewById(R.id.tv_date);
        tv_total_price = (TextView) findViewById(R.id.tv_total_price);
        tv_detail = (TextView) findViewById(R.id.tv_detail);
        btn_pay = (Button) findViewById(R.id.btn_pay);
        tv_comment = (TextView) findViewById(R.id.tv_comment);
        tv_room_count = (TextView) findViewById(R.id.tv_room_count);
        tv_during = (TextView) findViewById(R.id.tv_during);
        payChannelLay = (CommonPayChannelLayout) findViewById(R.id.payChannelLay);
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.getString("orderId") != null) {
                orderId = bundle.getString("orderId");
            }
            LogUtil.i(TAG, "orderId = " + orderId);
            if (bundle.getString("orderType") != null) {
                orderType = bundle.getString("orderType");
            }
            LogUtil.i(TAG, "orderType = " + orderType);
        }
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText("支付方式");

        unionpay = new UnionPayUtil(this);
        alipay = new AlipayUtil(this);
        wxpay = new WXPayUtils(this);
        qmfPayHelper = new QmfPayHelper();

        // 动态注册支付广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadCastConst.ACTION_PAY_STATUS);
        LocalBroadcastManager.getInstance(this).registerReceiver(mPayReceiver, intentFilter);
    }

    private void requestOrderPayInfo(String orderNo) {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("orderNo", orderNo);
        b.addBody("tradeType", "01"); // 01 酒店业务，02 机票业务
        b.addBody("payChannel", payChannelLay.getPayChannel());

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.PAY;
        d.flag = AppConst.PAY;

        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void requestOrderDetailInfo() {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("orderId", orderId);
        b.addBody("orderType", orderType); //1-酒店 2-机票

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.PAY_ORDER_DETAIL;
        d.flag = AppConst.PAY_ORDER_DETAIL;

        if (!isProgressShowing()) {
            showProgressDialog(this);
        }

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void refreshOrderDetailInfo() {
        if (null != orderPayDetailInfoBean) {
            tv_address.setText(orderPayDetailInfoBean.name);
            tv_room_name.setText(orderPayDetailInfoBean.roomName);
            tv_room_count.setText(String.format(getString(R.string.roomCountStr), orderPayDetailInfoBean.roomCnt));
            tv_date.setText(DateUtil.getDay("MM月dd日", orderPayDetailInfoBean.timeStart) + "-" + DateUtil.getDay("dd日", orderPayDetailInfoBean.timeEnd));
            during = DateUtil.getGapCount(new Date(orderPayDetailInfoBean.timeStart), new Date(orderPayDetailInfoBean.timeEnd));
            tv_during.setText(String.format(getString(R.string.duringNightStr), during));
            float totalPrice = 0;
            try {
                totalPrice = Float.parseFloat(orderPayDetailInfoBean.amount);
            } catch (Exception e) {
                e.printStackTrace();
            }
            tv_total_price.setText(String.valueOf((int) totalPrice));
            tv_comment.setText(orderPayDetailInfoBean.requirement);
            root_lay.setVisibility(View.VISIBLE);
        } else {
            root_lay.setVisibility(View.GONE);
        }
    }

    @Override
    public void initListeners() {
        super.initListeners();
        tv_detail.setOnClickListener(this);
        btn_pay.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
//        super.onClick(v);
        switch (v.getId()) {
            case R.id.tv_detail: {
                final CustomDialog dialog = new CustomDialog(this);
                View view = LayoutInflater.from(this).inflate(R.layout.dialog_order_detail_layout, null);
                LinearLayout service_lay = (LinearLayout) view.findViewById(R.id.service_lay);
                service_lay.removeAllViews();
                View commView = LayoutInflater.from(this).inflate(R.layout.dialog_order_detail_item, null);
                LinearLayout room_detail_layout = (LinearLayout) commView.findViewById(R.id.room_detail_layout);
                TextView tv_title_comm = (TextView) commView.findViewById(R.id.tv_title);
                TextView tv_price_comm = (TextView) commView.findViewById(R.id.tv_price);
                tv_price_comm.getPaint().setFakeBoldText(true);
                StringBuilder sb = new StringBuilder();
                sb.append(orderPayDetailInfoBean.roomName).append(" ")
                        .append(orderPayDetailInfoBean.roomCnt).append("间").append(" ")
                        .append(during).append("晚");
                tv_title_comm.setText(sb);
                tv_price_comm.setText(String.format(getString(R.string.rmbStr), String.valueOf(orderPayDetailInfoBean.roomPrice)));
                room_detail_layout.removeAllViews();
                for (int i = 0; i < orderPayDetailInfoBean.preTotalPriceList.size(); i++) {
                    View roomDetailItem = LayoutInflater.from(this).inflate(R.layout.dialog_order_detail_item, null);
                    TextView tv_room_title = (TextView) roomDetailItem.findViewById(R.id.tv_title);
                    TextView tv_room_price = (TextView) roomDetailItem.findViewById(R.id.tv_price);
                    if (!"2".equals(orderPayDetailInfoBean.preTotalPriceList.get(i).type)) {
                        String startDate = DateUtil.getDay("M月d号", orderPayDetailInfoBean.preTotalPriceList.get(i).activeTime);
                        String endDate = String.valueOf(Integer.parseInt(DateUtil.getDay("d", orderPayDetailInfoBean.preTotalPriceList.get(i).activeTime)) + 1) + "号";
                        tv_room_title.setText(startDate + "-" + endDate);
                        tv_room_title.setTextSize(10);
                        tv_room_title.setTextColor(getResources().getColor(R.color.titleSummaryColor));
                        tv_room_price.setText(String.format(getString(R.string.rmbStr), String.valueOf((int) Float.parseFloat(orderPayDetailInfoBean.preTotalPriceList.get(i).price))));
                        tv_room_price.setTextSize(10);
                        tv_room_title.setTextColor(getResources().getColor(R.color.titleSummaryColor));
                    } else {
                        tv_room_title.setText(orderPayDetailInfoBean.preTotalPriceList.get(i).name);
                        tv_room_title.setTextSize(10);
                        tv_room_title.setTextColor(getResources().getColor(R.color.titleSummaryColor));
                        tv_room_price.setText("-" + String.format(getString(R.string.rmbStr), String.valueOf((int) Float.parseFloat(orderPayDetailInfoBean.preTotalPriceList.get(i).price))));
                        tv_room_price.setTextSize(10);
                        tv_room_title.setTextColor(getResources().getColor(R.color.titleSummaryColor));
                    }
                    room_detail_layout.addView(roomDetailItem);
                }
                LinearLayout.LayoutParams roomDetailLlp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                roomDetailLlp.leftMargin = Utils.dp2px(15);
                room_detail_layout.setLayoutParams(roomDetailLlp);
                service_lay.addView(commView);

                for (int i = 0; i < orderPayDetailInfoBean.attachInfo.size(); i++) {
                    View item = LayoutInflater.from(this).inflate(R.layout.dialog_order_detail_item, null);
                    TextView tv_title = (TextView) item.findViewById(R.id.tv_title);
                    TextView tv_price = (TextView) item.findViewById(R.id.tv_price);
                    tv_price.getPaint().setFakeBoldText(true);
                    tv_title.setText(orderPayDetailInfoBean.attachInfo.get(i).serviceName + " " + getString(R.string.multipleSign) + " " + orderPayDetailInfoBean.attachInfo.get(i).serviceCnt);
                    tv_price.setText(String.format(getString(R.string.rmbStr), String.valueOf(orderPayDetailInfoBean.attachInfo.get(i).orderMoney)));
                    service_lay.addView(item);
                }

                dialog.addView(view);
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
                break;
            }
            case R.id.btn_pay: {
                CustomDialog dialog = new CustomDialog(this);
                dialog.setTitle("确认支付");
                dialog.setMessage("是否确认支付该订单？\n\n支付后，若酒店确认订单则无法取消\n");
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.setPositiveButton("去支付", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestOrderPayInfo(orderPayDetailInfoBean.orderNO);
                    }
                });
                dialog.show();
                break;
            }
            case R.id.iv_back: {
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
                        if (ActivityTack.getInstanse().isExitActivity(HotelListActivity.class)) {
                            startActivity(new Intent(HotelOrderPayActivity.this, HotelListActivity.class));
                        } else if (ActivityTack.getInstanse().isExitActivity(HotelRoomOrderActivity.class)) {
                            Intent intent = new Intent(HotelOrderPayActivity.this, HotelMainActivity.class);
                            intent.putExtra("isClosed", true);
                            intent.putExtra("index", 0);
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
            default:
                break;
        }
    }

    private void startPay(String str) {
        JSONObject mJson = JSON.parseObject(str);
//        String data;
        if (mJson.containsKey(HotelCommDef.ALIPAY)) {
            //支付宝第三方支付
            alipay.pay(mJson.getString(HotelCommDef.ALIPAY));
            //全民付
//            data = mJson.getString(HotelCommDef.ALIPAY);
//            qmfPayHelper.setPayStragety(new QmfAliPay());
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
            //全民付
//            data = mJson.getString(HotelCommDef.WXPAY);
//            qmfPayHelper.setPayStragety(new QmfWxPay());
        } else if (mJson.containsKey(HotelCommDef.UNIONPAY)) {
            //银联第三方支付
            JSONObject mmJson = mJson.getJSONObject(HotelCommDef.UNIONPAY);
            unionpay.setUnionPayServerMode(UnionPayUtil.RELEASE_MODE);
            unionpay.unionStartPay(mmJson.getString("tn"));
            //全民付
//            data = mJson.getString(HotelCommDef.UNIONPAY);
//            qmfPayHelper.setPayStragety(new QmfPosterPay(this));
        } else {
            CustomToast.show("支付失败", CustomToast.LENGTH_SHORT);
//            return;
        }
//        qmfPayHelper.startPay(data);
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
    protected void onDestroy() {
        super.onDestroy();
        wxpay = null;
        alipay = null;
        unionpay = null;
        qmfPayHelper = null;
        if (null != mPayReceiver) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mPayReceiver);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        if (data.hasExtra("pay_result")) {
            Intent intent = new Intent(BroadCastConst.ACTION_PAY_STATUS);
            intent.putExtra("info", data.getExtras().getString("pay_result"));
            LogUtil.i(TAG, "pay_result = " + data.getExtras().getString("pay_result"));
            intent.putExtra("type", "unionPay");
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.PAY_ORDER_DETAIL) {
                removeProgressDialog();
                LogUtil.i(TAG, "json = " + response.body.toString());
                orderPayDetailInfoBean = JSON.parseObject(response.body.toString(), OrderPayDetailInfoBean.class);
                HotelOrderManager.getInstance().setOrderPayDetailInfoBean(orderPayDetailInfoBean);
                refreshOrderDetailInfo();
            } else if (request.flag == AppConst.PAY) {
                LogUtil.i(TAG, "json = " + response.body.toString());
                removeProgressDialog();
                startPay(response.body.toString());
            }
        }
    }
}
