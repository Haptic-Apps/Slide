package me.ccrama.redditslide.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import me.ccrama.redditslide.Activities.FullscreenImage;
import me.ccrama.redditslide.Activities.GifView;
import me.ccrama.redditslide.Activities.Website;
import me.ccrama.redditslide.Adapters.AlbumView;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Views.PreCachingLayoutManager;

/**
 * Created by carlo_000 on 2/1/2016.
 */
public class AlbumUtils {

    public static SharedPreferences albumRequests;

    private static String getHash(String s) {
        String next = s.substring(s.lastIndexOf("/"), s.length());
        if (next.length() < 5) {
            return getHash(s.replace(next, ""));
        } else {
            return next;
        }

    }

    boolean slider;

    private static String cutEnds(String s) {
        if (s.endsWith("/")) {
            return s.substring(0, s.length() - 1);
        } else {
            return s;
        }
    }


    public static class LoadAlbumFromUrl extends AsyncTask<String, Void, Void> {

        public boolean gallery;
        public String hash;
        public boolean finishIfNone;
        public ActionBar supportActionBar;
        public boolean openExternalNotAlbum;
        public Activity baseActivity;
        public RecyclerView recyclerView;

        public LoadAlbumFromUrl(@NotNull String url, @NotNull Activity baseActivity, @NotNull boolean finishIfNone, @NotNull boolean openExternalNotAlbum, @Nullable ActionBar bar, @NotNull RecyclerView recyclerView) {

            this.finishIfNone = finishIfNone;
            this.recyclerView = recyclerView;
            this.supportActionBar = bar;
            this.openExternalNotAlbum = openExternalNotAlbum;
            this.baseActivity = baseActivity;

            String rawDat = cutEnds(url);
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
                if (finishIfNone)
                    baseActivity.finish();
            } else {

                hash = getHash(rawDat);

            }
        }


        public void doGallery(JsonObject result) {
            final ArrayList<JsonElement> jsons = new ArrayList<>();


            if (!result.getAsJsonObject("data").getAsJsonObject("image").get("is_album").getAsBoolean()) {
                if (openExternalNotAlbum) {
                    if (result.getAsJsonObject("data").getAsJsonObject("image").get("mimetype").getAsString().contains("gif")) {
                        Intent i = new Intent(baseActivity, GifView.class);
                        i.putExtra(GifView.EXTRA_URL, "http://imgur.com/" + result.getAsJsonObject("data").getAsJsonObject("image").get("hash").getAsString() + ".gif"); //could be a gif
                        baseActivity.startActivity(i);
                    } else {
                        Intent i = new Intent(baseActivity, FullscreenImage.class);
                        i.putExtra(FullscreenImage.EXTRA_URL, "http://imgur.com/" + result.getAsJsonObject("data").getAsJsonObject("image").get("hash").getAsString() + ".png"); //could be a gif
                        baseActivity.startActivity(i);
                    }


                }
                if (finishIfNone)
                    baseActivity.finish();
            } else {
                JsonArray obj = result.getAsJsonObject("data").getAsJsonObject("image").getAsJsonObject("album_images").get("images").getAsJsonArray();
                if (obj != null && !obj.isJsonNull() && obj.size() > 0) {

                    for (JsonElement o : obj) {
                        jsons.add(o);
                    }

                    if (supportActionBar != null)
                        supportActionBar.setTitle(baseActivity.getString(R.string.album_title_count, jsons.size()));


                    if (recyclerView != null) {
                        final PreCachingLayoutManager mLayoutManager;
                        mLayoutManager = new PreCachingLayoutManager(baseActivity);
                        recyclerView.setLayoutManager(mLayoutManager);
                        recyclerView.setAdapter(new AlbumView(baseActivity, jsons, true));
                    }


                } else {

                    if (openExternalNotAlbum) {
                        Intent i = new Intent(baseActivity, Website.class);
                        i.putExtra(Website.EXTRA_URL, "http://imgur.com/gallery/" + hash);

                        baseActivity.startActivity(i);
                    }
                    if (finishIfNone)
                        baseActivity.finish();
                    //Catch failed api call
                }
            }
        }

        public void doAlbum(JsonObject result) {
            Dialog dialog = new AlertDialogWrapper.Builder(baseActivity)
                    .setTitle(R.string.album_err_not_found)
                    .setMessage(R.string.album_err_msg_not_found)
                    .setCancelable(false)
                    .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            baseActivity.finish();
                        }
                    }).create();


            if (result != null) {
                Log.v(LogUtil.getTag(), result.toString());

                final ArrayList<JsonElement> jsons = new ArrayList<>();

                if (result.has("album")) {
                    if (supportActionBar != null)
                        if (result.get("album").getAsJsonObject().has("title") && !result.get("album").isJsonNull() && !result.get("album").getAsJsonObject().get("title").isJsonNull()) {
                            supportActionBar.setTitle(result.get("album").getAsJsonObject().get("title").getAsString());
                        } else {
                            supportActionBar.setTitle("Album");
                        }
                    JsonObject obj = result.getAsJsonObject("album");
                    if (obj != null && !obj.isJsonNull() && obj.has("images")) {

                        final JsonArray jsonAuthorsArray = obj.get("images").getAsJsonArray();

                        for (JsonElement o : jsonAuthorsArray) {
                            jsons.add(o);
                        }


                        if (recyclerView != null) {
                            final PreCachingLayoutManager mLayoutManager;
                            mLayoutManager = new PreCachingLayoutManager(baseActivity);
                            recyclerView.setLayoutManager(mLayoutManager);
                            recyclerView.setAdapter(new AlbumView(baseActivity, jsons, false));
                        }

                    } else {

                        if (finishIfNone) {
                            new AlertDialogWrapper.Builder(baseActivity)
                                    .setTitle(R.string.album_err_not_found)
                                    .setMessage(R.string.album_err_msg_not_found)
                                    .setCancelable(false)
                                    .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            baseActivity.finish();
                                        }
                                    }).create().show();
                        }
                    }
                } else {
                    if (finishIfNone)
                        dialog.show();
                }
            } else {
                if (finishIfNone)

                    dialog.show();
            }
        }

        @Override
        protected Void doInBackground(final String... sub) {


            if (gallery) {

                if (albumRequests.contains("https://imgur.com/gallery/" + hash + ".json")) {
                    doGallery(new JsonParser().parse(albumRequests.getString("https://imgur.com/gallery/" + hash + ".json", "")).getAsJsonObject());
                } else {
                    Ion.with(baseActivity)
                            .load("https://imgur.com/gallery/" + hash + ".json")
                            .asJsonObject()
                            .setCallback(new FutureCallback<JsonObject>() {
                                @Override
                                public void onCompleted(Exception e, JsonObject result) {

                                    if (result != null && result.has("data")) {
                                        albumRequests.edit().putString("https://imgur.com/gallery/" + hash + ".json", result.toString()).apply();

                                        doGallery(result);
                                    }
                                }

                            });
                }
            } else {
                if (albumRequests.contains("http://api.imgur.com/2/album" + hash + ".json")) {
                    doAlbum(new JsonParser().parse(albumRequests.getString("http://api.imgur.com/2/album" + hash + ".json", "")).getAsJsonObject());
                } else {
                    Ion.with(baseActivity)
                            .load("http://api.imgur.com/2/album" + hash + ".json")
                            .asJsonObject()
                            .setCallback(new FutureCallback<JsonObject>() {
                                @Override
                                public void onCompleted(Exception e, JsonObject result) {
                                    albumRequests.edit().putString("http://api.imgur.com/2/album" + hash + ".json", result.toString()).apply();

                                    doAlbum(result);
                                }

                            });
                }
            }

            return null;

        }


    }

    public static class GetAlbumJsonFromUrl extends AsyncTask<String, Void, ArrayList<JsonElement>> {

        public boolean gallery;
        public String hash;

        public Activity baseActivity;

        public GetAlbumJsonFromUrl(@NotNull String url, @NotNull Activity baseActivity) {

            this.baseActivity = baseActivity;

            String rawDat = cutEnds(url);
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
            {

                hash = getHash(rawDat);

            }
        }


        public void doWithData(ArrayList<JsonElement> data) {

        }

        public void doGallery(JsonObject result) {
            if (result != null && result.has("data")) {
                Log.v(LogUtil.getTag(), result.toString());


                final ArrayList<JsonElement> jsons = new ArrayList<>();


                JsonArray obj = result.getAsJsonObject("data").getAsJsonObject("image").getAsJsonObject("album_images").get("images").getAsJsonArray();
                if (obj != null && !obj.isJsonNull() && obj.size() > 0) {

                    for (JsonElement o : obj) {
                        jsons.add(o);
                    }


                    doWithData(jsons);

                }
            }
        }

        public void doAlbum(JsonObject result){



            if (result != null) {
                Log.v(LogUtil.getTag(), result.toString());

                final ArrayList<JsonElement> jsons = new ArrayList<>();

                if (result.has("album")) {

                    JsonObject obj = result.getAsJsonObject("album");
                    if (obj != null && !obj.isJsonNull() && obj.has("images")) {

                        final JsonArray jsonAuthorsArray = obj.get("images").getAsJsonArray();

                        for (JsonElement o : jsonAuthorsArray) {
                            jsons.add(o);
                        }


                        doWithData(jsons);

                    }
                }
            }
        }

        @Override
        protected ArrayList<JsonElement> doInBackground(final String... sub) {

            if (gallery) {
                if (albumRequests.contains("https://imgur.com/gallery/" + hash + ".json")) {
                    doGallery(new JsonParser().parse(albumRequests.getString("https://imgur.com/gallery/" + hash + ".json", "")).getAsJsonObject());
                } else {
                    Ion.with(baseActivity)
                            .load("https://imgur.com/gallery/" + hash + ".json")
                            .asJsonObject()

                            .setCallback(new FutureCallback<JsonObject>() {
                                @Override
                                public void onCompleted(Exception e, JsonObject result) {
                                    albumRequests.edit().putString("https://imgur.com/gallery/" + hash + ".json", result.toString()).apply();

                                    doGallery(result);
                                }

                            });
                }
            } else {
                Ion.with(baseActivity)
                        .load("http://api.imgur.com/2/album" + hash + ".json")
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                                         @Override
                                         public void onCompleted(Exception e, JsonObject result) {
                                             albumRequests.edit().putString("http://api.imgur.com/2/album" + hash + ".json", result.toString()).apply();

                                            doAlbum(result);
                                         }

                                     }

                        );
            }

            return null;


        }


    }


}
