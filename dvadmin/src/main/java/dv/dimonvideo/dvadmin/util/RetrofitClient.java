/**
 * Класс для создания и управления экземпляром Retrofit, используемым для выполнения сетевых запросов
 * к API сайта dimonvideo.ru в приложении DVAdmin. Настраивает клиент с поддержкой логирования,
 * тайм-аутов и конвертеров для обработки JSON и строковых ответов.
 */
package dv.dimonvideo.dvadmin.util;

import java.util.concurrent.TimeUnit;

import dv.dimonvideo.dvadmin.Config;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Предоставляет синглтон-экземпляр {@link Retrofit} для взаимодействия с API.
 */
public class RetrofitClient {
    /** Базовый URL API, полученный из {@link Config#HOST_URL}. */
    private static final String BASE_URL = Config.HOST_URL;

    /** Экземпляр Retrofit, инициализируемый при первом вызове {@link #getInstance()}. */
    private static Retrofit retrofit;

    /**
     * Возвращает синглтон-экземпляр {@link Retrofit}, создавая его при необходимости с
     * настройками логирования, тайм-аутов и конвертеров для JSON и строк.
     *
     * @return Настроенный экземпляр {@link Retrofit}.
     */
    public static Retrofit getInstance() {
        if (retrofit == null) {
            // Настройка логирования для отладки
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Создание HTTP-клиента с тайм-аутами и логированием
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .build();

            // Создание экземпляра Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(ScalarsConverterFactory.create()) // Для строковых (HTML) ответов
                    .addConverterFactory(GsonConverterFactory.create()) // Для JSON-ответов
                    .build();
        }
        return retrofit;
    }
}