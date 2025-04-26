package dv.dimonvideo.dvadmin.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dv.dimonvideo.dvadmin.BuildConfig;
import dv.dimonvideo.dvadmin.Config;
import dv.dimonvideo.dvadmin.R;
import dv.dimonvideo.dvadmin.util.AppController;

public class MainViewModel extends ViewModel {
    private final MutableLiveData<List<String>> countsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<String>> namesLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> subtitleLiveData = new MutableLiveData<>();
    private final MediatorLiveData<Pair<List<String>, List<String>>> combinedData = new MediatorLiveData<>();
    private AppController controller;
    private SharedPreferences prefs;

    private static final String PREF_NAMES_KEY = "cached_names";
    private static final String PREF_COUNTS_KEY = "cached_counts";
    private static final String PREF_SUBTITLE_KEY = "cached_subtitle";

    public MainViewModel() {
        combinedData.addSource(namesLiveData, names ->
                combinedData.setValue(new Pair<>(names, countsLiveData.getValue())));
        combinedData.addSource(countsLiveData, counts ->
                combinedData.setValue(new Pair<>(namesLiveData.getValue(), counts)));
    }

    public void fetchData(Context context) {
        // Инициализация SharedPreferences
        prefs = PreferenceManager.getDefaultSharedPreferences(context);

        // Загружаем кэшированные данные при старте
        loadCachedData(context);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, Config.COUNT_URL,
                response -> {
                    controller = AppController.getInstance(context);

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

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        List<String> counts = new ArrayList<>();
                        List<String> names = new ArrayList<>();

                        // Заполнение списков
                        if (BuildConfig.GOOGLE) {
                            counts.add(jsonObject.getString(Config.JSON_TODAY));
                            names.add(context.getString(R.string.today));
                        }
                        if (is_uploader) {
                            counts.add(jsonObject.getString(Config.JSON_UPLOADER));
                            names.add(context.getString(R.string.uploader));
                        }
                        if (is_vuploader) {
                            counts.add(jsonObject.getString(Config.JSON_VUPLOADER));
                            names.add(context.getString(R.string.vuploader));
                        }
                        if (is_muzon) {
                            counts.add(jsonObject.getString(Config.JSON_MUZON));
                            names.add(context.getString(R.string.muzon));
                        }
                        if (is_usernews) {
                            counts.add(jsonObject.getString(Config.JSON_USERNEWS));
                            names.add(context.getString(R.string.usernews));
                        }
                        if (is_gallery) {
                            counts.add(jsonObject.getString(Config.JSON_GALLERY));
                            names.add(context.getString(R.string.gallery));
                        }
                        if (is_devices) {
                            counts.add(jsonObject.getString(Config.JSON_DEVICES));
                            names.add(context.getString(R.string.devices));
                        }
                        if (is_forum) {
                            counts.add(jsonObject.getString(Config.JSON_FORUM));
                            names.add(context.getString(R.string.forum));
                        }
                        if (is_abuse_file) {
                            counts.add(jsonObject.getString(Config.JSON_ABUSE_FILE));
                            names.add(context.getString(R.string.abuse_file));
                        }
                        if (is_abuse_forum) {
                            counts.add(jsonObject.getString(Config.JSON_ABUSE_FORUM));
                            names.add(context.getString(R.string.abuse_forum));
                        }
                        if (is_space) {
                            counts.add(jsonObject.getString(Config.JSON_SPACE));
                            names.add(context.getString(R.string.space));
                        }
                        if (is_visitors) {
                            counts.add(jsonObject.getString(Config.JSON_VISITORS));
                            names.add(context.getString(R.string.visitors));
                        }
                        counts.add(jsonObject.getString(Config.JSON_TIC));
                        names.add(context.getString(R.string.tic));

                        // Сохраняем данные в кэш
                        saveToCache(names, counts, context.getString(R.string.actually) + jsonObject.getString(Config.JSON_DATE));

                        // Обновляем LiveData
                        countsLiveData.setValue(counts);
                        namesLiveData.setValue(names);
                        subtitleLiveData.setValue(context.getString(R.string.actually) + jsonObject.getString(Config.JSON_DATE));
                    } catch (JSONException e) {
                        Log.e(Config.TAG, "JSON parsing error", e);
                        loadCachedData(context); // Загружаем кэшированные данные при ошибке
                    }
                }, error -> {
            Log.e(Config.TAG, "Network error", error);
            loadCachedData(context); // Загружаем кэшированные данные при сетевой ошибке
        });

        AppController.getInstance(context).addToRequestQueue(stringRequest, 15000);
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