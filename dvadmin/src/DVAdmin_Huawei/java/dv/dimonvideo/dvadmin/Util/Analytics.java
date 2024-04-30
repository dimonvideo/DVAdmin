package dv.dimonvideo.dvadmin.util;

import android.content.Context;
import android.util.Log;

import com.huawei.agconnect.AGConnectInstance;
import com.huawei.hms.analytics.HiAnalytics;
import com.huawei.hms.analytics.HiAnalyticsInstance;
import com.huawei.hms.analytics.HiAnalyticsTools;
import com.huawei.hms.push.HmsMessaging;

import dv.dimonvideo.dvadmin.Config;

public class Analytics {
    static HiAnalyticsInstance instance;

    public static void init(Context context) {
        HiAnalyticsTools.enableLog();
        instance = HiAnalytics.getInstance(context);
        instance.setUserProfile("dv", "hms");
        if (AGConnectInstance.getInstance() == null) {
            AGConnectInstance.initialize(context);
        }

        try {
            // Subscribe to a topic.
            HmsMessaging.getInstance(context).subscribe("DVAdmin")
                    .addOnCompleteListener(task -> {
                        // Obtain the topic subscription result.
                        if (task.isSuccessful()) {
                            Log.i(Config.TAG, "subscribe topic successfully");
                        } else {
                            Log.e(Config.TAG, "subscribe topic failed, return value is " + task.getException().getMessage());
                        }
                    });
        } catch (Exception e) {
            Log.e(Config.TAG, "subscribe failed, catch exception : " + e.getMessage());
        }

    }

}