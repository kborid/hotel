package com.huicheng.hotel.android.ui.activity;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.content.AppConst;
import com.huicheng.hotel.android.content.NetURL;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.OrderDetailInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.OrdersSpendInfoBean;
import com.huicheng.hotel.android.ui.activity.hotel.HotelOrderDetailActivity;
import com.huicheng.hotel.android.ui.activity.plane.PlaneOrderDetailActivity;
import com.huicheng.hotel.android.ui.adapter.MainOrderAdapter;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.huicheng.hotel.android.ui.listener.OnRecycleViewItemClickListener;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.util.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author kborid
 * @date 2016/12/7 0007
 */
public class UcOrdersActivity extends BaseAppActivity {

    private static final int PAGESIZE = 10;

    @BindView(R.id.iv_hotel)
    ImageView ivHotel;
    @BindView(R.id.iv_plane)
    ImageView ivPlane;
    @BindView(R.id.iv_trace)
    ImageView ivTrace;
    @BindView(R.id.tv_spend_year)
    TextView tvSpendYear;
    @BindView(R.id.tv_save_year)
    TextView tvSaveYear;
    @BindView(R.id.consumption_lay)
    LinearLayout consumptionLay;

    private OrdersSpendInfoBean ordersSpendInfoBean = null;
    private List<OrderDetailInfoBean> list = new ArrayList<>();
    private MainOrderAdapter adapter;
    private RecyclerView recyclerView;
    private RelativeLayout empty_lay;

    private int pageIndex = 0;
    private String orderType;
    private boolean isLoadMore = false;

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_uc_orders);
    }

    @Override
    protected void requestData() {
        super.requestData();
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                requestMyOrdersList(orderType, pageIndex);
                requestSpendyearly();
            }
        }, 500);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        requestData();
    }

    @Override
    public void initViews() {
        super.initViews();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MainOrderAdapter(this, list);
        recyclerView.setAdapter(adapter);
        empty_lay = (RelativeLayout) findViewById(R.id.empty_lay);
    }

    @Override
    public void initParams() {
        super.initParams();
        orderType = "";
        refreshOrderTypeStatus(orderType);
    }

    private void refreshOrderTypeStatus(String type) {
        ivHotel.setSelected(HotelCommDef.Order_Hotel.equals(type));
        ivPlane.setSelected(HotelCommDef.Order_Plane.equals(type));
        ivTrace.setSelected("".equals(type));
    }

    private void requestMyOrdersList(String orderType, int pageIndex) {
        LogUtil.i(TAG, "requestMyOrdersList() pageIndex = " + pageIndex);
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("orderType", orderType);
        b.addBody("status", "");
        b.addBody("startYear", "");
        b.addBody("endYear", "");
        b.addBody("pageIndex", String.valueOf(pageIndex));
        b.addBody("pageSize", String.valueOf(PAGESIZE));

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.ORDER_LIST;
        d.flag = AppConst.ORDER_LIST;
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void requestSpendyearly() {
        LogUtil.i(TAG, "requestSpendyearly()");
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("startYear", "");
        b.addBody("endYear", "");

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.ORDER_SPEND;
        d.flag = AppConst.ORDER_SPEND;
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        swipeRefreshLayout.setRefreshing(true);
        pageIndex = 0;
        isLoadMore = false;
        requestData();
    }

    @Override
    public void initListeners() {
        super.initListeners();

        adapter.setOnRecycleViewItemClickListener(new OnRecycleViewItemClickListener() {
            @Override
            public void OnItemClick(View view, int position) {
                OrderDetailInfoBean bean = list.get(position);
                if (HotelCommDef.Order_Hotel.equals(bean.orderType)) {
                    Intent intent = new Intent(UcOrdersActivity.this, HotelOrderDetailActivity.class);
                    intent.putExtra("orderId", bean.orderId);
                    intent.putExtra("orderType", bean.orderType);
                    startActivityForResult(intent, 0x01);
                } else if (HotelCommDef.Order_Plane.equals(bean.orderType)) {
                    Intent intent = new Intent(UcOrdersActivity.this, PlaneOrderDetailActivity.class);
                    HashMap<String, String> params = new HashMap<>();
                    params.put("orderId", bean.orderId);
                    params.put("orderType", bean.orderType);
                    intent.putExtra("path", SessionContext.getUrl(NetURL.ORDER_PLANE, params));
                    startActivityForResult(intent, 0x01);
                }
            }
        });
    }

    private void updateOrdersSpendInfo() {
        if (ordersSpendInfoBean != null) {
            tvSpendYear.setText(String.valueOf((int) ordersSpendInfoBean.spend));
            tvSaveYear.setText(String.valueOf((int) ordersSpendInfoBean.totalsave));
        }
    }

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.ORDER_LIST) {
                swipeRefreshLayout.setRefreshing(false);
                LogUtil.i(TAG, "json = " + response.body.toString());
                List<OrderDetailInfoBean> temp = JSON.parseArray(response.body.toString(), OrderDetailInfoBean.class);
                if (!isLoadMore) {
                    list.clear();
                }
                if (temp.size() <= 0) {
                    pageIndex--;
                    CustomToast.show("没有更多数据", CustomToast.LENGTH_SHORT);
                }
                list.addAll(temp);
                adapter.notifyDataSetChanged();
            } else if (request.flag == AppConst.ORDER_SPEND) {
                LogUtil.i(TAG, "json = " + response.body.toString());
                ordersSpendInfoBean = JSON.parseObject(response.body.toString(), OrdersSpendInfoBean.class);
                updateOrdersSpendInfo();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK != resultCode) {
            return;
        }
        if (requestCode == 0x01) {
            requestData();
        }
    }

    @OnClick({R.id.iv_hotel, R.id.iv_plane, R.id.iv_trace, R.id.consumption_lay})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_hotel:
                orderType = HotelCommDef.Order_Hotel;
                refreshOrderTypeStatus(orderType);
                requestData();
                break;
            case R.id.iv_plane:
                orderType = HotelCommDef.Order_Plane;
                refreshOrderTypeStatus(orderType);
                requestData();
                break;
            case R.id.iv_trace:
                orderType = "";
                refreshOrderTypeStatus(orderType);
                requestData();
                break;
            case R.id.consumption_lay:
                Intent intent = new Intent(UcOrdersActivity.this, UcCostDetailActivity.class);
                intent.putExtra("ordersSpendInfoBean", ordersSpendInfoBean);
                startActivity(intent);
                break;
        }
    }
}
