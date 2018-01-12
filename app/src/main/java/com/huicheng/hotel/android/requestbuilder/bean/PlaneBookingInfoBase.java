package com.huicheng.hotel.android.requestbuilder.bean;

import java.util.List;

/**
 * @auth kborid
 * @date 2018/1/11 0011.
 */

public class PlaneBookingInfoBase {
    public String bookingTag;
    public ExtInfo extInfo;
    public List<FlightInfo> flightInfo;
    public PriceInfo priceInfo;


    public static class ExtInfo{
        public String clientId;
        public String flightNum;
        public String flightType;
        public String policyId;
        public String policyType;
        public String qt;
    }

    public static class FlightInfo{
        public String arf;
        public String arr;
        public String arrAirport;
        public String arrCity;
        public String arrDate;
        public String arrTerminal;
        public String arrTime;
        public String cabin;
        public String carrier;
        public String carrierName;
        public String childCabin;
        public boolean codeShare;
        public String ctof;
        public String dpt;
        public String dptAirport;
        public String dptCity;
        public String dptDate;
        public String dptTerminal;
        public String dptTime;
        public String flightNum;
        public int stops;
        public String tof;
    }

    public static class PriceInfo{

    }
}
