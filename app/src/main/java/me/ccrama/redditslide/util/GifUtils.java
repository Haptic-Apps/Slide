package me.ccrama.redditslide.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.nostra13.universalimageloader.core.assist.ContentLengthInputStream;
import com.nostra13.universalimageloader.utils.IoUtils;

import org.jetbrains.annotations.NotNull;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.UUID;

import me.ccrama.redditslide.Activities.MediaView;
import me.ccrama.redditslide.Activities.Shadowbox;
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
        if (!gfyUrl.endsWith("-mobile.mp4"))
            gfyUrl = gfyUrl.replaceAll("\\.mp4", "-mobile.mp4");
        return gfyUrl;
    }

    public static class AsyncLoadGif extends AsyncTask<String, Void, Void> {

        public Activity c;
        public MediaVideoView video;
        public ProgressBar progressBar;
        public View placeholder;
        public View gifSave;
        public boolean closeIfNull;
        public boolean hideControls;
        public boolean autostart;
        public Runnable doOnClick;

        public TextView size;

        public AsyncLoadGif(@NotNull Activity c, @NotNull MediaVideoView video, @Nullable ProgressBar p, @Nullable View placeholder, @Nullable Runnable gifSave, @NotNull boolean closeIfNull, @NotNull boolean hideControls, boolean autostart) {
            this.c = c;
            this.video = video;
            this.progressBar = p;
            this.closeIfNull = closeIfNull;
            this.placeholder = placeholder;
            this.doOnClick = gifSave;
            this.hideControls = hideControls;
            this.autostart = autostart;
        }

        public AsyncLoadGif(@NotNull Activity c, @NotNull MediaVideoView video, @Nullable ProgressBar p, @Nullable View placeholder, @Nullable Runnable gifSave, @NotNull boolean closeIfNull, @NotNull boolean hideControls, boolean autostart, TextView size) {
            this.c = c;
            this.video = video;
            this.progressBar = p;
            this.closeIfNull = closeIfNull;
            this.placeholder = placeholder;
            this.doOnClick = gifSave;
            this.hideControls = hideControls;
            this.autostart = autostart;
            this.size = size;
        }
        public AsyncLoadGif(@NotNull Activity c, @NotNull MediaVideoView video, @Nullable ProgressBar p, @Nullable View placeholder, @NotNull boolean closeIfNull, @NotNull boolean hideControls, boolean autostart) {
            this.c = c;
            this.video = video;
            this.progressBar = p;
            this.closeIfNull = closeIfNull;
            this.placeholder = placeholder;
            this.hideControls = hideControls;
            this.autostart = autostart;
        }

        public void cancel(){
            LogUtil.v("cancelling");
            if(stream != null)
                try {
                    stream.close();
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        @Override
        public void onCancelled(){
            super.onCancelled();
            cancel();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        public void showGif(final URL url) {
            c.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final File downloaded = GifCache.getGif(url.toString());
                    LogUtil.v("Path is " + "file://" + downloaded);
                    video.setVideoPath("file://" + downloaded);
                    //videoView.set

                    if (placeholder != null && !hideControls && !(c instanceof Shadowbox)) {
                        MediaController mediaController = new
                                MediaController(c);
                        mediaController.setAnchorView(placeholder);
                        video.setMediaController(mediaController);

                    }
                    showProgressBar(c, progressBar, false);
                    if (gifSave != null) {
                        gifSave.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                saveGif(downloaded, c);
                            }
                        });
                    } else if (doOnClick != null) {
                        MediaView.doOnClick = new Runnable() {
                            @Override
                            public void run() {
                                saveGif(downloaded, c);

                            }
                        };
                    }


                    video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {

                            if (placeholder != null)
                                placeholder.setVisibility(View.GONE);
                            mp.setLooping(true);


                        }

                    });
                    if (autostart)

                        video.start();


                }
            });

        }

        public enum VideoType {
            IMGUR, VID_ME, STREAMABLE, GFYCAT, DIRECT, OTHER
        }


        public String formatUrl(String s) {
            if (s.endsWith("v")) {
                s = s.substring(0, s.length() - 1);
            } else if (s.contains("gfycat") && (!s.contains("mp4") && !s.contains("webm"))) {
                if (s.contains("-size_restricted"))
                    s = s.replace("-size_restricted", "");

            }
            if (s.contains(".gif") && !s.contains(".gifv") && s.contains("imgur.com")) {
                s = s.replace(".gif", ".mp4");
            }
            if (s.endsWith("/"))
                s = s.substring(0, s.length() - 1);

            return s;
        }

        public VideoType getVideoType(String url) {
            if (url.contains(".mp4") || url.contains("webm"))
                return VideoType.DIRECT;
            if (url.contains("gfycat") && !url.contains("mp4"))
                return VideoType.GFYCAT;
            if (url.contains("imgur.com"))
                return VideoType.IMGUR;
            if (url.contains("vid.me"))
                return VideoType.VID_ME;
            if (url.contains("streamable.com"))
                return VideoType.STREAMABLE;
            return VideoType.OTHER;
        }

        @Override
        protected Void doInBackground(String... sub) {
            MediaView.didLoadGif = false;
            OkHttpClient client = new OkHttpClient();
            Gson gson = new Gson();
            final String url = formatUrl(sub[0]);
            VideoType videoType = getVideoType(url);
            LogUtil.v(url + ", VideoType: " + videoType);
            switch (videoType) {
                case GFYCAT:
                    showProgressBar(c, progressBar, true);

                    try {
                        LogUtil.v("https://gfycat.com/cajax/get" + url.substring(url.lastIndexOf("/", url.length())));
                        final JsonObject result =
                                getJsonObject(client, gson, "https://gfycat.com/cajax/get" + url.substring(url.lastIndexOf("/", url.length())));
                        String obj = "";
                        if (result == null || result.get("gfyItem") == null || result.getAsJsonObject("gfyItem").get("mp4Url").isJsonNull()) {

                            if (closeIfNull) {
                                c.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        new AlertDialogWrapper.Builder(c)
                                                .setTitle(R.string.gif_err_title)
                                                .setMessage(R.string.gif_err_msg)
                                                .setCancelable(false)
                                                .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        c.finish();
                                                    }
                                                }).create().show();
                                    }
                                });
                            }


                        } else {
                            if (((!NetworkUtil.isConnectedWifi(c) && SettingValues.lowResMobile) || SettingValues.lowResAlways) && result.getAsJsonObject("gfyItem").has("mobileUrl"))
                                obj = result.getAsJsonObject("gfyItem").get("mobileUrl").getAsString();
                            else
                                obj = result.getAsJsonObject("gfyItem").get("mp4Url").getAsString();
                        }
                        showProgressBar(c, progressBar, false);
                        final URL finalUrl = new URL(obj);
                        writeGif(finalUrl, progressBar, c, AsyncLoadGif.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case DIRECT:
                case IMGUR:
                    try {
                        writeGif(new URL(url), progressBar, c, AsyncLoadGif.this);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    break;
                case STREAMABLE:
                    String hash = url.substring(url.lastIndexOf("/") + 1, url.length());
                    LogUtil.v("https://api.streamable.com/videos/" + hash);
                    try {
                        final JsonObject result =
                                getJsonObject(client, gson, "https://api.streamable.com/videos/" + hash);
                        String obj = "";
                        if (result == null || result.get("files") == null || !(result.getAsJsonObject("files").has("mp4") || result.getAsJsonObject("files").has("mp4-mobile"))) {

                            if (closeIfNull) {
                                c.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        new AlertDialogWrapper.Builder(c)
                                                .setTitle(R.string.error_video_not_found)
                                                .setMessage(R.string.error_video_message)
                                                .setCancelable(false)
                                                .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        c.finish();
                                                    }
                                                }).create().show();
                                    }
                                });
                            }
                        } else {
                            if (result.getAsJsonObject().get("files").getAsJsonObject().has("mp4"))
                                obj = "https:" + result.getAsJsonObject().get("files").getAsJsonObject().get("mp4").getAsJsonObject().get("url").getAsString();
                            else
                                obj = "https:" + result.getAsJsonObject().get("files").getAsJsonObject().get("mp4-mobile").getAsJsonObject().get("url").getAsString();

                        }
                        final URL finalUrl = new URL(obj);
                        writeGif(finalUrl, progressBar, c, AsyncLoadGif.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case VID_ME:
                    LogUtil.v("https://api.vid.me/videoByUrl?url=" + url);
                    try {
                        final JsonObject result = getJsonObject(client, gson, "https://api.vid.me/videoByUrl?url=" + url);
                        String obj = "";
                        if (result == null || result.isJsonNull() || !result.has("video") || result.get("video").isJsonNull() || !result.get("video").getAsJsonObject().has("complete_url") || result.get("video").getAsJsonObject().get("complete_url").isJsonNull()) {

                            if (closeIfNull) {
                                Intent web = new Intent(c, Website.class);
                                web.putExtra(Website.EXTRA_URL, url);
                                web.putExtra(Website.EXTRA_COLOR, Color.BLACK);
                                c.startActivity(web);
                                c.finish();
                            }
                        } else {
                            obj = result.getAsJsonObject().get("video").getAsJsonObject().get("complete_url").getAsString();
                        }
                        final URL finalUrl = new URL(obj);
                        writeGif(finalUrl, progressBar, c, AsyncLoadGif.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    break;

                case OTHER:
                    LogUtil.v("https://gfycat.com/cajax/checkUrl/" + Uri.encode(url));
                    try {
                        final JsonObject result =
                                getJsonObject(client, gson, "https://gfycat.com/cajax/checkUrl/" + Uri.encode(url));
                        if (result != null && result.has("urlKnown") && result.get("urlKnown").getAsBoolean()) {
                            final URL finalUrl = new URL(getSmallerGfy(result.get("mp4Url").getAsString()));
                            writeGif(finalUrl, progressBar, c, AsyncLoadGif.this);
                        } else {
                            LogUtil.v("https://upload.gfycat.com/transcode?fetchUrl=" + Uri.encode(url));
                            showProgressBar(c, progressBar, true);
                            final JsonObject transcodeResult =
                                    getJsonObject(client, gson, "https://upload.gfycat.com/transcode?fetchUrl=" + Uri.encode(url));

                            // Handle the transcode result
                            showProgressBar(c, progressBar, false);
                            if (transcodeResult == null || transcodeResult.get("mp4Url") == null || transcodeResult.get("mp4Url").isJsonNull()) {

                                if (transcodeResult != null && transcodeResult.has("error") && transcodeResult.get("error").getAsString().contains("not animated")) {
                                    if (c instanceof MediaView && c.getIntent() != null && c.getIntent().hasExtra(MediaView.EXTRA_DISPLAY_URL)) {
                                        c.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ((MediaView) c).imageShown = false;
                                                ((MediaView) c).displayImage(c.getIntent().getStringExtra(MediaView.EXTRA_DISPLAY_URL));
                                            }
                                        });
                                    } else if (c instanceof Shadowbox) {
                                        //todo maybe load in shadowbox
                                    }
                                } else {
                                    if (closeIfNull)
                                        c.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                AlertDialogWrapper.Builder b = new AlertDialogWrapper.Builder(c)
                                                        .setTitle(R.string.gif_err_title)
                                                        .setMessage("Converting the gif through Gfycat did not work.")
                                                        .setCancelable(false)
                                                        .setPositiveButton("Open in web", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                Intent i = new Intent(c, Website.class);
                                                                i.putExtra(Website.EXTRA_URL, url);
                                                                c.startActivity(i);
                                                                if (closeIfNull)
                                                                    c.finish();
                                                            }
                                                        });
                                                if (closeIfNull)
                                                    b.setNegativeButton(R.string.btn_close, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            c.finish();
                                                        }
                                                    });
                                                b.create().show();
                                            }
                                        });
                                }
                            } else {
                                final URL finalUrl = new URL(transcodeResult.get("mp4Url").getAsString()); //wont exist on server yet, just load the full version
                                writeGif(finalUrl, progressBar, c, AsyncLoadGif.this);
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
            return null;
        }

        ContentLengthInputStream stream;
        URLConnection ucon;
        InputStream is;
        public static String readableFileSize(long size) {
            if(size <= 0) return "0";
            final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
            int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
            return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
        }

        public void writeGif(final URL url, final ProgressBar progressBar, final Activity c, final AsyncLoadGif afterDone) {
            try {
                if (!GifCache.fileExists(url)) {
                    ucon = url.openConnection();
                    ucon.setReadTimeout(5000);
                    ucon.setConnectTimeout(10000);
                    is = ucon.getInputStream();
                    //todo  MediaView.fileLoc = f.getAbsolutePath();
                    if(size != null){
                        c.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                size.setText(readableFileSize(ucon.getContentLength()));
                            }
                        });
                    }
                    stream = new ContentLengthInputStream(new BufferedInputStream(is, 5 * 1024), ucon.getContentLength());
                    GifCache.writeGif(url.toString(), stream, new IoUtils.CopyListener() {
                        @Override
                        public boolean onBytesCopied(int current, int total) {
                            final int percent = Math.round(100.0f * current / total);

                            if(isCancelled()){
                                return false;
                            }

                            if (progressBar != null) {
                                c.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressBar.setProgress(percent);
                                        if (percent == 100) {
                                            progressBar.setVisibility(View.GONE);
                                            afterDone.showGif(url);
                                            if(size != null)
                                                size.setVisibility(View.GONE);
                                        }
                                    }
                                });
                            }
                            if (percent == 100) {
                                MediaView.didLoadGif = true;
                            }
                            return true;
                        }
                    });
                } else {
                    if (progressBar != null) {
                        c.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                                afterDone.showGif(url);
                            }
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void showErrorDialog(final Activity a) {
        new AlertDialogWrapper.Builder(a)
                .setTitle(R.string.err_something_wrong)
                .setMessage(R.string.err_couldnt_save_choose_new)
                .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new FolderChooserDialogCreate.Builder((MediaView) a)
                                .chooseButton(R.string.btn_select)  // changes label of the choose button
                                .initialPath(Environment.getExternalStorageDirectory().getPath())  // changes initial path, defaults to external storage directory
                                .show();
                    }
                })
                .setNegativeButton(R.string.btn_no, null)
                .show();
    }

    public static void showFirstDialog(final Activity a) {
        new AlertDialogWrapper.Builder(a)
                .setTitle(R.string.set_gif_save_loc)
                .setMessage(R.string.set_gif_save_loc_msg)
                .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new FolderChooserDialogCreate.Builder((MediaView) a)
                                .chooseButton(R.string.btn_select)  // changes label of the choose button
                                .initialPath(Environment.getExternalStorageDirectory().getPath())  // changes initial path, defaults to external storage directory
                                .show();
                    }
                })
                .setNegativeButton(R.string.btn_no, null)
                .show();
    }

    public static void saveGif(File from, Activity a) {
        if (Reddit.appRestart.getString("imagelocation", "").isEmpty()) {
            showFirstDialog(a);
        } else if (!new File(Reddit.appRestart.getString("imagelocation", "")).exists()) {
            showErrorDialog(a);
        } else {
            File f = new File(Reddit.appRestart.getString("imagelocation", "") + File.separator + UUID.randomUUID().toString() + ".mp4");

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
                showErrorDialog(a);
            } finally {
                try {
                    if (out != null) {
                        out.close();
                        doNotifGif(f.getAbsolutePath(), a);
                    }
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    showErrorDialog(a);
                }
            }
        }
    }

    public static void doNotifGif(String s, Activity c) {
        Intent mediaScanIntent = new Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.parse("file://" + s);
        mediaScanIntent.setData(contentUri);
        c.sendBroadcast(mediaScanIntent);


        final Intent shareIntent = new Intent(Intent.ACTION_VIEW);
        shareIntent.setDataAndType(Uri.parse(s), "video/*");
        PendingIntent contentIntent = PendingIntent.getActivity(c, 0, shareIntent, PendingIntent.FLAG_CANCEL_CURRENT);


        Notification notif = new NotificationCompat.Builder(c)
                .setContentTitle(c.getString(R.string.gif_saved))
                .setSmallIcon(R.drawable.savecontent)
                .setContentIntent(contentIntent)
                .build();

        NotificationManager mNotificationManager =
                (NotificationManager) c.getSystemService(Activity.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, notif);
    }

    /**
     * Shows a ProgressBar in the UI. If this method is called from a non-main thread, it will run
     * the UI code on the main thread
     *
     * @param activity        The activity context to use to display the ProgressBar
     * @param progressBar     The ProgressBar to display
     * @param isIndeterminate True to show an indeterminate ProgressBar, false otherwise
     */
    private static void showProgressBar(final Activity activity, final ProgressBar progressBar, final boolean isIndeterminate) {
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
     * Gets a JsonObject by calling apiUrl and parsing the JSON response String
     *
     * @param client The OkHTTP client to use to make the request
     * @param gson   The GSON instance to use to parse the response String
     * @param apiUrl The URL to call to get the response from
     * @return A JsonObject representation of the API response
     */
    private static JsonObject getJsonObject(final OkHttpClient client, final Gson gson, final String apiUrl) {
        if (client == null || gson == null || TextUtils.isEmpty(apiUrl)) return null;
        Request request = new Request.Builder()
                .url(apiUrl)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            String json = response.body().string();
            return gson.fromJson(json, JsonObject.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
