package com.huicheng.hotel.android.ui.activity.plane;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.content.AppConst;
import com.huicheng.hotel.android.content.NetURL;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.kborid.Interface.OnCityItemClickListener;
import com.kborid.bean.CityBean;
import com.kborid.bean.DistrictBean;
import com.kborid.bean.ProvinceBean;
import com.kborid.citywheel.CityConfig;
import com.kborid.style.citypickerview.CityPickerView;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.util.LogUtil;

/**
 * @auth kborid
 * @date 2017/11/24 0024.
 */

public class PlaneAddrEditActivity extends BaseAppActivity {

    private CityPickerView mPicker = new CityPickerView();
    private EditText et_name, et_phone, et_address;
    private TextView tv_pca;
    private Switch switch_default;
    private boolean isEdit = false;

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_plane_addressedit);
    }

    @Override
    protected void requestData() {
        super.requestData();
        mPicker.init(this);
    }

    @Override
    protected void initViews() {
        super.initViews();
        et_name = (EditText) findViewById(R.id.et_name);
        et_phone = (EditText) findViewById(R.id.et_phone);
        et_address = (EditText) findViewById(R.id.et_address);
        tv_pca = (TextView) findViewById(R.id.tv_pca);
        switch_default = (Switch) findViewById(R.id.switch_default);
    }

    @Override
    protected void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            isEdit = bundle.getBoolean("isEdit", false);
        }
    }

    @Override
    protected void initParams() {
        super.initParams();
        findViewById(R.id.comm_title_rl).setBackgroundColor(getResources().getColor(R.color.white));
        tv_center_title.setText("添加地址");
        setRightButtonResource("保存", getResources().getColor(R.color.plane_mainColor));
        CityConfig cityConfig = new CityConfig.Builder().build();
        cityConfig.setConfirmText("完成");
        cityConfig.setConfirmTextColorStr("#0077db");
        cityConfig.setLineColor("#00000000");
        cityConfig.setCancelText("");
        mPicker.setConfig(cityConfig);
    }

    @Override
    protected void initListeners() {
        super.initListeners();
        tv_pca.setOnClickListener(this);
        mPicker.setOnCityItemClickListener(new OnCityItemClickListener() {
            @Override
            public void onSelected(ProvinceBean province, CityBean city, DistrictBean district) {
                String pca = "";
                //省份
                if (province != null) {
                    LogUtil.i(TAG, "province id = " + province.getId() + ", name = " + province.getName());
                    pca += province.getName();
                }

                //城市
                if (city != null) {
                    LogUtil.i(TAG, "city id = " + city.getId() + ", name = " + city.getName());
                    pca += city.getName();
                }

                //地区
                if (district != null) {
                    LogUtil.i(TAG, "district id = " + district.getId() + ", name = " + district.getName());
                    pca += district.getName();
                }
                tv_pca.setText(pca);
            }

            @Override
            public void onCancel() {
                LogUtil.i(TAG, "mPicker onCancel()");
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.tv_right:
//                setResult(RESULT_OK, new Intent().putExtra("isRefresh", true));
//                finish();
                requestUpdateAddressInfo();
                break;
            case R.id.tv_pca:
                mPicker.showCityPicker();
                break;
            default:
                break;
        }
    }

    private void requestUpdateAddressInfo() {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("name", et_name.getText().toString());
        b.addBody("phone", et_phone.getText().toString());
        b.addBody("address", et_address.getText().toString());
        b.addBody("isdefault", switch_default.isChecked() ? "1" : "0");
        ResponseData d = b.syncRequest(b);
        if (isEdit) {
            d.flag = AppConst.ADDRESS_UPDATE;
            d.path = NetURL.ADDRESS_UPDATE;
        } else {
            d.flag = AppConst.ADDRESS_ADDNEW;
            d.path = NetURL.ADDRESS_ADDNEW;
        }
        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    @Override
    protected void onNotifyMessage(ResponseData request, ResponseData response) {
        super.onNotifyMessage(request, response);
        if (response != null && response.body != null) {
            if (request.flag == AppConst.ADDRESS_ADDNEW) {
                removeProgressDialog();
                LogUtil.i(TAG, "json = " + response.body.toString());
            } else if (request.flag == AppConst.ADDRESS_UPDATE) {
                removeProgressDialog();
                LogUtil.i(TAG, "json = " + response.body.toString());
            }
        }
    }
}
