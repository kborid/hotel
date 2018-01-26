package com.huicheng.hotel.android.ui.activity;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.content.AppConst;
import com.huicheng.hotel.android.content.NetURL;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.AssessOrderInfoBean;
import com.huicheng.hotel.android.ui.activity.hotel.HotelAssessOrderDetailActivity;
import com.huicheng.hotel.android.ui.adapter.HotelAssessOrdersAdapter;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.huicheng.hotel.android.ui.custom.SimpleRefreshListView;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author kborid
 * @date 2016/12/14 0014
 */
public class UcAssessesActivity extends BaseAppActivity {

    private static final int PAGESIZE = 10;
    private int pageIndex = 0;
    private SimpleRefreshListView listview;
    private boolean isLoadMore = false;
    private HotelAssessOrdersAdapter adapter;
    private List<AssessOrderInfoBean> list = new ArrayList<>();

    private int mAssessHotelItemBgResId;

    @Override
    protected void requestData() {
        super.requestData();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                listview.refreshingHeaderView();
            }
        }, 350);
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_uc_assesses);
    }

    @Override
    protected void initTypeArrayAttributes() {
        super.initTypeArrayAttributes();
        TypedArray ta = obtainStyledAttributes(R.styleable.MyTheme);
        mAssessHotelItemBgResId = ta.getResourceId(R.styleable.MyTheme_assessesItemBg, R.drawable.iv_assess_hotel);
        ta.recycle();
    }

    @Override
    public void initViews() {
        super.initViews();
        listview = (SimpleRefreshListView) findViewById(R.id.listview);
        if (listview != null) {
            listview.setPullRefreshEnable(true);
            listview.setPullLoadEnable(true);
        }
        adapter = new HotelAssessOrdersAdapter(this, list);
        adapter.setAssessedBackgroundResId(mAssessHotelItemBgResId);
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
        emptyView.setPadding(0, Utils.dp2px(50), 0, Utils.dp2px(50));
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
                Intent intent = new Intent(UcAssessesActivity.this, HotelAssessOrderDetailActivity.class);
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
}
