package com.pixispace.elocauth.data;

import android.content.Context;
import android.net.Uri;

import androidx.core.content.FileProvider;

import com.pixispace.elocauth.ElocAuthApp;

import java.io.File;

public class DataHelper {
    private DataHelper() {
    }

    public static Uri getUriForFile(File file) {
        Context context = ElocAuthApp.getInstance();
        return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
    }

    public static File getTempFile() {
        Context context = ElocAuthApp.getInstance();
        return new File(context.getCacheDir(), String.valueOf(System.currentTimeMillis()));
    }

    public static Uri getTempFileUri() {
        return getUriForFile(getTempFile());
    }
}
