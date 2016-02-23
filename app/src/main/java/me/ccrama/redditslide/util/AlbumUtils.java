package me.ccrama.redditslide.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.concurrent.ExecutionException;

import me.ccrama.redditslide.Activities.AlbumPager;
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

    /**
     * Gets the imgur id/hash from a link
     *
     * @param s Link to get the hash from
     * @return Imgur hash
     */
    private static String getHash(String s) {
        String next = s.substring(s.lastIndexOf("/"), s.length());
        if (next.length() < 5) {
            return getHash(s.replace(next, ""));
        } else {
            if (next.startsWith("/")) next = next.substring(1);
            return next;
        }

    }

    /**
     * Cuts off slashes at the end of a given string
     *
     * @param s String to cut
     * @return String without "/" at the end
     */
    private static String cutEnds(String s) {
        if (s.endsWith("/")) {
            return s.substring(0, s.length() - 1);
        } else {
            return s;
        }
    }

    /**
     * Loads an imgur gallery and displays in a recyclerview.
     *
     * @param url              Imgur link to open
     * @param baseActivity     Activity for opening intents
     * @param finishIfNone     If true:
     *                         1. Close baseActivity on error;
     *                         2. Open different content types in their activity
     * @param supportActionBar Action bar for settings its title
     * @param recyclerView     RecyclerView for displaying the images
     */
    public static void loadGalleryAndDisplay(@NotNull String url, @NotNull Activity baseActivity, boolean finishIfNone, @Nullable ActionBar supportActionBar, @NotNull RecyclerView recyclerView) {
        boolean gallery = false;
        String hash;

        String shortUrl = cutEnds(url);
        if (shortUrl.contains("gallery")) {
            gallery = true;
        }
        if (shortUrl.endsWith("/")) {
            shortUrl = shortUrl.substring(0, shortUrl.length() - 1);
        }
        String rawdat2 = shortUrl;
        if (rawdat2.substring(shortUrl.lastIndexOf("/"), rawdat2.length()).length() < 4) {
            shortUrl = shortUrl.replace(shortUrl.substring(shortUrl.lastIndexOf("/"), rawdat2.length()), "");
        }
        if (shortUrl.isEmpty()) {
            if (finishIfNone) baseActivity.finish();
            return;
        } else {
            hash = getHash(shortUrl);
        }

        JsonObject result = getJsonObjectFromGallery(hash, gallery, baseActivity);

        final ArrayList<JsonElement> jsons = new ArrayList<>();
        if (result != null && result.has("data") && result.get("success").getAsBoolean()) {
            JsonObject resultData = result.getAsJsonObject("data");
            Log.v(LogUtil.getTag(), resultData.toString());

            //Is single image
            if (gallery || (resultData.has("is_album") && !resultData.get("is_album").getAsBoolean())) {
                if (finishIfNone) {
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

                // Add the album hash to shared preferences too (because it has the same json return)
                albumRequests.edit().putString("album/" + hash, result.toString()).apply();


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


                    final PreCachingLayoutManager mLayoutManager;
                    mLayoutManager = new PreCachingLayoutManager(baseActivity);
                    recyclerView.setLayoutManager(mLayoutManager);
                    recyclerView.setAdapter(new AlbumView(baseActivity, jsons));


                } else catchFailedApiCall(finishIfNone, baseActivity, hash);
            }
        } else catchFailedApiCall(finishIfNone, baseActivity, hash);
    }

    /**
     * Get gallery json elements from a link
     *
     * @param url          Imgur gallery / album link
     * @param baseActivity Activity for doing network calls
     * @return Imgur gallery JsonElements
     */
    public static ArrayList<JsonElement> getJsonElementsFromGallery(@NotNull String url, @NotNull Activity baseActivity) {
        boolean gallery = false;
        String hash;

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

        hash = getHash(rawDat);

        JsonObject result = getJsonObjectFromGallery(hash, gallery, baseActivity);

        if (result != null && result.has("data") && result.get("success").getAsBoolean()) {
            Log.v(LogUtil.getTag(), result.toString());
            final ArrayList<JsonElement> jsons = new ArrayList<>();

            final JsonObject resultData = result.getAsJsonObject("data");

            //Is album
            if (!gallery || resultData.get("is_album").getAsBoolean()) {
                if (gallery) {
                    // Add the album hash to shared preferences too (because it has the same json return)
                    albumRequests.edit().putString("album/" + hash, result.toString()).apply();
                }
                if (resultData.has("images")) {
                    JsonArray obj = resultData.get("images").getAsJsonArray();
                    if (obj != null && !obj.isJsonNull() && obj.size() > 0) {

                        for (JsonElement o : obj) {
                            jsons.add(o);
                        }
                        ActionBar supportActionBar = ((AlbumPager) baseActivity).getSupportActionBar();
                        if (supportActionBar != null) {
                            if (resultData.has("title") || !resultData.get("title").isJsonNull()) {
                                supportActionBar.setTitle(resultData.get("title").getAsString());
                            } else {
                                supportActionBar.setTitle(baseActivity.getString(R.string.album_title_count, jsons.size()));
                            }
                        }
                        return jsons;
                    } else {
                        Log.w(LogUtil.getTag(), "Result has no images in it");
                        return null;
                    }
                } else {
                    Log.w(LogUtil.getTag(), "Json has no image tag");
                    return null;
                }
            } else {
                //is single image
                jsons.add(resultData);
                return jsons;
            }

        } else {
            Log.w(LogUtil.getTag(), "Api call failed, result is: " + (result == null ? "null" : result.toString()));
            albumRequests.edit().remove("gallery/" + hash).apply();
            albumRequests.edit().remove("album/" + hash).apply();
            return null;
        }


    }

    /**
     * Get the JsonObject from an imgur link (gallery/album link). Returns a JsonObject from chache
     * if it's already cached
     *
     * @param hash         Imgur id hash
     * @param gallery      Wheter the hash is a gallery or album
     * @param baseActivity Activity for doing network calls
     * @return imgur gallery/album JsonObject
     */
    private static JsonObject getJsonObjectFromGallery(final String hash, boolean gallery, Activity baseActivity) {


        if (gallery) {
            if (albumRequests.contains("gallery/" + hash)) {
                return new JsonParser().parse(albumRequests.getString("gallery/" + hash, "")).getAsJsonObject();
            } else {
                String apiCallUrl = "https://api.imgur.com/3/gallery/" + hash + ".json";
                Log.v(LogUtil.getTag(), apiCallUrl);
                try {
                    return Ion.with(baseActivity)
                            .load(apiCallUrl)
                            .addHeader("Authorization", OpenImgurLink.IMGUR_CLIENT_ID)
                            .asJsonObject()
                            .setCallback(new FutureCallback<JsonObject>() {
                                @Override
                                public void onCompleted(Exception e, JsonObject result) {

                                    albumRequests.edit().putString("gallery/" + hash, result.toString()).apply();
                                }
                            }).get();
                } catch (InterruptedException | ExecutionException e) {
                    Log.w(LogUtil.getTag(), "Cannot execute Ion-request");
                    return null;
                }
            }
        } else {
            if (albumRequests.contains("album/" + hash)) {
                return new JsonParser().parse(albumRequests.getString("album/" + hash, "")).getAsJsonObject();
            } else {

                String apiCallUrl = "https://api.imgur.com/3/album/" + hash + ".json";
                Log.v(LogUtil.getTag(), apiCallUrl);
                try {
                    return Ion.with(baseActivity)
                            .load(apiCallUrl)
                            .addHeader("Authorization", OpenImgurLink.IMGUR_CLIENT_ID)
                            .asJsonObject()
                            .setCallback(new FutureCallback<JsonObject>() {
                                @Override
                                public void onCompleted(Exception e, JsonObject result) {

                                    albumRequests.edit().putString("album/" + hash, result.toString()).apply();

                                }
                            }).get();
                } catch (InterruptedException | ExecutionException e) {
                    Log.w(LogUtil.getTag(), "Cannot execute Ion-request");
                    return null;
                }

            }
        }

    }


    private static void catchFailedApiCall(boolean finishIfNone, Activity baseActivity, String hash) {
        Log.w(LogUtil.getTag(), "Api call failed");

        if (finishIfNone) {
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


    /**
     * This method gets an album or gallery and stores it in the sharedPrefs. It also
     * preloades the images in the image loader.
     *
     * @param c   Context
     * @param url Imgur url to cache
     */
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
        JsonObject result = getJsonObjectFromGallery(hash, gallery, c);

        if (result != null && result.get("success").getAsBoolean() && result.has("data")) {
            preloadImages(c, result);
        } else Log.w(LogUtil.getTag(), "Cannot cache gallery");
    }

    private static void preloadImages(Context c, JsonObject result) {
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
