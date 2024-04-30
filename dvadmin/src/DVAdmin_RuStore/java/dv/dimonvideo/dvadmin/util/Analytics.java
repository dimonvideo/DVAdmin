package dv.dimonvideo.dvadmin.util;

import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import dv.dimonvideo.dvadmin.Config;

public class Analytics {

    public static void init(Context context) {

        final boolean is_notify = AppController.getInstance().is_notify();

        try {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApplicationId("1:50549051988:android:a46a6e539a88fde4e7d3c1") // Required for Analytics.
                    .setProjectId("dvadmin-5a6d2") // Required for Firebase Installations.
                    .build();
            FirebaseApp.initializeApp(context, options, "DVAdmin");

            if (is_notify) {

                Log.i(Config.TAG, "subscribeToTopic");

                FirebaseMessaging.getInstance().subscribeToTopic("DVAdmin");
            } else FirebaseMessaging.getInstance().unsubscribeFromTopic("DVAdmin");
        } catch (Throwable ignored) {
        }

    }

}