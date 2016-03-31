package com.watch.timer;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.watch.timer.view.TimerTextUtils;
import com.watch.timer.view.TimerTextView;

import java.util.List;

public class MainActivity extends Activity {
    private List<String> mHourContentList;
    private List<String> mMinuteContentList;

    private TimerTextView mHourTextView;
    private TimerTextView mMinuteTextView;

    private ImageView mStartImg;


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
        mHourTextView = (TimerTextView) findViewById(R.id.id_hour_timer_text_view);
        mHourTextView.setItemListAndScaleContent(mHourContentList,
                TimerTextUtils.getScaleContent(TimerTextUtils.STYLE_HOUR));

        mMinuteTextView = (TimerTextView) findViewById(R.id.id_minute_timer_text_view);
        mMinuteTextView.setItemListAndScaleContent(mMinuteContentList,
                TimerTextUtils.getScaleContent(TimerTextUtils.STYLE_MINUTE));

        mStartImg = (ImageView) findViewById(R.id.id_start_img);
        mStartImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = mHourTextView.getSelectedPosition();
                int minute = mMinuteTextView.getSelectedPosition();

                Log.e("TAG", "hour=" + hour + ", minute=" + minute);
            }
        });
    }
}
