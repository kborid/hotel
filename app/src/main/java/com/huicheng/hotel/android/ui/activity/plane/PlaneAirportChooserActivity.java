package com.huicheng.hotel.android.ui.activity.plane;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.CityAirportInfoBean;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.MyGridViewWidget;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.prj.sdk.app.AppConst;
import com.prj.sdk.app.NetURL;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.LoggerUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @auth kborid
 * @date 2017/12/27 0027.
 */

public class PlaneAirportChooserActivity extends BaseActivity {

    private String mJsonStr;
    private List<CityAirportInfoBean> mList = new ArrayList<>();

    private List<String> mFirstCharList = new ArrayList<>();
    private List<CityAirportInfoBean> mAirportByFirstCharList = new ArrayList<>();

    private List<String> mHistoryList = new ArrayList<>();
    private List<String> mHotCityList = new ArrayList<>();
    private MyGridViewWidget gv_history;
    private MyGridViewWidget gv_hot;
    private ListView listView;
    private GridView gv_index;
    private TextView tv_city_index;

    private CityIndexAdapter cityIndexAdapter;
    private CityListAdapter cityListAdapter;

    private String airportType = "OFF";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_plane_airport_layout);
        initViews();
        initParams();
        initListeners();
        if (null == savedInstanceState) {
            if (StringUtil.isEmpty(mJsonStr)) {
                requestAirportInfo();
            } else {
                mList = JSON.parseArray(mJsonStr, CityAirportInfoBean.class);
                Collections.sort(mList);
                updateScreenInfo();
            }
        }
    }

    @Override
    public void initViews() {
        super.initViews();
        gv_history = (MyGridViewWidget) findViewById(R.id.gv_history);
        gv_hot = (MyGridViewWidget) findViewById(R.id.gv_hot);
        gv_index = (GridView) findViewById(R.id.gv_index);
        tv_city_index = (TextView) findViewById(R.id.tv_city_index);
        listView = (ListView) findViewById(R.id.listview);
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (null != bundle && bundle.getString("type") != null) {
            airportType = bundle.getString("type");
        }
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText("选择飞机场✈");
        findViewById(R.id.comm_title_rl).setBackgroundColor(getResources().getColor(R.color.white));

        //历史记录
        String historyCity = SharedPreferenceUtil.getInstance().getString(AppConst.AIRPORT_HISTORY, "", false);
        if (StringUtil.notEmpty(historyCity)) {
            mHistoryList = JSON.parseArray(historyCity, String.class);
        }
        QuickSelCityAdapter historyAdapter = new QuickSelCityAdapter(this, mHistoryList);
        gv_history.setAdapter(historyAdapter);

        // 热门城市
        mHotCityList = Arrays.asList(getResources().getStringArray(R.array.HotCity));
        QuickSelCityAdapter hotCityAdapter = new QuickSelCityAdapter(this, mHotCityList);
        gv_hot.setAdapter(hotCityAdapter);

        tv_city_index.setText("A");

        cityIndexAdapter = new CityIndexAdapter(this, mFirstCharList);
        gv_index.setAdapter(cityIndexAdapter);
        cityListAdapter = new CityListAdapter(this, mAirportByFirstCharList);
        listView.setAdapter(cityListAdapter);

        mJsonStr = SharedPreferenceUtil.getInstance().getString(AppConst.CITY_PLANE_JSON, "", false);
    }

    private void updateScreenInfo() {
        if (mList != null && mList.size() > 0) {
            String firstChar = mList.get(0).firstchar;
            tv_city_index.setText(firstChar);
            mFirstCharList.clear();
            mAirportByFirstCharList.clear();
            for (int i = 0; i < mList.size(); i++) {
                CityAirportInfoBean bean = mList.get(i);
                if (!mFirstCharList.contains(bean.firstchar)) {
                    mFirstCharList.add(bean.firstchar);
                }
                if (bean.firstchar.equals(firstChar)) {
                    mAirportByFirstCharList.add(bean);
                }
            }
        }
        cityIndexAdapter.notifyDataSetChanged();
        cityListAdapter.notifyDataSetChanged();
    }

    //更新历史记录，最大显示5条，超过替换最早一条，去重处理
    private void addHistory(String city) {
        int index = 0;
        if (mHistoryList.contains(city)) {
            index = mHistoryList.indexOf(city);
            mHistoryList.remove(index);
        } else {
            if (mHistoryList.size() == 5) {
                index = mHistoryList.size() - 1;
                mHistoryList.remove(index);
            }
        }
        if (StringUtil.notEmpty(city)) {
            mHistoryList.add(0, city);
            String jsonStr = JSON.toJSONString(mHistoryList);
            SharedPreferenceUtil.getInstance().setString(AppConst.AIRPORT_HISTORY, jsonStr, false);
        }
    }

    @Override
    public void initListeners() {
        super.initListeners();
        gv_index.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                cityIndexAdapter.setSelectedIndex(position);
                String firstChar = mFirstCharList.get(position);
                tv_city_index.setText(firstChar);
                mAirportByFirstCharList.clear();
                for (int i = 0; i < mList.size(); i++) {
                    CityAirportInfoBean bean = mList.get(i);
                    if (bean.firstchar.equals(firstChar)) {
                        mAirportByFirstCharList.add(bean);
                    }
                }
                cityListAdapter.notifyDataSetChanged();
                listView.setSelectionAfterHeaderView();
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CityAirportInfoBean bean = mAirportByFirstCharList.get(position);
                //增加历史记录
                addHistory(bean.cityname);
                Intent data = new Intent();
                data.putExtra("type", airportType);
                data.putExtra("cityAirport", bean);
                setResult(RESULT_OK, data);
                finish();
            }
        });

        gv_history.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CityAirportInfoBean bean = null;
                String city = mHistoryList.get(position);
                for (int i = 0; i < mList.size(); i++) {
                    if (mList.get(i).cityname.equals(city)) {
                        bean = mList.get(i);
                        break;
                    }
                }
                //增加历史记录
                addHistory(city);
                if (bean != null) {
                    Intent data = new Intent();
                    data.putExtra("type", airportType);
                    data.putExtra("cityAirport", bean);
                    setResult(RESULT_OK, data);
                    finish();
                } else {
                    CustomToast.show("暂未收录该城市", CustomToast.LENGTH_SHORT);
                }
            }
        });

        gv_hot.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CityAirportInfoBean bean = null;
                String city = mHotCityList.get(position);
                for (int i = 0; i < mList.size(); i++) {
                    if (mList.get(i).cityname.equals(city)) {
                        bean = mList.get(i);
                        break;
                    }
                }
                //增加历史记录
                addHistory(city);
                if (bean != null) {
                    Intent data = new Intent();
                    data.putExtra("type", airportType);
                    data.putExtra("cityAirport", bean);
                    setResult(RESULT_OK, data);
                    finish();
                } else {
                    CustomToast.show("暂未收录该城市", CustomToast.LENGTH_SHORT);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
    }

    private void requestAirportInfo() {
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.PLANE_AIRPORT_LIST;
        d.flag = AppConst.PLANE_AIRPORT_LIST;
        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        super.onNotifyMessage(request, response);
        if (response != null && response.body != null) {
            if (request.flag == AppConst.PLANE_AIRPORT_LIST) {
                removeProgressDialog();
                LogUtil.i(TAG, "json = " + response.body.toString());
                LoggerUtil.i(response.body.toString());
                SharedPreferenceUtil.getInstance().setString(AppConst.CITY_PLANE_JSON, response.body.toString(), false);
                List<CityAirportInfoBean> temp = JSON.parseArray(response.body.toString(), CityAirportInfoBean.class);
                if (temp.size() > 0) {
                    mList.clear();
                    mList.addAll(temp);
                    Collections.sort(mList);
                }
                updateScreenInfo();
            }
        }
    }

    @Override
    public void onNotifyError(ResponseData request) {
        super.onNotifyError(request);
        removeProgressDialog();
    }

    private class QuickSelCityAdapter extends BaseAdapter {
        private Context context;
        private List<String> list = new ArrayList<>();

        QuickSelCityAdapter(Context context, List<String> list) {
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
            final ViewHolder viewHolder;
            if (null == convertView) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.gv_hotcity_item, null);
                viewHolder.tv_city = (TextView) convertView.findViewById(R.id.tv_city);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.tv_city.setText(list.get(position));
            return convertView;
        }

        class ViewHolder {
            TextView tv_city;
        }
    }

    private class CityListAdapter extends BaseAdapter {
        private Context context;
        private List<CityAirportInfoBean> cityList = new ArrayList<>();

        CityListAdapter(Context context, List<CityAirportInfoBean> cityList) {
            this.context = context;
            this.cityList = cityList;
        }

        @Override
        public int getCount() {
            return cityList.size();
        }

        @Override
        public Object getItem(int position) {
            return cityList.get(position);
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
                convertView = LayoutInflater.from(context).inflate(R.layout.lv_citylist_item, null);
                viewHolder.tv_city = (TextView) convertView.findViewById(R.id.tv_city);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.tv_city.setText(cityList.get(position).cityname);
            return convertView;
        }

        class ViewHolder {
            TextView tv_city;
        }
    }

    private class CityIndexAdapter extends BaseAdapter {
        private Context context;
        private List<String> indexList = new ArrayList<>();
        private int selectedIndex = 0;

        CityIndexAdapter(Context context, List<String> indexList) {
            this.context = context;
            this.indexList = indexList;
        }

        @Override
        public int getCount() {
            return indexList.size();
        }

        @Override
        public Object getItem(int position) {
            return indexList.get(position);
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
                convertView = LayoutInflater.from(context).inflate(R.layout.gv_cityindex_item, null);
                viewHolder.tv_city = (TextView) convertView.findViewById(R.id.tv_city);
                viewHolder.tv_city.setBackground(context.getResources().getDrawable(R.drawable.city_b_sel));
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.tv_city.setText(indexList.get(position));
            if (selectedIndex == position) {
                viewHolder.tv_city.setSelected(true);
            } else {
                viewHolder.tv_city.setSelected(false);
            }

            if (position % 2 == 1) {
                convertView.setBackgroundColor(context.getResources().getColor(R.color.citySelColor));
            } else {
                convertView.setBackgroundColor(context.getResources().getColor(R.color.transparent));
            }

            return convertView;
        }

        void setSelectedIndex(int index) {
            selectedIndex = index;
            notifyDataSetChanged();
        }

        class ViewHolder {
            TextView tv_city;
        }
    }
}
