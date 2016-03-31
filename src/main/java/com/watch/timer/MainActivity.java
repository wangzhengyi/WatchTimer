package com.watch.timer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.watch.timer.view.TimerTextUtils;
import com.watch.timer.view.TimerTextView;
import com.watch.timer.view.TimerWatchView;

import java.util.List;

public class MainActivity extends Activity implements TimerWatchView.WatchTimerListener{
    private List<String> mHourContentList;
    private List<String> mMinuteContentList;

    private LinearLayout mStartLayout;
    private TimerTextView mHourTextView;
    private TimerTextView mMinuteTextView;
    private ImageView mStartImg;

    private RelativeLayout mRunningLayout;
    private ImageView mCloseImageView;
    private ImageView mPauseAndRestartImageView;
    private TimerWatchView mTimerWatchView;
    private TextView mBigTimeTv;
    private TextView mLittleTimeTv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();

        initView();
    }

    private void initData() {
        mHourContentList = TimerTextUtils.getContentList(TimerTextUtils.STYLE_HOUR);
        mMinuteContentList = TimerTextUtils.getContentList(TimerTextUtils.STYLE_MINUTE);
    }

    private void initView() {
        mStartLayout = (LinearLayout) findViewById(R.id.id_start_layout);
        mHourTextView = (TimerTextView) findViewById(R.id.id_hour_timer_text_view);
        mHourTextView.setItemListAndScaleContent(mHourContentList,
                TimerTextUtils.getScaleContent(TimerTextUtils.STYLE_HOUR));
        mHourTextView.setSelectedPosition(0);

        mMinuteTextView = (TimerTextView) findViewById(R.id.id_minute_timer_text_view);
        mMinuteTextView.setItemListAndScaleContent(mMinuteContentList,
                TimerTextUtils.getScaleContent(TimerTextUtils.STYLE_MINUTE));
        mMinuteTextView.setSelectedPosition(0);

        mStartImg = (ImageView) findViewById(R.id.id_start_img);
        mStartImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = mHourTextView.getSelectedPosition();
                int minute = mMinuteTextView.getSelectedPosition();
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
        mCloseImageView = (ImageView) findViewById(R.id.id_close_img);
        mPauseAndRestartImageView = (ImageView) findViewById(R.id.id_pause_restart_img);
        mCloseImageView.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                mStartLayout.setVisibility(View.VISIBLE);
                mRunningLayout.setVisibility(View.GONE);
                mHourTextView.setSelectedPosition(0);
                mMinuteTextView.setSelectedPosition(0);
            }
        });
        mTimerWatchView = (TimerWatchView) findViewById(R.id.id_timer_watch_view);
        mTimerWatchView.setWatchTimerListener(this);
        mBigTimeTv = (TextView) findViewById(R.id.id_big_time_text_view);
        mLittleTimeTv = (TextView) findViewById(R.id.id_little_time_text_view);
    }

    @Override
    public void timerFinish() {

    }

    @Override
    public void stop() {
        mBigTimeTv.setText(getString(R.string.init_big_time));
        mLittleTimeTv.setText(getString(R.string.init_little_time));
    }

    @Override
    public void onTimeChanged(String mBigTime, String mLittleTime) {
        mBigTimeTv.setText(mBigTime);
        mLittleTimeTv.setText(mLittleTime);
    }
}
