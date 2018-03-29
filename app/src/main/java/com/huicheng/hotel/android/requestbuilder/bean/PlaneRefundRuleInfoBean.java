package com.huicheng.hotel.android.requestbuilder.bean;

import java.io.Serializable;
import java.util.List;

/**
 * @author kborid
 * @date 2018/3/27 0027.
 */

public class PlaneRefundRuleInfoBean implements Serializable {
    public String childTgqMsg;
    public boolean hasTime;
    public String signText;
    public String tgqText;
    public List<TimePointChargeInfo> timePointChargeList;
    public String timePointCharges;
    public int viewType;

    public static class TimePointChargeInfo implements Serializable {
        public int changeFee;
        public int returnFee;
        public int time;
        public String timeText;
    }
}
