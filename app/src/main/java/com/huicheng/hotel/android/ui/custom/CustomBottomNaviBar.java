package com.huicheng.hotel.android.ui.custom;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
    private int[] mIndicatorColorId = {
            R.color.tabHotelColor,
            R.color.tabPlaneColor,
            R.color.tabTrainColor,
            R.color.tabTaxiColor
    };

    public CustomBottomNaviBar(Context context) {
        this(context, null);
    }

    public CustomBottomNaviBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomBottomNaviBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        LayoutInflater.from(context).inflate(R.layout.custom_bottom_navigation_bar, this);
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
                iv_indicator.setBackgroundResource(mIndicatorColorId[i]);
                iv_icon.setImageResource(mResId[i]);
            } else {
                iv_indicator.setBackgroundResource(R.color.transparent);
                Bitmap bm = BitmapUtils.getAlphaBitmap(context.getResources().getDrawable(mResId[i]), context.getResources().getColor(R.color.tabDefaultColor));
                iv_icon.setImageBitmap(bm);
            }
            final int finalI = i;
            v.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!AppConst.ISDEVELOP && 1 == finalI) {
                        CustomDialog dialog = new CustomDialog(context);
                        dialog.setMessage("机票预订正在测试中，即将与您见面");
                        dialog.setNegativeButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        dialog.setCanceledOnTouchOutside(true);
                        dialog.show();
                    } else if (!SessionContext.isLogin() && 1 == finalI) {
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
            my_icon.setImageResource(R.drawable.iv_tab_msg2);
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
                v.findViewById(R.id.tab_indicator).setBackgroundResource(mIndicatorColorId[i]);
                ((ImageView) v.findViewById(R.id.tab_icon)).setImageResource(mResId[i]);
            } else {
                v.findViewById(R.id.tab_indicator).setBackgroundResource(R.color.transparent);
                Bitmap bm = BitmapUtils.getAlphaBitmap(context.getResources().getDrawable(mResId[i]), context.getResources().getColor(R.color.tabDefaultColor));
                ((ImageView) v.findViewById(R.id.tab_icon)).setImageBitmap(bm);
            }
        }
    }
}