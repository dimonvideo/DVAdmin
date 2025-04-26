package dv.dimonvideo.dvadmin.util;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import dv.dimonvideo.dvadmin.Config;
import dv.dimonvideo.dvadmin.MainActivity;
import dv.dimonvideo.dvadmin.R;

public class WidgetProvider extends AppWidgetProvider {
    public static final String ACTION_UPDATE = "android.appwidget.action.APPWIDGET_UPDATE";

    @Override
    public void onUpdate(Context context, AppWidgetManager widgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            Bundle options = widgetManager.getAppWidgetOptions(appWidgetId);
            int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
            int showDate = width > 100 ? 1 : 0;
            updateAppWidget(context, appWidgetId, showDate, true); // Показываем ProgressBar при инициализации
        }
    }

    public void updateAppWidget(Context context, int appWidgetId, int showDate, boolean showProgress) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        String isWidget = AppController.getInstance(context).isWidget();
        String text = context.getString(R.string.visitors_widget);

        if (Objects.equals(isWidget, "tic")) {
            text = context.getString(R.string.tic_widget);
        } else if (Objects.equals(isWidget, "today")) {
            text = context.getString(R.string.today);
        }

        // Проверяем кэшированные данные
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String cachedData = prefs.getString("widget_data_" + appWidgetId, null);
        String res = "";
        String date = "";

        if (cachedData != null) {
            try {
                JSONObject jsonObject = new JSONObject(cachedData);
                String countTic = jsonObject.getString("tic");
                String countVisitors = jsonObject.getString("visitors");
                String today = jsonObject.getString("today");
                date = jsonObject.getString("date");

                res = countVisitors;
                if (Objects.equals(isWidget, "tic")) {
                    res = countTic;
                } else if (Objects.equals(isWidget, "today")) {
                    res = today.replace("->", " ");
                }
            } catch (JSONException e) {
                Log.e(Config.TAG, "Failed to parse cached data in WidgetProvider", e);
                res = context.getString(R.string.error_network);
            }
        }

        views.setTextViewText(R.id.text, text);
        views.setTextViewText(R.id.widget_list, res);
        views.setTextViewText(R.id.date, date);
        views.setViewVisibility(R.id.date, showDate == 1 ? View.VISIBLE : View.GONE);
        views.setViewVisibility(R.id.progress_bar, showProgress && res.isEmpty() ? View.VISIBLE : View.GONE);
        views.setOnClickPendingIntent(R.id.refresh_button, getPendingSelfIntent(context, appWidgetId));

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ?
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE :
                        PendingIntent.FLAG_UPDATE_CURRENT);

        views.setOnClickPendingIntent(R.id.text, pendingIntent);
        views.setOnClickPendingIntent(R.id.widget_list, pendingIntent);

        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_UPDATE.equals(intent.getAction())) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (appWidgetId != -1) {
                // Запускаем разовое обновление через WorkManager
                Data data = new Data.Builder()
                        .putInt(WidgetUpdateWorker.KEY_WIDGET_ID, appWidgetId)
                        .build();
                OneTimeWorkRequest updateRequest = new OneTimeWorkRequest.Builder(WidgetUpdateWorker.class)
                        .setInputData(data)
                        .addTag(WidgetUpdateWorker.TAG)
                        .build();
                WorkManager.getInstance(context).enqueue(updateRequest);

                // Показываем ProgressBar при нажатии на refresh_button
                Bundle options = AppWidgetManager.getInstance(context).getAppWidgetOptions(appWidgetId);
                int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
                int showDate = width > 100 ? 1 : 0;
                updateAppWidget(context, appWidgetId, showDate, true);
            } else {
                // Обновляем все виджеты
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                ComponentName myWidget = new ComponentName(context, WidgetProvider.class);
                int[] widgetIds = appWidgetManager.getAppWidgetIds(myWidget);
                onUpdate(context, appWidgetManager, widgetIds);
            }
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        int width = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
        int newShowDate = width > 100 ? 1 : 0;
        updateAppWidget(context, appWidgetId, newShowDate, false); // Не показываем ProgressBar при изменении размера
        Log.i(Config.TAG, "Widget Resized: " + width);
    }

    public void processResponse(Context context, String res, String date, int appWidgetId, int showDate) {
        String isWidget = AppController.getInstance(context).isWidget();
        String text = context.getString(R.string.visitors_widget);

        if (Objects.equals(isWidget, "tic")) {
            text = context.getString(R.string.tic_widget);
        } else if (Objects.equals(isWidget, "today")) {
            text = context.getString(R.string.today);
            res = res.replace("->", " ");
        }

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        views.setTextViewText(R.id.widget_list, res);
        views.setTextViewText(R.id.text, text);
        views.setTextViewText(R.id.date, date);
        views.setViewVisibility(R.id.date, showDate == 1 ? View.VISIBLE : View.GONE);
        views.setViewVisibility(R.id.progress_bar, View.GONE); // Скрываем ProgressBar после обновления
        views.setOnClickPendingIntent(R.id.refresh_button, getPendingSelfIntent(context, appWidgetId));

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ?
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE :
                        PendingIntent.FLAG_UPDATE_CURRENT);

        views.setOnClickPendingIntent(R.id.text, pendingIntent);
        views.setOnClickPendingIntent(R.id.widget_list, pendingIntent);

        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views);
    }

    private PendingIntent getPendingSelfIntent(Context context, int appWidgetId) {
        Intent intent = new Intent(context, WidgetProvider.class);
        intent.setAction(ACTION_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        return PendingIntent.getBroadcast(
                context, appWidgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
    }
}