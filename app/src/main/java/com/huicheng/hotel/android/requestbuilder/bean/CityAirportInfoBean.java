package com.huicheng.hotel.android.requestbuilder.bean;

import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * @auth kborid
 * @date 2018/1/3 0003.
 */

public class CityAirportInfoBean implements Serializable, Comparable<CityAirportInfoBean> {
    public String airport3code;
    public String airport4code;
    public String airportname;
    public String cityname;
    public String firstchar;
    public String id;
    public String pinyin;
    public String status;
    public String type;

    @Override
    public int compareTo(@NonNull CityAirportInfoBean o) {
        return this.firstchar.compareTo(o.firstchar);
    }
}
