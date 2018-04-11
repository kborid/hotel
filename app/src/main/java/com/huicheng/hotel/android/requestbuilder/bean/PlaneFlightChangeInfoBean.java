package com.huicheng.hotel.android.requestbuilder.bean;

import java.io.Serializable;

/**
 * @author kborid
 * @date 2018/4/11 0011.
 */

public class PlaneFlightChangeInfoBean implements Serializable {
    public String actFlightNo;
    public String adultUFee;
    public String adultUFeeText;
    public String allFee;
    public String allFeeText;
    public String arrAirportCode;
    public String arrTerminal;
    public String cabin;
    public String cabinCode;
    public String cabinStatus;
    public String cabinStatusText;
    public String carrier;
    public String dptAirportCode;
    public String dptTerminal;
    public String endPlace;
    public String endTime;
    public String extraPrice;
    public String extraPriceText;
    public String flight;
    public String flightNo;
    public String flightType;
    public String gqFee;
    public String gqFeeText;
    public String sDate;
    public boolean share;
    public String startPlace;
    public String startTime;
    public StopInfo stopFlightInfo;
    public String stopTypeDesc;
    public String uniqKey;
    public String upgradeFee;
    public String upgradeFeeText;

    public static class StopInfo implements Serializable {
        public String stopCityInfoList;
        public String stopTypeDesc;
        public int stopType;
    }
}
