package com.huicheng.hotel.android.common;

import com.huicheng.hotel.android.requestbuilder.bean.PlaneFlightInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneTicketInfoBean;
import com.prj.sdk.util.LogUtil;

/**
 * @auth kborid
 * @date 2017/12/18 0018.
 */

public enum PlaneOrderManager {
    instance;

    private static final String TAG = "PlaneOrderManager";
    private int status = PlaneCommDef.STATUS_GO;
    private int flightType = PlaneCommDef.FLIGHT_SINGLE;

    private AirportInfo offAirportInfo;
    private AirportInfo onAirportInfo;

    private long goOffDate = 0;
    private PlaneFlightInfoBean goFlightInfo;
    private PlaneTicketInfoBean goTicketInfo;
    private PlaneTicketInfoBean.VendorInfo goVendorInfo;

    private long backOffDate = 0;
    private PlaneFlightInfoBean backFlightInfo;
    private PlaneTicketInfoBean backTicketInfo;
    private PlaneTicketInfoBean.VendorInfo backVendorInfo;


    public int getFlightType() {
        return flightType;
    }

    public void setFlightType(int flightType) {
        this.flightType = flightType;
    }

    public void setGoFlightOffDate(long goOffDate) {
        this.goOffDate = goOffDate;
    }

    public long getGoFlightOffDate() {
        return goOffDate;
    }

    public void setBackFlightOffDate(long backOffDate) {
        this.backOffDate = backOffDate;
    }

    public long getBackFlightOffDate() {
        return backOffDate;
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


    public PlaneFlightInfoBean getGoFlightInfo() {
        return goFlightInfo;
    }

    public PlaneFlightInfoBean getBackFlightInfo() {
        return backFlightInfo;
    }

    public PlaneTicketInfoBean getGoTicketInfo() {
        return goTicketInfo;
    }

    public PlaneTicketInfoBean getBackTicketInfo() {
        return backTicketInfo;
    }

    public PlaneTicketInfoBean.VendorInfo getGoVendorInfo() {
        return goVendorInfo;
    }

    public PlaneTicketInfoBean.VendorInfo getBackVendorInfo() {
        return backVendorInfo;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }


    public void setFlightInfo(PlaneFlightInfoBean flightInfo) {
        if (isBackBookingTypeForGoBack()) {
            this.backFlightInfo = flightInfo;
        } else {
            this.goFlightInfo = flightInfo;
        }
    }

    public PlaneFlightInfoBean getFlightInfo() {
        if (isBackBookingTypeForGoBack()) {
            return backFlightInfo;
        } else {
            return goFlightInfo;
        }
    }

    public void setTicketInfo(PlaneTicketInfoBean ticketInfo) {
        if (isBackBookingTypeForGoBack()) {
            this.backTicketInfo = ticketInfo;
        } else {
            this.goTicketInfo = ticketInfo;
        }
    }

    public PlaneTicketInfoBean getTicketInfo() {
        if (isBackBookingTypeForGoBack()) {
            return backTicketInfo;
        } else {
            return goTicketInfo;
        }
    }

    public void setVendorInfo(PlaneTicketInfoBean.VendorInfo vendorInfo) {
        if (isBackBookingTypeForGoBack()) {
            this.backVendorInfo = vendorInfo;
        } else {
            this.goVendorInfo = vendorInfo;
        }
    }

    public PlaneTicketInfoBean.VendorInfo getVendorInfo() {
        if (isBackBookingTypeForGoBack()) {
            return backVendorInfo;
        } else {
            return goVendorInfo;
        }
    }

    public boolean isFlightGoBack() {
        return flightType == PlaneCommDef.FLIGHT_GOBACK;
    }

    public boolean isBackBookingTypeForGoBack() {
        LogUtil.i(TAG, "isBackBookingTypeForGoBack()");
        return flightType == PlaneCommDef.FLIGHT_GOBACK && status == PlaneCommDef.STATUS_BACK;
    }

    public boolean isGoBookingTypeForGoBack() {
        LogUtil.i(TAG, "isGoFlightInTypeAll()");
        return flightType == PlaneCommDef.FLIGHT_GOBACK && status == PlaneCommDef.STATUS_GO;
    }

    public void reset() {
        status = PlaneCommDef.STATUS_GO;
        flightType = PlaneCommDef.FLIGHT_SINGLE;
        goOffDate = 0;
        backOffDate = 0;
        offAirportInfo = null;
        onAirportInfo = null;

        goFlightInfo = null;
        backFlightInfo = null;
        goTicketInfo = null;
        backTicketInfo = null;
        goVendorInfo = null;
        backVendorInfo = null;
    }

    public static class AirportInfo {
        public String name;
        public String _3Code;
        public String _4Code;
        public String cityName;
    }
}
