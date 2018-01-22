package com.huicheng.hotel.android.ui.activity.hotel;

import android.os.Bundle;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.ui.base.BaseActivity;

/**
 * @author kborid
 * @date 2017/2/26 0026
 */
public class HotelHhySellingActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_hotel_hhyselling);
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
        tv_center_title.setText("后悔药");
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
    protected void onDestroy() {
        super.onDestroy();
    }
}
