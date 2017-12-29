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
    private PlaneCommDef.GoBackStatus status = PlaneCommDef.GoBackStatus.STATUS_GO;
    private int flightType = PlaneCommDef.FLIGHT_SINGLE;

    private String offCity;
    private String onCity;
    private String offAirport;
    private String onAirport;
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

    public void setStatus(PlaneCommDef.GoBackStatus status) {
        this.status = status;
    }

    public PlaneCommDef.GoBackStatus getStatus() {
        return status;
    }


    public void setFlightInfo(PlaneFlightInfoBean flightInfo) {
        if (PlaneCommDef.FLIGHT_SINGLE == flightType) {
            this.goFlightInfo = flightInfo;
        } else {
            if (PlaneCommDef.GoBackStatus.STATUS_GO == status) {
                this.goFlightInfo = flightInfo;
            } else {
                this.backFlightInfo = flightInfo;
            }
        }
    }

    public PlaneFlightInfoBean getFlightInfo() {
        if (PlaneCommDef.FLIGHT_SINGLE == flightType) {
            return goFlightInfo;
        } else {
            if (PlaneCommDef.GoBackStatus.STATUS_GO == status) {
                return goFlightInfo;
            } else {
                return backFlightInfo;
            }
        }
    }

    public void setTicketInfo(PlaneTicketInfoBean ticketInfo) {
        if (PlaneCommDef.FLIGHT_SINGLE == flightType) {
            this.goTicketInfo = ticketInfo;
        } else {
            if (PlaneCommDef.GoBackStatus.STATUS_GO == status) {
                this.goTicketInfo = ticketInfo;
            } else {
                this.backTicketInfo = ticketInfo;
            }
        }
    }

    public PlaneTicketInfoBean getTicketInfo() {
        if (PlaneCommDef.FLIGHT_SINGLE == flightType) {
            return goTicketInfo;
        } else {
            if (PlaneCommDef.GoBackStatus.STATUS_GO == status) {
                return goTicketInfo;
            } else {
                return backTicketInfo;
            }
        }
    }

    public void setFlightVendorInfo(PlaneTicketInfoBean.VendorInfo vendorInfo) {
        if (PlaneCommDef.FLIGHT_SINGLE == flightType) {
            this.goVendorInfo = vendorInfo;
        } else {
            if (PlaneCommDef.GoBackStatus.STATUS_GO == status) {
                this.goVendorInfo = vendorInfo;
            } else {
                this.backVendorInfo = vendorInfo;
            }
        }
    }

    public PlaneTicketInfoBean.VendorInfo getFlightVendorInfo() {
        if (PlaneCommDef.FLIGHT_SINGLE == flightType) {
            return goVendorInfo;
        } else {
            if (PlaneCommDef.GoBackStatus.STATUS_GO == status) {
                return goVendorInfo;
            } else {
                return backVendorInfo;
            }
        }
    }

    public boolean isBackFlightBack() {
        LogUtil.i(TAG, "FlightType = " + flightType + ", FlowStatus = " + status);
        return flightType == PlaneCommDef.FLIGHT_GO_BACK && status == PlaneCommDef.GoBackStatus.STATUS_BACK;
    }

    public void reset() {
        status = PlaneCommDef.GoBackStatus.STATUS_GO;
        flightType = PlaneCommDef.FLIGHT_SINGLE;
        goOffDate = 0;
        backOffDate = 0;
        offCity = "";
        offAirport = "";
        onCity = "";
        offAirport = "";
        offAirportInfo = null;
        onAirportInfo = null;

        goFlightInfo = null;
        backFlightInfo = null;
        goTicketInfo = null;
        backTicketInfo = null;
    }

    public class AirportInfo {
        public String name;
        public String _3Code;
        public String _4Code;
        public String cityName;
    }
}
