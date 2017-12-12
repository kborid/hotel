package com.huicheng.hotel.android.ui.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;

/**
 * @author kborid
 * @date 2017/1/4 0004
 */
public class CommonAddSubLayout extends LinearLayout implements View.OnClickListener {
    private int MINVALUE = 0;
    private int MAXVALUE = 50;
    private Context context;

    private ImageView iv_sub, iv_add;
    private TextView tv_count, tv_unit;

    private int count = 0;

    public CommonAddSubLayout(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public CommonAddSubLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public CommonAddSubLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        LayoutInflater.from(context).inflate(R.layout.comm_down_up_layout, this);
        findViews();
        setClickListeners();
        updateButtonStatus(count);
    }

    private void findViews() {
        iv_sub = (ImageView) findViewById(R.id.iv_sub);
        iv_add = (ImageView) findViewById(R.id.iv_add);
        tv_count = (TextView) findViewById(R.id.tv_count);
        tv_unit = (TextView) findViewById(R.id.tv_unit);
    }

    private void setClickListeners() {
        iv_add.setOnClickListener(this);
        iv_sub.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_add:
                if (count < MAXVALUE) {
                    count++;
                    tv_count.setText(count + "");
                    if (null != listener) {
                        listener.onCountChanged(count);
                    }
                }
                updateButtonStatus(count);
                break;
            case R.id.iv_sub:
                if (count > 0) {
                    count--;
                    tv_count.setText(count + "");
                    if (null != listener) {
                        listener.onCountChanged(count);
                    }
                }
                updateButtonStatus(count);
                break;
            default:
                break;
        }
    }

    private void updateButtonStatus(int count) {
        if (MINVALUE == MAXVALUE) {
            iv_sub.setEnabled(false);
            iv_sub.setAlpha(0.5f);
            iv_add.setEnabled(false);
            iv_add.setAlpha(0.5f);
        } else if (count <= MINVALUE) {
            iv_add.setEnabled(true);
            iv_add.setAlpha(1f);
            iv_sub.setEnabled(false);
            iv_sub.setAlpha(0.5f);
        } else if (count >= MAXVALUE) {
            iv_sub.setEnabled(true);
            iv_sub.setAlpha(1f);
            iv_add.setEnabled(false);
            iv_add.setAlpha(0.5f);
        } else {
            iv_sub.setEnabled(true);
            iv_sub.setAlpha(1f);
            iv_add.setEnabled(true);
            iv_add.setAlpha(1f);
        }
    }

    public void setButtonEnable(boolean flag) {
        if (flag) {
            iv_sub.setEnabled(true);
            iv_sub.setAlpha(1f);
            iv_add.setEnabled(true);
            iv_add.setAlpha(1f);
        } else {
            iv_sub.setEnabled(false);
            iv_sub.setAlpha(0.5f);
            iv_add.setEnabled(false);
            iv_add.setAlpha(0.5f);
        }
    }

    public int getCount() {
        return count;
    }

    public void setCount(int def) {
        if (def <= MINVALUE) {
            count = MINVALUE;
        } else {
            count = def;
        }
        tv_count.setText(count + "");
        updateButtonStatus(count);
    }

    public void setMinvalue(int def) {
        if (def >= count) {
            count = def;
        }
        MINVALUE = def;
        tv_count.setText(count + "");
        updateButtonStatus(count);
    }
    public void setMaxvalue(int def) {
        MAXVALUE = def;
        updateButtonStatus(count);
    }

    public void setUnit(String def) {
        tv_unit.setText(def);
    }

    public interface OnCountChangedListener {
        void onCountChanged(int count);
    }

    private OnCountChangedListener listener = null;

    public void setOnCountChangedListener(OnCountChangedListener listener) {
        this.listener = listener;
    }
}
