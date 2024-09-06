package dv.dimonvideo.dvadmin.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import dv.dimonvideo.dvadmin.Config;
import dv.dimonvideo.dvadmin.MainActivity;

public class Analytics {

    public static void init(Context context) {

        final boolean is_notify = AppController.getInstance(context).is_notify();

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApplicationId("1:50549051988:android:a46a6e539a88fde4e7d3c1") // Required for Analytics.
                .setProjectId("dvadmin-5a6d2") // Required for Firebase Installations.
                .build();
        FirebaseApp.initializeApp(context, options, "DVAdmin");

        if (is_notify) {
            FirebaseMessaging.getInstance().subscribeToTopic("DVAdmin")
                    .addOnCompleteListener(task -> {
                        String msg = "Subscribed";
                        if (!task.isSuccessful()) {
                            msg = "Subscribe failed";
                        }
                        Log.d(Config.TAG, msg);
                        Toast.makeText(context.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    });
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("DVAdmin")
                    .addOnCompleteListener(task -> {
                        String msg = "unSubscribed";
                        if (!task.isSuccessful()) {
                            msg = "unSubscribe failed";
                        }
                        Log.d(Config.TAG, msg);
                    });
        }
    }

}