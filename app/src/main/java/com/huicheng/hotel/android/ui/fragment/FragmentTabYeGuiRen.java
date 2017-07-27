package com.huicheng.hotel.android.ui.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.widget.CustomToast;
import com.wuxiaolong.pullloadmorerecyclerview.PullLoadMoreRecyclerView;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author kborid
 * @date 2017/2/23 0023
 */
public class FragmentTabYeGuiRen extends BaseFragment implements DataCallback, HotelListActivity.OnUpdateHotelInfoListener {
    private static final String TAG = "FragmentTagYGR";
    public static boolean isFirstLoad = false;
    private String key = null;
    private PullLoadMoreRecyclerView pullLoadMoreRecyclerView;
    private HotelListAdapter adapter = null;
    private List<HotelInfoBean> list = new ArrayList<>();
    private TextView tv_note;

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
        View view = inflater.inflate(R.layout.fragment_tab_ygr, container, false);
        initViews(view);
        initParams();
        initListeners();
        return view;
    }

    public static Fragment newInstance(String key, String keyword, int priceIndex) {
        Fragment fragment = new FragmentTabYeGuiRen();
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
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (DateUtil.getGapCount(HotelOrderManager.getInstance().getBeginDate(), HotelOrderManager.getInstance().getEndDate()) != 1
                            || DateUtil.getGapCount(new Date(System.currentTimeMillis()), HotelOrderManager.getInstance().getBeginDate()) != 0) {
                        tv_note.setVisibility(View.VISIBLE);
                        tv_note.setText(getResources().getString(R.string.ygrNote, DateUtil.getDay("M.d", System.currentTimeMillis()) + DateUtil.dateToWeek2(new Date(System.currentTimeMillis()))));
                    }
                    pullLoadMoreRecyclerView.setRefreshing(true);
                    requestHotelYGRList(pageIndex);
                }
            }, 500);
        }
    }

    @Override
    protected void onInvisible() {
        super.onInvisible();
        pullLoadMoreRecyclerView.setPullLoadMoreCompleted();
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        pullLoadMoreRecyclerView = (PullLoadMoreRecyclerView) view.findViewById(R.id.pullLoadMoreRecyclerView);
        pullLoadMoreRecyclerView.setStaggeredGridLayout(2);
        adapter = new HotelListAdapter(getActivity(), list, HotelCommDef.TYPE_YEGUIREN);
        pullLoadMoreRecyclerView.setAdapter(adapter);
        tv_note = (TextView) view.findViewById(R.id.tv_note);
    }

    @Override
    protected void initParams() {
        super.initParams();
        pullLoadMoreRecyclerView.setColorSchemeResources(mSwipeRefreshColorId);
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
        pullLoadMoreRecyclerView.setOnPullLoadMoreListener(new PullLoadMoreRecyclerView.PullLoadMoreListener() {
            @Override
            public void onRefresh() {
                pageIndex = 0;
                refreshType = 0;
                requestHotelYGRList(pageIndex);
            }

            @Override
            public void onLoadMore() {
                refreshType = 1;
                requestHotelYGRList(++pageIndex);
            }
        });
        adapter.setOnItemClickListener(new HotelListAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position) {
                HotelOrderManager.getInstance().setHotelType(HotelCommDef.TYPE_YEGUIREN);
                Intent intent = new Intent(getActivity(), RoomListActivity.class);
                intent.putExtra("key", key);
                intent.putExtra("hotelId", list.get(position).hotelId);
                startActivity(intent);
            }
        });
    }

    private void requestHotelYGRList(int pageIndex) {
        LogUtil.i(TAG, "requestHotelYGRList() pageIndex = " + pageIndex);

        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("cityCode", SharedPreferenceUtil.getInstance().getString(AppConst.SITEID, "", false));
        //星级
        b.addBody("star", star);
        //日期
        b.addBody("beginDate", String.valueOf(HotelOrderManager.getInstance().getBeginTime(true)));
        b.addBody("endDate", String.valueOf(HotelOrderManager.getInstance().getEndTime(true)));
        //评分
        b.addBody("gradeStart", point[0]);
        b.addBody("gradeEnd", point[1]);
        //价钱范围
        b.addBody("priceStart", priceStart);
        b.addBody("priceEnd", priceEnd);

        b.addBody("category", String.valueOf(HotelCommDef.TYPE_YEGUIREN));
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
    public void onResume() {
        super.onResume();
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
        pullLoadMoreRecyclerView.setPullLoadMoreCompleted();
        String message;
        if (e != null && e instanceof ConnectException) {
            message = getString(R.string.dialog_tip_net_error);
        } else {
            message = response != null && response.data != null ? response.data.toString() : getString(R.string.dialog_tip_null_error);
        }
        CustomToast.show(message, CustomToast.LENGTH_SHORT);
    }

    private class MyAsyncTask extends AsyncTask<Integer, Void, Void> {
        private boolean isNoMore = false;
        private ResponseData response = null;

        public MyAsyncTask(ResponseData response) {
            this.response = response;
        }

        @Override
        protected Void doInBackground(Integer... params) {
            List<HotelInfoBean> temp = JSON.parseArray(response.body.toString(), HotelInfoBean.class);
            if (params[0] == 0) {
                list.clear();
            }
            list.addAll(temp);
            isNoMore = temp.size() <= 0;

            List<HotelMapInfoBean> clockList = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                HotelMapInfoBean bean = new HotelMapInfoBean();
                bean.coordinate = list.get(i).hotelCoordinate;
                bean.hotelAddress = list.get(i).hotelAddress;
                bean.hotelName = list.get(i).hotelName;
                bean.hotelIcon = list.get(i).hotelFeaturePic;
                bean.hotelId = list.get(i).hotelId;
                clockList.add(bean);
            }
            SessionContext.setYgrList(clockList);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter.notifyDataSetChanged();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    pullLoadMoreRecyclerView.setPullLoadMoreCompleted();
                }
            }, 300);
            if (isNoMore) {
                CustomToast.show("没有更多数据", CustomToast.LENGTH_SHORT);
            }
        }
    }

    @Override
    public void onUpdate(String keyword) {
        LogUtil.i(TAG, "YeGuiRen onUpdate() keyword = " + keyword);
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
        requestHotelYGRList(pageIndex);
    }
}
