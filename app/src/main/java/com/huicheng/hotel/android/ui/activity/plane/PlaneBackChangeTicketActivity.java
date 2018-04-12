package com.huicheng.hotel.android.ui.activity.plane;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.PlaneCommDef;
import com.huicheng.hotel.android.common.PlaneOrderManager;
import com.huicheng.hotel.android.content.AppConst;
import com.huicheng.hotel.android.content.NetURL;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneFlightChangeInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneOrderDetailInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneTgqReasonInfoBean;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.StringUtil;

import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author kborid
 * @date 2018/3/16 0016.
 */

public class PlaneBackChangeTicketActivity extends BaseAppActivity {

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
    @BindView(R.id.third_step_layout)
    LinearLayout thirdStepLayout;
    @BindView(R.id.tv_third_name)
    TextView tvThirdTitleName;
    @BindView(R.id.third_content_layout)
    LinearLayout thirdContentLayout;

    @BindView(R.id.back_price_layout)
    LinearLayout backPriceLayout;
    @BindView(R.id.tv_cost_money)
    TextView tvCostMoney;
    @BindView(R.id.tv_cut_money)
    TextView tvCutMoney;
    @BindView(R.id.cost_money_detail_lay)
    LinearLayout costMoneyDetailLayout;
    @BindView(R.id.cut_money_detail_lay)
    LinearLayout cutMoneyDetailLayout;

    private String mTripId = "";
    private int mActionFlag = BACK_TICKET;
    private long mChangeTime = 0;
    private List<PlaneTgqReasonInfoBean> mPlaneTgqReasonList = null;
    private List<PlaneOrderDetailInfoBean.PassengerInfo> passengerInfoList = null;

    /*改签信息*/
    private LinearLayout new_ticket_lay = null;
    private String mDate, mStartCity, mEndCity;
    private PlaneFlightChangeInfoBean mFlightChangeInfo = null;

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
            if (bundle.getString("tripId") != null) {
                mTripId = bundle.getString("tripId");
            }
            mActionFlag = bundle.getInt("plane_action");
        }
    }

    @Override
    protected void requestData() {
        super.requestData();
        if (StringUtil.notEmpty(mTripId)) {
            if (BACK_TICKET == mActionFlag) {
                requestTicketBackQuery();
            } else if (CHANGE_TICKET == mActionFlag) {
                PlaneOrderManager.instance.setFlightType(PlaneCommDef.FLIGHT_SINGLE);
                Intent intent = new Intent(this, PlaneCalendarChooseActivity.class);
                startActivityForResult(intent, 0x01);
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

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.i(TAG, "onResume()");
    }

    private void refreshScreenInfoAboutTicket(String jsonStr) {
        if (StringUtil.notEmpty(jsonStr)) {
            JSONObject mJson = JSON.parseObject(jsonStr);
            printJsonByKey(jsonStr);
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
                                if (BACK_TICKET == mActionFlag) {
                                    refreshMoneySimpleLayout();
                                    refreshMoneyDetailLayout();
                                } else if (CHANGE_TICKET == mActionFlag) {

                                }
                            }
                        });
                    }
                } else {
                    return;
                }
            }
            //第二步：更新原因信息
            if (mJson.containsKey("tgqReasonList")) {
                mPlaneTgqReasonList = JSON.parseArray(mJson.getString("tgqReasonList"), PlaneTgqReasonInfoBean.class);
                if (mPlaneTgqReasonList != null && mPlaneTgqReasonList.size() > 0) {
                    mReasonStr = new String[mPlaneTgqReasonList.size()];
                    for (int i = 0; i < mPlaneTgqReasonList.size(); i++) {
                        mReasonStr[i] = mPlaneTgqReasonList.get(i).msg;
                    }
                } else {
                    return;
                }
                tvBackReason.setText(mReasonStr[mReasonSelectedIndex]);
            }

            //第三步：更新退票凭证或当前ticket信息
            if (BACK_TICKET == mActionFlag) {
                tvThirdTitleName.setText("提交凭证");
                if (mPlaneTgqReasonList != null && mPlaneTgqReasonList.size() > 0) {
                    updateThirdStepLayoutForBack(mPlaneTgqReasonList.get(mReasonSelectedIndex));
                }

                //最后：预估退款金额
                backPriceLayout.setVisibility(View.VISIBLE);
                refreshMoneySimpleLayout();
                refreshMoneyDetailLayout();

            } else if (CHANGE_TICKET == mActionFlag) {
                tvThirdTitleName.setText("选择新航班");
                if (mJson.containsKey("trip")) {
                    updateThirdStepLayoutForChange(mJson.getString("trip"));
                }

                //最后：不需要显示预估退款金额
                backPriceLayout.setVisibility(View.GONE);
            }

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

    private void updateThirdStepLayoutForBack(PlaneTgqReasonInfoBean tgqReasonInfoBean) {
        if (null != tgqReasonInfoBean) {
            if (StringUtil.notEmpty(tgqReasonInfoBean.note)) {
                thirdStepLayout.setVisibility(View.VISIBLE);
                tvBackReasonDetail.setText(tgqReasonInfoBean.note);
            } else {
                thirdStepLayout.setVisibility(View.GONE);
            }
        }
    }

    private void updateThirdStepLayoutForChange(String jsonStr) {
        if (StringUtil.notEmpty(jsonStr)) {
            final PlaneOrderDetailInfoBean.TripInfo tripInfo = JSON.parseObject(jsonStr, PlaneOrderDetailInfoBean.TripInfo.class);
            if (passengerInfoList != null && passengerInfoList.size() > 0) {
                thirdContentLayout.removeAllViews();
                View ticketLayout = LayoutInflater.from(this).inflate(R.layout.layout_ticket_change_layout, null);
                thirdContentLayout.addView(ticketLayout);
                LinearLayout currentTicketLayout = (LinearLayout) ticketLayout.findViewById(R.id.current_ticket_lay);
                ((TextView) currentTicketLayout.findViewById(R.id.tv_ticket_flag)).setText("原航班信息：");
                ((TextView) currentTicketLayout.findViewById(R.id.tv_ticket_price)).setText(String.format(getString(R.string.rmbStr2), passengerInfoList.get(0).barePrice));
                mStartCity = tripInfo.scity;
                mEndCity = tripInfo.ecity;
                ((TextView) currentTicketLayout.findViewById(R.id.tv_ticket_city)).setText(String.format("%1$s → %2$s", mStartCity, mEndCity));
                mDate = DateUtil.getDay("MM-dd", tripInfo.sDate) + " " + DateUtil.dateToWeek2(new Date(tripInfo.sDate));
                ((TextView) currentTicketLayout.findViewById(R.id.tv_ticket_date)).setText(mDate + " " + tripInfo.sTime);
                ((TextView) currentTicketLayout.findViewById(R.id.tv_ticket_cabin)).setText(tripInfo.airCabin);
                ((TextView) currentTicketLayout.findViewById(R.id.tv_ticket_airport)).setText(String.format("%1$s%2$s - %3$s%4$s", tripInfo.sAirport, tripInfo.sTerminal, tripInfo.eAirport, tripInfo.eTerminal));

                new_ticket_lay = (LinearLayout) ticketLayout.findViewById(R.id.new_ticket_layout);
                TextView tv_choose_new = (TextView) ticketLayout.findViewById(R.id.tv_choose_new);
                tv_choose_new.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (StringUtil.isEmpty(mPlaneTgqReasonList.get(mReasonSelectedIndex).changeFlightSegmentList)) {
                            CustomToast.show("没有可改签航班", CustomToast.LENGTH_SHORT);
                            return;
                        }
                        LogUtil.i("changeFlightSegmentList = " + mPlaneTgqReasonList.get(mReasonSelectedIndex).changeFlightSegmentList);
                        Intent intent = new Intent(PlaneBackChangeTicketActivity.this, PlaneFlightChangeListActivity.class);
                        intent.putExtra("flights", mPlaneTgqReasonList.get(mReasonSelectedIndex).changeFlightSegmentList);
                        startActivityForResult(intent, 0x02);
                    }
                });
            }
        }

    }

    private void requestTicketBackQuery() {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("tripId", mTripId);
        ResponseData d = b.syncRequest(b);
        d.flag = AppConst.PLANE_BACK_QUERY;
        d.path = NetURL.PLANE_BACK_QUERY;
        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void requestTicketBackApply() {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("code", String.valueOf(mPlaneTgqReasonList.get(mReasonSelectedIndex).code));
        b.addBody("passengerIds", selectedPassengerIds);
        b.addBody("tripId", mTripId);
        ResponseData d = b.syncRequest(b);
        d.flag = AppConst.PLANE_BACK;
        d.path = NetURL.PLANE_BACK;
        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void requestTicketChangeQuery(long changeTime) {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("changeDate", DateUtil.getDay("yyyy-MM-dd", changeTime));
        b.addBody("tripId", mTripId);
        ResponseData d = b.syncRequest(b);
        d.flag = AppConst.PLANE_CHANGE_QUERY;
        d.path = NetURL.PLANE_CHANGE_QUERY;
        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void requestTicketChangeApple() {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("passengerIds", selectedPassengerIds);
        b.addBody("tripId", mTripId);
        b.addBody("changeCauseId", String.valueOf(mPlaneTgqReasonList.get(mReasonSelectedIndex).code));
        b.addBody("flightSegment", JSON.toJSONString(mFlightChangeInfo));
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
            if (request.flag == AppConst.PLANE_BACK_QUERY || request.flag == AppConst.PLANE_CHANGE_QUERY) {
                removeProgressDialog();
                LogUtil.i("json = " + response.body.toString());
                refreshScreenInfoAboutTicket(response.body.toString());
            } else if (request.flag == AppConst.PLANE_BACK) {
                removeProgressDialog();
                LogUtil.i("json = " + response.body.toString());
                JSONObject mJson = JSON.parseObject(response.body.toString());
                boolean isSuccess = mJson.containsKey("SUCCESS") ? mJson.getBoolean("SUCCESS") : false;
                boolean isNeedLoad = mJson.containsKey("NEEDUPLOAD") ? mJson.getBoolean("NEEDUPLOAD") : false;
                if (isSuccess) {
                    if (isNeedLoad) {
                        CustomToast.show("尼玛，二货，要上传资料哟！", CustomToast.LENGTH_LONG);
                    } else {
                        CustomToast.show("退票申请成功", CustomToast.LENGTH_LONG);
                        setResult(RESULT_OK);
                        finish();
                    }
                } else {
                    CustomToast.show("退票申请失败", CustomToast.LENGTH_LONG);
                }
            } else if (request.flag == AppConst.PLANE_CHANGE) {
                removeProgressDialog();
                LogUtil.i("json = " + response.body.toString());
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
                            if (BACK_TICKET == mActionFlag) {
                                tvThirdTitleName.setText("提交凭证");
                                if (mPlaneTgqReasonList != null && mPlaneTgqReasonList.size() > 0) {
                                    updateThirdStepLayoutForBack(mPlaneTgqReasonList.get(mReasonSelectedIndex));
                                }
                                refreshMoneySimpleLayout();
                                refreshMoneyDetailLayout();
                            } else if (CHANGE_TICKET == mActionFlag) {
                                tvThirdTitleName.setText("选择新航班");
                                new_ticket_lay.removeAllViews();
                            }
                        }
                    }
                });
                reasonDialog.setCanceledOnTouchOutside(true);
                reasonDialog.show();
                break;
            case R.id.tv_back_submit:
                if (StringUtil.isEmpty(selectedPassengerIds)) {
                    CustomToast.show("请选择联系人", CustomToast.LENGTH_SHORT);
                    return;
                }
                if (BACK_TICKET == mActionFlag) {
                    requestTicketBackApply();
                } else if (CHANGE_TICKET == mActionFlag) {
                    requestTicketChangeApple();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.i(TAG, "onActivityResult()");
        if (RESULT_OK != resultCode) {
            if (CHANGE_TICKET == mActionFlag && mChangeTime == 0) {
                finish();
            }
            return;
        }

        if (requestCode == 0x01) {
            if (null != data) {
                mChangeTime = data.getLongExtra("beginTime", 0);
                LogUtil.i(TAG, "onActivityResult() mChangeTime = " + mChangeTime);
                requestTicketChangeQuery(mChangeTime);
            }
        } else if (requestCode == 0x02) {
            if (null != data) {
                mFlightChangeInfo = (PlaneFlightChangeInfoBean) data.getSerializableExtra("flightInfo");
                if (null != mFlightChangeInfo) {
                    new_ticket_lay.removeAllViews();
                    View newTicketLayout = LayoutInflater.from(PlaneBackChangeTicketActivity.this).inflate(R.layout.layout_ticket_change_item, null);
                    new_ticket_lay.addView(newTicketLayout);
                    ((TextView) newTicketLayout.findViewById(R.id.tv_ticket_flag)).setText("改签航班信息：");
                    ((TextView) newTicketLayout.findViewById(R.id.tv_ticket_price)).setText(String.format(getString(R.string.rmbStr2), Integer.valueOf(mFlightChangeInfo.allFee)));
                    ((TextView) newTicketLayout.findViewById(R.id.tv_ticket_city)).setText(String.format("%1$s → %2$s", mStartCity, mEndCity));
                    ((TextView) newTicketLayout.findViewById(R.id.tv_ticket_date)).setText(mDate + " " + mFlightChangeInfo.startTime);
                    ((TextView) newTicketLayout.findViewById(R.id.tv_ticket_cabin)).setText(mFlightChangeInfo.cabin);
                    ((TextView) newTicketLayout.findViewById(R.id.tv_ticket_airport)).setText(String.format("%1$s%2$s - %3$s%4$s", mFlightChangeInfo.startPlace, mFlightChangeInfo.dptTerminal, mFlightChangeInfo.endPlace, mFlightChangeInfo.arrTerminal));
                }
            }
        }
    }
}
