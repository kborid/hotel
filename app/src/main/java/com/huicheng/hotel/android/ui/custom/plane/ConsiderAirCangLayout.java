package com.huicheng.hotel.android.ui.custom.plane;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneFlightItemInfoBean;
import com.huicheng.hotel.android.ui.custom.MyListViewWidget;

import java.util.ArrayList;
import java.util.List;

/**
 * @auth kborid
 * @date 2017/11/29 0029.
 */

public class ConsiderAirCangLayout extends LinearLayout implements IPlaneConsiderAction {

    private Context context;
    private MyListViewWidget listview;
    private ArrayList<String> mList = new ArrayList<>();
    private AirCangAdapter adapter = null;

    public ConsiderAirCangLayout(Context context) {
        this(context, null);
    }

    public ConsiderAirCangLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ConsiderAirCangLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        LayoutInflater.from(context).inflate(R.layout.layout_plane_consider_aircang, this);
        listview = (MyListViewWidget) findViewById(R.id.listview);
        adapter = new AirCangAdapter(context, mList);
        listview.setAdapter(adapter);
    }

    public void updateCangInfo(List<PlaneFlightItemInfoBean> list) {
        if (list != null && list.size() > 0) {
            mList.clear();
            mList.add("不限");
            for (int i = 0; i < list.size(); i++) {
                PlaneFlightItemInfoBean bean = list.get(i);
                String type = "";
                if (bean.cabin.contains("Y")) {
                    type = "经济舱";
                } else {
                    type = "头等舱";
                }
                if (!mList.contains(type)) {
                    mList.add(type);
                }
            }
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void cancelConsiderConfig() {

    }

    @Override
    public void resetConsiderConfig() {

    }

    @Override
    public void saveConsiderConfig() {

    }

    @Override
    public void reloadConsiderConfig() {

    }

    private class AirCangAdapter extends BaseAdapter {

        private static final int IS_ALL = 0;
        private static final int NOT_ALL = 1;

        private Context context;
        private ArrayList<String> list = new ArrayList<>();

        AirCangAdapter(Context context, ArrayList<String> list) {
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
        public int getItemViewType(int position) {
            return list.get(position).equals("不限") ? IS_ALL : NOT_ALL;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (null == convertView) {
                viewHolder = new ViewHolder();
                switch (getItemViewType(position)) {
                    case IS_ALL:
                        convertView = LayoutInflater.from(context).inflate(R.layout.lv_plane_consider_all_item, null);
                        viewHolder.iv_logo = (ImageView) convertView.findViewById(R.id.iv_air_logo);
                        viewHolder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
                        viewHolder.iv_flag = (ImageView) convertView.findViewById(R.id.iv_flag);
                        break;
                    case NOT_ALL:
                    default:
                        convertView = LayoutInflater.from(context).inflate(R.layout.lv_plane_consider_aircompany_item, null);
                        viewHolder.iv_logo = (ImageView) convertView.findViewById(R.id.iv_air_logo);
                        viewHolder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
                        viewHolder.iv_flag = (ImageView) convertView.findViewById(R.id.iv_flag);
                        break;
                }
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.tv_title.setText(list.get(position));
            viewHolder.iv_logo.setVisibility(GONE);
            return convertView;
        }

        class ViewHolder {
            ImageView iv_logo;
            TextView tv_title;
            ImageView iv_flag;
        }
    }
}
