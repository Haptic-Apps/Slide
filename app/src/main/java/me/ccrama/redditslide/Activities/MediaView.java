package me.ccrama.redditslide.Activities;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.NotificationCompat;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.cocosw.bottomsheet.BottomSheet;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.Fragments.FolderChooserDialogCreate;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SecretConstants;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Views.ImageSource;
import me.ccrama.redditslide.Views.MediaVideoView;
import me.ccrama.redditslide.Views.SubsamplingScaleImageView;
import me.ccrama.redditslide.util.GifUtils;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.NetworkUtil;


/**
 * Created by ccrama on 3/5/2015.
 */
public class MediaView extends FullScreenActivity implements FolderChooserDialogCreate.FolderCallback {

    public static String fileLoc;
    public float previous;
    public static final String EXTRA_URL = "url";
    public static final String EXTRA_DISPLAY_URL = "displayUrl";
    public static final String EXTRA_LQ = "lq";
    public static final String EXTRA_SHARE_URL = "urlShare";
    public static boolean didLoadGif;
    public boolean hidden;
    public boolean imageShown;

    @Override
    public void onResume() {
        super.onResume();
        if (videoView != null) {
            videoView.seekTo(stopPosition);
            videoView.start();
        }
    }

    public String actuallyLoaded;
    public boolean isGif;

    public void showBottomSheetImage() {

        int[] attrs = new int[]{R.attr.tint};
        TypedArray ta = obtainStyledAttributes(attrs);

        int color = ta.getColor(0, Color.WHITE);
        Drawable external = getResources().getDrawable(R.drawable.openexternal);
        Drawable share = getResources().getDrawable(R.drawable.share);
        Drawable image = getResources().getDrawable(R.drawable.image);
        Drawable save = getResources().getDrawable(R.drawable.save);
        Drawable file = getResources().getDrawable(R.drawable.savecontent);

        external.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        share.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        image.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        save.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        file.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        ta.recycle();

        BottomSheet.Builder b = new BottomSheet.Builder(this)
                .title(contentUrl);

        b.sheet(2, external, "Open externally");
        b.sheet(5, share, "Share link");

        if (!isGif)
            b.sheet(3, image, "Share image");
        b.sheet(4, save, "Save " + (isGif ? "MP4" : "image"));
        if (isGif && !contentUrl.contains(".mp4") && !contentUrl.contains("streamable.com") && !contentUrl.contains("vid.me")) {
            String type = contentUrl.substring(contentUrl.lastIndexOf(".") + 1, contentUrl.length()).toUpperCase();
            try {
                if (type.equals("GIFV") && new URL(contentUrl).getHost().equals("i.imgur.com")) {
                    type = "GIF";
                    contentUrl = contentUrl.replace(".gifv", ".gif");
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            b.sheet(6, file, "Save " + type);
        }
        b.listener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case (2): {
                        Reddit.defaultShare(contentUrl, MediaView.this);
                        break;
                    }
                    case (3): {
                        shareImage(actuallyLoaded);
                        break;
                    }
                    case (5): {
                        Reddit.defaultShareText("", contentUrl, MediaView.this);
                        break;
                    }
                    case (6): {
                        saveFile(contentUrl);
                    }
                    break;
                    case (4): {
                        if (!isGif) {
                            new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected Void doInBackground(Void... params) {
                                    String url = contentUrl;
                                    final String finalUrl1 = url;
                                    final String finalUrl = contentUrl;
                                    try {
                                        ((Reddit) getApplication()).getImageLoader()
                                                .loadImage(finalUrl, new SimpleImageLoadingListener() {
                                                    @Override
                                                    public void onLoadingComplete(String imageUri, View view, final Bitmap loadedImage) {
                                                        saveImageGallery(loadedImage, finalUrl1);
                                                    }
                                                });
                                    } catch (Exception e) {
                                        Log.v(LogUtil.getTag(), "COULDN'T DOWNLOAD!");
                                    }
                                    return null;
                                }
                            }.execute();

                        } else {
                            doOnClick.run();
                        }
                        break;
                    }
                }
            }
        });
        b.show();
    }

    NotificationManager mNotifyManager;
    NotificationCompat.Builder mBuilder;

    public void saveFile(final String baseUrl) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if (Reddit.appRestart.getString("imagelocation", "").isEmpty()) {
                    showFirstDialog();
                } else if (!new File(Reddit.appRestart.getString("imagelocation", "")).exists()) {
                    showErrorDialog();
                } else {
                    final File f = new File(Reddit.appRestart.getString("imagelocation", "") + File.separator + UUID.randomUUID().toString() + baseUrl.substring(baseUrl.lastIndexOf(".")));
                    mNotifyManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mBuilder = new NotificationCompat.Builder(MediaView.this);
                    mBuilder.setContentTitle("Saving " + baseUrl)
                            .setSmallIcon(R.drawable.save);
                    try {

                        final URL url = new URL(baseUrl); //wont exist on server yet, just load the full version
                        URLConnection ucon = url.openConnection();
                        ucon.setReadTimeout(5000);
                        ucon.setConnectTimeout(10000);
                        InputStream is = ucon.getInputStream();
                        BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 5);
                        int length = ucon.getContentLength();
                        f.createNewFile();
                        FileOutputStream outStream = new FileOutputStream(f);
                        byte[] buff = new byte[5 * 1024];

                        int len;
                        int last = 0;
                        while ((len = inStream.read(buff)) != -1) {
                            outStream.write(buff, 0, len);
                            int percent = Math.round(100.0f * f.length() / length);
                            if (percent > last) {
                                last = percent;
                                mBuilder.setProgress(length, (int) f.length(), false);
                                mNotifyManager.notify(1, mBuilder.build());
                            }
                        }
                        outStream.flush();
                        outStream.close();
                        inStream.close();
                        MediaScannerConnection.scanFile(MediaView.this, new String[]{f.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                                Intent mediaScanIntent = new Intent(
                                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                Uri contentUri = Uri.parse("file://" + f.getAbsolutePath());
                                mediaScanIntent.setData(contentUri);
                                MediaView.this.sendBroadcast(mediaScanIntent);

                                final Intent shareIntent = new Intent(Intent.ACTION_VIEW);
                                shareIntent.setDataAndType(contentUri, "image/gif");
                                PendingIntent contentIntent = PendingIntent.getActivity(MediaView.this, 0, shareIntent, PendingIntent.FLAG_CANCEL_CURRENT);


                                Notification notif = new NotificationCompat.Builder(MediaView.this)
                                        .setContentTitle(getString(R.string.gif_saved))
                                        .setSmallIcon(R.drawable.savecontent)
                                        .setContentIntent(contentIntent)
                                        .build();

                                NotificationManager mNotificationManager =
                                        (NotificationManager) getSystemService(Activity.NOTIFICATION_SERVICE);
                                mNotificationManager.notify(1, notif);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        }.execute();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((SubsamplingScaleImageView) findViewById(R.id.submission_image)).recycle();
        if (gif != null)
            gif.cancel(true);
        doOnClick = null;
        if (!didLoadGif && fileLoc != null && !fileLoc.isEmpty()) {
            new File(fileLoc).delete();
        }
    }

    int stopPosition;

    GifUtils.AsyncLoadGif gif;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (videoView != null) {
            stopPosition = videoView.getCurrentPosition();
            videoView.pause();
            outState.putInt("position", stopPosition);
        }
    }

    public void hideOnLongClick() {
        (findViewById(R.id.gifheader)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (findViewById(R.id.gifheader).getVisibility() == View.GONE) {
                    animateIn(findViewById(R.id.gifheader));
                    fadeOut(findViewById(R.id.black));
                    getWindow().getDecorView().setSystemUiVisibility(0);
                } else {
                    animateOut(findViewById(R.id.gifheader));
                    fadeIn(findViewById(R.id.black));
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                }
            }
        });
        findViewById(R.id.submission_image).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v2) {
                if (findViewById(R.id.gifheader).getVisibility() == View.GONE) {
                    animateIn(findViewById(R.id.gifheader));
                    fadeOut(findViewById(R.id.black));
                    getWindow().getDecorView().setSystemUiVisibility(0);
                } else {
                    MediaView.this.finish();
                }
            }
        });
    }

    public static void animateIn(View l) {
        l.setVisibility(View.VISIBLE);

        ValueAnimator mAnimator = slideAnimator(0, Reddit.dpToPxVertical(56), l);

        mAnimator.start();
    }

    public static void fadeIn(View l) {
        ValueAnimator mAnimator = fadeAnimator(0.66f, 1, l);
        mAnimator.start();
    }

    private static ValueAnimator fadeAnimator(float start, float end, final View v) {
        ValueAnimator animator = ValueAnimator.ofFloat(start, end);
        animator.setInterpolator(new FastOutSlowInInterpolator());

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //Update Height
                float value = (Float) valueAnimator.getAnimatedValue();
                v.setAlpha(value);
            }
        });
        return animator;
    }

    private static ValueAnimator slideAnimator(int start, int end, final View v) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.setInterpolator(new FastOutSlowInInterpolator());

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //Update Height
                int value = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
                layoutParams.height = value;
                v.setLayoutParams(layoutParams);
            }
        });
        return animator;
    }

    public static void animateOut(final View l) {

        ValueAnimator mAnimator = slideAnimator(Reddit.dpToPxVertical(36), 0, l);
        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                l.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mAnimator.start();


    }

    public static void fadeOut(final View l) {
        ValueAnimator mAnimator = fadeAnimator(1, .66f, l);
        mAnimator.start();
    }

    String contentUrl;

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

    public void onCreate(Bundle savedInstanceState) {
        overrideRedditSwipeAnywhere();
        super.onCreate(savedInstanceState);
        getTheme().applyStyle(new ColorPreferences(this).getDarkThemeSubreddit(""), true);

        if (savedInstanceState != null && savedInstanceState.containsKey("position"))
            stopPosition = savedInstanceState.getInt("position");

        doOnClick = new Runnable() {
            @Override
            public void run() {

            }
        };
        setContentView(R.layout.activity_media);

        //Keep the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        final String firstUrl = getIntent().getExtras().getString(EXTRA_DISPLAY_URL, "");
        contentUrl = getIntent().getExtras().getString(EXTRA_URL);

        setShareUrl(contentUrl);

        if (contentUrl.contains("reddituploads.com")) {
            contentUrl = Html.fromHtml(contentUrl).toString();
        }
        if (contentUrl != null && shouldTruncate(contentUrl)) {
            contentUrl = contentUrl.substring(0, contentUrl.lastIndexOf("."));
        }
        actuallyLoaded = contentUrl;
        if (getIntent().hasExtra(EXTRA_LQ)) {
            String lqUrl = getIntent().getStringExtra(EXTRA_DISPLAY_URL);
            displayImage(lqUrl);
            findViewById(R.id.hq).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imageShown = false;
                    doLoad(contentUrl);
                    findViewById(R.id.hq).setVisibility(View.GONE);
                }
            });
        } else if (ContentType.isImgurImage(contentUrl) && SettingValues.imgurLq && SettingValues.loadImageLq && (SettingValues.lowResAlways || (!NetworkUtil.isConnectedWifi(this) && SettingValues.lowResMobile))) {
            String url = contentUrl;
            url = url.substring(0, url.lastIndexOf(".")) + "m" + url.substring(url.lastIndexOf("."), url.length());

            displayImage(url);
            findViewById(R.id.hq).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imageShown = false;
                    doLoad(contentUrl);
                    findViewById(R.id.hq).setVisibility(View.GONE);
                }
            });
        } else {
            if (!firstUrl.isEmpty() && contentUrl != null && ContentType.displayImage(ContentType.getContentType(contentUrl))) {
                ((ProgressBar) findViewById(R.id.progress)).setIndeterminate(true);
                displayImage(firstUrl);
            } else if (firstUrl.isEmpty()) {
                imageShown = false;
                ((ProgressBar) findViewById(R.id.progress)).setIndeterminate(true);
            }
            findViewById(R.id.hq).setVisibility(View.GONE);
            doLoad(contentUrl);
        }

        if (!Reddit.appRestart.contains("tutorialSwipe")) {
            startActivityForResult(new Intent(this, SwipeTutorial.class), 3);
        }
        findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheetImage();
            }
        });
        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isGif) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            String url = contentUrl;
                            final String finalUrl1 = url;
                            final String finalUrl = contentUrl;
                            try {
                                ((Reddit) getApplication()).getImageLoader()
                                        .loadImage(finalUrl, new SimpleImageLoadingListener() {
                                            @Override
                                            public void onLoadingComplete(String imageUri, View view, final Bitmap loadedImage) {
                                                saveImageGallery(loadedImage, finalUrl1);
                                            }

                                        });

                            } catch (Exception e) {
                                Log.v(LogUtil.getTag(), "COULDN'T DOWNLOAD!");
                            }
                            return null;
                        }
                    }.execute();

                } else {
                    doOnClick.run();
                }
            }
        });

        hideOnLongClick();
    }

    public void doLoad(final String contentUrl) {
        switch (ContentType.getContentType(contentUrl)) {
            case DEVIANTART:
                if (!imageShown) {
                    Ion.with(this).load("http://backend.deviantart.com/oembed?url=" + contentUrl).asJsonObject().setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (result != null && !result.isJsonNull() && (result.has("fullsize_url") || result.has("url"))) {

                                String url;
                                if (result.has("fullsize_url")) {
                                    url = result.get("fullsize_url").getAsString();
                                } else {
                                    url = result.get("url").getAsString();
                                }
                                doLoadImage(url);
                            } else {
                                Intent i = new Intent(MediaView.this, Website.class);
                                i.putExtra(Website.EXTRA_URL, contentUrl);
                                MediaView.this.startActivity(i);
                            }
                        }
                    });
                }
                break;
            case IMAGE:
                doLoadImage(contentUrl);
                break;
            case IMGUR:
                doLoadImgur(contentUrl);
                break;
            case VID_ME:
            case STREAMABLE:
            case GIF:
                doLoadGif(contentUrl);
                break;
        }
    }

    MediaVideoView videoView;

    public static Runnable doOnClick;

    public void doLoadGif(final String dat) {
        isGif = true;
        findViewById(R.id.hq).setVisibility(View.GONE);
        videoView = (MediaVideoView) findViewById(R.id.gif);
        findViewById(R.id.black).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(findViewById(R.id.gifheader).getVisibility() == View.GONE){
                    animateIn(findViewById(R.id.gifheader));
                    fadeOut(findViewById(R.id.black));
                }
            }
        });
        videoView.clearFocus();
        videoView.setZOrderOnTop(true);
        findViewById(R.id.gifarea).setVisibility(View.VISIBLE);
        findViewById(R.id.submission_image).setVisibility(View.GONE);
        final ProgressBar loader = (ProgressBar) findViewById(R.id.gifprogress);
        findViewById(R.id.progress).setVisibility(View.GONE);
        gif = new GifUtils.AsyncLoadGif(this, (MediaVideoView) findViewById(R.id.gif), loader, findViewById(R.id.placeholder), doOnClick, true, false, true, ((TextView) findViewById(R.id.size)));
        gif.execute(dat);
        findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheetImage();
            }
        });
    }

    public void doLoadImgur(String url) {

        final String finalUrl = url;
        final String finalUrl1 = url;
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        String hash = url.substring(url.lastIndexOf("/"), url.length());

        if (NetworkUtil.isConnected(this)) {

            if (hash.startsWith("/"))
                hash = hash.substring(1, hash.length());
            LogUtil.v("Loading" + "https://imgur-apiv3.p.mashape.com/3/image/" + hash + ".json");
            Ion.with(this).load("https://imgur-apiv3.p.mashape.com/3/image/" + hash + ".json")
                    .addHeader("X-Mashape-Key", SecretConstants.getImgurApiKey(MediaView.this)).addHeader("Authorization", "Client-ID " + "bef87913eb202e9")
                    .asJsonObject().setCallback(new FutureCallback<JsonObject>() {
                                                    @Override
                                                    public void onCompleted(Exception e, JsonObject obj) {

                                                        if (obj != null && !obj.isJsonNull() && obj.has("error")) {
                                                            LogUtil.v("Error loading content");
                                                            (MediaView.this).finish();
                                                        } else {
                                                            try {
                                                                if (obj != null && !obj.isJsonNull() && obj.has("image")) {
                                                                    String type = obj.get("image").getAsJsonObject().get("image").getAsJsonObject().get("type").getAsString();
                                                                    String urls = obj.get("image").getAsJsonObject().get("links").getAsJsonObject().get("original").getAsString();

                                                                    if (type.contains("gif")) {
                                                                        doLoadGif(urls);
                                                                    } else if (!imageShown) { //only load if there is no image
                                                                        doLoadImage(urls);
                                                                    }
                                                                } else if (obj.has("data")) {
                                                                    String type = obj.get("data").getAsJsonObject().get("type").getAsString();
                                                                    String urls = obj.get("data").getAsJsonObject().get("link").getAsString();
                                                                    String mp4 = "";
                                                                    if (obj.get("data").getAsJsonObject().has("mp4")) {
                                                                        mp4 = obj.get("data").getAsJsonObject().get("mp4").getAsString();
                                                                    }

                                                                    if (type.contains("gif")) {
                                                                        doLoadGif(((mp4 == null || mp4.isEmpty()) ? urls : mp4));
                                                                    } else if (!imageShown) { //only load if there is no image
                                                                        doLoadImage(urls);
                                                                    }
                                                                } else {
                                                                    if (!imageShown)
                                                                        doLoadImage(finalUrl1);
                                                                }
                                                            } catch (Exception e2) {
                                                                e2.printStackTrace();
                                                                Intent i = new Intent(MediaView.this, Website.class);
                                                                i.putExtra(Website.EXTRA_URL, finalUrl);
                                                                MediaView.this.startActivity(i);
                                                            }
                                                        }
                                                    }
                                                }
            );
        }
    }

    public void doLoadImage(String contentUrl) {
        if (contentUrl != null && contentUrl.contains("bildgur.de"))
            contentUrl = contentUrl.replace("b.bildgur.de", "i.imgur.com");
        if (contentUrl != null && ContentType.isImgurLink(contentUrl)) {
            contentUrl = contentUrl + ".png";
        }

        findViewById(R.id.gifprogress).setVisibility(View.GONE);

        if (contentUrl != null && contentUrl.contains("m.imgur.com"))
            contentUrl = contentUrl.replace("m.imgur.com", "i.imgur.com");
        if (contentUrl == null) {
            finish();
            //todo maybe something better

        }

        if ((contentUrl != null && !contentUrl.startsWith("https://i.redditmedia.com") && !contentUrl.startsWith("https://i.reddituploads.com") && !contentUrl.contains("imgur.com"))) { //we can assume redditmedia and imgur links are to direct images and not websites
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
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!imageShown && type != null && !type.isEmpty() && type.startsWith("image/")) {
                                    //is image
                                    if (type.contains("gif")) {
                                        doLoadGif(finalUrl2.replace(".jpg", ".gif").replace(".png", ".gif"));
                                    } else if (!imageShown) {
                                        displayImage(finalUrl2);
                                    }
                                    actuallyLoaded = finalUrl2;
                                } else if (!imageShown) {
                                    Intent i = new Intent(MediaView.this, Website.class);
                                    i.putExtra(Website.EXTRA_URL, finalUrl2);
                                    MediaView.this.startActivity(i);
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


        actuallyLoaded = contentUrl;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 3) {
            Reddit.appRestart.edit().putBoolean("tutorialSwipe", true).apply();
        }
    }

    public void displayImage(final String url) {
        if (!imageShown) {
            actuallyLoaded = url;
            final SubsamplingScaleImageView i = (SubsamplingScaleImageView) findViewById(R.id.submission_image);

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

            ImageView fakeImage = new ImageView(MediaView.this);
            fakeImage.setLayoutParams(new LinearLayout.LayoutParams(i.getWidth(), i.getHeight()));
            fakeImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

            File f = ((Reddit) getApplicationContext()).getImageLoader().getDiscCache().get(url);
            if (f != null && f.exists()) {
                imageShown = true;

                try {
                    i.setImage(ImageSource.uri(f.getAbsolutePath()));
                } catch (Exception e) {
                    //todo  i.setImage(ImageSource.bitmap(loadedImage));
                }
                (findViewById(R.id.progress)).setVisibility(View.GONE);
                handler.removeCallbacks(progressBarDelayRunner);

                previous = i.scale;
                final float base = i.scale;
                i.setOnZoomChangedListener(new SubsamplingScaleImageView.OnZoomChangedListener() {
                    @Override
                    public void onZoomLevelChanged(float zoom) {
                        if (zoom > previous && !hidden && zoom > base) {
                            hidden = true;
                            final View base = findViewById(R.id.gifheader);

                            ValueAnimator va = ValueAnimator.ofFloat(1.0f, 0.2f);
                            int mDuration = 250; //in millis
                            va.setDuration(mDuration);
                            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                public void onAnimationUpdate(ValueAnimator animation) {
                                    Float value = (Float) animation.getAnimatedValue();
                                    base.setAlpha(value);
                                }
                            });
                            va.start();
                            //hide
                        } else if (zoom <= previous && hidden) {
                            hidden = false;
                            final View base = findViewById(R.id.gifheader);

                            ValueAnimator va = ValueAnimator.ofFloat(0.2f, 1.0f);
                            int mDuration = 250; //in millis
                            va.setDuration(mDuration);
                            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                public void onAnimationUpdate(ValueAnimator animation) {
                                    Float value = (Float) animation.getAnimatedValue();
                                    base.setAlpha(value);
                                }
                            });
                            va.start();
                            //unhide
                        }
                        previous = zoom;
                    }
                });
            } else {
                ((Reddit) getApplication()).getImageLoader()
                        .displayImage(url, new ImageViewAware(fakeImage), new DisplayImageOptions.Builder()
                                .resetViewBeforeLoading(true)
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
                            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                                Log.v(LogUtil.getTag(), "LOADING FAILED");

                            }

                            @Override
                            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                imageShown = true;
                                File f = ((Reddit) getApplicationContext()).getImageLoader().getDiscCache().get(url);
                                if (f != null && f.exists()) {
                                    i.setImage(ImageSource.uri(f.getAbsolutePath()));
                                } else {
                                    i.setImage(ImageSource.bitmap(loadedImage));
                                }
                                (findViewById(R.id.progress)).setVisibility(View.GONE);
                                handler.removeCallbacks(progressBarDelayRunner);

                                previous = i.scale;
                                final float base = i.scale;
                                i.setOnZoomChangedListener(new SubsamplingScaleImageView.OnZoomChangedListener() {
                                    @Override
                                    public void onZoomLevelChanged(float zoom) {
                                        if (zoom > previous && !hidden && zoom > base) {
                                            hidden = true;
                                            final View base = findViewById(R.id.gifheader);

                                            ValueAnimator va = ValueAnimator.ofFloat(1.0f, 0.2f);
                                            int mDuration = 250; //in millis
                                            va.setDuration(mDuration);
                                            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                                public void onAnimationUpdate(ValueAnimator animation) {
                                                    Float value = (Float) animation.getAnimatedValue();
                                                    base.setAlpha(value);
                                                }
                                            });
                                            va.start();
                                            //hide
                                        } else if (zoom <= previous && hidden) {
                                            hidden = false;
                                            final View base = findViewById(R.id.gifheader);

                                            ValueAnimator va = ValueAnimator.ofFloat(0.2f, 1.0f);
                                            int mDuration = 250; //in millis
                                            va.setDuration(mDuration);
                                            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                                public void onAnimationUpdate(ValueAnimator animation) {
                                                    Float value = (Float) animation.getAnimatedValue();
                                                    base.setAlpha(value);
                                                }
                                            });
                                            va.start();
                                            //unhide
                                        }
                                        previous = zoom;
                                    }
                                });
                            }

                            @Override
                            public void onLoadingCancelled(String imageUri, View view) {
                                Log.v(LogUtil.getTag(), "LOADING CANCELLED");

                            }
                        }, new ImageLoadingProgressListener() {
                            @Override
                            public void onProgressUpdate(String imageUri, View view, int current, int total) {
                                ((ProgressBar) findViewById(R.id.progress)).setProgress(Math.round(100.0f * current / total));
                            }
                        });
            }
        }
    }

    public void showFirstDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {

                    new AlertDialogWrapper.Builder(MediaView.this)
                            .setTitle(R.string.set_save_location)
                            .setMessage(R.string.set_save_location_msg)
                            .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new FolderChooserDialogCreate.Builder(MediaView.this)
                                            .chooseButton(R.string.btn_select)  // changes label of the choose button
                                            .initialPath(Environment.getExternalStorageDirectory().getPath())  // changes initial path, defaults to external storage directory
                                            .show();
                                }
                            })
                            .setNegativeButton(R.string.btn_no, null)
                            .show();
                } catch (Exception ignored) {

                }

            }
        });
    }

    public void showNotifPhoto(final File localAbsoluteFilePath, final Bitmap loadedImage) {
        MediaScannerConnection.scanFile(MediaView.this, new String[]{localAbsoluteFilePath.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
            public void onScanCompleted(String path, Uri uri) {

                final Intent shareIntent = new Intent(Intent.ACTION_VIEW);
                shareIntent.setDataAndType(Uri.fromFile(localAbsoluteFilePath), "image/*");
                PendingIntent contentIntent = PendingIntent.getActivity(MediaView.this, 0, shareIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                Notification notif = new NotificationCompat.Builder(MediaView.this)
                        .setContentTitle(getString(R.string.info_photo_saved))
                        .setSmallIcon(R.drawable.savecontent)
                        .setLargeIcon(loadedImage)
                        .setContentIntent(contentIntent)
                        .setStyle(new NotificationCompat.BigPictureStyle()
                                .bigPicture(loadedImage)).build();

                NotificationManager mNotificationManager =
                        (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
                mNotificationManager.notify(1, notif);
                loadedImage.recycle();
            }
        });
    }

    public void showErrorDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialogWrapper.Builder(MediaView.this)
                        .setTitle(R.string.err_something_wrong)
                        .setMessage(R.string.err_couldnt_save_choose_new)
                        .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new FolderChooserDialogCreate.Builder(MediaView.this)
                                        .chooseButton(R.string.btn_select)  // changes label of the choose button
                                        .initialPath(Environment.getExternalStorageDirectory().getPath())  // changes initial path, defaults to external storage directory
                                        .show();
                            }
                        })
                        .setNegativeButton(R.string.btn_no, null)
                        .show();
            }
        });

    }

    private void shareImage(String finalUrl) {
        ((Reddit) getApplication()).getImageLoader()
                .loadImage(finalUrl, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        shareImage(loadedImage);
                    }
                });
    }

    private void saveImageGallery(final Bitmap bitmap, String URL) {
        if (Reddit.appRestart.getString("imagelocation", "").isEmpty()) {
            showFirstDialog();
        } else if (!new File(Reddit.appRestart.getString("imagelocation", "")).exists()) {
            showErrorDialog();
        } else {
            File f = new File(Reddit.appRestart.getString("imagelocation", "") + File.separator + UUID.randomUUID().toString() + ".png");

            FileOutputStream out = null;
            try {
                f.createNewFile();
                out = new FileOutputStream(f);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (Exception e) {
                e.printStackTrace();
                showErrorDialog();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                        showNotifPhoto(f, bitmap);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    showErrorDialog();
                }
            }
        }
    }

    private void shareImage(final Bitmap bitmap) {
        if (Reddit.appRestart.getString("imagelocation", "").isEmpty()) {
            showFirstDialog();
        } else if (!new File(Reddit.appRestart.getString("imagelocation", "")).exists()) {
            showErrorDialog();
        } else {
            File f = new File(Reddit.appRestart.getString("imagelocation", "") + File.separator + UUID.randomUUID().toString() + ".png");

            FileOutputStream out = null;
            try {
                f.createNewFile();
                out = new FileOutputStream(f);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (Exception e) {
                e.printStackTrace();
                showErrorDialog();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                        if (!f.getAbsolutePath().isEmpty()) {
                            Intent mediaScanIntent = new Intent(
                                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            Uri contentUri = Uri.parse("file://" + f.getAbsolutePath());
                            mediaScanIntent.setData(contentUri);
                            MediaView.this.sendBroadcast(mediaScanIntent);

                            Uri bmpUri = Uri.fromFile(f);
                            final Intent shareImageIntent = new Intent(Intent.ACTION_SEND);
                            shareImageIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                            shareImageIntent.setType("image/png");
                            startActivity(Intent.createChooser(shareImageIntent, getString(R.string.misc_img_share)));
                        } else {
                            showErrorDialog();
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    showErrorDialog();
                }
            }
        }
    }

    @Override
    public void onFolderSelection(FolderChooserDialogCreate dialog, File folder) {
        if (folder != null) {
            Reddit.appRestart.edit().putString("imagelocation", folder.getAbsolutePath().toString()).apply();
            Toast.makeText(this, "Images will be saved to " + folder.getAbsolutePath(), Toast.LENGTH_LONG).show();
        }
    }
}
