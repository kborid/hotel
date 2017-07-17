package com.prj.sdk.widget.wheel.adapters;

import java.util.List;

/**
 * @author kborid
 * @date 2017/2/24 0024
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
        return "id = " + id + ", parentId = " + parentId + ", level = " + level + ", province = " + province + ", areaName = " + areaName + ", list size = " + list.size() + ", shortName = " + shortName;
    }
}
