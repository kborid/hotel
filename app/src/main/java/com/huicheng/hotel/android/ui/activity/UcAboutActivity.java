package com.huicheng.hotel.android.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.ui.base.BaseActivity;

public class UcAboutActivity extends BaseActivity {

    public static final int STATEMENT = 1;
    public static final int WORK_CONDITION = 2;
    public static final int VERSION_INTRODUCE = 3;

    private TextView tv_content;
    private String title, content;
    private int index = 0;
    private WebView wv_content;
    private TextView tv_iflytek_tips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_about_layout);
        initViews();
        initParams();
        initListeners();
    }

    @Override
    public void initViews() {
        super.initViews();
        tv_content = (TextView) findViewById(R.id.tv_content);
        wv_content = (WebView) findViewById(R.id.wv_content);
        tv_iflytek_tips = (TextView) findViewById(R.id.tv_iflytek_tips);
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.getString("title") != null) {
            title = bundle.getString("title");
            if (bundle.getString("content") != null) {
                content = bundle.getString("content");
                tv_content.setVisibility(View.VISIBLE);
            }
            index = bundle.getInt("index");
            if (index == 0) {
                tv_iflytek_tips.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText(title);

        tv_content.setText(content);
        WebSettings webSetting = wv_content.getSettings();
        webSetting.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSetting.setSupportZoom(false);
        webSetting.setDisplayZoomControls(false);
        webSetting.setBuiltInZoomControls(false);
        webSetting.setTextZoom(200);
        webSetting.setUseWideViewPort(true);// 将图片调整到适合webview的大小
        webSetting.setLoadWithOverviewMode(true);// 充满全屏。
        webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);// 提高渲染的优先级
        webSetting.setDefaultTextEncodingName("utf-8");
        wv_content.setBackgroundColor(0);
        wv_content.getBackground().setAlpha(0);

        if (index > 0 && index < 3) {
            wv_content.setVisibility(View.VISIBLE);
            if (index == STATEMENT) {
                wv_content.loadUrl("file:///android_asset/abc_privacy_statement.html");
            } else if (index == WORK_CONDITION) {
                wv_content.loadUrl("file:///android_asset/working_condition.html");
            }
        } else if (index == 3) {
            wv_content.setVisibility(View.GONE);
        } else {
            wv_content.setVisibility(View.GONE);
        }
    }
}
