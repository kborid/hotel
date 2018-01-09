package com.huicheng.hotel.android.ui.custom.plane;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneFlightInfoBean;
import com.prj.sdk.util.Utils;

import java.util.Arrays;
import java.util.List;

/**
 * @auth kborid
 * @date 2017/11/29 0029.
 */

public class ConsiderAirCangLayout extends BaseConsiderAirLayout {

    public ConsiderAirCangLayout(Context context) {
        super(context);
    }

    public ConsiderAirCangLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ConsiderAirCangLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initParams() {
        setPadding(Utils.dp2px(34), 0, 0, 0);
        setOrientation(LinearLayout.VERTICAL);
        mSelectedIndex.clear();
        mSelectedIndex.add(0);
        mOriginalIndex.clear();
        mOriginalIndex.add(0);

        //初始化数据
        removeAllViews();
        List<String> tmp = Arrays.asList(context.getResources().getStringArray(R.array.flightCang));
        for (int i = 0; i < tmp.size(); i++) {
            if (i == 0) {
                addView(LayoutInflater.from(context).inflate(R.layout.lv_plane_consider_all_item, null));
            } else {
                addView(LayoutInflater.from(context).inflate(R.layout.lv_plane_consider_aircompany_item, null));
            }

            View item = getChildAt(i);
            item.findViewById(R.id.iv_air_logo).setVisibility(GONE);
            ((TextView) item.findViewById(R.id.tv_title)).setText(tmp.get(i));
            refreshViewSelectedStatus(mSelectedIndex);
        }
    }

    @Override
    protected void setListeners() {
        for (int i = 0; i < getChildCount(); i++) {
            View item = getChildAt(i);
            final int finalI = i;
            item.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSelectedIndex.clear();
                    mSelectedIndex.add(finalI);
                    refreshViewSelectedStatus(mSelectedIndex);
                }
            });
        }
    }

    @Override
    protected void updateDataInfo(List<PlaneFlightInfoBean> list) {
    }

    @Override
    public int[] getFlightConditionValue() {
        return convertArrays(mSelectedIndex);
    }

    @Override
    public void cancel() {
        mSelectedIndex.clear();
        mSelectedIndex.addAll(mOriginalIndex);
    }

    @Override
    public void reset() {
        mSelectedIndex.clear();
        mSelectedIndex.add(0);
        refreshViewSelectedStatus(mSelectedIndex);
    }

    @Override
    public void save() {
        mOriginalIndex.clear();
        mOriginalIndex.addAll(mSelectedIndex);
    }

    @Override
    public void reload() {
        mSelectedIndex.clear();
        mSelectedIndex.addAll(mOriginalIndex);
        refreshViewSelectedStatus(mSelectedIndex);
    }
}
