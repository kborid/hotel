package com.huicheng.hotel.android.ui.fragment;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
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
import com.huicheng.hotel.android.ui.base.BaseFragment;
import com.huicheng.hotel.android.ui.custom.RoundedTopImageView;
import com.huicheng.hotel.android.ui.mapoverlay.AMapUtil;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
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
    private HotelDataAdapter adapter = null;
    private List<HotelInfoBean> list = new ArrayList<>();
    private TextView tv_note;

    private static final int PAGESIZE = 10;
    private int pageIndex = 0;
    private String keyword = "";
    private String star, priceStart, priceEnd, type;
    private String[] point = new String[]{"", ""};
    private int refreshType = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        isFirstLoad = true;
        key = getArguments().getString("key");
        keyword = getArguments().getString("keyword");
        View view = inflater.inflate(R.layout.fragment_tab_ygr, container, false);
        initViews(view);
        initParams();
        initListeners();
        return view;
    }

    public static Fragment newInstance(String key, String keyword) {
        Fragment fragment = new FragmentTabYeGuiRen();
        Bundle bundle = new Bundle();
        bundle.putString("key", key);
        bundle.putString("keyword", keyword);
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
        adapter = new HotelDataAdapter(getActivity(), list);
        pullLoadMoreRecyclerView.setAdapter(adapter);
        tv_note = (TextView) view.findViewById(R.id.tv_note);
    }

    @Override
    protected void initParams() {
        super.initParams();
        pullLoadMoreRecyclerView.setColorSchemeResources(R.color.mainColor);
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
        adapter.setOnItemClickListener(new HotelDataAdapter.OnItemClickListener() {
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
        float priceMax = SharedPreferenceUtil.getInstance().getFloat(AppConst.RANGE_MAX, 5f);
        priceStart = HotelCommDef.convertHotelPrice((int) priceMin);
        priceEnd = HotelCommDef.convertHotelPrice((int) priceMax);
        type = HotelCommDef.convertHotelType(SharedPreferenceUtil.getInstance().getInt(AppConst.CONSIDER_TYPE, -1));
        isFirstLoad = false;
        this.keyword = keyword;
        refreshType = 0;
        requestHotelYGRList(pageIndex);
    }

    private static class HotelDataAdapter extends RecyclerView.Adapter<HotelDataAdapter.HotelViewHolder> {
        private static final String TAG = "HotelDataAdapter";

        private Context context;
        private List<HotelInfoBean> list;

        public HotelDataAdapter(Context context, List<HotelInfoBean> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public HotelViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            HotelViewHolder hotelViewHolder = new HotelViewHolder(LayoutInflater.from(context).inflate(R.layout.lv_hotel_item, parent, false));
            hotelViewHolder.setIsRecyclable(true);
            return hotelViewHolder;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onBindViewHolder(final HotelViewHolder holder, final int position) {

            HotelInfoBean bean = list.get(position);
            loadImage(holder.iv_hotel_icon, R.drawable.def_room_list, bean.hotelFeaturePic, 345, 250);
            if (HotelCommDef.IS_VIP.equals(bean.vipEnable) && bean.vipPrice > 0) {
                holder.vip_layout.setVisibility(View.VISIBLE);
                holder.tv_vip_price.setText(bean.vipPrice + "");
            } else {
                holder.vip_layout.setVisibility(View.GONE);
            }
            holder.content_layout.setBackgroundResource(R.drawable.comm_gradient_ygr_color);
            holder.tv_hotel_name.setTextColor(context.getResources().getColor(R.color.white));
            holder.tv_hotel_dis.setTextColor(context.getResources().getColor(R.color.white));
            holder.tv_hotel_price.setTextColor(context.getResources().getColor(R.color.white));
            holder.tv_hotel_special_price_note.setTextColor(context.getResources().getColor(R.color.white));
            holder.tv_hotel_special_price.setTextColor(context.getResources().getColor(R.color.white));

            LatLng start = null, des = null;
            float lon = Float.parseFloat(SharedPreferenceUtil.getInstance().getString(AppConst.LOCATION_LON, "0", false));
            float lat = Float.parseFloat(SharedPreferenceUtil.getInstance().getString(AppConst.LOCATION_LAT, "0", false));
            if (lon != 0 && lat != 0 && StringUtil.notEmpty(bean.hotelCoordinate)) {
                start = new LatLng(lat, lon);
                String[] pos = bean.hotelCoordinate.split("\\|");
                des = new LatLng(Float.valueOf(pos[0]), Float.valueOf(pos[1]));
                float dis = AMapUtils.calculateLineDistance(start, des);
                holder.tv_hotel_dis.setText(AMapUtil.getFriendlyLength((int) dis));
                holder.tv_hotel_dis.setVisibility(View.VISIBLE);
            } else {
                holder.tv_hotel_dis.setVisibility(View.GONE);
            }

            float point = 0;
            if (StringUtil.notEmpty(bean.hotelGrade)) {
                point = Float.parseFloat(bean.hotelGrade);
                holder.tv_hotel_point.setText(String.valueOf(point));
            } else {
                holder.tv_hotel_point.setText("0.0");
            }
            if (point >= 4) {
                holder.tv_hotel_point.setBackground(context.getResources().getDrawable(R.drawable.comm_rectangle_btn_assess_high));
            } else if (point >= 3) {
                holder.tv_hotel_point.setBackground(context.getResources().getDrawable(R.drawable.comm_rectangle_btn_assess_mid));
            } else {
                holder.tv_hotel_point.setBackground(context.getResources().getDrawable(R.drawable.comm_rectangle_btn_assess_low));
            }
            holder.tv_hotel_name.setText(bean.hotelName);

            if (bean.speciallyPrice > 0) {
                if (bean.price <= bean.speciallyPrice) {
                    holder.tv_hotel_price.setVisibility(View.GONE);
                } else {
                    holder.tv_hotel_price.setVisibility(View.VISIBLE);
                    holder.tv_hotel_price.setText(bean.price + "元起");
                }
                holder.tv_hotel_special_price_note.setText("特价：");
                holder.tv_hotel_special_price.setText(bean.speciallyPrice + " 元");
            } else {
                holder.tv_hotel_price.setVisibility(View.GONE);
                holder.tv_hotel_special_price_note.setText("价格：");
                holder.tv_hotel_special_price.setText((bean.price <= 0) ? "暂无" : bean.price + " 元起");
            }

            holder.cardview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != listener) {
                        listener.OnItemClick(v, position);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class HotelViewHolder extends RecyclerView.ViewHolder {

            CardView cardview;
            LinearLayout content_layout;
            LinearLayout vip_layout;
            TextView tv_vip_price;
            RoundedTopImageView iv_hotel_icon;
            TextView tv_hotel_point;
            TextView tv_hotel_name;
            TextView tv_hotel_dis;
            TextView tv_hotel_price;
            TextView tv_hotel_special_price_note;
            TextView tv_hotel_special_price;

            public HotelViewHolder(View itemView) {
                super(itemView);
                cardview = (CardView) itemView.findViewById(R.id.cardview);
                content_layout = (LinearLayout) itemView.findViewById(R.id.content_layout);
                vip_layout = (LinearLayout) itemView.findViewById(R.id.vip_layout);
                ((TextView) itemView.findViewById(R.id.tv_vip_price_note)).getPaint().setFakeBoldText(true);
                tv_vip_price = (TextView) itemView.findViewById(R.id.tv_vip_price);
                ((TextView) itemView.findViewById(R.id.tv_vip_price_unit)).getPaint().setFakeBoldText(true);
                iv_hotel_icon = (RoundedTopImageView) itemView.findViewById(R.id.iv_hotel_icon);
                tv_hotel_point = (TextView) itemView.findViewById(R.id.tv_point);
                tv_hotel_name = (TextView) itemView.findViewById(R.id.tv_hotel_name);
                tv_hotel_name.getPaint().setFakeBoldText(true);
                tv_hotel_dis = (TextView) itemView.findViewById(R.id.tv_hotel_dis);
                tv_hotel_price = (TextView) itemView.findViewById(R.id.tv_hotel_price);
                tv_hotel_price.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                tv_hotel_special_price_note = (TextView) itemView.findViewById(R.id.tv_hotel_special_price_note);
                tv_hotel_special_price_note.getPaint().setFakeBoldText(true);
                tv_hotel_special_price = (TextView) itemView.findViewById(R.id.tv_hotel_special_price);
                tv_hotel_special_price.getPaint().setFakeBoldText(true);
            }
        }

        public interface OnItemClickListener {
            void OnItemClick(View view, int position);
        }

        private OnItemClickListener listener = null;

        public void setOnItemClickListener(OnItemClickListener listener) {
            this.listener = listener;
        }
    }
}
