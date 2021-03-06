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

    // Hotel Free Coupon Status
    public static final String COUPON_NONE = "0";
    public static final String COUPON_HAVE = "1";

    //全文搜索type
    public static final String TYPE_HOTEL = "00";
    public static final String TYPE_LAND_MARK = "01";

    // Hotel Vip
    public static final String VIP_SUPPORT = "1";
    public static final String VIP_NOT_SUPPORT = "0";

    // Hotel Cert ICON
    public static final String CERT_NULL = "N";
    public static final String CERT_GOLD = "G";
    public static final String CERT_SILVER = "S";

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
    public static final String UNION_QMHUA = "04";

    public static String getPayChannel(int index) {
        String payChannel = ALIPAY;
        switch (index) {
            case 0:
                payChannel = ALIPAY;
                break;
            case 1:
                payChannel = WXPAY;
                break;
            case 2:
                payChannel = UNIONPAY;
                break;
            case 3:
                payChannel = UNION_QMHUA;
                break;
        }
        return payChannel;
    }

    //酒店评分
    public static String[] convertConsiderPoint(int index) {
        String[] point;
        switch (index) {
            case 0:
                point = new String[]{"4.5", ""};
                break;
            case 1:
                point = new String[]{"3.5", "4.5"};
                break;
            case 2:
                point = new String[]{"", "3.5"};
                break;
            default:
                point = new String[]{"", ""};
                break;
        }
        return point;
    }

    //酒店价格
    public static String[] convertConsiderPrice(int index) {
        String[] price;
        switch (index) {
            case 0:
                price = new String[]{"", "300"};
                break;
            case 1:
                price = new String[]{"300", "400"};
                break;
            case 2:
                price = new String[]{"400", "500"};
                break;
            case 3:
                price = new String[]{"500", "800"};
                break;
            case 4:
                price = new String[]{"800", "1200"};
                break;
            case 5:
                price = new String[]{"1200", ""};
                break;
            default:
                price = new String[]{"", ""};
                break;
        }
        return price;
    }

    //酒店星级
    public static String convertConsiderGrade(int index) {
        String grade;
        switch (index) {
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
                grade = "";
                break;
        }
        return grade;
    }

    //酒店类型
    public static String convertConsiderType(int index) {
        String type;
        switch (index) {
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

    //酒店星级
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

    //出行类型
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
            default:
                travel = "";
                break;
        }
        return travel;
    }
}
