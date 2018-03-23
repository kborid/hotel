package com.huicheng.hotel.android.ui.activity.plane;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneOrderDetailInfoBean;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author kborid
 * @date 2018/3/16 0016.
 */

public class PlaneBackTicketActivity extends BaseAppActivity {

    @BindView(R.id.passenger_lay)
    LinearLayout passengerLay;
    @BindView(R.id.tv_back_reason)
    TextView tvBackReason;
    @BindView(R.id.tv_back_price)
    TextView tvBackPrice;

    private PlaneOrderDetailInfoBean.TripInfo tripInfo = null;
    private String[] mReasonStr;
    private int mReasonSelectedIndex = 0;

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_plane_backticket);
    }

    @Override
    protected void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            if (null != bundle.getSerializable("tripInfo")) {
                tripInfo = (PlaneOrderDetailInfoBean.TripInfo) bundle.getSerializable("tripInfo");
            }
        }
    }

    @Override
    protected void initParams() {
        super.initParams();
        findViewById(R.id.comm_title_rl).setBackgroundColor(getResources().getColor(R.color.white));
        tv_center_title.setText("申请退款");
        initPassengerLayout();
        mReasonStr = getResources().getStringArray(R.array.BackTicketReason);
        tvBackReason.setText(mReasonStr[mReasonSelectedIndex]);
    }

    private void initPassengerLayout() {
        if (null != tripInfo && null != tripInfo.passengerList) {
            passengerLay.removeAllViews();
            for (int i = 0; i < tripInfo.passengerList.size(); i++) {
                View v = LayoutInflater.from(this).inflate(R.layout.layout_passenter_backticket_item, null);
                passengerLay.addView(v);
//                ImageView iv_flag = (ImageView) v.findViewById(R.id.iv_flag);
                TextView tv_name = (TextView) v.findViewById(R.id.tv_name);
                tv_name.setText(tripInfo.passengerList.get(i).name);
                tv_name.append(String.format("（%1$s）", tripInfo.passengerList.get(i).passengerType));
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.setSelected(!v.isSelected());
                    }
                });
            }
        }
    }

    @OnClick({R.id.tv_back_reason, R.id.tv_back_price})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_back_reason:
                CustomDialog reasonDialog = new CustomDialog(this);
                reasonDialog.setTitle("选择退票理由");
                reasonDialog.setSingleChoiceItems(mReasonStr, mReasonSelectedIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which != mReasonSelectedIndex) {
                            mReasonSelectedIndex = which;
                            tvBackReason.setText(mReasonStr[mReasonSelectedIndex]);
                            dialog.dismiss();
                        }
                    }
                });
                reasonDialog.setCanceledOnTouchOutside(true);
                reasonDialog.show();
                break;
            case R.id.tv_back_price:
                break;
        }
    }
}
