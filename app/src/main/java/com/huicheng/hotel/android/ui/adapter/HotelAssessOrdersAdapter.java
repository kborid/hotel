package com.huicheng.hotel.android.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.requestbuilder.bean.AssessOrderInfoBean;
import com.huicheng.hotel.android.ui.custom.CommonAssessStarsLayout;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.StringUtil;

import java.util.List;

/**
 * @author kborid
 * @date 2018/1/23 0023.
 */

public class HotelAssessOrdersAdapter extends BaseAdapter {
    private final String TAG = getClass().getName();
    private List<AssessOrderInfoBean> list;
    private Context context;
    private int assessedBackgroundResId;

    public HotelAssessOrdersAdapter(Context context, List<AssessOrderInfoBean> list) {
        this.context = context;
        this.list = list;
    }

    public void setAssessedBackgroundResId(int id){
        this.assessedBackgroundResId = id;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (null == convertView) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.lv_assessorder_item, null);
            viewHolder.root_lay = (LinearLayout) convertView.findViewById(R.id.root_lay);
            viewHolder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
            viewHolder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
            viewHolder.tv_status = (TextView) convertView.findViewById(R.id.tv_status);
            viewHolder.assess_star = (CommonAssessStarsLayout) convertView.findViewById(R.id.assess_star);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // 设置adapter数据
        AssessOrderInfoBean bean = list.get(position);
        if (StringUtil.isEmpty(bean.isevaluated) || "0".equals(bean.isevaluated)) { //待评价
            viewHolder.root_lay.setBackgroundResource(assessedBackgroundResId);
            viewHolder.tv_title.setTextColor(context.getResources().getColor(R.color.white));
            viewHolder.tv_time.setTextColor(context.getResources().getColor(R.color.white));
            viewHolder.tv_status.setTextColor(context.getResources().getColor(R.color.white));
            viewHolder.tv_status.setText("待评价");
            viewHolder.assess_star.setVisibility(View.GONE);
        } else if ("1".equals(bean.isevaluated)) { //已评价
            viewHolder.root_lay.setBackgroundResource(R.drawable.iv_assess_hotel_complete);
            viewHolder.tv_title.setTextColor(context.getResources().getColor(R.color.noDiscountColor));
            viewHolder.tv_time.setTextColor(context.getResources().getColor(R.color.noDiscountColor));
            viewHolder.tv_status.setTextColor(context.getResources().getColor(R.color.noDiscountColor));
            viewHolder.tv_status.setText("已评价");
            viewHolder.assess_star.setVisibility(View.VISIBLE);
            viewHolder.assess_star.setColorStars(Integer.parseInt(bean.grade));
        } else if ("2".equals(bean.isevaluated)) { //已删除
            viewHolder.tv_status.setText("已删除");
        } else { //无
            LogUtil.i(TAG, "warning!!!");
        }

        viewHolder.tv_title.setText(bean.hotelName + "(" + bean.cityName + ")");
        String start = DateUtil.getDay("yyyy年MM月dd日", bean.startTime);
        String end = DateUtil.getDay("dd日", bean.endTime);
        LogUtil.i(TAG, "开始时间：" + start + ", 结束时间：" + end);
        viewHolder.tv_time.setText(start + "-" + end);

        return convertView;
    }

    class ViewHolder {
        LinearLayout root_lay;
        TextView tv_title;
        TextView tv_time;
        TextView tv_status;
        CommonAssessStarsLayout assess_star;
    }
}
