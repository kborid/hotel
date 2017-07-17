package com.huicheng.hotel.android.common.pay.alipay;import com.prj.sdk.util.StringUtil;import java.io.Serializable;public class PayResult implements Serializable{    public String resultStatus;    public String result;    public String memo;    public PayResult(String rawResult) {        if (StringUtil.isEmpty(rawResult)) {            return;        }        String[] resultParams = rawResult.split(";");        for (String resultParam : resultParams) {            if (resultParam.startsWith("resultStatus")) {                resultStatus = gatValue(resultParam, "resultStatus");            }            if (resultParam.startsWith("result")) {                result = gatValue(resultParam, "result");            }            if (resultParam.startsWith("memo")) {                memo = gatValue(resultParam, "memo");            }        }    }    @Override    public String toString() {        return "resultStatus = " + resultStatus + ", memo = " + memo + ", result = " + result;    }    private String gatValue(String content, String key) {        String prefix = key + "={";        return content.substring(content.indexOf(prefix) + prefix.length(),                content.lastIndexOf("}"));    }}