package com.huicheng.hotel.android.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.MyGridViewWidget;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.widget.CustomToast;
import com.prj.sdk.widget.wheel.adapters.CityAreaInfoBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author kborid
 * @date 2017/1/17 0017
 */
public class LocationActivity2 extends BaseActivity {
    private String[] hotCityStr = {"上海", "北京", "深圳", "广州", "杭州"};
    private EditText et_city;
    private TextView tv_history;

    private ListView listView;
    private MyGridViewWidget gv_hotcity;
    private GridView gv_index;
    private CityIndexAdapter cityIndexAdapter;
    private CityListAdapter cityListAdapter;
    private List<CityAreaInfoBean> cityList = new ArrayList<>();
    private TextView tv_city_index;

    private String mProvince, mCity, mSiteId;
    private String cityStr = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_location2_layout);
        initViews();
        initParams();
        initListeners();
    }

    @Override
    public void initViews() {
        super.initViews();
        et_city = (EditText) findViewById(R.id.et_city);
        if (et_city != null) {
            et_city.setEnabled(false);
        }
        tv_history = (TextView) findViewById(R.id.tv_history);
        listView = (ListView) findViewById(R.id.listview);
        gv_hotcity = (MyGridViewWidget) findViewById(R.id.gv_hotcity);
        gv_index = (GridView) findViewById(R.id.gv_index);
        tv_city_index = (TextView) findViewById(R.id.tv_city_index);
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (null != bundle && bundle.getString("city") != null && StringUtil.notEmpty(bundle.getString("city"))) {
            cityStr = bundle.getString("city").split(" ")[0];
        }
    }

    @Override
    public void initParams() {
        super.initParams();

        tv_center_title.setText("选择城市");

        if (StringUtil.notEmpty(cityStr)) {
            et_city.setText(cityStr);
        }
        String historyCity = SharedPreferenceUtil.getInstance().getString(AppConst.HISTORY_CITY, "", false);
        if (StringUtil.notEmpty(historyCity)) {
            tv_history.setText(historyCity);
        } else {
            tv_history.setText(cityStr);
        }

        // 热门城市
        HotCityAdapter hotCityAdapter = new HotCityAdapter(this, Arrays.asList(hotCityStr));
        gv_hotcity.setAdapter(hotCityAdapter);

        cityIndexAdapter = new CityIndexAdapter(this, SessionContext.getCityIndexList());
        gv_index.setAdapter(cityIndexAdapter);

        tv_city_index.setText("A");
        cityList.addAll(SessionContext.getCityAreaMap().get("A"));
        cityListAdapter = new CityListAdapter(this, cityList);
        listView.setAdapter(cityListAdapter);
    }

    @Override
    public void initListeners() {
        super.initListeners();
        tv_history.setOnClickListener(this);
        gv_index.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedChar = SessionContext.getCityIndexList().get(position);
                tv_city_index.setText(selectedChar);
                cityList.clear();
                cityList.addAll(SessionContext.getCityAreaMap().get(selectedChar));
                cityListAdapter.notifyDataSetChanged();
                listView.setSelectionAfterHeaderView();
                cityIndexAdapter.setSelectedIndex(position);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String province = SharedPreferenceUtil.getInstance().getString(AppConst.PROVINCE, "", false);
                String city = SharedPreferenceUtil.getInstance().getString(AppConst.CITY, "", false);
                String siteId = SharedPreferenceUtil.getInstance().getString(AppConst.SITEID, "", false);

                SharedPreferenceUtil.getInstance().setString(AppConst.HISTORY_PROVINCE, province, false);
                SharedPreferenceUtil.getInstance().setString(AppConst.HISTORY_CITY, city, false);
                SharedPreferenceUtil.getInstance().setString(AppConst.HISTORY_SITEID, siteId, false);

                CityAreaInfoBean item = cityList.get(position);
                mCity = cityList.get(position).shortName;
                for (int i = 0; i < SessionContext.getCityAreaList().size(); i++) {
                    if (SessionContext.getCityAreaList().get(i).id.equals(item.parentId)) {
                        mProvince = SessionContext.getCityAreaList().get(i).shortName;
                        if (mProvince.equals(item.shortName)) {
                            mSiteId = SessionContext.getCityAreaList().get(i).id;
                        } else {
                            mSiteId = item.id;
                        }
                        break;
                    }
                }

                SharedPreferenceUtil.getInstance().setString(AppConst.PROVINCE, mProvince, false);
                SharedPreferenceUtil.getInstance().setString(AppConst.CITY, mCity, false);
                SharedPreferenceUtil.getInstance().setString(AppConst.SITEID, mSiteId, false);

                setResult(RESULT_OK, new Intent());
                finish();
            }
        });

        gv_hotcity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String province = SharedPreferenceUtil.getInstance().getString(AppConst.PROVINCE, "", false);
                String city = SharedPreferenceUtil.getInstance().getString(AppConst.CITY, "", false);
                String siteId = SharedPreferenceUtil.getInstance().getString(AppConst.SITEID, "", false);

                SharedPreferenceUtil.getInstance().setString(AppConst.HISTORY_PROVINCE, province, false);
                SharedPreferenceUtil.getInstance().setString(AppConst.HISTORY_CITY, city, false);
                SharedPreferenceUtil.getInstance().setString(AppConst.HISTORY_SITEID, siteId, false);

                mCity = hotCityStr[position];
                CityAreaInfoBean item = null;
                for (int i = 0; i < SessionContext.getCityAreaList().size(); i++) {
                    for (int j = 0; j < SessionContext.getCityAreaList().get(i).list.size(); j++) {
                        if (SessionContext.getCityAreaList().get(i).list.get(j).shortName.equals(mCity)) {
                            item = SessionContext.getCityAreaList().get(i).list.get(j);

                            mProvince = SessionContext.getCityAreaList().get(i).shortName;
                            if (mProvince.equals(item.shortName)) {
                                mSiteId = SessionContext.getCityAreaList().get(i).id;
                            } else {
                                mSiteId = item.id;
                            }

                            break;
                        }
                    }
                }

                if (item != null) {
                    SharedPreferenceUtil.getInstance().setString(AppConst.PROVINCE, mProvince, false);
                    SharedPreferenceUtil.getInstance().setString(AppConst.CITY, mCity, false);
                    SharedPreferenceUtil.getInstance().setString(AppConst.SITEID, mSiteId, false);

                    setResult(RESULT_OK, new Intent());
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
        switch (v.getId()) {
            case R.id.tv_history: {
                mProvince = SharedPreferenceUtil.getInstance().getString(AppConst.HISTORY_PROVINCE, "", false);
                mCity = SharedPreferenceUtil.getInstance().getString(AppConst.HISTORY_CITY, "", false);
                mSiteId = SharedPreferenceUtil.getInstance().getString(AppConst.HISTORY_SITEID, "", false);

                String province = SharedPreferenceUtil.getInstance().getString(AppConst.PROVINCE, "", false);
                String city = SharedPreferenceUtil.getInstance().getString(AppConst.CITY, "", false);
                String siteId = SharedPreferenceUtil.getInstance().getString(AppConst.SITEID, "", false);

                SharedPreferenceUtil.getInstance().setString(AppConst.HISTORY_PROVINCE, province, false);
                SharedPreferenceUtil.getInstance().setString(AppConst.HISTORY_CITY, city, false);
                SharedPreferenceUtil.getInstance().setString(AppConst.HISTORY_SITEID, siteId, false);

                SharedPreferenceUtil.getInstance().setString(AppConst.PROVINCE, mProvince, false);
                SharedPreferenceUtil.getInstance().setString(AppConst.CITY, mCity, false);
                SharedPreferenceUtil.getInstance().setString(AppConst.SITEID, mSiteId, false);

                setResult(RESULT_OK, new Intent());
                finish();
                break;
            }
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void closeSoftKeyboard() {
        View view = getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private class HotCityAdapter extends BaseAdapter {
        private Context context;
        private List<String> hotList = new ArrayList<>();

        HotCityAdapter(Context context, List<String> hotList) {
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
            if (position % 2 == 1) {
                viewHolder.tv_city.setBackgroundResource(R.drawable.sel_city_b_selector);
            } else {
                viewHolder.tv_city.setBackgroundResource(R.drawable.sel_city_a_selector);
            }

            if (selectedIndex == position) {
                viewHolder.tv_city.setSelected(true);
            } else {
                viewHolder.tv_city.setSelected(false);
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
