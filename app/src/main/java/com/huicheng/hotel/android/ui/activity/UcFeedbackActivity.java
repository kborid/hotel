package com.huicheng.hotel.android.ui.activity;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.huicheng.hotel.android.BuildConfig;
import com.huicheng.hotel.android.PRJApplication;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.permission.PermissionsActivity;
import com.huicheng.hotel.android.permission.PermissionsDef;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.ThumbnailUtil;
import com.prj.sdk.util.Utils;

import java.io.File;
import java.io.IOException;

/**
 * 意见反馈
 */
public class UcFeedbackActivity extends BaseAppActivity {

    private EditText et_content;

    private ImageView iv_picture, iv_screen;
    private LinearLayout hotel_phone_lay, train_phone_lay, taxi_phone_lay;
    private String model, osVersion, appVersion;
    private Button btn_submit;
    private String bgPath = null;
    private String imgUrl = null;

    private String mCallNumber = "";
    private CustomDialog mDialog = null;

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_uc_feedback);
    }

    @Override
    public void initViews() {
        super.initViews();
        et_content = (EditText) findViewById(R.id.et_content);
        iv_picture = (ImageView) findViewById(R.id.iv_picture);
        iv_screen = (ImageView) findViewById(R.id.iv_screen);
        btn_submit = (Button) findViewById(R.id.btn_submit);

        hotel_phone_lay = (LinearLayout) findViewById(R.id.hotel_phone_lay);
        train_phone_lay = (LinearLayout) findViewById(R.id.train_phone_lay);
        taxi_phone_lay = (LinearLayout) findViewById(R.id.taxi_phone_lay);
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText(getString(R.string.side_fd));
        model = Build.MODEL;
        osVersion = "Android" + Build.VERSION.RELEASE;
        appVersion = BuildConfig.VERSION_NAME;
    }

    @Override
    public void initListeners() {
        super.initListeners();
        iv_screen.setOnClickListener(this);
        iv_picture.setOnClickListener(this);
        btn_submit.setOnClickListener(this);
        hotel_phone_lay.setOnClickListener(this);
        train_phone_lay.setOnClickListener(this);
        taxi_phone_lay.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_screen:
                choosePictureDialog();
                break;
            case R.id.iv_picture:
                CustomDialog dialog = new CustomDialog(this);
                dialog.setMessage("是否删除该照片？");
                dialog.setNegativeButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        imgUrl = null;
                        iv_picture.setImageBitmap(null);
                        iv_picture.setVisibility(View.GONE);
                    }
                });
                dialog.setPositiveButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
                break;
            case R.id.btn_submit:
                if (StringUtil.isEmpty(et_content.getText().toString())) {
                    CustomToast.show("请输入反馈描述", CustomToast.LENGTH_SHORT);
                    return;
                }
                requestSubmit();
                break;
            case R.id.hotel_phone_lay:
                mCallNumber = getResources().getString(R.string.hotel_plane_phone);
                dialServicePhone();
                break;
            case R.id.train_phone_lay:
                mCallNumber = getResources().getString(R.string.train_phone);
                dialServicePhone();
                break;
            case R.id.taxi_phone_lay:
                mCallNumber = getResources().getString(R.string.taxi_phone);
                dialServicePhone();
                break;
            default:
                break;
        }
    }

    private void dialServicePhone() {
        if (PRJApplication.getPermissionsChecker(this).lacksPermissions(PermissionsDef.PHONE_PERMISSION)) {
            PermissionsActivity.startActivityForResult(this, PermissionsDef.PERMISSION_REQ_CODE, PermissionsDef.PHONE_PERMISSION);
            return;
        }
        showDialDialog(mCallNumber);
    }

    private void showDialDialog(final String number) {
        if (null == mDialog) {
            mDialog = new CustomDialog(this);
        }
        mDialog.setMessage(String.format("是否拨打客服电话%1$s", number));
        mDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        mDialog.setPositiveButton("拨打", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Uri dialUri = Uri.parse("tel:" + number);
                Intent callIntent = new Intent(Intent.ACTION_DIAL, dialUri);
                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(callIntent);
            }
        });
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.show();
    }

    private void choosePictureDialog() {
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

    private void requestSubmit() {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("content", et_content.getText().toString());
        b.addBody("imgUrl", imgUrl);
        b.addBody("deviceInfo", model);
        b.addBody("deviceVersion", osVersion);
        b.addBody("appVersion", appVersion);

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.FEEDBACK;
        d.flag = AppConst.FEEDBACK;
        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        if (request.flag == AppConst.UPLOAD) {
            removeProgressDialog();
            if (StringUtil.notEmpty(bgPath)) {
                ContentResolver resolver = getContentResolver();
                Bitmap bm = null;
                try {
                    bm = MediaStore.Images.Media.getBitmap(resolver, Uri.fromFile(new File(bgPath)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                iv_picture.setVisibility(View.VISIBLE);
                iv_picture.setImageBitmap(bm);
                if (response.body != null) {
                    imgUrl = response.body.toString();
                }
            }
        } else if (request.flag == AppConst.FEEDBACK) {
            CustomToast.show("提交成功", CustomToast.LENGTH_SHORT);
            this.finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == PermissionsDef.PERMISSIONS_GRANTED
                && requestCode == PermissionsDef.PERMISSION_REQ_CODE) {
            showDialDialog(mCallNumber);
            return;
        }
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
                    CustomToast.show("获取图片失败", CustomToast.LENGTH_SHORT);
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
}
