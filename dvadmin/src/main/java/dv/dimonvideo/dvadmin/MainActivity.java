package dv.dimonvideo.dvadmin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
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
    String countUploader, countVuploader, countMuzon, countUsernews, countGallery, countDevices, countForum, countTic, countVisitors, countSpace;
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

        final ArrayList<String> count = new ArrayList<>();
        count.add(countUploader);
        count.add(countVuploader);
        count.add(countMuzon);
        count.add(countUsernews);
        count.add(countGallery);
        count.add(countDevices);
        count.add(countForum);
        count.add(countSpace);
        count.add(countVisitors);
        count.add(countTic);

        final ArrayList<String> Names = new ArrayList<>();
        Names.add(getString(R.string.uploader));
        Names.add(getString(R.string.vuploader));
        Names.add(getString(R.string.muzon));
        Names.add(getString(R.string.usernews));
        Names.add(getString(R.string.gallery));
        Names.add(getString(R.string.devices));
        Names.add(getString(R.string.forum));
        Names.add(getString(R.string.space));
        Names.add(getString(R.string.visitors));
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
                            countSpace = jsonObject.getString("space");
                            countTic = jsonObject.getString("tic");
                            countVisitors = jsonObject.getString("visitors");

                            count.clear();
                            count.add(countUploader);
                            count.add(countVuploader);
                            count.add(countMuzon);
                            count.add(countUsernews);
                            count.add(countGallery);
                            count.add(countDevices);
                            count.add(countForum);
                            count.add(countSpace);
                            count.add(countVisitors);
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

        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_refresh) {
            recreate();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(View view, int position) {

        if (position == 0){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://dimonvideo.ru/logs/uploader/0"));
            try {
                startActivity(browserIntent);
            } catch (Throwable ignored) {
            }
        } else if (position == 1){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://dimonvideo.ru/logs/vuploader/0"));
            try {
                startActivity(browserIntent);
            } catch (Throwable ignored) {
            }
        } else if (position == 2){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://dimonvideo.ru/logs/muzon/0"));
            try {
                startActivity(browserIntent);
            } catch (Throwable ignored) {
            }
        } else if (position == 3){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://dimonvideo.ru/logs/usernews/0"));
            try {
                startActivity(browserIntent);
            } catch (Throwable ignored) {
            }
        } else if (position == 4){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://dimonvideo.ru/logs/gallery/0"));
            try {
                startActivity(browserIntent);
            } catch (Throwable ignored) {
            }
        } else if (position == 5){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://dimonvideo.ru/logs/device/0"));
            try {
                startActivity(browserIntent);
            } catch (Throwable ignored) {
            }
        } else if (position == 6){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://dimonvideo.ru/fadmin"));
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