package com.huicheng.hotel.android.ui.activity.plane;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.util.LogUtil;

/**
 * @author kborid
 * @date 2017/3/10 0010
 */
public class PlaneOrderDetailActivity extends BaseAppActivity {

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_plane_orderdetail);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        if (data.hasExtra("pay_result")) {
            Intent intent = new Intent(BroadCastConst.ACTION_PAY_STATUS);
            intent.putExtra("info", data.getExtras().getString("pay_result"));
            LogUtil.i(TAG, "pay_result = " + data.getExtras().getString("pay_result"));
            intent.putExtra("type", "unionPay");
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }
}
