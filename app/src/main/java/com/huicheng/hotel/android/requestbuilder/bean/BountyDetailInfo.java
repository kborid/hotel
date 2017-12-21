package com.huicheng.hotel.android.requestbuilder.bean;

import java.util.List;

/**
 * @auth kborid
 * @date 2017/12/11 0011.
 */

public class BountyDetailInfo {

    public List<BountyItemInfo> balances;
    public boolean hasMore;

    public class BountyItemInfo {
        public float amount;
        public long cancelTime;
        public long createTime;
        public long expiryTime;
        public String fromOrderNo;
        public String id;
        public String remark;
        public float restAmount;
        public String status;
        public String type;
        public String useOrderNo;
        public long useTime;
        public String userId;
        public boolean valid;
    }
}
