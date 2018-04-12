package com.huicheng.hotel.android.requestbuilder.bean;

import java.io.Serializable;

/**
 * @auth kborid
 * @date 2018/1/15 0015.
 */

public class PlaneBookingInfo_PriceInfo implements Serializable {
    public String arf;
    public String babyPrice;
    public String babyServiceFee;
    public String babyTicketPrice;
    public String barePrice;
    public String basePrice;
    public String childPrice;
    public String childTicketPrice;
    public String childtof;
    public String cutMoney;
    public String discount;
    public String dtTag;
    public Inventory inventory;
    public String prdTag;
    public String price;
    public PlaneBookingInfo_PriceTagInfo priceTag;
    public String returnMoney;
    public String ticketPrice;
    public String tof;

    public static class Inventory implements Serializable{
        public String adult;
        public String all;
        public String baby;
        public String child;
    }
}
