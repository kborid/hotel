package com.huicheng.hotel.android.ui.custom;


import android.content.Context;
import android.util.AttributeSet;

import com.huicheng.hotel.android.R;


public class SingleChoiceLayout extends CheckedRelativeLayout {

	public SingleChoiceLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		setImageResouce(R.drawable.iv_plane_order_unchecked, R.drawable.iv_plane_order_checked);
	}

}
