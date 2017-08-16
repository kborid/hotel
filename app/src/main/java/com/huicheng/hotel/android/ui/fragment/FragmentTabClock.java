package com.huicheng.hotel.android.ui.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.net.bean.HotelInfoBean;
import com.huicheng.hotel.android.net.bean.HotelMapInfoBean;
import com.huicheng.hotel.android.ui.activity.HotelListActivity;
import com.huicheng.hotel.android.ui.activity.RoomListActivity;
import com.huicheng.hotel.android.ui.adapter.HotelListAdapter;
import com.huicheng.hotel.android.ui.base.BaseFragment;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.CustomToast;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kborid
 * @date 2017/2/23 0023
 */
public class FragmentTabClock extends BaseFragment implements DataCallback, HotelListActivity.OnUpdateHotelInfoListener {
    private static final String TAG = "FragmentTagClock";
    public static boolean isFirstLoad = false;
    private String key = null;
    private HotelListAdapter adapter = null;
    private List<HotelInfoBean> list = new ArrayList<>();
    private RecyclerView recyclerView;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    private int lastVisibleItem = 0;
    private boolean isNoMore = false;

    private static final int PAGESIZE = 10;
    private int pageIndex = 0, priceIndex = 0;
    private String keyword = "";
    private String star, priceStart, priceEnd, type;
    private String[] point = new String[]{"", ""};
    private int refreshType = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        isFirstLoad = true;
        key = getArguments().getString("key");
        keyword = getArguments().getString("keyword");
        priceIndex = getArguments().getInt("priceIndex");
        View view = inflater.inflate(R.layout.fragment_tab_clock, container, false);
        initTypedArrayValue();
        initViews(view);
        initParams();
        initListeners();
        return view;
    }

    public static Fragment newInstance(String key, String keyword, int priceIndex) {
        Fragment fragment = new FragmentTabClock();
        Bundle bundle = new Bundle();
        bundle.putString("key", key);
        bundle.putString("keyword", keyword);
        bundle.putInt("priceIndex", priceIndex);
        fragment.setArguments(bundle);
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
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        staggeredGridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        adapter = new HotelListAdapter(getActivity(), list, HotelCommDef.TYPE_CLOCK);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void initParams() {
        super.initParams();
        swipeRefreshLayout.setColorSchemeResources(mSwipeRefreshColor);
        swipeRefreshLayout.setDistanceToTriggerSync(200);
        swipeRefreshLayout.setProgressViewOffset(true, 0, Utils.dip2px(20));
        swipeRefreshLayout.setSize(SwipeRefreshLayout.DEFAULT);

        if (priceIndex != 0) {
            float priceMin = SharedPreferenceUtil.getInstance().getFloat(AppConst.RANGE_MIN, 0f);
            float priceMax = SharedPreferenceUtil.getInstance().getFloat(AppConst.RANGE_MAX, 6f);
            priceStart = HotelCommDef.convertHotelPrice((int) priceMin);
            priceEnd = HotelCommDef.convertHotelPrice((int) priceMax);
        }
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
                staggeredGridLayoutManager.invalidateSpanAssignments();
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

        adapter.setOnItemClickListener(new HotelListAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position) {
                HotelOrderManager.getInstance().setHotelType(HotelCommDef.TYPE_CLOCK);
                Intent intent = new Intent(getActivity(), RoomListActivity.class);
                intent.putExtra("key", key);
                intent.putExtra("hotelId", list.get(position).hotelId);
                startActivity(intent);
            }
        });
    }

    private int getLastVisibleItem() {
        int[] lastPositions = staggeredGridLayoutManager.findLastVisibleItemPositions(new int[staggeredGridLayoutManager.getSpanCount()]);
        return getMaxPosition(lastPositions);
    }

    private int getMaxPosition(int[] positions) {
        int size = positions.length;
        int maxPosition = Integer.MIN_VALUE;
        for (int i = 0; i < size; i++) {
            maxPosition = Math.max(maxPosition, positions[i]);
        }
        return maxPosition;
    }

    private void requestHotelClockList(int pageIndex) {
        LogUtil.i(TAG, "requestHotelClockList() pageIndex = " + pageIndex);
        String cityCode = SharedPreferenceUtil.getInstance().getString(AppConst.SITEID, "", false);
        String beginDate = String.valueOf(HotelOrderManager.getInstance().getBeginTime());
        String endDate = String.valueOf(HotelOrderManager.getInstance().getEndTime());

        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("cityCode", cityCode);
        //星级
        b.addBody("star", star);
        //日期
        b.addBody("beginDate", beginDate);
        b.addBody("endDate", endDate);
        //评分
        b.addBody("gradeStart", point[0]);
        b.addBody("gradeEnd", point[1]);
        //价钱范围
        b.addBody("priceStart", priceStart);
        b.addBody("priceEnd", priceEnd);

        b.addBody("category", String.valueOf(HotelCommDef.TYPE_CLOCK));
        b.addBody("type", type);
        b.addBody("keyword", keyword);

        b.addBody("pageIndex", String.valueOf(pageIndex));
        b.addBody("pageSize", String.valueOf(PAGESIZE));

        b.addBody("longitude", SharedPreferenceUtil.getInstance().getString(AppConst.LOCATION_LON, "", false));
        b.addBody("latitude", SharedPreferenceUtil.getInstance().getString(AppConst.LOCATION_LAT, "", false));

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
            if (isNoMore) {
                CustomToast.show("没有更多数据", CustomToast.LENGTH_SHORT);
            }
        }
    }

    @Override
    public void onUpdate(String keyword) {
        LogUtil.i(TAG, "Clock onUpdate() keyword = " + keyword);
        star = HotelCommDef.convertHotelGrade(SharedPreferenceUtil.getInstance().getInt(AppConst.CONSIDER_GRADE, -1));
        point = HotelCommDef.convertHotelPoint(SharedPreferenceUtil.getInstance().getInt(AppConst.CONSIDER_POINT, -1));
        float priceMin = SharedPreferenceUtil.getInstance().getFloat(AppConst.RANGE_MIN, 0f);
        float priceMax = SharedPreferenceUtil.getInstance().getFloat(AppConst.RANGE_MAX, 6f);
        priceStart = HotelCommDef.convertHotelPrice((int) priceMin);
        priceEnd = HotelCommDef.convertHotelPrice((int) priceMax);
        type = HotelCommDef.convertHotelType(SharedPreferenceUtil.getInstance().getInt(AppConst.CONSIDER_TYPE, -1));
        isFirstLoad = false;
        this.keyword = keyword;
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
