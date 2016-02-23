package me.ccrama.redditslide.Views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import me.ccrama.redditslide.Activities.FullscreenImage;
import me.ccrama.redditslide.Activities.GifView;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.NetworkUtil;

/**
 * Created by carlo_000 on 1/16/2016.
 */

public class OpenImgurLink {
    public static final String IMGUR_CLIENT_ID = "Client-ID " + "bef87913eb202e9";

    /**
     * This method determines the content type of a shortened imgur link and openes it in the
     * appropriate class
     *
     * Possible content types are image, gif and gallery (not sure about the last, could be a bug on imgurs side)
     *
     * @param c context for opening the link in the class
     * @param url Url in the format www.imgur.com/$HASH
     */
    public static void openImgurLink(final Context c, String url) {

        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        String hash = url.substring(url.lastIndexOf("/"), url.length());

        if (NetworkUtil.isConnected(c)) {
            final String apiCallUrl = "https://api.imgur.com/3/image/" + hash + ".json";
            Log.v(LogUtil.getTag(), "Getting imgur content type (image api): " + apiCallUrl);
            Ion.with(c)
                    .load(apiCallUrl)
                    .addHeader("Authorization", IMGUR_CLIENT_ID)
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (result == null || !result.get("success").getAsBoolean()) {
                                Log.w(LogUtil.getTag(), "Cannot get imgur content type for " + apiCallUrl);
                                ((Activity) c).finish();
                            } else {
                                JsonObject resultData = result.get("data").getAsJsonObject();
                                boolean isGif = resultData.get("animated").getAsBoolean();
                                String url = resultData.get("link").getAsString();

                                if (isGif) {
                                    Intent i = new Intent(c, GifView.class);
                                    i.putExtra(GifView.EXTRA_URL, url);
                                    c.startActivity(i);

                                    ((Activity) c).overridePendingTransition(0, 0);
                                    ((Activity) c).finish();

                                } else {
                                    Intent i = new Intent(c, FullscreenImage.class);
                                    i.putExtra(FullscreenImage.EXTRA_URL, url);
                                    c.startActivity(i);

                                    ((Activity) c).overridePendingTransition(0, 0);
                                    ((Activity) c).finish();
                                }
                            }
                        }
                    });
        }
    }
}
