package com.huicheng.hotel.android.common;

import com.huicheng.hotel.android.requestbuilder.bean.CouponInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.FansHotelInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.HotelDetailInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.OrderPayDetailInfoBean;

import java.util.Calendar;
import java.util.Date;

/**
 * @author kborid
 * @date 2017/2/9 0009
 */
public class HotelOrderManager {

    private Date beginDate, endDate;
    private long beginTime, endTime;
    private Date ygrBeginDate, ygrEndDate;
    private long ygrBeginTime, ygrEndTime;
    private int hotelType = HotelCommDef.TYPE_ALL;
    private String payType;
    private String cityStr = null;
    private String dateStr = null;
    private HotelDetailInfoBean hotelDetailInfoBean = null;
    private FansHotelInfoBean fansHotelInfoBean = null;
    private CouponInfoBean.CouponInfo couponInfoBean = null;
    private OrderPayDetailInfoBean orderPayDetailInfoBean = null;

    private static HotelOrderManager instance = null;
    private HotelOrderManager() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(System.currentTimeMillis()));
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        ygrBeginDate = calendar.getTime();
        ygrBeginTime = ygrBeginDate.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        ygrEndDate = calendar.getTime();
        ygrEndTime = ygrEndDate.getTime();
    }

    public static HotelOrderManager getInstance() {
        if (null == instance) {
            synchronized (HotelOrderManager.class) {
                if (null == instance) {
                    instance = new HotelOrderManager();
                }
            }
        }
        return instance;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public int getHotelType() {
        return hotelType;
    }

    public void setHotelType(int hotelType) {
        this.hotelType = hotelType;
    }

    public Date getBeginDate() {
        return getBeginDate(false);
    }

    public Date getBeginDate(boolean isYgr) {
        if (isYgr) {
            return ygrBeginDate;
        } else {
            return beginDate;
        }
    }

    public Date getEndDate() {
        return getEndDate(false);
    }

    public Date getEndDate(boolean isYgr) {
        if (isYgr) {
            return ygrEndDate;
        } else {
            return endDate;
        }
    }

    public void setCityStr(String cityStr) {
        this.cityStr = cityStr;
    }

    public String getCityStr() {
        return cityStr;
    }

    public void setDateStr(String dateStr) {
        this.dateStr = dateStr;
    }

    public String getDateStr() {
        return dateStr;
    }

    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
        this.beginDate = new Date(beginTime);
    }

    public long getBeginTime() {
        return getBeginTime(false);
    }

    public long getBeginTime(boolean isYgr) {
        if (isYgr) {
            return ygrBeginTime;
        } else {
            return beginTime;
        }
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
        this.endDate = new Date(endTime);
    }

    public long getEndTime() {
        return getEndTime(false);
    }

    public long getEndTime(boolean isYgr) {
        if (isYgr) {
            return ygrEndTime;
        } else {
            return endTime;
        }
    }

    public void setHotelDetailInfo(HotelDetailInfoBean hotelDetailInfo) {
        this.hotelDetailInfoBean = hotelDetailInfo;
    }

    public HotelDetailInfoBean getHotelDetailInfo() {
        return hotelDetailInfoBean;
    }

    public void setFansHotelInfoBean(FansHotelInfoBean fansHotelInfo) {
        this.fansHotelInfoBean = fansHotelInfo;
    }

    public FansHotelInfoBean getFansHotelInfoBean() {
        return fansHotelInfoBean;
    }

    public void setCouponInfoBean(CouponInfoBean.CouponInfo couponInfoBean) {
        this.couponInfoBean = couponInfoBean;
    }

    public CouponInfoBean.CouponInfo getCouponInfoBean() {
        return couponInfoBean;
    }

    public void setOrderPayDetailInfoBean(OrderPayDetailInfoBean orderPayDetailInfoBean) {
        this.orderPayDetailInfoBean = orderPayDetailInfoBean;
    }

    public OrderPayDetailInfoBean getOrderPayDetailInfoBean() {
        return orderPayDetailInfoBean;
    }

    public void reset() {
        hotelType = HotelCommDef.TYPE_ALL;
        payType = "";
        hotelDetailInfoBean = null;
        fansHotelInfoBean = null;
        couponInfoBean = null;
        orderPayDetailInfoBean = null;
    }
}
