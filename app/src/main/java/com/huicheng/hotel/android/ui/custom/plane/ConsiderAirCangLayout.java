package com.huicheng.hotel.android.ui.custom.plane;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.ui.custom.MyListViewWidget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @auth kborid
 * @date 2017/11/29 0029.
 */

public class ConsiderAirCangLayout extends LinearLayout implements IPlaneConsiderAction {

    private Context context;
    private MyListViewWidget listview;
    private AirCangAdapter adapter = null;
    private List<String> mList = new ArrayList<>();
    private int selectedIndex = 0;

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
        mList.clear();
        mList.addAll(Arrays.asList(context.getResources().getStringArray(R.array.flightCang)));
        adapter = new AirCangAdapter(context, mList);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedIndex = position;
                adapter.setSelectedIndex(selectedIndex);
            }
        });
    }

//    public void updateCangInfo(List<PlaneFlightInfoBean> list) {
//        if (list != null && list.size() > 0) {
//            mList.clear();
//            mList.add("不限");
//            for (int i = 0; i < list.size(); i++) {
//                PlaneFlightInfoBean bean = list.get(i);
//                String type = PlaneCommDef.getCabinString(bean.positionLevel);
//                if (!mList.contains(type)) {
//                    mList.add(type);
//                }
//            }
//            adapter.notifyDataSetChanged();
//        }
//    }

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
        private List<String> list = new ArrayList<>();
        private int selectedIndex = 0;

        AirCangAdapter(Context context, List<String> list) {
            this.context = context;
            this.list = list;
        }

        public void setSelectedIndex(int selectedIndex) {
            this.selectedIndex = selectedIndex;
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
                        break;
                    case NOT_ALL:
                    default:
                        convertView = LayoutInflater.from(context).inflate(R.layout.lv_plane_consider_aircompany_item, null);
                        break;
                }
                viewHolder.root = (LinearLayout) convertView.findViewById(R.id.root);
                viewHolder.iv_logo = (ImageView) convertView.findViewById(R.id.iv_air_logo);
                viewHolder.iv_logo.setVisibility(GONE);
                viewHolder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
                viewHolder.iv_flag = (ImageView) convertView.findViewById(R.id.iv_flag);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.tv_title.setText(list.get(position));

            if (selectedIndex == 0) {
                viewHolder.root.setSelected(position == 0);
            } else {
                if (position == 0) {
                    viewHolder.root.setSelected(false);
                }
                if (position == selectedIndex) {
                    viewHolder.root.setSelected(!viewHolder.root.isSelected());
                }
            }

            return convertView;
        }

        class ViewHolder {
            LinearLayout root;
            ImageView iv_logo;
            TextView tv_title;
            ImageView iv_flag;
        }
    }
}
