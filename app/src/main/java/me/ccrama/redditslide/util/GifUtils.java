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
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import me.ccrama.redditslide.ImageLoaderUtils;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Views.MediaVideoView;

/**
 * Created by carlo_000 on 1/29/2016.
 */
public class GifUtils {

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

        public AsyncLoadGif(@NotNull Activity c, @NotNull MediaVideoView video, @Nullable ProgressBar p, @Nullable View placeholder, @Nullable View gifSave, @NotNull boolean closeIfNull) {
            this.c = c;
            this.video = video;
            this.progressBar = p;
            this.closeIfNull = closeIfNull;
            this.placeholder = placeholder;
            this.gifSave = gifSave;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }


        @Override
        protected Void doInBackground(String... sub) {

            String s = sub[0];
            if (s.contains("gfycat")) {
                s = sub[0].substring(sub[0].lastIndexOf("/"), sub[0].length());


                Log.v("Slide", "http://gfycat.com/cajax/get" + s);
                Ion.with(c)
                        .load("http://gfycat.com/cajax/get" + s)
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
                                            obj = result.getAsJsonObject("gfyItem").get("mp4Url").getAsString();

                                        }
                                        try {
                                            final URL url = new URL(obj);
                                            final File f = new File(ImageLoaderUtils.getCacheDirectory(c).getAbsolutePath() + File.separator + url.toString().replaceAll("[^a-zA-Z0-9]", "") + ".mp4");


                                            if (!f.exists()) {
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
                                                int readBytes = 0;
                                                while ((len = inStream.read(buff)) != -1) {
                                                    outStream.write(buff, 0, len);
                                                    Log.v("Slide", f.length() + " OVER " + length);
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
                                                    if (placeholder != null) {

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

                                                                                           File to = new File(Environment.DIRECTORY_DOWNLOADS + File.separator + f.getName());
                                                                                           f.renameTo(to);

                                                                                           Intent intent = new Intent();
                                                                                           intent.setAction(android.content.Intent.ACTION_VIEW);

                                                                                           intent.setData(Uri.parse(to.getAbsolutePath()));
                                                                                           intent.setData(Uri.parse(to.getAbsolutePath()));
                                                                                           Intent newI = Intent.createChooser(intent, "Open Video");
                                                                                           PendingIntent contentIntent = PendingIntent.getActivity(c, 0, newI, PendingIntent.FLAG_CANCEL_CURRENT);

                                                                                           Notification notif = new NotificationCompat.Builder(c)
                                                                                                   .setContentTitle("Gif saved to Downloads")
                                                                                                   .setSmallIcon(R.drawable.notif)
                                                                                                   .setContentIntent(contentIntent)
                                                                                                   .build();


                                                                                           NotificationManager mNotificationManager =
                                                                                                   (NotificationManager) c.getSystemService(Activity.NOTIFICATION_SERVICE);
                                                                                           mNotificationManager.notify(1, notif);

                                                                                       }
                                                                                   }

                                                        );
                                                    }


                                                    video.start();
                                                    video.setOnPreparedListener(new MediaPlayer.OnPreparedListener()

                                                                                {
                                                                                    @Override
                                                                                    public void onPrepared(MediaPlayer mp) {
                                                                                        if (placeholder != null) {

                                                                                            placeholder.setVisibility(View.GONE);
                                                                                        }
                                                                                        mp.setLooping(true);


                                                                                    }

                                                                                }

                                                    );

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


                                }

                                        .

                                                execute();
                            }


                        });

            } else

            {
                if (s.endsWith("v")) {
                    s = s.substring(0, s.length() - 1);
                }
                s = s.trim();

                final String finalS = s;
                Log.v("Slide", "http://gfycat.com/cajax/checkUrl/" + s);

                Ion.with(c).load("http://gfycat.com/cajax/checkUrl/" + s).asJsonObject().setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, final JsonObject result) {
                        if (result != null && result.has("urlKnown") && result.get("urlKnown").getAsBoolean()) {

                            new AsyncTask<Void, Void, Void>() {

                                @Override
                                protected Void doInBackground(Void... params) {
                                    try {

                                        final URL url = new URL(getSmallerGfy(result.get("mp4Url").getAsString()));
                                        final File f = new File(ImageLoaderUtils.getCacheDirectory(c).getAbsolutePath() + File.separator + url.toString().replaceAll("[^a-zA-Z0-9]", "") + ".mp4");


                                        if (!f.exists()) {
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

                                                if (placeholder != null) {
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

                                                            File to = new File(Environment.DIRECTORY_DOWNLOADS + File.separator + f.getName());
                                                            f.renameTo(to);

                                                            Intent intent = new Intent();
                                                            intent.setAction(android.content.Intent.ACTION_VIEW);

                                                            intent.setData(Uri.parse(to.getAbsolutePath()));
                                                            intent.setData(Uri.parse(to.getAbsolutePath()));
                                                            Intent newI = Intent.createChooser(intent, "Open Video");
                                                            PendingIntent contentIntent = PendingIntent.getActivity(c, 0, newI, PendingIntent.FLAG_CANCEL_CURRENT);

                                                            Notification notif = new NotificationCompat.Builder(c)
                                                                    .setContentTitle("Gif saved to Downloads")
                                                                    .setSmallIcon(R.drawable.notif)
                                                                    .setContentIntent(contentIntent)
                                                                    .build();


                                                            NotificationManager mNotificationManager =
                                                                    (NotificationManager) c.getSystemService(Activity.NOTIFICATION_SERVICE);
                                                            mNotificationManager.notify(1, notif);

                                                        }
                                                    });
                                                }


                                                video.start();
                                                video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                                    @Override
                                                    public void onPrepared(MediaPlayer mp) {

                                                        if (placeholder != null)
                                                            placeholder.setVisibility(View.GONE);
                                                        mp.setLooping(true);


                                                    }

                                                });

                                            }
                                        });


                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                    return null;
                                }

                                ;
                            }.execute();


                        } else {

                            Ion.with(c)
                                    .load("http://upload.gfycat.com/transcode?fetchUrl=" + finalS)
                                    .asJsonObject()
                                    .setCallback(new FutureCallback<JsonObject>() {
                                        @Override
                                        public void onCompleted(Exception e, final JsonObject result) {

                                            try {

                                                if (result == null || result.get("mp4Url") == null || result.get("mp4Url").isJsonNull()) {

                                                    if (closeIfNull)
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
                                                } else {
                                                    final URL url = new URL(getSmallerGfy(result.get("mp4Url").getAsString()));
                                                    URLConnection ucon = url.openConnection();
                                                    ucon.setReadTimeout(5000);
                                                    ucon.setConnectTimeout(10000);
                                                    InputStream is = ucon.getInputStream();
                                                    BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 5);

                                                    int length = ucon.getContentLength();

                                                    final File f = new File(ImageLoaderUtils.getCacheDirectory(c).getAbsolutePath() + File.separator + url.toString().replaceAll("[^a-zA-Z0-9]", "") + ".mp4");

                                                    f.createNewFile();

                                                    FileOutputStream outStream = new FileOutputStream(f);
                                                    byte[] buff = new byte[5 * 1024];

                                                    int len;
                                                    while ((len = inStream.read(buff)) != -1) {
                                                        outStream.write(buff, 0, len);
                                                        int percent = Math.round(100.0f * f.length() / length);
                                                        if (progressBar != null) {

                                                            progressBar.setProgress(percent);
                                                            if (percent == 100) {
                                                                progressBar.setVisibility(View.GONE);

                                                            }
                                                        }
                                                    }


                                                    outStream.flush();
                                                    outStream.close();
                                                    inStream.close();

                                                    c.runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            video.setVideoPath(f.getAbsolutePath());
                                                            //videoView.set

                                                            if (placeholder != null) {
                                                                MediaController mediaController = new
                                                                        MediaController(c);
                                                                mediaController.setAnchorView(placeholder);
                                                                video.setMediaController(mediaController);
                                                            }
                                                            if (progressBar != null)
                                                                progressBar.setIndeterminate(false);
                                                            if (gifSave != null) {
                                                                gifSave.setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View v) {

                                                                        File to = new File(Environment.DIRECTORY_DOWNLOADS + File.separator + f.getName());
                                                                        f.renameTo(to);

                                                                        Intent intent = new Intent();
                                                                        intent.setAction(android.content.Intent.ACTION_VIEW);

                                                                        intent.setData(Uri.parse(to.getAbsolutePath()));
                                                                        Intent newI = Intent.createChooser(intent, "Open Video");
                                                                        PendingIntent contentIntent = PendingIntent.getActivity(c, 0, newI, PendingIntent.FLAG_CANCEL_CURRENT);


                                                                        Notification notif = new NotificationCompat.Builder(c)
                                                                                .setContentTitle("Gif saved to Downloads")
                                                                                .setSmallIcon(R.drawable.notif)
                                                                                .setContentIntent(contentIntent)
                                                                                .build();


                                                                        NotificationManager mNotificationManager =
                                                                                (NotificationManager) c.getSystemService(Activity.NOTIFICATION_SERVICE);
                                                                        mNotificationManager.notify(1, notif);

                                                                    }
                                                                });
                                                            }


                                                            video.start();
                                                            video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                                                @Override
                                                                public void onPrepared(MediaPlayer mp) {

                                                                    if (placeholder != null)
                                                                        placeholder.setVisibility(View.GONE);
                                                                    mp.setLooping(true);


                                                                }

                                                            });

                                                        }
                                                    });
                                                }
                                            } catch (Exception e3) {
                                                e3.printStackTrace();
                                            }
                                        }
                                    });
                        }
                    }
                });
            }

            return null;

        }


    }

}
