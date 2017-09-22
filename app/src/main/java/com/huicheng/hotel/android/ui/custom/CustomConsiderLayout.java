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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;

/**
 * @author kborid
 * @date 2016/11/14 0014
 */
public class CustomConsiderLayout extends RelativeLayout implements View.OnClickListener, RangeSeekBar.OnRangeChangedListener {

    private final String TAG = getClass().getSimpleName();

    private Context context;

    private LinearLayout root_lay;
    private int pointIndex, gradeIndex, typeIndex;
    private CommonSingleSelLayout pointLay, typeLay, gradeLay;
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
        ((TextView) findViewById(R.id.tv_price_lable)).getPaint().setFakeBoldText(true);
        ((TextView) findViewById(R.id.tv_grade_lable)).getPaint().setFakeBoldText(true);
        ((TextView) findViewById(R.id.tv_type_lable)).getPaint().setFakeBoldText(true);
        pointLay = (CommonSingleSelLayout) findViewById(R.id.pointLay);
        typeLay = (CommonSingleSelLayout) findViewById(R.id.typeLay);
        gradeLay = (CommonSingleSelLayout) findViewById(R.id.gradeLay);

        rangeSeekBar = (RangeSeekBar) findViewById(R.id.seekbar);
        iv_reset = (ImageView) findViewById(R.id.iv_reset);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void initParamers() {
        Bitmap bm = getRoundedCornerBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.bg_white), 25);
        root_lay.setBackground(new BitmapDrawable(context.getResources(), bm));
        root_lay.getBackground().mutate().setAlpha((int) (255 * 0.95f));

        pointLay.setData(context.getResources().getStringArray(R.array.HotelPoint));
        typeLay.setData(context.getResources().getStringArray(R.array.HotelType));
        gradeLay.setData(context.getResources().getStringArray(R.array.HotelStar));
//        initAndRestoreConfig();
    }

    private void setClickListeners() {
        rangeSeekBar.setOnRangeChangedListener(this);
        iv_reset.setOnClickListener(this);
    }

    public void initAndRestoreConfig() {
        rangeMin = 0f;
        rangeMax = 6f;
        rangeSeekBar.setValue(rangeMin, rangeMax);
        pointIndex = pointLay.resetSelectedIndex();
        typeIndex = typeLay.resetSelectedIndex();
        gradeIndex = gradeLay.resetSelectedIndex();

    }

    public void setPriceRange(int index) {
        switch (index) {
            case 0:
                rangeMin = 0f;
                rangeMax = 6f;
                break;
            case 1:
                rangeMin = 0f;
                rangeMax = 2f;
                break;
            case 2:
                rangeMin = 2f;
                rangeMax = 3f;
                break;
            case 3:
                rangeMin = 3f;
                rangeMax = 4f;
                break;
            case 4:
                rangeMin = 4f;
                rangeMax = 5f;
                break;
            case 5:
                rangeMin = 5f;
                rangeMax = 6f;
                break;
            default:
                rangeMin = 0f;
                rangeMax = 6f;
                break;
        }
        SharedPreferenceUtil.getInstance().setFloat(AppConst.RANGE_MIN, rangeMin);
        SharedPreferenceUtil.getInstance().setFloat(AppConst.RANGE_MAX, rangeMax);
        rangeSeekBar.setValue(rangeMin, rangeMax);
    }

    private void saveConsiderConfig() {
        SharedPreferenceUtil.getInstance().setInt(AppConst.CONSIDER_POINT, pointIndex);
        SharedPreferenceUtil.getInstance().setInt(AppConst.CONSIDER_GRADE, gradeIndex);
        SharedPreferenceUtil.getInstance().setInt(AppConst.CONSIDER_TYPE, typeIndex);
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
        pointIndex = pointLay.getSelectedIndex();
        typeIndex = typeLay.getSelectedIndex();
        gradeIndex = gradeLay.getSelectedIndex();
        LogUtil.i(TAG, "pointIndex = " + pointIndex + ", typeIndex = " + typeIndex + ", gradeIndex = " + gradeIndex);
        saveConsiderConfig();
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
