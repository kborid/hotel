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

    public CustomInfoLayoutForHotel(Context context) {
        this(context, null);
    }

    public CustomInfoLayoutForHotel(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomInfoLayoutForHotel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        setClickListeners();
        initializeLayoutItem();
    }

    public int setPersonInfos(String json) {
        List<InPersonalInfoBean> temp = JSON.parseArray(json, InPersonalInfoBean.class);
        removeAllViews();
        for (int i = 0; i < temp.size(); i++) {
            View customChildView = getNewItemView();
            EditText et_last = (EditText) customChildView.findViewById(R.id.et_last);
            EditText et_first = (EditText) customChildView.findViewById(R.id.et_first);
            EditText et_phone = (EditText) customChildView.findViewById(R.id.et_phone);
            et_last.setText(temp.get(i).lastName);
            et_first.setText(temp.get(i).firstName);
            et_phone.setText(temp.get(i).phone);
            addView(customChildView, i);
            updateButtonStatus(getChildCount());
        }
        updateButtonStatus(getChildCount());
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
                addView(getNewItemView());
                updateButtonStatus(getChildCount());
            }
        });
        iv_remove.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                removeView(view);
                updateButtonStatus(getChildCount());
            }
        });

        return view;
    }

    public boolean isEditViewEmpty() {
        for (int i = 0; i < getChildCount(); i++) {
            EditText et_last = (EditText) getChildAt(i).findViewById(R.id.et_last);
            EditText et_first = (EditText) getChildAt(i).findViewById(R.id.et_first);
            EditText et_phone = (EditText) getChildAt(i).findViewById(R.id.et_phone);
            if (StringUtil.isEmpty(et_last.getText().toString()) || StringUtil.isEmpty(et_first.getText().toString()) || StringUtil.isEmpty(et_phone.getText().toString())) {
                return true;
            }
        }
        return false;
    }

    public boolean isValidPhoneNumber() {
        for (int i = 0; i < getChildCount(); i++) {
//            EditText et_last = (EditText) getChildAt(i).findViewById(R.id.et_last);
//            EditText et_first = (EditText) getChildAt(i).findViewById(R.id.et_first);
            EditText et_phone = (EditText) getChildAt(i).findViewById(R.id.et_phone);
            if (!Utils.isMobile(et_phone.getText().toString())) {
                return true;
            }
        }
        return false;
    }

    public String getCustomInfoJsonString() {
        List<InPersonalInfoBean> temp = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            EditText et_last = (EditText) getChildAt(i).findViewById(R.id.et_last);
            EditText et_first = (EditText) getChildAt(i).findViewById(R.id.et_first);
            EditText et_phone = (EditText) getChildAt(i).findViewById(R.id.et_phone);

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
        for (int i = 0; i < getChildCount(); i++) {
            EditText et_last = (EditText) getChildAt(i).findViewById(R.id.et_last);
            EditText et_first = (EditText) getChildAt(i).findViewById(R.id.et_first);
//            EditText et_phone = (EditText) getChildAt(i).findViewById(R.id.et_phone);

            nameStr += (et_last.getText().toString() + et_first.getText().toString());
            if (i < getChildCount() - 1) {
                nameStr += "|";
            }
        }
        return nameStr;
    }

    public String getCustomUserPhones() {
        String phoneStr = "";
        for (int i = 0; i < getChildCount(); i++) {
//            EditText et_last = (EditText) getChildAt(i).findViewById(R.id.et_last);
//            EditText et_first = (EditText) getChildAt(i).findViewById(R.id.et_first);
            EditText et_phone = (EditText) getChildAt(i).findViewById(R.id.et_phone);

            phoneStr += et_phone.getText().toString();
            if (i < getChildCount() - 1) {
                phoneStr += "|";
            }
        }
        return phoneStr;
    }

    private void initializeLayoutItem() {
        removeAllViews();
        addView(getNewItemView());
        updateButtonStatus(getChildCount());
    }

    private void setClickListeners() {

    }

    private void updateButtonStatus(int count) {
        LogUtil.i("CommonCustomInfoLayout", "updateButtonStatus() count = " + count);
        //刷新状态
        if (count == 1) {
            getChildAt(0).findViewById(R.id.iv_remove).setEnabled(false);
            getChildAt(0).findViewById(R.id.iv_remove).setAlpha(0.5f);
            getChildAt(0).findViewById(R.id.iv_add).setEnabled(true);
            getChildAt(0).findViewById(R.id.iv_add).setAlpha(1f);
        } else if (count > 1) {
            getChildAt(0).findViewById(R.id.iv_remove).setEnabled(true);
            getChildAt(0).findViewById(R.id.iv_remove).setAlpha(1.0f);
            getChildAt(0).findViewById(R.id.iv_add).setEnabled(true);
            getChildAt(0).findViewById(R.id.iv_add).setAlpha(1f);
        }
    }
}
