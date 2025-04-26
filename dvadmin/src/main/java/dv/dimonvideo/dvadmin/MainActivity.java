package dv.dimonvideo.dvadmin;

import android.Manifest;
import android.app.NotificationManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.Configuration;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import dv.dimonvideo.dvadmin.adapter.Adapter;
import dv.dimonvideo.dvadmin.databinding.LayoutBinding;
import dv.dimonvideo.dvadmin.model.MainViewModel;
import dv.dimonvideo.dvadmin.util.Analytics;
import dv.dimonvideo.dvadmin.util.AppController;
import dv.dimonvideo.dvadmin.util.ProgressHelper;
import dv.dimonvideo.dvadmin.util.RequestPermissionHandler;
import dv.dimonvideo.dvadmin.util.WidgetProvider;
import dv.dimonvideo.dvadmin.util.WidgetUpdateWorker;

public class MainActivity extends AppCompatActivity implements Adapter.ItemClickListener {
    private Adapter adapter;
    private RequestPermissionHandler mRequestPermissionHandler;
    private boolean doubleBackToExitPressedOnce = false;
    private LayoutBinding binding; // Убрано static
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private MainViewModel viewModel;
    private final Handler backPressHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(); // Для асинхронных задач
    private static final String PREF_NAMES_KEY = "cached_names";
    private static final String PREF_COUNTS_KEY = "cached_counts";
    private static final String PREF_SUBTITLE_KEY = "cached_subtitle";
    private static final int REQUEST_CODE_SETTINGS = 1001;
    private ActivityResultLauncher<Intent> settingsLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        final String is_dark = AppController.getInstance(getApplicationContext()).isDark();
        if (Objects.equals(is_dark, "true"))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else if (Objects.equals(is_dark, "system"))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);

        try {
            Analytics.init(this);
        } catch (Exception e) {
            Log.e(Config.TAG, "Failed to initialize Analytics", e);
        }

        binding = LayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adjustFontScale(getResources().getConfiguration());

        toolbar = binding.toolbar;
        toolbar.setTitle(getResources().getString(R.string.app_name));
        setSupportActionBar(toolbar);

        recyclerView = binding.rv;
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        SwipeRefreshLayout swipeRefreshLayout = binding.swipeLayout;
        swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.fetchData(MainActivity.this);
            swipeRefreshLayout.setRefreshing(false);
        });


        // shortcuts
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
            try {
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

                // Выполняем setDynamicShortcuts в рабочем потоке
                executorService.execute(() -> {
                    try {
                        shortcutManager.setDynamicShortcuts(Arrays.asList(logUploaderShortcut, logShortcut));
                        Log.d(Config.TAG, "Dynamic shortcuts set successfully");
                    } catch (Exception e) {
                        Log.e(Config.TAG, "Failed to set dynamic shortcuts", e);
                    }
                });
            } catch (Exception e) {
                Log.e(Config.TAG, "Failed to prepare dynamic shortcuts", e);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mRequestPermissionHandler = new RequestPermissionHandler();
            handlePerm();
        }

        // onBackPressed
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    finish();
                    return;
                }
                doubleBackToExitPressedOnce = true;
                Toast.makeText(getApplicationContext(), getString(R.string.press_twice), Toast.LENGTH_SHORT).show();
                backPressHandler.postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
            }
        };

        OnBackPressedDispatcher onBackPressedDispatcher = getOnBackPressedDispatcher();
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        setupRecyclerView();
        viewModel.fetchData(this);

        scheduleWidgetUpdate();
        triggerImmediateWidgetUpdate();

        // Инициализация ActivityResultLauncher
        settingsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Данные обновлены, вызов fetchData
                        Log.w("---", "refresh");
                        viewModel.fetchData(MainActivity.this);
                    }
                }
        );
    }

    private void scheduleWidgetUpdate() {
        PeriodicWorkRequest updateRequest = new PeriodicWorkRequest.Builder(
                WidgetUpdateWorker.class, 15, TimeUnit.MINUTES)
                .addTag(WidgetUpdateWorker.TAG)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                WidgetUpdateWorker.TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                updateRequest);
    }

    private void triggerImmediateWidgetUpdate() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        ComponentName widgetComponent = new ComponentName(this, WidgetProvider.class);
        int[] widgetIds = appWidgetManager.getAppWidgetIds(widgetComponent);

        if (widgetIds.length == 0) {
            Log.d(Config.TAG, "No widgets found to update");
            return;
        }

        for (int appWidgetId : widgetIds) {
            Intent intent = new Intent(this, WidgetProvider.class);
            intent.setAction(WidgetProvider.ACTION_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            sendBroadcast(intent);

            Data data = new Data.Builder()
                    .putInt(WidgetUpdateWorker.KEY_WIDGET_ID, appWidgetId)
                    .build();
            OneTimeWorkRequest updateRequest = new OneTimeWorkRequest.Builder(WidgetUpdateWorker.class)
                    .setInputData(data)
                    .addTag(WidgetUpdateWorker.TAG)
                    .build();
            WorkManager.getInstance(this).enqueue(updateRequest);
        }
    }

    private void setupRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(this, R.drawable.divider)));
        recyclerView.addItemDecoration(dividerItemDecoration);

        viewModel.getCombinedData().observe(this, pair -> {
            if (pair != null && pair.first != null && pair.second != null) {
                if (pair.first.isEmpty() && pair.second.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    binding.emptyView.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    binding.emptyView.setVisibility(View.GONE);
                    adapter = new Adapter(this, pair.first, pair.second);
                    adapter.setClickListener(MainActivity.this);
                    recyclerView.setAdapter(adapter);
                }
            }
        });

        viewModel.getSubtitle().observe(this, subtitle -> {
            if (toolbar != null) {
                toolbar.setSubtitle(subtitle);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            settingsLauncher.launch(intent);
            return true;
        }
        if (id == R.id.action_refresh) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(PREF_NAMES_KEY);
            editor.remove(PREF_COUNTS_KEY);
            editor.remove(PREF_SUBTITLE_KEY);
            editor.apply();
            viewModel.fetchData(MainActivity.this);
            return true;
        }
        if (id == R.id.action_bd) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getString(R.string.action_bd));
            WebView wv = new WebView(this);
            wv.loadUrl(Config.HOST_URL + "/dvadminapi.php?op=9");
            wv.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }
            });
            alert.setView(wv);
            alert.setNegativeButton(getString(R.string.action_close), (dialog, id12) -> {
                wv.destroy();
                dialog.dismiss();
            });
            alert.show();
            return true;
        }
        if (id == R.id.action_whoaddedfiles) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getString(R.string.action_whoaddedfiles));
            WebView wv = new WebView(this);
            wv.loadUrl(Config.HOST_URL + "/dvadminapi.php?op=12");
            wv.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }
            });
            alert.setView(wv);
            alert.setNegativeButton(getString(R.string.action_close), (dialog, id13) -> {
                wv.destroy();
                dialog.dismiss();
            });
            alert.show();
            return true;
        }
        if (id == R.id.action_lastban) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getString(R.string.action_lastbans));
            WebView wv = new WebView(this);
            wv.loadUrl(Config.HOST_URL + "/dvadminapi.php?op=14");
            wv.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }
            });
            alert.setView(wv);
            alert.setNegativeButton(getString(R.string.action_close), (dialog, id14) -> {
                wv.destroy();
                dialog.dismiss();
            });
            alert.show();
            return true;
        }
        if (id == R.id.action_lastdel) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getString(R.string.action_lastdel));
            WebView wv = new WebView(this);
            wv.loadUrl(Config.HOST_URL + "/dvadminapi.php?op=15");
            wv.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }
            });
            alert.setView(wv);
            alert.setNegativeButton(getString(R.string.action_close), (dialog, id15) -> {
                wv.destroy();
                dialog.dismiss();
            });
            alert.show();
            return true;
        }
        if (id == R.id.action_lasttopics) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getString(R.string.action_lasttopics));
            WebView wv = new WebView(this);
            wv.loadUrl(Config.HOST_URL + "/dvadminapi.php?op=11");
            wv.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }
            });
            alert.setView(wv);
            alert.setNegativeButton(getString(R.string.action_close), (dialog, id16) -> {
                wv.destroy();
                dialog.dismiss();
            });
            alert.show();
            return true;
        }
        if (id == R.id.action_lastcom) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getString(R.string.action_lastcom));
            WebView wv = new WebView(this);
            wv.loadUrl(Config.HOST_URL + "/dvadminapi.php?op=13");
            wv.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }
            });
            alert.setView(wv);
            alert.setNegativeButton(getString(R.string.action_close), (dialog, id1) -> {
                wv.destroy();
                dialog.dismiss();
            });
            alert.show();
            return true;
        }
        if (id == R.id.action_others) {
            String url = "https://play.google.com/store/apps/dev?id=6091758746633814135";
            if (!BuildConfig.GOOGLE) url = Config.BASE_URL + "/android.html";
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            try {
                startActivity(browserIntent);
            } catch (Exception e) {
                Log.e(Config.TAG, "Failed to open browser", e);
                Toast.makeText(this, R.string.error_server, Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        if (id == R.id.action_feedback) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.fromParts("mailto", getString(R.string.app_mail), null));
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " Feedback");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(intent);
            } catch (Exception e) {
                Log.e(Config.TAG, "Failed to open email client", e);
                Toast.makeText(this, R.string.error_server, Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(View view, int position) {
        String item = adapter.getItem(position);
        String url = null;
        String actionAdmin = null;

        if (item.equals(getString(R.string.uploader))) {
            url = Config.BASE_URL + "/logs/uploader/0";
            actionAdmin = "uploader";
        } else if (item.equals(getString(R.string.vuploader))) {
            url = Config.BASE_URL + "/logs/vuploader/0";
            actionAdmin = "vuploader";
        } else if (item.equals(getString(R.string.muzon))) {
            url = Config.BASE_URL + "/logs/muzon/0";
            actionAdmin = "muzon";
        } else if (item.equals(getString(R.string.usernews))) {
            url = Config.BASE_URL + "/logs/usernews/0";
            actionAdmin = "usernews";
        } else if (item.equals(getString(R.string.gallery))) {
            url = Config.BASE_URL + "/logs/gallery/0";
            actionAdmin = "gallery";
        } else if (item.equals(getString(R.string.devices))) {
            url = Config.BASE_URL + "/logs/device/0";
            actionAdmin = "device";
        } else if (item.equals(getString(R.string.forum))) {
            url = Config.BASE_URL + "/fadmin";
        } else if (item.equals(getString(R.string.abuse_file))) {
            url = Config.BASE_URL + "/forum/topic_1728146352";
        } else if (item.equals(getString(R.string.abuse_forum))) {
            url = Config.BASE_URL + "/forum/topic_1728146368";
        }

        if (startActivityForItem(url, actionAdmin)) {
            Toast.makeText(this, item, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean startActivityForItem(String url, String actionAdmin) {
        if (url == null && actionAdmin == null) {
            Log.e(Config.TAG, "Invalid URL and actionAdmin");
            Toast.makeText(this, R.string.error_server, Toast.LENGTH_SHORT).show();
            return false;
        }
        Intent intent;
        if (AppController.getInstance(getApplicationContext()).is_client() && actionAdmin != null) {
            intent = new Intent("com.dimonvideo.client.dvadmin");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("action_admin", actionAdmin);
        } else {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        }
        try {
            startActivity(intent);
            return true;
        } catch (Exception e) {
            Log.e(Config.TAG, "Failed to start activity", e);
            Toast.makeText(this, R.string.error_server, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        backPressHandler.removeCallbacksAndMessages(null);
        if (ProgressHelper.isDialogVisible()) ProgressHelper.dismissDialog();
        executorService.shutdown(); // Завершаем ExecutorService
        binding = null; // Очищаем binding
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void adjustFontScale(Configuration configuration) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        configuration.fontScale = Float.parseFloat(Objects.requireNonNull(sharedPrefs.getString("dvc_scale", "1.0f")));
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (wm == null) {
            Log.e(Config.TAG, "WindowManager is null");
            return;
        }
        wm.getDefaultDisplay().getMetrics(metrics);
        metrics.scaledDensity = configuration.fontScale * metrics.density;
        getBaseContext().getResources().updateConfiguration(configuration, metrics);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void handlePerm() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mRequestPermissionHandler.requestPermission(this, new String[]{
                Manifest.permission.POST_NOTIFICATIONS
        }, 123, new RequestPermissionHandler.RequestPermissionListener() {
            @Override
            public void onSuccess() {
                Log.d(Config.TAG, "Notification permission granted");
            }

            @Override
            public void onFailed() {
                Toast.makeText(MainActivity.this, getString(R.string.perm_invalid), Toast.LENGTH_SHORT).show();
            }
        });
    }

}