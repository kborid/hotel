package com.huicheng.hotel.android.ui.fragment;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.net.bean.HotelMapInfoBean;
import com.huicheng.hotel.android.net.bean.HouHuiYaoInfoBean;
import com.huicheng.hotel.android.ui.activity.HotelListActivity;
import com.huicheng.hotel.android.ui.activity.HouHuiYaoOrderDetailActivity;
import com.huicheng.hotel.android.ui.base.BaseFragment;
import com.huicheng.hotel.android.ui.custom.MyListViewWidget;
import com.huicheng.hotel.android.ui.custom.RoundedLeftImageView;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.huicheng.hotel.android.ui.glide.CustomReqURLFormatModelImpl;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kborid
 * @date 2016/11/7 0007
 */
public class FragmentTabHouHuiYao extends BaseFragment implements DataCallback, HotelListActivity.OnUpdateHotelInfoListener {

    private static boolean isFirstLoad = false;

    private LinearLayout recommend_lay, other_lay;
    private TextView tv_recommend_title, tv_nodata;
    private MyListViewWidget lv_recommend, lv_other;
    private HouHuiYaoInfoBean houHuiYaoInfoBean = null;
    private List<HouHuiYaoInfoBean.HouHuiYaoBean> recommendList = new ArrayList<>();
    private List<HouHuiYaoInfoBean.HouHuiYaoBean> otherList = new ArrayList<>();
    private MyHouHuiYaoAdapter recommendAdapter, otherAdapter;

    private int pageIndex = 0;
    private static final int PAGESIZE = 10;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        isFirstLoad = true;
        getArguments().getString("key");
        View view = inflater.inflate(R.layout.fragment_tab_houhuiyao, container, false);
        initViews(view);
        initParams();
        initListeners();
        return view;
    }

    public static Fragment newInstance(String key) {
        Fragment fragment = new FragmentTabHouHuiYao();
        Bundle bundle = new Bundle();
        bundle.putString("key", key);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void onVisible() {
        super.onVisible();
        if (isFirstLoad) {
            isFirstLoad = false;
            requestHouHuiYaoOrderList("");
        }
    }

    @Override
    protected void onInvisible() {
        super.onInvisible();
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        tv_nodata = (TextView) view.findViewById(R.id.tv_nodata);
        recommend_lay = (LinearLayout) view.findViewById(R.id.recommend_lay);
        tv_recommend_title = (TextView) view.findViewById(R.id.tv_recommend_title);
        lv_recommend = (MyListViewWidget) view.findViewById(R.id.lv_recommend);
        other_lay = (LinearLayout) view.findViewById(R.id.other_lay);
        lv_other = (MyListViewWidget) view.findViewById(R.id.lv_other);
    }

    @Override
    protected void initParams() {
        super.initParams();
        recommendAdapter = new MyHouHuiYaoAdapter(getActivity(), recommendList);
        lv_recommend.setAdapter(recommendAdapter);
        otherAdapter = new MyHouHuiYaoAdapter(getActivity(), otherList);
        lv_other.setAdapter(otherAdapter);
    }

    private void requestHouHuiYaoOrderList(String keyword) {
        String cityCode = SharedPreferenceUtil.getInstance().getString(AppConst.SITEID, "", false);
        String beginDate = String.valueOf(HotelOrderManager.getInstance().getBeginTime());
        String endDate = String.valueOf(HotelOrderManager.getInstance().getEndTime());

        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("cityCode", cityCode);
        b.addBody("beginDate", beginDate);
        b.addBody("endDate", endDate);
        b.addBody("pageIndex", String.valueOf(0));
        b.addBody("pageSize", String.valueOf(PAGESIZE));
        b.addBody("keyword", keyword);

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.HHY_LIST;
        d.flag = AppConst.HHY_LIST;

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void refreshHouHuiYaoList() {
        if (houHuiYaoInfoBean != null) {
            List<HotelMapInfoBean> hhyList = new ArrayList<>();
            if (houHuiYaoInfoBean.targetlist != null && houHuiYaoInfoBean.targetlist.size() > 0) {
                recommendList.clear();
                recommendList.addAll(houHuiYaoInfoBean.targetlist);
                recommendAdapter.notifyDataSetChanged();
                for (int i = 0; i < recommendList.size(); i++) {
                    HotelMapInfoBean bean = new HotelMapInfoBean();
                    bean.coordinate = recommendList.get(i).coordinate;
                    bean.hotelAddress = recommendList.get(i).address;
                    bean.hotelName = recommendList.get(i).hotelname;
                    bean.hotelIcon = recommendList.get(i).picpath;
                    bean.hotelId = recommendList.get(i).houtelid;
                    hhyList.add(bean);
                }
            }
            if (houHuiYaoInfoBean.otherlist != null && houHuiYaoInfoBean.otherlist.size() > 0) {
                otherList.clear();
                otherList.addAll(houHuiYaoInfoBean.otherlist);
                otherAdapter.notifyDataSetChanged();
                for (int i = 0; i < otherList.size(); i++) {
                    HotelMapInfoBean bean = new HotelMapInfoBean();
                    bean.coordinate = otherList.get(i).coordinate;
                    bean.hotelAddress = otherList.get(i).address;
                    bean.hotelName = otherList.get(i).hotelname;
                    bean.hotelIcon = otherList.get(i).picpath;
                    bean.hotelId = otherList.get(i).houtelid;
                    hhyList.add(bean);
                }
            }

            if (recommendList.size() == 0 && otherList.size() == 0) {
                tv_nodata.setVisibility(View.VISIBLE);
                recommend_lay.setVisibility(View.GONE);
                other_lay.setVisibility(View.GONE);
            } else {
                tv_nodata.setVisibility(View.GONE);
                if (recommendList.size() != 0) {
                    recommend_lay.setVisibility(View.VISIBLE);
                } else {
                    recommend_lay.setVisibility(View.GONE);
                }

                if (otherList.size() != 0) {
                    other_lay.setVisibility(View.VISIBLE);
                } else {
                    other_lay.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    protected void initListeners() {
        super.initListeners();
        HotelListActivity.registerOnUpdateHotelInfoListener(this);
        lv_recommend.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), HouHuiYaoOrderDetailActivity.class);
                intent.putExtra("regretOrderid", recommendList.get(position).id);
                startActivity(intent);
            }
        });

        lv_other.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), HouHuiYaoOrderDetailActivity.class);
                intent.putExtra("regretOrderid", otherList.get(position).id);
                startActivity(intent);
            }
        });
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
            if (request.flag == AppConst.HHY_LIST) {
                houHuiYaoInfoBean = JSON.parseObject(response.body.toString(), HouHuiYaoInfoBean.class);
                refreshHouHuiYaoList();
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
        }
        CustomToast.show(message, CustomToast.LENGTH_SHORT);
    }

    @Override
    public void onUpdate(String keyword) {
        LogUtil.i(TAG, "houhuiyao onUpdate() keyword = " + keyword);
        isFirstLoad = false;
        requestHouHuiYaoOrderList(keyword);
    }

    class MyHouHuiYaoAdapter extends BaseAdapter {

        private Context context;
        private List<HouHuiYaoInfoBean.HouHuiYaoBean> list = new ArrayList<>();

        public MyHouHuiYaoAdapter(Context context, List<HouHuiYaoInfoBean.HouHuiYaoBean> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (null == convertView) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.lv_hhy_item, null);
                viewHolder.iv_hotel_icon = (RoundedLeftImageView) convertView.findViewById(R.id.iv_hotel_icon);
                viewHolder.tv_hotel_name = (TextView) convertView.findViewById(R.id.tv_hotel_name);
                viewHolder.tv_hotel_name.getPaint().setFakeBoldText(true);
                viewHolder.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
                viewHolder.tv_point = (TextView) convertView.findViewById(R.id.tv_point);
                ((TextView) convertView.findViewById(R.id.tv_buy_price_note)).getPaint().setFakeBoldText(true);
                viewHolder.tv_buy_price = (TextView) convertView.findViewById(R.id.tv_buy_price);
                viewHolder.tv_buy_price.getPaint().setFakeBoldText(true);
                ((TextView) convertView.findViewById(R.id.tv_sale_price_note)).getPaint().setFakeBoldText(true);
                viewHolder.tv_sale_price = (TextView) convertView.findViewById(R.id.tv_sale_price);
                viewHolder.tv_sale_price.getPaint().setFakeBoldText(true);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            Glide.with(context)
                    .load(new CustomReqURLFormatModelImpl(list.get(position).picpath))
                    .placeholder(R.drawable.def_hhy)
                    .crossFade()
                    .centerCrop()
                    .override(350, 200)
                    .into(viewHolder.iv_hotel_icon);
            viewHolder.tv_hotel_name.setText(list.get(position).hotelname);
            float point = 0;
            if (StringUtil.notEmpty(list.get(position).grade)) {
                try {
                    point = Float.parseFloat(list.get(position).grade);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                viewHolder.tv_point.setText(String.valueOf(point));
            } else {
                viewHolder.tv_point.setText("0.0");
            }

            viewHolder.tv_buy_price.setText(list.get(position).buyprice + "");
            viewHolder.tv_sale_price.setText(list.get(position).sellprice + "");
            viewHolder.tv_date.setText(DateUtil.getDay("MM月dd日", list.get(position).starttime) + "-" + DateUtil.getDay("MM月dd日", list.get(position).endtime));
            return convertView;
        }

        class ViewHolder {
            RoundedLeftImageView iv_hotel_icon;
            TextView tv_hotel_name;
            TextView tv_point;
            TextView tv_date;
            TextView tv_buy_price;
            TextView tv_sale_price;
        }
    }
}
