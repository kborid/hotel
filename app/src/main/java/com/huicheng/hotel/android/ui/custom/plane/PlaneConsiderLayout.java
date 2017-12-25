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
import com.prj.sdk.app.AppConst;
import com.prj.sdk.util.SharedPreferenceUtil;

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
    private boolean isStraight = true;
    private ListView listview;
    private ConditionAdapter adapter;
    private LinearLayout consider_content_lay;
    private ArrayList<IPlaneConsiderAction> actions = new ArrayList<>();
    private ConsiderAirOffTimeLayout considerAirOffTimeLayout;
    private ConsiderAirCompanyLayout considerAirCompanyLayout;
    private ConsiderAirPortLayout considerAirPortLayout;
    private ConsiderAirTypeLayout considerAirTypeLayout;
    private ConsiderAirCangLayout considerAirCangLayout;
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
        initParmas();
        initListeners();
    }

    private void initViews() {
        LayoutInflater.from(context).inflate(R.layout.layout_plane_consider, this);
        switch_straight = (Switch) findViewById(R.id.switch_straight);
        listview = (ListView) findViewById(R.id.listview);
        consider_content_lay = (LinearLayout) findViewById(R.id.consider_content_lay);
        considerAirOffTimeLayout = new ConsiderAirOffTimeLayout(context);
        considerAirCompanyLayout = new ConsiderAirCompanyLayout(context);
        considerAirPortLayout = new ConsiderAirPortLayout(context);
        considerAirTypeLayout = new ConsiderAirTypeLayout(context);
        considerAirCangLayout = new ConsiderAirCangLayout(context);
        tv_cancel = (TextView) findViewById(R.id.tv_cancel);
        tv_reset = (TextView) findViewById(R.id.tv_reset);
        tv_confirm = (TextView) findViewById(R.id.tv_confirm);
    }

    private void initParmas() {
        adapter = new ConditionAdapter();
        listview.setAdapter(adapter);
        consider_content_lay.removeAllViews();
        consider_content_lay.addView(considerAirOffTimeLayout);
    }

    private void initListeners() {
        switch_straight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isStraight = isChecked;
            }
        });
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (adapter.getSelectedIndex() == position) {
                    return;
                }
                adapter.setSelectedIndex(position);
                changedContentLayout(position);
            }
        });

        actions.add(considerAirOffTimeLayout);
        actions.add(considerAirCompanyLayout);
        actions.add(considerAirPortLayout);
        actions.add(considerAirTypeLayout);
        actions.add(considerAirCangLayout);
        tv_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != listener) {
                    listener.onDismiss(false);
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
                if (null != listener) {
                    listener.onDismiss(true);
                }
                saveConfig();
            }
        });
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

    public void cancelConfig() {
        isStraight = SharedPreferenceUtil.getInstance().getBoolean(AppConst.CONSIDER_PLANE_IS_STRAIGHT, true);
        switch_straight.setChecked(isStraight);
        for (IPlaneConsiderAction action : actions) {
            action.cancelConsiderConfig();
        }
    }

    public void resetConfig() {
        isStraight = true;
        switch_straight.setChecked(true);
        for (IPlaneConsiderAction action : actions) {
            action.resetConsiderConfig();
        }
    }

    public void saveConfig() {
        SharedPreferenceUtil.getInstance().setBoolean(AppConst.CONSIDER_PLANE_IS_STRAIGHT, isStraight);
        for (IPlaneConsiderAction action : actions) {
            action.saveConsiderConfig();
        }
    }

    public void reloadConfig() {
        isStraight = SharedPreferenceUtil.getInstance().getBoolean(AppConst.CONSIDER_PLANE_IS_STRAIGHT, true);
        switch_straight.setChecked(isStraight);
        for (IPlaneConsiderAction action : actions) {
            action.reloadConsiderConfig();
        }
    }

    public interface OnConsiderLayoutDismissListener {
        void onDismiss(boolean isSave);
    }

    private OnConsiderLayoutDismissListener listener = null;

    public void setOnConsiderLayoutDismissListener(OnConsiderLayoutDismissListener listener) {
        this.listener = listener;
    }

    private class ConditionAdapter extends BaseAdapter {

        private List<String> list = Arrays.asList(getResources().getStringArray(R.array.PlaneConditionItem));
        private int selectedIndex = 0;

        void setSelectedIndex(int index) {
            this.selectedIndex = index;
            notifyDataSetChanged();
        }

        int getSelectedIndex(){
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
}
