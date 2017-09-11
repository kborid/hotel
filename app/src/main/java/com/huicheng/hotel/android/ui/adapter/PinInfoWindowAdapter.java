package com.huicheng.hotel.android.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.amap.api.maps.AMap;
import com.amap.api.maps.model.Marker;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.ui.custom.RoundedAllImageView;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.net.image.ImageLoader;
import com.prj.sdk.util.BitmapUtils;
import com.prj.sdk.util.StringUtil;

/**
 * @author kborid
 * @date 2016/11/4 0004
 */
public class PinInfoWindowAdapter implements AMap.InfoWindowAdapter {

    private Context context;
    private int pinBackgroundId;

    public PinInfoWindowAdapter(Context context) {
        this.context = context;
        TypedArray ta = context.obtainStyledAttributes(R.styleable.MyTheme);
        pinBackgroundId = ta.getResourceId(R.styleable.MyTheme_mainColor, R.color.mainColor);
        ta.recycle();
    }

    @Override
    public View getInfoWindow(final Marker marker) {
        marker.isInfoWindowEnable();
        final View infoWindow = LayoutInflater.from(context).inflate(R.layout.comm_pininfo_layout, null);
        LinearLayout root_lay = (LinearLayout) infoWindow.findViewById(R.id.root_lay);
        Bitmap bm = BitmapUtils.getAlphaBitmap(context.getResources().getDrawable(R.drawable.iv_pin_bg), context.getResources().getColor(pinBackgroundId));
        root_lay.setBackground(new BitmapDrawable(bm));
        final RoundedAllImageView iv_hotel_icon = (RoundedAllImageView) infoWindow.findViewById(R.id.iv_hotel_icon);
        TextView tv_title = (TextView) infoWindow.findViewById(R.id.tv_title);
        TextView tv_address = (TextView) infoWindow.findViewById(R.id.tv_address);
        ImageView iv_navi = (ImageView) infoWindow.findViewById(R.id.iv_route);

        String json = marker.getSnippet();
        final JSONObject mJson = JSON.parseObject(json);

        if (StringUtil.notEmpty(mJson.getString("icon"))) {
            ImageLoader.getInstance().loadBitmap(new ImageLoader.ImageCallback() {
                @Override
                public void imageCallback(Bitmap bm, String url, String imageTag) {
                    if (null != bm) {
                        iv_hotel_icon.setImageBitmap(bm);
                    }
                }
            }, mJson.getString("icon"), mJson.getString("icon"), 140, 100, -1);
        }
        tv_title.setText(mJson.getString("name"));
        if (StringUtil.isEmpty(mJson.getString("address"))) {
            tv_address.setVisibility(View.GONE);
        } else {
            tv_address.setVisibility(View.VISIBLE);
            tv_address.setText(mJson.getString("address"));
        }

        iv_navi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.calculateRoute(marker);
                }
            }
        });

        infoWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!SessionContext.isLogin()) {
                    context.sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION));
                    return;
                }
                if (null != listener) {
                    int hotelId = 0;
                    try {
                        hotelId = Integer.parseInt(mJson.getString("hotelId"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    listener.changeScreen(hotelId);
                }
            }
        });

        return infoWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    private NaviClickListener listener = null;

    public void setNaviClickListener(NaviClickListener listener) {
        this.listener = listener;
    }

    public interface NaviClickListener {
        void calculateRoute(Marker marker);

        void changeScreen(int hotelId);
    }
}
