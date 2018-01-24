package com.huicheng.hotel.android.pay.qmf;

import com.chinaums.pppay.unify.UnifyPayListener;
import com.prj.sdk.util.LogUtil;

/**
 * @author kborid
 * @date 2017/11/2 0002.
 */

public class QmfUnifyPayListener implements UnifyPayListener {
    @Override
    public void onResult(String s, String s1) {
        LogUtil.i("s = " + s + ", s1 = " + s1);
    }
}
