package com.huicheng.hotel.android.ui.activity.plane;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.prj.sdk.app.AppConst;
import com.prj.sdk.app.NetURL;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.util.LogUtil;

/**
 * @auth kborid
 * @date 2017/12/27 0027.
 */

public class PlaneAirportChooserActivity extends BaseActivity {

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_plane_airport_layout);
        initViews();
        initParams();
        initListeners();
        if (null == savedInstanceState) {
            requestAirportInfo();
        }
    }

    @Override
    public void initViews() {
        super.initViews();
        listView = (ListView) findViewById(R.id.listView);
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText("选择飞机场✈");
        findViewById(R.id.comm_title_rl).setBackgroundColor(getResources().getColor(R.color.white));
    }

    @Override
    public void initListeners() {
        super.initListeners();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
    }

    private void requestAirportInfo() {
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.PLANE_AIRPORT_LIST;
        d.flag = AppConst.PLANE_AIRPORT_LIST;
        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
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

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        super.onNotifyMessage(request, response);
        if (response != null && response.body != null) {
            if (request.flag == AppConst.PLANE_AIRPORT_LIST) {
                removeProgressDialog();
                LogUtil.i(TAG, "json = " + response.body.toString());
            }
        }
    }

    @Override
    public void onNotifyError(ResponseData request) {
        super.onNotifyError(request);
        removeProgressDialog();
    }
}
