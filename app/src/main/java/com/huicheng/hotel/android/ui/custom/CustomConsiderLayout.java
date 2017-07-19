package com.huicheng.hotel.android.ui.custom;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.prj.sdk.util.SharedPreferenceUtil;

/**
 * @author kborid
 * @date 2016/11/14 0014
 */
public class CustomConsiderLayout extends RelativeLayout implements View.OnClickListener, RadioGroup.OnCheckedChangeListener, RangeSeekBar.OnRangeChangedListener {

    private static final String TAG = "CustomConsiderLayout";

    private Context context;

    private LinearLayout root_lay;
    private RadioGroup rg_point, rg_grade, rg_type;
    private int point_index, grade_index, type_index;
    private RangeSeekBar rangeSeekBar;
    private float rangeMin, rangeMax;
    private ImageView iv_reset;

    public CustomConsiderLayout(Context context) {
        this(context, null);
    }

    public CustomConsiderLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomConsiderLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        LayoutInflater.from(context).inflate(R.layout.custom_consider_layout, this);

        findViews();
        initParamers();
        setClickListeners();
    }

    private void findViews() {
        root_lay = (LinearLayout) findViewById(R.id.root);
        ((TextView) findViewById(R.id.tv_title)).getPaint().setFakeBoldText(true);

        ((TextView) findViewById(R.id.tv_point_lable)).getPaint().setFakeBoldText(true);
        rg_point = (RadioGroup) findViewById(R.id.rg_point);
        ((RadioButton) findViewById(R.id.point_00)).getPaint().setFakeBoldText(true);
        ((RadioButton) findViewById(R.id.point_0)).getPaint().setFakeBoldText(true);
        ((RadioButton) findViewById(R.id.point_1)).getPaint().setFakeBoldText(true);
        ((RadioButton) findViewById(R.id.point_2)).getPaint().setFakeBoldText(true);

        ((TextView) findViewById(R.id.tv_price_lable)).getPaint().setFakeBoldText(true);
        ((TextView) findViewById(R.id.tv_grade_lable)).getPaint().setFakeBoldText(true);
        rg_grade = (RadioGroup) findViewById(R.id.rg_grade);
        ((RadioButton) findViewById(R.id.grade_00)).getPaint().setFakeBoldText(true);
        ((RadioButton) findViewById(R.id.grade_0)).getPaint().setFakeBoldText(true);
        ((RadioButton) findViewById(R.id.grade_1)).getPaint().setFakeBoldText(true);
        ((RadioButton) findViewById(R.id.grade_2)).getPaint().setFakeBoldText(true);

        ((TextView) findViewById(R.id.tv_type_lable)).getPaint().setFakeBoldText(true);
        rg_type = (RadioGroup) findViewById(R.id.rg_type);
        ((RadioButton) findViewById(R.id.type_00)).getPaint().setFakeBoldText(true);
        ((RadioButton) findViewById(R.id.type_0)).getPaint().setFakeBoldText(true);
        ((RadioButton) findViewById(R.id.type_1)).getPaint().setFakeBoldText(true);
        ((RadioButton) findViewById(R.id.type_2)).getPaint().setFakeBoldText(true);
        ((RadioButton) findViewById(R.id.type_3)).getPaint().setFakeBoldText(true);

        rangeSeekBar = (RangeSeekBar) findViewById(R.id.seekbar);
        iv_reset = (ImageView) findViewById(R.id.iv_reset);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void initParamers() {
        Bitmap bm = getRoundedCornerBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.bg_white), 25);
        root_lay.setBackground(new BitmapDrawable(context.getResources(), bm));
        root_lay.getBackground().mutate().setAlpha((int) (255 * 0.95f));
//        initAndRestoreConfig();
    }

    private void setClickListeners() {
        rg_point.setOnCheckedChangeListener(this);
        rg_type.setOnCheckedChangeListener(this);
        rangeSeekBar.setOnRangeChangedListener(this);

        iv_reset.setOnClickListener(this);
    }

    public void initAndRestoreConfig() {
        point_index = -1;
        grade_index = -1;
        type_index = -1;
        rangeMin = 0f;
        rangeMax = 5f;
        rg_point.getChildAt(0).performClick();
        rg_grade.getChildAt(0).performClick();
        rg_type.getChildAt(0).performClick();
        rangeSeekBar.setValue(rangeMin, rangeMax);
    }

    private void saveConsiderConfig() {
        SharedPreferenceUtil.getInstance().setInt(AppConst.CONSIDER_POINT, point_index);
        SharedPreferenceUtil.getInstance().setInt(AppConst.CONSIDER_GRADE, grade_index);
        SharedPreferenceUtil.getInstance().setInt(AppConst.CONSIDER_TYPE, type_index);
        SharedPreferenceUtil.getInstance().setFloat(AppConst.RANGE_MIN, rangeMin);
        SharedPreferenceUtil.getInstance().setFloat(AppConst.RANGE_MAX, rangeMax);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_reset:
                initAndRestoreConfig();
                break;
        }
    }

    public void dismiss() {
        saveConsiderConfig();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (group.getId()) {
            case R.id.rg_point:
                switch (checkedId) {
                    case R.id.point_00:
                        point_index = -1;
                        break;
                    case R.id.point_0:
                        point_index = 0;
                        break;
                    case R.id.point_1:
                        point_index = 1;
                        break;
                    case R.id.point_2:
                        point_index = 2;
                        break;
                    default:
                        point_index = -1;
                        break;
                }
                break;
            case R.id.rg_grade:
                switch (checkedId) {
                    case R.id.grade_00:
                        grade_index = -1;
                        break;
                    case R.id.grade_0:
                        grade_index = 0;
                        break;
                    case R.id.grade_1:
                        grade_index = 1;
                        break;
                    case R.id.grade_2:
                        grade_index = 2;
                        break;
                    default:
                        grade_index = -1;
                        break;
                }
                break;
            case R.id.rg_type:
                switch (checkedId) {
                    case R.id.type_00:
                        type_index = -1;
                        break;
                    case R.id.type_0:
                        type_index = 0;
                        break;
                    case R.id.type_1:
                        type_index = 1;
                        break;
                    case R.id.type_2:
                        type_index = 2;
                        break;
                    case R.id.type_3:
                        type_index = 3;
                        break;
                    default:
                        type_index = -1;
                        break;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onRangeChanged(RangeSeekBar view, float min, float max) {
        rangeMin = min;
        rangeMax = max;
    }

    private Bitmap getRoundedCornerBitmap(Bitmap bitmap, int roundPixelSize) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawRoundRect(rectF, (float) roundPixelSize, (float) roundPixelSize, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }
}
