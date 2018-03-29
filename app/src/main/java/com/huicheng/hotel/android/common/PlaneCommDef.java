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

    //证件类型
    public enum CardType {
        NI("NI", "身份证"),   //身份证
        PP("PP", "护照"),   //护照
        ID("ID", "其他"),;  //其他

        String valueId, valueName;

        CardType(String valueId, String valueName) {
            this.valueId = valueId;
            this.valueName = valueName;
        }

        public String getValueId() {
            return valueId;
        }

        public String getValueName() {
            return valueName;
        }
    }

    //机票订单状态
    public enum TicketOrderStatus {
        /**
         * 已取消
         */
        TICKET_CANCELED(-1),
        /**
         * 已完成
         */
        TICKET_FINISHED(1),
        /**
         * 待支付
         */
        TICKET_WAIT_PAY(2),
        /**
         * 已支付
         */
        TICKET_PAID(3),
        /**
         * 出票完成
         */
        TICKET_OUTED_COMP(13),
        /**
         * 改签完成
         */
        TICKET_CHANGED_COMP(16),
        /**
         * 退票成功
         */
        TICKET_BACKED_COMP(18),;

        int status;

        TicketOrderStatus(int status) {
            this.status = status;
        }

        public int getStatus() {
            return status;
        }

        public static TicketOrderStatus statusCodeOf(int status) {
            TicketOrderStatus orderStatus = TICKET_CANCELED;
            for (TicketOrderStatus ss : values()) {
                if (ss.getStatus() == status) {
                    orderStatus = ss;
                    break;
                }
            }
            return orderStatus;
        }
    }

    //机票乘客订票状态
    public enum PassengerStatus {
        Outing("00", "出票中"),
        Outed("02", "已出票"),
        Changing("10", "改签中"),
        Wait_Paying("11", "待支付"),
        Changed_Success("12", "已改签"),
        Changed_Failed("13", "改签失败"),
        Backing("20", "退票中"),
        Backed_Success("22", "退票完成"),
        Backed_Failed("23", "退票失败"),
        Canceled("30", "已取消"),;

        String statusCode, statusMsg;

        PassengerStatus(String statusCode, String statusMsg) {
            this.statusCode = statusCode;
            this.statusMsg = statusMsg;
        }

        public static PassengerStatus statusCodeOf(String statusCode) {
            PassengerStatus mStatus = PassengerStatus.Canceled;
            for (PassengerStatus status : values()) {
                if (status.getStatusCode().equals(statusCode)) {
                    mStatus = status;
                    break;
                }
            }
            return mStatus;
        }

        public String getStatusCode() {
            return statusCode;
        }

        public String getStatusMsg() {
            return statusMsg;
        }
    }

    //乘客类型
    public enum PassengerType {
        ADULT("成人"),
        CHILD("儿童"),;

        String type;

        PassengerType(String type) {
            this.type = type;
        }

        public String getPassengerType() {
            return type;
        }
    }

    public static final String ORDER_TYPE_BUY = "00";//订单类型-购买
    public static final String ORDER_TYPE_CHANGE = "01";//订单类型-改签
}
