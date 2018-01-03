package com.huicheng.hotel.android.ui.custom.plane;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneFlightInfoBean;
import com.huicheng.hotel.android.ui.custom.CustomDoubleSeekBar;

import java.util.List;

/**
 * @auth kborid
 * @date 2017/11/29 0029.
 */

public class ConsiderAirOffTimeLayout extends BaseConsiderAirLayout {

    private LinearLayout check_lay;
    private CustomDoubleSeekBar seekBar;

    private int mSelectedIndex, mOriginalIndex;
    private float mStartHour, mOriginalStartHour;
    private float mEndHour, mOriginalEndHour;

    public ConsiderAirOffTimeLayout(Context context) {
        super(context);
    }

    public ConsiderAirOffTimeLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ConsiderAirOffTimeLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initParams() {
        mSelectedIndex = -1;
        mStartHour = 0f;
        mEndHour = 24f;
        mOriginalIndex = -1;
        mOriginalStartHour = 0f;
        mOriginalEndHour = 24f;
        LayoutInflater.from(context).inflate(R.layout.layout_plane_consider_airofftime, this);
        check_lay = (LinearLayout) findViewById(R.id.check_lay);
        seekBar = (CustomDoubleSeekBar) findViewById(R.id.seekBar);
    }

    @Override
    protected void setListeners() {
        for (int i = 0; i < check_lay.getChildCount(); i++) {
            View view = check_lay.getChildAt(i);
            final int finalI = i;
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSelectedIndex = -1;
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
                mSelectedIndex = -1;
                setSelectedIndex(mSelectedIndex);
            }

            @Override
            public void onProgressChanged(CustomDoubleSeekBar seekBar, double progressLeft, double progressRight) {
                mStartHour = (float) progressLeft;
                mEndHour = (float) progressRight;
            }

            @Override
            public void onProgressAfter() {
            }
        });
    }

    @Override
    protected void updateDataInfo(List<PlaneFlightInfoBean> list) {

    }

    private void setSelectedIndex(int index) {
        for (int j = 0; j < check_lay.getChildCount(); j++) {
            if (check_lay.getChildAt(j).isSelected()) {
                check_lay.getChildAt(j).setSelected(false);
                break;
            }
        }
        if (index != -1) {
            mSelectedIndex = index;
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
    public void cancel() {
        mSelectedIndex = mOriginalIndex;
        mStartHour = mOriginalStartHour;
        mEndHour = mOriginalEndHour;
    }

    @Override
    public void reset() {
        mSelectedIndex = -1;
        setSelectedIndex(mSelectedIndex);

        mStartHour = 0;
        mEndHour = 24;
        seekBar.setProgressLeft(mStartHour);
        seekBar.setProgressRight(mEndHour);
    }

    @Override
    public void save() {
        mOriginalIndex = mSelectedIndex;
        mOriginalStartHour = mStartHour;
        mOriginalEndHour = mEndHour;
    }

    @Override
    public void reload() {
        mSelectedIndex = mOriginalIndex;
        setSelectedIndex(mSelectedIndex);

        mStartHour = mOriginalStartHour;
        mEndHour = mOriginalEndHour;
        seekBar.setProgressLeft(mStartHour);
        seekBar.setProgressRight(mEndHour);
    }

    public float[] getOffTimeLayoutValue() {
        float[] offTime = new float[2];
        offTime[0] = mStartHour;
        offTime[1] = mEndHour;
        return offTime;
    }
}
