/**
 * Служба для обработки push-уведомлений через Firebase Cloud Messaging (FCM) в приложении DVAdmin.
 * Обрабатывает входящие сообщения, обновляет виджеты и отправляет уведомления пользователю.
 * Используется в сборке приложения с поддержкой Google Services.
 */
package dv.dimonvideo.dvadmin;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;

import dv.dimonvideo.dvadmin.util.WidgetProvider;

/**
 * Наследует {@link FirebaseMessagingService} для обработки FCM-сообщений и токенов.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    /**
     * Обрабатывает входящее FCM-сообщение, обновляет виджеты приложения и отправляет уведомление
     * с текстом из сообщения.
     *
     * @param remoteMessage Входящее сообщение от FCM.
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Intent intent = new Intent(this, WidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        int[] ids = AppWidgetManager.getInstance(getApplication())
                .getAppWidgetIds(new ComponentName(getApplication(), WidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);

        Log.v(Config.TAG, "!!!! ====== NOTICE ======== !!!! ");

        if (remoteMessage.getNotification() != null) {
            sendNotification(remoteMessage.getNotification().getBody());
        }
    }

    /**
     * Вызывается при обновлении FCM-токена. В текущей реализации не используется.
     *
     * @param token Новый FCM-токен.
     */
    @Override
    public void onNewToken(@NonNull String token) {
    }

    /**
     * Создаёт и отправляет уведомление с указанным текстом. Настраивает канал уведомлений для
     * Android 8.0+ и проверяет разрешение на отправку уведомлений.
     *
     * @param text Текст уведомления.
     */
    private void sendNotification(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // channel for notifications
            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            if (notificationManager != null) {
                List<NotificationChannel> channelList = notificationManager.getNotificationChannels();
                for (int i = 0; channelList != null && i < channelList.size(); i++)
                    notificationManager.deleteNotificationChannel(channelList.get(i).getId());
            }

            String name = getString(R.string.app_name);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(getString(R.string.app_name), name, importance);

            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }

        Intent p_intent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(p_intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.app_name))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentTitle("DVAdmin")
                .setContentText(text)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(0, builder.build());
    }
}