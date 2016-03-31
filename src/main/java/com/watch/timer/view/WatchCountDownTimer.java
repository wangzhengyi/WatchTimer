package com.watch.timer.view;

import android.os.Handler;
import android.os.Message;

/**
 * 倒计时具体实现抽象类.
 */
public abstract class WatchCountDownTimer {
    private static final int MSG = 1;

    /**
     * 倒计时的总时间
     */
    private final long mMillisInFuture;

    /**
     * 倒计时的时间间隔
     */
    private final long mCountdownInterval;

    /**
     * 倒计时终止的时间
     */
    private long mStopTimeInFuture;

    /**
     * 倒计时暂停的时间
     */
    private long mPauseTime;

    private boolean mIsStop = false;
    private boolean mIsPause = false;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            synchronized (WatchCountDownTimer.this) {
                if (mIsStop || mIsPause) return;

                final long millisLeft = mStopTimeInFuture - System.currentTimeMillis();
                if (millisLeft <= 0) {
                    // 倒计时结束
                    onFinish();
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


    /**
     * 构造函数.
     * @param millisInFuture 总倒计时时间
     * @param countdownInterval 倒计时间隔时间
     */
    public WatchCountDownTimer(long millisInFuture, long countdownInterval) {
        mMillisInFuture = millisInFuture;
        mCountdownInterval = countdownInterval;
    }

    /**
     * 倒计时间隔回调
     * @param millisUntilFinished 剩余的毫秒数
     */
    public abstract void onTick(long millisUntilFinished);

    /** 倒计时结束回调 */
    public abstract void onFinish();


    private synchronized WatchCountDownTimer start(long millisInFuture) {
        mIsStop = false;
        if (mMillisInFuture <= 0) {
            onFinish();
            return this;
        }
        mStopTimeInFuture = System.currentTimeMillis() + millisInFuture;
        mHandler.sendMessage(mHandler.obtainMessage(MSG));
        return this;
    }

    /** 开始倒计时 */
    public synchronized final void onStart() {
        start(mMillisInFuture);
    }

    /** 停止倒计时 */
    public synchronized final void onStop() {
        mIsStop = true;
        mHandler.removeMessages(MSG);
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
}
