package com.huicheng.hotel.android.requestbuilder.bean;

import java.util.List;

/**
 * @auth kborid
 * @date 2017/12/22 0022.
 */

public class CityAreaInfoBean {
    public String id;
    public String parentId;
    public String level;
    public String province;
    public String areaName;
    public List<CityAreaInfoBean> list;
    public String shortName;

    @Override
    public String toString() {
        return "id:" + id + ", parentId:" + parentId + ", level:" + level + ", province:" + province + ", areaName:" + areaName + ", shortName:" + shortName;
    }
}
