package com.huicheng.hotel.android.ui.activity.plane;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.ui.base.BaseActivity;

/**
 * @auth kborid
 * @date 2017/11/22 0022.
 */

public class PlaneOrderActivity extends BaseActivity {

    private Switch btn_switch;
    private LinearLayout test_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_planeorder_layout);
        initViews();
        initParams();
        initListeners();
    }

    @Override
    public void initViews() {
        super.initViews();
        btn_switch = (Switch) findViewById(R.id.btn_switch);
        test_layout = (LinearLayout) findViewById(R.id.test_layout);
    }

    @Override
    public void initParams() {
        super.initParams();
        findViewById(R.id.comm_title_rl).setBackgroundColor(getResources().getColor(R.color.white));
        tv_center_title.setText("杭州 → 北京");
    }

    @Override
    public void initListeners() {
        super.initListeners();
        btn_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    test_layout.setVisibility(View.VISIBLE);
                } else {
                    test_layout.setVisibility(View.GONE);
                }
            }
        });
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
