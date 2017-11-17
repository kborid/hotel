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
import com.huicheng.hotel.android.net.bean.InvoiceDetailInfoBean;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.prj.sdk.util.StringUtil;

/**
 * @author kborid
 * @date 2017/1/7 0007
 */
public class HotelInvoiceActivity extends BaseActivity {
    private Switch btn_switch;
    private LinearLayout invoice_lay;

    private EditText et_company_name, et_id, et_email;
    private InvoiceDetailInfoBean bean = new InvoiceDetailInfoBean();
    private TextView tv_confirm;
    private boolean isInvoice = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_invoicedetail_layout);
        initViews();
        initParams();
        initListeners();
    }

    @Override
    public void initViews() {
        super.initViews();
        btn_switch = (Switch) findViewById(R.id.btn_switch);
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
            invoice_lay.setVisibility(View.VISIBLE);
            if (null != bean) {
                et_company_name.setText(bean.title);
                et_id.setText(bean.taxpayerIdentifyNum);
                et_email.setText(bean.email);
            }
        } else {
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
                    invoice_lay.setVisibility(View.VISIBLE);
                } else {
                    invoice_lay.setVisibility(View.GONE);
                }
            }
        });
        tv_confirm.setOnClickListener(this);
        btn_back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btn_back:
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
                    bean.email = et_id.getText().toString();
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
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            btn_back.performClick();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
