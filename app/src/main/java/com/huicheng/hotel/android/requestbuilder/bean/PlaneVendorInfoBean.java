package com.huicheng.hotel.android.requestbuilder.bean;

import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * @author kborid
 * @date 2018/4/12 0012.
 */

public class PlaneVendorInfoBean implements Comparable<PlaneVendorInfoBean>, Serializable {
    public String afee;
    public int barePrice;
    public int basePrice;
    public String bprtag;
    public String businessExt;
    public String cabin;
    public String cabinCount;
    public int cabinType;
    public String discount;
    public String domain;
    public String it;
    public String policyId;
    public String policyType;
    public int price;
    public String prtag;
    public int vppr;
    public String wrapperId;

    public String com;

    @Override
    public int compareTo(@NonNull PlaneVendorInfoBean o) {
        return Integer.valueOf(this.cabinType).compareTo(o.cabinType);
    }
}
