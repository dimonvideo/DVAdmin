/**
 * Активность для управления настройками приложения DVAdmin. Позволяет пользователю изменять тему
 * интерфейса, масштаб шрифта и тип виджета, а также обновлять соответствующие компоненты приложения
 * (например, виджеты). Реализует двойное нажатие кнопки "Назад" для выхода с подтверждением.
 */
package dv.dimonvideo.dvadmin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.Objects;

import dv.dimonvideo.dvadmin.databinding.SettingsActivityBinding;

/**
 * Наследует {@link AppCompatActivity} для отображения экрана настроек и управления их изменениями.
 */
public class SettingsActivity extends AppCompatActivity {
    /** Флаг для отслеживания двойного нажатия кнопки "Назад". */
    private boolean doubleBackToExitPressedOnce = false;

    /**
     * Инициализирует активность, настраивает привязку макета, панель действий и фрагмент настроек.
     * Регистрирует обработчик двойного нажатия кнопки "Назад".
     *
     * @param savedInstanceState Сохранённое состояние активности.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adjustFontScale(getResources().getConfiguration());
        SettingsActivityBinding binding = SettingsActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();

        OnBackPressedCallback onBackPressedCallback = getOnBackPressedCallback();
        OnBackPressedDispatcher onBackPressedDispatcher = getOnBackPressedDispatcher();
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback);
    }

    /**
     * Создаёт обработчик для двойного нажатия кнопки "Назад". При первом нажатии показывает
     * уведомление, при втором завершает активность с результатом.
     *
     * @return Объект {@link OnBackPressedCallback} для обработки нажатий.
     */
    @NonNull
    private OnBackPressedCallback getOnBackPressedCallback() {
        return new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    // Устанавливаем результат и завершаем активность
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    doubleBackToExitPressedOnce = true;
                    Toast.makeText(getApplicationContext(), getString(R.string.press_twice), Toast.LENGTH_SHORT).show();
                    new Handler(Looper.getMainLooper()).postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
                }
            }
        };
    }

    /**
     * Фрагмент для отображения и управления настройками приложения. Обрабатывает изменения темы,
     * масштаба шрифта и типа виджета.
     */
    public static class SettingsFragment extends PreferenceFragmentCompat {
        /**
         * Инициализирует настройки из XML-ресурса и настраивает слушатели изменений для тем,
         * масштаба шрифта и типа виджета.
         *
         * @param savedInstanceState Сохранённое состояние фрагмента.
         * @param rootKey            Ключ корневой настройки.
         */
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            Preference dvc_theme = findPreference("dvc_theme_list");
            assert dvc_theme != null;
            dvc_theme.setOnPreferenceChangeListener((preference, newValue) -> {
                if (newValue.equals("true")) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                else if (newValue.equals("system")) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                return true;
            });

            Preference dvc_scale = findPreference("dvc_scale");
            assert dvc_scale != null;
            dvc_scale.setOnPreferenceChangeListener((preference, newValue) -> {
                Toast.makeText(requireContext(), requireContext().getString(R.string.restart_app), Toast.LENGTH_LONG).show();
                requireActivity().recreate();
                return true;
            });

        }
    }

    /**
     * Настраивает масштаб шрифта на основе пользовательских настроек, обновляя конфигурацию
     * ресурсов приложения.
     *
     * @param configuration Конфигурация ресурсов приложения.
     */
    private void adjustFontScale(Configuration configuration) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        configuration.fontScale = Float.parseFloat(Objects.requireNonNull(sharedPrefs.getString("dvc_scale", "1.0f")));
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        assert wm != null;
        wm.getDefaultDisplay().getMetrics(metrics);
        metrics.scaledDensity = configuration.fontScale * metrics.density;
        getBaseContext().getResources().updateConfiguration(configuration, metrics);
    }
}