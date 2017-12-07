package com.huicheng.hotel.android.ui.activity.plane;

import android.os.Bundle;
import android.view.View;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.ui.base.BaseActivity;

/**
 * @auth kborid
 * @date 2017/12/7 0007.
 */

public class PlaneOrderPayActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_plane_orderpay_layout);
        initViews();
        initParams();
        initListeners();
    }

    @Override
    public void initViews() {
        super.initViews();
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText("支付方式");
    }

    @Override
    public void initListeners() {
        super.initListeners();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
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
}
