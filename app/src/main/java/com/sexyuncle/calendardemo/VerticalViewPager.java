package com.sexyuncle.calendardemo;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

/**
 * Created by SexyUncle on 16/5/18.
 * Copyright (c) 2016 Kinit.tec.Inc. All rights reserved.
 * Link: CalendarDemo com.sexyuncle.calendardemo
 * Description:
 */
public class VerticalViewPager extends LinearLayout {


    private static final String LOG_TAG = VerticalViewPager.class.getSimpleName();
    private View[] recentViews = new View[3];
    private BaseAdapter mAdapter;
    private int position = 0;
    private int width, height;

    public VerticalViewPager(Context context) {
        this(context, null);
    }

    public VerticalViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    /**
     * @description 初始化最近的view
     */
    void init() {
        setLongClickable(true);
        setOrientation(VERTICAL);
        for (int i = 0; i < recentViews.length; i++) {
            recentViews[i] = new View(getContext());
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (position == 0) {
            width = recentViews[1].getMeasuredWidth();
            height = recentViews[1].getMeasuredHeight();
        }
        for (int i = -1; i < 2; i++) {
            switch (i) {
                case -1:
                    recentViews[0].layout(getPaddingLeft(), getPaddingTop() - height, getPaddingLeft() + width, getPaddingTop());
                    break;
                case 0:
                    recentViews[1].layout(getPaddingLeft(), getPaddingTop(), getPaddingLeft() + width, getPaddingTop() + height);
                    break;
                case 1:
                    recentViews[2].layout(getPaddingLeft(), getPaddingTop() + height, getPaddingLeft() + width, getPaddingTop() + 2 * height);
                    break;
            }

        }
        measure(MeasureSpec.makeMeasureSpec(recentViews[1].getMeasuredWidth(), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(recentViews[1].getMeasuredHeight(), MeasureSpec.EXACTLY));
    }

    public BaseAdapter getAdapter() {
        return mAdapter;
    }

    public void setAdapter(BaseAdapter mAdapter) {
        this.mAdapter = mAdapter;
        initView();
    }

    private void initView() {
        if (position == 0) {
            removeAllViews();
            recentViews[1] = mAdapter.getView(position, recentViews[1], this);
            recentViews[2] = mAdapter.getView(position + 1, recentViews[2], this);
            this.addView(recentViews[1]);
            this.addView(recentViews[2]);
        } else {
            if (recentViews[0] != null)
                removeView(recentViews[0]);
            recentViews[0] = recentViews[1];
            recentViews[1] = recentViews[2];
            recentViews[2] = mAdapter.getView(position + 1, recentViews[2], this);
            this.addView(recentViews[2]);
        }
        recentViews[0].setBackgroundColor(Color.BLACK);
        recentViews[1].setBackgroundColor(Color.BLUE);
        recentViews[2].setBackgroundColor(Color.RED);
        requestLayout();
        invalidate();
    }

    float oldY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                oldY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                int location0[] = new int[2];
                Rect rect0 = new Rect();
                recentViews[0].getLocalVisibleRect(rect0);
                Rect rect = new Rect();
                getLocalVisibleRect(rect);
                Rect rect2 = new Rect();
                recentViews[2].getLocalVisibleRect(rect2);
                if (rect0.bottom <= height && rect2.bottom <= height) {
                    scrollBy(0, (int) (oldY - event.getY()) / 10);
                    if (rect0.bottom - rect0.top >= height) {
                        position--;
                        initView();
                    }
                    if (rect2.bottom - rect2.top >= height) {
                        position++;
                        initView();
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }
}
