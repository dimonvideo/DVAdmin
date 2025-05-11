/**
 * Активность для настройки типа данных виджета DVAdmin при его добавлении. Позволяет выбрать,
 * какую информацию отображать в виджете (посетители, TIC или статистика за день), и сохраняет
 * выбор для конкретного экземпляра виджета.
 */
package dv.dimonvideo.dvadmin;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.button.MaterialButton;

import dv.dimonvideo.dvadmin.util.WidgetUpdateWorker;

public class WidgetConfigActivity extends AppCompatActivity {
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private Spinner dataTypeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_config);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            Log.d(Config.TAG, "Received appWidgetId: " + appWidgetId);
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.e(Config.TAG, "Invalid appWidgetId, finishing activity");
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        dataTypeSpinner = findViewById(R.id.data_type_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.widget_list,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(R.layout.spinner_item);
        dataTypeSpinner.setAdapter(adapter);

        MaterialButton saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> saveWidgetConfig());
    }

    private void saveWidgetConfig() {
        String selectedDataType = getResources().getStringArray(R.array.widget_list_values)[dataTypeSpinner.getSelectedItemPosition()];
        Log.d(Config.TAG, "Saved data type: " + selectedDataType + " for widget ID: " + appWidgetId);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putString("widget_data_type_" + appWidgetId, selectedDataType).apply();

        Data data = new Data.Builder()
                .putInt(WidgetUpdateWorker.KEY_WIDGET_ID, appWidgetId)
                .build();
        OneTimeWorkRequest updateRequest = new OneTimeWorkRequest.Builder(WidgetUpdateWorker.class)
                .setInputData(data)
                .addTag(WidgetUpdateWorker.TAG)
                .build();
        WorkManager.getInstance(this).enqueue(updateRequest);

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}