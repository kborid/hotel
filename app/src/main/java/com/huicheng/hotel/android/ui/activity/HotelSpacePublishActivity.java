
package com.huicheng.hotel.android.ui.activity;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.net.bean.HotelSpaceTieCommentInfoBean;
import com.huicheng.hotel.android.net.bean.HotelSpaceTieInfoBean;
import com.huicheng.hotel.android.ui.adapter.PersonInfo;
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
import java.util.HashMap;
import java.util.Map;

/**
 * @author kborid
 * @date 2017/3/21 0021
 */
public class HotelSpacePublishActivity extends BaseActivity {
    private static final String TAG = "HotelSpacePublishActivity";

    private TextView tv_left, tv_right, tv_title;
    private ImageView iv_upload_pic, iv_picture;
    private EditText et_input;
    private String bgPath = null;
    private String imgUrl = null;

    private String replyStr = "";
    private HotelSpaceTieInfoBean tieInfoBean = null;
    private HotelSpaceTieCommentInfoBean tieCommentInfoBean = null;
    private int hotelId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_spacepublish_layout);
        initViews();
        initParams();
        initListeners();
    }

    @Override
    public void initViews() {
        super.initViews();
        tv_left = (TextView) findViewById(R.id.tv_left);
        tv_right = (TextView) findViewById(R.id.tv_right);
        tv_title = (TextView) findViewById(R.id.tv_title);
        et_input = (EditText) findViewById(R.id.et_input);
        iv_upload_pic = (ImageView) findViewById(R.id.iv_upload_pic);
        iv_picture = (ImageView) findViewById(R.id.iv_picture);
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.getString("reply") != null) {
                replyStr = bundle.getString("reply");
                et_input.setText(replyStr);
                et_input.setSelection(replyStr.length());
            }

            if (bundle.getSerializable("tieDetail") != null) {
                tieInfoBean = (HotelSpaceTieInfoBean) bundle.getSerializable("tieDetail");
            }
            if (bundle.getSerializable("replyDetail") != null) {
                tieCommentInfoBean = (HotelSpaceTieCommentInfoBean) bundle.getSerializable("replyDetail");
            }
            hotelId = bundle.getInt("hotelId");
        }
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_left.setText("取消");
        tv_right.setText("发送");
        tv_title.setText("评论");
    }

    @Override
    public void initListeners() {
        super.initListeners();
        tv_left.setOnClickListener(this);
        tv_right.setOnClickListener(this);
        iv_picture.setOnClickListener(this);
        et_input.setFilters(new InputFilter[]{new MyInputFilter()});
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.tv_left:
                this.finish();
                break;
            case R.id.tv_right:
                LogUtil.i(TAG, "public button onClick");
                LogUtil.i(TAG, et_input.getText().toString());
                requestPublishComment();
                break;
            case R.id.iv_picture:
                choosePictureDialog();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    private void requestPublishComment() {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("hotelId", String.valueOf(hotelId));
        b.addBody("articleId", tieInfoBean != null ? String.valueOf(tieInfoBean.id) : String.valueOf(tieCommentInfoBean.articleId));
        b.addBody("beRepliedUserType", tieInfoBean != null ? "" : String.valueOf(tieCommentInfoBean.replyUserType));
        b.addBody("beRepliedUserId", tieInfoBean != null ? "" : tieCommentInfoBean.replyUserId);
        b.addBody("beRepliedUserName", tieInfoBean != null ? "" : tieCommentInfoBean.replyUserName);
        b.addBody("pid", tieInfoBean != null ? "-1" : String.valueOf(tieCommentInfoBean.id));
        b.addBody("content", et_input.getText().toString().substring(replyStr.length(), et_input.getText().toString().length()));
        b.addBody("atGroup", selectedCids);
        b.addBody("picurl", imgUrl);

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.PUBLIC_COMMENT;
        d.flag = AppConst.PUBLIC_COMMENT;

        if (!isProgressShowing()) {
            showProgressDialog(this);
        }

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    @Override
    protected void onPause() {
        super.onPause();
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
                if (StringUtil.notEmpty(bgPath)) {
                    ContentResolver resolver = getContentResolver();
                    Bitmap bm = null;
                    try {
                        bm = MediaStore.Images.Media.getBitmap(resolver, Uri.fromFile(new File(bgPath)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    iv_upload_pic.setVisibility(View.VISIBLE);
                    iv_upload_pic.setImageBitmap(bm);
                    if (response.body != null) {
                        imgUrl = response.body.toString();
                    }
                }
            } else if (request.flag == AppConst.PUBLIC_COMMENT) {
                removeProgressDialog();
                LogUtil.i(TAG, "json = " + response.body.toString());
                CustomToast.show("发表成功", CustomToast.LENGTH_SHORT);
                setResult(RESULT_OK);
                finish();
            }
        }
    }

    /**
     * 识别输入框的是不是@符号
     */
    private class MyInputFilter implements InputFilter {

        @Override
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            if (source.toString().equalsIgnoreCase("@")
                    || source.toString().equalsIgnoreCase("＠")) {
                goAt();
            }

            return source;
        }
    }

    private void goAt() {
        StringBuilder tmp = new StringBuilder();
        // 把选中人的id已空格分隔，拼接成字符串
        for (Map.Entry<String, String> entry : cidNameMap.entrySet()) {
            tmp.append(entry.getKey()).append(" ");
        }
        Intent intent = new Intent(this, AttendPersonActivity.class);
        intent.putExtra(AttendPersonActivity.KEY_SELECTED, tmp.toString());
        startActivityForResult(intent, CODE_PERSON);
    }

    private static final int CODE_PERSON = 0x11;
    /**
     * 存储@的cid、name对
     */
    private Map<String, String> cidNameMap = new HashMap<String, String>();

    /**
     * 选中的@的人的cid,进入@列表时，需要传递过去
     */
    private String selectedCids;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        String filePath = null;
        switch (requestCode) {
            case CODE_PERSON:
                PersonInfo personInfo = (PersonInfo) data.getSerializableExtra("person");
                String tmpCidStr = personInfo.attentuserid + ",";
                String tmpNameStr = "@" + personInfo.attentusername + " ";
//                String tmpCidStr = data.getStringExtra(AttendPersonActivity.KEY_CID);
//                String tmpNameStr = data.getStringExtra(AttendPersonActivity.KEY_NAME);

//                String[] tmpCids = tmpCidStr.split(" ");
//                String[] tmpNames = tmpNameStr.split(" ");

//                if (tmpCids != null && tmpCids.length > 0) {
//                    for (int i = 0; i < tmpCids.length; i++) {
//                        if (tmpNames.length > i) {
//                            cidNameMap.put(tmpCids[i], tmpNames[i]);
//                        }
//                    }
//                }
                cidNameMap.put(tmpCidStr, tmpNameStr);

                if (selectedCids == null) {
                    selectedCids = tmpCidStr;
                } else {
                    selectedCids = selectedCids + tmpCidStr;
                }

                if (nameStr == null) {
                    nameStr = tmpNameStr;

                } else {
                    nameStr = nameStr + tmpNameStr;

                }
                lastNameStr = tmpNameStr;

                // 获取光标当前位置
                int curIndex = et_input.getSelectionStart();

                // 把要@的人插入光标所在位置
                et_input.getText().insert(curIndex, lastNameStr);
                // 通过输入@符号进入好友列表并返回@的人，要删除之前输入的@
                if (curIndex >= 1) {
                    et_input.getText().replace(curIndex - 1, curIndex, "");
                }
                setAtImageSpan(nameStr);
                LogUtil.i(TAG, "name str = " + nameStr);
                LogUtil.i(TAG, "id str = " + selectedCids);
                break;
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

    /**
     * 返回的所有的用户名,用于识别输入框中的所有要@的人
     * <p/>
     * 如果用户删除过，会出现不匹配的情况，需要在for循环中做处理
     */
    private String nameStr;

    /**
     * 上一次返回的用户名，用于把要@的用户名拼接到输入框中
     */
    private String lastNameStr;

    private void setAtImageSpan(String nameStr) {

        String content = String.valueOf(et_input.getText());
        if (content.endsWith("@") || content.endsWith("＠")) {
            content = content.substring(0, content.length() - 1);
        }
        String tmp = content;
        SpannableString ss = new SpannableString(tmp);
        if (nameStr != null) {
            String[] names = nameStr.split(" ");
            if (names != null && names.length > 0) {
                for (String name : names) {
                    if (name != null && name.trim().length() > 0) {
                        final Bitmap bmp = getNameBitmap(name);

                        // 这里会出现删除过的用户，需要做判断，过滤掉
                        if (tmp.contains(name) && (tmp.indexOf(name) + name.length()) <= tmp.length()) {

                            // 把取到的要@的人名，用DynamicDrawableSpan代替
                            ss.setSpan(
                                    new DynamicDrawableSpan(DynamicDrawableSpan.ALIGN_BASELINE) {
                                        @Override
                                        public Drawable getDrawable() {
                                            BitmapDrawable drawable = new BitmapDrawable(getResources(), bmp);
                                            drawable.setBounds(0, 0, bmp.getWidth(), bmp.getHeight());
                                            return drawable;
                                        }
                                    }, tmp.indexOf(name),
                                    tmp.indexOf(name) + name.length(),
                                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }
                }
            }
        }
        et_input.setTextKeepState(ss);
    }

    /**
     * 把返回的人名，转换成bitmap
     *
     * @param name
     * @return
     */
    private Bitmap getNameBitmap(String name) {
        /* 把@相关的字符串转换成bitmap 然后使用DynamicDrawableSpan加入输入框中 */
        name = "" + name;
        Paint paint = new Paint();
        paint.setColor(getResources().getColor(R.color.secColor));
        paint.setFakeBoldText(true);
        paint.setAntiAlias(true);
        paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
        Rect targetRect = new Rect();
        paint.getTextBounds(name, 0, name.length(), targetRect);

        // 获取字符串在屏幕上的长度
        int width = (int) (paint.measureText(name));
        final Bitmap bmp = Bitmap.createBitmap(width, targetRect.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        canvas.drawText(name, targetRect.left, targetRect.height() - targetRect.bottom, paint);
        return bmp;
    }
}
