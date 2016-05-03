package me.ccrama.redditslide.Models.Imgur.ImgurAlbum;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.jetbrains.annotations.NotNull;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SecretConstants;
import me.ccrama.redditslide.util.LogUtil;
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
    public static final String IMGUR_SITE_URL = "https://imgur.com/";
    public static final String IMGUR_AJAX_ALBUMS = "https://imgur.com/ajaxalbums/getimages/";
    public static final String IMGUR_AJAX_SUFFIX = "/hit.json?all=true";
    public static final String IMGUR_MASHAPE_BASE = "https://imgur-apiv3.p.mashape.com/3/";
    public static final String IMGUR_MASHAPE_ALBUM = IMGUR_MASHAPE_BASE + "album/";
    public static final String IMGUR_MASHAPE_IMAGE = IMGUR_MASHAPE_BASE + "image/";
    public static final int IMGUR_ALBUM_HASH_LENGTH = 5;
    public static final String JSON_SUFFIX = ".json";
    public static final String X_MASHAPE_KEY_HEADER = "X-Mashape-Key";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTHORIZATION_VALUE = "Client-ID bef87913eb202e9";

    public static SharedPreferences albumRequests;

    private static String getHash(String s) {
        String next = s.substring(s.lastIndexOf("/"), s.length());
        if (next.length() < 5) {
            return getHash(s.replace(next, ""));
        } else {
            return next;
        }

    }

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
        private JsonElement[] target;
        private int count;
        private int done;

        public GetAlbumWithCallback(@NotNull String url, @NotNull Activity baseActivity) {
            LogUtil.v("GetAlbumWithCallback() called with: " + "url = [" + url + "], baseActivity = [" + baseActivity + "]");
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

        /**
         * Handles Imgur JSON Object data that was downloaded by this class. This method must be
         * implemented in overridden classes.
         *
         * @param imageList A List of Imgur Image Objects to be displayed in the UI
         */
        public void doWithData(List<Image> imageList) {
            // Implement this method in overridden classes to handle Imgur Image List
        }

        public void doWithDataSingle(SingleImage data) {
            // TODO: Finish me
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

        /**
         * Attempts to parse a JSON String into Imgur Album data
         *
         * @param url        The API URL that requested the data
         * @param jsonString The JSON response String returned by the Imgur API
         */
        public void parseJson(String url, String jsonString) {
            LogUtil.v("parseJson() called with: " + "jsonString = [" + jsonString + "]");
            try {
                ImgurResponse response = getResponseFromJson(jsonString);
                final Data data = response.getData();
                if (data.getCount() < 1) {
                    LogUtil.e("parseJson: Valid data but album was empty");
                    // TODO: Handle empty albums
                }

                // JSON data was valid, cache the response
                if (!albumRequests.contains(url)) {
                    LogUtil.v("parseJson: Caching valid JSON data");
                    albumRequests.edit().putString(url, jsonString).apply();
                } else {
                    LogUtil.v("parseJson: JSON data already in cache");
                }

                baseActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        doWithData(data.getImages());
                    }
                });
            } catch (InvalidImgurResponseException e) {
                // TODO: Load WebView, try again with different API url?
                LogUtil.e("parseJson: Parse error ", e);
            } catch (Exception e) {
                LogUtil.e("parseJson: ", e);
            }
        }

        @Override
        protected ArrayList<JsonElement> doInBackground(final String... sub) {
            LogUtil.v("doInBackground() called with: " + "sub = [" + sub + "]");
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

                    final String apiUrl = IMGUR_MASHAPE_IMAGE + s + JSON_SUFFIX;
                    Request request = new Request.Builder()
                            .url(apiUrl)
                            .addHeader(X_MASHAPE_KEY_HEADER, SecretConstants.getImgurApiKey(baseActivity))
                            .addHeader(AUTHORIZATION_HEADER, AUTHORIZATION_VALUE)
                            .build();

                    client.newCall(request).enqueue(new OkHttpJson.ImgurCallback() {
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            LogUtil.e("onResponse() " + response.body().string());
                            try {
                                parseJson(apiUrl, OkHttpJson.getJsonString(response));
                            } catch (IOException e) {
                                LogUtil.e("onResponse: ", e);
                                // TODO: Handle 404's etc
                            }
                            // TODO: Handle single image API calls using new model
                        }
                    });
                }
            } else {
                if (baseActivity != null) {
                    final String url = getUrl(hash);
                    LogUtil.v("doInBackground: URL: " + url);
                    if (albumRequests.contains(url)) {
                        LogUtil.v("doInBackground: Cached");
                        // Use the cached gallery data to display the images
                        parseJson(url, albumRequests.getString(url, ""));
                    } else {
                        LogUtil.v("doInBackground: Making HTTP call");
                        // No cached gallery data found, make a new request to the Imgur API
                        Request request = new Request.Builder()
                                .url(url)
                                .build();
                        client.newCall(request).enqueue(new OkHttpJson.ImgurCallback() {
                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                LogUtil.v("onResponse() called with: " + "call = [" + call + "], response = [" + response + "]");
                                try {
                                    parseJson(url, OkHttpJson.getJsonString(response));
                                } catch (IOException e) {
                                    LogUtil.e("onResponse: ", e);
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

    /**
     * Gets the Imgur API URL for a given hash
     *
     * @param hash The hash to get the URL from
     * @return An API URL String for the passed in hash
     */
    public static String getUrl(String hash) {
        LogUtil.v("getUrl() called with: " + "hash = [" + hash + "]");
        return IMGUR_AJAX_ALBUMS + hash + IMGUR_AJAX_SUFFIX;
    }

    /**
     * Attempts to parse a JSON String as an ImgurResponse Object
     *
     * @param jsonResponse The JSON String to attempt to parse
     * @return A valid ImgurResponse de-serialized from the passed in JSON String
     * @throws InvalidImgurResponseException When passed-in JSON data could not be parsed as a
     *                                       valid
     *                                       ImgurResponse Object
     */
    private static ImgurResponse getResponseFromJson(String jsonResponse) throws InvalidImgurResponseException {
        try {
            return new Gson().fromJson(jsonResponse, ImgurResponse.class);
        } catch (Exception e) {
            LogUtil.e("getResponseFromJson: " + jsonResponse, e);
            throw new InvalidImgurResponseException();
            // TODO: Show UI error
        }
    }

    public static void preloadImages(Context c, JsonObject result, boolean gallery) {
        // TODO: Make me better
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
