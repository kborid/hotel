package com.huicheng.hotel.android.ui.activity.hotel;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.AssessOrderInfoBean;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.CommonAssessStarsLayout;
import com.huicheng.hotel.android.ui.custom.SimpleRefreshListView;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.prj.sdk.app.AppConst;
import com.prj.sdk.app.NetURL;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author kborid
 * @date 2016/12/14 0014
 */
public class HotelOrdersAssessActivity extends BaseActivity {

    private static final int PAGESIZE = 10;
    private int pageIndex = 0;
    private SimpleRefreshListView listview;
    private boolean isLoadMore = false;
    private MyAssessOrdersAdapter adapter;
    private List<AssessOrderInfoBean> list = new ArrayList<>();

    private int mAssessHotelItemBgResId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_hotelordersassess_layout);
        TypedArray ta = obtainStyledAttributes(R.styleable.MyTheme);
        mAssessHotelItemBgResId = ta.getResourceId(R.styleable.MyTheme_assessesItemBg, R.drawable.iv_assess_hotel);
        ta.recycle();
        initViews();
        initParams();
        initListeners();
        if (null == savedInstanceState) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    listview.refreshingHeaderView();
                }
            }, 350);
        }
    }

    @Override
    public void initViews() {
        super.initViews();
        listview = (SimpleRefreshListView) findViewById(R.id.listview);
        if (listview != null) {
            listview.setPullRefreshEnable(true);
            listview.setPullLoadEnable(true);
        }
        adapter = new MyAssessOrdersAdapter(this, list);
        listview.setAdapter(adapter);
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText("评价");

        //没有数据时，显示空view提示
        TextView emptyView = new TextView(this);
        emptyView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        emptyView.setText("暂无订单信息");
        emptyView.setPadding(0, Utils.dip2px(50), 0, Utils.dip2px(50));
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setTextColor(getResources().getColor(R.color.searchHintColor));
        emptyView.setVisibility(View.GONE);
        ((ViewGroup) listview.getParent()).addView(emptyView);
        listview.setEmptyView(emptyView);
    }

    @Override
    public void initListeners() {
        super.initListeners();
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(HotelOrdersAssessActivity.this, HotelAssessDetailActivity.class);
                intent.putExtra("order", (AssessOrderInfoBean) parent.getAdapter().getItem(position));
                startActivityForResult(intent, 0x01);
            }
        });
        listview.setXListViewListener(new SimpleRefreshListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pageIndex = 0;
                isLoadMore = false;
                requestAssessOrders(pageIndex);
            }

            @Override
            public void onLoadMore() {
                isLoadMore = true;
                requestAssessOrders(++pageIndex);
            }
        });
    }

    private void requestAssessOrders(int pageIndex) {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("pageSize", String.valueOf(PAGESIZE));
        b.addBody("pageIndex", String.valueOf(pageIndex));
        b.addBody("isevaluated", "");

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.ASSESS_ORDER;
        d.flag = AppConst.ASSESS_ORDER;

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        if (request.flag == AppConst.ASSESS_ORDER) {
            if (response.body != null) {
                JSONObject mJson = JSON.parseObject(response.body.toString());
                if (mJson.containsKey("evaluateList")) {
                    List<AssessOrderInfoBean> temp = JSON.parseArray(mJson.getString("evaluateList"), AssessOrderInfoBean.class);
                    if (temp.size() > 0) {
                        if (!isLoadMore) {
                            listview.setRefreshTime(DateUtil.getSecond(Calendar.getInstance().getTimeInMillis()));
                            list.clear();
                        }
                        list.addAll(temp);
                        adapter.notifyDataSetChanged();
                    } else {
                        pageIndex--;
                        CustomToast.show("没有更多数据", CustomToast.LENGTH_SHORT);
                    }
                    listview.stopLoadMore();
                    listview.stopRefresh();
                }
            }
        }
    }

    private class MyAssessOrdersAdapter extends BaseAdapter {
        private List<AssessOrderInfoBean> list;
        private Context context;

        MyAssessOrdersAdapter(Context context, List<AssessOrderInfoBean> list) {
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
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (null == convertView) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.lv_assessorder_item, null);
                viewHolder.root_lay = (LinearLayout) convertView.findViewById(R.id.root_lay);
                viewHolder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
                viewHolder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
                viewHolder.tv_status = (TextView) convertView.findViewById(R.id.tv_status);
                viewHolder.assess_star = (CommonAssessStarsLayout) convertView.findViewById(R.id.assess_star);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            // 设置adapter数据
            AssessOrderInfoBean bean = list.get(position);
            if (StringUtil.isEmpty(bean.isevaluated) || "0".equals(bean.isevaluated)) { //待评价
                viewHolder.root_lay.setBackgroundResource(mAssessHotelItemBgResId);
                viewHolder.tv_title.setTextColor(context.getResources().getColor(R.color.white));
                viewHolder.tv_time.setTextColor(context.getResources().getColor(R.color.white));
                viewHolder.tv_status.setTextColor(context.getResources().getColor(R.color.white));
                viewHolder.tv_status.setText("待评价");
                viewHolder.assess_star.setVisibility(View.GONE);
            } else if ("1".equals(bean.isevaluated)) { //已评价
                viewHolder.root_lay.setBackgroundResource(R.drawable.iv_assess_hotel_complete);
                viewHolder.tv_title.setTextColor(context.getResources().getColor(R.color.noDiscountColor));
                viewHolder.tv_time.setTextColor(context.getResources().getColor(R.color.noDiscountColor));
                viewHolder.tv_status.setTextColor(context.getResources().getColor(R.color.noDiscountColor));
                viewHolder.tv_status.setText("已评价");
                viewHolder.assess_star.setVisibility(View.VISIBLE);
                viewHolder.assess_star.setColorStars(Integer.parseInt(bean.grade));
            } else if ("2".equals(bean.isevaluated)) { //已删除
                viewHolder.tv_status.setText("已删除");
            } else { //无
                LogUtil.i(TAG, "warning!!!");
            }

            viewHolder.tv_title.setText(bean.hotelName + "(" + bean.cityName + ")");
            String start = DateUtil.getDay("yyyy年MM月dd日", bean.startTime);
            String end = DateUtil.getDay("dd日", bean.endTime);
            LogUtil.i(TAG, "开始时间：" + start + ", 结束时间：" + end);
            viewHolder.tv_time.setText(start + "-" + end);

            return convertView;
        }

        class ViewHolder {
            LinearLayout root_lay;
            TextView tv_title;
            TextView tv_time;
            TextView tv_status;
            CommonAssessStarsLayout assess_star;
        }
    }
}
