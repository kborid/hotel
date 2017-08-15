package com.huicheng.hotel.android.ui.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.bumptech.glide.Glide;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.net.bean.HotelInfoBean;
import com.huicheng.hotel.android.ui.custom.RoundedTopImageView;
import com.huicheng.hotel.android.ui.mapoverlay.AMapUtil;
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
//        loadImage(holder.iv_hotel_icon, R.drawable.def_hotel_list, bean.hotelFeaturePic, 690, 500);
        Glide.with(context)
                .load(bean.hotelFeaturePic)
                .placeholder(R.drawable.def_hotel_list)
                .error(R.drawable.def_hotel_list)
                .crossFade()
                .into(holder.iv_hotel_icon);

        if (HotelCommDef.VIP_SUPPORT.equals(bean.vipEnable) && bean.vipPrice > 0 && bean.vipPrice < bean.price) {
            holder.vip_layout.setVisibility(View.VISIBLE);
            holder.tv_vip_price.setText(String.valueOf(bean.vipPrice));
        } else {
            holder.vip_layout.setVisibility(View.GONE);
        }

        switch (type) {
            case HotelCommDef.TYPE_YEGUIREN:
                holder.cardview.setBackgroundResource(ygrRoomItemBackgroundId);
                break;
            case HotelCommDef.TYPE_ALL:
            case HotelCommDef.TYPE_CLOCK:
            default:
                holder.cardview.setBackgroundResource(R.drawable.comm_10radius_white_color);
                break;
        }

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

        float point = 0;
        if (StringUtil.notEmpty(bean.hotelGrade)) {
            point = Float.parseFloat(bean.hotelGrade);
            holder.tv_hotel_point.setText(String.valueOf(point));
        } else {
            holder.tv_hotel_point.setText("0.0");
        }
        if (point >= 4) {
            holder.tv_hotel_point.setBackground(context.getResources().getDrawable(R.drawable.comm_rectangle_btn_assess_high));
        } else if (point >= 3) {
            holder.tv_hotel_point.setBackground(context.getResources().getDrawable(R.drawable.comm_rectangle_btn_assess_mid));
        } else {
            holder.tv_hotel_point.setBackground(context.getResources().getDrawable(R.drawable.comm_rectangle_btn_assess_low));
        }
        holder.tv_hotel_name.setText(bean.hotelName);

        int price = HotelCommDef.TYPE_CLOCK == type ? bean.clockPrice : bean.price;
        if (bean.speciallyPrice > 0) {
            if (price <= bean.speciallyPrice) {
                holder.tv_hotel_price.setVisibility(View.GONE);
            } else {
                holder.tv_hotel_price.setVisibility(View.VISIBLE);
                holder.tv_hotel_price.setText(" " + price + "元起 ");
            }
            holder.tv_hotel_special_price_note.setText("特价：");
            holder.tv_hotel_special_price.setText(bean.speciallyPrice + " 元");
        } else {
            holder.tv_hotel_price.setVisibility(View.GONE);
            holder.tv_hotel_special_price_note.setText("价格：");
            holder.tv_hotel_special_price.setText((price <= 0) ? "暂无" : price + " 元起");
        }

        holder.cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        LinearLayout vip_layout;
        TextView tv_vip_price;
        RoundedTopImageView iv_hotel_icon;
        TextView tv_hotel_point;
        TextView tv_hotel_name;
        TextView tv_hotel_dis;
        TextView tv_hotel_price;
        TextView tv_hotel_special_price_note;
        TextView tv_hotel_special_price;

        HotelViewHolder(View itemView) {
            super(itemView);
            cardview = (CardView) itemView.findViewById(R.id.cardview);
            vip_layout = (LinearLayout) itemView.findViewById(R.id.vip_layout);
            ((TextView) itemView.findViewById(R.id.tv_vip_price_note)).getPaint().setFakeBoldText(true);
            tv_vip_price = (TextView) itemView.findViewById(R.id.tv_vip_price);
            ((TextView) itemView.findViewById(R.id.tv_vip_price_unit)).getPaint().setFakeBoldText(true);
            iv_hotel_icon = (RoundedTopImageView) itemView.findViewById(R.id.iv_hotel_icon);
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
