/**
 * Модель данных для обработки ответов API в приложении DVAdmin. Содержит статистику сайта
 * dimonvideo.ru, количество материалов, ожидающих модерации, и другие метрики, такие как
 * посещаемость и TIC. Используется для десериализации JSON-ответов от конечной точки
 * {@link ApiService#getCounts()}.
 */
package dv.dimonvideo.dvadmin.model;

import com.google.gson.annotations.SerializedName;

/**
 * Представляет структуру ответа API с полями, соответствующими категориям модерации и
 * статистике сайта.
 */
public class ApiResponse {
    /** Статистика за текущий день. */
    @SerializedName("today")
    private String today;

    /** Количество материалов в категории загрузок, ожидающих модерации. */
    @SerializedName("uploader")
    private String uploader;

    /** Количество видеоматериалов, ожидающих модерации. */
    @SerializedName("vuploader")
    private String vuploader;

    /** Количество музыкальных материалов, ожидающих модерации. */
    @SerializedName("muzon")
    private String muzon;

    /** Количество пользовательских новостей, ожидающих модерации. */
    @SerializedName("usernews")
    private String usernews;

    /** Количество изображений в галерее, ожидающих модерации. */
    @SerializedName("gallery")
    private String gallery;

    /** Количество устройств, ожидающих модерации. */
    @SerializedName("devices")
    private String devices;

    /** Количество сообщений на форуме, требующих проверки. */
    @SerializedName("forum")
    private String forum;

    /** Количество жалоб на файлы. */
    @SerializedName("abuse_file")
    private String abuseFile;

    /** Количество жалоб на сообщения форума. */
    @SerializedName("abuse_forum")
    private String abuseForum;

    /** Данные о доступном пространстве на сервере. */
    @SerializedName("space")
    private String space;

    /** Количество посетителей сайта. */
    @SerializedName("visitors")
    private String visitors;

    /** Значение TIC (тематический индекс цитирования). */
    @SerializedName("tic")
    private String tic;

    /** Дата данных, возвращённых API. */
    @SerializedName("date")
    private String date;

    /**
     * Возвращает статистику за текущий день.
     *
     * @return Статистика за день в виде строки.
     */
    public String getToday() { return today; }

    /**
     * Возвращает количество материалов в категории загрузок, ожидающих модерации.
     *
     * @return Количество загрузок в виде строки.
     */
    public String getUploader() { return uploader; }

    /**
     * Возвращает количество видеоматериалов, ожидающих модерации.
     *
     * @return Количество видеоматериалов в виде строки.
     */
    public String getVuploader() { return vuploader; }

    /**
     * Возвращает количество музыкальных материалов, ожидающих модерации.
     *
     * @return Количество музыкальных материалов в виде строки.
     */
    public String getMuzon() { return muzon; }

    /**
     * Возвращает количество пользовательских новостей, ожидающих модерации.
     *
     * @return Количество новостей в виде строки.
     */
    public String getUsernews() { return usernews; }

    /**
     * Возвращает количество изображений в галерее, ожидающих модерации.
     *
     * @return Количество изображений в виде строки.
     */
    public String getGallery() { return gallery; }

    /**
     * Возвращает количество устройств, ожидающих модерации.
     *
     * @return Количество устройств в виде строки.
     */
    public String getDevices() { return devices; }

    /**
     * Возвращает количество сообщений на форуме, требующих проверки.
     *
     * @return Количество сообщений форума в виде строки.
     */
    public String getForum() { return forum; }

    /**
     * Возвращает количество жалоб на файлы.
     *
     * @return Количество жалоб на файлы в виде строки.
     */
    public String getAbuseFile() { return abuseFile; }

    /**
     * Возвращает количество жалоб на сообщения форума.
     *
     * @return Количество жалоб на форум в виде строки.
     */
    public String getAbuseForum() { return abuseForum; }

    /**
     * Возвращает данные о доступном пространстве на сервере.
     *
     * @return Данные о пространстве в виде строки.
     */
    public String getSpace() { return space; }

    /**
     * Возвращает количество посетителей сайта.
     *
     * @return Количество посетителей в виде строки.
     */
    public String getVisitors() { return visitors; }

    /**
     * Возвращает значение TIC (тематический индекс цитирования).
     *
     * @return Значение TIC в виде строки.
     */
    public String getTic() { return tic; }

    /**
     * Возвращает дату данных, возвращённых API.
     *
     * @return Дата данных в виде строки.
     */
    public String getDate() { return date; }
}