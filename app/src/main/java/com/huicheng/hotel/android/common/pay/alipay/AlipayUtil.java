package com.huicheng.hotel.android.common.pay.alipay;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;

import com.alipay.sdk.app.PayTask;
import com.google.gson.Gson;
import com.huicheng.hotel.android.R;
import com.prj.sdk.constants.BroadCastConst;
import com.huicheng.hotel.android.ui.dialog.CustomToast;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * alipay操作工具类
 *
 * @author LiaoBo
 */
public class AlipayUtil {
    private static final int SDK_PAY_FLAG = 0x01;
    private Activity mContext;
    private PayTask mPayTask = null;
    private Handler mHandler = new MyHandler(this);

    public AlipayUtil(Activity context) {
        mContext = context;
        mPayTask = new PayTask(context);
    }

    /**
     * call alipay sdk pay. 调用SDK支付
     *
     * @param orderInfo 订单
     */
    public void pay(final String orderInfo) {
        // if (!check()) {
        // CustomToast.show("没有支护宝，请安装支护宝", CustomToast.LENGTH_SHORT);
        // return;
        // }

        // 对订单做RSA 签名
//        String sign = sign(orderInfo);
//        try {
        // 仅需对sign 做URL编码
//            sign = URLEncoder.encode(sign, "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }

        // 完整的符合支付宝参数规范的订单信息
//        final String payInfo = orderInfo + "&sign=\"" + sign + "\"&" + getSignType();

        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                // 构造PayTask 对象
//                PayTask alipay = new PayTask(mContext);
                // 调用支付接口，获取支付结果
//                String result = alipay.pay(payInfo);
                String result = mPayTask.pay(orderInfo, true);
                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };

        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    /**
     * check whether the device has authentication alipay account. 查询终端设备是否存在支付宝认证账户
     *
     * @return 是否存在支护宝app
     */
//    public boolean check() {
//        boolean isExist = false;
//        try {
//            // 构造PayTask 对象
//            PayTask payTask = new PayTask(mContext);
//            // 调用查询接口，获取查询结果
//            isExist = payTask.checkAccountIfExist();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return isExist;
//    }

    /**
     * get the sdk version. 获取SDK版本号
     */
    private void getSDKVersion() {
//        PayTask payTask = new PayTask(mContext);
        String version = mPayTask.getVersion();
        CustomToast.show(version, CustomToast.LENGTH_SHORT);
    }

    /**
     * 生成订单信息
     *
     * @param subject      商品名名称
     * @param body         商品详情
     * @param seller_id    商户id
     * @param total_fee    总金额
     * @param out_trade_no 订单号
     * @param notify_url   服务器异步通知页面路径
     * @return String      账单信息
     */
    public String getOrderInfo(String subject, String body, String seller_id, String total_fee, String out_trade_no, String notify_url) {

        // 签约合作者身份ID
        String orderInfo = "partner=" + "\"" + mContext.getResources().getString(R.string.partnerId) + "\"";

        // 签约卖家支付宝账号
        orderInfo += "&seller_id=" + "\"" + seller_id + "\"";

        // 商户网站唯一订单号
        orderInfo += "&out_trade_no=" + "\"" + out_trade_no + "\"";

        // 商品名称
        orderInfo += "&subject=" + "\"" + subject + "\"";

        // 商品详情
        orderInfo += "&body=" + "\"" + body + "\"";

        // 商品金额
        orderInfo += "&total_fee=" + "\"" + total_fee + "\"";

        // 服务器异步通知页面路径
        orderInfo += "&notify_url=" + "\"" + notify_url + "\"";

        // 服务接口名称， 固定值
        orderInfo += "&service=\"mobile.securitypay.pay\"";

        // 支付类型， 固定值
        orderInfo += "&payment_type=\"1\"";

        // 参数编码， 固定值
        orderInfo += "&_input_charset=\"utf-8\"";

        // 设置未付款交易的超时时间
        // 默认30分钟，一旦超时，该笔交易就会自动被关闭。
        // 取值范围：1m～15d。
        // m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
        // 该参数数值不接受小数点，如1.5h，可转换为90m。
        orderInfo += "&it_b_pay=\"30m\"";

        // extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
        // orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

        // 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
        orderInfo += "&return_url=\"m.alipay.com\"";

        // 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
        // orderInfo += "&paymethod=\"expressGateway\"";

        return orderInfo;
    }

    /**
     * get the out_trade_no for an order. 生成商户订单号，该值在商户端应保持唯一（可自定义格式规范）
     */
    private String getOutTradeNo() {
        SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss", Locale.getDefault());
        Date date = new Date();
        String key = format.format(date);

        Random r = new Random();
        key = key + r.nextInt();
        key = key.substring(0, 15);
        return key;
    }

    /**
     * sign the order info. 对订单信息进行签名
     *
     * @param content 待签名订单信息
     */
    private String sign(String content) {
        return SignUtils.sign(content, mContext.getResources().getString(R.string.rsa_private));
    }

    /**
     * get the sign type we use. 获取签名方式
     */
    public String getSignType() {
        return "sign_type=\"RSA\"";
    }

    private static class MyHandler extends Handler {
        WeakReference<AlipayUtil> mActivity;

        MyHandler(AlipayUtil activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final AlipayUtil activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case SDK_PAY_FLAG: {
                        Intent mIntent = new Intent(BroadCastConst.ACTION_PAY_STATUS);
                        PayResult payResult = new PayResult((String) msg.obj);
                        mIntent.putExtra("info", new Gson().toJson(payResult));
                        mIntent.putExtra("type", "aliPay");
                        LocalBroadcastManager.getInstance(mActivity.get().mContext).sendBroadcast(mIntent);
                        break;
                    }
                    default:
                        break;
                }
            }
        }
    }
}
