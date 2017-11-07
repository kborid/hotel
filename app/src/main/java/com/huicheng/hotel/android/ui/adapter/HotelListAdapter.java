package com.huicheng.hotel.android.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.bumptech.glide.Glide;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.net.bean.HotelInfoBean;
import com.huicheng.hotel.android.ui.activity.HtmlActivity;
import com.huicheng.hotel.android.ui.glide.CustomReqURLFormatModelImpl;
import com.huicheng.hotel.android.ui.mapoverlay.AMapUtil;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @auth kborid
 * @date 2017/7/17.
 */

public class HotelListAdapter extends RecyclerView.Adapter<HotelListAdapter.HotelViewHolder> {
    private Context context;
    private List<HotelInfoBean> list;
    private int type = 0;
    private int ygrRoomItemBackgroundId;

    private Map<Integer, String> trackMap = new HashMap<>();

    public HotelListAdapter(Context context, List<HotelInfoBean> list, int type) {
        this.context = context;
        this.list = list;
        this.type = type;
        TypedArray ta = context.obtainStyledAttributes(R.styleable.MyTheme);
        ygrRoomItemBackgroundId = ta.getResourceId(R.styleable.MyTheme_roomItemGradient, R.drawable.roomitem_ygr_gradient);
        ta.recycle();
    }

    @Override
    public HotelListAdapter.HotelViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        HotelListAdapter.HotelViewHolder hotelViewHolder;
        switch (type) {
            case HotelCommDef.TYPE_YEGUIREN:
                hotelViewHolder = new HotelListAdapter.HotelViewHolder(LayoutInflater.from(context).inflate(R.layout.lv_hotelitem_ygr, parent, false));
                break;
            case HotelCommDef.TYPE_ALL:
            case HotelCommDef.TYPE_CLOCK:
            default:
                hotelViewHolder = new HotelListAdapter.HotelViewHolder(LayoutInflater.from(context).inflate(R.layout.lv_hotelitem_allday, parent, false));
                break;
        }
        hotelViewHolder.setIsRecyclable(true);
        return hotelViewHolder;
    }

    @Override
    public void onBindViewHolder(final HotelListAdapter.HotelViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        final HotelInfoBean bean = list.get(position);

        //是否显示广告
        if (bean.ad == 1) {
            holder.hotel_item_lay.setVisibility(View.GONE);
            holder.iv_ad.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(new CustomReqURLFormatModelImpl(bean.picPath))
//                    .placeholder(R.drawable.def_hotel_list)
                    .crossFade()
//                    .fitCenter()
                    .into(holder.iv_ad);
        } else {
            holder.iv_ad.setVisibility(View.GONE);
            holder.hotel_item_lay.setVisibility(View.VISIBLE);

            holder.tv_hotel_name.setText(bean.hotelName);
            Glide.with(context)
                    .load(new CustomReqURLFormatModelImpl(bean.hotelFeaturePic))
                    .placeholder(R.drawable.def_hotel_list)
                    .crossFade()
                    .centerCrop()
                    .into(holder.iv_hotel_icon);

            //平台价
            int mPrice = bean.price;
            switch (type) {
                case HotelCommDef.TYPE_ALL:
                    mPrice = bean.price;
                    break;
                case HotelCommDef.TYPE_CLOCK:
                    mPrice = bean.clockPrice;
                    break;
                case HotelCommDef.TYPE_YEGUIREN:
                    mPrice = bean.yeguirenPrice;
                    break;
            }

            // 是否显示vip价格
            holder.tv_vip.setVisibility(View.GONE);
            boolean isShowVip = false;
            if (HotelCommDef.VIP_SUPPORT.equals(bean.vipEnable) && bean.vipPrice > 0) {
                int minPrice;
                //获取特价，平台价不为0且最小的价格
                if (mPrice != 0 && bean.speciallyPrice != 0) {
                    minPrice = Math.min(mPrice, bean.speciallyPrice);
                } else {
                    minPrice = 0 != mPrice ? mPrice : bean.speciallyPrice;
                }
                //会员价与最小价格比较
                if (bean.vipPrice < minPrice && mPrice > 0) {
                    holder.tv_vip.setVisibility(View.VISIBLE);
                    isShowVip = true;
//                    DecimalFormat decimalFormat = new DecimalFormat("0.0");
//                    float vipPercent = (float) bean.vipPrice / mPrice * 10;
//                    holder.tv_vip.setText(String.format(context.getString(R.string.vip_price_tips), decimalFormat.format(vipPercent)));
                    holder.tv_vip.setText(context.getString(R.string.vip_price_tips2));
                }
            }

            // 下方显示价格逻辑
            String price = "暂无";
            switch (type) {
                case HotelCommDef.TYPE_ALL:
                    if (mPrice > 0) {
                        if (bean.speciallyPrice > mPrice || bean.speciallyPrice <= 0) {
                            holder.tv_platform_price.setVisibility(View.GONE);
                            price = String.valueOf(mPrice);
                            holder.tv_price_unit.setVisibility(View.VISIBLE);

                            holder.off_lay.setVisibility(View.GONE);
                        } else {
                            // 判断是否显示带删除线的平台价
                            if (bean.speciallyPrice == mPrice) {
                                holder.tv_platform_price.setVisibility(View.GONE);
                            } else {
                                holder.tv_platform_price.setVisibility(View.VISIBLE);
                                holder.tv_platform_price.setText(String.format(context.getString(R.string.rmbStr), String.valueOf(mPrice)));
                            }
                            price = String.valueOf(bean.speciallyPrice);
                            holder.tv_price_unit.setVisibility(View.VISIBLE);

                            //显示折扣信息
                            holder.off_lay.setVisibility(View.VISIBLE);
                            DecimalFormat df = (DecimalFormat) NumberFormat.getPercentInstance();
                            float specialPercent = (float) (bean.speciallyPrice - mPrice) / mPrice;
                            holder.tv_off_percent.setText(df.format(specialPercent));
                            holder.tv_off_info.setText(bean.comment);
                            if (isShowVip) {
                                holder.off_lay.setBackgroundColor(context.getResources().getColor(R.color.offInfoColor2));
                            } else {
                                holder.off_lay.setBackgroundColor(context.getResources().getColor(R.color.offInfoColor1));
                            }
                        }
                    } else {
                        holder.tv_platform_price.setVisibility(View.GONE);
                        holder.off_lay.setVisibility(View.GONE);
                    }
                    holder.tv_real_price.setText(price);
                    break;
                case HotelCommDef.TYPE_CLOCK:
                    holder.tv_platform_price.setVisibility(View.GONE);
                    if (bean.clockPrice != 0) { //价格为0时判断处理
                        price = String.valueOf(bean.clockPrice);
                        holder.tv_price_unit.setVisibility(View.VISIBLE);
                    }
                    holder.tv_real_price.setText(price);
                    holder.off_lay.setVisibility(View.GONE);
                    break;
                case HotelCommDef.TYPE_YEGUIREN:
                    if (mPrice > 0) {
                        if (bean.speciallyPrice > mPrice || bean.speciallyPrice <= 0) {
                            holder.tv_platform_price.setVisibility(View.GONE);
                            price = String.valueOf(mPrice);
                            holder.tv_price_unit.setVisibility(View.VISIBLE);
                        } else {
                            // 判断是否显示带删除线的平台价
                            if (bean.speciallyPrice == mPrice) {
                                holder.tv_platform_price.setVisibility(View.GONE);
                            } else {
                                holder.tv_platform_price.setVisibility(View.VISIBLE);
                                holder.tv_platform_price.setText(String.format(context.getString(R.string.rmbStr), String.valueOf(mPrice)));
                            }
                            price = String.valueOf(bean.speciallyPrice);
                            holder.tv_price_unit.setVisibility(View.VISIBLE);
                        }
                    } else {
                        holder.tv_platform_price.setVisibility(View.GONE);
                    }
                    holder.tv_real_price.setText(price);
                    holder.off_lay.setVisibility(View.GONE);
                    break;
            }

            // 距离信息
            LatLng start = null, des = null;
            float lon = Float.parseFloat(SharedPreferenceUtil.getInstance().getString(AppConst.LOCATION_LON, "0", false));
            float lat = Float.parseFloat(SharedPreferenceUtil.getInstance().getString(AppConst.LOCATION_LAT, "0", false));
            if (lon != 0 && lat != 0 && StringUtil.notEmpty(bean.hotelCoordinate)) {
                start = new LatLng(lat, lon);
                String[] pos = bean.hotelCoordinate.split("\\|");
                des = new LatLng(Float.valueOf(pos[0]), Float.valueOf(pos[1]));
                float dis = AMapUtils.calculateLineDistance(start, des);
                holder.tv_hotel_dis.setText(AMapUtil.getFriendlyLength((int) dis));
            } else {
                holder.tv_hotel_dis.setText("暂无距离信息");
            }

            //浏览痕迹：随机1分钟~12小时
            if (!trackMap.containsKey(position)) {
                int min = ((int) (Math.random() * (60 * 12)) + 1);
                String trackStr;
                if (min >= 60) {
                    trackStr = String.valueOf(min / 60 + "小时");
                } else {
                    trackStr = String.valueOf(min + "分钟");
                }
                trackMap.put(position, trackStr);
            }
            holder.tv_track.setText(String.format(context.getString(R.string.track_str), trackMap.get(position)));

            // 评分信息
            float point;
            if (StringUtil.isEmpty(bean.hotelGrade)) {
                point = 0;
            } else {
                point = Float.parseFloat(bean.hotelGrade);
            }
            String tmp = String.format(context.getString(R.string.pointStr), String.valueOf(point));
            SpannableString ss = new SpannableString(tmp);
            ss.setSpan(/*new AbsoluteSizeSpan(11, true)*/new RelativeSizeSpan(0.78f), tmp.length() - 1, tmp.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            holder.tv_point.setText(ss);

            if (point >= 4.8) {
                holder.tv_point_tips.setText(context.getString(R.string.point_level_1));
            } else if (point >= 4.5) {
                holder.tv_point_tips.setText(context.getString(R.string.point_level_2));
            } else if (point >= 4.0) {
                holder.tv_point_tips.setText(context.getString(R.string.point_level_3));
            } else {
                holder.tv_point_tips.setText("");
            }

            //诚信盾牌认证
            if (StringUtil.isEmpty(bean.level) || HotelCommDef.CERT_NULL.equals(bean.level)) {
                holder.iv_cert_icon.setVisibility(View.GONE);
            } else {
                if (HotelCommDef.CERT_GOLD.equals(bean.level)) {
                    holder.iv_cert_icon.setImageResource(R.drawable.iv_cert_gold);
                } else {
                    holder.iv_cert_icon.setImageResource(R.drawable.iv_cert_silver);
                }
                holder.iv_cert_icon.setVisibility(View.VISIBLE);
            }
        }

        // 点击事件
        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (list.get(position).ad == 1) {
                    Intent intent = new Intent(context, HtmlActivity.class);
                    intent.putExtra("path", bean.link);
                    context.startActivity(intent);
                    return;
                }
                if (!SessionContext.isLogin()) {
                    context.sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION));
                    return;
                }
                if (null != listener) {
                    listener.OnItemClick(v, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class HotelViewHolder extends RecyclerView.ViewHolder {

        FrameLayout root;
        LinearLayout hotel_item_lay;
        ImageView iv_ad;
        RelativeLayout hotel_bg_lay;
        ImageView iv_hotel_icon;
        LinearLayout off_lay;
        TextView tv_off_percent;
        TextView tv_off_info;
        TextView tv_vip;
        TextView tv_hotel_name;
        ImageView iv_cert_icon;
        TextView tv_point;
        TextView tv_point_tips;
        TextView tv_hotel_dis;
        TextView tv_track;
        TextView tv_platform_price;
        TextView tv_real_price;
        TextView tv_price_unit;

        HotelViewHolder(View itemView) {
            super(itemView);
            root = (FrameLayout) itemView.findViewById(R.id.root);
            hotel_item_lay = (LinearLayout) itemView.findViewById(R.id.hotel_item_lay);
            iv_ad = (ImageView) itemView.findViewById(R.id.iv_ad);
            iv_ad.setVisibility(View.GONE);
            hotel_bg_lay = (RelativeLayout) itemView.findViewById(R.id.hotel_bg_lay);
            iv_hotel_icon = (ImageView) itemView.findViewById(R.id.iv_hotel_icon);
            off_lay = (LinearLayout) itemView.findViewById(R.id.off_lay);
            tv_off_percent = (TextView) itemView.findViewById(R.id.tv_off_percent);
            tv_off_info = (TextView) itemView.findViewById(R.id.tv_off_info);
            tv_vip = (TextView) itemView.findViewById(R.id.tv_vip);
            tv_hotel_name = (TextView) itemView.findViewById(R.id.tv_hotel_name);
            iv_cert_icon = (ImageView) itemView.findViewById(R.id.iv_cert_icon);
            tv_point = (TextView) itemView.findViewById(R.id.tv_point);
            tv_point_tips = (TextView) itemView.findViewById(R.id.tv_point_tips);
            tv_hotel_dis = (TextView) itemView.findViewById(R.id.tv_hotel_dis);
            tv_track = (TextView) itemView.findViewById(R.id.tv_track);
            tv_platform_price = (TextView) itemView.findViewById(R.id.tv_platform_price);
            tv_platform_price.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
            tv_real_price = (TextView) itemView.findViewById(R.id.tv_real_price);
            tv_price_unit = (TextView) itemView.findViewById(R.id.tv_price_unit);
        }
    }

    public interface OnItemClickListener {
        void OnItemClick(View view, int position);
    }

    private HotelListAdapter.OnItemClickListener listener = null;

    public void setOnItemClickListener(HotelListAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }
}
