package com.huicheng.hotel.android.ui.custom;


import android.content.Context;
import android.util.AttributeSet;

import com.huicheng.hotel.android.R;


public class MultiChoiceLayout extends CheckedRelativeLayout {

	public MultiChoiceLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		setImageResouce(R.drawable.check, R.drawable.iv_checked);
	}
}
