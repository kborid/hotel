package com.huicheng.hotel.android.ui.custom.plane;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.huicheng.hotel.android.R;

import java.util.Arrays;
import java.util.List;

/**
 * @auth kborid
 * @date 2017/11/24 0024.
 */

public class PlaneConsiderLayout extends LinearLayout {
    private Context context;

    private ListView listview;
    private ConditionAdapter adapter;

    public PlaneConsiderLayout(Context context) {
        this(context, null);
    }

    public PlaneConsiderLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlaneConsiderLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
        initListeners();
    }

    private void init() {
        LayoutInflater.from(context).inflate(R.layout.layout_plane_consider, this);
        listview = (ListView) findViewById(R.id.listview);
        adapter = new ConditionAdapter();
        listview.setAdapter(adapter);
    }

    private void initListeners() {
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.setSelectedIndex(position);
            }
        });
    }

    private class ConditionAdapter extends BaseAdapter {

        private List<String> list = Arrays.asList(getResources().getStringArray(R.array.PlaneConditionItem));
        private int selectedIndex = 0;

        void setSelectedIndex(int index) {
            this.selectedIndex = index;
            notifyDataSetChanged();
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
                convertView = LayoutInflater.from(context).inflate(R.layout.lv_plane_consider_item, null);
                viewHolder.tv_consider = (TextView) convertView.findViewById(R.id.tv_consider_item);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.tv_consider.setText(list.get(position));
            viewHolder.tv_consider.setSelected(position == selectedIndex);
            return convertView;
        }

        class ViewHolder {
            private TextView tv_consider;
        }
    }
}
