package me.ccrama.redditslide.util;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

import me.ccrama.redditslide.Activities.GifView;
import me.ccrama.redditslide.ImageLoaderUtils;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Views.MediaVideoView;

/**
 * Created by carlo_000 on 1/29/2016.
 */
public class StreamableUtil {


    public static class AsyncLoadStreamable extends AsyncTask<String, Void, Void> {

        public Activity c;
        public MediaVideoView video;
        public ProgressBar progressBar;
        public View placeholder;
        public View gifSave;
        public boolean closeIfNull;
        public boolean hideControls;

        public AsyncLoadStreamable(@NotNull Activity c, @NotNull MediaVideoView video, @Nullable ProgressBar p, @Nullable View placeholder, @Nullable View gifSave, @NotNull boolean closeIfNull, @NotNull boolean hideControls) {
            this.c = c;
            this.video = video;
            this.progressBar = p;
            this.closeIfNull = closeIfNull;
            this.placeholder = placeholder;
            this.gifSave = gifSave;
            this.hideControls = hideControls;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }


        @Override
        protected Void doInBackground(String... sub) {

            String s = sub[0];

            if (s.endsWith("/")) {
                s = s.substring(0, s.length() - 1);
            }
            String hash = s.substring(s.lastIndexOf("/") + 1, s.length());
            LogUtil.v("Loading " + "https://api.streamable.com/videos/" + hash);
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
                                        final File f = new File(ImageLoaderUtils.getCacheDirectoryGif(c).getAbsolutePath() + File.separator + url.toString().replaceAll("[^a-zA-Z0-9]", "") + ".mp4");


                                        if (!f.exists()) {
                                            URLConnection ucon = url.openConnection();
                                            ucon.setReadTimeout(5000);
                                            ucon.setConnectTimeout(10000);
                                            InputStream is = ucon.getInputStream();
                                            BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 5);

                                            int length = ucon.getContentLength();

                                            CacheUtil.makeRoom(c, length);

                                            f.createNewFile();

                                            FileOutputStream outStream = new FileOutputStream(f);
                                            byte[] buff = new byte[5 * 1024];

                                            int len;
                                            int readBytes = 0;
                                            while ((len = inStream.read(buff)) != -1) {
                                                outStream.write(buff, 0, len);
                                                final int percent = Math.round(100.0f * f.length() / length);
                                                if (progressBar != null) {
                                                    c.runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            progressBar.setProgress(percent);
                                                            if (percent == 100) {
                                                                progressBar.setVisibility(View.GONE);
                                                            }
                                                        }
                                                    });
                                                }

                                            }


                                            outStream.flush();
                                            outStream.close();
                                            inStream.close();
                                        } else {
                                            if (progressBar != null) {

                                                c.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        progressBar.setVisibility(View.GONE);
                                                    }
                                                });
                                            }
                                        }

                                        c.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                video.setVideoPath(f.getAbsolutePath());
                                                //videoView.set
                                                if (placeholder != null && !hideControls) {

                                                    MediaController mediaController = new
                                                            MediaController(c);
                                                    mediaController.setAnchorView(placeholder);
                                                    video.setMediaController(mediaController);
                                                }

                                                if (progressBar != null) {
                                                    progressBar.setIndeterminate(false);
                                                }

                                                if (gifSave != null) {
                                                    gifSave.setOnClickListener(
                                                            new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    saveGif(f, c);

                                                                }
                                                            }

                                                    );
                                                }


                                                video.setOnPreparedListener(new MediaPlayer.OnPreparedListener()

                                                                            {
                                                                                @Override
                                                                                public void onPrepared(MediaPlayer mp) {
                                                                                    if (placeholder != null)

                                                                                        placeholder.setVisibility(View.GONE);

                                                                                    mp.setLooping(true);


                                                                                }

                                                                            }

                                                );
                                                video.start();


                                            }
                                        });
                                    } catch (
                                            Exception e2
                                            )

                                    {
                                        e2.printStackTrace();
                                    }

                                    return null;
                                }

                                ;


                            }.execute();
                        }


                    });


            return null;

        }


    }

    public static void showErrorDialog(final Activity a) {
        new AlertDialogWrapper.Builder(a)
                .setTitle("Uh oh, something went wrong.")
                .setMessage("Slide couldn't save to the selected directory. Would you like to choose a new save location?")
                .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new FolderChooserDialog.Builder((GifView) a)
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
                .setTitle("Set video save location")
                .setMessage("Slide's video save location has not been set yet. Would you like to set this now?")
                .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new FolderChooserDialog.Builder((GifView) a)
                                .chooseButton(R.string.btn_select)  // changes label of the choose button
                                .initialPath(Environment.getExternalStorageDirectory().getPath())  // changes initial path, defaults to external storage directory
                                .show();
                    }
                })
                .setNegativeButton(R.string.btn_no, null)
                .show();
    }

    public static void saveGif(File from, Activity a) {
        if (Reddit.appRestart.getString("giflocation", "").isEmpty()) {
            showFirstDialog(a);
        } else if (!new File(Reddit.appRestart.getString("giflocation", "")).exists()) {
            showErrorDialog(a);
        } else {
            File f = new File(Reddit.appRestart.getString("giflocation", "") + File.separator + UUID.randomUUID().toString() + ".mp4");


            FileOutputStream out = null;
            try {
                InputStream in = new FileInputStream(from);
                out = new FileOutputStream(f);

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
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
                } catch (IOException e) {
                    e.printStackTrace();
                    showErrorDialog(a);
                }
            }
        }
    }

    public static void doNotifGif(String s, Activity c) {
        final Intent shareIntent = new Intent(Intent.ACTION_VIEW);
        shareIntent.setDataAndType(Uri.parse(s), "video/*");
        PendingIntent contentIntent = PendingIntent.getActivity(c, 0, shareIntent, PendingIntent.FLAG_CANCEL_CURRENT);


        Notification notif = new NotificationCompat.Builder(c)
                .setContentTitle("Video saved")
                .setSmallIcon(R.drawable.notif)
                .setContentIntent(contentIntent)
                .build();


        NotificationManager mNotificationManager =
                (NotificationManager) c.getSystemService(Activity.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, notif);
    }
}
