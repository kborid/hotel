package com.huicheng.hotel.android.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.prj.sdk.util.DateUtil;

import java.util.ArrayList;

/**
 * @auth kborid
 * @date 2017/12/8 0008.
 */

public class MyABCBalanceActivity extends BaseActivity {

    private ListView listview;
    private BalanceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_abcbalance_layout);
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
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            list.add(String.valueOf(i));
        }
        adapter = new BalanceAdapter(this, list);
        listview.setAdapter(adapter);
        View header = LayoutInflater.from(this).inflate(R.layout.lv_balance_header, null);
        listview.addHeaderView(header);
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

    private class BalanceAdapter extends BaseAdapter {

        private Context context;
        private ArrayList<String> list = new ArrayList<>();

        BalanceAdapter(Context context, ArrayList<String> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
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
                convertView = LayoutInflater.from(context).inflate(R.layout.lv_balance_item, null);
                viewHolder.tv_balance_title = (TextView) convertView.findViewById(R.id.tv_balance_title);
                viewHolder.tv_balance_price = (TextView) convertView.findViewById(R.id.tv_balance_price);
                viewHolder.tv_balance_date = (TextView) convertView.findViewById(R.id.tv_balance_date);
                viewHolder.tv_balance_user = (TextView) convertView.findViewById(R.id.tv_balance_user);
                viewHolder.tv_balance_user.append(list.get(position));
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.tv_balance_price.setText("+" + list.get(position));
            viewHolder.tv_balance_date.setText(DateUtil.getDay("yyyy/MM/dd", System.currentTimeMillis() + Integer.valueOf(list.get(position)) * 1000));

            return convertView;
        }

        class ViewHolder {
            TextView tv_balance_title;
            TextView tv_balance_price;
            TextView tv_balance_date;
            TextView tv_balance_user;
        }
    }
}
