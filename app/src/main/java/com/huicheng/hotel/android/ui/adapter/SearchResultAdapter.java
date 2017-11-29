package com.huicheng.hotel.android.ui.adapter;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.net.bean.HotelInfoBean;
import com.prj.sdk.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @auth kborid
 * @date 2017/8/31.
 */

public class SearchResultAdapter extends BaseAdapter {
    private Context context;
    private List<HotelInfoBean> list = new ArrayList<>();
    private String highStr = "";

    public SearchResultAdapter(Context context, List<HotelInfoBean> list) {
        this.context = context;
        this.list = list;
    }

    public void setHighLightShowString(String highStr) {
        this.highStr = highStr;
//        notifyDataSetChanged();
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
        ViewHolder viewHolder;
        if (null == convertView) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.lv_search_result_item, null);
            viewHolder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
            viewHolder.tv_summary = (TextView) convertView.findViewById(R.id.tv_summary);
            viewHolder.tv_type = (TextView) convertView.findViewById(R.id.tv_type);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        HotelInfoBean bean = list.get(position);
        //酒店名字
        viewHolder.tv_title.setText(updateHighLightString(bean.hotelName, highStr));
        //酒店价格、评分、地址或地标地址
        StringBuilder sb_summary = new StringBuilder("");
        if (HotelCommDef.TYPE_LAND_MARK.equals(bean.type)) { //01类型为地标
            viewHolder.tv_type.setText("地标");
        } else if (HotelCommDef.TYPE_HOTEL.equals(bean.type)) { //00类型为酒店
            viewHolder.tv_type.setText("酒店");
            sb_summary.append(String.format(context.getResources().getString(R.string.rmbStr), String.valueOf(bean.price)));
            sb_summary.append("，");
            if (StringUtil.notEmpty(bean.hotelGrade)) {
                if (!"0".equals(bean.hotelGrade) && !"0.0".equals(bean.hotelGrade)) {
                    String point = bean.hotelGrade + "分";
                    sb_summary.append(point).append("，");
                }
            }
        }
        sb_summary.append(bean.hotelAddress);
        viewHolder.tv_summary.setText(updateHighLightString(sb_summary.toString(), highStr));

        return convertView;
    }

    class ViewHolder {
        TextView tv_title;
        TextView tv_summary;
        TextView tv_type;
    }

    //设置高亮显示
    private SpannableStringBuilder updateHighLightString(String original, String highStr) {
        int originalLen = original.length();
        int highStrLen = highStr.length();
        SpannableStringBuilder style = new SpannableStringBuilder(original);

        if (StringUtil.notEmpty(highStr) && original.contains(highStr)) {
            ArrayList<Integer> index = new ArrayList<>();
            for (int i = 0; i < originalLen; i += highStrLen) {
                i = original.indexOf(highStr, i);
                if (i == -1) {
                    break;
                } else {
                    index.add(i);
                }
            }
            for (int i = 0; i < index.size(); i++) {
                style.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.mainColor)), index.get(i), index.get(i) + highStrLen, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            }
        }
        return style;
    }
}
