package com.watch.timer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class TimeOutScreen extends Activity {
    private final String TAG = this.getClass().getSimpleName();
    private String mTimerName;
    private int mTimerHour;
    private int mTimerMinute;
    private TextView mDisplayTimerName;

    private PowerManager.WakeLock mWakeLock;
    private Vibrator mVibrator;
    private MediaPlayer mMediaPlayer;

    private Uri mAlarmToneUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_out_screen);

        initData();

        initView();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        getIntentExtras(intent);
        if (!TextUtils.isEmpty(mTimerName)) {
            mDisplayTimerName.setText(mTimerName);
        }
    }

    private void initData() {
        Intent intent = getIntent();
        getIntentExtras(intent);

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mMediaPlayer = new MediaPlayer();
        mAlarmToneUri = RingtoneManager.getActualDefaultRingtoneUri(
                this, RingtoneManager.TYPE_ALARM);
    }

    private void getIntentExtras(Intent intent) {
        if (intent != null) {
            mTimerName = intent.getStringExtra("timer_name");
            mTimerHour = intent.getIntExtra("timer_hour", 0);
            mTimerMinute = intent.getIntExtra("timer_minute", 0);
        }
    }

    private void initView() {
        ImageView closeImg = (ImageView) findViewById(R.id.id_close_timer_img);
        closeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimeOutScreen.this.finish();
            }
        });

        ImageView startImg = (ImageView) findViewById(R.id.id_start_timer_img);
        startImg.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("timer_hour", mTimerHour);
                intent.putExtra("timer_minute", mTimerMinute);
                intent.setAction("com.ali.watch.TIMER");
                intent.setComponent(
                        new ComponentName("com.watch.timer", "com.watch.timer.MainActivity"));
                startActivity(intent);
            }
        });

        mDisplayTimerName = (TextView) findViewById(R.id.id_timer_display_name);
        if (!TextUtils.isEmpty(mTimerName)) {
            mDisplayTimerName.setText(mTimerName);
        }
    }

    private void startAlarmUser() {
        if (mVibrator != null && mVibrator.hasVibrator()) {
            long[] pattern = {0, 100, 1000};
            mVibrator.vibrate(pattern, 0);
        }

        try {
            if (mAlarmToneUri != null && mMediaPlayer != null) {
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(this, mAlarmToneUri);
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mMediaPlayer.setLooping(true);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopAlarmUser() {
        if (mVibrator != null) {
            mVibrator.cancel();
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Set the window keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        // Acquire wakelock
        PowerManager pm = (PowerManager) getApplicationContext().
                getSystemService(Context.POWER_SERVICE);
        if (mWakeLock == null) {
            //noinspection deprecation
            mWakeLock = pm.newWakeLock((PowerManager.FULL_WAKE_LOCK |
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), TAG);
        }

        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }

        startAlarmUser();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        stopAlarmUser();
    }
}
