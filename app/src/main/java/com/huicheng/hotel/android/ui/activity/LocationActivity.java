package com.huicheng.hotel.android.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.control.AMapLocationControl;
import com.huicheng.hotel.android.tools.PinyinUtils;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.CityAreaWheelView;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.wheel.adapters.CityAreaInfoBean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kborid
 * @date 2017/1/17 0017
 */
public class LocationActivity extends BaseActivity {
    private static final String TAG = "LocationActivity";
    private MyAdapter adapter;
    private Gallery gallery;
    private CityAreaWheelView wheelView;
    private EditText et_city;
    private TextView tv_current;
    private TextView tv_history;
    private ImageView iv_next;

    private boolean isJump = false;
    private boolean isEditable = false;
    private String mProvince, mCity, mSiteId;
    private String mFirstSpellChar = "A";
    private String cityStr = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_location_layout);
        initViews();
        initParams();
        initListeners();
    }

    @Override
    public void initViews() {
        super.initViews();
        et_city = (EditText) findViewById(R.id.et_city);
        tv_current = (TextView) findViewById(R.id.tv_current);
        tv_history = (TextView) findViewById(R.id.tv_history);
        iv_next = (ImageView) findViewById(R.id.iv_next);
        wheelView = (CityAreaWheelView) findViewById(R.id.wheelview);
        gallery = (Gallery) findViewById(R.id.gallery);
        adapter = new MyAdapter(this, SessionContext.getCityIndexList());
        if (gallery != null) {
            gallery.setAdapter(adapter);
        }
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (null != bundle && bundle.getString("city") != null && StringUtil.notEmpty(bundle.getString("city"))) {
            cityStr = bundle.getString("city").split(" ")[0];
            mFirstSpellChar = SessionContext.getFirstSpellChat(cityStr);
            isJump = true;
        }
    }

    @Override
    public void initParams() {
        super.initParams();

        tv_center_title.setText("选择城市");
        gallery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LogUtil.i(TAG, "gallery onItemSelected() citystr = " + cityStr);
                adapter.setSelectItem(position);
                adapter.notifyDataSetChanged();
                mFirstSpellChar = SessionContext.getCityIndexList().get(position);
                wheelView.setItems(SessionContext.getCityAreaMap().get(mFirstSpellChar));
                refreshWheelViewSelection();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        wheelView.setOnWheelViewListener(new CityAreaWheelView.OnWheelViewListener() {
            @Override
            public void onSelected(int selectedIndex, CityAreaInfoBean item) {
                super.onSelected(selectedIndex, item);
                LogUtil.i(TAG, "wheel onSelected city name = " + item.shortName);
                mCity = item.shortName;
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
                et_city.setText(mCity);
                et_city.setSelection(mCity.length());
            }
        });

        LogUtil.i(TAG, "gallery setSelection mFirstSpellChar = " + mFirstSpellChar);
        gallery.setSelection(SessionContext.getCityIndexList().indexOf(mFirstSpellChar));

        String currentCity = SharedPreferenceUtil.getInstance().getString(AppConst.LOCATION_CITY, "", false);
        if (StringUtil.notEmpty(currentCity)) {
            tv_current.setText(currentCity);
        } else {
            tv_current.setText("正在定位...");
            AMapLocationControl.getInstance().startLocationOnce(this, new AMapLocationControl.MyLocationListener() {
                @Override
                public void onLocation(AMapLocation aMapLocation) {
                    String province = aMapLocation.getProvince().replace("省", "");
                    String city = aMapLocation.getCity().replace("市", "");
                    SharedPreferenceUtil.getInstance().setString(AppConst.LOCATION_PROVINCE, province, false);
                    SharedPreferenceUtil.getInstance().setString(AppConst.LOCATION_CITY, city, false);
                    SharedPreferenceUtil.getInstance().setString(AppConst.LOCATION_SITEID, String.valueOf(aMapLocation.getAdCode()), false);
                    tv_current.setText(city);
                }
            }, true);
        }

        String historyCity = SharedPreferenceUtil.getInstance().getString(AppConst.HISTORY_CITY, "", false);
        if (StringUtil.notEmpty(historyCity)) {
            tv_history.setText(historyCity);
        } else {
            tv_history.setText(cityStr);
        }
    }

    private void refreshWheelViewSelection() {
        if (isJump) {
            isJump = false;
            int index = 0;
            for (int i = 0; i < SessionContext.getCityAreaMap().get(mFirstSpellChar).size(); i++) {
                String shortName = SessionContext.getCityAreaMap().get(mFirstSpellChar).get(i).shortName;
                if (shortName.contains(cityStr)
                        || PinyinUtils.getPingYin(shortName).startsWith(PinyinUtils.getPingYin(cityStr))) {
                    index = i;
                    break;
                }
            }
            LogUtil.i(TAG, "in for wheelView set selection index = " + index);
            wheelView.setSeletion(index);
        } else {
            wheelView.setSeletion(0);
        }
    }

    @Override
    public void initListeners() {
        super.initListeners();
        iv_next.setOnClickListener(this);
        tv_history.setOnClickListener(this);
        tv_current.setOnClickListener(this);
        et_city.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    isEditable = true;
                    et_city.setText("");
                } else {
                    isEditable = false;
                }
            }
        });
        et_city.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                LogUtil.i(TAG, "afterTextChanged s = " + s.toString());
                if (StringUtil.isEmpty(s)) {
                    return;
                }

                if (isEditable) {
                    et_city.setFocusable(false);
                    et_city.setFocusableInTouchMode(true);
                    closeSoftKeyboard();
                    isJump = true;
                    cityStr = s.toString();
                    String firstSpellChar = SessionContext.getFirstSpellChat(s.toString());
                    if (mFirstSpellChar.equals(firstSpellChar)) {
                        refreshWheelViewSelection();
                    } else {
                        mFirstSpellChar = firstSpellChar;
                        gallery.setSelection(SessionContext.getCityIndexList().indexOf(mFirstSpellChar));
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_next: {
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
                this.finish();
                break;
            }
            case R.id.tv_history: {
                mProvince = SharedPreferenceUtil.getInstance().getString(AppConst.HISTORY_PROVINCE, "", false);
                mCity = SharedPreferenceUtil.getInstance().getString(AppConst.HISTORY_CITY, "", false);
                mSiteId = SharedPreferenceUtil.getInstance().getString(AppConst.HISTORY_SITEID, "", false);
                isJump = true;
                cityStr = mCity;
                if (String.valueOf(PinyinUtils.getFirstSpell(cityStr).charAt(0)).toUpperCase().equals(mFirstSpellChar)) {
                    refreshWheelViewSelection();
                } else {
                    mFirstSpellChar = String.valueOf(PinyinUtils.getFirstSpell(cityStr).charAt(0)).toUpperCase();
                    gallery.setSelection(SessionContext.getCityIndexList().indexOf(mFirstSpellChar));
                }
                LogUtil.i(TAG, "gallery setSelection cityFirstSpell = " + mFirstSpellChar);
                break;
            }

            case R.id.tv_current: {
                mProvince = SharedPreferenceUtil.getInstance().getString(AppConst.LOCATION_PROVINCE, "", false);
                mCity = SharedPreferenceUtil.getInstance().getString(AppConst.LOCATION_CITY, "", false);
                mSiteId = SharedPreferenceUtil.getInstance().getString(AppConst.LOCATION_SITEID, "", false);
                isJump = true;
                cityStr = mCity;
                if (String.valueOf(PinyinUtils.getFirstSpell(cityStr).charAt(0)).toUpperCase().equals(mFirstSpellChar)) {
                    refreshWheelViewSelection();
                } else {
                    mFirstSpellChar = String.valueOf(PinyinUtils.getFirstSpell(cityStr).charAt(0)).toUpperCase();
                    gallery.setSelection(SessionContext.getCityIndexList().indexOf(mFirstSpellChar));
                }
                LogUtil.i(TAG, "gallery setSelection cityFirstSpell = " + mFirstSpellChar);
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

    class MyAdapter extends BaseAdapter {
        private Context context;
        private List<String> list = new ArrayList<>();
        private int selectItem = 0;

        public MyAdapter(Context context, List<String> list) {
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

        public void setSelectItem(int selectItem) {
            this.selectItem = selectItem;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.gl_item_layout, null);
            TextView tv_text = (TextView) view.findViewById(R.id.tv_text);
            tv_text.setText(list.get(position));
            Gallery.LayoutParams lp = new Gallery.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            lp.width = (Utils.mScreenWidth - Utils.dip2px(100)) / 5;
            view.setLayoutParams(lp);

            if (selectItem == position) {
                //选中时的动画
//            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.my_scale_action);    //实现动画效果
//            imageView.startAnimation(animation);  //选中时，这时设置的比较大
                tv_text.setTextSize(40f);
                tv_text.setAlpha(1);
            } else {
                //未选中时的动画
//            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.my_scale_action);    //实现动画效果
//            imageView.startAnimation(animation);
                tv_text.setTextSize(24f);
                tv_text.setAlpha(0.1f);
            }
            return view;
        }
    }

    private void closeSoftKeyboard() {
        View view = getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
