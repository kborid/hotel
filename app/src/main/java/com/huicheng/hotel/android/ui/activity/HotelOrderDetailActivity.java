package com.huicheng.hotel.android.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.net.bean.OrderPayDetailInfoBean;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.widget.CustomToast;

import java.util.Date;

/**
 * @author kborid
 * @date 2016/12/8 0008
 */
public class HotelOrderDetailActivity extends BaseActivity implements DataCallback {

    private String orderId, orderType;
    private OrderPayDetailInfoBean orderPayDetailInfoBean = null;

    private LinearLayout root_lay;
    private TextView tv_order_num, tv_order_date;
    private TextView tv_hotel_city, tv_hotel_name;
    private TextView tv_in_date, tv_price;
    private LinearLayout service_lay;
    private TextView tv_total_price;
    private EditText et_name, et_phone, et_require;
    private LinearLayout et_name_line, et_phone_line;
    private TextView tv_invoice_info;

    private Button btn_hhy, btn_pay, btn_cancel, btn_modify, btn_booking_again, btn_assess, btn_confirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_hotel_orderdetail_layout);
        initViews();
        initParams();
        initListeners();
        requestOrderDetailInfo();
    }

    @Override
    public void initViews() {
        super.initViews();
        root_lay = (LinearLayout) findViewById(R.id.root_lay);
        tv_order_num = (TextView) findViewById(R.id.tv_order_num);
        tv_order_date = (TextView) findViewById(R.id.tv_order_date);

        tv_hotel_city = (TextView) findViewById(R.id.tv_hotel_city);
        tv_hotel_name = (TextView) findViewById(R.id.tv_hotel_name);
        tv_in_date = (TextView) findViewById(R.id.tv_in_date);
        tv_price = (TextView) findViewById(R.id.tv_price);
        service_lay = (LinearLayout) findViewById(R.id.service_lay);
        tv_total_price = (TextView) findViewById(R.id.tv_total_price);
        et_name = (EditText) findViewById(R.id.et_name);
        et_name_line = (LinearLayout) findViewById(R.id.et_name_line);
        et_phone = (EditText) findViewById(R.id.et_phone);
        et_phone_line = (LinearLayout) findViewById(R.id.et_phone_line);
        et_require = (EditText) findViewById(R.id.et_require);
        tv_invoice_info = (TextView) findViewById(R.id.tv_invoice_info);

        btn_hhy = (Button) findViewById(R.id.btn_hhy);
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
        btn_back.setImageResource(R.drawable.iv_back_white);
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
            tv_order_num.setText(orderPayDetailInfoBean.orderNO);
            tv_order_date.setText(DateUtil.getDay("yyyy年MM月dd日", orderPayDetailInfoBean.timeStart) + "-" + DateUtil.getDay("dd日", orderPayDetailInfoBean.timeEnd));
            tv_hotel_city.setText(orderPayDetailInfoBean.location);
            tv_hotel_name.setText(orderPayDetailInfoBean.name);

            String date = DateUtil.getDay("MM月dd日", orderPayDetailInfoBean.timeStart) + "-" + DateUtil.getDay("dd日", orderPayDetailInfoBean.timeEnd);
            String during = DateUtil.getGapCount(new Date(orderPayDetailInfoBean.timeStart), new Date(orderPayDetailInfoBean.timeEnd)) + "晚";
            tv_in_date.setText(date + "  " + during);
            tv_price.setText(orderPayDetailInfoBean.roomPrice + " 元");

            service_lay.removeAllViews();
            for (int i = 0; i < orderPayDetailInfoBean.attachInfo.size(); i++) {
                View view = LayoutInflater.from(this).inflate(R.layout.order_service_item, null);
                TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
                TextView tv_price = (TextView) view.findViewById(R.id.tv_price);
                tv_title.setText(orderPayDetailInfoBean.attachInfo.get(i).serviceName + " * " + orderPayDetailInfoBean.attachInfo.get(i).custCount);
                tv_price.setText(orderPayDetailInfoBean.attachInfo.get(i).orderMoney + " 元");
                service_lay.addView(view);
            }
            float price = 0;
            try {
                price = Float.parseFloat(orderPayDetailInfoBean.amount);
            } catch (Exception e) {
                e.printStackTrace();
            }
            tv_total_price.setText((int) price + "");

            String name = orderPayDetailInfoBean.checkInName;
            name = name.replace("|", "，");
            et_name.setText(name);
            String phone = orderPayDetailInfoBean.checkInTel;
            phone = phone.replace("|", "，");
            et_phone.setText(phone);
            et_require.setText(orderPayDetailInfoBean.specialComment);
            tv_invoice_info.setText(orderPayDetailInfoBean.invoice);

            int status = Integer.parseInt(orderPayDetailInfoBean.status);
            modifyEnableOrderInfo(false);
            updateButtonStatus(status);
        } else {
            root_lay.setVisibility(View.GONE);
        }
    }

    private void updateButtonStatus(int status) {
        switch (status) {
            case HotelCommDef.WaitPay:
                btn_pay.setVisibility(View.VISIBLE);
                btn_hhy.setVisibility(View.GONE);
                btn_cancel.setEnabled(true);
                btn_modify.setEnabled(true);
                btn_assess.setEnabled(false);
                break;
            case HotelCommDef.WaitConfirm:
                btn_pay.setVisibility(View.GONE);
                btn_hhy.setVisibility(View.GONE);
                btn_cancel.setEnabled(true);
                btn_modify.setEnabled(true);
                btn_assess.setEnabled(false);
                break;
            case HotelCommDef.Confirmed:
                btn_pay.setVisibility(View.GONE);
                if (orderPayDetailInfoBean.payWay == 1) {
                    btn_hhy.setVisibility(View.GONE);
                } else {
                    btn_hhy.setVisibility(View.VISIBLE);
                }
                btn_cancel.setEnabled(false);
                btn_modify.setEnabled(false);
                btn_assess.setEnabled(false);
                break;
            case HotelCommDef.Finished:
                btn_pay.setVisibility(View.GONE);
                btn_hhy.setVisibility(View.GONE);
                btn_cancel.setEnabled(false);
                btn_modify.setEnabled(false);
                btn_assess.setEnabled(true);
                break;
            default:
                btn_pay.setVisibility(View.GONE);
                btn_hhy.setVisibility(View.GONE);
                btn_cancel.setEnabled(false);
                btn_modify.setEnabled(false);
                btn_assess.setEnabled(false);
                break;
        }
    }

    @Override
    public void initListeners() {
        super.initListeners();
        tv_hotel_name.setOnClickListener(this);
        btn_pay.setOnClickListener(this);
        btn_hhy.setOnClickListener(this);
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
                intent = new Intent(this, OrderPayActivity.class);
                intent.putExtra("orderPayDetailInfoBean", orderPayDetailInfoBean);
                break;
            case R.id.btn_hhy:
                intent = new Intent(this, HouHuiYaoOrderDetailActivity.class);
                intent.putExtra("regretOrderid", orderPayDetailInfoBean.orderID);
                String dateStr = DateUtil.getDay("M.dd", orderPayDetailInfoBean.timeStart) + DateUtil.dateToWeek2(new Date(orderPayDetailInfoBean.timeStart)) + " - " + DateUtil.getDay("M.dd", orderPayDetailInfoBean.timeEnd) + DateUtil.dateToWeek2(new Date(orderPayDetailInfoBean.timeEnd));
                intent.putExtra("date", dateStr);
                break;
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
                intent = new Intent(this, AssessOrdersActivity.class);
                break;
            case R.id.tv_hotel_name:
            case R.id.btn_booking_again:
                intent = new Intent(this, HotelCalendarChooseActivity.class);
                HotelOrderManager.getInstance().reset();
                intent.putExtra("rebooking", true);
                intent.putExtra("hotelId", orderPayDetailInfoBean.hotelID);
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

    private void modifyEnableOrderInfo(boolean flag) {
        if (flag) {
            et_name.setEnabled(true);
            et_name_line.setVisibility(View.VISIBLE);
            et_phone.setEnabled(true);
            et_phone_line.setVisibility(View.VISIBLE);
            et_require.setEnabled(true);
            et_require.setBackground(getResources().getDrawable(R.drawable.comm_rectangle_transparent_withbound_white));

            tv_order_date.setTextColor(getResources().getColor(R.color.transparent40_));
            tv_invoice_info.setTextColor(getResources().getColor(R.color.transparent40_));
            tv_hotel_name.setTextColor(getResources().getColor(R.color.transparent40_));

            btn_confirm.setVisibility(View.VISIBLE);
            btn_confirm.setEnabled(true);
            btn_modify.setVisibility(View.GONE);
            btn_cancel.setEnabled(false);
            btn_booking_again.setEnabled(false);
        } else {
            et_name.setEnabled(false);
            et_name_line.setVisibility(View.INVISIBLE);
            et_phone.setEnabled(false);
            et_phone_line.setVisibility(View.INVISIBLE);
            et_require.setEnabled(false);
            et_require.setBackground(null);

            tv_order_date.setTextColor(getResources().getColor(R.color.white));
            tv_invoice_info.setTextColor(getResources().getColor(R.color.white));
            tv_hotel_name.setTextColor(getResources().getColor(R.color.white));

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
    public void preExecute(ResponseData request) {

    }

    @Override
    public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.ORDER_DETAIL) {
                removeProgressDialog();
                System.out.println("json = " + response.body.toString());
                orderPayDetailInfoBean = JSON.parseObject(response.body.toString(), OrderPayDetailInfoBean.class);
                refreshOrderDetailInfo();
            } else if (request.flag == AppConst.ORDER_CANCEL) {
//                requestOrderDetailInfo();
                CustomToast.show("订单取消成功", CustomToast.LENGTH_SHORT);
                setResult(RESULT_OK);
                finish();
            } else if (request.flag == AppConst.ORDER_MODIFY) {
                removeProgressDialog();
                CustomToast.show("订单修改成功", CustomToast.LENGTH_SHORT);
                modifyEnableOrderInfo(false);
            }
        }
    }
}
