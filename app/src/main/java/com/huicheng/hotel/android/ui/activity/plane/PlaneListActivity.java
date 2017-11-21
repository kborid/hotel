package com.huicheng.hotel.android.ui.activity.plane;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.ui.base.BaseActivity;

import java.util.ArrayList;

/**
 * @auth kborid
 * @date 2017/11/21 0021.
 */

public class PlaneListActivity extends BaseActivity {

    private ListView listview;
    private ArrayList<String> list = new ArrayList<>();
    private PlaneItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_planelist_layout);
        initViews();
        initParams();
        initListeners();
    }

    @Override
    public void initViews() {
        super.initViews();
        listview = (ListView) findViewById(R.id.listview);
    }

    @Override
    public void initParams() {
        super.initParams();
        for (int i = 0; i < 10; i++) {
            list.add(String.valueOf(i));
        }
        adapter = new PlaneItemAdapter(this, list);
        listview.setAdapter(adapter);
    }

    @Override
    public void initListeners() {
        super.initListeners();
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

    private class PlaneItemAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<String> mList = new ArrayList<>();

        PlaneItemAdapter(Context context, ArrayList<String> list) {
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
                convertView = LayoutInflater.from(context).inflate(R.layout.lv_planeitem, null);
            }
            return convertView;
        }
    }
}
