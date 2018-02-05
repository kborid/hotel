package com.huicheng.hotel.android.ui.activity.plane;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.content.AppConst;
import com.huicheng.hotel.android.content.NetURL;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.AddressInfoBean;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
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
    private AddressInfoBean mBean = null;

    private CityConfig mCityConfig = null;
    private String mProvince, mCity, mArea;
    private String mProvinceId, mCityId, mAreaId;

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
            if (bundle.getSerializable("addressBean") != null) {
                mBean = (AddressInfoBean) bundle.getSerializable("addressBean");
            }
        }
    }

    @Override
    protected void initParams() {
        super.initParams();
        findViewById(R.id.comm_title_rl).setBackgroundColor(getResources().getColor(R.color.white));
        tv_center_title.setText("添加地址");
        setRightButtonResource("保存", getResources().getColor(R.color.plane_mainColor));
        mCityConfig = new CityConfig.Builder()
                .confirmText("完成")
                .confirTextColor("#0077db")
                .cancelText("")
                .build();
        if (isEdit && null != mBean) {
            et_name.setText(mBean.name);
            et_phone.setText(mBean.phone);
            et_address.setText(mBean.address);
            tv_pca.setText(mBean.province + mBean.city + mBean.area);
            switch_default.setChecked(mBean.isdefault);
            mProvince = mBean.province;
            mProvinceId = mBean.provincecode;
            mCity = mBean.city;
            mCityId = mBean.citycode;
            mArea = mBean.area;
            mAreaId = mBean.areacode;
            mCityConfig.setDefaultProvinceName(mProvince);
            mCityConfig.setDefaultCityName(mCity);
            mCityConfig.setDefaultDistrict(mArea);
        }
        mPicker.setConfig(mCityConfig);
    }

    @Override
    protected void initListeners() {
        super.initListeners();
        tv_pca.setOnClickListener(this);
        mPicker.setOnCityItemClickListener(new OnCityItemClickListener() {
            @Override
            public void onSelected(ProvinceBean province, CityBean city, DistrictBean district) {
                String tempProvince = "", tempCity = "", tempArea = "";
                String tempProvinceId = "", tempCityId = "", tempAreaId = "";
                //省份
                if (province != null) {
                    LogUtil.i(TAG, "province id = " + province.getId() + ", name = " + province.getName());
                    tempProvince = province.getName();
                    tempProvinceId = province.getId();
                }

                //城市
                if (city != null) {
                    LogUtil.i(TAG, "city id = " + city.getId() + ", name = " + city.getName());
                    tempCity = city.getName();
                    tempCityId = city.getId();
                }

                //地区
                if (district != null) {
                    LogUtil.i(TAG, "district id = " + district.getId() + ", name = " + district.getName());
                    tempArea = district.getName();
                    tempAreaId = district.getId();
                }
                mProvince = tempProvince;
                mProvinceId = tempProvinceId;
                mCity = tempCity;
                mCityId = tempCityId;
                mArea = tempArea;
                mAreaId = tempAreaId;

                tv_pca.setText(mProvince + mCity + mArea);
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
                mCityConfig.setDefaultProvinceName(mProvince);
                mCityConfig.setDefaultCityName(mCity);
                mCityConfig.setDefaultDistrict(mArea);
                mPicker.setConfig(mCityConfig);
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
        b.addBody("province", mProvince);
        b.addBody("provincecode", mProvinceId);
        if (isEdit) {
            if (null != mBean)
                b.addBody("id", mBean.id);
        }
        b.addBody("city", mCity);
        b.addBody("citycode", mCityId);
        b.addBody("area", mArea);
        b.addBody("areacode", mAreaId);
        ResponseData d = b.syncRequest(b);
        if (isEdit) {
            d.flag = AppConst.ADDRESS_EDIT;
            d.path = NetURL.ADDRESS_EDIT;
        } else {
            d.flag = AppConst.ADDRESS_ADD;
            d.path = NetURL.ADDRESS_ADD;
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
            if (request.flag == AppConst.ADDRESS_ADD) {
                removeProgressDialog();
                LogUtil.i(TAG, "json = " + response.body.toString());
            } else if (request.flag == AppConst.ADDRESS_EDIT) {
                removeProgressDialog();
                LogUtil.i(TAG, "json = " + response.body.toString());
            }
            CustomToast.show("保存成功", CustomToast.LENGTH_LONG);
            setResult(RESULT_OK, new Intent().putExtra("isRefresh", true));
            finish();
        }
    }
}
