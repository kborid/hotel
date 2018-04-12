package com.huicheng.hotel.android.ui.activity.plane;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.IdRes;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.PlaneCommDef;
import com.huicheng.hotel.android.common.PlaneOrderManager;
import com.huicheng.hotel.android.common.RequestCodeDef;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.content.AppConst;
import com.huicheng.hotel.android.content.NetURL;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.AddressInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.AirCompanyInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneBookingInfo;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneBookingInfo_FlightInfo;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneBookingInfo_PriceInfo;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneFlightInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneInvoiceTaxInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneTicketInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneVendorInfoBean;
import com.huicheng.hotel.android.ui.activity.HtmlActivity;
import com.huicheng.hotel.android.ui.activity.UcAboutActivity;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.huicheng.hotel.android.ui.custom.plane.CustomInfoLayoutForPlane;
import com.huicheng.hotel.android.ui.custom.plane.ICustomInfoLayoutPlaneCountListener;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.huicheng.hotel.android.ui.glide.CustomReqURLFormatModelImpl;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @auth kborid
 * @date 2017/11/22 0022.
 */

public class PlaneNewOrderActivity extends BaseAppActivity {

    private static final int TYPE_GO = 1;
    private static final int TYPE_BACK = 2;

    private FrameLayout root;
    //预订航班信息
    private LinearLayout flight_layout;
    private LinearLayout flight_flag_layout;
    private CustomDialog mFlightTicketDialog = null;
    //联系人信息
    private EditText et_contact, et_contactMob;
    //乘机人信息
    private CustomInfoLayoutForPlane custom_info_layout_plane;
    private ImageView iv_customInfo_add;

    private TextView tv_express_price;
    private Switch btn_invoice_switch;
    private LinearLayout invoice_lay;
    private TextView tv_invoice_tips;
    private TextView tv_invoice_type;
    private TableRow tr_invoice_header;
    private View line_invoice_header;
    private EditText et_receive_title, et_receive_number;
    private RadioGroup rg_invoice;
    private TextView tv_personal_invoice;
    private LinearLayout company_invoice;

    private CheckBox cb_accident;
    private TextView tv_accident_price;
    private CheckBox cb_delay;
    private TextView tv_delay_price;

    private TextView tv_express_addr, tv_express_name, tv_express_phone, tv_express_chooser;
    private TextView tv_plane_agreement1, tv_plane_agreement2;

    private TextView tv_amount;
    private TextView tv_passenger;
    private TextView tv_submit;
    private LinearLayout order_detail_layout;

    private int flightType = PlaneCommDef.FLIGHT_SINGLE;
    private PlaneBookingInfo goFlightBookingInfo = null;
    private PlaneBookingInfo backFlightBookingInfo = null;

    private List<PlaneBookingInfo> mBkInfo = new ArrayList<>();
    private PlaneInvoiceTaxInfoBean invoiceTaxInfoBean;
    private int invoiceType = PlaneCommDef.INVOICE_INVALID;
    private int receiverType = PlaneCommDef.RECEIVE_PERSONAL;

    private AddressInfoBean mBean = null;

    private int requestTagCount = 0;
    private HashMap<Integer, Integer> mTag = new HashMap<>();

    private int mAccidentPrice = 0; //意外险价格
    private int mDelayPrice = 0; //延误险价格
    private int mExpressPrice = 0; //邮费价格

    private int mTicketPrice = 0; //单人机票价格(包含基建和燃油费用)
    private int safeType = PlaneCommDef.SAFE_BUY_NON;
    private int mSafePrice = 0; //保险价格
    private int mPassengerCount = 0; //乘机人个数
    private int mAmount = 0; //最终支付价格


    @Override
    protected void setContentView() {
        setContentView(R.layout.act_plane_neworder_layout);
    }

    @Override
    protected void requestData() {
        super.requestData();
        mBkInfo.clear();
        requestTagCount = 0;
        mTag.clear();
        requestTicketBookingInfo(TYPE_GO);
        if (flightType == PlaneCommDef.FLIGHT_GOBACK) {
            requestTicketBookingInfo(TYPE_BACK);
        }
        requestDefaultAddressInfo();
    }

    @Override
    public void initViews() {
        super.initViews();
        root = (FrameLayout) findViewById(R.id.root);
        root.setLayoutAnimation(getAnimationController());
        flightType = PlaneOrderManager.instance.getFlightType();
        flight_layout = (LinearLayout) findViewById(R.id.flight_layout);
        flight_flag_layout = (LinearLayout) findViewById(R.id.flight_flag_layout);
        et_contact = (EditText) findViewById(R.id.et_contact);
        et_contactMob = (EditText) findViewById(R.id.et_contactMob);

        custom_info_layout_plane = (CustomInfoLayoutForPlane) findViewById(R.id.custom_info_layout_plane);
        iv_customInfo_add = (ImageView) findViewById(R.id.iv_customInfo_add);

        tv_express_price = (TextView) findViewById(R.id.tv_express_price);
        btn_invoice_switch = (Switch) findViewById(R.id.btn_invoice_switch);
        invoice_lay = (LinearLayout) findViewById(R.id.invoice_lay);
        tv_invoice_tips = (TextView) findViewById(R.id.tv_invoice_tips);
        tv_invoice_type = (TextView) findViewById(R.id.tv_invoice_type);
        tr_invoice_header = (TableRow) findViewById(R.id.tr_invoice_header);
        line_invoice_header = findViewById(R.id.line_invoice_header);
        rg_invoice = (RadioGroup) findViewById(R.id.rg_invoice);
        tv_personal_invoice = (TextView) findViewById(R.id.tv_personal_invoice);
        company_invoice = (LinearLayout) findViewById(R.id.company_invoice);
        et_receive_title = (EditText) findViewById(R.id.et_receive_title);
        et_receive_number = (EditText) findViewById(R.id.et_receive_number);

        cb_accident = (CheckBox) findViewById(R.id.cb_accident);
        tv_accident_price = (TextView) findViewById(R.id.tv_accident_price);
        cb_delay = (CheckBox) findViewById(R.id.cb_delay);
        tv_delay_price = (TextView) findViewById(R.id.tv_delay_price);

        tv_express_addr = (TextView) findViewById(R.id.tv_express_addr);
        tv_express_name = (TextView) findViewById(R.id.tv_express_name);
        tv_express_phone = (TextView) findViewById(R.id.tv_express_phone);
        tv_express_chooser = (TextView) findViewById(R.id.tv_express_chooser);

        tv_plane_agreement1 = (TextView) findViewById(R.id.tv_plane_agreement1);
        tv_plane_agreement2 = (TextView) findViewById(R.id.tv_plane_agreement2);

        tv_amount = (TextView) findViewById(R.id.tv_amount);
        tv_passenger = (TextView) findViewById(R.id.tv_passenger);
        tv_submit = (TextView) findViewById(R.id.tv_submit);

        order_detail_layout = (LinearLayout) findViewById(R.id.order_detail_layout);
    }

    @Override
    public void initParams() {
        super.initParams();
        findViewById(R.id.comm_title_rl).setBackgroundColor(getResources().getColor(R.color.white));
        tv_center_title.setText("创建订单");

        //initialize
        setAgreement1TextSpan(getString(R.string.plane_agreement1));
        setAgreement2TextSpan(getString(R.string.plane_agreement2));
        tv_express_price.setVisibility(View.GONE);
        btn_invoice_switch.setChecked(false);
        tv_invoice_tips.setVisibility(View.VISIBLE);
        tv_invoice_type.setVisibility(View.GONE);
        invoice_lay.setVisibility(View.GONE);
        invoiceTaxInfoBean = new PlaneInvoiceTaxInfoBean();

        //通过缓存，初始化联系人信息
        String contactCache = SharedPreferenceUtil.getInstance().getString(AppConst.PLANE_ORDER_CONTACT_INFO, "", true);
        if (StringUtil.notEmpty(contactCache)) {
            et_contact.setText(contactCache.split("\\|")[0]);
            et_contactMob.setText(contactCache.split("\\|")[1]);
        }

        //通过缓存，初始化乘机人信息
        String passengerJsonStr = SharedPreferenceUtil.getInstance().getString(AppConst.PLANE_ORDER_PASSENGERS_INFO, "", true);
        if (StringUtil.notEmpty(passengerJsonStr)) {
            mPassengerCount = custom_info_layout_plane.setPersonInfo(passengerJsonStr);
        } else {
            mPassengerCount = custom_info_layout_plane.getChildCount();
        }
    }

    @Override
    public void initListeners() {
        super.initListeners();
        custom_info_layout_plane.setICustomInfoLayoutPlaneCountListener(new ICustomInfoLayoutPlaneCountListener() {
            @Override
            public void onCountChanged(int count) {
                mPassengerCount = count;
                calculateAmount(mPassengerCount);
            }
        });
        iv_customInfo_add.setOnClickListener(this);
        flight_layout.setOnClickListener(this);
        cb_accident.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                calculateAmount(mPassengerCount);
            }
        });
        cb_delay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                calculateAmount(mPassengerCount);
            }
        });
        btn_invoice_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tv_invoice_tips.setVisibility(View.GONE);
                    invoice_lay.setVisibility(View.VISIBLE);
                } else {
                    tv_invoice_tips.setVisibility(View.VISIBLE);
                    invoice_lay.setVisibility(View.GONE);
                }
                calculateAmount(mPassengerCount);
            }
        });
        tv_express_chooser.setOnClickListener(this);
        tv_submit.setOnClickListener(this);
        order_detail_layout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_customInfo_add:
                if (null != custom_info_layout_plane) {
                    custom_info_layout_plane.addNewItem();
                }
                break;
            case R.id.flight_layout: {
                if (null == mFlightTicketDialog) {
                    mFlightTicketDialog = new CustomDialog(this);
                    mFlightTicketDialog.findViewById(R.id.content_layout).setPadding(Utils.dp2px(5), Utils.dp2px(5), Utils.dp2px(5), Utils.dp2px(5));
                    mFlightTicketDialog.setTitle("详情");
                    View view = LayoutInflater.from(this).inflate(R.layout.dialog_flightticket_detail, null);
                    mFlightTicketDialog.addView(view);

                    LinearLayout flight_layout = (LinearLayout) view.findViewById(R.id.flight_layout);
                    //刷新dialog信息
                    flight_layout.removeAllViews();
                    {
                        //去程信息
                        {
                            PlaneBookingInfo_FlightInfo goFlightInfo = goFlightBookingInfo.flightInfo.get(0);
                            PlaneBookingInfo_PriceInfo goPriceInfo = goFlightBookingInfo.priceInfo;

                            View goFlightView = LayoutInflater.from(this).inflate(R.layout.layout_plane_orderdetail_flightinfo_item, null);
                            flight_layout.addView(goFlightView);
                            TextView tv_flag = (TextView) goFlightView.findViewById(R.id.tv_flag);
                            TextView tv_date = (TextView) goFlightView.findViewById(R.id.tv_date);
                            TextView tv_price = (TextView) goFlightView.findViewById(R.id.tv_price);
                            TextView tv_off_time = (TextView) goFlightView.findViewById(R.id.tv_off_time);
                            TextView tv_on_time = (TextView) goFlightView.findViewById(R.id.tv_on_time);
                            TextView tv_during = (TextView) goFlightView.findViewById(R.id.tv_during);
                            TextView tv_off_airport = (TextView) goFlightView.findViewById(R.id.tv_off_airport);
                            TextView tv_on_airport = (TextView) goFlightView.findViewById(R.id.tv_on_airport);
                            TextView tv_company = (TextView) goFlightView.findViewById(R.id.tv_company);
                            TextView tv_code = (TextView) goFlightView.findViewById(R.id.tv_code);
                            ImageView iv_flight_icon = (ImageView) goFlightView.findViewById(R.id.iv_flight_icon);
                            TextView tv_build_price = (TextView) goFlightView.findViewById(R.id.tv_build_price);

                            tv_flag.setText("去程");
                            Date date = DateUtil.str2Date(goFlightInfo.dptDate, "yyyy-MM-dd");
                            tv_date.setText(DateUtil.getDay("MM月dd日", date.getTime()) + " " + goFlightInfo.dptTime);
                            tv_price.setText(String.format(getString(R.string.rmbStr2), Integer.valueOf(goFlightBookingInfo.extInfo.barePrice)));
                            tv_off_time.setText(goFlightInfo.dptTime);
                            tv_on_time.setText(goFlightInfo.arrTime);
                            tv_during.setText(goFlightInfo.flightTimes);
                            tv_off_airport.setText(goFlightInfo.dptAirport + goFlightInfo.dptTerminal);
                            tv_on_airport.setText(goFlightInfo.arrAirport + goFlightInfo.arrTerminal);
                            tv_code.setText(goFlightInfo.flightNum);
                            if (StringUtil.notEmpty(goFlightInfo.carrier)) {
                                if (SessionContext.getAirCompanyMap().size() > 0
                                        && SessionContext.getAirCompanyMap().containsKey(goFlightInfo.carrier)) {
                                    AirCompanyInfoBean companyInfoBean = SessionContext.getAirCompanyMap().get(goFlightInfo.carrier);
                                    tv_company.setText(companyInfoBean.company);
                                    iv_flight_icon.setVisibility(View.VISIBLE);
                                    Glide.with(this)
                                            .load(new CustomReqURLFormatModelImpl(companyInfoBean.logourl))
                                            .fitCenter()
                                            .override(Utils.dp2px(15), Utils.dp2px(15))
                                            .into(iv_flight_icon);
                                } else {
                                    tv_company.setText(goFlightInfo.carrier);
                                    iv_flight_icon.setVisibility(View.INVISIBLE);
                                }
                            } else {
                                iv_flight_icon.setVisibility(View.INVISIBLE);
                            }
                            tv_build_price.setText(String.format(getString(R.string.rmbStr2), (Integer.valueOf(goFlightInfo.arf) + Integer.valueOf(goFlightInfo.tof))));
                        }

                        //返程信息
                        {
                            if (PlaneOrderManager.instance.isFlightGoBack() && null != backFlightBookingInfo) {
                                PlaneBookingInfo_FlightInfo backFlightInfo = backFlightBookingInfo.flightInfo.get(0);
                                PlaneBookingInfo_PriceInfo backPriceInfo = backFlightBookingInfo.priceInfo;

                                View backFlightView = LayoutInflater.from(this).inflate(R.layout.layout_plane_orderdetail_flightinfo_item, null);
                                flight_layout.addView(backFlightView);
                                TextView tv_flag = (TextView) backFlightView.findViewById(R.id.tv_flag);
                                TextView tv_date = (TextView) backFlightView.findViewById(R.id.tv_date);
                                TextView tv_price = (TextView) backFlightView.findViewById(R.id.tv_price);
                                TextView tv_off_time = (TextView) backFlightView.findViewById(R.id.tv_off_time);
                                TextView tv_on_time = (TextView) backFlightView.findViewById(R.id.tv_on_time);
                                TextView tv_during = (TextView) backFlightView.findViewById(R.id.tv_during);
                                TextView tv_off_airport = (TextView) backFlightView.findViewById(R.id.tv_off_airport);
                                TextView tv_on_airport = (TextView) backFlightView.findViewById(R.id.tv_on_airport);
                                TextView tv_company = (TextView) backFlightView.findViewById(R.id.tv_company);
                                TextView tv_code = (TextView) backFlightView.findViewById(R.id.tv_code);
                                ImageView iv_flight_icon = (ImageView) backFlightView.findViewById(R.id.iv_flight_icon);
                                TextView tv_build_price = (TextView) backFlightView.findViewById(R.id.tv_build_price);

                                tv_flag.setText("返程");
                                Date date = DateUtil.str2Date(backFlightInfo.dptDate, "yyyy-MM-dd");
                                tv_date.setText(DateUtil.getDay("MM月dd日", date.getTime()) + " " + backFlightInfo.dptTime);
                                tv_price.setText(String.format(getString(R.string.rmbStr2), Integer.valueOf(backFlightBookingInfo.extInfo.barePrice)));
                                tv_off_time.setText(backFlightInfo.dptTime);
                                tv_on_time.setText(backFlightInfo.arrTime);
                                tv_during.setText(backFlightInfo.flightTimes);
                                tv_off_airport.setText(backFlightInfo.dptAirport + backFlightInfo.dptTerminal);
                                tv_on_airport.setText(backFlightInfo.arrAirport + backFlightInfo.arrTerminal);
                                tv_code.setText(backFlightInfo.flightNum);
                                if (StringUtil.notEmpty(backFlightInfo.carrier)) {
                                    if (SessionContext.getAirCompanyMap().size() > 0
                                            && SessionContext.getAirCompanyMap().containsKey(backFlightInfo.carrier)) {
                                        AirCompanyInfoBean companyInfoBean = SessionContext.getAirCompanyMap().get(backFlightInfo.carrier);
                                        tv_company.setText(companyInfoBean.company);
                                        iv_flight_icon.setVisibility(View.VISIBLE);
                                        Glide.with(this)
                                                .load(new CustomReqURLFormatModelImpl(companyInfoBean.logourl))
                                                .fitCenter()
                                                .override(Utils.dp2px(15), Utils.dp2px(15))
                                                .into(iv_flight_icon);
                                    } else {
                                        tv_company.setText(backFlightInfo.carrier);
                                        iv_flight_icon.setVisibility(View.INVISIBLE);
                                    }
                                } else {
                                    iv_flight_icon.setVisibility(View.INVISIBLE);
                                }
                                tv_build_price.setText(String.format(getString(R.string.rmbStr2), (Integer.valueOf(backFlightInfo.arf) + Integer.valueOf(backFlightInfo.tof))));
                            }
                        }
                    }

                    mFlightTicketDialog.setNegativeButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mFlightTicketDialog.dismiss();
                        }
                    });
                }
                mFlightTicketDialog.setCanceledOnTouchOutside(true);
                mFlightTicketDialog.show();
                break;
            }
            case R.id.tv_express_chooser: {
                Intent intent = new Intent(this, PlaneAddrChooserActivity.class);
                startActivityForResult(intent, RequestCodeDef.REQ_CODE_ADDRESS_SET_DEFAULT);
                break;
            }
            case R.id.tv_submit: {
                //发票相关
                if (btn_invoice_switch.isChecked()) {
                    invoiceTaxInfoBean.setExpressAmount(mExpressPrice);
                    invoiceTaxInfoBean.setInvoiceType(invoiceType);
                    invoiceTaxInfoBean.setReceiverType(receiverType);
                    String receiveTitle = tr_invoice_header.isShown() ? et_receive_title.getText().toString() : "";
                    invoiceTaxInfoBean.setReceiverTitle(receiveTitle);
                    if (findViewById(R.id.tv_express_tips).isShown()
                            || !findViewById(R.id.express_contact_lay).isShown()
                            || null == mBean) {
                        CustomToast.show("请选择收货地址", CustomToast.LENGTH_SHORT);
                        return;
                    }
                    invoiceTaxInfoBean.setAddress(mBean.province + mBean.city + mBean.area + mBean.address);
                    invoiceTaxInfoBean.setSjr(mBean.name);
                    invoiceTaxInfoBean.setSjrPhone(mBean.phone);
                } else {
                    invoiceTaxInfoBean = new PlaneInvoiceTaxInfoBean();
                }
                //联系人信息，必填（无论需不需要发票）
                if (checkRequestParamsValid()) {
                    invoiceTaxInfoBean.setContact(et_contact.getText().toString());
                    invoiceTaxInfoBean.setContactMob(et_contactMob.getText().toString());
                    requestSubmitOrderInfo();
                    //缓存机票订单联系人信息
                    String contactCache = et_contact.getText().toString() + "|" + et_contactMob.getText().toString();
                    SharedPreferenceUtil.getInstance().setString(AppConst.PLANE_ORDER_CONTACT_INFO, contactCache, true);
                    //缓存机票乘机人信息
                    String passengerJsonStr = custom_info_layout_plane.getCustomInfoJsonString();
                    SharedPreferenceUtil.getInstance().setString(AppConst.PLANE_ORDER_PASSENGERS_INFO, passengerJsonStr, true);
                }
                break;
            }
            case R.id.order_detail_layout:
                break;
        }
    }

    private void setAgreement1TextSpan(String agreement) {
        SpannableString ss = new SpannableString(agreement);
        //锂电池规定
        ss.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(PlaneNewOrderActivity.this, HtmlActivity.class);
                intent.putExtra("path", NetURL.PLANE_LITHIUM_RULE);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        }, 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.plane_mainColor)), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        //禁止危险品乘机说明
        ss.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(PlaneNewOrderActivity.this, HtmlActivity.class);
                intent.putExtra("path", NetURL.PLANE_DANGEROUS_RULE);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        }, 6, 15, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.plane_mainColor)), 6, 15, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        //特殊旅客购票须知
        ss.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(PlaneNewOrderActivity.this, HtmlActivity.class);
                intent.putExtra("path", NetURL.PLANE_PASSENGER_RULE);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        }, 18, agreement.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.plane_mainColor)), 18, agreement.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tv_plane_agreement1.setLinksClickable(true);
        tv_plane_agreement1.setMovementMethod(LinkMovementMethod.getInstance());
        tv_plane_agreement1.setText(ss);
    }

    private void setAgreement2TextSpan(String agreement) {
        SpannableString ss = new SpannableString(agreement);
        ss.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(PlaneNewOrderActivity.this, UcAboutActivity.class);
                intent.putExtra("title", getResources().getString(R.string.setting_usage_condition));
                intent.putExtra("index", UcAboutActivity.WORK_CONDITION);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        }, 0, agreement.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.plane_mainColor)), 0, agreement.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tv_plane_agreement2.setLinksClickable(true);
        tv_plane_agreement2.setMovementMethod(LinkMovementMethod.getInstance());
        tv_plane_agreement2.setText(ss);
    }

    private boolean checkRequestParamsValid() {
        boolean isValid = false;
        if (StringUtil.isEmpty(et_contact.getText().toString())) {
            CustomToast.show("请输入联系人姓名", CustomToast.LENGTH_SHORT);
        } else if (StringUtil.isEmpty(et_contactMob.getText().toString())) {
            CustomToast.show("请输入联系人手机号码", CustomToast.LENGTH_SHORT);
        } else if (!Utils.isMobile(et_contactMob.getText().toString())) {
            CustomToast.show("请输入正确的手机号码", CustomToast.LENGTH_SHORT);
        } else if (!((CheckBox) findViewById(R.id.cb_agreement_plane)).isChecked()) {
            CustomToast.show("请先阅读机票购票须知", CustomToast.LENGTH_SHORT);
        } else if (!((CheckBox) findViewById(R.id.cb_agreement_abc)).isChecked()) {
            CustomToast.show("请先阅读ABC旅行预订会员服务协议", CustomToast.LENGTH_SHORT);
        } else {
            isValid = true;
        }
        return isValid;
    }

    private int calculateSafePriceSingle() {
        int safePrice = 0;
        //如果是往返航班，则一个人需要支付两份保险的价钱
        int params = (flightType == PlaneCommDef.FLIGHT_GOBACK) ? 2 : 1;
        if (cb_delay.isChecked() && cb_accident.isChecked()) {
            safeType = PlaneCommDef.SAFE_BUY_ALL;
            safePrice = mDelayPrice + mAccidentPrice;
        } else if (cb_delay.isChecked()) {
            safeType = PlaneCommDef.SAFE_BUY_DEL;
            safePrice = mDelayPrice;
        } else if (cb_accident.isChecked()) {
            safeType = PlaneCommDef.SAFE_BUY_YII;
            safePrice = mAccidentPrice;
        } else {
            safeType = PlaneCommDef.SAFE_BUY_NON;
            safePrice = 0;
        }
        return safePrice * params;
    }

    private int calculateAmount(int passengers) {
        LogUtil.i(TAG, "calculateAmount passengers = " + passengers);
        int totalTicketPrice = passengers * mTicketPrice;
        mSafePrice = calculateSafePriceSingle();
        int totalSafePrice = passengers * mSafePrice;
        mAmount = totalTicketPrice + totalSafePrice + (btn_invoice_switch.isChecked() ? mExpressPrice : 0);
        tv_amount.setText(String.format(getString(R.string.rmbStr2), mAmount));
        tv_passenger.setText(String.format(getString(R.string.passengersStr), passengers));
        return mAmount;
    }

    private void updatePlaneOrderInfo() {
        if (null != goFlightBookingInfo) {
            PlaneBookingInfo_FlightInfo goFlightInfo = goFlightBookingInfo.flightInfo.get(0);
            PlaneBookingInfo_PriceInfo goPriceInfo = goFlightBookingInfo.priceInfo;
            // 刷新去程航班信息显示
            {
                flight_flag_layout.removeAllViews();
                View goPlaneOrder = LayoutInflater.from(this).inflate(R.layout.layout_plane_order_item, null);
                flight_flag_layout.addView(goPlaneOrder);
                TextView go_tv_flag = (TextView) goPlaneOrder.findViewById(R.id.tv_flag);
                go_tv_flag.setVisibility(View.GONE);
                TextView go_tv_offdate = (TextView) goPlaneOrder.findViewById(R.id.tv_offdate);
                TextView go_tv_cabin = (TextView) goPlaneOrder.findViewById(R.id.tv_cabin);
                TextView go_tv_airport = (TextView) goPlaneOrder.findViewById(R.id.tv_airport);
                TextView go_tv_price = (TextView) goPlaneOrder.findViewById(R.id.tv_price);
                TextView go_tv_jj_price = (TextView) goPlaneOrder.findViewById(R.id.tv_jj_price);

                Date go_date = DateUtil.str2Date(goFlightInfo.dptDate, "yyyy-MM-dd");
                go_tv_offdate.setText(DateUtil.getDay("MM-dd  ", go_date.getTime()));
                go_tv_offdate.append(DateUtil.dateToWeek2(go_date));
                go_tv_offdate.append("  " + goFlightInfo.dptTime);
                go_tv_airport.setText(String.format("%1$s%2$s - %3$s%4$s",
                        goFlightInfo.dptAirport,
                        goFlightInfo.dptTerminal,
                        goFlightInfo.arrAirport,
                        goFlightInfo.arrTerminal));
//                int goCabinType = 0;
//                if (goFlightDetailInfo.vendorInfo.cabinType >= 0 && goFlightDetailInfo.vendorInfo.cabinType <= 2) {
//                    goCabinType = goFlightDetailInfo.vendorInfo.cabinType;
//                }
//                go_tv_cabin.setText(PlaneCommDef.CabinLevel.values()[goCabinType].getValue());
                go_tv_cabin.setText(goFlightInfo.ccbcn);
                go_tv_price.setText(String.format(getString(R.string.rmbStr2), Integer.valueOf(goFlightBookingInfo.extInfo.barePrice)));
                go_tv_jj_price.setText(String.format(getString(R.string.rmbStr2), (Integer.valueOf(goPriceInfo.arf) + Integer.valueOf(goPriceInfo.tof))));
                mTicketPrice += Integer.valueOf(goFlightBookingInfo.extInfo.barePrice) + Integer.valueOf(goPriceInfo.arf) + Integer.valueOf(goPriceInfo.tof);

                // 如果往返航班，则增加返程航班信息显示
                if (PlaneOrderManager.instance.isFlightGoBack() && null != backFlightBookingInfo) {
                    PlaneBookingInfo_FlightInfo backFlightInfo = backFlightBookingInfo.flightInfo.get(0);
                    PlaneBookingInfo_PriceInfo backPriceInfo = backFlightBookingInfo.priceInfo;

                    View backPlaneOrder = LayoutInflater.from(this).inflate(R.layout.layout_plane_order_item, null);
                    flight_flag_layout.addView(backPlaneOrder);
                    TextView back_tv_flag = (TextView) backPlaneOrder.findViewById(R.id.tv_flag);
                    TextView back_tv_offdate = (TextView) backPlaneOrder.findViewById(R.id.tv_offdate);
                    TextView back_tv_cabin = (TextView) backPlaneOrder.findViewById(R.id.tv_cabin);
                    TextView back_tv_airport = (TextView) backPlaneOrder.findViewById(R.id.tv_airport);
                    TextView back_tv_price = (TextView) backPlaneOrder.findViewById(R.id.tv_price);
                    TextView back_tv_jj_price = (TextView) backPlaneOrder.findViewById(R.id.tv_jj_price);
                    go_tv_flag.setVisibility(View.VISIBLE);
                    go_tv_flag.setText("去程：");
                    back_tv_flag.setVisibility(View.VISIBLE);
                    back_tv_flag.setText("返程：");
                    Date back_date = DateUtil.str2Date(backFlightInfo.dptDate, "yyyy-MM-dd");
                    back_tv_offdate.setText(DateUtil.getDay("MM-dd  ", back_date.getTime()));
                    back_tv_offdate.append(DateUtil.dateToWeek2(back_date));
                    back_tv_offdate.append("  " + backFlightInfo.dptTime);
                    back_tv_airport.setText(String.format("%1$s%2$s - %3$s%4$s",
                            backFlightInfo.dptAirport,
                            backFlightInfo.dptTerminal,
                            backFlightInfo.arrAirport,
                            backFlightInfo.arrTerminal));
//                    int backCabinType = 0;
//                    if (backFlightDetailInfo.vendorInfo.cabinType >= 0 && backFlightDetailInfo.vendorInfo.cabinType <= 2) {
//                        backCabinType = backFlightDetailInfo.vendorInfo.cabinType;
//                    }
//                    back_tv_cabin.setText(PlaneCommDef.CabinLevel.values()[backCabinType].getValue());
                    back_tv_cabin.setText(backFlightInfo.ccbcn);
                    back_tv_price.setText(String.format(getString(R.string.rmbStr2), Integer.valueOf(backFlightBookingInfo.extInfo.barePrice)));
                    back_tv_jj_price.setText(String.format(getString(R.string.rmbStr2), (Integer.valueOf(backPriceInfo.arf) + Integer.valueOf(backPriceInfo.tof))));
                    mTicketPrice += Integer.valueOf(backFlightBookingInfo.extInfo.barePrice) + Integer.valueOf(backPriceInfo.arf) + Integer.valueOf(backPriceInfo.tof);
                }
            }
        }
        calculateAmount(mPassengerCount);

        if (null != goFlightBookingInfo) {
            //保险信息
            mAccidentPrice = goFlightBookingInfo.etAccidePrice;
            tv_accident_price.setText(String.format(getString(R.string.rmbStr2), mAccidentPrice));
            mDelayPrice = goFlightBookingInfo.etDelayPrice;
            tv_delay_price.setText(String.format(getString(R.string.rmbStr2), mDelayPrice));

            //邮费信息
            tv_express_price.setVisibility(View.VISIBLE);
            mExpressPrice = goFlightBookingInfo.expressInfo.price;
            if (null != backFlightBookingInfo) {
                int tmpPrice = backFlightBookingInfo.expressInfo.price;
                mExpressPrice = (mExpressPrice >= tmpPrice) ? mExpressPrice : tmpPrice; //如果往返航班，则取邮费最大值
            }
            tv_express_price.setText(String.format(getString(R.string.expressPrice), mExpressPrice));

            //发票类型
            boolean isShowInvoiceHeader = false;
            JSONObject goInvoiceTypeJson = goFlightBookingInfo.expressInfo.invoiceType;
            //如果单程，backInvoiceTypeJson等于goInvoiceTypeJson
            //如果往返，backInvoiceTypeJson等于backFlightBookingInfo.expressInfo.invoiceType
            JSONObject backInvoiceTypeJson = (null != backFlightBookingInfo) ? backFlightBookingInfo.expressInfo.invoiceType : goFlightBookingInfo.expressInfo.invoiceType;
            if (goInvoiceTypeJson != null && backInvoiceTypeJson != null) {
                tv_invoice_type.setVisibility(View.VISIBLE);
                if (goInvoiceTypeJson.containsKey("2") && backInvoiceTypeJson.containsKey("2")) { //如果同时包含2，则按照行程单差额发票开票
                    invoiceType = PlaneCommDef.INVOICE_XCD;
                    tv_invoice_type.setText(goInvoiceTypeJson.getString("2"));
                    tr_invoice_header.setVisibility(View.GONE);
                    line_invoice_header.setVisibility(View.GONE);
                } else if (goInvoiceTypeJson.containsKey("1") || backInvoiceTypeJson.containsKey("1")) { //如果至少一个包含1，则按照全额发票开票
                    invoiceType = PlaneCommDef.INVOICE_ALL;
                    isShowInvoiceHeader = true;
                    String invoiceText = (goInvoiceTypeJson.containsKey("1") ? goInvoiceTypeJson.getString("1") : "");
                    invoiceText = (backInvoiceTypeJson.containsKey("1") ? backInvoiceTypeJson.getString("1") : invoiceText);
                    tv_invoice_type.setText(invoiceText);
                    tr_invoice_header.setVisibility(View.VISIBLE);
                    line_invoice_header.setVisibility(View.VISIBLE);
                } else { //如果都不包含，则暂时不显示
                    invoiceType = PlaneCommDef.INVOICE_INVALID;
                    LogUtil.i(TAG, "未定义类型：" + goInvoiceTypeJson.toString());
                    tv_invoice_type.setText(goInvoiceTypeJson.toString());
                    tr_invoice_header.setVisibility(View.GONE);
                    line_invoice_header.setVisibility(View.GONE);
                }
            }

            //发票支持抬头信息
            if (isShowInvoiceHeader) {
                rg_invoice.removeAllViews();
                JSONObject goInvoiceHeaderJson = goFlightBookingInfo.expressInfo.receiverType;
                //如果单程，backInvoiceHeaderJson等于goInvoiceHeaderJson
                //如果往返，backInvoiceHeaderJson等于backFlightBookingInfo.expressInfo.receiverType
                JSONObject backInvoiceHeaderJson = (null != backFlightBookingInfo) ? backFlightBookingInfo.expressInfo.receiverType : goInvoiceHeaderJson;
                if (goInvoiceHeaderJson != null && backInvoiceHeaderJson != null) {
                    ArrayList<String> keyList = new ArrayList<>();
                    //获取往返航班同时包含的receiverType
                    for (String goKey : goInvoiceHeaderJson.keySet()) {
                        for (String backKey : backInvoiceHeaderJson.keySet()) {
                            if (goKey.equals(backKey)) {
                                keyList.add(goKey);
                            }
                        }
                    }
                    Collections.sort(keyList);
                    for (String key : keyList) {
                        RadioButton rb = new RadioButton(this);
                        RadioGroup.LayoutParams glp = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        glp.weight = 1;
                        glp.setMargins(Utils.dp2px(2), Utils.dp2px(2), Utils.dp2px(2), Utils.dp2px(2));
                        rb.setPadding(Utils.dp2px(5), Utils.dp2px(5), Utils.dp2px(5), Utils.dp2px(5));
                        rb.setGravity(Gravity.CENTER);
                        rb.setBackgroundDrawable(getResources().getDrawable(R.drawable.plane_invoice_type_bg_sel));
                        rb.setButtonDrawable(null);
                        rb.setTag(key);
                        rb.setText(goInvoiceHeaderJson.getString(key));
                        rb.setTextColor(getResources().getColorStateList(R.color.plane_invoice_type_text_sel));
                        rb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                        rg_invoice.addView(rb, glp);
                    }
                    rg_invoice.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                            RadioButton rb = (RadioButton) findViewById(checkedId);
                            receiverType = Integer.valueOf((String) rb.getTag());
                            if (PlaneCommDef.RECEIVE_DANWEII == receiverType) {
                                receiverType = PlaneCommDef.RECEIVE_BUSINESS;
                            }
                            if (PlaneCommDef.RECEIVE_PERSONAL == receiverType) {
                                tv_personal_invoice.setVisibility(View.VISIBLE);
                                company_invoice.setVisibility(View.GONE);
                            } else {
                                tv_personal_invoice.setVisibility(View.GONE);
                                company_invoice.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                    rg_invoice.check(rg_invoice.getChildAt(0).getId());
                }
            }
            root.setVisibility(View.VISIBLE);
        }
    }

    private void updateExpressAddressDisplayInfo(AddressInfoBean bean) {
        LogUtil.i(TAG, "updateExpressAddressDisplayInfo()");
        if (null != bean) {
            findViewById(R.id.tv_express_tips).setVisibility(View.GONE);
            tv_express_addr.setVisibility(View.VISIBLE);
            findViewById(R.id.express_contact_lay).setVisibility(View.VISIBLE);
            tv_express_addr.setText(bean.province + bean.city + bean.area + bean.address);
            tv_express_name.setText(bean.name);
            tv_express_phone.setText(bean.phone);
        } else {
            findViewById(R.id.tv_express_tips).setVisibility(View.VISIBLE);
            tv_express_addr.setVisibility(View.GONE);
            findViewById(R.id.express_contact_lay).setVisibility(View.GONE);
        }
    }

    private void requestDefaultAddressInfo() {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        ResponseData d = b.syncRequest(b);
        d.flag = AppConst.ADDRESS_GET_DEFAULT;
        d.path = NetURL.ADDRESS_GET_DEFAULT;
        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void requestTicketBookingInfo(int type) {
        PlaneFlightInfoBean flightInfo = null;
        PlaneTicketInfoBean ticketInfo = null;
        PlaneVendorInfoBean vendorInfo = null;
        if (type == TYPE_GO) {
            flightInfo = PlaneOrderManager.instance.getGoFlightInfo();
            ticketInfo = PlaneOrderManager.instance.getGoTicketInfo();
            vendorInfo = PlaneOrderManager.instance.getGoVendorInfo();
        } else if (type == TYPE_BACK) {
            flightInfo = PlaneOrderManager.instance.getBackFlightInfo();
            ticketInfo = PlaneOrderManager.instance.getBackTicketInfo();
            vendorInfo = PlaneOrderManager.instance.getBackVendorInfo();
        }
        if (flightInfo == null || ticketInfo == null || vendorInfo == null) {
            return;
        }

        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("barePrice", vendorInfo.barePrice);
        b.addBody("basePrice", vendorInfo.basePrice);
        b.addBody("businessExt", vendorInfo.businessExt);
        b.addBody("cabin", vendorInfo.cabin);
        b.addBody("client", vendorInfo.domain);
        b.addBody("policyId", vendorInfo.policyId);
        b.addBody("policyType", vendorInfo.policyType);
        b.addBody("price", vendorInfo.price);
        b.addBody("tag", vendorInfo.bprtag);
        b.addBody("ticketPrice", vendorInfo.vppr);
        b.addBody("userName", "wneydek9283");
        b.addBody("wrapperId", vendorInfo.wrapperId);
        b.addBody("carrier", flightInfo.carrier);
        b.addBody("flightNum", flightInfo.flightNum);
        String startTime = flightInfo.dptTime;
        b.addBody("dptTime", startTime.replace(":", ""));
        b.addBody("from", flightInfo.dpt);
        b.addBody("to", flightInfo.arr);
        String startDate = ticketInfo.date;
        b.addBody("startTime", startDate.replace("-", ""));
        b.addBody("flightType", "1"); //固定值，1表示单程

        ResponseData d = b.syncRequest(b);
        d.flag = AppConst.PLANE_BOOKING_INFO << type;
        d.path = NetURL.PLANE_BOOKING_INFO;

        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
        requestTagCount++;
    }

    private void requestSubmitOrderInfo() {
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("bKInfo", JSON.toJSONString(mBkInfo));
        String invoiceTax = JSON.toJSONString(invoiceTaxInfoBean);
        LogUtil.i(TAG, "invoiceTax:" + invoiceTax);
        b.addBody("invoiceTax", invoiceTax);
        b.addBody("insuranceType", String.valueOf(safeType));
        LogUtil.i(TAG, "insuranceType:" + safeType);
        String passenger = custom_info_layout_plane.getCustomInfoJsonString();
        LogUtil.i(TAG, "passengers:" + passenger);
        b.addBody("passengers", passenger);

        ResponseData d = b.syncRequest(b);
        d.flag = AppConst.PLANE_NEW_ORDER;
        d.path = NetURL.PLANE_NEW_ORDER;
        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    public void showAccidentDetail(View v) {
        Intent intent = new Intent(this, HtmlActivity.class);
        intent.putExtra("path", NetURL.PLANE_ACCIDENT_DETAIL);
        startActivity(intent);
    }

    public void showDelayDetail(View v) {
        Intent intent = new Intent(this, HtmlActivity.class);
        intent.putExtra("path", NetURL.PLANE_DELAY_DETAIL);
        startActivity(intent);
    }

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        super.onNotifyMessage(request, response);
        if (response != null && response.body != null) {
            LogUtil.i(TAG, "flag = " + request.flag);
            if (request.flag == (AppConst.PLANE_BOOKING_INFO << TYPE_GO)) {
                mTag.put(request.flag, request.flag);
                LogUtil.i(TAG, "go booking info json = " + response.body.toString());
                goFlightBookingInfo = JSON.parseObject(response.body.toString(), PlaneBookingInfo.class);
                mBkInfo.add(goFlightBookingInfo);
            } else if (request.flag == (AppConst.PLANE_BOOKING_INFO << TYPE_BACK)) {
                mTag.put(request.flag, request.flag);
                LogUtil.i(TAG, "back booking info json = " + response.body.toString());
                backFlightBookingInfo = JSON.parseObject(response.body.toString(), PlaneBookingInfo.class);
                mBkInfo.add(backFlightBookingInfo);
            } else if (request.flag == AppConst.ADDRESS_GET_DEFAULT) {
                LogUtil.i(TAG, "json = " + response.body.toString());
                JSONObject mJson = JSON.parseObject(response.body.toString());
                if (mJson.containsKey("address")) {
                    String address = mJson.getString("address");
                    if (StringUtil.notEmpty(address)) {
                        mBean = JSON.parseObject(address, AddressInfoBean.class);
                        updateExpressAddressDisplayInfo(mBean);
                    }
                }
            } else if (request.flag == AppConst.PLANE_NEW_ORDER) {
                removeProgressDialog();
                LogUtil.i(TAG, "json = " + response.body.toString());
                JSONObject mJson = JSON.parseObject(response.body.toString());
                if (StringUtil.notEmpty(mJson) && mJson.containsKey("orderNum")) {
                    Intent intent = new Intent(this, PlaneOrderPayActivity.class);
                    intent.putExtra("orderNo", mJson.getString("orderNum"));
                    intent.putExtra("amount", mAmount);
                    intent.putExtra("bookingInfoList", (Serializable) mBkInfo);
                    startActivity(intent);
                } else {
                    CustomToast.show("创建订单失败", CustomToast.LENGTH_SHORT);
                }
            }

            if (mTag.size() == requestTagCount) {
                removeProgressDialog();
                mTag.clear();
                updatePlaneOrderInfo();
            }
        }
    }

    @Override
    protected boolean isCheckException(ResponseData request, ResponseData response) {
//        if (null != response && null != response.data) {
//            if (PlaneErrorDef.FLIGHT_REQUEST_IS_INVALID.equals(response.code)) {
//                return true;
//            }
//        }
        return super.isCheckException(request, response);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == RequestCodeDef.REQ_CODE_ADDRESS_SET_DEFAULT) {
            if (null != data) {
                mBean = (AddressInfoBean) data.getSerializableExtra("addressBean");
                updateExpressAddressDisplayInfo(mBean);
            }
        }
    }
}

