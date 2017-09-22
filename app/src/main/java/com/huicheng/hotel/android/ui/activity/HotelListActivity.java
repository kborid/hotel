package com.huicheng.hotel.android.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.ui.base.BaseFragmentActivity;
import com.huicheng.hotel.android.ui.custom.CustomConsiderLayout;
import com.huicheng.hotel.android.ui.fragment.FragmentTabAllDay;
import com.huicheng.hotel.android.ui.fragment.FragmentTabClock;
import com.huicheng.hotel.android.ui.fragment.FragmentTabYeGuiRen;
import com.prj.sdk.util.Utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kborid
 * @date 2016/11/1 0001
 */
public class HotelListActivity extends BaseFragmentActivity implements View.OnClickListener {

    private int index = 0;
    private ImageView btn_back;
    private ImageView btn_right;
    private TextView tv_center_title;
    private TextView tv_summary;

    private long beginTime, endTime;
    private String hotelDateStr;
    private EditText et_keyword;
    private ImageView iv_search;

    private TabLayout tabs;
    private FloatingActionButton fab;
    private TextView shadow_lay;
    private boolean fabOpened = false;

    private CustomConsiderLayout customConsiderLayout;
    private float mWidth, mHeight;
    private String keyword = "";
    private int mPriceIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_hotellist_layout);
        dealIntent();
        initViews();
        initParams();
        initListeners();
    }

    @Override
    public void initViews() {
        super.initViews();
        btn_back = (ImageView) findViewById(R.id.btn_back);
        btn_right = (ImageView) findViewById(R.id.btn_right);
        tv_center_title = (TextView) findViewById(R.id.tv_center_title);
        tv_summary = (TextView) findViewById(R.id.tv_center_summary);
        et_keyword = (EditText) findViewById(R.id.et_keyword);
        iv_search = (ImageView) findViewById(R.id.iv_search);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        shadow_lay = (TextView) findViewById(R.id.shadow_lay);
        customConsiderLayout = (CustomConsiderLayout) findViewById(R.id.customConsiderLayout);
        ViewPager pager = (ViewPager) findViewById(R.id.viewpager);
        if (null != pager) {
            pager.setOffscreenPageLimit(3);
            pager.setAdapter(new myPagerAdapter(getSupportFragmentManager()));
            pager.setCurrentItem(index);
            pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    index = position;
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            //向ViewPager绑定PagerSlidingTabStrip
            tabs = (TabLayout) findViewById(R.id.tabs);
            if (null != tabs) {
                tabs.setupWithViewPager(pager);
            }
        }
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            index = bundle.getInt("index");
            if (bundle.getString("keyword") != null) {
                keyword = bundle.getString("keyword");
            }
            mPriceIndex = bundle.getInt("priceIndex");
        }
    }

    @Override
    public void initParams() {
        super.initParams();
        beginTime = HotelOrderManager.getInstance().getBeginTime();
        endTime = HotelOrderManager.getInstance().getEndTime();
        hotelDateStr = HotelOrderManager.getInstance().getDateStr();

//        tv_center_title.setText(HotelOrderManager.getInstance().getCityStr() + "(" + HotelOrderManager.getInstance().getDateStr() + ")");
        tv_center_title.getPaint().setFakeBoldText(true);
        tv_center_title.setText(
                String.format(getString(R.string.titleCityDateStr),
                        HotelOrderManager.getInstance().getCityStr(),
                        hotelDateStr)
        );

        et_keyword.setText(keyword);

        setIndicator(tabs, 40, 40);
        customConsiderLayout.initAndRestoreConfig();
        customConsiderLayout.setPriceRange(mPriceIndex);
    }

    @Override
    public void initListeners() {
        super.initListeners();
        btn_back.setOnClickListener(this);
        btn_right.setOnClickListener(this);
        iv_search.setOnClickListener(this);
        fab.setOnClickListener(this);
        shadow_lay.setOnClickListener(this);
        et_keyword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    iv_search.performClick();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        HotelOrderManager.getInstance().setBeginTime(beginTime);
        HotelOrderManager.getInstance().setEndTime(endTime);
        HotelOrderManager.getInstance().setDateStr(hotelDateStr);
    }

    private void dismissConsiderLayout() {
        customConsiderLayout.dismiss();
        if (null != listenerList && listenerList.size() > 0) {
            for (OnUpdateHotelInfoListener listener : listenerList) {
                listener.onUpdate(et_keyword.getText().toString());
            }
        }
    }

    private void openMenu(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotation", 0, -155, -135);

        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(customConsiderLayout, "alpha", 0.5f, 1f);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) customConsiderLayout.getLayoutParams();
        lp.bottomMargin = fab.getHeight() / 2 + (int) getResources().getDimension(R.dimen.fab_margin) - Utils.dip2px(20);
        customConsiderLayout.setLayoutParams(lp);
        customConsiderLayout.setPivotX(mWidth / 2);
        customConsiderLayout.setPivotY(mHeight);
        ObjectAnimator animatorScaleX = ObjectAnimator.ofFloat(customConsiderLayout, "scaleX", 0.97f, 1.02f, 1.00f);
        ObjectAnimator animatorScaleY = ObjectAnimator.ofFloat(customConsiderLayout, "scaleY", 0.97f, 1.02f, 1.00f);
        ObjectAnimator alphaAnimator2 = ObjectAnimator.ofFloat(shadow_lay, "alpha", 0f, 0.2f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(animator).with(alphaAnimator).with(animatorScaleX).with(animatorScaleY).with(alphaAnimator2);
        animatorSet.setDuration(500);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                customConsiderLayout.setVisibility(View.VISIBLE);
                shadow_lay.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.start();

        fabOpened = true;
    }

    private void closeMenu(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotation", -135, 20, 0);
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(customConsiderLayout, "alpha", 1f, 0.5f);
        customConsiderLayout.setPivotX(mWidth / 2);
        customConsiderLayout.setPivotY(mHeight);
        ObjectAnimator animatorScaleX = ObjectAnimator.ofFloat(customConsiderLayout, "scaleX", 1.00f, 1.01f, 0.9f);
        ObjectAnimator animatorScaleY = ObjectAnimator.ofFloat(customConsiderLayout, "scaleY", 1.00f, 1.01f, 0.9f);
        ObjectAnimator alphaAnimator2 = ObjectAnimator.ofFloat(shadow_lay, "alpha", 0.2f, 0f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(animator).with(alphaAnimator).with(animatorScaleX).with(animatorScaleY).with(alphaAnimator2);
        animatorSet.setDuration(500);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                customConsiderLayout.setVisibility(View.GONE);
                shadow_lay.setVisibility(View.GONE);
                dismissConsiderLayout();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.start();

        fabOpened = false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_right:
                Intent intent = new Intent(this, HotelMapActivity.class);
                intent.putExtra("index", index);
                startActivity(intent);
                break;
            case R.id.fab:
                if (fabOpened) {
                    closeMenu(v);
                } else {
                    openMenu(v);
                }
                break;
            case R.id.shadow_lay:
                if (fabOpened) {
                    closeMenu(fab);
                }
                break;
            case R.id.iv_search:
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                if (listenerList != null && listenerList.size() > 0) {
                    if (!keyword.equals(et_keyword.getText().toString())) {
                        keyword = et_keyword.getText().toString();
                        for (OnUpdateHotelInfoListener listener : listenerList) {
                            listener.onUpdate(keyword);
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        mWidth = customConsiderLayout.getWidth();
        mHeight = customConsiderLayout.getHeight();
        super.onWindowFocusChanged(hasFocus);
    }

    private class myPagerAdapter extends FragmentPagerAdapter {
        String[] title = {"全日", "钟点", "夜归人"/*, "后悔药"*/};

        myPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return FragmentTabAllDay.newInstance(HotelCommDef.ALLDAY, keyword, mPriceIndex);
                case 1:
                    return FragmentTabClock.newInstance(HotelCommDef.CLOCK, keyword, mPriceIndex);
                case 2:
                    return FragmentTabYeGuiRen.newInstance(HotelCommDef.YEGUIREN, keyword, mPriceIndex);
//                case 3:
//                    return FragmentTabHouHuiYao.newInstance(HotelCommDef.HOUHUIYAO, dateStr);
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return title.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return title[position];
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (fabOpened) {
                closeMenu(fab);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public interface OnUpdateHotelInfoListener {
        void onUpdate(String keyword);
    }

    private static List<OnUpdateHotelInfoListener> listenerList = new ArrayList<>();

    public static void registerOnUpdateHotelInfoListener(OnUpdateHotelInfoListener listener) {
        if (!listenerList.contains(listener)) {
            listenerList.add(listener);
        }
    }

    public static void unRegisterOnUpdateHotelInfoListener(OnUpdateHotelInfoListener listener) {
        if (listenerList.contains(listener)) {
            listenerList.remove(listener);
        }
    }

    //通过反射设置TabLayout的Indicator的宽度
    public void setIndicator(TabLayout tabs, int leftDip, int rightDip) {
        Class<?> tabLayout = tabs.getClass();
        Field tabStrip = null;
        try {
            tabStrip = tabLayout.getDeclaredField("mTabStrip");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        tabStrip.setAccessible(true);
        LinearLayout llTab = null;
        try {
            llTab = (LinearLayout) tabStrip.get(tabs);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        int left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, leftDip, Resources.getSystem().getDisplayMetrics());
        int right = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, rightDip, Resources.getSystem().getDisplayMetrics());

        for (int i = 0; i < llTab.getChildCount(); i++) {
            View child = llTab.getChildAt(i);
            child.setPadding(0, 0, 0, 0);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1);
            params.leftMargin = left;
            params.rightMargin = right;
            params.gravity = Gravity.CENTER;
            child.setLayoutParams(params);
            child.setBackgroundResource(0);
            child.invalidate();
        }
    }
}
