package me.ccrama.redditslide.Activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

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
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Views.PreCachingLayoutManager;
import me.ccrama.redditslide.Views.ToolbarColorizeHelper;
import me.ccrama.redditslide.util.LogUtil;


/**
 * Created by ccrama on 3/5/2015.
 */
public class Album extends FullScreenActivity {
    public static final String EXTRA_URL = "url";
    boolean gallery = false;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    public void onCreate(Bundle savedInstanceState) {
        overrideRedditSwipeAnywhere();
        super.onCreate(savedInstanceState);
        getTheme().applyStyle(new ColorPreferences(this).getFontStyle().getBaseId(), true);

        setContentView(R.layout.album);

        if (Reddit.imageViewerSolidBackground) {
            findViewById(R.id.root).setBackgroundColor(ContextCompat.getColor(this, R.color.darkbg));
        }

        final Toolbar b = (Toolbar) findViewById(R.id.toolbar);
        b.setTitle(R.string.album_loading);
        ToolbarColorizeHelper.colorizeToolbar(b, Color.WHITE, this);
        setSupportActionBar(b);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        String rawDat = cutEnds(getIntent().getExtras().getString(EXTRA_URL, ""));
        if (rawDat.contains("gallery")) {
            gallery = true;
        }
        if (rawDat.endsWith("/")) {
            rawDat = rawDat.substring(0, rawDat.length() - 1);
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

    private String getHash(String s) {
        String next = s.substring(s.lastIndexOf("/"), s.length());
        if (next.length() < 5) {
            return getHash(s.replace(next, ""));
        } else {
            return next;
        }

    }

    boolean slider;

    private String cutEnds(String s) {
        if (s.endsWith("/")) {
            return s.substring(0, s.length() - 1);
        } else {
            return s;
        }
    }


    private class AsyncImageLoader extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(final String... sub) {
            if (gallery) {
                Ion.with(Album.this)
                        .load("https://imgur.com/gallery/" + sub[0] + ".json")
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                if (result != null && result.has("data")) {
                                    Log.v(LogUtil.getTag(), result.toString());


                                    final ArrayList<JsonElement> jsons = new ArrayList<>();


                                    if (!result.getAsJsonObject("data").getAsJsonObject("image").get("is_album").getAsBoolean()) {
                                        if (result.getAsJsonObject("data").getAsJsonObject("image").get("mimetype").getAsString().contains("gif")) {
                                            Intent i = new Intent(Album.this, GifView.class);
                                            i.putExtra(GifView.EXTRA_URL, "http://imgur.com/" + result.getAsJsonObject("data").getAsJsonObject("image").get("hash").getAsString() + ".gif"); //could be a gif
                                            startActivity(i);
                                        } else {
                                            Intent i = new Intent(Album.this, FullscreenImage.class);
                                            i.putExtra(FullscreenImage.EXTRA_URL, "http://imgur.com/" + result.getAsJsonObject("data").getAsJsonObject("image").get("hash").getAsString() + ".png"); //could be a gif
                                            startActivity(i);
                                        }
                                        finish();

                                    } else {
                                        JsonArray obj = result.getAsJsonObject("data").getAsJsonObject("image").getAsJsonObject("album_images").get("images").getAsJsonArray();
                                        if (obj != null && !obj.isJsonNull() && obj.size() > 0) {

                                            for (JsonElement o : obj) {
                                                jsons.add(o);
                                            }

                                            getSupportActionBar().setTitle(getString(R.string.album_title_count, jsons.size()));


                                            final RecyclerView v = (RecyclerView) findViewById(R.id.images);
                                            final PreCachingLayoutManager mLayoutManager;
                                            mLayoutManager = new PreCachingLayoutManager(Album.this);
                                            v.setLayoutManager(mLayoutManager);
                                            v.setAdapter(new AlbumView(Album.this, jsons, true));
                                            findViewById(R.id.slider).setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v3) {
                                                    if (slider) {
                                                        v.setVisibility(View.VISIBLE);
                                                        final PreCachingLayoutManager mLayoutManager;
                                                        mLayoutManager = new PreCachingLayoutManager(Album.this);
                                                        v.setLayoutManager(mLayoutManager);
                                                        v.setAdapter(new AlbumView(Album.this, jsons, true));
                                                        findViewById(R.id.images_horizontal).setVisibility(View.GONE);
                                                        slider = false;
                                                    } else {
                                                        v.setVisibility(View.GONE);
                                                        ViewPager p = (ViewPager) findViewById(R.id.images_horizontal);
                                                        //   p.setAdapter(new AlbumViewPager(getSupportFragmentManager(), Album.this, jsons, true));
                                                        p.setVisibility(View.VISIBLE);

                                                        slider = true;
                                                    }
                                                }
                                            });

                                        }
                                    }
                                } else {

                                    Intent i = new Intent(Album.this, Website.class);
                                    i.putExtra(Website.EXTRA_URL, "http://imgur.com/gallery/" + sub[0]);

                                    startActivity(i);
                                    finish();
                                    //Catch failed api call
                                }
                            }

                        });
            } else {
                Log.v(LogUtil.getTag(), "http://api.imgur.com/2/album" + sub[0] + ".json");
                Ion.with(Album.this)
                        .load("http://api.imgur.com/2/album" + sub[0] + ".json")
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                Dialog dialog = new AlertDialogWrapper.Builder(Album.this)
                                        .setTitle(R.string.album_err_not_found)
                                        .setMessage(R.string.album_err_msg_not_found)
                                        .setCancelable(false)
                                        .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                finish();
                                            }
                                        }).create();

                                if (result != null) {
                                    Log.v(LogUtil.getTag(), result.toString());

                                    final ArrayList<JsonElement> jsons = new ArrayList<>();

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


                                            final RecyclerView v = (RecyclerView) findViewById(R.id.images);
                                            final PreCachingLayoutManager mLayoutManager;
                                            mLayoutManager = new PreCachingLayoutManager(Album.this);
                                            v.setLayoutManager(mLayoutManager);
                                            v.setAdapter(new AlbumView(Album.this, jsons, false));
                                            findViewById(R.id.slider).setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v3) {
                                                    if (slider) {
                                                        v.setVisibility(View.VISIBLE);
                                                        final PreCachingLayoutManager mLayoutManager;
                                                        mLayoutManager = new PreCachingLayoutManager(Album.this);
                                                        v.setLayoutManager(mLayoutManager);
                                                        v.setAdapter(new AlbumView(Album.this, jsons, false));
                                                        findViewById(R.id.images_horizontal).setVisibility(View.GONE);
                                                        slider = false;
                                                    } else {
                                                        v.setVisibility(View.GONE);
                                                        ViewPager p = (ViewPager) findViewById(R.id.images_horizontal);
                                                        p.setVisibility(View.VISIBLE);

                                                        // p.setAdapter(new AlbumViewPager(getSupportFragmentManager(), Album.this, jsons, false));
                                                        slider = true;
                                                    }
                                                }
                                            });
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
                                        dialog.show();
                                    }
                                } else {
                                    dialog.show();
                                }
                            }

                        });
            }

            return null;

        }


    }


}