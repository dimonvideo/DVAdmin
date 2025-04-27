package dv.dimonvideo.dvadmin.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.preference.PreferenceManager;

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


public class MainViewModel extends ViewModel {
    private final MutableLiveData<List<String>> countsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<String>> namesLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> subtitleLiveData = new MutableLiveData<>();
    private final MediatorLiveData<Pair<List<String>, List<String>>> combinedData = new MediatorLiveData<>();
    private SharedPreferences prefs;
    private AppController controller;

    private static final String PREF_NAMES_KEY = "cached_names";
    private static final String PREF_COUNTS_KEY = "cached_counts";
    private static final String PREF_SUBTITLE_KEY = "cached_subtitle";
    private final MutableLiveData<Boolean> loadingState = new MutableLiveData<>();

    public LiveData<Boolean> getLoadingState() {
        return loadingState;
    }

    public MainViewModel() {
        combinedData.addSource(namesLiveData, names ->
                combinedData.setValue(new Pair<>(names, countsLiveData.getValue())));
        combinedData.addSource(countsLiveData, counts ->
                combinedData.setValue(new Pair<>(namesLiveData.getValue(), counts)));
    }

    public void fetchData(Context context) {
        controller = AppController.getInstance(context);
        ApiService apiService = controller.getApiService();

        // Инициализация SharedPreferences
        if (prefs == null) {
            prefs = PreferenceManager.getDefaultSharedPreferences(context);
        }
        apiService.getCounts().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {

                loadingState.setValue(false); // Скрываем прогресс-бар после завершения загрузки

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    List<String> counts = new ArrayList<>();
                    List<String> names = new ArrayList<>();

                    // Получаем настройки пользователя
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

                    // Заполняем списки на основе условий
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

                    // Обновляем LiveData
                    countsLiveData.setValue(counts);
                    namesLiveData.setValue(names);
                    subtitleLiveData.setValue(context.getString(R.string.actually) + apiResponse.getDate());

                    // Сохраняем в кэш
                    saveToCache(names, counts, context.getString(R.string.actually) + apiResponse.getDate());
                } else {
                    Log.e(Config.TAG, "Response error: " + response.message());
                    loadCachedData(context);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                loadingState.setValue(false); // Скрываем прогресс-бар при ошибке
                Log.e(Config.TAG, "Network error", t);
                loadCachedData(context);
            }
        });
    }

    private void saveToCache(List<String> names, List<String> counts, String subtitle) {
        try {
            // Сериализуем списки в JSON
            JSONArray namesArray = new JSONArray(names);
            JSONArray countsArray = new JSONArray(counts);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(PREF_NAMES_KEY, namesArray.toString());
            editor.putString(PREF_COUNTS_KEY, countsArray.toString());
            editor.putString(PREF_SUBTITLE_KEY, subtitle);
            editor.apply();
        } catch (Exception e) {
            Log.e(Config.TAG, "Failed to save cache", e);
        }
    }

    private void loadCachedData(Context context) {
        try {
            // Загружаем кэшированные данные
            String namesJson = prefs.getString(PREF_NAMES_KEY, null);
            String countsJson = prefs.getString(PREF_COUNTS_KEY, null);
            String subtitle = prefs.getString(PREF_SUBTITLE_KEY, null);

            if (namesJson != null && countsJson != null && subtitle != null) {
                // Десериализуем списки из JSON
                JSONArray namesArray = new JSONArray(namesJson);
                JSONArray countsArray = new JSONArray(countsJson);
                List<String> names = new ArrayList<>();
                List<String> counts = new ArrayList<>();

                for (int i = 0; i < namesArray.length(); i++) {
                    names.add(namesArray.getString(i));
                }
                for (int i = 0; i < countsArray.length(); i++) {
                    counts.add(countsArray.getString(i));
                }

                // Обновляем LiveData
                countsLiveData.setValue(counts);
                namesLiveData.setValue(names);
                subtitleLiveData.setValue(subtitle + context.getString(R.string.cached_data_suffix));
            } else {
                // Если кэш пуст, устанавливаем пустые списки
                countsLiveData.setValue(new ArrayList<>());
                namesLiveData.setValue(new ArrayList<>());
                subtitleLiveData.setValue(context.getString(R.string.no_data));
            }
        } catch (Exception e) {
            Log.e(Config.TAG, "Failed to load cache", e);
            countsLiveData.setValue(new ArrayList<>());
            namesLiveData.setValue(new ArrayList<>());
            subtitleLiveData.setValue(context.getString(R.string.no_data));
        }
    }

    public LiveData<List<String>> getCounts() {
        return countsLiveData;
    }

    public LiveData<List<String>> getNames() {
        return namesLiveData;
    }

    public LiveData<String> getSubtitle() {
        return subtitleLiveData;
    }

    public LiveData<Pair<List<String>, List<String>>> getCombinedData() {
        return combinedData;
    }

    public static class Pair<F, S> {
        public final F first;
        public final S second;

        public Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair<?, ?> pair = (Pair<?, ?>) o;
            return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }
    }
}