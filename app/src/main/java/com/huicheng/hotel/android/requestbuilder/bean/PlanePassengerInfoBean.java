package com.huicheng.hotel.android.requestbuilder.bean;

/**
 * @auth kborid
 * @date 2018/1/15 0015.
 */

public class PlanePassengerInfoBean {
    public int accidentAmount;
    public int accidentCount;
    public int ageType;
    public String birthday;
    public String cardNo;
    public String cardType;
    public int coAmount;
    public int delayAmount;
    public int discountAmount;
    public int insuranceType;
    public String name;
    public int sex;

    public PlanePassengerInfoBean(String name, String cardType, String cardNo, String birthday) {
        this.name = name;
        this.cardType = cardType;
        this.cardNo = cardNo;
        this.birthday = birthday;
    }
}
