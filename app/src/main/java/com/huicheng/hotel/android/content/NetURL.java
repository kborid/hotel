package com.huicheng.hotel.android.content;

import com.prj.sdk.util.SharedPreferenceUtil;

public final class NetURL {

    public static String getApi() {
        if (AppConst.ISDEVELOP) {
            int type = SharedPreferenceUtil.getInstance().getInt(AppConst.APPTYPE, 1);
            if (type == 4) {
                return SharedPreferenceUtil.getInstance().getString(AppConst.DEV_URL, "", false);
            } else {
                return ServerDomain.values()[type].getValue();
            }
        } else {
            return ServerDomain.DOMAIN_RELEASE.getValue();
        }
    }

    private static final String DOMAIN = getApi();
    private static final String PORTAL = (SharedPreferenceUtil.getInstance().getInt(AppConst.APPTYPE, 0) == 4) ? DOMAIN : DOMAIN + "hmp_website/"; // PORTAL地址  hmp_website/

    //---------------------------------common------------------------------------------------------
    public static final String UPLOAD = PORTAL + "upload/img.up"; // 上传图片
    public static final String WEATHER = PORTAL + "weather/getWeatherInfo.json";

    //---------------------------------用户信息、ticket相关------------------------------------------
    public static final String REGISTER = PORTAL + "user/regist.json"; // 注册
    public static final String LOGIN = PORTAL + "user/login.json"; // 登录
    public static final String CHECK_USER = PORTAL + "user/checkUserState.json"; //登录前检查用户状态
    public static final String CHECK_NAME = PORTAL + "user/checkusername.json"; // 检查用户名是否存在
    public static final String GET_USER_INFO = PORTAL + "user/getuserinfo.json"; //获取用户信息
    public static final String REMOVE_TICKET = PORTAL + "user/loginout.json";// 注销票据
    public static final String CHANGE_PHONENUMBER = PORTAL + "user/changeMobile.json"; //修改绑定手机号码
    public static final String CHECK_PHONE = PORTAL + "user/checkmobile.json"; // 检查手机号是否存在
    public static final String GET_YZM = PORTAL + "user/sendsms.json"; // 发送短信获取验证码
    public static final String CHECK_YZM = PORTAL + "user/checksafecodesn.json"; //校验验证码
    public static final String FIND_PASSWORD = PORTAL + "user/findPassword.json"; //找回密码
    public static final String CHANGE_PASSWORD = PORTAL + "user/changePassword.json"; //修改密码

    /*奖励金*/
    public static final String BOUNTY_USER_BASE = PORTAL + "user/bounty/meta"; //奖励金基本信息
    public static final String BOUNTY_USER_DETAIL = PORTAL + "user/bounty/page"; //奖励金明细
    public static final String BOUNTY_ACTIVES = PORTAL + "activity/activityVaultPics.json"; //奖励金活动列表

    //---------------------------------个人中心------------------------------------------------------
    public static final String CENTER_USERINFO = PORTAL + "user/getUserPreferSetting.json"; //个人中心详细信息
    public static final String SAVE_USERINFO = PORTAL + "user/saveOrUpdateUserPreferSetting.json";
    public static final String ASSESS_ORDER = PORTAL + "user_center/evaluates.json"; //评价列表
    public static final String ASSESS_DETAIL = PORTAL + "user_center/evaluate/detail.json"; //评价详情
    public static final String ASSESS_PUBLIC = PORTAL + "user_center/evaluate/add.json";
    public static final String FEEDBACK = PORTAL + "user_center/opinion/add.json"; //意見反饋
    public static final String VIP_HOTEL = PORTAL + "vip/queryMyHotels.json"; //会员酒店

    public static final String COUPON_ALL = PORTAL + "user/getUserCouponList.json"; //优惠券
    public static final String COUPON_USEFUL_CHECK = PORTAL + "order/checkCoupons.json";//检查是否有可用优惠券
    public static final String COUPON_USEFUL_LIST = PORTAL + "order/getCoupons.json"; //获取可用优惠券列表

    public static final String ATTEND_USER = PORTAL + "user_center/saveUserAttent.json"; //关注某用户
    public static final String DELETE_ATTEND_USER = PORTAL + "user_center/deleteUserAttent.json"; //取消关注某用户
    public static final String ATTEND_LIST = PORTAL + "user_center/selectAttentUserByPage.json"; //获取关注列表
    public static final String SPACE_VIDEO = PORTAL + "videoagain.html"; //空间视频播放url
    public static final String HOTEL_SPACE = PORTAL + "hotel/gethotelspace.json"; //酒店空间基本信息
    public static final String HOTEL_TIE = PORTAL + "hotel/gethotelarticlepage.json"; //酒店空间帖子列表
    public static final String HOTEL_TIE_DETAIL = PORTAL + "hotel/gethotelarticledetail.json"; //空间帖子详情
    public static final String HOTEL_TIE_COMMENT = PORTAL + "hotel/getreply4app.json"; //酒店空间帖子评论列表接口
    public static final String HOTEL_TIE_SUB_COMMENT = PORTAL + "hotel/getreply4reply.json"; //子回复列表接口
    public static final String PUBLIC_COMMENT = PORTAL + "hotel/addarticlereply.json"; //帖子回复接口
    public static final String HOTEL_TIE_SHARE = PORTAL + "hotel/addshare.json"; //帖子分享
    public static final String HOTEL_TIE_SUPPORT = PORTAL + "hotel/addpraise.json"; //点赞
    public static final String HOTEL_COMMENT_DELETE = PORTAL + "hotel/deleteReply.json"; //删除评论

    //---------------------------------消息---------------------------------------------------------
    public static final String MESSAGE_COUNT = PORTAL + "msg/cnt.json";
    public static final String ALL_MESSAGE = PORTAL + "msg/page.json";
    public static final String MESSAGE_UPDATE = PORTAL + "msg/update.json";

    public static final String MENUS_STATUS = PORTAL + "user/mainpageUsers.json";

    //---------------------------------酒店---------------------------------------------------------
    public static final String ALL_SEARCH_HOTEL = PORTAL + "hotel/search.json"; //全文检索
    public static final String HOTEL_LIST = PORTAL + "hotel/list.json";
    public static final String HOTEL_DETAIL = PORTAL + "hotel/detail.json";
    public static final String CHECK_ROOM_EMPTY = PORTAL + "hotel/before_room_detail.json";
    public static final String ROOM_DETAIL = PORTAL + "hotel/room_detail.json";
    public static final String HHY_LIST = PORTAL + "hotel/regretlist.json";
    public static final String HHY_DETAIL = PORTAL + "hotel/regretorderdetail.json";
    public static final String FREE_CURRENT_ACTIVE = PORTAL + "activity/getActivity.json"; //获取当前活动
    public static final String FREE_ACTIVE_DETAIL = PORTAL + "activity/list.json"; //获取当前活动优惠券列表
    public static final String FREE_GRAB_COUPON = PORTAL + "activity/grabCoupon.json"; //抢优惠券

    public static final String HOTEL_COMMENT = PORTAL + "hotel/hotel_evaluatelist.json";

    public static final String HOTEL_VIP = PORTAL + "hotel/addVip.json"; //会员
    public static final String ROOM_CONFIRM_ORDER = PORTAL + "order/add.json"; //下单，确认订单
    public static final String PAY_ORDER_DETAIL = PORTAL + "order/shortdetail.json"; //订单详情服务
    public static final String ORDER_LIST = PORTAL + "order/list.json"; //订单列表
    public static final String ORDER_SPEND = PORTAL + "order/spendyearly.json"; //消费年金
    public static final String ORDER_DETAIL = PORTAL + "order/detail.json"; //订单详情
    public static final String ORDER_CANCEL = PORTAL + "order/cancel.json"; //取消订单
    public static final String ORDER_MODIFY = PORTAL + "order/update.json"; //修改订单

    public static final String ORDER_PLANE = PORTAL + "pages/plain/pages/myplaneorder.html"; //机票订单详情
    public static final String PAY = PORTAL + "pay/to_payment.json"; //支付
    public static final String PAY_UNION = PORTAL + "pay/to_ums_payment.json"; //银联支付
    public static final String PAY_RESULT = PORTAL + "pay/search.json"; //支付结果查询

    // ----------------------------------------使用第三方登录绑定-------------------------------------
    public static final String BIND_CHECK = PORTAL + "user/getUserByToken.json"; //判断是否绑定
    public static final String BIND = PORTAL + "user/bindThirdPartUser.json"; //绑定
    public static final String VALIDATE_TICKET = PORTAL + "user/CW1014";// 判断票据是否过期
    public static final String AD_GDT_IF = PORTAL + "widePointAd/conversion.json"; //广点通统计接口

    // -------------------------------------------app信息（强制升级、邀请信息等）、广告-----------------
    public static final String APP_INFO = PORTAL + "system/getappversion.json"; //app版本信息
    public static final String HOTEL_BANNER = PORTAL + "activity/activityPics.json"; //首页banner
    public static final String ACTIVE_ABOUT = PORTAL + "system/gettipshowornot.json"; //活动相关
    public static final String COMMON_CITY_LIST = PORTAL + "system/getcityjson.json"; //城市列表

    // -----------------------------------Html 5----------------------------------------------------
    public static final String SHARE_BOUNTY = DOMAIN + "wechat/rule/inviteshare.html"; //旅行币分享链接
    public static final String SHARE = PORTAL + "pages/plain/pages/agreements/share.html"; //分享
    public static final String SAVE_RECOMMAND = PORTAL + "user/saveRecommand.json"; //推荐
    public static final String PLANE_HOME = PORTAL + "pages/plain/pages/air.html"; //机票页
    public static final String TRAIN_HOME = "https://m.ctrip.com/webapp/train/v2/index.html?allianceid=30613&sid=997104&hiderecommapp=1&popup=close&autoawaken=close&showhead=0"; //火车票首页
    public static final String SZ_TAXI_HOME_DEBUG = "https://commonwappre.10101111.com/join";
    public static final String SZ_TAXI_HOME_RELEASE = "https://commonwap.10101111.com/join";
    public static final String BOUNTY_LXB_RULE = "http://pro.abcbooking.cn/wechat/rule/index.html";
    public static final String PLANE_ACCIDENT_DETAIL = "http://pro.abcbooking.cn/hmp_website/pages/plain/pages/agreements/agreements-1.html";
    public static final String PLANE_DELAY_DETAIL = "http://pro.abcbooking.cn/hmp_website/pages/plain/pages/agreements/agreements-3.html";
    public static final String PLANE_LITHIUM_RULE = "http://pro.abcbooking.cn/hmp_website/pages/plain/pages/agreements/agreements-4.html";
    public static final String PLANE_DANGEROUS_RULE = "http://pro.abcbooking.cn/hmp_website/pages/plain/pages/agreements/agreements-5.html";
    public static final String PLANE_PASSENGER_RULE = "http://pro.abcbooking.cn/hmp_website/pages/plain/pages/agreements/agreements-2.html";

    //-------------------------------------机票-----------------------------------------------------
    public static final String PLANE_AIRPORT_LIST = PORTAL + "search/airports.json"; //查询机场列表
    public static final String PLANE_FLIGHT_LIST = PORTAL + "search/flight.json"; //搜索航班列表
    public static final String PLANE_TICKET_LIST = PORTAL + "search/price.json"; //查询机票列表
    public static final String PLANE_COMPANY_LIST = PORTAL + "search/getaircompanys.json"; //查询航司列表
    public static final String PLANE_BOOKING_INFO = PORTAL + "booking.json"; //机票booking预留信息
    public static final String PLANE_NEW_ORDER = PORTAL + "order.json"; //机票下单
    public static final String PLANE_ORDER_DETAIL = PORTAL + "search/order/detail.json"; //机票订单详情
    public static final String PLANE_BACK_QUERY = PORTAL + "search/cancel.json"; //机票退票查询
    public static final String PLANE_BACK = PORTAL + "refund.json"; //机票退票
    public static final String PLANE_CHANGE_QUERY = PORTAL + "search/change.json";//机票改签查询
    public static final String PLANE_CHANGE = PORTAL + "change.json";//机票改签
    public static final String PLANE_ORDER_CANCEL = PORTAL + "cancel.json";//取消机票订单
    public static final String PLANE_ORDER_INFO = PORTAL + "orderPriceInfo.json ";//机票支付订单支付详情

    //-------------------------------------地址管理--------------------------------------------------
    public static final String ADDRESS_GET_DEFAULT = PORTAL + "user/getdefaultaddress.json";
    public static final String ADDRESS_LIST = PORTAL + "user/addresslist.json";
    public static final String ADDRESS_ADD = PORTAL + "user/addaddress.json";
    public static final String ADDRESS_EDIT = PORTAL + "user/updateaddress.json";
    public static final String ADDRESS_DEFAULT = PORTAL + "user/setdefaultaddress.json";
    public static final String ADDRESS_DELETE = PORTAL + "user/deleteaddress.json";

    // --------------------------------------------设置缓存的URL-------------------------------------
    public static final String[] CACHE_URL = {};
}