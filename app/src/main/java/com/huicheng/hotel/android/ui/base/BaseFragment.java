package com.huicheng.hotel.android.ui.base;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.huicheng.hotel.android.PRJApplication;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.ui.dialog.ProgressDialog;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.LogUtil;
import com.squareup.leakcanary.RefWatcher;
import com.umeng.analytics.MobclickAgent;

/**
 * fragment基类，提供公共属性
 */
public abstract class BaseFragment extends Fragment {
    protected final String TAG = getClass().getSimpleName();

    private ProgressDialog mProgressDialog;
    protected static String requestID;
    protected int mMainColor = R.color.mainColor;
    protected int mSwipeRefreshColor = mMainColor;
    /**
     * Fragment当前状态是否可见
     */
    protected boolean isVisible = false;
    protected boolean isPrepared = false;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isResumed()) {
            onVisibilityChangedToUser(isVisibleToUser, true);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        isPrepared = true;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    protected void initTypedArrayValue() {
        TypedArray ta = getActivity().obtainStyledAttributes(R.styleable.MyTheme);
        mMainColor = ta.getResourceId(R.styleable.MyTheme_mainColor, R.color.mainColor);
        mSwipeRefreshColor = ta.getResourceId(R.styleable.MyTheme_hotelRefreshColor, mMainColor);
        ta.recycle();
    }

    protected void onVisible() {
    }

    protected void onInvisible() {
    }

    protected void initViews(View view) {
    }

    public void dealIntent() {
    }

    // 参数设置
    protected void initParams() {
    }

    protected void initListeners() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getUserVisibleHint()) {
            onVisibilityChangedToUser(true, false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getUserVisibleHint()) {
            onVisibilityChangedToUser(false, false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = PRJApplication.getRefWatcher(getActivity());
        refWatcher.watch(this);
        DataLoader.getInstance().clearRequests();
    }

    /**
     * 当Fragment对用户的可见性发生了改变的时候就会回调此方法
     *
     * @param isVisibleToUser                      true：用户能看见当前Fragment；false：用户看不见当前Fragment
     * @param isHappenedInSetUserVisibleHintMethod true：本次回调发生在setUserVisibleHintMethod方法里；false：发生在onResume或onPause方法里
     */
    public void onVisibilityChangedToUser(boolean isVisibleToUser, boolean isHappenedInSetUserVisibleHintMethod) {
        if (isVisibleToUser) {
            isVisible = true;
            onVisible();
            MobclickAgent.onPageStart(this.getClass().getName());
        } else {
            isVisible = false;
            onInvisible();
            MobclickAgent.onPageEnd(this.getClass().getName());
            LogUtil.d(getClass().getSimpleName(), " - hidden - " + (isHappenedInSetUserVisibleHintMethod ? "setUserVisibleHint" : "onPause"));
        }
    }

    public final void showProgressDialog(Context context) {
        showProgressDialog(context, null);
    }

    /**
     * 显示loading对话框
     */
    public final void showProgressDialog(Context cxt, String tip) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(cxt);
        }
//        mProgressDialog.setMessage(tip);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    public final boolean isProgressShowing() {
        return mProgressDialog != null && mProgressDialog.isShowing();
    }

    /**
     * 销毁loading对话框
     */
    public final void removeProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }
}
