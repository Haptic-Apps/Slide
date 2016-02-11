package me.ccrama.redditslide.Views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import me.ccrama.redditslide.Activities.FullscreenImage;
import me.ccrama.redditslide.Activities.GifView;
import me.ccrama.redditslide.util.NetworkUtil;

/**
 * Created by carlo_000 on 1/16/2016.
 */
public class OpenImgurLink {
    public static final String IMGUR_CLIENT_ID = "Client-ID " + "bef87913eb202e9";

    public static void openImgurLink(final Context c, String url) {
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        String hash = url.substring(url.lastIndexOf("/"), url.length());

        if(NetworkUtil.isConnected(c))
        Ion.with(c).load("https://api.imgur.com/2/image/" + hash + ".json")
                .asJsonObject().setCallback(new FutureCallback<JsonObject>() {
            @Override
            public void onCompleted(Exception e, JsonObject obj) {
                if (obj.has("error")) {
                    ((Activity) c).finish();
                } else {
                    String type = obj.get("image").getAsJsonObject().get("image").getAsJsonObject().get("type").getAsString();
                    String urls = obj.get("image").getAsJsonObject().get("links").getAsJsonObject().get("original").getAsString();

                    if (type.contains("gif")) {
                        Intent i = new Intent(c, GifView.class);
                        i.putExtra(GifView.EXTRA_URL, urls);
                        c.startActivity(i);
                        ((Activity)c).overridePendingTransition(0, 0);

                        ((Activity) c).finish();


                    } else {
                        Intent i = new Intent(c, FullscreenImage.class);
                        i.putExtra(FullscreenImage.EXTRA_URL, urls);
                        c.startActivity(i);
                        ((Activity)c).overridePendingTransition(0, 0);

                        ((Activity) c).finish();

                    }
                }
            }
        });


    }
}
