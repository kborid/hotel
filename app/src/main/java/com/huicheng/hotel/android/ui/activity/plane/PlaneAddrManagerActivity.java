package com.huicheng.hotel.android.ui.activity.plane;

import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;

import com.alibaba.fastjson.JSON;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.RequestCodeDef;
import com.huicheng.hotel.android.content.AppConst;
import com.huicheng.hotel.android.content.NetURL;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.AddressInfoBean;
import com.huicheng.hotel.android.ui.adapter.AddressManagerAdapter;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @auth kborid
 * @date 2017/11/23 00
 */

public class PlaneAddrManagerActivity extends BaseAppActivity {

    private ListView listView;
    private List<AddressInfoBean> list = new ArrayList<>();
    private AddressManagerAdapter addressManagerAdapter;
    private int deleteIndex = -1;
    private boolean isNeedRefresh = false;

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
        tv_center_title.setText("管理地址");
        setRightButtonResource("添加", getResources().getColor(R.color.plane_mainColor));
        swipeRefreshLayout.setColorSchemeResources(R.color.plane_mainColor);
        addressManagerAdapter = new AddressManagerAdapter(this, list);
        listView.setAdapter(addressManagerAdapter);
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
        addressManagerAdapter.setOnActionListener(new AddressManagerAdapter.OnActionListener() {
            @Override
            public void onEdit(int position) {
                Intent intent = new Intent(PlaneAddrManagerActivity.this, PlaneAddrEditActivity.class);
                intent.putExtra("isEdit", true);
                intent.putExtra("addressBean", list.get(position));
                startActivityForResult(intent, RequestCodeDef.REQ_CODE_ADDRESS_EDIT);
            }

            @Override
            public void onDelete(int position) {
                deleteIndex = position;
                requestDeleteAddress(list.get(deleteIndex).id);
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_back:
                setResult(RESULT_OK, new Intent().putExtra("needRefresh", isNeedRefresh));
                finish();
                break;
            case R.id.tv_right:
                Intent intent = new Intent(this, PlaneAddrEditActivity.class);
                startActivityForResult(intent, RequestCodeDef.REQ_CODE_ADDRESS_ADD);
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

    private void requestDeleteAddress(String id) {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("id", id);
        ResponseData d = b.syncRequest(b);
        d.flag = AppConst.ADDRESS_DELETE;
        d.path = NetURL.ADDRESS_DELETE;
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
                addressManagerAdapter.notifyDataSetChanged();
            } else if (request.flag == AppConst.ADDRESS_DELETE) {
                removeProgressDialog();
                LogUtil.i(TAG, "json = " + response.body.toString());
                list.remove(deleteIndex);
                addressManagerAdapter.notifyDataSetChanged();
                isNeedRefresh |= true;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == RequestCodeDef.REQ_CODE_ADDRESS_ADD
                || requestCode == RequestCodeDef.REQ_CODE_ADDRESS_EDIT) {
            boolean isRefresh = false;
            if (null != data) {
                isRefresh = data.getBooleanExtra("isRefresh", false);
            }
            if (isRefresh) {
                requestData();
            }
            isNeedRefresh |= isRefresh;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            iv_back.performClick();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
