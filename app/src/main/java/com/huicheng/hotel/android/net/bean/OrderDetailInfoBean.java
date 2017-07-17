package com.huicheng.hotel.android.net.bean;

import java.io.Serializable;
import java.util.List;

/**
 * @author kborid
 * @date 2017/3/8 0008
 */
public class OrderDetailInfoBean implements Serializable {
    public long beginDate;
    public long beginDateLong;
    public String cityName;
    public long createtime;
    public int during;
    public long endDate;
    public long endDateLong;
    public long flyDateLong;
    public long flydate;
    public String hotelName;
    public String isback;
    public int majorPrice;
    public String message;
    public String orderId;
    public String orderNo;
    public String orderStatus;
    public String orderStatusName;
    public String orderType;
    public String payStatus;
    public String roomName;
    public List<Object> serviceList;
    public int totalPrice;
    public int type;
    public String checkInAndOutDate;
}
