package me.ccrama.redditslide.Views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import me.ccrama.redditslide.Activities.MediaView;
import me.ccrama.redditslide.Activities.Website;
import me.ccrama.redditslide.SecretConstants;
import me.ccrama.redditslide.util.NetworkUtil;

/**
 * Created by carlo_000 on 1/16/2016.
 */
public class OpenImgurLink {
    public static void openImgurLink(final Context c, String url) {
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        String hash = url.substring(url.lastIndexOf("/"), url.length());

        if (NetworkUtil.isConnected(c)) {
            final String finalUrl = url;
            Ion.with(c).load("https://imgur-apiv3.p.mashape.com/3/image/" + hash + ".json")
                    .addHeader("X-Mashape-Key", SecretConstants.getImgurApiKey(c)).addHeader("Authorization", "Client-ID " + "bef87913eb202e9")
                    .asJsonObject().setCallback(new FutureCallback<JsonObject>() {
                @Override
                public void onCompleted(Exception e, JsonObject obj) {
                    if (obj != null && !obj.isJsonNull() && obj.has("error")) {
                        ((Activity) c).finish();
                    } else {
                        try {
                            String urls = obj.get("image").getAsJsonObject().get("links").getAsJsonObject().get("original").getAsString();

                            Intent i = new Intent(c, MediaView.class);
                            i.putExtra(MediaView.EXTRA_URL, urls);
                            c.startActivity(i);
                            ((Activity) c).overridePendingTransition(0, 0);
                            ((Activity) c).finish();
                        } catch (Exception e2) {
                            Intent i = new Intent(c, Website.class);
                            i.putExtra(Website.EXTRA_URL, finalUrl);
                            c.startActivity(i);
                        }
                    }
                }
            });
        }


    }
}
