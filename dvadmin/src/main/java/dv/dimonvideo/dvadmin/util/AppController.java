package dv.dimonvideo.dvadmin.util;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class AppController extends Application {
    private static AppController sInstance;
    private RequestQueue mRequestQueue;
    private SharedPreferences sharedPrefs;
    public static final String TAG = AppController.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        sInstance = this;
    }

    public static synchronized AppController getInstance() {
        return sInstance;
    }

    public RequestQueue getRequestQueueV() {

        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
            mRequestQueue.getCache().clear();
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueueV().add(req);
    }

    public SharedPreferences getSharedPreferences() {

        if (sharedPrefs == null) {
            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        }

        return sharedPrefs;
    }

    // ===================================== preferences ========================================================= //
    public String isDark() {
        return getSharedPreferences().getString("dvc_theme_list", "true");
    }
    public String isWidget() {
        return getSharedPreferences().getString("dvc_widget_list", "visitors");
    }
    public boolean is_uploader() { return getSharedPreferences().getBoolean("uploader", true);}
    public boolean is_vuploader() { return getSharedPreferences().getBoolean("vuploader", true);}
    public boolean is_muzon() { return getSharedPreferences().getBoolean("muzon", true);}
    public boolean is_usernews() { return getSharedPreferences().getBoolean("usernews", true);}
    public boolean is_gallery() { return getSharedPreferences().getBoolean("gallery", true);}
    public boolean is_devices() { return getSharedPreferences().getBoolean("devices", true);}
    public boolean is_forum() { return getSharedPreferences().getBoolean("forum", true);}
    public boolean is_abuse_file() { return getSharedPreferences().getBoolean("abuse_file", true);}
    public boolean is_abuse_forum() { return getSharedPreferences().getBoolean("abuse_forum", true);}
    public boolean is_space() { return getSharedPreferences().getBoolean("space", true);}
    public boolean is_visitors() { return getSharedPreferences().getBoolean("visitors", true);}
    public boolean is_notify() { return getSharedPreferences().getBoolean("sync", true);}
    public boolean is_client() { return getSharedPreferences().getBoolean("dvclient", false);}

    // =============================================== put preferences ================================================================== //

    public void putThemeLight() {
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("dvc_theme_list", "false").apply();
    }

    public void putVersionCode(int versionCode) {
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putInt("last_version_code", versionCode).apply();
    }
}
