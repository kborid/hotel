package com.huicheng.hotel.android.ui.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.net.bean.HotelSpaceTieCommentInfoBean;
import com.huicheng.hotel.android.ui.activity.HotelSpacePublishActivity;
import com.huicheng.hotel.android.ui.activity.ImageScaleActivity;
import com.huicheng.hotel.android.ui.custom.CircleImageView;
import com.huicheng.hotel.android.ui.custom.MyListViewWidget;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.image.ImageLoader;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.widget.CustomToast;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kborid
 * @date 2017/4/11 0011
 */
public class MySpaceCommentAdapter extends BaseAdapter implements DataCallback {

    private Context context;
    private List<HotelSpaceTieCommentInfoBean> list = new ArrayList<>();
    private int selectedPosition = 0;

    public MySpaceCommentAdapter(Context context, List<HotelSpaceTieCommentInfoBean> list) {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.lv_hotelspace_reply_item, null);
            viewHolder.iv_icon = (CircleImageView) convertView.findViewById(R.id.iv_icon);
            viewHolder.user_lay = (LinearLayout) convertView.findViewById(R.id.user_lay);
            viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            viewHolder.tv_name.getPaint().setFakeBoldText(true);
            viewHolder.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
            viewHolder.tv_content = (TextView) convertView.findViewById(R.id.tv_content);
            viewHolder.picture_lay = (LinearLayout) convertView.findViewById(R.id.picture_lay);
            viewHolder.tv_space_comment = (TextView) convertView.findViewById(R.id.tv_space_comment);
            viewHolder.tv_space_support = (TextView) convertView.findViewById(R.id.tv_space_support);
            viewHolder.sub_reply_lay = (LinearLayout) convertView.findViewById(R.id.sub_reply_lay);
            viewHolder.tv_more_reply = (TextView) convertView.findViewById(R.id.tv_more_reply);
            viewHolder.tv_more_reply.getPaint().setFakeBoldText(true);
            viewHolder.listview = (MyListViewWidget) convertView.findViewById(R.id.listview);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //设置list数据
        final HotelSpaceTieCommentInfoBean bean = list.get(position);
        if (StringUtil.notEmpty(bean.replyUserHeadUrl)) {
            viewHolder.iv_icon.setImageResource(R.drawable.def_photo);
            ImageLoader.getInstance().loadBitmap(new ImageLoader.ImageCallback() {
                @Override
                public void imageCallback(Bitmap bm, String url, String imageTag) {
                    if (null != bm) {
                        viewHolder.iv_icon.setImageBitmap(bm);
                    }
                }
            }, bean.replyUserHeadUrl, bean.replyUserHeadUrl, 50, 50, -1);
        }
        viewHolder.tv_name.setText(bean.replyUserName);
        viewHolder.tv_date.setText(DateUtil.getDay("M月d日 HH:mm", bean.createTime));
        if (StringUtil.notEmpty(bean.content)) {
            viewHolder.tv_content.setVisibility(View.VISIBLE);
            viewHolder.tv_content.setText(bean.content);
        } else {
            viewHolder.tv_content.setVisibility(View.GONE);
        }

        if (StringUtil.notEmpty(bean.picUrl)) {
            viewHolder.picture_lay.setVisibility(View.VISIBLE);
            final ImageView imageView = (ImageView) viewHolder.picture_lay.findViewById(R.id.imageView);
            if (StringUtil.notEmpty(bean.replyUserHeadUrl)) {
                imageView.setImageResource(R.color.hintColor);
                ImageLoader.getInstance().loadBitmap(new ImageLoader.ImageCallback() {
                    @Override
                    public void imageCallback(Bitmap bm, String url, String imageTag) {
                        if (null != bm) {
                            imageView.setImageBitmap(bm);
                        }
                    }
                }, bean.picUrl, bean.picUrl, 1920, 1080, -1);
            }
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

        viewHolder.tv_space_support.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPosition = position;
                requestSupport(list.get(position));
            }
        });

        viewHolder.listview.setAdapter(new MySpaceSubCommentAdapter(context, bean.replyList));

        if (StringUtil.notEmpty(bean.replyHolder) && StringUtil.notEmpty(bean.replyHolder.replyusername) && bean.replyCnt > 0) {
            viewHolder.sub_reply_lay.setVisibility(View.VISIBLE);
            if (bean.replyList.size() <= 0) {
                viewHolder.tv_more_reply.setVisibility(View.VISIBLE);
                viewHolder.tv_more_reply.setText(String.format(context.getResources().getString(R.string.moreReply), bean.replyHolder.replyusername, bean.replyCnt));
                viewHolder.listview.setVisibility(View.GONE);
            } else {
                viewHolder.tv_more_reply.setVisibility(View.GONE);
                viewHolder.listview.setVisibility(View.VISIBLE);
            }
        } else {
            viewHolder.sub_reply_lay.setVisibility(View.GONE);
            viewHolder.tv_more_reply.setVisibility(View.GONE);
            viewHolder.listview.setVisibility(View.GONE);
        }

        viewHolder.tv_more_reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPosition = position;
                requestSubCommentInfo(list.get(position));
            }
        });

        viewHolder.tv_space_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.user_lay.performClick();
            }
        });
        viewHolder.user_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, HotelSpacePublishActivity.class);
                String replyString = "回复" + viewHolder.tv_name.getText().toString() + "：";
                intent.putExtra("reply", replyString);
                intent.putExtra("replyDetail", bean);
                intent.putExtra("hotelId", bean.hotelId);
                context.startActivity(intent);
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
                            requestDeleteComment(list.get(position));
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
                            requestCareUser(list.get(position));
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
                System.out.println("json = " + response.body.toString());
                HotelSpaceTieCommentInfoBean bean = list.get(selectedPosition);
                bean.praiseCnt += 1;
                list.set(selectedPosition, bean);
                notifyDataSetChanged();
            } else if (request.flag == AppConst.HOTEL_TIE_SUB_COMMENT) {
                System.out.println("。。。。。json = " + response.body.toString());
                HotelSpaceTieCommentInfoBean bean = list.get(selectedPosition);
                List<HotelSpaceTieCommentInfoBean> allReplyList = JSON.parseArray(response.body.toString(), HotelSpaceTieCommentInfoBean.class);
                bean.replyList.clear();
                bean.replyList.addAll(allReplyList);
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
        TextView tv_date;
        TextView tv_content;
        LinearLayout picture_lay;
        TextView tv_space_comment;
        TextView tv_space_support;
        LinearLayout sub_reply_lay;
        TextView tv_more_reply;
        MyListViewWidget listview;
    }

    private void requestSubCommentInfo(HotelSpaceTieCommentInfoBean bean) {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("hotelId", String.valueOf(bean.hotelId));
        b.addBody("articleId", String.valueOf(bean.articleId));
        b.addBody("pid", String.valueOf(bean.id));

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.HOTEL_TIE_SUB_COMMENT;
        d.flag = AppConst.HOTEL_TIE_SUB_COMMENT;
        DataLoader.getInstance().loadData(this, d);
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
