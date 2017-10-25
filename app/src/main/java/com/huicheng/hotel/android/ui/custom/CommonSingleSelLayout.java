package com.huicheng.hotel.android.ui.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;

import java.util.Arrays;
import java.util.List;

/**
 * @auth kborid
 * @date 2017/8/8.
 */

public class CommonSingleSelLayout extends LinearLayout {
    private Context context;

    private int textColor;
    private float textSize = 15;

    public CommonSingleSelLayout(Context context) {
        this(context, null);
    }

    public CommonSingleSelLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CommonSingleSelLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CommonSingleSelLayout);
        textColor = ta.getResourceId(R.styleable.CommonSingleSelLayout_textColor, R.color.consider_text_sel);
        textSize = ta.getDimension(R.styleable.CommonSingleSelLayout_textSize, 15);
        ta.recycle();
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.singleselectorlayout, this);
    }

    public void setData(List<String> list) {
        if (list != null && list.size() > 0) {
            removeAllViews();
            for (int i = 0; i < list.size(); i++) {
                TextView tv = new TextView(context);
                tv.setGravity(Gravity.CENTER);
                tv.setTextSize(textSize);
                tv.setTextColor(context.getResources().getColorStateList(textColor));
                tv.setText(list.get(i));
                final int finalI = i;
                tv.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (getChildAt(finalI).isSelected()) {
                            getChildAt(finalI).setSelected(false);
                        } else {
                            setSelected(finalI);
                        }
                    }
                });
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                lp.weight = 1;
                addView(tv, lp);
            }
        }

    }

    public void setSelected(int index) {
        for (int i = 0; i < getChildCount(); i++) {
            if (index == i) {
                getChildAt(i).setSelected(true);
            } else {
                getChildAt(i).setSelected(false);
            }
        }
    }

    public int getSelectedIndex() {
        int index = -1;
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i).isSelected()) {
                index = i;
                break;
            }
        }
        return index;
    }

    public String getSelectedItem() {
        String str = "";
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i).isSelected()) {
                str = ((TextView) getChildAt(i)).getText().toString();
                break;
            }
        }
        return str;
    }

    public int resetSelectedIndex() {
        setSelected(0);
        getChildAt(0).setSelected(false);
        return -1;
    }

    public void setData(String[] data) {
        List<String> list;
        list = Arrays.asList(data);
        setData(list);
    }
}
