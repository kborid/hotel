package com.huicheng.hotel.android.requestbuilder.bean;

import java.util.List;

/**
 * @auth kborid
 * @date 2018/1/11 0011.
 */

public class PlaneBookingInfoBase {
    public String bookingTag;
    public PlaneBookingInfo_ExtInfo extInfo;
    public List<PlaneBookingInfo_FlightInfo> flightInfo;
    public PlaneBookingInfo_PriceInfo priceInfo;
    public boolean trip;
    public int etAccidePrice;   //意外险
    public int etDelayPrice;    //延误险
}
