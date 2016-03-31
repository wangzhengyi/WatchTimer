package com.watch.timer.view;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TimerTextUtils {
    /**
     * 小时标识
     */
    public static final int STYLE_HOUR = 1;

    /**
     * 分钟标识
     */
    public static final int STYLE_MINUTE = 2;

    public static String getScaleContent(int style) {
        switch (style) {
            case STYLE_HOUR:
                return createHourScale();
            case STYLE_MINUTE:
                return createMinuteScale();
            default:
                return "";
        }
    }

    private static String createHourScale() {
        String language = Locale.getDefault().getLanguage();
        Log.e("TAG", "language=" + language);
        if ("en".equals(language)) {
            return "Hour";
        } else {
            String country = Locale.getDefault().getCountry();
            Log.e("TAG", "country=" + country);
            if ("TW".equals(country)) {
                return "小時";
            }
            return "小时";
        }
    }

    private static String createMinuteScale() {
        String language = Locale.getDefault().getLanguage();
        Log.e("TAG", "language=" + language);
        if ("en".equals(language)) {
            return "Minute";
        } else {
            String country = Locale.getDefault().getCountry();
            Log.e("TAG", "country=" + country);
            if ("TW".equals(country)) {
                return "分鐘";
            }
            return "分钟";
        }
    }

    public static List<String> getContentList(int style) {
        switch (style) {
            case STYLE_HOUR:
                return createHourContents();
            case STYLE_MINUTE:
                return createMinuteContents();
            default:
                return Collections.emptyList();
        }
    }

    private static List<String> createMinuteContents() {
        List<String> minuteContents = new ArrayList<>();
        for (int i = 0; i <= 59; i++) {
            minuteContents.add(String.format("%02d", i));
        }
        return minuteContents;
    }

    private static List<String> createHourContents() {
        List<String> hourContents = new ArrayList<>();
        for (int i = 0; i <= 12; i++) {
            hourContents.add(String.format("%02d", i));
        }
        return hourContents;
    }
}
