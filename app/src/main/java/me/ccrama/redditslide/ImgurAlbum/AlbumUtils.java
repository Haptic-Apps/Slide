package me.ccrama.redditslide.ImgurAlbum;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SecretConstants;
import me.ccrama.redditslide.util.LogUtil;

/**
 * Created by carlo_000 on 2/1/2016.
 */
public class AlbumUtils {

    public static SharedPreferences albumRequests;

    private static String getHash(String s) {
        String next = s.substring(s.lastIndexOf("/"), s.length());
        if (next.contains(".")) {
            next = next.substring(0, next.indexOf("."));
        }
        if (next.startsWith("/")) {
            next = next.substring(1, next.length());
        }
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

        public void onError() {

        }

        public GetAlbumWithCallback(@NotNull String url, @NotNull Activity baseActivity) {


            this.baseActivity = baseActivity;
            String rawDat = cutEnds(url);

            if (rawDat.endsWith("/")) {
                rawDat = rawDat.substring(0, rawDat.length() - 1);
            }

            if (rawDat.substring(rawDat.lastIndexOf("/")+1, rawDat.length()).length() < 4) {
                rawDat = rawDat.replace(rawDat.substring(rawDat.lastIndexOf("/"), rawDat.length()), "");
            }
            if (rawDat.contains("?")) {
                rawDat = rawDat.substring(0, rawDat.indexOf("?"));
            }

            hash = getHash(rawDat);

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
                if(data.getAdditionalProperties().keySet().contains("mp4")){
                    toDo.setHash(getHash(data.getAdditionalProperties().get("mp4").toString()));
                } else {
                    toDo.setHash(getHash(data.getLink()));
                }
                toDo.setTitle(data.getTitle());
                toDo.setExt(data.getLink().substring(data.getLink().lastIndexOf("."), data.getLink().length()));
                toDo.setHeight(data.getHeight());
                toDo.setWidth(data.getWidth());
                return toDo;
            } catch (Exception e) {
                e.printStackTrace();
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
                    doWithData(album.getData().getImages());
                } else {
                    Ion.with(baseActivity).load("https://imgur-apiv3.p.mashape.com/3/image/" + hash + ".json")
                            .addHeader("X-Mashape-Key", SecretConstants.getImgurApiKey(baseActivity)).addHeader("Authorization", "Client-ID " + "bef87913eb202e9")
                            .asJsonObject().setCallback(
                            new FutureCallback<JsonObject>() {
                                @Override
                                public void onCompleted(Exception e, JsonObject obj) {
                                    try {
                                        if(obj == null) {
                                            onError();
                                            return;
                                        }
                                        SingleImage single = new ObjectMapper().readValue(obj.toString(), SingleAlbumImage.class).getData();
                                        if (single.getLink() != null)
                                            doWithDataSingle(single);
                                        else
                                            onError();


                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            }
                    );
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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
                            target[pos] = obj;

                            done += 1;
                            if (done == target.length) {
                                final ArrayList<Image> jsons = new ArrayList<>();
                                for (JsonElement el : target) {
                                    if (el != null) {
                                        try {
                                            SingleImage single = new ObjectMapper().readValue(el.toString(), SingleAlbumImage.class).getData();
                                            LogUtil.v(el.toString());
                                            jsons.add(convertToSingle(single));
                                        } catch (IOException e1) {
                                            e1.printStackTrace();
                                        }
                                    }
                                }
                                if (jsons.isEmpty()) {
                                    onError();
                                } else {
                                    doWithData(jsons);
                                }
                            }
                        }
                    });

                }

            } else {
                if (baseActivity != null) {
                    final String url = getUrl(hash);
                    if (albumRequests.contains(url) && new JsonParser().parse(albumRequests.getString(url, "")).getAsJsonObject().has("data")) {
                        baseActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                parseJson(new JsonParser().parse(albumRequests.getString(url, "")).getAsJsonObject());
                            }
                        });

                    } else {
                        Ion.with(baseActivity)
                                .load(url)
                                .asJsonObject()
                                .setCallback(new FutureCallback<JsonObject>() {
                                    @Override
                                    public void onCompleted(Exception e, final JsonObject result) {
                                        if (result != null && result.has("data")) {
                                            albumRequests.edit().putString(url, result.toString()).apply();
                                            baseActivity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    parseJson(result);
                                                }
                                            });
                                        } else {
                                            onError();
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
