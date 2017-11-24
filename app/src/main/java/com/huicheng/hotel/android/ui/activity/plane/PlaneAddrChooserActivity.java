package com.huicheng.hotel.android.ui.activity.plane;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.ui.base.BaseActivity;

import java.util.ArrayList;

/**
 * @auth kborid
 * @date 2017/11/23 0023.
 */

public class PlaneAddrChooserActivity extends BaseActivity {

    private TextView tv_right;
    private ListView listview;
    private ArrayList<String> list = new ArrayList<>();
    private AddressChooserAdapter addressChooserAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_plane_addrchooser_layout);
        initViews();
        initParams();
        initListeners();
    }

    @Override
    public void initViews() {
        super.initViews();
        tv_right = (TextView) findViewById(R.id.tv_right);
        listview = (ListView) findViewById(R.id.listview);
    }

    @Override
    public void initParams() {
        super.initParams();
        for (int i = 0; i < 10; i++) {
            list.add(String.valueOf(i));
        }
        addressChooserAdapter = new AddressChooserAdapter(this, list);
        listview.setAdapter(addressChooserAdapter);
    }

    @Override
    public void initListeners() {
        super.initListeners();
        tv_right.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.tv_right:
                Intent intent = new Intent(this, PlaneAddrManagerActivity.class);
                startActivity(intent);
                break;
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

    private class AddressChooserAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<String> mList = new ArrayList<>();

        AddressChooserAdapter(Context context, ArrayList<String> list) {
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
                convertView = LayoutInflater.from(context).inflate(R.layout.lv_plane_address_item, null);
            }
            convertView.findViewById(R.id.action_lay).setVisibility(View.GONE);
            return convertView;
        }
    }
}
