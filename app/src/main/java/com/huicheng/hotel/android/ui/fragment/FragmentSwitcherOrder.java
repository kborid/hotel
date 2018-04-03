package com.huicheng.hotel.android.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

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
import com.huicheng.hotel.android.ui.base.BaseFragment;
import com.huicheng.hotel.android.ui.listener.OnRecycleViewItemClickListener;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * @author kborid
 * @date 2018/2/9 0009.
 */

public class FragmentSwitcherOrder extends BaseFragment implements View.OnClickListener, DataCallback {
    private Bundle bundle = null;
    public static boolean isFirstLoad = false;

    private OrdersSpendInfoBean mOrdersSpendInfoBean = null;
    private List<OrderDetailInfoBean> list = new ArrayList<>();
    private MainOrderAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView recyclerView;
    private RelativeLayout empty_lay;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        isFirstLoad = true;
        bundle = getArguments();
        View view = inflater.inflate(R.layout.layout_content_order, container, false);
        initTypedArrayValue();
        initViews(view);
        initParams();
        initListeners();
        return view;
    }

    public static Fragment newInstance() {
        Fragment fragment = new FragmentSwitcherOrder();
        fragment.setArguments(null);
        return fragment;
    }

    @Override
    protected void onVisible() {
        super.onVisible();
        if (isFirstLoad) {
            isFirstLoad = false;
            swipeRefreshLayout.setRefreshing(true);
            myHandler.sendEmptyMessageDelayed(0x01, 500);
        }
    }

    @Override
    protected void onInvisible() {
        super.onInvisible();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MainOrderAdapter(getActivity(), list);
        recyclerView.setAdapter(adapter);
        empty_lay = (RelativeLayout) view.findViewById(R.id.empty_lay);
    }

    @Override
    protected void initParams() {
        super.initParams();
        swipeRefreshLayout.setColorSchemeResources(mSwipeRefreshColor);
        swipeRefreshLayout.setDistanceToTriggerSync(200);
        swipeRefreshLayout.setProgressViewOffset(true, 0, Utils.dp2px(20));
        swipeRefreshLayout.setSize(SwipeRefreshLayout.DEFAULT);
    }

    @Override
    protected void initListeners() {
        super.initListeners();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestMyOrdersList();
//                requestSpendyearly();
            }
        });
        adapter.setOnRecycleViewItemClickListener(new OnRecycleViewItemClickListener() {
            @Override
            public void OnItemClick(View view, int position) {
                OrderDetailInfoBean bean = list.get(position);
                if (HotelCommDef.Order_Hotel.equals(bean.orderType)) {
                    Intent intent = new Intent(getActivity(), HotelOrderDetailActivity.class);
                    intent.putExtra("orderId", bean.orderId);
                    intent.putExtra("orderType", bean.orderType);
                    startActivityForResult(intent, 0x01);
                } else if (HotelCommDef.Order_Plane.equals(bean.orderType)) {
                    Intent intent = new Intent(getActivity(), PlaneOrderDetailActivity.class);
                    intent.putExtra("planeOrderNo", bean.orderNo);
                    intent.putExtra("planeOrderType", bean.type);
                    startActivityForResult(intent, 0x01);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
    }

    private void requestMyOrdersList() {
        LogUtil.i(TAG, "requestMyOrdersList()");
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("orderType", "");
        b.addBody("status", "");
        b.addBody("startYear", "");
        b.addBody("endYear", "");
        b.addBody("pageIndex", "0");
        b.addBody("pageSize", "5");

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.i(TAG, "onActivityResult() " + requestCode + ", " + resultCode);
        if (RESULT_OK != resultCode) {
            return;
        }
        if (requestCode == 0x01) {
            swipeRefreshLayout.setRefreshing(true);
            myHandler.sendEmptyMessageDelayed(0x01, 500);
        }
    }

    @Override
    public void preExecute(ResponseData request) {
    }

    @Override
    public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.ORDER_LIST) {
                LogUtil.i(TAG, "json = " + response.body.toString());
                List<OrderDetailInfoBean> temp = JSON.parseArray(response.body.toString(), OrderDetailInfoBean.class);
                if (temp.size() <= 0) {
                    empty_lay.setVisibility(View.VISIBLE);
                } else {
                    list.clear();
                    list.addAll(temp);
                    adapter.notifyDataSetChanged();
                    empty_lay.setVisibility(View.GONE);
                }
                swipeRefreshLayout.setRefreshing(false);
            } else if (request.flag == AppConst.ORDER_SPEND) {
                LogUtil.i(TAG, "json = " + response.body.toString());
                mOrdersSpendInfoBean = JSON.parseObject(response.body.toString(), OrdersSpendInfoBean.class);
            }
        }
    }

    @Override
    public void notifyError(ResponseData request, ResponseData response, Exception e) {
        swipeRefreshLayout.setRefreshing(false);
    }

    private MyHandler myHandler = new MyHandler(FragmentSwitcherOrder.this);

    private static class MyHandler extends Handler {

        private WeakReference<FragmentSwitcherOrder> fragment = null;

        MyHandler(FragmentSwitcherOrder ref) {
            fragment = new WeakReference<>(ref);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x01) {
                fragment.get().requestMyOrdersList();
            }
        }
    }
}
