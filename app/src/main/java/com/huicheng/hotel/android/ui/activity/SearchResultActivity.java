package com.huicheng.hotel.android.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.net.bean.HotelInfoBean;
import com.huicheng.hotel.android.ui.adapter.SearchResultAdapter;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kborid
 * @date 2017/8/31.
 */

public class SearchResultActivity extends BaseActivity {

    private static final int PAGE_SIZE = 20;

    private EditText et_input;
    private ImageView iv_all_rec;
    private TextView tv_cancel;

    private String keyWorld;

    private List<HotelInfoBean> list = new ArrayList<>();
    private SearchResultAdapter adapter;
    private ListView listview;
    private TextView tv_empty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_search_result_layout);
        initViews();
        initParams();
        initListeners();
    }

    @Override
    public void initViews() {
        super.initViews();
        et_input = (EditText) findViewById(R.id.et_input);
        iv_all_rec = (ImageView) findViewById(R.id.iv_all_rec);
        tv_cancel = (TextView) findViewById(R.id.tv_cancel);
        listview = (ListView) findViewById(R.id.listview);
        tv_empty = (TextView) findViewById(R.id.tv_empty);
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
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
                if (!SessionContext.isLogin()) {
                    sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION));
                    return;
                }
                HotelInfoBean bean = list.get(position);
                HotelOrderManager.getInstance().setHotelType(HotelCommDef.TYPE_ALL);
                Intent intent = new Intent(SearchResultActivity.this, RoomListActivity.class);
                intent.putExtra("key", HotelCommDef.ALLDAY);
                intent.putExtra("hotelId", bean.hotelId);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_all_rec:
                RecognizerDialog mDialog = new RecognizerDialog(this, null);
                mDialog.setParameter(SpeechConstant.ASR_PTT, "0");
                mDialog.setParameter(SpeechConstant.ASR_SCH, "1");
                mDialog.setParameter(SpeechConstant.NLP_VERSION, "3.0");
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
                break;
            case R.id.tv_cancel:
                this.finish();
                overridePendingTransition(R.anim.alpha_fade_in, R.anim.alpha_fade_out);
                break;
            default:
                break;
        }
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
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        super.onNotifyMessage(request, response);
        if (response != null && response.body != null) {
            if (request.flag == AppConst.ALL_SEARCH_HOTEL) {
                LogUtil.i(TAG, "json = " + response.body.toString());
                List<HotelInfoBean> temp = JSON.parseArray(response.body.toString(), HotelInfoBean.class);
                list.clear();
                list.addAll(temp);
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            tv_cancel.performClick();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
