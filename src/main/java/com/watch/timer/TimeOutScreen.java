package com.watch.timer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class TimeOutScreen extends Activity {
    private String mTimerName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_out_screen);

        initData();

        initView();
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            mTimerName = intent.getStringExtra("timer_name");
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
                intent.setAction("com.ali.watch.TIMER");
                intent.setComponent(
                        new ComponentName("com.watch.timer", "com.watch.timer.MainActivity"));
                startActivity(intent);
            }
        });

        TextView textView = (TextView) findViewById(R.id.id_timer_display_name);
        if (!TextUtils.isEmpty(mTimerName)) {
            textView.setText(mTimerName);
        }
    }
}
