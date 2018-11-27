package com.example.hojiang.wallpaperchange;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.SystemClock;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

public class MyWallpaperManager {

    private static final String TAG = MyWallpaperManager.class.getSimpleName();

    private Context appContext;

    private WallpaperManager wallpaperManager;

    private BroadcastReceiver newBingWallpaperDownloadedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "onReceive");
            onNewBingWallpaperDownloaded(context);
        }
    };

    private MyHandlerThread mHandlerThread;

    public static MyWallpaperManager getInstance() {
        return MyWallpaperManagerHolder.instance;
    }

    public void startAlarm(Context context, long triggerAtMillis) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Service.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }
        Intent intent = new Intent(context, AlarmManagerReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + triggerAtMillis, pi);
    }

    public void init(Context appContext) {
        this.appContext = appContext;
        mHandlerThread = new MyHandlerThread("Wallpaper thread", Thread.NORM_PRIORITY);
        mHandlerThread.start();
        mHandlerThread.prepareHandler();
        wallpaperManager = WallpaperManager.getInstance(appContext);
    }

    public void unInit() {
        appContext.unregisterReceiver(this.newBingWallpaperDownloadedReceiver);
    }

    public void onNewBingWallpaperDownloaded(Context context) {
        Log.e(TAG, "onNewBingWallpaperDownloaded");
        String[] bingWallpapers = context.getResources().getStringArray(R.array.wallpaper_name);
        if (bingWallpapers.length == 0) return;

        int size = bingWallpapers.length;
        Random rand = new Random();
        int nextIndex = rand.nextInt(size);
        String fileName = "wallpaper_" + bingWallpapers[nextIndex];
        Resources resources = context.getResources();
        final int resourceId = resources.getIdentifier(fileName, "drawable",
                context.getPackageName());
        //resources.getDrawable(resourceId);
        Bitmap wallpaper = BitmapFactory.decodeResource(resources, resourceId);
        setWallpaper(wallpaper, 3);
        startAlarm(context, 5000);
    }


    public void setWallpaper(Bitmap bitmap, int reqCode) {
        int bw = bitmap.getWidth();
        int bh = bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(0.5f, 0.5f);
        Bitmap res = Bitmap.createBitmap(bitmap, 0, 0, bw, bh, matrix, true);
        int aw = res.getWidth();
        int ah = res.getHeight();
        mHandlerThread.postTask(() -> {
            try {
                Log.e(TAG, "UIThread Thread id: " + Thread.currentThread().getName());
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                if (res.compress(Bitmap.CompressFormat.JPEG, 100, stream)) {
                    InputStream resIs = new ByteArrayInputStream(stream.toByteArray());
                    Log.e(TAG, "mHandlerThread mHandlerThread Thread id: " + Thread.currentThread().getName());
                    wallpaperManager.setStream(resIs, null, false, reqCode);
                } else {
                    wallpaperManager.setBitmap(bitmap, null, false, reqCode);
                }
                Log.e(TAG, "systemWallpaperId: " + wallpaperManager.getWallpaperId(WallpaperManager.FLAG_SYSTEM) + ", lockWallpaperId: " + wallpaperManager.getWallpaperId(WallpaperManager.FLAG_LOCK));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static class MyWallpaperManagerHolder {
        public static final MyWallpaperManager instance = new MyWallpaperManager();
    }
}
