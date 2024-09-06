package dv.dimonvideo.dvadmin.util;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import dv.dimonvideo.dvadmin.Config;

public class AppController {

    @SuppressLint("StaticFieldLeak")
    private static AppController mInstance;

    private RequestQueue mRequestQueue;
    private static SharedPreferences sharedPrefs;
    private static SharedPreferences.Editor editor;

    @SuppressLint("StaticFieldLeak")
    private static Context mCtx;

    private AppController(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized AppController getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new AppController(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {

        if (mRequestQueue == null) {

            mRequestQueue = Volley.newRequestQueue(mCtx);
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, int timeout) {
        req.setTag(Config.TAG);
        req.setRetryPolicy(new DefaultRetryPolicy(
                timeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        req.setShouldCache(false);
        getRequestQueue().add(req);

    }

    public SharedPreferences getSharedPreferences() {

        if (sharedPrefs == null) {
            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mCtx);
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

    public boolean is_uploader() {
        return getSharedPreferences().getBoolean("uploader", true);
    }

    public boolean is_vuploader() {
        return getSharedPreferences().getBoolean("vuploader", true);
    }

    public boolean is_muzon() {
        return getSharedPreferences().getBoolean("muzon", true);
    }

    public boolean is_usernews() {
        return getSharedPreferences().getBoolean("usernews", true);
    }

    public boolean is_gallery() {
        return getSharedPreferences().getBoolean("gallery", true);
    }

    public boolean is_devices() {
        return getSharedPreferences().getBoolean("devices", true);
    }

    public boolean is_forum() {
        return getSharedPreferences().getBoolean("forum", true);
    }

    public boolean is_abuse_file() {
        return getSharedPreferences().getBoolean("abuse_file", true);
    }

    public boolean is_abuse_forum() {
        return getSharedPreferences().getBoolean("abuse_forum", true);
    }

    public boolean is_space() {
        return getSharedPreferences().getBoolean("space", true);
    }

    public boolean is_visitors() {
        return getSharedPreferences().getBoolean("visitors", true);
    }

    public boolean is_notify() {
        return getSharedPreferences().getBoolean("sync", true);
    }

    public boolean is_client() {
        return getSharedPreferences().getBoolean("dvclient", false);
    }

    // =============================================== put preferences ================================================================== //

    public void putThemeLight() {
        PreferenceManager.getDefaultSharedPreferences(mCtx).edit().putString("dvc_theme_list", "false").apply();
    }

    public void putVersionCode(int versionCode) {
        PreferenceManager.getDefaultSharedPreferences(mCtx).edit().putInt("last_version_code", versionCode).apply();
    }
}
