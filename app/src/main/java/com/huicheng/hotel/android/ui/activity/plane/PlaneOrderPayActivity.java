package com.huicheng.hotel.android.ui.activity.plane;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.ui.base.BaseActivity;

/**
 * @auth kborid
 * @date 2017/12/7 0007.
 */

public class PlaneOrderPayActivity extends BaseActivity {

    private Button btn_pay;

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
        btn_pay = (Button) findViewById(R.id.btn_pay);
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText("支付方式");
    }

    @Override
    public void initListeners() {
        super.initListeners();
        btn_pay.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btn_pay:
                Intent intent = new Intent(this, PlaneOrderPaySuccessActivity.class);
                startActivity(intent);
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
}
