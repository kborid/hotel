package com.huicheng.hotel.android.content;

import com.prj.sdk.BuildConfig;

/**
 * 项目常量
 */
public final class AppConst {
    public static final boolean ISDEVELOP = BuildConfig.DEBUG; // 开发者模式

    public static final String APPINFO = "app_info"; //app版本信息
    public static final String IGNORE_UPDATE_VERSION = "ignore_update_version"; //忽略更新版本
    public static final String APPTYPE = "type"; // 类型：0代表UAT，1代表生产
    public static final String DEV_URL = "dev_url";

    public static final String IS_FIRST_LAUNCH = "first_launch"; //是否第一次启动
    public static final String SKIN_INDEX = "skin_index"; //皮肤资源index
    public static final String HAS_SHOW_VIP_TIPS = "has_show_vip_tips"; //是否已经显示vip提示

    public static final String CITY_HOTEL_JSON_FILE = "hotel_city_json_file"; //酒店模块城市选择文件缓存
    public static final String CITY_HOTEL_JSON = "hotel_city_json_string"; //酒店模块城市选择缓存
    public static final String CITY_LIST_JSON_VERSION = "city_list_json_version"; //通用城市列表版本号
    public static final String CITY_PLANE_JSON = "plane_city_json_string"; //机场缓存
    public static final String AIR_COMPANY_JSON = "air_company_json"; //航司缓存

    public static final String IN_PERSON_INFO = "in_person_info";

    public static final String ACCESS_TICKET = "accessTicket"; // 记录用户登录ticket
    public static final String LAST_LOGIN_DATE = "last_login_time"; // 上次登录时间
    public static final String USERNAME = "username"; // 用户名
    public static final String USER_PHOTO_URL = "user_photo_url"; // 用户头像地址
    public static final String USER_INFO = "user_info"; // 用户信息

    public static final String HISTORY = "history";
    public static final String AIRPORT_HISTORY = "airport_history";

    //粉丝申请用户信息缓存
    public static final String FANS_NAME = "fans_name";
    public static final String FANS_ID = "fans_id";
    public static final String FANS_EMAIL = "fans_email";

    public static final String APPID = "ANDROID-0571-0001"; // appid
    public static final String APPKEY = "fbe938c4bfe0a7cda1dcae7c85c7f83e37736207d637dc1d";
    public static final String VERSION = "2.0"; // version

    public static final String SORT_INDEX = "sort_index";
    public static final String CONSIDER_POINT = "consider_point";
    public static final String CONSIDER_PRICE = "consider_price";
    public static final String CONSIDER_GRADE = "consider_grade";
    public static final String CONSIDER_TYPE = "consider_type";

    public static final int ACTIVITY_IMAGE_CAPTURE = 0x01;
    public static final int ACTIVITY_GET_IMAGE = 0x02;
    public static final int ACTIVITY_TAILOR = 0x03;

    // BUSINESSTYPE
    public static final String BUSINESS_TYPE_REGISTER = "regist001";
    public static final String BUSINESS_TYPE_CHANGEPHONE = "user001";
    public static final String BUSINESS_TYPE_FINDPWD = "user002";
    public static final String BUSINESS_TYPE_BIND = "thirdpartlogin001";

    // 数据请求常量
    private static final int HOTEL_HANDLE_FLAG = 0x0000;
    // 上传图片
    public static final int UPLOAD = HOTEL_HANDLE_FLAG + 1;
    // 用户模块
    public static final int REGISTER = HOTEL_HANDLE_FLAG + 2;
    public static final int LOGIN = HOTEL_HANDLE_FLAG + 3;
    public static final int CHECK_USER = HOTEL_HANDLE_FLAG + 4;
    public static final int CHECK_NAME = HOTEL_HANDLE_FLAG + 5;
    public static final int GET_USER_INFO = HOTEL_HANDLE_FLAG + 6;
    public static final int REMOVE_TICKET = HOTEL_HANDLE_FLAG + 7;
    public static final int CHANGE_PHONENUMBER = HOTEL_HANDLE_FLAG + 8;
    public static final int CHECK_PHONE = HOTEL_HANDLE_FLAG + 9;
    public static final int GET_YZM = HOTEL_HANDLE_FLAG + 10;
    public static final int CHECK_YZM = HOTEL_HANDLE_FLAG + 11;
    public static final int FIND_PASSWORD = HOTEL_HANDLE_FLAG + 12;
    public static final int CHANGE_PASSWORD = HOTEL_HANDLE_FLAG + 13;
    // 个人中心
    public static final int CENTER_USERINFO = HOTEL_HANDLE_FLAG + 14;
    public static final int SAVE_USERINFO = HOTEL_HANDLE_FLAG + 15;
    public static final int UPDATE_USERINFO = HOTEL_HANDLE_FLAG + 16;
    // 消息
    public static final int ALL_MESSAGE = HOTEL_HANDLE_FLAG + 17;
    public static final int MESSAGE_COUNT = HOTEL_HANDLE_FLAG + 18;
    public static final int MESSAGE_UPDATE = HOTEL_HANDLE_FLAG + 19;
    // 酒店详情、列表
    public static final int HOTEL_DETAIL = HOTEL_HANDLE_FLAG + 20;

    public static final int FEEDBACK = HOTEL_HANDLE_FLAG + 21;
    public static final int ASSESS_ORDER = HOTEL_HANDLE_FLAG + 22;
    public static final int ASSESS_DETAIL = HOTEL_HANDLE_FLAG + 23;
    public static final int VIP_HOTEL = HOTEL_HANDLE_FLAG + 24;
    public static final int COUPON_ALL = HOTEL_HANDLE_FLAG + 25;
    public static final int ROOM_DETAIL = HOTEL_HANDLE_FLAG + 26;

    public static final int HOTEL_LIST = HOTEL_HANDLE_FLAG + 27;
    public static final int HHY_LIST = HOTEL_HANDLE_FLAG + 28;
    public static final int HHY_DETAIL = HOTEL_HANDLE_FLAG + 29;

    public static final int FREE_CURRENT_ACTIVE = HOTEL_HANDLE_FLAG + 30;
    public static final int FREE_ACTIVE_DETAIL = HOTEL_HANDLE_FLAG + 31;
    public static final int FREE_GRAB_COUPON = HOTEL_HANDLE_FLAG + 32;

    public static final int HOTEL_COMMENT = HOTEL_HANDLE_FLAG + 33;

    public static final int BIND_CHECK = HOTEL_HANDLE_FLAG + 34;
    public static final int BIND = HOTEL_HANDLE_FLAG + 35;

    public static final int SAVE_RECOMMAND = HOTEL_HANDLE_FLAG + 36;

    public static final int ROOM_CONFIRM_ORDER = HOTEL_HANDLE_FLAG + 37;
    public static final int HOTEL_VIP = HOTEL_HANDLE_FLAG + 38;

    public static final int PAY_ORDER_DETAIL = HOTEL_HANDLE_FLAG + 39;
    public static final int ORDER_LIST = HOTEL_HANDLE_FLAG + 40;
    public static final int ORDER_DETAIL = HOTEL_HANDLE_FLAG + 41;
    public static final int ORDER_CANCEL = HOTEL_HANDLE_FLAG + 42;
    public static final int ORDER_MODIFY = HOTEL_HANDLE_FLAG + 43;
    public static final int PAY = HOTEL_HANDLE_FLAG + 44;

    public static final int ATTEND_USER = HOTEL_HANDLE_FLAG + 45;
    public static final int DELETE_ATTEND_USER = HOTEL_HANDLE_FLAG + 46;
    public static final int ATTEND_LIST = HOTEL_HANDLE_FLAG + 47;
    public static final int HOTEL_SPACE = HOTEL_HANDLE_FLAG + 48;
    public static final int HOTEL_TIE = HOTEL_HANDLE_FLAG + 49;
    public static final int HOTEL_TIE_COMMENT = HOTEL_HANDLE_FLAG + 50;
    public static final int HOTEL_TIE_DETAIL = HOTEL_HANDLE_FLAG + 51;
    public static final int HOTEL_TIE_SUB_COMMENT = HOTEL_HANDLE_FLAG + 52;
    public static final int PUBLIC_COMMENT = HOTEL_HANDLE_FLAG + 53;
    public static final int HOTEL_TIE_SUPPORT = HOTEL_HANDLE_FLAG + 54;
    public static final int HOTEL_TIE_SHARE = HOTEL_HANDLE_FLAG + 55;

    public static final int ASSESS_PUBLIC = HOTEL_HANDLE_FLAG + 56;
    public static final int CHECK_ROOM_EMPTY = HOTEL_HANDLE_FLAG + 57;
    public static final int HOTEL_COMMENT_DELETE = HOTEL_HANDLE_FLAG + 58;

    public static final int APP_INFO = HOTEL_HANDLE_FLAG + 59;
    public static final int ORDER_SPEND = HOTEL_HANDLE_FLAG + 60;

    public static final int HOTEL_BANNER = HOTEL_HANDLE_FLAG + 61;
    public static final int ACTIVE_ABOUT = HOTEL_HANDLE_FLAG + 62;
    public static final int ALL_SEARCH_HOTEL = HOTEL_HANDLE_FLAG + 63;

    public static final int AD_GDT_IF = HOTEL_HANDLE_FLAG + 64;
    public static final int WEATHER = HOTEL_HANDLE_FLAG + 65;

    public static final int COUPON_USEFUL_CHECK = HOTEL_HANDLE_FLAG + 66;
    public static final int COUPON_USEFUL_LIST = HOTEL_HANDLE_FLAG + 67;
    public static final int BOUNTY_USER_BASE = HOTEL_HANDLE_FLAG + 68;
    public static final int BOUNTY_USER_DETAIL = HOTEL_HANDLE_FLAG + 69;

    public static final int MENUS_STATUS = HOTEL_HANDLE_FLAG + 70;
    public static final int PAY_UNION = HOTEL_HANDLE_FLAG + 71;
    public static final int PAY_RESULT = HOTEL_HANDLE_FLAG + 72;
    public static final int BOUNTY_ACTIVES = HOTEL_HANDLE_FLAG + 73;
    public static final int COMMON_CITY_LIST = HOTEL_HANDLE_FLAG + 74;
    /**
     * 机票相关
     */
    private static final int PLANE_HANDLE_FLAG = 0x0100;
    public static final int PLANE_AIRPORT_LIST = PLANE_HANDLE_FLAG + 1;
    public static final int PLANE_FLIGHT_LIST = PLANE_HANDLE_FLAG + 2;
    public static final int PLANE_TICKET_LIST = PLANE_HANDLE_FLAG + 3;
    public static final int PLANE_COMPANY_LIST = PLANE_HANDLE_FLAG + 4;
    public static final int PLANE_BOOKING_INFO = PLANE_HANDLE_FLAG + 5;
    public static final int PLANE_NEW_ORDER = PLANE_HANDLE_FLAG + 6;

    public static final int ADDRESS_GET_DEFAULT = PLANE_HANDLE_FLAG + 7;
    public static final int ADDRESS_LIST = PLANE_HANDLE_FLAG + 8;
    public static final int ADDRESS_ADD = PLANE_HANDLE_FLAG + 9;
    public static final int ADDRESS_EDIT = PLANE_HANDLE_FLAG + 10;
    public static final int ADDRESS_DEFAULT = PLANE_HANDLE_FLAG + 11;
    public static final int ADDRESS_DELETE = PLANE_HANDLE_FLAG + 12;
}
