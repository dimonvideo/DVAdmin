/**
 * Класс конфигурации приложения DVAdmin, содержащий константы для URL и ключей, используемых в
 * приложении. Эти константы определяют базовые URL для сайта dimonvideo.ru и его API, а также
 * ключи для кэширования данных, связанных с задачами модерации.
 */
package dv.dimonvideo.dvadmin;

public class Config {
    /**
     * Базовый URL сайта dimonvideo.ru, используемый для создания ссылок на журналы модерации и
     * другие ресурсы.
     */
    public static final String BASE_URL = "https://dimonvideo.ru";

    /**
     * Базовый URL API dimonvideo.net, используемый для получения данных о состоянии сайта и
     * задачах модерации.
     */
    public static final String HOST_URL = "https://api.dimonvideo.net/";

    /**
     * Конечная точка API для получения количества материалов, ожидающих модерации.
     */
    public static final String COUNT_URL = "/dvadminapi.php?op=18";

    /**
     * URL для доступа к странице журналов модерации на сайте dimonvideo.ru.
     */
    public static final String ADMIN_URL = BASE_URL + "/logs";

    /**
     * URL для доступа к странице журналов загрузок, используемый для проверки контента,
     * загруженного пользователями.
     */
    public static final String UPL_URL = BASE_URL + "/logs/uploader/0";

    /**
     * Тег, используемый для логирования событий и ошибок в приложении DVAdmin.
     */
    public static final String TAG = "DVAdminApp";

    /**
     * Ключ для кэширования списка названий категорий модерации в SharedPreferences.
     */
    public static final String PREF_NAMES_KEY = "cached_names";

    /**
     * Ключ для кэширования списка количества материалов в каждой категории модерации в
     * SharedPreferences.
     */
    public static final String PREF_COUNTS_KEY = "cached_counts";

    /**
     * Ключ для кэширования подзаголовка (например, даты последнего обновления) в
     * SharedPreferences.
     */
    public static final String PREF_SUBTITLE_KEY = "cached_subtitle";
}