package com.huicheng.hotel.android.requestbuilder.bean;

import java.io.Serializable;
import java.util.List;

/**
 * @author kborid
 * @date 2017/2/20 0020
 */
public class HotelDetailInfoBean implements Serializable {
    public String address;
    public List<Attachments> attachments;
    public List<RoomListInfoBean> clockRoomList;
    public String evaluateCount;
    public String grade;
    public String guestQualification;
    public int id;
    public String lat;
    public String lon;
    public String name;
    public String phone;
    public List<String> picPath;
    public List<RoomListInfoBean> roomList;
    public int scala;
    public String servicePhone;
    public int star;
    public String typeByService;
    public boolean hasArticle;
    public boolean isPopup;
    public boolean isSupportVip;
    public boolean isVip;
    public String provinceName;
    public String cityName;


    public static class Attachments implements Serializable {
        public List<Contents> content;
        public String title;

        public static class Contents implements Serializable {
            public String content;
            public String index;
            public String title;
        }
    }
}

