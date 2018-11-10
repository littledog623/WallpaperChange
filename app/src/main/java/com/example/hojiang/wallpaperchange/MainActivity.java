package com.example.hojiang.wallpaperchange;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView changeBothWallpaperButton;
    private TextView changeSystemWallpaperButton;
    private TextView changeLockScreenWallpaperButton;

    private WallpaperManager wallpaperManager;

    private static final int GALLERY_REQUEST_BOTH = 3;
    private static final int GALLERY_REQUEST_SYSTEM = 1;
    private static final int GALLERY_REQUEST_LOCK = 2;

    private MyHandlerThread mHandlerThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*changeBothWallpaperButton = findViewById(R.id.change_both_wallpaper_btn);
        changeBothWallpaperButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, GALLERY_REQUEST_BOTH);
        });
        changeSystemWallpaperButton = findViewById(R.id.change_system_wallpaper_btn);
        changeSystemWallpaperButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, GALLERY_REQUEST_SYSTEM);
        });
        changeLockScreenWallpaperButton = findViewById(R.id.change_lockscreen_wallpaper_btn);
        changeLockScreenWallpaperButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, GALLERY_REQUEST_LOCK);
        });
        wallpaperManager = WallpaperManager.getInstance(this);
        mHandlerThread = new MyHandlerThread("Wallpaper thread", Thread.NORM_PRIORITY);
        mHandlerThread.start();
        mHandlerThread.prepareHandler();*/
        BingWallpaperDownloadService.start(this);
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        Log.e(TAG, "resultCode: " + resultCode);
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            try {
                Uri imageUri = data.getData();
                InputStream is = getContentResolver().openInputStream(imageUri);
                Log.e(TAG, "imageUri: " + imageUri.toString());
                Bitmap bitmap = BitmapFactory.decodeStream(is);
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
                        res.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        InputStream resIs = new ByteArrayInputStream(stream.toByteArray());
                        Log.e(TAG, "mHandlerThread mHandlerThread Thread id: " + Thread.currentThread().getName());
                        wallpaperManager.setStream(resIs, null, false, reqCode);
                        Log.e(TAG, "systemWallpaperId: " + wallpaperManager.getWallpaperId(WallpaperManager.FLAG_SYSTEM) + ", lockWallpaperId: " + wallpaperManager.getWallpaperId(WallpaperManager.FLAG_LOCK));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void changeWallpaper(Bitmap bitmap) {
        int bw = bitmap.getWidth();
        int bh = bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(0.5f, 0.5f);
        Bitmap rs = Bitmap.createBitmap(bitmap, 0, 0, bw, bh, matrix, true);
        int aw = rs.getWidth();
        int ah = rs.getHeight();
        wallpaperManager.suggestDesiredDimensions(aw, ah);
        try {
            wallpaperManager.setBitmap(rs);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mHandlerThread.postTask(()->{
            mHandlerThread.postTask(() -> {
                try {
                    wallpaperManager.setBitmap(rs);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    private class MyAsyncTask extends AsyncTask<Bitmap, Integer, Void> {

        @Override
        protected Void doInBackground(Bitmap... bitmaps) {
            Bitmap bitmap = bitmaps[0];
            int bw = bitmap.getWidth();
            int bh = bitmap.getHeight();
            Matrix matrix = new Matrix();
            matrix.postScale(0.5f, 0.5f);
            Bitmap res = Bitmap.createBitmap(bitmap, 0, 0, bw, bh, matrix, true);
            int aw = res.getWidth();
            int ah = res.getHeight();
            //wallpaperManager.suggestDesiredDimensions(aw, ah);*/
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            res.compress(Bitmap.CompressFormat.JPEG, 90, stream);
            InputStream is = new ByteArrayInputStream(stream.toByteArray());
            try {
                wallpaperManager.setStream(is);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //return bitmap;
            return null;
        }

        @Override
        protected void onPostExecute(Void voidObject) {

        }
    }

}
