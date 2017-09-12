package com.huicheng.hotel.android.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.net.bean.HotelDetailInfoBean;
import com.huicheng.hotel.android.net.bean.HouHuiYaoDetailInfoBean;
import com.huicheng.hotel.android.net.bean.RoomConfirmInfoBean;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.SharedPreferenceUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author kborid
 * @date 2017/1/11 0011
 */
public class HouHuiYaoOrderDetailActivity extends BaseActivity {

    private LinearLayout root_lay;
    private TextView tv_hotel_name, tv_hotel_address, tv_hotel_phone, tv_hotel_detail;
    private ImageView iv_hotel_location;
    private TextView tv_room_name, tv_date, tv_room_detail;
    private LinearLayout choose_service_lay;
    private TextView tv_buy_price, tv_sale_price;
    private Button btn_confirm;
    private String orderId, dateStr;
    private HouHuiYaoDetailInfoBean houHuiYaoDetailInfoBean = null;
    private HotelDetailInfoBean hotelDetailInfoBean = null;
    private Map<String, RoomConfirmInfoBean> chooseServiceInfoMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_hhyorderdetail_layout);
        initViews();
        initParams();
        initListeners();
        requestHouHuiYaoDetail();
    }

    @Override
    public void initViews() {
        super.initViews();
        root_lay = (LinearLayout) findViewById(R.id.root_lay);
        root_lay.setLayoutAnimation(getAnimationController());
        tv_hotel_name = (TextView) findViewById(R.id.tv_hotel_name);
        tv_hotel_address = (TextView) findViewById(R.id.tv_hotel_address);
        tv_hotel_phone = (TextView) findViewById(R.id.tv_hotel_phone);
        tv_hotel_detail = (TextView) findViewById(R.id.tv_hotel_detail);
        iv_hotel_location = (ImageView) findViewById(R.id.iv_hotel_location);
        tv_room_name = (TextView) findViewById(R.id.tv_room_name);
        tv_room_detail = (TextView) findViewById(R.id.tv_room_detail);
        tv_date = (TextView) findViewById(R.id.tv_date);
        choose_service_lay = (LinearLayout) findViewById(R.id.choose_service_lay);
        tv_buy_price = (TextView) findViewById(R.id.tv_buy_price);
        tv_sale_price = (TextView) findViewById(R.id.tv_sale_price);
        btn_confirm = (Button) findViewById(R.id.btn_confirm);
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.getString("regretOrderid") != null && bundle.getString("date") != null) {
            orderId = bundle.getString("regretOrderid");
            dateStr = bundle.getString("date");
        }
    }

    @Override
    public void initParams() {
        tv_center_title.setText(SharedPreferenceUtil.getInstance().getString(AppConst.CITY, "", false) + "·" + SharedPreferenceUtil.getInstance().getString(AppConst.PROVINCE, "", false));
        tv_center_summary.setText(dateStr);
        super.initParams();
    }

    @Override
    public void initListeners() {
        super.initListeners();
        tv_hotel_detail.setOnClickListener(this);
        iv_hotel_location.setOnClickListener(this);
        tv_room_detail.setOnClickListener(this);
        btn_confirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        Intent intent = null;
        switch (v.getId()) {
            case R.id.tv_hotel_detail:
                if (houHuiYaoDetailInfoBean != null) {
                    intent = new Intent(this, RoomListActivity.class);
                    intent.putExtra("date", dateStr);
                    intent.putExtra("hotelId", houHuiYaoDetailInfoBean.hotelId);
                }
                break;
            case R.id.iv_hotel_location:
                if (hotelDetailInfoBean != null) {
                    intent = new Intent(this, HotelMapActivity.class);
                    intent.putExtra("date", dateStr);
                    intent.putExtra("bean", hotelDetailInfoBean);
                }
                break;
            case R.id.tv_room_detail:
                if (houHuiYaoDetailInfoBean != null) {
                    intent = new Intent(this, RoomDetailActivity.class);
                    intent.putExtra("date", dateStr);
                    intent.putExtra("hotelId", houHuiYaoDetailInfoBean.hotelId);
                    intent.putExtra("roomId", houHuiYaoDetailInfoBean.roomId);
                    intent.putExtra("room_type", HotelCommDef.TYPE_ALL);
                }
                break;
            case R.id.btn_confirm:
                if (houHuiYaoDetailInfoBean != null) {
                    intent = new Intent(this, RoomOrderConfirmActivity.class);
                    HotelOrderManager.getInstance().setHotelType(HotelCommDef.TYPE_ALL);
                    HotelOrderManager.getInstance().setPayType(HotelCommDef.PAY_PRE);
                    intent.putExtra("roomPrice", houHuiYaoDetailInfoBean.sellprice);
                    intent.putExtra("chooseServiceInfo", (Serializable) chooseServiceInfoMap);
                    intent.putExtra("picUrl", houHuiYaoDetailInfoBean.picpath);
                    intent.putExtra("name", houHuiYaoDetailInfoBean.roomName);
                    intent.putExtra("hotelId", houHuiYaoDetailInfoBean.hotelId);
                    intent.putExtra("hhy", true);
                }
                break;
            default:
                break;
        }

        if (null != intent) {
            startActivity(intent);
        }
    }

    private void requestHouHuiYaoDetail() {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("regretOrderid", orderId);

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.HHY_DETAIL;
        d.flag = AppConst.HHY_DETAIL;
        if (!isProgressShowing()) {
            showProgressDialog(this);
        }

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void requestHotelDetailInfo() {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("hotelId", String.valueOf(houHuiYaoDetailInfoBean.hotelId));
        b.addBody("beginDate", String.valueOf(houHuiYaoDetailInfoBean.starttime));
        b.addBody("endDate", String.valueOf(houHuiYaoDetailInfoBean.endtime));
        b.addBody("type", "4");

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.HOTEL_DETAIL;
        d.flag = AppConst.HOTEL_DETAIL;

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void refreshLayoutInfo() {
        if (houHuiYaoDetailInfoBean != null) {
            root_lay.setVisibility(View.VISIBLE);
            tv_hotel_name.setText(houHuiYaoDetailInfoBean.name);
            tv_hotel_address.setText(houHuiYaoDetailInfoBean.address);
            tv_hotel_phone.setText(houHuiYaoDetailInfoBean.phone);

            tv_room_name.setText(houHuiYaoDetailInfoBean.roomName);
            tv_date.setText(DateUtil.getDay("MM月dd日", houHuiYaoDetailInfoBean.starttime) + "-" + DateUtil.getDay("MM月dd日", houHuiYaoDetailInfoBean.endtime));

            choose_service_lay.removeAllViews();
            chooseServiceInfoMap.clear();
            for (int i = 0; i < houHuiYaoDetailInfoBean.attachs.size(); i++) {
                if (houHuiYaoDetailInfoBean.attachs.get(i).serviceCnt > 0) {
                    TextView tv_service = new TextView(this);
                    tv_service.setTextSize(18f);
                    tv_service.setTextColor(getResources().getColor(R.color.unSelectedTextColor));
                    tv_service.setText(houHuiYaoDetailInfoBean.attachs.get(i).serviceName + " " + getString(R.string.multipleSign) + " " + houHuiYaoDetailInfoBean.attachs.get(i).serviceCnt);
                    choose_service_lay.addView(tv_service);

                    RoomConfirmInfoBean bean = new RoomConfirmInfoBean();
                    bean.serviceCount = houHuiYaoDetailInfoBean.attachs.get(i).serviceCnt;
                    bean.serviceTitle = houHuiYaoDetailInfoBean.attachs.get(i).serviceName;
                    bean.servicePrice = houHuiYaoDetailInfoBean.attachs.get(i).servicePrice;
                    bean.serviceTotalPrice = bean.serviceCount * bean.servicePrice;
                    chooseServiceInfoMap.put(bean.serviceTitle, bean);
                }
            }

            tv_buy_price.setText(houHuiYaoDetailInfoBean.buyprice + "");
            tv_sale_price.setText(houHuiYaoDetailInfoBean.sellprice + "");
        } else {
            root_lay.setVisibility(View.GONE);
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
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.HHY_DETAIL) {
                houHuiYaoDetailInfoBean = JSON.parseObject(response.body.toString(), HouHuiYaoDetailInfoBean.class);
                refreshLayoutInfo();
                if (houHuiYaoDetailInfoBean != null) {
                    requestHotelDetailInfo();
                } else {
                    removeProgressDialog();
                }
            } else if (request.flag == AppConst.HOTEL_DETAIL) {
                removeProgressDialog();
                hotelDetailInfoBean = JSON.parseObject(response.body.toString(), HotelDetailInfoBean.class);
            }
        }
    }
}
