package com.huicheng.hotel.android.ui.custom;

import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.requestbuilder.bean.InPersonalInfoBean;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kborid
 * @date 2017/3/6 0006
 */
public class CustomInfoLayoutForHotel extends LinearLayout {
    private Context context;

    private LinearLayout custom_info_layout;

    public CustomInfoLayoutForHotel(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public CustomInfoLayoutForHotel(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public CustomInfoLayoutForHotel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        LayoutInflater.from(context).inflate(R.layout.layout_hotel_custominfo, this);
        findViews();
        setClickListeners();
        initializeLayoutItem();
    }

    private void findViews() {
        custom_info_layout = (LinearLayout) findViewById(R.id.custom_info_layout);
    }

    public int setPersonInfos(String json) {
        List<InPersonalInfoBean> temp = JSON.parseArray(json, InPersonalInfoBean.class);
        custom_info_layout.removeAllViews();
        for (int i = 0; i < temp.size(); i++) {
            View customChildView = getNewItemView();
            EditText et_last = (EditText) customChildView.findViewById(R.id.et_last);
            EditText et_first = (EditText) customChildView.findViewById(R.id.et_first);
            EditText et_phone = (EditText) customChildView.findViewById(R.id.et_phone);
            et_last.setText(temp.get(i).lastName);
            et_first.setText(temp.get(i).firstName);
            et_phone.setText(temp.get(i).phone);
            custom_info_layout.addView(customChildView, i);
            updateButtonStatus(custom_info_layout.getChildCount());
        }
        updateButtonStatus(custom_info_layout.getChildCount());
        return temp.size();
    }

    private View getNewItemView() {
        final View view = LayoutInflater.from(context).inflate(R.layout.layout_hotel_custominfo_item, null);
        final TextView tv_person_label = (TextView) view.findViewById(R.id.tv_person_label);
        final ImageView iv_remove = (ImageView) view.findViewById(R.id.iv_remove);
        final ImageView iv_add = (ImageView) view.findViewById(R.id.iv_add);
        tv_person_label.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomDialog dialog = new CustomDialog(context);
                dialog.setMessage(context.getResources().getString(R.string.in_person_msg));
                dialog.setNegativeButton(context.getResources().getString(R.string.iknown), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
            }
        });
        iv_add.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                custom_info_layout.addView(getNewItemView());
                updateButtonStatus(custom_info_layout.getChildCount());
            }
        });
        iv_remove.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                custom_info_layout.removeView(view);
                updateButtonStatus(custom_info_layout.getChildCount());
            }
        });

        return view;
    }

    public boolean isEditViewEmpty() {
        for (int i = 0; i < custom_info_layout.getChildCount(); i++) {
            EditText et_last = (EditText) custom_info_layout.getChildAt(i).findViewById(R.id.et_last);
            EditText et_first = (EditText) custom_info_layout.getChildAt(i).findViewById(R.id.et_first);
            EditText et_phone = (EditText) custom_info_layout.getChildAt(i).findViewById(R.id.et_phone);
            if (StringUtil.isEmpty(et_last.getText().toString()) || StringUtil.isEmpty(et_first.getText().toString()) || StringUtil.isEmpty(et_phone.getText().toString())) {
                return true;
            }
        }
        return false;
    }

    public boolean isValidPhoneNumber() {
        for (int i = 0; i < custom_info_layout.getChildCount(); i++) {
//            EditText et_last = (EditText) custom_info_layout.getChildAt(i).findViewById(R.id.et_last);
//            EditText et_first = (EditText) custom_info_layout.getChildAt(i).findViewById(R.id.et_first);
            EditText et_phone = (EditText) custom_info_layout.getChildAt(i).findViewById(R.id.et_phone);
            if (!Utils.isMobile(et_phone.getText().toString())) {
                return true;
            }
        }
        return false;
    }

    public String getCustomInfoJsonString() {
        List<InPersonalInfoBean> temp = new ArrayList<>();
        for (int i = 0; i < custom_info_layout.getChildCount(); i++) {
            EditText et_last = (EditText) custom_info_layout.getChildAt(i).findViewById(R.id.et_last);
            EditText et_first = (EditText) custom_info_layout.getChildAt(i).findViewById(R.id.et_first);
            EditText et_phone = (EditText) custom_info_layout.getChildAt(i).findViewById(R.id.et_phone);

            InPersonalInfoBean bean = new InPersonalInfoBean();
            bean.lastName = et_last.getText().toString();
            bean.firstName = et_first.getText().toString();
            bean.phone = et_phone.getText().toString();
            temp.add(bean);
        }
        return JSON.toJSON(temp).toString();
    }

    public String getCustomUserNames() {
        String nameStr = "";
        for (int i = 0; i < custom_info_layout.getChildCount(); i++) {
            EditText et_last = (EditText) custom_info_layout.getChildAt(i).findViewById(R.id.et_last);
            EditText et_first = (EditText) custom_info_layout.getChildAt(i).findViewById(R.id.et_first);
//            EditText et_phone = (EditText) custom_info_layout.getChildAt(i).findViewById(R.id.et_phone);

            nameStr += (et_last.getText().toString() + et_first.getText().toString());
            if (i < custom_info_layout.getChildCount() - 1) {
                nameStr += "|";
            }
        }
        return nameStr;
    }

    public String getCustomUserPhones() {
        String phoneStr = "";
        for (int i = 0; i < custom_info_layout.getChildCount(); i++) {
//            EditText et_last = (EditText) custom_info_layout.getChildAt(i).findViewById(R.id.et_last);
//            EditText et_first = (EditText) custom_info_layout.getChildAt(i).findViewById(R.id.et_first);
            EditText et_phone = (EditText) custom_info_layout.getChildAt(i).findViewById(R.id.et_phone);

            phoneStr += et_phone.getText().toString();
            if (i < custom_info_layout.getChildCount() - 1) {
                phoneStr += "|";
            }
        }
        return phoneStr;
    }

    private void initializeLayoutItem() {
        custom_info_layout.removeAllViews();
        custom_info_layout.addView(getNewItemView());
        updateButtonStatus(custom_info_layout.getChildCount());
    }

    private void setClickListeners() {

    }

    private void updateButtonStatus(int count) {
        LogUtil.i("CommonCustomInfoLayout", "updateButtonStatus() count = " + count);
        //刷新状态
        if (count == 1) {
            custom_info_layout.getChildAt(0).findViewById(R.id.iv_remove).setEnabled(false);
            custom_info_layout.getChildAt(0).findViewById(R.id.iv_remove).setAlpha(0.5f);
            custom_info_layout.getChildAt(0).findViewById(R.id.iv_add).setEnabled(true);
            custom_info_layout.getChildAt(0).findViewById(R.id.iv_add).setAlpha(1f);
        } else if (count > 1) {
            custom_info_layout.getChildAt(0).findViewById(R.id.iv_remove).setEnabled(true);
            custom_info_layout.getChildAt(0).findViewById(R.id.iv_remove).setAlpha(1.0f);
            custom_info_layout.getChildAt(0).findViewById(R.id.iv_add).setEnabled(true);
            custom_info_layout.getChildAt(0).findViewById(R.id.iv_add).setAlpha(1f);
        }
    }
}
