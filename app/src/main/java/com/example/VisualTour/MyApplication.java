package com.example.VisualTour;

import android.app.Application;

import com.mapbox.vision.VisionManager;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        VisionManager.init(this, getString(R.string.access_token));
    }
}
