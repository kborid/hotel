package com.huicheng.hotel.android.requestbuilder.bean;

import java.io.Serializable;
import java.util.List;

/**
 * @author kborid
 * @date 2018/3/21 0021.
 */

public class PlaneOrderDetailInfoBean implements Serializable {
    public int bonusAmount;
    public int commission;
    public String contactor;
    public long createTime;
    public long paidTime;
    public String email;
    public String exAddress;
    public int invoiceType;
    public String mobile;
    public String orderId;
    public String orderStatus;
    public String orderType;
    public String payNo;
    public String paychannel;
    public String receiverMobile;
    public String receiverName;
    public String receiverTitle;
    public int shouldPayAmount;
    public String taxpayerId;
    public List<TripInfo> tripList;
    public String type;
    public long updateTime;
    public String userId;

    public static class TripInfo implements Serializable {
        public int accidentCount;
        public int accidentMoney;
        public String airCabin;
        public String airco;
        public String aircoName;
        public int buildAndFuelPrice;
        public String clientSite;
        public String contactor;
        public long createtime;
        public int delayCount;
        public int delayMoney;
        public String eAirport;
        public String eAirportCode;
        public String eTerminal;
        public String eTime;
        public String ecity;
        public String effectiveOrderNo;
        public String flightNo;
        public String flyTime;
        public String mainOrderId;
        public String mobile;
        public String orderStatus;
        public String orderType;
        public int qOrderId;
        public String qOrderNo;
        public String sAirport;
        public String sAirportCode;
        public long sDate;
        public String sTerminal;
        public String sTime;
        public String scity;
        public int shouldPayAmount;
        public String tradeNo;
        public String tripId;
        public int tripType;
        public long updatetime;
        public List<PassengerInfo> passengerList;
        public boolean stop;
        public String stopCityName;
    }

    public static class PassengerInfo implements Serializable {
        public int barePrice;
        public String birthday;
        public int buildPrice;
        public String cardNo;
        public String cardType;
        public int fuelPrice;
        public String id;
        public String name;
        public String originTickno;
        public String passengerType;
        public String qPassengerId;
        public int sex;
        public String status;
        public String ticketno;
        public String tripId;
        public List<RefundAmountInfo> refundAmount;
        public int insureAmount;
    }

    public static class RefundAmountInfo implements Serializable {
        public int code;
        public int nextRefundFee;
        public int nextReturnRefundFee;
        public int refundFee;
        public int returnRefundFee;
    }
}
