package com.huicheng.hotel.android.ui.activity.plane;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.PlaneCommDef;
import com.huicheng.hotel.android.common.PlaneOrderManager;
import com.huicheng.hotel.android.content.AppConst;
import com.huicheng.hotel.android.content.NetURL;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneTicketInfoBean;
import com.huicheng.hotel.android.ui.adapter.OnItemRecycleViewClickListener;
import com.huicheng.hotel.android.ui.adapter.PlaneTicketVendorItemAdapter;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.LoggerUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @auth kborid
 * @date 2017/11/22 0022.
 */

public class PlaneTicketListActivity extends BaseAppActivity {

    private int status;
    private long mOffTime = 0;
    private String mOff3Code, mOn3Code;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listview;
    private PlaneTicketInfoBean mTicketBean = null;
    private List<PlaneTicketInfoBean.VendorInfo> mVendorList = new ArrayList<>();
    private PlaneTicketVendorItemAdapter adapter;
    private View mListHeaderView;
    private TextView tv_off_city, tv_on_city;
    private TextView tv_off_time, tv_on_time;
    private TextView tv_off_airport, tv_on_airport;
    private TextView tv_off_terminal, tv_on_terminal;
    private LinearLayout stopover_lay;
    private TextView tv_flight_during;
    private LinearLayout flight_server_lay;

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_plane_ticketlist_layout);
    }

    @Override
    protected void requestData() {
        super.requestData();
        requestPlaneTicketInfo(true);
    }

    @Override
    public void initViews() {
        super.initViews();
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        listview = (ListView) findViewById(R.id.listview);
        TextView tv_empty = new TextView(this);
        tv_empty.setText(getString(R.string.empty_flight_list));
        tv_empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        tv_empty.setTextColor(getResources().getColor(R.color.commonHintColor));
        listview.setEmptyView(tv_empty);
        mListHeaderView = LayoutInflater.from(this).inflate(R.layout.lv_plane_ticket_header, null);
        tv_off_city = (TextView) mListHeaderView.findViewById(R.id.tv_off_city);
        tv_off_time = (TextView) mListHeaderView.findViewById(R.id.tv_off_time);
        tv_off_airport = (TextView) mListHeaderView.findViewById(R.id.tv_off_airport);
        tv_off_terminal = (TextView) mListHeaderView.findViewById(R.id.tv_off_terminal);
        tv_on_city = (TextView) mListHeaderView.findViewById(R.id.tv_on_city);
        tv_on_time = (TextView) mListHeaderView.findViewById(R.id.tv_on_time);
        tv_on_airport = (TextView) mListHeaderView.findViewById(R.id.tv_on_airport);
        tv_on_terminal = (TextView) mListHeaderView.findViewById(R.id.tv_on_terminal);
        stopover_lay = (LinearLayout) mListHeaderView.findViewById(R.id.stopover_lay);
        tv_flight_during = (TextView) mListHeaderView.findViewById(R.id.tv_flight_during);
        flight_server_lay = (LinearLayout) mListHeaderView.findViewById(R.id.flight_server_lay);
    }

    @Override
    public void initParams() {
        super.initParams();
        findViewById(R.id.comm_title_rl).setBackgroundColor(getResources().getColor(R.color.white));
        tv_center_title.setText(DateUtil.getDay("M月d日", mOffTime) + DateUtil.dateToWeek2(new Date(mOffTime)));
        setRightButtonResource(R.drawable.iv_plane_share);

        swipeRefreshLayout.setColorSchemeResources(R.color.plane_mainColor);
        swipeRefreshLayout.setDistanceToTriggerSync(200);
        swipeRefreshLayout.setProgressViewOffset(true, 0, Utils.dp2px(20));
        swipeRefreshLayout.setSize(SwipeRefreshLayout.DEFAULT);

        status = PlaneOrderManager.instance.getStatus();
        LogUtil.i(TAG, "FlightType = " + PlaneOrderManager.instance.getFlightType() + ", FlowStatus = " + status);
        mOffTime = PlaneOrderManager.instance.getGoFlightOffDate();
        mOff3Code = PlaneOrderManager.instance.getFlightInfo().dpt;
        mOn3Code = PlaneOrderManager.instance.getFlightInfo().arr;
        if (PlaneOrderManager.instance.isBackBookingTypeForGoBack()) {
            mOffTime = PlaneOrderManager.instance.getBackFlightOffDate();
            //如果往返，则交换起飞着陆机场信息
//            String tmp = mOn3Code;
//            mOn3Code = mOff3Code;
//            mOff3Code = tmp;
        }

        adapter = new PlaneTicketVendorItemAdapter(this, mVendorList);
        listview.setAdapter(adapter);
        listview.addHeaderView(mListHeaderView);
    }

    @Override
    public void initListeners() {
        super.initListeners();
        adapter.setOnItemRecycleViewClickListener(new OnItemRecycleViewClickListener() {
            @Override
            public void OnItemClick(View v, int position) {
                Intent intent;
                PlaneOrderManager.instance.setTicketInfo(mTicketBean);
                PlaneOrderManager.instance.setVendorInfo(mVendorList.get(position));
                if (PlaneOrderManager.instance.isGoBookingTypeForGoBack()) {
                    PlaneOrderManager.instance.setStatus(PlaneCommDef.STATUS_BACK);
                    intent = new Intent(PlaneTicketListActivity.this, PlaneFlightListActivity.class);
                } else {
                    intent = new Intent(PlaneTicketListActivity.this, PlaneNewOrderActivity.class);
                }
                startActivity(intent);
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                requestPlaneTicketInfo(false);
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
    }

    private void requestPlaneTicketInfo(boolean isBusy) {
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("dpt", mOff3Code); //起飞机场
        b.addBody("arr", mOn3Code); //着陆机场
        b.addBody("date", DateUtil.getDay("yyyy-MM-dd", mOffTime));
        b.addBody("flightNum", PlaneOrderManager.instance.getFlightInfo().flightNum);
        ResponseData d = b.syncRequest(b);
        d.flag = AppConst.PLANE_TICKET_LIST;
        d.path = NetURL.PLANE_TICKET_LIST;
        if (!isProgressShowing() && isBusy) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void updateTicketHeaderInfo() {
        if (null != mTicketBean) {
            //起飞信息
            tv_off_city.setText(PlaneOrderManager.instance.getFlightOffAirportInfo().cityname);
            tv_off_time.setText(mTicketBean.btime);
            tv_off_airport.setText(mTicketBean.depAirport);
            tv_off_terminal.setText(mTicketBean.depTerminal);
            //降落信息
            tv_on_city.setText(PlaneOrderManager.instance.getFlightOnAirportInfo().cityname);
            tv_on_time.setText(mTicketBean.etime);
            tv_on_airport.setText(mTicketBean.arrAirport);
            tv_on_terminal.setText(mTicketBean.arrTerminal);
            //经停
            if (mTicketBean.stop) {
                stopover_lay.setVisibility(View.VISIBLE);
                ((TextView) stopover_lay.findViewById(R.id.tv_stop_city)).setText("经停" + mTicketBean.stopCityName);
            } else {
                stopover_lay.setVisibility(View.GONE);
            }
            //航班飞行时间
            tv_flight_during.setText(PlaneOrderManager.instance.getFlightInfo().flightTimes);

            //航班基本信息
            ArrayList<String> temp = new ArrayList<>();
            //航班名称、代号
            if (StringUtil.notEmpty(mTicketBean.com) || StringUtil.notEmpty(mTicketBean.code)) {
                temp.add(mTicketBean.com + mTicketBean.code);
            }
            //航班机型
            if (StringUtil.notEmpty(PlaneOrderManager.instance.getFlightInfo().flightTypeFullName)) {
                temp.add(PlaneOrderManager.instance.getFlightInfo().flightTypeFullName);
            }
            //准点率
            if (StringUtil.notEmpty(mTicketBean.correct)) {
                temp.add("准点率  " + mTicketBean.correct);
            }
            //有无餐食
            temp.add(String.format("%1$s餐食", mTicketBean.meal ? "有" : "无"));
            if (temp.size() <= 0) {
                flight_server_lay.setVisibility(View.GONE);
            }

            flight_server_lay.removeAllViews();
            for (int i = 0; i < temp.size(); i++) {
                TextView tv_server = new TextView(this);
                tv_server.setTextColor(Color.parseColor("#666666"));
                tv_server.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                tv_server.setGravity(Gravity.CENTER);
                tv_server.setText(temp.get(i));
                //the fucking divider's line
                if (i > 0) {
                    View line = new View(this);
                    line.setBackgroundColor(Color.parseColor("#666666"));
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Utils.dp2px(1), ViewGroup.LayoutParams.MATCH_PARENT);
                    lp.setMargins(Utils.dp2px(6), Utils.dp2px(3), Utils.dp2px(6), Utils.dp2px(3));
                    flight_server_lay.addView(line, lp);
                }
                flight_server_lay.addView(tv_server);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PlaneOrderManager.instance.setStatus(status);
        LogUtil.i(TAG, "FlightType = " + PlaneOrderManager.instance.getFlightType() + ", FlowStatus = " + status);
    }

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        super.onNotifyMessage(request, response);
        if (response != null && response.body != null) {
            if (request.flag == AppConst.PLANE_TICKET_LIST) {
                removeProgressDialog();
                swipeRefreshLayout.setRefreshing(false);
                LogUtil.i(TAG, "json = " + response.body.toString());
                LoggerUtil.i(response.body.toString());
                mTicketBean = JSON.parseObject(response.body.toString(), PlaneTicketInfoBean.class);
                if (mTicketBean != null && mTicketBean.vendors != null && mTicketBean.vendors.size() > 0) {
                    mVendorList.clear();
                    mVendorList.addAll(mTicketBean.vendors);
                    Collections.sort(mVendorList);
                }
                updateTicketHeaderInfo();
                adapter.setFlightCompany(mTicketBean.com);
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onNotifyError(ResponseData request, ResponseData response) {
        super.onNotifyError(request, response);
        swipeRefreshLayout.setRefreshing(false);
    }
}
