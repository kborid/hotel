package com.huicheng.hotel.android.net.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kborid
 * @date 2017/3/29 0029
 */
public class HotelSpaceTieCommentInfoBean implements Serializable {
    public int articleId;
    public List<AtLists> atList;
    public String beRepliedUserId;
    public String beRepliedUserName;
    public int beRepliedUserType;
    public String content;
    public long createTime;
    public int hotelId;
    public int id;
    public String isLatestReply;
    public int pId;
    public String picUrl;
    public int replyCnt;
    public int praiseCnt;
    public List<HotelSpaceTieCommentInfoBean> replyList = new ArrayList<>();
    public ReplyHolder replyHolder;
    public String replyUserHeadUrl;
    public String replyUserId;
    public String replyUserName;
    public int replyUserType;
    public int status;


    public static class AtLists implements Serializable{
        public int articleId;
        public String atUserId;
        public String atUserName;
        public long createTime;
        public int id;
        public String receiveUserId;
        public String receiveUserName;
        public int replyId;
        public int status;
        public long updateTime;
    }

    public static class ReplyHolder implements Serializable{
        public int replycnt;
        public String replyuserid;
        public String replyusername;
    }
}
