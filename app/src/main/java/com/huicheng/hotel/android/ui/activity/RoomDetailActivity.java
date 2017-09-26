package com.huicheng.hotel.android.ui.activity;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.control.ShareControl;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.net.bean.HotelDetailInfoBean;
import com.huicheng.hotel.android.net.bean.RoomConfirmInfoBean;
import com.huicheng.hotel.android.net.bean.RoomDetailInfoBean;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.CommonAddSubLayout;
import com.huicheng.hotel.android.ui.custom.CommonAssessStarsLayout;
import com.huicheng.hotel.android.ui.custom.CustomNoAutoScrollBannerLayout;
import com.huicheng.hotel.android.ui.custom.CustomSharePopup;
import com.huicheng.hotel.android.ui.custom.MyGridViewWidget;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;
import com.huicheng.hotel.android.ui.glide.CustomReqURLFormatModelImpl;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.BitmapUtils;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author kborid
 * @date 2017/1/3 0003
 */
public class RoomDetailActivity extends BaseActivity {

    private static final int SELECTED_BAR_COUNT = 2;

    private LinearLayout root_lay;
    private RelativeLayout banner_lay;
    private ViewPager viewPager;
    private LinearLayout indicator_lay;
    private int positionIndex = 0;
    private ImageView iv_qtips_active;

    private RelativeLayout point_lay;
    private TextView tv_point, tv_comment, tv_room_name;
    private CommonAssessStarsLayout assess_star_lay;

    private TabHost tabHost;
    private View arrTabView, preTabView;
    private TextView tv_date, tv_during, tv_pay_type, tv_price;

    private LinearLayout service_lay;

    private LinearLayout choose_service_lay;
    private LinearLayout more_lay;
    private ImageView iv_more_down;

    private LinearLayout free_service_lay;

    private ImageView iv_share, iv_fans;
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
    private HotelDetailInfoBean hotelDetailInfoBean = null;
    private RoomDetailInfoBean roomDetailInfoBean = null;

    private PopupWindow mSharePopupWindow = null;
    private CustomSharePopup mCustomShareView = null;

    private boolean isSetDefaultPrePay = false;

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
        root_lay = (LinearLayout) findViewById(R.id.root_lay);
        root_lay.setLayoutAnimation(getAnimationController());
        banner_lay = (RelativeLayout) findViewById(R.id.banner_lay);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        llp.width = Utils.mScreenWidth;
        llp.height = (int) ((float) llp.width / 15 * 14);
        banner_lay.setLayoutParams(llp);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        indicator_lay = (LinearLayout) findViewById(R.id.indicator_lay);
        iv_qtips_active = (ImageView) findViewById(R.id.iv_qtips_active);
        iv_qtips_active.setVisibility(View.GONE);
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        rlp.width = (int) ((float) Utils.mScreenWidth / 5 * 3);
        rlp.height = (int) ((float) rlp.width / 3 * 2);
        rlp.bottomMargin = Utils.dip2px(60);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        iv_qtips_active.setLayoutParams(rlp);

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
        iv_fans = (ImageView) findViewById(R.id.iv_fans);
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            hotelId = bundle.getInt("hotelId");
            room_type = bundle.getInt("roomType");
            roomId = bundle.getInt("roomId");
            if (bundle.getBoolean("isYgr")) {
                isYgr = bundle.getBoolean("isYgr");
            }
        }
    }

    @Override
    public void initParams() {
        super.initParams();
        btn_back.setImageResource(R.drawable.iv_back_white);

        hotelDetailInfoBean = HotelOrderManager.getInstance().getHotelDetailInfo();
        if (null != hotelDetailInfoBean && hotelDetailInfoBean.isSupportVip) {
            iv_fans.setVisibility(View.VISIBLE);
            if (hotelDetailInfoBean.isVip) {
                iv_fans.setImageResource(R.drawable.iv_detail_viped);
            } else {
                iv_fans.setImageResource(R.drawable.iv_detail_vippp);
            }
        } else {
            iv_fans.setVisibility(View.GONE);
        }

        tabHost.setup();
        for (int i = 0; i < SELECTED_BAR_COUNT; i++) {
            View view = LayoutInflater.from(this).inflate(R.layout.selected_bar_item, null);
            if (i == 1) {
                tabHost.addTab(tabHost.newTabSpec("tab" + i).setIndicator(view).setContent(R.id.tab_pre));
                preTabView = tabHost.getTabWidget().getChildAt(i);
            } else {
                tabHost.addTab(tabHost.newTabSpec("tab" + i).setIndicator(view).setContent(R.id.tab_on));
                arrTabView = tabHost.getTabWidget().getChildAt(i);
            }
        }
        tabHost.setCurrentTab(0);
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
                LogUtil.i(TAG, "tabid = " + tabId);
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
        iv_fans.setOnClickListener(this);
        point_lay.setOnClickListener(this);
    }

    private void initIndicatorLay(int count) {
        indicator_lay.removeAllViews();
        if (count > 1) {
            for (int i = 0; i < count; i++) {
                View view = new View(this);
                view.setBackgroundResource(R.drawable.indicator_sel);
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

            isSetDefaultPrePay = false;
            if (roomDetailInfoBean.showTipsOrNot) {
                String province = SharedPreferenceUtil.getInstance().getString(AppConst.PROVINCE, "", false);
                if ("海南省".contains(province)) {
                    isSetDefaultPrePay = true;
                    iv_qtips_active.setVisibility(View.VISIBLE);
                }
            }

            root_lay.setVisibility(View.VISIBLE);
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
                if (StringUtil.notEmpty(roomDetailInfoBean.grade)) {
                    grade = Float.parseFloat(roomDetailInfoBean.grade);
                }
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
            String arrPayText, prePayText;
            if (roomDetailInfoBean.preTotalPriceList != null && roomDetailInfoBean.totalPriceList != null) {
                arrTabView.setEnabled(true);
                arrPayText = String.format(getString(R.string.arrPayStr), String.valueOf(roomDetailInfoBean.totalPrice));
                preTabView.setEnabled(true);
                prePayText = String.format(getString(R.string.prePayStr), String.valueOf(roomDetailInfoBean.preTotalPrice));
                if (isSetDefaultPrePay) {
                    tabHost.setCurrentTab(1);
                }
                if (roomDetailInfoBean.onlyOnline) {
                    arrTabView.setEnabled(false);
                    arrPayText = getString(R.string.arrPayNotSupport);
                    tabHost.setCurrentTab(1);
                }
            } else if (roomDetailInfoBean.preTotalPriceList != null) {
                arrTabView.setEnabled(false);
                arrPayText = getString(R.string.arrPayNotSupport);
                preTabView.setEnabled(true);
                prePayText = String.format(getString(R.string.prePayStr), String.valueOf(roomDetailInfoBean.preTotalPrice));

                tabHost.setCurrentTab(1);
            } else if (roomDetailInfoBean.totalPriceList != null) {
                arrTabView.setEnabled(true);
                arrPayText = String.format(getString(R.string.arrPayStr), String.valueOf(roomDetailInfoBean.totalPrice));
                preTabView.setEnabled(false);
                prePayText = getString(R.string.prePayNotSupport);
                tabHost.setCurrentTab(0);
            } else {
                arrTabView.setEnabled(false);
                arrPayText = getString(R.string.arrPayNotSupport);
                preTabView.setEnabled(false);
                prePayText = getString(R.string.prePayNotSupport);
            }
            ((TextView) arrTabView.findViewById(R.id.tv_tab)).setText(arrPayText);
            ((TextView) preTabView.findViewById(R.id.tv_tab)).setText(prePayText);

            tv_date.setText(DateUtil.getDay("MM月dd日", HotelOrderManager.getInstance().getBeginTime(isYgr)) + "-" + DateUtil.getDay("dd日", HotelOrderManager.getInstance().getEndTime(isYgr)));
            tv_during.setText(DateUtil.getGapCount(HotelOrderManager.getInstance().getBeginDate(isYgr), HotelOrderManager.getInstance().getEndDate(isYgr)) + "晚");

            updateTab(tabHost);

            //设置选购服务
            refreshServiceLayout();

            //设置房间所提供的服务
            refreshRoomDetailService();
        } else {
            root_lay.setVisibility(View.GONE);
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
                if (roomDetailInfoBean.totalPriceList == null || roomDetailInfoBean.onlyOnline) {
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
                    gv_detail_room.setAdapter(new RoomServiceGridViewAdapter(this, roomDetailInfoBean.serviceList.get(i).capacityVOList));
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
                expendedService(!hasShowAll, true);
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

                final UMWeb web = new UMWeb(url);
                web.setTitle(roomDetailInfoBean.hotelName + " " + roomDetailInfoBean.roomName);
                Glide.with(this)
                        .load(new CustomReqURLFormatModelImpl(roomDetailInfoBean.picList.get(0)))
                        .asBitmap()
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                if (null != resource) {
                                    web.setThumb(new UMImage(RoomDetailActivity.this, resource));
                                } else {
                                    web.setThumb(new UMImage(RoomDetailActivity.this, BitmapFactory.decodeResource(getResources(), R.drawable.logo)));
                                }
                            }
                        });
                web.setDescription(tv_date.getText().toString() + " " + tv_during.getText().toString() + "\n共计：" + tv_price.getText().toString());
                ShareControl.getInstance().setUMWebContent(this, web, null);
                showSharePopupWindow();
                break;
            case R.id.iv_fans:
                if (null != hotelDetailInfoBean && !hotelDetailInfoBean.isVip) {
                    showAddVipDialog(this, hotelDetailInfoBean);
                }
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

    private void expendedService(boolean isShow, boolean isAnim) {
        int during = 0;
        if (isAnim) {
            during = 300;
        }
        if (isShow) {
            hasShowAll = true;
            tv_more.setText("收起");
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(iv_more_down, "rotation", 0f, 180f);
            objectAnimator.setDuration(during);
            objectAnimator.start();
            for (int i = 3; i < roomDetailInfoBean.chooseList.size(); i++) {
                choose_service_lay.getChildAt(i).setVisibility(View.VISIBLE);
            }
        } else {
            hasShowAll = false;
            tv_more.setText("更多");
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(iv_more_down, "rotation", 180f, 360f);
            objectAnimator.setDuration(during);
            objectAnimator.start();
            for (int i = 3; i < roomDetailInfoBean.chooseList.size(); i++) {
                choose_service_lay.getChildAt(i).setVisibility(View.GONE);
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
                final RoomDetailInfoBean.ChooseService serviceBean = chooseList.get(i);
                View view = LayoutInflater.from(this).inflate(R.layout.lv_choose_service_item, null);
                TextView tv_choose_service_title = (TextView) view.findViewById(R.id.tv_choose_service_title);
                tv_choose_service_title.getPaint().setFakeBoldText(true);
                final TextView tv_choose_service_price = (TextView) view.findViewById(R.id.tv_choose_service_price);
                final TextView tv_choose_service_total_price = (TextView) view.findViewById(R.id.tv_choose_service_total_price);
                final TextView tv_choose_service_detail = (TextView) view.findViewById(R.id.tv_choose_server_detail);
                tv_choose_service_total_price.getPaint().setFakeBoldText(true);
                CommonAddSubLayout addSubLayout = (CommonAddSubLayout) view.findViewById(R.id.addSubLayout);
                addSubLayout.setUnit("份");
                addSubLayout.setMaxvalue(serviceBean.limitCnt);
                LogUtil.i(TAG, "limit count = " + serviceBean.limitCnt);
                tv_choose_service_title.setText(serviceBean.serviceName);
                tv_choose_service_price.setText(serviceBean.price + "元/份");
                tv_choose_service_total_price.setText("0元");

                if (StringUtil.isEmpty(serviceBean.detail) && StringUtil.isEmpty(serviceBean.pics)) {
                    tv_choose_service_detail.setVisibility(View.GONE);
                } else {
                    tv_choose_service_detail.setVisibility(View.VISIBLE);
                }

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                lp.topMargin = Utils.dip2px(20);
                if (i >= 3 && isShowMore) {
                    more_lay.setVisibility(View.VISIBLE);
                    view.setVisibility(View.GONE);
                }

                final RoomConfirmInfoBean bean = new RoomConfirmInfoBean();

                tv_choose_service_detail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final CustomDialog detailDialog = new CustomDialog(RoomDetailActivity.this);
                        detailDialog.findViewById(R.id.content_layout).setPadding(0, 0, 0, 0);
                        View view = LayoutInflater.from(RoomDetailActivity.this).inflate(R.layout.dialog_service_detail_item_lay, null);
                        TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
                        tv_title.getPaint().setFakeBoldText(true);
                        TextView tv_content = (TextView) view.findViewById(R.id.tv_content);
                        ImageView iv_close = (ImageView) view.findViewById(R.id.iv_close);
                        iv_close.setImageBitmap(BitmapUtils.getAlphaBitmap(getResources().getDrawable(R.drawable.iv_detail_close), getResources().getColor(R.color.white)));
                        CustomNoAutoScrollBannerLayout custom_viewpager = (CustomNoAutoScrollBannerLayout) view.findViewById(R.id.custom_viewpager);
                        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        llp.width = Utils.dip2px(300);
                        llp.height = (int) ((float) llp.width / 34 * 25);
                        custom_viewpager.setLayoutParams(llp);

                        tv_title.setText(serviceBean.serviceName);
                        String detail = serviceBean.detail;
                        tv_content.setText(detail);
                        if (StringUtil.isEmpty(detail)) {
                            tv_content.setVisibility(View.GONE);
                        }

                        final ArrayList<String> list = new ArrayList<>();
                        String pics = serviceBean.pics;
                        if (StringUtil.isEmpty(pics)) {
                            custom_viewpager.setVisibility(View.GONE);
                        } else {
                            String[] picArr = pics.split(";");
                            list.addAll(Arrays.asList(picArr));
                            custom_viewpager.setVisibility(View.VISIBLE);
                            custom_viewpager.setImageResourcePaths(list);
                        }

                        iv_close.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                detailDialog.dismiss();
                            }
                        });
                        detailDialog.addView(view);
                        detailDialog.setCanceledOnTouchOutside(true);
                        detailDialog.show();
                    }
                });
                addSubLayout.setOnCountChangedListener(new CommonAddSubLayout.OnCountChangedListener() {
                    @Override
                    public void onCountChanged(int count) {
                        bean.serviceId = serviceBean.id;
                        bean.serviceTitle = serviceBean.serviceName;
                        bean.serviceCount = count;
                        bean.servicePrice = serviceBean.price;
                        bean.serviceTotalPrice = serviceBean.price * count;
                        tv_choose_service_total_price.setText(bean.serviceTotalPrice + "元");
                        LogUtil.i(TAG, "choose service price = " + bean.serviceTotalPrice);

                        chooseServiceInfoMap.put(bean.serviceTitle, bean);

                        // 计算总价
                        allChooseServicePrice = constChooseServicePrice();
                        tv_total_price.setText(allChooseServicePrice + roomPrice + "元");
                    }
                });
                choose_service_lay.addView(view, lp);
            }
            if (isShowMore) {
                expendedService(hasShowAll, false);
            }
        } else {
            findViewById(R.id.tv_choose_service_note).setVisibility(View.GONE);
        }

        if (freeChooseList.size() > 0) {
            findViewById(R.id.tv_free_service_note).setVisibility(View.VISIBLE);
            free_service_lay.removeAllViews();
            TextView tv_content = new TextView(this);
            tv_content.setTextSize(12);
            tv_content.setTextColor(getResources().getColor(R.color.lableColor));
            tv_content.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            for (int i = 0; i < freeChooseList.size(); i++) {
                tv_content.append(freeChooseList.get(i).serviceName);
                int limitCount = freeChooseList.get(i).limitCnt;
                //大于等于10000，表示不限，次数不显示
                if (limitCount < 10000) {
                    tv_content.append(" " + getString(R.string.multipleSign) + " ");
                    tv_content.append(String.valueOf(freeChooseList.get(i).limitCnt));
                }
                if (i != freeChooseList.size() - 1) {
                    tv_content.append("，");
                }
            }
            free_service_lay.setPadding(0, Utils.dip2px(20), 0, 0);
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
        LogUtil.i(TAG, "room total price = " + chooseServiceTotal);
        return chooseServiceTotal;
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.i(TAG, "isVipRefresh = " + isHotelVipRefresh);
    }

    @Override
    public void refreshScreenInfoVipPrice() {
        super.refreshScreenInfoVipPrice();
        LogUtil.i(TAG, "refreshScreenInfoVipPrice()");
        iv_fans.setVisibility(View.VISIBLE);
        iv_fans.setImageResource(R.drawable.iv_detail_viped);
        requestRoomDetailInfo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tabHost != null) {
            tabHost.clearAllTabs();
            tabHost = null;
        }
        ShareControl.getInstance().destroy();
    }

    @Override
    public void preExecute(ResponseData request) {

    }

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.ROOM_DETAIL) {
                removeProgressDialog();
                LogUtil.i(TAG, "roomedetail json = " + response.body.toString());
                roomDetailInfoBean = JSON.parseObject(response.body.toString(), RoomDetailInfoBean.class);
                refreshRoomDetailInfo();
            }
        }
    }

    @Override
    public void onNotifyError(ResponseData request) {
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
            Glide.with(context)
                    .load(new CustomReqURLFormatModelImpl(list.get(position)))
                    .placeholder(R.drawable.def_room_banner)
                    .crossFade()
                    .centerCrop()
                    .override(750, 700)
                    .into(imageView);
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
            private ImageView iv_icon;
            private TextView tv_count;
            private TextView tv_name;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.gv_support_service_item, null);
                GridView.LayoutParams vlp = new GridView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                vlp.width = (int) ((float) (Utils.mScreenWidth - Utils.dip2px(40)) / 4);
                vlp.height = vlp.width;
                convertView.setLayoutParams(vlp);

                holder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
                holder.tv_count = (TextView) convertView.findViewById(R.id.tv_count);
                holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            String name = list.get(position).name;
            holder.tv_name.setText(name);
            Glide.with(context)
                    .load(new CustomReqURLFormatModelImpl(list.get(position).iconUrl))
                    .asBitmap()
                    .override(60, 60)
                    .into(holder.iv_icon);
            String count = list.get(position).count;
            if (StringUtil.isEmpty(count) || "0".equals(count)) {
                holder.tv_count.setVisibility(View.GONE);
            } else {
                holder.tv_count.setVisibility(View.VISIBLE);
                holder.tv_count.setText(getString(R.string.multipleSign) + count);
            }
            return convertView;
        }
    }
}
