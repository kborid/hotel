package com.huicheng.hotel.android.ui.activity;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.content.AppConst;
import com.huicheng.hotel.android.content.NetURL;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.BountyBaseInfo;
import com.huicheng.hotel.android.requestbuilder.bean.HomeBannerInfoBean;
import com.huicheng.hotel.android.ui.adapter.BountyLxbActiveAdapter;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kborid
 * @date 2018/1/26 0026.
 */

public class UcBountyMainActivity extends BaseAppActivity {

    private boolean isFirstLoad = false;
    private BountyBaseInfo mBountyBaseInfo = null;

    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout current_lxb_lay;
    private TextView tv_current_lxb;
    private ListView listview;
    private List<HomeBannerInfoBean> mList = new ArrayList<>();
    private BountyLxbActiveAdapter adapter;

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_uc_bountymain);
    }

    @Override
    protected void initViews() {
        super.initViews();
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        View header = LayoutInflater.from(this).inflate(R.layout.lv_lxb_main_header, null);
        current_lxb_lay = (LinearLayout) header.findViewById(R.id.current_lxb_lay);
        tv_current_lxb = (TextView) header.findViewById(R.id.tv_current_lxb);
        listview = (ListView) findViewById(R.id.listview);
        listview.addHeaderView(header);
    }

    @Override
    protected void initParams() {
        super.initParams();
        tv_center_title.setText(getString(R.string.side_lxb));
        findViewById(R.id.comm_title_rl).setBackgroundColor(getResources().getColor(R.color.white));
        swipeRefreshLayout.setColorSchemeResources(R.color.mainColor);
        swipeRefreshLayout.setDistanceToTriggerSync(200);
        swipeRefreshLayout.setProgressViewOffset(true, 0, Utils.dp2px(20));
        swipeRefreshLayout.setSize(SwipeRefreshLayout.DEFAULT);
        isFirstLoad = true;
        tv_current_lxb.setText(String.format(getString(R.string.lxb_util), 0));
        adapter = new BountyLxbActiveAdapter(this, mList);
        listview.setAdapter(adapter);
    }

    @Override
    protected void initListeners() {
        super.initListeners();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                requestBountyBaseInfo();
                requestBountyActive();
            }
        });
        current_lxb_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UcBountyMainActivity.this, UcBountyActivity.class);
                intent.putExtra("bountyBaseInfo", mBountyBaseInfo);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void requestData() {
        super.requestData();
        requestBountyBaseInfo();
        requestBountyActive();
    }

    private void refreshBountyBaseInfo() {
        if (null != mBountyBaseInfo) {
            tv_current_lxb.setText(String.format(getString(R.string.lxb_util), mBountyBaseInfo.rest));
        }
    }

    private void requestBountyBaseInfo() {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        ResponseData d = b.syncRequest(b);
        d.flag = AppConst.BOUNTY_USER_BASE;
        d.path = NetURL.BOUNTY_USER_BASE;
        if (isFirstLoad && !isProgressShowing()) {
            isFirstLoad = false;
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void requestBountyActive() {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        ResponseData d = b.syncRequest(b);
        d.flag = AppConst.BOUNTY_ACTIVES;
        d.path = NetURL.BOUNTY_ACTIVES;
        if (isFirstLoad && !isProgressShowing()) {
            isFirstLoad = false;
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        super.onNotifyMessage(request, response);
        if (response != null && response.body != null) {
            if (request.flag == AppConst.BOUNTY_USER_BASE) {
                LogUtil.i(TAG, "json = " + response.body.toString());
                mBountyBaseInfo = JSONObject.parseObject(response.body.toString(), BountyBaseInfo.class);
                refreshBountyBaseInfo();
                removeProgressDialog();
                swipeRefreshLayout.setRefreshing(false);
            } else if (request.flag == AppConst.BOUNTY_ACTIVES) {
                LogUtil.i(TAG, "json = " + response.body.toString());
                List<HomeBannerInfoBean> temp = JSON.parseArray(response.body.toString(), HomeBannerInfoBean.class);
                mList.clear();
                mList.addAll(temp);
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onNotifyError(ResponseData request, ResponseData response) {
        super.onNotifyError(request, response);
        swipeRefreshLayout.setRefreshing(false);
    }
}
