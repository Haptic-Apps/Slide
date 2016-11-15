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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
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
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.apache.commons.lang3.StringEscapeUtils;

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
import me.ccrama.redditslide.Fragments.SubmissionsView;
import me.ccrama.redditslide.Notifications.ImageDownloadNotificationService;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SecretConstants;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Views.ImageSource;
import me.ccrama.redditslide.Views.MediaVideoView;
import me.ccrama.redditslide.Views.SubsamplingScaleImageView;
import me.ccrama.redditslide.util.GifUtils;
import me.ccrama.redditslide.util.HttpUtil;
import me.ccrama.redditslide.util.LinkUtil;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.NetworkUtil;

import static me.ccrama.redditslide.Activities.AlbumPager.readableFileSize;


/**
 * Created by ccrama on 3/5/2015.
 */
public class MediaView extends FullScreenActivity
        implements FolderChooserDialogCreate.FolderCallback {
    public static final String EXTRA_URL         = "url";
    public static final String ADAPTER_POSITION  = "adapter_position";
    public static final String SUBMISSION_URL    = "submission";
    public static final String EXTRA_DISPLAY_URL = "displayUrl";
    public static final String EXTRA_LQ          = "lq";
    public static final String EXTRA_SHARE_URL   = "urlShare";

    public static String   fileLoc;
    public static Runnable doOnClick;
    public static boolean  didLoadGif;

    public float   previous;
    public boolean hidden;
    public boolean imageShown;
    public String  actuallyLoaded;
    public boolean isGif;

    private NotificationManager        mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private int                        stopPosition;
    private GifUtils.AsyncLoadGif      gif;
    private String                     contentUrl;
    private MediaVideoView             videoView;
    private Gson                       gson;
    private String                     mashapeKey;

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

    public static boolean shouldTruncate(String url) {
        try {
            final URI uri = new URI(url);
            final String path = uri.getPath();

            return !ContentType.isGif(uri) && !ContentType.isImage(uri) && path.contains(".");
        } catch (URISyntaxException e) {
            return false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (videoView != null) {
            videoView.seekTo(stopPosition);
            videoView.start();
        }
    }

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

        BottomSheet.Builder b = new BottomSheet.Builder(this).title(contentUrl);

        b.sheet(2, external, getString(R.string.submission_link_extern));
        b.sheet(5, share, getString(R.string.submission_link_share));

        if (!isGif) b.sheet(3, image, getString(R.string.share_image));
        b.sheet(4, save, "Save " + (isGif ? "MP4" : "image"));
        if (isGif
                && !contentUrl.contains(".mp4")
                && !contentUrl.contains("streamable.com")
                && !contentUrl.contains("gfycat.com")
                && !contentUrl.contains("vid.me")) {
            String type = contentUrl.substring(contentUrl.lastIndexOf(".") + 1, contentUrl.length())
                    .toUpperCase();
            try {
                if (type.equals("GIFV") && new URL(contentUrl).getHost().equals("i.imgur.com")) {
                    type = "GIF";
                    contentUrl = contentUrl.replace(".gifv", ".gif");
                  //todo possibly share gifs  b.sheet(9, share, "Share GIF");
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            b.sheet(6, file, getString(R.string.mediaview_save, type));
        }
        b.listener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case (2): {
                        LinkUtil.openExternally(StringEscapeUtils.unescapeHtml4(contentUrl),
                                MediaView.this, true);
                        break;
                    }
                    case (3): {
                        shareImage(actuallyLoaded);
                        break;
                    }
                    case (5): {
                        Reddit.defaultShareText("", StringEscapeUtils.unescapeHtml4(contentUrl),
                                MediaView.this);
                        break;
                    }
                    case (6): {
                        saveFile(contentUrl);
                    }
                    break;
                    case (9): {
                        shareGif(contentUrl);
                    }
                    break;
                    case (4): {
                        doImageSave();
                        break;
                    }
                }
            }
        });
        b.show();
    }

    public void doImageSave() {
        if (!isGif) {
            if (Reddit.appRestart.getString("imagelocation", "").isEmpty()) {
                showFirstDialog();
            } else if (!new File(Reddit.appRestart.getString("imagelocation", "")).exists()) {
                showErrorDialog();
            } else {
                Intent i = new Intent(this, ImageDownloadNotificationService.class);
                //always download the original file, or use the cached original if that is currently displayed
                i.putExtra("actuallyLoaded", contentUrl);
                startService(i);
            }
        } else {
            doOnClick.run();
        }
    }

    public void saveFile(final String baseUrl) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if (Reddit.appRestart.getString("imagelocation", "").isEmpty()) {
                    showFirstDialog();
                } else if (!new File(Reddit.appRestart.getString("imagelocation", "")).exists()) {
                    showErrorDialog();
                } else {
                    final File f = new File(
                            Reddit.appRestart.getString("imagelocation", "") + File.separator + UUID
                                    .randomUUID()
                                    .toString() + baseUrl.substring(baseUrl.lastIndexOf(".")));
                    mNotifyManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mBuilder = new NotificationCompat.Builder(MediaView.this);
                    mBuilder.setContentTitle(getString(R.string.mediaview_saving, baseUrl))
                            .setSmallIcon(R.drawable.save);
                    try {

                        final URL url =
                                new URL(baseUrl); //wont exist on server yet, just load the full version
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
                        MediaScannerConnection.scanFile(MediaView.this,
                                new String[]{f.getAbsolutePath()}, null,
                                new MediaScannerConnection.OnScanCompletedListener() {
                                    public void onScanCompleted(String path, Uri uri) {
                                        Intent mediaScanIntent =
                                                new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                        Uri contentUri = Uri.parse("file://" + f.getAbsolutePath());
                                        mediaScanIntent.setData(contentUri);
                                        MediaView.this.sendBroadcast(mediaScanIntent);

                                        final Intent shareIntent = new Intent(Intent.ACTION_VIEW);
                                        shareIntent.setDataAndType(contentUri, "image/gif");
                                        PendingIntent contentIntent =
                                                PendingIntent.getActivity(MediaView.this, 0,
                                                        shareIntent,
                                                        PendingIntent.FLAG_CANCEL_CURRENT);


                                        Notification notif = new NotificationCompat.Builder(
                                                MediaView.this).setContentTitle(
                                                getString(R.string.gif_saved))
                                                .setSmallIcon(R.drawable.savecontent)
                                                .setContentIntent(contentIntent)
                                                .build();

                                        NotificationManager mNotificationManager =
                                                (NotificationManager) getSystemService(
                                                        Activity.NOTIFICATION_SERVICE);
                                        mNotificationManager.notify(1, notif);
                                    }
                                });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    public void shareGif(final String baseUrl) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if (Reddit.appRestart.getString("imagelocation", "").isEmpty()) {
                    showFirstDialog();
                } else if (!new File(Reddit.appRestart.getString("imagelocation", "")).exists()) {
                    showErrorDialog();
                } else {
                    final File f = new File(
                            Reddit.appRestart.getString("imagelocation", "") + File.separator + UUID
                                    .randomUUID()
                                    .toString() + baseUrl.substring(baseUrl.lastIndexOf(".")));
                    mNotifyManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mBuilder = new NotificationCompat.Builder(MediaView.this);
                    mBuilder.setContentTitle(getString(R.string.mediaview_saving, baseUrl))
                            .setSmallIcon(R.drawable.save);
                    try {

                        final URL url =
                                new URL(baseUrl); //wont exist on server yet, just load the full version
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
                        MediaScannerConnection.scanFile(MediaView.this,
                                new String[]{f.getAbsolutePath()}, null,
                                new MediaScannerConnection.OnScanCompletedListener() {
                                    public void onScanCompleted(String path, Uri uri) {
                                        Intent mediaScanIntent =
                                                new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                        Uri contentUri = Uri.parse("file://" + f.getAbsolutePath());
                                        mediaScanIntent.setData(contentUri);
                                        MediaView.this.sendBroadcast(mediaScanIntent);

                                        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                        shareIntent.setDataAndType(contentUri, "image/gif");
                                        startActivity(Intent.createChooser(shareIntent, "Share GIF"));
                                        NotificationManager mNotificationManager =
                                                (NotificationManager) getSystemService(
                                                        Activity.NOTIFICATION_SERVICE);
                                        mNotificationManager.cancel(1);
                                    }
                                });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        ((SubsamplingScaleImageView) findViewById(R.id.submission_image)).recycle();
        if (gif != null) gif.cancel(true);
        doOnClick = null;
        if (!didLoadGif && fileLoc != null && !fileLoc.isEmpty()) {
            new File(fileLoc).delete();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (videoView != null) {
            stopPosition = videoView.getCurrentPosition();
            videoView.pause();
            outState.putInt("position", stopPosition);
        }
    }

    /* Possible drag to exit implementation in the future
     @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        if(event.getF) {
            // peekView.doScroll(event);

            FrameLayout.LayoutParams params =
                    (FrameLayout.LayoutParams) base.getLayoutParams();

            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:

                    params.topMargin = (int) -((origY - event.getY()));
                    if (event.getY() != origY) {
                        params.leftMargin = twelve *2;
                        params.rightMargin = twelve * 2;
                    } else {
                        params.leftMargin = 0;
                        params.rightMargin = 0;
                    }

                    if (event.getY() != (origY)) {
                        shouldClose = true;
                    } else if (event.getY() == (origY)) {
                        shouldClose = false;
                    }
                    base.setLayoutParams(params);
                    break;
                case MotionEvent.ACTION_DOWN:
                    origY = event.getY();
                    break;
            }
        }
            // we don't want to pass along the touch event or else it will just scroll under the PeekView}

        if (event.getAction() == MotionEvent.ACTION_UP && shouldClose) {
            finish();
            return false;
        }

        return super.dispatchTouchEvent(event);
    }
     */

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
                    getWindow().getDecorView()
                            .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
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
                    finish();
                }
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        overrideRedditSwipeAnywhere();
        super.onCreate(savedInstanceState);
        getTheme().applyStyle(new ColorPreferences(this).getDarkThemeSubreddit(""), true);

        gson = new Gson();
        mashapeKey = SecretConstants.getImgurApiKey(this);

        if (savedInstanceState != null && savedInstanceState.containsKey("position")) {
            stopPosition = savedInstanceState.getInt("position");
        }

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
        if (getIntent().hasExtra(SUBMISSION_URL)) {
            final int commentUrl = getIntent().getExtras().getInt(ADAPTER_POSITION);
            findViewById(R.id.comments).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                    SubmissionsView.datachanged(commentUrl);
                }
            });
        } else {
            findViewById(R.id.comments).setVisibility(View.GONE);
        }

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
        } else if (ContentType.isImgurImage(contentUrl)
                && SettingValues.imgurLq
                && SettingValues.loadImageLq
                && (SettingValues.lowResAlways || (!NetworkUtil.isConnectedWifi(this)
                && SettingValues.lowResMobile))) {
            String url = contentUrl;
            url = url.substring(0, url.lastIndexOf(".")) + "m" + url.substring(url.lastIndexOf("."),
                    url.length());

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
            if (!firstUrl.isEmpty() && contentUrl != null && ContentType.displayImage(
                    ContentType.getContentType(contentUrl))) {
                ((ProgressBar) findViewById(R.id.progress)).setIndeterminate(true);
                if (ContentType.isImgurHash(firstUrl)) {
                    displayImage(firstUrl + ".png");
                } else {
                    displayImage(firstUrl);
                }
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
                doImageSave();
            }
        });

        hideOnLongClick();
    }

    public void doLoad(final String contentUrl) {
        switch (ContentType.getContentType(contentUrl)) {
            case DEVIANTART:
                doLoadDeviantArt(contentUrl);
                break;
            case IMAGE:
                doLoadImage(contentUrl);
                break;
            case IMGUR:
                doLoadImgur(contentUrl);
                break;
            case XKCD:
                doLoadXKCD(contentUrl);
                break;
            case VID_ME:
            case STREAMABLE:
            case GIF:
                doLoadGif(contentUrl);
                break;
        }
    }

    public void doLoadGif(final String dat) {
        isGif = true;
        findViewById(R.id.hq).setVisibility(View.GONE);
        videoView = (MediaVideoView) findViewById(R.id.gif);
        findViewById(R.id.black).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (findViewById(R.id.gifheader).getVisibility() == View.GONE) {
                    animateIn(findViewById(R.id.gifheader));
                    fadeOut(findViewById(R.id.black));
                }
            }
        });
        videoView.clearFocus();
        findViewById(R.id.gifarea).setVisibility(View.VISIBLE);
        findViewById(R.id.submission_image).setVisibility(View.GONE);
        final ProgressBar loader = (ProgressBar) findViewById(R.id.gifprogress);
        findViewById(R.id.progress).setVisibility(View.GONE);
        gif = new GifUtils.AsyncLoadGif(this, (MediaVideoView) findViewById(R.id.gif), loader,
                findViewById(R.id.placeholder), doOnClick, true, false, true,
                ((TextView) findViewById(R.id.size)));
        gif.execute(dat);
        findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheetImage();
            }
        });
    }

    public void doLoadImgur(String url) {
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        final String finalUrl = url;
        String hash = url.substring(url.lastIndexOf("/"), url.length());

        if (NetworkUtil.isConnected(this)) {
            if (hash.startsWith("/")) hash = hash.substring(1, hash.length());
            final String apiUrl = "https://imgur-apiv3.p.mashape.com/3/image/" + hash + ".json";
            LogUtil.v(apiUrl);

            new AsyncTask<Void, Void, JsonObject>() {
                @Override
                protected JsonObject doInBackground(Void... params) {
                    return HttpUtil.getImgurMashapeJsonObject(Reddit.client, gson, apiUrl,
                            mashapeKey);
                }

                @Override
                protected void onPostExecute(JsonObject result) {
                    if (result != null && !result.isJsonNull() && result.has("error")) {
                        LogUtil.v("Error loading content");
                        (MediaView.this).finish();
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
                            e2.printStackTrace();
                            Intent i = new Intent(MediaView.this, Website.class);
                            i.putExtra(Website.EXTRA_URL, finalUrl);
                            MediaView.this.startActivity(i);
                            finish();
                        }
                    }

                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void doLoadXKCD(String url) {
        if (!url.endsWith("/")) {
            url = url + "/";
        }

        if (NetworkUtil.isConnected(this)) {
            final String apiUrl = url + "info.0.json";
            LogUtil.v(apiUrl);

            final String finalUrl = url;
            new AsyncTask<Void, Void, JsonObject>() {
                @Override
                protected JsonObject doInBackground(Void... params) {
                    return HttpUtil.getJsonObject(Reddit.client, gson, apiUrl);
                }

                @Override
                protected void onPostExecute(final JsonObject result) {
                    if (result != null && !result.isJsonNull() && result.has("error")) {
                        LogUtil.v("Error loading content");
                        (MediaView.this).finish();
                    } else {
                        try {
                            if (result != null && !result.isJsonNull() && result.has("img")) {
                                doLoadImage(result.get("img").getAsString());
                                findViewById(R.id.submission_image).setOnLongClickListener(
                                        new View.OnLongClickListener() {
                                            @Override
                                            public boolean onLongClick(View v) {
                                                try {
                                                    new AlertDialogWrapper.Builder(
                                                            MediaView.this).setTitle(
                                                            result.get("safe_title").getAsString())
                                                            .setMessage(
                                                                    result.get("alt").getAsString())
                                                            .show();
                                                } catch (Exception ignored) {

                                                }
                                                return true;
                                            }
                                        });
                            } else {
                                Intent i = new Intent(MediaView.this, Website.class);
                                i.putExtra(Website.EXTRA_URL, finalUrl);
                                MediaView.this.startActivity(i);
                                finish();
                            }
                        } catch (Exception e2) {
                            e2.printStackTrace();
                            Intent i = new Intent(MediaView.this, Website.class);
                            i.putExtra(Website.EXTRA_URL, finalUrl);
                            MediaView.this.startActivity(i);
                            finish();
                        }
                    }

                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void doLoadDeviantArt(String url) {
        final String apiUrl = "http://backend.deviantart.com/oembed?url=" + url;
        LogUtil.v(apiUrl);
        new AsyncTask<Void, Void, JsonObject>() {
            @Override
            protected JsonObject doInBackground(Void... params) {
                return HttpUtil.getJsonObject(Reddit.client, gson, apiUrl);
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
                    Intent i = new Intent(MediaView.this, Website.class);
                    i.putExtra(Website.EXTRA_URL, contentUrl);
                    MediaView.this.startActivity(i);
                    finish();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

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
            finish();
            //todo maybe something better

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
                        runOnUiThread(new Runnable() {
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
                                    Intent i = new Intent(MediaView.this, Website.class);
                                    i.putExtra(Website.EXTRA_URL, finalUrl2);
                                    MediaView.this.startActivity(i);
                                    finish();
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
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

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

            ImageView fakeImage = new ImageView(MediaView.this);
            fakeImage.setLayoutParams(new LinearLayout.LayoutParams(i.getWidth(), i.getHeight()));
            fakeImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

            File f = ((Reddit) getApplicationContext()).getImageLoader().getDiscCache().get(url);
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
                } catch (Exception e) {
                    imageShown = false;
                    //todo  i.setImage(ImageSource.bitmap(loadedImage));
                }
                (findViewById(R.id.progress)).setVisibility(View.GONE);
                handler.removeCallbacks(progressBarDelayRunner);

                previous = i.scale;
                final float base = i.scale;
                i.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        i.setOnZoomChangedListener(
                                new SubsamplingScaleImageView.OnZoomChangedListener() {
                                    @Override
                                    public void onZoomLevelChanged(float zoom) {
                                        if (zoom > previous && !hidden && zoom > base) {
                                            hidden = true;
                                            final View base = findViewById(R.id.gifheader);

                                            ValueAnimator va = ValueAnimator.ofFloat(1.0f, 0.2f);
                                            int mDuration = 250; //in millis
                                            va.setDuration(mDuration);
                                            va.addUpdateListener(
                                                    new ValueAnimator.AnimatorUpdateListener() {
                                                        public void onAnimationUpdate(
                                                                ValueAnimator animation) {
                                                            Float value =
                                                                    (Float) animation.getAnimatedValue();
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
                                            va.addUpdateListener(
                                                    new ValueAnimator.AnimatorUpdateListener() {
                                                        public void onAnimationUpdate(
                                                                ValueAnimator animation) {
                                                            Float value =
                                                                    (Float) animation.getAnimatedValue();
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
                }, 2000);

            } else {
                final TextView size = (TextView) findViewById(R.id.size);

                ((Reddit) getApplication()).getImageLoader()
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
                                        size.setVisibility(View.VISIBLE);
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
                                        size.setVisibility(View.GONE);

                                        File f = ((Reddit) getApplicationContext()).getImageLoader()
                                                .getDiscCache()
                                                .get(url);
                                        if (f != null && f.exists()) {
                                            i.setImage(ImageSource.uri(f.getAbsolutePath()));
                                        } else {
                                            i.setImage(ImageSource.bitmap(loadedImage));
                                        }
                                        (findViewById(R.id.progress)).setVisibility(View.GONE);
                                        handler.removeCallbacks(progressBarDelayRunner);

                                        previous = i.scale;
                                        final float base = i.scale;
                                        i.setOnZoomChangedListener(
                                                new SubsamplingScaleImageView.OnZoomChangedListener() {
                                                    @Override
                                                    public void onZoomLevelChanged(float zoom) {
                                                        if (zoom > previous
                                                                && !hidden
                                                                && zoom > base) {
                                                            hidden = true;
                                                            final View base =
                                                                    findViewById(R.id.gifheader);

                                                            ValueAnimator va =
                                                                    ValueAnimator.ofFloat(1.0f,
                                                                            0.2f);
                                                            int mDuration = 250; //in millis
                                                            va.setDuration(mDuration);
                                                            va.addUpdateListener(
                                                                    new ValueAnimator.AnimatorUpdateListener() {
                                                                        public void onAnimationUpdate(
                                                                                ValueAnimator animation) {
                                                                            Float value =
                                                                                    (Float) animation
                                                                                            .getAnimatedValue();
                                                                            base.setAlpha(value);
                                                                        }
                                                                    });
                                                            va.start();
                                                            //hide
                                                        } else if (zoom <= previous && hidden) {
                                                            hidden = false;
                                                            final View base =
                                                                    findViewById(R.id.gifheader);

                                                            ValueAnimator va =
                                                                    ValueAnimator.ofFloat(0.2f,
                                                                            1.0f);
                                                            int mDuration = 250; //in millis
                                                            va.setDuration(mDuration);
                                                            va.addUpdateListener(
                                                                    new ValueAnimator.AnimatorUpdateListener() {
                                                                        public void onAnimationUpdate(
                                                                                ValueAnimator animation) {
                                                                            Float value =
                                                                                    (Float) animation
                                                                                            .getAnimatedValue();
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
                                    public void onProgressUpdate(String imageUri, View view,
                                            int current, int total) {
                                        size.setText(readableFileSize(total));

                                        ((ProgressBar) findViewById(R.id.progress)).setProgress(
                                                Math.round(100.0f * current / total));
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

                    new AlertDialogWrapper.Builder(MediaView.this).setTitle(
                            R.string.set_save_location)
                            .setMessage(R.string.set_save_location_msg)
                            .setPositiveButton(R.string.btn_yes,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            new FolderChooserDialogCreate.Builder(
                                                    MediaView.this).chooseButton(
                                                    R.string.btn_select)  // changes label of the choose button
                                                    .initialPath(
                                                            Environment.getExternalStorageDirectory()
                                                                    .getPath())  // changes initial path, defaults to external storage directory
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

    public void showErrorDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialogWrapper.Builder(MediaView.this).setTitle(
                        R.string.err_something_wrong)
                        .setMessage(R.string.err_couldnt_save_choose_new)
                        .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new FolderChooserDialogCreate.Builder(MediaView.this).chooseButton(
                                        R.string.btn_select)  // changes label of the choose button
                                        .initialPath(Environment.getExternalStorageDirectory()
                                                .getPath())  // changes initial path, defaults to external storage directory
                                        .show();
                            }
                        })
                        .setNegativeButton(R.string.btn_no, null)
                        .show();
            }
        });
    }

    private void shareImage(final String finalUrl) {
        ((Reddit) getApplication()).getImageLoader()
                .loadImage(finalUrl, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        shareImage(loadedImage, finalUrl);
                    }
                });
    }

    /**
     * Converts an image to a PNG, stores it to the cache, then shares it. Saves the image to
     * /cache/shared_image for easy deletion. If the /cache/shared_image folder already exists, we
     * clear it's contents as to avoid increasing the cache size unnecessarily.
     *
     * @param bitmap image to share
     */
    private void shareImage(final Bitmap bitmap, String original) {
        File image; //image to share

        //check to see if the cache/shared_images directory is present
        final File imagesDir =
                new File(this.getCacheDir().toString() + File.separator + "shared_image");
        if (!imagesDir.exists()) {
            imagesDir.mkdir(); //create the folder if it doesn't exist
        } else {
            deleteFilesInDir(imagesDir);
        }

        try {
            //creates a file in the cache; filename will be prefixed with "img" and end with ".png"
            image = File.createTempFile("img", ".png", imagesDir);

            File f = ((Reddit) getApplicationContext()).getImageLoader()
                    .getDiscCache()
                    .get(original);
            if (f != null) {
                try {
                    Files.copy(f, image);
                    shareFile(image);
                } catch (IOException e) {
                    doAlternativeImageSave(image, bitmap, original);
                    shareFile(image);
                    e.printStackTrace();
                }
            } else {
                doAlternativeImageSave(image, bitmap, original);
                shareFile(image);
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.err_share_image), Toast.LENGTH_LONG).show();
        }
    }

    public void shareFile(File image) {
        final String authority =
                (this.getPackageName()).concat(".").concat(MediaView.class.getSimpleName());

        final Uri contentUri = FileProvider.getUriForFile(this, authority, image);

        if (contentUri != null) {
            final Intent shareImageIntent = new Intent(Intent.ACTION_SEND);
            shareImageIntent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION); //temp permission for receiving app to read this file
            shareImageIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareImageIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));

            //Select a share option
            startActivity(
                    Intent.createChooser(shareImageIntent, getString(R.string.misc_img_share)));
        } else {
            Toast.makeText(this, getString(R.string.err_share_image), Toast.LENGTH_LONG).show();
        }
    }

    public void doAlternativeImageSave(File image, Bitmap bitmap, String original)
            throws IOException {
        FileOutputStream out = null;

        try {
            //convert image to png
            out = new FileOutputStream(image);
            bitmap.compress(original.endsWith("png") ? Bitmap.CompressFormat.PNG
                    : Bitmap.CompressFormat.JPEG, 100, out);
        } finally {
            if (out != null) {
                out.close();

                /**
                 * If a user has both a debug build and a release build installed, the authority name needs to be unique
                 */
                final String authority =
                        (this.getPackageName()).concat(".").concat(MediaView.class.getSimpleName());

                final Uri contentUri = FileProvider.getUriForFile(this, authority, image);

                if (contentUri != null) {
                    final Intent shareImageIntent = new Intent(Intent.ACTION_SEND);
                    shareImageIntent.addFlags(
                            Intent.FLAG_GRANT_READ_URI_PERMISSION); //temp permission for receiving app to read this file
                    shareImageIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                    shareImageIntent.setDataAndType(contentUri,
                            getContentResolver().getType(contentUri));

                    //Select a share option
                    startActivity(Intent.createChooser(shareImageIntent,
                            getString(R.string.misc_img_share)));
                } else {
                    Toast.makeText(this, getString(R.string.err_share_image), Toast.LENGTH_LONG)
                            .show();
                }
            }
        }
    }

    /**
     * Deletes all files in a folder
     *
     * @param dir to clear contents
     */
    private void deleteFilesInDir(File dir) {
        for (File child : dir.listFiles()) {
            child.delete();
        }
    }

    @Override
    public void onFolderSelection(FolderChooserDialogCreate dialog, File folder) {
        if (folder != null) {
            Reddit.appRestart.edit().putString("imagelocation", folder.getAbsolutePath()).apply();
            Toast.makeText(this,
                    getString(R.string.settings_set_image_location, folder.getAbsolutePath()),
                    Toast.LENGTH_LONG).show();
        }
    }
}
