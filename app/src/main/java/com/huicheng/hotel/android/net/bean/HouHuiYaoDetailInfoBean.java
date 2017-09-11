package com.huicheng.hotel.android.net.bean;

import java.util.List;

/**
 * @author kborid
 * @date 2017/2/27 0027
 */
public class HouHuiYaoDetailInfoBean {
    public String address;
    public List<Attachs> attachs;
    public int buyprice;
    public long endtime;
    public String guestQualification;
    public int hotelId;
    public String lat;
    public String lon;
    public String name;
    public String phone;
    public String picpath;
    public int roomId;
    public String roomName;
    public int scala;
    public int sellprice;
    public String servicePhone;
    public int star;
    public long starttime;


    public static class Attachs {
        public int serviceCnt;
        public String orderId;
        public int orderMoney;
        public String serviceName;
        public int servicePrice;
        public int serviceVipPrice;
    }
}
