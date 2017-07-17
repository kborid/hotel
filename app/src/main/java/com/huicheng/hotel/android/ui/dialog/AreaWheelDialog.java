package com.huicheng.hotel.android.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.SessionContext;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.wheel.OnWheelChangedListener;
import com.prj.sdk.widget.wheel.WheelView;
import com.prj.sdk.widget.wheel.adapters.ArrayWheelAdapter;
import com.prj.sdk.widget.wheel.adapters.CityAreaInfoBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 区域选择滚轮列表展示对话框
 */
public class AreaWheelDialog extends Dialog implements OnWheelChangedListener, View.OnClickListener {

    private static final int VISITABLE_COUNT = 5;
    private WheelView mProvince;
    private WheelView mCity;
    private WheelView mArea;

    private List<CityAreaInfoBean> provinceList = new ArrayList<>();
    private List<CityAreaInfoBean> cityList = new ArrayList<>();
    private List<CityAreaInfoBean> areaList = new ArrayList<>();

    private String mCurrentProviceName;
    private String mCurrentCityName;
    private String mCurrentAreaName;
    private String mId;
    private String mParentId;

    private Context mContext;
    private AreaWheelCallback mAreaWheelCallback;

    public AreaWheelDialog(Context context) {
        this(context, null);
    }

    public AreaWheelDialog(Context context, AreaWheelCallback mAreaWheelCallback) {
        super(context);
        this.mContext = context;
        this.mAreaWheelCallback = mAreaWheelCallback;
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.dialog_wheel_view);
        this.getWindow().setBackgroundDrawable(new ColorDrawable(0));// 去除窗口透明部分显示的黑色
        LayoutParams p = getWindow().getAttributes(); // 获取对话框当前的参数值
        p.width = Utils.mScreenWidth - Utils.dip2px(50);
        p.dimAmount = 0.9f;
        getWindow().setAttributes(p);
//        getWindow().setGravity(Gravity.BOTTOM);
        this.setCanceledOnTouchOutside(false);// 点击空白区域默认消失
        initViews();
        initParams();
        initListeners();
    }

    private void initViews() {
        mProvince = (WheelView) findViewById(R.id.id_province);
        mCity = (WheelView) findViewById(R.id.id_city);
        mArea = (WheelView) findViewById(R.id.id_district);
    }

    public final void initParams() {
        initDatas();
        mProvince.setViewAdapter(new ArrayWheelAdapter<>(mContext, provinceList));
        mProvince.setVisibleItems(VISITABLE_COUNT);
        if (provinceList.size() <= VISITABLE_COUNT - 2) {
            mProvince.setCyclic(false);
        } else {
            mProvince.setCyclic(true);
        }
        mCity.setVisibleItems(VISITABLE_COUNT);
        mArea.setVisibleItems(VISITABLE_COUNT);
        updateCities();
        updateAreas();
    }

    public final void initListeners() {
        // 添加change事件
        mProvince.addChangingListener(this);
        // 添加change事件
        mCity.addChangingListener(this);
        // 添加change事件
        mArea.addChangingListener(this);
    }

    public final void setCanceled(boolean bool) {
        this.setCanceledOnTouchOutside(bool);
    }

    public void show() {
        super.show();
    }

    public void dismiss() {
        super.dismiss();
        mAreaWheelCallback.onAreaWheelInfo(mCurrentProviceName, mCurrentCityName, mCurrentAreaName, mId, mParentId);
    }

    public boolean isShowing() {
        return super.isShowing();
    }

    private void updateAreas() {
        int pCurrent = mCity.getCurrentItem();
        mCurrentCityName = cityList.get(pCurrent).shortName;
        areaList = infliterList(cityList.get(pCurrent).list);
        if (areaList.size() <= VISITABLE_COUNT - 2) {
            mArea.setCyclic(false);
        } else {
            mArea.setCyclic(true);
        }
        mArea.setViewAdapter(new ArrayWheelAdapter<>(mContext, areaList));
        mArea.setCurrentItem(0);
        mCurrentAreaName = areaList.get(0).shortName;
        mId = areaList.get(0).id;
        mParentId = areaList.get(0).parentId;
    }

    /**
     * 根据当前的省，更新市WheelView的信息
     */
    private void updateCities() {
        int pCurrent = mProvince.getCurrentItem();
        mCurrentProviceName = provinceList.get(pCurrent).shortName;
        cityList = infliterList(provinceList.get(pCurrent).list);
        if (cityList.size() <= VISITABLE_COUNT - 2) {
            mCity.setCyclic(false);
        } else {
            mCity.setCyclic(true);
        }
        mCity.setViewAdapter(new ArrayWheelAdapter<>(mContext, cityList));
        mCity.setCurrentItem(0);
        updateAreas();
    }

    private void initDatas() {
        provinceList = infliterList(SessionContext.getCityAreaList());
    }

    private List<CityAreaInfoBean> infliterList(List<CityAreaInfoBean> list) {
        List<CityAreaInfoBean> temp = new ArrayList<>();
        for (CityAreaInfoBean bean : list) {
            if (!temp.contains(bean)) {
                temp.add(bean);
            }
        }
        return temp;
    }

    @Override
    public void onChanged(WheelView wheel, int oldValue, int newValue) {
        if (wheel == mProvince) {
            updateCities();
        } else if (wheel == mCity) {
            updateAreas();
        } else if (wheel == mArea) {
            mCurrentAreaName = areaList.get(newValue).shortName;
            mId = areaList.get(newValue).id;
            mParentId = areaList.get(newValue).parentId;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
        }
    }

    /**
     * 回调地址信息
     */
    public interface AreaWheelCallback {
        void onAreaWheelInfo(String ProviceName, String CityName, String AreaName, String Id, String ParentId);
    }

}
