/*
 * BadgeView.java
 * BadgeView
 * 
 * Copyright (c) 2012 Stefan Jauker.
 * https://github.com/kodex83/BadgeView
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huicheng.hotel.android.ui.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TabWidget;

import com.prj.sdk.util.LogUtil;

/**
 * 在右上角显示红色的数字\标记
 * 
 * @author LiaoBo
 */
public class BadgeView extends android.support.v7.widget.AppCompatTextView {

	private boolean	mHideOnNull	= true;

	public BadgeView(Context context) {
		this(context, null);
	}

	public BadgeView(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.textViewStyle);
	}

	public BadgeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		init();
	}

	private void init() {
		if (!(getLayoutParams() instanceof LayoutParams)) {
			LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT
					| Gravity.TOP);
			setLayoutParams(layoutParams);
		}

		// set default font
		setTextColor(Color.WHITE);
		setTypeface(Typeface.DEFAULT_BOLD);
		setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
		setPadding(dp2px(5), dp2px(1), dp2px(5), dp2px(1));

		// set default background
		setBackground(9, Color.parseColor("#d3321b"));

		setGravity(Gravity.CENTER);

		// default values
		setHideOnNull(true);
		setBadgeCount(0);
	}

	@SuppressLint("NewApi")
	public void setBackground(int dipRadius, int badgeColor) {
		int radius = dp2px(dipRadius);
		float[] radiusArray = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

		RoundRectShape roundRect = new RoundRectShape(radiusArray, null, null);
		ShapeDrawable bgDrawable = new ShapeDrawable(roundRect);
		bgDrawable.getPaint().setColor(badgeColor);
		setBackground(bgDrawable);
	}

	/**
	 * @return Returns true if view is hidden on badge value 0 or null;
	 */
	public boolean isHideOnNull() {
		return mHideOnNull;
	}

	/**
	 * @param hideOnNull
	 *            the hideOnNull to set
	 */
	public void setHideOnNull(boolean hideOnNull) {
		mHideOnNull = hideOnNull;
		setText(getText());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.TextView#setText(java.lang.CharSequence, android.widget.TextView.BufferType)
	 */
	@Override
	public void setText(CharSequence text, BufferType type) {
		if (isHideOnNull() && (text == null || text.toString().equalsIgnoreCase("0"))) {
			setVisibility(View.GONE);
		} else {
			setVisibility(View.VISIBLE);
		}
		super.setText(text, type);
	}

	public void setBadgeCount(int count) {
		setText(String.valueOf(count));
	}

	public Integer getBadgeCount() {
		if (getText() == null) {
			return null;
		}

		String text = getText().toString();
		try {
			return Integer.parseInt(text);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public void setBadgeGravity(int gravity) {
		LayoutParams params = (LayoutParams) getLayoutParams();
		params.gravity = gravity;
		setLayoutParams(params);
	}

	public int getBadgeGravity() {
		LayoutParams params = (LayoutParams) getLayoutParams();
		return params.gravity;
	}

	public void setBadgeMargin(int dipMargin) {
		setBadgeMargin(dipMargin, dipMargin, dipMargin, dipMargin);
	}

	public void setBadgeMargin(int leftDipMargin, int topDipMargin, int rightDipMargin, int bottomDipMargin) {
		LayoutParams params = (LayoutParams) getLayoutParams();
		params.leftMargin = dp2px(leftDipMargin);
		params.topMargin = dp2px(topDipMargin);
		params.rightMargin = dp2px(rightDipMargin);
		params.bottomMargin = dp2px(bottomDipMargin);
		setLayoutParams(params);
	}

	public int[] getBadgeMargin() {
		LayoutParams params = (LayoutParams) getLayoutParams();
		return new int[]{params.leftMargin, params.topMargin, params.rightMargin, params.bottomMargin};
	}

	public void incrementBadgeCount(int increment) {
		Integer count = getBadgeCount();
		if (count == null) {
			setBadgeCount(increment);
		} else {
			setBadgeCount(increment + count);
		}
	}

	public void decrementBadgeCount(int decrement) {
		incrementBadgeCount(-decrement);
	}

	/*
	 * Attach the BadgeView to the TabWidget
	 * 
	 * @param target the TabWidget to attach the BadgeView
	 * 
	 * @param tabIndex index of the tab
	 */
	public void setTargetView(TabWidget target, int tabIndex) {
		View tabView = target.getChildTabViewAt(tabIndex);
		setTargetView(tabView);
	}

	/*
	 * Attach the BadgeView to the target view
	 * 
	 * @param target the view to attach the BadgeView
	 */
	public void setTargetView(View target) {
		if (getParent() != null) {
			((ViewGroup) getParent()).removeView(this);
		}

		if (target == null) {
			return;
		}

		if (target.getParent() instanceof FrameLayout) {
			((FrameLayout) target.getParent()).addView(this);

		} else if (target.getParent() instanceof ViewGroup) {
			// use a new Framelayout container for adding badge
			ViewGroup parentContainer = (ViewGroup) target.getParent();
			int groupIndex = parentContainer.indexOfChild(target);
			parentContainer.removeView(target);

			FrameLayout badgeContainer = new FrameLayout(getContext());
			ViewGroup.LayoutParams parentLayoutParams = target.getLayoutParams();

			badgeContainer.setLayoutParams(parentLayoutParams);
			target.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

			parentContainer.addView(badgeContainer, groupIndex, parentLayoutParams);
			badgeContainer.addView(target);

			badgeContainer.addView(this);
		} else if (target.getParent() == null) {
			LogUtil.e(getClass().getSimpleName(), "ParentView is needed");
		}

	}

	/*
	 * converts dip to px
	 */
	private int dp2px(float dip) {
		return (int) (dip * getContext().getResources().getDisplayMetrics().density + 0.5f);
	}

	/**
	 * 设置晃动动画
	 */
	public void setShakeAnimation() {
		this.setAnimation(shakeAnimation(5));
	}

	/**
	 * 晃动动画
	 * 
	 * @param counts
	 *            1秒钟晃动多少下
	 * @return
	 */
	public static Animation shakeAnimation(int counts) {
		Animation translateAnimation = new TranslateAnimation(0, 10, 0, 0);
		// 设置一个循环加速器，使用传入的次数就会出现摆动的效果。
		translateAnimation.setInterpolator(new CycleInterpolator(counts));
		translateAnimation.setDuration(1000);
		return translateAnimation;
	}
}