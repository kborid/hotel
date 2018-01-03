package com.huicheng.hotel.android.ui.custom.plane;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.PlaneCommDef;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneFlightInfoBean;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;

import java.util.List;

/**
 * @auth kborid
 * @date 2017/11/29 0029.
 */

public class ConsiderAirCompanyLayout extends BaseConsiderAirLayout {

    private int mOriginalIndex;
    private int selectedIndex;

    public ConsiderAirCompanyLayout(Context context) {
        super(context);
    }

    public ConsiderAirCompanyLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ConsiderAirCompanyLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initParams() {
        mOriginalIndex = 0;
        selectedIndex = 0;
        setPadding(Utils.dp2px(34), 0, 0, 0);
        setOrientation(LinearLayout.VERTICAL);
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
        if (list != null && list.size() > 0) {
            removeAllViews();
            for (int i = 0; i < list.size(); i++) {
                if (i == 0) {
                    addView(LayoutInflater.from(context).inflate(R.layout.lv_plane_consider_all_item, null));
                } else {
                    addView(LayoutInflater.from(context).inflate(R.layout.lv_plane_consider_aircompany_item, null));
                }

                View item = getChildAt(i);
                ImageView iv_air_logo = (ImageView) item.findViewById(R.id.iv_air_logo);
                String name = list.get(i).carrier;
                if (StringUtil.notEmpty(name)
                        && PlaneCommDef.AIR_ICON_CODE.containsKey(name)
                        && PlaneCommDef.AIR_ICON_CODE.get(name) != 0) {
                    iv_air_logo.setImageResource(PlaneCommDef.AIR_ICON_CODE.get(name));
                    iv_air_logo.setVisibility(View.VISIBLE);
                } else {
                    iv_air_logo.setVisibility(View.INVISIBLE);
                }
                ((TextView) item.findViewById(R.id.tv_title)).setText(name);
                item.findViewById(R.id.root).setSelected(i == selectedIndex);
            }
        }
    }

    @Override
    public int[] getAirCompanyValue() {
        return new int[2];
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
