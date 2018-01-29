package com.huicheng.hotel.android.ui.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.HotelCommDef;

/**
 * @auth kborid
 * @date 2017/12/7 0007.
 */

public class CommonPayChannelLayout extends LinearLayout {
    private Context context;
    private int[] payIcon = new int[]{
            R.drawable.iv_pay_zhifubao,
            R.drawable.iv_pay_weixin,
            R.drawable.iv_pay_union
    };
    private String[] payChannel = new String[]{"支付宝", "微信"/*, "银联"*/};
    private LinearLayout payListLay;
    private int payIndex = 0;


    public CommonPayChannelLayout(Context context) {
        this(context, null);
    }

    public CommonPayChannelLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CommonPayChannelLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        LayoutInflater.from(context).inflate(R.layout.layout_pay_channel, this);
        payListLay = (LinearLayout) findViewById(R.id.payListLay);
        payListLay.removeAllViews();
        for (int i = 0; i < payChannel.length; i++) {
            View view = LayoutInflater.from(context).inflate(R.layout.lv_pay_item, null);
            ImageView iv_pay_icon = (ImageView) view.findViewById(R.id.iv_pay_icon);
            TextView tv_pay_title = (TextView) view.findViewById(R.id.tv_pay_title);
            ImageView iv_pay_sel = (ImageView) view.findViewById(R.id.iv_pay_sel);
            iv_pay_icon.setImageResource(payIcon[i]);
            tv_pay_title.setText(payChannel[i]);
            if (0 == i) {
                iv_pay_sel.setImageResource(R.drawable.iv_pay_checked);
            }
            final int finalI = i;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    payIndex = finalI;
                    ((ImageView) payListLay.getChildAt(payIndex).findViewById(R.id.iv_pay_sel)).setImageResource(R.drawable.iv_pay_checked);
                    for (int j = 0; j < payChannel.length; j++) {
                        if (j != payIndex) {
                            ((ImageView) payListLay.getChildAt(j).findViewById(R.id.iv_pay_sel)).setImageResource(R.drawable.iv_pay_check);
                        }
                    }
                }
            });
            payListLay.addView(view);
        }
    }

    public int getPayIndex() {
        return payIndex;
    }

    public String getPayChannel() {
        return HotelCommDef.getPayChannel(payIndex);
    }
}
