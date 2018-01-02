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
        GROUP_1("昨天我用这个APP订房巨便宜！快去下载！", "别忘了参加小金库活动，拿完优惠券还能躺赚旅行币！"),
        GROUP_2("昨天我用这个APP订房巨便宜！快去下载！", "别忘了参加小金库活动，拿完优惠券还能躺赚旅行币！"),
        GROUP_3("昨天我用这个APP订房巨便宜！快去下载！", "别忘了参加小金库活动，拿完优惠券还能躺赚旅行币！"),;

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
