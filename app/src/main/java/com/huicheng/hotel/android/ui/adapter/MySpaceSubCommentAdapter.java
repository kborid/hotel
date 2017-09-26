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

import com.bumptech.glide.Glide;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.net.bean.HotelSpaceTieCommentInfoBean;
import com.huicheng.hotel.android.ui.activity.HotelSpacePublishActivity;
import com.huicheng.hotel.android.ui.activity.ImageScaleActivity;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.huicheng.hotel.android.ui.glide.CustomReqURLFormatModelImpl;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.StringUtil;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * @author kborid
 * @date 2017/4/11 0011
 */
public class MySpaceSubCommentAdapter extends BaseAdapter implements DataCallback {

    private final String TAG = getClass().getSimpleName();
    private Context context;
    private List<HotelSpaceTieCommentInfoBean> list = new ArrayList<>();
    private int selectedPosition = 0;

    public MySpaceSubCommentAdapter(Context context, List<HotelSpaceTieCommentInfoBean> list) {
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
    public View getView(final int position, View convertView, ViewGroup parent) {
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
        Glide.with(context)
                .load(new CustomReqURLFormatModelImpl(bean.replyUserHeadUrl))
                .placeholder(R.drawable.def_photo)
                .crossFade()
                .centerCrop()
                .override(150, 150)
                .into(viewHolder.iv_icon);
        viewHolder.tv_name.setText(bean.replyUserName);
        if (StringUtil.notEmpty(bean.content)) {
            viewHolder.tv_content.setVisibility(View.VISIBLE);
            String strBold = "";
            if (StringUtil.notEmpty(bean.beRepliedUserName)) {
                strBold = "回复" + bean.beRepliedUserName + "： ";
            }
            String str = strBold + bean.content;
            SpannableString ss = new SpannableString(str);
            ss.setSpan(new StyleSpan(Typeface.BOLD), 0, strBold.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            viewHolder.tv_content.setText(ss);
        } else {
            viewHolder.tv_content.setVisibility(View.GONE);
        }

        if (StringUtil.notEmpty(bean.picUrl)) {
            viewHolder.picture_lay.setVisibility(View.VISIBLE);
            ImageView imageView = (ImageView) viewHolder.picture_lay.findViewById(R.id.imageView);
            Glide.with(context)
                    .load(new CustomReqURLFormatModelImpl(bean.picUrl))
                    .placeholder(R.color.hintColor)
                    .crossFade()
                    .centerCrop()
                    .override(500, 500)
                    .into(imageView);
        } else {
            viewHolder.picture_lay.setVisibility(View.GONE);
        }

        viewHolder.picture_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ImageScaleActivity.class);
                intent.putExtra("url", bean.picUrl);
                int[] location = new int[2];
                ImageView imageView = (ImageView) viewHolder.picture_lay.findViewById(R.id.imageView);
                imageView.getLocationOnScreen(location);
                intent.putExtra("locationX", location[0]);//必须
                intent.putExtra("locationY", location[1]);//必须
                intent.putExtra("width", imageView.getWidth());//必须
                intent.putExtra("height", imageView.getHeight());//必须
                context.startActivity(intent);
            }
        });

        viewHolder.tv_space_comment.setText(String.valueOf(bean.replyCnt));
        viewHolder.tv_space_support.setText(String.valueOf(bean.praiseCnt));

        viewHolder.user_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, HotelSpacePublishActivity.class);
                String replyString = "回复" + viewHolder.tv_name.getText().toString() + "：";
                intent.putExtra("reply", replyString);
                intent.putExtra("hotelId", bean.hotelId);
                intent.putExtra("replyDetail", bean);
                context.startActivity(intent);
            }
        });
        viewHolder.tv_space_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.user_lay.performClick();
            }
        });

        viewHolder.tv_space_support.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPosition = position;
                requestSupport(bean);
            }
        });

        viewHolder.iv_icon.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (SessionContext.mUser.user.userid.equals(bean.replyUserId)) {
                    CustomDialog dialog = new CustomDialog(context);
                    dialog.setTitle("提示");
                    dialog.setMessage("是否删除该条评论？");
                    dialog.setNegativeButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            selectedPosition = position;
                            requestDeleteComment(bean);
                        }
                    });
                    dialog.setPositiveButton("否", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.show();
                } else {
                    CustomDialog dialog = new CustomDialog(context);
                    dialog.setTitle("提示");
                    dialog.setMessage("关注该用户，回复时可以@到他/她");
                    dialog.setPositiveButton("关注", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            selectedPosition = position;
                            requestCareUser(bean);
                        }
                    });
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.show();
                }
                return true;
            }
        });
        viewHolder.user_lay.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                viewHolder.iv_icon.performLongClick();
                return true;
            }
        });

        return convertView;
    }

    @Override
    public void preExecute(ResponseData request) {

    }

    @Override
    public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.ATTEND_USER) {
                CustomToast.show("关注成功", CustomToast.LENGTH_SHORT);
            } else if (request.flag == AppConst.HOTEL_TIE_SUPPORT) {
                LogUtil.i(TAG, "json = " + response.body.toString());
                HotelSpaceTieCommentInfoBean bean = list.get(selectedPosition);
                bean.praiseCnt += 1;
                list.set(selectedPosition, bean);
                notifyDataSetChanged();
            } else if (request.flag == AppConst.HOTEL_COMMENT_DELETE) {
                list.remove(selectedPosition);
                notifyDataSetChanged();
                CustomToast.show("删除成功", CustomToast.LENGTH_SHORT);
            }
        }
    }

    @Override
    public void notifyError(ResponseData request, ResponseData response, Exception e) {
        String message;
        if (e != null && e instanceof ConnectException) {
            message = context.getResources().getString(R.string.dialog_tip_net_error);
        } else {
            message = response != null && response.data != null ? response.data.toString() : context.getResources().getString(R.string.dialog_tip_null_error);
        }
        CustomToast.show(message, CustomToast.LENGTH_SHORT);
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

    private void requestCareUser(HotelSpaceTieCommentInfoBean bean) {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("attentuserid", bean.replyUserId);

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.ATTEND_USER;
        d.flag = AppConst.ATTEND_USER;

        DataLoader.getInstance().loadData(this, d);
    }

    private void requestSupport(HotelSpaceTieCommentInfoBean bean) {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("replyId", String.valueOf(bean.id));
        b.addBody("hotelId", String.valueOf(bean.hotelId));
        b.addBody("articleId", String.valueOf(bean.articleId));

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.HOTEL_TIE_SUPPORT;
        d.flag = AppConst.HOTEL_TIE_SUPPORT;

        DataLoader.getInstance().loadData(this, d);
    }

    private void requestDeleteComment(HotelSpaceTieCommentInfoBean bean) {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("replyId", String.valueOf(bean.id));

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.HOTEL_COMMENT_DELETE;
        d.flag = AppConst.HOTEL_COMMENT_DELETE;

        DataLoader.getInstance().loadData(this, d);
    }

}