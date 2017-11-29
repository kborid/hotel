package com.huicheng.hotel.android.common;

import com.prj.sdk.BuildConfig;

/**
 * 项目常量
 *
 * @author LiaoBo
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

    public static final String IN_PERSON_INFO = "in_person_info";

    public static final String ACCESS_TICKET = "accessTicket"; // 记录用户登录ticket
    public static final String LAST_LOGIN_DATE = "last_login_time"; // 上次登录时间
    public static final String USERNAME = "username"; // 用户名
    public static final String USER_PHOTO_URL = "user_photo_url"; // 用户头像地址
    public static final String USER_INFO = "user_info"; // 用户信息
    public static final String LOCATION_LON = "location_lon";
    public static final String LOCATION_LAT = "location_lat";

    public static final String HISTORY = "history";
    public static final String PROVINCE = "province";
    public static final String CITY = "city";
    public static final String SITEID = "siteid"; // site id

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
    private static final int CONST_HANDLE_FLAG = 0x00;
    // 上传图片
    public static final int UPLOAD = CONST_HANDLE_FLAG + 1;
    // 用户模块
    public static final int REGISTER = CONST_HANDLE_FLAG + 2;
    public static final int LOGIN = CONST_HANDLE_FLAG + 3;
    public static final int CHECK_USER = CONST_HANDLE_FLAG + 4;
    public static final int CHECK_NAME = CONST_HANDLE_FLAG + 5;
    public static final int GET_USER_INFO = CONST_HANDLE_FLAG + 6;
    public static final int REMOVE_TICKET = CONST_HANDLE_FLAG + 7;
    public static final int CHANGE_PHONENUMBER = CONST_HANDLE_FLAG + 8;
    public static final int CHECK_PHONE = CONST_HANDLE_FLAG + 9;
    public static final int GET_YZM = CONST_HANDLE_FLAG + 10;
    public static final int CHECK_YZM = CONST_HANDLE_FLAG + 11;
    public static final int FIND_PASSWORD = CONST_HANDLE_FLAG + 12;
    public static final int CHANGE_PASSWORD = CONST_HANDLE_FLAG + 13;
    // 个人中心
    public static final int CENTER_USERINFO = CONST_HANDLE_FLAG + 14;
    public static final int SAVE_USERINFO = CONST_HANDLE_FLAG + 15;
    public static final int UPDATE_USERINFO = CONST_HANDLE_FLAG + 16;
    // 消息
    public static final int ALL_MESSAGE = CONST_HANDLE_FLAG + 17;
    public static final int MESSAGE_COUNT = CONST_HANDLE_FLAG + 18;
    public static final int MESSAGE_UPDATE = CONST_HANDLE_FLAG + 19;
    // 酒店详情、列表
    public static final int HOTEL_DETAIL = CONST_HANDLE_FLAG + 20;

    public static final int FEEDBACK = CONST_HANDLE_FLAG + 21;
    public static final int ASSESS_ORDER = CONST_HANDLE_FLAG + 22;
    public static final int ASSESS_DETAIL = CONST_HANDLE_FLAG + 23;
    public static final int VIP_HOTEL = CONST_HANDLE_FLAG + 24;
    public static final int COUPON_ALL = CONST_HANDLE_FLAG + 25;
    public static final int ROOM_DETAIL = CONST_HANDLE_FLAG + 26;

    public static final int HOTEL_LIST = CONST_HANDLE_FLAG + 27;
    public static final int HHY_LIST = CONST_HANDLE_FLAG + 28;
    public static final int HHY_DETAIL = CONST_HANDLE_FLAG + 29;

    public static final int FREE_CURRENT_ACTIVE = CONST_HANDLE_FLAG + 30;
    public static final int FREE_ACTIVE_DETAIL = CONST_HANDLE_FLAG + 31;
    public static final int FREE_GRAB_COUPON = CONST_HANDLE_FLAG + 32;

    public static final int HOTEL_COMMENT = CONST_HANDLE_FLAG + 33;

    public static final int BIND_CHECK = CONST_HANDLE_FLAG + 34;
    public static final int BIND = CONST_HANDLE_FLAG + 35;

    public static final int SAVE_RECOMMAND = CONST_HANDLE_FLAG + 36;

    public static final int ROOM_CONFIRM_ORDER = CONST_HANDLE_FLAG + 37;
    public static final int HOTEL_VIP = CONST_HANDLE_FLAG + 38;

    public static final int PAY_ORDER_DETAIL = CONST_HANDLE_FLAG + 39;
    public static final int ORDER_LIST = CONST_HANDLE_FLAG + 40;
    public static final int ORDER_DETAIL = CONST_HANDLE_FLAG + 41;
    public static final int ORDER_CANCEL = CONST_HANDLE_FLAG + 42;
    public static final int ORDER_MODIFY = CONST_HANDLE_FLAG + 43;

    public static final int PAY = CONST_HANDLE_FLAG + 44;

    public static final int ATTEND_USER = CONST_HANDLE_FLAG + 45;
    public static final int DELETE_ATTEND_USER = CONST_HANDLE_FLAG + 46;
    public static final int ATTEND_LIST = CONST_HANDLE_FLAG + 47;

    public static final int HOTEL_SPACE = CONST_HANDLE_FLAG + 48;
    public static final int HOTEL_TIE = CONST_HANDLE_FLAG + 49;
    public static final int HOTEL_TIE_COMMENT = CONST_HANDLE_FLAG + 50;
    public static final int HOTEL_TIE_DETAIL = CONST_HANDLE_FLAG + 51;
    public static final int HOTEL_TIE_SUB_COMMENT = CONST_HANDLE_FLAG + 52;
    public static final int PUBLIC_COMMENT = CONST_HANDLE_FLAG + 53;

    public static final int HOTEL_TIE_SUPPORT = CONST_HANDLE_FLAG + 54;
    public static final int HOTEL_TIE_SHARE = CONST_HANDLE_FLAG + 55;

    public static final int ASSESS_PUBLIC = CONST_HANDLE_FLAG + 56;

    public static final int CHECK_ROOM_EMPTY = CONST_HANDLE_FLAG + 57;
    public static final int HOTEL_COMMENT_DELETE = CONST_HANDLE_FLAG + 58;

    public static final int APP_INFO = CONST_HANDLE_FLAG + 59;
    public static final int ORDER_SPEND = CONST_HANDLE_FLAG + 60;

    public static final int HOTEL_BANNER = CONST_HANDLE_FLAG + 61;
    public static final int ACTIVE_ABOUT = CONST_HANDLE_FLAG + 62;
    public static final int ALL_SEARCH_HOTEL = CONST_HANDLE_FLAG + 63;

    public static final int AD_GDT_IF = CONST_HANDLE_FLAG + 64;

    public static final int WEATHER = CONST_HANDLE_FLAG + 65;

    public static final int COUPON_USEFUL_CHECK = CONST_HANDLE_FLAG + 66;
    public static final int COUPON_USEFUL_LIST = CONST_HANDLE_FLAG + 67;
}
