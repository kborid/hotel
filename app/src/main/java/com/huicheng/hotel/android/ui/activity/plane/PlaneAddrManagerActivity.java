package com.huicheng.hotel.android.ui.activity.plane;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;

import java.util.ArrayList;

/**
 * @auth kborid
 * @date 2017/11/23 00
 */

public class PlaneAddrManagerActivity extends BaseAppActivity {

    private TextView tv_right;
    private ListView listview;
    private ArrayList<String> list = new ArrayList<>();
    private AddressManagerAdapter addressManagerAdapter;

    @Override
    protected void preOnCreate() {
        super.preOnCreate();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_plane_addrmanager_layout);
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
        addressManagerAdapter = new AddressManagerAdapter(this, list);
        listview.setAdapter(addressManagerAdapter);
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
                Intent intent = new Intent(this, PlaneAddrEditActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    private class AddressManagerAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<String> mList = new ArrayList<>();

        AddressManagerAdapter(Context context, ArrayList<String> list) {
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
            convertView.findViewById(R.id.flag_lay).setVisibility(View.INVISIBLE);
            return convertView;
        }
    }
}
