package com.huicheng.hotel.android.ui.activity.hotel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.CouponInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.InvoiceDetailInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.OrderDetailInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.RoomConfirmInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.RoomDetailInfoBean;
import com.huicheng.hotel.android.ui.activity.UcCouponsActivity;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.CommonAddSubLayout;
import com.huicheng.hotel.android.ui.custom.CustomInfoLayoutForHotel;
import com.huicheng.hotel.android.ui.custom.RoundedAllImageView;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.huicheng.hotel.android.ui.glide.CustomReqURLFormatModelImpl;
import com.prj.sdk.app.AppConst;
import com.prj.sdk.app.NetURL;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
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
    private ImageView iv_coupon_next;
    private ImageView iv_coupon_del;
    private LinearLayout bounty_lay;
    private TextView tv_bounty_info;
    private Switch switch_bounty;

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
    private CustomInfoLayoutForHotel custom_lay;
    private boolean isYgr = false;

    private int mBountyPrice = 0;
    private int mYhqPrice = 0;
    private boolean isCanUseYhq = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_hotelroomorder_layout);
        initViews();
        initParams();
        initListeners();
        if (null == savedInstanceState) {
            if (isCanUseYhq) {
                requestCheckValidCoupon();
            }
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
        tv_coupon_info = (TextView) findViewById(R.id.tv_coupon_info);
        tv_submit = (TextView) findViewById(R.id.tv_submit);
        tv_submit.getPaint().setFakeBoldText(true);
        custom_lay = (CustomInfoLayoutForHotel) findViewById(R.id.custom_lay);
        bounty_lay = (LinearLayout) findViewById(R.id.bounty_lay);
        tv_bounty_info = (TextView) findViewById(R.id.tv_bounty_info);
        switch_bounty = (Switch) findViewById(R.id.switch_bounty);
        iv_coupon_next = (ImageView) findViewById(R.id.iv_coupon_next);
        iv_coupon_del = (ImageView) findViewById(R.id.iv_coupon_del);
        iv_coupon_del.setEnabled(false);
        iv_coupon_del.setAlpha(0.5f);
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
        room_addsub_lay.setMinvalue(1);
        finalPrice = calculatePayPrice(room_addsub_lay.getCount());
        if (isHhy || null != HotelOrderManager.getInstance().getCouponInfoBean()) {
            room_addsub_lay.setButtonEnable(false);
        }

        String jsonStr = SharedPreferenceUtil.getInstance().getString(AppConst.IN_PERSON_INFO, "", true);
        if (StringUtil.isEmpty(jsonStr)) {
            ((EditText) custom_lay.getChildAt(0).findViewById(R.id.et_phone)).setText(SessionContext.mUser.user.mobile);
        } else {
            int person = custom_lay.setPersonInfos(jsonStr);
            LogUtil.i(TAG, "customLayout person = " + person);
        }

        isCanUseYhq = HotelOrderManager.getInstance().getPayType().equals(HotelCommDef.PAY_PRE);
        if (isCanUseYhq) {
            coupon_lay.setVisibility(View.VISIBLE);
        } else {
            coupon_lay.setVisibility(View.GONE);
        }

        if (HotelCommDef.PAY_ARR.equals(HotelOrderManager.getInstance().getPayType())) {
            tv_submit.setText(getString(R.string.order_submit));
        } else {
            tv_submit.setText(getString(R.string.order_topay));
        }
    }

    private void requestCheckValidCoupon() {
        LogUtil.i(TAG, "requestCheckValidCoupon()");
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("beginDate", String.valueOf(HotelOrderManager.getInstance().getBeginTime()));
        b.addBody("endDate", String.valueOf(HotelOrderManager.getInstance().getEndTime()));
        b.addBody("hotelid", String.valueOf(hotelId));
        b.addBody("money", String.valueOf(finalPrice));
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
        b.addBody("useBounty", String.valueOf(switch_bounty.isChecked()));
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
        room_addsub_lay.setOnCountChangedListener(new CommonAddSubLayout.OnCountChangedListener() {
            @Override
            public void onCountChanged(int count) {
                finalPrice = calculatePayPrice(count);
                if (isCanUseYhq) {
                    requestCheckValidCoupon();
                }
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

        switch_bounty.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                finalPrice = calculatePayPrice(room_addsub_lay.getCount());
            }
        });
        tv_coupon_info.setOnClickListener(this);
        iv_coupon_next.setOnClickListener(this);
        iv_coupon_del.setOnClickListener(this);
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
            case R.id.iv_coupon_next:
            case R.id.tv_coupon_info:
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
            case R.id.iv_coupon_del:
                iv_coupon_del.setEnabled(false);
                iv_coupon_del.setAlpha(0.5f);
                couponInfo = null;
                tv_coupon_info.setText("");
                mYhqPrice = 0;
                finalPrice = calculatePayPrice(room_addsub_lay.getCount());
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
                        tv_invoice_info.setText(getString(R.string.order_need_invoice));
                    } else {
                        invoiceDetailInfoBean = new InvoiceDetailInfoBean();
                        tv_invoice_info.setText(getString(R.string.order_need_not_invoice));
                        isInvoice = false;
                    }
                } else {
                    invoiceDetailInfoBean = new InvoiceDetailInfoBean();
                    tv_invoice_info.setText(getString(R.string.order_need_not_invoice));
                }
                LogUtil.i(TAG, new Gson().toJson(invoiceDetailInfoBean));
                if (invoiceDetailInfoBean != null) {
                    LogUtil.i(TAG, invoiceDetailInfoBean.toString());
                }
            }
        } else if (requestCode == 0x02) {
            if (null != data) {
                mYhqPrice = 0;
                if (data.getExtras().get("coupon") != null) {
                    couponInfo = (CouponInfoBean.CouponInfo) data.getExtras().get("coupon");
                    if (null != couponInfo) {
                        iv_coupon_del.setEnabled(true);
                        iv_coupon_del.setAlpha(1.0f);
                        tv_coupon_info.setText(couponInfo.name);
                        LogUtil.i(TAG, "couponInfo.name = " + couponInfo.name + "\ncouponInfo.id = " + couponInfo.id + "\ncouponInfo.couponvalue = " + couponInfo.couponvalue);
                        mYhqPrice = couponInfo.couponvalue / 100;
                    }
                }
                finalPrice = calculatePayPrice(room_addsub_lay.getCount());
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
                        Intent intent = new Intent(this, HotelOrderPaySuccessActivity.class);
                        intent.putExtra("hotelId", String.valueOf(hotelId));
                        intent.putExtra("roomName", bean.roomName);
                        intent.putExtra("checkRoomDate", bean.checkInAndOutDate);
                        intent.putExtra("beginTime", bean.beginDateLong);
                        intent.putExtra("endTime", bean.endDateLong);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(this, HotelOrderPayActivity.class);
                        intent.putExtra("orderId", bean.orderId);
                        intent.putExtra("orderType", bean.orderType);
                        startActivity(intent);
                    }
                }
            } else if (request.flag == AppConst.COUPON_USEFUL_CHECK) {
                removeProgressDialog();
                LogUtil.i(TAG, "json = " + response.body.toString());
                if (StringUtil.notEmpty(response.body.toString())) {
                    JSONObject jsonObject = JSON.parseObject(response.body.toString());
                    if (jsonObject.containsKey("couponstatus")) {
                        refreshCouponStatus(jsonObject.getBoolean("couponstatus"));
                    }
                    if (jsonObject.containsKey("bounty") && jsonObject.getIntValue("bounty") > 0) {
                        bounty_lay.setVisibility(View.VISIBLE);
                        mBountyPrice = jsonObject.getIntValue("bounty");
                        tv_bounty_info.setText(String.format(getString(R.string.bountyStr), mBountyPrice));
                        finalPrice = calculatePayPrice(room_addsub_lay.getCount());
                    } else {
                        mBountyPrice = 0;
                        bounty_lay.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    private void refreshCouponStatus(boolean hasValidCoupon) {
        if (hasValidCoupon) {
            coupon_lay.setVisibility(View.VISIBLE);
        } else {
            couponInfo = null;
            tv_coupon_info.setText("");
            coupon_lay.setVisibility(View.GONE);
        }
    }

    private int calculatePayPrice(int count) {
        int totalPrice = roomPrice * count + allChooseServicePrice;
        int yhqPrice = mYhqPrice;
        int bounty = switch_bounty.isChecked() ? mBountyPrice : 0;
        int ret = (totalPrice <= yhqPrice) ? 0 : (((totalPrice - yhqPrice) <= bounty) ? 0 : (totalPrice - yhqPrice) - bounty);
        tv_final_price.setText(ret + "元");
        return ret;
    }
}
