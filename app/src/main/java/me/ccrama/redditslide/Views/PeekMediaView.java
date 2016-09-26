package me.ccrama.redditslide.Views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.klinker.android.peekview.PeekViewActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import me.ccrama.redditslide.Activities.MediaView;
import me.ccrama.redditslide.Activities.Website;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SecretConstants;
import me.ccrama.redditslide.SettingValues;
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
    private MediaVideoView            videoView;
    private WebView                   website;
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


    public void setUrl(String url) {
        contentType = ContentType.getContentType(url);
        switch (contentType) {
            case ALBUM:
            case TUMBLR:
            case EMBEDDED:
            case EXTERNAL:
            case LINK:
            case VIDEO:
            case SELF:
            case SPOILER:
            case NONE:
            case REDDIT:
                doLoadLink(url);
                break;
            case DEVIANTART:
                doLoadDeviantArt(url);
                break;
            case IMAGE:
            case XKCD:
                doLoadImage(url);
                break;
            case IMGUR:
                doLoadImgur(url);
            case GIF:
            case STREAMABLE:
            case VID_ME:
                doLoadGif(url);
                break;
        }
    }
    
    WebChromeClient   client;
    WebViewClient webClient;

    private void doLoadLink(String url) {
        client = new WebChromeClient();
        webClient = new WebViewClient();
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
        }.execute();
    }

    public void doLoadImgur(String url) {
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        final String finalUrl = url;
        String hash = url.substring(url.lastIndexOf("/"), url.length());

        if (NetworkUtil.isConnected(getContext())) {
            if (hash.startsWith("/")) hash = hash.substring(1, hash.length());
            final String apiUrl = "https://imgur-apiv3.p.mashape.com/3/image/" + hash + ".json";
            LogUtil.v(apiUrl);

            new AsyncTask<Void, Void, JsonObject>() {
                @Override
                protected JsonObject doInBackground(Void... params) {
                    return HttpUtil.getImgurMashapeJsonObject(Reddit.client, new Gson(), apiUrl, SecretConstants
                            .getImgurApiKey(getContext()));
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
            }.execute();
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
        findViewById(R.id.gifprogress).setVisibility(View.GONE);

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
            findViewById(R.id.progress).setVisibility(View.VISIBLE);
            ((ProgressBar) findViewById(R.id.progress)).setIndeterminate(true);

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
                    findViewById(R.id.progress).setVisibility(View.GONE);
                }
            }.execute();

        } else {
            displayImage(contentUrl);
        }
    }

    String actuallyLoaded;
    
    public void doLoadGif(final String dat) {
        videoView = (MediaVideoView) findViewById(R.id.gif);
        videoView.clearFocus();
        findViewById(R.id.gifarea).setVisibility(View.VISIBLE);
        findViewById(R.id.submission_image).setVisibility(View.GONE);
        final ProgressBar loader = (ProgressBar) findViewById(R.id.gifprogress);
        findViewById(R.id.progress).setVisibility(View.GONE);
        gif = new GifUtils.AsyncLoadGif((PeekViewActivity)getContext(), (MediaVideoView) findViewById(R.id.gif), loader,
                findViewById(R.id.placeholder), null, false, true, true);
        gif.execute(dat);
    }

    public void displayImage(final String urlB) {
        LogUtil.v("Displaying " + urlB);
        final String url = StringEscapeUtils.unescapeHtml4(urlB);

        if (!imageShown) {
            actuallyLoaded = url;
            final SubsamplingScaleImageView i =
                    (SubsamplingScaleImageView) findViewById(R.id.submission_image);

            i.setMinimumDpi(70);
            i.setMinimumTileDpi(240);
            final ProgressBar bar = (ProgressBar) findViewById(R.id.progress);
            bar.setIndeterminate(false);
            bar.setProgress(0);

            final Handler handler = new Handler();
            final Runnable progressBarDelayRunner = new Runnable() {
                public void run() {
                    bar.setVisibility(View.VISIBLE);
                }
            };
            handler.postDelayed(progressBarDelayRunner, 500);

            ImageView fakeImage = new ImageView(getContext());
            fakeImage.setLayoutParams(new LinearLayout.LayoutParams(i.getWidth(), i.getHeight()));
            fakeImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

            File f = ((Reddit) getContext().getApplicationContext()).getImageLoader()
                    .getDiscCache()
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
                });
                try {
                    i.setImage(ImageSource.uri(f.getAbsolutePath()));
                    i.setZoomEnabled(false);
                } catch (Exception e) {
                    imageShown = false;
                    //todo  i.setImage(ImageSource.bitmap(loadedImage));
                }
                (findViewById(R.id.progress)).setVisibility(View.GONE);
                handler.removeCallbacks(progressBarDelayRunner);

            } else {
                ((Reddit) getContext().getApplicationContext()).getImageLoader()
                        .displayImage(url, new ImageViewAware(fakeImage),
                                new DisplayImageOptions.Builder().resetViewBeforeLoading(true)
                                        .cacheOnDisk(true)
                                        .imageScaleType(ImageScaleType.NONE)
                                        .cacheInMemory(false)
                                        .build(), new ImageLoadingListener() {
                                    private View mView;

                                    @Override
                                    public void onLoadingStarted(String imageUri, View view) {
                                        imageShown = true;
                                        mView = view;
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
                                                        .getDiscCache()
                                                        .get(url);
                                        if (f != null && f.exists()) {
                                            i.setImage(ImageSource.uri(f.getAbsolutePath()));
                                        } else {
                                            i.setImage(ImageSource.bitmap(loadedImage));
                                        }
                                        (findViewById(R.id.progress)).setVisibility(View.GONE);
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
                                        ((ProgressBar) findViewById(R.id.progress)).setProgress(
                                                Math.round(100.0f * current / total));
                                    }
                                });
            }
        }
    }

    private void init() {
        inflate(getContext(), R.layout.peek_media_view, this);
        this.image = (SubsamplingScaleImageView) findViewById(R.id.submission_image);
        this.videoView = (MediaVideoView) findViewById(R.id.gif);
        this.website = (WebView) findViewById(R.id.website);
    }

}