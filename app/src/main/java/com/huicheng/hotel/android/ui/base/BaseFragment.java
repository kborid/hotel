package com.huicheng.hotel.android.ui.base;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.huicheng.hotel.android.PRJApplication;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.ui.dialog.ProgressDialog;
import com.prj.sdk.net.image.ImageLoader;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.StringUtil;
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
    protected int mMainColorAccent = R.color.mainColorAccent;
    protected int mSwipeRefreshColor = mMainColorAccent;
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
        mMainColorAccent = ta.getResourceId(R.styleable.MyTheme_mainColorAccent, R.color.mainColorAccent);
        mSwipeRefreshColor = ta.getResourceId(R.styleable.MyTheme_hotelRefreshColor, mMainColorAccent);
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

    public static void loadImage(final View view, String url, int width, int height) {
        loadImage(view, -1, url, width, height);
    }

    public static void loadImage(final View view, int defId, String url, int width, int height) {
        int resId = R.color.hintColor;
        if (defId != -1) {
            resId = defId;
        }
        if (view instanceof ImageView) {
            ((ImageView) view).setImageResource(resId);
        } else {
            view.setBackgroundResource(resId);
        }

        if (StringUtil.notEmpty(url)) {
            ImageLoader.getInstance().loadBitmap(new ImageLoader.ImageCallback() {
                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void imageCallback(Bitmap bm, String url, String imageTag) {
                    if (null != bm) {
                        if (view instanceof ImageView) {
                            ((ImageView) view).setImageBitmap(bm);
                        } else {
                            view.setBackground(new BitmapDrawable(bm));
                        }
                    }
                }
            }, url, url, width, height, -1);
        }
    }
}
