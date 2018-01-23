package com.huicheng.hotel.android.requestbuilder.bean;

/**
 * 用户信息
 */
public class UserInfo {

    public User user;
    public Up up;

    public static class User {
        public String address;
        public String birthdate;
        public String channelid;
        public String headphotourl;
        public String ip;
        public long lastlogindate;
        public String level;
        public String mobile;
        public String nickname;
        public String password;
        public long registdate;
        public String sex;
        public String siteid;
        public String status;
        public long updatetime;
        public String userid;
        public String username;
    }

    public static class Up {
        public String isstartup; // 0-关闭 1-开启
        public String evalutegrade; // 评价等级
        public int maxprice;
        public int minprice;
        public String preferfacilities;
        public String preferfacilitieschoosen;
        public String stargrade; // 星级等级
        public String userid;
    }
}
