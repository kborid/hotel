package com.huicheng.hotel.android.net.bean;

import java.io.Serializable;
import java.util.List;

/**
 * @author kborid
 * @date 2017/2/22 0022
 */
public class RoomDetailInfoBean implements Serializable {

    public String address;
    public String hotelName;
    public List<ChooseService> chooseList;
    public List<ChooseService> chooseList_free;
    public List<ChooseService> offlineChooseList;
    public List<ChooseService> offlineChooseList_free;
    public String evaluateCount;
    public String grade;
    public String isSupportAfterPay;
    public String isvip;
    public List<String> picList;
    public int preTotalPrice;
    public List<TotalPriceList> preTotalPriceList;
    public int preUnitPrice;
    public String roomName;
    public List<ServiceList> serviceList;
    public int totalPrice;
    public List<TotalPriceList> totalPriceList;
    public int unitPrice;

    public static class ChooseService implements Serializable {
        public String editable;
        public int id;
        public int limitCnt;
        public int price;
        public String serviceName;
        public int unitPrice;
        public int vipPrice;
        public String detail;
        public String pics;
    }

    public static class TotalPriceList implements Serializable {
        public long activeTime;
        public long activeTimeStamp;
        public String price;
    }

    public static class ServiceList implements Serializable {
        public List<CapacityVOList> capacityVOList;
        public String remarks;
        public String title;
    }

    public static class CapacityVOList implements Serializable {
        public String count;
        public String iconUrl;
        public String name;
    }
}
