package com.huicheng.hotel.android.ui.activity;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.net.bean.AssessOrderDetailInfoBean;
import com.huicheng.hotel.android.net.bean.AssessOrderInfoBean;
import com.huicheng.hotel.android.net.bean.HotelDetailInfoBean;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.CustomRatingBar;
import com.huicheng.hotel.android.ui.custom.RoundedAllImageView;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.ThumbnailUtil;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.CustomToast;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author kborid
 * @date 2016/12/22 0022
 */
public class AssessOrderDetailActivity extends BaseActivity {

    private final String TAG = getClass().getSimpleName();

    private LinearLayout root_lay;
    private RoundedAllImageView iv_background;
    private TextView tv_hotel_name, tv_time, tv_point, tv_comment;
    private LinearLayout point_lay;
    private EditText et_content;
    private ImageView iv_picture;
    private ImageView iv_screen;
    private CustomRatingBar ratingBar;
    private TagFlowLayout flowlayout;
    private TextView tv_public;

    private List<String> tagList = new ArrayList<>();
    private Set<Integer> mTagSet = new HashSet<>();
    private boolean isAssessed = false;
    private AssessOrderInfoBean orderBean = null;
    private AssessOrderDetailInfoBean orderDetailBean = null;
    private HotelDetailInfoBean hotelDetailBean = null;
    private String bgPath = null;
    private String imgUrl = null;
    private int selectedIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_assessorderdetail_layout);
        getWindow().getDecorView().setVisibility(View.GONE);
        initViews();
        initParams();
        initListeners();
        requestHotelDetailInfo();
    }

    @Override
    public void initViews() {
        super.initViews();
        root_lay = (LinearLayout) findViewById(R.id.root_lay);
        iv_background = (RoundedAllImageView) findViewById(R.id.iv_background);
        tv_hotel_name = (TextView) findViewById(R.id.tv_hotel_name);
        tv_time = (TextView) findViewById(R.id.tv_time);
        tv_point = (TextView) findViewById(R.id.tv_point);
        tv_comment = (TextView) findViewById(R.id.tv_comment);
        point_lay = (LinearLayout) findViewById(R.id.point_lay);
        et_content = (EditText) findViewById(R.id.et_content);
        iv_picture = (ImageView) findViewById(R.id.iv_picture);
        iv_screen = (ImageView) findViewById(R.id.iv_screen);

        ratingBar = (CustomRatingBar) findViewById(R.id.ratingbar);
        ratingBar.setIntegerMark(true);
        flowlayout = (TagFlowLayout) findViewById(R.id.flowlayout);
        tv_public = (TextView) findViewById(R.id.tv_public);
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.getSerializable("order") != null) {
            orderBean = (AssessOrderInfoBean) bundle.getSerializable("order");
            isAssessed = "1".equals(orderBean.isevaluated);
        } else {
            orderBean = new AssessOrderInfoBean();
        }
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText(orderBean.hotelName);
        tv_hotel_name.setText(orderBean.hotelName + "(" + orderBean.cityName + ")");
        String start = DateUtil.getDay("yyyy年MM月dd日", orderBean.startTime);
        String end = DateUtil.getDay("dd日", orderBean.endTime);
        tv_time.setText(start + "-" + end);
        float grade = 0;
        try {
            grade = Float.parseFloat(orderBean.grade);
        } catch (Exception e) {
            e.printStackTrace();
        }
        tv_point.setText(String.valueOf(grade));
        tv_comment.setText(orderBean.specialComment);

        ratingBar.setStarMark(0.0f);

        tagList.addAll(Arrays.asList(getResources().getStringArray(R.array.TravelType)));
        flowlayout.setAdapter(new TagAdapter<String>(tagList) {
            @Override
            public View getView(FlowLayout parent, int position, String o) {
                TextView tv_tag = (TextView) LayoutInflater.from(AssessOrderDetailActivity.this).inflate(R.layout.lv_tag_item, flowlayout, false);
                tv_tag.setText(o);
                return tv_tag;
            }
        });
        flowlayout.setMaxSelectCount(1);
    }

    private void requestHotelDetailInfo() {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("beginDate", String.valueOf(orderBean.startTime));
        b.addBody("endDate", String.valueOf(orderBean.endTime));
        b.addBody("type", String.valueOf(1));
        b.addBody("hotelId", String.valueOf(orderBean.hotelId));

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.HOTEL_DETAIL;
        d.flag = AppConst.HOTEL_DETAIL;
        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void refreshOrderInfo() {
        if (orderDetailBean != null && hotelDetailBean != null) {
            if (hotelDetailBean.picPath != null) {
                iv_background.setVisibility(View.VISIBLE);
                loadImage(iv_background, R.drawable.def_hotel_banner, hotelDetailBean.picPath.get(0), 1920, 1080);
            } else {
                iv_background.setVisibility(View.GONE);
            }
            tv_comment.setText(hotelDetailBean.evaluateCount + "条");
            if (isAssessed) {
                ratingBar.setStarMark(orderDetailBean.grade);
                ratingBar.setCanTouch(false);
                if (StringUtil.notEmpty(orderDetailBean.traveltype)) {
                    mTagSet.add(Integer.valueOf(orderDetailBean.traveltype));
                    flowlayout.getAdapter().setSelectedList(mTagSet);
                }
                flowlayout.setEnabled(false);
                et_content.setText(orderDetailBean.cotent);
                et_content.setEnabled(false);
                if (StringUtil.notEmpty(orderDetailBean.imgUrl)) {
                    iv_picture.setVisibility(View.VISIBLE);
                    loadImage(iv_picture, orderDetailBean.imgUrl, 800, 600);
                } else {
                    iv_picture.setVisibility(View.GONE);
                }
                iv_picture.setEnabled(false);
                iv_screen.setEnabled(false);
                tv_public.setEnabled(false);
            }
        }
        root_lay.setVisibility(View.VISIBLE);
    }

    private void requestPublicComment() {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("imgUrl", imgUrl);
        b.addBody("orderId", String.valueOf(orderBean.id));
        b.addBody("content", et_content.getText().toString());
        b.addBody("traveltype", HotelCommDef.convertTravelType(selectedIndex));
        b.addBody("grade", String.valueOf((int) ratingBar.getStarMark()));
        b.addBody("hotelId", String.valueOf(orderBean.hotelId));

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.ASSESS_PUBLIC;
        d.flag = AppConst.ASSESS_PUBLIC;
        if (!isProgressShowing()) {
            showProgressDialog(this);
        }

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    @Override
    public void initListeners() {
        super.initListeners();
        point_lay.setOnClickListener(this);
        flowlayout.setOnSelectListener(new TagFlowLayout.OnSelectListener() {
            @Override
            public void onSelected(Set<Integer> selectPosSet) {
                LogUtil.i(TAG, "position = " + selectPosSet.toString());
                selectedIndex = selectPosSet.iterator().next();
                LogUtil.i(TAG, "selectedIndex = " + selectedIndex);
            }
        });
        iv_screen.setOnClickListener(this);
        iv_picture.setOnClickListener(this);
        tv_public.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.point_lay:
                Intent intent = new Intent(this, AssessCommendActivity.class);
                intent.putExtra("hotelId", hotelDetailBean.id);
                startActivity(intent);
                break;
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
            case R.id.tv_public:
                requestPublicComment();
                break;
            default:
                break;
        }
    }

    private void requestOrderDetail() {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("evaluateId", String.valueOf(orderBean.evaluateid));

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.ASSESS_DETAIL;
        d.flag = AppConst.ASSESS_DETAIL;

        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void choosePictureDialog() {
        if (!SessionContext.isLogin()) {
            sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION));
            return;
        }
        CustomDialog mDialog = new CustomDialog(this);
        mDialog.setMessage("请选择图片获取方式");
        mDialog.setPositiveButton("图库", new DialogInterface.OnClickListener() {
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
        mDialog.setNegativeButton("相机", new DialogInterface.OnClickListener() {
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
        mDialog.setCanceledOnTouchOutside(true);

        mDialog.show();
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
            if (request.flag == AppConst.ASSESS_DETAIL) {
                removeProgressDialog();
                LogUtil.i(TAG, "json = " + response.body.toString());
                orderDetailBean = JSON.parseObject(response.body.toString(), AssessOrderDetailInfoBean.class);
                refreshOrderInfo();
            } else if (request.flag == AppConst.HOTEL_DETAIL) {
                LogUtil.i(TAG, "json = " + response.body.toString());
                hotelDetailBean = JSON.parseObject(response.body.toString(), HotelDetailInfoBean.class);
                if (hotelDetailBean != null) {
                    requestOrderDetail();
                } else {
                    removeProgressDialog();
                }
            } else if (request.flag == AppConst.UPLOAD) {
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
            } else if (request.flag == AppConst.ASSESS_PUBLIC) {
                removeProgressDialog();
                CustomToast.show("发表成功", CustomToast.LENGTH_SHORT);
                setResult(RESULT_OK);
                finish();
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
