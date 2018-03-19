package com.huicheng.hotel.android.ui.activity;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.content.AppConst;
import com.huicheng.hotel.android.content.NetURL;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.OrdersSpendInfoBean;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.huicheng.hotel.android.ui.custom.CustomCirclePieChart;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.util.LogUtil;

import java.util.Calendar;

/**
 * @author kborid
 * @date 2016/12/8 0008
 */
public class UcCostDetailActivity extends BaseAppActivity {

    private LinearLayout root_lay;
    private LinearLayout chart_lay;
    private CustomCirclePieChart pieChart;
    private Spinner spinner;
    private String[] year = new String[6];
    private String startYear, endYear;
    private int selectorIndex = 0;
    private boolean isFirstLoad = false;
    private OrdersSpendInfoBean ordersSpendInfoBean = null;

    private TextView tv_spend_year, tv_save_all;
    private TextView tv_hotel_spend, tv_hotel_save, tv_plane_spend, tv_plane_save;
    private TextView tv_year_count, tv_month_count, tv_week_count;

    @Override
    protected void requestData() {
        super.requestData();
        if (null == ordersSpendInfoBean) {
            requestSpendRecorded();
        } else {
            updateOrdersSpendInfo();
        }
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_uc_costdetail);
    }

    @Override
    public void initViews() {
        super.initViews();
        root_lay = (LinearLayout) findViewById(R.id.root_lay);
        root_lay.setVisibility(View.GONE);
        root_lay.setLayoutAnimation(getAnimationController());
        chart_lay = (LinearLayout) findViewById(R.id.chart_lay);
        spinner = (Spinner) findViewById(R.id.spinner);
        pieChart = (CustomCirclePieChart) findViewById(R.id.piechart);
        tv_spend_year = (TextView) findViewById(R.id.tv_spend_year);
        tv_save_all = (TextView) findViewById(R.id.tv_save_all);

        tv_hotel_spend = (TextView) findViewById(R.id.tv_hotel_spend);
        tv_hotel_save = (TextView) findViewById(R.id.tv_hotel_save);
        tv_plane_spend = (TextView) findViewById(R.id.tv_plane_spend);
        tv_plane_save = (TextView) findViewById(R.id.tv_plane_save);

        tv_year_count = (TextView) findViewById(R.id.tv_year_count);
        tv_month_count = (TextView) findViewById(R.id.tv_month_count);
        tv_week_count = (TextView) findViewById(R.id.tv_week_count);
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            if (bundle.get("ordersSpendInfoBean") != null) {
                ordersSpendInfoBean = (OrdersSpendInfoBean) bundle.get("ordersSpendInfoBean");
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText("消费详情");
        isFirstLoad = true;
        Calendar c = Calendar.getInstance();
        int currentYear = c.get(Calendar.YEAR);
        for (int i = 0; i < year.length; i++) {
            if (i == 0) {
                year[i] = "全部年度";
            } else {
                if (i != year.length - 1) {
                    year[i] = currentYear + 2 - i + "年";
                } else {
                    year[i] = currentYear + 3 - i + "年以前";
                }
            }
        }
        ArrayAdapter spinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_layout_item, year);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dialog_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setSelection(selectorIndex);
    }

    @Override
    public void initListeners() {
        super.initListeners();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LogUtil.i(TAG, "spinner date 's OnItemSelectedListener request....");
                if (isFirstLoad) {
                    isFirstLoad = false;
                    return;
                }
                String selectedYear = year[position];
                if (selectedYear.contains("全")) {
                    startYear = "";
                    endYear = "";
                } else if (selectedYear.contains("以前")) {
                    startYear = "";
                    endYear = selectedYear.split("年")[0];
                } else {
                    startYear = selectedYear.split("年")[0];
                    endYear = String.valueOf(Integer.parseInt(startYear) + 1);
                }
                LogUtil.i(TAG, "start = " + selectedYear + ", end = " + endYear);
                requestSpendRecorded();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void requestSpendRecorded() {
        LogUtil.i(TAG, "requestSpendRecorded()");
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("startYear", startYear);
        b.addBody("endYear", endYear);

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.ORDER_SPEND;
        d.flag = AppConst.ORDER_SPEND;

        if (!isProgressShowing()) {
            showProgressDialog(this);
        }

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void updateOrdersSpendInfo() {
        if (null != ordersSpendInfoBean) {
            tv_spend_year.setText(String.valueOf((int) ordersSpendInfoBean.spend));
            tv_save_all.setText(String.valueOf((int) ordersSpendInfoBean.totalsave));

            tv_hotel_spend.setText(String.format(getString(R.string.rmbStr), String.valueOf((int) ordersSpendInfoBean.spendhotel)));
            tv_plane_spend.setText(String.format(getString(R.string.rmbStr), String.valueOf((int) ordersSpendInfoBean.spendplain)));

            if (0 == ordersSpendInfoBean.spendhotel
                    && 0 == ordersSpendInfoBean.spendplain
                    && 0 == ordersSpendInfoBean.totalsave) {
                chart_lay.setVisibility(View.GONE);
            } else {
                chart_lay.setVisibility(View.VISIBLE);
                pieChart.setConst(ordersSpendInfoBean.spendhotel, ordersSpendInfoBean.spendplain, 0f, 0f, ordersSpendInfoBean.totalsave);
            }
            tv_year_count.setText(String.valueOf((int) ordersSpendInfoBean.thisyear));
            tv_month_count.setText(String.valueOf((int) ordersSpendInfoBean.thismonth));
            tv_week_count.setText(String.valueOf((int) ordersSpendInfoBean.thisweek));
            root_lay.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.ORDER_SPEND) {
                removeProgressDialog();
                isFirstLoad = false;
                LogUtil.i(TAG, "json = " + response.body.toString());
                ordersSpendInfoBean = JSON.parseObject(response.body.toString(), OrdersSpendInfoBean.class);
                updateOrdersSpendInfo();
            }
        }
    }
}
