package me.ccrama.redditslide.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

import me.ccrama.redditslide.Adapters.AlbumView;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Views.PreCachingLayoutManager;
import me.ccrama.redditslide.Views.ToolbarColorizeHelper;


/**
 * Created by ccrama on 3/5/2015.
 */
public class Album extends BaseActivity {
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    public void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);


        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getTheme().applyStyle(new ColorPreferences(this).getFontStyle().getBaseId(), true);

        setContentView(R.layout.album);

        final Toolbar b = (Toolbar) findViewById(R.id.toolbar);
        b.setTitle(R.string.album_loading);
        ToolbarColorizeHelper.colorizeToolbar(b, Color.WHITE, this);
        setSupportActionBar(b);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        String rawDat = cutEnds(getIntent().getExtras().getString("url", ""));
        if (rawDat.contains("gallery")) {
            gallery = true;
        }
        String rawdat2 = rawDat;
        if (rawdat2.substring(rawDat.lastIndexOf("/"), rawdat2.length()).length() < 4) {
            rawDat = rawDat.replace(rawDat.substring(rawDat.lastIndexOf("/"), rawdat2.length()), "");
        }
        if (rawDat.isEmpty()) {
            finish();
        } else {

            new AsyncImageLoader().execute(getHash(rawDat));

        }

    }

    boolean gallery = false;

    private String getHash(String s) {
        String next = s.substring(s.lastIndexOf("/"), s.length());
        if (next.length() < 5) {
            return getHash(s.replace(next, ""));
        } else {
            return next;
        }

    }

    private String cutEnds(String s) {
        if (s.endsWith("/")) {
            return s.substring(0, s.length() - 1);
        } else {
            return s;
        }
    }


    private class AsyncImageLoader extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... sub) {
            if (gallery) {
                Ion.with(Album.this)
                        .load("https://imgur.com/gallery/" + sub[0] + ".json")
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                if (result != null && result.has("data")) {
                                    Log.v("Slide", result.toString());


                                    ArrayList<JsonElement> jsons = new ArrayList<>();


                                    if (!result.getAsJsonObject("data").getAsJsonObject("image").get("is_album").getAsBoolean()) {
                                        if (result.getAsJsonObject("data").getAsJsonObject("image").get("mimetype").getAsString().contains("gif")) {
                                            Intent i = new Intent(Album.this, GifView.class);
                                            i.putExtra("url", "http://imgur.com/" + result.getAsJsonObject("data").getAsJsonObject("image").get("hash").getAsString() + ".gif"); //could be a gif
                                            startActivity(i);
                                        } else {
                                            Intent i = new Intent(Album.this, FullscreenImage.class);
                                            i.putExtra("url", "http://imgur.com/" + result.getAsJsonObject("data").getAsJsonObject("image").get("hash").getAsString() + ".png"); //could be a gif
                                            startActivity(i);
                                        }
                                        finish();

                                    } else {
                                        getSupportActionBar().setTitle("Gallery");

                                        JsonArray obj = result.getAsJsonObject("data").getAsJsonObject("image").getAsJsonObject("album_images").get("images").getAsJsonArray();
                                        if (obj != null && !obj.isJsonNull() && obj.size() > 0) {

                                            for (JsonElement o : obj) {
                                                jsons.add(o);
                                            }


                                            RecyclerView v = (RecyclerView) findViewById(R.id.images);
                                            final PreCachingLayoutManager mLayoutManager;
                                            mLayoutManager = new PreCachingLayoutManager(Album.this);
                                            v.setLayoutManager(mLayoutManager);
                                            v.setAdapter(new AlbumView(Album.this, jsons, true));

                                        }
                                    }
                                } else {

                                    new AlertDialogWrapper.Builder(Album.this)
                                            .setTitle(R.string.album_err_not_found)
                                            .setMessage(R.string.album_err_msg_not_found)
                                            .setCancelable(false)
                                            .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    finish();
                                                }
                                            }).create().show();
                                }
                            }

                        });
            } else {
                Log.v("Slide", "http://api.imgur.com/2/album" + sub[0] + ".json");
                Ion.with(Album.this)
                        .load("http://api.imgur.com/2/album" + sub[0] + ".json")
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                Log.v("Slide", result.toString());


                                ArrayList<JsonElement> jsons = new ArrayList<>();

                                if (result.has("album")) {
                                    if (result.get("album").getAsJsonObject().has("title") && !result.get("album").isJsonNull() && !result.get("album").getAsJsonObject().get("title").isJsonNull()) {
                                        getSupportActionBar().setTitle(result.get("album").getAsJsonObject().get("title").getAsString());
                                    } else {
                                        getSupportActionBar().setTitle("Album");

                                    }
                                    JsonObject obj = result.getAsJsonObject("album");
                                    if (obj != null && !obj.isJsonNull() && obj.has("images")) {

                                        final JsonArray jsonAuthorsArray = obj.get("images").getAsJsonArray();

                                        for (JsonElement o : jsonAuthorsArray) {
                                            jsons.add(o);
                                        }


                                        RecyclerView v = (RecyclerView) findViewById(R.id.images);
                                        final PreCachingLayoutManager mLayoutManager;
                                        mLayoutManager = new PreCachingLayoutManager(Album.this);
                                        v.setLayoutManager(mLayoutManager);
                                        v.setAdapter(new AlbumView(Album.this, jsons, false));

                                    } else {

                                        new AlertDialogWrapper.Builder(Album.this)
                                                .setTitle(R.string.album_err_not_found)
                                                .setMessage(R.string.album_err_msg_not_found)
                                                .setCancelable(false)
                                                .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        finish();
                                                    }
                                                }).create().show();
                                    }
                                } else {

                                    new AlertDialogWrapper.Builder(Album.this)
                                            .setTitle(R.string.album_err_not_found)
                                            .setMessage(R.string.album_err_msg_not_found)
                                            .setCancelable(false)
                                            .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    finish();
                                                }
                                            }).create().show();
                                }
                            }
                        });
            }

            return null;

        }


    }


}