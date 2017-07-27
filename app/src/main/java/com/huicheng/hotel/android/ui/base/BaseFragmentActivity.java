package com.huicheng.hotel.android.ui.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.prj.sdk.util.ActivityTack;
import com.prj.sdk.util.SharedPreferenceUtil;

/**
 * FragmentActivity 基类提供公共属性
 */
public abstract class BaseFragmentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SharedPreferenceUtil.getInstance().getInt(AppConst.SKIN_INDEX, 0) == 1) {
            setTheme(R.style.femaleTheme);
        } else {
            setTheme(R.style.defaultTheme);
        }
        ActivityTack.getInstanse().addActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityTack.getInstanse().removeActivity(this);
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    public void initViews() {
        //透明状态栏
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            getWindow().setFlags(
//                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
//                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        }
    }

    public void dealIntent() {
    }

    // 参数设置
    public void initParams() {
    }

    // 监听设置
    public void initListeners() {
    }

    @Override
    public void finish() {
        super.finish();
    }
}
