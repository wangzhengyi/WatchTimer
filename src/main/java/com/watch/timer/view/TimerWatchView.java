package com.watch.timer.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.watch.timer.R;

import java.util.ArrayList;
import java.util.List;

public class TimerWatchView extends View {
    /**
     * 倒计时默认间隔时间.
     */
    private static final int DEFAULT_COUNT_DOWN_INTERVAL = 1000;

    private static final float DEFAULT_CIRCLE_RADIUS = 180;
    private static final float DEFAULT_SCALE_LINE_HEIGHT = 8;
    private static final float DEFAULT_SCALE_LINE_WIDTH = 2;
    private static final int DEFAULT_SCALE_LINE_COLOR = Color.parseColor("#ffffff");

    private Matrix mMatrix;
    private List<LineDrawable> mScaleList;

    private float mCircleRadius;
    private int mScaleLineColor;
    private float mScaleLineHeight;
    private float mScaleLineWidth;
    private int mCenterX;
    private int mCenterY;
    private boolean once;

    private static final int MSG = 1;

    /**
     * 倒计时的总时间
     */
    private long mMillisInFuture;

    /**
     * 倒计时的时间间隔
     */
    private long mCountdownInterval;

    /**
     * 倒计时终止的时间
     */
    private long mStopTimeInFuture;

    /**
     * 倒计时暂停的时间
     */
    private long mPauseTime;

    /**
     * 倒计时停止标志
     */
    private boolean mIsStop = false;

    /**
     * 倒计时暂停标志
     */
    private boolean mIsPause = false;

    /**
     * 每个刻度代表的毫秒数
     */
    private long mScaleMillis;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            synchronized (TimerWatchView.this) {
                if (mIsStop || mIsPause) return;

                final long millisLeft = mStopTimeInFuture - System.currentTimeMillis();
                if (millisLeft <= 0) {
                    // 倒计时结束
                    if (mListener != null) {
                        mListener.timerFinish();
                    }
                } else {
                    long lastTickStart = System.currentTimeMillis();
                    onTick(millisLeft);

                    long delay = lastTickStart + mCountdownInterval <= mStopTimeInFuture ?
                            mCountdownInterval : mStopTimeInFuture - lastTickStart;
                    sendMessageDelayed(obtainMessage(MSG), delay);
                }
            }
        }
    };

    private void onTick(long millisLeft) {
        //当前的记录时间(总毫秒数, 秒数, 分钟数, 小时数)
        long mMillisecondL = millisLeft;
        long mSecondNum = (mMillisecondL / 1000) % 60;
        long mMinuteNum = (mMillisecondL / 1000 / 60) % 60;
        long mHourNum = mMillisecondL / 1000 / 60 / 60;

        String mSecondText = String.format("%02d", mSecondNum);
        String mMinuteText = String.format("%02d", mMinuteNum);
        String mHourText = mHourNum == 0 ? "" : String.format("%02d", mHourNum);

        mMillisecondL = (mMillisecondL % 1000) / 10;
        String mMillisecondText = String.format("%02d", mMillisecondL);

        String mTime;
        if ("".equals(mHourText)) {
            mTime = mMinuteText + ":" + mSecondText;
        } else {
            mTime = mHourText + ":" + mMinuteText + ":" + mSecondText;
        }
        mMillisecondText = "." + mMillisecondText;
        if (mListener != null) {
            mListener.onTimeChanged(mTime, mMillisecondText);
        }
        postInvalidate();
    }

    public TimerWatchView(Context context) {
        this(context, null);
    }

    public TimerWatchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimerWatchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.TimerWatchView);

        mCircleRadius = ta.getDimension(R.styleable.TimerWatchView_circle_radius,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, DEFAULT_CIRCLE_RADIUS,
                        getResources().getDisplayMetrics()));
        mScaleLineColor = ta.getColor(R.styleable.TimerWatchView_scale_line_color,
                DEFAULT_SCALE_LINE_COLOR);
        mScaleLineHeight = ta.getDimension(R.styleable.TimerWatchView_scale_line_height,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, DEFAULT_SCALE_LINE_HEIGHT,
                        getResources().getDisplayMetrics()));
        mScaleLineWidth = ta.getDimension(R.styleable.TimerWatchView_scale_line_width,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, DEFAULT_SCALE_LINE_WIDTH,
                        getResources().getDisplayMetrics()));
        ta.recycle();

        initData();
    }

    private void initData() {
        mMatrix = new Matrix();
        mScaleList = new ArrayList<>();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mCenterX = getMeasuredWidth() / 2;
        mCenterY = getMeasuredHeight() / 2;

        if (!once && !isInEditMode()) {
            setup();
            once = true;
        }
    }

    private void setup() {
        PointF scaleInPointF = new PointF(mCenterX, mCenterY - mCircleRadius);
        PointF scaleExtPointF = new PointF(mCenterX, mCenterY - mCircleRadius - mScaleLineHeight);

        for (int i = 0; i < 60; i ++) {
            LineDrawable lineDrawable = new LineDrawable(scaleInPointF, scaleExtPointF);
            mScaleList.add(lineDrawable);

            scaleInPointF = rotate(scaleInPointF, mCenterX, mCenterY);
            scaleExtPointF = rotate(scaleExtPointF, mCenterX, mCenterY);
        }
    }

    private PointF rotate(PointF point, float centerX, float centerY) {
        mMatrix.setRotate(6, centerX, centerY);
        float[] pts = new float[2];

        pts[0] = point.x;
        pts[1] = point.y;

        mMatrix.mapPoints(pts);

        return new PointF(pts[0], pts[1]);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 计算度过的时间
        long durationTime = mStopTimeInFuture - System.currentTimeMillis();
        // 绘制刻度
        for (int i = 1; i <= mScaleList.size(); i ++) {
            LineDrawable lineDrawable = mScaleList.get(i % mScaleList.size());
            lineDrawable.setStrokeWidth((int) mScaleLineWidth);
            Log.e("TAG", "i=" + i + ", scaleTime=" + (i * mScaleMillis) + ", durationTime=" + durationTime);
            if (mMillisInFuture - (i * mScaleMillis) > durationTime) {
                lineDrawable.setColor(Color.TRANSPARENT);
            } else {
                lineDrawable.setColor(mScaleLineColor);
            }
            lineDrawable.draw(canvas);
        }
    }

    /** 开始倒计时 */
    public synchronized final void onStart(long millisInFuture) {
        mMillisInFuture = millisInFuture;
        mCountdownInterval = DEFAULT_COUNT_DOWN_INTERVAL;
        mIsPause = mIsStop = false;
        mStopTimeInFuture = System.currentTimeMillis() + millisInFuture;
        mScaleMillis = millisInFuture / 60;
        mHandler.sendMessage(mHandler.obtainMessage(MSG));
    }

    /** 停止倒计时 */
    public synchronized final void onStop() {
        mIsStop = true;
        mHandler.removeMessages(MSG);
        if (mListener != null) {
            mListener.stop();
        }
    }

    /** 暂停倒计时 */
    public synchronized final void onPause() {
        mIsPause = true;
        mPauseTime = System.currentTimeMillis();
        mHandler.removeMessages(MSG);
    }

    /** 重新开始倒计时 */
    public synchronized final void onRestart() {
        mIsPause = false;
        mStopTimeInFuture = mStopTimeInFuture + (System.currentTimeMillis() - mPauseTime);
        mHandler.sendMessage(mHandler.obtainMessage(MSG));
    }

    public final long getStopTimeInFuture() {
        return mStopTimeInFuture;
    }

    /** 判断秒表是否正在运行 */
    public boolean isRunning() {
        return !mIsPause && !mIsStop;
    }

    private WatchTimerListener mListener;

    public void setWatchTimerListener(WatchTimerListener listener) {
        mListener = listener;
    }

    public interface WatchTimerListener {
        void timerFinish();

        void stop();

        void onTimeChanged(String mBigTime, String mLittleTime);
    }
}
