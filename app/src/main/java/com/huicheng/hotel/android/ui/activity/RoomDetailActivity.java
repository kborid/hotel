package com.huicheng.hotel.android.ui.activity;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.control.ShareControl;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.net.bean.RoomConfirmInfoBean;
import com.huicheng.hotel.android.net.bean.RoomDetailInfoBean;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.CommonAddSubLayout;
import com.huicheng.hotel.android.ui.custom.CommonAssessStarsLayout;
import com.huicheng.hotel.android.ui.custom.MyGridViewWidget;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.image.ImageLoader;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.CustomToast;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author kborid
 * @date 2017/1/3 0003
 */
public class RoomDetailActivity extends BaseActivity implements DataCallback {
    private static final int SELECTED_BAR_COUNT = 2;

    private LinearLayout root_detail_lay;
    private ViewPager viewPager;
    private LinearLayout indicator_lay;
    private int positionIndex = 0;

    private RelativeLayout point_lay;
    private TextView tv_point, tv_comment, tv_room_name;
    private CommonAssessStarsLayout assess_star_lay;

    private TabHost tabHost;
    private TextView tv_date, tv_during, tv_pay_type, tv_price;

    private LinearLayout service_lay;

    private LinearLayout choose_service_lay;
    private LinearLayout more_lay;
    private ImageView iv_more_down;

    private LinearLayout free_service_lay;

    private ImageView iv_share, iv_favorite;
    private TextView tv_more;
    private TextView tv_confirm;
    private boolean isShowMore = false;
    private boolean hasShowAll = false;
    private int roomPrice = 0;
    private int allChooseServicePrice = 0;
    private Map<String, RoomConfirmInfoBean> chooseServiceInfoMap = new HashMap<>();

    private TextView tv_total_price;

    private boolean isYgr = false;
    private int hotelId, room_type = -1, roomId;
    private String hotelName;
    private RoomDetailInfoBean roomDetailInfoBean = null;
    private Bitmap thumb = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_roomdetail_layout);

        initViews();
        initParams();
        initListeners();
        requestRoomDetailInfo();
    }

    @Override
    public void initViews() {
        super.initViews();
        root_detail_lay = (LinearLayout) findViewById(R.id.root_detail_lay);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        indicator_lay = (LinearLayout) findViewById(R.id.indicator_lay);

        point_lay = (RelativeLayout) findViewById(R.id.point_lay);
        tv_point = (TextView) findViewById(R.id.tv_point);
        tv_point.getPaint().setFakeBoldText(true);
        tv_comment = (TextView) findViewById(R.id.tv_comment);
        tv_comment.getPaint().setFakeBoldText(true);
        tv_room_name = (TextView) findViewById(R.id.tv_room_name);
        tv_room_name.getPaint().setFakeBoldText(true);
        assess_star_lay = (CommonAssessStarsLayout) findViewById(R.id.assess_star_lay);

        tabHost = (TabHost) findViewById(R.id.tabHost);
        ((TextView) findViewById(R.id.tv_price_note)).getPaint().setFakeBoldText(true);
        tv_date = (TextView) findViewById(R.id.tv_date);
        tv_date.getPaint().setFakeBoldText(true);
        tv_during = (TextView) findViewById(R.id.tv_during);
        tv_during.getPaint().setFakeBoldText(true);
        tv_pay_type = (TextView) findViewById(R.id.tv_pay_type);
        tv_price = (TextView) findViewById(R.id.tv_price);
        tv_price.getPaint().setFakeBoldText(true);

        service_lay = (LinearLayout) findViewById(R.id.service_lay);

        ((TextView) findViewById(R.id.tv_choose_service_note)).getPaint().setFakeBoldText(true);
        choose_service_lay = (LinearLayout) findViewById(R.id.choose_service_lay);
        more_lay = (LinearLayout) findViewById(R.id.more_lay);
        tv_more = (TextView) findViewById(R.id.tv_more);
        tv_more.getPaint().setFakeBoldText(true);
        iv_more_down = (ImageView) findViewById(R.id.iv_more_down);
        ((TextView) findViewById(R.id.tv_free_service_note)).getPaint().setFakeBoldText(true);
        free_service_lay = (LinearLayout) findViewById(R.id.free_service_lay);

        tv_total_price = (TextView) findViewById(R.id.tv_total_price);
        tv_total_price.getPaint().setFakeBoldText(true);
        tv_confirm = (TextView) findViewById(R.id.tv_confirm);
        tv_confirm.getPaint().setFakeBoldText(true);

        iv_share = (ImageView) findViewById(R.id.iv_share);
        iv_favorite = (ImageView) findViewById(R.id.iv_favorite);
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            hotelId = bundle.getInt("hotelId");
            room_type = bundle.getInt("roomType");
            roomId = bundle.getInt("roomId");
            if (bundle.getString("hotelName") != null) {
                hotelName = bundle.getString("hotelName");
            } else {
                hotelName = HotelOrderManager.getInstance().getHotelDetailInfo().name;
            }
            if (bundle.getBoolean("isYgr")) {
                isYgr = bundle.getBoolean("isYgr");
            }
        }
    }

    @Override
    public void initParams() {
        super.initParams();
        btn_back.setImageResource(R.drawable.iv_back_white);

        tabHost.setup();
        for (int i = 0; i < SELECTED_BAR_COUNT; i++) {
            View view = LayoutInflater.from(this).inflate(R.layout.selected_bar_item, null);
            if (i == 1) {
                tabHost.addTab(tabHost.newTabSpec("tab" + i).setIndicator(view).setContent(R.id.tab_pre));
            } else {
                tabHost.addTab(tabHost.newTabSpec("tab" + i).setIndicator(view).setContent(R.id.tab_on));
            }
        }
        tabHost.setCurrentTab(0);
        thumb = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
    }

    @Override
    public void initListeners() {
        super.initListeners();
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                indicator_lay.getChildAt(position).setEnabled(true);
                indicator_lay.getChildAt(positionIndex).setEnabled(false);
                positionIndex = position;
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                if (viewPagerContainer != null) {
//                    viewPagerContainer.invalidate();
//                }
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                System.out.println("tabid = " + tabId);
                if (null != roomDetailInfoBean) {
                    tabHost.setCurrentTabByTag(tabId);
                    updateTab(tabHost);
                    refreshServiceLayout();
                }
            }
        });
        more_lay.setOnClickListener(this);
        tv_confirm.setOnClickListener(this);
        iv_share.setOnClickListener(this);
        iv_favorite.setOnClickListener(this);
        point_lay.setOnClickListener(this);
    }

    private void initIndicatorLay(int count) {
        indicator_lay.removeAllViews();
        if (count > 1) {
            for (int i = 0; i < count; i++) {
                View view = new View(this);
                view.setBackgroundResource(R.drawable.indicator_selector);
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

    private void requestRoomDetailInfo() {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("beginDate", String.valueOf(HotelOrderManager.getInstance().getBeginTime(isYgr)));
        b.addBody("endDate", String.valueOf(HotelOrderManager.getInstance().getEndTime(isYgr)));
        b.addBody("hotelId", String.valueOf(hotelId));
        b.addBody("roomId", String.valueOf(roomId));
        b.addBody("type", String.valueOf(room_type));

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.ROOM_DETAIL;
        d.flag = AppConst.ROOM_DETAIL;
        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void refreshRoomDetailInfo() {
        if (null != roomDetailInfoBean) {
            root_detail_lay.setVisibility(View.VISIBLE);
            //设置banner
//            viewPager.setPageMargin(Utils.dip2px(10));
            viewPager.setAdapter(new MyPagerAdapter(this, roomDetailInfoBean.picList));
            // to cache all page, or we will see the right item delayed
            if (roomDetailInfoBean.picList.size() > 0) {
                viewPager.setOffscreenPageLimit(roomDetailInfoBean.picList.size());
            }
            initIndicatorLay(roomDetailInfoBean.picList.size());

            //设置title、评分信息
            tv_room_name.setText(roomDetailInfoBean.roomName);
            float grade = 0;
            try {
                grade = Float.parseFloat(roomDetailInfoBean.grade);
            } catch (Exception e) {
                e.printStackTrace();
            }
            tv_point.setText(String.valueOf(grade));
            assess_star_lay.setColorStars((int) grade);

            if ("0".equals(roomDetailInfoBean.evaluateCount)) {
                tv_comment.setText("没有评论");
            } else {
                tv_comment.setText(roomDetailInfoBean.evaluateCount + "条");
            }

            //设置付款信息
            if (roomDetailInfoBean.totalPriceList != null) {
                tabHost.getTabWidget().getChildAt(0).setEnabled(true);
                ((TextView) tabHost.getTabWidget().getChildAt(0).findViewById(R.id.tv_tab)).setText("到店付:" + roomDetailInfoBean.totalPrice + "元");
            } else {
                tabHost.getTabWidget().getChildAt(0).setEnabled(false);
                ((TextView) tabHost.getTabWidget().getChildAt(0).findViewById(R.id.tv_tab)).setText("到店付:不支持");
            }

            if (roomDetailInfoBean.preTotalPriceList != null) {
                tabHost.getTabWidget().getChildAt(1).setEnabled(true);
                ((TextView) tabHost.getTabWidget().getChildAt(1).findViewById(R.id.tv_tab)).setText("预付:" + roomDetailInfoBean.preTotalPrice + "元");
            } else {
                tabHost.getTabWidget().getChildAt(1).setEnabled(false);
                ((TextView) tabHost.getTabWidget().getChildAt(1).findViewById(R.id.tv_tab)).setText("预付:不支持");
            }

            tv_date.setText(DateUtil.getDay("MM月dd日", HotelOrderManager.getInstance().getBeginTime(isYgr)) + "-" + DateUtil.getDay("dd日", HotelOrderManager.getInstance().getEndTime(isYgr)));
            tv_during.setText(DateUtil.getGapCount(HotelOrderManager.getInstance().getBeginDate(isYgr), HotelOrderManager.getInstance().getEndDate(isYgr)) + "晚");
            updateTab(tabHost);

            //设置选购服务
            refreshServiceLayout();

            //设置房间所提供的服务
            refreshRoomDetailService();
        } else {
            root_detail_lay.setVisibility(View.GONE);
        }
    }

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

            if (tabHost.getCurrentTab() == 1) {
                tv_pay_type.setText("（预付）");
                tv_price.setText(roomDetailInfoBean.preTotalPrice + "元");
                roomPrice = roomDetailInfoBean.preTotalPrice;
                HotelOrderManager.getInstance().setPayType(HotelCommDef.PAY_PRE);
                if (roomDetailInfoBean.preTotalPriceList == null) {
                    tv_confirm.setEnabled(false);
                } else {
                    tv_confirm.setEnabled(true);
                }
            } else {
                tv_pay_type.setText("（到店付）");
                tv_price.setText(roomDetailInfoBean.totalPrice + "元");
                roomPrice = roomDetailInfoBean.totalPrice;
                HotelOrderManager.getInstance().setPayType(HotelCommDef.PAY_ARR);
                if (roomDetailInfoBean.totalPriceList == null) {
                    tv_confirm.setEnabled(false);
                } else {
                    tv_confirm.setEnabled(true);
                }
            }
        }
    }

    private void refreshRoomDetailService() {
        if (roomDetailInfoBean != null) {
            service_lay.removeAllViews();
            for (int i = 0; i < roomDetailInfoBean.serviceList.size(); i++) {
                View view = LayoutInflater.from(this).inflate(R.layout.roomdetail_service_item, null);
                TextView tv_detail_title = (TextView) view.findViewById(R.id.tv_detail_title);
                tv_detail_title.getPaint().setFakeBoldText(true);
                MyGridViewWidget gv_detail_room = (MyGridViewWidget) view.findViewById(R.id.gv_detail_room);
                TextView tv_detail_remark = (TextView) view.findViewById(R.id.tv_detail_remark);
                tv_detail_remark.getPaint().setFakeBoldText(true);

                tv_detail_title.setText(roomDetailInfoBean.serviceList.get(i).title);
                if (StringUtil.notEmpty(roomDetailInfoBean.serviceList.get(i).remarks)) {
                    tv_detail_remark.setVisibility(View.VISIBLE);
                    tv_detail_remark.setText(roomDetailInfoBean.serviceList.get(i).remarks);
                } else {
                    if (roomDetailInfoBean.serviceList.get(i).capacityVOList != null && roomDetailInfoBean.serviceList.get(i).capacityVOList.size() > 0) {
                        tv_detail_remark.setVisibility(View.GONE);
                    } else {
                        tv_detail_remark.setVisibility(View.VISIBLE);
                        tv_detail_remark.setText("无");
                    }
                }

                if (roomDetailInfoBean.serviceList.get(i).capacityVOList != null && roomDetailInfoBean.serviceList.get(i).capacityVOList.size() > 0) {
                    gv_detail_room.setVisibility(View.VISIBLE);
                    RoomServiceGridViewAdapter adapter = new RoomServiceGridViewAdapter(this, roomDetailInfoBean.serviceList.get(i).capacityVOList);
                    gv_detail_room.setAdapter(adapter);
                } else {
                    gv_detail_room.setVisibility(View.GONE);
                }
                service_lay.addView(view);
            }
        }
    }


    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.more_lay:
                expendedService(hasShowAll);
                break;
            case R.id.tv_confirm: {
                Intent intent = new Intent(this, RoomOrderConfirmActivity.class);
                intent.putExtra("roomDetailInfo", roomDetailInfoBean);
                intent.putExtra("chooseServiceInfo", (Serializable) chooseServiceInfoMap);
                intent.putExtra("roomPrice", roomPrice);
                intent.putExtra("allChooseServicePrice", allChooseServicePrice);
                intent.putExtra("hotelId", hotelId);
                intent.putExtra("roomId", roomId);
                intent.putExtra("isYgr", isYgr);
                startActivity(intent);
                break;
            }
            case R.id.iv_share:
                HashMap<String, String> params = new HashMap<>();
                params.put("type", "room");
                params.put("hotelID", String.valueOf(HotelOrderManager.getInstance().getHotelDetailInfo().id));
                params.put("roomID", String.valueOf(roomId));
                params.put("userID", SessionContext.mUser.user.userid);
                params.put("hotelType", String.valueOf(room_type));
                params.put("beginDate", String.valueOf(HotelOrderManager.getInstance().getBeginTime(isYgr)));
                params.put("endDate", String.valueOf(HotelOrderManager.getInstance().getEndTime(isYgr)));
                params.put("channel", HotelCommDef.SHARE_ROOM);
                String url = SessionContext.getUrl(NetURL.SHARE, params);

                UMWeb web = new UMWeb(url);
                web.setTitle(roomDetailInfoBean.hotelName + " " + roomDetailInfoBean.roomName);
                if (ImageLoader.getInstance().getCacheBitmap(roomDetailInfoBean.picList.get(0)) != null) {
                    thumb = ImageLoader.getInstance().getCacheBitmap(roomDetailInfoBean.picList.get(0));
                }
                web.setThumb(new UMImage(this, thumb));
                web.setDescription(tv_date.getText().toString() + " " + tv_during.getText().toString() + "\n共计：" + tv_price.getText().toString());
                ShareControl.getInstance(this).openShareDisplay(web);
                break;
            case R.id.iv_favorite:
                requestHotelVip(HotelOrderManager.getInstance().getHotelDetailInfo().id);
                break;
            case R.id.point_lay: {
                Intent intent = new Intent(this, AssessCommendActivity.class);
                intent.putExtra("hotelId", HotelOrderManager.getInstance().getHotelDetailInfo().id);
                startActivity(intent);
                break;
            }
            default:
                break;
        }
    }

    private void expendedService(boolean flag) {
        if (flag) {
            hasShowAll = false;
            tv_more.setText("更多");
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(iv_more_down, "rotation", 180f, 360f);
            objectAnimator.setDuration(300);
            objectAnimator.start();
            for (int i = 3; i < roomDetailInfoBean.chooseList.size(); i++) {
                choose_service_lay.getChildAt(i).setVisibility(View.GONE);
            }
        } else {
            hasShowAll = true;
            tv_more.setText("收起");
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(iv_more_down, "rotation", 0f, 180f);
            objectAnimator.setDuration(300);
            objectAnimator.start();
            for (int i = 3; i < roomDetailInfoBean.chooseList.size(); i++) {
                choose_service_lay.getChildAt(i).setVisibility(View.VISIBLE);
            }
        }
    }

    private void refreshServiceLayout() {

        //刷新前，初始化
        allChooseServicePrice = 0;
        tv_total_price.setText(allChooseServicePrice + roomPrice + "元");
        more_lay.setVisibility(View.GONE);
        isShowMore = false;
        hasShowAll = true;
        chooseServiceInfoMap.clear();
        choose_service_lay.removeAllViews();
        free_service_lay.removeAllViews();

        final List<RoomDetailInfoBean.ChooseService> chooseList, freeChooseList;
        if (tabHost.getCurrentTab() == 1) {
            chooseList = roomDetailInfoBean.chooseList;
            freeChooseList = roomDetailInfoBean.chooseList_free;
        } else {
            chooseList = roomDetailInfoBean.offlineChooseList;
            freeChooseList = roomDetailInfoBean.offlineChooseList_free;
        }

        int choose_service_size = chooseList.size();
        if (choose_service_size > 0) {
            findViewById(R.id.tv_choose_service_note).setVisibility(View.VISIBLE);
            if (choose_service_size >= 3) {
                isShowMore = true;
                hasShowAll = false;
            }
            chooseServiceInfoMap = new HashMap<>();
            choose_service_lay.removeAllViews();
            for (int i = 0; i < choose_service_size; i++) {
                View view = LayoutInflater.from(this).inflate(R.layout.lv_choose_service_item, null);
                TextView tv_choose_service_title = (TextView) view.findViewById(R.id.tv_choose_service_title);
                tv_choose_service_title.getPaint().setFakeBoldText(true);
                final TextView tv_choose_service_price = (TextView) view.findViewById(R.id.tv_choose_service_price);
                final TextView tv_choose_service_total_price = (TextView) view.findViewById(R.id.tv_choose_service_total_price);
                tv_choose_service_total_price.getPaint().setFakeBoldText(true);
                CommonAddSubLayout addSubLayout = (CommonAddSubLayout) view.findViewById(R.id.addSubLayout);
                addSubLayout.setUnit("份");
                addSubLayout.setMaxvalue(chooseList.get(i).limitCnt);
                System.out.println("limit count = " + chooseList.get(i).limitCnt);
                tv_choose_service_title.setText(chooseList.get(i).serviceName);
                tv_choose_service_price.setText((HotelOrderManager.getInstance().isVipHotel() ? chooseList.get(i).vipPrice : chooseList.get(i).price) + "元/份");
                tv_choose_service_total_price.setText("0元");
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                lp.topMargin = Utils.dip2px(17);
                if (i >= 3 && isShowMore) {
                    more_lay.setVisibility(View.VISIBLE);
                    view.setVisibility(View.GONE);
                }
                final int finalI = i;
                final RoomConfirmInfoBean bean = new RoomConfirmInfoBean();
                addSubLayout.setOnCountChangedListener(new CommonAddSubLayout.OnCountChangedListener() {
                    @Override
                    public void onCountChanged(int count) {
                        bean.serviceId = chooseList.get(finalI).id;
                        bean.serviceTitle = chooseList.get(finalI).serviceName;
                        bean.serviceCount = count;
                        bean.servicePrice = HotelOrderManager.getInstance().isVipHotel() ? chooseList.get(finalI).vipPrice : chooseList.get(finalI).price;
                        bean.serviceTotalPrice = (HotelOrderManager.getInstance().isVipHotel() ? chooseList.get(finalI).vipPrice : chooseList.get(finalI).price) * count;
                        tv_choose_service_total_price.setText(bean.serviceTotalPrice + "元");
                        System.out.println("choose service price = " + bean.serviceTotalPrice);

                        chooseServiceInfoMap.put(bean.serviceTitle, bean);

                        // 计算总价
                        allChooseServicePrice = constChooseServicePrice();
                        tv_total_price.setText(allChooseServicePrice + roomPrice + "元");
                    }
                });
                choose_service_lay.addView(view, lp);
            }
        } else {
            findViewById(R.id.tv_choose_service_note).setVisibility(View.GONE);
        }

        if (freeChooseList.size() > 0) {
            findViewById(R.id.tv_free_service_note).setVisibility(View.VISIBLE);
            free_service_lay.removeAllViews();
            TextView tv_content = new TextView(this);
            tv_content.setTextSize(15);
            tv_content.setTextColor(getResources().getColor(R.color.lableColor));
            tv_content.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            for (int i = 0; i < freeChooseList.size(); i++) {
//                View view = LayoutInflater.from(this).inflate(R.layout.lv_choose_service_item, null);
//                TextView tv_choose_service_title = (TextView) view.findViewById(R.id.tv_choose_service_title);
//                tv_choose_service_title.getPaint().setFakeBoldText(true);
//                final TextView tv_choose_service_price = (TextView) view.findViewById(R.id.tv_choose_service_price);
//                final TextView tv_choose_service_total_price = (TextView) view.findViewById(R.id.tv_choose_service_total_price);
//                tv_choose_service_total_price.getPaint().setFakeBoldText(true);
//                CommonAddSubLayout addSubLayout = (CommonAddSubLayout) view.findViewById(R.id.addSubLayout);
//                addSubLayout.setUnit("份");
//                addSubLayout.setMaxvalue(freeChooseList.get(i).limitCnt);
//                addSubLayout.setCount(freeChooseList.get(i).limitCnt);
//                addSubLayout.setButtonEnable(false);
//                System.out.println("limit count = " + freeChooseList.get(i).limitCnt);
//                tv_choose_service_title.setText(freeChooseList.get(i).serviceName);
//                tv_choose_service_price.setText((HotelOrderManager.getInstance().isVipHotel() ? freeChooseList.get(i).vipPrice : freeChooseList.get(i).price) + "元/份");
//                tv_choose_service_total_price.setText("0元");
//                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//                lp.topMargin = Utils.dip2px(17);
//                free_service_lay.addView(view, lp);

                tv_content.append(freeChooseList.get(i).serviceName);
                tv_content.append("*");
                tv_content.append(String.valueOf(freeChooseList.get(i).limitCnt));
                if (i != freeChooseList.size() - 1) {
                    tv_content.append("，");
                }
            }
            free_service_lay.setPadding(0, Utils.dip2px(17), 0, 0);
            free_service_lay.addView(tv_content);
        } else {
            findViewById(R.id.tv_free_service_note).setVisibility(View.GONE);
        }
    }

    private int constChooseServicePrice() {
        int chooseServiceTotal = 0;
        allChooseServicePrice = 0;
        if (chooseServiceInfoMap != null) {
            for (String key : chooseServiceInfoMap.keySet()) {
                chooseServiceTotal += chooseServiceInfoMap.get(key).serviceTotalPrice;
            }
        }
        System.out.println("room total price = " + chooseServiceTotal);
        return chooseServiceTotal;
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
            if (request.flag == AppConst.ROOM_DETAIL) {
                removeProgressDialog();
                System.out.println("roomedetail json = " + response.body.toString());
                roomDetailInfoBean = JSON.parseObject(response.body.toString(), RoomDetailInfoBean.class);
                refreshRoomDetailInfo();
            } else if (request.flag == AppConst.HOTEL_VIP) {
                removeProgressDialog();
                System.out.println("Json = " + response.body.toString());
                CustomToast.show("您已成为该酒店会员", CustomToast.LENGTH_SHORT);
            }
        }
    }

    @Override
    protected void onNotifyError(ResponseData request) {
        super.onNotifyError(request);
        if (request.flag == AppConst.ROOM_DETAIL) {
            this.finish();
        }
    }

    private class MyPagerAdapter extends PagerAdapter {
        private Context context;
        private List<String> list = new ArrayList<>();

        MyPagerAdapter(Context context, List<String> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            loadImage(imageView, R.drawable.def_room_banner, list.get(position), 1920, 1080);
            container.addView(imageView, position);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    private class RoomServiceGridViewAdapter extends BaseAdapter {
        private Context context;
        private List<RoomDetailInfoBean.CapacityVOList> list;

        RoomServiceGridViewAdapter(Context context, List<RoomDetailInfoBean.CapacityVOList> list) {
            this.list = list;
            this.context = context;
        }

        public int getCount() {
            return list.size();
        }

        public Object getItem(int position) {
            return list.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public final class ViewHolder {
            private TextView tv_summary;
            private ImageView iv_icon;
            private TextView tv_count;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.gv_support_service_item, null);
                holder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
                holder.tv_summary = (TextView) convertView.findViewById(R.id.tv_summary);
                holder.tv_count = (TextView) convertView.findViewById(R.id.tv_count);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.tv_summary.setText(list.get(position).name);
            loadImage(holder.iv_icon, getResources().getColor(R.color.transparent), list.get(position).iconUrl, 80, 48);
            String count = list.get(position).count;
            if (StringUtil.isEmpty(count) /*|| "0".equals(count)*/) {
                holder.tv_count.setVisibility(View.GONE);
            } else {
                holder.tv_count.setVisibility(View.VISIBLE);
                holder.tv_count.setText("×" + count);
            }
            return convertView;
        }
    }
}
