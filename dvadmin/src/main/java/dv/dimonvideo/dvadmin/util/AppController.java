package dv.dimonvideo.dvadmin.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class AppController {
    private static AppController mInstance;
    private static Context mCtx;
    private static SharedPreferences sharedPrefs;

    private AppController(Context context) {
        mCtx = context.getApplicationContext();
    }

    public static synchronized AppController getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new AppController(context);
        }
        return mInstance;
    }

    public ApiService getApiService() {
        return RetrofitClient.getInstance().create(ApiService.class);
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

}
