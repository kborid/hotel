package com.huicheng.hotel.android.ui.activity;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huicheng.hotel.android.BuildConfig;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.control.DataCleanManager;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.Utils;

import java.io.File;

/**
 * @author kborid
 * @date 2016/12/13 0013
 */
public class SettingActivity extends BaseActivity {

    private final String TAG = getClass().getSimpleName();
    private TextView tv_notify, tv_cache, tv_about, tv_version, tv_assess, tv_layer, tv_secret;
    private TextView tv_cache_size;

    private int mCCount = 0;
    private LinearLayout debug_lay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_setting_layout);
        initViews();
        initParams();
        initListeners();
    }

    @Override
    public void initViews() {
        super.initViews();
        tv_notify = (TextView) findViewById(R.id.tv_notify);
        tv_cache = (TextView) findViewById(R.id.tv_cache);
        tv_cache_size = (TextView) findViewById(R.id.tv_cache_size);
        tv_about = (TextView) findViewById(R.id.tv_about);
        tv_version = (TextView) findViewById(R.id.tv_version);
        tv_assess = (TextView) findViewById(R.id.tv_assess);
        tv_layer = (TextView) findViewById(R.id.tv_layer);
        tv_secret = (TextView) findViewById(R.id.tv_secret);

        debug_lay = (LinearLayout) findViewById(R.id.debug_lay);
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText(R.string.setting);
        tv_version.setText(BuildConfig.VERSION_NAME + getString(R.string.setting_version));
        tv_cache_size.setText(calculateCacheSize());
    }

    @Override
    public void initListeners() {
        super.initListeners();
        tv_notify.setOnClickListener(this);
        tv_cache.setOnClickListener(this);
        tv_about.setOnClickListener(this);
        tv_version.setOnClickListener(this);
        tv_assess.setOnClickListener(this);
        tv_layer.setOnClickListener(this);
        tv_secret.setOnClickListener(this);

        tv_center_title.setOnClickListener(this);
        debug_lay.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        Intent intent = null;
        switch (v.getId()) {
            case R.id.tv_notify:
                intent = new Intent(Settings.ACTION_SETTINGS);
                break;
            case R.id.tv_cache:
                CustomDialog dialog = new CustomDialog(this);
                dialog.setMessage("是否清除缓存？");
                dialog.setNegativeButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DataCleanManager.clearAllCache(SettingActivity.this);
                        DataCleanManager.cleanCustomCache(Utils.getFolderDir("dataCache"));
                        DataCleanManager.cleanCustomCache(Utils.getFolderDir("imageCache"));
                        tv_cache_size.setText(calculateCacheSize());
                        dialog.dismiss();
                    }
                });
                dialog.setPositiveButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.out.println(getDatabasePath("databases").getPath());
                        System.out.println(getFileStreamPath("shared_pref").getPath());
                        System.out.println(getFilesDir().getPath());
                        dialog.dismiss();
                    }
                });
                dialog.show();
                break;
            case R.id.tv_about:
                intent = new Intent(this, AboutActivity.class);
                intent.putExtra("title", getString(R.string.about_us));
                intent.putExtra("content", getString(R.string.about_us_content));
                break;
            case R.id.tv_version:
                intent = new Intent(this, AboutActivity.class);
                intent.putExtra("title", getString(R.string.version));
                intent.putExtra("content", getString(R.string.versionIntroduce));
                intent.putExtra("index", AboutActivity.VERSION_INTRODUCE);
                break;
            case R.id.tv_assess:
                try {
                    Uri uri = Uri.parse("market://details?id=" + getPackageName());
                    intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "未找到应用商店", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.tv_layer:
                intent = new Intent(this, AboutActivity.class);
                intent.putExtra("title", getString(R.string.setting_condition));
                intent.putExtra("index", AboutActivity.WORK_CONDITION);
                break;
            case R.id.tv_secret:
                intent = new Intent(this, AboutActivity.class);
                intent.putExtra("title", getString(R.string.setting_private));
                intent.putExtra("index", AboutActivity.STATEMENT);
                break;
            case R.id.tv_center_title:
                if (AppConst.ISDEVELOP && !debug_lay.isShown()) {
                    if (++mCCount >= 7) {
                        mCCount = 0;
                        SharedPreferenceUtil.getInstance().setBoolean("isShowDebug", true);
                        debug_lay.setVisibility(View.VISIBLE);
                    }
                    System.out.println("Tap Count = " + mCCount);
                }
                break;
            case R.id.debug_lay:
                intent = new Intent(this, DebugInfoActivity.class);
                break;
            default:
                break;
        }
        if (null != intent) {
            startActivity(intent);
        }
    }

    private String calculateCacheSize() {
        long totalCacheSize = 0;
        try {
            LogUtil.i(TAG, "内部缓存大小：" + DataCleanManager.getFormatSize(DataCleanManager.getFolderSize(this.getCacheDir())));
            LogUtil.i(TAG, "内部文件大小：" + DataCleanManager.getFormatSize(DataCleanManager.getFolderSize(this.getFilesDir())));
            LogUtil.i(TAG, "外部缓存大小：" + DataCleanManager.getFormatSize(DataCleanManager.getFolderSize(this.getExternalCacheDir())));
            LogUtil.i(TAG, "请求数据缓存大小：" + DataCleanManager.getFormatSize(DataCleanManager.getFolderSize(new File(Utils.getFolderDir("dataCache")))));
            LogUtil.i(TAG, "请求图片缓存大小：" + DataCleanManager.getFormatSize(DataCleanManager.getFolderSize(new File(Utils.getFolderDir("imageCache")))));
            totalCacheSize += DataCleanManager.getFolderSize(this.getCacheDir());
            totalCacheSize += DataCleanManager.getFolderSize(this.getExternalCacheDir());
            totalCacheSize += DataCleanManager.getFolderSize(new File(Utils.getFolderDir("dataCache")));
            totalCacheSize += DataCleanManager.getFolderSize(new File(Utils.getFolderDir("imageCache")));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return DataCleanManager.getFormatSize(totalCacheSize);
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean isShowDebug = SharedPreferenceUtil.getInstance().getBoolean("isShowDebug", false);
        if (AppConst.ISDEVELOP && isShowDebug) {
            debug_lay.setVisibility(View.VISIBLE);
        } else {
            debug_lay.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}