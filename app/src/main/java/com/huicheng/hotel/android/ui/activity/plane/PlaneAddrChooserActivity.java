package com.huicheng.hotel.android.ui.activity.plane;

import android.content.Intent;
import android.view.View;
import android.widget.ListView;

import com.alibaba.fastjson.JSON;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.RequestCodeDef;
import com.huicheng.hotel.android.content.AppConst;
import com.huicheng.hotel.android.content.NetURL;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.AddressInfoBean;
import com.huicheng.hotel.android.ui.adapter.AddressChooserAdapter;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @auth kborid
 * @date 2017/11/23 0023.
 */

public class PlaneAddrChooserActivity extends BaseAppActivity {

    private ListView listView;
    private List<AddressInfoBean> list = new ArrayList<>();
    private AddressChooserAdapter addressChooserAdapter;

    private AddressInfoBean mBean = null;
    private String mId = "";

    @Override
    protected void requestData() {
        super.requestData();
        swipeRefreshLayout.setRefreshing(true);
        requestAddressList();
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_plane_addresslist);
    }

    @Override
    public void initViews() {
        super.initViews();
        listView = (ListView) findViewById(R.id.listView);
    }

    @Override
    public void initParams() {
        super.initParams();
        findViewById(R.id.comm_title_rl).setBackgroundColor(getResources().getColor(R.color.white));
        tv_center_title.setText("选择地址");
        setRightButtonResource("管理", getResources().getColor(R.color.plane_mainColor));
        swipeRefreshLayout.setColorSchemeResources(R.color.plane_mainColor);
        addressChooserAdapter = new AddressChooserAdapter(this, list);
        listView.setAdapter(addressChooserAdapter);
        listView.setEmptyView(findViewById(R.id.tv_empty));
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        requestData();
    }

    @Override
    public void initListeners() {
        super.initListeners();
        addressChooserAdapter.setOnFlagCheckedListener(new AddressChooserAdapter.OnFlagCheckedListener() {
            @Override
            public void onCheck(int position) {
                mBean = list.get(position);
                mId = mBean.id;
                if (StringUtil.notEmpty(mId)) {
                    requestSetDefault(mId);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.tv_right:
                Intent intent = new Intent(this, PlaneAddrManagerActivity.class);
                startActivityForResult(intent, RequestCodeDef.REQ_CODE_ADDRESS_MANAGER);
                break;
            default:
                break;
        }
    }

    private void requestAddressList() {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        ResponseData d = b.syncRequest(b);
        d.flag = AppConst.ADDRESS_LIST;
        d.path = NetURL.ADDRESS_LIST;
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void requestSetDefault(String id) {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("id", id);
        ResponseData d = b.syncRequest(b);
        d.flag = AppConst.ADDRESS_DEFAULT;
        d.path = NetURL.ADDRESS_DEFAULT;
        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    @Override
    protected void onNotifyMessage(ResponseData request, ResponseData response) {
        super.onNotifyMessage(request, response);
        if (response != null && response.body != null) {
            if (request.flag == AppConst.ADDRESS_LIST) {
                removeProgressDialog();
                swipeRefreshLayout.setRefreshing(false);
                LogUtil.i(TAG, "json = " + response.body.toString());
                List<AddressInfoBean> tmp = JSON.parseArray(response.body.toString(), AddressInfoBean.class);
                list.clear();
                list.addAll(tmp);
                addressChooserAdapter.notifyDataSetChanged();
                int defaultIndex = addressChooserAdapter.getSelectedIndex();
                if (defaultIndex != -1) {
                    mBean = list.get(defaultIndex);
                    mId = mBean.id;
                }
            } else if (request.flag == AppConst.ADDRESS_DEFAULT) {
                removeProgressDialog();
                LogUtil.i(TAG, "json = " + response.body.toString());
                setResult(RESULT_OK, new Intent().putExtra("address", mBean));
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == RequestCodeDef.REQ_CODE_ADDRESS_MANAGER) {
            boolean isRefresh = false;
            if (null != data) {
                isRefresh = data.getBooleanExtra("needRefresh", false);
            }
            if (isRefresh) {
                requestData();
            }
        }
    }
}
