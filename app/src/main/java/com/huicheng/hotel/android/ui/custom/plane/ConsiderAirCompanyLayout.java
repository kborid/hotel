package com.huicheng.hotel.android.ui.custom.plane;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.PlaneCommDef;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.requestbuilder.bean.AirCompanyInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneFlightInfoBean;
import com.huicheng.hotel.android.ui.glide.CustomReqURLFormatModelImpl;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @auth kborid
 * @date 2017/11/29 0029.
 */

public class ConsiderAirCompanyLayout extends BaseConsiderAirLayout {

    private List<String> mList = new ArrayList<>();

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
        setPadding(Utils.dp2px(34), 0, 0, 0);
        setOrientation(LinearLayout.VERTICAL);
        mOriginalIndex.clear();
        mOriginalIndex.add(0);
        mSelectedIndex.clear();
        mSelectedIndex.add(0);
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
        if (list != null && list.size() > 0) {
            mList.clear();
            mList.add("不限");

            for (int i = 0; i < list.size(); i++) {
                String carrier = list.get(i).carrier;
                if (!mList.contains(carrier)) {
                    mList.add(carrier);
                }
            }

            removeAllViews();
            for (int i = 0; i < mList.size(); i++) {
                if (i == 0) {
                    addView(LayoutInflater.from(context).inflate(R.layout.lv_plane_consider_all_item, null));
                } else {
                    addView(LayoutInflater.from(context).inflate(R.layout.lv_plane_consider_aircompany_item, null));
                }

                View item = getChildAt(i);
                ImageView iv_air_logo = (ImageView) item.findViewById(R.id.iv_air_logo);
                TextView tv_name = ((TextView) item.findViewById(R.id.tv_title));
                String name = mList.get(i);
                if (StringUtil.notEmpty(name)) {
                    if (SessionContext.getAirCompanyMap().size() > 0
                            && SessionContext.getAirCompanyMap().containsKey(name)) {
                        AirCompanyInfoBean companyInfoBean = SessionContext.getAirCompanyMap().get(name);
                        tv_name.setText(companyInfoBean.company);
                        iv_air_logo.setVisibility(View.VISIBLE);
                        Glide.with(context)
                                .load(new CustomReqURLFormatModelImpl(companyInfoBean.logourl))
                                .fitCenter()
                                .override(Utils.dp2px(15), Utils.dp2px(15))
                                .into(iv_air_logo);
                    } else {
                        tv_name.setText(name);
                        if (StringUtil.notEmpty(name)
                                && PlaneCommDef.AIR_ICON_CODE.containsKey(name)
                                && PlaneCommDef.AIR_ICON_CODE.get(name) != 0) {
                            iv_air_logo.setVisibility(View.VISIBLE);
                            iv_air_logo.setImageResource(PlaneCommDef.AIR_ICON_CODE.get(name));
                        } else {
                            iv_air_logo.setVisibility(View.INVISIBLE);
                        }
                    }
                } else {
                    iv_air_logo.setVisibility(View.GONE);
                }
                refreshViewSelectedStatus(mSelectedIndex);
            }
            setListeners();
        }
    }

    @Override
    public List<String> getListData() {
        return mList;
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
