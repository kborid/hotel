package com.huicheng.hotel.android.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.net.bean.MessageInfoBean;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.SimpleRefreshListView;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.CustomToast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author kborid
 * @date 2016/12/19 0019
 */
public class MessageListActivity extends BaseActivity implements DataCallback {
    private static final String MESSAGE_TYPE_ALL = "";
    private static final String MESSAGE_TYPE_UNREAD = "01";
    private static final String MESSAGE_TYPE_READED = "03";
    private static final int PAGESIZE = 10;
    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x01:
                    MessageInfoBean bean = (MessageInfoBean) msg.obj;
                    tv_subject.setText(bean.title);
                    tv_sender.setText(bean.sendUserName);
                    tv_content.setText(bean.content);
                    break;
                default:
                    break;
            }
        }
    };

    private TextView tv_subject, tv_sender, tv_content;
    private Spinner spinner_type;
    private String[] keys = new String[]{"全", "已", "未"};

    private List<MessageInfoBean> list = new ArrayList<>();
    private SimpleRefreshListView listview;
    private MyMessageAdapter adapter;
    private String keyword, messageType;
    private int pageIndex = 1;
    private boolean isLoadMore = false;
    private int selectedIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_message_layout);
        initViews();
        initParams();
        initListeners();
//        listview.refreshingHeaderView();
    }

    @Override
    public void initViews() {
        super.initViews();
        tv_subject = (TextView) findViewById(R.id.tv_subject);
        tv_sender = (TextView) findViewById(R.id.tv_sender);
        tv_content = (TextView) findViewById(R.id.tv_content);
        spinner_type = (Spinner) findViewById(R.id.spinner_type);
        listview = (SimpleRefreshListView) findViewById(R.id.listview);
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText("消息");
        tv_content.setMovementMethod(ScrollingMovementMethod.getInstance());

        MyMsgTypeAdapter spinner_type_adapter = new MyMsgTypeAdapter(this, keys);
        spinner_type.setPopupBackgroundDrawable(getResources().getDrawable(R.drawable.comm_rect_main_withwhite_bound));
        spinner_type.setAdapter(spinner_type_adapter);

        adapter = new MyMessageAdapter(this, list);
        listview.setAdapter(adapter);
        listview.setPullLoadEnable(true);
        listview.setPullRefreshEnable(true);
        listview.setHeaderTextViewColor(R.color.transparent40_);
        listview.setFooterTextViewColor(R.color.transparent40_);

        //没有数据时，显示空view提示
        TextView emptyView = new TextView(this);
        emptyView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        emptyView.setText("暂无消息");
        emptyView.setPadding(0, Utils.dip2px(50), 0, Utils.dip2px(50));
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setTextColor(getResources().getColor(R.color.searchHintColor));
        emptyView.setVisibility(View.GONE);
        emptyView.setClickable(false);
        ((ViewGroup) listview.getParent()).addView(emptyView);
        listview.setEmptyView(emptyView);
    }

    @Override
    public void initListeners() {
        super.initListeners();

        spinner_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("spinner type 's OnItemSelectedListener request....");
                switch (position) {
                    case 1:
                        messageType = MESSAGE_TYPE_READED;
                        break;
                    case 2:
                        messageType = MESSAGE_TYPE_UNREAD;
                        break;
                    case 0:
                    default:
                        messageType = MESSAGE_TYPE_ALL;
                        break;
                }
                System.out.println("messageType = " + messageType);
                listview.refreshingHeaderView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedIndex = (int) parent.getAdapter().getItemId(position);
                adapter.setSelectItem(selectedIndex);
                if (MESSAGE_TYPE_UNREAD.equals(list.get(selectedIndex).statusCode)) {
                    requestUpdateMessageStatus(list.get(selectedIndex).id);
                }
            }
        });

        listview.setXListViewListener(new SimpleRefreshListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pageIndex = 1;
                requestAllMessage(keyword, messageType, pageIndex);
            }

            @Override
            public void onLoadMore() {
                isLoadMore = true;
                requestAllMessage(keyword, messageType, ++pageIndex);
            }
        });

        adapter.setOnUpdateListener(new OnUpdateListener() {
            @Override
            public void onUpdateInfo(MessageInfoBean bean) {
                Message msg = new Message();
                msg.what = 0x01;
                msg.obj = bean;
                myHandler.sendMessage(msg);
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            default:
                break;
        }
    }

    private void requestAllMessage(String keyword, String type, int pageIndex) {
        System.out.println("keyword = " + keyword + ", type = " + type + ", pageIndex = " + pageIndex);
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("keywords", keyword);
        b.addBody("type", type);
        b.addBody("pageSize", String.valueOf(PAGESIZE * pageIndex));
        b.addBody("pageIndex", String.valueOf(pageIndex));

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.ALL_MESSAGE;
        d.flag = AppConst.ALL_MESSAGE;

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void requestUpdateMessageStatus(int msgId) {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("msgId", String.valueOf(msgId));

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.MESSAGE_UPDATE;
        d.flag = AppConst.MESSAGE_UPDATE;

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void preExecute(ResponseData request) {

    }

    @Override
    public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.ALL_MESSAGE) {
                System.out.println("json = " + response.body.toString());
                List<MessageInfoBean> temp = JSON.parseArray(response.body.toString(), MessageInfoBean.class);
                if (temp != null) {
                    if (!isLoadMore) {
                        list.clear();
                        listview.setRefreshTime(DateUtil.getSecond(Calendar.getInstance().getTimeInMillis()));
                    } else {
                        isLoadMore = false;
                        if (temp.size() == 0) {
                            pageIndex--;
                            CustomToast.show("没有更多数据", CustomToast.LENGTH_SHORT);
                        }
                    }
                    listview.stopLoadMore();
                    listview.stopRefresh();
                    list.addAll(temp);
                    adapter.notifyDataSetChanged();
                }
            } else if (request.flag == AppConst.MESSAGE_UPDATE) {
                System.out.println("json = " + response.body.toString());
                list.get(selectedIndex).statusCode = MESSAGE_TYPE_READED;
                list.get(selectedIndex).statusName = "已读";
                adapter.notifyDataSetChanged();
            }
        }
    }

    class MyMessageAdapter extends BaseAdapter {

        private Context context;
        private List<MessageInfoBean> list = new ArrayList<>();
        private int selectItem;
        private OnUpdateListener listener = null;

        public MyMessageAdapter(Context context, List<MessageInfoBean> list) {
            this.context = context;
            this.list = list;
            selectItem = 0;
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

        public void setSelectItem(int index) {
            if (index != selectItem) {
                selectItem = index;
            }
            notifyDataSetChanged();
        }

        public void setOnUpdateListener(OnUpdateListener listener) {
            this.listener = listener;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            // findViews
            if (null == convertView) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.lv_message_item, null);
                viewHolder.iv_flag = (ImageView) convertView.findViewById(R.id.iv_flag);
                viewHolder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
                viewHolder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            // 全部
            if (MESSAGE_TYPE_READED.equals(list.get(position).statusCode)) {
                viewHolder.iv_flag.setVisibility(View.INVISIBLE);
            } else {
                viewHolder.iv_flag.setVisibility(View.VISIBLE);
            }
            viewHolder.tv_title.setText(list.get(position).title);
            viewHolder.tv_time.setText(list.get(position).createTime);
            convertView.setVisibility(View.VISIBLE);


            // setSelected
            if (selectItem == 0 && MESSAGE_TYPE_UNREAD.equals(list.get(selectedIndex).statusCode)) {
                requestUpdateMessageStatus(list.get(selectedIndex).id);
            }
            if (selectItem == position) {
                viewHolder.tv_title.setSelected(true);
                viewHolder.tv_time.setSelected(true);
                if (null != listener) {
                    listener.onUpdateInfo(list.get(position));
                }
            } else {
                viewHolder.tv_title.setSelected(false);
                viewHolder.tv_time.setSelected(false);
            }

            return convertView;
        }

        class ViewHolder {
            ImageView iv_flag;
            TextView tv_title;
            TextView tv_time;
        }
    }

    interface OnUpdateListener {
        void onUpdateInfo(MessageInfoBean bean);
    }

    class MyMsgTypeAdapter extends BaseAdapter {
        private Context context;
        private String[] keys = new String[3];

        public MyMsgTypeAdapter(Context context, String[] keys) {
            this.context = context;
            this.keys = keys;
        }

        @Override
        public int getCount() {
            return keys.length;
        }

        @Override
        public Object getItem(int position) {
            return keys[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(context).inflate(R.layout.message_spinner_item, null);
            if (null != convertView) {
                TextView iv_item = (TextView) convertView.findViewById(R.id.iv_item);
                iv_item.setText(keys[position]);
            }
            return convertView;
        }
    }
}
