package com.watch.timer;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.watch.timer.view.TimerTextUtils;
import com.watch.timer.view.WheelView;
import com.watch.timer.view.TimerWatchView;

import java.util.List;

public class MainActivity extends Activity implements TimerWatchView.WatchTimerListener{
    private static final int REQUEST_CODE = 0x6006;
    private List<String> mHourContentList;
    private List<String> mMinuteContentList;

    private LinearLayout mStartLayout;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();

        initView();
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
        super.onDestroy();
    }

    private PendingIntent createPendingIntent() {
        Intent intent = new Intent(MainActivity.this, TimeOutScreen.class);
        intent.putExtra("timer_name", mTimerName);
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

        ImageView mStartImg = (ImageView) findViewById(R.id.id_start_img);
        mStartImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = mHourTextView.getSelectedPosition();
                int minute = mMinuteTextView.getSelectedPosition();
                mTimerName = "";
                if (hour > 0) {
                    mTimerName += hour + getResources().getString(R.string.hour);
                }
                if (minute > 0) {
                    mTimerName += minute + getResources().getString(R.string.minute);
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

    @Override
    public void timerFinish() {
        Intent intent = new Intent(this, TimeOutScreen.class);
        intent.putExtra("timer_name", mTimerName);
        startActivity(intent);
        MainActivity.this.finish();
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
