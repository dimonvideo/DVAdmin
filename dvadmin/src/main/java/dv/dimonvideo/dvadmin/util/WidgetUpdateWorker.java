/**
 * Рабочий процесс для периодического или разового обновления виджетов приложения DVAdmin.
 * Выполняет сетевые запросы через API для получения данных о посещаемости, TIC или статистике
 * за день, сохраняет их в кэш и обновляет виджет через {@link WidgetProvider}.
 */
package dv.dimonvideo.dvadmin.util;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;

import java.util.Objects;

import dv.dimonvideo.dvadmin.Config;
import dv.dimonvideo.dvadmin.R;
import dv.dimonvideo.dvadmin.model.ApiResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Наследует {@link Worker} для выполнения фоновых задач обновления виджетов с использованием
 * {@link .WorkManager}.
 */
public class WidgetUpdateWorker extends Worker {
    public static final String TAG = "WidgetUpdateWorker";
    public static final String KEY_WIDGET_ID = "widget_id";
    private final AppController controller;
    private static final int MAX_RETRIES = 3;

    public WidgetUpdateWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        controller = AppController.getInstance();
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName widgetComponent = new ComponentName(context, WidgetProvider.class);
        int[] widgetIds;

        int widgetId = getInputData().getInt(KEY_WIDGET_ID, -1);
        if (widgetId != -1) {
            widgetIds = new int[]{widgetId};
        } else {
            widgetIds = appWidgetManager.getAppWidgetIds(widgetComponent);
        }

        for (int appWidgetId : widgetIds) {

            fetchDataAndUpdateWidget(context, appWidgetId, 0);
        }

        return Result.success();
    }

    private void fetchDataAndUpdateWidget(Context context, int appWidgetId, int retryCount) {
        ApiService apiService = controller.getApiService();

        apiService.getCounts().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    prefs.edit()
                            .putString("widget_data_" + appWidgetId, new Gson().toJson(apiResponse))
                            .putLong("last_update_" + appWidgetId, System.currentTimeMillis())
                            .apply();

                    updateWidgetFromCache(context, appWidgetId, apiResponse.getTic(), apiResponse.getVisitors(), apiResponse.getToday(), apiResponse.getDate());
                } else {
                    Log.e(Config.TAG, "Ошибка ответа сервера для виджета " + appWidgetId + ": " + response.message());
                    retryOrFail(context, appWidgetId, retryCount);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                retryOrFail(context, appWidgetId, retryCount);
            }
        });
    }

    private void retryOrFail(Context context, int appWidgetId, int retryCount) {
        if (retryCount < MAX_RETRIES) {
            fetchDataAndUpdateWidget(context, appWidgetId, retryCount + 1);
        } else {
            WidgetProvider widgetProvider = new WidgetProvider();
            widgetProvider.updateAppWidget(context, appWidgetId, false);
        }
    }

    private void updateWidgetFromCache(Context context, int appWidgetId, String countTic, String countVisitors, String today, String countDate) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String dataType = prefs.getString("widget_data_type_" + appWidgetId, "visitors");
        String res = countVisitors;

        if (Objects.equals(dataType, "tic")) {
            res = countTic;
        } else if (Objects.equals(dataType, "today")) {
            res = today.replace("->", " ");
        }

        if (res == null || res.isEmpty()) {
            res = context.getString(R.string.error_network);
        }

        WidgetProvider widgetProvider = new WidgetProvider();
        widgetProvider.processResponse(context, res, countDate, appWidgetId, getShowDate(context, appWidgetId));
    }

    private int getShowDate(Context context, int appWidgetId) {
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        Bundle options = widgetManager.getAppWidgetOptions(appWidgetId);
        int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
        return width > 100 ? 1 : 0;
    }

}