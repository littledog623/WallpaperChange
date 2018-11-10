package com.example.hojiang.wallpaperchange;

import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BingWallpaperInfo {
    private static final String TAG = BingWallpaperInfo.class.getSimpleName();

    public static final String BING_WALLPAPER_KEY_PREFIX = "bingwallpaper";
    public static final String BING_THUMBNAIL_KEY_PREFIX = "sbingwallpaper";

    private static final String BING_WALLPAPER_URL_BASE_PATTERN_STRING = "/(.*/)*(.*)";
    private static final String BING_WALLPAPER_KEY_PATTERN = BING_WALLPAPER_KEY_PREFIX + "_(\\d*)_(\\d*)_(.*)";
    private static final String BING_WALLPAPER_FILE_NAME_PATTERN = BING_WALLPAPER_KEY_PREFIX + "_(\\d*)_(\\d*)_.*.jpg";

    private static final Pattern bingWallpaperUrlBasePattern = Pattern.compile(BING_WALLPAPER_URL_BASE_PATTERN_STRING);
    private static final Pattern bingWallpaperFileNamePattern = Pattern.compile(BING_WALLPAPER_FILE_NAME_PATTERN);

    private String startDateString;
    private String endDateString;
    private String copyright;

    public static BingWallpaperInfo createBingWallpaperInfo(String fileName, String copyright) {
        if (null == fileName || fileName.isEmpty()) {
            return null;
        }

        if (null == copyright || copyright.isEmpty()) {
        }

        String matchedStartDateString = "";
        String matchedEndDateString = "";
        Matcher matcher = bingWallpaperFileNamePattern.matcher(fileName);
        if (matcher.find() && matcher.groupCount() > 1) {
            matchedStartDateString = matcher.group(1);
            matchedEndDateString = matcher.group(2);
        }

        return createBingWallpaperInfo(matchedStartDateString, matchedEndDateString, copyright);
    }

    public static BingWallpaperInfo createBingWallpaperInfo(String startDateString, String endDateString, String copyright) {
        if (null == startDateString || startDateString.isEmpty()) {
            return null;
        }

        if (null == endDateString || endDateString.isEmpty()) {
            return null;
        }

        BingWallpaperInfo bingWallpaperInfo = new BingWallpaperInfo();
        bingWallpaperInfo.startDateString = startDateString;
        bingWallpaperInfo.endDateString = endDateString;
        bingWallpaperInfo.copyright = copyright;

        return bingWallpaperInfo;
    }

    public boolean isBingWallpaper() {
        return true;
    }

    public String getCopyrightText() {
        return this.copyright;
    }


    // bing download url   https://www.bing.com/az/hprichbg/rb/MountainDayNepal_ROW11115933291_1080x1920.jpg
    // bing key            bingwallpaper_20180216_20180217_MountainDayNepal_ROW11115933291
    // bing file path      /data/user/0/com.microsoft.launcher/files/bingwallpaper_20180216_20180217_MountainDayNepal_ROW11115933291_1080x1920.jpg

    public static String createKeyForBingWallpaper(String bingWallpaperUrlBase, String startDateString, String endDateString) {
        String key = "";

        if (null == bingWallpaperUrlBase || bingWallpaperUrlBase.isEmpty()) {
            return key;
        }

        String matchedDescription = "";
        Matcher matcher = bingWallpaperUrlBasePattern.matcher(bingWallpaperUrlBase);
        if (matcher.find()) {
            matchedDescription = matcher.group(matcher.groupCount());
        }

        if (!TextUtils.isEmpty(matchedDescription)) {
            key = String.format("%s_%s_%s_%s", BING_WALLPAPER_KEY_PREFIX, startDateString, endDateString, matchedDescription);
        }
        return key;
    }

    public static String parseUrlbaseFromBingKey(String key){
        // from  bingwallpaper_20180216_20180217_MountainDayNepal_ROW11115933291
        // return az/hprichbg/rb/MountainDayNepal_ROW11115933291
        Pattern keyPattern = Pattern.compile(BING_WALLPAPER_KEY_PATTERN);
        Matcher matcher = keyPattern.matcher(key);
        if (matcher.find() && matcher.groupCount() > 1){
            return "/az/hprichbg/rb/" + matcher.group(3);
        }
        return "";
    }

    public static String parseKeyFromFilePath(String filePath) {
        if (filePath == null) {
            return "";
        }

        return parseKeyFromFilePath(filePath, filePath.contains(BING_THUMBNAIL_KEY_PREFIX));
    }

    public static String parseKeyFromFilePath(String filePath, boolean isThumbnail){
        // from /data/user/0/com.microsoft.launcher/files/bingwallpaper_20180216_20180217_MountainDayNepal_ROW11115933291_1080x1920.jpg
        // get bingwallpaper_20180216_20180217_MountainDayNepal_ROW11115933291
        String prefix = isThumbnail? BING_THUMBNAIL_KEY_PREFIX : BING_WALLPAPER_KEY_PREFIX;
        String BING_WALLPAPER_FILE_PATH_PATTERN = "(.*/)" + prefix + "(.*)_(\\d*)x(\\d*).jpg";
        Matcher matcher = Pattern.compile(BING_WALLPAPER_FILE_PATH_PATTERN).matcher(filePath);
        if (matcher.find() && matcher.groupCount() > 3) {
            return  BING_WALLPAPER_KEY_PREFIX + matcher.group(2);
        }
        return "";
    }

    public static String createKeyForBingThumbnail(String bingWallpaperUrlBase, String startDateString, String endDateString) {
        String key = "";

        if (null == bingWallpaperUrlBase || bingWallpaperUrlBase.isEmpty()) {
            return key;
        }

        String matchedDescription = "";
        Matcher matcher = bingWallpaperUrlBasePattern.matcher(bingWallpaperUrlBase);
        if (matcher.find()) {
            matchedDescription = matcher.group(matcher.groupCount());
        }

        if (!TextUtils.isEmpty(matchedDescription)) {
            key = String.format("%s_%s_%s_%s", BING_THUMBNAIL_KEY_PREFIX, startDateString, endDateString, matchedDescription);
        }
        return key;
    }
}