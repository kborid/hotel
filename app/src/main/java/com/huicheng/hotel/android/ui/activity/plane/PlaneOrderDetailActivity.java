package com.huicheng.hotel.android.ui.activity.plane;

import android.os.Bundle;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.ui.base.BaseActivity;

/**
 * @author kborid
 * @date 2017/3/10 0010
 */
public class PlaneOrderDetailActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_plane_orderdetail_layout);
        initViews();
        initParams();
        initListeners();
    }

    @Override
    public void initViews() {
        super.initViews();
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText("机票订单详情");
        findViewById(R.id.comm_title_rl).setBackgroundColor(getResources().getColor(R.color.white));
        setRightButtonResource(R.drawable.iv_back_white);
    }

    @Override
    public void initListeners() {
        super.initListeners();
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
