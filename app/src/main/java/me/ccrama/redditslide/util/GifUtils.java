package me.ccrama.redditslide.util;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory;
import com.google.android.exoplayer2.source.dash.manifest.AdaptationSet;
import com.google.android.exoplayer2.source.dash.manifest.DashManifest;
import com.google.android.exoplayer2.source.dash.manifest.DashManifestParser;
import com.google.android.exoplayer2.source.dash.manifest.Representation;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSourceInputStream;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.jetbrains.annotations.NotNull;
import org.mp4parser.Container;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.builder.DefaultMp4Builder;
import org.mp4parser.muxer.container.mp4.MovieCreator;
import org.mp4parser.muxer.tracks.ClippedTrack;

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
import java.util.Locale;
import java.util.UUID;

import me.ccrama.redditslide.Activities.MediaView;
import me.ccrama.redditslide.Activities.Website;
import me.ccrama.redditslide.Fragments.FolderChooserDialogCreate;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Views.ExoVideoView;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * GIF handling utilities
 */
public class GifUtils {
    /**
     * Create a notification that opens a newly-saved GIF
     *
     * @param f File referencing the GIF
     * @param c
     */
    public static void doNotifGif(File f, Activity c) {
        MediaScannerConnection.scanFile(c,
                new String[]{f.getAbsolutePath()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        final Intent shareIntent = FileUtil.getFileIntent(f, new Intent(Intent.ACTION_VIEW), c);
                        PendingIntent contentIntent =
                                PendingIntent.getActivity(c, 0, shareIntent, PendingIntent.FLAG_CANCEL_CURRENT);


                        Notification notif =
                                new NotificationCompat.Builder(c, Reddit.CHANNEL_IMG).setContentTitle(c.getString(R.string.gif_saved))
                                        .setSmallIcon(R.drawable.save_content)
                                        .setContentIntent(contentIntent)
                                        .build();

                        NotificationManager mNotificationManager =
                                (NotificationManager) c.getSystemService(Activity.NOTIFICATION_SERVICE);
                        mNotificationManager.notify((int) System.currentTimeMillis(), notif);
                    }
                }
        );
    }

    /**
     * Show an error dialog for failed GIF saving
     *
     * @param a
     */
    private static void showErrorDialog(final Activity a) {
        new AlertDialogWrapper.Builder(a).setTitle(R.string.err_something_wrong)
                .setMessage(R.string.err_couldnt_save_choose_new)
                .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new FolderChooserDialogCreate.Builder((MediaView) a).chooseButton(
                                R.string.btn_select)  // changes label of the choose button
                                .initialPath(Environment.getExternalStorageDirectory().getPath())
                                // changes initial path, defaults to external storage directory
                                .show();
                    }
                })
                .setNegativeButton(R.string.btn_no, null)
                .show();
    }

    /**
     * Show the first-save dialog
     *
     * @param a
     */
    private static void showFirstDialog(final Activity a) {
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

    /**
     * Temporarily cache or permanently save a GIF
     *
     * @param uri       URL of the GIF
     * @param a
     * @param subreddit Subreddit for saving in sub-specific folders
     * @param save      Whether to permanently save the GIF of just temporarily cache it
     */
    public static void cacheSaveGif(Uri uri, Activity a, String subreddit, boolean save) {
        if (save) {
            try {
                Toast.makeText(a, a.getString(R.string.mediaview_notif_title), Toast.LENGTH_SHORT).show();
            } catch (Exception ignored) {
            }
        }

        if (Reddit.appRestart.getString("imagelocation", "").isEmpty()) {
            showFirstDialog(a);
        } else if (!new File(Reddit.appRestart.getString("imagelocation", "")).exists()) {
            showErrorDialog(a);
        } else {
            new AsyncTask<Void, Integer, Boolean>() {
                File outFile;
                NotificationManager notifMgr = (NotificationManager) a.getSystemService(Activity.NOTIFICATION_SERVICE);

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    if (save) {
                        Notification notif = new NotificationCompat.Builder(a, Reddit.CHANNEL_IMG)
                                .setContentTitle(a.getString(R.string.mediaview_saving,
                                        uri.toString().replace("/DASHPlaylist.mpd", "")))
                                .setSmallIcon(R.drawable.save)
                                .setProgress(0, 0, true)
                                .setOngoing(true)
                                .build();
                        notifMgr.notify(1, notif);
                    }
                }

                @Override
                protected Boolean doInBackground(Void... voids) {
                    if (SettingValues.imageSubfolders && !subreddit.isEmpty()) {
                        File directory = new File(Reddit.appRestart.getString("imagelocation", "")
                                + (SettingValues.imageSubfolders
                                && !subreddit.isEmpty() ? File.separator + subreddit : ""));
                        directory.mkdirs();
                    }
                    outFile = new File(Reddit.appRestart.getString("imagelocation", "")
                            + (SettingValues.imageSubfolders && !subreddit.isEmpty() ? File.separator
                            + subreddit : "")
                            + File.separator
                            + UUID.randomUUID().toString()
                            + ".mp4");

                    OutputStream out = null;
                    InputStream in = null;

                    try {
                        DataSource.Factory downloader = new OkHttpDataSourceFactory(Reddit.client, a.getString(R.string.app_name));
                        DataSource.Factory cacheDataSourceFactory =
                                new CacheDataSource.Factory()
                                        .setCache(Reddit.videoCache)
                                        .setUpstreamDataSourceFactory(downloader);
                        if (uri.getLastPathSegment().endsWith("DASHPlaylist.mpd")) {
                            InputStream dashManifestStream = new DataSourceInputStream(cacheDataSourceFactory.createDataSource(),
                                    new DataSpec(uri));
                            DashManifest dashManifest = new DashManifestParser().parse(uri, dashManifestStream);
                            dashManifestStream.close();

                            Uri audioUri = null;
                            Uri videoUri = null;

                            for (int i = 0; i < dashManifest.getPeriodCount(); i++) {
                                for (AdaptationSet as : dashManifest.getPeriod(i).adaptationSets) {
                                    boolean isAudio = false;
                                    int bitrate = 0;
                                    String hqUri = null;
                                    for (Representation r : as.representations) {
                                        if (r.format.bitrate > bitrate) {
                                            bitrate = r.format.bitrate;
                                            hqUri = r.baseUrl;
                                        }
                                        if (MimeTypes.isAudio(r.format.sampleMimeType)) {
                                            isAudio = true;
                                        }
                                    }
                                    if (isAudio) {
                                        audioUri = Uri.parse(hqUri);
                                    } else {
                                        videoUri = Uri.parse(hqUri);
                                    }
                                }
                            }

                            if (audioUri != null) {
                                LogUtil.v("Downloading DASH audio from: " + audioUri);
                                DataSourceInputStream audioInputStream = new DataSourceInputStream(
                                        cacheDataSourceFactory.createDataSource(), new DataSpec(audioUri));
                                if (save) {
                                    FileUtils.copyInputStreamToFile(audioInputStream,
                                            new File(a.getCacheDir().getAbsolutePath(), "audio.mp4"));
                                } else {
                                    IOUtils.copy(audioInputStream, NullOutputStream.NULL_OUTPUT_STREAM);
                                }
                                audioInputStream.close();
                            }
                            if (videoUri != null) {
                                LogUtil.v("Downloading DASH video from: " + videoUri);
                                DataSourceInputStream videoInputStream = new DataSourceInputStream(
                                        cacheDataSourceFactory.createDataSource(), new DataSpec(videoUri));
                                if (save) {
                                    FileUtils.copyInputStreamToFile(videoInputStream,
                                            new File(a.getCacheDir().getAbsolutePath(), "video.mp4"));
                                } else {
                                    IOUtils.copy(videoInputStream, NullOutputStream.NULL_OUTPUT_STREAM);
                                }
                                videoInputStream.close();
                            }

                            if (!save) {
                                return true;
                            } else if (audioUri != null && videoUri != null) {
                                if (mux(new File(a.getCacheDir().getAbsolutePath(), "video.mp4").getAbsolutePath(),
                                        new File(a.getCacheDir().getAbsolutePath(), "video.mp4").getAbsolutePath(),
                                        new File(a.getCacheDir().getAbsolutePath(), "muxed.mp4").getAbsolutePath())) {
                                    in = new FileInputStream(new File(a.getCacheDir().getAbsolutePath(), "muxed.mp4"));
                                } else {
                                    throw new IOException("Muxing failed!");
                                }
                            } else {
                                in = new FileInputStream(new File(a.getCacheDir().getAbsolutePath(), "video.mp4"));
                            }
                        } else {
                            in = new DataSourceInputStream(cacheDataSourceFactory.createDataSource(), new DataSpec(uri));
                        }

                        out = save ? new FileOutputStream(outFile) : NullOutputStream.NULL_OUTPUT_STREAM;
                        IOUtils.copy(in, out);
                        out.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        LogUtil.e("Error saving GIF called with: "
                                + "from = ["
                                + uri
                                + "], in = ["
                                + in
                                + "]");
                        return false;
                    } finally {
                        try {
                            if (out != null) {
                                out.close();
                            }
                            if (in != null) {
                                in.close();
                            }
                        } catch (IOException e) {
                            LogUtil.e("Error closing GIF called with: "
                                    + "from = ["
                                    + uri
                                    + "], out = ["
                                    + out
                                    + "]");
                            return false;
                        }
                    }
                    return true;
                }

                @Override
                protected void onPostExecute(Boolean success) {
                    super.onPostExecute(success);
                    if (save) {
                        notifMgr.cancel(1);
                        if (success) {
                            doNotifGif(outFile, a);
                        } else {
                            showErrorDialog(a);
                        }
                    }
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public static class AsyncLoadGif extends AsyncTask<String, Void, Uri> {

        private Activity c;
        private ExoVideoView video;
        private ProgressBar progressBar;
        private View placeholder;
        private View gifSave;
        private boolean closeIfNull;
        private Runnable doOnClick;
        private boolean autostart;
        public String subreddit;

        private TextView size;

        public AsyncLoadGif(@NotNull Activity c, @NotNull ExoVideoView video,
                @Nullable ProgressBar p, @Nullable View placeholder, @Nullable Runnable gifSave,
                boolean closeIfNull, boolean autostart, String subreddit) {
            this.c = c;
            this.subreddit = subreddit;
            this.video = video;
            this.progressBar = p;
            this.closeIfNull = closeIfNull;
            this.placeholder = placeholder;
            this.doOnClick = gifSave;
            this.autostart = autostart;
        }

        public AsyncLoadGif(@NotNull Activity c, @NotNull ExoVideoView video,
                @Nullable ProgressBar p, @Nullable View placeholder, @Nullable Runnable gifSave,
                boolean closeIfNull, boolean autostart, TextView size, String subreddit) {
            this.c = c;
            this.video = video;
            this.subreddit = subreddit;
            this.progressBar = p;
            this.closeIfNull = closeIfNull;
            this.placeholder = placeholder;
            this.doOnClick = gifSave;
            this.autostart = autostart;
            this.size = size;
        }

        public void onError() {

        }

        public AsyncLoadGif(@NotNull Activity c, @NotNull ExoVideoView video,
                @Nullable ProgressBar p, @Nullable View placeholder, @NotNull boolean closeIfNull,
                boolean autostart, String subreddit) {
            this.c = c;
            this.video = video;
            this.subreddit = subreddit;
            this.progressBar = p;
            this.closeIfNull = closeIfNull;
            this.placeholder = placeholder;
            this.autostart = autostart;
        }

        public void cancel() {
            LogUtil.v("cancelling");
            video.stop();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            gson = new Gson();
        }

        Gson gson;

        public enum VideoType {
            IMGUR, STREAMABLE, GFYCAT, DIRECT, OTHER, VREDDIT;

            public boolean shouldLoadPreview() {
                return this == OTHER;
            }
        }

        /**
         * Format a video URL correctly and strip unnecessary parts
         *
         * @param s URL to format
         * @return Formatted URL
         */
        public static String formatUrl(String s) {
            if (s.endsWith("v") && !s.contains("streamable.com")) {
                s = s.substring(0, s.length() - 1);
            } else if (s.contains("gfycat") && (!s.contains("mp4") && !s.contains("webm"))) {
                s = s.replace("-size_restricted", "");
                s = s.replace(".gif", "");
            }
            if ((s.contains(".webm") || s.contains(".gif")) && !s.contains(".gifv") && s.contains("imgur.com")) {
                s = s.replace(".gif", ".mp4");
                s = s.replace(".webm", ".mp4");
            }
            if (s.endsWith("/")) s = s.substring(0, s.length() - 1);
            if (s.endsWith("?r")) s = s.substring(0, s.length() - 2);
            if (s.contains("v.redd.it") && !s.contains("DASHPlaylist")) {
                if (s.contains("DASH")) {
                    s = s.substring(0, s.indexOf("DASH"));
                }
                if (s.endsWith("/")) {
                    s = s.substring(0, s.length() - 1);
                }

                s += "/DASHPlaylist.mpd";
            }

            return s;
        }

        /**
         * Identifies the type of a video URL
         *
         * @param url URL to identify the type of
         * @return The type of video
         */
        public static VideoType getVideoType(String url) {
            String realURL = url.toLowerCase(Locale.ENGLISH);
            if (realURL.contains("v.redd.it")) {
                return VideoType.VREDDIT;
            }
            if (realURL.contains(".mp4") || realURL.contains("webm") || realURL.contains("redditmedia.com")
                    || realURL.contains("preview.redd.it")) {
                return VideoType.DIRECT;
            }
            if (realURL.contains("gfycat") && !realURL.contains("mp4")) return VideoType.GFYCAT;
            if (realURL.contains("redgifs") && !realURL.contains("mp4")) return VideoType.GFYCAT;
            if (realURL.contains("imgur.com")) return VideoType.IMGUR;
            if (realURL.contains("streamable.com")) return VideoType.STREAMABLE;
            return VideoType.OTHER;
        }

        OkHttpClient client = Reddit.client;

        /**
         * Load the correct URL for a gfycat gif
         *
         * @param name    Name of the gfycat gif
         * @param fullUrl full URL to the gfycat
         * @param gson
         * @return Correct URL
         */
        Uri loadGfycat(String name, String fullUrl, Gson gson) {
            showProgressBar(c, progressBar, true);
            String host = "gfycat";
            if (fullUrl.contains("redgifs")) {
                host = "redgifs";
            }
            if (!name.startsWith("/")) name = "/" + name;
            if (name.contains("-")) {
                name = name.split("-")[0];
            }
            String gfycatUrl = "https://api." + host + ".com/v1/gfycats" + name;
            final JsonObject result = HttpUtil.getJsonObject(client, gson, gfycatUrl);
            String obj;
            if (result == null || result.get("gfyItem") == null || result.getAsJsonObject("gfyItem")
                    .get("mp4Url")
                    .isJsonNull()) {
                //If the result null, the gfycat link may be redirecting to gifdeliverynetwork which is powered by redgifs.
                //Try getting the redirected url from gfycat and check if redirected url is gifdeliverynetwork and return the url
                if (result == null) {
                    try {
                        URL newUrl = new URL(fullUrl);
                        HttpURLConnection ucon = (HttpURLConnection) newUrl.openConnection();
                        ucon.setInstanceFollowRedirects(false);
                        String secondURL = new URL(ucon.getHeaderField("location")).toString();
                        if (secondURL.contains("gifdeliverynetwork")){
                            return Uri.parse(secondURL);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

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
                            } catch (Exception ignored) {
                            }
                        }
                    });
                }

                return null;
            } else {
                if (result.getAsJsonObject("gfyItem").has("mobileUrl")) {
                    obj = result.getAsJsonObject("gfyItem").get("mobileUrl").getAsString();
                } else {
                    obj = result.getAsJsonObject("gfyItem").get("mp4Url").getAsString();
                }
            }
            return Uri.parse(obj);
        }

        /*Loads a direct MP4, used for DASH mp4 or direct/imgur videos, currently unused
        private void loadDirect(String url) {
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
        }*/

        //Handles failures of loading a DASH mp4 or muxing a Reddit video
        private void catchVRedditFailure(Exception e, String url) {
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
                openWebsite(url);
            }
        }

        @Override
        protected Uri doInBackground(String... sub) {
            MediaView.didLoadGif = false;
            Gson gson = new Gson();
            final String url = formatUrl(sub[0]);
            VideoType videoType = getVideoType(url);
            LogUtil.v(url + ", VideoType: " + videoType);
            if (size != null) {
                getRemoteFileSize(url, client, size, c);
            }
            switch (videoType) {
                case VREDDIT:
                    /* We may not need this after all, but keeping the code here in case we run into more DASH issues. This is implemented in the iOS app
                    try {
                        //If it's an HLSPlaylist, there is a good chance we can find a DASH mp4 url
                        if (url.contains("HLSPlaylist")) {
                            //Test these qualities
                            getQualityURL(url, new String[]{"1080", "720", "480", "360", "240", "96"},
                                    (didFindVideo, videoUrl) -> {
                                        if (didFindVideo) {
                                            //Load the MP4 directly
                                            loadDirect(videoUrl);
                                        } else {
                                            try {
                                                //Fall back to muxing code
                                                WriteGifMuxed(new URL(url), progressBar, c, subreddit);
                                            } catch (Exception e) {
                                                catchVRedditFailure(e, url);
                                            }
                                        }
                                    });
                        } else {
                            WriteGifMuxed(new URL(url), progressBar, c, subreddit);
                        }
                    } catch (Exception e) {
                        catchVRedditFailure(e, url);
                    }
                    break;*/
                    return Uri.parse(url);
                case GFYCAT:
                    String name = url.substring(url.lastIndexOf("/"));
                    String gfycatUrl = "https://api.gfycat.com/v1/gfycats" + name;

                    //Check if resolved gfycat link is gifdeliverynetwork. If it is gifdeliverynetwork, open the link externally
                    try {
                        Uri uri = loadGfycat(name, url, gson);
                        if(uri.toString().contains("gifdeliverynetwork")){
                            openWebsite(url);
                            return null;
                        } else return uri;
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
                        return Uri.parse(url);
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
                            openWebsite(url);
                        }
                    }
                    break;
                case STREAMABLE:
                    String hash = url.substring(url.lastIndexOf("/") + 1);
                    String streamableUrl = "https://api.streamable.com/videos/" + hash;
                    LogUtil.v(streamableUrl);
                    try {
                        final JsonObject result =
                                HttpUtil.getJsonObject(client, gson, streamableUrl);
                        String obj;
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
                                obj = result.getAsJsonObject()
                                        .get("files")
                                        .getAsJsonObject()
                                        .get("mp4-mobile")
                                        .getAsJsonObject()
                                        .get("url")
                                        .getAsString();
                            } else {
                                obj = result.getAsJsonObject()
                                        .get("files")
                                        .getAsJsonObject()
                                        .get("mp4")
                                        .getAsJsonObject()
                                        .get("url")
                                        .getAsString();
                            }
                            return Uri.parse(obj);
                        }
                    } catch (Exception e) {
                        LogUtil.e(e, "Error loading streamable video url = ["
                                + url
                                + "] streamableUrl = ["
                                + streamableUrl
                                + "]");

                        c.runOnUiThread(this::onError);
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
                                    } catch (Exception ignored) {
                                    }
                                }
                            });
                        }
                    }
                    break;
                case OTHER:
                    LogUtil.e("We shouldn't be here!");
                    // unless it's a .gif that reddit didn't generate a preview vid for, then we should be here
                    // e.g. https://www.reddit.com/r/testslideforreddit/comments/hpht5o/stinky/
                    openWebsite(url);
                    break;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Uri uri) {
            if (uri == null) {
                cancel();
                return;
            }
            progressBar.setIndeterminate(true);

            if (gifSave != null) {
                gifSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cacheSaveGif(uri, c, subreddit, true);
                    }
                });
            } else if (doOnClick != null) {
                MediaView.doOnClick = new Runnable() {
                    @Override
                    public void run() {
                        cacheSaveGif(uri, c, subreddit, true);
                    }
                };
            }

            ExoVideoView.VideoType type = uri.getHost().equals("v.redd.it")
                    ? ExoVideoView.VideoType.DASH : ExoVideoView.VideoType.STANDARD;
            video.setVideoURI(uri, type, new Player.EventListener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    if (playbackState == Player.STATE_READY) {
                        progressBar.setVisibility(View.GONE);
                        if (size != null) {
                            size.setVisibility(View.GONE);
                        }
                    } else if (playbackState == Player.STATE_BUFFERING) {
                        progressBar.setVisibility(View.VISIBLE);
                        if (size != null) {
                            size.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
            if (autostart) {
                video.play();
            }
        }

        /* Code currently unused, but could be used for future DASH issues
                public interface VideoSuccessCallback {
            void onVideoFound(Boolean didFindVideo, String videoUrl);
        }

        public interface VideoTestCallback {
            void onTestComplete(Boolean testSuccess, String videoUrl);
        }

        //Find a Reddit video MP4 URL by replacing HLSPlaylist.m3u8 with tests of different qualities
        public static void getQualityURL(String urlToLoad, String[] qualityList, VideoSuccessCallback callback) {
            if (qualityList.length == 0) {
                //Will fall back to muxing code if no URL was found
                callback.onVideoFound(false, "");
            } else {
                //Test current first link in qualityList
                VideoTestCallback testCallback = (testSuccess, videoUrl) -> {
                    if (testSuccess) {
                        //Success, load this video
                        callback.onVideoFound(true, videoUrl);
                    } else {
                        //Failed, check next video URL
                        String[] newList = Arrays.copyOfRange(qualityList, 1, qualityList.length);
                        getQualityURL(urlToLoad, newList, callback);
                    }
                };
                testQuality(urlToLoad, qualityList[0], testCallback);

            }
        }

        //Test URL headers to see if this quality URL exists
        private static void testQuality(String urlToLoad, String quality, AsyncLoadGif.VideoTestCallback callback) {
            String newURL = urlToLoad.replace("HLSPlaylist.m3u8", "DASH_" + quality + ".mp4");
            try {
                URL url = new URL(newURL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("HEAD");
                con.connect();
                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    //Success, load this MP4
                    callback.onTestComplete(true, newURL);
                } else {
                    //Failed, this callback will test a new URL
                    callback.onTestComplete(false, newURL);
                }
            } catch (Exception e) {
                e.printStackTrace();
                //Failed, this callback will test a new URL
                callback.onTestComplete(false, newURL);
            }
        }
         */

        /**
         * Convert a byte count into a human-readable size
         *
         * @param size Byte count
         * @return Human-readable size
         */
        static String readableFileSize(long size) {
            if (size <= 0) return "0";
            final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
            int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
            return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups))
                    + " "
                    + units[digitGroups];
        }

        /**
         * Get a remote video's file size
         *
         * @param url      URL of video (or v.redd.it DASH manifest) to get
         * @param client   OkHttpClient
         * @param sizeText TextView to put size into
         * @param c        Activity
         */
        static void getRemoteFileSize(String url, OkHttpClient client,
                final TextView sizeText, Activity c) {
            if (!url.contains("v.redd.it")) {
                Request request = new Request.Builder().url(url).head().build();
                Response response;
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                DataSource.Factory downloader = new OkHttpDataSourceFactory(Reddit.client,
                        c.getString(R.string.app_name));
                DataSource.Factory cacheDataSourceFactory =
                        new CacheDataSource.Factory()
                                .setCache(Reddit.videoCache)
                                .setUpstreamDataSourceFactory(downloader);
                InputStream dashManifestStream = new DataSourceInputStream(cacheDataSourceFactory.createDataSource(),
                        new DataSpec(Uri.parse(url)));
                try {
                    DashManifest dashManifest = new DashManifestParser().parse(Uri.parse(url), dashManifestStream);
                    dashManifestStream.close();
                    long videoSize = 0;
                    long audioSize = 0;

                    for (int i = 0; i < dashManifest.getPeriodCount(); i++) {
                        for (AdaptationSet as : dashManifest.getPeriod(i).adaptationSets) {
                            boolean isAudio = false;
                            int bitrate = 0;
                            String hqUri = null;
                            for (Representation r : as.representations) {
                                if (r.format.bitrate > bitrate) {
                                    bitrate = r.format.bitrate;
                                    hqUri = r.baseUrl;
                                }
                                if (MimeTypes.isAudio(r.format.sampleMimeType)) {
                                    isAudio = true;
                                }
                            }

                            Request request = new Request.Builder().url(hqUri).head().build();
                            Response response = null;
                            try {
                                response = client.newCall(request).execute();
                                if (isAudio) {
                                    audioSize = response.body().contentLength();
                                } else {
                                    videoSize = response.body().contentLength();
                                }
                                response.close();
                            } catch (IOException e) {
                                if (response != null)
                                    response.close();
                            }
                        }
                    }
                    final long totalSize = videoSize + audioSize;
                    c.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // We can't know which quality will be selected, so we display <= the highest quality size
                            if (totalSize > 0)
                                sizeText.setText("≤ " + readableFileSize(totalSize));
                        }
                    });
                } catch (IOException ignored) {
                }
            }
        }

        private void openWebsite(String url){
            if (closeIfNull) {
                Intent web = new Intent(c, Website.class);
                web.putExtra(LinkUtil.EXTRA_URL, url);
                web.putExtra(LinkUtil.EXTRA_COLOR, Color.BLACK);
                c.startActivity(web);
                c.finish();
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

    /**
     * Mux a video and audio file (e.g. from DASH) together into a single video
     *
     * @param videoFile  Video file
     * @param audioFile  Audio file
     * @param outputFile File to output muxed video to
     * @return Whether the muxing completed successfully
     */
    private static boolean mux(String videoFile, String audioFile, String outputFile) {
        Movie video;
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

        Movie audio;
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

        Track audioTrack = audio.getTracks().get(0);

        ClippedTrack croppedTrack = new ClippedTrack(audioTrack, 0, audioTrack.getSamples().size());
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
        private final ByteBuffer byteBuffer;
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
}
