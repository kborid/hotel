package com.huicheng.hotel.android.ui.custom.plane;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.ui.custom.MyListViewWidget;

import java.util.ArrayList;

/**
 * @auth kborid
 * @date 2017/11/29 0029.
 */

public class ConsiderAirTypeLayout extends LinearLayout implements IPlaneConsiderAction {

    private Context context;
    private MyListViewWidget listview;
    private ArrayList<String> list = new ArrayList<>();

    public ConsiderAirTypeLayout(Context context) {
        this(context, null);
    }

    public ConsiderAirTypeLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ConsiderAirTypeLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        LayoutInflater.from(context).inflate(R.layout.layout_plane_consider_airtype, this);
        listview = (MyListViewWidget) findViewById(R.id.listview);
        for (int i = 0; i < 5; i++) {
            list.add(String.valueOf(i));
        }
        listview.setAdapter(new AirTypeAdapter(context, list));
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

    private class AirTypeAdapter extends BaseAdapter {

        private Context context;
        private ArrayList<String> list = new ArrayList<>();

        AirTypeAdapter(Context context, ArrayList<String> list) {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.lv_plane_consider_aircompany_item, null);
            TextView tv_title = (TextView) convertView.findViewById(R.id.tv_title);
            tv_title.append(list.get(position));
            return convertView;
        }
    }
}
