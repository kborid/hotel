package com.prj.sdk.net.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.prj.sdk.constants.InfoType;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.StringUtil;

import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


/**
 * http请求封装类 增加代理处理 URL验证 超时控制等
 */
public class OKHttpHelper {
    private final String TAG = getClass().getSimpleName();

    public OKHttpHelper() {
    }

    public byte[] executeHttpRequest(String url, String httpType, Map<String, Object> header, Object mEntity, boolean isForm) {
        Response response = null;
        ResponseBody mResponseBody = null;
        try {
            response = getResponse(url, httpType, header, mEntity, isForm);
            mResponseBody = response != null && response.isSuccessful() ? response.body() : null;
            return mResponseBody != null ? mResponseBody.bytes() : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (null != mResponseBody) {
                mResponseBody.close();
            }
            if (null != response) {
                response.close();
            }
        }
    }

    private Response getResponse(String url, String httpType, Map<String, Object> header, Object mEntity, boolean isForm) {
        Request request = null;
        try {
            if (InfoType.GET_REQUEST.toString().equals(httpType)) {
                if (mEntity != null) {
                    JSONObject mJson = null;
                    if (mEntity instanceof String) {
                        String mJsonString = (String) mEntity;
                        mJson = JSON.parseObject(mJsonString);
                    } else if (mEntity instanceof JSONObject) {
                        mJson = (JSONObject) mEntity;
                    }
                    if (null != mJson) {
                        StringBuilder params = new StringBuilder();
                        for (String key : mJson.keySet()) {
                            params.append(key).append("=").append(mJson.getString(key) != null ? mJson.getString(key) : "").append("&");
                        }
                        if (StringUtil.notEmpty(url)) {
                            if (url.contains("?")) {
                                if (url.endsWith("&")) {
                                    url += params.toString();
                                } else {
                                    url += "&" + params.toString();
                                }
                            } else {
                                url += "?" + params.toString();
                            }
                        }
                    }
                }
                LogUtil.d(TAG, "url = " + url);
                request = OkHttpClientControl.getInstance().buildGetRequest(url, header);
            } else if (InfoType.POST_REQUEST.toString().equals(httpType)) {
                if (mEntity != null) {
                    if (mEntity instanceof String) {
                        String mJson = (String) mEntity;
                        request = OkHttpClientControl.getInstance().buildPostRequest(url, header, mJson);
                    } else {
                        if (mEntity instanceof JSONObject) {
                            JSONObject mJson = (JSONObject) mEntity;
                            if (isForm) {
                                request = OkHttpClientControl.getInstance().buildPostMultipartFormRequest(url, header, mJson);
                            } else {
                                request = OkHttpClientControl.getInstance().buildPostFormRequest(url, header, mJson);
                            }
                        } else if (mEntity instanceof byte[]) {
                            byte[] data = (byte[]) mEntity;
                            request = OkHttpClientControl.getInstance().buildPostRequest(url, header, data);
                        }
                    }
                } else {
                    request = OkHttpClientControl.getInstance().buildPostRequest(url, header, new byte[]{});
                }
            } else {
                LogUtil.d(TAG, "warning: not define " + httpType);
            }

            return OkHttpClientControl.getInstance().sync(request);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
