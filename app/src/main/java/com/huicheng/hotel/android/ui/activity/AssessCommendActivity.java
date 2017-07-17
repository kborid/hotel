package com.huicheng.hotel.android.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.net.bean.AssessCommendInfoBean;
import com.huicheng.hotel.android.net.bean.AssessOrderDetailInfoBean;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.CommonAssessStarsLayout;
import com.huicheng.hotel.android.ui.custom.RoundedAllImageView;
import com.huicheng.hotel.android.ui.custom.SimpleRefreshListView;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.CustomToast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author kborid
 * @date 2017/1/13 0013
 */
public class AssessCommendActivity extends BaseActivity implements DataCallback {

    private static final int PAGESIZE = 10;
    private int pageIndex = 0;
    private boolean isLoadMore = false;

    private LinearLayout root_lay;
    private int hotelId = -1;
    private TextView tv_commend_count;
    private CommonAssessStarsLayout assess_layout;
    private SimpleRefreshListView listview;
    private MyAssessCommendAdapter adapter;
    private AssessCommendInfoBean bean = null;
    private List<AssessOrderDetailInfoBean> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_assesscommend_layout);
        initViews();
        initParams();
        initListeners();
        listview.refreshingHeaderView();
    }

    @Override
    public void initViews() {
        super.initViews();
        root_lay = (LinearLayout) findViewById(R.id.root_lay);
        tv_commend_count = (TextView) findViewById(R.id.tv_comment_count);
        if (tv_commend_count != null) {
            tv_commend_count.getPaint().setFakeBoldText(true);
        }
        assess_layout = (CommonAssessStarsLayout) findViewById(R.id.assess_layout);
        listview = (SimpleRefreshListView) findViewById(R.id.listview);
        adapter = new MyAssessCommendAdapter(this, list);
        listview.setAdapter(adapter);
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            hotelId = bundle.getInt("hotelId");
        }
    }

    @Override
    public void initParams() {
        super.initParams();
        listview.setPullRefreshEnable(true);
        listview.setPullLoadEnable(true);
        //没有数据时，显示空view提示
        TextView emptyView = new TextView(this);
        emptyView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        emptyView.setText("暂无评论");
        emptyView.setPadding(0, Utils.dip2px(30), 0, Utils.dip2px(30));
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setTextColor(getResources().getColor(R.color.searchHintColor));
        emptyView.setVisibility(View.GONE);
        ((ViewGroup) listview.getParent()).addView(emptyView);
        listview.setEmptyView(emptyView);
    }

    private void requestCommentListInfo(int pageIndex) {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("hotelId", String.valueOf(hotelId));
        b.addBody("pageSize", String.valueOf(PAGESIZE));
        b.addBody("pageIndex", String.valueOf(pageIndex));

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.HOTEL_COMMENT;
        d.flag = AppConst.HOTEL_COMMENT;

        if (!isProgressShowing()) {
            showProgressDialog(this);
        }

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void refreshCommendInfo() {
        if (null != bean) {
            root_lay.setVisibility(View.VISIBLE);
            if (StringUtil.isEmpty(bean.commentCount) || "0".equals(bean.commentCount)) {
                tv_commend_count.setText("无评论");
            } else {
                tv_commend_count.setText(bean.commentCount + "条评论");
            }
            float grade = 0;
            try {
                grade = Float.parseFloat(bean.grade);
            } catch (Exception e) {
                e.printStackTrace();
            }
            assess_layout.setColorStars((int) grade);
        } else {
            root_lay.setVisibility(View.GONE);
        }
    }

    @Override
    public void initListeners() {
        super.initListeners();
        listview.setXListViewListener(new SimpleRefreshListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isLoadMore = false;
                pageIndex = 0;
                requestCommentListInfo(pageIndex);
            }

            @Override
            public void onLoadMore() {
                isLoadMore = true;
                requestCommentListInfo(++pageIndex);
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
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
    public void preExecute(ResponseData request) {

    }

    @Override
    public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.HOTEL_COMMENT) {
                removeProgressDialog();
                System.out.println("assesscommend json = " + response.body.toString());
                bean = JSON.parseObject(response.body.toString(), AssessCommendInfoBean.class);
                refreshCommendInfo();

                if (bean.commentList != null && bean.commentList.size() > 0) {
                    if (!isLoadMore) {
                        listview.setRefreshTime(DateUtil.getSecond(Calendar.getInstance().getTimeInMillis()));
                        list.clear();
                    }
                    list.addAll(bean.commentList);
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

    class MyAssessCommendAdapter extends BaseAdapter {
        private Context context;
        private List<AssessOrderDetailInfoBean> list = new ArrayList<>();

        public MyAssessCommendAdapter(Context context, List<AssessOrderDetailInfoBean> list) {
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
            final ViewHolder viewHolder;
            if (null == convertView) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.lv_assess_commend_item, null);
                viewHolder.iv_photo = (RoundedAllImageView) convertView.findViewById(R.id.iv_photo);
                viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
                viewHolder.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
                viewHolder.tv_point = (TextView) convertView.findViewById(R.id.tv_point);
                viewHolder.assess_layout = (CommonAssessStarsLayout) convertView.findViewById(R.id.assess_layout);
                viewHolder.iv_picture = (ImageView) convertView.findViewById(R.id.iv_picture);
                viewHolder.tv_content = (TextView) convertView.findViewById(R.id.tv_content);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            loadImage(viewHolder.iv_photo, R.drawable.def_photo, list.get(position).headphotourl, 80, 48);
            viewHolder.tv_name.setText(list.get(position).username);
            viewHolder.tv_name.getPaint().setFakeBoldText(true);
            viewHolder.tv_date.setText(DateUtil.getDay("MM月 yyyy", list.get(position).createTime));

            float grade = 0;
            try {
                grade = Float.parseFloat(String.valueOf(list.get(position).grade));
            } catch (Exception e) {
                e.printStackTrace();
            }
            viewHolder.tv_point.setText(grade + "分");
            viewHolder.assess_layout.setColorStars(list.get(position).grade);

            if (StringUtil.notEmpty(list.get(position).imgUrl)) {
                viewHolder.iv_picture.setVisibility(View.VISIBLE);
                loadImage(viewHolder.iv_picture, list.get(position).imgUrl, 800, 480);
            } else {
                viewHolder.iv_picture.setVisibility(View.GONE);
            }
            viewHolder.iv_picture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ImageScaleActivity.class);
                    intent.putExtra("url", list.get(position).imgUrl);
                    int[] location = new int[2];
                    viewHolder.iv_picture.getLocationOnScreen(location);
                    intent.putExtra("locationX", location[0]);//必须
                    intent.putExtra("locationY", location[1]);//必须
                    intent.putExtra("width", viewHolder.iv_picture.getWidth());//必须
                    intent.putExtra("height", viewHolder.iv_picture.getHeight());//必须
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            });
            viewHolder.tv_content.setText(list.get(position).cotent);
            return convertView;
        }

        class ViewHolder {
            RoundedAllImageView iv_photo;
            TextView tv_name;
            TextView tv_date;
            TextView tv_point;
            CommonAssessStarsLayout assess_layout;
            ImageView iv_picture;
            TextView tv_content;
        }
    }
}
