package me.ccrama.redditslide.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import me.ccrama.redditslide.Activities.MediaView;
import me.ccrama.redditslide.Activities.Website;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SecretConstants;

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

    public static class GetAlbumJsonFromUrl extends AsyncTask<String, Void, ArrayList<JsonElement>> {

        public boolean gallery;
        public String hash;
        public Activity baseActivity;
        public boolean overrideAlbum;

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
            if (rawDat.contains("?")) {
                rawDat = rawDat.substring(0, rawDat.indexOf("?"));
            }

            hash = getHash(rawDat);

        }


        public void doWithData(ArrayList<JsonElement> data) {

        }

        public boolean dontClose;

        public void doGallery(JsonObject result) {
            if (result != null && result.has("data")) {
                Log.v(LogUtil.getTag(), result.toString());
                final ArrayList<JsonElement> jsons = new ArrayList<>();


                if (result.has("album_images")) {
                    JsonArray obj = result.getAsJsonObject("data").getAsJsonObject("image").getAsJsonObject("album_images").get("images").getAsJsonArray();
                    if (obj != null && !obj.isJsonNull() && obj.size() > 0) {
                        overrideAlbum = true;

                        for (JsonElement o : obj) {
                            jsons.add(o);
                        }


                        doWithData(jsons);

                    }
                } else if (result.has("data") && result.get("data").getAsJsonObject().get("image").getAsJsonObject().has("album_images")) {
                    JsonArray obj = result.getAsJsonObject("data").getAsJsonObject("image").getAsJsonObject("album_images").get("images").getAsJsonArray();
                    if (obj != null && !obj.isJsonNull() && obj.size() > 0) {
                        overrideAlbum = true;

                        for (JsonElement o : obj) {
                            jsons.add(o);
                        }


                        doWithData(jsons);

                    }
                } else if (result.has("data") && result.get("data").getAsJsonObject().has("image")) {
                    if (dontClose) {
                        jsons.add(result.get("data").getAsJsonObject().get("image"));
                        gallery = true;
                        doWithData(jsons);
                    } else {
                        Intent i = new Intent(baseActivity, MediaView.class);
                        if (result.getAsJsonObject("data").getAsJsonObject("image").get("mimetype").getAsString().contains("gif")) {
                            i.putExtra(GifView.EXTRA_URL, "http://imgur.com/" + result.getAsJsonObject("data").getAsJsonObject("image").get("hash").getAsString() + ".gif"); //could be a gif
                        } else {
                            i.putExtra(FullscreenImage.EXTRA_URL, "http://imgur.com/" + result.getAsJsonObject("data").getAsJsonObject("image").get("hash").getAsString() + ".png"); //could be a gif
                        }
                        baseActivity.startActivity(i);
                        baseActivity.finish();
                    }
                }
            }
        }

        public void doAlbum(JsonObject result) {


            if (result != null) {
                Log.v(LogUtil.getTag(), result.toString());

                final ArrayList<JsonElement> jsons = new ArrayList<>();

                if (result.has("data")) {

                    JsonObject obj = result.getAsJsonObject("data");
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

        JsonElement[] target;
        int count;
        int done;

        @Override
        protected ArrayList<JsonElement> doInBackground(final String... sub) {
            if (hash.startsWith("/")) {
                hash = hash.substring(1, hash.length());
            }
            if (hash.contains(",")) {
                target = new JsonElement[hash.split(",").length];
                count = 0;
                done = 0;
                for (String s : hash.split(",")) {
                    final int pos = count;
                    count++;
                    Ion.with(baseActivity).load("https://imgur-apiv3.p.mashape.com/3/image/" + s + ".json")
                            .addHeader("X-Mashape-Key", SecretConstants.getImgurApiKey(baseActivity)).addHeader("Authorization", "Client-ID " + "bef87913eb202e9")
                            .asJsonObject().setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject obj) {
                            if (obj != null && obj.has("data")) {
                                target[pos] = obj.get("data");
                            }
                            done += 1;
                            if (done == target.length -1) {
                                ArrayList<JsonElement> jsons = new ArrayList<>();
                                for(JsonElement el : target){
                                    if(el != null)
                                        jsons.add(el);
                                }
                                if (jsons.isEmpty()) {
                                    Intent i = new Intent(baseActivity, Website.class);
                                    i.putExtra(Website.EXTRA_URL, "https://imgur.com/" + hash);
                                    baseActivity.startActivity(i);
                                    baseActivity.finish();
                                } else {
                                    doWithData(jsons);
                                }
                            }
                        }
                    });

                }

            } else {
                if (baseActivity != null) {

                    if (gallery) {
                        if (albumRequests.contains("https://imgur.com/gallery/" + hash + ".json") && new JsonParser().parse(albumRequests.getString("https://imgur.com/gallery/" + hash + ".json", "")).getAsJsonObject().has("data")) {
                            Log.v(LogUtil.getTag(), albumRequests.getString("https://imgur.com/gallery/" + hash + ".json", ""));
                            baseActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    doGallery(new JsonParser().parse(albumRequests.getString("https://imgur.com/gallery/" + hash + ".json", "")).getAsJsonObject());
                                }
                            });

                        } else {
                            Ion.with(baseActivity)
                                    .load("https://imgur.com/gallery/" + hash + ".json")
                                    .asJsonObject()

                                    .setCallback(new FutureCallback<JsonObject>() {
                                        @Override
                                        public void onCompleted(Exception e, final JsonObject result) {
                                            if (result != null && result.has("data")) {
                                                albumRequests.edit().putString("https://imgur.com/gallery/" + hash + ".json", result.toString()).apply();

                                                baseActivity.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        doGallery(result);
                                                    }
                                                });
                                            } else if (!dontClose) {
                                                gallery = false;
                                                doInBackground(hash);
                                            }
                                        }

                                    });
                        }
                    } else {
                        if (albumRequests.contains("https://imgur-apiv3.p.mashape.com/3/album/" + hash + ".json")) {
                            baseActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    doAlbum(new JsonParser().parse(albumRequests.getString("https://imgur-apiv3.p.mashape.com/3/album/" + hash + ".json", "")).getAsJsonObject());
                                }
                            });
                        } else {
                            Ion.with(baseActivity)
                                    .load("https://imgur-apiv3.p.mashape.com/3/album/" + hash + ".json")
                                    .addHeader("X-Mashape-Key", SecretConstants.getImgurApiKey(baseActivity))
                                    .addHeader("Authorization", "Client-ID " + "bef87913eb202e9")
                                    .asJsonObject()
                                    .setCallback(new FutureCallback<JsonObject>() {
                                                     @Override
                                                     public void onCompleted(Exception e, final JsonObject result) {
                                                         if (result != null && !result.isJsonNull()) {
                                                             albumRequests.edit().putString("https://imgur-apiv3.p.mashape.com/3/album/" + hash + ".json", result.toString()).apply();
                                                             baseActivity.runOnUiThread(new Runnable() {
                                                                 @Override
                                                                 public void run() {
                                                                     doAlbum(result);
                                                                 }
                                                             });
                                                         } else if (!dontClose) {
                                                             Intent i = new Intent(baseActivity, Website.class);
                                                             i.putExtra(Website.EXTRA_URL, "https://imgur.com/a/" + hash);
                                                             baseActivity.startActivity(i);
                                                             baseActivity.finish();
                                                         }
                                                     }

                                                 }

                                    );
                        }
                    }

                    return null;


                }
            }
            return null;

        }


    }


    public static void saveAlbumToCache(final Activity c, String url) {

        boolean gallery = false;

        final String hash;
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
        if (gallery) {
            if (albumRequests.contains("https://imgur.com/gallery/" + hash + ".json")) {
                preloadImages(c, new JsonParser().parse(albumRequests.getString("https://imgur.com/gallery/" + hash + ".json", "")).getAsJsonObject(), true);
            } else {
                Ion.with(c)
                        .load("https://imgur.com/gallery/" + hash + ".json")
                        .asJsonObject()

                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                albumRequests.edit().putString("https://imgur.com/gallery/" + hash + ".json", result.toString()).apply();

                                preloadImages(c, result, true);
                            }

                        });
            }
        } else {
            if (albumRequests.contains("https://imgur-apiv3.p.mashape.com/3/album" + hash + ".json")) {
                preloadImages(c, new JsonParser().parse(albumRequests.getString("http://api.imgur.com/3/album" + hash + ".json", "")).getAsJsonObject(), false);
            } else {
                Ion.with(c)
                        .load("https://imgur-apiv3.p.mashape.com/3/album" + hash + ".json")
                        .addHeader("X-Mashape-Key", SecretConstants.getImgurApiKey(c)).addHeader("Authorization", "Client-ID " + "bef87913eb202e9")
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                                         @Override
                                         public void onCompleted(Exception e, JsonObject result) {
                                             albumRequests.edit().putString("https://imgur-apiv3.p.mashape.com/3/album" + hash + ".json", result.toString()).apply();

                                             preloadImages(c, result, false);
                                         }

                                     }

                        );
            }
        }


    }

    public static void preloadImages(Context c, JsonObject result, boolean gallery) {
        if (gallery && result != null) {

            if (result.has("data") && result.get("data").getAsJsonObject().has("image") && result.get("data").getAsJsonObject().get("image").getAsJsonObject().has("album_images") && result.get("data").getAsJsonObject().get("image").getAsJsonObject().get("album_images").getAsJsonObject().has("images")) {
                JsonArray obj = result.getAsJsonObject("data").getAsJsonObject("image").getAsJsonObject("album_images").get("images").getAsJsonArray();
                if (obj != null && !obj.isJsonNull() && obj.size() > 0) {

                    for (JsonElement o : obj) {
                        ((Reddit) c.getApplicationContext()).getImageLoader().loadImage("https://imgur.com/" + o.getAsJsonObject().get("hash").getAsString() + ".png", new SimpleImageLoadingListener());
                    }

                }
            }

        } else if (result != null) {
            if (result.has("album") && result.get("album").getAsJsonObject().has("images")) {
                JsonObject obj = result.getAsJsonObject("album");
                if (obj != null && !obj.isJsonNull() && obj.has("images")) {

                    final JsonArray jsonAuthorsArray = obj.get("images").getAsJsonArray();

                    for (JsonElement o : jsonAuthorsArray) {
                        ((Reddit) c.getApplicationContext()).getImageLoader().loadImage(o.getAsJsonObject().getAsJsonObject("links").get("original").getAsString(), new SimpleImageLoadingListener());
                    }
                }
            }
        }
    }

}
