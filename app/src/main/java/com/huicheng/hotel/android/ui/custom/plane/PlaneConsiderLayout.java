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

import java.util.ArrayList;
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
    private LinearLayout consider_content_lay;
    private ArrayList<IPlaneConsiderAction> actions = new ArrayList<>();
    private ConsiderAirOffTimeLayout considerAirOffTimeLayout;
    private ConsiderAirCompanyLayout considerAirCompanyLayout;
    private ConsiderAirPortLayout considerAirPortLayout;
    private ConsiderAirTypeLayout considerAirTypeLayout;
    private ConsiderAirCangLayout considerAirCangLayout;

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
        consider_content_lay = (LinearLayout) findViewById(R.id.consider_content_lay);
        considerAirOffTimeLayout = new ConsiderAirOffTimeLayout(context);
        considerAirCompanyLayout = new ConsiderAirCompanyLayout(context);
        considerAirPortLayout = new ConsiderAirPortLayout(context);
        considerAirTypeLayout = new ConsiderAirTypeLayout(context);
        considerAirCangLayout = new ConsiderAirCangLayout(context);
        consider_content_lay.removeAllViews();
        consider_content_lay.addView(considerAirOffTimeLayout);
    }

    private void initListeners() {
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.setSelectedIndex(position);
                changedContentLayout(position);
            }
        });

        actions.add(considerAirOffTimeLayout);
        actions.add(considerAirCompanyLayout);
        actions.add(considerAirPortLayout);
        actions.add(considerAirTypeLayout);
        actions.add(considerAirCangLayout);
    }

    private void changedContentLayout(int index) {
        consider_content_lay.removeAllViews();
        switch (index) {
            case 0:
                consider_content_lay.addView(considerAirOffTimeLayout);
                break;
            case 1:
                consider_content_lay.addView(considerAirCompanyLayout);
                break;
            case 2:
                consider_content_lay.addView(considerAirPortLayout);
                break;
            case 3:
                consider_content_lay.addView(considerAirTypeLayout);
                break;
            case 4:
                consider_content_lay.addView(considerAirCangLayout);
                break;
        }
    }

    public void reloadConfig() {
        for (IPlaneConsiderAction action : actions) {
            action.reloadConsiderConfig();
        }
    }

    public void saveConfig() {
        for (IPlaneConsiderAction action : actions) {
            action.saveConsiderConfig();
        }
    }

    public void resetConfig() {
        for (IPlaneConsiderAction action : actions) {
            action.resetConsiderConfig();
        }
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
