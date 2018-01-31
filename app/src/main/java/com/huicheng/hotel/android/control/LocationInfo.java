package com.huicheng.hotel.android.control;

/**
 * @author kborid
 * @date 2018/1/31 0031.
 */

public enum LocationInfo {
    instance;
    private boolean isLocated = false;
    private boolean isMyLoc = false;
    private String lon;
    private String lat;
    private String province;
    private String city;
    private String cityCode;

    public void init(String lon, String lat, String province, String city, String cityCode) {
        this.isLocated = true;
        this.lon = lon;
        this.lat = lat;
        this.province = province;
        this.city = city;
        this.cityCode = cityCode;
    }

    public void resetCity(String province, String city, String cityCode) {
        this.province = province;
        this.city = city;
        this.cityCode = cityCode;
    }

    public boolean isLocated() {
        return isLocated;
    }

    public void setIsMyLoc(boolean flag){
        this.isMyLoc = flag;
    }

    public boolean isMyLoc(){
        return isMyLoc;
    }

    public String getLon() {
        return lon;
    }

    public String getLat() {
        return lat;
    }

    public String getProvince() {
        return province;
    }

    public String getCity() {
        return city;
    }

    public String getCityCode() {
        return cityCode;
    }
}
