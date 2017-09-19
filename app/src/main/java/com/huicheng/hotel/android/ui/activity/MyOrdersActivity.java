package com.huicheng.hotel.android.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.net.bean.OrderDetailInfoBean;
import com.huicheng.hotel.android.net.bean.OrdersSpendInfoBean;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.CustomSwipeView;
import com.huicheng.hotel.android.ui.custom.SimpleRefreshListView;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.BitmapUtils;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;
import com.huicheng.hotel.android.ui.dialog.CustomToast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * @author kborid
 * @date 2016/12/7 0007
 */
public class MyOrdersActivity extends BaseActivity {

    private static final String TAG = "MyOrdersActivity";
    private static final int PAGESIZE = 10;

    private SimpleRefreshListView listview;
    private MyOrderAdapter adapter;
    private OrdersSpendInfoBean ordersSpendInfoBean = null;
    private List<OrderDetailInfoBean> list = new ArrayList<>();
    // 继续有多少个条目的delete被展示出来的集合
    private List<CustomSwipeView> slideDeleteArrayList = new ArrayList<>();
    private LinearLayout consumption_lay;
    private List<Bitmap> resIdList = new ArrayList<>();
    private Spinner spinner_type, spinner_status, spinner_date;
    private MyOrderTypeAdapter spinner_type_adapter;
    private ArrayAdapter spinner_status_adapter, spinner_date_adapter;
    private String[] year = new String[6];
    private int dateSelectorIndex = 0;
    private int pageIndex = 0;
    private boolean isLoadMore = false;
    private boolean isFirstLoad = false;
    private String orderType, orderStatus;
    private String startYear, endYear;

    private ImageView iv_accessory;
    private TextView tv_spend_year, tv_save_year;
    private int mMainColorId;
    private int mOrderHotelItemBgResId, mOrderPlaneItemBgResId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_myorders_layout);
        TypedArray ta = obtainStyledAttributes(R.styleable.MyTheme);
        mMainColorId = ta.getColor(R.styleable.MyTheme_mainColor, getResources().getColor(R.color.mainColor));
        mOrderHotelItemBgResId = ta.getResourceId(R.styleable.MyTheme_orderHotelItemBg, R.drawable.iv_order_hotel);
        mOrderPlaneItemBgResId = ta.getResourceId(R.styleable.MyTheme_orderPlaneItemBg, R.drawable.iv_order_plane);
        ta.recycle();
        initViews();
        initParams();
        initListeners();
        isFirstLoad = true;
        listview.refreshingHeaderView();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        LogUtil.i(TAG, "onNewIntent() test");
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
        spinner_type = (Spinner) findViewById(R.id.spinner_type);
        spinner_status = (Spinner) findViewById(R.id.spinner_status);
        spinner_date = (Spinner) findViewById(R.id.spinner_date);
    }

    @Override
    public void initParams() {
        super.initParams();

        iv_accessory.setImageBitmap(BitmapUtils.getAlphaBitmap(getResources().getDrawable(R.drawable.iv_accessory_blue), mMainColorId));

        resIdList.add(BitmapUtils.getAlphaBitmap(getResources().getDrawable(R.drawable.iv_consider_white), getResources().getColor(R.color.white)));
        resIdList.add(BitmapUtils.getAlphaBitmap(getResources().getDrawable(R.drawable.iv_tab_hotel), getResources().getColor(R.color.white)));
        resIdList.add(BitmapUtils.getAlphaBitmap(getResources().getDrawable(R.drawable.iv_tab_plane), getResources().getColor(R.color.white)));
        spinner_type_adapter = new MyOrderTypeAdapter(this, resIdList);
        spinner_type.setAdapter(spinner_type_adapter);


        spinner_status_adapter = new ArrayAdapter<>(this, R.layout.spinner_layout_item, Arrays.asList(getResources().getStringArray(R.array.order_status)));
        spinner_status_adapter.setDropDownViewResource(R.layout.spinner_dialog_item);
        spinner_status.setAdapter(spinner_status_adapter);


        Calendar c = Calendar.getInstance();
        int currentYear = c.get(Calendar.YEAR);
        for (int i = 0; i < year.length; i++) {
            if (i == 0) {
                year[i] = "全部年度";
            } else {
                if (i != year.length - 1) {
                    year[i] = currentYear + 2 - i + "年";
                } else {
                    year[i] = currentYear + 3 - i + "年以前";
                }
            }
        }
        spinner_date_adapter = new ArrayAdapter<>(this, R.layout.spinner_layout_item, year);
        spinner_date_adapter.setDropDownViewResource(R.layout.spinner_dialog_item);
        spinner_date.setAdapter(spinner_date_adapter);

        //没有数据时，显示空view提示
        TextView emptyView = new TextView(this);
        emptyView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        emptyView.setText("暂无订单信息");
        emptyView.setPadding(0, Utils.dip2px(50), 0, Utils.dip2px(50));
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
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        spinner_status.setDropDownWidth(spinner_status.getWidth());
//        spinner_status.setDropDownVerticalOffset(spinner_status.getHeight());
        spinner_date.setDropDownWidth(spinner_status.getWidth());
//        spinner_date.setDropDownVerticalOffset(spinner_date.getHeight());
    }

    private void requestMyOrdersList(String orderType, String orderStatus, String startYear, String endYear, int pageIndex) {
        LogUtil.i(TAG, "type = " + orderType + ", status = " + orderStatus + ", startYear = " + startYear + ", endYear = " + endYear + ", pageIndex = " + pageIndex);
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("orderType", orderType);
        b.addBody("status", orderStatus);
        b.addBody("startYear", startYear);
        b.addBody("endYear", endYear);
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
                requestMyOrdersList(orderType, orderStatus, startYear, endYear, pageIndex);
                requestSpendyearly(startYear, endYear);
            }

            @Override
            public void onLoadMore() {
                isLoadMore = true;
                requestMyOrdersList(orderType, orderStatus, startYear, endYear, ++pageIndex);
            }
        });
        consumption_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeOtherItem();
                Intent intent = new Intent(MyOrdersActivity.this, ConsumptionDetailActivity.class);
                intent.putExtra("ordersSpendInfoBean", ordersSpendInfoBean);
                intent.putExtra("startYear", startYear);
                intent.putExtra("endYear", endYear);
                intent.putExtra("selectorIndex", dateSelectorIndex);
                startActivity(intent);
            }
        });

        listview.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_FLING || scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    closeOtherItem();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });

        spinner_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LogUtil.i(TAG, "spinner type 's OnItemSelectedListener request....");
                if (isFirstLoad) {
                    return;
                }
                switch (position) {
                    case 1:
                        orderType = HotelCommDef.Order_Hotel;
                        break;
                    case 2:
                        orderType = HotelCommDef.Order_Plane;
                        break;
                    case 0:
                    default:
                        orderType = "";
                        break;
                }
                LogUtil.i(TAG, "order type = " + orderType);
                listview.refreshingHeaderView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner_status.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LogUtil.i(TAG, "spinner status 's OnItemSelectedListener request....");
                if (isFirstLoad) {
                    return;
                }
                switch (position) {
                    case 1:
                        orderStatus = String.valueOf(HotelCommDef.WaitPay);
                        break;
                    case 2:
                        orderStatus = String.valueOf(HotelCommDef.WaitConfirm);
                        break;
                    case 3:
                        orderStatus = String.valueOf(HotelCommDef.Confirmed);
                        break;
                    case 4:
                        orderStatus = String.valueOf(HotelCommDef.Canceled);
                        break;
                    case 5:
                        orderStatus = String.valueOf(HotelCommDef.Finished);
                        break;
                    case 0:
                    default:
                        orderStatus = "";
                        break;
                }
                LogUtil.i(TAG, "order status = " + orderStatus);
                listview.refreshingHeaderView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner_date.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LogUtil.i(TAG, "spinner date 's OnItemSelectedListener request....");
                if (isFirstLoad) {
                    return;
                }
                dateSelectorIndex = position;
                String selectedYear = year[dateSelectorIndex];
                if (selectedYear.contains("全")) {
                    startYear = "";
                    endYear = "";
                } else if (selectedYear.contains("以前")) {
                    startYear = "";
                    endYear = selectedYear.split("年")[0];
                } else {
                    startYear = selectedYear.split("年")[0];
                    endYear = String.valueOf(Integer.parseInt(startYear) + 1);
                }
                LogUtil.i(TAG, "start = " + selectedYear + ", end = " + endYear);
                listview.refreshingHeaderView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void updateOrdersSpendInfo() {
        if (ordersSpendInfoBean != null) {
            tv_spend_year.setText(String.valueOf((int) ordersSpendInfoBean.spend));
            tv_save_year.setText(String.valueOf((int) ordersSpendInfoBean.totalsave));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.ORDER_LIST) {
                LogUtil.i(TAG, "json = " + response.body.toString());
                isFirstLoad = false;
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
    public void onNotifyError(ResponseData request) {
        listview.stopRefresh();
        listview.stopLoadMore();
    }

    class MyOrderAdapter extends BaseAdapter {
        private Context context;
        private List<OrderDetailInfoBean> list = new ArrayList<>();

        public MyOrderAdapter(Context context, List<OrderDetailInfoBean> list) {
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
                viewHolder.root_lay = (LinearLayout) convertView.findViewById(R.id.root_lay);
                viewHolder.iv_order_point = (ImageView) convertView.findViewById(R.id.iv_order_point);
                // TODO
//                viewHolder.shadow_up_view = convertView.findViewById(R.id.shadow_up_view);
//                viewHolder.shadow_down_view = convertView.findViewById(R.id.shadow_down_view);
                viewHolder.swipeView = (CustomSwipeView) convertView.findViewById(R.id.swipeview);
                viewHolder.delete_lay = (RelativeLayout) convertView.findViewById(R.id.delete_lay);
                viewHolder.item_lay = (RelativeLayout) convertView.findViewById(R.id.item_lay);
                viewHolder.tv_order_title = (TextView) convertView.findViewById(R.id.tv_order_title);
                viewHolder.tv_order_date = (TextView) convertView.findViewById(R.id.tv_order_date);
                viewHolder.tv_order_date.getPaint().setFakeBoldText(true);
                viewHolder.tv_order_city = (TextView) convertView.findViewById(R.id.tv_order_city);
                viewHolder.tv_order_status = (TextView) convertView.findViewById(R.id.tv_order_status);
                viewHolder.tv_order_type = (TextView) convertView.findViewById(R.id.tv_order_type);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

//            if (getCount() == 1) {
//                viewHolder.shadow_up_view.setVisibility(View.VISIBLE);
//                viewHolder.shadow_down_view.setVisibility(View.VISIBLE);
//            } else {
//                if (position == 0) {
//                    viewHolder.shadow_up_view.setVisibility(View.VISIBLE);
//                    viewHolder.shadow_down_view.setVisibility(View.GONE);
//                } else if (position == list.size() - 1) {
//                    viewHolder.shadow_up_view.setVisibility(View.GONE);
//                    viewHolder.shadow_down_view.setVisibility(View.VISIBLE);
//                } else {
//                    viewHolder.shadow_up_view.setVisibility(View.GONE);
//                    viewHolder.shadow_down_view.setVisibility(View.GONE);
//                }
//            }

            viewHolder.iv_order_point.setImageBitmap(BitmapUtils.getAlphaBitmap(context.getResources().getDrawable(R.drawable.iv_order_flag),
                    mMainColorId));

            viewHolder.tv_order_title.setText(list.get(position).hotelName);
            if (StringUtil.notEmpty(list.get(position).cityName)) {
                viewHolder.tv_order_title.append("（" + list.get(position).cityName + "）");
            }

            if (HotelCommDef.Order_Hotel.equals(list.get(position).orderType)) {

                String date = DateUtil.getDay("yyyy年MM月dd日", list.get(position).beginDate) + "-" + DateUtil.getDay("dd日", list.get(position).endDate);
                viewHolder.tv_order_date.setText(date);

                viewHolder.tv_order_city.setVisibility(View.GONE);
                if (HotelCommDef.Canceled == Integer.parseInt(list.get(position).orderStatus)) {
                    viewHolder.root_lay.setBackgroundResource(R.drawable.iv_order_hotel_canceled);
                } else {
                    viewHolder.root_lay.setBackgroundResource(mOrderHotelItemBgResId);
                }
                viewHolder.tv_order_type.setVisibility(View.GONE);
            } else {
                String date = DateUtil.getDay("yyyy年MM月dd日 hh:mm a", list.get(position).flydate);
                viewHolder.tv_order_date.setText(date);

                viewHolder.tv_order_city.setVisibility(View.VISIBLE);
                viewHolder.tv_order_city.setText(list.get(position).message);
                if ("1".equals(list.get(position).isback)) {
                    viewHolder.tv_order_city.append("（" + "往返" + "）");
                } else {
                    viewHolder.tv_order_city.append("（" + "单程" + "）");
                }

                if (HotelCommDef.Canceled == Integer.parseInt(list.get(position).orderStatus)) {
                    viewHolder.root_lay.setBackgroundResource(R.drawable.iv_order_plane_canceled);
                } else {
                    viewHolder.root_lay.setBackgroundResource(mOrderPlaneItemBgResId);
                }

                //机票购买状态
                if (HotelCommDef.BuyTicket == list.get(position).type) {
                    viewHolder.tv_order_type.setVisibility(View.GONE);
                } else {
                    viewHolder.tv_order_type.setVisibility(View.VISIBLE);
                    if (HotelCommDef.ReturnTicket == list.get(position).type) {
                        viewHolder.tv_order_type.setText("退");
                    } else if (HotelCommDef.ChangeTicket == list.get(position).type) {
                        viewHolder.tv_order_type.setText("改");
                    } else {
                        viewHolder.tv_order_type.setVisibility(View.GONE);
                    }
                }
            }

            //支付状态
            viewHolder.tv_order_status.setText(list.get(position).orderStatusName);

            viewHolder.swipeView.setOnSlideDeleteListener(new CustomSwipeView.OnSlideDeleteListener() {
                @Override
                public void onOpen(CustomSwipeView swipeView) {
                    closeOtherItem();
                    slideDeleteArrayList.add(swipeView);
                    LogUtil.i(TAG, "slideDeleteArrayList当前数量：" + slideDeleteArrayList.size());
                }

                @Override
                public void onClose(CustomSwipeView swipeView) {
                    slideDeleteArrayList.remove(swipeView);
                    LogUtil.i(TAG, "slideDeleteArrayList当前数量：" + slideDeleteArrayList.size());
                }
            });

            viewHolder.delete_lay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogUtil.i(TAG, "delete onclick");
                    closeOtherItem();
                    list.remove(position);
                    adapter.notifyDataSetChanged();
                }
            });
            viewHolder.item_lay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogUtil.i(TAG, "item onclick");
                    if (slideDeleteArrayList.size() > 0) {
                        closeOtherItem();
                    } else {
                        if (HotelCommDef.Order_Hotel.equals(list.get(position).orderType)) {
                            Intent intent = new Intent(context, HotelOrderDetailActivity.class);
                            intent.putExtra("orderId", list.get(position).orderId);
                            intent.putExtra("orderType", list.get(position).orderType);
                            startActivityForResult(intent, 0x01);
                        } else {
                            Intent intent = new Intent(context, PlaneOrderDetailActivity.class);
                            HashMap<String, String> params = new HashMap<>();
                            params.put("orderId", list.get(position).orderId);
                            params.put("orderType", list.get(position).orderType);
                            intent.putExtra("path", SessionContext.getUrl(NetURL.ORDER_PLANE, params));
                            startActivityForResult(intent, 0x01);
                        }
                    }
                }
            });

            return convertView;
        }

        class ViewHolder {
            LinearLayout root_lay;
            ImageView iv_order_point;
//            View shadow_up_view;
//            View shadow_down_view;
            CustomSwipeView swipeView;
            RelativeLayout delete_lay;
            RelativeLayout item_lay;
            TextView tv_order_title;
            TextView tv_order_date;
            TextView tv_order_city;
            TextView tv_order_status;
            TextView tv_order_type;
        }
    }

    private void closeOtherItem() {
        // 采用Iterator的原因是for是线程不安全的，迭代器是线程安全的
        for (CustomSwipeView swipeView : slideDeleteArrayList) {
            swipeView.isShowDelete(false);
        }
        slideDeleteArrayList.clear();
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

    class MyOrderTypeAdapter extends BaseAdapter {
        private Context context;
        private List<Bitmap> resIds = new ArrayList<>();

        public MyOrderTypeAdapter(Context context, List<Bitmap> list) {
            this.context = context;
            this.resIds = list;
        }

        @Override
        public int getCount() {
            return resIds.size();
        }

        @Override
        public Object getItem(int position) {
            return resIds.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(context).inflate(R.layout.consider_spinner_item, null);
            if (null != convertView) {
                ImageView iv_item = (ImageView) convertView.findViewById(R.id.iv_item);
                iv_item.setImageBitmap(resIds.get(position));
            }
            return convertView;
        }
    }
}
