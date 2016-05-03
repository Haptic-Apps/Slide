package me.ccrama.redditslide.Models.Imgur.ImgurAlbum;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.jetbrains.annotations.NotNull;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Activities.Website;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SecretConstants;
import me.ccrama.redditslide.util.OkHttpJson;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by carlo_000 on 2/1/2016.
 */
public class AlbumUtils {

    // URLS should all have trailing slash
    public static final String IMGUR_SITE_URL       = "https://imgur.com/";
    public static final String IMGUR_AJAX_ALBUMS    = "https://imgur.com/ajaxalbums/getimages/";
    public static final String IMGUR_AJAX_SUFFIX    = "/hit.json?all=true";
    public static final String IMGUR_MASHAPE_BASE   = "https://imgur-apiv3.p.mashape.com/3/";
    public static final String IMGUR_MASHAPE_ALBUM  = IMGUR_MASHAPE_BASE + "album/";
    public static final String IMGUR_MASHAPE_IMAGE  = IMGUR_MASHAPE_BASE + "image/";
    public static final String JSON_SUFFIX          = ".json";
    public static final String X_MASHAPE_KEY_HEADER = "X-Mashape-Key";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTHORIZATION_VALUE  = "Client-ID bef87913eb202e9";

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

    public static class GetAlbumWithCallback extends AsyncTask<String, Void, ArrayList<JsonElement>> {

        public String hash;
        public Activity baseActivity;
        public boolean overrideAlbum;

        public GetAlbumWithCallback(@NotNull String url, @NotNull Activity baseActivity) {


            this.baseActivity = baseActivity;
            String rawDat = cutEnds(url);

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


        public void doWithData(List<Image> data) {

        }

        public void doWithDataSingle(SingleImage data) {
            final Image toDo = new Image();
            toDo.setAnimated(data.getAnimated());
            toDo.setDescription(data.getDescription());
            toDo.setHash(getHash(data.getLink()));
            toDo.setTitle(data.getTitle());
            toDo.setExt(data.getLink().substring(data.getLink().lastIndexOf("."), data.getLink().length()));
            toDo.setHeight(data.getHeight());
            toDo.setWidth(data.getWidth());
            doWithData(new ArrayList<Image>() {
                {
                    this.add(toDo);
                }
            });
        }

        JsonElement[] target;
        int count;
        int done;

        AlbumImage album;

        public void parseJson(JsonElement baseData) {
            OkHttpClient client = new OkHttpClient();
            try {
                if (!baseData.toString().contains("\"data\":[]")) {
                    album = new ObjectMapper().readValue(baseData.toString(), AlbumImage.class);
                    doWithData(album.getData().getImages());
                } else  {
                    Request request = new Request.Builder()
                            .url(IMGUR_MASHAPE_ALBUM + hash + JSON_SUFFIX)
                            .addHeader(X_MASHAPE_KEY_HEADER, SecretConstants.getImgurApiKey(baseActivity))
                            .addHeader(AUTHORIZATION_HEADER, AUTHORIZATION_VALUE)
                            .build();
                    client.newCall(request).enqueue(new OkHttpJson.ImgurCallback() {
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            final JsonObject obj = OkHttpJson.getJsonFromResponse(response);
                            if (obj == null || obj.isJsonNull()) return; // TODO handle null obj
                            try {
                                SingleImage single = new ObjectMapper().readValue(obj.toString(), SingleAlbumImage.class).getData();
                                doWithDataSingle(single);
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected ArrayList<JsonElement> doInBackground(final String... sub) {
            OkHttpClient client = new OkHttpClient();

            if (hash.startsWith("/")) {
                // Remove stray forward slash
                hash = hash.substring(1, hash.length());
            }
            if (hash.contains(",")) {
                // URL had a comma in it, attempt to split up the hashes and load each image individually
                target = new JsonElement[hash.split(",").length];
                count = 0;
                done = 0;
                for (String s : hash.split(",")) {
                    final int pos = count++;

                    Request request = new Request.Builder()
                            .url(IMGUR_MASHAPE_IMAGE + s + JSON_SUFFIX)
                            .addHeader(X_MASHAPE_KEY_HEADER, SecretConstants.getImgurApiKey(baseActivity))
                            .addHeader(AUTHORIZATION_HEADER, AUTHORIZATION_VALUE)
                            .build();

                    client.newCall(request).enqueue(new OkHttpJson.ImgurCallback() {
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            JsonObject obj = OkHttpJson.getJsonFromResponse(response);
                            if (obj != null && obj.has("data")) {
                                target[pos] = obj.get("data");
                            }
                            done += 1;
                            if (done == target.length) {
                                ArrayList<Image> imageArrayList = new ArrayList<>();
                                for (JsonElement element : target) {
                                    if (element != null)
                                        imageArrayList.add(new Image()); // TODO: make this work
                                }
                                if (imageArrayList.isEmpty()) {
                                    Intent i = new Intent(baseActivity, Website.class);
                                    i.putExtra(Website.EXTRA_URL, IMGUR_SITE_URL + hash);
                                    baseActivity.startActivity(i);
                                    baseActivity.finish();
                                } else {
                                    doWithData(imageArrayList);
                                }
                            }
                        }
                    });
                }

            } else {
                if (baseActivity != null) {
                    final String url = getUrl(hash);
                    if (albumRequests.contains(url) && new JsonParser().parse(albumRequests.getString(url, "")).getAsJsonObject().has("data")) {
                        // Use the cached gallery data to display the images
                        baseActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                parseJson(new JsonParser().parse(albumRequests.getString(url, "")).getAsJsonObject());
                            }
                        });

                    } else {
                        // No cached gallery data found, make a new request to the Imgur API
                        Request request = new Request.Builder()
                                .url(url)
                                .build();
                        client.newCall(request).enqueue(new OkHttpJson.ImgurCallback() {
                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                final JsonObject result = OkHttpJson.getJsonFromResponse(response);
                                if (result != null && result.has("data")) {
                                    albumRequests.edit().putString(url, result.toString()).apply();
                                    baseActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            parseJson(result);
                                        }
                                    });
                                }
                            }
                        });
                    }
                    return null;
                }
            }
            return null;

        }


    }

    public static String getUrl(String hash) {
        return IMGUR_AJAX_ALBUMS + hash + IMGUR_AJAX_SUFFIX;
    }

    public static void preloadImages(Context c, JsonObject result, boolean gallery) {
        if (gallery && result != null) {

            if (result.has("data") && result.get("data").getAsJsonObject().has("image") && result.get("data").getAsJsonObject().get("image").getAsJsonObject().has("album_images") && result.get("data").getAsJsonObject().get("image").getAsJsonObject().get("album_images").getAsJsonObject().has("images")) {
                JsonArray obj = result.getAsJsonObject("data").getAsJsonObject("image").getAsJsonObject("album_images").get("images").getAsJsonArray();
                if (obj != null && !obj.isJsonNull() && obj.size() > 0) {

                    for (JsonElement o : obj) {
                        ((Reddit) c.getApplicationContext()).getImageLoader().loadImage(IMGUR_SITE_URL + o.getAsJsonObject().get("hash").getAsString() + ".png", new SimpleImageLoadingListener());
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
