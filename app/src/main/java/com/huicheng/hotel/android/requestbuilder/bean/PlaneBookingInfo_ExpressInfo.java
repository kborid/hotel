package com.huicheng.hotel.android.requestbuilder.bean;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

/**
 * @auth kborid
 * @date 2018/1/15 0015.
 */

public class PlaneBookingInfo_ExpressInfo implements Serializable {
    public int id;
    public JSONObject invoiceType;
    public int price;
    public JSONObject receiverType;
}
