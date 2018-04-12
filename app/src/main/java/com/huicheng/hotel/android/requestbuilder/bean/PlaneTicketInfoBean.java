package com.huicheng.hotel.android.requestbuilder.bean;

import java.io.Serializable;
import java.util.List;

/**
 * @auth kborid
 * @date 2017/12/27 0027.
 */

public class PlaneTicketInfoBean implements Serializable {
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
    public List<PlaneVendorInfoBean> vendors;
    public boolean zhiji;
    public String correct;
}
