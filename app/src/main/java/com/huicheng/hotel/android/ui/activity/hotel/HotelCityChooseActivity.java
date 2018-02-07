package com.huicheng.hotel.android.ui.activity.hotel;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
import com.huicheng.hotel.android.content.AppConst;
import com.huicheng.hotel.android.control.CityListDataManager;
import com.huicheng.hotel.android.control.LocationInfo;
import com.huicheng.hotel.android.requestbuilder.bean.CityAreaInfoBean;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.huicheng.hotel.android.ui.custom.MyGridViewWidget;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author kborid
 * @date 2017/1/17 0017
 */
public class HotelCityChooseActivity extends BaseAppActivity {

    private Handler myHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x01:
                    initCityListData();
                    removeProgressDialog();
                    break;
            }
        }
    };

    private List<CityAreaInfoBean> mList = new ArrayList<>();
    private HashMap<String, List<CityAreaInfoBean>> mMap = new HashMap<>();

    private TextView tv_search_input;
    private List<String> mHistoryList = new ArrayList<>();
    private List<String> mHotCityList = new ArrayList<>();

    private MyGridViewWidget gv_history;
    private MyGridViewWidget gv_hot;
    private ListView listView;
    private GridView gv_index;
    private List<String> cityIndexList = new ArrayList<>();
    private CityIndexAdapter cityIndexAdapter;
    private List<CityAreaInfoBean> cityList = new ArrayList<>();
    private CityListAdapter cityListAdapter;
    private TextView tv_city_index;

    private String mProvince, mCity, mSiteId;

    @Override
    protected void requestData() {
        super.requestData();
        if (StringUtil.notEmpty(SharedPreferenceUtil.getInstance().getString(AppConst.CITY_HOTEL_JSON_FILE, "", false))
                && StringUtil.notEmpty(SharedPreferenceUtil.getInstance().getString(AppConst.CITY_HOTEL_JSON, "", false))) {
            myHandler.sendEmptyMessage(0x01);
        } else {
            showProgressDialog(this);
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    CityListDataManager.getInstance().initCityList(new CityListDataManager.OnCityParseCompleteListener() {
                        @Override
                        public void onComplete(boolean isSuccess) {
                            if (isSuccess) {
                                myHandler.sendEmptyMessage(0x01);
                            }
                        }
                    });
                }
            }.start();
        }
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_hotel_citychoose);
    }

    @Override
    public void initViews() {
        super.initViews();
        tv_search_input = (TextView) findViewById(R.id.tv_search_input);

        gv_history = (MyGridViewWidget) findViewById(R.id.gv_history);
        gv_hot = (MyGridViewWidget) findViewById(R.id.gv_hot);

        gv_index = (GridView) findViewById(R.id.gv_index);
        cityIndexAdapter = new CityIndexAdapter(this, cityIndexList);
        gv_index.setAdapter(cityIndexAdapter);

        tv_city_index = (TextView) findViewById(R.id.tv_city_index);
        listView = (ListView) findViewById(R.id.listview);
        cityListAdapter = new CityListAdapter(this, cityList);
        listView.setAdapter(cityListAdapter);
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText("选择目的地");
        //历史记录
        String historyCity = SharedPreferenceUtil.getInstance().getString(AppConst.HISTORY, "", false);
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
    }

    private void initCityListData() {
        mList = CityListDataManager.mCityAreaList;
        mMap = CityListDataManager.mCityAreaMap;
        if (mList.size() == 0 || mMap.size() == 0) {
            return;
        }

        List<String> tmp = new ArrayList<>(mMap.keySet());
        Collections.sort(tmp);
        cityIndexList.clear();
        cityIndexList.addAll(tmp);
        cityIndexAdapter.notifyDataSetChanged();
        cityList.clear();
        cityList.addAll(mMap.get(tv_city_index.getText().toString()));
        cityListAdapter.notifyDataSetChanged();
    }

    @Override
    public void initListeners() {
        super.initListeners();
        tv_search_input.setOnClickListener(this);
        gv_index.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedChar = cityIndexList.get(position);
                tv_city_index.setText(selectedChar);
                cityList.clear();
                cityList.addAll(mMap.get(selectedChar));
                cityListAdapter.notifyDataSetChanged();
                listView.setSelectionAfterHeaderView();
                cityIndexAdapter.setSelectedIndex(position);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCity = cityList.get(position).shortName;
                CityAreaInfoBean item = findCityBeanByCityString(mCity);
                actionResult(item);
            }
        });

        gv_history.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCity = mHistoryList.get(position);
                CityAreaInfoBean item = findCityBeanByCityString(mCity);
                actionResult(item);
            }
        });

        gv_hot.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCity = mHotCityList.get(position);
                CityAreaInfoBean item = findCityBeanByCityString(mCity);
                actionResult(item);
            }
        });
    }

    private CityAreaInfoBean findCityBeanByCityString(String cityStr) {
        LogUtil.i(TAG, "---begin---findCityBeanByCityString() cityStr = " + cityStr);
        CityAreaInfoBean item = null;
        if (StringUtil.notEmpty(cityStr)) {
            for (int i = 0; i < mList.size(); i++) {
                for (int j = 0; j < mList.get(i).list.size(); j++) {
                    if (mList.get(i).list.get(j).shortName.equals(cityStr)) {
                        item = mList.get(i).list.get(j);
                        mProvince = mList.get(i).shortName;
//                        if (mProvince.equals(item.shortName)) {
//                            mSiteId = mList.get(i).id;
//                        } else {
                        mSiteId = item.id;
//                        }

                        break;
                    }
                }
            }
        }
        LogUtil.i(TAG, "---enddd---findCityBeanByCityString()");
        return item;
    }

    private void actionResult(CityAreaInfoBean item) {
        //增加历史记录
        addHistory(LocationInfo.instance.getCity());
        if (item != null) {
            LogUtil.i(TAG, "========================================");
            LogUtil.i(TAG, "[Find Result Info:]" + item.toString());
            LogUtil.i(TAG, "[Save CtyBen Info:]" + "mSiteId:" + mSiteId + ", mProvince:" + mProvince + ", mCity:" + mCity);
            LocationInfo.instance.resetCity(mProvince, mCity, mSiteId);
            setResult(RESULT_OK, new Intent());
            finish();
        } else {
            CustomToast.show("暂未收录该城市", CustomToast.LENGTH_SHORT);
        }
    }

    //更新历史记录，最大显示5条，超过替换最早一条，去重处理
    private void addHistory(String city) {
        LogUtil.i(TAG, "---begin---addHistory() city = " + city);
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
            SharedPreferenceUtil.getInstance().setString(AppConst.HISTORY, jsonStr, false);
        }
        LogUtil.i(TAG, "---enddd---addHistory()");
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.tv_search_input:
                Intent intent = new Intent(this, HotelAllSearchActivity.class);
                startActivity(intent);
//                overridePendingTransition(R.anim.alpha_fade_in, R.anim.alpha_fade_out);
                break;
            default:
                break;
        }
    }

    private class QuickSelCityAdapter extends BaseAdapter {
        private Context context;
        private List<String> hotList = new ArrayList<>();

        QuickSelCityAdapter(Context context, List<String> hotList) {
            this.context = context;
            this.hotList = hotList;
        }

        @Override
        public int getCount() {
            return hotList.size();
        }

        @Override
        public Object getItem(int position) {
            return hotList.get(position);
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

            viewHolder.tv_city.setText(hotList.get(position));
            return convertView;
        }

        class ViewHolder {
            TextView tv_city;
        }
    }

    private class CityListAdapter extends BaseAdapter {
        private Context context;
        private List<CityAreaInfoBean> cityList = new ArrayList<>();

        CityListAdapter(Context context, List<CityAreaInfoBean> cityList) {
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

            viewHolder.tv_city.setText(cityList.get(position).shortName);
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
