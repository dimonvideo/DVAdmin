/**
 * Интерфейс для определения конечных точек API, используемых приложением DVAdmin для получения
 * данных о состоянии сайта dimonvideo.ru и модераторских задачах. Определяет методы для запросов
 * к API, возвращающих статистику, списки материалов на проверку и другие отчёты.
 */
package dv.dimonvideo.dvadmin.util;

import dv.dimonvideo.dvadmin.Config;
import dv.dimonvideo.dvadmin.model.ApiResponse;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Определяет HTTP-запросы к API с использованием Retrofit.
 */
public interface ApiService {
    /**
     * Запрашивает статистику и количество материалов, ожидающих модерации, с конечной точки,
     * указанной в {@link Config#COUNT_URL}.
     *
     * @return Объект {@link Call} для получения {@link ApiResponse} с данными модерации.
     */
    @GET(Config.COUNT_URL)
    Call<ApiResponse> getCounts();

    /**
     * Запрашивает список дней рождений пользователей с конечной точки "/dvadminapi.php?op=9".
     *
     * @return Объект {@link Call} для получения HTML-строки с данными о днях рождения.
     */
    @GET("/dvadminapi.php?op=9")
    Call<String> getBirthdays();

    /**
     * Запрашивает список пользователей, добавивших файлы, с конечной точки
     * "/dvadminapi.php?op=12".
     *
     * @return Объект {@link Call} для получения HTML-строки с данными о добавленных файлах.
     */
    @GET("/dvadminapi.php?op=12")
    Call<String> getWhoAddedFiles();

    /**
     * Запрашивает список последних заблокированных пользователей с конечной точки
     * "/dvadminapi.php?op=14".
     *
     * @return Объект {@link Call} для получения HTML-строки с данными о банах.
     */
    @GET("/dvadminapi.php?op=14")
    Call<String> getLastBans();

    /**
     * Запрашивает список последних удалённых материалов с конечной точки
     * "/dvadminapi.php?op=15".
     *
     * @return Объект {@link Call} для получения HTML-строки с данными об удалённых материалах.
     */
    @GET("/dvadminapi.php?op=15")
    Call<String> getLastDeleted();

    /**
     * Запрашивает список последних тем форума с конечной точки "/dvadminapi.php?op=11".
     *
     * @return Объект {@link Call} для получения HTML-строки с данными о темах форума.
     */
    @GET("/dvadminapi.php?op=11")
    Call<String> getLastTopics();

    /**
     * Запрашивает список последних комментариев с конечной точки "/dvadminapi.php?op=13".
     *
     * @return Объект {@link Call} для получения HTML-строки с данными о комментариях.
     */
    @GET("/dvadminapi.php?op=13")
    Call<String> getLastComments();
}