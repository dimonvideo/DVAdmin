package dv.dimonvideo.dvadmin;

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
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class WidgetProvider extends AppWidgetProvider {
    String countDate, countTic, countVisitors, today, text;
    String hostUrl = "https://api.dimonvideo.net";
    String countUrl = hostUrl + "/smart/dvadminapi.php?op=18";
    SharedPreferences sharedPrefs;
    private static final String ACTION_UPDATE = "android.appwidget.action.APPWIDGET_UPDATE";
    private static final String ACTION_OPEN = "android.appwidget.action.APPWIDGET_OPEN";

    @Override
    public void onUpdate(Context context, AppWidgetManager widgetManager, int[] appWidgetIds) {
        for(int appWidgetId : appWidgetIds)
            updateAppWidget(context, appWidgetId);
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
        Log.i("---","App Widget Enabled");

    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        Log.i("---","App Widget Disabled");

    }

    @Override
    public void onAppWidgetOptionsChanged(Context ctx, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(ctx, appWidgetManager, appWidgetId, newOptions);
        Log.i("---","Widget Resized");
    }

    public void sendRequest(Context context, int appWidgetId) {

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String is_widget = sharedPrefs.getString("dvc_widget_list", "visitors");

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

                        processResponse(context, countVisitors, appWidgetId);
                        if (Objects.equals(is_widget, "tic")) processResponse(context, countTic, appWidgetId);
                        if ((Objects.equals(is_widget, "today")) && (BuildConfig.FLAVOR.equals("DVAdminPro")))
                            processResponse(context, today, appWidgetId);

                    } catch (JSONException e) {
                        Toast.makeText(context, context.getString(R.string.error_network_timeout), Toast.LENGTH_LONG).show();
                    }
                }, error -> {

        });

        queue.add(stringRequest);

    }

    public void processResponse(Context context, String res, int appWidgetId) {

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String is_widget = sharedPrefs.getString("dvc_widget_list", "visitors");

        text = context.getString(R.string.visitors_widget);
        if (Objects.equals(is_widget, "tic")) text = context.getString(R.string.tic_widget);
        if ((Objects.equals(is_widget, "today")) && (BuildConfig.FLAVOR.equals("DVAdminPro"))) text = context.getString(R.string.today);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        views.setTextViewText(R.id.widget_list, res);
        views.setTextViewText(R.id.text, text);
        views.setOnClickPendingIntent(R.id.refresh_button, getPendingSelfIntent(context));

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        } else pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        views.setOnClickPendingIntent(R.id.text, pendingIntent);
        views.setOnClickPendingIntent(R.id.widget_list, pendingIntent);

        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views);
    }

    private PendingIntent getPendingSelfIntent(Context context) {

        Intent intent = new Intent(context, getClass());
        intent.setAction(WidgetProvider.ACTION_UPDATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        } else return PendingIntent.getBroadcast(context, 0, intent, 0);
    }
}