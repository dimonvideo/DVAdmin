/**
 * Класс приложения DVAdmin, управляющий глобальными настройками, темой интерфейса, доступом к API
 * и пользовательскими предпочтениями. Предоставляет синглтон для доступа к общим ресурсам, таким
 * как {@link SharedPreferences} и {@link ApiService}, а также методы для проверки настроек
 * модерации и виджетов.
 */
package dv.dimonvideo.dvadmin.util;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import dv.dimonvideo.dvadmin.Config;

/**
 * Наследует {@link Application} для инициализации приложения и управления глобальными настройками.
 */
public class AppController extends Application {
    /** SharedPreferences для хранения пользовательских настроек. */
    private SharedPreferences sharedPrefs;

    /** Экземпляр синглтона {@link AppController}. */
    private static AppController instance;

    /** Ключ для хранения темы интерфейса в настройках. */
    private static final String KEY_THEME = "dvc_theme_list";

    /** Значение темы по умолчанию (тёмная тема). */
    private static final String DEFAULT_THEME = "true";

    /** Ключ для хранения типа виджета в настройках. */
    private static final String KEY_WIDGET = "dvc_widget_list";

    /** Значение типа виджета по умолчанию (посетители). */
    private static final String DEFAULT_WIDGET = "visitors";

    /**
     * Инициализирует приложение, создаёт синглтон, настраивает SharedPreferences и применяет тему
     * интерфейса. Регистрирует слушатель изменений настроек для динамического обновления темы.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPrefs.registerOnSharedPreferenceChangeListener((prefs, key) -> {
            if (KEY_THEME.equals(key)) {
                applyTheme();
                Log.d(Config.TAG, "Тема изменена через SharedPreferences: " + getThemeAdmin());
            }
        });
        applyTheme();
        Log.d(Config.TAG, "Тема применена при запуске: " + getThemeAdmin());
    }

    /**
     * Применяет тему интерфейса на основе пользовательских настроек, используя
     * {@link AppCompatDelegate} для переключения между тёмной, светлой или системной темой.
     */
    private void applyTheme() {
        String theme = getThemeAdmin();
        if ("true".equals(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if ("system".equals(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    /**
     * Возвращает экземпляр синглтона {@link AppController}.
     *
     * @return Экземпляр {@link AppController}.
     */
    public static AppController getInstance() {
        return instance;
    }

    /**
     * Возвращает экземпляр {@link ApiService} для выполнения запросов к API сайта dimonvideo.ru.
     *
     * @return Экземпляр {@link ApiService}, созданный через {@link RetrofitClient}.
     */
    public ApiService getApiService() {
        return RetrofitClient.getInstance().create(ApiService.class);
    }

    /**
     * Возвращает объект {@link SharedPreferences} для доступа к пользовательским настройкам.
     * Инициализирует SharedPreferences, если они ещё не созданы.
     *
     * @return Объект {@link SharedPreferences}.
     */
    public SharedPreferences getSharedPreferences() {
        if (sharedPrefs == null) {
            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        }
        return sharedPrefs;
    }

    /**
     * Возвращает текущую тему интерфейса из настроек.
     *
     * @return Значение темы ("true" для тёмной, "system" для системной, иначе светлая).
     */
    public String getThemeAdmin() {
        return getSharedPreferences().getString(KEY_THEME, DEFAULT_THEME);
    }

    /**
     * Возвращает тип виджета, выбранный в настройках.
     *
     * @return Тип виджета ("visitors", "tic" или "today").
     */
    public String isWidget() {
        return getSharedPreferences().getString(KEY_WIDGET, DEFAULT_WIDGET);
    }

    /**
     * Проверяет, включена ли модерация категории загрузок в настройках.
     *
     * @return true, если модерация загрузок включена, иначе false.
     */
    public boolean is_uploader() {
        return getSharedPreferences().getBoolean("uploader", true);
    }

    /**
     * Проверяет, включена ли модерация видеоматериалов в настройках.
     *
     * @return true, если модерация видеоматериалов включена, иначе false.
     */
    public boolean is_vuploader() {
        return getSharedPreferences().getBoolean("vuploader", true);
    }

    /**
     * Проверяет, включена ли модерация музыкальных материалов в настройках.
     *
     * @return true, если модерация музыки включена, иначе false.
     */
    public boolean is_muzon() {
        return getSharedPreferences().getBoolean("muzon", true);
    }

    /**
     * Проверяет, включена ли модерация пользовательских новостей в настройках.
     *
     * @return true, если модерация новостей включена, иначе false.
     */
    public boolean is_usernews() {
        return getSharedPreferences().getBoolean("usernews", true);
    }

    /**
     * Проверяет, включена ли модерация галереи в настройках.
     *
     * @return true, если модерация галереи включена, иначе false.
     */
    public boolean is_gallery() {
        return getSharedPreferences().getBoolean("gallery", true);
    }

    /**
     * Проверяет, включена ли модерация устройств в настройках.
     *
     * @return true, если модерация устройств включена, иначе false.
     */
    public boolean is_devices() {
        return getSharedPreferences().getBoolean("devices", true);
    }

    /**
     * Проверяет, включена ли модерация форума в настройках.
     *
     * @return true, если модерация форума включена, иначе false.
     */
    public boolean is_forum() {
        return getSharedPreferences().getBoolean("forum", true);
    }

    /**
     * Проверяет, включена ли обработка жалоб на файлы в настройках.
     *
     * @return true, если обработка жалоб на файлы включена, иначе false.
     */
    public boolean is_abuse_file() {
        return getSharedPreferences().getBoolean("abuse_file", true);
    }

    /**
     * Проверяет, включена ли обработка жалоб на сообщения форума в настройках.
     *
     * @return true, если обработка жалоб на форум включена, иначе false.
     */
    public boolean is_abuse_forum() {
        return getSharedPreferences().getBoolean("abuse_forum", true);
    }

    /**
     * Проверяет, включена ли проверка свободного пространства в настройках.
     *
     * @return true, если проверка пространства включена, иначе false.
     */
    public boolean is_space() {
        return getSharedPreferences().getBoolean("space", true);
    }

    /**
     * Проверяет, включено ли отображение статистики посетителей в настройках.
     *
     * @return true, если статистика посетителей включена, иначе false.
     */
    public boolean is_visitors() {
        return getSharedPreferences().getBoolean("visitors", true);
    }

    /**
     * Проверяет, включены ли уведомления в настройках.
     *
     * @return true, если уведомления включены, иначе false.
     */
    public boolean is_notify() {
        return getSharedPreferences().getBoolean("sync", true);
    }

    /**
     * Проверяет, включён ли клиентский режим в настройках.
     *
     * @return true, если клиентский режим включён, иначе false.
     */
    public boolean is_client() {
        return getSharedPreferences().getBoolean("dvclient", false);
    }
}