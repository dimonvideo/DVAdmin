package dv.dimonvideo.dvadmin.util;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;

import java.util.Objects;

import dv.dimonvideo.dvadmin.Config;
import dv.dimonvideo.dvadmin.MainActivity;
import dv.dimonvideo.dvadmin.R;
import dv.dimonvideo.dvadmin.model.ApiResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WidgetUpdateWorker extends Worker {
    public static final String TAG = "WidgetUpdateWorker";
    public static final String KEY_WIDGET_ID = "widget_id";

    public WidgetUpdateWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName widgetComponent = new ComponentName(context, WidgetProvider.class);
        int[] widgetIds;

        // Если указан конкретный widgetId, обновляем только его
        int widgetId = getInputData().getInt(KEY_WIDGET_ID, -1);
        if (widgetId != -1) {
            widgetIds = new int[]{widgetId};
        } else {
            widgetIds = appWidgetManager.getAppWidgetIds(widgetComponent);
        }

        for (int appWidgetId : widgetIds) {
            fetchDataAndUpdateWidget(context, appWidgetId);
        }

        return Result.success();
    }

    private void fetchDataAndUpdateWidget(Context context, int appWidgetId) {
        ApiService apiService = AppController.getInstance(context).getApiService();

        apiService.getCounts().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();

                    // Сохранение в кэш
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    prefs.edit()
                            .putString("widget_data_" + appWidgetId, new Gson().toJson(apiResponse))
                            .putLong("last_update_" + appWidgetId, System.currentTimeMillis())
                            .apply();

                    updateWidgetFromCache(context, appWidgetId, apiResponse.getTic(), apiResponse.getVisitors(), apiResponse.getToday(), apiResponse.getDate());
                } else {
                    Log.e(Config.TAG, "Response error: " + response.message());
                    updateWidgetWithError(context, appWidgetId);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                Log.e(Config.TAG, "Network error", t);
                updateWidgetWithError(context, appWidgetId);
            }
        });
    }

    private void updateWidgetFromCache(Context context, int appWidgetId, String countTic, String countVisitors, String today, String countDate) {
        String isWidget = AppController.getInstance(context).isWidget();
        String text = context.getString(R.string.visitors_widget);
        String res = countVisitors;

        if (Objects.equals(isWidget, "tic")) {
            text = context.getString(R.string.tic_widget);
            res = countTic;
        } else if (Objects.equals(isWidget, "today")) {
            text = context.getString(R.string.today);
            res = today.replace("->", " ");
        }

        WidgetProvider widgetProvider = new WidgetProvider();
        widgetProvider.processResponse(context, res, countDate, appWidgetId, getShowDate(context, appWidgetId));
    }

    private void updateWidgetWithError(Context context, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        String isWidget = AppController.getInstance(context).isWidget();
        String text = context.getString(R.string.visitors_widget);

        if (Objects.equals(isWidget, "tic")) {
            text = context.getString(R.string.tic_widget);
        } else if (Objects.equals(isWidget, "today")) {
            text = context.getString(R.string.today);
        }

        views.setTextViewText(R.id.text, text);
        views.setTextViewText(R.id.widget_list, context.getString(R.string.error_network));
        views.setTextViewText(R.id.date, "");
        views.setViewVisibility(R.id.date, View.GONE);
        views.setViewVisibility(R.id.progress_bar, View.GONE); // Скрываем ProgressBar при ошибке
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

    private int getShowDate(Context context, int appWidgetId) {
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        Bundle options = widgetManager.getAppWidgetOptions(appWidgetId);
        int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
        return width > 100 ? 1 : 0;
    }

    private PendingIntent getPendingSelfIntent(Context context, int appWidgetId) {
        Intent intent = new Intent(context, WidgetProvider.class);
        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        return PendingIntent.getBroadcast(context, appWidgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
    }
}