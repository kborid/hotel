package com.huicheng.hotel.android.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.ui.adapter.MainFragmentAdapter;
import com.huicheng.hotel.android.ui.base.BaseFragmentActivity;
import com.huicheng.hotel.android.ui.custom.CustomBottomNaviBar;
import com.huicheng.hotel.android.ui.custom.CustomViewPager;
import com.huicheng.hotel.android.ui.custom.LeftDrawerLayout;
import com.huicheng.hotel.android.ui.fragment.HotelPagerFragment;
import com.huicheng.hotel.android.ui.fragment.WebViewPagerFragment;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.ActivityTack;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.widget.CustomToast;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

public class MainFragmentActivity extends BaseFragmentActivity implements OnPageChangeListener, DataCallback, CustomBottomNaviBar.OnChangeClickListener, View.OnClickListener {

    public static final String TAB_HOTEL = "tab_hotel";
    public static final String TAB_PLANE = "tab_plane";
    public static final String TAB_TRAIN = "tab_train";
    public static final String TAB_TAXI = "tab_taxi";

    private static final String TAG = "MainFragmentActivity";
    private CustomViewPager viewPager;
    private CustomBottomNaviBar custom_bar;
    private DrawerLayout drawer_layout;
    private LeftDrawerLayout left_layout;

    private RelativeLayout blur_lay;
    private ImageView iv_blur;
    private ImageView iv_logo_vertical;
    private ImageView iv_left;

    private long exitTime = 0;
    private int currentIndex = 0;
    private boolean isNeedCloseLeftDrawer = false;

    //For PlanePagerFragment use...Fucking the code!!!
    private static boolean isReload = false;
    private static String planeOrderId = "";

    private int oldSkinIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_main_tab);
        initViews();
        dealIntent();
        initParams();
        initListeners();
    }

    public void initViews() {
        super.initViews();
        viewPager = (CustomViewPager) findViewById(R.id.viewPager);
        if (viewPager != null) {
            viewPager.setPagingEnabled(false);
        }
        custom_bar = (CustomBottomNaviBar) findViewById(R.id.custom_bar);
        drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (null != drawer_layout) {
            drawer_layout.setScrimColor(getResources().getColor(R.color.transparent50));
        }
        left_layout = (LeftDrawerLayout) findViewById(R.id.left_layout);

        blur_lay = (RelativeLayout) findViewById(R.id.blur_lay);
        iv_blur = (ImageView) findViewById(R.id.iv_blur);
        iv_logo_vertical = (ImageView) findViewById(R.id.iv_logo_vertical);
        iv_left = (ImageView) findViewById(R.id.iv_left);
    }

    @Override
    public void initParams() {
        super.initParams();
        if (SessionContext.isLogin()) {
            String lastLoginTime = SharedPreferenceUtil.getInstance().getString(AppConst.LAST_LOGIN_DATE, "", false);
        }
        oldSkinIndex = SharedPreferenceUtil.getInstance().getInt(AppConst.SKIN_INDEX, 0);
        initFragmentView();
    }

    @Override
    public void onResume() {
        super.onResume();

        int newSkinIndex = SharedPreferenceUtil.getInstance().getInt(AppConst.SKIN_INDEX, 0);
        if (oldSkinIndex != newSkinIndex) {
            oldSkinIndex = newSkinIndex;
            recreate();
        }

        if (SessionContext.isLogin()) {
            requestMessageCount();
        }


        if (currentIndex != 0 && !SessionContext.isLogin()) {
            sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION).putExtra("is_show_tip_dialog", true));
        } else {
            viewPager.setCurrentItem(currentIndex);
        }
        if (isNeedCloseLeftDrawer && drawer_layout.isDrawerOpen(left_layout)) {
            isNeedCloseLeftDrawer = false;
            drawer_layout.closeDrawers();
        }
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            currentIndex = bundle.getInt("index");
            isNeedCloseLeftDrawer = bundle.getBoolean("isClosed");
            isReload = bundle.getBoolean("isReload");
            planeOrderId = bundle.getString("orderId");
        }
        if (SessionContext.getWakeUpAppData() != null) {
            JSONObject mJson = JSON.parseObject(SessionContext.getWakeUpAppData().getData());
            if (mJson.containsKey("channel")) {
                String channel = mJson.getString("channel");
                if (HotelCommDef.SHARE_HOTEL.equals(channel)) {
                    long beginDate = Long.valueOf(mJson.getString("beginDate"));
                    long endDate = Long.valueOf(mJson.getString("endDate"));
                    HotelOrderManager.getInstance().setBeginTime(beginDate);
                    HotelOrderManager.getInstance().setEndTime(endDate);
                    Intent intent = new Intent(this, RoomListActivity.class);
                    intent.putExtra("hotelId", Integer.valueOf(mJson.getString("hotelID")));
                    startActivity(intent);
                } else if (HotelCommDef.SHARE_ROOM.equals(channel)) {
                    long beginDate = Long.valueOf(mJson.getString("beginDate"));
                    long endDate = Long.valueOf(mJson.getString("endDate"));
                    HotelOrderManager.getInstance().setBeginTime(beginDate);
                    HotelOrderManager.getInstance().setEndTime(endDate);
                    Intent intent = new Intent(this, RoomDetailActivity.class);
                    intent.putExtra("hotelId", Integer.valueOf(mJson.getString("hotelID")));
                    intent.putExtra("roomId", Integer.valueOf(mJson.getString("roomID")));
                    intent.putExtra("roomType", Integer.valueOf(mJson.getString("hotelType")));
                    startActivity(intent);
                } else if (HotelCommDef.SHARE_FREE.equals(channel)) {
                    Intent intent = new Intent(this, Hotel0YuanHomeActivity.class);
                    startActivity(intent);
                } else if (HotelCommDef.SHARE_TIE.equals(channel)) {
                    Intent intent = new Intent(this, HotelSpaceDetailActivity.class);
                    intent.putExtra("hotelId", Integer.valueOf(mJson.getString("hotelID")));
                    intent.putExtra("articleId", Integer.valueOf(mJson.getString("blogID")));
                    startActivity(intent);
                } else {
                    LogUtil.d("MainFragmentActivity", "warning~~~");
                }
            }
            SessionContext.setWakeUpAppData(null);
        }
    }

    public static String getPlaneOrderId() {
        String tmp = planeOrderId;
        //如果不为空，调用一次之后重置
        if (StringUtil.notEmpty(planeOrderId)) {
            planeOrderId = "";
        }
        return tmp;
    }

    public static boolean getIsNeedReload() {
        boolean tmp = isReload;
        //如果为true，调用一次之后重置
        if (isReload) {
            isReload = false;
        }
        return tmp;
    }

    private void requestMessageCount() {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.MESSAGE_COUNT;
        d.flag = AppConst.MESSAGE_COUNT;
        DataLoader.getInstance().loadData(this, d);
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Note that getIntent() still returns the original Intent. You can use setIntent(Intent) to update it to this new Intent.
        setIntent(intent);
        dealIntent();
    }

    /**
     * 初始化Fragment视图
     */
    private void initFragmentView() {
        List<Fragment> mList = new ArrayList<>();
        mList.add(HotelPagerFragment.newInstance(TAB_HOTEL));
        mList.add(WebViewPagerFragment.newInstance(TAB_PLANE));
        mList.add(WebViewPagerFragment.newInstance(TAB_TRAIN));
        mList.add(WebViewPagerFragment.newInstance(TAB_TAXI));
        viewPager.setOffscreenPageLimit(mList.size());
        viewPager.setAdapter(new MainFragmentAdapter(getSupportFragmentManager(), mList));
    }

    @Override
    public void initListeners() {
        super.initListeners();
        viewPager.addOnPageChangeListener(this);
        custom_bar.setOnChangeClickListener(this);
        custom_bar.findViewById(R.id.my_lay).setOnClickListener(this);
        custom_bar.findViewById(R.id.order_lay).setOnClickListener(this);
        left_layout.setOnLeftDrawerListener(new LeftDrawerLayout.OnLeftDrawerListener() {
            @Override
            public void closeDrawer() {
                drawer_layout.closeDrawers();
            }
        });
        drawer_layout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                iv_blur.setAlpha(slideOffset * 1f);
                iv_logo_vertical.setAlpha(slideOffset);
                iv_left.setAlpha(slideOffset);
                if (slideOffset > 0) {
                    blur_lay.setVisibility(View.VISIBLE);
                } else {
                    blur_lay.setVisibility(View.GONE);
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
            }

            @Override
            public void onDrawerClosed(View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (drawer_layout.isDrawerOpen(left_layout)) {
                drawer_layout.closeDrawers();
                return true;
            }
            if (viewPager.getCurrentItem() != 0) {
                viewPager.setCurrentItem(0, false);
                return true;
            }
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                CustomToast.show(getString(R.string.exit_tip), CustomToast.LENGTH_SHORT);
                exitTime = System.currentTimeMillis();
            } else {
                SessionContext.destroy();
                MobclickAgent.onKillProcess(this);// 调用Process.kill或者System.exit之类的方法杀死进程前保存统计数据
                DataLoader.getInstance().clearRequests();
                ActivityTack.getInstanse().exit();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onPause() {
        super.onPause();
        currentIndex = viewPager.getCurrentItem();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        left_layout.unregisterBroadReceiver();
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
    }

    @Override
    public void onPageSelected(int arg0) {
        custom_bar.refreshTabLayout(arg0, true);
    }

    @Override
    public void preExecute(ResponseData request) {
    }

    @Override
    public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.MESSAGE_COUNT) {
                JSONObject mJson = JSON.parseObject(response.body.toString());
                if (mJson.containsKey("count")) {
                    custom_bar.updateUserMsgBtnStatus(!"0".equals(mJson.getString("count")));
                }
                left_layout.updateUserInfo();

                left_layout.updateMsgCount(mJson.getString("count"));
            }
        }
    }

    @Override
    public void notifyError(ResponseData request, ResponseData response, Exception e) {
    }

    @Override
    public void onChanged(int index) {
        viewPager.setCurrentItem(index, false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_lay:
                drawer_layout.openDrawer(left_layout);
                break;
            case R.id.order_lay:
                if (SessionContext.isLogin()) {
                    Intent intent = new Intent(this, MyOrdersActivity.class);
                    startActivity(intent);
                } else {
                    sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION));
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.i(TAG, "onActivityResult() " + requestCode + ", " + resultCode);
    }
}
