package com.huicheng.hotel.android.net.bean;

import java.io.Serializable;
import java.util.List;

/**
 * @author kborid
 * @date 2017/3/8 0008
 */
public class OrderPayDetailInfoBean implements Serializable {
    public String amount;
    public List<AttachInfo> attachInfo;
    public String billDetail;
    public String checkInName;
    public String checkInTel;
    public String hotelID;
    public String invoice;
    public String location;
    public String name;
    public String orderID;
    public String orderNO;
    public String province;
    public String requirement;
    public String roomName;
    public int roomPrice;
    public String status;
    public long timeEnd;
    public long timeStart;
    public String specialComment;
    public int payWay;  //1、到店付，2、在线付
    public String checkRoomDate;
    public List<RoomDetailInfoBean.TotalPriceList> preTotalPriceList;
    public List<RoomDetailInfoBean.TotalPriceList> totalPriceList;


    public static class AttachInfo implements Serializable {
        public int custCount;
        public String orderId;
        public int orderMoney;
        public String serviceName;
        public int servicePrice;
        public int serviceVipPrice;
    }
}
