package me.ccrama.redditslide.util;

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
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nostra13.universalimageloader.core.assist.ContentLengthInputStream;
import com.nostra13.universalimageloader.utils.IoUtils;

import org.jetbrains.annotations.NotNull;

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

                    if (progressBar != null) {
                        progressBar.setIndeterminate(false);
                    }
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
                s = s.substring(3, s.length());
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

            final String url = formatUrl(sub[0]);
            switch (getVideoType(url)) {
                case GFYCAT:
                    c.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (progressBar != null)
                                progressBar.setIndeterminate(true);
                        }
                    });
                    Ion.with(c)
                            .load("http://gfycat.com/cajax/get" + url.substring(url.lastIndexOf("/", url.length())))
                            .asJsonObject()
                            .setCallback(new FutureCallback<JsonObject>() {
                                @Override
                                public void onCompleted(Exception e, final JsonObject result) {
                                    new AsyncTask<Void, Void, Void>() {
                                        @Override
                                        protected Void doInBackground(Void... params) {
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
                                            c.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (progressBar != null)
                                                        progressBar.setIndeterminate(false);
                                                }
                                            });
                                            try {
                                                final URL url = new URL(obj);
                                                writeGif(url, progressBar, c, AsyncLoadGif.this);
                                            } catch (Exception e2) {
                                                e2.printStackTrace();
                                            }
                                            return null;
                                        }
                                    }.execute();
                                }
                            });
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
                    Ion.with(c)
                            .load("https://api.streamable.com/videos/" + hash)
                            .asJsonObject()
                            .setCallback(new FutureCallback<JsonObject>() {
                                @Override
                                public void onCompleted(Exception e, final JsonObject result) {
                                    new AsyncTask<Void, Void, Void>() {

                                        @Override
                                        protected Void doInBackground(Void... params) {
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
                                            try {
                                                final URL url = new URL(obj);
                                                writeGif(url, progressBar, c, AsyncLoadGif.this);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            return null;
                                        }
                                    }.execute();
                                }
                            });


                    break;
                case VID_ME:
                    Ion.with(c)
                            .load("https://api.vid.me/videoByUrl?url=" + url)
                            .asJsonObject()
                            .setCallback(new FutureCallback<JsonObject>() {
                                @Override
                                public void onCompleted(Exception e, final JsonObject result) {
                                    new AsyncTask<Void, Void, Void>() {

                                        @Override
                                        protected Void doInBackground(Void... params) {
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
                                            try {
                                                final URL url = new URL(obj);
                                                writeGif(url, progressBar, c, AsyncLoadGif.this);
                                            } catch (
                                                    Exception e2
                                                    )

                                            {
                                                e2.printStackTrace();
                                            }

                                            return null;
                                        }


                                    }.execute();
                                }


                            });
                    break;

                case OTHER:
                    Ion.with(c).load("https://gfycat.com/cajax/checkUrl/" + Uri.encode(url))
                            .asJsonObject()
                            .setCallback(
                                    new FutureCallback<JsonObject>() {
                                        @Override
                                        public void onCompleted(Exception e, final JsonObject result) {
                                            if (result != null && result.has("urlKnown") && result.get("urlKnown").getAsBoolean()) {
                                                new AsyncTask<Void, Void, Void>() {

                                                    @Override
                                                    protected Void doInBackground(Void... params) {
                                                        try {
                                                            final URL url = new URL(getSmallerGfy(result.get("mp4Url").getAsString()));
                                                            writeGif(url, progressBar, c, AsyncLoadGif.this);
                                                        } catch (Exception ex) {
                                                            ex.printStackTrace();
                                                        }
                                                        return null;
                                                    }

                                                }.execute();
                                            } else {
                                                Log.v(LogUtil.getTag(), "https://upload.gfycat.com/transcode?fetchUrl=" + Uri.encode(url));
                                                if (progressBar != null)
                                                    progressBar.setIndeterminate(true);
                                                Ion.with(c)
                                                        .load("http://upload.gfycat.com/transcode?fetchUrl=" + Uri.encode(url))
                                                        .asJsonObject()
                                                        .setCallback(new FutureCallback<JsonObject>() {
                                                                         @Override
                                                                         public void onCompleted(Exception e, final JsonObject result) {
                                                                             if (progressBar != null)
                                                                                 progressBar.setIndeterminate(false);
                                                                             new AsyncTask<Void, Void, Void>() {

                                                                                 @Override
                                                                                 protected Void doInBackground(Void... params) {
                                                                                     try {
                                                                                         if (result == null || result.get("mp4Url") == null || result.get("mp4Url").isJsonNull()) {

                                                                                             if (result != null && result.has("error") && result.get("error").getAsString().contains("not animated")) {
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
                                                                                                                             if(closeIfNull)
                                                                                                                                 c.finish();
                                                                                                                         }
                                                                                                                     });
                                                                                                             if(closeIfNull)
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
                                                                                             final URL url = new URL(result.get("mp4Url").getAsString()); //wont exist on server yet, just load the full version
                                                                                             writeGif(url, progressBar, c, AsyncLoadGif.this);
                                                                                         }
                                                                                     } catch (Exception e3) {
                                                                                         e3.printStackTrace();
                                                                                     }
                                                                                     return null;
                                                                                 }
                                                                             }.execute();
                                                                         }
                                                                     }
                                                        );
                                            }
                                        }
                                    }
                            );
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
}