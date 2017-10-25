package com.huicheng.hotel.android.ui.custom;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @auth kborid
 * @date 2017/10/24 0024.
 */

public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
    private int space;

    public SpacesItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {
        outRect.bottom = space / 2;

        // Add top margin only for the first item to avoid double space between items
        int position = parent.getChildLayoutPosition(view);
        if (position == 0 || position == 1) {
            outRect.top = space;
        } else {
            outRect.top = space / 2;
        }

        if (position % 2 == 0) {
            outRect.left = 0;
            outRect.right = space / 2;
        } else {
            outRect.left = space / 2;
            outRect.right = 0;
        }
    }
}
