/**
 * Основная активность приложения DVAdmin, предоставляющая интерфейс для модераторов сайта
 * dimonvideo.ru. Отображает состояние сайта и количество пользовательских материалов,
 * ожидающих проверки. Активность интегрируется с приложением DVClient для передачи ссылок
 * на дальнейшую обработку и поддерживает виджеты для быстрого доступа к данным модерации.
 */
package dv.dimonvideo.dvadmin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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

/**
 * Реализует интерфейс {@link Adapter.ItemClickListener} для обработки нажатий на категории
 * модерации, позволяя модераторам переходить к журналам или передавать ссылки в приложение
 * DVClient.
 */
public class MainActivity extends AppCompatActivity implements Adapter.ItemClickListener {
    /** Адаптер для отображения категорий модерации и их количества в RecyclerView. */
    private Adapter adapter;

    /** Обработчик для запроса разрешений на уведомления на Android 13 и выше. */
    private RequestPermissionHandler mRequestPermissionHandler;

    /** Флаг для отслеживания двойного нажатия кнопки "Назад" для выхода из приложения. */
    private boolean doubleBackToExitPressedOnce = false;

    /** Привязка к макету активности. */
    private LayoutBinding binding;

    /** RecyclerView для отображения категорий модерации и их количества. */
    private RecyclerView recyclerView;

    /** Панель инструментов активности, отображающая заголовок и подзаголовок приложения. */
    private Toolbar toolbar;

    /** ViewModel для управления данными модерации и взаимодействия с API. */
    private MainViewModel viewModel;

    /** Обработчик для выполнения отложенных задач, таких как сброс флага двойного нажатия. */
    private final Handler backPressHandler = new Handler(Looper.getMainLooper());

    /** Сервис для выполнения фоновых задач, таких как установка динамических ярлыков. */
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /** Ключ для кэширования названий категорий модерации из {@link Config#PREF_NAMES_KEY}. */
    private static final String PREF_NAMES_KEY = Config.PREF_NAMES_KEY;

    /** Ключ для кэширования количества материалов из {@link Config#PREF_COUNTS_KEY}. */
    private static final String PREF_COUNTS_KEY = Config.PREF_COUNTS_KEY;

    /** Ключ для кэширования подзаголовка из {@link Config#PREF_SUBTITLE_KEY}. */
    private static final String PREF_SUBTITLE_KEY = Config.PREF_SUBTITLE_KEY;

    /** Лаунчер для запуска активности настроек и обработки её результата. */
    private ActivityResultLauncher<Intent> settingsLauncher;

    /** Экземпляр синглтона {@link AppController} для доступа к глобальным настройкам приложения. */
    private AppController controller;

    /** Приёмник широковещательных сообщений для обработки событий, связанных с виджетами. */
    private final BroadcastReceiver widgetReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AppWidgetManager.ACTION_APPWIDGET_ENABLED.equals(intent.getAction()) ||
                    AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(intent.getAction())) {
                triggerImmediateWidgetUpdate();
            }
        }
    };

    /**
     * Инициализирует активность, настраивая интерфейс, ViewModel и обновления виджетов.
     * Регистрирует {@link BroadcastReceiver} для событий виджетов и запрашивает необходимые
     * разрешения.
     *
     * @param savedInstanceState Сохранённое состояние активности или null, если запуск новый.
     */
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        controller = AppController.getInstance();
        super.onCreate(savedInstanceState);

        try {
            Analytics.init(this);
        } catch (Exception e) {
            Log.e(Config.TAG, "Ошибка инициализации аналитики", e);
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

                executorService.execute(() -> {
                    try {
                        shortcutManager.setDynamicShortcuts(Arrays.asList(logUploaderShortcut, logShortcut));
                    } catch (Exception e) {
                        Log.e(Config.TAG, "Ошибка установки динамических ярлыков", e);
                    }
                });
            } catch (Exception e) {
                Log.e(Config.TAG, "Ошибка подготовки динамических ярлыков", e);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mRequestPermissionHandler = new RequestPermissionHandler();
            handlePerm();
        }

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

        viewModel.getLoadingState().observe(this, isLoading -> {
            Log.d("DVAdminApp", "Состояние загрузки: " + isLoading);
            if (isLoading) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.rv.setVisibility(View.GONE);
            } else {
                binding.progressBar.setVisibility(View.GONE);
                binding.rv.setVisibility(View.VISIBLE);
                Log.d("DVAdminApp", "RecyclerView виден: " + (binding.rv.getVisibility() == View.VISIBLE));
            }
        });

        viewModel.fetchData(this);

        scheduleWidgetUpdate();
        triggerImmediateWidgetUpdate();

        IntentFilter filter = new IntentFilter();
        filter.addAction(AppWidgetManager.ACTION_APPWIDGET_ENABLED);
        filter.addAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            registerReceiver(widgetReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(widgetReceiver, filter);
        }

        settingsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            if (data.getBooleanExtra("theme_changed", false) || data.getBooleanExtra("font_scale_changed", false)) {
                                // Пересоздаём активность для применения новой темы или размера шрифта
                                recreate();
                            }
                        }
                        Log.w("---", "Обновление данных");
                        viewModel.fetchData(MainActivity.this);
                    }
                }
        );
        SwipeRefreshLayout swipeRefreshLayout = binding.swipeLayout;
        swipeRefreshLayout.setOnRefreshListener(() -> {
            recreate();
            swipeRefreshLayout.setRefreshing(false);

        });
    }

    /**
     * Планирует периодические обновления виджетов с помощью {@link WorkManager} каждые 15 минут
     * для обновления данных модерации.
     */
    private void scheduleWidgetUpdate() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        ComponentName widgetComponent = new ComponentName(this, WidgetProvider.class);
        int[] widgetIds = appWidgetManager.getAppWidgetIds(widgetComponent);

        if (widgetIds.length == 0) {
            return;
        }

        PeriodicWorkRequest updateRequest = new PeriodicWorkRequest.Builder(
                WidgetUpdateWorker.class, 30, TimeUnit.MINUTES)
                .addTag(WidgetUpdateWorker.TAG)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                WidgetUpdateWorker.TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                updateRequest);
    }

    /**
     * Запускает немедленное обновление всех активных виджетов с помощью {@link WorkManager} для
     * получения последних данных модерации.
     */
    private void triggerImmediateWidgetUpdate() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        ComponentName widgetComponent = new ComponentName(this, WidgetProvider.class);
        int[] widgetIds = appWidgetManager.getAppWidgetIds(widgetComponent);

        if (widgetIds.length == 0) {
            Log.d(Config.TAG, "Виджеты для обновления не найдены");
            return;
        }

        for (int appWidgetId : widgetIds) {
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

    /**
     * Настраивает {@link RecyclerView} для отображения категорий модерации и их количества,
     * отслеживая изменения данных из {@link MainViewModel}.
     */
    private void setupRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(this, R.drawable.divider)));
        recyclerView.addItemDecoration(dividerItemDecoration);

        viewModel.getCombinedData().observe(this, pair -> {
            if (pair != null && pair.first != null && pair.second != null) {
                Log.d("DVAdminApp", "Данные получены: названия=" + pair.first + ", количества=" + pair.second);
                if (pair.first.isEmpty() && pair.second.isEmpty()) {
                    Log.d("DVAdminApp", "Данные пусты, скрываем RecyclerView");
                    recyclerView.setVisibility(View.GONE);
                    binding.emptyView.setVisibility(View.VISIBLE);
                    adapter = null; // Очищаем адаптер
                    recyclerView.setAdapter(null);
                } else {
                    Log.d("DVAdminApp", "Данные есть, показываем RecyclerView");
                    recyclerView.setVisibility(View.VISIBLE);
                    binding.emptyView.setVisibility(View.GONE);
                    adapter = new Adapter(this, pair.first, pair.second);
                    adapter.setClickListener(MainActivity.this); // Устанавливаем слушатель каждый раз
                    recyclerView.setAdapter(adapter);
                }
            } else {
                Log.w("DVAdminApp", "Данные из ViewModel null");
            }
        });

        viewModel.getSubtitle().observe(this, subtitle -> {
            if (toolbar != null) {
                toolbar.setSubtitle(subtitle);
            }
        });
    }

    /**
     * Создаёт меню опций для активности, предоставляя модераторам действия, такие как
     * обновление данных или просмотр дополнительных отчётов.
     *
     * @param menu Меню для заполнения.
     * @return True, если меню успешно создано.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    /**
     * Обрабатывает выбор пунктов меню, позволяя модераторам выполнять действия, такие как
     * обновление данных, просмотр дней рождений, банов или отправка обратной связи.
     *
     * @param item Выбранный пункт меню.
     * @return True, если пункт обработан, иначе false.
     */
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
            viewModel.fetchBirthdays();
            viewModel.getBirthdays().observe(this, html -> {
                showWebViewDialog(getString(R.string.action_bd), html);
            });
            return true;
        }

        if (id == R.id.action_whoaddedfiles) {
            viewModel.fetchWhoAddedFiles();
            viewModel.getWhoAddedFiles().observe(this, html -> {
                showWebViewDialog(getString(R.string.action_whoaddedfiles), html);
            });
            return true;
        }

        if (id == R.id.action_lastban) {
            viewModel.fetchLastBans();
            viewModel.getLastBans().observe(this, html -> {
                showWebViewDialog(getString(R.string.action_lastbans), html);
            });
            return true;
        }

        if (id == R.id.action_lastdel) {
            viewModel.fetchLastDeleted();
            viewModel.getLastDeleted().observe(this, html -> {
                showWebViewDialog(getString(R.string.action_lastdel), html);
            });
            return true;
        }

        if (id == R.id.action_lasttopics) {
            viewModel.fetchLastTopics();
            viewModel.getLastTopics().observe(this, html -> {
                showWebViewDialog(getString(R.string.action_lasttopics), html);
            });
            return true;
        }

        if (id == R.id.action_lastcom) {
            viewModel.fetchLastComments();
            viewModel.getLastComments().observe(this, html -> {
                showWebViewDialog(getString(R.string.action_lastcom), html);
            });
            return true;
        }

        if (id == R.id.action_others) {
            String url = "https://play.google.com/store/apps/dev?id=6091758746633814135";
            if (!BuildConfig.GOOGLE) url = Config.BASE_URL + "/android.html";
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            try {
                startActivity(browserIntent);
            } catch (Exception e) {
                Log.e(Config.TAG, "Ошибка открытия браузера", e);
                Toast.makeText(this, R.string.error_server, Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        if (id == R.id.action_feedback) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.fromParts("mailto", getString(R.string.app_mail), null));
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " Обратная связь");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(intent);
            } catch (Exception e) {
                Log.e(Config.TAG, "Ошибка открытия почтового клиента", e);
                Toast.makeText(this, R.string.error_server, Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Отображает диалог с {@link WebView} для показа HTML-контента (например, дней рождений,
     * банов) или {@link TextView} для сообщений об ошибках или отсутствии данных.
     *
     * @param title Заголовок диалога.
     * @param html  HTML-контент для отображения, или null/пустой для состояний ошибки/отсутствия данных.
     */
    private void showWebViewDialog(String title, String html) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(title);

        if (html == null) {
            TextView textView = new TextView(this);
            textView.setText(R.string.error_server);
            textView.setPadding(16, 16, 16, 16);
            alert.setView(textView);
        } else if (html.isEmpty()) {
            TextView textView = new TextView(this);
            textView.setText(R.string.no_data);
            textView.setPadding(16, 16, 16, 16);
            alert.setView(textView);
        } else {
            WebView wv = new WebView(this);
            wv.loadData(html, "text/html", "UTF-8");
            alert.setView(wv);
            alert.setNegativeButton(getString(R.string.action_close), (dialog, which) -> {
                wv.destroy();
                dialog.dismiss();
            });
        }

        alert.setNegativeButton(getString(R.string.action_close), (dialog, which) -> dialog.dismiss());
        alert.show();
    }

    /**
     * Обрабатывает нажатия на категории модерации в {@link RecyclerView}, переходя к
     * соответствующей странице журналов или передавая ссылку в приложение DVClient.
     *
     * @param view     Нажатый вид.
     * @param position Позиция нажатого элемента в RecyclerView.
     */
    @Override
    public void onItemClick(View view, int position) {
        Log.d("DVAdminApp", "onItemClick вызван для позиции: " + position);
        String item = adapter.getItem(position);
        Log.d("DVAdminApp", "Выбранный элемент: " + item);
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

    /**
     * Запускает активность для открытия URL журнала модерации или передаёт ссылку в приложение
     * DVClient.
     *
     * @param url         URL журнала модерации, или null, если не применимо.
     * @param actionAdmin Идентификатор действия для DVClient, или null, если не применимо.
     * @return True, если активность успешно запущена, иначе false.
     */
    private boolean startActivityForItem(String url, String actionAdmin) {
        if (url == null && actionAdmin == null) {
            Log.e(Config.TAG, "Недопустимый URL и actionAdmin");
            Toast.makeText(this, R.string.error_server, Toast.LENGTH_SHORT).show();
            return false;
        }
        Intent intent;
        if (controller.is_client() && actionAdmin != null) {
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
            Log.e(Config.TAG, "Ошибка запуска активности", e);
            Toast.makeText(this, R.string.error_server, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /**
     * Очищает ресурсы и отменяет регистрацию приёмника виджетов при уничтожении активности.
     */
    @Override
    protected void onDestroy() {
        unregisterReceiver(widgetReceiver);
        backPressHandler.removeCallbacksAndMessages(null);
        executorService.shutdownNow();
        if (ProgressHelper.isDialogVisible()) ProgressHelper.dismissDialog();
        recyclerView.setAdapter(null);
        binding = null;
        super.onDestroy();
    }

    /**
     * Вызывается при возобновлении активности, обеспечивая актуальность интерфейса.
     */
    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Вызывается при приостановке активности, сохраняя необходимое состояние.
     */
    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * Настраивает масштаб шрифта на основе пользовательских предпочтений, обеспечивая
     * единообразие размера текста в приложении.
     *
     * @param configuration Текущая конфигурация для изменения.
     */
    private void adjustFontScale(Configuration configuration) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        configuration.fontScale = Float.parseFloat(Objects.requireNonNull(sharedPrefs.getString("dvc_scale", "1.0f")));
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (wm == null) {
            Log.e(Config.TAG, "WindowManager не доступен");
            return;
        }
        wm.getDefaultDisplay().getMetrics(metrics);
        metrics.scaledDensity = configuration.fontScale * metrics.density;
        getBaseContext().getResources().updateConfiguration(configuration, metrics);
    }

    /**
     * Запрашивает разрешение на уведомления на Android 13 и выше, необходимое для обновления
     * виджетов.
     */
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
                Log.d(Config.TAG, "Разрешение на уведомления получено");
            }

            @Override
            public void onFailed() {
                Toast.makeText(MainActivity.this, getString(R.string.perm_invalid), Toast.LENGTH_SHORT).show();
            }
        });
    }
}