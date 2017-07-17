package com.huicheng.hotel.android.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.CustomCirclePieChart;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author kborid
 * @date 2016/12/8 0008
 */
public class ConsumptionDetailActivity extends BaseActivity {

    private final String TAG = getClass().getSimpleName();
    private CustomCirclePieChart piechart;
    private Spinner spinner;
    private List<String> list = new ArrayList<>();
    private ArrayAdapter spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_consumption_detail_layout);

        initViews();
        initParams();
        initListeners();
    }

    @Override
    public void initViews() {
        super.initViews();
        spinner = (Spinner) findViewById(R.id.spinner);
        piechart = (CustomCirclePieChart) findViewById(R.id.piechart);
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText("消费详情");
        spinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_layout_item, list);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dialog_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setDropDownWidth(Utils.dip2px(75));
        piechart.setConst(5227f, 4300f, 0f, 0f, 657f);
    }

    @Override
    public void initListeners() {
        super.initListeners();
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LogUtil.i(TAG, "spinner selected position = " + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                LogUtil.i(TAG, "onNothingSelected");
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (list != null && list.size() > 0) {
            list.clear();
        }
        list.addAll(Arrays.asList(getResources().getStringArray(R.array.order_status)));
        spinnerAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
