package com.huicheng.hotel.android.requestbuilder.bean;

/**
 * @author kborid
 * @date 2018/2/1 0001.
 */

public class PlaneInvoiceTaxInfoBean {
    //    private int coAmount;
    //    private int discountAmount;
    //    private int expressAmount;
    private String address;             //发票邮寄地址
    private String contact;             //订单联系人
    private String contactMob;          //订单联系电话
    private String contactPreNum;       //订单联系电话前缀
    private int invoiceType;            //发票类型
    private String receiverTitle;       //发票抬头
    private int receiverType;           //发票抬头类型
    private String sjr;                 //发票邮寄收件人
    private String sjrPhone;            //发票邮寄收件人联系电话
    private String userId;              //当前登陆用户ID

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getContactMob() {
        return contactMob;
    }

    public void setContactMob(String contactMob) {
        this.contactMob = contactMob;
    }

    public String getContactPreNum() {
        return contactPreNum;
    }

    public void setContactPreNum(String contactPreNum) {
        this.contactPreNum = contactPreNum;
    }

    public int getInvoiceType() {
        return invoiceType;
    }

    public void setInvoiceType(int invoiceType) {
        this.invoiceType = invoiceType;
    }

    public String getReceiverTitle() {
        return receiverTitle;
    }

    public void setReceiverTitle(String receiverTitle) {
        this.receiverTitle = receiverTitle;
    }

    public int getReceiverType() {
        return receiverType;
    }

    public void setReceiverType(int receiverType) {
        this.receiverType = receiverType;
    }

    public String getSjr() {
        return sjr;
    }

    public void setSjr(String sjr) {
        this.sjr = sjr;
    }

    public String getSjrPhone() {
        return sjrPhone;
    }

    public void setSjrPhone(String sjrPhone) {
        this.sjrPhone = sjrPhone;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
