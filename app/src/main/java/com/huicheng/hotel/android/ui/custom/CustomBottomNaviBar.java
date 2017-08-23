package com.huicheng.hotel.android.ui.custom;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.util.BitmapUtils;

/**
 * @author kborid
 * @date 2016/10/27 0027
 */
public class CustomBottomNaviBar extends LinearLayout {

    private Context context;

    private LinearLayout root_lay;
    private RelativeLayout tab_my;
    private ImageView my_icon;
    private int[] mResId = {
            R.drawable.iv_tab_hotel,
            R.drawable.iv_tab_plane,
            R.drawable.iv_tab_train,
            R.drawable.iv_tab_taxi
    };
    private int[] mTabColorId = new int[4];
    private String[] tips = new String[4];

    private int hasMessageImageId;

    public CustomBottomNaviBar(Context context) {
        this(context, null);
    }

    public CustomBottomNaviBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomBottomNaviBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        Resources resources = context.getResources();
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MyTheme);
        mTabColorId[0] = ta.getColor(R.styleable.MyTheme_tabHotelColor, resources.getColor(R.color.tabHotelColor));
        mTabColorId[1] = ta.getColor(R.styleable.MyTheme_tabPlaneColor, resources.getColor(R.color.tabPlaneColor));
        mTabColorId[2] = ta.getColor(R.styleable.MyTheme_tabTrainColor, resources.getColor(R.color.tabTrainColor));
        mTabColorId[3] = ta.getColor(R.styleable.MyTheme_tabTaxiColor, resources.getColor(R.color.tabTaxiColor));
        hasMessageImageId = ta.getResourceId(R.styleable.MyTheme_messageHasImage, R.drawable.iv_tab_msg2);
        ta.recycle();
        init();
    }

    private void init() {
        LayoutInflater.from(context).inflate(R.layout.custom_bottom_navigation_bar, this);
        tips = context.getResources().getStringArray(R.array.MainTabTips);
        findViews();
        initTabLayout();
        setListeners();
    }

    private void findViews() {
        root_lay = (LinearLayout) findViewById(R.id.tab_root_lay);
        tab_my = (RelativeLayout) findViewById(R.id.my_lay);
        my_icon = (ImageView) findViewById(R.id.my_icon);
    }

    private void initTabLayout() {
        root_lay.removeAllViews();
        for (int i = 0; i < 4; i++) {
            View v = LayoutInflater.from(context).inflate(R.layout.lv_tab_item, null);
            LinearLayout.LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            lp.weight = 1;
            ImageView iv_indicator = (ImageView) v.findViewById(R.id.tab_indicator);
            ImageView iv_icon = (ImageView) v.findViewById(R.id.tab_icon);
            if (0 == i) {
                iv_indicator.setBackgroundColor(mTabColorId[i]);
                Bitmap bm = BitmapUtils.getAlphaBitmap(context.getResources().getDrawable(mResId[i]), mTabColorId[i]);
                iv_icon.setImageBitmap(bm);
            } else {
                iv_indicator.setBackgroundColor(context.getResources().getColor(R.color.transparent));
                Bitmap bm = BitmapUtils.getAlphaBitmap(context.getResources().getDrawable(mResId[i]), context.getResources().getColor(R.color.tabDefaultColor));
                iv_icon.setImageBitmap(bm);
            }
            final int finalI = i;
            v.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!AppConst.ISDEVELOP && finalI != 0) {
                        CustomDialog dialog = new CustomDialog(context);
                        dialog.setMessage(tips[finalI]);
                        dialog.setNegativeButton(context.getResources().getString(R.string.iknown), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        dialog.setCanceledOnTouchOutside(true);
                        dialog.show();
                    } else if (!SessionContext.isLogin() && finalI != 0) {
                        context.sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION).putExtra(BroadCastConst.IS_SHOW_TIP_DIALOG, true));
                    } else {
                        refreshTabLayout(finalI, true);
                        if (null != listener) {
                            listener.onChanged(finalI);
                        }
                    }
                }
            });
            root_lay.addView(v, i, lp);
        }
    }

    public void updateUserMsgBtnStatus(boolean hasMsg) {
        if (hasMsg) {
            my_icon.setImageResource(hasMessageImageId);
        } else {
            my_icon.setImageResource(R.drawable.iv_tab_msg1);
        }
    }

    public interface OnChangeClickListener {
        void onChanged(int index);
    }

    private OnChangeClickListener listener = null;

    public void setOnChangeClickListener(OnChangeClickListener listener) {
        this.listener = listener;
    }

    private void setListeners() {
    }

    public void refreshTabLayout(int index, boolean isSelected) {
        for (int i = 0; i < 4; i++) {
            View v = root_lay.getChildAt(i);
            if (index == i) {
                v.findViewById(R.id.tab_indicator).setBackgroundColor(mTabColorId[i]);
                Bitmap bm = BitmapUtils.getAlphaBitmap(context.getResources().getDrawable(mResId[i]), mTabColorId[i]);
                ((ImageView) v.findViewById(R.id.tab_icon)).setImageBitmap(bm);
            } else {
                v.findViewById(R.id.tab_indicator).setBackgroundColor(context.getResources().getColor(R.color.transparent));
                Bitmap bm = BitmapUtils.getAlphaBitmap(context.getResources().getDrawable(mResId[i]), context.getResources().getColor(R.color.tabDefaultColor));
                ((ImageView) v.findViewById(R.id.tab_icon)).setImageBitmap(bm);
            }
        }
    }
}
