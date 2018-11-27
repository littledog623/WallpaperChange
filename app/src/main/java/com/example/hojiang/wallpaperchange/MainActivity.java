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

    private static final int GALLERY_REQUEST_BOTH = 3;
    private static final int GALLERY_REQUEST_SYSTEM = 1;
    private static final int GALLERY_REQUEST_LOCK = 2;

    private MyWallpaperManager myWallpaperManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myWallpaperManager = MyWallpaperManager.getInstance();
        myWallpaperManager.init(this);
        changeBothWallpaperButton = findViewById(R.id.change_both_wallpaper_btn);
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
        //BingWallpaperDownloadService.start(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyWallpaperManager.getInstance().startAlarm(this, 5000);
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
                myWallpaperManager.setWallpaper(bitmap, reqCode);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
