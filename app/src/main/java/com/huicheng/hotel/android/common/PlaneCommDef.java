package com.huicheng.hotel.android.common;

import com.huicheng.hotel.android.R;

import java.util.HashMap;

/**
 * @auth kborid
 * @date 2017/12/18 0018.
 */

public class PlaneCommDef {
    //航班类型，单程或往返
    public static final int FLIGHT_SINGLE = 0;
    public static final int FLIGHT_GOBACK = 1;

    //航班预订状态，单程、往返去程和往返回程
    public static final int STATUS_GO = 0;
    public static final int STATUS_BACK = 1;

    //航班筛选：航班类型
    public static final int FLIGHT_TYPE_ALL = 0; //不限
    public static final int FLIGHT_TYPE_BIG = 1; //大型机
    public static final int FLIGHT_TYPE_MID = 2; //中型机
    public static final int FLIGHT_TYPE_SML = 3; //小型机

    //航班筛选：航班仓位
    public static final int FLIGHT_CANG_ALL = 0; //不限
    public static final int FLIGHT_CANG_JINGJI = 1; //经济舱
    public static final int FLIGHT_CANG_TOUDENG = 2; //头等舱
    public static final int FLIGHT_CANG_SHANGWU = 3; //商务舱

    //仓位等级
    public static final int CABIN_JINGJI = 0; //经济舱，0或其他
    public static final int CABIN_SHANGWU = 1; //商务舱
    public static final int CABIN_TOUDENG = 2; //头等舱

    public static String getCabinString(int level) {
        String cabin = "经济舱";
        switch (level) {
            case CABIN_SHANGWU:
                cabin = "商务舱";
                break;
            case CABIN_TOUDENG:
                cabin = "头等舱";
                break;
            default:
                cabin = "经济舱";
                break;
        }
        return cabin;
    }

    //自定保险购买类型
    public static final int SAFE_BUY_NON = 0; //一个都不买
    public static final int SAFE_BUY_YII = 1; //只买意外险
    public static final int SAFE_BUY_DEL = 2; //只买延误险
    public static final int SAFE_BUY_ALL = 3; //全买

    public static HashMap<String, String> AIR_COMPANY_CODE = new HashMap<String, String>();
    public static HashMap<String, Integer> AIR_ICON_CODE = new HashMap<String, Integer>() {
        {
            put("CA", R.drawable.air_ca_gjhk);//国航
            put("CZ", R.drawable.air_cz_nfhk);//南航
            put("MU", R.drawable.air_mu_dfhk);//东航
            put("HU", R.drawable.air_hu_hnhk);//海航
            put("3U", R.drawable.air_3u_schk);//川航
            put("MF", R.drawable.air_mf_xmhk);//厦航
            put("FM", R.drawable.air_fm_shhk);//上航
            put("ZH", R.drawable.air_zh_szhk);//深航
            put("JD", R.drawable.air_jd_sdhk);//首都航空
            put("CN", 0);//大新华
            put("GS", R.drawable.air_gs_tjhk);//天津航空
            put("PN", R.drawable.air_pn_xbhk);//西部航空
            put("8L", R.drawable.air_8l_xphk);//祥鹏航空
            put("9C", R.drawable.air_9c_cqhk);//春秋航空
            put("SC", R.drawable.air_sc_sdhk);//山航
            put("OQ", 0);//重庆航空
            put("TV", R.drawable.air_tv_xzhk);//西藏航空
            put("JR", R.drawable.air_jr_xfhk);//幸福航空
            put("KY", R.drawable.air_ky_kmhk);//昆明航空
            put("G5", R.drawable.air_g5_hxhk);//华夏航空
            put("HO", R.drawable.air_ho_jxhk);//吉祥航空
            put("EU", R.drawable.air_eu_cdhk);//成都航空
            put("OK", R.drawable.air_ok_akhk);//奥凯
            put("KN", R.drawable.air_kn_lhhk);//联航
            put("VD", 0);//河南航空
            put("CX", R.drawable.air_cx_gthk);//国泰
            put("KA", R.drawable.air_ka_glhk);//港龙
            put("LD", 0);//LD
            put("HX", R.drawable.air_hx_xghk);//香港航空
            put("UO", 0);//香港快运
            put("NX", R.drawable.air_nx_amhk);//澳门航空
            put("CI", 0);//中华航空
            put("BR", 0);//长荣
            put("AE", R.drawable.air_ae_hxhk);//华信
            put("GE", 0);//复兴
            put("B7", 0);//立荣
            put("EF", 0);//远东
            put("GJ", R.drawable.air_gj_clhk);//长龙航空
            put("X2", R.drawable.air_x2_xhhk);//新华航空
            put("WH", 0);//西北航空
            put("SZ", 0);//西南航空
            put("CJ", 0);//北方航空
            put("F6", 0);//浙江航空
            put("Z2", 0);//中原航空
            put("2Z", R.drawable.air_2z_cahk);//长安航空
            put("3W", 0);//南京航空
            put("XO", 0);//新疆航空
            put("3Q", 0);//云南航空
            put("G8", 0);//长城航空
            put("WU", 0);//武汉航空
            put("G4", R.drawable.air_g4_gzhk);//贵州航空
            put("IV", 0);//福建航空
        }
    };
}
