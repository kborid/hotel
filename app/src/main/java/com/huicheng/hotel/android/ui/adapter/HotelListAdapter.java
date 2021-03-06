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
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.control.LocationInfo;
import com.huicheng.hotel.android.requestbuilder.bean.HotelInfoBean;
import com.huicheng.hotel.android.ui.activity.HtmlActivity;
import com.huicheng.hotel.android.ui.glide.CustomReqURLFormatModelImpl;
import com.huicheng.hotel.android.ui.listener.OnRecycleViewItemClickListener;
import com.huicheng.hotel.android.ui.mapoverlay.AMapUtil;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.util.StringUtil;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

/**
 * @auth kborid
 * @date 2017/7/17.
 */

public class HotelListAdapter extends RecyclerView.Adapter<HotelListAdapter.HotelViewHolder> {
    private Context context;
    private List<HotelInfoBean> list;
    private int type = 0;
    private int ygrRoomItemBackgroundId;
    private String lonLat = null;
    private String landmark = "";

    public HotelListAdapter(Context context, List<HotelInfoBean> list, int type) {
        this.context = context;
        this.list = list;
        this.type = type;
        TypedArray ta = context.obtainStyledAttributes(R.styleable.MyTheme);
        ygrRoomItemBackgroundId = ta.getResourceId(R.styleable.MyTheme_roomItemGradient, R.drawable.roomitem_ygr_gradient);
        ta.recycle();
    }

    public void setLandMarkLonLat(String landmark, String lonLat) {
        this.landmark = landmark;
        this.lonLat = lonLat;
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
            holder.hotel_item_lay.setVisibility(View.INVISIBLE);
            holder.iv_ad.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(new CustomReqURLFormatModelImpl(bean.picPath))
                    .crossFade()
                    .centerCrop()
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

            //用户是否是该酒店会员
            boolean isVip = bean.vip == 1; //1是会员，0不是会员
            // 是否显示vip Flag
            holder.tv_vip.setVisibility(View.GONE);
            boolean isShowVipFlag = false;
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
                    isShowVipFlag = true;
                    holder.tv_vip.setText(context.getString(R.string.vip_price_tips));
                }
            }

            // 下方价格显示逻辑
            String price = "暂无";
            switch (type) {
                case HotelCommDef.TYPE_ALL:
                case HotelCommDef.TYPE_YEGUIREN:
                    if (mPrice > 0) {
                        //判断是否显示特价
                        if (bean.speciallyPrice >= mPrice || bean.speciallyPrice <= 0) {
                            holder.tv_platform_price.setVisibility(View.INVISIBLE);
                            price = String.valueOf(mPrice);
                            holder.tv_price_unit.setVisibility(View.VISIBLE);

                            holder.off_lay.setVisibility(View.GONE);
                        } else {
                            holder.tv_platform_price.setVisibility(View.VISIBLE);
                            holder.tv_platform_price.setText(String.format(context.getString(R.string.rmbStr), String.valueOf(mPrice)));
                            price = String.valueOf(bean.speciallyPrice);
                            holder.tv_price_unit.setVisibility(View.VISIBLE);

                            //显示特价OFF_SALE信息，特价/平台价
                            holder.off_lay.setVisibility(View.VISIBLE);
                            DecimalFormat df = (DecimalFormat) NumberFormat.getPercentInstance();
                            float specialPercent = (float) (bean.speciallyPrice - mPrice) / mPrice;
                            holder.tv_off_percent.setText(df.format(specialPercent));
                            holder.tv_off_info.setText(bean.comment);
                            holder.off_lay.setBackgroundColor(context.getResources().getColor(R.color.offInfoColor1));
                        }

                        //判断是否显示VIP价
                        if (isShowVipFlag) {
                            if (isVip) {
                                holder.tv_platform_price.setVisibility(View.VISIBLE);
                                holder.tv_platform_price.setText(String.format(context.getString(R.string.rmbStr), String.valueOf(mPrice)));
                                price = String.valueOf(bean.vipPrice);
                                holder.tv_price_unit.setVisibility(View.VISIBLE);

                                //显示VIP价OFF_SALE信息，VIP价格/平台价
                                holder.tv_vip.setVisibility(View.GONE); //如果用户是酒店会员，则不显示VIP条
                                holder.off_lay.setVisibility(View.VISIBLE);
                                DecimalFormat df = (DecimalFormat) NumberFormat.getPercentInstance();
                                float specialPercent = (float) (bean.vipPrice - mPrice) / mPrice;
                                holder.tv_off_percent.setText(df.format(specialPercent));
                                holder.tv_off_info.setText("粉丝价");
                                holder.off_lay.setBackgroundColor(context.getResources().getColor(R.color.offInfoColor3));
                            } else {
                                //显示特价OFF_SALE信息，特价/平台价
                                holder.tv_vip.setVisibility(View.VISIBLE); //如果用户不是酒店会员，则显示VIP条
//                                holder.off_lay.setVisibility(View.VISIBLE);
                                holder.off_lay.setBackgroundColor(context.getResources().getColor(R.color.offInfoColor2));
                            }
                        }
                    } else {
                        holder.tv_platform_price.setVisibility(View.INVISIBLE);
                        holder.tv_price_unit.setVisibility(View.GONE);
                        holder.off_lay.setVisibility(View.GONE);
                    }
                    holder.tv_real_price.setText(price);
                    break;
                case HotelCommDef.TYPE_CLOCK:
                    holder.tv_platform_price.setVisibility(View.INVISIBLE);
                    if (bean.clockPrice != 0) { //价格为0时判断处理
                        price = String.valueOf(bean.clockPrice);
                        holder.tv_price_unit.setVisibility(View.VISIBLE);
                    }
                    holder.tv_real_price.setText(price);
                    holder.off_lay.setVisibility(View.GONE);
                    break;
            }


            // 距离信息
            try {
                LatLng start = null, des = null;
                String landMarkStr = "";
                float lon, lat;
                if (StringUtil.notEmpty(lonLat)) {
                    String[] pos = lonLat.split("\\|");
                    lon = Float.valueOf(pos[1]);
                    lat = Float.valueOf(pos[0]);
                    landMarkStr = "距离" + landmark;
                } else {
                    lon = Float.parseFloat(LocationInfo.instance.getLon());
                    lat = Float.parseFloat(LocationInfo.instance.getLat());
                }
                if (lon != 0 && lat != 0 && StringUtil.notEmpty(bean.hotelCoordinate)) {
                    start = new LatLng(lat, lon);
                    String[] pos = bean.hotelCoordinate.split("\\|");
                    des = new LatLng(Float.valueOf(pos[0]), Float.valueOf(pos[1]));
                    float dis = AMapUtils.calculateLineDistance(start, des);
                    holder.tv_hotel_dis.setText(landMarkStr + AMapUtil.getFriendlyLength((int) dis));
                } else {
                    holder.tv_hotel_dis.setText("暂无距离信息");
                }
            } catch (Exception e) {
                e.printStackTrace();
                holder.tv_hotel_dis.setText("暂无距离信息");
            }

            //用户浏览痕迹
            String trackStr = "暂无人浏览过";
            if (StringUtil.notEmpty(bean.lastVisit)) {
                trackStr = String.format(context.getString(R.string.track_str), bean.lastVisit);
            }
            holder.tv_track.setText(trackStr);

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
        ImageView iv_ad;
        LinearLayout hotel_item_lay;
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
            iv_ad = (ImageView) itemView.findViewById(R.id.iv_ad);
            hotel_item_lay = (LinearLayout) itemView.findViewById(R.id.hotel_item_lay);
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

    private OnRecycleViewItemClickListener listener = null;

    public void setOnRecycleViewItemClickListener(OnRecycleViewItemClickListener listener) {
        this.listener = listener;
    }
}
