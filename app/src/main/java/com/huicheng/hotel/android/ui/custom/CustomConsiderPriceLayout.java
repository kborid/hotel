package com.huicheng.hotel.android.ui.custom;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @auth kborid
 * @date 2017/8/8.
 */

public class CustomConsiderPriceLayout extends LinearLayout {
    private Context context;

    private GridView gv_price;
    private QuickSelAdapter adapter;
    private List<String> mList = new ArrayList<>();

    private int textColor;
    private float textSize = 15;

    public CustomConsiderPriceLayout(Context context) {
        this(context, null);
    }

    public CustomConsiderPriceLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomConsiderPriceLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.layout_consider_price, this);
        gv_price = (GridView) findViewById(R.id.gv_price);
        adapter = new QuickSelAdapter(context, mList);
        gv_price.setAdapter(adapter);

        gv_price.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (adapter.selectedIndex == position) {
                    adapter.setSelectedIndex(-1);
                } else {
                    adapter.setSelectedIndex(position);
                }
            }
        });
    }

    public void setData(String[] data) {
        List<String> list;
        list = Arrays.asList(data);
        setData(list);
    }

    public void setData(List<String> list) {
        if (list.size() > 0) {
            mList.clear();
            mList.addAll(list);
            adapter.notifyDataSetChanged();
        }
    }

    public void setSelected(int index) {
        adapter.setSelectedIndex(index);
    }

    public int getSelectedIndex() {
        return adapter.selectedIndex;
    }

    public String getSelectedItem() {
        return mList.get(adapter.selectedIndex);
    }

    public int resetSelectedIndex() {
        adapter.setSelectedIndex(-1);
        return getSelectedIndex();
    }

    private class QuickSelAdapter extends BaseAdapter {
        private Context context;
        private List<String> list = new ArrayList<>();
        private int selectedIndex = -1;

        QuickSelAdapter(Context context, List<String> list) {
            this.context = context;
            this.list = list;
        }

        void setSelectedIndex(int index) {
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
            final ViewHolder viewHolder;
            if (null == convertView) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.layout_consider_price_item, null);
                viewHolder.tv_price = (TextView) convertView.findViewById(R.id.tv_price);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.tv_price.setText(list.get(position));
            if (selectedIndex == position) {
                viewHolder.tv_price.setSelected(true);
            } else {
                viewHolder.tv_price.setSelected(false);
            }
            return convertView;
        }

        class ViewHolder {
            TextView tv_price;
        }
    }

}
