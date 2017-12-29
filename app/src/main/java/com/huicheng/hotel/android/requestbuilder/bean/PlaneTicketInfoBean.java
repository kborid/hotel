package com.huicheng.hotel.android.requestbuilder.bean;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * @auth kborid
 * @date 2017/12/27 0027.
 */

public class PlaneTicketInfoBean {
    public String actCode;
    public int arf;
    public String arrAirport;
    public String arrCode;
    public String arrTerminal;
    public String bairdrome;
    public String btime;
    public String carrier;
    public String code;
    public boolean codeShare;
    public String com;
    public String date;
    public String depAirport;
    public String depCode;
    public String depTerminal;
    public int distance;
    public String eairdrome;
    public String etime;
    public String flightType;
    public boolean meal;
    public boolean stop;
    public String stopAirportCode;
    public String stopAirportFullName;
    public String stopAirportName;
    public String stopCityCode;
    public String stopCityName;
    public int stopsNum;
    public int tof;
    public List<VendorInfo> vendors;
    public boolean zhiji;
    public String correct;

    public static class VendorInfo implements Comparable<VendorInfo> {
        public String afee;
        public int barePrice;
        public int basePrice;
        public String bprtag;
        public String businessExt;
        public String cabin;
        public String cabinCount;
        public int cabinType;
        public String discount;
        public String domain;
        public String it;
        public String policyId;
        public String policyType;
        public int price;
        public String prtag;
        public int vppr;
        public String wrapperId;

        public String com;

        @Override
        public int compareTo(@NonNull VendorInfo o) {
            return Integer.valueOf(this.cabinType).compareTo(o.cabinType);
        }
    }
}
