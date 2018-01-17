package com.huicheng.hotel.android.requestbuilder.bean;

import java.util.List;

/**
 * @auth kborid
 * @date 2018/1/11 0011.
 */

public class PlaneBookingInfo_TgqShowDataInfo {
    public boolean airlineTgq;
    public boolean allowChange;
    public boolean canCharge;
    public boolean canRefund;
    public String changeText;
    public String returnText;
    public String signText;
    public int tgqFrom;
    public List<TgqPointChargesInfo> tgqPointCharges;
    public String tgqText;

    public static class TgqPointChargesInfo {
        public int changeFee;
        public int returnFee;
        public int time;
        public String timeText;
    }
}
