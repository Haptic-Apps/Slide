package me.ccrama.redditslide.util;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.danikula.videocache.CacheListener;
import com.danikula.videocache.HttpProxyCacheServer;
import com.devbrackets.android.exomedia.listener.OnPreparedListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
        shareIntent.setType("video/*");
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

        private  Activity       c;
        private MediaVideoView video;
        private  ProgressBar    progressBar;
        private  View           placeholder;
        private  View           gifSave;
        private  boolean        closeIfNull;
        private  boolean        hideControls;
        private  boolean        autostart;
        private  Runnable       doOnClick;
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
        Gson   gson;

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
                s = s + "/DASHPlaylist.mpd";
            } else if(s.contains("v.redd.it")){
                s = s.substring(0, s.indexOf("DASH") - 2);
                s = s + "/DASHPlaylist.mpd";
            }

            return s;
        }

        public static VideoType getVideoType(String url) {
            if(url.contains("v.redd.it")){
                return VideoType.VREDDIT;
            }
            if (url.contains(".mp4")
                    || url.contains("webm")
                    || url.contains("redditmedia.com")
                    ) {
                return VideoType.DIRECT;
            }
            if (url.contains("gfycat") && !url.contains("mp4")) return VideoType.GFYCAT;
            if (url.contains("imgur.com")) return VideoType.IMGUR;
            if (url.contains("vid.me")) return VideoType.VID_ME;
            if (url.contains("streamable.com")) return VideoType.STREAMABLE;
            return VideoType.OTHER;
        }

        OkHttpClient client = Reddit.client;

        public void loadGfycat(String name, Gson gson) throws Exception {
            showProgressBar(c, progressBar, false);
            if (!name.startsWith("/")) name = "/" + name;
            String gfycatUrl = "https://gfycat.com/cajax/get" + name;
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
                        writeGifHSL(new URL(url), progressBar, c, subreddit);
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
                                web.putExtra(Website.EXTRA_URL, url);
                                web.putExtra(Website.EXTRA_COLOR, Color.BLACK);
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
                        loadGfycat(name, gson);
                    } catch (Exception e) {
                        LogUtil.e(e, "Error loading gfycat video url = ["
                                + url
                                + "] gfycatUrl = ["
                                + gfycatUrl
                                + "]");
                    }
                    break;
                case DIRECT:
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
                                web.putExtra(Website.EXTRA_URL, url);
                                web.putExtra(Website.EXTRA_COLOR, Color.BLACK);
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
                                web.putExtra(Website.EXTRA_URL, url);
                                web.putExtra(Website.EXTRA_COLOR, Color.BLACK);
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
                        web.putExtra(Website.EXTRA_URL, url);
                        web.putExtra(Website.EXTRA_COLOR, Color.BLACK);
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
                 final String subreddit) throws Exception {
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
                                                    saveGif(getProxy().getCacheFile(url), c, subreddit);
                                                }
                                            });
                                        } else if (doOnClick != null) {
                                            MediaView.doOnClick = new Runnable() {
                                                @Override
                                                public void run() {
                                                    saveGif(getProxy().getCacheFile(url), c, subreddit);
                                                    try {
                                                        Toast.makeText(c, c.getString(
                                                                R.string.mediaview_notif_title),
                                                                Toast.LENGTH_SHORT).show();
                                                    } catch (Exception ignored) {

                                                    }
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

        public void writeGifHSL(final URL url, final ProgressBar progressBar, final Activity c,
                final String subreddit) throws Exception {
            c.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    video.setVideoDASH(url);
                    if (autostart) {
                        video.start();
                    }
                    progressBar.setVisibility(View.GONE);

                }
            });

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


}
