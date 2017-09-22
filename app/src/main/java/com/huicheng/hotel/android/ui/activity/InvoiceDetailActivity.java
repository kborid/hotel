package com.huicheng.hotel.android.ui.activity;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TabHost;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.net.bean.InvoiceDetailInfoBean;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.ThumbnailUtil;
import com.prj.sdk.util.Utils;
import com.huicheng.hotel.android.ui.dialog.CustomToast;

import java.io.File;
import java.io.IOException;

/**
 * @author kborid
 * @date 2017/1/7 0007
 */
public class InvoiceDetailActivity extends BaseActivity {
    private static final int SELECTED_BAR_COUNT = 2;
    private Switch btn_switch;
    private LinearLayout invoice_lay;
    private TabHost tabHost;

    private EditText et_company_name;
    private LinearLayout special_lay;
    private EditText et_id, et_company_addr, et_company_phone, et_bank, et_bank_access;
    private RadioGroup rg_lay;
    private LinearLayout third_lay, one_lay;
    private ImageView iv_thirdCommon, iv_taxRegistration, iv_businessLicense, iv_taxpayerIdentify;
    private int picType = -1;
    private String bgPath = null;
    private InvoiceDetailInfoBean bean = new InvoiceDetailInfoBean();
    private TextView tv_confirm;
    private boolean isInvoice = false;
    private int invoiceType = 0;

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
        tabHost = (TabHost) findViewById(R.id.tabHost);

        special_lay = (LinearLayout) findViewById(R.id.special_lay);
        rg_lay = (RadioGroup) findViewById(R.id.rg_lay);
        third_lay = (LinearLayout) findViewById(R.id.thrid_lay);
        one_lay = (LinearLayout) findViewById(R.id.one_lay);

        et_company_name = (EditText) findViewById(R.id.et_company_name);
        et_company_phone = (EditText) findViewById(R.id.et_company_phone);
        et_company_addr = (EditText) findViewById(R.id.et_company_addr);
        et_id = (EditText) findViewById(R.id.et_id);
        et_bank = (EditText) findViewById(R.id.et_bank);
        et_bank_access = (EditText) findViewById(R.id.et_bank_access);

        iv_thirdCommon = (ImageView) findViewById(R.id.iv_thirdCommon);
        iv_taxRegistration = (ImageView) findViewById(R.id.iv_taxRegistration);
        iv_businessLicense = (ImageView) findViewById(R.id.iv_businessLicense);
        iv_taxpayerIdentify = (ImageView) findViewById(R.id.iv_taxpayerIdentify);

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
            if (isInvoice) {
                invoiceType = bundle.getInt("InvoiceType");
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

        tabHost.setup();
        for (int i = 0; i < SELECTED_BAR_COUNT; i++) {
            View view = LayoutInflater.from(this).inflate(R.layout.selected_bar_item, null);
            TextView tv_tab = (TextView) view.findViewById(R.id.tv_tab);
            if (i == 0) {
                tv_tab.setText("普通发票");
                tabHost.addTab(tabHost.newTabSpec("tab" + i).setIndicator(view).setContent(R.id.tab_common));
            } else {
                tv_tab.setText("专用发票");
                tabHost.addTab(tabHost.newTabSpec("tab" + i).setIndicator(view).setContent(R.id.tab_special));
            }
        }
        tabHost.setCurrentTab(invoiceType);
        updateTab(tabHost);
        updateInvoiceInfo();

        if (rg_lay.getCheckedRadioButtonId() == R.id.rb_three) {
            third_lay.setVisibility(View.VISIBLE);
            one_lay.setVisibility(View.GONE);
        } else {
            third_lay.setVisibility(View.GONE);
            one_lay.setVisibility(View.VISIBLE);
        }
    }

    private void updateInvoiceInfo() {
        if (isInvoice) {
            invoice_lay.setVisibility(View.VISIBLE);
            if (invoiceType == 0) {
                special_lay.setVisibility(View.GONE);
                if (null != bean) {
                    et_company_name.setText(bean.title);
                    et_id.setText(bean.taxpayerIdentifyNum);
                }
            } else {
                special_lay.setVisibility(View.VISIBLE);
                if (null != bean) {
                    et_company_name.setText(bean.title);
                    et_id.setText(bean.taxpayerIdentifyNum);
                    et_company_addr.setText(bean.companyAddr);
                    et_company_phone.setText(bean.companyPhone);
                    et_bank.setText(bean.openingBank);
                    et_bank_access.setText(bean.bankAccount);
                    if (StringUtil.notEmpty(bean.thirdCommonPicPath)) {
                        rg_lay.check(R.id.rb_one);
                        loadImage(iv_thirdCommon, R.drawable.def_invoice, bean.thirdCommonPicPath, 200, 120);
                    } else {
                        rg_lay.check(R.id.rb_three);
                        loadImage(iv_businessLicense, R.drawable.def_invoice, bean.businessLicensePicPath, 200, 120);
                        loadImage(iv_taxpayerIdentify, R.drawable.def_invoice, bean.taxpayerIdentifyPicPath, 200, 120);
                        loadImage(iv_taxRegistration, R.drawable.def_invoice, bean.taxRegistrationPicPath, 200, 120);
                    }
                }
            }
        } else {
            invoice_lay.setVisibility(View.GONE);
        }
    }

    private void updateTab(final TabHost tabHost) {
        for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
            View view = tabHost.getTabWidget().getChildAt(i);
            if (i == 0) {
                view.setBackgroundResource(R.drawable.comm_rectangle_selected_bar_left_bg);
            } else if (i == tabHost.getTabWidget().getChildCount() - 1) {
                view.setBackgroundResource(R.drawable.comm_rectangle_selected_bar_right_bg);
            } else {
                view.setBackgroundResource(R.drawable.comm_rectangle_selected_bar_middle_bg);
            }
            TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(R.id.tv_tab);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            tv.setTextColor(getResources().getColorStateList(R.color.white));

            if (tabHost.getCurrentTab() == 0) {
                special_lay.setVisibility(View.GONE);
                bean.type = "1";
            } else {
                special_lay.setVisibility(View.VISIBLE);
                bean.type = "2";
            }
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

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                tabHost.setCurrentTabByTag(tabId);
                updateTab(tabHost);
            }
        });

        rg_lay.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_three:
                        third_lay.setVisibility(View.VISIBLE);
                        one_lay.setVisibility(View.GONE);
                        break;
                    case R.id.rb_one:
                        third_lay.setVisibility(View.GONE);
                        one_lay.setVisibility(View.VISIBLE);
                        break;
                    default:
                        break;
                }
            }
        });
        iv_thirdCommon.setOnClickListener(this);
        iv_taxRegistration.setOnClickListener(this);
        iv_businessLicense.setOnClickListener(this);
        iv_taxpayerIdentify.setOnClickListener(this);
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

                if (tabHost.getCurrentTab() == 1) {
                    if (StringUtil.notEmpty(et_company_addr.getText().toString())) {
                        bean.companyAddr = et_company_addr.getText().toString();
                    } else {
                        CustomToast.show("请输入公司地址", CustomToast.LENGTH_SHORT);
                        return;
                    }
                    if (StringUtil.notEmpty(et_company_phone.getText().toString())) {
                        bean.companyPhone = et_company_phone.getText().toString();
                    } else {
                        CustomToast.show("请输入公司电话", CustomToast.LENGTH_SHORT);
                        return;
                    }
                    if (StringUtil.notEmpty(et_bank.getText().toString())) {
                        bean.openingBank = et_bank.getText().toString();
                    } else {
                        CustomToast.show("请输入开户行", CustomToast.LENGTH_SHORT);
                        return;
                    }
                    if (StringUtil.notEmpty(et_bank_access.getText().toString())) {
                        bean.bankAccount = et_bank_access.getText().toString();
                    } else {
                        CustomToast.show("请输入开户行帐号", CustomToast.LENGTH_SHORT);
                        return;
                    }

                    if (rg_lay.getCheckedRadioButtonId() == R.id.rb_one) {
                        if (StringUtil.isEmpty(bean.thirdCommonPicPath)) {
                            CustomToast.show("请上传三证合一照片", CustomToast.LENGTH_SHORT);
                            return;
                        }
                        bean.taxpayerIdentifyPicPath = "";
                        bean.businessLicensePicPath = "";
                        bean.taxRegistrationPicPath = "";
                    } else {
                        if (StringUtil.isEmpty(bean.taxRegistrationPicPath)) {
                            CustomToast.show("请上传税务登记证照片", CustomToast.LENGTH_SHORT);
                            return;
                        }
                        if (StringUtil.isEmpty(bean.businessLicensePicPath)) {
                            CustomToast.show("请上传营业执照照片", CustomToast.LENGTH_SHORT);
                            return;
                        }
                        if (StringUtil.isEmpty(bean.taxpayerIdentifyPicPath)) {
                            CustomToast.show("请上传纳税人身份证照片", CustomToast.LENGTH_SHORT);
                            return;
                        }
                        bean.thirdCommonPicPath = "";
                    }
                }
                Intent data = new Intent();
                data.putExtra("isInvoice", btn_switch.isChecked());
                data.putExtra("InvoiceType", tabHost.getCurrentTab());
                data.putExtra("InvoiceDetail", bean);
                setResult(RESULT_OK, data);
                this.finish();
                break;

            case R.id.iv_thirdCommon:
                picType = 0;
                choosePictureDialog();
                break;
            case R.id.iv_taxRegistration:
                picType = 1;
                choosePictureDialog();
                break;
            case R.id.iv_businessLicense:
                picType = 2;
                choosePictureDialog();
                break;
            case R.id.iv_taxpayerIdentify:
                picType = 3;
                choosePictureDialog();
                break;
            default:
                break;
        }
    }

    private void choosePictureDialog() {
        if (!SessionContext.isLogin()) {
            sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION));
            return;
        }
        CustomDialog dialog = new CustomDialog(this);
        dialog.setMessage("请选择图片获取方式");
        dialog.setPositiveButton("图库", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Utils.isSDCardEnable()) {
                    Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    i.setType("image/*");
                    startActivityForResult(i, AppConst.ACTIVITY_GET_IMAGE);
                } else {
                    CustomToast.show("内存卡不可用，请检测内存卡", CustomToast.LENGTH_SHORT);
                }
                dialog.dismiss();
            }
        });
        dialog.setNegativeButton("相机", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Utils.isSDCardEnable()) {
                    File file = new File(Utils.getFolderDir("pic"), "_temp.jpg");
                    file.deleteOnExit();
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                    intent.putExtra("android.intent.extra.screenOrientation", false);
                    startActivityForResult(intent, AppConst.ACTIVITY_IMAGE_CAPTURE);
                } else {
                    CustomToast.show("内存卡不可用，请检测内存卡", CustomToast.LENGTH_SHORT);
                }
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(true);

        dialog.show();
    }

    /**
     * 上传图片 。图片缩略图最大为480x800的50%精度质量
     */
    private void uploadImage(File file) {
        LogUtil.i(TAG, "UploadImage()");
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("img", file);
        ResponseData d = b.syncRequestForForm(b);
        d.path = NetURL.UPLOAD;
        d.flag = AppConst.UPLOAD;
        d.isForm = true;

        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
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
    public void preExecute(ResponseData request) {

    }

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.UPLOAD) {
                removeProgressDialog();
                ContentResolver resolver = getContentResolver();
                Bitmap bm = null;
                try {
                    bm = MediaStore.Images.Media.getBitmap(resolver, Uri.fromFile(new File(bgPath)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                switch (picType) {
                    case 0:
                        bean.thirdCommonPicPath = response.body.toString();
                        iv_thirdCommon.setImageBitmap(bm);
                        break;
                    case 1:
                        bean.taxRegistrationPicPath = response.body.toString();
                        iv_taxRegistration.setImageBitmap(bm);
                        break;
                    case 2:
                        bean.businessLicensePicPath = response.body.toString();
                        iv_businessLicense.setImageBitmap(bm);
                        break;
                    case 3:
                        bean.taxpayerIdentifyPicPath = response.body.toString();
                        iv_taxpayerIdentify.setImageBitmap(bm);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        String filePath = null;
        switch (requestCode) {
            case AppConst.ACTIVITY_GET_IMAGE:
                Uri imageUri = data.getData();
                imageUri = ThumbnailUtil.geturi(this, data);//解决方案,小米手机无法获取filepath
                if (imageUri != null) {
                    filePath = ThumbnailUtil.getPicPath(this, imageUri);
                    LogUtil.i(TAG, "filepath image = " + filePath);
                    bgPath = Utils.getFolderDir("pic") + "_temp_compress.jpg";
                    String newFilePath = ThumbnailUtil.compressImage(filePath, bgPath);
                    if (StringUtil.notEmpty(newFilePath)) {
                        uploadImage(new File(newFilePath));
                    } else {
                        CustomToast.show("图片无效", CustomToast.LENGTH_SHORT);
                    }
                } else {
                    CustomToast.show("获取图片失败", 0);
                }
                break;
            case AppConst.ACTIVITY_IMAGE_CAPTURE:
                filePath = Utils.getFolderDir("pic") + "_temp.jpg";
                bgPath = Utils.getFolderDir("pic") + "_temp_compress.jpg";
                String newFilePath = ThumbnailUtil.compressImage(filePath, bgPath);
                if (StringUtil.notEmpty(newFilePath)) {
                    uploadImage(new File(newFilePath));
                } else {
                    CustomToast.show("图片无效", CustomToast.LENGTH_SHORT);
                }
                break;
            default:
                break;
        }
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
