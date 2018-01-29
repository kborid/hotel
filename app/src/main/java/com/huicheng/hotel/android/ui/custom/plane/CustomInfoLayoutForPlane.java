package com.huicheng.hotel.android.ui.custom.plane;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.alibaba.fastjson.JSON;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.requestbuilder.bean.InPersonalInfoBean;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @auth kborid
 * @date 2017/12/18 0018.
 */

public class CustomInfoLayoutForPlane extends LinearLayout {
    private Context context;

    public CustomInfoLayoutForPlane(Context context) {
        this(context, null);
    }

    public CustomInfoLayoutForPlane(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomInfoLayoutForPlane(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        setOrientation(LinearLayout.VERTICAL);
        initializeFirstLayoutItem();
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
        final View itemView = LayoutInflater.from(context).inflate(R.layout.layout_plane_custominfo_item, null);
        final EditText ed_custom_name = (EditText) itemView.findViewById(R.id.ed_custom_name);
        final EditText et_card_number = (EditText) itemView.findViewById(R.id.et_card_number);
        final Spinner spinner_card_type = (Spinner) itemView.findViewById(R.id.spinner_card_type);
        // 建立Adapter并且绑定数据源
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.layout_plane_custominfo_item_cardtype_item, context.getResources().getStringArray(R.array.cardType));
        adapter.setDropDownViewResource(R.layout.layout_plane_custominfo_item_cardtype_item);
        spinner_card_type.setAdapter(adapter);
        spinner_card_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    et_card_number.setHint("请输入证件号码");
                    itemView.findViewById(R.id.line_birthday_for_hz).setVisibility(GONE);
                    itemView.findViewById(R.id.row_birthday_for_hz).setVisibility(GONE);
                } else {
                    et_card_number.setHint("必须和乘机人一致");
                    itemView.findViewById(R.id.line_birthday_for_hz).setVisibility(VISIBLE);
                    itemView.findViewById(R.id.row_birthday_for_hz).setVisibility(VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
//        final ImageView iv_add = (ImageView) itemView.findViewById(R.id.iv_add);
        final ImageView iv_sub = (ImageView) itemView.findViewById(R.id.iv_sub);
//        iv_add.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                addView(getNewItemView());
//                updateButtonStatus(getChildCount());
//            }
//        });
        iv_sub.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                removeView(itemView);
                updateButtonStatus(getChildCount());
            }
        });

        return itemView;
    }

    public void addNewItem() {
        addView(getNewItemView());
        updateButtonStatus(getChildCount());
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

    private void initializeFirstLayoutItem() {
        removeAllViews();
        addView(getNewItemView());
        updateButtonStatus(getChildCount());
    }

    private void updateButtonStatus(int count) {
        LogUtil.i("CommonCustomInfoLayout", "updateButtonStatus() count = " + count);
        //刷新状态
        if (count == 1) {
            getChildAt(0).findViewById(R.id.iv_sub).setEnabled(false);
        } else if (count > 1) {
            getChildAt(0).findViewById(R.id.iv_sub).setEnabled(true);
        }
    }
}
