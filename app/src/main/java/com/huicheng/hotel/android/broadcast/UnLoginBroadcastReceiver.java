package com.huicheng.hotel.android.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.ui.activity.UserLoginActivity;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;
import com.prj.sdk.constants.BroadCastConst;

/**
 * 未登录广播
 */
public class UnLoginBroadcastReceiver extends BroadcastReceiver {

    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        String action = intent.getAction();
        if (!BroadCastConst.UNLOGIN_ACTION.equals(action)) {
            return;
        }
        boolean isShowDialog = false;
        Bundle bundle = intent.getExtras();
        if (bundle != null && bundle.getBoolean("is_show_tip_dialog")) {
            isShowDialog = true;
        }

        if (isShowDialog) {
            showLoginDialog();
        } else {
            intentLogin();
        }
    }

    private void showLoginDialog() {

        String msg;
        if (SessionContext.isLogin()) {
            msg = context.getResources().getString(R.string.login_token_daily);
        } else {
            msg = context.getResources().getString(R.string.login_not_token);
        }
        // 注销登录状态
        SessionContext.cleanUserInfo();


        if (!CustomDialog.isDialogShow()) {
            CustomDialog dialog = new CustomDialog(context);
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
            dialog.setCancelable(false);
            dialog.setMessage(msg);
            dialog.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            dialog.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    intentLogin();
                }
            });

            dialog.show();
        }
    }

    /**
     * 跳转登录页面
     */
    private void intentLogin() {
        Intent mIntent = new Intent(context, UserLoginActivity.class);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(mIntent);
        context.sendBroadcast(new Intent(BroadCastConst.UPDATE_USERINFO));
    }
}
