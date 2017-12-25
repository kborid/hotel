package com.huicheng.hotel.android.ui.activity.plane;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.PlaneOrderManager;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneFlightItemInfoBean;
import com.huicheng.hotel.android.tools.CityParseUtils;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.plane.PlaneConsiderLayout;
import com.orhanobut.logger.Logger;
import com.prj.sdk.app.AppConst;
import com.prj.sdk.app.NetURL;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

    private static final int CALENDAR_SHOW_MAX = 100;

    private ListView listview;
    private PlaneItemAdapter planeItemAdapter;
    private List<PlaneFlightItemInfoBean> mFlightList = new ArrayList<>();
    private Comparator<PlaneFlightItemInfoBean> mComparator = new FlightTimesComparator();
    private PlaneFlightItemInfoBean minBean = null;
    private long mOffTime = 0;

    private Gallery gallery;
    private PlaneDatePriceAdapter planeDatePriceAdapter;
    private List<Date> mDateList = new ArrayList<>();
    private int mCurrentPrice = 0;

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
    }

    @Override
    public void initViews() {
        super.initViews();
        calendar_lay = (RelativeLayout) findViewById(R.id.calendar_lay);
        gallery = (Gallery) findViewById(R.id.gallery);
        listview = (ListView) findViewById(R.id.listview);
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
    public void dealIntent() {
        super.dealIntent();
    }

    @Override
    public void initParams() {
        super.initParams();
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

        mOffTime = PlaneOrderManager.instance.getFlightOffDate();
        int selection = DateUtil.getGapCount(Calendar.getInstance().getTime(), new Date(mOffTime));
        int max = selection < CALENDAR_SHOW_MAX ? CALENDAR_SHOW_MAX : selection;
        for (int i = 0; i < max; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
            calendar.add(Calendar.DAY_OF_MONTH, i);
            mDateList.add(calendar.getTime());
        }
        planeDatePriceAdapter = new PlaneDatePriceAdapter(this, mDateList);
        gallery.setAdapter(planeDatePriceAdapter);
        gallery.setCallbackDuringFling(false);
//        setCallbackOnUnselectedItemClick(gallery, false);
        gallery.setSelection(selection);

        planeItemAdapter = new PlaneItemAdapter(this, mFlightList);
        listview.setAdapter(planeItemAdapter);
    }

    private void setCallbackOnUnselectedItemClick(Gallery gallery, boolean flag) {
        Class<?> galleryWidget = gallery.getClass();
        Method callbackOnUnselectedItemClick = null;
        Field test = null;
        try {
            test = galleryWidget.getDeclaredField("mShouldCallbackOnUnselectedItemClick");
            callbackOnUnselectedItemClick = galleryWidget.getMethod("setCallbackOnUnselectedItemClick", Boolean.class);
            callbackOnUnselectedItemClick.invoke(galleryWidget, flag);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(PlaneFlightListActivity.this, PlaneTicketListActivity.class);
                startActivity(intent);
            }
        });
        gallery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //TODO 点击切换动作待优化
                mOffTime = mDateList.get(position).getTime();
                requestPlaneFlightInfo(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
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
                break;
        }
    }

    private void requestPlaneFlightInfo(boolean isBusy) {
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
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        calendar_lay.setMinimumHeight(gallery.getHeight());
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

    private class PlaneItemAdapter extends BaseAdapter {
        private Context context;
        private List<PlaneFlightItemInfoBean> mList = new ArrayList<>();

        PlaneItemAdapter(Context context, List<PlaneFlightItemInfoBean> list) {
            this.context = context;
            this.mList = list;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (null == convertView) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.lv_plane_flight_item, null);
                viewHolder.tv_off_time = (TextView) convertView.findViewById(R.id.tv_off_time);
                viewHolder.tv_off_airport = (TextView) convertView.findViewById(R.id.tv_off_airport);
                viewHolder.tv_off_terminal = (TextView) convertView.findViewById(R.id.tv_off_terminal);
                viewHolder.tv_on_time = (TextView) convertView.findViewById(R.id.tv_on_time);
                viewHolder.tv_on_airport = (TextView) convertView.findViewById(R.id.tv_on_airport);
                viewHolder.tv_on_terminal = (TextView) convertView.findViewById(R.id.tv_on_terminal);
                viewHolder.stopover_lay = (LinearLayout) convertView.findViewById(R.id.stopover_lay);
                viewHolder.tv_stop_city = (TextView) convertView.findViewById(R.id.tv_stop_city);
                viewHolder.tv_flight_during = (TextView) convertView.findViewById(R.id.tv_flight_during);
                viewHolder.tv_flight_price = (TextView) convertView.findViewById(R.id.tv_flight_price);
                viewHolder.iv_flight_icon = (ImageView) convertView.findViewById(R.id.iv_flight_icon);
                viewHolder.tv_flight_company = (TextView) convertView.findViewById(R.id.tv_flight_company);
                viewHolder.tv_flight_code = (TextView) convertView.findViewById(R.id.tv_flight_code);
                viewHolder.tv_flight_name = (TextView) convertView.findViewById(R.id.tv_flight_name);
                viewHolder.tv_flight_percentInTime = (TextView) convertView.findViewById(R.id.tv_flight_percentInTime);
                viewHolder.tv_tag_lowest = (TextView) convertView.findViewById(R.id.tv_tag_lowest);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            PlaneFlightItemInfoBean bean = mList.get(position);

            viewHolder.tv_off_time.setText(bean.dptTime);
            viewHolder.tv_off_airport.setText(bean.dptAirport);
            viewHolder.tv_off_terminal.setText(bean.dptTerminal);
            viewHolder.tv_on_time.setText(bean.arrTime);
            viewHolder.tv_on_airport.setText(bean.arrAirport);
            viewHolder.tv_on_terminal.setText(bean.arrTerminal);
            if (bean.stop) {
                viewHolder.stopover_lay.setVisibility(View.VISIBLE);
                viewHolder.tv_stop_city.setText("经停" + bean.stopCityName);
            } else {
                viewHolder.stopover_lay.setVisibility(View.GONE);
            }
            viewHolder.tv_flight_during.setText(bean.flightTimes);
            viewHolder.tv_flight_price.setText(String.valueOf((int) bean.barePrice));
            viewHolder.tv_flight_name.setText(bean.flightTypeFullName);
            viewHolder.tv_flight_code.setText(bean.flightNum);

            if (bean.equals(minBean)) {
                viewHolder.tv_tag_lowest.setVisibility(View.VISIBLE);
            } else {
                viewHolder.tv_tag_lowest.setVisibility(View.GONE);
            }

            return convertView;
        }

        class ViewHolder {
            TextView tv_off_time;
            TextView tv_off_airport;
            TextView tv_off_terminal;
            TextView tv_on_time;
            TextView tv_on_airport;
            TextView tv_on_terminal;
            LinearLayout stopover_lay;
            TextView tv_stop_city;
            TextView tv_flight_during;
            TextView tv_flight_price;
            ImageView iv_flight_icon;
            TextView tv_flight_company;
            TextView tv_flight_code;
            TextView tv_flight_name;
            TextView tv_flight_percentInTime;
            TextView tv_tag_lowest;
        }
    }

    private class PlaneDatePriceAdapter extends BaseAdapter {
        private Context context;
        private List<Date> mList = new ArrayList<>();

        PlaneDatePriceAdapter(Context context, List<Date> list) {
            this.context = context;
            this.mList = list;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (null == convertView) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.plane_date_item, null);
                int width = (int) ((float) (Utils.mScreenWidth - Utils.dp2px(40) - Utils.dp2px(6)) / 7);
//                int height = (int) ((float) width / Utils.dp2px(50) * Utils.dp2px(66));
                Gallery.LayoutParams layoutParams = new Gallery.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT);
                convertView.setLayoutParams(layoutParams);

                viewHolder.tv_week = (TextView) convertView.findViewById(R.id.tv_week);
                viewHolder.tv_day = (TextView) convertView.findViewById(R.id.tv_day);
                viewHolder.tv_price = (TextView) convertView.findViewById(R.id.tv_price);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            Date date = mList.get(position);
            viewHolder.tv_week.setText(DateUtil.dateToWeek(date));
            viewHolder.tv_day.setText(DateUtil.getDay("d", date.getTime()));

            if (DateUtil.getGapCount(date, new Date(mOffTime)) == 0 && mCurrentPrice > 0) {
                viewHolder.tv_price.setVisibility(View.VISIBLE);
                viewHolder.tv_price.setText(String.format(getString(R.string.rmbStr), String.valueOf(mCurrentPrice)));
            } else {
                viewHolder.tv_price.setVisibility(View.INVISIBLE);
            }

            return convertView;
        }

        class ViewHolder {
            TextView tv_week;
            TextView tv_day;
            TextView tv_price;
        }
    }

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        super.onNotifyMessage(request, response);
        if (response != null && response.body != null) {
            if (request.flag == AppConst.PLANE_FLIGHT_LIST) {
                removeProgressDialog();
                swipeRefreshLayout.setRefreshing(false);
                LogUtil.i(TAG, "json = " + response.body.toString());
                Logger.i(response.body.toString());
                List<PlaneFlightItemInfoBean> temp = JSON.parseArray(response.body.toString(), PlaneFlightItemInfoBean.class);
                if (temp.size() > 0) {
                    mFlightList.clear();
                    mFlightList.addAll(temp);
                    Collections.sort(mFlightList, mComparator);
                }
                LogUtil.i(TAG, "flight count = " + mFlightList.size());
                planeItemAdapter.notifyDataSetChanged();
                listview.setSelection(0);
                minBean = Collections.min(mFlightList, new BarePriceComparator());
                mCurrentPrice = (int) minBean.barePrice;
                planeDatePriceAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onNotifyError(ResponseData request) {
        super.onNotifyError(request);
        removeProgressDialog();
        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * 航班价格排序比较器
     */
    private static class BarePriceComparator implements Comparator<PlaneFlightItemInfoBean> {

        @Override
        public int compare(PlaneFlightItemInfoBean o1, PlaneFlightItemInfoBean o2) {
            return Double.valueOf(o1.barePrice).compareTo(o2.barePrice);
        }
    }

    /**
     * 起飞时间排序比较器
     */
    private static class OffTimeComparator implements Comparator<PlaneFlightItemInfoBean> {
        @Override
        public int compare(PlaneFlightItemInfoBean o1, PlaneFlightItemInfoBean o2) {
            return o1.dptTime.compareTo(o2.dptTime);
        }
    }

    /**
     * 飞行时间排序比较器(默认)
     */
    private static class FlightTimesComparator implements Comparator<PlaneFlightItemInfoBean> {
        @Override
        public int compare(PlaneFlightItemInfoBean o1, PlaneFlightItemInfoBean o2) {
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

            if (o1.flightTimes.contains("天")) {
                formatStr2 += "dd天";
            }
            if (o1.flightTimes.contains("小时")) {
                formatStr2 += "HH小时";
            }
            if (o1.flightTimes.contains("分钟")) {
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
}
