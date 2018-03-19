package com.huicheng.hotel.android.ui.activity.plane;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;

/**
 * @author kborid
 * @date 2018/3/16 0016.
 */

public class PlaneBackTicketActivity extends BaseAppActivity {
    @Override
    protected void setContentView() {
        setContentView(R.layout.act_plane_backticket);
    }

    @Override
    protected void initParams() {
        super.initParams();
        findViewById(R.id.comm_title_rl).setBackgroundColor(getResources().getColor(R.color.white));
        tv_center_title.setText("申请退款");
    }
}
