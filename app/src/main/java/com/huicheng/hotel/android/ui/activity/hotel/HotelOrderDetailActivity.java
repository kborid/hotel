package com.huicheng.hotel.android.ui.activity.hotel;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.OrderPayDetailInfoBean;
import com.huicheng.hotel.android.tools.CityParseUtils;
import com.huicheng.hotel.android.ui.activity.CalendarChooseActivity;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.prj.sdk.app.AppConst;
import com.prj.sdk.app.NetURL;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;

import java.util.Date;

/**
 * @author kborid
 * @date 2016/12/8 0008
 */
public class HotelOrderDetailActivity extends BaseActivity {

    private String orderId, orderType;
    private OrderPayDetailInfoBean orderPayDetailInfoBean = null;

    private LinearLayout root_lay;
    private TextView tv_in_date, tv_during_days, tv_out_date;
    private TextView tv_hotel_position, tv_hotel_phone;

    private TextView tv_room_name;
    private TextView tv_price;
    private LinearLayout service_lay;
    private TextView tv_total_price;

    private EditText et_name, et_phone, et_require;
    private TextView tv_invoice_info;
    private TextView tv_order_num;
    private View name_line, phone_line;

    private Button /*btn_hhy, */btn_pay, btn_cancel, btn_modify, btn_booking_again, btn_assess, btn_confirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_hotel_orderdetail_layout);
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
        tv_in_date = (TextView) findViewById(R.id.tv_in_date);
        tv_during_days = (TextView) findViewById(R.id.tv_during_days);
        tv_out_date = (TextView) findViewById(R.id.tv_out_date);

        tv_hotel_position = (TextView) findViewById(R.id.tv_hotel_position);
        tv_hotel_phone = (TextView) findViewById(R.id.tv_hotel_phone);

        tv_order_num = (TextView) findViewById(R.id.tv_order_num);
        tv_room_name = (TextView) findViewById(R.id.tv_room_name);
        tv_price = (TextView) findViewById(R.id.tv_price);
        service_lay = (LinearLayout) findViewById(R.id.service_lay);
        tv_total_price = (TextView) findViewById(R.id.tv_total_price);
        et_name = (EditText) findViewById(R.id.et_name);
        name_line = findViewById(R.id.name_line);
        et_phone = (EditText) findViewById(R.id.et_phone);
        phone_line = findViewById(R.id.phone_line);
        et_require = (EditText) findViewById(R.id.et_require);
        tv_invoice_info = (TextView) findViewById(R.id.tv_invoice_info);

//        btn_hhy = (Button) findViewById(R.id.btn_hhy);
        btn_pay = (Button) findViewById(R.id.btn_pay);
        btn_assess = (Button) findViewById(R.id.btn_assess);
        btn_booking_again = (Button) findViewById(R.id.btn_booking_again);
        btn_cancel = (Button) findViewById(R.id.btn_cancel);
        btn_modify = (Button) findViewById(R.id.btn_modify);
        btn_confirm = (Button) findViewById(R.id.btn_confirm);
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            if (bundle.getString("orderId") != null && bundle.getString("orderType") != null) {
                orderId = bundle.getString("orderId");
                orderType = bundle.getString("orderType");
            }
        }
    }

    @Override
    public void initParams() {
        super.initParams();
    }

    private void requestOrderDetailInfo() {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("orderId", orderId);
        b.addBody("orderType", orderType);

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.ORDER_DETAIL;
        d.flag = AppConst.ORDER_DETAIL;
        if (!isProgressShowing()) {
            showProgressDialog(this);
        }

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void requestCancelOrder() {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("orderId", orderId);

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.ORDER_CANCEL;
        d.flag = AppConst.ORDER_CANCEL;

        if (!isProgressShowing()) {
            showProgressDialog(this);
        }

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void refreshOrderDetailInfo() {
        if (null != orderPayDetailInfoBean) {
            root_lay.setVisibility(View.VISIBLE);
            tv_center_title.setText(orderPayDetailInfoBean.name);
            int days = DateUtil.getGapCount(new Date(orderPayDetailInfoBean.timeStart), new Date(orderPayDetailInfoBean.timeEnd));
            tv_during_days.setText(String.format(getString(R.string.duringDayStr), days));
            tv_in_date.setText(DateUtil.getDay("MM / dd", orderPayDetailInfoBean.timeStart));
            tv_out_date.setText(DateUtil.getDay("MM / dd", orderPayDetailInfoBean.timeEnd));
            StringBuilder sb = new StringBuilder();
            sb.append(orderPayDetailInfoBean.roomName).append(" ").append(getString(R.string.multipleSign)).append(" ")
                    .append(String.format(getString(R.string.duringNightStr), days));
            if (orderPayDetailInfoBean.roomCnt > 0) {
                sb.append(" ").append(getString(R.string.multipleSign)).append(" ").append(orderPayDetailInfoBean.roomCnt).append("间");
            }
            tv_room_name.setText(sb);
            tv_price.setText(orderPayDetailInfoBean.roomPrice + " 元");

            service_lay.removeAllViews();
            for (int i = 0; i < orderPayDetailInfoBean.attachInfo.size(); i++) {
                View view = LayoutInflater.from(this).inflate(R.layout.order_service_item, null);
                TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
                TextView tv_price = (TextView) view.findViewById(R.id.tv_price);
                tv_title.setText(orderPayDetailInfoBean.attachInfo.get(i).serviceName);
                if (orderPayDetailInfoBean.attachInfo.get(i).serviceCnt > 0) {
                    tv_title.append(" " + getString(R.string.multipleSign) + " " + orderPayDetailInfoBean.attachInfo.get(i).serviceCnt);
                }
                if ("".equals(orderPayDetailInfoBean.attachInfo.get(i).type)){
                    (view.findViewById(R.id.tv_gong)).setVisibility(View.VISIBLE);
                }
                tv_price.setText(orderPayDetailInfoBean.attachInfo.get(i).orderMoney + " 元");
                service_lay.addView(view);
            }

            float price = 0;
            try {
                price = Float.parseFloat(orderPayDetailInfoBean.amount);
            } catch (Exception e) {
                e.printStackTrace();
            }
            tv_total_price.setText(String.valueOf((int) price));

            tv_hotel_position.setText(orderPayDetailInfoBean.hotelAddress);
            tv_hotel_phone.setText(orderPayDetailInfoBean.hotelPhone);


            String name = orderPayDetailInfoBean.checkInName;
            name = name.replace("|", "，");
            et_name.setText(name);
            String phone = orderPayDetailInfoBean.checkInTel;
            phone = phone.replace("|", "，");
            et_phone.setText(phone);
            et_require.setText(orderPayDetailInfoBean.specialComment);
            tv_invoice_info.setText(orderPayDetailInfoBean.invoice);
            tv_order_num.setText(orderPayDetailInfoBean.orderNO);
            int status = Integer.parseInt(orderPayDetailInfoBean.status);
            modifyEnableOrderInfo(false);
            updateButtonStatus(status);
        }
    }

    private void updateButtonStatus(int status) {
        switch (status) {
            case HotelCommDef.WaitPay:
                btn_pay.setVisibility(View.VISIBLE);
//                btn_hhy.setVisibility(View.GONE);
                btn_cancel.setEnabled(true);
                btn_modify.setEnabled(true);
                btn_assess.setEnabled(false);
                break;
            case HotelCommDef.WaitConfirm:
                btn_pay.setVisibility(View.GONE);
//                btn_hhy.setVisibility(View.GONE);
                btn_cancel.setEnabled(true);
                btn_modify.setEnabled(true);
                btn_assess.setEnabled(false);
                break;
            case HotelCommDef.Confirmed:
                btn_pay.setVisibility(View.GONE);
//                if (orderPayDetailInfoBean.payWay == 1) {
//                    btn_hhy.setVisibility(View.GONE);
//                } else {
//                    btn_hhy.setVisibility(View.VISIBLE);
//                }
                btn_cancel.setEnabled(false);
                btn_modify.setEnabled(false);
                btn_assess.setEnabled(false);
                break;
            case HotelCommDef.Finished:
                btn_pay.setVisibility(View.GONE);
//                btn_hhy.setVisibility(View.GONE);
                btn_cancel.setEnabled(false);
                btn_modify.setEnabled(false);
                btn_assess.setEnabled(true);
                break;
            default:
                btn_pay.setVisibility(View.GONE);
//                btn_hhy.setVisibility(View.GONE);
                btn_cancel.setEnabled(false);
                btn_modify.setEnabled(false);
                btn_assess.setEnabled(false);
                break;
        }
    }

    @Override
    public void initListeners() {
        super.initListeners();
        btn_pay.setOnClickListener(this);
//        btn_hhy.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
        btn_modify.setOnClickListener(this);
        btn_assess.setOnClickListener(this);
        btn_booking_again.setOnClickListener(this);
        btn_confirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        Intent intent = null;
        switch (v.getId()) {
            case R.id.btn_pay:
                intent = new Intent(this, HotelOrderPayActivity.class);
                intent.putExtra("orderId", orderId);
                intent.putExtra("orderType", orderType);
                break;
//            case R.id.btn_hhy:
//                intent = new Intent(this, HotelHhyOrderDetailActivity.class);
//                intent.putExtra("regretOrderid", orderPayDetailInfoBean.orderID);
//                String dateStr = DateUtil.getDay("M.dd", orderPayDetailInfoBean.timeStart) + DateUtil.dateToWeek2(new Date(orderPayDetailInfoBean.timeStart)) + " - " + DateUtil.getDay("M.dd", orderPayDetailInfoBean.timeEnd) + DateUtil.dateToWeek2(new Date(orderPayDetailInfoBean.timeEnd));
//                intent.putExtra("date", dateStr);
//                break;
            case R.id.btn_cancel:
                requestCancelOrder();
                break;
            case R.id.btn_modify:
                modifyEnableOrderInfo(true);
                break;
            case R.id.btn_confirm:
                requestModifyOrderInfo();
                break;
            case R.id.btn_assess:
                intent = new Intent(this, HotelOrdersAssessActivity.class);
                break;
            case R.id.tv_hotel_name:
            case R.id.btn_booking_again:
                HotelOrderManager.getInstance().reset();
                HotelOrderManager.getInstance().setOrderPayDetailInfoBean(orderPayDetailInfoBean);
                HotelOrderManager.getInstance().setCityStr(CityParseUtils.getProvinceCityString(orderPayDetailInfoBean.province, orderPayDetailInfoBean.location, "-"));
                intent = new Intent(this, CalendarChooseActivity.class);
                intent.putExtra("isReBooking", true);
                break;
            default:
                break;
        }

        if (null != intent) {
            startActivity(intent);
        }
    }

    private void requestModifyOrderInfo() {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("guest", et_name.getText().toString());
        b.addBody("guestMobile", et_phone.getText().toString());
        b.addBody("orderId", orderId);
        b.addBody("specialComment", et_require.getText().toString());

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.ORDER_MODIFY;
        d.flag = AppConst.ORDER_MODIFY;

        if (!isProgressShowing()) {
            showProgressDialog(this);
        }

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void modifyEnableOrderInfo(boolean flag) {
        if (flag) {
            et_name.setEnabled(true);
            name_line.setVisibility(View.VISIBLE);
            et_phone.setEnabled(true);
            phone_line.setVisibility(View.VISIBLE);
            et_require.setEnabled(true);
            et_require.setBackground(getResources().getDrawable(R.drawable.orderdetail_edit_require_bg));

            btn_confirm.setVisibility(View.VISIBLE);
            btn_confirm.setEnabled(true);
            btn_modify.setVisibility(View.GONE);
            btn_cancel.setEnabled(false);
            btn_booking_again.setEnabled(false);
        } else {
            et_name.setEnabled(false);
            name_line.setVisibility(View.INVISIBLE);
            et_phone.setEnabled(false);
            phone_line.setVisibility(View.INVISIBLE);
            et_require.setEnabled(false);
            et_require.setBackground(null);

            btn_modify.setVisibility(View.VISIBLE);
            btn_confirm.setVisibility(View.GONE);
            btn_confirm.setEnabled(false);
            btn_cancel.setEnabled(true);
            btn_booking_again.setEnabled(true);
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
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.ORDER_DETAIL) {
                removeProgressDialog();
                LogUtil.i(TAG, "json = " + response.body.toString());
                orderPayDetailInfoBean = JSON.parseObject(response.body.toString(), OrderPayDetailInfoBean.class);
                HotelOrderManager.getInstance().setOrderPayDetailInfoBean(orderPayDetailInfoBean);
                refreshOrderDetailInfo();
            } else if (request.flag == AppConst.ORDER_CANCEL) {
                LogUtil.i(TAG, "json = " + response.body.toString());
                JSONObject mJson = JSON.parseObject(response.body.toString());
                if (mJson.containsKey("success")) {
                    String ret = mJson.getString("success");
                    if ("1".equals(ret)) {
                        String msg = getString(R.string.order_cancel_success);
                        if (mJson.containsKey("tips")) {
                            msg = mJson.getString("tips");
                        }
                        CustomToast.show(msg, CustomToast.LENGTH_SHORT);
                    } else {
                        CustomToast.show(getString(R.string.order_cancel_fail), CustomToast.LENGTH_SHORT);
                    }
                }
                setResult(RESULT_OK);
                finish();
            } else if (request.flag == AppConst.ORDER_MODIFY) {
                removeProgressDialog();
                CustomToast.show(getString(R.string.order_modify_success), CustomToast.LENGTH_SHORT);
                modifyEnableOrderInfo(false);
            }
        }
    }
}
