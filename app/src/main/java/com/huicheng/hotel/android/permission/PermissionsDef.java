package com.huicheng.hotel.android.permission;

import android.Manifest;

/**
 * @auth kborid
 * @date 2017/9/27.
 */

public class PermissionsDef {

    public static final int PERMISSION_REQ_CODE = 0x999;

    //App启动所必需的权限
    public static final String[] LAUNCH_REQUIRE_PERMISSIONS = new String[]{
            //存储权限
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,

            //位置权限
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,

            //电话权限
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CALL_PHONE,

            //联系人权限
            Manifest.permission.READ_CONTACTS,//科大讯飞需要
    };

    public static final String[] STORAGE_PERMISSION = new String[]{
            //存储权限
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    public static final String[] PHONE_PERMISSION = new String[]{
            //电话权限
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CALL_PHONE,
    };

    public static final String[] MIC_PERMISSION = new String[]{
            //麦克风权限
            Manifest.permission.RECORD_AUDIO,
    };


    public static final String[] ALL_PERMISSION = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.RECORD_AUDIO,
    };
}
