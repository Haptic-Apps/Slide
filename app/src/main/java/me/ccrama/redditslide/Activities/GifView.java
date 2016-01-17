package me.ccrama.redditslide.Activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.ImageLoaderUtils;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Views.MediaVideoView;


/**
 * Created by ccrama on 3/5/2015.
 */
public class GifView extends FullScreenActivity {

    public ProgressBar loader;
    SharedPreferences prefs;

    public static String getSmallerGfy(String gfyUrl) {
        gfyUrl = gfyUrl.replaceAll("fat|zippy|giant", "thumbs");
        if (!gfyUrl.endsWith("-mobile.mp4"))
            gfyUrl = gfyUrl.replaceAll("\\.mp4", "-mobile.mp4");
        return gfyUrl;
    }

    /**
     * Called when the activity is first created.
     */
    public void onCreate(Bundle savedInstanceState) {
        overrideRedditSwipeAnywhere();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gif);

        final MediaVideoView v = (MediaVideoView) findViewById(R.id.gif);
        v.clearFocus();


        String dat = getIntent().getExtras().getString("url");

        findViewById(R.id.exitComment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GifView.this.finish();
            }
        });

        if(dat.contains("webm") && dat.contains("imgur")){
            dat = dat.replace("webm", "gifv");
        }

            if (dat.endsWith("v")) {
                dat = dat.substring(0, dat.length() - 1);
            } else if (dat.contains("gfycat")) {
                dat = dat.substring(3, dat.length());
            }
            new AsyncImageLoader().execute(dat);



        prefs = getSharedPreferences("DATA", 0);

        loader = (ProgressBar) findViewById(R.id.gifprogress);

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.fade_out);
    }

    public class AsyncImageLoader extends AsyncTask<String, Void, Void> {


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
                Ion.with(GifView.this)
                        .load("http://gfycat.com/cajax/get" + s)
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, final JsonObject result) {
                                new AsyncTask<Void, Void, Void>() {

                                    @Override
                                    protected Void doInBackground(Void... params) {
                                        final MediaVideoView videoView = (MediaVideoView) findViewById(R.id.gif);
                                        String obj = "";
                                        if (result == null || result.get("gfyItem") == null || result.getAsJsonObject("gfyItem").get("mp4Url").isJsonNull()) {

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    new AlertDialogWrapper.Builder(GifView.this)
                                                            .setTitle(R.string.gif_err_title)
                                                            .setMessage(R.string.gif_err_msg)
                                                            .setCancelable(false)
                                                            .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    finish();
                                                                }
                                                            }).create().show();
                                                }
                                            });


                                        } else {
                                            obj = result.getAsJsonObject("gfyItem").get("mp4Url").getAsString();

                                        }
                                        try {
                                            final URL url = new URL(obj);
                                            final File f = new File(ImageLoaderUtils.getCacheDirectory(GifView.this).getAbsolutePath() + File.separator + url.toString().replaceAll("[^a-zA-Z0-9]", "") + ".mp4");


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
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            loader.setProgress(percent);
                                                            if (percent == 100) {
                                                                loader.setVisibility(View.GONE);

                                                            }
                                                        }
                                                    });

                                                }


                                                outStream.flush();
                                                outStream.close();
                                                inStream.close();
                                            } else {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {

                                                        loader.setVisibility(View.GONE);

                                                    }
                                                });
                                            }

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    videoView.setVideoPath(f.getAbsolutePath());
                                                    //videoView.set

                                                    MediaController mediaController = new
                                                            MediaController(GifView.this);
                                                    mediaController.setAnchorView(findViewById(R.id.placeholder));
                                                    videoView.setMediaController(mediaController);

                                                    loader.setIndeterminate(false);
                                                    findViewById(R.id.gifsave).setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {

                                                            File to = new File(Environment.DIRECTORY_DOWNLOADS + File.separator + f.getName());
                                                            f.renameTo(to);

                                                            Intent intent = new Intent();
                                                            intent.setAction(android.content.Intent.ACTION_VIEW);

                                                            intent.setData(Uri.parse(to.getAbsolutePath()));
                                                            intent.setData(Uri.parse(to.getAbsolutePath()));
                                                            Intent newI = Intent.createChooser(intent, "Open Video");
                                                            PendingIntent contentIntent = PendingIntent.getActivity(GifView.this, 0, newI, PendingIntent.FLAG_CANCEL_CURRENT);

                                                            Notification notif = new NotificationCompat.Builder(GifView.this)
                                                                    .setContentTitle("Gif saved to Downloads")
                                                                    .setSmallIcon(R.drawable.notif)
                                                                    .setContentIntent(contentIntent)
                                                                    .build();


                                                            NotificationManager mNotificationManager =
                                                                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                                            mNotificationManager.notify(1, notif);

                                                        }
                                                    });


                                                    videoView.start();
                                                    videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                                        @Override
                                                        public void onPrepared(MediaPlayer mp) {
                                                            View placeholder = findViewById(R.id.placeholder);

                                                            placeholder.setVisibility(View.GONE);
                                                            mp.setLooping(true);


                                                        }

                                                    });

                                                }
                                            });
                                        } catch (Exception e2) {
                                            e2.printStackTrace();
                                        }
                                        return null;
                                    };


                                }.execute();
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

                Ion.with(GifView.this).load("http://gfycat.com/cajax/checkUrl/" + s).asJsonObject().setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, final JsonObject result) {
                        if (result != null && result.has("urlKnown") && result.get("urlKnown").getAsBoolean()) {
                            final MediaVideoView videoView =
                                    (MediaVideoView) findViewById(R.id.gif);
                            new AsyncTask<Void, Void, Void>() {

                                @Override
                                protected Void doInBackground(Void... params) {
                                    try {

                                        final URL url = new URL(getSmallerGfy(result.get("mp4Url").getAsString()));
                                        final File f = new File(ImageLoaderUtils.getCacheDirectory(GifView.this).getAbsolutePath() + File.separator + url.toString().replaceAll("[^a-zA-Z0-9]", "") + ".mp4");


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
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        loader.setProgress(percent);
                                                        if (percent == 100) {
                                                            loader.setVisibility(View.GONE);

                                                        }
                                                    }
                                                });

                                            }


                                            outStream.flush();
                                            outStream.close();
                                            inStream.close();
                                        } else {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {

                                                    loader.setVisibility(View.GONE);

                                                }
                                            });
                                        }
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                videoView.setVideoPath(f.getAbsolutePath());
                                                //videoView.set

                                                MediaController mediaController = new
                                                        MediaController(GifView.this);
                                                mediaController.setAnchorView(findViewById(R.id.placeholder));
                                                videoView.setMediaController(mediaController);

                                                loader.setIndeterminate(false);
                                                findViewById(R.id.gifsave).setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {

                                                        File to = new File(Environment.DIRECTORY_DOWNLOADS + File.separator + f.getName());
                                                        f.renameTo(to);

                                                        Intent intent = new Intent();
                                                        intent.setAction(android.content.Intent.ACTION_VIEW);

                                                        intent.setData(Uri.parse(to.getAbsolutePath()));
                                                        intent.setData(Uri.parse(to.getAbsolutePath()));
                                                        Intent newI = Intent.createChooser(intent, "Open Video");
                                                        PendingIntent contentIntent = PendingIntent.getActivity(GifView.this, 0, newI, PendingIntent.FLAG_CANCEL_CURRENT);

                                                        Notification notif = new NotificationCompat.Builder(GifView.this)
                                                                .setContentTitle("Gif saved to Downloads")
                                                                .setSmallIcon(R.drawable.notif)
                                                                .setContentIntent(contentIntent)
                                                                .build();


                                                        NotificationManager mNotificationManager =
                                                                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                                        mNotificationManager.notify(1, notif);

                                                    }
                                                });


                                                videoView.start();
                                                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                                    @Override
                                                    public void onPrepared(MediaPlayer mp) {
                                                        View placeholder = findViewById(R.id.placeholder);

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

                            Ion.with(GifView.this)
                                    .load("http://upload.gfycat.com/transcode?fetchUrl=" + finalS)
                                    .asJsonObject()
                                    .setCallback(new FutureCallback<JsonObject>() {
                                        @Override
                                        public void onCompleted(Exception e, final JsonObject result) {

                                            try {
                                                final MediaVideoView videoView =
                                                        (MediaVideoView) findViewById(R.id.gif);

                                                if (result == null || result.get("mp4Url") == null || result.get("mp4Url").isJsonNull()) {

                                                    new AlertDialogWrapper.Builder(GifView.this)
                                                            .setTitle(R.string.gif_err_title)
                                                            .setMessage(R.string.gif_err_msg)
                                                            .setCancelable(false)
                                                            .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    finish();
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

                                                    final File f = new File(ImageLoaderUtils.getCacheDirectory(GifView.this).getAbsolutePath() + File.separator + url.toString().replaceAll("[^a-zA-Z0-9]", "") + ".mp4");

                                                    f.createNewFile();

                                                    FileOutputStream outStream = new FileOutputStream(f);
                                                    byte[] buff = new byte[5 * 1024];

                                                    int len;
                                                    while ((len = inStream.read(buff)) != -1) {
                                                        outStream.write(buff, 0, len);
                                                        int percent = Math.round(100.0f * f.length() / length);
                                                        loader.setProgress(percent);
                                                        if (percent == 100) {
                                                            loader.setVisibility(View.GONE);

                                                        }
                                                    }


                                                    outStream.flush();
                                                    outStream.close();
                                                    inStream.close();

                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            videoView.setVideoPath(f.getAbsolutePath());
                                                            //videoView.set

                                                            MediaController mediaController = new
                                                                    MediaController(GifView.this);
                                                            mediaController.setAnchorView(findViewById(R.id.placeholder));
                                                            videoView.setMediaController(mediaController);

                                                            loader.setIndeterminate(false);
                                                            findViewById(R.id.gifsave).setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {

                                                                    File to = new File(Environment.DIRECTORY_DOWNLOADS + File.separator + f.getName());
                                                                    f.renameTo(to);

                                                                    Intent intent = new Intent();
                                                                    intent.setAction(android.content.Intent.ACTION_VIEW);

                                                                    intent.setData(Uri.parse(to.getAbsolutePath()));
                                                                    Intent newI = Intent.createChooser(intent, "Open Video");
                                                                    PendingIntent contentIntent = PendingIntent.getActivity(GifView.this, 0, newI, PendingIntent.FLAG_CANCEL_CURRENT);


                                                                    Notification notif = new NotificationCompat.Builder(GifView.this)
                                                                            .setContentTitle("Gif saved to Downloads")
                                                                            .setSmallIcon(R.drawable.notif)
                                                                            .setContentIntent(contentIntent)
                                                                            .build();


                                                                    NotificationManager mNotificationManager =
                                                                            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                                                    mNotificationManager.notify(1, notif);

                                                                }
                                                            });



                                                            videoView.start();
                                                            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                                                @Override
                                                                public void onPrepared(MediaPlayer mp) {
                                                                    View placeholder = findViewById(R.id.placeholder);

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

    public static class AsyncSaveToFile extends AsyncTask<String, Void, Void> {

        Context c;

        public AsyncSaveToFile(Context c) {
            this.c = c;
        }

        @Override
        protected Void doInBackground(String... params) {
            String s = params[0];
            if (s.contains("gfycat")) {
                s = params[0].substring(params[0].lastIndexOf("/"), params[0].length());


                Log.v("Slide", "http://gfycat.com/cajax/get" + s);
                Ion.with(c)
                        .load("http://gfycat.com/cajax/get" + s)
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, final JsonObject result) {

                                String obj = "";
                                if (result != null && result.get("gfyItem") != null && !result.getAsJsonObject("gfyItem").get("mp4Url").isJsonNull()) {
                                    obj = result.getAsJsonObject("gfyItem").get("mp4Url").getAsString();

                                }
                                try {
                                    URL url = new URL(obj);
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


                                        }


                                        outStream.flush();
                                        outStream.close();
                                        inStream.close();
                                    }
                                } catch(Exception e2){
                                    e2.printStackTrace();
                                }
                            }
                        });
            }else
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

                                    URL url = new URL(getSmallerGfy(result.get("mp4Url").getAsString()));
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


                                        }


                                        outStream.flush();
                                        outStream.close();
                                        inStream.close();
                                    }


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


                                            URL url = new URL(getSmallerGfy(result.get("mp4Url").getAsString()));
                                            URLConnection ucon = url.openConnection();
                                            ucon.setReadTimeout(5000);
                                            ucon.setConnectTimeout(10000);
                                            InputStream is = ucon.getInputStream();
                                            BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 5);


                                            final File f = new File(ImageLoaderUtils.getCacheDirectory(c).getAbsolutePath() + File.separator + "lastgif.mp4");

                                            f.createNewFile();

                                            FileOutputStream outStream = new FileOutputStream(f);
                                            byte[] buff = new byte[5 * 1024];

                                            int len;
                                            while ((len = inStream.read(buff)) != -1) {
                                                outStream.write(buff, 0, len);

                                            }


                                            outStream.flush();
                                            outStream.close();
                                            inStream.close();


                                        } catch (Exception e3) {
                                            e3.printStackTrace();
                                        }
                                    }
                                });
                    }
                }
            });

            return null;

        }

    }

    private class AsyncGyfcat extends AsyncTask<String, Void, Void> {


        @Override
        protected Void doInBackground(String... sub) {

            String s = sub[0].substring(sub[0].lastIndexOf("/"), sub[0].length());


            Log.v("Slide", "http://gfycat.com/cajax/get" + s);
            Ion.with(GifView.this)
                    .load("http://gfycat.com/cajax/get" + s)
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, final JsonObject result) {

                            final MediaVideoView v = (MediaVideoView) findViewById(R.id.gif);
                            String obj = "";
                            if (result == null || result.get("gfyItem") == null || result.getAsJsonObject("gfyItem").get("mp4Url").isJsonNull()) {

                                new AlertDialogWrapper.Builder(GifView.this)
                                        .setTitle(R.string.gif_err_title)
                                        .setMessage(R.string.gif_err_msg)
                                        .setCancelable(false)
                                        .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                finish();
                                            }
                                        }).create().show();

                            } else {
                                obj = result.getAsJsonObject("gfyItem").get("mp4Url").getAsString();

                            }

                            try {
                                MediaController mediacontroller = new MediaController(GifView.this);
                                mediacontroller.setAnchorView(v);

                                mediacontroller.setKeepScreenOn(true);

                                Uri videoUri = Uri.parse(obj);
                                v.setMediaController(mediacontroller);
                                v.setVideoURI(videoUri);

                                v.start();


                            } catch (Exception ex) {

                                ex.printStackTrace();
                            }

                            v.requestFocus();
                            findViewById(R.id.gifsave).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + File.separator + "Slide"); //Creates app specific folder
                                    path.mkdirs();
                                    File imageFile = new File(path, DataShare.sharedSubmission.getId() + ".mp4"); // Imagename.png
                                    new SaveGifAsync().execute(result.get("mp4Url").getAsString(), imageFile.getAbsolutePath());

                                }
                            });
                            v.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    View placeholder = findViewById(R.id.placeholder);

                                    placeholder.setVisibility(View.GONE);
                                    mp.setLooping(true);

                                    mp.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                                        @Override
                                        public void onBufferingUpdate(MediaPlayer mp, int percent) {
                                            loader.setProgress(percent);

                                            if (percent == 100) {
                                                loader.setVisibility(View.GONE);
                                                if (result != null && result.has("extraLemmaText") && !result.getAsJsonObject("extraLemmaText").isJsonNull()) {

                                                    String s = result.getAsJsonObject("extraLemmaText").getAsString();
                                                    String extra = "";
                                                    if (s.length() > 19) {
                                                        extra = "...";
                                                    }
                                                } else {
                                                }

                                            }
                                        }
                                    });

                                }
                            });


                        }
                    });

            return null;

        }


    }

    public class SaveGifAsync extends AsyncTask<String, Void, File> {

        @Override
        protected File doInBackground(String... params) {

            try {
                URL url = new URL(params[0]);
                URLConnection ucon = url.openConnection();
                ucon.setReadTimeout(5000);
                ucon.setConnectTimeout(10000);
                InputStream is = ucon.getInputStream();
                BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 5);


                File f = new File(params[1]);
                f.createNewFile();

                FileOutputStream outStream = new FileOutputStream(f);
                byte[] buff = new byte[5 * 1024];

                int len;
                while ((len = inStream.read(buff)) != -1) {
                    outStream.write(buff, 0, len);
                }

                outStream.flush();
                outStream.close();
                inStream.close();
                Notification.Builder notif = new Notification.Builder(GifView.this)
                        .setContentTitle(getString(R.string.gif_saved))
                        .setSmallIcon(R.drawable.notif);


                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                mNotificationManager.notify(1, notif.build());
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    class Meta {
        public String num;
        public String type;
        public String ext;

        Meta(String num, String ext, String type) {
            this.num = num;
            this.ext = ext;
            this.type = type;
        }
    }

    class Video {
        public String ext = "";
        public String type = "";
        public String url = "";

        Video(String ext, String type, String url) {
            this.ext = ext;
            this.type = type;
            this.url = url;
        }
    }


}