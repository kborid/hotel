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
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.calendar.CalendarSelectedListener;
import com.huicheng.hotel.android.ui.custom.calendar.CalendarUtils;
import com.huicheng.hotel.android.ui.custom.calendar.CustomCalendarView;
import com.huicheng.hotel.android.ui.custom.calendar.SimpleMonthAdapter;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.widget.CustomToast;

/**
 * @author kborid
 * @date 2016/11/17 0017
 */
public class HotelCalendarChooseActivity extends BaseActivity implements CalendarSelectedListener {

    private static final String TAG = "HotelCalendarChooseActivity";

    private LinearLayout week_lay;
    private CustomCalendarView calendar_lay;
    private TextView tv_begin;
    private TextView tv_end;

    private Button btn_next;

    private int jumpIndex = 0;
    private boolean rebooking = false;
    private String hotelId = null;
    private String keyword = null;
    private int mPriceIndex = 0;

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
            jumpIndex = bundle.getInt("index");
            rebooking = bundle.getBoolean("rebooking");
            if (bundle.getString("hotelId") != null) {
                hotelId = bundle.getString("hotelId");
            }
            if (bundle.getString("keyword") != null) {
                keyword = bundle.getString("keyword");
            }
            mPriceIndex = bundle.getInt("priceIndex");
        }
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText(HotelOrderManager.getInstance().getCityStr());
        tv_center_title.getPaint().setFakeBoldText(true);
        initWeekLayout();
    }

    @Override
    public void initListeners() {
        super.initListeners();
        calendar_lay.setController(this);
        btn_next.setOnClickListener(this);
        tv_center_title.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btn_next: {
                Intent intent = null;
                if (rebooking) {
                    HotelOrderManager.getInstance().setHotelType(HotelCommDef.TYPE_ALL);
                    intent = new Intent(this, RoomListActivity.class);
                    intent.putExtra("key", HotelCommDef.ALLDAY);
                    intent.putExtra("hotelId", Integer.parseInt(hotelId));
                } else if (HotelOrderManager.getInstance().isVipHotel() && HotelOrderManager.getInstance().getVipHotelId() != -1) {
                    HotelOrderManager.getInstance().setHotelType(HotelCommDef.TYPE_ALL);
                    intent = new Intent(this, RoomListActivity.class);
                    intent.putExtra("key", HotelCommDef.ALLDAY);
                    intent.putExtra("hotelId", HotelOrderManager.getInstance().getVipHotelId());
                } else if (HotelOrderManager.getInstance().isUseCoupon() && HotelOrderManager.getInstance().getCouponId() != -1) {
                    intent = new Intent(this, RoomOrderConfirmActivity.class);
                    intent.putExtra("hotelId", Integer.parseInt(hotelId));
                } else {
                    intent = new Intent(this, HotelListActivity.class);
                }
                SimpleMonthAdapter.CalendarDay begin = new SimpleMonthAdapter.CalendarDay(HotelOrderManager.getInstance().getBeginTime());
                SimpleMonthAdapter.CalendarDay end = new SimpleMonthAdapter.CalendarDay(HotelOrderManager.getInstance().getEndTime());
                HotelOrderManager.getInstance().setDateStr((begin.getMonth() + 1) + "." + begin.getDay() /*+ DateUtil.dateToWeek2(begin.getDate())*/ + " - "/* + (end.getMonth() + 1) + "."*/ + end.getDay()/* + DateUtil.dateToWeek2(end.getDate())*/);
                intent.putExtra("index", jumpIndex);
                intent.putExtra("keyword", keyword);
                intent.putExtra("priceIndex", mPriceIndex);
                startActivity(intent);
            }
            break;
            case R.id.tv_center_title: {
                Intent intent = new Intent(this, LocationActivity2.class);
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
            if (StringUtil.notEmpty(tempProvince) && tempProvince.equals(tempCity)) {
                HotelOrderManager.getInstance().setCityStr(tempProvince);
            } else {
                HotelOrderManager.getInstance().setCityStr(tempCity + "-" + tempProvince);
            }
            tv_center_title.setText(HotelOrderManager.getInstance().getCityStr());
        }
    }

    private void initWeekLayout() {
        week_lay.removeAllViews();
        for (int i = 0; i < 7; i++) {
            TextView tv_week = new TextView(this);
            tv_week.setText(CalendarUtils.getWeekStringByNum(i));
            tv_week.setGravity(Gravity.CENTER);
            tv_week.setTextColor(getResources().getColor(R.color.bottomBtnTextDisable));
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
            if (HotelOrderManager.getInstance().isUseCoupon() && HotelOrderManager.getInstance().getCouponId() != -1) {
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
            HotelOrderManager.getInstance().setBeginTime(selectedDays.getFirst().getDate().getTime() / 1000 * 1000);
        }
        if (selectedDays.getLast() != null) {
            HotelOrderManager.getInstance().setEndTime(selectedDays.getLast().getDate().getTime() / 1000 * 1000);
        }
    }
}