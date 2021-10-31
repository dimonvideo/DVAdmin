package dv.dimonvideo.dvadmin;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    MyRecyclerViewAdapter adapter;
    SwipeRefreshLayout swipLayout;
    String countUploader, countVuploader, countMuzon, countUsernews, countGallery, countDevices, countForum, countTic, countVisitors, countSpace, countAfile, countAforum, today;
    String hostUrl = "https://api.dimonvideo.net";
    String countUrl = hostUrl + "/smart/dvadminapi.php?op=18";
    String adminUrl = "https://dimonvideo.ru/logs";
    String uplUrl = "https://dimonvideo.ru/logs/uploader/0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(true);
        swipLayout = this.findViewById(R.id.swipe_layout);
        swipLayout.setOnRefreshListener(this);
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        set_adapter();

        // shortcuts
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {




            ShortcutManagerCompat shortcutManager = getSystemService(ShortcutManagerCompat.class);

            ShortcutInfoCompat logUploaderShortcut = new ShortcutInfoCompat.Builder(this, "shortcut_visit_1")
                    .setShortLabel(getString(R.string.action_admin_upl))
                    .setIcon(IconCompat.createWithResource(this, R.drawable.ic_launcher))
                    .setIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(uplUrl)))
                    .build();

            ShortcutInfoCompat logShortcut = new ShortcutInfoCompat.Builder(this, "shortcut_visit")
                    .setShortLabel(getString(R.string.action_admin))
                    .setIcon(IconCompat.createWithResource(this, R.drawable.ic_launcher))
                    .setIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(adminUrl)))
                    .build();


                ShortcutManagerCompat.setDynamicShortcuts(this, Arrays.asList(logUploaderShortcut, logShortcut));

        }


    }


    // получение данных
    private void set_adapter(){

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        final boolean is_uploader = sharedPrefs.getBoolean("uploader",true);
        final boolean is_vuploader = sharedPrefs.getBoolean("vuploader",true);
        final boolean is_muzon = sharedPrefs.getBoolean("muzon",true);
        final boolean is_usernews = sharedPrefs.getBoolean("usernews",true);
        final boolean is_gallery = sharedPrefs.getBoolean("gallery",true);
        final boolean is_devices = sharedPrefs.getBoolean("devices",true);
        final boolean is_forum = sharedPrefs.getBoolean("forum",true);
        final boolean is_abuse_file = sharedPrefs.getBoolean("abuse_file",true);
        final boolean is_abuse_forum = sharedPrefs.getBoolean("abuse_forum",true);
        final boolean is_space = sharedPrefs.getBoolean("space",true);
        final boolean is_visitors = sharedPrefs.getBoolean("visitors",true);
        final boolean is_notify = sharedPrefs.getBoolean("sync",true);

        final ArrayList<String> count = new ArrayList<>();

        final ArrayList<String> Names = new ArrayList<>();
        if (BuildConfig.FLAVOR.equals("DVAdminPro")) Names.add(getString(R.string.today));
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
        final ProgressDialog pd = ProgressDialog.show(this,null,getString(R.string.please_wait));

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, countUrl,
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
                        countVisitors = jsonObject.getString("visitors");
                        today = jsonObject.getString("today");

                        count.clear();
                        if (BuildConfig.FLAVOR.equals("DVAdminPro")) count.add(today);
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

                        RecyclerView recyclerView = findViewById(R.id.rv);
                        recyclerView.setHasFixedSize(true);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
                        dividerItemDecoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(MainActivity.this, R.drawable.divider)));
                        recyclerView.addItemDecoration(dividerItemDecoration);
                        adapter = new MyRecyclerViewAdapter(getApplicationContext(), Names, count);
                        adapter.setClickListener(MainActivity.this);
                        recyclerView.setAdapter(adapter);

                        try {
                            FirebaseOptions options = new FirebaseOptions.Builder()
                                    .setApplicationId("1:50549051988:android:a46a6e539a88fde4e7d3c1") // Required for Analytics.
                                    .setProjectId("dvadmin-5a6d2") // Required for Firebase Installations.
                                    .build();
                            FirebaseApp.initializeApp(MainActivity.this, options, "DVAdmin");

                            if (is_notify) {
                                FirebaseMessaging.getInstance().subscribeToTopic("all");
                            } else FirebaseMessaging.getInstance().unsubscribeFromTopic("all");
                        } catch (Throwable ignored) {
                        }

                        if(pd!=null && pd.isShowing()) pd.dismiss();

                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), getString(R.string.error_network_timeout), Toast.LENGTH_LONG).show();
                    }

                }, error -> {
                    if(pd!=null && pd.isShowing()) pd.dismiss();

            Log.e("RESULTfailder",error.getMessage());


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

        queue.add(stringRequest);

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
            startActivityForResult(i, 1);
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
            wv.loadUrl(hostUrl + "/smart/dvadminapi.php?op=9");
            wv.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);

                    return true;
                }
            });

            alert.setView(wv);
            alert.setNegativeButton(getString(R.string.action_close), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            alert.show();
        }
        // who added files now
        if (id == R.id.action_whoaddedfiles) {

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getString(R.string.action_whoaddedfiles));

            WebView wv = new WebView(this);
            wv.loadUrl(hostUrl + "/smart/dvadminapi.php?op=12");
            wv.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);

                    return true;
                }
            });

            alert.setView(wv);
            alert.setNegativeButton(getString(R.string.action_close), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            alert.show();
        }
        // last ban
        if (id == R.id.action_lastban) {

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getString(R.string.action_lastbans));

            WebView wv = new WebView(this);

            wv.loadUrl(hostUrl + "/smart/dvadminapi.php?op=14");
            wv.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);

                    return true;
                }
            });

            alert.setView(wv);
            alert.setNegativeButton(getString(R.string.action_close), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            alert.show();

        }
        // last del uploader
        if (id == R.id.action_lastdel) {

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getString(R.string.action_lastdel));

            WebView wv = new WebView(this);

            wv.loadUrl(hostUrl + "/smart/dvadminapi.php?op=15");
            wv.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);

                    return true;
                }
            });

            alert.setView(wv);
            alert.setNegativeButton(getString(R.string.action_close), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            alert.show();

        }
        // last del forum
        if (id == R.id.action_lasttopics) {

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getString(R.string.action_lasttopics));

            WebView wv = new WebView(this);

            wv.loadUrl(hostUrl + "/smart/dvadminapi.php?op=11");
            wv.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);

                    return true;
                }
            });

            alert.setView(wv);
            alert.setNegativeButton(getString(R.string.action_close), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            alert.show();

        }
        // last del com
        if (id == R.id.action_lastcom) {

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getString(R.string.action_lastcom));

            WebView wv = new WebView(this);

            wv.loadUrl(hostUrl + "/smart/dvadminapi.php?op=13");
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

        if (adapter.getItem(position).equals(getString(R.string.uploader))){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://dimonvideo.ru/logs/uploader/0"));
            try {
                startActivity(browserIntent);
            } catch (Throwable ignored) {
            }
        } else if (adapter.getItem(position).equals(getString(R.string.vuploader))){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://dimonvideo.ru/logs/vuploader/0"));
            try {
                startActivity(browserIntent);
            } catch (Throwable ignored) {
            }
        } else if (adapter.getItem(position).equals(getString(R.string.muzon))){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://dimonvideo.ru/logs/muzon/0"));
            try {
                startActivity(browserIntent);
            } catch (Throwable ignored) {
            }
        } else if (adapter.getItem(position).equals(getString(R.string.usernews))){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://dimonvideo.ru/logs/usernews/0"));
            try {
                startActivity(browserIntent);
            } catch (Throwable ignored) {
            }
        } else if (adapter.getItem(position).equals(getString(R.string.gallery))){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://dimonvideo.ru/logs/gallery/0"));
            try {
                startActivity(browserIntent);
            } catch (Throwable ignored) {
            }
        } else if (adapter.getItem(position).equals(getString(R.string.devices))){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://dimonvideo.ru/logs/device/0"));
            try {
                startActivity(browserIntent);
            } catch (Throwable ignored) {
            }
        } else if (adapter.getItem(position).equals(getString(R.string.forum))){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://dimonvideo.ru/fadmin"));
            try {
                startActivity(browserIntent);
            } catch (Throwable ignored) {
            }
        } else if (adapter.getItem(position).equals(getString(R.string.abuse_file))){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://dimonvideo.ru/forum/topic_1728146352"));
            try {
                startActivity(browserIntent);
            } catch (Throwable ignored) {
            }
        } else if (adapter.getItem(position).equals(getString(R.string.abuse_forum))){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://dimonvideo.ru/forum/topic_1728146368"));
            try {
                startActivity(browserIntent);
            } catch (Throwable ignored) {
            }
        }

        Toast.makeText(this, adapter.getItem(position), Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onKeyLongPress(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        }
        return super.onKeyLongPress(keycode, event);
    }

    @Override
    public void onRefresh() {
        recreate();
    }

}