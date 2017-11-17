package com.huicheng.hotel.android.ui.activity.hotel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.net.bean.CouponInfoBean;
import com.huicheng.hotel.android.net.bean.InvoiceDetailInfoBean;
import com.huicheng.hotel.android.net.bean.OrderDetailInfoBean;
import com.huicheng.hotel.android.net.bean.RoomConfirmInfoBean;
import com.huicheng.hotel.android.net.bean.RoomDetailInfoBean;
import com.huicheng.hotel.android.ui.activity.OrderPayActivity;
import com.huicheng.hotel.android.ui.activity.OrderPaySuccessActivity;
import com.huicheng.hotel.android.ui.activity.UcCouponsActivity;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.CommonAddSubLayout;
import com.huicheng.hotel.android.ui.custom.CommonCustomInfoLayout;
import com.huicheng.hotel.android.ui.custom.RoundedAllImageView;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.huicheng.hotel.android.ui.glide.CustomReqURLFormatModelImpl;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kborid
 * @date 2017/1/5 0005
 */
public class HotelRoomOrderActivity extends BaseActivity {

    private RoomDetailInfoBean roomDetailInfoBean = null;
    //    private List<RoomConfirmInfoBean> roomConfirmList = new ArrayList<>();
    private Map<String, RoomConfirmInfoBean> roomServiceMap = new HashMap<>();
    private RadioGroup rg_arrived;
    private int arrivedValue = 1;
    private EditText et_content;
    private RoundedAllImageView iv_room_pic;
    private TextView tv_date, tv_total_price, tv_final_price;
    private LinearLayout choose_service_lay;
    private CommonAddSubLayout room_addsub_lay;

    private LinearLayout invoice_lay;
    private TextView tv_invoice_info;
    private LinearLayout coupon_lay;
    private TextView tv_coupon_info;

    private TextView tv_submit;
    private String mPicUrl, roomName;
    private boolean isHhy = false;
    private int roomPrice = 0;
    private int allChooseServicePrice = 0;
    private int finalPrice = 0;
    private boolean isInvoice = false;
    private InvoiceDetailInfoBean invoiceDetailInfoBean = new InvoiceDetailInfoBean();
    private CouponInfoBean.CouponInfo couponInfo = null;
    private int hotelId, roomId = -1;
    private StringBuilder serviceIds = new StringBuilder();
    private StringBuilder serviceCounts = new StringBuilder();
    private CommonCustomInfoLayout custom_lay;
    private boolean isYgr = false;

    private boolean hasValidCoupon = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_hotelroomorder_layout);
        initViews();
        initParams();
        initListeners();
        if (null == savedInstanceState) {
            requestCheckValidCoupon();
        }
    }

    @Override
    public void initViews() {
        super.initViews();
        rg_arrived = (RadioGroup) findViewById(R.id.rg_arrived);
        et_content = (EditText) findViewById(R.id.et_content);
        iv_room_pic = (RoundedAllImageView) findViewById(R.id.iv_room_pic);
        tv_date = (TextView) findViewById(R.id.tv_date);
        tv_total_price = (TextView) findViewById(R.id.tv_total_price);
        tv_total_price.getPaint().setFakeBoldText(true);
        tv_final_price = (TextView) findViewById(R.id.tv_final_price);
        tv_final_price.getPaint().setFakeBoldText(true);
        choose_service_lay = (LinearLayout) findViewById(R.id.choose_service_lay);
        room_addsub_lay = (CommonAddSubLayout) findViewById(R.id.room_addsub_lay);
        invoice_lay = (LinearLayout) findViewById(R.id.invoice_lay);
        tv_invoice_info = (TextView) findViewById(R.id.tv_invoice_info);
        coupon_lay = (LinearLayout) findViewById(R.id.coupon_lay);
        coupon_lay.setVisibility(View.GONE);
        tv_coupon_info = (TextView) findViewById(R.id.tv_coupon_info);
        tv_submit = (TextView) findViewById(R.id.tv_submit);
        tv_submit.getPaint().setFakeBoldText(true);
        custom_lay = (CommonCustomInfoLayout) findViewById(R.id.custom_lay);
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            isHhy = bundle.getBoolean("hhy");
            LogUtil.i(TAG, "isHhy = " + isHhy);
            roomPrice = bundle.getInt("roomPrice");
            LogUtil.i(TAG, "roomPrice = " + roomPrice);
            allChooseServicePrice = bundle.getInt("allChooseServicePrice");
            LogUtil.i(TAG, "allChooseServicePrice = " + allChooseServicePrice);
            if (bundle.getSerializable("roomDetailInfo") != null) {
                roomDetailInfoBean = (RoomDetailInfoBean) bundle.getSerializable("roomDetailInfo");
            }
            if (bundle.getString("picUrl") != null && bundle.getString("name") != null) {
                mPicUrl = bundle.getString("picUrl");
                roomName = bundle.getString("name");
            }
            roomId = bundle.getInt("roomId");
            hotelId = bundle.getInt("hotelId");
            if (bundle.getBoolean("isYgr")) {
                isYgr = bundle.getBoolean("isYgr");
            }
        }
        if (getIntent().getSerializableExtra("chooseServiceInfo") != null) {
            roomServiceMap = (Map<String, RoomConfirmInfoBean>) getIntent().getSerializableExtra("chooseServiceInfo");
            if (roomServiceMap != null && roomServiceMap.size() > 0) {
                for (String key : roomServiceMap.keySet()) {
                    LogUtil.i(TAG, "ServiceTitle:" + roomServiceMap.get(key).serviceTitle + ", " + getString(R.string.multipleSign) + roomServiceMap.get(key).serviceCount);
                    serviceCounts.append(roomServiceMap.get(key).serviceCount).append("|");
                    serviceIds.append(roomServiceMap.get(key).serviceId).append("|");
                }
            }
        }
    }

    @Override
    public void initParams() {
        super.initParams();

        String picUrl = mPicUrl;
        if (roomDetailInfoBean != null) {
            tv_center_title.setText(roomDetailInfoBean.roomName);
            if (roomDetailInfoBean.picList != null && roomDetailInfoBean.picList.size() > 0) {
                picUrl = roomDetailInfoBean.picList.get(0);
            }
        } else {
            tv_center_title.setText(roomName);
        }
        Glide.with(this)
                .load(new CustomReqURLFormatModelImpl(picUrl))
                .placeholder(R.drawable.def_order_confirm)
                .crossFade()
                .centerCrop()
                .override(200, 200)
                .into(iv_room_pic);

        String date = DateUtil.getDay("MM月dd日", HotelOrderManager.getInstance().getBeginTime(isYgr)) + "-" + DateUtil.getDay("dd日", HotelOrderManager.getInstance().getEndTime(isYgr));
        String during = DateUtil.getGapCount(HotelOrderManager.getInstance().getBeginDate(isYgr), HotelOrderManager.getInstance().getEndDate(isYgr)) + "晚";
        tv_date.setText(date + " " + during);
        tv_total_price.setText(roomPrice + allChooseServicePrice + "元");
        choose_service_lay.removeAllViews();
        for (String key : roomServiceMap.keySet()) {
            if (roomServiceMap.get(key).serviceCount > 0) {
                TextView tv_service = new TextView(this);
                tv_service.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
                tv_service.setTextSize(15f);
                tv_service.setTextColor(getResources().getColor(R.color.lableColor));
                tv_service.setText(roomServiceMap.get(key).serviceTitle + " " + getString(R.string.multipleSign) + " " + roomServiceMap.get(key).serviceCount);
                choose_service_lay.addView(tv_service);
            }
        }

        room_addsub_lay.setUnit("间");
        room_addsub_lay.setCount(1);
        finalPrice = roomPrice + allChooseServicePrice;
        tv_final_price.setText(finalPrice + "元");
        if (isHhy || null != HotelOrderManager.getInstance().getCouponInfoBean()) {
            room_addsub_lay.setButtonEnable(false);
        }

        String jsonStr = SharedPreferenceUtil.getInstance().getString(AppConst.IN_PERSON_INFO, "", true);
        if (StringUtil.isEmpty(jsonStr)) {
            ((EditText) custom_lay.getChildAt(0).findViewById(R.id.et_phone)).setText(SessionContext.mUser.user.mobile);
        } else {
            int person = custom_lay.setPersonInfos(jsonStr);
        }

        if (HotelCommDef.PAY_ARR.equals(HotelOrderManager.getInstance().getPayType())) {
            tv_submit.setText("提交订单");
        } else {
            tv_submit.setText("去支付");
        }
    }

    private void requestCheckValidCoupon() {
        LogUtil.i(TAG, "requestCheckValidCoupon()");
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("beginDate", String.valueOf(HotelOrderManager.getInstance().getBeginTime()));
        b.addBody("endDate", String.valueOf(HotelOrderManager.getInstance().getEndTime()));
        b.addBody("hotelid", String.valueOf(hotelId));
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.COUPON_USEFUL_CHECK;
        d.flag = AppConst.COUPON_USEFUL_CHECK;
        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void requestAddOrderDetail() {
        LogUtil.i(TAG, "requestAddOrderDetail() arrivedValue = " + arrivedValue);
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("hotelId", String.valueOf(hotelId));
        b.addBody("roomCount", String.valueOf(room_addsub_lay.getCount()));
        b.addBody("roomId", String.valueOf(roomId));
        b.addBody("needInvoice", String.valueOf(isInvoice));
        b.addBody("beginDate", String.valueOf(HotelOrderManager.getInstance().getBeginTime(isYgr)));
        b.addBody("endDate", String.valueOf(HotelOrderManager.getInstance().getEndTime(isYgr)));
        b.addBody("userId", SessionContext.mUser.user.userid);
        b.addBody("invoice", invoiceDetailInfoBean);
        b.addBody("arrivalTag", String.valueOf(arrivedValue));
        if (coupon_lay.isShown() && couponInfo != null) {
            b.addBody("couponid", String.valueOf(couponInfo.id));
        }
        b.addBody("specialComment", et_content.getText().toString());

        b.addBody("useMobiles", custom_lay.getCustomUserPhones());
        b.addBody("useNames", custom_lay.getCustomUserNames());

        b.addBody("serviceIds", serviceIds.toString());
        b.addBody("serviceCnts", serviceCounts.toString());
        b.addBody("type", String.valueOf(HotelOrderManager.getInstance().getHotelType())); //酒店type
        b.addBody("paytab", HotelOrderManager.getInstance().getPayType()); //支付type

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.ROOM_CONFIRM_ORDER;
        d.flag = AppConst.ROOM_CONFIRM_ORDER;

        if (!isProgressShowing()) {
            showProgressDialog(this);
        }

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    @Override
    public void initListeners() {
        super.initListeners();
        invoice_lay.setOnClickListener(this);
        coupon_lay.setOnClickListener(this);
        room_addsub_lay.setOnCountChangedListener(new CommonAddSubLayout.OnCountChangedListener() {
            @Override
            public void onCountChanged(int count) {
                finalPrice = roomPrice * count + allChooseServicePrice;
                tv_final_price.setText(finalPrice + "元");
                refreshCouponStatus();
            }
        });
        rg_arrived.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.btn_rb1:
                        arrivedValue = 1;
                        break;
                    case R.id.btn_rb2:
                        arrivedValue = 2;
                        break;
                    case R.id.btn_rb3:
                        arrivedValue = 3;
                        break;
                    case R.id.btn_rb4:
                        arrivedValue = 4;
                        break;
                }
            }
        });
        tv_submit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.invoice_lay:
                Intent intent = new Intent(this, HotelInvoiceActivity.class);
                intent.putExtra("isInvoice", isInvoice);
                intent.putExtra("InvoiceDetail", invoiceDetailInfoBean);
                startActivityForResult(intent, 0x01);
                break;
            case R.id.coupon_lay:
                Intent intent1 = new Intent(this, UcCouponsActivity.class);
                intent1.putExtra("showUsefulCoupon", true);
                startActivityForResult(intent1, 0x02);
                break;
            case R.id.tv_submit:
                if (custom_lay.isEditViewEmpty()) {
                    CustomToast.show("请填写入住人信息", CustomToast.LENGTH_SHORT);
                    return;
                }
                if (custom_lay.isValidPhoneNumber()) {
                    CustomToast.show("请填写正确的手机号码", CustomToast.LENGTH_SHORT);
                    return;
                }
                LogUtil.i(TAG, "nameStr = " + custom_lay.getCustomUserNames());
                LogUtil.i(TAG, "phoneStr = " + custom_lay.getCustomUserPhones());
                LogUtil.i(TAG, "custom json string = " + custom_lay.getCustomInfoJsonString());
                SharedPreferenceUtil.getInstance().setString(AppConst.IN_PERSON_INFO, custom_lay.getCustomInfoJsonString(), true);
                requestAddOrderDetail();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        invoiceDetailInfoBean = null;
        roomServiceMap.clear();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Activity.RESULT_OK != resultCode) {
            return;
        }

        if (requestCode == 0x01) {
            if (null != data) {
                isInvoice = data.getExtras().getBoolean("isInvoice");
                if (isInvoice) {
                    if (data.getExtras().get("InvoiceDetail") != null) {
                        invoiceDetailInfoBean = (InvoiceDetailInfoBean) data.getExtras().get("InvoiceDetail");
                        tv_invoice_info.setText("需要发票");
                    } else {
                        invoiceDetailInfoBean = new InvoiceDetailInfoBean();
                        tv_invoice_info.setText("不需要发票");
                        isInvoice = false;
                    }
                } else {
                    invoiceDetailInfoBean = new InvoiceDetailInfoBean();
                    tv_invoice_info.setText("不需要发票");
                }
                LogUtil.i(TAG, new Gson().toJson(invoiceDetailInfoBean));
                if (invoiceDetailInfoBean != null) {
                    LogUtil.i(TAG, invoiceDetailInfoBean.toString());
                }
            }
        } else if (requestCode == 0x02) {
            if (null != data) {
                if (data.getExtras().get("coupon") != null) {
                    couponInfo = (CouponInfoBean.CouponInfo) data.getExtras().get("coupon");
                    if (null != couponInfo) {
                        tv_coupon_info.setText(couponInfo.name);
                    }
                }
            }
        }
    }

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.ROOM_CONFIRM_ORDER) {
                removeProgressDialog();
                LogUtil.i(TAG, "json = " + response.body.toString());
                OrderDetailInfoBean bean = JSON.parseObject(response.body.toString(), OrderDetailInfoBean.class);
                if (null != bean) {
                    if (HotelCommDef.PAY_ARR.equals(HotelOrderManager.getInstance().getPayType())) {
                        Intent intent = new Intent(this, OrderPaySuccessActivity.class);
                        intent.putExtra("hotelId", String.valueOf(hotelId));
                        intent.putExtra("roomName", bean.roomName);
                        intent.putExtra("checkRoomDate", bean.checkInAndOutDate);
                        intent.putExtra("beginTime", bean.beginDateLong);
                        intent.putExtra("endTime", bean.endDateLong);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(this, OrderPayActivity.class);
                        intent.putExtra("orderId", bean.orderId);
                        intent.putExtra("orderType", bean.orderType);
                        startActivity(intent);
                    }
                }
            } else if (request.flag == AppConst.COUPON_USEFUL_CHECK) {
                removeProgressDialog();
                LogUtil.i(TAG, "json = " + response.body.toString());
                hasValidCoupon = (Boolean) response.body;
                refreshCouponStatus();
            }
        }
    }

    private void refreshCouponStatus() {
        if (!hasValidCoupon) {
            return;
        }
        if (finalPrice >= 500
                && HotelCommDef.PAY_PRE.equals(HotelOrderManager.getInstance().getPayType())) {
            coupon_lay.setVisibility(View.VISIBLE);
        } else {
            couponInfo = null;
            tv_coupon_info.setText("");
            coupon_lay.setVisibility(View.GONE);
        }
    }
}
