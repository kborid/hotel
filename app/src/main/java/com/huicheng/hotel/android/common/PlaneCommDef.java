package com.huicheng.hotel.android.common;

/**
 * @auth kborid
 * @date 2017/12/18 0018.
 */

public class PlaneCommDef {
    //航班类型，单程或往返
    public static final int FLIGHT_SINGLE = 0;
    public static final int FLIGHT_GOBACK = 1;

    //航班预订状态，单程、往返去程和往返回程
    public static final int STATUS_GO = 0;
    public static final int STATUS_BACK = 1;

    //航班筛选：航班类型
    public static final int FLIGHT_TYPE_DEFAULT = 0;    //不限
    public static final int FLIGHT_TYPE_BIG = 1;        //大型机
    public static final int FLIGHT_TYPE_MID = 2;        //中型机
    public static final int FLIGHT_TYPE_SML = 3;        //小型机

    //航班筛选：航班仓位
    public static final int FLIGHT_CANG_DEFAULT = 0;    //不限
    public static final int FLIGHT_CANG_JINGJI = 1;     //经济舱
    public static final int FLIGHT_CANG_TOUDENG = 2;    //头等舱
    public static final int FLIGHT_CANG_SHANGWU = 3;    //商务舱

    //仓位等级
    public enum CabinLevel {
        CABIN_JINGJI("经济舱"),
        CABIN_SHANGWU("商务舱"),
        CABIN_TOUDENG("头等舱"),;

        String value;

        CabinLevel(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    //自定保险购买类型
    public static final int SAFE_BUY_NON = 0;           //一个都不买
    public static final int SAFE_BUY_YII = 1;           //只买意外险
    public static final int SAFE_BUY_DEL = 2;           //只买延误险
    public static final int SAFE_BUY_ALL = 3;           //全买

    //发票类型
    public static final int INVOICE_INVALID = 0;
    public static final int INVOICE_ALL = 1;            //全额发票
    public static final int INVOICE_XCD = 2;            //行程单

    //抬头类型
    public static final int RECEIVE_DANWEII = 1;        //单位
    public static final int RECEIVE_PERSONAL = 2;       //个人
    public static final int RECEIVE_BUSINESS = 3;       //企业
    public static final int RECEIVE_OFFICIAL = 4;       //政府
}
