package com.huicheng.hotel.android.net.bean;

import java.util.List;

/**
 * @author kborid
 * @date 2017/2/20 0020
 */
public class CouponInfoBean {
    public ActivityInfo activity;
    public List<CouponInfo> coupon;

    public static class ActivityInfo {
        public long createTime;
        public boolean deleted;
        public int during;
        public long endTime;
        public long endtime;
        public int id;
        public String name;
        public int roomCnt;
        public int roomNum;
        public long startTime;
        public long starttime;
        public String statusCode;
        public String statusName;
    }

    public static class CouponInfo {
        public long activeTime;
        public int activityId;
        public String activityName;
        public String cityName;
        public int cnt;
        public String code;
        public long createTime;
        public String featurePicPath;
        public String grade;
        public int hotelId;
        public String hotelName;
        public int id;
        public String name;
        public String provinceName;
        public int roomId;
        public String roomName;
        public int status;
        public int type;
        public String userId;
    }
}
