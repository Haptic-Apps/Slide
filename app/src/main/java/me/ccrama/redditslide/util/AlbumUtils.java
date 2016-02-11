package me.ccrama.redditslide.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import me.ccrama.redditslide.Activities.FullscreenImage;
import me.ccrama.redditslide.Activities.GifView;
import me.ccrama.redditslide.Activities.Website;
import me.ccrama.redditslide.Adapters.AlbumView;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Views.OpenImgurLink;
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

        public boolean mIsAlbum;
        public JsonObject mJsonResult;

        public LoadAlbumFromUrl(@NotNull String url, @NotNull Activity baseActivity, boolean finishIfNone, @Nullable ActionBar bar, @NotNull RecyclerView recyclerView) {

            this.finishIfNone = finishIfNone;
            this.recyclerView = recyclerView;
            this.supportActionBar = bar;
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



        private void catchFailedApiCall(){
            Log.w(LogUtil.getTag(), "Api call failed");
            if (openExternalNotAlbum) {
                Intent i = new Intent(baseActivity, Website.class);
                i.putExtra(Website.EXTRA_URL, "https://imgur.com/gallery/" + hash);

                baseActivity.startActivity(i);
            }
            if (finishIfNone)
                baseActivity.finish();
            //Catch failed api call

            albumRequests.edit().remove("gallery/" + hash).apply();
            albumRequests.edit().remove("album/" + hash).apply();
        }

        @Override
        protected Void doInBackground(final String... sub) {

            if (gallery) {

                if (albumRequests.contains("gallery/" + hash)) {
                    mJsonResult = new JsonParser().parse(albumRequests.getString("gallery/" + hash, "")).getAsJsonObject();
                    mIsAlbum = false;
                } else {
                    Ion.with(baseActivity)
                            .load("https://api.imgur.com/3/gallery/" + hash + ".json")
                            .addHeader("Authorization", OpenImgurLink.IMGUR_CLIENT_ID)
                            .asJsonObject()
                            .setCallback(new FutureCallback<JsonObject>() {
                                @Override
                                public void onCompleted(Exception e, JsonObject result) {

                                    albumRequests.edit().putString("gallery/" + hash, result.toString()).apply();
                                    mJsonResult = result;
                                    mIsAlbum = false;
                                }
                            });
                }
            } else {
                if (albumRequests.contains("album/" + hash)) {
                    mJsonResult = new JsonParser().parse(albumRequests.getString("album/" + hash, "")).getAsJsonObject();
                    mIsAlbum = true;
                } else {
                    Ion.with(baseActivity)
                            .load("https://api.imgur.com/3/album" + hash + ".json")
                            .addHeader("Authorization", OpenImgurLink.IMGUR_CLIENT_ID)
                            .asJsonObject()
                            .setCallback(new FutureCallback<JsonObject>() {
                                @Override
                                public void onCompleted(Exception e, JsonObject result) {

                                    albumRequests.edit().putString("album/" + hash, result.toString()).apply();
                                    mJsonResult = result;
                                    mIsAlbum = true;

                                }
                            });
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            final ArrayList<JsonElement> jsons = new ArrayList<>();
            if (mJsonResult != null && mJsonResult.has("data") && mJsonResult.get("success").getAsBoolean()) {
                JsonObject resultData = mJsonResult.getAsJsonObject("data");
                Log.v(LogUtil.getTag(), resultData.toString());

                //Is single image
                if (!mIsAlbum || (resultData.has("is_album") && !resultData.get("is_album").getAsBoolean())) {
                    if (openExternalNotAlbum) {
                        if (resultData.get("animated").getAsBoolean()) {
                            Intent i = new Intent(baseActivity, GifView.class);
                            i.putExtra(GifView.EXTRA_URL, resultData.get("link").getAsString());
                            baseActivity.startActivity(i);
                        } else {
                            Intent i = new Intent(baseActivity, FullscreenImage.class);
                            i.putExtra(FullscreenImage.EXTRA_URL, resultData.get("link").getAsString());
                            baseActivity.startActivity(i);
                        }


                    }
                    if (finishIfNone)
                        baseActivity.finish();
                } else {
                    //is album
                    JsonArray obj = resultData.get("images").getAsJsonArray();
                    if (obj != null && !obj.isJsonNull() && obj.size() > 0) {

                        for (JsonElement o : obj) {
                            jsons.add(o);
                        }

                        if (supportActionBar != null) {
                            if (resultData.has("title") || !resultData.get("title").isJsonNull()) {
                                supportActionBar.setTitle(resultData.get("title").getAsString());
                            } else {
                                supportActionBar.setTitle(baseActivity.getString(R.string.album_title_count, jsons.size()));
                            }
                        }

                        if (recyclerView != null) {
                            final PreCachingLayoutManager mLayoutManager;
                            mLayoutManager = new PreCachingLayoutManager(baseActivity);
                            recyclerView.setLayoutManager(mLayoutManager);
                            recyclerView.setAdapter(new AlbumView(baseActivity, jsons));
                        }


                    } else catchFailedApiCall();
                }
            } else catchFailedApiCall();
        }
    }

    public static class GetAlbumJsonFromUrl extends AsyncTask<String, Void, ArrayList<JsonElement>> {

        public boolean gallery;
        public String hash;

        public Activity baseActivity;

        public boolean mIsAlbum;
        public JsonObject mJsonResult;

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


        @Override
        protected ArrayList<JsonElement> doInBackground(final String... sub) {

            if (gallery) {

                if (albumRequests.contains("gallery/" + hash) && new JsonParser().parse(albumRequests.getString("gallery/" + hash, "")).getAsJsonObject().has("data")) {
                    Log.v(LogUtil.getTag(), albumRequests.getString("gallery/" + hash, ""));

                    mIsAlbum = false;
                    mJsonResult = new JsonParser().parse(albumRequests.getString("gallery/" + hash, "")).getAsJsonObject();
                } else {
                    Ion.with(baseActivity)
                            .load("https://api.imgur.com/3/gallery/" + hash + ".json")
                            .addHeader("Authorization", OpenImgurLink.IMGUR_CLIENT_ID)
                            .asJsonObject()
                            .setCallback(new FutureCallback<JsonObject>() {
                                @Override
                                public void onCompleted(Exception e, JsonObject result) {
                                    albumRequests.edit().putString("gallery/" + hash, result.toString()).apply();

                                    mIsAlbum = false;
                                    mJsonResult = result;
                                }

                            });
                }
            } else {

                if (albumRequests.contains("album/" + hash)) {
                    mIsAlbum = true;
                    mJsonResult = new JsonParser().parse(albumRequests.getString("album/" + hash, "")).getAsJsonObject();
                } else {
                    Ion.with(baseActivity)
                            .load("https://api.imgur.com/3/album" + hash + ".json")
                            .addHeader("Authorization", OpenImgurLink.IMGUR_CLIENT_ID)
                            .asJsonObject()
                            .setCallback(new FutureCallback<JsonObject>() {
                                             @Override
                                             public void onCompleted(Exception e, JsonObject result) {
                                                 albumRequests.edit().putString("album/" + hash, result.toString()).apply();

                                                 mIsAlbum = true;
                                                 mJsonResult = result;
                                             }
                                         }
                            );
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<JsonElement> jsonElements) {
            if (mJsonResult != null && mJsonResult.has("data") && mJsonResult.get("success").getAsBoolean()) {
                Log.v(LogUtil.getTag(), mJsonResult.toString());
                final ArrayList<JsonElement> jsons = new ArrayList<>();

                final JsonObject resultData = mJsonResult.getAsJsonObject("data");


                if(mIsAlbum || resultData.get("is_album").getAsBoolean()) {
                    if (resultData.has("images")) {
                        JsonArray obj = resultData.get("images").getAsJsonArray();
                        if (obj != null && !obj.isJsonNull() && obj.size() > 0) {

                            for (JsonElement o : obj) {
                                jsons.add(o);
                            }

                            doWithData(jsons);
                        }
                    }
                } else {
                    //is single image
                    jsons.add(resultData);
                    doWithData(jsons);

                }
            } else {
                //Feedback for user todo
                Log.w(LogUtil.getTag(), "Api call failed, result is: " + mJsonResult == null ? "null" : mJsonResult.toString());
                albumRequests.edit().remove("gallery/" + hash).apply();
                albumRequests.edit().remove("album/" + hash).apply();
            }
        }
    }


    public static void saveAlbumToCache(final Activity c, String url) {

        boolean gallery = false;

        final String hash;
        String rawDat = cutEnds(url);
        if (rawDat.contains("gallery")) {
            gallery = true;
        }

        rawDat = cutEnds(rawDat);

        String rawdat2 = rawDat;
        if (rawdat2.substring(rawDat.lastIndexOf("/"), rawdat2.length()).length() < 4) {
            rawDat = rawDat.replace(rawDat.substring(rawDat.lastIndexOf("/"), rawdat2.length()), "");
        }
        {

            hash = getHash(rawDat);

        }
        if (gallery) {
            if (albumRequests.contains("gallery/" + hash)) {
                preloadImages(c, new JsonParser().parse(albumRequests.getString("gallery/" + hash, "")).getAsJsonObject());
            } else {
                Ion.with(c)
                        .load("https://api.imgur.com/3/gallery/" + hash + ".json")
                        .addHeader("Authorization", OpenImgurLink.IMGUR_CLIENT_ID)
                        .asJsonObject()

                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                albumRequests.edit().putString("gallery/" + hash, result.toString()).apply();

                                preloadImages(c, result);
                            }

                        });
            }
        } else {
            if (albumRequests.contains("album/" + hash)) {
                preloadImages(c, new JsonParser().parse(albumRequests.getString("album/" + hash, "")).getAsJsonObject());
            } else {
                Ion.with(c)
                        .load("https://api.imgur.com/3/album" + hash + ".json")
                        .addHeader("Authorization", OpenImgurLink.IMGUR_CLIENT_ID)
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                                         @Override
                                         public void onCompleted(Exception e, JsonObject result) {
                                             albumRequests.edit().putString("album/" + hash, result.toString()).apply();

                                             preloadImages(c, result);
                                         }

                                     }

                        );
            }
        }


    }

    public static void preloadImages(Context c, JsonObject result) {
        JsonArray obj = result.getAsJsonObject("data").get("images").getAsJsonArray();
        if (obj != null && !obj.isJsonNull() && obj.size() > 0) {

            for (JsonElement o : obj) {
                ((Reddit) c.getApplicationContext()).getImageLoader().loadImage(o.getAsJsonObject().get("link").getAsString(), new SimpleImageLoadingListener());
            }
        } else {
            Log.w(LogUtil.getTag(), "Got invalid result");
        }
    }

}
