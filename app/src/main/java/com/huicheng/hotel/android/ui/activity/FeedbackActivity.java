package com.huicheng.hotel.android.ui.activity;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.huicheng.hotel.android.BuildConfig;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.ThumbnailUtil;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.CustomToast;

import java.io.File;

/**
 * 意见反馈
 */
public class FeedbackActivity extends BaseActivity implements DataCallback, DialogInterface.OnCancelListener {

    private final String TAG = getClass().getSimpleName();
    private EditText et_type, et_content;

    private ImageView iv_picture, iv_screen;
    private TextView tv_model, tv_os, tv_version;
    private Button btn_submit;
    private String bgPath = null;
    private String imgUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_feedback_layout);
        initViews();
        initParams();
        initListeners();
    }

    @Override
    public void initViews() {
        super.initViews();
        et_type = (EditText) findViewById(R.id.et_type);
        et_content = (EditText) findViewById(R.id.et_content);
        iv_picture = (ImageView) findViewById(R.id.iv_picture);
        iv_screen = (ImageView) findViewById(R.id.iv_screen);
        btn_submit = (Button) findViewById(R.id.btn_submit);

        tv_model = (TextView) findViewById(R.id.tv_model);
        tv_os = (TextView) findViewById(R.id.tv_os);
        tv_version = (TextView) findViewById(R.id.tv_version);
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText(getString(R.string.side_fd));
        tv_model.setText(Build.MODEL);
        tv_os.setText("Android" + Build.VERSION.RELEASE);
        tv_version.setText(BuildConfig.VERSION_NAME);
    }

    @Override
    public void initListeners() {
        super.initListeners();
        iv_screen.setOnClickListener(this);
        iv_picture.setOnClickListener(this);
        btn_submit.setOnClickListener(this);
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
                if (StringUtil.isEmpty(et_type.getText().toString())) {
                    CustomToast.show("请输入反馈类型", CustomToast.LENGTH_SHORT);
                    return;
                }
                if (StringUtil.isEmpty(et_content.getText().toString())) {
                    CustomToast.show("请输入反馈描述", CustomToast.LENGTH_SHORT);
                    return;
                }
                requestSubmit();
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

    private void requestSubmit() {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("title", et_type.getText().toString());
        b.addBody("content", et_content.getText().toString());
        b.addBody("imgUrl", imgUrl);
        b.addBody("deviceInfo", tv_model.getText().toString());
        b.addBody("deviceVersion", tv_os.getText().toString());
        b.addBody("appVersion", tv_version.getText().toString());

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.FEEDBACK;
        d.flag = AppConst.FEEDBACK;
        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    @Override
    public void preExecute(ResponseData request) {
    }

    @Override
    public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
        if (request.flag == AppConst.UPLOAD) {
            removeProgressDialog();
            if (StringUtil.notEmpty(bgPath)) {
                ContentResolver resolver = getContentResolver();
                Bitmap bm = MediaStore.Images.Media.getBitmap(resolver, Uri.fromFile(new File(bgPath)));
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
    public void onCancel(DialogInterface dialog) {
        DataLoader.getInstance().clear(requestID);
        removeProgressDialog();
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
                    uploadImage(new File(ThumbnailUtil.compressImage(filePath, bgPath)));
                } else {
                    CustomToast.show("获取图片失败", 0);
                }
                break;
            case AppConst.ACTIVITY_IMAGE_CAPTURE:
                filePath = Utils.getFolderDir("pic") + "_temp.jpg";
                bgPath = Utils.getFolderDir("pic") + "_temp_compress.jpg";
                uploadImage(new File(ThumbnailUtil.compressImage(filePath, bgPath)));
                break;
            default:
                break;
        }
    }
}
