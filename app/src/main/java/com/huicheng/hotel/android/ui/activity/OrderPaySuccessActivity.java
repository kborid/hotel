package com.huicheng.hotel.android.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.RoundedAllImageView;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.widget.CustomToast;

/**
 * @author kborid
 * @date 2017/3/13 0013
 */
public class OrderPaySuccessActivity extends BaseActivity implements DataCallback {

    private Button btn_vip;
    private TextView tv_hotel_name, tv_room_name, tv_in_date;
    private RoundedAllImageView iv_hotel_icon;
    private TextView tv_hotel_name_big;
    private String checkRoomDate = null;
    private String hotelName = null, roomName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_ordersuccess_layout);
        getWindow().getDecorView().setVisibility(View.GONE);
        initViews();
        initParams();
        initListeners();
    }

    @Override
    public void initViews() {
        super.initViews();
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
            if (bundle.getString("checkRoomDate")!=null) {
                checkRoomDate = bundle.getString("checkRoomDate");
            }
            if (bundle.getString("hotelName") != null && bundle.getString("roomName") != null) {
                hotelName = bundle.getString("hotelName");
                roomName = bundle.getString("roomName");
            }
        }
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText("订单成功");
        tv_hotel_name.setText(hotelName);
        tv_room_name.setText(roomName);
        if (StringUtil.notEmpty(checkRoomDate)) {
            tv_in_date.setVisibility(View.VISIBLE);
            tv_in_date.setText(checkRoomDate);
        } else {
            tv_in_date.setVisibility(View.GONE);
        }
        tv_hotel_name_big.setText(hotelName);
        String pic0Url = "";
        if (HotelOrderManager.getInstance().getHotelDetailInfo().picPath != null
                && HotelOrderManager.getInstance().getHotelDetailInfo().picPath.size() > 0) {
            pic0Url = HotelOrderManager.getInstance().getHotelDetailInfo().picPath.get(0);
        }
        loadImage(iv_hotel_icon, pic0Url, 1280, 1024);
        getWindow().getDecorView().setVisibility(View.VISIBLE);
    }

    private void requestHotelVip(int hotelId) {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("hotelId", String.valueOf(hotelId));

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.HOTEL_VIP;
        d.flag = AppConst.HOTEL_VIP;

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
                startActivity(new Intent(this, MainFragmentActivity.class));
                break;
            case R.id.btn_vip:
                requestHotelVip(HotelOrderManager.getInstance().getHotelDetailInfo().id);
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
    public void preExecute(ResponseData request) {

    }

    @Override
    public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.HOTEL_VIP) {
                removeProgressDialog();
                System.out.println("Json = " + response.body.toString());
                CustomToast.show("您已成为该酒店会员", CustomToast.LENGTH_SHORT);
            }
        }
    }
}
