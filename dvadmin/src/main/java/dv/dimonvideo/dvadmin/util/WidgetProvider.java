package dv.dimonvideo.dvadmin.util;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import dv.dimonvideo.dvadmin.BuildConfig;
import dv.dimonvideo.dvadmin.Config;
import dv.dimonvideo.dvadmin.MainActivity;
import dv.dimonvideo.dvadmin.R;

public class WidgetProvider extends AppWidgetProvider {
    String countDate, countTic, countVisitors, today, text;
    String hostUrl = Config.HOST_URL;
    String countUrl = Config.COUNT_URL;
    private static final String ACTION_UPDATE = "android.appwidget.action.APPWIDGET_UPDATE";
    int showDate = 0;

    @Override
    public void onUpdate(Context context, AppWidgetManager widgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            Bundle options = widgetManager.getAppWidgetOptions(appWidgetId);
            int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
            if (width > 100) showDate = 1;
            updateAppWidget(context, appWidgetId);
        }

    }

    public void updateAppWidget(Context context, int appWidgetId) {
        sendRequest(context, appWidgetId);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName myWidget = new ComponentName(context.getPackageName(), WidgetProvider.class.getName());
        int[] widgetIds = appWidgetManager.getAppWidgetIds(myWidget);
        String action = intent.getAction();
        if (action != null && action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            this.onUpdate(context, AppWidgetManager.getInstance(context), widgetIds);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        Log.i("---", "App Widget Enabled");

    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        Log.i("---", "App Widget Disabled");

    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        int width = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
        if (width > 100) showDate = 1;
        updateAppWidget(context, appWidgetId);
        Log.i("---", "Widget Resized: " + width);
    }

    public void sendRequest(Context context, int appWidgetId) {

        String is_widget = AppController.getInstance(context).isWidget();

        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, countUrl,
                response -> {
                    try {

                        JSONObject jsonObject;
                        jsonObject = new JSONObject(response);

                        countTic = jsonObject.getString("tic");
                        countVisitors = jsonObject.getString("visitors");
                        today = jsonObject.getString("today");
                        countDate = jsonObject.getString("date");

                        processResponse(context, countVisitors, countDate, appWidgetId, showDate);
                        if (Objects.equals(is_widget, "tic"))
                            processResponse(context, countTic, countDate, appWidgetId, showDate);
                        if ((Objects.equals(is_widget, "today")) && (BuildConfig.FLAVOR.equals("DVAdminPro")))
                            processResponse(context, today, countDate, appWidgetId, showDate);

                    } catch (JSONException e) {
                        Toast.makeText(context, context.getString(R.string.error_network_timeout), Toast.LENGTH_LONG).show();
                    }
                }, error -> {

        });

        queue.add(stringRequest);

    }

    public void processResponse(Context context, String res, String date, int appWidgetId, int showDateSwitch) {

        String is_widget = AppController.getInstance(context).isWidget();

        text = context.getString(R.string.visitors_widget);
        if (Objects.equals(is_widget, "tic")) text = context.getString(R.string.tic_widget);
        if ((Objects.equals(is_widget, "today")) && (BuildConfig.FLAVOR.equals("DVAdminPro"))) {
            text = context.getString(R.string.today);
            res = res.replace("->", " ");
        }

        Log.i("---", "showDateSwitch: " + showDateSwitch);
        Log.i("---", "showDateSwitch: " + showDateSwitch);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        views.setTextViewText(R.id.widget_list, res);
        views.setTextViewText(R.id.text, text);
        if (showDateSwitch == 0) views.setViewVisibility(R.id.date, View.GONE);
        else
            views.setViewVisibility(R.id.date, View.VISIBLE);

        if (BuildConfig.FLAVOR.equals("DVAdminPro"))
            views.setViewVisibility(R.id.date, View.VISIBLE);

        views.setTextViewText(R.id.date, date);
        views.setOnClickPendingIntent(R.id.refresh_button, getPendingSelfIntent(context));

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        } else
            pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        views.setOnClickPendingIntent(R.id.text, pendingIntent);
        views.setOnClickPendingIntent(R.id.widget_list, pendingIntent);

        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views);
    }

    private PendingIntent getPendingSelfIntent(Context context) {

        Intent intent = new Intent(context, getClass());
        intent.setAction(WidgetProvider.ACTION_UPDATE);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

    }
}