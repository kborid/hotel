package com.huicheng.hotel.android.ui.activity.plane;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.PlaneCommDef;
import com.huicheng.hotel.android.common.PlaneOrderManager;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.plane.CustomInfoLayoutForPlane;

/**
 * @auth kborid
 * @date 2017/11/22 0022.
 */

public class PlaneNewOrderActivity extends BaseActivity {

    //预订航班信息
    private LinearLayout flight_layout;
    private LinearLayout flight_flag_layout;
    private CustomInfoLayoutForPlane custom_info_layout_plane;

    private Switch btn_invoice_switch;
    private LinearLayout invoice_lay;
    private TextView tv_invoice_tips;

    private TextView tv_chooser;
    private TextView tv_submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_plane_neworder_layout);
        initViews();
        initParams();
        initListeners();
    }

    @Override
    public void initViews() {
        super.initViews();
        flight_layout = (LinearLayout) findViewById(R.id.flight_layout);
        flight_flag_layout = (LinearLayout) findViewById(R.id.flight_flag_layout);

        custom_info_layout_plane = (CustomInfoLayoutForPlane) findViewById(R.id.custom_info_layout_plane);

        btn_invoice_switch = (Switch) findViewById(R.id.btn_invoice_switch);
        invoice_lay = (LinearLayout) findViewById(R.id.invoice_lay);
        tv_invoice_tips = (TextView) findViewById(R.id.tv_invoice_tips);
        tv_chooser = (TextView) findViewById(R.id.tv_chooser);
        tv_submit = (TextView) findViewById(R.id.tv_submit);
    }

    @Override
    public void initParams() {
        super.initParams();
        findViewById(R.id.comm_title_rl).setBackgroundColor(getResources().getColor(R.color.white));
        tv_center_title.setText("杭州 → 北京");
        if (PlaneOrderManager.Instance.getFlightType() == PlaneCommDef.FLIGHT_GO_BACK) {
            flight_flag_layout.addView(LayoutInflater.from(this).inflate(R.layout.layout_plane_order_item, null));
            flight_flag_layout.getChildAt(0).findViewById(R.id.tv_flight_flag).setVisibility(View.VISIBLE);
            ((TextView) flight_flag_layout.getChildAt(0).findViewById(R.id.tv_flight_flag)).setText("去程：");
            flight_flag_layout.getChildAt(1).findViewById(R.id.tv_flight_flag).setVisibility(View.VISIBLE);
            ((TextView) flight_flag_layout.getChildAt(1).findViewById(R.id.tv_flight_flag)).setText("返程：");
        }

        //initialize
        btn_invoice_switch.setChecked(false);
        tv_invoice_tips.setVisibility(View.VISIBLE);
        invoice_lay.setVisibility(View.GONE);
    }

    @Override
    public void initListeners() {
        super.initListeners();
        btn_invoice_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tv_invoice_tips.setVisibility(View.GONE);
                    invoice_lay.setVisibility(View.VISIBLE);
                } else {
                    tv_invoice_tips.setVisibility(View.VISIBLE);
                    invoice_lay.setVisibility(View.GONE);
                }
            }
        });
        tv_chooser.setOnClickListener(this);
        tv_submit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.tv_chooser: {
                Intent intent = new Intent(this, PlaneAddrChooserActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.tv_submit: {
                Intent intent = new Intent(this, PlaneOrderPayActivity.class);
                startActivity(intent);
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
}
