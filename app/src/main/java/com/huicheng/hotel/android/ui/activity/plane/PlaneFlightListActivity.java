package com.huicheng.hotel.android.ui.activity.plane;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
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
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.content.AppConst;
import com.huicheng.hotel.android.content.NetURL;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.AirCompanyInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneFlightInfoBean;
import com.huicheng.hotel.android.tools.CityParseUtils;
import com.huicheng.hotel.android.ui.adapter.PlaneFlightCalendarPriceAdapter;
import com.huicheng.hotel.android.ui.adapter.PlaneFlightItemAdapter;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.huicheng.hotel.android.ui.custom.plane.OnConsiderLayoutResultListener;
import com.huicheng.hotel.android.ui.custom.plane.PlaneConsiderLayout;
import com.huicheng.hotel.android.ui.listener.OnRecycleViewItemClickListener;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.LoggerUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
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

public class PlaneFlightListActivity extends BaseAppActivity {

    private int status;
    private String mOffPiny, mOnPiny;

    private RecyclerView recyclerView;
    private TextView tv_empty;
    private PlaneFlightItemAdapter planeFlightItemAdapter;
    private List<PlaneFlightInfoBean> mFlightList = new ArrayList<>();
    private Comparator<PlaneFlightInfoBean> mComparator = new FlightTimesComparator();
    private long mOffTime = 0;

    private RecyclerView calendarRecycleView;
    private PlaneFlightCalendarPriceAdapter planeFlightCalendarPriceAdapter;
    private List<Date> mDateList = new ArrayList<>();
    private int selectedIndex = 0;

    private RelativeLayout calendar_lay;
    private LinearLayout consider_lay;

    //筛选
    private PopupWindow mPlaneConsiderPopupWindow = null;
    private PlaneConsiderLayout mPlaneConsiderLayout = null;

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_plane_flightlist_layout);
    }

    @Override
    protected void requestData() {
        super.requestData();
        requestPlaneFlightInfo(true);
        if (StringUtil.isEmpty(SharedPreferenceUtil.getInstance().getString(AppConst.AIR_COMPANY_JSON, "", false))) {
            requestAirCompaniesInfo();
        }
    }

    @Override
    public void initViews() {
        super.initViews();
        calendarRecycleView = (RecyclerView) findViewById(R.id.calendarRecycleView);
        calendarRecycleView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
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
        mPlaneConsiderLayout.setOnConsiderLayoutResultListener(new OnConsiderLayoutResultListener() {
            @Override
            public void saveConfig() {
                mPlaneConsiderPopupWindow.dismiss();
                requestPlaneFlightInfo(true);
            }

            @Override
            public void cancelConfig() {
                mPlaneConsiderPopupWindow.dismiss();
                //重置consider
                mPlaneConsiderLayout.cancelConfig();
            }
        });
    }

    @Override
    public void initParams() {
        super.initParams();
        swipeRefreshLayout.setColorSchemeResources(R.color.plane_mainColor);
        findViewById(R.id.comm_title_rl).setBackgroundColor(getResources().getColor(R.color.white));
        status = PlaneOrderManager.instance.getStatus();
        LogUtil.i(TAG, "FlightType = " + PlaneOrderManager.instance.getFlightType() + ", FlowStatus = " + status);

        mOffPiny = PlaneOrderManager.instance.getFlightOffAirportInfo().pinyin;
        mOnPiny = PlaneOrderManager.instance.getFlightOnAirportInfo().pinyin;
        tv_center_title.setText(
                CityParseUtils.getPlaneOffOnCity(
                        PlaneOrderManager.instance.getFlightOffAirportInfo().cityname,
                        PlaneOrderManager.instance.getFlightOnAirportInfo().cityname,
                        "→"
                )
        );

        Calendar curr = Calendar.getInstance(); //当前、今天日期Calendar
        Calendar max = Calendar.getInstance(); //应该显示最大日期Calendar
        Calendar start = Calendar.getInstance(); //航班列表头部起始日期显示calendar

        //计算日历显示的最大日期
        {
            //根据当前日期date，计算calendar显示的最大月份和该月份天数，并转成日期date
            int maxYear = curr.get(Calendar.YEAR);
            int maxMonth = curr.get(Calendar.MONTH) + 5;
            int maxDate = curr.get(Calendar.DATE);
            maxYear = maxMonth > 12 ? maxYear + 1 : maxYear;
            maxMonth = maxMonth > 12 ? maxMonth - 12 : maxMonth;

            //将计算出的最大月份和该月份最大天数转换成最大的日期date
            max.set(maxYear, maxMonth, maxDate);
            max.set(Calendar.DATE, 1);
            max.roll(Calendar.DATE, -1);
            maxDate = max.get(Calendar.DATE);
            max.set(maxYear, maxMonth, maxDate);
        }

        /**
         * 如果当前订票类型是往返，则需要判断返程日期与去程日期，
         * 如果之前记录的返程日期比去程日期小，则默认设置返程日期为去程日期后一天
         */
        mOffTime = PlaneOrderManager.instance.getGoFlightOffDate();
        if (PlaneOrderManager.instance.isBackBookingTypeForGoBack()) {
            long backOffTime = PlaneOrderManager.instance.getBackFlightOffDate();
            start.setTime(new Date(backOffTime));
            if (mOffTime >= backOffTime) {
                start.setTime(new Date(mOffTime));
                start.add(Calendar.DATE, +1);
            }
            mOffTime = start.getTimeInMillis();
        }

        // 设置后的日期打印
        LogUtil.i(TAG, "Current Date:" + printlnTimeStampByDate(curr));
        LogUtil.i(TAG, "OffTime Date:" + printlnTimeStampByDate(mOffTime));
        LogUtil.i(TAG, "Start_t Date:" + printlnTimeStampByDate(start));
        LogUtil.i(TAG, "Maximum Date:" + printlnTimeStampByDate(max));

        //通过最大月份天数和当前日期计算出显示的总天数
        int maxDays = DateUtil.getGapCount(start.getTime(), max.getTime());
        LogUtil.i(TAG, "maxDays = " + maxDays);

        //根据计算结果显示航班列表头部的calendar栏
        for (int i = 0; i <= maxDays; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(start.get(Calendar.YEAR), start.get(Calendar.MONTH), start.get(Calendar.DATE), 0, 0, 0);
            calendar.add(Calendar.DATE, i);
            mDateList.add(calendar.getTime());
        }

        selectedIndex = DateUtil.getGapCount(start.getTime(), new Date(mOffTime));
        planeFlightCalendarPriceAdapter = new PlaneFlightCalendarPriceAdapter(this, mDateList);
        calendarRecycleView.setAdapter(planeFlightCalendarPriceAdapter);
        calendarRecycleView.scrollToPosition(selectedIndex);
        planeFlightCalendarPriceAdapter.setSelectedIndex(selectedIndex);
        planeFlightItemAdapter = new PlaneFlightItemAdapter(this, mFlightList);
        recyclerView.setAdapter(planeFlightItemAdapter);
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        swipeRefreshLayout.setRefreshing(true);
        requestPlaneFlightInfo(false);
    }

    @Override
    public void initListeners() {
        super.initListeners();
        calendar_lay.setOnClickListener(this);
        planeFlightCalendarPriceAdapter.setOnRecycleViewItemClickListener(new OnRecycleViewItemClickListener() {
            @Override
            public void OnItemClick(View v, int position) {
                if (selectedIndex == position) {
                    return;
                }
                selectedIndex = position;
                mOffTime = mDateList.get(position).getTime();
                LogUtil.i(TAG, "SingleLine Selected OffDate:" + printlnTimeStampByDate(mOffTime));
                planeFlightCalendarPriceAdapter.setSelectedIndex(selectedIndex);
                requestPlaneFlightInfo(true);
            }
        });
        planeFlightItemAdapter.setOnRecycleViewItemClickListener(new OnRecycleViewItemClickListener() {
            @Override
            public void OnItemClick(View v, int position) {
                if (PlaneOrderManager.instance.isBackBookingTypeForGoBack()) {
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
                Intent intent = new Intent(this, PlaneCalendarChooseActivity.class);
                startActivityForResult(intent, 0x01);
                break;
        }
    }

    private void requestPlaneFlightInfo(boolean isBusy) {
        LogUtil.i(TAG, "requestPlaneFlightInfo()");
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("date", DateUtil.getDay("yyyy-MM-dd", mOffTime));
        b.addBody("dpt", mOffPiny); //起飞机场
        b.addBody("arr", mOnPiny); //着陆机场
        ResponseData d = b.syncRequest(b);
        d.flag = AppConst.PLANE_FLIGHT_LIST;
        d.path = NetURL.PLANE_FLIGHT_LIST;

        if (!isProgressShowing() && isBusy) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void requestAirCompaniesInfo() {
        LogUtil.i(TAG, "requestAirCompaniesInfo()");
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.PLANE_COMPANY_LIST;
        d.flag = AppConst.PLANE_COMPANY_LIST;
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
    protected void onDestroy() {
        super.onDestroy();
        PlaneOrderManager.instance.setStatus(PlaneCommDef.STATUS_GO);
        LogUtil.i(TAG, "FlightType = " + PlaneOrderManager.instance.getFlightType() + ", FlowStatus = " + status);
    }

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        super.onNotifyMessage(request, response);
        if (response != null && response.body != null) {
            if (request.flag == AppConst.PLANE_FLIGHT_LIST) {
                new DealResponseAsyncTask().execute(response.body.toString());
            } else if (request.flag == AppConst.PLANE_COMPANY_LIST) {
                if (StringUtil.notEmpty(response.body.toString())) {
                    SharedPreferenceUtil.getInstance().setString(AppConst.AIR_COMPANY_JSON, response.body.toString(), false);
                    List<AirCompanyInfoBean> tmp = JSON.parseArray(response.body.toString(), AirCompanyInfoBean.class);
                    SessionContext.setAirCompanyMap(tmp);
                }
            }
        }
    }

    @Override
    public void onNotifyOverrideMessage(ResponseData request, ResponseData response) {
        super.onNotifyOverrideMessage(request, response);
        //刷新数据
        mFlightList.clear();
        planeFlightItemAdapter.notifyDataSetChanged();
        planeFlightCalendarPriceAdapter.setMinPrice(0, mOffTime);
        planeFlightCalendarPriceAdapter.notifyDataSetChanged();
        //刷新筛选框选项
        mPlaneConsiderLayout.updateChildConsiderInfo(mFlightList);
        swipeRefreshLayout.setRefreshing(false);
        if (mFlightList.size() <= 0) {
            tv_empty.setVisibility(View.VISIBLE);
        } else {
            tv_empty.setVisibility(View.GONE);
        }
    }

    @Override
    protected boolean isCheckException(ResponseData request, ResponseData response) {
        if (response != null && response.data != null) {
            if (response.code.equals("1001")) { //flightRequest is invalid
                return true;
            }
        }
        return super.isCheckException(request, response);
    }

    @Override
    public void onNotifyError(ResponseData request, ResponseData response) {
        super.onNotifyError(request, response);
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
            if (StringUtil.notEmpty(params[0]) && !"{}".equals(params[0])) {
                allList = JSON.parseArray(params[0], PlaneFlightInfoBean.class);
            }
            if (allList.size() > 0) {
                //获取最小价格航班
                PlaneFlightInfoBean min = Collections.min(allList, new BarePriceComparator());
                planeFlightItemAdapter.updateMinFlightItemInfo(min);
                minPrice = (int) min.barePrice;


                //刷新数据
                List<PlaneFlightInfoBean> temp = checkConditionsResult(allList);
                Collections.sort(temp, mComparator);
                mFlightList.clear();
                mFlightList.addAll(temp);
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
            planeFlightCalendarPriceAdapter.setMinPrice(minPrice, mOffTime);
            planeFlightCalendarPriceAdapter.notifyDataSetChanged();

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
        String[] considerName = getResources().getStringArray(R.array.PlaneConditionItem);
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
                    int[] times = mPlaneConsiderLayout.getConsiderAirLayout(considerName[0]).getFlightConditionValue();
                    String start = String.format("%1$02d" + String.valueOf(":00"), times[0]);
                    String enddd = String.format("%1$02d" + String.valueOf(":00"), times[1]);
                    //航班起飞时间小于开始时间或大于结束时间，则跳过
                    if (start.compareTo(bean.dptTime) > 0 || enddd.compareTo(bean.dptTime) < 0) {
                        continue;
                    }
                }

                //航空公司
                {
                    int[] airCompanies = mPlaneConsiderLayout.getConsiderAirLayout(considerName[1]).getFlightConditionValue();
                    if (airCompanies != null && airCompanies.length > 0) {
                        if (airCompanies[0] == 0) {
                            //如果值为0或包含0，则表示不限
                            //do nothing
                        } else {
                            boolean isEqual = false;
                            for (int airCompany : airCompanies) {
                                if (bean.carrier.equals(mPlaneConsiderLayout.getConsiderAirLayout(considerName[1]).getDataList(0).get(airCompany))) {
                                    isEqual = true;
                                    break;
                                }
                            }
                            if (!isEqual) {
                                continue;
                            }
                        }
                    }
                }
                //机场信息
                {
                    int[] airports = mPlaneConsiderLayout.getConsiderAirLayout(considerName[2]).getFlightConditionValue();
                    int offIndex = airports[0];
                    int onIndex = airports[1];
                    String offAirport = mPlaneConsiderLayout.getConsiderAirLayout(considerName[2]).getDataList(0).get(offIndex);
                    String onAirport = mPlaneConsiderLayout.getConsiderAirLayout(considerName[2]).getDataList(1).get(onIndex);
                    //起飞机场不等于不限，且不等于选择的机场，则跳过，无需判断降落机场
                    if (!"不限".equals(offAirport) && !bean.dptAirport.equals(offAirport)) {
                        continue;
                    }
                    //起飞机场满足条件，则判断降落机场，降落机场不等于不限，且不等于选择的机场，则跳过
                    if (!"不限".equals(onAirport) && !bean.arrAirport.equals(onAirport)) {
                        continue;
                    }
                }

                //机型信息
                {
                    int[] airTypes = mPlaneConsiderLayout.getConsiderAirLayout(considerName[3]).getFlightConditionValue();
                    if (airTypes != null && airTypes.length > 0) {
                        switch (airTypes[0]) {
                            case PlaneCommDef.FLIGHT_TYPE_BIG:
                                if (!bean.flightTypeFullName.contains("大") && !bean.flightTypeFullName.contains("宽")) {
                                    continue;
                                }
                                break;
                            case PlaneCommDef.FLIGHT_TYPE_MID:
                                if (!bean.flightTypeFullName.contains("中")) {
                                    continue;
                                }
                                break;
                            case PlaneCommDef.FLIGHT_TYPE_SML:
                                if (!bean.flightTypeFullName.contains("小")) {
                                    continue;
                                }
                                break;
                            default:
                                //do nothing
                                break;

                        }
                    }
                }
                //仓位信息
                {
                    int[] airCangs = mPlaneConsiderLayout.getConsiderAirLayout(considerName[4]).getFlightConditionValue();
                    if (airCangs != null && airCangs.length > 0) {
                        switch (airCangs[0]) {
                            case PlaneCommDef.FLIGHT_CANG_JINGJI:
                                if (bean.positionLevel != PlaneCommDef.CabinLevel.CABIN_JINGJI.ordinal()) {
                                    continue;
                                }
                                break;
                            case PlaneCommDef.FLIGHT_CANG_TOUDENG:
                                if (bean.positionLevel != PlaneCommDef.CabinLevel.CABIN_TOUDENG.ordinal()) {
                                    continue;
                                }
                                break;
                            case PlaneCommDef.FLIGHT_CANG_SHANGWU:
                                if (bean.positionLevel != PlaneCommDef.CabinLevel.CABIN_SHANGWU.ordinal()) {
                                    continue;
                                }
                                break;
                            default:
                                // do nothing
                                break;
                        }
                    }
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
                LogUtil.i(TAG, "Calendar Selected Off Date:" + printlnTimeStampByDate(mOffTime));
                selectedIndex = DateUtil.getGapCount(Calendar.getInstance().getTime(), new Date(mOffTime));
                planeFlightCalendarPriceAdapter.setSelectedIndex(selectedIndex);
                calendarRecycleView.scrollToPosition(selectedIndex);
                requestPlaneFlightInfo(true);
            }
        }
    }

    private String printlnTimeStampByDate(long timestamp) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(timestamp));
        return printlnTimeStampByDate(c);
    }

    private String printlnTimeStampByDate(Calendar c) {
        return c.get(Calendar.YEAR) + "-" + c.get(Calendar.MONTH) + "-" + c.get(Calendar.DATE);
    }
}
