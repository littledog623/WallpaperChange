package com.example.hojiang.wallpaperchange;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmManagerReceiver extends BroadcastReceiver {

    public static final int NoExtra = -1;
    public static final int ProductStatusLog = 0;
    public static final int UpdateBingWallpaper = 1;
    public static final int UpdateWeather = 2;
    public static final int UpdateLocation = 3;
    public static final int UpdateLoopAnnotation = 4;
    public static final int UpdateMemoryUsage = 5;
    public static final int DailyCustomWallpaper = 6;
    public static final int BingDownloadTask = 7;
    public static String KEY_ALARM_TYPE = "alarmType";

    @Override
    public void onReceive(Context context, Intent intent) {
        int alarmType = intent.getIntExtra(KEY_ALARM_TYPE, NoExtra);

        switch (alarmType) {
            case BingDownloadTask:
                BingWallpaperDownloadService.start(context);
                break;
        }

    }
}
