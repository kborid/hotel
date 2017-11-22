package com.huicheng.hotel.android.ui.activity.plane;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.prj.sdk.util.DateUtil;

import java.util.ArrayList;
import java.util.Date;

/**
 * @auth kborid
 * @date 2017/11/22 0022.
 */

public class PlaneDetailActivity extends BaseActivity {

    private ArrayList<String> list = new ArrayList<>();
    private ListView listview;
    private PlaneDetailItemAdapter adapter;
    private View mListHeaderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_planedetail_layout);
        initViews();
        initParams();
        initListeners();
    }

    @Override
    public void initViews() {
        super.initViews();
        listview = (ListView) findViewById(R.id.listview);
        mListHeaderView = LayoutInflater.from(this).inflate(R.layout.lv_planedetail_header, null);
    }

    @Override
    public void initParams() {
        super.initParams();
        findViewById(R.id.comm_title_rl).setBackgroundColor(getResources().getColor(R.color.white));
        tv_center_title.setText(DateUtil.getDay("M月d日", System.currentTimeMillis()) + DateUtil.dateToWeek2(new Date(System.currentTimeMillis())));
        btn_right.setImageResource(R.drawable.iv_plane_share);
        btn_right.setVisibility(View.VISIBLE);
        for (int i = 0; i < 10; i++) {
            list.add(String.valueOf(i));
        }
        adapter = new PlaneDetailItemAdapter(this, list);
        listview.setAdapter(adapter);
        listview.addHeaderView(mListHeaderView);
    }

    @Override
    public void initListeners() {
        super.initListeners();
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(PlaneDetailActivity.this, PlaneOrderActivity.class);
                startActivity(intent);
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
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class PlaneDetailItemAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<String> mList = new ArrayList<>();

        PlaneDetailItemAdapter(Context context, ArrayList<String> list) {
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
            if (null == convertView) {
                convertView = LayoutInflater.from(context).inflate(R.layout.lv_planedetail_item, null);
            }
            return convertView;
        }
    }
}
