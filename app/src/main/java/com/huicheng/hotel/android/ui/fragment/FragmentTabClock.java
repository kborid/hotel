package com.huicheng.hotel.android.ui.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.content.AppConst;
import com.huicheng.hotel.android.content.NetURL;
import com.huicheng.hotel.android.control.LocationInfo;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.HotelInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.HotelMapInfoBean;
import com.huicheng.hotel.android.ui.activity.hotel.HotelDetailActivity;
import com.huicheng.hotel.android.ui.activity.hotel.HotelListActivity;
import com.huicheng.hotel.android.ui.adapter.HotelListAdapter;
import com.huicheng.hotel.android.ui.base.BaseFragment;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.huicheng.hotel.android.ui.listener.OnRecycleViewItemClickListener;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kborid
 * @date 2017/2/23 0023
 */
public class FragmentTabClock extends BaseFragment implements DataCallback, HotelListActivity.OnUpdateHotelInfoListener {
    public static boolean isFirstLoad = false;
    private HotelListAdapter adapter = null;
    private List<HotelInfoBean> list = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    private int lastVisibleItem = 0;

    private boolean isNoMore = false;
    private RelativeLayout empty_lay;

    private static final int PAGESIZE = 10;
    private int pageIndex = 0;
    private int refreshType = 0;

    private String key = null;
    private Bundle bundle = null;
    private int pointIndex, gradeIndex, priceIndex, typeIndex;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        isFirstLoad = true;
        key = HotelCommDef.CLOCK;
        bundle = getArguments();
        View view = inflater.inflate(R.layout.fragment_tab_clock, container, false);
        initTypedArrayValue();
        initViews(view);
        initParams();
        initListeners();
        return view;
    }

    public static Fragment newInstance(Bundle searchParams) {
        Fragment fragment = new FragmentTabClock();
        fragment.setArguments(searchParams);
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
                    requestHotelClockList(pageIndex);
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
        layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        ((StaggeredGridLayoutManager) layoutManager).setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new HotelListAdapter(getActivity(), list, HotelCommDef.TYPE_CLOCK);
        adapter.setLandMarkLonLat(bundle.getString("landmark"), bundle.getString("lonLat"));
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

        //初始化筛选条件
        pointIndex = SharedPreferenceUtil.getInstance().getInt(AppConst.CONSIDER_POINT, -1);
        gradeIndex = SharedPreferenceUtil.getInstance().getInt(AppConst.CONSIDER_GRADE, -1);
        priceIndex = SharedPreferenceUtil.getInstance().getInt(AppConst.CONSIDER_PRICE, -1);
        typeIndex = SharedPreferenceUtil.getInstance().getInt(AppConst.CONSIDER_TYPE, -1);
    }

    @Override
    protected void initListeners() {
        super.initListeners();
        HotelListActivity.registerOnUpdateHotelInfoListener(this);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pageIndex = 0;
                refreshType = 0;
                requestHotelClockList(pageIndex);
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                ((StaggeredGridLayoutManager) layoutManager).invalidateSpanAssignments();
                if (!isNoMore) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == adapter.getItemCount()) {
                        swipeRefreshLayout.setRefreshing(true);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                refreshType = 1;
                                requestHotelClockList(++pageIndex);
                            }
                        }, 500);
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = getLastVisibleItem();
            }
        });

        adapter.setOnRecycleViewItemClickListener(new OnRecycleViewItemClickListener() {
            @Override
            public void OnItemClick(View view, int position) {
                HotelOrderManager.getInstance().setHotelType(HotelCommDef.TYPE_CLOCK);
                Intent intent = new Intent(getActivity(), HotelDetailActivity.class);
                intent.putExtra("key", key);
                intent.putExtra("hotelId", list.get(position).hotelId);
                startActivity(intent);
            }
        });
    }

    private int getLastVisibleItem() {
        int lastPosition = 0;
        if (layoutManager instanceof GridLayoutManager) {
            lastPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
        } else if (layoutManager instanceof LinearLayoutManager) {
            lastPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int[] lastPositions = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
            ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(lastPositions);
            lastPosition = getMaxPosition(lastPositions);
        }
        return lastPosition;
    }

    private int getMaxPosition(int[] positions) {
        int size = positions.length;
        int maxPosition = Integer.MIN_VALUE;
        for (int position : positions) {
            maxPosition = Math.max(maxPosition, position);
        }
        return maxPosition;
    }

    private void requestHotelClockList(int pageIndex) {
        LogUtil.i(TAG, "requestHotelClockList()");
        LogUtil.i(TAG, "pageIndex = " + pageIndex);
        LogUtil.i(TAG, "keyword = " + bundle.getString("keyword"));
        String star = HotelCommDef.convertConsiderGrade(gradeIndex);
        LogUtil.i(TAG, "star = " + star);
        String[] point = HotelCommDef.convertConsiderPoint(pointIndex);
        LogUtil.i(TAG, "point = " + point[0] + " " + point[1]);
        String[] price = HotelCommDef.convertConsiderPrice(priceIndex);
        LogUtil.i(TAG, "price = " + price[0] + " " + price[1]);
        String type = HotelCommDef.convertConsiderType(typeIndex);
        LogUtil.i(TAG, "type = " + type);
        int orderType = SharedPreferenceUtil.getInstance().getInt(AppConst.SORT_INDEX, 0);
        orderType = orderType == 0 ? orderType : 2;
        LogUtil.i(TAG, "orderType = " + orderType);

        RequestBeanBuilder b = RequestBeanBuilder.create(SessionContext.isLogin());
        //关键字
        b.addBody("keyword", bundle.getString("keyword"));
        //星级
        b.addBody("star", star);
        //评分
        b.addBody("gradeStart", point[0]);
        b.addBody("gradeEnd", point[1]);
        //价钱范围
        b.addBody("priceStart", price[0]);
        b.addBody("priceEnd", price[1]);
        //酒店类型
        b.addBody("type", type);
        //排序类型
        b.addBody("orderType", String.valueOf(orderType));

        b.addBody("showAd", "1");//用于后台区分旧版本不支持广告模块的flag
        b.addBody("beginDate", String.valueOf(HotelOrderManager.getInstance().getBeginTime()));
        b.addBody("endDate", String.valueOf(HotelOrderManager.getInstance().getEndTime()));
        b.addBody("cityCode", LocationInfo.instance.getCityCode());
        b.addBody("category", String.valueOf(HotelCommDef.TYPE_CLOCK));
        b.addBody("pageIndex", String.valueOf(pageIndex));
        b.addBody("pageSize", String.valueOf(PAGESIZE));
        b.addBody("longitude", LocationInfo.instance.getLon());
        b.addBody("latitude", LocationInfo.instance.getLat());

        //地标信息
        String searchType = HotelCommDef.TYPE_HOTEL;
        if (bundle.getBoolean("isLandMark")) {
            searchType = HotelCommDef.TYPE_LAND_MARK;
            b.addBody("landmark", bundle.getString("landmark"));
            b.addBody("cityCode", bundle.getString("siteId"));
            String lonLat = bundle.getString("lonLat");
            if (StringUtil.isEmpty(lonLat)) {
                b.addBody("longitude", LocationInfo.instance.getLon());
                b.addBody("latitude", LocationInfo.instance.getLat());
            } else {
                String[] pos = lonLat.split("\\|");
                b.addBody("longitude", pos[1]);
                b.addBody("latitude", pos[0]);
            }
        }
        b.addBody("searchType", searchType);
        LogUtil.i(TAG, "searchType = " + searchType);
        LogUtil.i(TAG, "isLandMark = " + bundle.getBoolean("isLandMark"));
        LogUtil.i(TAG, "landmark = " + bundle.getString("landmark"));
        LogUtil.i(TAG, "siteId = " + bundle.getString("siteId"));

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.HOTEL_LIST;
        d.flag = AppConst.HOTEL_LIST;

        DataLoader.getInstance().loadData(this, d);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        HotelListActivity.unRegisterOnUpdateHotelInfoListener(this);
    }

    @Override
    public void preExecute(ResponseData request) {
    }

    @Override
    public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
        if (response != null && response.body != null) {
            synchronized (MyAsyncTask.class) {
                new MyAsyncTask(response).execute(refreshType);
            }
        }
    }

    @Override
    public void notifyError(ResponseData request, ResponseData response, Exception e) {
        removeProgressDialog();
        String message = response != null && response.data != null ? response.data.toString() : getString(R.string.dialog_tip_null_error);
        if (e != null && e instanceof ConnectException) {
            message = getString(R.string.dialog_tip_net_error);
        }
        CustomToast.show(message, CustomToast.LENGTH_SHORT);
        SessionContext.setClockList(null);
        swipeRefreshLayout.setRefreshing(false);
        if (list.size() <= 0) {
            empty_lay.setVisibility(View.VISIBLE);
        } else {
            empty_lay.setVisibility(View.GONE);
        }
    }

    private class MyAsyncTask extends AsyncTask<Integer, Void, Void> {
        private ResponseData response = null;

        MyAsyncTask(ResponseData response) {
            this.response = response;
        }

        @Override
        protected Void doInBackground(Integer... params) {
            LogUtil.i(TAG, "json = " + response.body.toString());
            if (response != null && response.body != null && !"{}".equals(response.body.toString())) {
                List<HotelInfoBean> temp = JSON.parseArray(response.body.toString(), HotelInfoBean.class);
                if (params[0] == 0) {
                    list.clear();
                }
                list.addAll(temp);

                isNoMore = temp.size() < PAGESIZE;

                //设置缓存
                List<HotelMapInfoBean> clockList = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    HotelMapInfoBean bean = new HotelMapInfoBean();
                    bean.coordinate = list.get(i).hotelCoordinate;
                    bean.hotelAddress = list.get(i).hotelAddress;
                    bean.hotelName = list.get(i).hotelName;
                    bean.hotelIcon = list.get(i).hotelFeaturePic;
                    bean.hotelId = list.get(i).hotelId;
                    bean.price = list.get(i).clockPrice;
                    clockList.add(bean);
                }
                SessionContext.setClockList(clockList);
            } else {
                isNoMore = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }, 300);
            adapter.notifyDataSetChanged();
            if (list.size() <= 0) {
                empty_lay.setVisibility(View.VISIBLE);
            } else {
                empty_lay.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onUpdate(String keyword) {
        LogUtil.i(TAG, "Clock onUpdate() keyword = " + keyword);
        pointIndex = SharedPreferenceUtil.getInstance().getInt(AppConst.CONSIDER_POINT, -1);
        gradeIndex = SharedPreferenceUtil.getInstance().getInt(AppConst.CONSIDER_GRADE, -1);
        priceIndex = SharedPreferenceUtil.getInstance().getInt(AppConst.CONSIDER_PRICE, -1);
        typeIndex = SharedPreferenceUtil.getInstance().getInt(AppConst.CONSIDER_TYPE, -1);
        isFirstLoad = false;
        bundle.putString("keyword",keyword);
        refreshType = 0;
        if (getUserVisibleHint()) {
            swipeRefreshLayout.setRefreshing(true);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    requestHotelClockList(pageIndex);
                }
            }, 500);
        } else {
            isFirstLoad = true;
        }
    }
}
