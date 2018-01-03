package com.huicheng.hotel.android.ui.activity.plane;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.CityAirportInfoBean;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.prj.sdk.app.AppConst;
import com.prj.sdk.app.NetURL;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.LoggerUtil;
import com.prj.sdk.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @auth kborid
 * @date 2017/12/27 0027.
 */

public class PlaneAirportChooserActivity extends BaseActivity {

    private ListView listView;
    private CityAirportAdapter adapter = null;
    private List<CityAirportInfoBean> mList = new ArrayList<>();
    private String airportType = "OFF";

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
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (null != bundle && bundle.getString("type") != null) {
            airportType = bundle.getString("type");
        }
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText("选择飞机场✈");
        findViewById(R.id.comm_title_rl).setBackgroundColor(getResources().getColor(R.color.white));
        adapter = new CityAirportAdapter(this, mList);
        listView.setAdapter(adapter);
    }

    @Override
    public void initListeners() {
        super.initListeners();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CityAirportInfoBean bean = mList.get(position);
                Intent data = new Intent();
                data.putExtra("type", airportType);
                data.putExtra("cityAirport", bean);
                setResult(RESULT_OK, data);
                finish();
            }
        });
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

    private class CityAirportAdapter extends BaseAdapter {
        private Context context;
        private List<CityAirportInfoBean> list = new ArrayList<>();

        CityAirportAdapter(Context context, List<CityAirportInfoBean> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView test = new TextView(context);
            test.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            test.setGravity(Gravity.CENTER);
            test.setPadding(Utils.dp2px(20), Utils.dp2px(10), Utils.dp2px(10), Utils.dp2px(20));
            test.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            test.setText(list.get(position).cityname);
            return test;
        }
    }

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        super.onNotifyMessage(request, response);
        if (response != null && response.body != null) {
            if (request.flag == AppConst.PLANE_AIRPORT_LIST) {
                removeProgressDialog();
                LogUtil.i(TAG, "json = " + response.body.toString());
                LoggerUtil.i(response.body.toString());
                List<CityAirportInfoBean> temp = JSON.parseArray(response.body.toString(), CityAirportInfoBean.class);
                if (temp.size() > 0) {
                    mList.clear();
                    mList.addAll(temp);
                }
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onNotifyError(ResponseData request) {
        super.onNotifyError(request);
        removeProgressDialog();
    }
}
