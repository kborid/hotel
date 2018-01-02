package com.huicheng.hotel.android.requestbuilder.bean;

import java.util.Comparator;

/**
 * @auth kborid
 * @date 2017/12/22 0022.
 */

public class PlaneFlightInfoBean implements Comparator<PlaneFlightInfoBean> {
    public String stopCityName;
    public String arr;
    public String bfTag;
    public String arrAirport;
    public String flightTimes;
    public boolean codeShare;
    public boolean stop;
    public String stopAirportName;
    public float discount;
    public int stopsNum;
    public String arrTerminal;
    public int tof;
    public String tag;
    public String flightNum;
    public String actFlightNum;
    public int minVppr;
    public String stopCityCode;
    public int arf;
    public boolean meal;
    public String dptTime;
    public String cabin;
    public String dpt;
    public String planetype;
    public String stopAirportFullName;
    public String stopAirportCode;
    public String dptTerminal;
    public double bfPrice;
    public double barePrice;
    public String dptAirport;
    public String arrTime;
    public String flightTypeFullName;
    public double bfBarePrice;
    public String carrier;
    public String distance;
    public int positionLevel;

    @Override
    public int compare(PlaneFlightInfoBean o1, PlaneFlightInfoBean o2) {
        return o1.flightTimes.compareTo(o2.flightTimes);
    }
}
