/**
 * Класс провайдера виджета для приложения DVAdmin, отвечающий за создание, обновление и обработку
 * событий виджета. Виджет отображает данные о посещаемости сайта dimonvideo.ru, TIC или статистике
 * за день, получаемые через API. Каждый виджет имеет индивидуальные настройки типа данных,
 * выбираемые при его установке.
 */
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
            updateAppWidget(context, appWidgetId, true);
            Data data = new Data.Builder()
                    .putInt(WidgetUpdateWorker.KEY_WIDGET_ID, appWidgetId)
                    .build();
            OneTimeWorkRequest updateRequest = new OneTimeWorkRequest.Builder(WidgetUpdateWorker.class)
                    .setInputData(data)
                    .addTag(WidgetUpdateWorker.TAG)
                    .build();
            WorkManager.getInstance(context).enqueue(updateRequest);
        }
    }

    public void updateAppWidget(Context context, int appWidgetId, boolean showProgress) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String dataType = prefs.getString("widget_data_type_" + appWidgetId, "visitors");
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        String text = context.getString(R.string.visitors_widget);
        String res = "";
        String date = "";
        int showDate = getShowDate(context, appWidgetId);

        if (Objects.equals(dataType, "tic")) {
            text = context.getString(R.string.tic_widget);
        } else if (Objects.equals(dataType, "today")) {
            text = context.getString(R.string.today);
        }

        String cachedData = prefs.getString("widget_data_" + appWidgetId, null);
        if (cachedData != null) {
            try {
                JSONObject jsonObject = new JSONObject(cachedData);
                String countTic = jsonObject.getString("tic");
                String countVisitors = jsonObject.getString("visitors");
                String today = jsonObject.getString("today");
                date = jsonObject.getString("date");

                res = countVisitors;
                if (Objects.equals(dataType, "tic")) {
                    res = countTic;
                } else if (Objects.equals(dataType, "today")) {
                    res = today.replace("->", " ");
                }
            } catch (JSONException e) {
                Log.e(Config.TAG, "Ошибка парсинга кэшированных данных в WidgetProvider", e);
                res = context.getString(R.string.error_network);
            }
        }

        updateWidgetViews(context, views, appWidgetId, text, res, date, showDate, showProgress && res.isEmpty());
        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views);
    }

    public void processResponse(Context context, String res, String date, int appWidgetId, int showDate) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String dataType = prefs.getString("widget_data_type_" + appWidgetId, "visitors");
        String text = context.getString(R.string.visitors_widget);

        if (Objects.equals(dataType, "tic")) {
            text = context.getString(R.string.tic_widget);
        } else if (Objects.equals(dataType, "today")) {
            text = context.getString(R.string.today);
            res = res.replace("->", " ");
        }

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        updateWidgetViews(context, views, appWidgetId, text, res, date, showDate, false);
        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views);
    }

    private void updateWidgetViews(Context context, RemoteViews views, int appWidgetId, String text, String res, String date, int showDate, boolean showProgress) {
        views.setTextViewText(R.id.text, text);
        views.setTextViewText(R.id.widget_list, res);
        views.setTextViewText(R.id.date, date);
        views.setViewVisibility(R.id.date, showDate == 1 ? View.VISIBLE : View.GONE);
        views.setViewVisibility(R.id.progress_bar, showProgress ? View.VISIBLE : View.GONE);
        views.setOnClickPendingIntent(R.id.refresh_button, getPendingSelfIntent(context, appWidgetId));

        Intent intent = new Intent(context, MainActivity.class);
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, flags);

        views.setOnClickPendingIntent(R.id.text, pendingIntent);
        views.setOnClickPendingIntent(R.id.widget_list, pendingIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (ACTION_UPDATE.equals(action)) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (appWidgetId != -1) {
                Data data = new Data.Builder()
                        .putInt(WidgetUpdateWorker.KEY_WIDGET_ID, appWidgetId)
                        .build();
                OneTimeWorkRequest updateRequest = new OneTimeWorkRequest.Builder(WidgetUpdateWorker.class)
                        .setInputData(data)
                        .addTag(WidgetUpdateWorker.TAG)
                        .build();
                WorkManager.getInstance(context).enqueue(updateRequest);
                updateAppWidget(context, appWidgetId, true);
            } else {
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                ComponentName myWidget = new ComponentName(context, WidgetProvider.class);
                int[] widgetIds = appWidgetManager.getAppWidgetIds(myWidget);
                onUpdate(context, appWidgetManager, widgetIds);
            }
        } else if (AppWidgetManager.ACTION_APPWIDGET_ENABLED.equals(action)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName myWidget = new ComponentName(context, WidgetProvider.class);
            int[] widgetIds = appWidgetManager.getAppWidgetIds(myWidget);
            onUpdate(context, appWidgetManager, widgetIds);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        updateAppWidget(context, appWidgetId, false);
        Log.i(Config.TAG, "Виджет изменён: " + newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH));
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        for (int appWidgetId : appWidgetIds) {
            editor.remove("widget_data_type_" + appWidgetId);
            editor.remove("widget_data_" + appWidgetId);
            editor.remove("last_update_" + appWidgetId);
        }
        editor.apply();
    }

    private int getShowDate(Context context, int appWidgetId) {
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        Bundle options = widgetManager.getAppWidgetOptions(appWidgetId);
        int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
        return width > 100 ? 1 : 0;
    }

    private PendingIntent getPendingSelfIntent(Context context, int appWidgetId) {
        Intent intent = new Intent(context, WidgetProvider.class);
        intent.setAction(ACTION_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT;
        return PendingIntent.getBroadcast(context, appWidgetId, intent, flags);
    }
}