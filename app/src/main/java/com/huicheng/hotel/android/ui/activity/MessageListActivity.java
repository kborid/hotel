package com.huicheng.hotel.android.ui.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.CustomToast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author kborid
 * @date 2016/12/19 0019
 */
public class MessageListActivity extends BaseActivity {
    private static final String TAG = "MessageListActivity";
    private static final String MESSAGE_TYPE_ALL = "";
    private static final String MESSAGE_TYPE_UNREAD = "01";
    private static final String MESSAGE_TYPE_READED = "03";
    private static final int PAGESIZE = 10;
    private Handler myHandler = new MyHandler(this);
    private TextView tv_subject, tv_content;
    private Spinner spinner_type;
    private String[] keys = new String[]{"全", "已", "未"};

    private LinearLayout detail_lay;
    private List<MessageInfoBean> list = new ArrayList<>();
    private SimpleRefreshListView listview;
    private MyMessageAdapter adapter;
    private EditText et_keyword;
    private ImageView iv_search;
    private String keyword, messageType;
    private int pageIndex = 1;
    private boolean isLoadMore = false;
    private int selectedIndex = 0;

    private int spinnerTypeBgResId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_message_layout);
        TypedArray ta = obtainStyledAttributes(R.styleable.MyTheme);
        spinnerTypeBgResId = ta.getResourceId(R.styleable.MyTheme_msgSpinnerBg, R.drawable.msg_spinner_mainwhite_withbound_bg);
        ta.recycle();

        initViews();
        initParams();
        initListeners();
//        listview.refreshingHeaderView();
    }

    @Override
    public void initViews() {
        super.initViews();
        detail_lay = (LinearLayout) findViewById(R.id.detail_lay);
        tv_subject = (TextView) findViewById(R.id.tv_subject);
        tv_content = (TextView) findViewById(R.id.tv_content);
        spinner_type = (Spinner) findViewById(R.id.spinner_type);
        listview = (SimpleRefreshListView) findViewById(R.id.listview);
        et_keyword = (EditText) findViewById(R.id.et_keyword);
        iv_search = (ImageView) findViewById(R.id.iv_search);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText("消息");
        tv_content.setMovementMethod(ScrollingMovementMethod.getInstance());

        MyMsgTypeAdapter spinner_type_adapter = new MyMsgTypeAdapter(this, keys);
        spinner_type.setPopupBackgroundResource(spinnerTypeBgResId);
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

        detail_lay.setOnClickListener(this);
        spinner_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LogUtil.i(TAG, "spinner type 's OnItemSelectedListener request....");
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
                LogUtil.i(TAG, "messageType = " + messageType);
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

        et_keyword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    iv_search.performClick();
                    return true;
                }
                return false;
            }
        });
        iv_search.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.detail_lay:
                if (list != null && list.size() > 0) {
                    if ("22".equals(list.get(selectedIndex).typeCode) || "23".equals(list.get(selectedIndex).typeCode)) {
                        String mark = list.get(selectedIndex).mark;
                        if (StringUtil.notEmpty(mark)) {
                            try {
                                String[] marks = mark.split("\\|");
                                Intent intent = new Intent(this, HotelSpaceDetailActivity.class);
                                intent.putExtra("hotelId", Integer.valueOf(marks[0]));
                                intent.putExtra("articleId", Integer.valueOf(marks[1]));
                                startActivity(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                break;
            case R.id.iv_search:
                if (!et_keyword.getText().toString().equals(keyword)) {
                    keyword = et_keyword.getText().toString();
                    listview.refreshingHeaderView();
                }
                break;
            default:
                break;
        }
    }

    private void requestAllMessage(String keyword, String type, int pageIndex) {
        LogUtil.i(TAG, "keyword = " + keyword + ", type = " + type + ", pageIndex = " + pageIndex);
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
        myHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.ALL_MESSAGE) {
                LogUtil.i(TAG, "json = " + response.body.toString());
                List<MessageInfoBean> temp = JSON.parseArray(response.body.toString(), MessageInfoBean.class);
                if (temp != null) {
                    if (!isLoadMore) {
                        list.clear();
                        selectedIndex = 0;
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
                    if (temp.size() < PAGESIZE) {
                        listview.setPullLoadEnable(false);
                    } else {
                        listview.setPullLoadEnable(true);
                    }
                    list.addAll(temp);
                    adapter.setSelectItem(selectedIndex);
//                    adapter.notifyDataSetChanged();
                }
            } else if (request.flag == AppConst.MESSAGE_UPDATE) {
                LogUtil.i(TAG, "json = " + response.body.toString());
                list.get(selectedIndex).statusCode = MESSAGE_TYPE_READED;
                list.get(selectedIndex).statusName = "已读";
                adapter.notifyDataSetChanged();
            }
        }
    }

    private class MyMessageAdapter extends BaseAdapter {

        private Context context;
        private List<MessageInfoBean> list = new ArrayList<>();
        private int selectItem;
        private OnUpdateListener listener = null;

        MyMessageAdapter(Context context, List<MessageInfoBean> list) {
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

    private class MyMsgTypeAdapter extends BaseAdapter {
        private Context context;
        private String[] keys = new String[3];

        MyMsgTypeAdapter(Context context, String[] keys) {
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

    private static class MyHandler extends Handler {
        WeakReference<MessageListActivity> mActivity;

        MyHandler(MessageListActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final Activity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case 0x01:
                        MessageInfoBean bean = (MessageInfoBean) msg.obj;
                        mActivity.get().tv_subject.setText(bean.title);
                        mActivity.get().tv_content.setText(bean.content);
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
