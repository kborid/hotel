package com.huicheng.hotel.android.ui.activity.hotel;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;

/**
 * @author kborid
 * @date 2017/2/26 0026
 */
public class HotelHhySellingActivity extends BaseAppActivity {

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_hotel_hhyselling);
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText("后悔药");
    }
}
