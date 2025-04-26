package dv.dimonvideo.dvadmin.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

import dv.dimonvideo.dvadmin.Config;

public class Analytics {

    private static final String FIREBASE_APP_NAME = "DVAdmin";

    public static void init(Context context) {
        // Проверяем, инициализировано ли приложение Firebase
        Log.d(Config.TAG, "FirebaseApp already initialized");

        // Проверяем, включены ли уведомления
        final boolean isNotify = AppController.getInstance(context).is_notify();
        FirebaseMessaging messaging = FirebaseMessaging.getInstance();

        if (isNotify) {
            messaging.subscribeToTopic(FIREBASE_APP_NAME)
                    .addOnCompleteListener(task -> {
                        String msg = task.isSuccessful() ? "Subscribed to DVAdmin" : "Subscribe failed";
                        Log.d(Config.TAG, msg);
                     //   Toast.makeText(context.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    });
        } else {
            messaging.unsubscribeFromTopic(FIREBASE_APP_NAME)
                    .addOnCompleteListener(task -> {
                        String msg = task.isSuccessful() ? "Unsubscribed from DVAdmin" : "Unsubscribe failed";
                        Log.d(Config.TAG, msg);
                        Toast.makeText(context.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    });
        }
    }
}