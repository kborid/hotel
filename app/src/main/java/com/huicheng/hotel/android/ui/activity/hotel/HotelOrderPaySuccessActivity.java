package com.huicheng.hotel.android.ui.activity.hotel;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.HotelDetailInfoBean;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.huicheng.hotel.android.ui.custom.RoundedAllImageView;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;
import com.huicheng.hotel.android.ui.glide.CustomReqURLFormatModelImpl;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;

/**
 * @author kborid
 * @date 2017/3/13 0013
 */
public class HotelOrderPaySuccessActivity extends BaseAppActivity {

    private HotelDetailInfoBean hotelDetailInfoBean;

    private LinearLayout root_lay;
    private Button btn_vip;
    private TextView tv_hotel_name, tv_room_name, tv_in_date;
    private RoundedAllImageView iv_hotel_icon;
    private TextView tv_hotel_name_big;

    private long beginTime, endTime;
    private String checkRoomDate;
    private String hotelId, roomName;
    private boolean isPrePaySuccess = false;
    private boolean showTipsOrNot = false;

    @Override
    protected void requestData() {
        super.requestData();
        requestHotelDetailInfo();
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_hotel_orderpaysuccess);
    }

    @Override
    public void initViews() {
        super.initViews();
        root_lay = (LinearLayout) findViewById(R.id.root_lay);
        root_lay.setVisibility(View.GONE);
        root_lay.setLayoutAnimation(getAnimationController());
        btn_vip = (Button) findViewById(R.id.btn_vip);
        tv_hotel_name = (TextView) findViewById(R.id.tv_hotel_name);
        tv_room_name = (TextView) findViewById(R.id.tv_room_name);
        tv_in_date = (TextView) findViewById(R.id.tv_in_date);
        iv_hotel_icon = (RoundedAllImageView) findViewById(R.id.iv_hotel_icon);
        tv_hotel_name_big = (TextView) findViewById(R.id.tv_hotel_name_big);
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            if (bundle.getString("checkRoomDate") != null) {
                checkRoomDate = bundle.getString("checkRoomDate");
            }
            if (bundle.getString("roomName") != null) {
                roomName = bundle.getString("roomName");
            }
            if (bundle.getString("hotelId") != null) {
                hotelId = bundle.getString("hotelId");
            }
            beginTime = bundle.getLong("beginTime");
            endTime = bundle.getLong("endTime");
            isPrePaySuccess = bundle.getBoolean("isPrePaySuccess");
            showTipsOrNot = bundle.getBoolean("showTipsOrNot");
        }
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText(getString(R.string.order_waitConfirm));
    }

    private void refreshHotelInfo() {
        if (null != hotelDetailInfoBean) {
            root_lay.setVisibility(View.VISIBLE);
            if (hotelDetailInfoBean.isPopup) {
                btn_vip.setVisibility(View.VISIBLE);
            } else {
                btn_vip.setVisibility(View.GONE);
            }
            tv_hotel_name.setText(hotelDetailInfoBean.name);
            tv_room_name.setText(roomName);
            if (StringUtil.notEmpty(checkRoomDate)) {
                tv_in_date.setVisibility(View.VISIBLE);
                tv_in_date.setText(checkRoomDate);
            } else {
                tv_in_date.setVisibility(View.GONE);
            }
            tv_hotel_name_big.setText(hotelDetailInfoBean.name);
            if (hotelDetailInfoBean.picPath != null
                    && hotelDetailInfoBean.picPath.size() > 0) {
                String pic = hotelDetailInfoBean.picPath.get(0);
                Glide.with(this)
                        .load(new CustomReqURLFormatModelImpl(pic))
                        .placeholder(R.drawable.def_hotel_banner)
                        .crossFade()
                        .centerCrop()
                        .override(650, 480)
                        .into(iv_hotel_icon);
            }
        }
        if (isPrePaySuccess && showTipsOrNot) {
            String province = SharedPreferenceUtil.getInstance().getString(AppConst.PROVINCE, "", false);
            if ("海南省".contains(province)) {
                showBookingAirTicketDialog();
            }
        }
    }

    private void showBookingAirTicketDialog() {
        CustomDialog dialog = new CustomDialog(this);
        dialog.setMessage(getString(R.string.active_bookingAirTicket));
        dialog.setNegativeButton(getString(R.string.active_go), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(HotelOrderPaySuccessActivity.this, HotelMainActivity.class);
                intent.putExtra("index", 1);
                startActivity(intent);
            }
        });
        dialog.setPositiveButton(getString(R.string.active_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void requestHotelDetailInfo() {
        LogUtil.i(TAG, "requestHotelDetailInfo()");
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("hotelId", hotelId);
        b.addBody("beginDate", String.valueOf(beginTime));
        b.addBody("endDate", String.valueOf(endTime));
        b.addBody("type", String.valueOf(HotelCommDef.TYPE_ALL));

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.HOTEL_DETAIL;
        d.flag = AppConst.HOTEL_DETAIL;

        if (!isProgressShowing()) {
            showProgressDialog(this);
        }

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    @Override
    public void initListeners() {
        super.initListeners();
        btn_vip.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
//        super.onClick(v);
        switch (v.getId()) {
            case R.id.btn_back:
                startActivity(new Intent(this, HotelMainActivity.class));
                break;
            case R.id.btn_vip:
                if (hotelDetailInfoBean != null) {
                    showAddVipDialog(this, hotelDetailInfoBean);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.HOTEL_DETAIL) {
                removeProgressDialog();
                LogUtil.i(TAG, "hoteldetail json = " + response.body.toString());
                hotelDetailInfoBean = JSON.parseObject(response.body.toString(), HotelDetailInfoBean.class);
                HotelOrderManager.getInstance().setHotelDetailInfo(hotelDetailInfoBean);
                refreshHotelInfo();
            } else if (request.flag == AppConst.HOTEL_VIP) {
                btn_vip.setVisibility(View.GONE);
            }
        }
    }
}
