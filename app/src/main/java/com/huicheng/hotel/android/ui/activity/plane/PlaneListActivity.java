package com.huicheng.hotel.android.ui.activity.plane;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.plane.PlaneConsiderLayout;
import com.prj.sdk.util.Utils;

import java.util.ArrayList;

/**
 * @auth kborid
 * @date 2017/11/21 0021.
 */

public class PlaneListActivity extends BaseActivity {

    private ListView listview;
    private ArrayList<String> list = new ArrayList<>();
    private PlaneItemAdapter planeItemAdapter;
    private Gallery gallery;
    private PlaneDatePriceAdapter planeDatePriceAdapter;

    private RelativeLayout calendar_lay;
    private TextView tv_consider;

    //筛选
    private PopupWindow mPlaneConsiderPopupWindow = null;
    private PlaneConsiderLayout mPlaneConsiderLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_planelist_layout);
        initViews();
        initParams();
        initListeners();
    }

    @Override
    public void initViews() {
        super.initViews();
        calendar_lay = (RelativeLayout) findViewById(R.id.calendar_lay);
        gallery = (Gallery) findViewById(R.id.gallery);
        listview = (ListView) findViewById(R.id.listview);
        tv_consider = (TextView) findViewById(R.id.tv_consider);
        //筛选
        mPlaneConsiderLayout = new PlaneConsiderLayout(this);
        mPlaneConsiderPopupWindow = new PopupWindow(mPlaneConsiderLayout, ViewGroup.LayoutParams.MATCH_PARENT, Utils.dp2px(470), true);
        mPlaneConsiderPopupWindow.setAnimationStyle(R.style.share_anmi);
        mPlaneConsiderPopupWindow.setFocusable(true);
        mPlaneConsiderPopupWindow.setOutsideTouchable(true);
        mPlaneConsiderPopupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        mPlaneConsiderPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                System.out.println("plane consider popupwindow ondismiss");
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });
        mPlaneConsiderLayout.setOnConsiderLayoutDismissListener(new PlaneConsiderLayout.OnConsiderLayoutDismissListener() {
            @Override
            public void onDismiss(boolean isSave) {
                System.out.println("plane consider layout callback ondismiss");
                mPlaneConsiderPopupWindow.dismiss();
                if (!isSave) {
                    //重置consider
                    mPlaneConsiderLayout.cancelConfig();
                }
            }
        });
    }

    @Override
    public void initParams() {
        super.initParams();
        findViewById(R.id.comm_title_rl).setBackgroundColor(getResources().getColor(R.color.white));
        tv_center_title.setText("杭州 → 北京");
        for (int i = 0; i < 10; i++) {
            list.add(String.valueOf(i));
        }
        planeDatePriceAdapter = new PlaneDatePriceAdapter(this, list);
        gallery.setAdapter(planeDatePriceAdapter);
        gallery.setSelection(3);
        planeItemAdapter = new PlaneItemAdapter(this, list);
        listview.setAdapter(planeItemAdapter);
    }

    @Override
    public void initListeners() {
        super.initListeners();
        calendar_lay.setOnClickListener(this);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(PlaneListActivity.this, PlaneDetailActivity.class);
                startActivity(intent);
            }
        });
        tv_consider.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.calendar_lay:
                break;
            case R.id.tv_consider:
                showConsiderPopupWindow();
                break;
        }
    }

    private void showConsiderPopupWindow() {
        mPlaneConsiderLayout.reloadConfig();
        // 设置背景颜色变暗
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);
        mPlaneConsiderPopupWindow.showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        calendar_lay.setMinimumHeight(gallery.getHeight());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class PlaneItemAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<String> mList = new ArrayList<>();

        PlaneItemAdapter(Context context, ArrayList<String> list) {
            this.context = context;
            this.mList = list;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (null == convertView) {
                convertView = LayoutInflater.from(context).inflate(R.layout.lv_planeticket_item, null);
            }
            return convertView;
        }
    }

    private class PlaneDatePriceAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<String> mList = new ArrayList<>();

        PlaneDatePriceAdapter(Context context, ArrayList<String> list) {
            this.context = context;
            this.mList = list;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (null == convertView) {
                convertView = LayoutInflater.from(context).inflate(R.layout.plane_date_item, null);
                int width = (int) ((float) (Utils.mScreenWidth - Utils.dp2px(40) - Utils.dp2px(6)) / 7);
//                int height = (int) ((float) width / Utils.dp2px(50) * Utils.dp2px(66));
                Gallery.LayoutParams layoutParams = new Gallery.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT);
                convertView.setLayoutParams(layoutParams);
            }
            return convertView;
        }
    }
}
