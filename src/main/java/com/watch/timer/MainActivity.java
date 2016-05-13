package com.watch.timer;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.watch.timer.events.AlphaEvent;
import com.watch.timer.view.TimerTextUtils;
import com.watch.timer.view.TimerWatchView;
import com.watch.timer.view.WheelView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class MainActivity extends Activity implements TimerWatchView.WatchTimerListener{
    private static final int REQUEST_CODE = 0x6006;
    private List<String> mHourContentList;
    private List<String> mMinuteContentList;

    private LinearLayout mStartLayout;
    private ImageView mStartImg;

    private WheelView mHourTextView;
    private WheelView mMinuteTextView;

    private RelativeLayout mRunningLayout;
    private ImageView mPauseAndRestartImageView;
    private TimerWatchView mTimerWatchView;
    private TextView mBigTimeTv;
    private TextView mLittleTimeTv;

    private String mTimerName;

    private boolean isInBackGround = false;

    private AlarmManager mAlarmManager;

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onChangeAlphaEvent(AlphaEvent event) {
        if (mHourTextView.getSelectedPosition() != 0
                || mMinuteTextView.getSelectedPosition() != 0) {
            mStartImg.setImageAlpha(255);
        } else {
            mStartImg.setImageAlpha(125);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();

        initView();

        getParamsFromIntent();

        registerEventBus();
    }

    private void registerEventBus() {
        EventBus.getDefault().register(this);
    }

    private void unregisterEventBus() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isInBackGround) {
            // 取消定时任务
            isInBackGround = false;
            PendingIntent pendingIntent = createPendingIntent();
            pendingIntent.cancel();
            mAlarmManager.cancel(pendingIntent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 使用AlarmManager定期发送通知
        if (mTimerWatchView.isRunning()) {
            long stopTime = mTimerWatchView.getStopTimeInFuture();
            PendingIntent pendingIntent = createPendingIntent();
            mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, stopTime, pendingIntent);
            isInBackGround = true;
        }
    }

    @Override
    protected void onDestroy() {
        if (mTimerWatchView != null && mTimerWatchView.isRunning()) {
            mTimerWatchView.onStop();
        }
        unregisterEventBus();
        super.onDestroy();
    }

    private PendingIntent createPendingIntent() {
        Intent intent = createTimeOutScreenActivity();
        return PendingIntent.getActivity(this, REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void initData() {
        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mHourContentList = TimerTextUtils.getContentList(TimerTextUtils.STYLE_HOUR);
        mMinuteContentList = TimerTextUtils.getContentList(TimerTextUtils.STYLE_MINUTE);
    }

    private void initView() {
        mStartLayout = (LinearLayout) findViewById(R.id.id_start_layout);
        mHourTextView = (WheelView) findViewById(R.id.id_hour_timer_text_view);
        mHourTextView.setItemListAndScaleContent(mHourContentList,
                TimerTextUtils.getScaleContent(TimerTextUtils.STYLE_HOUR));
        mHourTextView.setSelectedPosition(0);

        mMinuteTextView = (WheelView) findViewById(R.id.id_minute_timer_text_view);
        mMinuteTextView.setItemListAndScaleContent(mMinuteContentList,
                TimerTextUtils.getScaleContent(TimerTextUtils.STYLE_MINUTE));
        mMinuteTextView.setSelectedPosition(0);

        mStartImg = (ImageView) findViewById(R.id.id_start_img);
        mStartImg.setImageAlpha(125);
        mStartImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = mHourTextView.getSelectedPosition();
                int minute = mMinuteTextView.getSelectedPosition();
                mTimerName = "";
                if (hour > 0) {
                    if (hour == 1) {
                        mTimerName += hour + getResources().getString(R.string.hour);
                    } else {
                        mTimerName += hour + getResources().getString(R.string.hours);
                    }
                }

                if (minute > 0) {
                    if (minute == 1) {
                        mTimerName += minute + getResources().getString(R.string.minute);
                    } else {
                        mTimerName += minute + getResources().getString(R.string.minutes);
                    }
                }

                long millisInFuture = (hour * 60 + minute) * 60 * 1000;
                if (millisInFuture <= 0) {
                    return;
                }
                mStartLayout.setVisibility(View.GONE);
                mRunningLayout.setVisibility(View.VISIBLE);
                mTimerWatchView.onStart(millisInFuture);
            }
        });

        mRunningLayout = (RelativeLayout) findViewById(R.id.id_running_layout);

        mPauseAndRestartImageView = (ImageView) findViewById(R.id.id_pause_restart_img);
        mPauseAndRestartImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTimerWatchView.isRunning()) {
                    mPauseAndRestartImageView.setImageResource(R.drawable.start);
                    mTimerWatchView.onPause();
                } else {
                    mPauseAndRestartImageView.setImageResource(R.drawable.pause);
                    mTimerWatchView.onRestart();
                }
            }
        });

        ImageView mCloseImageView = (ImageView) findViewById(R.id.id_close_img);
        mCloseImageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mTimerWatchView.onStop();
            }
        });
        mTimerWatchView = (TimerWatchView) findViewById(R.id.id_timer_watch_view);
        mTimerWatchView.setWatchTimerListener(this);
        mBigTimeTv = (TextView) findViewById(R.id.id_big_time_text_view);
        mLittleTimeTv = (TextView) findViewById(R.id.id_little_time_text_view);
    }

    private void getParamsFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            int hour = intent.getIntExtra("timer_hour", 0);
            int minute = intent.getIntExtra("timer_minute", 0);
            if (hour != 0 || minute != 0) {
                mHourTextView.setSelectedPosition(hour);
                mMinuteTextView.setSelectedPosition(minute);
                mStartImg.setImageAlpha(255);
            }
        }
    }

    @Override
    public void timerFinish() {
        Intent intent = createTimeOutScreenActivity();
        startActivity(intent);
        MainActivity.this.finish();
    }

    @NonNull
    private Intent createTimeOutScreenActivity() {
        Intent intent = new Intent(this, TimeOutScreen.class);
        intent.putExtra("timer_name", mTimerName);
        intent.putExtra("timer_hour", mHourTextView.getSelectedPosition());
        intent.putExtra("timer_minute", mMinuteTextView.getSelectedPosition());
        return intent;
    }

    @Override
    public void stop() {
        mBigTimeTv.setText(getString(R.string.init_big_time));
        mLittleTimeTv.setText(getString(R.string.init_little_time));
        mStartLayout.setVisibility(View.VISIBLE);
        mPauseAndRestartImageView.setImageResource(R.drawable.pause);
        mRunningLayout.setVisibility(View.GONE);
        mHourTextView.setSelectedPosition(mHourTextView.getSelectedPosition());
        mMinuteTextView.setSelectedPosition(mMinuteTextView.getSelectedPosition());
    }

    @Override
    public void onTimeChanged(String mBigTime, String mLittleTime) {
        mBigTimeTv.setText(mBigTime);
        mLittleTimeTv.setText(mLittleTime);
    }
}
