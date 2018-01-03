package com.huicheng.hotel.android.ui.custom.plane;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneFlightInfoBean;
import com.prj.sdk.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @auth kborid
 * @date 2017/11/24 0024.
 */

public class PlaneConsiderLayout extends LinearLayout {
    private Context context;

    private Switch switch_straight;
    private boolean mIsOriginalStraight = false;
    private boolean isStraight = false;
    private int mSelectedIndex = 0;
    private ListView considerNamesListView;
    private ConditionAdapter adapter;
    private LinearLayout mContentLayout;
    private List<String> considerNames = new ArrayList<>();
    private List<IPlaneConsiderNotifyListener> listeners = new ArrayList<>();
    private List<BaseConsiderAirLayout> considerLayouts = new ArrayList<>();
    private TextView tv_cancel, tv_reset, tv_confirm;

    public PlaneConsiderLayout(Context context) {
        this(context, null);
    }

    public PlaneConsiderLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlaneConsiderLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initViews();
        initParams();
        initListeners();
    }

    private void initViews() {
        LayoutInflater.from(context).inflate(R.layout.layout_plane_consider, this);
        switch_straight = (Switch) findViewById(R.id.switch_straight);
        considerNamesListView = (ListView) findViewById(R.id.listview);
        considerNames = Arrays.asList(getResources().getStringArray(R.array.PlaneConditionItem));
        considerLayouts.add(new ConsiderAirOffTimeLayout(context));
        considerLayouts.add(new ConsiderAirCompanyLayout(context));
        considerLayouts.add(new ConsiderAirPortLayout(context));
        considerLayouts.add(new ConsiderAirTypeLayout(context));
        considerLayouts.add(new ConsiderAirCangLayout(context));
        mContentLayout = (LinearLayout) findViewById(R.id.consider_content_lay);
        tv_cancel = (TextView) findViewById(R.id.tv_cancel);
        tv_reset = (TextView) findViewById(R.id.tv_reset);
        tv_confirm = (TextView) findViewById(R.id.tv_confirm);
    }

    private void initParams() {
        mSelectedIndex = 0;
        adapter = new ConditionAdapter(context, considerNames);
        considerNamesListView.setAdapter(adapter);
        mContentLayout.removeAllViews();
        mContentLayout.addView(considerLayouts.get(mSelectedIndex));
    }

    private void initListeners() {
        listeners.addAll(considerLayouts);
        switch_straight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isStraight = isChecked;
            }
        });
        considerNamesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mSelectedIndex == position) {
                    return;
                }
                mSelectedIndex = position;
                adapter.setSelectedIndex(mSelectedIndex);
                mContentLayout.removeAllViews();
                mContentLayout.addView(considerLayouts.get(mSelectedIndex));
            }
        });

        tv_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelConfig();
                if (null != listener) {
                    listener.cancelConfig();
                }
            }
        });
        tv_reset.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                resetConfig();
            }
        });
        tv_confirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveConfig();
                if (null != listener) {
                    listener.saveConfig();
                }
            }
        });
    }

    public void cancelConfig() {
        isStraight = mIsOriginalStraight;
        for (IPlaneConsiderNotifyListener listener : listeners) {
            listener.cancel();
        }
    }

    public void resetConfig() {
        isStraight = false;
        switch_straight.setChecked(false);
        for (IPlaneConsiderNotifyListener listener : listeners) {
            listener.reset();
        }
    }

    public void saveConfig() {
        mIsOriginalStraight = isStraight;
        for (IPlaneConsiderNotifyListener listener : listeners) {
            listener.save();
        }
    }

    public void reloadConfig() {
        isStraight = mIsOriginalStraight;
        switch_straight.setChecked(isStraight);
        for (IPlaneConsiderNotifyListener listener : listeners) {
            listener.reload();
        }
    }

    private OnConsiderLayoutResultListener listener = null;

    public void setOnConsiderLayoutResultListener(OnConsiderLayoutResultListener listener) {
        this.listener = listener;
    }

    private class ConditionAdapter extends BaseAdapter {

        private Context context;
        private List<String> list = new ArrayList<>();
        private int selectedIndex = 0;

        ConditionAdapter(Context context, List<String> list) {
            this.context = context;
            this.list = list;
        }

        void setSelectedIndex(int index) {
            this.selectedIndex = index;
            notifyDataSetChanged();
        }

        int getSelectedIndex() {
            return selectedIndex;
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

    public boolean isStraight() {
        return switch_straight.isChecked();
    }

    public void updateChildConsiderInfo(List<PlaneFlightInfoBean> list) {
        for (int i = 0; i < considerLayouts.size(); i++) {
            considerLayouts.get(i).updateDataInfo(list);
        }
    }

    public BaseConsiderAirLayout getConsiderAirLayout(String name) {
        if (StringUtil.notEmpty(name)) {
            int index = considerNames.indexOf(name);
            return considerLayouts.get(index);
        }
        return null;
    }
}
