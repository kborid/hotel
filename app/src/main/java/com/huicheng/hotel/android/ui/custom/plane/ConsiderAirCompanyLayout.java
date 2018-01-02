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
import com.huicheng.hotel.android.common.PlaneCommDef;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneFlightInfoBean;
import com.huicheng.hotel.android.ui.custom.MyListViewWidget;
import com.prj.sdk.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @auth kborid
 * @date 2017/11/29 0029.
 */

public class ConsiderAirCompanyLayout extends LinearLayout implements IPlaneConsiderAction {

    private Context context;
    private MyListViewWidget listview;
    private ArrayList<String> mList = new ArrayList<>();
    private AirCompanyAdapter adapter;
    private int selectedIndex = 0;

    public ConsiderAirCompanyLayout(Context context) {
        this(context, null);
    }

    public ConsiderAirCompanyLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ConsiderAirCompanyLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        LayoutInflater.from(context).inflate(R.layout.layout_plane_consider_aircompany, this);
        listview = (MyListViewWidget) findViewById(R.id.listview);
        adapter = new AirCompanyAdapter(context, mList);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (selectedIndex == position) {
                    return;
                }
                selectedIndex = position;
                adapter.setSelectedIndex(selectedIndex);
            }
        });
    }

    public void updateAirCompanyInfo(List<PlaneFlightInfoBean> list) {
        if (list != null && list.size() > 0) {
            mList.clear();
            mList.add("不限");
            for (int i = 0; i < list.size(); i++) {
                PlaneFlightInfoBean bean = list.get(i);
                if (!mList.contains(bean.carrier)) {
                    mList.add(bean.carrier);
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

    private class AirCompanyAdapter extends BaseAdapter {

        private static final int IS_ALL = 0;
        private static final int NOT_ALL = 1;

        private Context context;
        private ArrayList<String> list = new ArrayList<>();
        private int selectedIndex = 0;

        AirCompanyAdapter(Context context, ArrayList<String> list) {
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
                viewHolder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
                viewHolder.iv_flag = (ImageView) convertView.findViewById(R.id.iv_flag);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            String name = list.get(position);
            if (StringUtil.notEmpty(name)
                    && PlaneCommDef.AIR_ICON_CODE.containsKey(name)
                    && PlaneCommDef.AIR_ICON_CODE.get(name) != 0) {
                viewHolder.iv_logo.setImageResource(PlaneCommDef.AIR_ICON_CODE.get(name));
                viewHolder.iv_logo.setVisibility(View.VISIBLE);
            } else {
                viewHolder.iv_logo.setVisibility(View.INVISIBLE);
            }
            viewHolder.tv_title.setText(list.get(position));
            viewHolder.root.setSelected(selectedIndex == position);
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
