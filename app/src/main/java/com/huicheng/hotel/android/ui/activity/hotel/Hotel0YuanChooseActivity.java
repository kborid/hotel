package com.huicheng.hotel.android.ui.activity.hotel;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.net.bean.CouponDetailInfoBean;
import com.huicheng.hotel.android.net.bean.FreeOneNightBean;
import com.huicheng.hotel.android.ui.adapter.Hotel0YuanAdapter;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.huicheng.hotel.android.ui.dialog.AreaWheelDialog;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;

import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author kborid
 * @date 2016/11/15 0015
 */
public class Hotel0YuanChooseActivity extends BaseAppActivity implements AreaWheelDialog.AreaWheelCallback {

    private static final int ROW = 5;
    private static final int UPDATETIMER = 0x01;
    private static final int GAMEOVER = 0x02;

    private static final int PAGESIZE = 30;
    private ImageView iv_back;

    private TextView tv_city, tv_area;

    private int hour, min, sec;
    private TextView tv_hour, tv_min, tv_sec;
    private Timer timer;
    private Handler myHandler = new MyHandler(this);
    private RecyclerView recyclerView;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;
    private Hotel0YuanAdapter adapter = null;
    private List<CouponDetailInfoBean> list = new ArrayList<>();

    private LinearLayout addr_lay;

    private FreeOneNightBean bean = null;

    @Override
    protected void requestData() {
        super.requestData();
        requestFreeActiveDetail(0);
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_hotel_0yuanchoose);
    }

    @Override
    public void initViews() {
        super.initViews();
        iv_back = (ImageView) findViewById(R.id.iv_back);

        tv_city = (TextView) findViewById(R.id.tv_city);
        tv_area = (TextView) findViewById(R.id.tv_area);

        tv_hour = (TextView) findViewById(R.id.tv_hour);
        tv_min = (TextView) findViewById(R.id.tv_min);
        tv_sec = (TextView) findViewById(R.id.tv_sec);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(ROW, StaggeredGridLayoutManager.HORIZONTAL);
        staggeredGridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        adapter = new Hotel0YuanAdapter(this, list);
        recyclerView.setAdapter(adapter);

        addr_lay = (LinearLayout) findViewById(R.id.addr_lay);
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.getSerializable("active") != null) {
                bean = (FreeOneNightBean) bundle.getSerializable("active");
            }
        }
    }

    @Override
    public void initParams() {
        super.initParams();
        timer = new Timer();
        timer.schedule(new MyTimerTask(), 1000, 1000);
        if (bean != null) {
            long value = bean.endTime - bean.startTime;
            hour = (int) value / DateUtil.HOUR_UNIT;
            min = (int) (value - hour * DateUtil.HOUR_UNIT) / DateUtil.MIN_UNIT;
            sec = (int) (value - hour * DateUtil.HOUR_UNIT - min * DateUtil.MIN_UNIT) / DateUtil.SEC_UNIT;
        } else {
            hour = 0;
            min = 0;
            sec = 0;
        }
        tv_hour.setText(String.format("%02d", hour));
        tv_min.setText(String.format("%02d", min));
        tv_sec.setText(String.format("%02d", sec));
    }

    private void requestFreeActiveDetail(int pageIndex) {
        requestFreeActiveDetail("", "", pageIndex);
    }

    private void requestFreeActiveDetail(String cityCode, String areaCode, int pageIndex) {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("cityCode", cityCode);
        b.addBody("areaCode", areaCode);
        b.addBody("activityId", String.valueOf(bean.id));
        b.addBody("pageIndex", String.valueOf(pageIndex));
        b.addBody("pageSize", String.valueOf(PAGESIZE));

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.FREE_ACTIVE_DETAIL;
        d.flag = AppConst.FREE_ACTIVE_DETAIL;

        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void requestGrabCoupon(int index) {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("activityId", String.valueOf(bean.id));
        b.addBody("couponId", String.valueOf(index));

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.FREE_GRAB_COUPON;
        d.flag = AppConst.FREE_GRAB_COUPON;
        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    @Override
    public void initListeners() {
        super.initListeners();
        iv_back.setOnClickListener(this);
        addr_lay.setOnClickListener(this);
        adapter.setOnItemClickListener(new Hotel0YuanAdapter.OnItemClickListeners() {
            @Override
            public void OnItemClick(View v, int index) {
                requestGrabCoupon(list.get(index).couponId);
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_back:
                this.finish();
                break;
            case R.id.addr_lay:
                AreaWheelDialog dialog = new AreaWheelDialog(this, this);
                dialog.setCanceled(true);
                dialog.show();
                break;
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
        timer.cancel();
        myHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onAreaWheelInfo(String ProviceName, String CityName, String AreaName, String Id, String ParentId) {
        LogUtil.i(TAG, ProviceName + " " + CityName + " " + AreaName);
        tv_city.setText(CityName);
        tv_area.setText(AreaName);
        if (bean != null) {
            requestFreeActiveDetail(ParentId, Id, 0);
        } else {
            CustomToast.show("当前无活动", CustomToast.LENGTH_SHORT);
        }
    }

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.FREE_ACTIVE_DETAIL) {
                removeProgressDialog();
                List<CouponDetailInfoBean> temp = JSON.parseArray(response.body.toString(), CouponDetailInfoBean.class);
                if (temp != null && temp.size() > 0) {
                    list.clear();
                    list.addAll(temp);
                }
                adapter.notifyDataSetChanged();
            } else if (request.flag == AppConst.FREE_GRAB_COUPON) {
                removeProgressDialog();
                if (Boolean.valueOf(response.body.toString())) {
                    CustomToast.show("抢到了", CustomToast.LENGTH_SHORT);
                }
                requestFreeActiveDetail(0);
            }
        }
    }

    @Override
    public void notifyError(ResponseData request, ResponseData response, Exception e) {
        removeProgressDialog();
        String message;
        if (e != null && e instanceof ConnectException) {
            message = getString(R.string.dialog_tip_net_error);
        } else {
            message = response != null && response.data != null ? response.data.toString() : getString(R.string.dialog_tip_null_error);
            if (response != null) {
                requestFreeActiveDetail(0);
            }
        }
        CustomToast.show(message, CustomToast.LENGTH_SHORT);
    }

    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            if (sec > 0 && sec <= 59) {
                sec--;
            } else {
                if (min > 0 && min <= 59) {
                    sec = 59;
                    min--;
                } else {
                    if (hour > 0) {
                        hour--;
                        min = 59;
                        sec = 59;
                    } else {
                        myHandler.sendEmptyMessage(GAMEOVER);
                        return;
                    }
                }
            }
            myHandler.sendEmptyMessage(UPDATETIMER);
        }
    }

    private static class MyHandler extends Handler {
        WeakReference<Hotel0YuanChooseActivity> mActivity;

        MyHandler(Hotel0YuanChooseActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final Activity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case UPDATETIMER:
                        mActivity.get().tv_hour.setText(String.format("%02d", mActivity.get().hour));
                        mActivity.get().tv_min.setText(String.format("%02d", mActivity.get().min));
                        mActivity.get().tv_sec.setText(String.format("%02d", mActivity.get().sec));
                        break;
                    case GAMEOVER:
                        mActivity.get().timer.cancel();
                        CustomToast.show("活动已结束", CustomToast.LENGTH_SHORT);
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
