package com.huicheng.hotel.android.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.net.bean.HotelDetailInfoBean;
import com.huicheng.hotel.android.net.bean.RoomDetailCheckResultInfoBean;
import com.huicheng.hotel.android.net.bean.RoomListInfoBean;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.CommonAssessStarsLayout;
import com.huicheng.hotel.android.ui.custom.RoundedLeftImageView;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.CustomToast;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kborid
 * @date 2017/1/1 0001
 */
public class RoomListActivity extends BaseActivity implements DataCallback {

    private static final int SELECTED_BAR_COUNT = 2;
    private LinearLayout root_detail_lay;

    private ViewPager viewPager;
    private LinearLayout indicator_lay;
    private int positionIndex = 0;

    private TextView tv_assess_point, tv_comment;
    private CommonAssessStarsLayout assess_star_lay;
    private TextView tv_hotel_name, tv_hotel_add, tv_hotel_phone;
    private TextView tv_map, tv_service, tv_view_comment;
    private TextView tv_hotel_service_name;
    private LinearLayout service_lay1, service_lay2;

    private TabHost tabHost;
    private LinearLayout tab_day, tab_clock;
    private String key = null;
    private int hotelId = 0;
    private int roomId = -1, roomType = 0;
    private boolean isClickYgr = false;
    private HotelDetailInfoBean hotelDetailInfoBean = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_roomlist_layout);
        initViews();
        initParams();
        initListeners();
    }

    @Override
    public void initViews() {
        super.initViews();
        root_detail_lay = (LinearLayout) findViewById(R.id.root_detail_lay);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        indicator_lay = (LinearLayout) findViewById(R.id.indicator_lay);
        tv_assess_point = (TextView) findViewById(R.id.tv_assess_point);
        tv_assess_point.getPaint().setFakeBoldText(true);
        assess_star_lay = (CommonAssessStarsLayout) findViewById(R.id.assess_star_lay);
        tv_comment = (TextView) findViewById(R.id.tv_comment);
        tv_comment.getPaint().setFakeBoldText(true);
        tv_hotel_name = (TextView) findViewById(R.id.tv_hotel_name);
        tv_hotel_add = (TextView) findViewById(R.id.tv_hotel_add);
        tv_hotel_phone = (TextView) findViewById(R.id.tv_hotel_phone);
        tv_map = (TextView) findViewById(R.id.tv_map);
        tv_service = (TextView) findViewById(R.id.tv_service);
        tv_view_comment = (TextView) findViewById(R.id.tv_view_comment);

        tv_hotel_service_name = (TextView) findViewById(R.id.tv_hotel_service_name);
        tv_hotel_service_name.getPaint().setFakeBoldText(true);
        service_lay1 = (LinearLayout) findViewById(R.id.service_lay1);
        service_lay2 = (LinearLayout) findViewById(R.id.service_lay2);

        tabHost = (TabHost) findViewById(R.id.tabHost);
        tab_day = (LinearLayout) findViewById(R.id.tab_day);
        tab_clock = (LinearLayout) findViewById(R.id.tab_clock);
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.getString("key") != null) {
                key = bundle.getString("key");
            }
            hotelId = bundle.getInt("hotelId");
        }
    }

    @Override
    public void initParams() {
        tv_center_title.setText(HotelOrderManager.getInstance().getCityStr() + "(" + HotelOrderManager.getInstance().getDateStr() + ")");
        tv_center_title.getPaint().setFakeBoldText(true);
        btn_right.setImageResource(R.drawable.iv_favorite_gray);
        btn_right.setVisibility(View.VISIBLE);
        super.initParams();

        tabHost.setup();
        for (int i = 0; i < SELECTED_BAR_COUNT; i++) {
            View view = LayoutInflater.from(this).inflate(R.layout.selected_bar_item, null);
            TextView tv_tab = (TextView) view.findViewById(R.id.tv_tab);
            if (i == 0) {
                if (HotelCommDef.YEGUIREN.equals(key)) {
                    tv_tab.setText("夜归人");
                } else {
                    tv_tab.setText("全日房");
                }
                tabHost.addTab(tabHost.newTabSpec("tab" + i).setIndicator(view).setContent(tab_day.getId()));
            } else {
                tv_tab.setText("钟点房");
                tabHost.addTab(tabHost.newTabSpec("tab" + i).setIndicator(view).setContent(tab_clock.getId()));
            }
        }
        if (HotelCommDef.CLOCK.equals(key)) {
            tabHost.setCurrentTab(1);
        } else {
            tabHost.setCurrentTab(0);
        }
        updateTab(tabHost);

        requestHotelDetailInfo();
    }

    @Override
    public void initListeners() {
        super.initListeners();
        btn_right.setOnClickListener(this);
        tv_map.setOnClickListener(this);
        tv_service.setOnClickListener(this);
        tv_view_comment.setOnClickListener(this);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                int newPosition = position % hotelDetailInfoBean.picPath.size();
                indicator_lay.getChildAt(newPosition).setEnabled(true);
                if (positionIndex != newPosition) {
                    indicator_lay.getChildAt(positionIndex).setEnabled(false);
                    positionIndex = newPosition;
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                tabHost.setCurrentTabByTag(tabId);
                updateTab(tabHost);
            }
        });
        tv_assess_point.setOnClickListener(this);
        tv_comment.setOnClickListener(this);
    }

    private void requestHotelVip(int hotelId) {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("hotelId", String.valueOf(hotelId));

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.HOTEL_VIP;
        d.flag = AppConst.HOTEL_VIP;

        if (!isProgressShowing()) {
            showProgressDialog(this);
        }

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void requestHotelDetailInfo() {
        System.out.println("requestHotelDetailInfo()");
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("hotelId", String.valueOf(hotelId));
        b.addBody("beginDate", String.valueOf(HotelOrderManager.getInstance().getBeginTime()));
        b.addBody("endDate", String.valueOf(HotelOrderManager.getInstance().getEndTime()));
        b.addBody("type", String.valueOf(HotelOrderManager.getInstance().getHotelType()));

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.HOTEL_DETAIL;
        d.flag = AppConst.HOTEL_DETAIL;

        if (!isProgressShowing()) {
            showProgressDialog(this);
        }

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void refreshRoomListInfo() {
        if (null != hotelDetailInfoBean) {
            root_detail_lay.setVisibility(View.VISIBLE);
            //设置banner
            int marginValue = Utils.dip2px(10);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                marginValue = Utils.dip2px(5);
            }
            viewPager.setPageMargin(marginValue);
            viewPager.setAdapter(new MyPagerAdapter(this, hotelDetailInfoBean.picPath));
            initIndicatorLay(hotelDetailInfoBean.picPath.size());

            //TODO viewPager一个假的无限循环，初始位置是viewPager count的100倍
            viewPager.setCurrentItem(hotelDetailInfoBean.picPath.size() * 100);
            if (hotelDetailInfoBean.picPath.size() > 0) {
                int offset = 0;
                if (hotelDetailInfoBean.picPath.size() == 1) {
                    offset = 1;
                }
                viewPager.setOffscreenPageLimit(hotelDetailInfoBean.picPath.size() + offset);
            }
            //设置评分、等级、评论
            float grade = 0;
            try {
                grade = Float.parseFloat(hotelDetailInfoBean.grade);
            } catch (Exception e) {
                e.printStackTrace();
            }
            tv_assess_point.setText(String.valueOf(grade));
            assess_star_lay.setColorStars((int) grade);

            if ("0".equals(hotelDetailInfoBean.evaluateCount)) {
                tv_comment.setText("没有评论");
            } else {
                tv_comment.setText(hotelDetailInfoBean.evaluateCount + "条评论");
            }
            //设置酒店基本信息
            tv_hotel_name.setText(hotelDetailInfoBean.name);
            tv_hotel_add.setText(hotelDetailInfoBean.address);
            tv_hotel_phone.setText(hotelDetailInfoBean.phone);
            updateSpaceAreaStatus(hotelDetailInfoBean.hasArticle);
            //设置房间列表信息
            updateRoomListData();
            //设置酒店服务信息
            refreshHotelServiceInfo();

        } else {
            root_detail_lay.setVisibility(View.GONE);
        }
    }

    private void updateSpaceAreaStatus(boolean isFlag) {
        if (isFlag) {
            tv_view_comment.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.iv_message_yellow2), null, null, null);
        } else {
            tv_view_comment.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.iv_message_yellow), null, null, null);
        }
    }

    /**
     * 更新Tab标签的颜色，和字体的颜色
     *
     * @param tabHost
     */
    private void updateTab(final TabHost tabHost) {
        for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
            View view = tabHost.getTabWidget().getChildAt(i);
            if (i == 0) {
                view.setBackgroundResource(R.drawable.comm_rectangle_selected_bar_left_bg);
            } else if (i == tabHost.getTabWidget().getChildCount() - 1) {
                view.setBackgroundResource(R.drawable.comm_rectangle_selected_bar_right_bg);
            } else {
                view.setBackgroundResource(R.drawable.comm_rectangle_selected_bar_middle_bg);
            }
            TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(R.id.tv_tab);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            tv.setTextColor(getResources().getColorStateList(R.color.white));
        }
    }

    private void updateRoomListData() {
        //全日房
        tab_day.removeAllViews();
        for (int i = 0; i < hotelDetailInfoBean.roomList.size(); i++) {
            View view = LayoutInflater.from(this).inflate(R.layout.lv_room_item, null);
            LinearLayout content_layout = (LinearLayout) view.findViewById(R.id.content_layout);
            RoundedLeftImageView iv_icon = (RoundedLeftImageView) view.findViewById(R.id.iv_icon);
            TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
            TextView tv_price_note = (TextView) view.findViewById(R.id.tv_price_note);
            TextView tv_price = (TextView) view.findViewById(R.id.tv_price);
            tv_price.getPaint().setFakeBoldText(true);
            LinearLayout clock_lay = (LinearLayout) view.findViewById(R.id.clock_lay);
            LinearLayout time_lay = (LinearLayout) view.findViewById(R.id.time_lay);
            LinearLayout durning_lay = (LinearLayout) view.findViewById(R.id.durning_lay);
            durning_lay.setVisibility(View.GONE);
            loadImage(iv_icon, R.drawable.def_room_list, hotelDetailInfoBean.roomList.get(i).picPath, 800, 480);
            tv_title.setText(hotelDetailInfoBean.roomList.get(i).name);

            if (HotelCommDef.YEGUIREN.equals(key)) {
                if ("夜归人".equals(hotelDetailInfoBean.roomList.get(i).priceType)) {
                    int ygrRoomCount = 0;
                    try {
                        ygrRoomCount = Integer.parseInt(hotelDetailInfoBean.roomList.get(i).yeguirenRoomCount);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    tv_title.append(" " + String.format(getResources().getString(R.string.ygrRoomCount), ygrRoomCount));
                    clock_lay.setVisibility(View.VISIBLE);
                    ((TextView) time_lay.findViewById(R.id.tv_time_label)).setText("入住时段：");
                    ((TextView) time_lay.findViewById(R.id.tv_time)).setText(hotelDetailInfoBean.roomList.get(i).yeguirenRoomTime);
                    content_layout.setBackgroundResource(R.drawable.comm_gradient_ygr_color);
                    tv_title.setTextColor(getResources().getColor(R.color.white));
                    tv_price_note.setTextColor(getResources().getColor(R.color.white));
                    tv_price.setTextColor(getResources().getColor(R.color.white));
                } else {
                    clock_lay.setVisibility(View.GONE);
                    content_layout.setBackgroundResource(0);
                    tv_title.setTextColor(getResources().getColor(R.color.registerhintColor));
                    tv_price_note.setTextColor(getResources().getColor(R.color.registerhintColor));
                    tv_price.setTextColor(getResources().getColor(R.color.indicatorColor));
                }
            } else {
                clock_lay.setVisibility(View.GONE);
            }
            tv_price_note.setText(hotelDetailInfoBean.roomList.get(i).priceType + "：");
            tv_price.setText(hotelDetailInfoBean.roomList.get(i).price + "元");

            final int finalI = i;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    roomId = hotelDetailInfoBean.roomList.get(finalI).id;
                    isClickYgr = "夜归人".equals(hotelDetailInfoBean.roomList.get(finalI).priceType);
                    roomType = Integer.valueOf(hotelDetailInfoBean.roomList.get(finalI).searchType);
                    requestCheckRoomEmpty(hotelId, roomId, roomType);
                }
            });
            tab_day.addView(view);
        }

        //钟点房
        tab_clock.removeAllViews();
        for (int i = 0; i < hotelDetailInfoBean.clockRoomList.size(); i++) {
            View view = LayoutInflater.from(this).inflate(R.layout.lv_room_item, null);
            RoundedLeftImageView iv_icon = (RoundedLeftImageView) view.findViewById(R.id.iv_icon);
            TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
            TextView tv_price_note = (TextView) view.findViewById(R.id.tv_price_note);
            TextView tv_price = (TextView) view.findViewById(R.id.tv_price);
            tv_price.getPaint().setFakeBoldText(true);
            LinearLayout clock_lay = (LinearLayout) view.findViewById(R.id.clock_lay);
            clock_lay.setVisibility(View.VISIBLE);
            TextView tv_time = (TextView) view.findViewById(R.id.tv_time);
            TextView tv_during = (TextView) view.findViewById(R.id.tv_during);
            loadImage(iv_icon, R.drawable.def_room_list, hotelDetailInfoBean.clockRoomList.get(i).picPath, 800, 480);
            tv_title.setText(hotelDetailInfoBean.clockRoomList.get(i).name);
            tv_price_note.setText(hotelDetailInfoBean.roomList.get(i).priceType + "：");
            tv_price.setText(hotelDetailInfoBean.clockRoomList.get(i).price + "元");
            tv_time.setText(hotelDetailInfoBean.clockRoomList.get(i).roomTime);
            tv_during.setText(Float.valueOf(hotelDetailInfoBean.clockRoomList.get(i).roomDuration) + "小时");
            final int finalI = i;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    roomId = hotelDetailInfoBean.clockRoomList.get(finalI).id;
//                    roomType = HotelCommDef.TYPE_CLOCK;
                    roomType = Integer.valueOf(hotelDetailInfoBean.clockRoomList.get(finalI).searchType);
                    requestCheckRoomEmpty(hotelId, roomId, roomType);
                }
            });
            tab_clock.addView(view);
        }
    }

    private void requestCheckRoomEmpty(int hotelId, int roomId, int roomType) {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("beginDate", String.valueOf(HotelOrderManager.getInstance().getBeginTime()));
        b.addBody("endDate", String.valueOf(HotelOrderManager.getInstance().getEndTime()));
        b.addBody("hotelId", String.valueOf(hotelId));
        b.addBody("roomId", String.valueOf(roomId));
        b.addBody("type", String.valueOf(roomType));

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.CHECK_ROOM_EMPTY;
        d.flag = AppConst.CHECK_ROOM_EMPTY;
        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void refreshHotelServiceInfo() {
        if (hotelDetailInfoBean != null) {
            tv_hotel_service_name.setText(hotelDetailInfoBean.name);
            if (hotelDetailInfoBean.star > 0) {
                tv_hotel_service_name.append("（" + HotelCommDef.convertHotelStar(hotelDetailInfoBean.star) + "）");
            }

            {
                service_lay1.removeAllViews();
                View view1 = LayoutInflater.from(this).inflate(R.layout.room_service_layout_item, null);
                TextView tv_service_layout_title = (TextView) view1.findViewById(R.id.tv_service_layout_title);
                tv_service_layout_title.getPaint().setFakeBoldText(true);
                LinearLayout service_layout_item = (LinearLayout) view1.findViewById(R.id.service_layout_item);
                tv_service_layout_title.setVisibility(View.GONE);
                LinearLayout line_lay = (LinearLayout) view1.findViewById(R.id.line_lay);
                line_lay.setVisibility(View.GONE);
                service_layout_item.removeAllViews();
                //接宾资质
//                View itemView1 = LayoutInflater.from(this).inflate(R.layout.room_service_item, null);
//                ((TextView) itemView1.findViewById(R.id.tv_service_item_title)).setText("接宾资质");
//                ((TextView) itemView1.findViewById(R.id.tv_service_item_content)).setText(hotelDetailInfoBean.guestQualification);
//                service_layout_item.addView(itemView1);
                //房间规模
                View itemView2 = LayoutInflater.from(this).inflate(R.layout.room_service_item, null);
                ((TextView) itemView2.findViewById(R.id.tv_service_item_title)).setText("规模");
                if (StringUtil.notEmpty(hotelDetailInfoBean.scala) && hotelDetailInfoBean.scala > 0) {
                    ((TextView) itemView2.findViewById(R.id.tv_service_item_content)).setText(hotelDetailInfoBean.scala + "间房");
                }
                service_layout_item.addView(itemView2);
                //服务范围
                View itemView3 = LayoutInflater.from(this).inflate(R.layout.room_service_item, null);
                ((TextView) itemView3.findViewById(R.id.tv_service_item_title)).setText("服务范围");
                ((TextView) itemView3.findViewById(R.id.tv_service_item_content)).setText(hotelDetailInfoBean.typeByService);
                service_layout_item.addView(itemView3);

                service_lay1.addView(view1);
            }
            {
                service_lay2.removeAllViews();
                for (int i = 0; i < hotelDetailInfoBean.attachments.size(); i++) {
                    if (hotelDetailInfoBean.attachments.get(i).content != null && hotelDetailInfoBean.attachments.get(i).content.size() > 0) {
                        View view2 = LayoutInflater.from(this).inflate(R.layout.room_service_layout_item, null);
                        TextView tv_service_layout_title = (TextView) view2.findViewById(R.id.tv_service_layout_title);
                        tv_service_layout_title.getPaint().setFakeBoldText(true);
                        LinearLayout service_layout_item = (LinearLayout) view2.findViewById(R.id.service_layout_item);
                        tv_service_layout_title.setText(hotelDetailInfoBean.attachments.get(i).title);
                        service_layout_item.removeAllViews();
                        for (int j = 0; j < hotelDetailInfoBean.attachments.get(i).content.size(); j++) {
                            View itemView = null;
                            if ("酒店政策".equals(hotelDetailInfoBean.attachments.get(i).title)) {
                                itemView = LayoutInflater.from(this).inflate(R.layout.room_service_item2, null);
                            } else {
                                itemView = LayoutInflater.from(this).inflate(R.layout.room_service_item, null);
                            }
                            TextView tv_service_item_title = (TextView) itemView.findViewById(R.id.tv_service_item_title);
                            tv_service_item_title.getPaint().setFakeBoldText(true);
                            TextView tv_service_item_content = (TextView) itemView.findViewById(R.id.tv_service_item_content);
                            tv_service_item_title.setText(hotelDetailInfoBean.attachments.get(i).content.get(j).title);
                            tv_service_item_content.setText(hotelDetailInfoBean.attachments.get(i).content.get(j).content);
                            service_layout_item.addView(itemView);
                        }
                        service_lay2.addView(view2);
                    }
                }
            }
        }
    }

    private void showRecommendRoomListDialog(final RoomDetailCheckResultInfoBean bean) {
        if (bean != null) {
            final CustomDialog commendRoomListDialog = new CustomDialog(this);
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_recommend_roomlist, null);
            TextView tv_remark = (TextView) view.findViewById(R.id.tv_remark);
            ListView listview = (ListView) view.findViewById(R.id.listview);
            if (bean.roomlist != null && bean.roomlist.size() > 1) {
                LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                llp.height = Utils.dip2px(120);
                listview.setLayoutParams(llp);
            }
            //没有数据时，显示空view提示
            TextView emptyView = new TextView(this);
            emptyView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            emptyView.setText("暂无推荐");
            emptyView.setTextSize(14);
            emptyView.setPadding(0, Utils.dip2px(15), 0, Utils.dip2px(15));
            emptyView.setGravity(Gravity.CENTER);
            emptyView.setTextColor(getResources().getColor(R.color.selectedBarColor));
            emptyView.setVisibility(View.GONE);
            ((ViewGroup) listview.getParent()).addView(emptyView);
            listview.setEmptyView(emptyView);

            tv_remark.setText(bean.remark);
            tv_remark.append("，请选择其他房型：");
            listview.setAdapter(new MyRecommendRoomListAdapter(this, bean.roomlist));
            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    commendRoomListDialog.dismiss();
                    Intent intent = new Intent(RoomListActivity.this, RoomDetailActivity.class);
                    intent.putExtra("hotelId", hotelId);
                    intent.putExtra("roomId", bean.roomlist.get(position).id);
                    intent.putExtra("roomType", roomType);
                    startActivity(intent);
                }
            });

            commendRoomListDialog.addView(view);
            commendRoomListDialog.setTitle("推荐");
            commendRoomListDialog.setCanceledOnTouchOutside(true);
            commendRoomListDialog.show();
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        Intent intent = null;
        switch (v.getId()) {
            case R.id.btn_right:
                System.out.println("right button onclick");
                requestHotelVip(hotelId);
                break;
            case R.id.tv_map:
                if (hotelDetailInfoBean != null) {
                    intent = new Intent(this, HotelMapActivity.class);
                    intent.putExtra("bean", hotelDetailInfoBean);
                }
                break;
            case R.id.tv_service:
                if (hotelDetailInfoBean != null) {
                    if (StringUtil.notEmpty(hotelDetailInfoBean.phone)) {
                        Uri dialUri = Uri.parse("tel:" + hotelDetailInfoBean.phone);
                        intent = new Intent(Intent.ACTION_DIAL, dialUri);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    }
                }
                break;
            case R.id.tv_view_comment:
                if (hotelDetailInfoBean != null) {
                    updateSpaceAreaStatus(false);
                    intent = new Intent(this, HotelSpaceHomeActivity.class);
                }
                break;
            case R.id.tv_comment:
            case R.id.tv_assess_point:
                if (hotelDetailInfoBean != null) {
                    intent = new Intent(this, AssessCommendActivity.class);
                    intent.putExtra("hotelId", HotelOrderManager.getInstance().getHotelDetailInfo().id);
                }
                break;
            default:
                break;
        }
        if (null != intent) {
            startActivity(intent);
        }
    }

    private void initIndicatorLay(int count) {
        indicator_lay.removeAllViews();
        System.out.println("count = " + count);
        if (count >= 1) {
            for (int i = 0; i < count; i++) {
                View view = new View(this);
                view.setBackgroundResource(R.drawable.banner_indicator_indicator_bg);
                view.setEnabled(false);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Utils.dip2px(7), Utils.dip2px(7));
                if (i > 0) {
                    lp.leftMargin = Utils.dip2px(7);
                }
                view.setLayoutParams(lp);
                indicator_lay.addView(view);
            }
            indicator_lay.getChildAt(positionIndex).setEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    class MyPagerAdapter extends PagerAdapter {
        private Context context;
        private List<String> list = new ArrayList<>();

        public MyPagerAdapter(Context context, List<String> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public int getCount() {
//            return list.size();
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
//            View view = LayoutInflater.from(context).inflate(R.layout.room_banner_item, null);
//            ImageView iv_background = (ImageView) view.findViewById(R.id.iv_background);
//            loadImage(iv_background, list.get(position), 800, 480);
//            container.addView(view, position);
//            return view;
            position %= list.size();
            if (position < 0) {
                position = list.size() + position;
            }
            final View view = LayoutInflater.from(context).inflate(R.layout.room_banner_item, null);
            ImageView iv_background = (ImageView) view.findViewById(R.id.iv_background);
            loadImage(iv_background, R.drawable.def_hotel_banner, list.get(position), 1920, 1080);
            //如果View已经在之前添加到了一个父组件，则必须先remove，否则会抛出IllegalStateException。
            ViewParent vp = view.getParent();
            if (vp != null) {
                ViewGroup parent = (ViewGroup) vp;
                parent.removeView(view);
            }
            container.addView(view);
            //add listeners here if necessary
            final int finalPosition = position;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ImageScaleActivity.class);
                    intent.putExtra("url", list.get(finalPosition));
                    ImageView imageView = (ImageView) view.findViewById(R.id.iv_background);
                    int[] location = new int[2];
                    imageView.getLocationOnScreen(location);
                    intent.putExtra("locationX", location[0]);//必须
                    intent.putExtra("locationY", location[1]);//必须
                    intent.putExtra("width", imageView.getWidth());//必须
                    intent.putExtra("height", imageView.getHeight());//必须
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            });
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
//            container.removeView((View) object);
            //Warning：不要在这里调用removeView
        }
    }

    @Override
    public void preExecute(ResponseData request) {

    }

    @Override
    public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.HOTEL_DETAIL) {
                removeProgressDialog();
                System.out.println("hoteldetail json = " + response.body.toString());
                hotelDetailInfoBean = JSON.parseObject(response.body.toString(), HotelDetailInfoBean.class);
                HotelOrderManager.getInstance().setHotelDetailInfo(hotelDetailInfoBean);
                refreshRoomListInfo();
            } else if (request.flag == AppConst.HOTEL_VIP) {
                removeProgressDialog();
                System.out.println("Json = " + response.body.toString());
                CustomToast.show("您已成为该酒店会员", CustomToast.LENGTH_SHORT);
            } else if (request.flag == AppConst.CHECK_ROOM_EMPTY) {
                removeProgressDialog();
                System.out.println("json = " + response.body.toString());
                RoomDetailCheckResultInfoBean bean = JSON.parseObject(response.body.toString(), RoomDetailCheckResultInfoBean.class);
                if ("false".equals(bean.status)) {
                    showRecommendRoomListDialog(bean);
                } else {
                    Intent intent = new Intent(this, RoomDetailActivity.class);
                    intent.putExtra("hotelId", hotelId);
                    intent.putExtra("roomId", roomId);
                    intent.putExtra("roomType", roomType);
                    intent.putExtra("isYgr", isClickYgr);
                    startActivity(intent);
                }
            }
        }
    }

    class MyRecommendRoomListAdapter extends BaseAdapter {
        private Context context;
        private List<RoomListInfoBean> list = new ArrayList<>();

        public MyRecommendRoomListAdapter(Context context, List<RoomListInfoBean> list) {
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
                convertView = LayoutInflater.from(context).inflate(R.layout.lv_recommend_room_item, null);
                viewHolder.iv_icon = (RoundedLeftImageView) convertView.findViewById(R.id.iv_icon);
                viewHolder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
                viewHolder.tv_price_note = (TextView) convertView.findViewById(R.id.tv_price_note);
                viewHolder.tv_price = (TextView) convertView.findViewById(R.id.tv_price);
                viewHolder.tv_price.getPaint().setFakeBoldText(true);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            loadImage(viewHolder.iv_icon, R.drawable.def_room_list, list.get(position).picPath, 800, 480);
            viewHolder.tv_title.setText(list.get(position).name);
            viewHolder.tv_price_note.setText(list.get(position).priceType + "：");
            viewHolder.tv_price.setText(list.get(position).price + "元");

            return convertView;
        }

        class ViewHolder {
            RoundedLeftImageView iv_icon;
            TextView tv_title;
            TextView tv_price_note;
            TextView tv_price;
        }
    }
}