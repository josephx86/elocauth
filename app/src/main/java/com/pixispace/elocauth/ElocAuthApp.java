package com.pixispace.elocauth;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class ElocAuthApp extends Application {
    private static ElocAuthApp instance;

    public static ElocAuthApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        FirebaseApp.initializeApp(this);
    }
}
