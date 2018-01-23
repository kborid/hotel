package com.huicheng.hotel.android.ui.activity.hotel;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.requestbuilder.bean.InvoiceDetailInfoBean;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.prj.sdk.util.StringUtil;

/**
 * @author kborid
 * @date 2017/1/7 0007
 */
public class HotelInvoiceActivity extends BaseAppActivity {
    private Switch btn_switch;
    private TextView tv_status;
    private LinearLayout invoice_lay;

    private EditText et_company_name, et_id, et_email;
    private InvoiceDetailInfoBean bean = new InvoiceDetailInfoBean();
    private TextView tv_confirm;
    private boolean isInvoice = false;

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_hotel_invoicedetail);
    }

    @Override
    public void initViews() {
        super.initViews();
        btn_switch = (Switch) findViewById(R.id.btn_switch);
        tv_status = (TextView) findViewById(R.id.tv_status);
        invoice_lay = (LinearLayout) findViewById(R.id.invoice_lay);
        et_company_name = (EditText) findViewById(R.id.et_company_name);
        et_id = (EditText) findViewById(R.id.et_id);
        et_email = (EditText) findViewById(R.id.et_email);
        tv_confirm = (TextView) findViewById(R.id.tv_confirm);
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.getBoolean("isInvoice")) {
                isInvoice = bundle.getBoolean("isInvoice");
            }
            if (bundle.get("InvoiceDetail") != null) {
                bean = (InvoiceDetailInfoBean) bundle.get("InvoiceDetail");
            }
        }
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText("发票");
        btn_switch.setChecked(isInvoice);
        invoice_lay.setVisibility(View.GONE);
        updateInvoiceInfo();
    }

    private void updateInvoiceInfo() {
        if (isInvoice) {
            tv_status.setText(getString(R.string.order_invoice_tips));
            invoice_lay.setVisibility(View.VISIBLE);
            if (null != bean) {
                et_company_name.setText(bean.title);
                et_id.setText(bean.taxpayerIdentifyNum);
                et_email.setText(bean.email);
            }
        } else {
            tv_status.setText(getString(R.string.order_need_not_invoice));
            invoice_lay.setVisibility(View.GONE);
        }
    }

    @Override
    public void initListeners() {
        super.initListeners();
        btn_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                btn_switch.setChecked(isChecked);
                if (isChecked) {
                    tv_status.setText(getString(R.string.order_invoice_tips));
                    invoice_lay.setVisibility(View.VISIBLE);
                } else {
                    tv_status.setText(getString(R.string.order_need_not_invoice));
                    invoice_lay.setVisibility(View.GONE);
                }
            }
        });
        tv_confirm.setOnClickListener(this);
        iv_back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_back:
                if (!btn_switch.isChecked()) {
                    Intent data1 = new Intent();
                    data1.putExtra("isInvoice", btn_switch.isChecked());
                    setResult(RESULT_OK, data1);
                }
                this.finish();
                break;
            case R.id.tv_confirm:
                if (StringUtil.notEmpty(et_company_name.getText().toString())) {
                    bean.title = et_company_name.getText().toString();
                } else {
                    CustomToast.show("请输入发票抬头", CustomToast.LENGTH_SHORT);
                    return;
                }

                if (StringUtil.notEmpty(et_id.getText().toString())) {
                    bean.taxpayerIdentifyNum = et_id.getText().toString();
                } else {
                    CustomToast.show("请输入纳税人识别号", CustomToast.LENGTH_SHORT);
                    return;
                }

                if (StringUtil.notEmpty(et_email.getText().toString())) {
                    bean.email = et_email.getText().toString();
                } else {
                    CustomToast.show("请输入电子邮箱地址", CustomToast.LENGTH_SHORT);
                    return;
                }

                Intent data = new Intent();
                data.putExtra("isInvoice", btn_switch.isChecked());
                data.putExtra("InvoiceDetail", bean);
                setResult(RESULT_OK, data);
                this.finish();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            iv_back.performClick();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
