package com.huicheng.hotel.android.ui.activity.plane;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.ui.activity.UcOrdersActivity;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.prj.sdk.util.BitmapUtils;

public class PlaneOrderPaySuccessActivity extends BaseActivity {

    private static final int ORDER_SINGLE = 0;
    private static final int ORDER_GOBACK = 1;

    private ImageView iv_pay_success;
    private LinearLayout order_info_lay;
    private TextView tv_gohome, tv_myorder;

    private View orderLayout;
    private int mPlaneOrderType = ORDER_SINGLE;
    private ImageView iv_plane_icon, iv_plane_icon_back;
    private TextView tv_plane_name, tv_plane_name_back;
    private TextView tv_plane_code, tv_plane_code_back;
    private TextView tv_plane_date, tv_plane_date_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_plane_paysuccess_layout);
        initViews();
        initParams();
        initListeners();
    }

    @Override
    public void initViews() {
        super.initViews();
        iv_pay_success = (ImageView) findViewById(R.id.iv_pay_success);
        order_info_lay = (LinearLayout) findViewById(R.id.order_info_lay);
        tv_gohome = (TextView) findViewById(R.id.tv_gohome);
        tv_myorder = (TextView) findViewById(R.id.tv_myorder);
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            mPlaneOrderType = bundle.getInt("planeOrderType");
        }
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText("订单成功");
        iv_pay_success.setImageBitmap(BitmapUtils.getAlphaBitmap(iv_pay_success.getDrawable(), getResources().getColor(R.color.plane_mainColor)));
        mPlaneOrderType = ORDER_GOBACK;
        order_info_lay.removeAllViews();
        if (ORDER_SINGLE == mPlaneOrderType) {
            orderLayout = LayoutInflater.from(this).inflate(R.layout.layout_gosingle_order_item, null);
        } else {
            orderLayout = LayoutInflater.from(this).inflate(R.layout.layout_goback_order_item, null);
            iv_plane_icon_back = (ImageView)orderLayout.findViewById(R.id.iv_plane_icon_back);
            tv_plane_name_back = (TextView) orderLayout.findViewById(R.id.tv_plane_name_back);
            tv_plane_code_back = (TextView)orderLayout.findViewById(R.id.tv_plane_code_back);
            tv_plane_date_back = (TextView)orderLayout.findViewById(R.id.tv_plane_date_back);
        }
        iv_plane_icon = (ImageView)orderLayout.findViewById(R.id.iv_plane_icon);
        tv_plane_name = (TextView) orderLayout.findViewById(R.id.tv_plane_name);
        tv_plane_code = (TextView)orderLayout.findViewById(R.id.tv_plane_code);
        tv_plane_date = (TextView)orderLayout.findViewById(R.id.tv_plane_date);
        order_info_lay.addView(orderLayout);
    }

    @Override
    public void initListeners() {
        super.initListeners();
        tv_gohome.setOnClickListener(this);
        tv_myorder.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_back:
            case R.id.tv_gohome: {
                startActivity(new Intent(this, PlaneMainActivity.class));
                break;
            }
            case R.id.tv_myorder: {
                startActivity(new Intent(this, PlaneMainActivity.class));
                startActivity(new Intent(this, UcOrdersActivity.class));
                break;
            }
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
            iv_back.performClick();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}