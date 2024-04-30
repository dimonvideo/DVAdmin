package dv.dimonvideo.dvadmin.util;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

import com.huawei.hms.push.HmsMessageService;
import com.huawei.hms.push.RemoteMessage;

import dv.dimonvideo.dvadmin.Config;

public class CustomPushService extends HmsMessageService {
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.i(Config.TAG, "receive token:" + token);
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);

        try {

            Log.i(Config.TAG, "getSignal: " + message.getData());


            Intent intent = new Intent(this, WidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

            int[] ids = AppWidgetManager.getInstance(getApplication())
                    .getAppWidgetIds(new ComponentName(getApplication(), WidgetProvider.class));
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            sendBroadcast(intent);
        } catch (Exception ignored) {

        }

    }
}