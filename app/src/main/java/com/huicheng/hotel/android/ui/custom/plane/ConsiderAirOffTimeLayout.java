package com.huicheng.hotel.android.ui.custom.plane;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.ui.custom.CustomDoubleSeekBar;
import com.prj.sdk.app.AppConst;
import com.prj.sdk.util.SharedPreferenceUtil;

/**
 * @auth kborid
 * @date 2017/11/29 0029.
 */

public class ConsiderAirOffTimeLayout extends LinearLayout implements IPlaneConsiderAction {

    private Context context;
    private LinearLayout check_lay;
    private int selectedIndex = -1;
    private CustomDoubleSeekBar seekBar;
    private float startHour, endHour;

    public ConsiderAirOffTimeLayout(Context context) {
        this(context, null);
    }

    public ConsiderAirOffTimeLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ConsiderAirOffTimeLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
        initListeners();
    }

    private void init() {
        LayoutInflater.from(context).inflate(R.layout.layout_plane_consider_airofftime, this);
        check_lay = (LinearLayout) findViewById(R.id.check_lay);
        seekBar = (CustomDoubleSeekBar) findViewById(R.id.seekBar);
    }

    private void initListeners() {
        for (int i = 0; i < check_lay.getChildCount(); i++) {
            View view = check_lay.getChildAt(i);
            final int finalI = i;
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedIndex = -1;
                    if (v.isSelected()) {
                        v.setSelected(false);
                    } else {
                        setSelectedIndex(finalI);
                    }
                }
            });
        }
        seekBar.setOnSeekBarChangeListener(new CustomDoubleSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressBefore() {
                selectedIndex = -1;
                setSelectedIndex(selectedIndex);
            }

            @Override
            public void onProgressChanged(CustomDoubleSeekBar seekBar, double progressLeft, double progressRight) {
                startHour = (float) progressLeft;
                endHour = (float) progressRight;
            }

            @Override
            public void onProgressAfter() {
            }
        });
    }

    private void setSelectedIndex(int index) {
        for (int j = 0; j < check_lay.getChildCount(); j++) {
            if (check_lay.getChildAt(j).isSelected()) {
                check_lay.getChildAt(j).setSelected(false);
                break;
            }
        }
        if (index != -1) {
            selectedIndex = index;
            check_lay.getChildAt(index).setSelected(true);
            switch (index) {
                case 0:
                    seekBar.setProgressLeft(0);
                    seekBar.setProgressRight(12);
                    break;
                case 1:
                    seekBar.setProgressLeft(12);
                    seekBar.setProgressRight(18);
                    break;
                case 2:
                    seekBar.setProgressLeft(18);
                    seekBar.setProgressRight(24);
                    break;
            }
        }
    }

    @Override
    public void cancelConsiderConfig() {
        reloadConsiderConfig();
    }

    @Override
    public void resetConsiderConfig() {
        selectedIndex = -1;
        setSelectedIndex(selectedIndex);
        startHour = 0;
        endHour = 24;
        seekBar.setProgressLeft(startHour);
        seekBar.setProgressRight(endHour);
    }

    @Override
    public void saveConsiderConfig() {
        SharedPreferenceUtil.getInstance().setInt(AppConst.CONSIDER_PLANE_DURING, selectedIndex);
        SharedPreferenceUtil.getInstance().setFloat(AppConst.CONSIDER_PLANE_START_HOUR, startHour);
        SharedPreferenceUtil.getInstance().setFloat(AppConst.CONSIDER_PLANE_END_HOUR, endHour);
    }

    @Override
    public void reloadConsiderConfig() {
        selectedIndex = SharedPreferenceUtil.getInstance().getInt(AppConst.CONSIDER_PLANE_DURING, -1);
        setSelectedIndex(selectedIndex);
        startHour = SharedPreferenceUtil.getInstance().getFloat(AppConst.CONSIDER_PLANE_START_HOUR, 0f);
        endHour = SharedPreferenceUtil.getInstance().getFloat(AppConst.CONSIDER_PLANE_END_HOUR, 24f);
        seekBar.setProgressLeft(startHour);
        seekBar.setProgressRight(endHour);
    }

    public float[] getOffTimeStartEnd() {
        float[] offTime = new float[2];
        offTime[0] = startHour;
        offTime[1] = endHour;
        return offTime;
    }
}
