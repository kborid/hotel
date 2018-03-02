package com.huicheng.hotel.android.ui.activity.hotel;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.huicheng.hotel.android.PRJApplication;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.common.HotelErrorDef;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.content.AppConst;
import com.huicheng.hotel.android.content.NetURL;
import com.huicheng.hotel.android.permission.PermissionsActivity;
import com.huicheng.hotel.android.permission.PermissionsDef;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.HotelDetailInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.RoomDetailCheckResultInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.RoomListInfoBean;
import com.huicheng.hotel.android.tools.CityParseUtils;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.huicheng.hotel.android.ui.custom.CommonAssessStarsLayout;
import com.huicheng.hotel.android.ui.custom.RoundedAllImageView;
import com.huicheng.hotel.android.ui.custom.RoundedLeftImageView;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.huicheng.hotel.android.ui.glide.CustomReqURLFormatModelImpl;
import com.huicheng.hotel.android.ui.listener.CustomOnPageChangeListener;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kborid
 * @date 2017/1/1 0001
 */
public class HotelDetailActivity extends BaseAppActivity {

    private static final int SELECTED_BAR_COUNT = 2;
    private LinearLayout root_lay;

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
    private String hotelCityStr, hotelDateStr;
    private int hotelId = 0;
    private int roomId = -1, roomType = 0;
    private boolean isClickYgr = false;
    private HotelDetailInfoBean hotelDetailInfoBean = null;
    private long beginTime, endTime;

    private int mRoomPriceColorId;
    private Drawable mRoomDetailSpaceId, mRoomDetailSpace2Id;
    private int ygrRoomItemBackgroundId;
    private int indicatorSelId;

    private LinearLayout vip_layout;
    private ImageView iv_ok;
    private CheckBox cb_check;

    private CustomDialog mDialDialog = null;
    private WeakReferenceHandler<HotelDetailActivity> myHandle = new WeakReferenceHandler<HotelDetailActivity>(this);

    @Override
    protected void requestData() {
        super.requestData();
        requestHotelDetailInfo();
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_hotel_detail);
    }

    @Override
    protected void initTypeArrayAttributes() {
        super.initTypeArrayAttributes();
        TypedArray ta = obtainStyledAttributes(R.styleable.MyTheme);
        mRoomPriceColorId = ta.getInt(R.styleable.MyTheme_roomItemPrice, getResources().getColor(R.color.indicatorColor));
        mRoomDetailSpaceId = ta.getDrawable(R.styleable.MyTheme_roomDetailSpace);
        mRoomDetailSpace2Id = ta.getDrawable(R.styleable.MyTheme_roomDetailSpace2);
        ygrRoomItemBackgroundId = ta.getResourceId(R.styleable.MyTheme_roomItemGradient, R.drawable.roomitem_ygr_gradient);
        indicatorSelId = ta.getResourceId(R.styleable.MyTheme_indicatorSel, R.drawable.indicator_sel);
        ta.recycle();
    }

    @Override
    public void initViews() {
        super.initViews();
        root_lay = (LinearLayout) findViewById(R.id.root_lay);
        root_lay.setVisibility(View.GONE);
        root_lay.setLayoutAnimation(getAnimationController());
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        indicator_lay = (LinearLayout) findViewById(R.id.indicator_lay);
        tv_assess_point = (TextView) findViewById(R.id.tv_assess_point);
        assess_star_lay = (CommonAssessStarsLayout) findViewById(R.id.assess_star_lay);
        tv_comment = (TextView) findViewById(R.id.tv_comment);
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

        vip_layout = (LinearLayout) findViewById(R.id.vip_layout);
        iv_ok = (ImageView) findViewById(R.id.iv_ok);
        cb_check = (CheckBox) findViewById(R.id.cb_check);
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
        super.initParams();
        setRightButtonResource(R.drawable.iv_vippp);
        hotelDetailInfoBean = HotelOrderManager.getInstance().getHotelDetailInfo();
        beginTime = HotelOrderManager.getInstance().getBeginTime();
        endTime = HotelOrderManager.getInstance().getEndTime();
        hotelDateStr = DateUtil.getDay("M.d", beginTime) + " - " + DateUtil.getDay("M.d", endTime);

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
    }

    @Override
    public void initListeners() {
        super.initListeners();
        tv_center_title.setOnClickListener(this);
        tv_map.setOnClickListener(this);
        tv_service.setOnClickListener(this);
        tv_view_comment.setOnClickListener(this);
        viewPager.addOnPageChangeListener(new CustomOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                int newPosition = position % hotelDetailInfoBean.picPath.size();
                if (null != indicator_lay && indicator_lay.getChildCount() > 1) {
                    indicator_lay.getChildAt(newPosition).setEnabled(true);
                    if (positionIndex != newPosition) {
                        indicator_lay.getChildAt(positionIndex).setEnabled(false);
                        positionIndex = newPosition;
                    }
                }
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
        iv_ok.setOnClickListener(this);
    }

    private void requestHotelDetailInfo() {
        LogUtil.i(TAG, "requestHotelDetailInfo()");
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("hotelId", String.valueOf(hotelId));
        b.addBody("beginDate", String.valueOf(beginTime));
        b.addBody("endDate", String.valueOf(endTime));
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
            // 会员按钮显示状态
            if (hotelDetailInfoBean.isSupportVip) {
                iv_right.setVisibility(View.VISIBLE);
                if (hotelDetailInfoBean.isVip) {
                    setRightButtonResource(R.drawable.iv_viped);
                } else {
                    setRightButtonResource(R.drawable.iv_vippp);
                    if (!SharedPreferenceUtil.getInstance().getBoolean(AppConst.HAS_SHOW_VIP_TIPS, false)) {
                        vip_layout.setVisibility(View.VISIBLE);
                    } else {
                        vip_layout.setVisibility(View.GONE);
                    }
                }
            } else {
                iv_right.setVisibility(View.INVISIBLE);
            }

            //设置 title
            hotelCityStr = CityParseUtils.getProvinceCityString(hotelDetailInfoBean.provinceName, hotelDetailInfoBean.cityName, "-");
//            tv_center_title.setText(hotelCityStr + "(" + hotelDateStr + ")");
            tv_center_title.setText(
                    String.format(getString(R.string.titleCityDateStr),
                            hotelCityStr,
                            hotelDateStr)
            );

            //设置banner
            int marginValue = Utils.dp2px(10);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                marginValue = Utils.dp2px(5);
            }
            viewPager.setPageMargin(marginValue);
            viewPager.setAdapter(new MyPagerAdapter(this, hotelDetailInfoBean.picPath));
            initIndicatorLay(hotelDetailInfoBean.picPath.size());

            //viewPager一个假的无限循环，初始位置是viewPager count的100倍
            viewPager.setCurrentItem(hotelDetailInfoBean.picPath.size() * 100);
            viewPager.setOffscreenPageLimit(hotelDetailInfoBean.picPath.size());
            //设置评分、等级、评论
            float grade = 0;
            try {
                if (StringUtil.notEmpty(hotelDetailInfoBean.grade)) {
                    grade = Float.parseFloat(hotelDetailInfoBean.grade);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            tv_assess_point.setText(String.valueOf(grade));
            assess_star_lay.setColorStars((int) grade);

//            if ("0".equals(hotelDetailInfoBean.evaluateCount)) {
//                tv_comment.setText("没有评论");
//            } else {
//                tv_comment.setText(hotelDetailInfoBean.evaluateCount + "条评论");
//            }
            //设置酒店基本信息
            tv_hotel_name.setText(hotelDetailInfoBean.name);
            tv_hotel_add.setText(hotelDetailInfoBean.address);
            tv_hotel_phone.setText(hotelDetailInfoBean.phone);
            updateSpaceAreaStatus(hotelDetailInfoBean.hasArticle);
            //设置房间列表信息
            updateRoomListData();
            //设置酒店服务信息
            refreshHotelServiceInfo();

            root_lay.setVisibility(View.VISIBLE);
        }
    }

    private void updateSpaceAreaStatus(boolean isFlag) {
        if (isFlag) {
            tv_view_comment.setCompoundDrawablesWithIntrinsicBounds(mRoomDetailSpace2Id, null, null, null);
        } else {
            tv_view_comment.setCompoundDrawablesWithIntrinsicBounds(mRoomDetailSpaceId, null, null, null);
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
            CardView cardview = (CardView) view.findViewById(R.id.cardview);
            RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rlp.width = Utils.mScreenWidth - Utils.dp2px(20);
            rlp.height = (int) ((float) rlp.width / 7 * 2);
            rlp.setMargins(Utils.dp2px(10), Utils.dp2px(5), Utils.dp2px(10), Utils.dp2px(5));
            rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
            cardview.setLayoutParams(rlp);
            LinearLayout content_layout = (LinearLayout) view.findViewById(R.id.content_layout);
            RoundedLeftImageView iv_icon = (RoundedLeftImageView) view.findViewById(R.id.iv_icon);
            ImageView iv_accessory = (ImageView) view.findViewById(R.id.iv_accessory);
            TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
            TextView tv_price_note = (TextView) view.findViewById(R.id.tv_price_note);
            TextView tv_price = (TextView) view.findViewById(R.id.tv_price);
            tv_price.getPaint().setFakeBoldText(true);
            LinearLayout clock_lay = (LinearLayout) view.findViewById(R.id.clock_lay);
            LinearLayout time_lay = (LinearLayout) view.findViewById(R.id.time_lay);
            LinearLayout durning_lay = (LinearLayout) view.findViewById(R.id.durning_lay);
            durning_lay.setVisibility(View.GONE);
            Glide.with(this)
                    .load(new CustomReqURLFormatModelImpl(hotelDetailInfoBean.roomList.get(i).picPath))
                    .placeholder(R.drawable.def_room_list)
                    .crossFade()
                    .centerCrop()
                    .override(350, 200)
                    .into(iv_icon);
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
                    iv_accessory.setImageResource(R.drawable.iv_accessory_white);
                    clock_lay.setVisibility(View.VISIBLE);
                    ((TextView) time_lay.findViewById(R.id.tv_time_label)).setText("入住时段：");
                    ((TextView) time_lay.findViewById(R.id.tv_time)).setText(hotelDetailInfoBean.roomList.get(i).yeguirenRoomTime);
                    content_layout.setBackgroundResource(ygrRoomItemBackgroundId);
                    tv_title.setTextColor(getResources().getColor(R.color.white));
                    tv_price_note.setTextColor(getResources().getColor(R.color.white));
                    tv_price.setTextColor(getResources().getColor(R.color.white));
                } else {
                    clock_lay.setVisibility(View.GONE);
                    iv_accessory.setImageResource(R.drawable.iv_accessory_black_alpha3);
                    content_layout.setBackgroundResource(0);
                    tv_title.setTextColor(getResources().getColor(R.color.lableColor));
                    tv_price_note.setTextColor(getResources().getColor(R.color.lableColor));
                    tv_price.setTextColor(mRoomPriceColorId);
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
                    HotelOrderManager.getInstance().setHotelType(roomType);
                    requestCheckRoomEmpty(hotelId, roomId, roomType);
                }
            });
            tab_day.addView(view);
        }

        //钟点房
        tab_clock.removeAllViews();
        for (int i = 0; i < hotelDetailInfoBean.clockRoomList.size(); i++) {
            View view = LayoutInflater.from(this).inflate(R.layout.lv_room_item, null);
            CardView cardview = (CardView) view.findViewById(R.id.cardview);
            RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rlp.width = Utils.mScreenWidth - Utils.dp2px(20);
            rlp.height = (int) ((float) rlp.width / 7 * 2);
            rlp.setMargins(Utils.dp2px(10), Utils.dp2px(5), Utils.dp2px(10), Utils.dp2px(5));
            rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
            cardview.setLayoutParams(rlp);
            RoundedLeftImageView iv_icon = (RoundedLeftImageView) view.findViewById(R.id.iv_icon);
            ImageView iv_accessory = (ImageView) view.findViewById(R.id.iv_accessory);
            iv_accessory.setImageResource(R.drawable.iv_accessory_black_alpha3);
            TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
            TextView tv_price_note = (TextView) view.findViewById(R.id.tv_price_note);
            TextView tv_price = (TextView) view.findViewById(R.id.tv_price);
            tv_price.getPaint().setFakeBoldText(true);
            LinearLayout clock_lay = (LinearLayout) view.findViewById(R.id.clock_lay);
            clock_lay.setVisibility(View.VISIBLE);
            TextView tv_time = (TextView) view.findViewById(R.id.tv_time);
            TextView tv_during = (TextView) view.findViewById(R.id.tv_during);
            Glide.with(this)
                    .load(new CustomReqURLFormatModelImpl(hotelDetailInfoBean.clockRoomList.get(i).picPath))
                    .placeholder(R.drawable.def_room_list)
                    .crossFade()
                    .centerCrop()
                    .override(350, 200)
                    .into(iv_icon);
            tv_title.setText(hotelDetailInfoBean.clockRoomList.get(i).name);
            tv_price_note.setText(hotelDetailInfoBean.clockRoomList.get(i).priceType + "：");
            tv_price.setText(hotelDetailInfoBean.clockRoomList.get(i).clockPrice + "元");
            tv_time.setText(hotelDetailInfoBean.clockRoomList.get(i).roomTime);
            tv_during.setText(Float.valueOf(hotelDetailInfoBean.clockRoomList.get(i).roomDuration) + "小时");
            final int finalI = i;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    roomId = hotelDetailInfoBean.clockRoomList.get(finalI).id;
                    roomType = Integer.valueOf(hotelDetailInfoBean.clockRoomList.get(finalI).searchType);
                    HotelOrderManager.getInstance().setHotelType(roomType);
                    requestCheckRoomEmpty(hotelId, roomId, roomType);
                }
            });
            tab_clock.addView(view);
        }
    }

    private void requestCheckRoomEmpty(int hotelId, int roomId, int roomType) {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("beginDate", String.valueOf(beginTime));
        b.addBody("endDate", String.valueOf(endTime));
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
                View line_lay = view1.findViewById(R.id.line_lay);
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
                llp.height = Utils.dp2px(120);
                listview.setLayoutParams(llp);
            }
            //没有数据时，显示空view提示
            TextView emptyView = new TextView(this);
            emptyView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            emptyView.setText("暂无推荐");
            emptyView.setTextSize(14);
            emptyView.setPadding(0, Utils.dp2px(15), 0, Utils.dp2px(15));
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
                    Intent intent = new Intent(HotelDetailActivity.this, HotelRoomDetailActivity.class);
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
            case R.id.tv_center_title: {
                Intent intentRes = new Intent(this, HotelCalendarChooseActivity.class);
//                intentRes.putExtra("beginTime", beginTime);
//                intentRes.putExtra("endTime", endTime);
                startActivityForResult(intentRes, 0x01);
            }
            break;
            case R.id.iv_right:
                LogUtil.i(TAG, "right button onclick");
                if (hotelDetailInfoBean != null && !hotelDetailInfoBean.isVip) {
                    showAddVipDialog(this, hotelDetailInfoBean);
                }
                break;
            case R.id.tv_map:
                if (hotelDetailInfoBean != null) {
                    intent = new Intent(this, HotelMapActivity.class);
                    intent.putExtra("bean", hotelDetailInfoBean);
                    intent.putExtra("hotelCityStr", hotelCityStr);
                }
                break;
            case R.id.tv_service:
                if (hotelDetailInfoBean != null && StringUtil.notEmpty(hotelDetailInfoBean.phone)) {
                    if (PRJApplication.getPermissionsChecker(this).lacksPermissions(PermissionsDef.PHONE_PERMISSION)) {
                        PermissionsActivity.startActivityForResult(this, PermissionsDef.PERMISSION_REQ_CODE, PermissionsDef.PHONE_PERMISSION);
                        return;
                    }
                    showDialDialog(hotelDetailInfoBean.phone);
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
                    intent = new Intent(this, HotelCommendsActivity.class);
                    intent.putExtra("hotelId", HotelOrderManager.getInstance().getHotelDetailInfo().id);
                }
                break;
            case R.id.iv_ok:
                vip_layout.setVisibility(View.GONE);
                if (cb_check.isChecked()) {
                    SharedPreferenceUtil.getInstance().setBoolean(AppConst.HAS_SHOW_VIP_TIPS, true);
                }
                break;
            default:
                break;
        }
        if (null != intent) {
            startActivity(intent);
        }
    }

    private void showDialDialog(final String number) {
        if (null == mDialDialog) {
            mDialDialog = new CustomDialog(this);
        }
        mDialDialog.setMessage(String.format("是否拨打客服电话%1$s", number));
        mDialDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        mDialDialog.setPositiveButton("拨打", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Uri dialUri = Uri.parse("tel:" + number);
                Intent callIntent = new Intent(Intent.ACTION_DIAL, dialUri);
                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(callIntent);
            }
        });
        mDialDialog.setCanceledOnTouchOutside(true);
        mDialDialog.show();
    }

    private void initIndicatorLay(int count) {
        indicator_lay.removeAllViews();
        LogUtil.i(TAG, "count = " + count);
        if (count > 1) {
            for (int i = 0; i < count; i++) {
                View view = new View(this);
                view.setBackgroundResource(indicatorSelId);
                view.setEnabled(false);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Utils.dp2px(7), Utils.dp2px(7));
                if (i > 0) {
                    lp.leftMargin = Utils.dp2px(7);
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
        LogUtil.i(TAG, "isVipRefresh = " + isHotelVipRefresh);
        if (isHotelVipRefresh) {
            refreshScreenInfoVipPrice();
        }
    }

    @Override
    public void refreshScreenInfoVipPrice() {
        super.refreshScreenInfoVipPrice();
        LogUtil.i(TAG, "refreshScreenInfoVipPrice()");
        isHotelVipRefresh = false;
        requestHotelDetailInfo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tabHost != null) {
            tabHost.clearAllTabs();
            tabHost = null;
        }
        if (myHandle != null) {
            myHandle.removeCallbacksAndMessages(null);
            myHandle = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == PermissionsDef.PERMISSIONS_GRANTED
                && requestCode == PermissionsDef.PERMISSION_REQ_CODE) {
            showDialDialog(hotelDetailInfoBean.phone);
            return;
        }
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == 0x01) {
            if (null != data) {
                beginTime = data.getLongExtra("beginTime", beginTime);
                endTime = data.getLongExtra("endTime", endTime);
                hotelDateStr = DateUtil.getDay("M.d", beginTime) + " - " + DateUtil.getDay("M.d", endTime);
                HotelOrderManager.getInstance().setBeginTime(beginTime);
                HotelOrderManager.getInstance().setEndTime(endTime);
                HotelOrderManager.getInstance().setDateStr(hotelDateStr);
                requestHotelDetailInfo();
            }
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
            if (list.size() > 1) {
                return Integer.MAX_VALUE;
            }
            return list.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            position %= list.size();
            if (position < 0) {
                position = list.size() + position;
            }
            final View view = LayoutInflater.from(context).inflate(R.layout.room_banner_item, null);
            final RoundedAllImageView iv_background = (RoundedAllImageView) view.findViewById(R.id.iv_background);
            Glide.with(context)
                    .load(new CustomReqURLFormatModelImpl(list.get(position)))
                    .placeholder(R.drawable.def_hotel_banner)
                    .crossFade()
                    .centerCrop()
                    .override(650, 400)
                    .into(iv_background);
            //如果View已经在之前添加到了一个父组件，则必须先remove，否则会抛出IllegalStateException。
            ViewParent vp = view.getParent();
            if (vp != null) {
                ViewGroup parent = (ViewGroup) vp;
                parent.removeView(view);
            }
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
        }
    }

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        super.onNotifyMessage(request, response);
        if (response != null && response.body != null) {
            if (request.flag == AppConst.HOTEL_DETAIL) {
                removeProgressDialog();
                LogUtil.i(TAG, "json = " + response.body.toString());
                hotelDetailInfoBean = JSON.parseObject(response.body.toString(), HotelDetailInfoBean.class);
                HotelOrderManager.getInstance().setHotelDetailInfo(hotelDetailInfoBean);
                refreshRoomListInfo();
            } else if (request.flag == AppConst.CHECK_ROOM_EMPTY) {
                removeProgressDialog();
                LogUtil.i(TAG, "json = " + response.body.toString());
                RoomDetailCheckResultInfoBean bean = JSON.parseObject(response.body.toString(), RoomDetailCheckResultInfoBean.class);
                if ("false".equals(bean.status)) {
                    showRecommendRoomListDialog(bean);
                } else {
                    Intent intent = new Intent(this, HotelRoomDetailActivity.class);
                    intent.putExtra("hotelId", hotelId);
                    intent.putExtra("roomId", roomId);
                    intent.putExtra("roomType", roomType);
                    intent.putExtra("isYgr", isClickYgr);
                    startActivity(intent);
                }
            }
        }
    }

    @Override
    protected boolean isCheckException(ResponseData request, ResponseData response) {
        if (response != null && response.data != null) {
            if (HotelErrorDef.ERR_HOTEL_HOTEL_OFF.equals(response.code)) { //006000 酒店下架
                CustomToast.show(response.data.toString(), CustomToast.LENGTH_LONG);
                myHandle.sendEmptyMessageDelayed(WeakReferenceHandler.CODE_FINISH, 2000);
                return true;
            }
        }
        return super.isCheckException(request, response);
    }

    private class MyRecommendRoomListAdapter extends BaseAdapter {
        private Context context;
        private List<RoomListInfoBean> list = new ArrayList<>();

        MyRecommendRoomListAdapter(Context context, List<RoomListInfoBean> list) {
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

            Glide.with(context)
                    .load(new CustomReqURLFormatModelImpl(list.get(position).picPath))
                    .placeholder(R.drawable.def_room_list)
                    .crossFade()
                    .centerCrop()
                    .override(350, 200)
                    .into(viewHolder.iv_icon);
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
