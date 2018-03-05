package com.huicheng.hotel.android.ui.activity.plane;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.PlaneOrderManager;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.huicheng.hotel.android.ui.custom.calendar.CalendarSelectedListener;
import com.huicheng.hotel.android.ui.custom.calendar.CalendarUtils;
import com.huicheng.hotel.android.ui.custom.calendar.CustomCalendarRecyclerView;
import com.huicheng.hotel.android.ui.custom.calendar.SimpleMonthAdapter;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;

import java.util.Calendar;

/**
 * @author kborid
 * @date 2016/11/17 0017
 */
public class PlaneCalendarChooseActivity extends BaseAppActivity implements CalendarSelectedListener {
    private static final String GO_TIP1 = "去程：%1$s";
    private static final String GO_TIP2 = "出发日期：%1$s";
    private static final String BACK_TIP = "返程：%1$s";
    private static String GO_TIP = GO_TIP1;

    private LinearLayout week_lay;
    private CustomCalendarRecyclerView calendar_lay;
    private TextView tv_go_date;
    private TextView tv_back_date;
    private ImageButton btn_next;

    private boolean isSingleFlight = true;

    private long beginTime, endTime;

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_plane_calendarchooser);
    }

    @Override
    public void initViews() {
        super.initViews();
        week_lay = (LinearLayout) findViewById(R.id.week_lay);
        calendar_lay = (CustomCalendarRecyclerView) findViewById(R.id.calendar_lay);
        btn_next = (ImageButton) findViewById(R.id.btn_next);
        btn_next.setEnabled(false);
        tv_go_date = (TextView) findViewById(R.id.tv_go_date);
        tv_back_date = (TextView) findViewById(R.id.tv_back_date);
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            beginTime = bundle.getLong("beginTime");
            endTime = bundle.getLong("endTime");
        }
    }

    @Override
    public void initParams() {
        super.initParams();
        isSingleFlight = !PlaneOrderManager.instance.isFlightGoBack();
        tv_center_title.setText(isSingleFlight ? "出发日期" : "往返日期");
        initWeekLayout();
        GO_TIP = isSingleFlight ? GO_TIP2 : GO_TIP1;
        tv_back_date.setVisibility(isSingleFlight ? View.GONE : View.VISIBLE);
        tv_go_date.setText(String.format(GO_TIP, "— —"));
        tv_back_date.setText(String.format(BACK_TIP, "— —"));
    }

    @Override
    public void initListeners() {
        super.initListeners();
        calendar_lay.setController(this);
        calendar_lay.setBeginAndEndDays(beginTime, endTime);
        calendar_lay.setSelectedType(isSingleFlight ? SimpleMonthAdapter.SELECTED_SINGLE : SimpleMonthAdapter.SELECTED_DOUBLE);
        btn_next.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btn_next: {
                Intent dataIntent = new Intent();
                dataIntent.putExtra("beginTime", beginTime);
                dataIntent.putExtra("endTime", endTime);
                setResult(RESULT_OK, dataIntent);
                this.finish();
            }
            break;
        }
    }

    private void initWeekLayout() {
        week_lay.removeAllViews();
        for (int i = 0; i < 7; i++) {
            TextView tv_week = new TextView(this);
            tv_week.setText(CalendarUtils.getWeekStringByNum(i));
            tv_week.setGravity(Gravity.CENTER);
            tv_week.setTextColor(Color.parseColor("#3c3c3c"));
            tv_week.setTextSize(14);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            lp.weight = 1;
            week_lay.addView(tv_week, lp);
        }
    }

    @Override
    public void onDayOfMonthSelected(int year, int month, int day) {
        LogUtil.i(TAG, "onDayOfMonthSelected() = " + day + "-" + month + "-" + year);
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DATE, day);
        beginTime = c.getTimeInMillis();
        tv_go_date.setText(String.format(GO_TIP, DateUtil.getDay("MM-dd", c.getTimeInMillis()) + " " + DateUtil.dateToWeek2(c.getTime())));
        tv_back_date.setText(String.format(BACK_TIP, "— —"));
        btn_next.setEnabled(isSingleFlight);
    }

    @Override
    public void onDateRangeSelected(SimpleMonthAdapter.SelectedDays<SimpleMonthAdapter.CalendarDay> selectedDays) {
        LogUtil.i(TAG, "onDateRangeSelected() first days = " + selectedDays.getFirst());
        LogUtil.i(TAG, "onDateRangeSelected() las-t days = " + selectedDays.getLast());
        tv_go_date.setText(String.format(GO_TIP, DateUtil.getDay("MM-dd", selectedDays.getFirst().getDate().getTime()) + " " + DateUtil.dateToWeek2(selectedDays.getFirst().getDate())));
        if (selectedDays.getLast() != null) {
            tv_back_date.setText(String.format(BACK_TIP, DateUtil.getDay("MM-dd", selectedDays.getLast().getDate().getTime()) + " " + DateUtil.dateToWeek2(selectedDays.getLast().getDate())));
            btn_next.setEnabled(true);
        } else {
            tv_back_date.setText(String.format(BACK_TIP, "— —"));
            btn_next.setEnabled(false);
        }
        if (selectedDays.getFirst() != null) {
            beginTime = selectedDays.getFirst().getDate().getTime() / 1000 * 1000;
        }
        if (selectedDays.getLast() != null) {
            endTime = selectedDays.getLast().getDate().getTime() / 1000 * 1000;
        }
    }
}