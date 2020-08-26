package me.ccrama.redditslide.ImgurAlbum;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SecretConstants;
import me.ccrama.redditslide.util.HttpUtil;
import me.ccrama.redditslide.util.LogUtil;
import okhttp3.OkHttpClient;

/**
 * Created by carlo_000 on 2/1/2016.
 */
public class AlbumUtils {

    public static SharedPreferences albumRequests;

    private static String getHash(String s) {
        if(s.contains("/comment/")){
            s = s.substring(0, s.indexOf("/comment"));
        }
        String next = s.substring(s.lastIndexOf("/"));
        if (next.contains(".")) {
            next = next.substring(0, next.indexOf("."));
        }
        if (next.startsWith("/")) {
            next = next.substring(1);
        }
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

        private OkHttpClient client;
        private Gson gson;
        private String mashapeKey;

        public void onError() {

        }

        public GetAlbumWithCallback(@NotNull String url, @NotNull Activity baseActivity) {

            this.baseActivity = baseActivity;
            if(url.contains("/layout/")){
                url = url.substring(0, url.indexOf("/layout"));
            }
            String rawDat = cutEnds(url);

            if (rawDat.endsWith("/")) {
                rawDat = rawDat.substring(0, rawDat.length() - 1);
            }

            if (rawDat.substring(rawDat.lastIndexOf("/")+1).length() < 4) {
                rawDat = rawDat.replace(rawDat.substring(rawDat.lastIndexOf("/")), "");
            }
            if (rawDat.contains("?")) {
                rawDat = rawDat.substring(0, rawDat.indexOf("?"));
            }

            hash = getHash(rawDat);
            client = Reddit.client;
            gson = new Gson();
            mashapeKey = SecretConstants.getImgurApiKey(baseActivity);
        }

        public void doWithData(List<Image> data) {
            if(data == null || data.isEmpty()){
                onError();
            }
        }

        public void doWithDataSingle(final SingleImage data) {
            doWithData(new ArrayList<Image>() {
                {
                    this.add(convertToSingle(data));
                }
            });
        }

        public Image convertToSingle(SingleImage data) {
            try {
                final Image toDo = new Image();
                toDo.setAnimated(data.getAnimated() || data.getLink().contains(".gif"));
                toDo.setDescription(data.getDescription());
                if(data.getAdditionalProperties().containsKey("mp4")){
                    toDo.setHash(getHash(data.getAdditionalProperties().get("mp4").toString()));
                } else {
                    toDo.setHash(getHash(data.getLink()));
                }
                toDo.setTitle(data.getTitle());
                toDo.setExt(data.getLink().substring(data.getLink().lastIndexOf(".")));
                toDo.setHeight(data.getHeight());
                toDo.setWidth(data.getWidth());
                return toDo;
            } catch (Exception e) {
                LogUtil.e(e, "convertToSingle error, data [" + data + "]");
                onError();
                return null;
            }
        }

        JsonElement[] target;
        int count;
        int done;

        AlbumImage album;

        public void parseJson(JsonElement baseData) {
            try {
                if (!baseData.toString().contains("\"data\":[]")) {
                    album = new ObjectMapper().readValue(baseData.toString(), AlbumImage.class);
                    baseActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            doWithData(album.getData().getImages());
                        }
                    });
                } else {
                    String apiUrl = "https://imgur-apiv3.p.mashape.com/3/image/" + hash + ".json";
                    LogUtil.v(apiUrl);
                    JsonObject result = HttpUtil.getImgurMashapeJsonObject(client, gson, apiUrl, mashapeKey);
                    try {
                        if (result == null) {
                            onError();
                            return;
                        }
                        final SingleImage single = new ObjectMapper().readValue(result.toString(), SingleAlbumImage.class).getData();
                        if (single.getLink() != null) {
                            baseActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    doWithDataSingle(single);
                                }
                            });
                        } else {
                            onError();
                        }
                    } catch (Exception e) {
                        LogUtil.e(e, "Error " + apiUrl);
                    }

                }
            } catch (IOException e) {
                LogUtil.e(e, "parseJson error, baseData [" + baseData + "]");
            }
        }

        @Override
        protected ArrayList<JsonElement> doInBackground(final String... sub) {
            if (hash.startsWith("/")) {
                hash = hash.substring(1);
            }
            if (hash.contains(",")) {
                target = new JsonElement[hash.split(",").length];
                count = 0;
                done = 0;
                for (String s : hash.split(",")) {
                    final int pos = count;
                    count++;
                    String apiUrl = "https://imgur-apiv3.p.mashape.com/3/image/" + s + ".json";
                    LogUtil.v(apiUrl);
                    JsonObject result = HttpUtil.getImgurMashapeJsonObject(client, gson, apiUrl, mashapeKey);
                    target[pos] = result;
                    done += 1;
                    if (done == target.length) {
                        final ArrayList<Image> jsons = new ArrayList<>();
                        for (JsonElement el : target) {
                            if (el != null) {
                                try {
                                    SingleImage single = new ObjectMapper().readValue(el.toString(), SingleAlbumImage.class).getData();
                                    LogUtil.v(el.toString());
                                    jsons.add(convertToSingle(single));
                                } catch (IOException e) {
                                    LogUtil.e(e, "Error " + apiUrl);
                                }
                            }
                        }
                        if (jsons.isEmpty()) {
                            onError();
                        } else {
                            baseActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    doWithData(jsons);
                                }
                            });
                        }
                    }
                }
            } else {
                if (baseActivity != null) {
                    final String apiUrl = getUrl(hash);
                    if (albumRequests.contains(apiUrl) && JsonParser.parseString(albumRequests.getString(apiUrl, "")).getAsJsonObject().has("data")) {
                        parseJson(JsonParser.parseString(albumRequests.getString(apiUrl, "")).getAsJsonObject());
                    } else {
                        LogUtil.v(apiUrl);
                        // This call requires no mashape headers, don't pass in the headers Map
                        final JsonObject result = HttpUtil.getJsonObject(client, gson, apiUrl);
                        if (result != null && result.has("data")) {
                            albumRequests.edit().putString(apiUrl, result.toString()).apply();
                            parseJson(result);
                        } else {
                            onError();
                        }
                    }
                    return null;
                }
            }
            return null;

        }


    }

    public static String getUrl(String hash) {
        return "http://imgur.com/ajaxalbums/getimages/" + hash + "/hit.json?all=true";
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
