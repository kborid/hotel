package com.huicheng.hotel.android.common;

/**
 * @auth kborid
 * @date 2017/12/18 0018.
 */

public enum PlaneOrderManager {
    instance;

    private int flightType = PlaneCommDef.FLIGHT_SINGLE;
    public int getFlightType() {
        return flightType;
    }
    public void setFlightType(int flightType) {
        this.flightType = flightType;
    }

    private long offDate = 0;
    public void setFlightOffDate(long offDate) {
        this.offDate = offDate;
    }
    public long getFlightOffDate() {
        return offDate;
    }

    private long onDate = 0;
    public void setFlightOnDate(long onDate) {
        this.onDate = onDate;
    }
    public long getFlightOnDate(){
        return onDate;
    }

    private String offCity;
    public void setFlightOffCity(String offCity){
        this.offCity = offCity;
    }
    public String getFlightOffCity(){
        return offCity;
    }

    private String offAirport;
    public void setFlightOffAirport(String offAirport){
        this.offAirport = offAirport;
    }
    public String getFlightOffAirport(){
        return offAirport;
    }

    private String onCity;
    public void setFlightOnCity(String onCity){
        this.onCity = onCity;
    }
    public String getFlightOnCity(){
        return onCity;
    }

    private String onAirport;
    public void setFlightOnAirport(String onAirport){
        this.onAirport = onAirport;
    }
    public String getFlightOnAirport(){
        return onAirport;
    }

    public void reset(){}
}
