package com.huicheng.hotel.android.ui.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.net.bean.HotelSpaceTieCommentInfoBean;
import com.huicheng.hotel.android.ui.activity.HotelSpacePublishActivity;
import com.huicheng.hotel.android.ui.custom.CircleImageView;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;
import com.prj.sdk.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kborid
 * @date 2017/4/2 0002
 */
public class HotelSpaceSubReplyAdapter extends BaseAdapter {

    private Context context;
    private List<HotelSpaceTieCommentInfoBean> list = new ArrayList<>();

    public HotelSpaceSubReplyAdapter(Context context, List<HotelSpaceTieCommentInfoBean> list) {
        this.context = context;
        this.list = list;
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
        final ViewHolder viewHolder;
        if (null == convertView) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.lv_hotelspace_sub_reply_item, null);
            viewHolder.iv_icon = (CircleImageView) convertView.findViewById(R.id.iv_icon);
            viewHolder.user_lay = (LinearLayout) convertView.findViewById(R.id.user_lay);
            viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            viewHolder.tv_name.getPaint().setFakeBoldText(true);
            viewHolder.tv_content = (TextView) convertView.findViewById(R.id.tv_content);
            viewHolder.picture_lay = (LinearLayout) convertView.findViewById(R.id.picture_lay);
            viewHolder.tv_space_comment = (TextView) convertView.findViewById(R.id.tv_space_comment);
            viewHolder.tv_space_support = (TextView) convertView.findViewById(R.id.tv_space_support);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //设置数据
        final HotelSpaceTieCommentInfoBean bean = list.get(position);
//        loadImage(viewHolder.iv_icon, R.drawable.def_photo_gary, bean.replyUserHeadUrl, 50, 50);
        viewHolder.tv_name.setText(bean.replyUserName);
        if (StringUtil.notEmpty(bean.content)) {
            viewHolder.tv_content.setVisibility(View.VISIBLE);
            String strBold = "回复" + bean.beRepliedUserName;
            String str = strBold + " " + bean.content;
            SpannableString ss = new SpannableString(str);
            ss.setSpan(new StyleSpan(Typeface.BOLD), 0, strBold.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            viewHolder.tv_content.setText(ss);
        } else {
            viewHolder.tv_content.setVisibility(View.GONE);
        }

        if (StringUtil.notEmpty(bean.picUrl)) {
            viewHolder.picture_lay.setVisibility(View.VISIBLE);
            ImageView imageView = (ImageView) viewHolder.picture_lay.findViewById(R.id.imageView);
//            loadImage(imageView, bean.picUrl, 1920, 1080);
        } else {
            viewHolder.picture_lay.setVisibility(View.GONE);
        }
        viewHolder.tv_space_comment.setText(String.valueOf(bean.replyCnt));
        viewHolder.tv_space_support.setText(String.valueOf(bean.replyCnt));

        viewHolder.user_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, HotelSpacePublishActivity.class);
                intent.putExtra("reply", viewHolder.tv_name.getText().toString());
                context.startActivity(intent);
            }
        });

        View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                CustomDialog dialog = new CustomDialog(context);
                dialog.setTitle("提示");
                dialog.setMessage("关注该用户，回复时可以@到他/她");
                dialog.setPositiveButton("关注", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        requestAttendUser(bean.replyUserId);
                    }
                });
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
                return true;
            }
        };

        viewHolder.iv_icon.setOnLongClickListener(longClickListener);
        viewHolder.user_lay.setOnLongClickListener(longClickListener);

        return convertView;
    }

    class ViewHolder {
        CircleImageView iv_icon;
        LinearLayout user_lay;
        TextView tv_name;
        TextView tv_content;
        LinearLayout picture_lay;
        TextView tv_space_comment;
        TextView tv_space_support;
    }
}
