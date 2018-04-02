package com.huicheng.hotel.android.ui.activity.plane;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.content.AppConst;
import com.huicheng.hotel.android.content.NetURL;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneOrderDetailInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneTgqReasonInfoBean;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.StringUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author kborid
 * @date 2018/3/16 0016.
 */

public class PlaneBackTicketActivity extends BaseAppActivity {

    private static final int BACK_TICKET = 0;
    private static final int CHANGE_TICKET = 1;

    @BindView(R.id.root)
    FrameLayout root;
    @BindView(R.id.passenger_lay)
    LinearLayout passengerLay;
    @BindView(R.id.tv_back_reason)
    TextView tvBackReason;
    @BindView(R.id.tv_reason_detail)
    TextView tvBackReasonDetail;
    @BindView(R.id.tv_back_price)
    TextView tvBackPrice;
    @BindView(R.id.third_layout)
    LinearLayout thirdLayout;

    @BindView(R.id.tv_cost_money)
    TextView tvCostMoney;
    @BindView(R.id.tv_cut_money)
    TextView tvCutMoney;
    @BindView(R.id.cost_money_detail_lay)
    LinearLayout costMoneyDetailLayout;
    @BindView(R.id.cut_money_detail_lay)
    LinearLayout cutMoneyDetailLayout;

    private PlaneOrderDetailInfoBean.TripInfo tripInfo = null;
    private int mActionFlag = BACK_TICKET;
    private List<PlaneTgqReasonInfoBean> mPlaneTgqReasonList = null;
    private List<PlaneOrderDetailInfoBean.PassengerInfo> passengerInfoList = null;

    private String selectedPassengerIds = "";
    private int selectedPassengerCnt = 0;
    private int backAllFee = 0;
    private int costAllFee = 0;
    private int cutAllFee = 0;

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
            mActionFlag = bundle.getInt("plane_action");
        }
    }

    @Override
    protected void requestData() {
        super.requestData();
        if (null != tripInfo) {
            if (0 == mActionFlag) {
                requestTicketBackQuery(tripInfo.tripId);
            } else {
//                requestTicketChangeQuery();
            }
        }
    }

    @Override
    protected void initParams() {
        super.initParams();
        findViewById(R.id.comm_title_rl).setBackgroundColor(getResources().getColor(R.color.white));
        tv_center_title.setText("申请退款");
        root.setLayoutAnimation(getAnimationController());
        root.setVisibility(View.INVISIBLE);
    }

    private void refreshScreenTicketInfo(String jsonStr) {
        if (StringUtil.notEmpty(jsonStr)) {
            JSONObject mJson = JSON.parseObject(jsonStr);
            for (String key : mJson.keySet()) {
                LogUtil.i(key + ":" + mJson.getString(key));
            }
            //第一步：更新乘机人信息
            if (mJson.containsKey("passengers")) {
                passengerInfoList = JSON.parseArray(mJson.getString("passengers"), PlaneOrderDetailInfoBean.PassengerInfo.class);
                if (passengerInfoList != null && passengerInfoList.size() > 0) {
                    passengerLay.removeAllViews();
                    for (int i = 0; i < passengerInfoList.size(); i++) {
                        View v = LayoutInflater.from(this).inflate(R.layout.layout_passenter_backticket_item, null);
                        passengerLay.addView(v);
                        TextView tv_name = (TextView) v.findViewById(R.id.tv_name);
                        tv_name.setText(passengerInfoList.get(i).name);
                        tv_name.append(String.format("（%1$s）", "0".equals(passengerInfoList.get(i).passengerType) ? "成人" : "儿童"));
                        v.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                v.setSelected(!v.isSelected());
                                selectedPassengerIds = getSelectedPassengerIds();
                                refreshMoneySimpleLayout();
                                refreshMoneyDetailLayout();
                            }
                        });
                    }
                }
            }
            //第二步：更新退票原因
            if (mJson.containsKey("tgqReasonList")) {
                mPlaneTgqReasonList = JSON.parseArray(mJson.getString("tgqReasonList"), PlaneTgqReasonInfoBean.class);
                if (mPlaneTgqReasonList != null && mPlaneTgqReasonList.size() > 0) {
                    mReasonStr = new String[mPlaneTgqReasonList.size()];
                    for (int i = 0; i < mPlaneTgqReasonList.size(); i++) {
                        mReasonStr[i] = mPlaneTgqReasonList.get(i).msg;
                    }
                }
                tvBackReason.setText(mReasonStr[mReasonSelectedIndex]);
            }

            //第三步：更新退票凭证
            if (mPlaneTgqReasonList != null && mPlaneTgqReasonList.size() > 0) {
                updateThirdLayoutText(mPlaneTgqReasonList.get(mReasonSelectedIndex));
            }

            //最后：预估退款金额
            refreshMoneySimpleLayout();
            refreshMoneyDetailLayout();

            root.setVisibility(View.VISIBLE);
        }
    }

    private String getSelectedPassengerIds() {
        StringBuilder tmpStr = new StringBuilder();
        if (passengerInfoList != null && passengerInfoList.size() > 0) {
            for (int i = 0; i < passengerLay.getChildCount(); i++) {
                if (passengerLay.getChildAt(i).isSelected()) {
                    if (StringUtil.notEmpty(tmpStr)) {
                        tmpStr.append(",");
                    }
                    tmpStr.append(passengerInfoList.get(i).id);
                }
            }
        }
        return tmpStr.toString();
    }

    private void refreshMoneySimpleLayout() {
        calculatorBackFee(mReasonSelectedIndex);
        //预估可退金额
        tvBackPrice.setText(String.format(getString(R.string.rmbStr2), backAllFee));
        //已购产品总额
        tvCostMoney.setText(String.format(getString(R.string.rmbStr2), costAllFee));
        //需扣款项总额
        tvCutMoney.setText(String.format(getString(R.string.sub_rmbStr2), cutAllFee));
    }

    //TODO:待优化，重复remove or add View操作
    private void refreshMoneyDetailLayout() {
        if (passengerInfoList != null && passengerInfoList.size() > 0) {
            int ticketAmount = 0, buildFuelAmount = 0, insureAmount = 0, cutAmount = 0;
            for (int i = 0; i < passengerLay.getChildCount(); i++) {
                if (passengerLay.getChildAt(i).isSelected()) {
                    PlaneOrderDetailInfoBean.PassengerInfo passengerInfo = passengerInfoList.get(i);
                    ticketAmount = passengerInfo.barePrice;
                    buildFuelAmount = (passengerInfo.buildPrice + passengerInfo.fuelPrice);
                    insureAmount = passengerInfo.insureAmount;
                    cutAmount = passengerInfo.refundAmount.get(0).refundFee;
                }
            }
            costMoneyDetailLayout.removeAllViews();
            if (ticketAmount > 0) {
                View ticketView = LayoutInflater.from(this).inflate(R.layout.layout_refund_money_item, null);
                costMoneyDetailLayout.addView(ticketView);
                ((TextView) ticketView.findViewById(R.id.tv_item_name)).setText("机票价（成人）");
                ((TextView) ticketView.findViewById(R.id.tv_item_price)).setText(String.format(getString(R.string.rmbStr2), ticketAmount));
                ((TextView) ticketView.findViewById(R.id.tv_item_count)).setText(String.format("%1$d人", selectedPassengerCnt));
            }
            if (buildFuelAmount > 0) {
                View buildFuelView = LayoutInflater.from(this).inflate(R.layout.layout_refund_money_item, null);
                costMoneyDetailLayout.addView(buildFuelView);
                ((TextView) buildFuelView.findViewById(R.id.tv_item_name)).setText("机建+燃油（成人）");
                ((TextView) buildFuelView.findViewById(R.id.tv_item_price)).setText(String.format(getString(R.string.rmbStr2), buildFuelAmount));
                ((TextView) buildFuelView.findViewById(R.id.tv_item_count)).setText(String.format("%1$d人", selectedPassengerCnt));
            }
            if (insureAmount > 0) {
                View insureView = LayoutInflater.from(this).inflate(R.layout.layout_refund_money_item, null);
                costMoneyDetailLayout.addView(insureView);
                ((TextView) insureView.findViewById(R.id.tv_item_name)).setText("保险（成人）");
                ((TextView) insureView.findViewById(R.id.tv_item_price)).setText(String.format(getString(R.string.rmbStr2), insureAmount));
                ((TextView) insureView.findViewById(R.id.tv_item_count)).setText(String.format("%1$d人", selectedPassengerCnt));
            }

            cutMoneyDetailLayout.removeAllViews();
            if (cutAllFee > 0) {
                View cutView = LayoutInflater.from(this).inflate(R.layout.layout_refund_money_item, null);
                cutMoneyDetailLayout.addView(cutView);
                ((TextView) cutView.findViewById(R.id.tv_item_name)).setText("手续费（成人）");
                ((TextView) cutView.findViewById(R.id.tv_item_price)).setText(String.format(getString(R.string.rmbStr2), cutAmount));
                ((TextView) cutView.findViewById(R.id.tv_item_count)).setText(String.format("%1$d人", selectedPassengerCnt));
            }
        }
    }

    /**
     * 计算预估可退金额
     *
     * @param reasonCode
     */
    private void calculatorBackFee(int reasonCode) {
        int tmpSelectedPassengerCnt = 0, tmpBackFee = 0, tmpCostFee = 0, tmpCutFee = 0;
        if (passengerInfoList != null && passengerInfoList.size() > 0) {
            for (int i = 0; i < passengerLay.getChildCount(); i++) {
                if (passengerLay.getChildAt(i).isSelected()) {
                    tmpSelectedPassengerCnt++;
                    PlaneOrderDetailInfoBean.PassengerInfo passengerInfo = passengerInfoList.get(i);
                    tmpCostFee += (passengerInfo.barePrice + passengerInfo.buildPrice + passengerInfo.insureAmount);
                    if (passengerInfo.refundAmount != null) {
                        PlaneOrderDetailInfoBean.RefundAmountInfo refundAmountInfo = null;
                        if (mPlaneTgqReasonList != null && mPlaneTgqReasonList.size() > 0) {
                            for (PlaneOrderDetailInfoBean.RefundAmountInfo tmp : passengerInfo.refundAmount) {
                                if (tmp.code == mPlaneTgqReasonList.get(reasonCode).code) {
                                    refundAmountInfo = tmp;
                                    break;
                                }
                            }
                        }
                        if (null != refundAmountInfo) {
                            tmpBackFee += refundAmountInfo.returnRefundFee;
                            tmpCutFee += refundAmountInfo.refundFee;
                        }
                    }
                }
            }
        }
        selectedPassengerCnt = tmpSelectedPassengerCnt;
        backAllFee = tmpBackFee;
        costAllFee = tmpCostFee;
        cutAllFee = tmpCutFee;
    }

    private void updateThirdLayoutText(PlaneTgqReasonInfoBean tgqReasonInfoBean) {
        if (null != tgqReasonInfoBean) {
            if (StringUtil.notEmpty(tgqReasonInfoBean.note)) {
                thirdLayout.setVisibility(View.VISIBLE);
                tvBackReasonDetail.setText(tgqReasonInfoBean.note);
            } else {
                thirdLayout.setVisibility(View.GONE);
            }
        }
    }

    private void requestTicketBackQuery(String tripId) {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("tripId", tripId);
        ResponseData d = b.syncRequest(b);
        d.flag = AppConst.PLANE_BACK_QUERY;
        d.path = NetURL.PLANE_BACK_QUERY;
        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void requestTicketBackApply(String tripId) {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("code", String.valueOf(mPlaneTgqReasonList.get(mReasonSelectedIndex).code));
        b.addBody("passengerIds", selectedPassengerIds);
        b.addBody("tripId", tripId);
        ResponseData d = b.syncRequest(b);
        d.flag = AppConst.PLANE_BACK;
        d.path = NetURL.PLANE_BACK;
        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void requestTicketChangeQuery(String tripId) {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("changeDate", tripId);
        b.addBody("orderNum", tripId);
        b.addBody("type", tripId);
        ResponseData d = b.syncRequest(b);
        d.flag = AppConst.PLANE_CHANGE_QUERY;
        d.path = NetURL.PLANE_CHANGE_QUERY;
        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void requestTicketChangeApple(String tripId) {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("changeDate", tripId);
        b.addBody("orderNum", tripId);
        b.addBody("type", tripId);
        ResponseData d = b.syncRequest(b);
        d.flag = AppConst.PLANE_CHANGE;
        d.path = NetURL.PLANE_CHANGE;
        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    @Override
    protected void onNotifyMessage(ResponseData request, ResponseData response) {
        super.onNotifyMessage(request, response);
        if (response != null && response.body != null) {
            if (request.flag == AppConst.PLANE_BACK_QUERY) {
                removeProgressDialog();
                LogUtil.i("json = " + response.body.toString());
                refreshScreenTicketInfo(response.body.toString());
            }
        }
    }

    @OnClick({R.id.tv_back_reason, R.id.tv_back_submit})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_back_reason:
                CustomDialog reasonDialog = new CustomDialog(this);
                reasonDialog.setTitle("选择退票理由");
                reasonDialog.setSingleChoiceItems(mReasonStr, mReasonSelectedIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which != mReasonSelectedIndex) {
                            dialog.dismiss();
                            mReasonSelectedIndex = which;
                            tvBackReason.setText(mReasonStr[mReasonSelectedIndex]);
                            if (mPlaneTgqReasonList != null && mPlaneTgqReasonList.size() > 0) {
                                updateThirdLayoutText(mPlaneTgqReasonList.get(mReasonSelectedIndex));
                            }
                            refreshMoneySimpleLayout();
                            refreshMoneyDetailLayout();
                        }
                    }
                });
                reasonDialog.setCanceledOnTouchOutside(true);
                reasonDialog.show();
                break;
            case R.id.tv_back_submit:
                CustomToast.show("翻滚吧，少年！", CustomToast.LENGTH_LONG);
                if (mActionFlag == BACK_TICKET) {
                    requestTicketBackApply(tripInfo.tripId);
                } else {

                }
                break;
        }
    }
}
