package com.pixispace.elocauth.activities;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.pixispace.elocauth.callbacks.VoidCallback;
import com.pixispace.elocauth.data.UserAccountViewModel;

public class ActivityHelper {

    private ActivityHelper() {
    }

    public static void open(AppCompatActivity activity, Class<?> target, boolean finish) {
        if (activity != null) {
            Intent intent = new Intent(activity, target);
            activity.startActivity(intent);
            if (finish) {
                activity.finish();
            }
        }
    }

    public static void showModalAlert(Context context, String title, String message) {
        if (message == null) {
            message = "";
        }
        message = message.trim();
        if ((context == null) || message.isEmpty()) {
            return;
        }
        new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, ((dialog, w) -> dialog.dismiss()))
                .show();
    }

    public static void showModalAlert(Context context, String title, String message, VoidCallback callback) {
        if (message == null) {
            message = "";
        }
        message = message.trim();
        if ((context == null) || message.isEmpty()) {
            return;
        }
        new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, ((dialog, w) -> {
                    dialog.dismiss();
                    if (callback != null) {
                        callback.handler();
                    }
                }))
                .show();
    }

    public static void hideKeyboard(AppCompatActivity activity) {
        if (activity != null) {
            View view = activity.getCurrentFocus();
            if (view != null) {
                IBinder binder = view.getWindowToken();
                if (binder != null) {
                    InputMethodManager manager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (manager != null) {
                        manager.hideSoftInputFromWindow(binder, 0);
                    }
                }
            }
        }
    }
}
