package me.ccrama.redditslide.Views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.core.text.HtmlCompat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SecretConstants;
import me.ccrama.redditslide.util.GifUtils;
import me.ccrama.redditslide.util.HttpUtil;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.NetworkUtil;
import okhttp3.OkHttpClient;


/**
 * Created by ccrama on 3/5/2015.
 */
public class PopMediaView {

    public static boolean shouldTruncate(String url) {
        try {
            final URI uri = new URI(url);
            final String path = uri.getPath();

            return !ContentType.isGif(uri)
                    && !ContentType.isImage(uri)
                    && path.contains(".");
        } catch (URISyntaxException e) {
            return false;
        }
    }

    OkHttpClient client;
    Gson gson;
    String mashapeKey;

    public void doPop(View v, String contentUrl, Context c) {
        client = Reddit.client;
        gson = new Gson();
        mashapeKey = SecretConstants.getImgurApiKey(c);

        if (contentUrl.contains("reddituploads.com")) {
            contentUrl = HtmlCompat.fromHtml(contentUrl, HtmlCompat.FROM_HTML_MODE_LEGACY).toString();
        }
        if (contentUrl != null && shouldTruncate(contentUrl)) {
            contentUrl = contentUrl.substring(0, contentUrl.lastIndexOf("."));
        }

        doLoad(contentUrl, v);
    }

    public void doLoad(final String contentUrl, View v) {
        switch (ContentType.getContentType(contentUrl)) {
            case DEVIANTART:
                doLoadDeviantArt(contentUrl, v);
                break;
            case IMAGE:
                doLoadImage(contentUrl, v);
                break;
            case IMGUR:
                doLoadImgur(contentUrl, v);
                break;
            case STREAMABLE:
            case GIF:
                doLoadGif(contentUrl, v);
                break;
        }
    }

    public void doLoadGif(final String dat, View v) {
        v.findViewById(R.id.gifarea).setVisibility(View.VISIBLE);

        ExoVideoView videoView = v.findViewById(R.id.gif);

        videoView.clearFocus();
        v.findViewById(R.id.submission_image).setVisibility(View.GONE);
        final ProgressBar loader = v.findViewById(R.id.gifprogress);
        v.findViewById(R.id.progress).setVisibility(View.GONE);
        GifUtils.AsyncLoadGif gif = new GifUtils.AsyncLoadGif(((Activity) v.getContext()), videoView, loader, null,
                null, false, true, "");
        gif.execute(dat);
    }

    public void doLoadImgur(String url, final View v) {
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        final String finalUrl = url;
        String hash = url.substring(url.lastIndexOf("/"));

        if (NetworkUtil.isConnected(v.getContext())) {
            if (hash.startsWith("/")) hash = hash.substring(1);
            final String apiUrl = "https://imgur-apiv3.p.mashape.com/3/image/" + hash + ".json";
            LogUtil.v(apiUrl);

            new AsyncTask<Void, Void, JsonObject>() {
                @Override
                protected JsonObject doInBackground(Void... params) {
                    return HttpUtil.getImgurMashapeJsonObject(client, gson, apiUrl, mashapeKey);
                }

                @Override
                protected void onPostExecute(JsonObject result) {
                    if (result != null && !result.isJsonNull() && result.has("error")) {
                        LogUtil.v("Error loading content");
                    } else {
                        try {
                            if (result != null && !result.isJsonNull() && result.has("image")) {
                                String type = result.get("image")
                                        .getAsJsonObject()
                                        .get("image")
                                        .getAsJsonObject()
                                        .get("type")
                                        .getAsString();
                                String urls = result.get("image")
                                        .getAsJsonObject()
                                        .get("links")
                                        .getAsJsonObject()
                                        .get("original")
                                        .getAsString();

                                if (type.contains("gif")) {
                                    doLoadGif(urls, v);
                                } else { //only load if there is no image
                                    doLoadImage(urls, v);
                                }
                            } else if (result != null && result.has("data")) {
                                String type = result.get("data")
                                        .getAsJsonObject()
                                        .get("type")
                                        .getAsString();
                                String urls = result.get("data")
                                        .getAsJsonObject()
                                        .get("link")
                                        .getAsString();
                                String mp4 = "";
                                if (result.get("data").getAsJsonObject().has("mp4")) {
                                    mp4 = result.get("data")
                                            .getAsJsonObject()
                                            .get("mp4")
                                            .getAsString();
                                }

                                if (type.contains("gif")) {
                                    doLoadGif(((mp4 == null || mp4.isEmpty()) ? urls : mp4), v);
                                } else { //only load if there is no image
                                    doLoadImage(urls, v);
                                }
                            } else {
                                doLoadImage(finalUrl, v);
                            }
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }

                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void doLoadDeviantArt(String url, final View v) {
        final String apiUrl = "http://backend.deviantart.com/oembed?url=" + url;
        LogUtil.v(apiUrl);
        new AsyncTask<Void, Void, JsonObject>() {
            @Override
            protected JsonObject doInBackground(Void... params) {
                return HttpUtil.getJsonObject(client, gson, apiUrl);
            }

            @Override
            protected void onPostExecute(JsonObject result) {
                LogUtil.v("doLoad onPostExecute() called with: " + "result = [" + result + "]");
                if (result != null && !result.isJsonNull() && (result.has("fullsize_url") || result.has("url"))) {
                    String url;
                    if (result.has("fullsize_url")) {
                        url = result.get("fullsize_url").getAsString();
                    } else {
                        url = result.get("url").getAsString();
                    }
                    doLoadImage(url, v);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void doLoadImage(String contentUrl, final View v) {
        if (contentUrl != null && contentUrl.contains("bildgur.de"))
            contentUrl = contentUrl.replace("b.bildgur.de", "i.imgur.com");
        if (contentUrl != null && ContentType.isImgurLink(contentUrl)) {
            contentUrl = contentUrl + ".png";
        }

        v.findViewById(R.id.gifprogress).setVisibility(View.GONE);

        if (contentUrl != null && contentUrl.contains("m.imgur.com"))
            contentUrl = contentUrl.replace("m.imgur.com", "i.imgur.com");
        if (contentUrl == null) {

            //todo maybe something better

        }

        if ((contentUrl != null && !contentUrl.startsWith("https://i.redditmedia.com") && !contentUrl.startsWith("https://i.reddituploads.com") && !contentUrl.contains("imgur.com"))) { //we can assume redditmedia and imgur links are to direct images and not websites
            v.findViewById(R.id.progress).setVisibility(View.VISIBLE);
            ((ProgressBar) v.findViewById(R.id.progress)).setIndeterminate(true);

            final String finalUrl2 = contentUrl;
            new AsyncTask<Void, Void, Void>() {
                String type;

                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        URL obj = new URL(finalUrl2);
                        URLConnection conn = obj.openConnection();
                        type = conn.getHeaderField("Content-Type");


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    if (type != null && !type.isEmpty() && type.startsWith("image/")) {
                        //is image
                        if (type.contains("gif")) {
                            doLoadGif(finalUrl2.replace(".jpg", ".gif").replace(".png", ".gif"), v);
                        } else {
                            displayImage(finalUrl2, v);
                        }
                    }
                    v.findViewById(R.id.progress).setVisibility(View.GONE);
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        } else {
            displayImage(contentUrl, v);
        }
    }

    public void displayImage(final String url, final View v) {
        final SubsamplingScaleImageView i = v.findViewById(R.id.submission_image);

        i.setMinimumDpi(70);
        i.setMinimumTileDpi(240);
        final ProgressBar bar = v.findViewById(R.id.progress);
        bar.setIndeterminate(false);
        bar.setProgress(0);

        final Handler handler = new Handler();
        final Runnable progressBarDelayRunner = new Runnable() {
            public void run() {
                bar.setVisibility(View.VISIBLE);
            }
        };
        handler.postDelayed(progressBarDelayRunner, 500);

        ImageView fakeImage = new ImageView(v.getContext());
        fakeImage.setLayoutParams(new LinearLayout.LayoutParams(i.getWidth(), i.getHeight()));
        fakeImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

        File f = ((Reddit) v.getContext().getApplicationContext()).getImageLoader().getDiskCache().get(url);
        if (f != null && f.exists()) {
            try {
                i.setImage(ImageSource.uri(f.getAbsolutePath()));
            } catch (Exception e) {
                //todo  i.setImage(ImageSource.bitmap(loadedImage));
            }
            (v.findViewById(R.id.progress)).setVisibility(View.GONE);
            handler.removeCallbacks(progressBarDelayRunner);
        } else {
            ((Reddit) v.getContext().getApplicationContext()).getImageLoader()
                    .displayImage(url, new ImageViewAware(fakeImage), new DisplayImageOptions.Builder()
                            .resetViewBeforeLoading(true)
                            .cacheOnDisk(true)
                            .imageScaleType(ImageScaleType.NONE)
                            .cacheInMemory(false)
                            .build(), new ImageLoadingListener() {

                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                            Log.v(LogUtil.getTag(), "LOADING FAILED");

                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            File f = ((Reddit) v.getContext().getApplicationContext()).getImageLoader().getDiskCache().get(url);
                            if (f != null && f.exists()) {
                                i.setImage(ImageSource.uri(f.getAbsolutePath()));
                            } else {
                                i.setImage(ImageSource.bitmap(loadedImage));
                            }
                            (v.findViewById(R.id.progress)).setVisibility(View.GONE);
                            handler.removeCallbacks(progressBarDelayRunner);
                        }

                        @Override
                        public void onLoadingCancelled(String imageUri, View view) {
                            Log.v(LogUtil.getTag(), "LOADING CANCELLED");

                        }
                    }, new ImageLoadingProgressListener() {
                        @Override
                        public void onProgressUpdate(String imageUri, View view, int current, int total) {
                            ((ProgressBar) v.findViewById(R.id.progress)).setProgress(Math.round(100.0f * current / total));
                        }
                    });
        }
    }
}
