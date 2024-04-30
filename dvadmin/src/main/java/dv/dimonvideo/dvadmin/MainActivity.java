package dv.dimonvideo.dvadmin;

import android.Manifest;
import android.app.NotificationManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.Configuration;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import dv.dimonvideo.dvadmin.adapter.Adapter;
import dv.dimonvideo.dvadmin.databinding.LayoutBinding;
import dv.dimonvideo.dvadmin.util.Analytics;
import dv.dimonvideo.dvadmin.util.AppController;
import dv.dimonvideo.dvadmin.util.ProgressHelper;
import dv.dimonvideo.dvadmin.util.RequestPermissionHandler;
import dv.dimonvideo.dvadmin.util.WidgetProvider;

public class MainActivity extends AppCompatActivity implements Adapter.ItemClickListener {
    private Adapter adapter;
    private String countDate, countUploader, countVuploader, countMuzon, countUsernews, countGallery, countDevices, countForum, countTic, countVisitors, countSpace, countAfile, countAforum, today;
    private RequestPermissionHandler mRequestPermissionHandler;
    private boolean doubleBackToExitPressedOnce = false;
    public static LayoutBinding binding;
    private RecyclerView recyclerView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        final String is_dark = AppController.getInstance().isDark();
        if (Objects.equals(is_dark, "true"))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else if (Objects.equals(is_dark, "system"))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        binding = LayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adjustFontScale(getResources().getConfiguration());

        Analytics.init(this);

        toolbar = binding.toolbar;
        toolbar.setTitle(getResources().getString(R.string.app_name));
        recyclerView = binding.rv;

        setSupportActionBar(toolbar);
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        set_adapter();
        SwipeRefreshLayout swipeRefreshLayout = binding.swipeLayout;
        swipeRefreshLayout.setOnRefreshListener(() -> {
            recreate();
            swipeRefreshLayout.setRefreshing(false);

        });

        // shortcuts
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {

            ShortcutManager shortcutManager = (ShortcutManager) getSystemService(SHORTCUT_SERVICE);

            ShortcutInfo logUploaderShortcut = new ShortcutInfo.Builder(this, "shortcut_visit_1")
                    .setShortLabel(getString(R.string.action_admin_upl))
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_launcher))
                    .setIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(Config.UPL_URL)))
                    .build();

            ShortcutInfo logShortcut = new ShortcutInfo.Builder(this, "shortcut_visit")
                    .setShortLabel(getString(R.string.action_admin))
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_launcher))
                    .setIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(Config.ADMIN_URL)))
                    .build();

            new Thread(() -> shortcutManager.setDynamicShortcuts(Arrays.asList(logUploaderShortcut, logShortcut))).start();

        }

        // обновление виджета
        Intent intent = new Intent(this, WidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        int[] ids = AppWidgetManager.getInstance(getApplication())
                .getAppWidgetIds(new ComponentName(getApplication(), WidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);

        if ((Build.VERSION.SDK_INT >= 33)) {
            mRequestPermissionHandler = new RequestPermissionHandler();
            handlePerm();
        }
    }


    // получение данных
    private void set_adapter() {

        final boolean is_uploader = AppController.getInstance().is_uploader();
        final boolean is_vuploader = AppController.getInstance().is_vuploader();
        final boolean is_muzon = AppController.getInstance().is_muzon();
        final boolean is_usernews = AppController.getInstance().is_usernews();
        final boolean is_gallery = AppController.getInstance().is_gallery();
        final boolean is_devices = AppController.getInstance().is_devices();
        final boolean is_forum = AppController.getInstance().is_forum();
        final boolean is_abuse_file = AppController.getInstance().is_abuse_file();
        final boolean is_abuse_forum = AppController.getInstance().is_abuse_forum();
        final boolean is_space = AppController.getInstance().is_space();
        final boolean is_visitors = AppController.getInstance().is_visitors();

        final ArrayList<String> count = new ArrayList<>();

        final ArrayList<String> Names = new ArrayList<>();
        if (BuildConfig.PRO) Names.add(getString(R.string.today));
        if (is_uploader) Names.add(getString(R.string.uploader));
        if (is_vuploader) Names.add(getString(R.string.vuploader));
        if (is_muzon) Names.add(getString(R.string.muzon));
        if (is_usernews) Names.add(getString(R.string.usernews));
        if (is_gallery) Names.add(getString(R.string.gallery));
        if (is_devices) Names.add(getString(R.string.devices));
        if (is_forum) Names.add(getString(R.string.forum));
        if (is_abuse_file) Names.add(getString(R.string.abuse_file));
        if (is_abuse_forum) Names.add(getString(R.string.abuse_forum));
        if (is_space) Names.add(getString(R.string.space));
        if (is_visitors) Names.add(getString(R.string.visitors));

        Names.add(getString(R.string.tic));

        ProgressHelper.showDialog(this, getString(R.string.please_wait));

        StringRequest stringRequest = new StringRequest(Request.Method.GET, Config.COUNT_URL,
                response -> {
                    try {

                        JSONObject jsonObject;
                        jsonObject = new JSONObject(response);

                        countUploader = jsonObject.getString("uploader");
                        countVuploader = jsonObject.getString("vuploader");
                        countMuzon = jsonObject.getString("muzon");
                        countUsernews = jsonObject.getString("usernews");
                        countGallery = jsonObject.getString("gallery");
                        countDevices = jsonObject.getString("devices");
                        countForum = jsonObject.getString("forum");
                        countAfile = jsonObject.getString("abuse_file");
                        countAforum = jsonObject.getString("abuse_forum");
                        countSpace = jsonObject.getString("space");
                        countTic = jsonObject.getString("tic");
                        countDate = jsonObject.getString("date");
                        countVisitors = jsonObject.getString("visitors");
                        today = jsonObject.getString("today");

                        count.clear();
                        if (BuildConfig.PRO) count.add(today);
                        if (is_uploader) count.add(countUploader);
                        if (is_vuploader) count.add(countVuploader);
                        if (is_muzon) count.add(countMuzon);
                        if (is_usernews) count.add(countUsernews);
                        if (is_gallery) count.add(countGallery);
                        if (is_devices) count.add(countDevices);
                        if (is_forum) count.add(countForum);
                        if (is_abuse_file) count.add(countAfile);
                        if (is_abuse_forum) count.add(countAforum);
                        if (is_space) count.add(countSpace);
                        if (is_visitors) count.add(countVisitors);
                        count.add(countTic);

                        toolbar.setSubtitle(getString(R.string.actually) + countDate);

                        recyclerView.setHasFixedSize(true);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
                        dividerItemDecoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(MainActivity.this, R.drawable.divider)));
                        recyclerView.addItemDecoration(dividerItemDecoration);
                        adapter = new Adapter(this, Names, count);
                        adapter.setClickListener(MainActivity.this);
                        recyclerView.setAdapter(adapter);

                        if (ProgressHelper.isDialogVisible()) ProgressHelper.dismissDialog();

                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), getString(R.string.error_network_timeout), Toast.LENGTH_LONG).show();
                    }

                }, error -> {

            if (ProgressHelper.isDialogVisible()) ProgressHelper.dismissDialog();

            if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_network_timeout), Toast.LENGTH_LONG).show();
            } else if (error instanceof AuthFailureError) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_network_timeout), Toast.LENGTH_LONG).show();
            } else if (error instanceof ServerError) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_server), Toast.LENGTH_LONG).show();
            } else if (error instanceof NetworkError) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_network), Toast.LENGTH_LONG).show();
            } else if (error instanceof ParseError) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_server), Toast.LENGTH_LONG).show();
            }
        });

        AppController.getInstance().addToRequestQueue(stringRequest);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        // settings
        if (id == R.id.action_settings) {
            Intent i = new Intent(MainActivity.this, SettingsActivity.class);

            startActivity(i);

            return true;
        }
        // refresh
        if (id == R.id.action_refresh) {
            recreate();
        }
        // birthdays
        if (id == R.id.action_bd) {

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getString(R.string.action_bd));

            WebView wv = new WebView(this);
            wv.loadUrl(Config.HOST_URL + "/smart/dvadminapi.php?op=9");
            wv.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);

                    return true;
                }
            });

            alert.setView(wv);
            alert.setNegativeButton(getString(R.string.action_close), (dialog, id12) -> dialog.dismiss());
            alert.show();
        }
        // who added files now
        if (id == R.id.action_whoaddedfiles) {

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getString(R.string.action_whoaddedfiles));

            WebView wv = new WebView(this);
            wv.loadUrl(Config.HOST_URL + "/smart/dvadminapi.php?op=12");
            wv.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);

                    return true;
                }
            });

            alert.setView(wv);
            alert.setNegativeButton(getString(R.string.action_close), (dialog, id13) -> dialog.dismiss());
            alert.show();
        }
        // last ban
        if (id == R.id.action_lastban) {

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getString(R.string.action_lastbans));

            WebView wv = new WebView(this);

            wv.loadUrl(Config.HOST_URL + "/smart/dvadminapi.php?op=14");
            wv.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);

                    return true;
                }
            });

            alert.setView(wv);
            alert.setNegativeButton(getString(R.string.action_close), (dialog, id14) -> dialog.dismiss());
            alert.show();

        }
        // last del uploader
        if (id == R.id.action_lastdel) {

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getString(R.string.action_lastdel));

            WebView wv = new WebView(this);

            wv.loadUrl(Config.HOST_URL + "/smart/dvadminapi.php?op=15");
            wv.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);

                    return true;
                }
            });

            alert.setView(wv);
            alert.setNegativeButton(getString(R.string.action_close), (dialog, id15) -> dialog.dismiss());
            alert.show();

        }
        // last del forum
        if (id == R.id.action_lasttopics) {

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getString(R.string.action_lasttopics));

            WebView wv = new WebView(this);

            wv.loadUrl(Config.HOST_URL + "/smart/dvadminapi.php?op=11");
            wv.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);

                    return true;
                }
            });

            alert.setView(wv);
            alert.setNegativeButton(getString(R.string.action_close), (dialog, id16) -> dialog.dismiss());
            alert.show();

        }
        // last del com
        if (id == R.id.action_lastcom) {

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getString(R.string.action_lastcom));

            WebView wv = new WebView(this);

            wv.loadUrl(Config.HOST_URL + "/smart/dvadminapi.php?op=13");
            wv.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);

                    return true;
                }
            });

            alert.setView(wv);
            alert.setNegativeButton(getString(R.string.action_close), (dialog, id1) -> dialog.dismiss());
            alert.show();

        }
        // other apps
        if (id == R.id.action_others) {

            String url = "https://play.google.com/store/apps/dev?id=6091758746633814135";
            if (!BuildConfig.GOOGLE) url = Config.BASE_URL + "/android.html";

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    url));


            try {
                startActivity(browserIntent);
            } catch (Throwable ignored) {
            }
        }
        // feedback
        if (id == R.id.action_feedback) {

            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.fromParts("mailto", getString(R.string.app_mail), null));
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " Feedback");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            try {
                startActivity(intent);
            } catch (Throwable ignored) {
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(View view, int position) {

        Intent browserIntent = null;

        if (adapter.getItem(position).equals(getString(R.string.uploader))) {
            browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Config.BASE_URL + "/logs/uploader/0"));

            if (AppController.getInstance().is_client()) {
                browserIntent = new Intent("com.dimonvideo.client.dvadmin");
                browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                browserIntent.putExtra("action_admin", "uploader");
            }

        } else if (adapter.getItem(position).equals(getString(R.string.vuploader))) {
            browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    Config.BASE_URL + "/logs/vuploader/0"));

            if (AppController.getInstance().is_client()) {
                browserIntent = new Intent("com.dimonvideo.client.dvadmin");
                browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                browserIntent.putExtra("action_admin", "vuploader");
            }
        } else if (adapter.getItem(position).equals(getString(R.string.muzon))) {
            browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    Config.BASE_URL + "/logs/muzon/0"));
            if (AppController.getInstance().is_client()) {
                browserIntent = new Intent("com.dimonvideo.client.dvadmin");
                browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                browserIntent.putExtra("action_admin", "muzon");
            }
        } else if (adapter.getItem(position).equals(getString(R.string.usernews))) {
            browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    Config.BASE_URL + "/logs/usernews/0"));
            if (AppController.getInstance().is_client()) {
                browserIntent = new Intent("com.dimonvideo.client.dvadmin");
                browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                browserIntent.putExtra("action_admin", "usernews");
            }
        } else if (adapter.getItem(position).equals(getString(R.string.gallery))) {
            browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    Config.BASE_URL + "/logs/gallery/0"));
            if (AppController.getInstance().is_client()) {
                browserIntent = new Intent("com.dimonvideo.client.dvadmin");
                browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                browserIntent.putExtra("action_admin", "gallery");
            }
        } else if (adapter.getItem(position).equals(getString(R.string.devices))) {
            browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    Config.BASE_URL + "/logs/device/0"));
            if (AppController.getInstance().is_client()) {
                browserIntent = new Intent("com.dimonvideo.client.dvadmin");
                browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                browserIntent.putExtra("action_admin", "device");
            }
        } else if (adapter.getItem(position).equals(getString(R.string.forum))) {
            browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    Config.BASE_URL + "/fadmin"));

        } else if (adapter.getItem(position).equals(getString(R.string.abuse_file))) {
            browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    Config.BASE_URL + "/forum/topic_1728146352"));


        } else if (adapter.getItem(position).equals(getString(R.string.abuse_forum))) {
            browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    Config.BASE_URL + "/forum/topic_1728146368"));

        }

        try {
            startActivity(browserIntent);
        } catch (Throwable ignored) {
        }

        Toast.makeText(this, adapter.getItem(position), Toast.LENGTH_SHORT).show();
        finish();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ProgressHelper.isDialogVisible()) ProgressHelper.dismissDialog();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void adjustFontScale(Configuration configuration) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        configuration.fontScale = Float.parseFloat(Objects.requireNonNull(sharedPrefs.getString("dvc_scale", "1.0f")));
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        assert wm != null;
        wm.getDefaultDisplay().getMetrics(metrics);
        metrics.scaledDensity = configuration.fontScale * metrics.density;
        getBaseContext().getResources().updateConfiguration(configuration, metrics);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void handlePerm() {
        mRequestPermissionHandler.requestPermission(this, new String[]{
                Manifest.permission.POST_NOTIFICATIONS
        }, 123, new RequestPermissionHandler.RequestPermissionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailed() {
                Toast.makeText(MainActivity.this, getString(R.string.perm_invalid), Toast.LENGTH_SHORT).show();
            }
        });
    }

}