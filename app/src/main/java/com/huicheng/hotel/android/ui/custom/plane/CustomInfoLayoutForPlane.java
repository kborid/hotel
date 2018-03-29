package com.huicheng.hotel.android.ui.custom.plane;

import android.app.DatePickerDialog;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.PlaneCommDef;
import com.huicheng.hotel.android.requestbuilder.bean.PlanePassengerInfoBean;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.StringUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @auth kborid
 * @date 2017/12/18 0018.
 */

public class CustomInfoLayoutForPlane extends LinearLayout {
    private static final int SEX_FEMALE = 0;
    private static final int SEX_MALE = 1;
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

    public int setPersonInfo(String json) {
        final List<PlanePassengerInfoBean> temp = JSON.parseArray(json, PlanePassengerInfoBean.class);
        removeAllViews();
        for (int i = 0; i < temp.size(); i++) {
            final View itemView = getNewItemView();
            final EditText ed_custom_name = (EditText) itemView.findViewById(R.id.ed_custom_name);
            final Spinner spinner_cardType = (Spinner) itemView.findViewById(R.id.spinner_cardType);
            final EditText et_card_number = (EditText) itemView.findViewById(R.id.et_card_number);
            final TextView tv_birthday = (TextView) itemView.findViewById(R.id.tv_birthday);
            final Spinner spinner_sex = (Spinner) itemView.findViewById(R.id.spinner_sex);
            ed_custom_name.setText(temp.get(i).name);
            final int finalI = i;
            spinner_cardType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position != 0) {
                        itemView.findViewById(R.id.line_birthday).setVisibility(VISIBLE);
                        itemView.findViewById(R.id.row_birthday).setVisibility(VISIBLE);
                        tv_birthday.setText(temp.get(finalI).birthday);
                        itemView.findViewById(R.id.line_sex).setVisibility(VISIBLE);
                        itemView.findViewById(R.id.row_sex).setVisibility(VISIBLE);
                    } else {
                        itemView.findViewById(R.id.line_birthday).setVisibility(GONE);
                        itemView.findViewById(R.id.row_birthday).setVisibility(GONE);
                        tv_birthday.setText("");
                        itemView.findViewById(R.id.line_sex).setVisibility(GONE);
                        itemView.findViewById(R.id.row_sex).setVisibility(GONE);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            spinner_cardType.setSelection(PlaneCommDef.CardType.valueOf(temp.get(i).cardType).ordinal());
            et_card_number.setText(temp.get(i).cardNo);
            spinner_sex.setSelection(convertValue2SexSpinnerSelection(temp.get(i).sex));

            addView(itemView, i);
            updateButtonStatus(getChildCount());
        }
        updateButtonStatus(getChildCount());
        if (null != countListener) {
            countListener.onCountChanged(getChildCount());
        }
        return temp.size();
    }

    private int convertSexSpinnerSelection2Value(int position) {
        LogUtil.i("convertSexSpinnerSelection2Value() position = " + position);
        return position == 0 ? SEX_MALE : SEX_FEMALE;
    }

    private int convertValue2SexSpinnerSelection(int sex) {
        LogUtil.i("convertValue2SexSpinnerSelection() sex = " + sex);
        return sex == SEX_MALE ? 0 : 1;
    }

    private View getNewItemView() {
        final View itemView = LayoutInflater.from(context).inflate(R.layout.layout_plane_custominfo_item, null);
        final Spinner spinner_cardType = (Spinner) itemView.findViewById(R.id.spinner_cardType);
        ArrayAdapter<String> adapterCardType = new ArrayAdapter<>(context, R.layout.layout_plane_custominfo_item_cardtype_item, context.getResources().getStringArray(R.array.cardType));
        adapterCardType.setDropDownViewResource(R.layout.layout_plane_custominfo_item_cardtype_item);
        spinner_cardType.setAdapter(adapterCardType);
        spinner_cardType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    ((EditText) itemView.findViewById(R.id.et_card_number)).setHint("请输入证件号码");
                    itemView.findViewById(R.id.line_birthday).setVisibility(GONE);
                    itemView.findViewById(R.id.row_birthday).setVisibility(GONE);
                    itemView.findViewById(R.id.line_sex).setVisibility(GONE);
                    itemView.findViewById(R.id.row_sex).setVisibility(GONE);
                } else {
                    ((EditText) itemView.findViewById(R.id.et_card_number)).setHint("必须和乘机人一致");
                    itemView.findViewById(R.id.line_birthday).setVisibility(VISIBLE);
                    itemView.findViewById(R.id.row_birthday).setVisibility(VISIBLE);
                    itemView.findViewById(R.id.line_sex).setVisibility(VISIBLE);
                    itemView.findViewById(R.id.row_sex).setVisibility(VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        final TextView tv_birthday = (TextView) itemView.findViewById(R.id.tv_birthday);
        tv_birthday.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int year, month, day;
                Calendar calendar = Calendar.getInstance();
                month = calendar.get(Calendar.MONTH) + 1;
                day = calendar.get(Calendar.DATE);
                calendar.set(Calendar.YEAR, 1990);
                year = calendar.get(Calendar.YEAR);
                LogUtil.i("current year = " + year + ", month = " + month + ", day = " + day);
                String birthday = tv_birthday.getText().toString();
                if (StringUtil.notEmpty(birthday)) {
                    LogUtil.i("not empty birthday = " + birthday);
                    calendar = DateUtil.str2Calendar(birthday, "yyyy-MM-dd");
                    if (null != calendar) {
                        year = calendar.get(Calendar.YEAR);
                        month = calendar.get(Calendar.MONTH) + 1;
                        day = calendar.get(Calendar.DATE);
                    }
                    LogUtil.i("not empty year = " + year + ", month = " + month + ", day = " + day);
                }

                new DatePickerDialog(context, R.style.MyMaterialDialog,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                LogUtil.i("year = " + year + ", month = " + month + ", day = " + dayOfMonth);
                                tv_birthday.setText(String.format(context.getString(R.string.birthdayStr), year, month + 1, dayOfMonth));
                            }
                        }, year, month - 1, day).show();
            }
        });
        final Spinner spinner_sex = (Spinner) itemView.findViewById(R.id.spinner_sex);
        ArrayAdapter<String> adapterSex = new ArrayAdapter<>(context, R.layout.layout_plane_custominfo_item_cardtype_item, context.getResources().getStringArray(R.array.sex));
        adapterSex.setDropDownViewResource(R.layout.layout_plane_custominfo_item_cardtype_item);
        spinner_sex.setAdapter(adapterSex);
        final ImageView iv_sub = (ImageView) itemView.findViewById(R.id.iv_sub);
        iv_sub.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                removeView(itemView);
                updateButtonStatus(getChildCount());
                if (null != countListener) {
                    countListener.onCountChanged(getChildCount());
                }
            }
        });

        return itemView;
    }

    public void addNewItem() {
        addView(getNewItemView());
        updateButtonStatus(getChildCount());
        if (null != countListener) {
            countListener.onCountChanged(getChildCount());
        }
    }

    public String getCustomInfoJsonString() {
        List<PlanePassengerInfoBean> temp = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            final EditText ed_custom_name = (EditText) getChildAt(i).findViewById(R.id.ed_custom_name);
            final Spinner spinner_cardType = (Spinner) getChildAt(i).findViewById(R.id.spinner_cardType);
            final EditText et_card_number = (EditText) getChildAt(i).findViewById(R.id.et_card_number);
            final TextView tv_birthday = (TextView) getChildAt(i).findViewById(R.id.tv_birthday);
            final Spinner spinner_sex = (Spinner) getChildAt(i).findViewById(R.id.spinner_sex);

            PlanePassengerInfoBean bean = new PlanePassengerInfoBean(
                    ed_custom_name.getText().toString(),
                    PlaneCommDef.CardType.values()[spinner_cardType.getSelectedItemPosition()].getValueId(),
                    et_card_number.getText().toString(),
                    tv_birthday.getText().toString(),
                    convertSexSpinnerSelection2Value(spinner_sex.getSelectedItemPosition())
            );
            temp.add(bean);
        }
        return JSON.toJSON(temp).toString();
    }

    private void initializeFirstLayoutItem() {
        removeAllViews();
        addNewItem();
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

    private ICustomInfoLayoutPlaneCountListener countListener = null;

    public void setICustomInfoLayoutPlaneCountListener(ICustomInfoLayoutPlaneCountListener countListener) {
        this.countListener = countListener;
    }
}
