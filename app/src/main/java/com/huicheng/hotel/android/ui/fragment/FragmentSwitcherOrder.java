package com.huicheng.hotel.android.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.requestbuilder.bean.OrderDetailInfoBean;
import com.huicheng.hotel.android.ui.adapter.MainOrderAdapter;
import com.huicheng.hotel.android.ui.base.BaseFragment;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kborid
 * @date 2018/2/9 0009.
 */

public class FragmentSwitcherOrder extends BaseFragment implements View.OnClickListener, DataCallback {
    private Bundle bundle = null;
    public static boolean isFirstLoad = false;

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
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    requestUserOrders();
                }
            }, 500);
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
        //test
        for (int i = 0; i < 10; i++) {
            OrderDetailInfoBean bean = new OrderDetailInfoBean();
            bean.type = i % 2;
            list.add(bean);
        }
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
                requestUserOrders();
            }
        });
    }

    @Override
    public void onClick(View v) {
    }

    private void requestUserOrders() {
        LogUtil.i(TAG, "requestUserOrders()");
//        RequestBeanBuilder b = RequestBeanBuilder.create(true);
//        ResponseData d = b.syncRequest(b);
//        DataLoader.getInstance().loadData(this, d);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 2000);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.i(TAG, "onActivityResult() " + requestCode + ", " + resultCode);
    }

    @Override
    public void preExecute(ResponseData request) {

    }

    @Override
    public void notifyMessage(ResponseData request, ResponseData response) throws Exception {

    }

    @Override
    public void notifyError(ResponseData request, ResponseData response, Exception e) {

    }
}
