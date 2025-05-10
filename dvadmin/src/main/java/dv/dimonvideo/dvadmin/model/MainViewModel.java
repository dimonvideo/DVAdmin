/**
 * ViewModel для управления данными модерации в приложении DVAdmin. Запрашивает статистику и списки
 * материалов, ожидающих проверки, с API сайта dimonvideo.ru, кэширует данные в
 * {@link SharedPreferences} и предоставляет их через {@link LiveData} для отображения в
 * {@link dv.dimonvideo.dvadmin.MainActivity}. Также обрабатывает дополнительные запросы, такие
 * как списки дней рождений, банов и комментариев.
 */
package dv.dimonvideo.dvadmin.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dv.dimonvideo.dvadmin.BuildConfig;
import dv.dimonvideo.dvadmin.Config;
import dv.dimonvideo.dvadmin.R;
import dv.dimonvideo.dvadmin.util.ApiService;
import dv.dimonvideo.dvadmin.util.AppController;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Наследует {@link ViewModel} для управления жизненным циклом данных и их предоставления
 * пользовательскому интерфейсу.
 */
public class MainViewModel extends ViewModel {
    /** LiveData для списка количеств материалов в категориях модерации. */
    private final MutableLiveData<List<String>> countsLiveData = new MutableLiveData<>();

    /** LiveData для списка названий категорий модерации. */
    private final MutableLiveData<List<String>> namesLiveData = new MutableLiveData<>();

    /** LiveData для подзаголовка (например, даты обновления данных). */
    private final MutableLiveData<String> subtitleLiveData = new MutableLiveData<>();

    /** Комбинирует списки названий и количеств для синхронного обновления интерфейса. */
    private final MediatorLiveData<Pair<List<String>, List<String>>> combinedData = new MediatorLiveData<>();

    /** SharedPreferences для кэширования данных. */
    private SharedPreferences prefs;

    /** Экземпляр синглтона {@link AppController} для доступа к API и настройкам. */
    private AppController controller;

    /** LiveData для списка дней рождений пользователей. */
    private final MutableLiveData<String> birthdaysLiveData = new MutableLiveData<>();

    /** LiveData для списка пользователей, добавивших файлы. */
    private final MutableLiveData<String> whoAddedFilesLiveData = new MutableLiveData<>();

    /** LiveData для списка последних заблокированных пользователей. */
    private final MutableLiveData<String> lastBansLiveData = new MutableLiveData<>();

    /** LiveData для списка последних удалённых материалов. */
    private final MutableLiveData<String> lastDeletedLiveData = new MutableLiveData<>();

    /** LiveData для списка последних тем форума. */
    private final MutableLiveData<String> lastTopicsLiveData = new MutableLiveData<>();

    /** LiveData для списка последних комментариев. */
    private final MutableLiveData<String> lastCommentsLiveData = new MutableLiveData<>();

    /** LiveData для состояния загрузки данных (true — загрузка, false — завершено). */
    private final MutableLiveData<Boolean> loadingState = new MutableLiveData<>();

    /**
     * Возвращает LiveData для отслеживания состояния загрузки данных.
     *
     * @return {@link LiveData} с булевым значением состояния загрузки.
     */
    public LiveData<Boolean> getLoadingState() {
        return loadingState;
    }

    /**
     * Возвращает LiveData для списка дней рождений пользователей.
     *
     * @return {@link LiveData} с HTML-строкой, содержащей данные о днях рождения.
     */
    public LiveData<String> getBirthdays() {
        return birthdaysLiveData;
    }

    /**
     * Возвращает LiveData для списка пользователей, добавивших файлы.
     *
     * @return {@link LiveData} с HTML-строкой, содержащей данные о добавленных файлах.
     */
    public LiveData<String> getWhoAddedFiles() {
        return whoAddedFilesLiveData;
    }

    /**
     * Возвращает LiveData для списка последних заблокированных пользователей.
     *
     * @return {@link LiveData} с HTML-строкой, содержащей данные о банах.
     */
    public LiveData<String> getLastBans() {
        return lastBansLiveData;
    }

    /**
     * Возвращает LiveData для списка последних удалённых материалов.
     *
     * @return {@link LiveData} с HTML-строкой, содержащей данные об удалённых материалах.
     */
    public LiveData<String> getLastDeleted() {
        return lastDeletedLiveData;
    }

    /**
     * Возвращает LiveData для списка последних тем форума.
     *
     * @return {@link LiveData} с HTML-строкой, содержащей данные о темах форума.
     */
    public LiveData<String> getLastTopics() {
        return lastTopicsLiveData;
    }

    /**
     * Возвращает LiveData для списка последних комментариев.
     *
     * @return {@link LiveData} с HTML-строкой, содержащей данные о комментариях.
     */
    public LiveData<String> getLastComments() {
        return lastCommentsLiveData;
    }

    /**
     * Конструктор, настраивающий комбинирование данных названий и количеств через
     * {@link MediatorLiveData}.
     */
    public MainViewModel() {
        combinedData.addSource(namesLiveData, names ->
                combinedData.setValue(new Pair<>(names, countsLiveData.getValue())));
        combinedData.addSource(countsLiveData, counts ->
                combinedData.setValue(new Pair<>(namesLiveData.getValue(), counts)));
    }

    /**
     * Запрашивает данные о категориях модерации и статистике с API, обновляет LiveData и кэширует
     * результаты. При ошибке загружает кэшированные данные.
     *
     * @param context Контекст приложения для доступа к строковым ресурсам и настройкам.
     */
    public void fetchData(Context context) {
        controller = AppController.getInstance();
        ApiService apiService = controller.getApiService();
        prefs = controller.getSharedPreferences();
        loadingState.setValue(true);
        apiService.getCounts().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                loadingState.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    List<String> counts = new ArrayList<>();
                    List<String> names = new ArrayList<>();

                    final boolean is_uploader = controller.is_uploader();
                    final boolean is_vuploader = controller.is_vuploader();
                    final boolean is_muzon = controller.is_muzon();
                    final boolean is_usernews = controller.is_usernews();
                    final boolean is_gallery = controller.is_gallery();
                    final boolean is_devices = controller.is_devices();
                    final boolean is_forum = controller.is_forum();
                    final boolean is_abuse_file = controller.is_abuse_file();
                    final boolean is_abuse_forum = controller.is_abuse_forum();
                    final boolean is_space = controller.is_space();
                    final boolean is_visitors = controller.is_visitors();

                    if (BuildConfig.GOOGLE) {
                        counts.add(apiResponse.getToday());
                        names.add(context.getString(R.string.today));
                    }
                    if (is_uploader) {
                        counts.add(apiResponse.getUploader());
                        names.add(context.getString(R.string.uploader));
                    }
                    if (is_vuploader) {
                        counts.add(apiResponse.getVuploader());
                        names.add(context.getString(R.string.vuploader));
                    }
                    if (is_muzon) {
                        counts.add(apiResponse.getMuzon());
                        names.add(context.getString(R.string.muzon));
                    }
                    if (is_usernews) {
                        counts.add(apiResponse.getUsernews());
                        names.add(context.getString(R.string.usernews));
                    }
                    if (is_gallery) {
                        counts.add(apiResponse.getGallery());
                        names.add(context.getString(R.string.gallery));
                    }
                    if (is_devices) {
                        counts.add(apiResponse.getDevices());
                        names.add(context.getString(R.string.devices));
                    }
                    if (is_forum) {
                        counts.add(apiResponse.getForum());
                        names.add(context.getString(R.string.forum));
                    }
                    if (is_abuse_file) {
                        counts.add(apiResponse.getAbuseFile());
                        names.add(context.getString(R.string.abuse_file));
                    }
                    if (is_abuse_forum) {
                        counts.add(apiResponse.getAbuseForum());
                        names.add(context.getString(R.string.abuse_forum));
                    }
                    if (is_space) {
                        counts.add(apiResponse.getSpace());
                        names.add(context.getString(R.string.space));
                    }
                    if (is_visitors) {
                        counts.add(apiResponse.getVisitors());
                        names.add(context.getString(R.string.visitors));
                    }
                    counts.add(apiResponse.getTic());
                    names.add(context.getString(R.string.tic));

                    countsLiveData.setValue(counts);
                    namesLiveData.setValue(names);
                    subtitleLiveData.setValue(context.getString(R.string.actually) + apiResponse.getDate());

                    saveToCache(names, counts, context.getString(R.string.actually) + apiResponse.getDate());
                } else {
                    Log.e(Config.TAG, "Ошибка ответа сервера: " + response.message());
                    loadCachedData(context);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                loadingState.setValue(false);
                Log.e(Config.TAG, "Сетевая ошибка: " + t.getMessage(), t);
                loadCachedData(context);
            }
        });
    }

    /**
     * Запрашивает список дней рождений пользователей с API и обновляет соответствующую LiveData.
     */
    public void fetchBirthdays() {
        if (controller == null) {
            controller = AppController.getInstance();
        }
        fetchApiData(controller.getApiService().getBirthdays(), birthdaysLiveData, "Birthdays");
    }

    /**
     * Запрашивает список пользователей, добавивших файлы, с API и обновляет соответствующую LiveData.
     */
    public void fetchWhoAddedFiles() {
        if (controller == null) {
            controller = AppController.getInstance();
        }
        fetchApiData(controller.getApiService().getWhoAddedFiles(), whoAddedFilesLiveData, "WhoAddedFiles");
    }

    /**
     * Запрашивает список последних заблокированных пользователей с API и обновляет соответствующую LiveData.
     */
    public void fetchLastBans() {
        if (controller == null) {
            controller = AppController.getInstance();
        }
        fetchApiData(controller.getApiService().getLastBans(), lastBansLiveData, "LastBans");
    }

    /**
     * Запрашивает список последних удалённых материалов с API и обновляет соответствующую LiveData.
     */
    public void fetchLastDeleted() {
        if (controller == null) {
            controller = AppController.getInstance();
        }
        fetchApiData(controller.getApiService().getLastDeleted(), lastDeletedLiveData, "LastDeleted");
    }

    /**
     * Запрашивает список последних тем форума с API и обновляет соответствующую LiveData.
     */
    public void fetchLastTopics() {
        if (controller == null) {
            controller = AppController.getInstance();
        }
        fetchApiData(controller.getApiService().getLastTopics(), lastTopicsLiveData, "LastTopics");
    }

    /**
     * Запрашивает список последних комментариев с API и обновляет соответствующую LiveData.
     */
    public void fetchLastComments() {
        if (controller == null) {
            controller = AppController.getInstance();
        }
        fetchApiData(controller.getApiService().getLastComments(), lastCommentsLiveData, "LastComments");
    }

    /**
     * Универсальный метод для выполнения API-запросов, возвращающих HTML-данные, с обработкой
     * ответов и ошибок.
     *
     * @param call        Объект {@link Call} для выполнения запроса.
     * @param liveData    LiveData для обновления с полученными данными.
     * @param requestName Имя запроса для логирования.
     */
    private void fetchApiData(Call<String> call, MutableLiveData<String> liveData, String requestName) {
        loadingState.setValue(true);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                loadingState.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    String html = response.body();
                    // Проверяем, пустой ли HTML
                    if (isEmptyHtml(html)) {
                        Log.w(Config.TAG, requestName + ": Пустой HTML-ответ");
                        liveData.setValue(""); // Пустая строка для пустого HTML
                    } else {
                        liveData.setValue(html);
                    }
                } else {
                    Log.e(Config.TAG, requestName + " ошибка ответа: " + response.message());
                    liveData.setValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                loadingState.setValue(false);
                Log.e(Config.TAG, requestName + " сетевая ошибка: " + t.getMessage(), t);
                liveData.setValue(null);
            }
        });
    }

    /**
     * Проверяет, является ли HTML-ответ пустым, анализируя содержимое тега <body>.
     *
     * @param html HTML-строка для проверки.
     * @return true, если HTML пустой, иначе false.
     */
    private boolean isEmptyHtml(String html) {
        if (TextUtils.isEmpty(html)) {
            return true;
        }
        // Удаляем пробелы и проверяем наличие содержимого в <body>
        String trimmed = html.replaceAll("\\s", "").toLowerCase();
        return trimmed.contains("<body></body>") || trimmed.contains("<body/>");
    }

    /**
     * Сохраняет списки названий, количеств и подзаголовок в кэш через {@link SharedPreferences}.
     *
     * @param names    Список названий категорий.
     * @param counts   Список количеств материалов.
     * @param subtitle Подзаголовок (например, дата обновления).
     */
    private void saveToCache(List<String> names, List<String> counts, String subtitle) {
        try {
            JSONArray namesArray = new JSONArray(names);
            JSONArray countsArray = new JSONArray(counts);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(Config.PREF_NAMES_KEY, namesArray.toString());
            editor.putString(Config.PREF_COUNTS_KEY, countsArray.toString());
            editor.putString(Config.PREF_SUBTITLE_KEY, subtitle);
            editor.apply();
        } catch (Exception e) {
            Log.e(Config.TAG, "Ошибка сохранения кэша", e);
        }
    }

    /**
     * Загружает кэшированные данные из {@link SharedPreferences} и обновляет LiveData при
     * отсутствии сетевого ответа.
     *
     * @param context Контекст приложения для доступа к строковым ресурсам.
     */
    private void loadCachedData(Context context) {
        try {
            String namesJson = prefs.getString(Config.PREF_NAMES_KEY, null);
            String countsJson = prefs.getString(Config.PREF_COUNTS_KEY, null);
            String subtitle = prefs.getString(Config.PREF_SUBTITLE_KEY, null);

            if (namesJson != null && countsJson != null && subtitle != null) {
                JSONArray namesArray = new JSONArray(namesJson);
                JSONArray countsArray = new JSONArray(countsJson);
                List<String> names = new ArrayList<>();
                List<String> counts = new ArrayList<>();

                int length = Math.min(namesArray.length(), countsArray.length());
                for (int i = 0; i < length; i++) {
                    names.add(namesArray.getString(i));
                    counts.add(countsArray.getString(i));
                }

                countsLiveData.setValue(counts);
                namesLiveData.setValue(names);
                subtitleLiveData.setValue(subtitle + context.getString(R.string.cached_data_suffix));
            } else {
                countsLiveData.setValue(new ArrayList<>());
                namesLiveData.setValue(new ArrayList<>());
                subtitleLiveData.setValue(context.getString(R.string.no_data));
            }
        } catch (Exception e) {
            Log.e(Config.TAG, "Ошибка загрузки кэша", e);
            countsLiveData.setValue(new ArrayList<>());
            namesLiveData.setValue(new ArrayList<>());
            subtitleLiveData.setValue(context.getString(R.string.no_data));
        }
    }

    /**
     * Возвращает LiveData для списка количеств материалов в категориях модерации.
     *
     * @return {@link LiveData} с списком количеств.
     */
    public LiveData<List<String>> getCounts() {
        return countsLiveData;
    }

    /**
     * Возвращает LiveData для списка названий категорий модерации.
     *
     * @return {@link LiveData} с списком названий.
     */
    public LiveData<List<String>> getNames() {
        return namesLiveData;
    }

    /**
     * Возвращает LiveData для подзаголовка (например, даты обновления).
     *
     * @return {@link LiveData} с подзаголовком.
     */
    public LiveData<String> getSubtitle() {
        return subtitleLiveData;
    }

    /**
     * Возвращает LiveData для комбинированных данных названий и количеств.
     *
     * @return {@link LiveData} с парой списков названий и количеств.
     */
    public LiveData<Pair<List<String>, List<String>>> getCombinedData() {
        return combinedData;
    }

    /**
     * Вспомогательный класс для хранения пары списков (названий и количеств).
     */
    public static class Pair<F, S> {
        /** Первый элемент пары (например, список названий). */
        public final F first;

        /** Второй элемент пары (например, список количеств). */
        public final S second;

        /**
         * Создаёт пару из двух элементов.
         *
         * @param first  Первый элемент.
         * @param second Второй элемент.
         */
        public Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }

        /**
         * Сравнивает пары на равенство.
         *
         * @param o Объект для сравнения.
         * @return true, если пары равны, иначе false.
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair<?, ?> pair = (Pair<?, ?>) o;
            return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
        }

        /**
         * Вычисляет хэш-код пары.
         *
         * @return Хэш-код на основе элементов пары.
         */
        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }
    }
}