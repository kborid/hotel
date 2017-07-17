package com.huicheng.hotel.android.ui.JSBridge.functions;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.ui.JSBridge.WVJBWebViewClient;
import com.huicheng.hotel.android.ui.activity.MainFragmentActivity;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;

/**
 * @author kborid
 * @date 2017/3/17 0017
 */
public class reBookPlane implements WVJBWebViewClient.WVJBHandler {
    private Context mContext;

    public reBookPlane(Context context) {
        this.mContext = context;
    }

    @Override
    public void request(Object data, WVJBWebViewClient.WVJBResponseCallback callback) {
        if (!AppConst.ISDEVELOP) {
            CustomDialog dialog = new CustomDialog(mContext);
            dialog.setMessage("机票预订正在测试中，即将与您见面");
            dialog.setNegativeButton(mContext.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        } else {
            Intent intent = new Intent(mContext, MainFragmentActivity.class);
            intent.putExtra("index", 1);
            intent.putExtra("isClosed", true);
            intent.putExtra("isReload", true);
            mContext.startActivity(intent);
        }
    }
}
