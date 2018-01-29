package com.huicheng.hotel.android.ui.activity.plane;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;

/**
 * @auth kborid
 * @date 2017/11/24 0024.
 */

public class PlaneAddrEditActivity extends BaseAppActivity {
    @Override
    protected void setContentView() {
        setContentView(R.layout.act_plane_addredit_layout);
    }

    @Override
    protected void initParams() {
        super.initParams();
        findViewById(R.id.comm_title_rl).setBackgroundColor(getResources().getColor(R.color.white));
        tv_center_title.setText("添加地址");
        setRightButtonResource("保存", getResources().getColor(R.color.plane_mainColor));
    }
}
