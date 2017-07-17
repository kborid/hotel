package com.prj.sdk.net.http;

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
    private static final String TAG = OKHttpHelper.class.getName();

    private Request request;
    private Response response;

    public OKHttpHelper() {
    }

    public byte[] executeHttpRequest(String url, String httpType, Map<String, Object> header, Object mEntity, boolean isForm) {
        ResponseBody mResponseBody = null;
        try {
            Response response = getResponse(url, httpType, header, mEntity, isForm);
            mResponseBody = response.isSuccessful() ? response.body() : null;
            return mResponseBody != null ? mResponseBody.bytes() : null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mResponseBody != null) {
                mResponseBody.close();
            }
        }
        return null;
    }

    public Response getResponse(String url, String httpType, Map<String, Object> header, Object mEntity, boolean isForm) {
        try {
            if (InfoType.GET_REQUEST.toString().equals(httpType)) {
                if (mEntity != null) {
                    if (mEntity instanceof JSONObject) {
                        JSONObject mJson = (JSONObject) mEntity;
                        StringBuffer params = new StringBuffer();
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
                        } else if (mEntity == null) {
                            request = OkHttpClientControl.getInstance().buildPostRequest(url, header, new byte[]{});
                        }
                    }
                }
            } else {
                LogUtil.d(TAG, "warning!!!!!!" + httpType);
            }

            response = OkHttpClientControl.getInstance().sync(request);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Request getRequest() {
        return request;
    }

    public Response getResponse() {
        return response;
    }

}
