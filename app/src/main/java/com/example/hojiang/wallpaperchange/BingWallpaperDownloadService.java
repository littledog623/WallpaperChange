package com.example.hojiang.wallpaperchange;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import com.example.hojiang.wallpaperchange.BingWallpaperInfo;
import com.example.hojiang.wallpaperchange.utils.HttpDownloader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yingcai on 9/2/2015.
 */
public class BingWallpaperDownloadService extends JobIntentService {
    public static final String NEW_BING_WALLPAPER_SET_DOWNLOADED =
            "com.microsoft.launcher.wallpaper.intent.action.NEW_BING_WALLPAPER_SET_DOWNLOADED";
    private static final String TAG = BingWallpaperDownloadService.class.getSimpleName();


    private static final String BING_WALLPAPER_HPIMAGE_ARCHIVE_API_URL_STR_FOR_7_DAYS = "https://www.bing.com/HPImageArchive.aspx?n=7&idx=%s&format=js&mkt=en-ww";
    private static final String BING_HOST_URL = "https://www.bing.com";

    private static final int MAX_COUNT_OF_BING_WALLPAPER_TO_KEEP = 30;
    private static boolean usingMobileData = false;

    private Map<String, String> urlFilePathMap = new HashMap<>();

    public interface BingDownloadListener {
        void onFinish(final String message);
    }

    private static BingDownloadListener bingDownloadListener;

    public static void start(Context context) {
        enqueueWork(context, BingWallpaperDownloadService.class, JOB_ID, new Intent(context, BingWallpaperDownloadService.class));
        //MyWallpaperManager.getInstance().startAlarm(AlarmManagerReceiver.BingDownloadTask, AlarmManager.INTERVAL_HOUR);
    }

    public static void setBingDownloadListener(BingDownloadListener dl) {
        bingDownloadListener = dl;
    }

    private static final int JOB_ID = 10113;

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        this.downloadBingWallpaper();
    }

    private void downloadBingWallpaper() {
        Map<BingWallpaperInfo, String> bingWallpaperListToDownloadMap = this.fetchLatestBingWallpaperInfoMap();
        Log.e(TAG, "map size: " + bingWallpaperListToDownloadMap.size());
        for (BingWallpaperInfo bingWallpaperInfo : bingWallpaperListToDownloadMap.keySet()) {
            String url = bingWallpaperListToDownloadMap.get(bingWallpaperInfo);
            if (null == bingWallpaperInfo) {
                continue;
            }

            boolean succeededToDownloadOneWallpaper = this.fetchBingWallpaper(url, bingWallpaperInfo);
        }

        Intent intent = new Intent(NEW_BING_WALLPAPER_SET_DOWNLOADED);
        sendBroadcast(intent);
    }

    private boolean fetchBingWallpaper(String urlString, BingWallpaperInfo bingWallpaperInfo) {
        boolean succeeded = false;
        Log.e(TAG, urlString);
        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            return succeeded;
        }

        String targetFilePath = getFilePathFromUrl(url.toString());
        Log.e(TAG, "targetFilePath: " + targetFilePath);
        HttpDownloader downloader = new HttpDownloader(url);
        HttpDownloader.DownloadResultCode downloadResultCode = downloader.downloadToFile(targetFilePath);
        return succeeded;
    }

    private String getFilePathFromUrl(String urlString) {
        String[] strs = urlString.split("/");
        return this.getFilesDir().getAbsolutePath() + File.separator + strs[strs.length - 1];
    }


    private Map<BingWallpaperInfo, String> fetchLatestBingWallpaperInfoMap() {
        return fetchSevenDaysBingWallpaperInfoMap();
    }

    private HashMap<BingWallpaperInfo, String> fetchSevenDaysBingWallpaperInfoMap() {
        URL url;
        try {
            url = new URL(String.format(BING_WALLPAPER_HPIMAGE_ARCHIVE_API_URL_STR_FOR_7_DAYS, 0));
        } catch (MalformedURLException e) {
            return new HashMap<>();
        }

        Log.e(TAG, url.toString());
        HttpDownloader downloader = new HttpDownloader(url);
        HttpDownloader.DownloadResult downloadResult = downloader.download();

        if (downloadResult.isSucceeded()) {
            try {
                String content = new String(downloadResult.content, "UTF-8");
                Log.e(TAG, content);
                result = this.parseBingWallpaperHPImageArchiveContent(content);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    private HashMap<String, String> parseBingWallpaperHPImageArchiveContent(String content) {
        HashMap<String, String> result = new HashMap<>();

        JSONObject jsonBody;
        JSONArray imageJsonArray;
        try {
            jsonBody = new JSONObject(content);
            imageJsonArray = jsonBody.getJSONArray("images");
        } catch (JSONException e) {
            return result;
        }

        String startDateString;
        String endDateString;
        String bingWallpaperUrlBase;
        String copyright;

        for (int i = 0; i < imageJsonArray.length(); ++i) {
            try {
                JSONObject imageJsonObject = imageJsonArray.getJSONObject(i);
                bingWallpaperUrlBase = imageJsonObject.getString("urlbase");
            } catch (JSONException e) {
                continue;
            }

            String targetUrl;
            targetUrl = buildBingWallpaperDownloadUrl(bingWallpaperUrlBase, false);
            String filePath = getFilePathFromUrl(targetUrl);
            result.put(targetUrl, filePath);
        }

        return result;
    }

    public static String buildBingWallpaperDownloadUrl(String urlbase, boolean isThumbnail) {
        return String.format("%s%s_%s.jpg", BING_HOST_URL, urlbase, "1920x1080");
    }
}
