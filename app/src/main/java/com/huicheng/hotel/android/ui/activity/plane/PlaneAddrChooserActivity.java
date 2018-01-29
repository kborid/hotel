package com.huicheng.hotel.android.ui.activity.plane;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;

import java.util.ArrayList;

/**
 * @auth kborid
 * @date 2017/11/23 0023.
 */

public class PlaneAddrChooserActivity extends BaseAppActivity {

    private TextView tv_right;
    private ListView listview;
    private ArrayList<String> list = new ArrayList<>();
    private AddressChooserAdapter addressChooserAdapter;

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_plane_addrchooser_layout);
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
        findViewById(R.id.comm_title_rl).setBackgroundColor(getResources().getColor(R.color.white));
        tv_center_title.setText("选择地址");
        setRightButtonResource("管理", getResources().getColor(R.color.plane_mainColor));
        for (int i = 0; i < 10; i++) {
            list.add(String.valueOf(i));
        }
        addressChooserAdapter = new AddressChooserAdapter(this, list);
        listview.setAdapter(addressChooserAdapter);
    }

    @Override
    public void initListeners() {
        super.initListeners();
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

    private class AddressChooserAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<String> mList = new ArrayList<>();
        private int mSelectedIndex;

        AddressChooserAdapter(Context context, ArrayList<String> list) {
            this.context = context;
            this.mList = list;
            mSelectedIndex = -1;
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            if (null == convertView) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.lv_plane_address_item, null);
                viewHolder.iv_flag = (ImageView) convertView.findViewById(R.id.iv_flag);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            convertView.findViewById(R.id.action_lay).setVisibility(View.GONE);

            if (position == mSelectedIndex) {
                viewHolder.iv_flag.setSelected(true);
            } else {
                viewHolder.iv_flag.setSelected(false);
            }

            viewHolder.iv_flag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setSelectedIndex(v.isSelected() ? -1 : position);
                }
            });

            return convertView;
        }

        public void setSelectedIndex(int index) {
            this.mSelectedIndex = index;
            notifyDataSetChanged();
        }

        class ViewHolder {
            ImageView iv_flag;
        }
    }
}
