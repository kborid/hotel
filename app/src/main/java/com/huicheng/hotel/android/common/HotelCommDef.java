package com.huicheng.hotel.android.common;

/**
 * @author kborid
 * @date 2017/2/21 0021
 */
public class HotelCommDef {
    // 标签索引
    public static final String ALLDAY = "all";
    public static final String CLOCK = "clock";
    public static final String YEGUIREN = "yeguiren";
    public static final String HOUHUIYAO = "houhuiyao";

    // Hotel Type
    public static final int TYPE_ALL = 1;
    public static final int TYPE_YEGUIREN = 2;
    public static final int TYPE_FREE = 3;
    public static final int TYPE_CLOCK = 4;
    public static final int TYPE_HOUHUIYAO = 5;

    // Hotel Channel
    public static final String SHARE_FREE = "01";
    public static final String SHARE_HOTEL = "02";
    public static final String SHARE_ROOM = "03";
    public static final String SHARE_TIE = "04";

    // Hotel Free Coupon Status
    public static final String COUPON_NONE = "0";
    public static final String COUPON_HAVE = "1";

    // Hotel Vip
    public static final String VIP_SUPPORT = "1";
    public static final String VIP_NOT_SUPPORT = "0";

    //Invoice Type
    public static final String COMMON_INVOICE = "0";
    public static final String SPECIAL_INVOICE = "1";

    // Pay Type
    public static final String PAY_ARR = "02";
    public static final String PAY_PRE = "01";

    // Hotel Order Status
    public static final int Canceled = -1;
    public static final int Finished = 1;
    public static final int WaitPay = 2;
    public static final int Payed = 3;
    public static final int Transform = 4;
    public static final int Confirmed = 5;
    public static final int WaitConfirm = 8;
    public static final int Processing = 9;
    public static final int Transforming = 10;
    public static final int ReFunding = 11;
    public static final int ReFunded = 12;

    // Plane Ticket Order Status
    public static final int BuyTicket = 1;
    public static final int ReturnTicket = 2;
    public static final int ChangeTicket = 3;

    // Order Type
    public static final String Order_Hotel = "1";
    public static final String Order_Plane = "2";

    // 支付
    public static final String ALIPAY = "01";
    public static final String WXPAY = "02";
    public static final String UNIONPAY = "03";

    public static String getPayChannel(int index) {
        String payChannel = ALIPAY;
        switch (index) {
            case 0:
                payChannel = UNIONPAY;
                break;
            case 1:
                payChannel = ALIPAY;
                break;
            case 2:
                payChannel = WXPAY;
                break;
        }
        return payChannel;
    }

    // Hotel Point
    public static String[] convertHotelPoint(int index) {
        String[] point;
        switch (index) {
            case -1:
                point = new String[]{"", ""};
                break;
            case 0:
                point = new String[]{"4.5", ""};
                break;
            case 1:
                point = new String[]{"3.5", "4.5"};
                break;
            case 2:
                point = new String[]{"0", "3.5"};
                break;
            default:
                point = new String[]{"", ""};
                break;
        }
        return point;
    }

    // Hotel Price
    public static String convertHotelPrice(int index) {
        String price = null;
        switch (index) {
            case 0:
                price = "0";
                break;
            case 1:
                price = "100";
                break;
            case 2:
                price = "300";
                break;
            case 3:
                price = "500";
                break;
            case 4:
                price = "1000";
                break;
            case 5:
                price = "2000";
                break;
            case 6:
                price = "";
                break;
            default:
                price = "0";
                break;
        }
        return price;
    }

    // Hotel Grade
    public static String convertHotelGrade(int index) {
        String grade = null;
        switch (index) {
            case -1:
                grade = "";
                break;
            case 0:
                grade = "3";
                break;
            case 1:
                grade = "4";
                break;
            case 2:
                grade = "5";
                break;
            default:
                grade = "3";
                break;
        }
        return grade;
    }

    // Hotel Type
    public static String convertHotelType(int index) {
        String type = "";
        switch (index) {
            case -1:
                type = "";
                break;
            case 0:
                type = "01";
                break;
            case 1:
                type = "02";
                break;
            case 2:
                type = "03";
                break;
            case 3:
                type = "04";
                break;
            default:
                type = "";
                break;
        }
        return type;
    }

    public static String convertHotelStar(int star) {
        String starStr = "";
        switch (star) {
            case 1:
                starStr = "一星";
                break;
            case 2:
                starStr = "二星";
                break;
            case 3:
                starStr = "三星";
                break;
            case 4:
                starStr = "四星";
                break;
            case 5:
                starStr = "五星";
                break;
            default:
                starStr = "一星";
                break;
        }
        return starStr;
    }

    //TravelType
    public static String convertTravelType(int index) {
        String travel = "00";
        switch (index) {
            case 0:
                travel = "00";
                break;
            case 1:
                travel = "01";
                break;
            case 2:
                travel = "02";
                break;
            case 3:
                travel = "03";
                break;
            case 4:
                travel = "04";
                break;
        }
        return travel;
    }
}
