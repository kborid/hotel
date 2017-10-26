package com.huicheng.hotel.android.ui.custom;

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
import android.widget.ListView;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.prj.sdk.util.SharedPreferenceUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @auth kborid
 * @date 2017/10/26 0026.
 */

public class CustomSortLayout extends LinearLayout {

    private Context context;

    private List<String> mList = new ArrayList<>();
    private ListView listview;

    public CustomSortLayout(Context context) {
        this(context, null);
    }

    public CustomSortLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomSortLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        LayoutInflater.from(context).inflate(R.layout.pw_sort_layout, this);
        listview = (ListView) findViewById(R.id.listview);
        mList.clear();
        mList.addAll(Arrays.asList(context.getResources().getStringArray(R.array.SortString)));
        final MySortAdapter adapter = new MySortAdapter(context, mList);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (adapter.selectedIndex != position) {
                    adapter.setSelectedIndex(position);
                    SharedPreferenceUtil.getInstance().setInt(AppConst.SORT_INDEX, position);
                    if (listener != null) {
                        listener.doAction();
                    }
                }
                if (listener != null) {
                    listener.dismiss();
                }

            }
        });
    }


    private class MySortAdapter extends BaseAdapter {

        private List<String> list = new ArrayList<>();
        private Context context;
        private int selectedIndex = 0;

        MySortAdapter(Context context, List<String> list) {
            this.context = context;
            this.list = list;
            selectedIndex = SharedPreferenceUtil.getInstance().getInt(AppConst.SORT_INDEX, 0);
        }

        public void setSelectedIndex(int index) {
            selectedIndex = index;
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
                convertView = LayoutInflater.from(context).inflate(R.layout.pw_sort_layout_item, null);
                viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
                viewHolder.iv_flag = (ImageView) convertView.findViewById(R.id.iv_flag);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.tv_name.setText(list.get(position));

            if (selectedIndex == position) {
                viewHolder.iv_flag.setVisibility(VISIBLE);
            } else {
                viewHolder.iv_flag.setVisibility(INVISIBLE);
            }

            return convertView;
        }

        class ViewHolder {
            TextView tv_name;
            ImageView iv_flag;
        }

    }

    private OnSortResultListener listener = null;

    public void setOnSortResultListener(OnSortResultListener listener) {
        this.listener = listener;
    }

    public interface OnSortResultListener {
        void dismiss();
        void doAction();
    }
}
