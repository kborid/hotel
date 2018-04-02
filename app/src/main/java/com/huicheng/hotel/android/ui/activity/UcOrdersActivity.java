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
import com.huicheng.hotel.android.content.AppConst;
import com.huicheng.hotel.android.content.NetURL;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.OrderDetailInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.OrdersSpendInfoBean;
import com.huicheng.hotel.android.ui.activity.hotel.HotelOrderDetailActivity;
import com.huicheng.hotel.android.ui.activity.plane.PlaneOrderDetailActivity;
import com.huicheng.hotel.android.ui.adapter.MainOrderAdapter;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.huicheng.hotel.android.ui.listener.OnRecycleViewItemClickListener;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.LoggerUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author kborid
 * @date 2016/12/7 0007
 */
public class UcOrdersActivity extends BaseAppActivity {

    private static final int PAGESIZE = 20;

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
    @BindView(R.id.empty_lay)
    RelativeLayout emptyLay;

    private OrdersSpendInfoBean ordersSpendInfoBean = null;
    private List<OrderDetailInfoBean> list = new ArrayList<>();
    private MainOrderAdapter adapter;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;

    private int pageIndex = 0;
    private String orderType;
    private boolean isLoadMore = false;
    private boolean isNoMore = false;
    private int lastVisibleItem = 0;

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_uc_orders);
    }

    @Override
    protected void requestData() {
        super.requestData();
        requestOrderListRefreshOrLoadMore(true, false);
    }

    private void requestOrderListRefreshOrLoadMore(final boolean isRequestSpend, boolean isLoadMore) {
        this.isLoadMore = isLoadMore;
        pageIndex = !isLoadMore ? 0 : ++pageIndex;

        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                requestMyOrdersList(orderType, pageIndex);
                if (isRequestSpend) {
                    requestSpendyearly();
                }
            }
        }, 500);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        requestOrderListRefreshOrLoadMore(true, false);
    }

    @Override
    public void initViews() {
        super.initViews();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new MainOrderAdapter(this, list);
        recyclerView.setAdapter(adapter);
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
        requestOrderListRefreshOrLoadMore(true, false);
    }

    @Override
    public void initListeners() {
        super.initListeners();

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!isNoMore && newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == adapter.getItemCount()) {
                    requestOrderListRefreshOrLoadMore(false, true);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
            }
        });

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
                    intent.putExtra("planeOrderNo", bean.orderNo);
                    intent.putExtra("planeOrderType", bean.type);
                    startActivityForResult(intent, 0x01);
                }
            }
        });
    }

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.ORDER_LIST) {
                swipeRefreshLayout.setRefreshing(false);
                LogUtil.i(TAG, "json = " + response.body.toString());
                LoggerUtil.i(response.body.toString());
                List<OrderDetailInfoBean> temp = JSON.parseArray(response.body.toString(), OrderDetailInfoBean.class);
                if (!isLoadMore) {
                    recyclerView.smoothScrollToPosition(0);
                    list.clear();
                }
                list.addAll(temp);
                adapter.notifyDataSetChanged();

                if (temp.size() >= PAGESIZE) {
                    isNoMore = false;
                } else if (temp.size() <= 0) {
                    pageIndex--;
                } else {
                    isNoMore = true;
                }

                if (list.size() <= 0) {
                    emptyLay.setVisibility(View.VISIBLE);
                } else {
                    emptyLay.setVisibility(View.GONE);
                }
            } else if (request.flag == AppConst.ORDER_SPEND) {
                LogUtil.i(TAG, "json = " + response.body.toString());
                ordersSpendInfoBean = JSON.parseObject(response.body.toString(), OrdersSpendInfoBean.class);
                if (ordersSpendInfoBean != null) {
                    tvSpendYear.setText(String.valueOf((int) ordersSpendInfoBean.spend));
                    tvSaveYear.setText(String.valueOf((int) ordersSpendInfoBean.totalsave));
                }
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
            requestOrderListRefreshOrLoadMore(true, false);
        }
    }

    @OnClick({R.id.iv_hotel, R.id.iv_plane, R.id.iv_trace, R.id.consumption_lay})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_hotel:
                orderType = HotelCommDef.Order_Hotel;
                refreshOrderTypeStatus(orderType);
                requestOrderListRefreshOrLoadMore(false, false);
                break;
            case R.id.iv_plane:
                orderType = HotelCommDef.Order_Plane;
                refreshOrderTypeStatus(orderType);
                requestOrderListRefreshOrLoadMore(false, false);
                break;
            case R.id.iv_trace:
                orderType = "";
                refreshOrderTypeStatus(orderType);
                requestOrderListRefreshOrLoadMore(false, false);
                break;
            case R.id.consumption_lay:
                Intent intent = new Intent(UcOrdersActivity.this, UcCostDetailActivity.class);
                intent.putExtra("ordersSpendInfoBean", ordersSpendInfoBean);
                startActivity(intent);
                break;
        }
    }
}
