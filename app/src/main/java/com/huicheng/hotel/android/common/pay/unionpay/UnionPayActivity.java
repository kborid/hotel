package com.huicheng.hotel.android.common.pay.unionpay;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.huicheng.hotel.android.ui.activity.MainFragmentActivity;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.util.LogUtil;

/**
 * @auth kborid
 * @date 2017/9/11.
 */

public class UnionPayActivity extends Activity {
    private static final String TAG = "UnionPayActivity";

    private String tn;
    private String mServerMode = UnionPayUtil.RELEASE_MODE;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initParams();
        doDealAction();
    }

    private void initParams() {
        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            if (bundle.getString("tn") != null) {
                tn = bundle.getString("tn");
            }
            if (bundle.getString("ServerMode") != null) {
                mServerMode = bundle.getString("ServerMode");
            }
        }
    }

    private void doDealAction() {
        UnionPayUtil unionPayUtil = new UnionPayUtil(this);
        unionPayUtil.setUnionPayServerMode(mServerMode);
        unionPayUtil.unionStartPay(tn);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.i(TAG, "onActivityResult() " + requestCode + ", " + resultCode);

        if (Activity.RESULT_OK != resultCode) {
            return;
        }
        if (data.hasExtra("pay_result")) {
            Intent intent = new Intent(BroadCastConst.ACTION_PAY_STATUS);
            intent.putExtra("info", data.getExtras().getString("pay_result"));
            LogUtil.i(TAG, "pay_result = " + data.getExtras().getString("pay_result"));
            intent.putExtra("type", "unionPay");
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
        finish();
    }
}
