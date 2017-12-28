package com.huicheng.hotel.android.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.PlaneOrderManager;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneTicketInfoBean;
import com.prj.sdk.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @auth kborid
 * @date 2017/12/27 0027.
 */

public class PlaneTicketVendorItemAdapter extends BaseAdapter {

    private static final HashMap<Integer, String> cabinMap = new HashMap<Integer, String>() {
        {
            put(0, "经济舱");
            put(1, "头等舱");
            put(2, "商务舱");
            put(3, "经济舱精选");
            put(4, "经济舱Y舱");
            put(5, "超值头等舱");
        }
    };

    private Context context;
    private List<PlaneTicketInfoBean.VendorInfo> mList = new ArrayList<>();

    public PlaneTicketVendorItemAdapter(Context context, List<PlaneTicketInfoBean.VendorInfo> list) {
        this.context = context;
        this.mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (null == convertView) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.lv_plane_ticket_item, null);
            viewHolder.tv_price = (TextView) convertView.findViewById(R.id.tv_price);
            viewHolder.tv_discount_cabin = (TextView) convertView.findViewById(R.id.tv_discount_cabin);
            viewHolder.tv_tgq = (TextView) convertView.findViewById(R.id.tv_tgq);
            viewHolder.flight_base_layout = (LinearLayout) convertView.findViewById(R.id.flight_base_layout);
            viewHolder.tv_air_company = (TextView) convertView.findViewById(R.id.tv_air_company);
            viewHolder.tv_vendor_name = (TextView) convertView.findViewById(R.id.tv_vendor_name);
            viewHolder.remark_layout = (LinearLayout) convertView.findViewById(R.id.remark_layout);
            viewHolder.tv_order = (TextView) convertView.findViewById(R.id.tv_order);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        PlaneTicketInfoBean.VendorInfo vendorInfo = mList.get(position);
        viewHolder.tv_price.setText(String.valueOf(vendorInfo.barePrice));
        String discount_cabin = "";
        if (StringUtil.notEmpty(vendorInfo.discount)) {
            float discount = 1.1f;
            try {
                discount = Float.parseFloat(vendorInfo.discount);
            } catch (Exception e) {
                e.printStackTrace();
            }
            discount_cabin = (discount > 0f) ? "" : String.format("%1$0.1f折", discount);
        }
        viewHolder.tv_discount_cabin.setText(discount_cabin);
        viewHolder.tv_discount_cabin.append(cabinMap.get(vendorInfo.cabinType));
        viewHolder.tv_air_company.setText(PlaneOrderManager.instance.getCurrTicketInfo().com);
        viewHolder.tv_vendor_name.setText(vendorInfo.domain);

        viewHolder.remark_layout.setVisibility(View.GONE);

        viewHolder.tv_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != listener) {
                    listener.OnItemClick(v, position);
                }
            }
        });
        return convertView;
    }

    private class ViewHolder {
        TextView tv_price;
        TextView tv_discount_cabin;
        TextView tv_tgq;
        LinearLayout flight_base_layout;
        TextView tv_air_company;
        TextView tv_vendor_name;
        LinearLayout remark_layout;
        TextView tv_order;
    }

    private OnItemRecycleViewClickListener listener = null;

    public void setOnItemRecycleViewClickListener(OnItemRecycleViewClickListener listener) {
        this.listener = listener;
    }
}
