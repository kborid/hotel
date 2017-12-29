package com.huicheng.hotel.android.common;

/**
 * @auth kborid
 * @date 2017/12/18 0018.
 */

public class PlaneCommDef {
    //航班类型，单程或往返
    public static final int FLIGHT_SINGLE = 0;
    public static final int FLIGHT_GO_BACK = 1;

    //往返航班的去、往状态
    public enum GoBackStatus {
        STATUS_GO,
        STATUS_BACK;
    }
}
