package com.sexyuncle.calendardemo;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.KeyboardView;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by SexyUncle on 16/5/17.
 * Copyright (c) 2016 Kinit.tec.Inc. All rights reserved.
 * Link: CalendarDemo com.sexyuncle.calendardemo
 * Description:
 */
public class MonthCalenderView extends View {

    private static final String LOG_TAG = MonthCalenderView.class.getSimpleName();

    private static final boolean isDebug = false;
    private static final int ROWS = 7;//日历的行数
    private static final int COLUMNS = 7;//日历的列数
    private int mDividerColor;
    private float mDividerWidth = 1 * TypedValue.COMPLEX_UNIT_DIP;
    private PointF mCurrentPoint = new PointF();
    private float width;
    private float height;
    private float datumSize;
    private static String dayNames[] = new String[]{"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
    private static final int PREVIOUS_MONTH = -1;
    private static final int CURRENT_MONTH = 0;
    private static final int NEXT_MONTH = 1;
    private SparseArray<DateInfo> mMonthDays = new SparseArray<>();

    public MonthCalenderView(Context context) {
        this(context, null);
    }

    public MonthCalenderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MonthCalenderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(attrs);
    }


    void initAttr(AttributeSet attrs) {
        setWillNotDraw(false);
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.MonthCalenderView);
            mDividerColor = typedArray.getColor(R.styleable.MonthCalenderView_dividerColor, 0);
            mDividerWidth = typedArray.getDimension(R.styleable.MonthCalenderView_dividerWidth, mDividerWidth);
            typedArray.recycle();
        }
        if (mDividerColor == 0)
            mDividerColor = getResources().getColor(R.color.divider);
        init();
    }


    private class DateInfo {
        public String data;
        public int flag;

        public DateInfo(String data, int flag) {
            this.data = data;
            this.flag = flag;
        }
    }

    /**
     * @description 初始化
     */
    public void init() {
        String[] previousMonthDays = getPreviousMonthDays(getDayOfweek(getFirstDateOfMonth()));
        int dayLength = getDaysOfMonth();
        for (int index = 0; index < (ROWS - 1) * COLUMNS; index++) {
            if (index < previousMonthDays.length) {
                mMonthDays.put(index, new DateInfo(previousMonthDays[index], PREVIOUS_MONTH));
            } else if (index >= previousMonthDays.length && index <= previousMonthDays.length + dayLength - 1) {
                mMonthDays.put(index, new DateInfo(String.valueOf(index - previousMonthDays.length + 1), CURRENT_MONTH));
            } else {
                mMonthDays.put(index, new DateInfo(String.valueOf(index - previousMonthDays.length - dayLength + 1), NEXT_MONTH));
            }
        }
    }




    /**
     * @return
     * @description 获取当月第一天
     */
    private Date getFirstDateOfMonth() {
        Calendar calender = getCalender();
        calender.set(Calendar.DAY_OF_MONTH, 1);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return calender.getTime();
    }

    /**
     * @return
     * @description 获取当前月天数
     */
    private int getDaysOfMonth() {
        Calendar calender = getCalender();
        calender.set(Calendar.DAY_OF_MONTH, calender.getActualMaximum(Calendar.DAY_OF_MONTH));
        return calender.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * @param dayOfweek
     * @return
     * @description获取日历上上个月的天数
     */
    private String[] getPreviousMonthDays(int dayOfweek) {
        String[] days = new String[dayOfweek - 1];
        for (int i = 1; i < dayOfweek; i++) {
            Calendar calender = getCalender();
            calender.set(Calendar.DAY_OF_MONTH, i - (dayOfweek - 1));
            days[i - 1] = String.valueOf(calender.get(Calendar.DAY_OF_MONTH));
        }
        return days;
    }

    /**
     * @param date
     * @return
     * @description 根据某一时间获取当天是星期几
     */
    private int getDayOfweek(Date date) {
        Calendar calender = getCalender();
        calender.setTime(date);
        int dayOfWeek = calender.get(Calendar.DAY_OF_WEEK);
        dayOfWeek = dayOfWeek == 1 ? 7 : dayOfWeek - 1;
        return dayOfWeek;
    }

    /**
     * @return
     * @description 获取calender实例
     */
    private Calendar getCalender() {
        Calendar calender = Calendar.getInstance(Locale.CHINA);
        calender.setFirstDayOfWeek(Calendar.MONDAY);
        return calender;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.translate(getPaddingLeft(), getPaddingTop());
        canvas.drawColor(Color.parseColor("#E8E8E8"));
        width = getWidth() - mDividerWidth * 2;
        height = getHeight() - mDividerWidth * 2;
        datumSize = width / ((float) COLUMNS);
        drawHeader(canvas);
        drawFrame(canvas);
        drawDay(canvas);
        if (isDebug)
            drawTouchPointer(canvas);
    }

    /**
     * @param canvas
     * @description
     */
    void drawHeader(Canvas canvas) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.parseColor("#535353"));
        paint.setTextSize(20 * TypedValue.COMPLEX_UNIT_SP);
        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextAlign(Paint.Align.CENTER);
        for (int column = 0; column < COLUMNS; column++) {
            canvas.translate(datumSize * column, 0);
            canvas.drawText(dayNames[column],
                    datumSize / 2,
                    datumSize / 2
                            + (paint.getTextSize() - paint.descent()) / 2,
                    paint);
            canvas.translate(-datumSize * column, 0);
        }
    }

    /**
     * @param canvas
     * @description 画边框
     */
    void drawFrame(Canvas canvas) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(mDividerWidth);
        paint.setColor(mDividerColor);
        for (int i = 0; i <= COLUMNS; i++) {
            float lineX = i * datumSize;
            canvas.drawLine(lineX, datumSize, lineX, height, paint);
        }
        for (int i = 1; i <= ROWS; i++) {
            float lineY = i * datumSize;
            canvas.drawLine(0, lineY, width, lineY, paint);
        }
    }

    /**
     * @param canvas
     * @description 画没每天
     */
    void drawDay(Canvas canvas) {
        Drawable unFocusedBg = getResources().getDrawable(R.drawable.calendar_month_view_day_unfocused);
        Drawable focusedBg = getResources().getDrawable(R.drawable.calendar_month_view_day_focused);
        Drawable moveFocused = getResources().getDrawable(R.drawable.calendar_month_view_move_focused);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.parseColor("#535353"));
        paint.setTextSize(20 * TypedValue.COMPLEX_UNIT_SP);
        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextAlign(Paint.Align.CENTER);
        for (int row = 1; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                int index = (row - 1) * COLUMNS + column;
                DateInfo info = mMonthDays.get(index);
                int dayWidth = (int) Math.ceil(datumSize);
                int currentX = (int) (column * datumSize);
                int currentY = (int) (row * datumSize);

                final Rect bounds = unFocusedBg.getBounds();
                if (datumSize != bounds.right ||
                        datumSize != bounds.bottom) {
                    unFocusedBg.setBounds(0, 0, dayWidth, dayWidth);
                }
                canvas.translate(currentX, currentY);
                if (info.flag != CURRENT_MONTH)
                    unFocusedBg.draw(canvas);
                if (row == getCurrentLocation().x - 1 && column == getCurrentLocation().y - 1) {
                    final Rect bounds1 = focusedBg.getBounds();
                    if (datumSize != bounds1.right ||
                            datumSize != bounds1.bottom) {
                        focusedBg.setBounds(0, 0, dayWidth, dayWidth);
                    }
                    focusedBg.draw(canvas);
                    final Rect bounds2 = moveFocused.getBounds();
                    if (datumSize != bounds2.right ||
                            datumSize != bounds2.bottom) {
                        moveFocused.setBounds(0, 0, dayWidth, dayWidth);
                    }
                    moveFocused.draw(canvas);
                }

                // Draw a drop shadow for the text
                // Draw the text
                canvas.drawText(info.data,
                        datumSize / 2,
                        datumSize / 2
                                + (paint.getTextSize() - paint.descent()) / 2,
                        paint);
                paint.setShadowLayer(0, 0, 0, 0);
                canvas.translate(-currentX, -currentY);
            }
        }
    }

    /**
     * @param canvas
     * @description 画手指的触摸位置
     */
    void drawTouchPointer(Canvas canvas) {
        if (mCurrentPoint.x != 0 || mCurrentPoint.y != 0) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.RED);
            canvas.drawLine(mCurrentPoint.x, getHeight() - 1, mCurrentPoint.x, 0, paint);
            canvas.drawLine(1, mCurrentPoint.y, getWidth(), mCurrentPoint.y, paint);
            paint.setTextSize(20);
            paint.setTextAlign(Paint.Align.RIGHT);
            Point location = getCurrentLocation();
            canvas.drawText(location.toString(), getWidth() - paint.getTextSize(), paint.getTextSize(), paint);
        }
    }

    /**
     * @return
     * @description 获取手指当前所处位置
     */
    Point getCurrentLocation() {
        Point mCurrentLocation = new Point();
        mCurrentLocation.x = (int) Math.ceil(mCurrentPoint.y / datumSize);
        mCurrentLocation.y = (int) Math.ceil(mCurrentPoint.x / datumSize);
        return mCurrentLocation;
    }

    /**
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     * @description 日历月视图共有7x6
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);//获取view宽度
        int height = MeasureSpec.getSize(heightMeasureSpec);//获取view高度
        double sampleWidth = width / (double) COLUMNS;
        double sampleHeight = height / (double) ROWS;
        double sampleSize = sampleWidth < sampleHeight ? sampleWidth : sampleHeight;
        width = (int) Math.ceil(sampleSize * COLUMNS);
        height = (int) Math.ceil(sampleSize * ROWS);
        Log.e(LOG_TAG,"width "+width+" , height "+height);
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mCurrentPoint.x = event.getX();
        mCurrentPoint.y = event.getY();
        invalidate();
        return super.onTouchEvent(event);
    }
}
