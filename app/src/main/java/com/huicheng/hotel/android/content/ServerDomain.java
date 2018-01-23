package com.huicheng.hotel.android.content;

/**
 * @author kborid
 * @date 2017/9/19.
 */

enum ServerDomain {
    DOMAIN_UAT("http://uat.abcbooking.cn/"),
    DOMAIN_RELEASE("http://pro.abcbooking.cn/"),
    DOMAIN_DEVELOP("https://dev.abcbooking.cn:82/"),
    DOMAIN_DEMO("http://show.abcbooking.cn/"),;

    private String value = "";

    ServerDomain(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
