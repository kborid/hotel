package com.huicheng.hotel.android.requestbuilder.bean;

import java.util.List;

/**
 * @author kborid
 * @date 2017/2/27 0027
 */
public class HouHuiYaoInfoBean {

    public List<HouHuiYaoBean> otherlist;
    public List<HouHuiYaoBean> targetlist;

    public static class HouHuiYaoBean {
        public String address;
        public String bankaccountname;
        public String bankno;
        public String buyerid;
        public int buyprice;
        public String coordinate;
        public long createtime;
        public long endtime;
        public String grade;
        public int houtelid;
        public String hotelname;
        public String id;
        public String lat;
        public String lon;
        public String orderNO;
        public int orderid;
        public String picpath;
        public int roomid;
        public long sellouttime;
        public int sellprice;
        public long starttime;
        public String status;
    }
}
