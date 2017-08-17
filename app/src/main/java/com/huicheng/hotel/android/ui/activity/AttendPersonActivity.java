package com.huicheng.hotel.android.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.ui.adapter.AttendPersonAdapter;
import com.huicheng.hotel.android.ui.adapter.PersonInfo;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class AttendPersonActivity extends BaseActivity {

    private final static int PAGESIZE = 10;
    /**
     * 保存选中的人对应的id的字符串 id以空格分隔
     */
    private String mIniSelectedCids;

    public static final String KEY_SELECTED = "selected";
    private ListView mListView;
    private AttendPersonAdapter mAdapter;
    private List<PersonInfo> list = new ArrayList<PersonInfo>();
    private int pageIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_attendperson_layout);
        initViews();
        initParams();
        initListeners();
        requestAttendPersonList(pageIndex);
    }

    @Override
    public void initViews() {
        super.initViews();
        mListView = (ListView) findViewById(R.id.lv);
        mAdapter = new AttendPersonAdapter(this, list);
        mListView.setAdapter(mAdapter);

        //没有数据时，显示空view提示
        TextView emptyView = new TextView(this);
        emptyView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        emptyView.setText("没有更多关注的好友");
        emptyView.setPadding(0, Utils.dip2px(30), 0, Utils.dip2px(30));
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setTextColor(getResources().getColor(R.color.searchHintColor));
        emptyView.setVisibility(View.GONE);
        ((ViewGroup) mListView.getParent()).addView(emptyView);
        mListView.setEmptyView(emptyView);
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        mIniSelectedCids = getIntent().getStringExtra(KEY_SELECTED);
    }

    @Override
    public void initParams() {
        super.initParams();
    }

    private void requestAttendPersonList(int pageIndex) {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("pageIndex", String.valueOf(pageIndex));
        b.addBody("pageSize", String.valueOf(PAGESIZE));

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.ATTEND_LIST;
        d.flag = AppConst.ATTEND_LIST;

        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    @Override
    public void initListeners() {
        super.initListeners();
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                PersonInfo personInfo = (PersonInfo) parent.getItemAtPosition(position);
                Intent intent = new Intent();
//                intent.putExtra(KEY_CID, personInfo.id + " ");
//                intent.putExtra(KEY_NAME, "@" + personInfo.attentusername + " ");
                intent.putExtra("person", personInfo);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private void removeSelectedCids() {
        PersonInfo entity = null;
        List<PersonInfo> tmp = new ArrayList<PersonInfo>();
        for (int i = 0; i < list.size(); i++) {
            entity = list.get(i);
            if (mIniSelectedCids != null) {
                if (mIniSelectedCids.contains(String.valueOf(entity.id))) {
                    tmp.add(entity);
                }
            }
        }
        list.removeAll(tmp);
    }

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.ATTEND_LIST) {
                removeProgressDialog();
                List<PersonInfo> temp = JSON.parseArray(response.body.toString(), PersonInfo.class);
                if (temp.size() > 0) {
                    list.clear();
                    list.addAll(temp);
                    removeSelectedCids();
                }
                mAdapter.notifyDataSetChanged();
            }
        }
    }
}
