package com.huicheng.hotel.android.requestbuilder.bean;

/**
 * @auth kborid
 * @date 2018/1/15 0015.
 */

public class PlanePassengerInfoBean {
    //    public int ageType;           //年龄类型，0:成人；1:儿童
    public String birthday;             //生日
    public String cardNo;               //证件号码
    public String cardType;             //证件类型，NI:护照；PP:身份证；ID:其他
    //    public int coAmount;          //佣金
    //    public int discountAmount;    //优惠
//    public int insuranceType;           //保险类型，0:不买保险；1:只买意外险；2:只买延误险；3:都买
    public String name;                 //乘机人姓名
    public int sex;                     //性别，0:女，1:男

    public PlanePassengerInfoBean(String name, String cardType, String cardNo, String birthday, int sex) {
        this.name = name;
        this.cardType = cardType;
        this.cardNo = cardNo;
        this.birthday = birthday;
        this.sex = sex;
        //        this.coAmount = 0;          //默认0
        //        this.discountAmount = 0;    //默认0
//        this.insuranceType = insuranceType;
    }
}
