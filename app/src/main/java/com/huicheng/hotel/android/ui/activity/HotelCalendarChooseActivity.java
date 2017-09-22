package com.huicheng.hotel.android.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.tools.CityParseUtils;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.calendar.CalendarSelectedListener;
import com.huicheng.hotel.android.ui.custom.calendar.CalendarUtils;
import com.huicheng.hotel.android.ui.custom.calendar.CustomCalendarView;
import com.huicheng.hotel.android.ui.custom.calendar.SimpleMonthAdapter;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.huicheng.hotel.android.ui.dialog.CustomToast;

/**
 * @author kborid
 * @date 2016/11/17 0017
 */
public class HotelCalendarChooseActivity extends BaseActivity implements CalendarSelectedListener {

    private LinearLayout week_lay;
    private CustomCalendarView calendar_lay;
    private TextView tv_begin;
    private TextView tv_end;
    private Button btn_next;

    private boolean isForbidTitleClick = false;
    private boolean isReBooking = false;
    private boolean isFansBooking = false;
    private boolean isCouponBooking = false;

    private long beginTime, endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_datechoose_layout);

        initViews();
        initParams();
        initListeners();
    }

    @Override
    public void initViews() {
        super.initViews();
        week_lay = (LinearLayout) findViewById(R.id.week_lay);
        calendar_lay = (CustomCalendarView) findViewById(R.id.calendar_lay);
        btn_next = (Button) findViewById(R.id.btn_next);
        tv_begin = (TextView) findViewById(R.id.tv_begin);
        tv_end = (TextView) findViewById(R.id.tv_end);
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            isForbidTitleClick = bundle.getBoolean("isForbidTitleClick");
            isReBooking = bundle.getBoolean("isReBooking");
            isFansBooking = bundle.getBoolean("isFansBooking");
            isCouponBooking = bundle.getBoolean("isCouponBooking");
            beginTime = bundle.getLong("beginTime");
            endTime = bundle.getLong("endTime");
        }
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText(HotelOrderManager.getInstance().getCityStr());
        tv_center_title.getPaint().setFakeBoldText(true);
        initWeekLayout();
        if (isForbidTitleClick) {
            tv_center_title.setEnabled(false);
        } else {
            tv_center_title.setEnabled(true);
        }
        if (0 != beginTime && 0 != endTime) {
            tv_begin.setText(DateUtil.getDay("M月d日", beginTime));
            tv_end.setText(DateUtil.getDay("M月d日", endTime));
            btn_next.setEnabled(true);
        }
    }

    @Override
    public void initListeners() {
        super.initListeners();
        calendar_lay.setController(this);
        calendar_lay.setBeginAndEndDays(beginTime, endTime);
        btn_next.setOnClickListener(this);
        tv_center_title.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btn_next: {
                Intent intent = null;
                if (isReBooking) {
                    HotelOrderManager.getInstance().setBeginTime(beginTime);
                    HotelOrderManager.getInstance().setEndTime(endTime);
                    HotelOrderManager.getInstance().setDateStr(DateUtil.getDay("M.d", beginTime) + " - " + DateUtil.getDay("M.d", endTime));
                    HotelOrderManager.getInstance().setHotelType(HotelCommDef.TYPE_ALL);
                    intent = new Intent(this, RoomListActivity.class);
                    intent.putExtra("key", HotelCommDef.ALLDAY);
                    intent.putExtra("hotelId", Integer.parseInt(HotelOrderManager.getInstance().getOrderPayDetailInfoBean().hotelID));
                } else if (isFansBooking) {
                    HotelOrderManager.getInstance().setBeginTime(beginTime);
                    HotelOrderManager.getInstance().setEndTime(endTime);
                    HotelOrderManager.getInstance().setDateStr(DateUtil.getDay("M.d", beginTime) + " - " + DateUtil.getDay("M.d", endTime));
                    HotelOrderManager.getInstance().setHotelType(HotelCommDef.TYPE_ALL);
                    intent = new Intent(this, RoomListActivity.class);
                    intent.putExtra("key", HotelCommDef.ALLDAY);
                    intent.putExtra("hotelId", HotelOrderManager.getInstance().getFansHotelInfoBean().hotelId);
                } else if (isCouponBooking) {
                    HotelOrderManager.getInstance().setBeginTime(beginTime);
                    HotelOrderManager.getInstance().setEndTime(endTime);
                    HotelOrderManager.getInstance().setDateStr(DateUtil.getDay("M.d", beginTime) + " - " + DateUtil.getDay("M.d", endTime));
                    intent = new Intent(this, RoomOrderConfirmActivity.class);
                    intent.putExtra("hotelId", HotelOrderManager.getInstance().getCouponInfoBean().hotelId);
                } else {
                    Intent dataIntent = new Intent();
                    dataIntent.putExtra("beginTime", beginTime);
                    dataIntent.putExtra("endTime", endTime);
                    setResult(RESULT_OK, dataIntent);
                    this.finish();
                }
                if (null != intent) {
                    startActivity(intent);
                }
            }
            break;
            case R.id.tv_center_title: {
                Intent intent = new Intent(this, LocationChooseActivity.class);
                startActivityForResult(intent, 0x01);
            }
            break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK != resultCode) {
            return;
        }
        if (requestCode == 0x01) {
            String tempProvince = SharedPreferenceUtil.getInstance().getString(AppConst.PROVINCE, "", false);
            String tempCity = SharedPreferenceUtil.getInstance().getString(AppConst.CITY, "", false);
            String cityStr = CityParseUtils.getProvinceCityString(tempProvince, tempCity, "-");
            HotelOrderManager.getInstance().setCityStr(cityStr);
            tv_center_title.setText(cityStr);
        }
    }

    private void initWeekLayout() {
        week_lay.removeAllViews();
        for (int i = 0; i < 7; i++) {
            TextView tv_week = new TextView(this);
            tv_week.setText(CalendarUtils.getWeekStringByNum(i));
            tv_week.setGravity(Gravity.CENTER);
            tv_week.setTextColor(getResources().getColor(R.color.lableColor));
            tv_week.setTextSize(12);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            lp.weight = 1;
            week_lay.addView(tv_week, lp);
        }
    }

    @Override
    public void onDayOfMonthSelected(int year, int month, int day) {
        LogUtil.i(TAG, "Day Selected = " + day + " / " + month + " / " + year);
        tv_begin.setText(month + 1 + "月" + day + "日");
        tv_end.setText("");
        btn_next.setEnabled(false);
    }

    @Override
    public void onDateRangeSelected(SimpleMonthAdapter.SelectedDays<SimpleMonthAdapter.CalendarDay> selectedDays) {
        LogUtil.i(TAG, "Selected first days = " + selectedDays.getFirst());
        LogUtil.i(TAG, "Selected last days = " + selectedDays.getLast());
        tv_begin.setText(selectedDays.getFirst().getMonth() + 1 + "月" + selectedDays.getFirst().getDay() + "日");
        if (selectedDays.getLast() != null) {
            if (isCouponBooking) {
                if (DateUtil.getGapCount(selectedDays.getFirst().getDate(), selectedDays.getLast().getDate()) > 1) {
                    CustomToast.show("使用优惠券只能住一晚，请重新选择", CustomToast.LENGTH_SHORT);
                    btn_next.setEnabled(false);
                    return;
                }
            }
            tv_end.setText(selectedDays.getLast().getMonth() + 1 + "月" + selectedDays.getLast().getDay() + "日");
            btn_next.setEnabled(true);
        } else {
            tv_end.setText("");
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