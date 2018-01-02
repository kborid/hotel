package com.huicheng.hotel.android.ui.activity.plane;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.PlaneCommDef;
import com.huicheng.hotel.android.common.PlaneOrderManager;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneFlightInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneTicketInfoBean;
import com.huicheng.hotel.android.tools.CityParseUtils;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.plane.CustomInfoLayoutForPlane;
import com.prj.sdk.util.DateUtil;

import java.util.Date;

/**
 * @auth kborid
 * @date 2017/11/22 0022.
 */

public class PlaneNewOrderActivity extends BaseActivity {

    //预订航班信息
    private LinearLayout flight_layout;
    private LinearLayout flight_flag_layout;
    private CustomInfoLayoutForPlane custom_info_layout_plane;

    private Switch btn_invoice_switch;
    private LinearLayout invoice_lay;
    private TextView tv_invoice_tips;
    private RadioGroup rg_invoice;
    private TextView tv_personal_invoice;
    private LinearLayout company_invoice;

    private TextView tv_chooser;
    private TextView tv_submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_plane_neworder_layout);
        initViews();
        initParams();
        initListeners();
    }

    @Override
    public void initViews() {
        super.initViews();
        flight_layout = (LinearLayout) findViewById(R.id.flight_layout);
        flight_flag_layout = (LinearLayout) findViewById(R.id.flight_flag_layout);

        custom_info_layout_plane = (CustomInfoLayoutForPlane) findViewById(R.id.custom_info_layout_plane);

        btn_invoice_switch = (Switch) findViewById(R.id.btn_invoice_switch);
        invoice_lay = (LinearLayout) findViewById(R.id.invoice_lay);
        tv_invoice_tips = (TextView) findViewById(R.id.tv_invoice_tips);
        rg_invoice = (RadioGroup) findViewById(R.id.rg_invoice);
        tv_personal_invoice = (TextView) findViewById(R.id.tv_personal_invoice);
        company_invoice = (LinearLayout) findViewById(R.id.company_invoice);

        tv_chooser = (TextView) findViewById(R.id.tv_chooser);
        tv_submit = (TextView) findViewById(R.id.tv_submit);
    }

    @Override
    public void initParams() {
        super.initParams();
        findViewById(R.id.comm_title_rl).setBackgroundColor(getResources().getColor(R.color.white));
        tv_center_title.setText(
                CityParseUtils.getPlaneOffOnCity(
                        PlaneOrderManager.instance.getFlightOffAirportInfo().cityName,
                        PlaneOrderManager.instance.getFlightOnAirportInfo().cityName,
                        "→"
                )
        );

        // 刷新去程航班信息显示
        flight_flag_layout.removeAllViews();
        View goPlaneOrder = LayoutInflater.from(this).inflate(R.layout.layout_plane_order_item, null);
        flight_flag_layout.addView(goPlaneOrder);
        TextView go_tv_flag = (TextView) goPlaneOrder.findViewById(R.id.tv_flag);
        go_tv_flag.setVisibility(View.GONE);
        TextView go_tv_offdate = (TextView) goPlaneOrder.findViewById(R.id.tv_offdate);
        TextView go_tv_cabin = (TextView) goPlaneOrder.findViewById(R.id.tv_cabin);
        TextView go_tv_airport = (TextView) goPlaneOrder.findViewById(R.id.tv_airport);
        TextView go_tv_price = (TextView) goPlaneOrder.findViewById(R.id.tv_price);
        long goOffDate = PlaneOrderManager.instance.getGoFlightOffDate();
        go_tv_offdate.setText(DateUtil.getDay("MM-dd  ", goOffDate));
        go_tv_offdate.append(DateUtil.dateToWeek2(new Date(goOffDate)));
        PlaneFlightInfoBean goFlightInfo = PlaneOrderManager.instance.getGoFlightInfo();
        go_tv_airport.setText(String.format("%1$s%2$s - %3$s%4$s", goFlightInfo.dptAirport, goFlightInfo.dptTerminal, goFlightInfo.arrAirport, goFlightInfo.arrTerminal));
        go_tv_offdate.append("  " + goFlightInfo.dptTime);
        PlaneTicketInfoBean.VendorInfo goVendorInfo = PlaneOrderManager.instance.getGoVendorInfo();
        go_tv_cabin.setText(PlaneCommDef.getCabinString(goVendorInfo.cabinType));
        go_tv_price.setText(String.valueOf((int) goVendorInfo.barePrice));

        // 如果往返航班，则增加返程航班信息显示
        if (PlaneOrderManager.instance.isFlightGoBack()) {
            View backPlaneOrder = LayoutInflater.from(this).inflate(R.layout.layout_plane_order_item, null);
            flight_flag_layout.addView(backPlaneOrder);
            TextView back_tv_flag = (TextView) backPlaneOrder.findViewById(R.id.tv_flag);
            TextView back_tv_offdate = (TextView) backPlaneOrder.findViewById(R.id.tv_offdate);
            TextView back_tv_cabin = (TextView) backPlaneOrder.findViewById(R.id.tv_cabin);
            TextView back_tv_airport = (TextView) backPlaneOrder.findViewById(R.id.tv_airport);
            TextView back_tv_price = (TextView) backPlaneOrder.findViewById(R.id.tv_price);
            go_tv_flag.setVisibility(View.VISIBLE);
            go_tv_flag.setText("去程：");
            back_tv_flag.setVisibility(View.VISIBLE);
            back_tv_flag.setText("返程：");
            long backOffDate = PlaneOrderManager.instance.getBackFlightOffDate();
            back_tv_offdate.setText(DateUtil.getDay("MM-dd  ", backOffDate));
            back_tv_offdate.append(DateUtil.dateToWeek2(new Date(backOffDate)));
            PlaneFlightInfoBean backFlightInfo = PlaneOrderManager.instance.getBackFlightInfo();
            back_tv_airport.setText(String.format("%1$s%2$s - %3$s%4$s", backFlightInfo.dptAirport, backFlightInfo.dptTerminal, backFlightInfo.arrAirport, backFlightInfo.arrTerminal));
            back_tv_offdate.append("  " + backFlightInfo.dptTime);
            PlaneTicketInfoBean.VendorInfo backVendorInfo = PlaneOrderManager.instance.getBackVendorInfo();
            back_tv_cabin.setText(PlaneCommDef.getCabinString(backVendorInfo.cabinType));
            back_tv_price.setText(String.valueOf((int) backVendorInfo.barePrice));
        }

        //initialize
        btn_invoice_switch.setChecked(false);
        tv_invoice_tips.setVisibility(View.VISIBLE);
        invoice_lay.setVisibility(View.GONE);
    }

    @Override
    public void initListeners() {
        super.initListeners();
        flight_flag_layout.setOnClickListener(this);
        rg_invoice.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {
                    case R.id.rb_personal:
                        tv_personal_invoice.setVisibility(View.VISIBLE);
                        company_invoice.setVisibility(View.GONE);
                        break;
                    case R.id.rb_company:
                        tv_personal_invoice.setVisibility(View.GONE);
                        company_invoice.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });
        btn_invoice_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tv_invoice_tips.setVisibility(View.GONE);
                    invoice_lay.setVisibility(View.VISIBLE);
                    rg_invoice.check(R.id.rb_personal);
                } else {
                    tv_invoice_tips.setVisibility(View.VISIBLE);
                    invoice_lay.setVisibility(View.GONE);
                }
            }
        });
        tv_chooser.setOnClickListener(this);
        tv_submit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.flight_flag_layout: {
                Intent intent = new Intent(this, PlaneOrderDetailActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.tv_chooser: {
                Intent intent = new Intent(this, PlaneAddrChooserActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.tv_submit: {
                Intent intent = new Intent(this, PlaneOrderPayActivity.class);
                startActivity(intent);
                break;
            }
        }
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
}
