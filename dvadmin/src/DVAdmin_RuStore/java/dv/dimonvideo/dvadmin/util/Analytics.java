/**
 * Утилитный класс для управления подпиской на push-уведомления через Firebase Cloud Messaging
 * (FCM) в приложении DVAdmin. Выполняет подписку или отписку от темы уведомлений в зависимости
 * от пользовательских настроек.
 */
package dv.dimonvideo.dvadmin.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

import dv.dimonvideo.dvadmin.Config;

/**
 * Управляет подпиской на FCM-тему для получения уведомлений.
 */
public class Analytics {
    /** Название FCM-темы для подписки. */
    private static final String FIREBASE_APP_NAME = "DVAdmin";

    /**
     * Инициализирует подписку на FCM-тему в зависимости от настройки уведомлений в
     * {@link AppController}. Подписывает или отписывает приложение от темы "DVAdmin".
     *
     * @param context Контекст приложения.
     */
    public static void init(Context context) {
        // Проверяем, инициализировано ли приложение Firebase
        Log.d(Config.TAG, "FirebaseApp already initialized");

        // Проверяем, включены ли уведомления
        AppController controller = AppController.getInstance();
        final boolean isNotify = controller.is_notify();
        FirebaseMessaging messaging = FirebaseMessaging.getInstance();

        if (isNotify) {
            messaging.subscribeToTopic(FIREBASE_APP_NAME)
                    .addOnCompleteListener(task -> {
                        String msg = task.isSuccessful() ? "Subscribed to DVAdmin" : "Subscribe failed";
                        Log.d(Config.TAG, msg);
                        // Toast.makeText(context.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
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