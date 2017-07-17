package com.huicheng.hotel.android.ui.activity;

import android.os.Bundle;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;

/**
 * @author kborid
 * @date 2017/2/26 0026
 */
public class HouHuiYaoSalingActivity extends BaseActivity implements DataCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_hhysaling_layout);
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
        tv_center_title.setTextColor(getResources().getColor(R.color.houhuiyaoLableColor));
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

    @Override
    public void preExecute(ResponseData request) {

    }

    @Override
    public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
    }
}
