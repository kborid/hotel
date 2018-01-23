package com.huicheng.hotel.android.ui.activity.hotel;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huicheng.hotel.android.PRJApplication;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.permission.PermissionsActivity;
import com.huicheng.hotel.android.permission.PermissionsDef;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.HotelInfoBean;
import com.huicheng.hotel.android.tools.CityParseUtils;
import com.huicheng.hotel.android.ui.adapter.SearchResultAdapter;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.huicheng.hotel.android.content.AppConst;
import com.huicheng.hotel.android.content.NetURL;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kborid
 * @date 2017/8/31.
 */

public class HotelAllSearchActivity extends BaseAppActivity {

    private static final int PAGE_SIZE = 20;

    private EditText et_input;
    private ImageView iv_all_rec;
    private TextView tv_cancel;

    private RecognizerDialog mDialog = null;
    private String keyWorld;

    private List<HotelInfoBean> list = new ArrayList<>();
    private SearchResultAdapter adapter;
    private ListView listview;
    private TextView tv_empty;
    private TextView mHeaderView;

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_hotel_allsearch);
    }

    @Override
    public void initViews() {
        super.initViews();
        et_input = (EditText) findViewById(R.id.et_input);
        iv_all_rec = (ImageView) findViewById(R.id.iv_all_rec);
        tv_cancel = (TextView) findViewById(R.id.tv_cancel);
        listview = (ListView) findViewById(R.id.listview);
        tv_empty = (TextView) findViewById(R.id.tv_empty);
        mHeaderView = new TextView(this);
        mHeaderView.setText("加上城市名搜索更准确哦!");
        mHeaderView.setPadding(0, Utils.dp2px(20), 0, Utils.dp2px(20));
        mHeaderView.setTextSize(15);
        mHeaderView.setTextColor(Color.parseColor("#333333"));
        mHeaderView.setGravity(Gravity.CENTER);
    }

    @Override
    public void initParams() {
        super.initParams();
        adapter = new SearchResultAdapter(this, list);
        listview.setAdapter(adapter);
        listview.setEmptyView(tv_empty);
    }

    @Override
    public void initListeners() {
        super.initListeners();
        et_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (StringUtil.notEmpty(s) && !s.toString().equals(keyWorld)) {
                    keyWorld = s.toString();
                    adapter.setHighLightShowString(keyWorld);
                    requestAllSearch(keyWorld);
                }
            }
        });

        et_input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    keyWorld = et_input.getText().toString();
                    if (StringUtil.notEmpty(keyWorld)) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        adapter.setHighLightShowString(keyWorld);
                        requestAllSearch(keyWorld);
                    }
                    return true;
                }
                return false;
            }
        });
        iv_all_rec.setOnClickListener(this);
        tv_cancel.setOnClickListener(this);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HotelInfoBean bean = (HotelInfoBean) parent.getAdapter().getItem(position);
                if (null == bean) {
                    return;
                }
                if (HotelCommDef.TYPE_HOTEL.equals(bean.type)) {
                    if (!SessionContext.isLogin()) {
                        sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION));
                        return;
                    }
                    HotelOrderManager.getInstance().setHotelType(HotelCommDef.TYPE_ALL);
                    Intent intent = new Intent(HotelAllSearchActivity.this, HotelDetailActivity.class);
                    intent.putExtra("key", HotelCommDef.ALLDAY);
                    intent.putExtra("hotelId", bean.hotelId);
                    startActivity(intent);
                } else if (HotelCommDef.TYPE_LAND_MARK.equals(bean.type)) {
                    //如果是地标类型数据，则手动设置定位城市、SiteId、省信息
//                    setLocationInfo(bean.province, bean.city, bean.adcode);
                    Intent intent = new Intent(HotelAllSearchActivity.this, HotelListActivity.class);
                    intent.putExtra("landmark", bean.landmark);
                    intent.putExtra("isLandMark", true);
                    intent.putExtra("lonLat", bean.hotelCoordinate);
                    intent.putExtra("siteId", CityParseUtils.getSiteIdString(bean.adcode));
                    startActivity(intent);
                }
            }
        });
    }

    private void setLocationInfo(String tempProvince, String tempCity, String tempSiteId) {
        LogUtil.i(TAG, "setLocationInfo()");
        LogUtil.i(TAG, tempProvince + ", " + tempCity + ", " + tempSiteId);
        if (StringUtil.notEmpty(tempProvince) && StringUtil.notEmpty(tempCity) && StringUtil.notEmpty(tempSiteId)) {
            String province = CityParseUtils.getProvinceString(tempProvince);
            String city = CityParseUtils.getCityString(tempCity);
            String siteId = CityParseUtils.getSiteIdString(tempSiteId);
            SharedPreferenceUtil.getInstance().setString(AppConst.PROVINCE, province, false);
            SharedPreferenceUtil.getInstance().setString(AppConst.CITY, city, false);
            SharedPreferenceUtil.getInstance().setString(AppConst.SITEID, siteId, false);
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_all_rec:
                if (PRJApplication.getPermissionsChecker(this).lacksPermissions(PermissionsDef.MIC_PERMISSION)) {
                    PermissionsActivity.startActivityForResult(this, PermissionsDef.PERMISSION_REQ_CODE, PermissionsDef.MIC_PERMISSION);
                    return;
                }
                showMicDialog();
                break;
            case R.id.tv_cancel:
                finish();
                break;
            default:
                break;
        }
    }

    private void showMicDialog() {
        if (null == mDialog) {
            mDialog = new RecognizerDialog(this, null);
            mDialog.setParameter(SpeechConstant.ASR_PTT, "0");
            mDialog.setParameter(SpeechConstant.ASR_SCH, "1");
            mDialog.setParameter(SpeechConstant.NLP_VERSION, "3.0");
        }
        mDialog.setListener(new RecognizerDialogListener() {
            @Override
            public void onResult(RecognizerResult recognizerResult, boolean b) {
                if (b) {
                    String jsonStr = recognizerResult.getResultString();
                    JSONObject mJson = JSON.parseObject(jsonStr);
                    if (mJson.containsKey("text")) {
                        et_input.setText(mJson.getString("text"));
                        et_input.setSelection(et_input.getText().length());
                    }
                }
            }

            @Override
            public void onError(SpeechError speechError) {
                LogUtil.e(TAG, "voice error code:" + speechError.getErrorCode() + ", " + speechError.getErrorDescription());
            }
        });
        mDialog.show();
    }

    private void requestAllSearch(String keyWorld) {
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("keyword", keyWorld);
        b.addBody("beginDate", String.valueOf(HotelOrderManager.getInstance().getBeginTime()));
        b.addBody("endDate", String.valueOf(HotelOrderManager.getInstance().getEndTime()));
        b.addBody("pageSize", String.valueOf(PAGE_SIZE));

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.ALL_SEARCH_HOTEL;
        d.flag = AppConst.ALL_SEARCH_HOTEL;

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        super.onNotifyMessage(request, response);
        if (response != null && response.body != null) {
            if (request.flag == AppConst.ALL_SEARCH_HOTEL) {
                LogUtil.i(TAG, "json = " + response.body.toString());
                List<HotelInfoBean> temp = JSON.parseArray(response.body.toString(), HotelInfoBean.class);
                if (temp.size() > 0 && SessionContext.isFirstLaunchDoAction(getClass().getSimpleName())) {
                    listview.removeHeaderView(mHeaderView);
                    listview.addHeaderView(mHeaderView);
                } else {
                    listview.removeHeaderView(mHeaderView);
                }
                list.clear();
                list.addAll(temp);
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.alpha_fade_in, R.anim.alpha_fade_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == PermissionsDef.PERMISSIONS_GRANTED
                && requestCode == PermissionsDef.PERMISSION_REQ_CODE) {
            showMicDialog();
        }
    }
}
