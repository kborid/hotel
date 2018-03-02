package com.huicheng.hotel.android.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.huicheng.hotel.android.ui.custom.SimpleRefreshListView;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.util.BitmapUtils;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author kborid
 * @date 2016/12/7 0007
 */
public class UcOrdersActivity extends BaseAppActivity {

    private static final int PAGESIZE = 10;

    private SimpleRefreshListView listview;
    private MyOrderAdapter adapter;
    private OrdersSpendInfoBean ordersSpendInfoBean = null;
    private List<OrderDetailInfoBean> list = new ArrayList<>();
    private LinearLayout consumption_lay;
    private ImageView iv_accessory;
    private TextView tv_spend_year, tv_save_year;

    private int dateSelectorIndex = 0;
    private int pageIndex = 0;
    private String orderType;
    private boolean isLoadMore = false;
    private int mMainColorId;
    private int mOrderHotelItemBgResId, mOrderPlaneItemBgResId;

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_uc_orders);
    }

    @Override
    protected void initTypeArrayAttributes() {
        super.initTypeArrayAttributes();
        TypedArray ta = obtainStyledAttributes(R.styleable.MyTheme);
        mMainColorId = ta.getColor(R.styleable.MyTheme_mainColor, getResources().getColor(R.color.mainColor));
        mOrderHotelItemBgResId = ta.getResourceId(R.styleable.MyTheme_orderHotelItemBg, R.drawable.iv_order_hotel);
        mOrderPlaneItemBgResId = ta.getResourceId(R.styleable.MyTheme_orderPlaneItemBg, R.drawable.iv_order_plane);
        ta.recycle();
    }

    @Override
    protected void requestData() {
        super.requestData();
        listview.refreshingHeaderView();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        listview.refreshingHeaderView();
    }

    @Override
    public void initViews() {
        super.initViews();
        tv_spend_year = (TextView) findViewById(R.id.tv_spend_year);
        tv_save_year = (TextView) findViewById(R.id.tv_save_year);
        iv_accessory = (ImageView) findViewById(R.id.iv_accessory);

        listview = (SimpleRefreshListView) findViewById(R.id.listview);
        if (listview != null) {
            listview.setPullRefreshEnable(true);
            listview.setPullLoadEnable(true);
        }

        adapter = new MyOrderAdapter(this, list);
        listview.setAdapter(adapter);
        consumption_lay = (LinearLayout) findViewById(R.id.consumption_lay);
    }

    @Override
    public void initParams() {
        super.initParams();

        tv_center_title.setText("我的行程");
        iv_accessory.setImageBitmap(BitmapUtils.getAlphaBitmap(getResources().getDrawable(R.drawable.iv_accessory_blue), mMainColorId));

        //没有数据时，显示空view提示
        TextView emptyView = new TextView(this);
        emptyView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        emptyView.setText("暂无订单信息");
        emptyView.setPadding(0, Utils.dp2px(50), 0, Utils.dp2px(50));
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setTextColor(getResources().getColor(R.color.searchHintColor));
        emptyView.setVisibility(View.GONE);
        emptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listview.refreshingHeaderView();
            }
        });
        ((ViewGroup) listview.getParent()).addView(emptyView);
        listview.setEmptyView(emptyView);

        orderType = HotelCommDef.Order_Hotel;
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

    private void requestSpendyearly(String startYear, String endYear) {
        LogUtil.i(TAG, "requestSpendyearly()");
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("startYear", startYear);
        b.addBody("endYear", endYear);

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.ORDER_SPEND;
        d.flag = AppConst.ORDER_SPEND;

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    @Override
    public void initListeners() {
        super.initListeners();

        listview.setXListViewListener(new SimpleRefreshListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pageIndex = 0;
                isLoadMore = false;
                requestMyOrdersList(orderType, pageIndex);
                requestSpendyearly("", "");
            }

            @Override
            public void onLoadMore() {
                isLoadMore = true;
                requestMyOrdersList(orderType, ++pageIndex);
            }
        });
        consumption_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UcOrdersActivity.this, UcCostDetailActivity.class);
                intent.putExtra("ordersSpendInfoBean", ordersSpendInfoBean);
                intent.putExtra("startYear", "");
                intent.putExtra("endYear", "");
                intent.putExtra("selectorIndex", dateSelectorIndex);
                startActivity(intent);
            }
        });
    }

    private void updateOrdersSpendInfo() {
        if (ordersSpendInfoBean != null) {
            tv_spend_year.setText(String.valueOf((int) ordersSpendInfoBean.spend));
            tv_save_year.setText(String.valueOf((int) ordersSpendInfoBean.totalsave));
        }
    }

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.ORDER_LIST) {
                LogUtil.i(TAG, "json = " + response.body.toString());
                List<OrderDetailInfoBean> temp = JSON.parseArray(response.body.toString(), OrderDetailInfoBean.class);
                if (!isLoadMore) {
                    listview.setRefreshTime(DateUtil.getSecond(Calendar.getInstance().getTimeInMillis()));
                    list.clear();
                }
                if (temp.size() <= 0) {
                    pageIndex--;
                    CustomToast.show("没有更多数据", CustomToast.LENGTH_SHORT);
                }

                listview.stopLoadMore();
                listview.stopRefresh();
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
    public void onNotifyError(ResponseData request, ResponseData response) {
        super.onNotifyError(request, response);
        listview.stopRefresh();
        listview.stopLoadMore();
    }

    private class MyOrderAdapter extends BaseAdapter {
        private Context context;
        private List<OrderDetailInfoBean> list = new ArrayList<>();

        MyOrderAdapter(Context context, List<OrderDetailInfoBean> list) {
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

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (null == convertView) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.lv_item_order, null);
                viewHolder.flag_lay = (RelativeLayout) convertView.findViewById(R.id.flag_lay);
                viewHolder.flag_date_lay = (LinearLayout) convertView.findViewById(R.id.flag_date_lay);
                viewHolder.tv_flag_year = (TextView) convertView.findViewById(R.id.tv_flag_year);
                viewHolder.tv_flag_date = (TextView) convertView.findViewById(R.id.tv_flag_date);
                viewHolder.item_lay = (RelativeLayout) convertView.findViewById(R.id.item_lay);
                viewHolder.tv_item_name = (TextView) convertView.findViewById(R.id.tv_item_name);
                viewHolder.tv_item_date = (TextView) convertView.findViewById(R.id.tv_item_date);
                viewHolder.tv_item_position = (TextView) convertView.findViewById(R.id.tv_item_position);
                viewHolder.tv_item_phone = (TextView) convertView.findViewById(R.id.tv_item_phone);
                viewHolder.iv_item_type = (ImageView) convertView.findViewById(R.id.iv_item_type);
                viewHolder.tv_item_status = (TextView) convertView.findViewById(R.id.tv_item_status);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final OrderDetailInfoBean item = list.get(position);
            String currBeginDateStr = DateUtil.getDay("yyyyMMdd", item.beginDate);

            if (0 == position) {
                viewHolder.flag_date_lay.setVisibility(View.VISIBLE);
                viewHolder.item_lay.setBackground(context.getResources().getDrawable(R.drawable.iv_order_item_arrow_bg));
            } else {
                String lastBeginDateStr = DateUtil.getDay("yyyyMMdd", list.get(position - 1).beginDate);
                if (lastBeginDateStr.equals(currBeginDateStr)) {
                    viewHolder.flag_date_lay.setVisibility(View.GONE);
                    viewHolder.item_lay.setBackground(context.getResources().getDrawable(R.drawable.iv_order_item_normal_bg));
                } else {
                    viewHolder.flag_date_lay.setVisibility(View.VISIBLE);
                    viewHolder.item_lay.setBackground(context.getResources().getDrawable(R.drawable.iv_order_item_arrow_bg));
                }
            }

            viewHolder.tv_flag_year.setText(DateUtil.getDay("yyyy", item.beginDate));
            viewHolder.tv_flag_date.setText(DateUtil.getDay("MM/dd", item.beginDate));
            viewHolder.tv_item_name.setText(item.hotelName);
            viewHolder.tv_item_date.setText(String.format(context.getString(R.string.orderListItemDateStr),
                    DateUtil.getDay("MM月dd日", item.beginDate),
                    DateUtil.getDay("MM月dd日", item.endDate),
                    DateUtil.getGapCount(new Date(item.beginDate), new Date(item.endDate))));
            viewHolder.tv_item_position.setText(item.hotelAddress);
            viewHolder.tv_item_phone.setText(item.hotelPhone);
            viewHolder.tv_item_status.setText(item.orderStatusName);

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (HotelCommDef.Order_Hotel.equals(item.orderType)) {
                        Intent intent = new Intent(context, HotelOrderDetailActivity.class);
                        intent.putExtra("orderId", item.orderId);
                        intent.putExtra("orderType", item.orderType);
                        startActivityForResult(intent, 0x01);
                    } else {
                        Intent intent = new Intent(context, PlaneOrderDetailActivity.class);
                        HashMap<String, String> params = new HashMap<>();
                        params.put("orderId", item.orderId);
                        params.put("orderType", item.orderType);
                        intent.putExtra("path", SessionContext.getUrl(NetURL.ORDER_PLANE, params));
                        startActivityForResult(intent, 0x01);
                    }
                }
            });

            return convertView;
        }

        class ViewHolder {
            RelativeLayout flag_lay;
            LinearLayout flag_date_lay;
            TextView tv_flag_year;
            TextView tv_flag_date;
            RelativeLayout item_lay;
            TextView tv_item_name;
            TextView tv_item_date;
            TextView tv_item_position;
            TextView tv_item_phone;
            ImageView iv_item_type;
            TextView tv_item_status;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK != resultCode) {
            return;
        }
        if (requestCode == 0x01) {
            listview.refreshingHeaderView();
        }
    }
}
