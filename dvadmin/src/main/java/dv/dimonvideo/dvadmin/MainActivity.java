package dv.dimonvideo.dvadmin;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    MyRecyclerViewAdapter adapter;
    SwipeRefreshLayout swipLayout;
    String countUploader, countVuploader, countMuzon, countUsernews, countGallery, countDevices, countForum, countTic, countVisitors, countSpace, countAfile, countAforum;
    String countUrl = "https://api.dimonvideo.ru/smart/dvadminapi.php?op=18";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(true);
        swipLayout = this.findViewById(R.id.swipe_layout);
        swipLayout.setOnRefreshListener(this);

        set_adapter();
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

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, countUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);

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

                            count.clear();
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


                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), getString(R.string.error_network_timeout), Toast.LENGTH_LONG).show();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
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
            wv.loadUrl("https://api.dimonvideo.ru/smart/dvadminapi.php?op=9");
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
            wv.loadUrl("https://api.dimonvideo.ru/smart/dvadminapi.php?op=12");
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