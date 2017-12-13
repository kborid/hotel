package com.huicheng.hotel.android.common;

/**
 * @auth kborid
 * @date 2017/12/11 0011.
 */

public class ShareTypeDef {
    // OpenInstall分享类型
    public static final String SHARE_FREE           = "01"; //0元住
    public static final String SHARE_HOTEL          = "02"; //酒店
    public static final String SHARE_ROOM           = "03"; //房间
    public static final String SHARE_TIE            = "04"; //帖子
    public static final String SHARE_B2C            = "05"; //B2C推广
    public static final String SHARE_UNION          = "06"; //银联优惠券二维码推广
    public static final String SHARE_C2C            = "07"; //C2C推广

    public enum ShareContentEnum{
        GROUP_1("大家都改用这个APP订房了，价格真的比别家都低啊！", "注册时千万别忘了填我的手机号，拿完优惠券还能赚旅行币抵现！！"),
        GROUP_2("我转给你，就是为了让你便宜的！", "大家都改用这个APP订房了，价格都比别家都低，还能..."),
        GROUP_3("我跟你说，昨天我用这个APP订房巨便宜！", "注册时千万别忘了填我的手机号，还能拿额外优惠啊~"),
        ;

        private String title, description;

        ShareContentEnum(String title, String description) {
            this.title = title;
            this.description = description;
        }

        public String getShareTitle() {
            return title;
        }
        public String getShareDescription() {
            return description;
        }
    }
}
