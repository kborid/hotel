package com.huicheng.hotel.android.ui.activity.plane;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.PlaneCommDef;
import com.huicheng.hotel.android.common.PlaneOrderManager;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneFlightInfoBean;
import com.huicheng.hotel.android.tools.CityParseUtils;
import com.huicheng.hotel.android.ui.activity.CalendarChooseActivity;
import com.huicheng.hotel.android.ui.adapter.OnItemRecycleViewClickListener;
import com.huicheng.hotel.android.ui.adapter.PlaneFlightDatePriceItemAdapter;
import com.huicheng.hotel.android.ui.adapter.PlaneFlightItemAdapter;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.plane.PlaneConsiderLayout;
import com.prj.sdk.app.AppConst;
import com.prj.sdk.app.NetURL;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.LoggerUtil;
import com.prj.sdk.util.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * @auth kborid
 * @date 2017/11/21 0021.
 */

public class PlaneFlightListActivity extends BaseActivity {

    private PlaneCommDef.GoBackStatus status;

    private RecyclerView recyclerView;
    private TextView tv_empty;
    private PlaneFlightItemAdapter planeFlightItemAdapter;
    private List<PlaneFlightInfoBean> mFlightList = new ArrayList<>();
    private Comparator<PlaneFlightInfoBean> mComparator = new FlightTimesComparator();
    private long mOffTime = 0;

    private RecyclerView dateRecycleView;
    private PlaneFlightDatePriceItemAdapter planeFlightDatePriceItemAdapter;
    private List<Date> mDateList = new ArrayList<>();
    private int selectedIndex = 0;

    private RelativeLayout calendar_lay;
    private LinearLayout consider_lay;

    //筛选
    private PopupWindow mPlaneConsiderPopupWindow = null;
    private PlaneConsiderLayout mPlaneConsiderLayout = null;

    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_plane_flightlist_layout);
        initViews();
        initParams();
        initListeners();
        if (null == savedInstanceState) {
            requestPlaneFlightInfo(true);
        }
    }

    @Override
    public void initViews() {
        super.initViews();
        dateRecycleView = (RecyclerView) findViewById(R.id.dateRecycleView);
        dateRecycleView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        calendar_lay = (RelativeLayout) findViewById(R.id.calendar_lay);
        tv_empty = (TextView) findViewById(R.id.tv_empty);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        consider_lay = (LinearLayout) findViewById(R.id.consider_lay);
        //筛选
        mPlaneConsiderLayout = new PlaneConsiderLayout(this);
        mPlaneConsiderPopupWindow = new PopupWindow(mPlaneConsiderLayout, ViewGroup.LayoutParams.MATCH_PARENT, Utils.dp2px(470), true);
        mPlaneConsiderPopupWindow.setAnimationStyle(R.style.share_anim);
        mPlaneConsiderPopupWindow.setFocusable(true);
        mPlaneConsiderPopupWindow.setOutsideTouchable(true);
        mPlaneConsiderPopupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        mPlaneConsiderPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });
        mPlaneConsiderLayout.setOnConsiderLayoutDismissListener(new PlaneConsiderLayout.OnConsiderLayoutDismissListener() {
            @Override
            public void onDismiss(boolean isSave) {
                mPlaneConsiderPopupWindow.dismiss();
                if (isSave) {
                    requestPlaneFlightInfo(true);
                } else {
                    //重置consider
                    mPlaneConsiderLayout.cancelConfig();
                }
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
    }

    @Override
    public void initParams() {
        super.initParams();
        status = PlaneOrderManager.instance.getStatus();
        mOffTime = PlaneOrderManager.instance.getGoFlightOffDate();
        Calendar dd = Calendar.getInstance();
        dd.set(Calendar.DATE, -1);

        //TODO 返程航班查询日历显示处理
        long time = dd.getTime().getTime();
        if (PlaneOrderManager.instance.isBackFlightBack()){
            mOffTime = PlaneOrderManager.instance.getBackFlightOffDate();
            time = PlaneOrderManager.instance.getGoFlightOffDate();
        }

        findViewById(R.id.comm_title_rl).setBackgroundColor(getResources().getColor(R.color.white));
        tv_center_title.setText(
                CityParseUtils.getPlaneOffOnCity(
                        PlaneOrderManager.instance.getFlightOffCity(),
                        PlaneOrderManager.instance.getFlightOnCity(),
                        "→"
                )
        );

        swipeRefreshLayout.setColorSchemeResources(R.color.plane_mainColor);
        swipeRefreshLayout.setDistanceToTriggerSync(200);
        swipeRefreshLayout.setProgressViewOffset(true, 0, Utils.dp2px(20));
        swipeRefreshLayout.setSize(SwipeRefreshLayout.DEFAULT);

        Calendar c = Calendar.getInstance();
        LogUtil.i(TAG, "Current Date:" + c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DATE));
        int maxYear = c.get(Calendar.YEAR);
        int maxMonth = c.get(Calendar.MONTH) + 6;
        maxYear = maxMonth > 12 ? maxYear + 1 : maxYear;
        maxMonth = maxMonth > 12 ? maxMonth - 12 : maxMonth;
        c.set(Calendar.DATE, 1);
        c.roll(Calendar.DATE, -1);
        int maxDate = c.get(Calendar.DATE);
        LogUtil.i(TAG, "Maximum Date:" + maxYear + "-" + maxMonth + "-" + maxDate);
        c.set(maxYear, maxMonth - 1, c.get(Calendar.DATE));
        int max = DateUtil.getGapCount(Calendar.getInstance().getTime(), c.getTime());
        LogUtil.i(TAG, "showAllDays = " + max);
        for (int i = 0; i <= max; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), 0, 0, 0);
            calendar.add(Calendar.DATE, i);
            mDateList.add(calendar.getTime());
        }

        selectedIndex = DateUtil.getGapCount(Calendar.getInstance().getTime(), new Date(mOffTime));
        planeFlightDatePriceItemAdapter = new PlaneFlightDatePriceItemAdapter(this, mDateList);
        dateRecycleView.setAdapter(planeFlightDatePriceItemAdapter);
        planeFlightDatePriceItemAdapter.setSelectedIndex(selectedIndex);
        planeFlightItemAdapter = new PlaneFlightItemAdapter(this, mFlightList);
        recyclerView.setAdapter(planeFlightItemAdapter);
        //初始化筛选条件
        mPlaneConsiderLayout.initConfig();
    }

    @Override
    public void initListeners() {
        super.initListeners();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                requestPlaneFlightInfo(false);
            }
        });
        calendar_lay.setOnClickListener(this);
        planeFlightDatePriceItemAdapter.setOnItemRecycleViewClickListener(new OnItemRecycleViewClickListener() {
            @Override
            public void OnItemClick(View v, int position) {
                if (selectedIndex == position) {
                    return;
                }
                selectedIndex = position;
                mOffTime = mDateList.get(position).getTime();
                planeFlightDatePriceItemAdapter.setSelectedIndex(selectedIndex);
                requestPlaneFlightInfo(true);
            }
        });
        planeFlightItemAdapter.setOnItemRecycleViewClickListener(new OnItemRecycleViewClickListener() {
            @Override
            public void OnItemClick(View v, int position) {
                if (PlaneOrderManager.instance.isBackFlightBack()) {
                    PlaneOrderManager.instance.setBackFlightOffDate(mOffTime);
                } else {
                    PlaneOrderManager.instance.setGoFlightOffDate(mOffTime);
                }
                PlaneOrderManager.instance.setFlightInfo(mFlightList.get(position));
                Intent intent = new Intent(PlaneFlightListActivity.this, PlaneTicketListActivity.class);
                startActivity(intent);
            }
        });

        for (int i = 0; i < consider_lay.getChildCount(); i++) {
            View view = consider_lay.getChildAt(i);
            final int finalI = i;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (finalI) {
                        case 0:
                            showConsiderPopupWindow();
                            break;
                        case 1:
                            consider_lay.getChildAt(0).setSelected(false);
                            consider_lay.getChildAt(2).setSelected(false);
                            if (v.isSelected()) {
                                v.setSelected(false);
                                mComparator = new FlightTimesComparator();
                            } else {
                                v.setSelected(true);
                                mComparator = new OffTimeComparator();
                            }
                            requestPlaneFlightInfo(true);
                            break;
                        case 2:
                            consider_lay.getChildAt(0).setSelected(false);
                            consider_lay.getChildAt(1).setSelected(false);
                            if (v.isSelected()) {
                                v.setSelected(false);
                                mComparator = new FlightTimesComparator();
                            } else {
                                v.setSelected(true);
                                mComparator = new BarePriceComparator();
                            }
                            requestPlaneFlightInfo(true);
                            break;
                    }
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.calendar_lay:
                Intent intent = new Intent(this, CalendarChooseActivity.class);
                startActivityForResult(intent, 0x01);
                break;
        }
    }

    private void requestPlaneFlightInfo(boolean isBusy) {
        LogUtil.i(TAG, "requestPlaneFlightInfo()");
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("date", DateUtil.getDay("yyyy-MM-dd", mOffTime));
        b.addBody("dpt", "PEK"); //起飞机场
        b.addBody("arr", "SHA"); //着陆机场
        ResponseData d = b.syncRequest(b);
        d.flag = AppConst.PLANE_FLIGHT_LIST;
        d.path = NetURL.PLANE_FLIGHT_LIST;

        if (!isProgressShowing() && isBusy) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void showConsiderPopupWindow() {
        mPlaneConsiderLayout.reloadConfig();
        // 设置背景颜色变暗
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);
        mPlaneConsiderPopupWindow.showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
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
        PlaneOrderManager.instance.setStatus(status);
    }

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        super.onNotifyMessage(request, response);
        if (response != null && response.body != null) {
            if (request.flag == AppConst.PLANE_FLIGHT_LIST) {
                new DealResponseAsyncTask().execute(response.body.toString());
            }
        }
    }

    @Override
    public void onNotifyError(ResponseData request) {
        super.onNotifyError(request);
        removeProgressDialog();
        swipeRefreshLayout.setRefreshing(false);
        if (mFlightList.size() <= 0) {
            tv_empty.setVisibility(View.VISIBLE);
        } else {
            tv_empty.setVisibility(View.GONE);
        }
    }

    private class DealResponseAsyncTask extends AsyncTask<String, Integer, Void> {

        private List<PlaneFlightInfoBean> allList = new ArrayList<>();
        private int minPrice = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            LogUtil.i(TAG, "json = " + params[0]);
            LoggerUtil.i(params[0]);
            allList = JSON.parseArray(params[0], PlaneFlightInfoBean.class);
            if (allList.size() > 0) {
                //获取最小价格航班
                PlaneFlightInfoBean min = Collections.min(allList, new BarePriceComparator());
                planeFlightItemAdapter.updateMinFlightItemInfo(min);
                minPrice = (int) min.barePrice;

                //刷新数据
                mFlightList.clear();
                mFlightList.addAll(checkConditionsResult(allList));
                Collections.sort(mFlightList, mComparator);
            }
            LogUtil.i(TAG, "flight count = " + mFlightList.size());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            LogUtil.i(TAG, "onPostExecute()");
            removeProgressDialog();
            swipeRefreshLayout.setRefreshing(false);
            planeFlightItemAdapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(0);
            if (mFlightList.size() <= 0) {
                tv_empty.setVisibility(View.VISIBLE);
            } else {
                tv_empty.setVisibility(View.GONE);
            }
            planeFlightDatePriceItemAdapter.setMinPrice(minPrice, mOffTime);
            planeFlightDatePriceItemAdapter.notifyDataSetChanged();
            //刷新筛选框选项
            mPlaneConsiderLayout.updateChildConsiderInfo(allList);
        }
    }

    /**
     * 航班价格排序比较器
     */
    private static class BarePriceComparator implements Comparator<PlaneFlightInfoBean> {

        @Override
        public int compare(PlaneFlightInfoBean o1, PlaneFlightInfoBean o2) {
            return Double.valueOf(o1.barePrice).compareTo(o2.barePrice);
        }
    }

    /**
     * 起飞时间排序比较器
     */
    private static class OffTimeComparator implements Comparator<PlaneFlightInfoBean> {
        @Override
        public int compare(PlaneFlightInfoBean o1, PlaneFlightInfoBean o2) {
            return o1.dptTime.compareTo(o2.dptTime);
        }
    }

    /**
     * 飞行时间排序比较器(默认)
     */
    private static class FlightTimesComparator implements Comparator<PlaneFlightInfoBean> {
        @Override
        public int compare(PlaneFlightInfoBean o1, PlaneFlightInfoBean o2) {
            String formatStr1 = "", formatStr2 = "";
            if (o1.flightTimes.contains("天")) {
                formatStr1 += "dd天";
            }
            if (o1.flightTimes.contains("小时")) {
                formatStr1 += "HH小时";
            }
            if (o1.flightTimes.contains("分钟")) {
                formatStr1 += "mm分钟";
            }

            if (o2.flightTimes.contains("天")) {
                formatStr2 += "dd天";
            }
            if (o2.flightTimes.contains("小时")) {
                formatStr2 += "HH小时";
            }
            if (o2.flightTimes.contains("分钟")) {
                formatStr2 += "mm分钟";
            }

            try {
                Date d1 = new SimpleDateFormat(formatStr1).parse(o1.flightTimes);
                Date d2 = new SimpleDateFormat(formatStr2).parse(o2.flightTimes);
                return d1.compareTo(d2);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }
    }

    private List<PlaneFlightInfoBean> checkConditionsResult(List<PlaneFlightInfoBean> list) {
        LogUtil.i(TAG, "checkConditionsResult()");
        List<PlaneFlightInfoBean> temp = null;
        if (list != null && list.size() > 0) {
            temp = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                PlaneFlightInfoBean bean = list.get(i);
                //是否直达
                {
                    if (mPlaneConsiderLayout.isStraight()) {
                        //航班经停，则跳过
                        if (bean.stop) {
                            continue;
                        }
                    }
                }

                //起飞时段
                {
                    float[] hours = mPlaneConsiderLayout.getConsiderAirOffTimeLayout().getOffTimeStartEnd();
                    String startHour = String.format("%1$02d:00", (int) hours[0]);
                    String endHour = String.format("%1$02d:00", (int) hours[1]);
                    //航班起飞时间小于开始时间或大于结束时间，则跳过
                    if (startHour.compareTo(bean.dptTime) > 0 || endHour.compareTo(bean.dptTime) < 0) {
                        continue;
                    }
                }

                //TODO 航空公司
                {
                }
                //TODO 机场信息
                {
                }
                //TODO 机型信息
                {
                }
                //TODO 仓位信息
                {
                }

                temp.add(bean);
            }
        }
        return temp;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == 0x01) {
            if (null != data) {
                long tmp = data.getLongExtra("beginTime", mOffTime);
                if (tmp == mOffTime) {
                    return;
                }
                mOffTime = tmp;
                selectedIndex = DateUtil.getGapCount(Calendar.getInstance().getTime(), new Date(mOffTime));
                dateRecycleView.scrollToPosition(selectedIndex);
                planeFlightDatePriceItemAdapter.setSelectedIndex(selectedIndex);
                requestPlaneFlightInfo(true);
            }
        }
    }
}
