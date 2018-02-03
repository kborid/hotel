package com.huicheng.hotel.android.ui.activity.plane;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.ui.adapter.AddressManagerAdapter;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.prj.sdk.util.LogUtil;

import java.util.ArrayList;

/**
 * @auth kborid
 * @date 2017/11/23 00
 */

public class PlaneAddrManagerActivity extends BaseAppActivity {

    private ListView listView;
    private ArrayList<String> list = new ArrayList<>();
    private AddressManagerAdapter addressManagerAdapter;

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_plane_addresslist);
    }

    @Override
    public void initViews() {
        super.initViews();
        listView = (ListView) findViewById(R.id.listView);
    }

    @Override
    public void initParams() {
        super.initParams();
        findViewById(R.id.comm_title_rl).setBackgroundColor(getResources().getColor(R.color.white));
        tv_center_title.setText("管理地址");
        setRightButtonResource("添加", getResources().getColor(R.color.plane_mainColor));
        swipeRefreshLayout.setEnabled(false);
        addressManagerAdapter = new AddressManagerAdapter(this, list);
        listView.setAdapter(addressManagerAdapter);
        listView.setEmptyView(findViewById(R.id.tv_empty));
    }

    @Override
    public void initListeners() {
        super.initListeners();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LogUtil.i(TAG, "listView onitemclick!!!!");
                setResult(RESULT_OK, new Intent().putExtra("isRefresh", true));
                finish();
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.tv_right:
                Intent intent = new Intent(this, PlaneAddrEditActivity.class);
                startActivityForResult(intent, 0x02);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == 0x02) {
            boolean isRefresh = false;
            if (null != data) {
                isRefresh = data.getBooleanExtra("isRefresh", false);
            }
            if (isRefresh) {
                addressManagerAdapter.notifyDataSetChanged();
            }
        }
    }
}
