package com.huicheng.hotel.android.pay.qmf;

import com.chinaums.pppay.unify.UnifyPayListener;

/**
 * @auth kborid
 * @date 2017/11/2 0002.
 */

public class QmfUnifyPayListener implements UnifyPayListener {

    @Override
    public void onResult(int i, String s) {
        System.out.println("onResult() i = " + i + ", s = " + s);
    }
}
