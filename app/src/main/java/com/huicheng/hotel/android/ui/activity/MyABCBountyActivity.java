package com.huicheng.hotel.android.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.common.ShareTypeDef;
import com.huicheng.hotel.android.control.ShareControl;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.net.bean.BountyBaseInfo;
import com.huicheng.hotel.android.net.bean.BountyDetailInfo;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.CustomSharePopup;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.Utils;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * @auth kborid
 * @date 2017/12/8 0008.
 */

public class MyABCBountyActivity extends BaseActivity {
    private static final int PAGE_SIZE = 10;
    private int mPageIndex = 0;
    private BountyBaseInfo mBountyBaseInfo = null;
    private BountyDetailInfo mBountyDetailInfo = null;
    private List<BountyDetailInfo.BountyItemInfo> mList = new ArrayList<>();
    private boolean isHasMore = false;
    private boolean isFirstLoad = false;

    private SwipeRefreshLayout swipeRefreshLayout;
    private View header = null;
    private TextView tv_current, tv_in, tv_out;
    private TextView tv_invite;
    private ListView listview;
    private LinearLayout empty_lay;
    private BalanceAdapter adapter;

    private PopupWindow mSharePopupWindow = null;
    private CustomSharePopup mCustomShareView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_abcbounty_layout);
        initViews();
        initParams();
        initListeners();
        if (null == savedInstanceState) {
            requestBountyBaseInfo();
        }
    }

    @Override
    public void initViews() {
        super.initViews();
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        header = LayoutInflater.from(this).inflate(R.layout.lv_balance_header, null);
        empty_lay = (LinearLayout) header.findViewById(R.id.empty_lay);
        tv_current = (TextView) header.findViewById(R.id.tv_current);
        tv_in = (TextView) header.findViewById(R.id.tv_in);
        tv_out = (TextView) header.findViewById(R.id.tv_out);
        listview = (ListView) findViewById(R.id.listview);
        tv_invite = (TextView) findViewById(R.id.tv_invite);
    }

    @Override
    public void initParams() {
        super.initParams();
        swipeRefreshLayout.setColorSchemeResources(R.color.mainColor);
        swipeRefreshLayout.setDistanceToTriggerSync(200);
        swipeRefreshLayout.setProgressViewOffset(true, 0, Utils.dip2px(20));
        swipeRefreshLayout.setSize(SwipeRefreshLayout.DEFAULT);
        adapter = new BalanceAdapter(this, mList);
        listview.setAdapter(adapter);
        listview.addHeaderView(header);
        isFirstLoad = true;
    }

    @Override
    public void initListeners() {
        super.initListeners();
        listview.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && view.getLastVisiblePosition() == view.getCount() - 1) {
                    if (isHasMore) {
                        swipeRefreshLayout.setRefreshing(true);
                        requestBountyDetailInfo(++mPageIndex);
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPageIndex = 0;
                requestBountyBaseInfo();
            }
        });

        tv_invite.setOnClickListener(this);
    }

    private void showSharePopupWindow() {
        if (null == mSharePopupWindow) {
            mCustomShareView = new CustomSharePopup(this);
            mCustomShareView.setOnCancelListener(new CustomSharePopup.OnCanceledListener() {
                @Override
                public void onDismiss() {
                    mSharePopupWindow.dismiss();
                }
            });
            mSharePopupWindow = new PopupWindow(mCustomShareView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        }
        mSharePopupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        mSharePopupWindow.setAnimationStyle(R.style.share_anmi);
        mSharePopupWindow.setBackgroundDrawable(new ColorDrawable(0));
        mSharePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.alpha = 1.0f;
                getWindow().setAttributes(params);
            }
        });
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.alpha = 0.8f;
        getWindow().setAttributes(params);
        mSharePopupWindow.showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.tv_invite:
                HashMap<String, String> params = new HashMap<>();
                params.put("userID", SessionContext.mUser.user.userid);
                params.put("mobile", SessionContext.mUser.user.mobile);
                params.put("channel", ShareTypeDef.SHARE_C2C);
                String url = SessionContext.getUrl(NetURL.SHARE, params);

                final UMWeb web = new UMWeb(url);
                Random random = new Random(System.currentTimeMillis());
                int index = random.nextInt(ShareTypeDef.ShareContentEnum.values().length);
                ShareTypeDef.ShareContentEnum value = ShareTypeDef.ShareContentEnum.values()[index];
                web.setTitle(value.getShareTitle());
                web.setThumb(new UMImage(MyABCBountyActivity.this, BitmapFactory.decodeResource(getResources(), R.drawable.logo)));
                web.setDescription(value.getShareDescription());
                ShareControl.getInstance().setUMWebContent(this, web, null);
                showSharePopupWindow();
                break;
            case R.id.tv_right:
                Intent intent = new Intent(this, HtmlActivity.class);
                intent.putExtra("title", getString(R.string.lxb_how));
                intent.putExtra("path", NetURL.BOUNTY_LXB_RULE);
                startActivity(intent);
                break;
        }
    }

    private void requestBountyBaseInfo() {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        ResponseData d = b.syncRequest(b);
        d.flag = AppConst.BOUNTY_USER_BASE;
        d.path = NetURL.BOUNTY_USER_BASE;
        if (isFirstLoad && !isProgressShowing()) {
            isFirstLoad = false;
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void requestBountyDetailInfo(int pageIndex) {
        LogUtil.i(TAG, "requestBountyDetailInfo() pageIndex = " + pageIndex);
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("pageIndex", pageIndex);
        b.addBody("pageSize", PAGE_SIZE);
        b.addBody("timeRank", "DESC");//排序类型，默认ASC升序，DESC降序
        ResponseData d = b.syncRequest(b);
        d.flag = AppConst.BOUNTY_USER_DETAIL;
        d.path = NetURL.BOUNTY_USER_DETAIL;
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void refreshBountyBaseInfo() {
        int current = 0;
        int lxbIn = 0;
        int lxbOut = 0;
        if (null != mBountyBaseInfo) {
            current = mBountyBaseInfo.rest;
            lxbIn = mBountyBaseInfo.total;
            lxbOut = mBountyBaseInfo.used;
        }
        tv_current.setText(String.valueOf(current));
        tv_in.setText(String.valueOf(lxbIn));
        tv_out.setText(String.valueOf(lxbOut));
    }

    private void refreshBountyDetailListInfo() {
        if (mBountyDetailInfo != null && mBountyDetailInfo.balances != null && mBountyDetailInfo.balances.size() > 0) {
            if (mPageIndex <= 0) {
                mList.clear();
            }
            mList.addAll(mBountyDetailInfo.balances);
            adapter.notifyDataSetChanged();
        }

        //empty view判断
        if (mList.size() > 0) {
            empty_lay.setVisibility(View.GONE);
        } else {
            empty_lay.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class BalanceAdapter extends BaseAdapter {

        private Context context;
        private List<BountyDetailInfo.BountyItemInfo> list = new ArrayList<>();

        BalanceAdapter(Context context, List<BountyDetailInfo.BountyItemInfo> list) {
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
            ViewHolder viewHolder;
            if (null == convertView) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.lv_balance_item, null);
                viewHolder.tv_balance_title = (TextView) convertView.findViewById(R.id.tv_balance_title);
                viewHolder.tv_balance_price = (TextView) convertView.findViewById(R.id.tv_balance_price);
                viewHolder.tv_balance_date = (TextView) convertView.findViewById(R.id.tv_balance_date);
                viewHolder.tv_balance_user = (TextView) convertView.findViewById(R.id.tv_balance_user);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if ("1".equals(list.get(position).type)) {
                viewHolder.tv_balance_price.setText("+");
                viewHolder.tv_balance_title.setText("订单奖励");
            } else if ("2".equals(list.get(position).type)) {
                viewHolder.tv_balance_price.setText("-");
                viewHolder.tv_balance_title.setText("订单使用");
            } else {
                viewHolder.tv_balance_price.setText("");
                viewHolder.tv_balance_title.setText("旅行币");
            }
            viewHolder.tv_balance_price.append(String.valueOf(list.get(position).amount));
            viewHolder.tv_balance_date.setText(DateUtil.getDay("yyyy/MM/dd", list.get(position).createTime));
            viewHolder.tv_balance_user.setText(list.get(position).remark);
            return convertView;
        }

        class ViewHolder {
            TextView tv_balance_title;
            TextView tv_balance_price;
            TextView tv_balance_date;
            TextView tv_balance_user;
        }
    }

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        super.onNotifyMessage(request, response);
        if (response != null && response.body != null) {
            if (request.flag == AppConst.BOUNTY_USER_BASE) {
                LogUtil.i(TAG, "json = " + response.body.toString());
                mBountyBaseInfo = JSONObject.parseObject(response.body.toString(), BountyBaseInfo.class);
                refreshBountyBaseInfo();
                requestBountyDetailInfo(mPageIndex);
            } else if (request.flag == AppConst.BOUNTY_USER_DETAIL) {
                LogUtil.i(TAG, "json = " + response.body.toString());
                swipeRefreshLayout.setRefreshing(false);
                removeProgressDialog();
                mBountyDetailInfo = JSONObject.parseObject(response.body.toString(), BountyDetailInfo.class);
                isHasMore = mBountyDetailInfo.hasMore;
                if (!isHasMore && (mBountyDetailInfo.balances == null || mBountyDetailInfo.balances.size() == 0)) {
                    mPageIndex--;
                }
                refreshBountyDetailListInfo();
            }
        }
    }

    @Override
    public void onNotifyError(ResponseData request) {
        super.onNotifyError(request);
        removeProgressDialog();
        swipeRefreshLayout.setRefreshing(false);
    }
}
