package com.example.hojiang.wallpaperchange;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.ArrayList;

public class MyWallpaperManager {

    private static final String TAG = MyWallpaperManager.class.getSimpleName();

    private Context appContext;

    private BroadcastReceiver newBingWallpaperDownloadedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "onReceive");
            onNewBingWallpaperDownloaded();
        }
    };

    public static MyWallpaperManager getInstance() {
        return MyWallpaperManagerHolder.instance;
    }

    public void init(Context appContext) {
        this.appContext = appContext;
        this.appContext.registerReceiver(this.newBingWallpaperDownloadedReceiver, new IntentFilter(BingWallpaperDownloadService.NEW_BING_WALLPAPER_SET_DOWNLOADED));
    }

    public void unInit() {
        appContext.unregisterReceiver(this.newBingWallpaperDownloadedReceiver);
    }

    private void onNewBingWallpaperDownloaded() {
        ArrayList<String> bingWallpapers = getBingWallpaperFileList();
        if (bingWallpapers == null || bingWallpapers.size() == 0) return;

        String fileName = bingWallpapers.get(0);
        if (fileName == null || fileName.isEmpty() || fileName.contains("zzzzzzzz_DEFAULT_BING_WALLPAPER")) return;

        String key = BingWallpaperInfo.parseKeyFromFilePath(fileName);
        BingWallpaperInfo newBingWallpaperInfo = BingWallpaperInfo.createBingWallpaperInfo(key, fileName, copyrightContent);

        if (!this.shouldUpdateWithNewBingWallpaper(newBingWallpaperInfo)) {
            return;
        }

        changeToBingWallpaper(newBingWallpaperInfo);
    }

    private static class MyWallpaperManagerHolder {
        public static final MyWallpaperManager instance = new MyWallpaperManager();
    }
}
