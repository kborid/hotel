package com.huicheng.hotel.android.ui.activity.hotel;

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
import com.bumptech.glide.Glide;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.content.AppConst;
import com.huicheng.hotel.android.content.NetURL;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.AssessCommendInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.AssessOrderDetailInfoBean;
import com.huicheng.hotel.android.ui.activity.ImageScaleActivity;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.huicheng.hotel.android.ui.custom.CommonAssessStarsLayout;
import com.huicheng.hotel.android.ui.custom.SimpleRefreshListView;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.huicheng.hotel.android.ui.glide.CustomReqURLFormatModelImpl;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * @author kborid
 * @date 2017/1/13 0013
 */
public class HotelCommendsActivity extends BaseAppActivity {

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
    protected void requestData() {
        super.requestData();
        listview.refreshingHeaderView();
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_hotel_commends);
    }

    @Override
    public void initViews() {
        super.initViews();
        root_lay = (LinearLayout) findViewById(R.id.root_lay);
        root_lay.setLayoutAnimation(getAnimationController());
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
        emptyView.setPadding(0, Utils.dp2px(30), 0, Utils.dp2px(30));
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
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.HOTEL_COMMENT) {
                removeProgressDialog();
                LogUtil.i(TAG, "assesscommend json = " + response.body.toString());
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
                    if (list.size() > 0) {
                        CustomToast.show("没有更多数据", CustomToast.LENGTH_SHORT);
                    }
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
                viewHolder.iv_photo = (CircleImageView) convertView.findViewById(R.id.iv_photo);
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

            final AssessOrderDetailInfoBean bean = list.get(position);
            Glide.with(context)
                    .load(new CustomReqURLFormatModelImpl(bean.headphotourl))
                    .placeholder(R.drawable.def_photo)
                    .crossFade()
                    .centerCrop()
                    .override(150, 150)
                    .into(viewHolder.iv_photo);
            viewHolder.tv_name.setText(bean.username);
            viewHolder.tv_name.getPaint().setFakeBoldText(true);
            viewHolder.tv_date.setText(DateUtil.getDay("MM月 yyyy", bean.createTime));

            float grade = 0;
            try {
                grade = Float.parseFloat(String.valueOf(bean.grade));
            } catch (Exception e) {
                e.printStackTrace();
            }
            viewHolder.tv_point.setText(grade + "分");
            viewHolder.assess_layout.setColorStars(bean.grade);

            if (StringUtil.notEmpty(bean.imgUrl)) {
                viewHolder.iv_picture.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(new CustomReqURLFormatModelImpl(bean.imgUrl))
                        .placeholder(R.color.hintColor)
                        .crossFade()
                        .fitCenter()
                        .override(500, 500)
                        .into(viewHolder.iv_picture);
            } else {
                viewHolder.iv_picture.setVisibility(View.GONE);
            }
            viewHolder.iv_picture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ImageScaleActivity.class);
                    intent.putExtra("url", bean.imgUrl);
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
            viewHolder.tv_content.setText(bean.cotent);
            return convertView;
        }

        class ViewHolder {
            CircleImageView iv_photo;
            TextView tv_name;
            TextView tv_date;
            TextView tv_point;
            CommonAssessStarsLayout assess_layout;
            ImageView iv_picture;
            TextView tv_content;
        }
    }
}
