package com.huicheng.hotel.android.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.common.pay.alipay.AlipayUtil;
import com.huicheng.hotel.android.common.pay.wxpay.WXPayUtils;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.net.bean.OrderPayDetailInfoBean;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.ActivityTack;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.widget.CustomToast;

import java.util.Date;

/**
 * @author kborid
 * @date 2017/3/7 0007
 */
public class OrderPayActivity extends BaseActivity implements DataCallback {

    private static final String TAG = "OrderPayActivity";
    private OrderPayDetailInfoBean orderPayDetailInfoBean = null;
    private String during = null;
    private String checkRoomDate = null;
    private LinearLayout root_lay;
    private TextView tv_address, tv_date, tv_room_name, tv_total_price, tv_detail, tv_comment;
    private Button btn_pay;
    private TextView tv_pay_title;
    private ImageView iv_pay_icon, iv_pay_change;
    private String orderId = null;
    private String hotelName = null, roomName = null;

    private AlipayUtil alipay = null;
    private WXPayUtils wxpay = null;
    private int payIndex = 0;
    private int[] payIcon = new int[]{R.drawable.iv_pay_zhifubao, R.drawable.iv_pay_weixin};
    private String[] payChannel = new String[]{"支付宝", "微信"};
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.i(TAG, "OrderPayActivity onReceive ACTION_PAY_STATUS callback");
            String action = intent.getAction();
            if (BroadCastConst.ACTION_PAY_STATUS.equals(action)) {
                String info = intent.getExtras().getString("info");
                String type = intent.getExtras().getString("type");
                LogUtil.i(TAG, info);
                LogUtil.i(TAG, type);
                if (isProgressShowing()) {
                    removeProgressDialog();
                }

                String code = "", msg = "";
                if ("aliPay".equals(type)) {
                    JSONObject mJson = JSON.parseObject(info);
                    code = mJson.getString("resultStatus");
                    msg = mJson.getString("memo");
                } else {
                    code = info;
                    if ("-1".equals(code)) {
                        msg = "支付失败";
                    } else if ("-2".equals(code)) {
                        msg = "取消支付";
                    }
                }

                if ("9000".equals(code) || "0".equals(code)) {
                    Intent intent1 = new Intent(OrderPayActivity.this, OrderPaySuccessActivity.class);
                    intent1.putExtra("hotelName", hotelName);
                    intent1.putExtra("roomName", roomName);
                    intent1.putExtra("checkRoomDate", checkRoomDate);
                    startActivity(intent1);
                } else {
                    if (StringUtil.isEmpty(msg)) {
                        msg = "支付失败";
                    }
                    CustomToast.show(msg, CustomToast.LENGTH_SHORT);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_pay_layout);
        initViews();
        initParams();
        initListeners();
        if (StringUtil.notEmpty(orderPayDetailInfoBean)) {
            refreshOrderDetailInfo();
        } else if (StringUtil.notEmpty(orderId)) {
            requestOrderDetailInfo();
        }
    }

    @Override
    public void initViews() {
        super.initViews();
        root_lay = (LinearLayout) findViewById(R.id.root_lay);
        tv_address = (TextView) findViewById(R.id.tv_address);
        tv_room_name = (TextView) findViewById(R.id.tv_room_name);
        tv_date = (TextView) findViewById(R.id.tv_date);
        tv_total_price = (TextView) findViewById(R.id.tv_total_price);
        tv_detail = (TextView) findViewById(R.id.tv_detail);
        btn_pay = (Button) findViewById(R.id.btn_pay);
        tv_comment = (TextView) findViewById(R.id.tv_comment);

        iv_pay_icon = (ImageView) findViewById(R.id.iv_pay_icon);
        tv_pay_title = (TextView) findViewById(R.id.tv_pay_title);
        iv_pay_change = (ImageView) findViewById(R.id.iv_pay_change);
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.getString("orderId") != null) {
                orderId = bundle.getString("orderId");
            }
            if (bundle.getSerializable("orderPayDetailInfoBean") != null) {
                orderPayDetailInfoBean = (OrderPayDetailInfoBean) bundle.getSerializable("orderPayDetailInfoBean");
                hotelName = orderPayDetailInfoBean.name;
                roomName = orderPayDetailInfoBean.roomName;
                checkRoomDate = orderPayDetailInfoBean.checkRoomDate;
            } else {
                if (bundle.getString("hotelName") != null && bundle.getString("roomName") != null) {
                    hotelName = bundle.getString("hotelName");
                    roomName = bundle.getString("roomName");
                }
                if (bundle.getString("checkRoomDate") != null) {
                    checkRoomDate = bundle.getString("checkRoomDate");
                }
            }
        }
        LogUtil.i(TAG, "checkRoomDate = " + checkRoomDate);
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText("支付方式");
        iv_pay_icon.setImageResource(payIcon[payIndex]);
        tv_pay_title.setText(payChannel[payIndex]);
        alipay = new AlipayUtil(this);
        wxpay = new WXPayUtils(this);
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(BroadCastConst.ACTION_PAY_STATUS);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, mIntentFilter);
    }

    private void requestOrderPayInfo(String orderNo) {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("orderNo", orderNo);
        b.addBody("tradeType", "01"); // 01 酒店业务，02 机票业务
        b.addBody("payChannel", HotelCommDef.getPayChannel(payIndex));

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
        b.addBody("orderType", "1"); //1-酒店 2-机票

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
            during = DateUtil.getGapCount(new Date(orderPayDetailInfoBean.timeStart), new Date(orderPayDetailInfoBean.timeEnd)) + "晚";
            if (StringUtil.notEmpty(checkRoomDate)) {
                tv_date.setVisibility(View.VISIBLE);
                tv_date.setText(checkRoomDate);
            } else {
                tv_date.setVisibility(View.GONE);
            }
            float totalPrice = 0;
            try {
                totalPrice = Float.parseFloat(orderPayDetailInfoBean.amount);
            } catch (Exception e) {
                e.printStackTrace();
            }
            tv_total_price.setText((int) totalPrice + "");
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
        iv_pay_change.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
//        super.onClick(v);
        switch (v.getId()) {
            case R.id.tv_detail: {
                final CustomDialog dialog = new CustomDialog(this);
                View view = LayoutInflater.from(this).inflate(R.layout.order_detail_item, null);
                ((TextView) view.findViewById(R.id.tv_title)).getPaint().setFakeBoldText(true);
                LinearLayout service_lay = (LinearLayout) view.findViewById(R.id.service_lay);
                service_lay.removeAllViews();
                View commView = LayoutInflater.from(this).inflate(R.layout.order_detail_service_item, null);
                TextView tv_title_comm = (TextView) commView.findViewById(R.id.tv_title);
                TextView tv_price_comm = (TextView) commView.findViewById(R.id.tv_price);
                ((TextView) commView.findViewById(R.id.tv_price_unit)).getPaint().setFakeBoldText(true);
                tv_price_comm.getPaint().setFakeBoldText(true);
                tv_title_comm.setText(orderPayDetailInfoBean.roomName + " " + during);
                tv_price_comm.setText(orderPayDetailInfoBean.roomPrice + "");
                service_lay.addView(commView);

                for (int i = 0; i < orderPayDetailInfoBean.attachInfo.size(); i++) {
                    View item = LayoutInflater.from(this).inflate(R.layout.order_detail_service_item, null);
                    TextView tv_title = (TextView) item.findViewById(R.id.tv_title);
                    TextView tv_price = (TextView) item.findViewById(R.id.tv_price);
                    ((TextView) item.findViewById(R.id.tv_price_unit)).getPaint().setFakeBoldText(true);
                    tv_price.getPaint().setFakeBoldText(true);
                    tv_title.setText(orderPayDetailInfoBean.attachInfo.get(i).serviceName + " *" + orderPayDetailInfoBean.attachInfo.get(i).custCount);
                    tv_price.setText(orderPayDetailInfoBean.attachInfo.get(i).servicePrice + "");
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
            case R.id.iv_pay_change: {
                CustomDialog dialog = new CustomDialog(this);
                dialog.setTitle("选择支付方式");
                dialog.setSingleChoiceItems(payChannel, payIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        payIndex = which;
                        iv_pay_icon.setImageResource(payIcon[payIndex]);
                        tv_pay_title.setText(payChannel[payIndex]);
                        dialog.dismiss();
                    }
                });
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
                break;
            }
            case R.id.btn_back: {
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
                        if (ActivityTack.getInstanse().isExitActivity(RoomOrderConfirmActivity.class)) {
                            startActivity(new Intent(OrderPayActivity.this, MainFragmentActivity.class));
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            btn_back.performClick();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void preExecute(ResponseData request) {

    }

    @Override
    public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.PAY_ORDER_DETAIL) {
                removeProgressDialog();
                LogUtil.i(TAG, "json = " + response.body.toString());
                orderPayDetailInfoBean = JSON.parseObject(response.body.toString(), OrderPayDetailInfoBean.class);
                refreshOrderDetailInfo();
            } else if (AppConst.PAY == request.flag) {
                LogUtil.i(TAG, "json = " + response.body.toString());
                JSONObject mJson = JSON.parseObject(response.body.toString());
                if (mJson.containsKey(HotelCommDef.ALIPAY)) {
                    alipay.pay(mJson.getString(HotelCommDef.ALIPAY));
                } else if (mJson.containsKey(HotelCommDef.WXPAY)) {
                    JSONObject mmJson = mJson.getJSONObject(HotelCommDef.WXPAY);
                    wxpay.sendPayReq(
                            mmJson.getString("package"),
                            mmJson.getString("appid"),
                            mmJson.getString("sign"),
                            mmJson.getString("partnerid"),
                            mmJson.getString("prepayid"),
                            mmJson.getString("noncestr"),
                            mmJson.getString("timestamp"));
                } else {
                    removeProgressDialog();
                    CustomToast.show("支付失败", CustomToast.LENGTH_SHORT);
                }
            }
        }
    }
}
