package com.huicheng.hotel.android.common;

import com.huicheng.hotel.android.requestbuilder.bean.PlaneFlightInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneTicketInfoBean;

/**
 * @auth kborid
 * @date 2017/12/18 0018.
 */

public enum PlaneOrderManager {
    instance;


    private int flightType = PlaneCommDef.FLIGHT_SINGLE;
    private long offDate = 0;
    private long onDate = 0;
    private String offCity;
    private String offAirport;
    private String onCity;
    private String onAirport;
    private AirportInfo offAirportInfo;
    private AirportInfo onAirportInfo;
    private PlaneFlightInfoBean goFlightInfo;
    private PlaneFlightInfoBean backFlightInfo;
    private PlaneFlightInfoBean currFlightInfo;
    private PlaneTicketInfoBean goTicketInfo;
    private PlaneTicketInfoBean backTicketInfo;
    private PlaneTicketInfoBean currTicketInfo;


    public int getFlightType() {
        return flightType;
    }

    public void setFlightType(int flightType) {
        this.flightType = flightType;
    }

    public void setFlightOffDate(long offDate) {
        this.offDate = offDate;
    }

    public long getFlightOffDate() {
        return offDate;
    }

    public void setFlightOnDate(long onDate) {
        this.onDate = onDate;
    }

    public long getFlightOnDate() {
        return onDate;
    }

    public void setFlightOffCity(String offCity) {
        this.offCity = offCity;
    }

    public String getFlightOffCity() {
        return offCity;
    }

    public void setFlightOffAirport(String offAirport) {
        this.offAirport = offAirport;
    }

    public String getFlightOffAirport() {
        return offAirport;
    }

    public void setFlightOnCity(String onCity) {
        this.onCity = onCity;
    }

    public String getFlightOnCity() {
        return onCity;
    }

    public void setFlightOnAirport(String onAirport) {
        this.onAirport = onAirport;
    }

    public String getFlightOnAirport() {
        return onAirport;
    }

    public void setFlightOffAirportInfo(AirportInfo offAirportInfo) {
        this.offAirportInfo = offAirportInfo;
    }

    public AirportInfo getFlightOffAirportInfo() {
        return offAirportInfo;
    }

    public void setFlightOnAirportInfo(AirportInfo onAirportInfo) {
        this.onAirportInfo = onAirportInfo;
    }

    public AirportInfo getFlightOnAirportInfo() {
        return onAirportInfo;
    }

    public void setGoFlightInfo(PlaneFlightInfoBean goFlightInfo) {
        this.goFlightInfo = goFlightInfo;
        this.currFlightInfo = goFlightInfo;
    }

    public PlaneFlightInfoBean getGoFlightInfo() {
        return goFlightInfo;
    }

    public void setBackFlightInfo(PlaneFlightInfoBean backFlightInfo) {
        this.backFlightInfo = backFlightInfo;
        this.currFlightInfo = backFlightInfo;
    }

    public PlaneFlightInfoBean getBackFlightInfo() {
        return backFlightInfo;
    }

    public PlaneFlightInfoBean getCurrFlightInfo() {
        return currFlightInfo;
    }

    public void setGoTicketInfo(PlaneTicketInfoBean goTicketInfo) {
        this.goTicketInfo = goTicketInfo;
        this.currTicketInfo = goTicketInfo;
    }

    public PlaneTicketInfoBean getGoTicketInfo() {
        return goTicketInfo;
    }

    public void setBackTicketInfo(PlaneTicketInfoBean backTicketInfo) {
        this.backTicketInfo = backTicketInfo;
        this.currTicketInfo = backTicketInfo;
    }

    public PlaneTicketInfoBean getBackTicketInfo() {
        return backTicketInfo;
    }

    public PlaneTicketInfoBean getCurrTicketInfo() {
        return currTicketInfo;
    }

    public void reset() {
        flightType = PlaneCommDef.FLIGHT_SINGLE;
        offDate = 0;
        onDate = 0;
        offCity = "";
        offAirport = "";
        onCity = "";
        offAirport = "";
        offAirportInfo = null;
        onAirportInfo = null;
        goFlightInfo = null;
        backFlightInfo = null;
        currFlightInfo = null;
        goTicketInfo = null;
        backTicketInfo = null;
        currTicketInfo = null;
    }

    public class AirportInfo {
        public String name;
        public String _3Code;
        public String _4Code;
        public String cityName;
    }
}
