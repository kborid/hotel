package com.huicheng.hotel.android.ui.activity.plane;

import android.content.Intent;
import android.view.View;
import android.widget.Button;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;

/**
 * @auth kborid
 * @date 2017/12/7 0007.
 */

public class PlaneOrderPayActivity extends BaseAppActivity {

    private Button btn_pay;

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_plane_orderpay_layout);
    }

    @Override
    public void initViews() {
        super.initViews();
        btn_pay = (Button) findViewById(R.id.btn_pay);
    }

    @Override
    public void initParams() {
        super.initParams();
        findViewById(R.id.comm_title_rl).setBackgroundColor(getResources().getColor(R.color.white));
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
}
