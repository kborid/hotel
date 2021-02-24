package com.huicheng.hotel.android.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * @author kborid
 * @date 2018/1/21 0021.
 */

public abstract class BaseActivity extends AppCompatActivity {

    protected void preOnCreate() {
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        preOnCreate();
        super.onCreate(savedInstanceState);
        setContentView();
        initTypeArrayAttributes();
        initViews();
        initParams();
        initListeners();
        if (null == savedInstanceState) {
            requestData();
        }
    }

    protected void initTypeArrayAttributes(){}

    protected abstract void setContentView();

    protected abstract void initViews();

    protected abstract void initParams();

    protected abstract void initListeners();

    protected void requestData() {
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
