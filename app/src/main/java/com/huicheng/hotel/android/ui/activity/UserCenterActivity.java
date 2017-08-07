package com.huicheng.hotel.android.ui.activity;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.net.bean.UserInfo;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.CustomRatingBar;
import com.huicheng.hotel.android.ui.custom.RangeSeekBar;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.ThumbnailUtil;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.CustomToast;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author kborid
 * @date 2016/11/30 0030
 */
public class UserCenterActivity extends BaseActivity implements DataCallback {

    private static final String TAG = "UserCenterActivity";

    private Calendar calendar = Calendar.getInstance();
    private boolean isEdited = false;
    private ScrollView scroll_view;
    private Button btn_setting;
    private RelativeLayout camer_lay;
    private RelativeLayout photo_lay;
    private EditText et_name;
    private ImageView iv_clear, iv_left;

    private LinearLayout male_lay, female_lay;
    private View line_lay, line_male_lay, line_female_lay;

    private TextView tv_phone, tv_birthday;
    private int mYear, mMonth, mDay;

    private float maxValue, minValue;
    private RangeSeekBar seekbar;

    private CustomRatingBar assess_ratingbar, grade_ratingbar;
    private float assessValue, gradeValue;

    private TagFlowLayout flowlayout;
    private List<String> tagOrgList = new ArrayList<>();
    private List<String> tagList = new ArrayList<>();
    private List<String> tagListSel = new ArrayList<>();
    private Set<Integer> mTagSet = new HashSet<>();
    private String mTagSelStr = null;

    private String phoneNumber, code;
    private Button btn_yzm;
    private CountDownTimer mCountDownTimer;
    private boolean isValid = false;
    private CustomDialog dialog;

    private Switch btn_switch;

    private String bgPath = null;

    private static Handler handler = new Handler(Looper.getMainLooper());
    private int oldSkinIndex = 0, scrollY;
    private int lineSelectedColorId, lineSelectedDisableColorId;
    private int thumbId, thumbDisableId, settingId, settingOkId, leftImageId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("onCreate()");
        setContentView(R.layout.act_usercenter_layout);

        TypedArray ta = obtainStyledAttributes(R.styleable.MyTheme);
        lineSelectedColorId = ta.getInt(R.styleable.MyTheme_userCenterThumbText, getResources().getColor(R.color.mainColor));
        lineSelectedDisableColorId = ta.getInt(R.styleable.MyTheme_userCenterThumbTextDisable, getResources().getColor(R.color.indicatorColor));
        thumbId = ta.getResourceId(R.styleable.MyTheme_userCenterThumb, R.drawable.iv_thumb);
        thumbDisableId = ta.getResourceId(R.styleable.MyTheme_userCenterThumbDisable, R.drawable.iv_thumb_yello);
        settingId = ta.getResourceId(R.styleable.MyTheme_settingButton, R.drawable.iv_setting);
        settingOkId = ta.getResourceId(R.styleable.MyTheme_settingOKButton, R.drawable.iv_setting_ok);
        leftImageId = ta.getResourceId(R.styleable.MyTheme_leftImage, R.drawable.iv_left);
        ta.recycle();

        initViews();
        initParams();
        initListeners();
    }

    @Override
    public void initViews() {
        super.initViews();
        scroll_view = (ScrollView) findViewById(R.id.scroll_view);
        btn_setting = (Button) findViewById(R.id.btn_setting);
        camer_lay = (RelativeLayout) findViewById(R.id.camer_lay);
        photo_lay = (RelativeLayout) findViewById(R.id.photo_lay);
        et_name = (EditText) findViewById(R.id.et_name);

        male_lay = (LinearLayout) findViewById(R.id.male_lay);
        female_lay = (LinearLayout) findViewById(R.id.female_lay);
        line_male_lay = findViewById(R.id.line_male_lay);
        line_female_lay = findViewById(R.id.line_female_lay);

        iv_clear = (ImageView) findViewById(R.id.iv_clear);
        line_lay = findViewById(R.id.line_lay);
        iv_left = (ImageView) findViewById(R.id.iv_left);
        iv_left.setImageResource(leftImageId);

        tv_phone = (TextView) findViewById(R.id.tv_phone);
        tv_birthday = (TextView) findViewById(R.id.tv_birthday);

        seekbar = (RangeSeekBar) findViewById(R.id.seekbar);

        assess_ratingbar = (CustomRatingBar) findViewById(R.id.assess_ratingbar);
        assess_ratingbar.setIntegerMark(true);
        grade_ratingbar = (CustomRatingBar) findViewById(R.id.grade_ratingbar);
        grade_ratingbar.setIntegerMark(true);

        flowlayout = (TagFlowLayout) findViewById(R.id.flowlayout);

        btn_switch = (Switch) findViewById(R.id.btn_switch);
        if (btn_switch != null) {
            btn_switch.setChecked(false);
        }
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            scrollY = bundle.getInt("scrollY");
        }
    }

    @Override
    public void initParams() {
        super.initParams();
        btn_back.setImageResource(R.drawable.iv_back_white);
        setCountDownTimer(60 * 1000, 1000);
        scroll_view.getBackground().mutate().setAlpha((int) (0.8 * 255));

        oldSkinIndex = SharedPreferenceUtil.getInstance().getInt(AppConst.SKIN_INDEX, 0);
        //更新个人中心信息
        // 设置头像
        String photoUrl = SessionContext.mUser.user.headphotourl;
        if (StringUtil.notEmpty(photoUrl)) {
            loadImage(photo_lay, R.drawable.def_photo_bg, photoUrl, 1920, 1080);
        } else {
            photo_lay.setBackgroundResource(R.drawable.def_photo_bg);
        }

        // 设置名字
        if (StringUtil.notEmpty(SessionContext.mUser.user.nickname)) {
            et_name.setText(SessionContext.mUser.user.nickname);
        } else {
            et_name.setText(SessionContext.mUser.user.username);
        }
        et_name.setSelection(et_name.getText().length());
        // 设置性别
        if (StringUtil.notEmpty(SessionContext.mUser.user.sex)) {
            if ("1".equals(SessionContext.mUser.user.sex)) {
                male_lay.setSelected(true);
                female_lay.setSelected(false);
            } else {
                male_lay.setSelected(false);
                female_lay.setSelected(true);
            }
        } else {
            male_lay.setSelected(true);
            female_lay.setSelected(false);
        }
        // 设置手机号码
        if (StringUtil.notEmpty(SessionContext.mUser.user.mobile)) {
            tv_phone.setText(SessionContext.mUser.user.mobile);
        }

        // 设置生日

        System.out.println("birthday = " + SessionContext.mUser.user.birthdate);
        if (StringUtil.notEmpty(SessionContext.mUser.user.birthdate)) {
            String[] birth = SessionContext.mUser.user.birthdate.split(" ");
            if (birth.length >= 3) {
                try {
                    mYear = Integer.valueOf(birth[0]);
                    mMonth = Integer.valueOf(birth[1]);
                    mDay = Integer.valueOf(birth[2]);
                    tv_birthday.setText(mYear + "年" + mMonth + "月" + mDay + "日");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                mYear = calendar.get(Calendar.YEAR);
                mMonth = calendar.get(Calendar.MONTH) + 1;
                mDay = calendar.get(Calendar.DAY_OF_MONTH);
            }
        } else {
            mYear = calendar.get(Calendar.YEAR);
            mMonth = calendar.get(Calendar.MONTH) + 1;
            mDay = calendar.get(Calendar.DAY_OF_MONTH);
        }

        // 设置用户设定信息
        if (SessionContext.mUser.up != null) {
            if (StringUtil.notEmpty(SessionContext.mUser.up.isstartup)) {
                if ("0".equals(SessionContext.mUser.up.isstartup)) {
                    btn_switch.setChecked(false);
                } else {
                    btn_switch.setChecked(true);
                }
            }

            if (SessionContext.mUser.up.minprice != SessionContext.mUser.up.maxprice) {
                minValue = priceConvertValue(SessionContext.mUser.up.minprice);
                maxValue = priceConvertValue(SessionContext.mUser.up.maxprice);
            } else {
                minValue = 0f;
                maxValue = 5f;
            }
            SharedPreferenceUtil.getInstance().setFloat("range_min", minValue);
            SharedPreferenceUtil.getInstance().setFloat("range_max", maxValue);
            seekbar.setValue(minValue, maxValue);

            if (StringUtil.notEmpty(SessionContext.mUser.up.evalutegrade)) {
                try {
                    assessValue = Float.valueOf(SessionContext.mUser.up.evalutegrade);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                assess_ratingbar.setStarMark(assessValue);
            } else {
                assess_ratingbar.setStarMark(0);
            }
            if (StringUtil.notEmpty(SessionContext.mUser.up.stargrade)) {
                try {
                    gradeValue = Float.valueOf(SessionContext.mUser.up.stargrade);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                grade_ratingbar.setStarMark(gradeValue);
            } else {
                grade_ratingbar.setStarMark(0);
            }

            if (StringUtil.notEmpty(SessionContext.mUser.up.preferfacilities)) {
                String[] prefer = SessionContext.mUser.up.preferfacilities.split(",");
                tagOrgList.clear();
                tagList.clear();
                for (String aPrefer : prefer) {
                    if (StringUtil.notEmpty(aPrefer)) {
                        tagOrgList.add(aPrefer);
                        String[] item = aPrefer.split("\\|");
                        tagList.add(item[2]);
                    }
                }

                String[] preferSelected = SessionContext.mUser.up.preferfacilitieschoosen.split(",");
                tagListSel.clear();
                for (String aPrefer : preferSelected) {
                    if (StringUtil.notEmpty(aPrefer)) {
                        String[] item = aPrefer.split("\\|");
                        tagListSel.add(item[2]);
                    }
                }

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < tagList.size(); i++) {
                    for (int j = 0; j < tagListSel.size(); j++) {
                        if (tagList.get(i).equals(tagListSel.get(j)) || tagList.get(i).contains(tagListSel.get(j))) {
                            mTagSet.add(i);
                            sb.append(tagOrgList.get(i));
                            sb.append(",");
                        }
                    }
                }
                mTagSelStr = sb.toString();
            }
        }
        flowlayout.setAdapter(new TagAdapter<String>(tagList) {
            @Override
            public View getView(FlowLayout parent, int position, String o) {
                TextView tv_tag = (TextView) LayoutInflater.from(UserCenterActivity.this).inflate(R.layout.lv_tag_item, flowlayout, false);
                tv_tag.setText(o);
                return tv_tag;
            }
        });
        flowlayout.getAdapter().setSelectedList(mTagSet);

        changeEditedStatus(isEdited);
    }

    /**
     * 设置倒计时
     */
    private void setCountDownTimer(long millisInFuture, long countDownInterval) {
        mCountDownTimer = new CountDownTimer(millisInFuture, countDownInterval) {

            @Override
            public void onTick(long millisUntilFinished) {
                btn_yzm.setText(getString(R.string.get_checknumber) + "(" + millisUntilFinished / 1000 + "s)");
            }

            @Override
            public void onFinish() {
                btn_yzm.setEnabled(true);
                btn_yzm.setText(R.string.get_checknumber);
            }
        };
    }

    @Override
    public void initListeners() {
        super.initListeners();
        btn_setting.setOnClickListener(this);
        camer_lay.setOnClickListener(this);
        iv_clear.setOnClickListener(this);
        male_lay.setOnClickListener(this);
        female_lay.setOnClickListener(this);
        tv_phone.setOnClickListener(this);
        tv_birthday.setOnClickListener(this);
        seekbar.setOnRangeChangedListener(new RangeSeekBar.OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float min, float max) {
                minValue = min;
                maxValue = max;
            }
        });

        assess_ratingbar.setOnStarChangeListener(new CustomRatingBar.OnStarChangeListener() {
            @Override
            public void onStarChange(float mark) {
                assessValue = mark;
            }
        });

        grade_ratingbar.setOnStarChangeListener(new CustomRatingBar.OnStarChangeListener() {
            @Override
            public void onStarChange(float mark) {
                gradeValue = mark;
            }
        });

        flowlayout.setOnSelectListener(new TagFlowLayout.OnSelectListener() {
            @Override
            public void onSelected(Set<Integer> selectPosSet) {
                mTagSelStr = "";
                StringBuilder sb = new StringBuilder();
                for (Integer index : selectPosSet) {
                    sb.append(tagOrgList.get(index));
                    sb.append(",");
                }
                mTagSelStr = sb.toString();
            }
        });
        btn_switch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return !isEdited;
            }
        });
    }

    public void chooseImage() {
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

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btn_back:
                startActivity(new Intent(this, MainFragmentActivity.class));
//                finish();
                break;
            case R.id.camer_lay:
                if (!isEdited) {
                    return;
                }
                chooseImage();
                break;
            case R.id.iv_clear:
                if (!isEdited) {
                    return;
                }
                et_name.setText("");
                break;
            case R.id.male_lay:
                if (!isEdited) {
                    return;
                }
                male_lay.setSelected(true);
                female_lay.setSelected(false);
                refreshSexStatus();
                break;
            case R.id.female_lay:
                if (!isEdited) {
                    return;
                }
                male_lay.setSelected(false);
                female_lay.setSelected(true);
                refreshSexStatus();
                break;
            case R.id.btn_setting:
                isEdited = !isEdited;
                if (!isEdited) {
                    SharedPreferenceUtil.getInstance().setFloat("range_min", minValue);
                    SharedPreferenceUtil.getInstance().setFloat("range_max", maxValue);
                    requestSaveUserPerferSetting();
                }
                changeEditedStatus(isEdited);
                break;
            case R.id.tv_phone:
                if (isEdited) {
                    dialog = new CustomDialog(this);
                    View view = LayoutInflater.from(this).inflate(R.layout.dialog_modify_phone_layout, null);
                    final EditText et_phone = (EditText) view.findViewById(R.id.et_phone);
                    final EditText et_yzm = (EditText) view.findViewById(R.id.et_yzm);
                    btn_yzm = (Button) view.findViewById(R.id.btn_yzm);
                    btn_yzm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (StringUtil.isEmpty(et_phone.getText().toString())) {
                                CustomToast.show("请输入手机号码", CustomToast.LENGTH_SHORT);
                                return;
                            } else {
                                if (!Utils.isMobile(et_phone.getText().toString())) {
                                    CustomToast.show("请输入正确的手机号码", CustomToast.LENGTH_SHORT);
                                    return;
                                }
                            }
                            phoneNumber = et_phone.getText().toString();
                            requestCheckPhoneNumber();
                        }
                    });

                    dialog.addView(view);
                    dialog.setTitle("修改手机号码");

                    dialog.setPositiveButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    dialog.setNegativeButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (StringUtil.isEmpty(et_phone.getText().toString())) {
                                CustomToast.show("请输入手机号码", CustomToast.LENGTH_SHORT);
                                return;
                            } else {
                                if (!Utils.isMobile(et_phone.getText().toString())) {
                                    CustomToast.show("请输入正确的手机号码", CustomToast.LENGTH_SHORT);
                                    return;
                                }
                            }
                            if (StringUtil.isEmpty(et_yzm.getText().toString())) {
                                CustomToast.show("请输入验证码", CustomToast.LENGTH_SHORT);
                                return;
                            }
                            code = et_yzm.getText().toString();
                            requestCheckYZM();
                        }
                    });

                    dialog.setCanceledOnTouchOutside(true);
                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            mCountDownTimer.cancel();
                        }
                    });
                    dialog.show();
                }
                break;
            case R.id.tv_birthday:
                if (isEdited) {
                    new DatePickerDialog(UserCenterActivity.this, R.style.MyMaterialDialog,
                            mDateSetListener, mYear, mMonth - 1, mDay).show();
                }
                break;
            default:
                break;
        }
    }

    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear + 1;
            mDay = dayOfMonth;
            tv_birthday.setText(mYear + "年" + mMonth + "月" + mDay + "日");

        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            btn_back.performClick();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void requestSaveUserPerferSetting() {
        LogUtil.i(TAG, "requestSaveUserPerferSetting()");
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("sex", male_lay.isSelected() ? "1" : "0");
        b.addBody("nickname", et_name.getText().toString());
        b.addBody("headphotourl", SessionContext.mUser.user.headphotourl);
        b.addBody("birthdate", mYear + " " + mMonth + " " + mDay);
        b.addBody("isstartup", btn_switch.isChecked() ? "1" : "0");
        b.addBody("minprice", valueConvertPrice(minValue));
        b.addBody("maxprice", valueConvertPrice(maxValue));
        b.addBody("evalutegrade", String.valueOf((int) assessValue));
        b.addBody("stargrade", String.valueOf((int) gradeValue));
        b.addBody("preferfacilitieschoosen", mTagSelStr);

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.SAVE_USERINFO;
        d.flag = AppConst.SAVE_USERINFO;
        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    /**
     * 检测手机号是否已经注册
     */
    public void requestCheckPhoneNumber() {
        LogUtil.i(TAG, "requestCheckPhoneNumber()");
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("mobile", phoneNumber);

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.CHECK_PHONE;
        d.flag = AppConst.CHECK_PHONE;

        if (!isProgressShowing()) {
            showProgressDialog(this, getString(R.string.loading));
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    /**
     * 加载验证码
     */
    private void requestYZM() {
        LogUtil.d(TAG, "requestYZM()");
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("businesstype", AppConst.BUSINESS_TYPE_REGISTER);
        b.addBody("mobile", phoneNumber);
        b.addBody("smsparam", "code");

        ResponseData data = b.syncRequest(b);
        data.path = NetURL.GET_YZM;
        data.flag = AppConst.GET_YZM;

        requestID = DataLoader.getInstance().loadData(this, data);
    }

    /**
     * 校验验证码
     */
    private void requestCheckYZM() {
        LogUtil.i(TAG, "requestCheckYZM()");
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("businesstype", AppConst.BUSINESS_TYPE_CHANGEPHONE);
        b.addBody("mobile", phoneNumber);
        b.addBody("code", code);

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.CHECK_YZM;
        d.flag = AppConst.CHECK_YZM;

        if (!isProgressShowing()) {
            showProgressDialog(this);
        }

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void requestChangePhoneNumber() {
        LogUtil.i(TAG, "requestChangePhoneNumber()");
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("businesstype", AppConst.BUSINESS_TYPE_CHANGEPHONE);
        b.addBody("mobile", phoneNumber);
        b.addBody("code", code);
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.CHANGE_PHONENUMBER;
        d.flag = AppConst.CHANGE_PHONENUMBER;

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("onResume()");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                scroll_view.scrollTo(0, scrollY);
            }
        }, 10);

    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("onPause()");
        scrollY = scroll_view.getScrollY();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("onDestroy()");
        isEdited = false;
    }

    private void refreshSexStatus() {
        if (male_lay.isSelected()) {
            line_male_lay.setVisibility(View.VISIBLE);
            line_female_lay.setVisibility(View.INVISIBLE);
            SharedPreferenceUtil.getInstance().setInt(AppConst.SKIN_INDEX, 0);
        } else if (female_lay.isSelected()) {
            line_male_lay.setVisibility(View.INVISIBLE);
            line_female_lay.setVisibility(View.VISIBLE);
            SharedPreferenceUtil.getInstance().setInt(AppConst.SKIN_INDEX, 1);
        } else {
            LogUtil.d(TAG, "warning!!!");
        }
    }

    /**
     * 获取用户信息
     */
    private void reqeustGetUserInfo() {

        RequestBeanBuilder builder = RequestBeanBuilder.create(true);

        ResponseData data = builder.syncRequest(builder);
        data.path = NetURL.CENTER_USERINFO;
        data.flag = AppConst.CENTER_USERINFO;
        requestID = DataLoader.getInstance().loadData(this, data);
    }

    private void changeEditedStatus(boolean isflag) {
        if (isflag) {
            btn_setting.setBackgroundResource(settingOkId);
            camer_lay.setVisibility(View.VISIBLE);

            line_lay.setVisibility(View.VISIBLE);
            iv_clear.setVisibility(View.VISIBLE);
            et_name.setEnabled(true);

            if (male_lay.isShown()) {
                line_male_lay.setVisibility(View.VISIBLE);
                line_female_lay.setVisibility(View.INVISIBLE);
            } else {
                line_male_lay.setVisibility(View.INVISIBLE);
                line_female_lay.setVisibility(View.VISIBLE);
            }
            male_lay.setVisibility(View.VISIBLE);
            female_lay.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) female_lay.getLayoutParams();
            lp.leftMargin = Utils.dip2px(15);
            female_lay.setLayoutParams(lp);

            seekbar.setSeekBarResId(thumbId);
            seekbar.setColorLineEdge(lineSelectedColorId);
            seekbar.setColorLineSelected(lineSelectedColorId);
            seekbar.setCanTouch(true);

            assess_ratingbar.setStarEmptyDrawable(getResources().getDrawable(R.drawable.iv_active_star));
            assess_ratingbar.setStarFillDrawable(getResources().getDrawable(R.drawable.iv_active_star_selected));
            assess_ratingbar.setCanTouch(true);
            grade_ratingbar.setStarEmptyDrawable(getResources().getDrawable(R.drawable.iv_active_star));
            grade_ratingbar.setStarFillDrawable(getResources().getDrawable(R.drawable.iv_active_star_selected));
            grade_ratingbar.setCanTouch(true);

            flowlayout.setEnabled(true);
            changeTagFlowStatus(true);
        } else {
            btn_setting.setBackgroundResource(settingId);
            camer_lay.setVisibility(View.GONE);

            line_lay.setVisibility(View.INVISIBLE);
            iv_clear.setVisibility(View.GONE);
            et_name.setEnabled(false);

            if (male_lay.isSelected()) {
                male_lay.setVisibility(View.VISIBLE);
                female_lay.setVisibility(View.GONE);
            } else {
                male_lay.setVisibility(View.GONE);
                female_lay.setVisibility(View.VISIBLE);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) female_lay.getLayoutParams();
                lp.leftMargin = 0;
                female_lay.setLayoutParams(lp);
            }
            line_male_lay.setVisibility(View.INVISIBLE);
            line_female_lay.setVisibility(View.INVISIBLE);

            seekbar.setSeekBarResId(thumbDisableId);
            seekbar.setColorLineEdge(lineSelectedDisableColorId);
            seekbar.setColorLineSelected(lineSelectedDisableColorId);
            seekbar.setCanTouch(false);

            assess_ratingbar.setStarEmptyDrawable(getResources().getDrawable(R.drawable.iv_inactive_star));
            assess_ratingbar.setStarFillDrawable(getResources().getDrawable(R.drawable.iv_inactive_star_selected));
            assess_ratingbar.setCanTouch(false);
            grade_ratingbar.setStarEmptyDrawable(getResources().getDrawable(R.drawable.iv_inactive_star));
            grade_ratingbar.setStarFillDrawable(getResources().getDrawable(R.drawable.iv_inactive_star_selected));
            grade_ratingbar.setCanTouch(false);

            flowlayout.setEnabled(false);
            changeTagFlowStatus(false);
        }
    }

    private void changeTagFlowStatus(boolean flag) {
        if (flowlayout != null) {
            for (int i = 0; i < flowlayout.getChildCount(); i++) {
                flowlayout.getChildAt(i).setEnabled(flag);
            }
        }
    }

    @Override
    public void preExecute(ResponseData request) {

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.GET_YZM) {
                removeProgressDialog();
                CustomToast.show("验证码已发送，请稍候...", CustomToast.LENGTH_SHORT);
                btn_yzm.setEnabled(false);
                mCountDownTimer.start();// 启动倒计时
            } else if (request.flag == AppConst.CHECK_PHONE) {
                String jsonStr = response.body.toString();
                JSONObject mjson = JSON.parseObject(jsonStr);
                if (mjson.containsKey("status")) {
                    String status = mjson.getString("status");
                    switch (status) {
                        case "001010":
                            requestYZM();
                            break;
                        case "001011":
                            removeProgressDialog();
                            CustomToast.show("你输入的手机号码已被占用", CustomToast.LENGTH_SHORT);
                            break;
                        case "001002":
                            removeProgressDialog();
                            CustomToast.show("你输入的手机号码为空", CustomToast.LENGTH_SHORT);
                            break;
                        default:
                            break;
                    }
                }
            } else if (request.flag == AppConst.CHECK_YZM) {
                String jsonStr = response.body.toString();
                JSONObject mjson = JSON.parseObject(jsonStr);
                if (mjson.containsKey("status")) {
                    String status = mjson.getString("status");
                    switch (status) {
                        case "0":
                            isValid = false;
                            break;
                        case "1":
                            isValid = true;
                            break;
                        default:
                            break;
                    }
                    if (isValid) {
                        requestChangePhoneNumber();
                    } else {
                        removeProgressDialog();
                        CustomToast.show("验证码错误", CustomToast.LENGTH_SHORT);
                    }
                }
            } else if (request.flag == AppConst.CHANGE_PHONENUMBER) {
                removeProgressDialog();
                dialog.dismiss();
                tv_phone.setText(phoneNumber);
            } else if (request.flag == AppConst.UPLOAD) {
                removeProgressDialog();
                if (StringUtil.notEmpty(bgPath)) {
                    ContentResolver resolver = getContentResolver();
                    try {
                        Bitmap bm = MediaStore.Images.Media.getBitmap(resolver, Uri.fromFile(new File(bgPath)));
                        photo_lay.setBackground(new BitmapDrawable(bm));
                        SessionContext.mUser.user.headphotourl = response.body.toString();
                        SharedPreferenceUtil.getInstance().setString(AppConst.USER_PHOTO_URL, SessionContext.mUser != null ? SessionContext.mUser.user.headphotourl : "", false);
                        SharedPreferenceUtil.getInstance().setString(AppConst.USER_INFO, JSON.toJSONString(SessionContext.mUser), true);
                        sendBroadcast(new Intent(BroadCastConst.UPDATE_USERINFO));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (request.flag == AppConst.SAVE_USERINFO) {
                reqeustGetUserInfo();
            } else if (request.flag == AppConst.CENTER_USERINFO) {
                removeProgressDialog();
                if (StringUtil.isEmpty(response.body.toString()) || response.body.toString().equals("{}")) {
                    CustomToast.show("获取用户信息失败，请重试1", 0);
                    return;
                }
                SessionContext.mUser = JSON.parseObject(response.body.toString(), UserInfo.class);

                if (SessionContext.mUser == null || StringUtil.isEmpty(SessionContext.mUser)) {
                    CustomToast.show("获取用户信息失败，请重试2", 0);
                    return;
                }

                SharedPreferenceUtil.getInstance().setString(AppConst.USER_PHOTO_URL, SessionContext.mUser != null ? SessionContext.mUser.user.headphotourl : "", false);
                SharedPreferenceUtil.getInstance().setString(AppConst.USER_INFO, response.body.toString(), true);
                sendBroadcast(new Intent(BroadCastConst.UPDATE_USERINFO));
                int newSkinIndex = SharedPreferenceUtil.getInstance().getInt(AppConst.SKIN_INDEX, 0);
                if (oldSkinIndex != newSkinIndex) {
                    Intent intent = new Intent(this, UserCenterActivity.class);
                    intent.putExtra("scrollY", scroll_view.getScrollY());
                    startActivity(intent);
                    overridePendingTransition(R.anim.act_restart, R.anim.act_restop);
                    finish();
                }
            }
        }
    }

    @Override
    protected void onNotifyError(ResponseData request) {
        super.onNotifyError(request);
        if (request.flag == AppConst.CHECK_YZM) {
            isValid = false;
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

    private int valueConvertPrice(float value) {
        String price = RangeSeekBar.textSummary[(int) value];
        return Integer.valueOf(price);
    }

    private float priceConvertValue(int price) {
        float value = 0f;
        switch (price) {
            case 0:
                value = 0f;
                break;
            case 100:
                value = 1f;
                break;
            case 300:
                value = 2f;
                break;
            case 500:
                value = 3f;
                break;
            case 1000:
                value = 4f;
                break;
            case 2000:
                value = 5f;
                break;
            default:
                value = 0f;
                break;
        }
        return value;
    }
}
