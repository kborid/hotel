package com.prj.sdk.net.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.prj.sdk.constants.InfoType;
import com.prj.sdk.util.LogUtil;

import java.util.Iterator;
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
            if (response != null) {
                LogUtil.i(TAG, "Res:" + response.toString());
                if (response.isSuccessful()) {
                    mResponseBody = response.body();
                }
            }
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
                        Iterator<String> it = mJson.keySet().iterator();
                        StringBuffer sb = null;
                        while (it.hasNext()) {
                            String key = it.next();
                            String value = mJson.getString(key);
                            if (sb == null) {
                                sb = new StringBuffer();
                                sb.append("?");
                            } else {
                                sb.append("&");
                            }
                            sb.append(key);
                            sb.append("=");
                            sb.append(value);
                        }
                        if (sb != null) {
                            url += sb.toString();
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

            if (null != request) {
                LogUtil.i(TAG, "Req:" + request.toString());
            }
            return OkHttpClientControl.getInstance().sync(request);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
