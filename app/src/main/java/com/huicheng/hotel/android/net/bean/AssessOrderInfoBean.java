package com.huicheng.hotel.android.net.bean;

import java.io.Serializable;

/**
 * @author kborid
 * @date 2017/2/17 0017
 */
public class AssessOrderInfoBean implements Serializable{
    public String arrivalTime;
    public String cityName;
    public long createTime;
    public int during;
    public long endTime;
    public int evaluateid;
    public String grade;
    public int hotelId;
    public String hotelName;
    public int id;
    public String isevaluated; //0或者null-未评价 1-已评价 2-评价被删除
    public int orderMoney;
    public String orderName;
    public String orderNo;
    public String payNo;
    public String remark;
    public int roomId;
    public String roomName;
    public String specialComment;
    public long startTime;
    public int status;
    public int totalMoney;
    public int type;
    public long updateTime;
    public String userId;
    public String userMobile;
    public String userName;
}
