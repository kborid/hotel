package com.huicheng.hotel.android.common;

/**
 * @auth kborid
 * @date 2017/12/18 0018.
 */

public enum PlaneOrderManager {
    Instance;
    private int flightType = PlaneCommDef.FLIGHT_SINGLE;
    public int getFlightType() {
        return flightType;
    }
    public void setFlightType(int flightType) {
        this.flightType = flightType;
    }
}
