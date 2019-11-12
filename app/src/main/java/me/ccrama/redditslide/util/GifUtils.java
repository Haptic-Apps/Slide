package me.ccrama.redditslide.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.coremedia.iso.boxes.Container;
import com.danikula.videocache.CacheListener;
import com.danikula.videocache.HttpProxyCacheServer;
import com.devbrackets.android.exomedia.listener.OnPreparedListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.text.DecimalFormat;
import java.util.UUID;

import me.ccrama.redditslide.Activities.MediaView;
import me.ccrama.redditslide.Activities.Website;
import me.ccrama.redditslide.Fragments.FolderChooserDialogCreate;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Views.MediaVideoView;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by carlo_000 on 1/29/2016.
 */
public class GifUtils {

    private GifUtils() {
    }

    public static String getSmallerGfy(String gfyUrl) {
        gfyUrl = gfyUrl.replaceAll("fat|zippy|giant", "thumbs");
        if (!gfyUrl.endsWith("-mobile.mp4")) gfyUrl = gfyUrl.replaceAll("\\.mp4", "-mobile.mp4");
        return gfyUrl;
    }

    public static void doNotifGif(File f, Activity c) {
        Intent mediaScanIntent =
                FileUtil.getFileIntent(f, new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE), c);
        c.sendBroadcast(mediaScanIntent);


        final Intent shareIntent = FileUtil.getFileIntent(f, new Intent(Intent.ACTION_VIEW), c);
        PendingIntent contentIntent =
                PendingIntent.getActivity(c, 0, shareIntent, PendingIntent.FLAG_CANCEL_CURRENT);


        Notification notif =
                new NotificationCompat.Builder(c).setContentTitle(c.getString(R.string.gif_saved))
                        .setSmallIcon(R.drawable.save_png)
                        .setContentIntent(contentIntent)
                        .setChannelId(Reddit.CHANNEL_IMG)
                        .build();

        NotificationManager mNotificationManager =
                (NotificationManager) c.getSystemService(Activity.NOTIFICATION_SERVICE);
        mNotificationManager.notify((int) System.currentTimeMillis(), notif);
    }

    public static void showErrorDialog(final Activity a) {
        new AlertDialogWrapper.Builder(a).setTitle(R.string.err_something_wrong)
                .setMessage(R.string.err_couldnt_save_choose_new)
                .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new FolderChooserDialogCreate.Builder((MediaView) a).chooseButton(
                                R.string.btn_select)  // changes label of the choose button
                                .initialPath(Environment.getExternalStorageDirectory()
                                        .getPath())  // changes initial path, defaults to external storage directory
                                .show();
                    }
                })
                .setNegativeButton(R.string.btn_no, null)
                .show();
    }

    public static void showFirstDialog(final Activity a) {
        new AlertDialogWrapper.Builder(a).setTitle(R.string.set_gif_save_loc)
                .setMessage(R.string.set_gif_save_loc_msg)
                .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new FolderChooserDialogCreate.Builder((MediaView) a).chooseButton(
                                R.string.btn_select)  // changes label of the choose button
                                .initialPath(Environment.getExternalStorageDirectory()
                                        .getPath())  // changes initial path, defaults to external storage directory
                                .show();
                    }
                })
                .setNegativeButton(R.string.btn_no, null)
                .show();
    }

    public static void saveGif(File from, Activity a, String subreddit) {

        LogUtil.v(from.getAbsolutePath());
        try {
            Toast.makeText(a, a.getString(R.string.mediaview_notif_title), Toast.LENGTH_SHORT)
                    .show();
        } catch (Exception ignored) {

        }
        if (Reddit.appRestart.getString("imagelocation", "").isEmpty()) {
            showFirstDialog(a);
        } else if (!new File(Reddit.appRestart.getString("imagelocation", "")).exists()) {
            showErrorDialog(a);
        } else {
            if (SettingValues.imageSubfolders && !subreddit.isEmpty()) {
                File directory = new File(Reddit.appRestart.getString("imagelocation", "") + (
                        SettingValues.imageSubfolders && !subreddit.isEmpty() ? File.separator
                                + subreddit : ""));
                directory.mkdirs();
            }
            File f = new File(Reddit.appRestart.getString("imagelocation", "")
                    + (SettingValues.imageSubfolders && !subreddit.isEmpty() ? File.separator
                    + subreddit : "")
                    + File.separator
                    + UUID.randomUUID().toString()
                    + ".mp4");

            FileOutputStream out = null;
            InputStream in = null;
            try {
                in = new FileInputStream(from);
                out = new FileOutputStream(f);


                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.e("Error saving GIF called with: "
                        + "from = ["
                        + from
                        + "], in = ["
                        + in
                        + "]");
                showErrorDialog(a);
            } finally {
                try {
                    if (out != null) {
                        out.close();
                        doNotifGif(f, a);
                    }
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    LogUtil.e("Error closing GIF called with: "
                            + "from = ["
                            + from
                            + "], out = ["
                            + out
                            + "]");
                    showErrorDialog(a);
                }
            }
        }
    }

    public static class AsyncLoadGif extends AsyncTask<String, Void, Void> {

        private Activity       c;
        private MediaVideoView video;
        private ProgressBar    progressBar;
        private View           placeholder;
        private View           gifSave;
        private boolean        closeIfNull;
        private boolean        hideControls;
        private boolean        autostart;
        private Runnable       doOnClick;
        private View           mute;
        public String subreddit = "";
        private boolean cacheOnly;

        private TextView size;

        public AsyncLoadGif(@NotNull Activity c, @NotNull MediaVideoView video,
                @Nullable ProgressBar p, @Nullable View placeholder, @Nullable Runnable gifSave,
                @NotNull boolean closeIfNull, @NotNull boolean hideControls, boolean autostart,
                String subreddit) {
            this.c = c;
            this.subreddit = subreddit;
            this.video = video;
            this.progressBar = p;
            this.closeIfNull = closeIfNull;
            this.placeholder = placeholder;
            this.doOnClick = gifSave;
            this.hideControls = hideControls;
            this.autostart = autostart;
        }

        public AsyncLoadGif(@NotNull Activity c, @NotNull MediaVideoView video,
                @Nullable ProgressBar p, @Nullable View placeholder, @Nullable Runnable gifSave,
                @NotNull boolean closeIfNull, @NotNull boolean hideControls, boolean autostart,
                TextView size, String subreddit) {
            this.c = c;
            this.video = video;
            this.subreddit = subreddit;
            this.progressBar = p;
            this.closeIfNull = closeIfNull;
            this.placeholder = placeholder;
            this.doOnClick = gifSave;
            this.hideControls = hideControls;
            this.autostart = autostart;
            this.size = size;
        }

        public void onError() {

        }

        public void setMuteVisibility(final boolean visible) {
            if (mute != null) {
                c.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!visible) {
                            mute.setVisibility(View.GONE);
                        } else {
                            mute.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        }

        public void setMute(View muteView) {
            mute = muteView;
        }

        public AsyncLoadGif(@NotNull Activity c, @NotNull MediaVideoView video,
                @Nullable ProgressBar p, @Nullable View placeholder, @NotNull boolean closeIfNull,
                @NotNull boolean hideControls, boolean autostart, String subreddit) {
            this.c = c;
            this.video = video;
            this.subreddit = subreddit;
            this.progressBar = p;
            this.closeIfNull = closeIfNull;
            this.placeholder = placeholder;
            this.hideControls = hideControls;
            this.autostart = autostart;
        }

        public AsyncLoadGif() {
            cacheOnly = true;
        }

        public void cancel() {
            LogUtil.v("cancelling");
            video.suspend();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            gson = new Gson();
        }

        Gson gson;

        public enum VideoType {
            IMGUR, VID_ME, STREAMABLE, GFYCAT, DIRECT, OTHER, VREDDIT;

            public boolean shouldLoadPreview() {
                return this == OTHER;
            }
        }


        public String formatUrl(String s) {
            if (s.endsWith("v") && !s.contains("streamable.com")) {
                s = s.substring(0, s.length() - 1);
            } else if (s.contains("gfycat") && (!s.contains("mp4") && !s.contains("webm"))) {
                if (s.contains("-size_restricted")) s = s.replace("-size_restricted", "");
            }
            if ((s.contains(".webm") || s.contains(".gif")) && !s.contains(".gifv") && s.contains(
                    "imgur.com")) {
                s = s.replace(".gif", ".mp4");
                s = s.replace(".webm", ".mp4");
            }
            if (s.endsWith("/")) s = s.substring(0, s.length() - 1);
            if (s.endsWith("?r")) s = s.substring(0, s.length() - 2);
            if (s.contains("v.redd.it") && !s.contains("DASH")) {
                if (s.endsWith("/")) {
                    s = s.substring(s.length() - 2);
                }
                s = s + "/DASH_9_6_M";
            }

            return s;
        }

        public static VideoType getVideoType(String url) {
            if (url.contains("v.redd.it")) {
                return VideoType.VREDDIT;
            }
            if (url.contains(".mp4") || url.contains("webm") || url.contains("redditmedia.com") || url.contains("preview.redd.it")) {
                return VideoType.DIRECT;
            }
            if (url.contains("gfycat") && !url.contains("mp4")) return VideoType.GFYCAT;
            if (url.contains("imgur.com")) return VideoType.IMGUR;
            if (url.contains("vid.me")) return VideoType.VID_ME;
            if (url.contains("streamable.com")) return VideoType.STREAMABLE;
            return VideoType.OTHER;
        }

        OkHttpClient client = Reddit.client;

        public void loadGfycat(String name, String fullUrl, Gson gson) throws Exception {
            showProgressBar(c, progressBar, false);
            if (!name.startsWith("/")) name = "/" + name;
            if (name.contains("-")) {
                name = name.split("-")[0];
            }
            String gfycatUrl = "https://api.gfycat.com/v1/gfycats" + name;
            LogUtil.v(gfycatUrl);
            final JsonObject result = HttpUtil.getJsonObject(client, gson, gfycatUrl);
            String obj = "";
            if (result == null || result.get("gfyItem") == null || result.getAsJsonObject("gfyItem")
                    .get("mp4Url")
                    .isJsonNull()) {

                onError();
                if (closeIfNull) {
                    c.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                new AlertDialogWrapper.Builder(c).setTitle(R.string.gif_err_title)
                                        .setMessage(R.string.gif_err_msg)
                                        .setCancelable(false)
                                        .setPositiveButton(R.string.btn_ok,
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog,
                                                            int which) {
                                                        c.finish();
                                                    }
                                                })
                                        .setNeutralButton(R.string.open_externally,
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        LinkUtil.openExternally(fullUrl);
                                                        c.finish();
                                                    }
                                                })
                                        .create()
                                        .show();
                            } catch (Exception e) {

                            }
                        }
                    });
                }


            } else {
                if (result.getAsJsonObject("gfyItem").has("mobileUrl")) {
                    obj = result.getAsJsonObject("gfyItem").get("mobileUrl").getAsString();
                } else {
                    obj = result.getAsJsonObject("gfyItem").get("mp4Url").getAsString();
                }
            }
            showProgressBar(c, progressBar, false);
            final URL finalUrl = new URL(obj);
            writeGif(finalUrl, progressBar, c, subreddit);
        }

        @Override
        protected Void doInBackground(String... sub) {
            MediaView.didLoadGif = false;
            Gson gson = new Gson();
            final String url = formatUrl(sub[0]);
            VideoType videoType = getVideoType(url);
            LogUtil.v(url + ", VideoType: " + videoType);
            switch (videoType) {
                case VREDDIT:
                    try {
                        WriteGifMuxed(new URL(url), progressBar, c, subreddit);
                    } catch (Exception e) {
                        LogUtil.e(e,
                                "Error loading URL " + url); //Most likely is an image, not a gif!
                        if (c instanceof MediaView && url.contains("imgur.com") && url.endsWith(
                                ".mp4")) {
                            c.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    (c).startActivity(new Intent(c, MediaView.class).putExtra(
                                            MediaView.EXTRA_URL, url.replace(".mp4",
                                                    ".png"))); //Link is likely an image and not a gif
                                    (c).finish();
                                }
                            });
                        } else {
                            if (closeIfNull) {
                                Intent web = new Intent(c, Website.class);
                                web.putExtra(LinkUtil.EXTRA_URL, url);
                                web.putExtra(LinkUtil.EXTRA_COLOR, Color.BLACK);
                                c.startActivity(web);
                                c.finish();
                            }
                        }
                    }
                    break;
                case GFYCAT:
                    String name = url.substring(url.lastIndexOf("/", url.length()));
                    String gfycatUrl = "https://gfycat.com/cajax/get" + name;

                    try {
                        loadGfycat(name, url, gson);
                    } catch (Exception e) {
                        LogUtil.e(e, "Error loading gfycat video url = ["
                                + url
                                + "] gfycatUrl = ["
                                + gfycatUrl
                                + "]");
                    }
                    break;
                case DIRECT:
                    setMuteVisibility(true);
                case IMGUR:
                    try {
                        writeGif(new URL(url), progressBar, c, subreddit);
                    } catch (Exception e) {
                        LogUtil.e(e,
                                "Error loading URL " + url); //Most likely is an image, not a gif!
                        if (c instanceof MediaView && url.contains("imgur.com") && url.endsWith(
                                ".mp4")) {
                            c.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    (c).startActivity(new Intent(c, MediaView.class).putExtra(
                                            MediaView.EXTRA_URL, url.replace(".mp4",
                                                    ".png"))); //Link is likely an image and not a gif
                                    (c).finish();
                                }
                            });
                        } else {
                            if (closeIfNull) {
                                Intent web = new Intent(c, Website.class);
                                web.putExtra(LinkUtil.EXTRA_URL, url);
                                web.putExtra(LinkUtil.EXTRA_COLOR, Color.BLACK);
                                c.startActivity(web);
                                c.finish();
                            }
                        }
                    }
                    break;
                case STREAMABLE:
                    String hash = url.substring(url.lastIndexOf("/") + 1, url.length());
                    String streamableUrl = "https://api.streamable.com/videos/" + hash;
                    LogUtil.v(streamableUrl);
                    try {
                        final JsonObject result =
                                HttpUtil.getJsonObject(client, gson, streamableUrl);
                        String obj = "";
                        if (result == null
                                || result.get("files") == null
                                || !(result.getAsJsonObject("files").has("mp4")
                                || result.getAsJsonObject("files").has("mp4-mobile"))) {

                            onError();
                            if (closeIfNull) {
                                c.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        new AlertDialogWrapper.Builder(c).setTitle(
                                                R.string.error_video_not_found)
                                                .setMessage(R.string.error_video_message)
                                                .setCancelable(false)
                                                .setPositiveButton(R.string.btn_ok,
                                                        new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(
                                                                    DialogInterface dialog,
                                                                    int which) {
                                                                c.finish();
                                                            }
                                                        })
                                                .create()
                                                .show();
                                    }
                                });
                            }
                        } else {
                            if (result.getAsJsonObject()
                                    .get("files")
                                    .getAsJsonObject()
                                    .has("mp4-mobile") && !result.getAsJsonObject()
                                    .get("files")
                                    .getAsJsonObject()
                                    .get("mp4-mobile")
                                    .getAsJsonObject()
                                    .get("url")
                                    .getAsString()
                                    .isEmpty()) {
                                obj = "https:" + result.getAsJsonObject()
                                        .get("files")
                                        .getAsJsonObject()
                                        .get("mp4-mobile")
                                        .getAsJsonObject()
                                        .get("url")
                                        .getAsString();
                            } else {
                                obj = "https:" + result.getAsJsonObject()
                                        .get("files")
                                        .getAsJsonObject()
                                        .get("mp4")
                                        .getAsJsonObject()
                                        .get("url")
                                        .getAsString();
                            }

                        }
                        final URL finalUrl = new URL(obj);
                        writeGif(finalUrl, progressBar, c, subreddit);
                    } catch (Exception e) {
                        LogUtil.e(e, "Error loading streamable video url = ["
                                + url
                                + "] streamableUrl = ["
                                + streamableUrl
                                + "]");

                        c.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onError();
                            }
                        });
                        if (closeIfNull) {
                            c.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        new AlertDialogWrapper.Builder(c).setTitle(
                                                R.string.error_video_not_found)
                                                .setMessage(R.string.error_video_message)
                                                .setCancelable(false)
                                                .setPositiveButton(R.string.btn_ok,
                                                        new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(
                                                                    DialogInterface dialog,
                                                                    int which) {
                                                                c.finish();
                                                            }
                                                        })
                                                .create()
                                                .show();
                                    } catch (Exception e) {

                                    }
                                }
                            });
                        }
                    }
                    break;
                case VID_ME:
                    String vidmeUrl = "https://api.vid.me/videoByUrl?url=" + url;
                    LogUtil.v(vidmeUrl);
                    try {
                        final JsonObject result = HttpUtil.getJsonObject(client, gson, vidmeUrl);
                        String obj = "";
                        if (result == null
                                || result.isJsonNull()
                                || !result.has("video")
                                || result.get("video").isJsonNull()
                                || !result.get("video").getAsJsonObject().has("complete_url")
                                || result.get("video")
                                .getAsJsonObject()
                                .get("complete_url")
                                .isJsonNull()) {

                            onError();
                            if (closeIfNull) {
                                Intent web = new Intent(c, Website.class);
                                web.putExtra(LinkUtil.EXTRA_URL, url);
                                web.putExtra(LinkUtil.EXTRA_COLOR, Color.BLACK);
                                c.startActivity(web);
                                c.finish();
                            }
                        } else {
                            obj = result.getAsJsonObject()
                                    .get("video")
                                    .getAsJsonObject()
                                    .get("complete_url")
                                    .getAsString();
                        }
                        final URL finalUrl = new URL(obj);
                        writeGif(finalUrl, progressBar, c, subreddit);
                    } catch (Exception e) {
                        LogUtil.e(e, "Error loading vid.me video url = ["
                                + url
                                + "] vidmeUrl = ["
                                + vidmeUrl
                                + "]");
                    }

                    break;

                case OTHER:
                    LogUtil.e("We shouldn't be here!");
                    if (closeIfNull) {
                        Intent web = new Intent(c, Website.class);
                        web.putExtra(LinkUtil.EXTRA_URL, url);
                        web.putExtra(LinkUtil.EXTRA_COLOR, Color.BLACK);
                        c.startActivity(web);
                        c.finish();
                    }
                    break;
            }
            return null;
        }

        public static String readableFileSize(long size) {
            if (size <= 0) return "0";
            final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
            int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
            return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups))
                    + " "
                    + units[digitGroups];
        }

        public static void getRemoteFileSize(String url, OkHttpClient client,
                final TextView sizeText, Activity c) {

            Request request = new Request.Builder().url(url).head().build();
            Response response = null;
            try {
                response = client.newCall(request).execute();
                final long size = response.body().contentLength();
                response.close();
                c.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sizeText.setText(readableFileSize(size));
                    }
                });
                return;
            } catch (IOException e) {
                if (response != null) {
                    response.close();

                }
                e.printStackTrace();
            }
            c.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sizeText.setVisibility(View.GONE);
                }
            });

        }

        public void writeGif(final URL url, final ProgressBar progressBar, final Activity c,
                final String subreddit) {
            if (size != null && c != null && !getProxy().isCached(url.toString())) {
                getRemoteFileSize(url.toString(), client, size, c);
            }
            c.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    String toLoad = getProxy().getProxyUrl(url.toString());

                    video.setVideoPath(toLoad);

                    video.setOnPreparedListener(new OnPreparedListener() {
                        @Override
                        public void onPrepared() {
                            if (placeholder != null) placeholder.setVisibility(View.GONE);
                            LogUtil.v("Prepared");
                        }

                    });

                    if (autostart) {
                        video.start();
                    }

                    if (getProxy().isCached(url.toString())) {
                        progressBar.setVisibility(View.GONE);
                        if (gifSave != null) {
                            gifSave.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    saveGif(getProxy().getCacheFile(url.toString()), c, subreddit);
                                }
                            });
                        } else if (doOnClick != null) {
                            MediaView.doOnClick = new Runnable() {
                                @Override
                                public void run() {
                                    saveGif(getProxy().getCacheFile(url.toString()), c, subreddit);
                                }
                            };
                        }
                    } else {
                        getProxy().registerCacheListener(new CacheListener() {
                            @Override
                            public void onCacheAvailable(final File cacheFile, final String url,
                                    final int percent) {
                                if (progressBar != null && c != null) {
                                    progressBar.setProgress(percent);
                                    if (percent == 100) {
                                        progressBar.setVisibility(View.GONE);
                                        getProxy().unregisterCacheListener(this);
                                        if (size != null) size.setVisibility(View.GONE);
                                        if (gifSave != null) {
                                            gifSave.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    saveGif(getProxy().getCacheFile(url), c,
                                                            subreddit);
                                                }
                                            });
                                        } else if (doOnClick != null) {
                                            MediaView.doOnClick = new Runnable() {
                                                @Override
                                                public void run() {
                                                    saveGif(getProxy().getCacheFile(url), c,
                                                            subreddit);
                                                }
                                            };
                                        }

                                    }
                                }
                                if (percent == 100) {
                                    MediaView.didLoadGif = true;
                                }

                            }
                        }, url.toString());
                    }

                }
            });

        }

        public void WriteGifMuxed(final URL url, final ProgressBar progressBar, final Activity c,
                final String subreddit) {
            if (size != null && c != null && !getProxy().isCached(url.toString())) {
                getRemoteFileSize(url.toString(), client, size, c);
            }

            File videoFile = getProxy().getCacheFile(url.toString());

            if (videoFile.length() <= 0) {

                try {


                    if (!videoFile.exists()) {
                        if (!videoFile.getParentFile().exists()) {
                            videoFile.getParentFile().mkdirs();
                        }
                        videoFile.createNewFile();
                    }

                    HttpURLConnection conv = (HttpURLConnection) url.openConnection();
                    conv.setRequestMethod("GET");

                    //c.setDoOutput(true);
                    conv.connect();

                    String downloadsPath = c.getCacheDir().getAbsolutePath();
                    String fileName = "video.mp4"; //temporary location for video
                    File videoOutput = new File(downloadsPath, fileName);
                    HttpURLConnection cona = (HttpURLConnection) new URL(
                            url.toString().substring(0, url.toString().lastIndexOf("/") + 1)
                                    + "audio").openConnection();
                    cona.setRequestMethod("GET");

                    if (!videoOutput.exists()) {
                        videoOutput.createNewFile();
                    }

                    FileOutputStream fos = new FileOutputStream(videoOutput);
                    InputStream is = conv.getInputStream();
                    int fileLength = conv.getContentLength() + cona.getContentLength();

                    byte data[] = new byte[4096];
                    long total = 0;
                    int count;
                    while ((count = is.read(data)) != -1) {
                        // allow canceling with back button
                        if (isCancelled()) {
                            is.close();
                        }
                        total += count;
                        // publishing the progress....
                        if (fileLength > 0) // only if total length is known
                        {
                            publishProgress((int) (total * 100 / fileLength), url);
                        }
                        fos.write(data, 0, count);
                    }
                    fos.close();
                    is.close();


                    //c.setDoOutput(true);
                    cona.connect();

                    String fileNameAudio = "audio.mp4"; //temporary location for audio
                    File audioOutput = new File(downloadsPath, fileNameAudio);
                    File muxedPath = new File(downloadsPath, "muxedvideo.mp4");
                    muxedPath.createNewFile();

                    if (!audioOutput.exists()) {
                        audioOutput.createNewFile();
                    }

                    fos = new FileOutputStream(audioOutput);

                    int stat = cona.getResponseCode();
                    if (stat != 403) {
                        InputStream isa = cona.getInputStream();

                        byte dataa[] = new byte[4096];
                        int counta;
                        while ((counta = isa.read(dataa)) != -1) {
                            // allow canceling with back button
                            if (isCancelled()) {
                                isa.close();
                            }
                            total += counta;
                            // publishing the progress....
                            if (fileLength > 0) // only if total length is known
                            {
                                publishProgress((int) (total * 100 / fileLength), url);
                            }
                            fos.write(dataa, 0, counta);
                        }
                        fos.close();
                        isa.close();

                        publishProgressInd();

                        GifUtils.mux(videoOutput.getAbsolutePath(), audioOutput.getAbsolutePath(),
                                muxedPath.getAbsolutePath());

                        copy(muxedPath, videoFile);
                        new File(videoFile.getAbsolutePath() + ".a").createNewFile();
                        setMuteVisibility(true);

                    } else {
                        copy(videoOutput, videoFile);
                        //no audio!
                        setMuteVisibility(false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                File isAudio = new File(videoFile.getAbsolutePath() + ".a");
                if (isAudio.exists()) {
                    setMuteVisibility(true);
                }
            }
            c.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    String toLoad = getProxy().getCacheFile(url.toString()).getAbsolutePath();

                    video.setVideoPath(toLoad);

                    video.setOnPreparedListener(new OnPreparedListener() {
                        @Override
                        public void onPrepared() {
                            if (placeholder != null) placeholder.setVisibility(View.GONE);
                            LogUtil.v("Prepared");
                        }

                    });

                    if (autostart) {
                        video.start();
                    }
                    progressBar.setVisibility(View.GONE);
                    if (gifSave != null) {
                        gifSave.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                saveGif(getProxy().getCacheFile(url.toString()), c, subreddit);
                            }
                        });
                    } else if (doOnClick != null) {
                        MediaView.doOnClick = new Runnable() {
                            @Override
                            public void run() {
                                saveGif(getProxy().getCacheFile(url.toString()), c, subreddit);

                                try {
                                    Toast.makeText(c, c.getString(R.string.mediaview_notif_title),
                                            Toast.LENGTH_SHORT).show();
                                } catch (Exception ignored) {

                                }
                            }
                        };
                    }
                }
            });

        }

        private void publishProgressInd() {
            c.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (progressBar != null && c != null) {
                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.setIndeterminate(true);
                    }
                }
            });
        }

        private void publishProgress(final int percent, final URL url) {
            c.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (progressBar != null && c != null) {
                        progressBar.setProgress(percent);
                        if (percent == 100) {
                            progressBar.setVisibility(View.GONE);
                            if (size != null) size.setVisibility(View.GONE);

                        }
                    }
                    if (percent == 100) {
                        MediaView.didLoadGif = true;
                    }

                }
            });
        }


        //Code from https://stackoverflow.com/a/9293885/3697225
        public static void copy(File src, File dst) throws IOException {
            InputStream in = new FileInputStream(src);
            try {
                OutputStream out = new FileOutputStream(dst);
                try {
                    // Transfer bytes from in to out
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                } finally {
                    out.close();
                }
            } finally {
                in.close();
            }
        }

    }


    /**
     * Shows a ProgressBar in the UI. If this method is called from a non-main thread, it will run
     * the UI code on the main thread
     *
     * @param activity        The activity context to use to display the ProgressBar
     * @param progressBar     The ProgressBar to display
     * @param isIndeterminate True to show an indeterminate ProgressBar, false otherwise
     */
    private static void showProgressBar(final Activity activity, final ProgressBar progressBar,
            final boolean isIndeterminate) {
        if (activity == null) return;
        if (Looper.myLooper() == Looper.getMainLooper()) {
            // Current Thread is Main Thread.
            if (progressBar != null) progressBar.setIndeterminate(isIndeterminate);
        } else {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (progressBar != null) progressBar.setIndeterminate(isIndeterminate);
                }
            });
        }
    }

    public static HttpProxyCacheServer getProxy() {
        return Reddit.proxy;
    }


    public static boolean mux(String videoFile, String audioFile, String outputFile) {
        com.googlecode.mp4parser.authoring.Movie video;
        try {
            new MovieCreator();
            video = MovieCreator.build(videoFile);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        com.googlecode.mp4parser.authoring.Movie audio;
        try {
            new MovieCreator();
            audio = MovieCreator.build(audioFile);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }

        com.googlecode.mp4parser.authoring.Track audioTrack = audio.getTracks().get(0);

        CroppedTrack croppedTrack = new CroppedTrack(audioTrack, 0, audioTrack.getSamples().size());
        video.addTrack(croppedTrack);
        Container out = new DefaultMp4Builder().build(video);

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(outputFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        BufferedWritableFileByteChannel byteBufferByteChannel =
                new BufferedWritableFileByteChannel(fos);
        try {
            out.writeContainer(byteBufferByteChannel);
            byteBufferByteChannel.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static class BufferedWritableFileByteChannel implements WritableByteChannel {
        private static final int BUFFER_CAPACITY = 1000000;

        private boolean isOpen = true;
        private final OutputStream outputStream;
        private final ByteBuffer   byteBuffer;
        private final byte[] rawBuffer = new byte[BUFFER_CAPACITY];

        private BufferedWritableFileByteChannel(OutputStream outputStream) {
            this.outputStream = outputStream;
            this.byteBuffer = ByteBuffer.wrap(rawBuffer);
        }

        @Override
        public int write(ByteBuffer inputBuffer) {
            int inputBytes = inputBuffer.remaining();

            if (inputBytes > byteBuffer.remaining()) {
                dumpToFile();
                byteBuffer.clear();

                if (inputBytes > byteBuffer.remaining()) {
                    throw new BufferOverflowException();
                }
            }

            byteBuffer.put(inputBuffer);

            return inputBytes;
        }

        @Override
        public boolean isOpen() {
            return isOpen;
        }

        @Override
        public void close() {
            dumpToFile();
            isOpen = false;
        }

        private void dumpToFile() {
            try {
                outputStream.write(rawBuffer, 0, byteBuffer.position());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static final String AUDIO_RECORDING_FILE_NAME       =
            "audio_Capturing-190814-034638.422.wav";// Input PCM file
    public static final String COMPRESSED_AUDIO_FILE_NAME      = "convertedmp4.m4a";
    // Output MP4/M4A file
    public static final String COMPRESSED_AUDIO_FILE_MIME_TYPE = "audio/mp4a-latm";
    public static final int    COMPRESSED_AUDIO_FILE_BIT_RATE  = 64000; // 64kbps
    public static final int    SAMPLING_RATE                   = 48000;
    public static final int    BUFFER_SIZE                     = 48000;
    public static final int    CODEC_TIMEOUT_IN_MS             = 5000;
    String   LOGTAG  = "CONVERT AUDIO";
    Runnable convert = new Runnable() {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            try {
                String filePath = Environment.getExternalStorageDirectory().getPath()
                        + "/"
                        + AUDIO_RECORDING_FILE_NAME;
                File inputFile = new File(filePath);
                FileInputStream fis = new FileInputStream(inputFile);

                File outputFile = new File(
                        Environment.getExternalStorageDirectory().getAbsolutePath()
                                + "/"
                                + COMPRESSED_AUDIO_FILE_NAME);
                if (outputFile.exists()) outputFile.delete();

                MediaMuxer mux = new MediaMuxer(outputFile.getAbsolutePath(),
                        MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

                MediaFormat outputFormat =
                        MediaFormat.createAudioFormat(COMPRESSED_AUDIO_FILE_MIME_TYPE,
                                SAMPLING_RATE, 1);
                outputFormat.setInteger(MediaFormat.KEY_AAC_PROFILE,
                        MediaCodecInfo.CodecProfileLevel.AACObjectLC);
                outputFormat.setInteger(MediaFormat.KEY_BIT_RATE, COMPRESSED_AUDIO_FILE_BIT_RATE);
                outputFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384);

                MediaCodec codec = MediaCodec.createEncoderByType(COMPRESSED_AUDIO_FILE_MIME_TYPE);
                codec.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                codec.start();

                ByteBuffer[] codecInputBuffers = codec.getInputBuffers(); // Note: Array of buffers
                ByteBuffer[] codecOutputBuffers = codec.getOutputBuffers();

                MediaCodec.BufferInfo outBuffInfo = new MediaCodec.BufferInfo();
                byte[] tempBuffer = new byte[BUFFER_SIZE];
                boolean hasMoreData = true;
                double presentationTimeUs = 0;
                int audioTrackIdx = 0;
                int totalBytesRead = 0;
                int percentComplete = 0;
                do {
                    int inputBufIndex = 0;
                    while (inputBufIndex != -1 && hasMoreData) {
                        inputBufIndex = codec.dequeueInputBuffer(CODEC_TIMEOUT_IN_MS);

                        if (inputBufIndex >= 0) {
                            ByteBuffer dstBuf = codecInputBuffers[inputBufIndex];
                            dstBuf.clear();

                            int bytesRead = fis.read(tempBuffer, 0, dstBuf.limit());
                            Log.e("bytesRead", "Readed " + bytesRead);
                            if (bytesRead == -1) { // -1 implies EOS
                                hasMoreData = false;
                                codec.queueInputBuffer(inputBufIndex, 0, 0,
                                        (long) presentationTimeUs,
                                        MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            } else {
                                totalBytesRead += bytesRead;
                                dstBuf.put(tempBuffer, 0, bytesRead);
                                codec.queueInputBuffer(inputBufIndex, 0, bytesRead,
                                        (long) presentationTimeUs, 0);
                                presentationTimeUs =
                                        1000000l * (totalBytesRead / 2) / SAMPLING_RATE;
                            }
                        }
                    }
                    // Drain audio
                    int outputBufIndex = 0;
                    while (outputBufIndex != MediaCodec.INFO_TRY_AGAIN_LATER) {
                        outputBufIndex =
                                codec.dequeueOutputBuffer(outBuffInfo, CODEC_TIMEOUT_IN_MS);
                        if (outputBufIndex >= 0) {
                            ByteBuffer encodedData = codecOutputBuffers[outputBufIndex];
                            encodedData.position(outBuffInfo.offset);
                            encodedData.limit(outBuffInfo.offset + outBuffInfo.size);
                            if ((outBuffInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0
                                    && outBuffInfo.size != 0) {
                                codec.releaseOutputBuffer(outputBufIndex, false);
                            } else {
                                mux.writeSampleData(audioTrackIdx,
                                        codecOutputBuffers[outputBufIndex], outBuffInfo);
                                codec.releaseOutputBuffer(outputBufIndex, false);
                            }
                        } else if (outputBufIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                            outputFormat = codec.getOutputFormat();
                            Log.v(LOGTAG, "Output format changed - " + outputFormat);
                            audioTrackIdx = mux.addTrack(outputFormat);
                            mux.start();
                        } else if (outputBufIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                            Log.e(LOGTAG, "Output buffers changed during encode!");
                        } else if (outputBufIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                            // NO OP
                        } else {
                            Log.e(LOGTAG, "Unknown return code from dequeueOutputBuffer - "
                                    + outputBufIndex);
                        }
                    }
                    percentComplete = (int) Math.round(
                            ((float) totalBytesRead / (float) inputFile.length()) * 100.0);
                    Log.v(LOGTAG, "Conversion % - " + percentComplete);
                } while (outBuffInfo.flags != MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                fis.close();
                mux.stop();
                mux.release();
                Log.v(LOGTAG, "Compression done ...");
            } catch (FileNotFoundException e) {
                Log.e(LOGTAG, "File not found!", e);
            } catch (IOException e) {
                Log.e(LOGTAG, "IO exception!", e);
            }

            //mStop = false;
            // Notify UI thread...
        }
    };


}
