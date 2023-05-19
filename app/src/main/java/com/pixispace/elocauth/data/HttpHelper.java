package com.pixispace.elocauth.data;

import android.graphics.Bitmap;

import androidx.annotation.Nullable;
import androidx.collection.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.pixispace.elocauth.ElocAuthApp;

public class HttpHelper {
    private final ImageLoader imageLoader;

    private static HttpHelper instance;

    public static HttpHelper getInstance() {
        if (instance == null) {
            instance = new HttpHelper();
        }
        return instance;
    }

    private HttpHelper() {
        ImageLoader.ImageCache cache = new ImageLoader.ImageCache() {
            // 100MB cache size
            private final LruCache<String, Bitmap> bitmapCache = new LruCache<>(100 * 1024 * 1024);

            @Nullable
            @Override
            public Bitmap getBitmap(String url) {
                return bitmapCache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                bitmapCache.put(url, bitmap);
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(ElocAuthApp.getInstance());
        imageLoader = new ImageLoader(requestQueue, cache);
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }
}
