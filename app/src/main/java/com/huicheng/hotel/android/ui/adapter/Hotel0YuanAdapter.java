package com.huicheng.hotel.android.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.net.bean.CouponDetailInfoBean;
import com.huicheng.hotel.android.ui.custom.RoundedAllImageView;
import com.prj.sdk.net.image.ImageLoader;
import com.prj.sdk.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author kborid
 * @date 2017/1/12 0012
 */
public class Hotel0YuanAdapter extends RecyclerView.Adapter<Hotel0YuanAdapter.Hotel0YuanViewHolder> {
    private final String TAG = getClass().getSimpleName();

    private Context context;
    private List<CouponDetailInfoBean> list;
    private List<Integer> marginValue;
    private SparseArray<int[]> colorMap;

    public Hotel0YuanAdapter(Context context, List<CouponDetailInfoBean> list) {
        this.context = context;
        this.list = list;
        marginValue = new ArrayList<>();
        colorMap = new SparseArray<>();
    }

    @Override
    public Hotel0YuanViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Hotel0YuanViewHolder hotel0YuanViewHolder = new Hotel0YuanViewHolder(LayoutInflater.from(context).inflate(R.layout.lv_0yuan_item, parent, false));
        hotel0YuanViewHolder.setIsRecyclable(true);
        return hotel0YuanViewHolder;
    }

    @Override
    public void onBindViewHolder(final Hotel0YuanViewHolder holder, final int position) {
        // 随机left margin, 模拟瀑布效果.
        StaggeredGridLayoutManager.LayoutParams lp = (StaggeredGridLayoutManager.LayoutParams) holder.root_view.getLayoutParams();

        if (marginValue.size() <= position) {
            if (position == 1 || position == 3) {
                marginValue.add(Utils.dip2px(60));
            } else {
                marginValue.add(0);
            }

            Random random = new Random();
            int ranColorLeft = 0xff000000 | random.nextInt(0x00ffffff);
            int ranColorRight = 0xff000000 | random.nextInt(0x00ffffff);
            colorMap.put(position, new int[]{ranColorLeft, ranColorRight});
        }
        lp.leftMargin = marginValue.get(position);
        holder.root_view.setLayoutParams(lp);

        String url = list.get(position).picPath;
        ImageLoader.getInstance().loadBitmap(new ImageLoader.ImageCallback() {
            @Override
            public void imageCallback(Bitmap bm, String url, String imageTag) {
                if (null != bm) {
                    holder.iv_hotel_bg.setImageBitmap(bm);
                }
            }
        }, url, url, 400, 240, -1);

        GradientDrawable gd = null;
        if (HotelCommDef.COUPON_HAVE.equals(list.get(position).emptyMark)) {
            holder.tv_title.setText(list.get(position).name);
            holder.tv_title.setTextColor(context.getResources().getColor(R.color.mainColor));
            gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colorMap.get(position));
            holder.iv_hotel_alpha.setAlpha(0.6f);

            holder.root_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.OnItemClick(v, position);
                    }
                }
            });

        } else {
            holder.tv_title.setText("已抢完");
            holder.tv_title.setTextColor(context.getResources().getColor(R.color.white));
            gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{context.getResources().getColor(R.color.mainColorDark), context.getResources().getColor(R.color.mainColorDark)});
            holder.iv_hotel_alpha.setAlpha(0.8f);

            holder.root_view.setOnClickListener(null);
        }
        gd.setCornerRadius(Utils.dip2px(20));
        holder.iv_hotel_alpha.setBackground(gd);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class Hotel0YuanViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout root_view;
        private TextView tv_title;
        private RoundedAllImageView iv_hotel_bg;
        private RoundedAllImageView iv_hotel_alpha;

        public Hotel0YuanViewHolder(View itemView) {
            super(itemView);
            root_view = (RelativeLayout) itemView.findViewById(R.id.root_view);
            tv_title = (TextView) itemView.findViewById(R.id.tv_title);
            iv_hotel_bg = (RoundedAllImageView) itemView.findViewById(R.id.iv_hotel_bg);
            iv_hotel_alpha = (RoundedAllImageView) itemView.findViewById(R.id.iv_hotel_alpha);
        }
    }

    private OnItemClickListeners listener = null;
    public interface OnItemClickListeners {
        void OnItemClick(View v, int index);
    }
    public void setOnItemClickListener(OnItemClickListeners listener) {
        this.listener = listener;
    }
}
