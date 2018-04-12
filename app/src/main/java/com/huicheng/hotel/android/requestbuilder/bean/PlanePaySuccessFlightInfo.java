package com.huicheng.hotel.android.requestbuilder.bean;

/**
 * @author kborid
 * @date 2018/4/12 0012.
 */

public class PlanePaySuccessFlightInfo {
    public String carrier;
    public String startDate;
    public String flightNo;

    public PlanePaySuccessFlightInfo(String carrier, String startDate, String flightNo) {
        this.carrier = carrier;
        this.startDate = startDate;
        this.flightNo = flightNo;
    }
}
