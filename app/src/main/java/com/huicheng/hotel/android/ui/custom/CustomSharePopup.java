package com.huicheng.hotel.android.ui.custom;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.control.ShareControl;
import com.umeng.socialize.bean.SHARE_MEDIA;

/**
 * @author kborid
 * @date 2016/10/26 0026
 */
public class CustomSharePopup extends LinearLayout implements View.OnClickListener {
    private Context context;

    private LinearLayout wx_lay;
    private LinearLayout circle_lay;
    private LinearLayout qq_lay;
//    private LinearLayout sina_lay;

    private TextView tv_cancel;

    public CustomSharePopup(Context context) {
        this(context, null);
    }

    public CustomSharePopup(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomSharePopup(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        LayoutInflater.from(context).inflate(R.layout.share_layout, this);
        findViews();
        setOnClickListeners();
    }

    private void findViews() {
        wx_lay = (LinearLayout) findViewById(R.id.wx_share);
        circle_lay = (LinearLayout) findViewById(R.id.circle_share);
        qq_lay = (LinearLayout) findViewById(R.id.qq_share);
//        sina_lay = (LinearLayout) findViewById(R.id.sina_share);

        ((ImageView) wx_lay.findViewById(R.id.iv_icon)).setImageResource(R.drawable.iv_wx);
        ((TextView) wx_lay.findViewById(R.id.tv_title)).setText("微信好友");

        ((ImageView) circle_lay.findViewById(R.id.iv_icon)).setImageResource(R.drawable.iv_wx_circle);
        ((TextView) circle_lay.findViewById(R.id.tv_title)).setText("微信朋友圈");

        ((ImageView) qq_lay.findViewById(R.id.iv_icon)).setImageResource(R.drawable.iv_qq);
        ((TextView) qq_lay.findViewById(R.id.tv_title)).setText("手机QQ");

//        ((ImageView) sina_lay.findViewById(R.id.iv_icon)).setImageResource(R.drawable.iv_sina);
//        ((TextView) sina_lay.findViewById(R.id.tv_title)).setText("新浪微博");

        tv_cancel = (TextView) findViewById(R.id.tv_cancel);
    }

    private void setOnClickListeners() {
        wx_lay.setOnClickListener(this);
        circle_lay.setOnClickListener(this);
        qq_lay.setOnClickListener(this);
//        sina_lay.setOnClickListener(this);

        tv_cancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        SHARE_MEDIA sm = null;
        switch (v.getId()) {
            case R.id.wx_share:
                sm = SHARE_MEDIA.WEIXIN;
                break;
            case R.id.circle_share:
                sm = SHARE_MEDIA.WEIXIN_CIRCLE;
                break;
            case R.id.qq_share:
                sm = SHARE_MEDIA.QQ;
                break;
//            case R.id.sina_share:
//                sm = SHARE_MEDIA.SINA;
//                break;
            case R.id.tv_cancel:
                break;
            default:
                break;
        }

        if (sm != null) {
            ShareControl.getInstance().shareUMWeb(sm);
        }
        if (null != listener) {
            listener.onDismiss();
        }
    }
    public void setOnCancelListener(OnCanceledListener listener) {
        this.listener = listener;
    }
    private OnCanceledListener listener = null;
    public interface OnCanceledListener {
        void onDismiss();
    }
}
