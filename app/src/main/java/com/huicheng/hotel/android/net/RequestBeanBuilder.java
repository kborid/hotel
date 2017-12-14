package com.huicheng.hotel.android.net;

import android.content.Intent;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.huicheng.hotel.android.BuildConfig;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.tools.Algorithm3DES;
import com.huicheng.hotel.android.tools.AlgorithmData;
import com.huicheng.hotel.android.tools.Base64;
import com.prj.sdk.algo.MD5Tool;
import com.prj.sdk.app.AppContext;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.constants.InfoType;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 构建请求处理
 */

public class RequestBeanBuilder {

    private Map<String, Object> head;
    private Map<String, Object> body;

    private RequestBeanBuilder(boolean isNeedTicket) {
        head = new HashMap<>();
        body = new HashMap<>();
        if (isNeedTicket) {
            if (StringUtil.isEmpty(SessionContext.getTicket())) {
                AppContext.mAppContext.sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION));
            }
            addHeadToken(SessionContext.getTicket());
        }
    }

    /**
     * 构建请求
     *
     * @param isNeedTicket 是否需要ticket ,如果需要登录就需要ticket
     */
    public static RequestBeanBuilder create(boolean isNeedTicket) {
        return new RequestBeanBuilder(isNeedTicket);
    }

    private RequestBeanBuilder addHeadToken(String token) {
        return addHead("accessTicket", token);
    }

    private RequestBeanBuilder addHead(String key, Object value) {
        head.put(key, value);
        return this;
    }

    public RequestBeanBuilder addBody(String key, Object value) {
        body.put(key, value);
        return this;
    }

    private String sign() {
        AlgorithmData data = new AlgorithmData();
        try {
            // 先对body进行base64
            String bodyText = new Gson().toJson(body);
            LogUtil.i("RequestBeanBuilder", "bodyTextJson:" + bodyText);
            // 对报文进行BASE64编码，避免中文处理问题
            String base64Text = new String(Base64.encodeBase64((AppConst.APPID + bodyText).getBytes("utf-8"), false));
            LogUtil.i("RequestBeanBuilder", "bodyBase64Text:" + base64Text);
            // MD5摘要，生成固定长度字符串用于加密
            String destText = MD5Tool.getMD5(base64Text);
            LogUtil.i("RequestBeanBuilder", "bodyMD5Text:" + destText);
            data.setDataMing(destText);
            data.setKey(AppConst.APPKEY);
            // 3DES加密
            Algorithm3DES.encryptMode(data);
            LogUtil.i("RequestBeanBuilder", "bodyAlgorithm3DESText:" + data.getDataMi());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data.getDataMi();
    }

    /**
     * 请求数据的json字符串
     */
    private String toJson() {
        HashMap<String, Object> json = new HashMap<>();
        json.put("head", head);
        json.put("body", body);

        head.put("appid", AppConst.APPID);
        head.put("sign", sign());
        head.put("version", AppConst.VERSION);
        head.put("siteid", SharedPreferenceUtil.getInstance().getString(AppConst.SITEID, "", false));
        head.put("appversion", BuildConfig.VERSION_NAME);

        return new Gson().toJson(json);
    }

    private JSONObject toJsonForForm() {
        HashMap<String, Object> json = new HashMap<>();
        for (String key : body.keySet()) {
            if (StringUtil.isEmpty(key)) {
                continue;
            }
            json.put(key, body.get(key));
        }
        return new JSONObject(json);
    }

    /**
     * GET请求数据
     */
    public ResponseData syncRequestGET(RequestBeanBuilder builder) {
        ResponseData data = new ResponseData();
        data.data = new Gson().toJson(builder.body);
        data.type = InfoType.GET_REQUEST.toString();
        return data;
    }

    /**
     * POST请求数据
     *
     * @return
     */
    public ResponseData syncRequest(RequestBeanBuilder builder) {
        ResponseData data = new ResponseData();
        data.data = builder.toJson();
        data.type = InfoType.POST_REQUEST.toString();
        return data;
    }

    /**
     * POST请求数据
     *
     * @return
     */
    public ResponseData syncRequestForForm(RequestBeanBuilder builder) {
        ResponseData data = new ResponseData();
        data.data = builder.toJsonForForm();
        data.type = InfoType.POST_REQUEST.toString();
        return data;
    }

}
