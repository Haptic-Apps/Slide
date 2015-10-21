package me.ccrama.redditslide.Activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Views.MediaVideoView;


/**
 * Created by ccrama on 3/5/2015.
 */
public class GifView extends BaseActivity {

    public ProgressBar loader;
    SharedPreferences prefs;

    /**
     * Called when the activity is first created.
     */
    public void onCreate(Bundle savedInstanceState) {


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

        if (dat.contains("gfy")) {
            findViewById(R.id.gifsave).setVisibility(View.GONE);
            new AsyncGyfcat().execute(dat.substring(3, dat.length()));
        } else {
            if (dat.endsWith("v")) {
                dat = dat.substring(0, dat.length() - 1);
            }
            final String finalDat1 = dat;

            new AsyncImageLoader().execute(dat);
        }
        final String finalDat = dat;


        prefs = getSharedPreferences("DATA", 0);

        loader = (ProgressBar) findViewById(R.id.gifprogress);

    }


    public class AsyncImageLoader extends AsyncTask<String, Void, Void> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }


        @Override
        protected Void doInBackground(String... sub) {
            String s = sub[0];
            if (s.endsWith("v")) {
                s = s.substring(0, s.length() - 1);
            }
            s = s.trim();

            final String finalS = s;
            Log.v("Slide", "http://gfycat.com/cajax/checkUrl/" + s);
            Ion.with(GifView.this).load("http://gfycat.com/cajax/checkUrl/" + s).asJsonObject().setCallback(new FutureCallback<JsonObject>() {
                @Override
                public void onCompleted(Exception e, final JsonObject result) {
                    if (result != null && result.get("urlKnown").getAsBoolean()) {
                        final MediaVideoView videoView =
                                (MediaVideoView) findViewById(R.id.gif);

                        videoView.setVideoPath(
                                result.get("mp4Url").getAsString());
                        //videoView.set

                        MediaController mediaController = new
                                MediaController(GifView.this);
                        mediaController.setAnchorView(findViewById(R.id.placeholder));
                        videoView.setMediaController(mediaController);


                       /* Ion.with(GifView.this).load("http://gfycat.com/cajax/get/" + result.get("gfyName").getAsString()).asJsonObject().setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                int gifsize = result.get("gfyItem").getAsJsonObject().get("gifSize").getAsInt();
                                int gfysize = result.get("gfyItem").getAsJsonObject().get("gfysize").getAsInt();
                                prefs.edit().putInt("GifUsage", (prefs.getInt("GifUsage", 0) + gfysize)).apply();
                                prefs.edit().putInt("GifSaved", (prefs.getInt("GifSaved", 0) + (gifsize - gfysize))).apply();
                            }
                        });*/
                        //TODO THIS!


                        loader.setIndeterminate(false);
                        findViewById(R.id.gifsave).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + File.separator + "Slide"); //Creates app specific folder
                                path.mkdirs();
                                File imageFile = new File(path, DataShare.sharedSubmission.getId() + ".mp4"); // Imagename.png
                                Log.v("Slide", imageFile.getAbsolutePath());

                                new SaveGifAsync().execute(result.get("mp4Url").getAsString(), imageFile.getAbsolutePath());


                            }
                        });


                        videoView.start();
                        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
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

                    } else {

                        Ion.with(GifView.this)
                                .load("http://upload.gfycat.com/transcode?fetchUrl=" + finalS)
                                .asJsonObject()
                                .setCallback(new FutureCallback<JsonObject>() {
                                    @Override
                                    public void onCompleted(Exception e, final JsonObject result) {

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
                                            videoView.setVideoPath(
                                                    result.get("mp4Url").getAsString());

                                            MediaController mediaController = new
                                                    MediaController(GifView.this);
                                            mediaController.setAnchorView(videoView);
                                            videoView.setMediaController(mediaController);


                                            int gifsize = result.get("gifSize").getAsInt();
                                            int gfysize = result.get("gfysize").getAsInt();
                                            prefs.edit().putInt("GifUsage", (prefs.getInt("GifUsage", 0) + gfysize)).apply();
                                            prefs.edit().putInt("GifSaved", (prefs.getInt("GifSaved", 0) + (gifsize - gfysize))).apply();

                                            loader.setIndeterminate(false);

                                            findViewById(R.id.gifsave).setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + File.separator + "Slide"); //Creates app specific folder
                                                    path.mkdirs();
                                                    File imageFile = new File(path, DataShare.sharedSubmission.getId() + ".mp4"); // Imagename.png
                                                    Log.v("Slide", imageFile.getAbsolutePath());
                                                    new SaveGifAsync().execute(result.get("mp4Url").getAsString(), imageFile.getAbsolutePath());


                                                }
                                            });


                                            videoView.start();
                                            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
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

                                    }
                                });
                    }
                }
            });

            return null;

        }


    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.fade_out);
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
                                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
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

    public class SaveGifAsync extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

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
                            .setContentTitle("GIF Saved")
                            .setSmallIcon(R.drawable.notif);


                    NotificationManager mNotificationManager =
                            (NotificationManager) getSystemService(GifView.this.NOTIFICATION_SERVICE);
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