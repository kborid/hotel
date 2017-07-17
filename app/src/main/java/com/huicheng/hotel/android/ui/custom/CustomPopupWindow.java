package com.huicheng.hotel.android.ui.custom;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.huicheng.hotel.android.R;

import java.util.ArrayList;

/**
 * @author kborid
 * @date 2016/12/14 0014
 */
public class CustomPopupWindow extends LinearLayout implements View.OnClickListener {
    private Context context;

    private View popupWindowRootView;
    private ListView popupListView;
    private ArrayList<String> popupList = new ArrayList<>();
    private MyPopupWindowAdapter adapter;

    private LinearLayout pop_lay;
    private TextView tv_title;
    private ImageView iv_indicator;

    private boolean isOpenIndicator = false;
    private boolean isPlaying = false;
    private int popWidth = 0;
    private PopupWindow popupWindow;

    public CustomPopupWindow(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public CustomPopupWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public CustomPopupWindow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        LayoutInflater.from(context).inflate(R.layout.custom_popupwindow_layout, this);
        findViews();
        setListeners();
        initPopupWindow();
    }

    private void findViews() {
        pop_lay = (LinearLayout) findViewById(R.id.pop_lay);
        tv_title = (TextView) findViewById(R.id.tv_title);
        iv_indicator = (ImageView) findViewById(R.id.iv_indicator);
    }

    private void setListeners() {
        pop_lay.setOnClickListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        popWidth = pop_lay.getMeasuredWidth();
    }

    private void initPopupWindow() {
        popupWindowRootView = LayoutInflater.from(context).inflate(R.layout.popup_window_layout, null);
        popupListView = (ListView) popupWindowRootView.findViewById(R.id.listview);
        adapter = new MyPopupWindowAdapter(popupList);
        popupListView.setAdapter(adapter);
    }

    public void setContentList(ArrayList<String> list) {
        if (list != null && list.size() > 0) {
            if (popupList != null) {
                if (popupList.size() > 0) {
                    popupList.clear();
                }
                popupList.addAll(list);
            }
            adapter.notifyDataSetChanged();
            tv_title.setText(list.get(0));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pop_lay:
                showPopupWindow();
                break;
            default:
                break;
        }
    }

    private void showPopupWindow() {
        openIndicatorAnimation(iv_indicator);
        if (null == popupWindow) {
            popupWindow = new PopupWindow(popupWindowRootView, popWidth, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        }
        popupWindow.setAnimationStyle(R.style.AnimTools);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0));
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (isOpenIndicator) {
                    closeIndicatorAnimation(iv_indicator);
                }
            }
        });
//        popupWindow.showAtLocation(pop_lay, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        popupWindow.showAsDropDown(pop_lay, 0, 0);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void openIndicatorAnimation(View view) {
        if (isPlaying) {
            return;
        }

        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotation", 0, 180);
        animator.setDuration(500);
        animator.addListener(animatorListener);
        animator.start();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void closeIndicatorAnimation(View view) {
        if (isPlaying) {
            return;
        }
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotation", 180, 360);
        animator.setDuration(500);
        animator.addListener(animatorListener);
        animator.start();
    }

    private Animator.AnimatorListener animatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
            isPlaying = true;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            isOpenIndicator = !isOpenIndicator;
            isPlaying = false;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    };

    class MyPopupWindowAdapter extends BaseAdapter {
        private ArrayList<String> list;

        MyPopupWindowAdapter(ArrayList<String> list) {
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
            ViewHolder viewHolder;
            if (null == convertView) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.popup_window_item, null);
                viewHolder.tv_channel = (TextView) convertView.findViewById(R.id.tv_channel);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.tv_channel.setText(list.get(position));

            return convertView;
        }
    }

    class ViewHolder {
        TextView tv_channel;
    }
}
