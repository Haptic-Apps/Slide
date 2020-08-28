package me.ccrama.redditslide.Views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;

import org.apache.commons.text.StringEscapeUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.ccrama.redditslide.Adapters.ImageGridAdapter;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.ForceTouch.PeekViewActivity;
import me.ccrama.redditslide.ImgurAlbum.AlbumUtils;
import me.ccrama.redditslide.ImgurAlbum.Image;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SecretConstants;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Tumblr.Photo;
import me.ccrama.redditslide.Tumblr.TumblrUtils;
import me.ccrama.redditslide.util.AdBlocker;
import me.ccrama.redditslide.util.GifUtils;
import me.ccrama.redditslide.util.HttpUtil;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.NetworkUtil;


/**
 * Created by ccrama on 3/5/2015.
 */
public class PeekMediaView extends RelativeLayout {

    ContentType.Type contentType;
    private GifUtils.AsyncLoadGif     gif;
    private ExoVideoView              videoView;
    public  WebView                   website;
    private ProgressBar               progress;
    private SubsamplingScaleImageView image;


    public PeekMediaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public PeekMediaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PeekMediaView(Context context) {
        super(context);
        init();
    }

    boolean web;
    float origY = 0;

    public void doClose() {
        website.setVisibility(View.GONE);
        website.loadUrl("about:blank");
        videoView.stop();
        if (gif != null) gif.cancel(true);
    }

    public void doScroll(MotionEvent event) {
        if (origY == 0) {
            origY = event.getY();
        }
        if (web
                && website.canScrollVertically((origY - event.getY()) > 0 ? 0 : 1)
                && Math.abs(origY - event.getY()) > website.getHeight() / 4.0f) {
            website.scrollBy(0, (int) -(origY - event.getY()) / 5);
        }
    }


    public void setUrl(String url) {
        contentType = ContentType.getContentType(url);
        switch (contentType) {
            case ALBUM:
                doLoadAlbum(url);
                progress.setIndeterminate(true);
                break;
            case TUMBLR:
                doLoadTumblr(url);
                progress.setIndeterminate(true);
                break;
            case EMBEDDED:
            case EXTERNAL:
            case LINK:
            case VIDEO:
            case SELF:
            case SPOILER:
            case NONE:
                doLoadLink(url);
                progress.setIndeterminate(false);
                break;
            case REDDIT:
                progress.setIndeterminate(true);
                doLoadReddit(url);
                break;
            case DEVIANTART:
                doLoadDeviantArt(url);
                progress.setIndeterminate(false);
                break;
            case IMAGE:
                doLoadImage(url);
                progress.setIndeterminate(false);
                break;
            case XKCD:
                doLoadXKCD(url);
                progress.setIndeterminate(false);
                break;
            case IMGUR:
                doLoadImgur(url);
                progress.setIndeterminate(false);
                break;
            case GIF:
            case VREDDIT_REDIRECT:
            case VREDDIT_DIRECT:
            case STREAMABLE:
                doLoadGif(url);
                progress.setIndeterminate(false);
                break;
        }
    }

    private void doLoadAlbum(final String url) {
        new AlbumUtils.GetAlbumWithCallback(url, (PeekViewActivity) getContext()) {

            @Override
            public void onError() {
                ((PeekViewActivity) getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        doLoadLink(url);
                    }
                });
            }

            @Override
            public void doWithData(final List<Image> jsonElements) {
                super.doWithData(jsonElements);
                progress.setVisibility(View.GONE);
                images = new ArrayList<>(jsonElements);
                displayImage(images.get(0).getImageUrl());
                if (images.size() > 1) {
                    GridView grid = findViewById(R.id.grid_area);
                    grid.setNumColumns(5);
                    grid.setVisibility(VISIBLE);
                    grid.setAdapter(new ImageGridAdapter(getContext(), images));
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void doLoadTumblr(final String url) {
        new TumblrUtils.GetTumblrPostWithCallback(url, (PeekViewActivity) getContext()) {

            @Override
            public void onError() {
                ((PeekViewActivity) getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        doLoadLink(url);
                    }
                });
            }

            @Override
            public void doWithData(final List<Photo> jsonElements) {
                super.doWithData(jsonElements);
                progress.setVisibility(View.GONE);
                tumblrImages = new ArrayList<>(jsonElements);
                displayImage(tumblrImages.get(0).getOriginalSize().getUrl());
                if (tumblrImages.size() > 1) {
                    GridView grid = findViewById(R.id.grid_area);
                    grid.setNumColumns(5);
                    grid.setVisibility(VISIBLE);
                    grid.setAdapter(new ImageGridAdapter(getContext(), tumblrImages, true));
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    List<Image> images;
    List<Photo> tumblrImages;

    WebChromeClient client;
    WebViewClient   webClient;

    public void setValue(int newProgress) {
        progress.setProgress(newProgress);
        if (newProgress == 100) {
            progress.setVisibility(View.GONE);
        } else if (progress.getVisibility() == View.GONE) {
            progress.setVisibility(View.VISIBLE);
        }
    }

    private class MyWebViewClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            setValue(newProgress);
            super.onProgressChanged(view, newProgress);
        }
    }

    public void doLoadXKCD(final String url) {
        if (NetworkUtil.isConnected(getContext())) {
            final String apiUrl = (url.endsWith("/") ? url : (url + "/")) + "info.0.json";

            new AsyncTask<Void, Void, JsonObject>() {
                @Override
                protected JsonObject doInBackground(Void... params) {
                    return HttpUtil.getJsonObject(Reddit.client, new Gson(), apiUrl);
                }

                @Override
                protected void onPostExecute(final JsonObject result) {
                    if (result != null && !result.isJsonNull() && result.has("error")) {
                        doLoadLink(url);
                    } else {
                        try {
                            if (result != null && !result.isJsonNull() && result.has("img")) {
                                doLoadImage(result.get("img").getAsString());
                            } else {
                                doLoadLink(url);
                            }
                        } catch (Exception e2) {
                            doLoadLink(url);
                        }
                    }

                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void doLoadLink(String url) {
        client = new MyWebViewClient();
        web = true;
        webClient = new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                website.loadUrl(
                        "javascript:(function() { document.getElementsByTagName('video')[0].play(); })()");
            }

            private Map<String, Boolean> loadedUrls = new HashMap<>();

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                boolean ad;
                if (!loadedUrls.containsKey(url)) {
                    ad = AdBlocker.isAd(url, getContext());
                    loadedUrls.put(url, ad);
                } else {
                    ad = loadedUrls.get(url);
                }
                return ad && SettingValues.isPro ? AdBlocker.createEmptyResource()
                        : super.shouldInterceptRequest(view, url);
            }

        };
        website.setVisibility(View.VISIBLE);
        website.setWebChromeClient(client);
        website.setWebViewClient(webClient);
        website.getSettings().setBuiltInZoomControls(true);
        website.getSettings().setDisplayZoomControls(false);
        website.getSettings().setJavaScriptEnabled(true);
        website.getSettings().setLoadWithOverviewMode(true);
        website.getSettings().setUseWideViewPort(true);
        website.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent, String contentDisposition,
                    String mimetype, long contentLength) {
                //Downloads using download manager on default browser
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                getContext().startActivity(i);
            }
        });
        website.loadUrl(url);
    }

    private void doLoadReddit(String url) {
        RedditItemView v = findViewById(R.id.reddit_item);
        v.loadUrl(this, url, progress);
    }

    public void doLoadDeviantArt(String url) {
        final String apiUrl = "http://backend.deviantart.com/oembed?url=" + url;
        LogUtil.v(apiUrl);
        new AsyncTask<Void, Void, JsonObject>() {
            @Override
            protected JsonObject doInBackground(Void... params) {
                return HttpUtil.getJsonObject(Reddit.client, new Gson(), apiUrl);
            }

            @Override
            protected void onPostExecute(JsonObject result) {
                LogUtil.v("doLoad onPostExecute() called with: " + "result = [" + result + "]");
                if (result != null && !result.isJsonNull() && (result.has("fullsize_url")
                        || result.has("url"))) {
                    String url;
                    if (result.has("fullsize_url")) {
                        url = result.get("fullsize_url").getAsString();
                    } else {
                        url = result.get("url").getAsString();
                    }
                    doLoadImage(url);
                } else {
                    //todo error out
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void doLoadImgur(String url) {
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        final String finalUrl = url;
        String hash = url.substring(url.lastIndexOf("/"));

        if (NetworkUtil.isConnected(getContext())) {
            if (hash.startsWith("/")) hash = hash.substring(1);
            final String apiUrl = "https://imgur-apiv3.p.mashape.com/3/image/" + hash + ".json";
            LogUtil.v(apiUrl);

            new AsyncTask<Void, Void, JsonObject>() {
                @Override
                protected JsonObject doInBackground(Void... params) {
                    return HttpUtil.getImgurMashapeJsonObject(Reddit.client, new Gson(), apiUrl,
                            SecretConstants.getImgurApiKey(getContext()));
                }

                @Override
                protected void onPostExecute(JsonObject result) {
                    if (result != null && !result.isJsonNull() && result.has("error")) {
                        ///todo error out
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
                                    doLoadGif(urls);
                                } else if (!imageShown) { //only load if there is no image
                                    displayImage(urls);
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
                                    doLoadGif(((mp4 == null || mp4.isEmpty()) ? urls : mp4));
                                } else if (!imageShown) { //only load if there is no image
                                    displayImage(urls);
                                }
                            } else {
                                if (!imageShown) doLoadImage(finalUrl);
                            }
                        } catch (Exception e2) {
                            //todo error out
                        }
                    }

                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    boolean imageShown;

    public void doLoadImage(String contentUrl) {
        if (contentUrl != null && contentUrl.contains("bildgur.de")) {
            contentUrl = contentUrl.replace("b.bildgur.de", "i.imgur.com");
        }
        if (contentUrl != null && ContentType.isImgurLink(contentUrl)) {
            contentUrl = contentUrl + ".png";
        }
        if (contentUrl != null && contentUrl.contains("m.imgur.com")) {
            contentUrl = contentUrl.replace("m.imgur.com", "i.imgur.com");
        }
        if (contentUrl == null) {
            //todo error out
        }

        if ((contentUrl != null
                && !contentUrl.startsWith("https://i.redditmedia.com")
                && !contentUrl.startsWith("https://i.reddituploads.com")
                && !contentUrl.contains(
                "imgur.com"))) { //we can assume redditmedia and imgur links are to direct images and not websites
            progress.setVisibility(View.VISIBLE);
            progress.setIndeterminate(true);

            final String finalUrl2 = contentUrl;
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        URL obj = new URL(finalUrl2);
                        URLConnection conn = obj.openConnection();
                        final String type = conn.getHeaderField("Content-Type");
                        ((PeekViewActivity) getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!imageShown
                                        && type != null
                                        && !type.isEmpty()
                                        && type.startsWith("image/")) {
                                    //is image
                                    if (type.contains("gif")) {
                                        doLoadGif(finalUrl2.replace(".jpg", ".gif")
                                                .replace(".png", ".gif"));
                                    } else if (!imageShown) {
                                        displayImage(finalUrl2);
                                    }
                                    actuallyLoaded = finalUrl2;
                                } else if (!imageShown) {
                                    //todo error out
                                }
                            }
                        });

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    progress.setVisibility(View.GONE);
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        } else {
            displayImage(contentUrl);
        }
    }

    String actuallyLoaded;

    public void doLoadGif(final String dat) {
        videoView = findViewById(R.id.gif);
        videoView.clearFocus();
        findViewById(R.id.gifarea).setVisibility(View.VISIBLE);
        findViewById(R.id.submission_image).setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        gif = new GifUtils.AsyncLoadGif((PeekViewActivity) getContext(),
                videoView, progress, null, false, true, "") {
            @Override
            public void onError() {
                doLoadLink(dat);
            }
        };
        gif.execute(dat);
    }

    public void displayImage(final String urlB) {
        LogUtil.v("Displaying " + urlB);
        final String url = StringEscapeUtils.unescapeHtml4(urlB);

        if (!imageShown) {
            actuallyLoaded = url;
            final SubsamplingScaleImageView i =
                    findViewById(R.id.submission_image);

            i.setMinimumDpi(70);
            i.setMinimumTileDpi(240);
            progress.setIndeterminate(false);
            progress.setProgress(0);

            final Handler handler = new Handler();
            final Runnable progressBarDelayRunner = new Runnable() {
                public void run() {
                    progress.setVisibility(View.VISIBLE);
                }
            };
            handler.postDelayed(progressBarDelayRunner, 500);

            ImageView fakeImage = new ImageView(getContext());
            fakeImage.setLayoutParams(new LinearLayout.LayoutParams(i.getWidth(), i.getHeight()));
            fakeImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

            File f = ((Reddit) getContext().getApplicationContext()).getImageLoader()
                    .getDiskCache()
                    .get(url);
            if (f != null && f.exists()) {
                imageShown = true;

                i.setOnImageEventListener(new SubsamplingScaleImageView.OnImageEventListener() {
                    @Override
                    public void onReady() {

                    }

                    @Override
                    public void onImageLoaded() {

                    }

                    @Override
                    public void onPreviewLoadError(Exception e) {

                    }

                    @Override
                    public void onImageLoadError(Exception e) {
                        imageShown = false;
                        LogUtil.v("No image displayed");
                    }

                    @Override
                    public void onTileLoadError(Exception e) {

                    }

                    @Override
                    public void onPreviewReleased() {

                    }
                });
                try {
                    i.setImage(ImageSource.uri(f.getAbsolutePath()));
                    i.setZoomEnabled(false);
                } catch (Exception e) {
                    imageShown = false;
                    //todo  i.setImage(ImageSource.bitmap(loadedImage));
                }
                (progress).setVisibility(View.GONE);
                handler.removeCallbacks(progressBarDelayRunner);

            } else {
                ((Reddit) getContext().getApplicationContext()).getImageLoader()
                        .displayImage(url, new ImageViewAware(fakeImage),
                                new DisplayImageOptions.Builder().resetViewBeforeLoading(true)
                                        .cacheOnDisk(true)
                                        .imageScaleType(ImageScaleType.NONE)
                                        .cacheInMemory(false)
                                        .build(), new ImageLoadingListener() {

                                    @Override
                                    public void onLoadingStarted(String imageUri, View view) {
                                        imageShown = true;
                                    }

                                    @Override
                                    public void onLoadingFailed(String imageUri, View view,
                                            FailReason failReason) {
                                        Log.v(LogUtil.getTag(), "LOADING FAILED");
                                        imageShown = false;
                                    }

                                    @Override
                                    public void onLoadingComplete(String imageUri, View view,
                                            Bitmap loadedImage) {
                                        imageShown = true;

                                        File f =
                                                ((Reddit) getContext().getApplicationContext()).getImageLoader()
                                                        .getDiskCache()
                                                        .get(url);
                                        if (f != null && f.exists()) {
                                            i.setImage(ImageSource.uri(f.getAbsolutePath()));
                                        } else {
                                            i.setImage(ImageSource.bitmap(loadedImage));
                                        }
                                        (progress).setVisibility(View.GONE);
                                        handler.removeCallbacks(progressBarDelayRunner);
                                    }

                                    @Override
                                    public void onLoadingCancelled(String imageUri, View view) {
                                        Log.v(LogUtil.getTag(), "LOADING CANCELLED");

                                    }
                                }, new ImageLoadingProgressListener() {
                                    @Override
                                    public void onProgressUpdate(String imageUri, View view,
                                            int current, int total) {
                                        progress.setProgress(Math.round(100.0f * current / total));
                                    }
                                });
            }
        }
    }

    private void init() {
        inflate(getContext(), R.layout.peek_media_view, this);
        this.image = findViewById(R.id.submission_image);
        this.videoView = findViewById(R.id.gif);
        this.website = findViewById(R.id.website);
        this.progress = findViewById(R.id.progress);
    }
}