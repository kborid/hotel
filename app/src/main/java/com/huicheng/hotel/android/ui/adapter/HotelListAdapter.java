package com.huicheng.hotel.android.ui.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.huicheng.hotel.android.ui.custom.RoundedTopImageView;
import com.huicheng.hotel.android.ui.mapoverlay.AMapUtil;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;

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
        HotelListAdapter.HotelViewHolder hotelViewHolder = null;
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(final HotelListAdapter.HotelViewHolder holder, final int position) {

        HotelInfoBean bean = list.get(position);
        holder.tv_hotel_name.setText(bean.hotelName);
//        loadImage(holder.iv_hotel_icon, R.drawable.def_hotel_list, bean.hotelFeaturePic, 690, 500);
        Glide.with(context)
                .load(bean.hotelFeaturePic)
                .placeholder(R.drawable.def_hotel_list)
                .error(R.drawable.def_hotel_list)
                .crossFade()
                .into(holder.iv_hotel_icon);

        // 是否显示vip价格
        holder.vip_layout.setVisibility(View.GONE);
        if (HotelCommDef.VIP_SUPPORT.equals(bean.vipEnable) && bean.vipPrice > 0) {
            int minPrice = 0;
            switch (type) {
                case HotelCommDef.TYPE_ALL:
                    minPrice = bean.price;
                    break;
                case HotelCommDef.TYPE_CLOCK:
                    minPrice = bean.clockPrice;
                    break;
                case HotelCommDef.TYPE_YEGUIREN:
                    minPrice = bean.yeguirenPrice;
                    break;
            }

            if (minPrice != 0 && bean.speciallyPrice != 0) {
                minPrice = Math.min(minPrice, bean.speciallyPrice);
            } else {
                minPrice = 0 != minPrice ? minPrice : bean.speciallyPrice;
            }
            if (bean.vipPrice < minPrice) {
                holder.vip_layout.setVisibility(View.VISIBLE);
                holder.tv_vip_price.setText(String.valueOf(bean.vipPrice) + "元");
            }
        }

        // 下方显示价格逻辑
        String note = "价格：", price = "暂无";
        switch (type) {
            case HotelCommDef.TYPE_ALL:
//                holder.detail_lay.setBackgroundResource(R.drawable.lv_hotel_item_bg);
                if (bean.price > 0) {
                    if (bean.speciallyPrice > bean.price || bean.speciallyPrice <= 0) {
                        holder.tv_hotel_price.setVisibility(View.GONE);
                        price = bean.price + " 元起";
                    } else {
                        // 判断是否显示带删除线的平台价
                        if (bean.speciallyPrice == bean.price) {
                            holder.tv_hotel_price.setVisibility(View.GONE);
                        } else {
                            holder.tv_hotel_price.setVisibility(View.VISIBLE);
                            holder.tv_hotel_price.setText(" " + bean.price + "元 ");
                        }

                        note = "特价：";
                        price = bean.speciallyPrice + " 元起";
                    }
                } else {
                    holder.tv_hotel_price.setVisibility(View.GONE);
                }
                holder.tv_hotel_special_price_note.setText(note);
                holder.tv_hotel_special_price.setText(price);
                break;
            case HotelCommDef.TYPE_CLOCK:
//                holder.detail_lay.setBackgroundResource(R.drawable.lv_hotel_item_bg);
                holder.tv_hotel_price.setVisibility(View.GONE);
                note = "价格：";
                price = bean.clockPrice != 0 ? bean.clockPrice + " 元起" : price; //价格为0时判断处理
                holder.tv_hotel_special_price_note.setText(note);
                holder.tv_hotel_special_price.setText(price);
                break;
            case HotelCommDef.TYPE_YEGUIREN:
//                holder.detail_lay.setBackgroundResource(ygrRoomItemBackgroundId);
                if (bean.yeguirenPrice > 0) {
                    if (bean.speciallyPrice > bean.yeguirenPrice || bean.speciallyPrice <= 0) {
                        holder.tv_hotel_price.setVisibility(View.GONE);
                        price = bean.yeguirenPrice + " 元起";
                    } else {
                        // 判断是否显示带删除线的平台价
                        if (bean.speciallyPrice == bean.yeguirenPrice) {
                            holder.tv_hotel_price.setVisibility(View.GONE);
                        } else {
                            holder.tv_hotel_price.setVisibility(View.VISIBLE);
                            holder.tv_hotel_price.setText(" " + bean.yeguirenPrice + "元 ");
                        }

                        note = "特价：";
                        price = bean.speciallyPrice + " 元起";
                    }
                } else {
                    holder.tv_hotel_price.setVisibility(View.GONE);
                }
                holder.tv_hotel_special_price_note.setText(note);
                holder.tv_hotel_special_price.setText(price);
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
            holder.tv_hotel_dis.setVisibility(View.VISIBLE);
        } else {
            holder.tv_hotel_dis.setVisibility(View.GONE);
        }

        // 评分信息
        float point = 0;
        if (StringUtil.notEmpty(bean.hotelGrade)) {
            point = Float.parseFloat(bean.hotelGrade);
            holder.tv_hotel_point.setText(String.valueOf(point));
            holder.tv_hotel_point.setVisibility(View.VISIBLE);
        } else {
            holder.tv_hotel_point.setText("0.0");
            holder.tv_hotel_point.setVisibility(View.GONE);
        }
        if (point >= 4) {
            holder.tv_hotel_point.setBackground(context.getResources().getDrawable(R.drawable.comm_rectangle_btn_assess_high));
        } else if (point >= 3) {
            holder.tv_hotel_point.setBackground(context.getResources().getDrawable(R.drawable.comm_rectangle_btn_assess_mid));
        } else {
            holder.tv_hotel_point.setBackground(context.getResources().getDrawable(R.drawable.comm_rectangle_btn_assess_low));
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

        // 点击事件
        holder.cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        CardView cardview;
        RelativeLayout vip_layout;
        TextView tv_vip_price;
        RoundedTopImageView iv_hotel_icon;
        LinearLayout detail_lay;
        TextView tv_hotel_point;
        TextView tv_hotel_name;
        TextView tv_hotel_dis;
        TextView tv_hotel_price;
        TextView tv_hotel_special_price_note;
        TextView tv_hotel_special_price;
        ImageView iv_cert_icon;

        HotelViewHolder(View itemView) {
            super(itemView);
            cardview = (CardView) itemView.findViewById(R.id.cardview);
            vip_layout = (RelativeLayout) itemView.findViewById(R.id.vip_layout);
            ((TextView) itemView.findViewById(R.id.tv_vip_note)).getPaint().setFakeBoldText(true);
            tv_vip_price = (TextView) itemView.findViewById(R.id.tv_vip_price);
            tv_vip_price.getPaint().setFakeBoldText(true);
            iv_hotel_icon = (RoundedTopImageView) itemView.findViewById(R.id.iv_hotel_icon);
            detail_lay = (LinearLayout) itemView.findViewById(R.id.detail_lay);
            tv_hotel_point = (TextView) itemView.findViewById(R.id.tv_point);
            tv_hotel_name = (TextView) itemView.findViewById(R.id.tv_hotel_name);
            tv_hotel_name.getPaint().setFakeBoldText(true);
            tv_hotel_dis = (TextView) itemView.findViewById(R.id.tv_hotel_dis);
            tv_hotel_price = (TextView) itemView.findViewById(R.id.tv_hotel_price);
            tv_hotel_price.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
            tv_hotel_special_price_note = (TextView) itemView.findViewById(R.id.tv_hotel_special_price_note);
            tv_hotel_special_price_note.getPaint().setFakeBoldText(true);
            tv_hotel_special_price = (TextView) itemView.findViewById(R.id.tv_hotel_special_price);
            tv_hotel_special_price.getPaint().setFakeBoldText(true);
            iv_cert_icon = (ImageView) itemView.findViewById(R.id.iv_cert_icon);
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
