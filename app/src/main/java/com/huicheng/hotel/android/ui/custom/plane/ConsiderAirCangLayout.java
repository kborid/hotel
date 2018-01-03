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

    private int mOriginalIndex;
    private int selectedIndex;

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
        mOriginalIndex = 0;
        selectedIndex = 0;
        setPadding(Utils.dp2px(34), 0, 0, 0);
        setOrientation(LinearLayout.VERTICAL);
        removeAllViews();
        List<String> list = Arrays.asList(context.getResources().getStringArray(R.array.flightCang));
        for (int i = 0; i < list.size(); i++) {
            if (i == 0) {
                addView(LayoutInflater.from(context).inflate(R.layout.lv_plane_consider_all_item, null));
            } else {
                addView(LayoutInflater.from(context).inflate(R.layout.lv_plane_consider_aircompany_item, null));
            }

            View item = getChildAt(i);
            item.findViewById(R.id.iv_air_logo).setVisibility(GONE);
            ((TextView) item.findViewById(R.id.tv_title)).setText(list.get(i));
            item.findViewById(R.id.root).setSelected(i == selectedIndex);
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
                    selectedIndex = finalI;
                    refreshViewSelectedStatus(selectedIndex);
                }
            });
        }
    }

    @Override
    protected void updateDataInfo(List<PlaneFlightInfoBean> list) {

    }

    @Override
    public int getFlightCang() {
        return selectedIndex;
    }

    @Override
    public void cancel() {
        selectedIndex = mOriginalIndex;
    }

    @Override
    public void reset() {
        selectedIndex = 0;
        refreshViewSelectedStatus(selectedIndex);
    }

    @Override
    public void save() {
        mOriginalIndex = selectedIndex;
    }

    @Override
    public void reload() {
        selectedIndex = mOriginalIndex;
        refreshViewSelectedStatus(selectedIndex);
    }
}
