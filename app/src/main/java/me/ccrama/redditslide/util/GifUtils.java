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
import me.ccrama.redditslide.Activities.MediaView;
import me.ccrama.redditslide.Activities.Shadowbox;
import me.ccrama.redditslide.ImageLoaderUtils;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
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
        public boolean hideControls;
        public boolean autostart;
        public Runnable doOnClick;

        public AsyncLoadGif(@NotNull Activity c, @NotNull MediaVideoView video, @Nullable ProgressBar p, @Nullable View placeholder, @Nullable View gifSave, @NotNull boolean closeIfNull, @NotNull boolean hideControls, boolean autostart) {
            this.c = c;
            this.video = video;
            this.progressBar = p;
            this.closeIfNull = closeIfNull;
            this.placeholder = placeholder;
            this.gifSave = gifSave;
            this.hideControls = hideControls;
            this.autostart = autostart;
        }

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

        public AsyncLoadGif(@NotNull Activity c, @NotNull MediaVideoView video, @Nullable ProgressBar p, @Nullable View placeholder, @NotNull boolean closeIfNull, @NotNull boolean hideControls, boolean autostart) {
            this.c = c;
            this.video = video;
            this.progressBar = p;
            this.closeIfNull = closeIfNull;
            this.placeholder = placeholder;
            this.hideControls = hideControls;
            this.autostart = autostart;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }


        @Override
        protected Void doInBackground(String... sub) {

            MediaView.didLoadGif = false;
            String s = sub[0];


            if (s.contains("webm") && s.contains("imgur")) {
                s = s.replace("webm", "mp4");
            }

            if (s.endsWith("v")) {
                s = s.substring(0, s.length() - 1);
            } else if (s.contains("gfycat") && !s.contains("mp4")) {
                s = s.substring(3, s.length());
            }
            if (s.contains(".gif") && !s.contains(".gifv") && s.contains("imgur.com")) {
                s = s.replace(".gif", ".mp4");
            }

            if (s.contains("gfycat") && !s.contains("mp4")) {
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

                                                CacheUtil.makeRoom(c, length);

                                                f.createNewFile();
                                                MediaView.fileLoc = f.getAbsolutePath();

                                                FileOutputStream outStream = new FileOutputStream(f);
                                                byte[] buff = new byte[5 * 1024];

                                                int len;
                                                int readBytes = 0;
                                                while ((len = inStream.read(buff)) != -1) {
                                                    outStream.write(buff, 0, len);
                                                    final int percent = Math.round(100.0f * f.length() / length);
                                                    if (percent == 100)
                                                        MediaView.didLoadGif = true;

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
                                                        gifSave.setOnClickListener(
                                                                new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View v) {
                                                                        saveGif(f, c);

                                                                    }
                                                                }

                                                        );
                                                    } else if (doOnClick != null) {
                                                        MediaView.doOnClick = new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                saveGif(f, c);

                                                            }
                                                        };
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
                                                    if (autostart)
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


                                }

                                        .

                                                execute();
                            }


                        });

            } else if (s.contains("imgur.com") || s.contains("mp4")) {

                try {

                    final URL url = new URL(s);
                    final File f = new File(ImageLoaderUtils.getCacheDirectory(c).getAbsolutePath() + File.separator + url.toString().replaceAll("[^a-zA-Z0-9]", "") + ".mp4");


                    if (!f.exists()) {
                        URLConnection ucon = url.openConnection();
                        ucon.setReadTimeout(5000);
                        ucon.setConnectTimeout(10000);
                        InputStream is = ucon.getInputStream();
                        BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 5);

                        int length = ucon.getContentLength();

                        CacheUtil.makeRoom(c, length);

                        f.createNewFile();
                        MediaView.fileLoc = f.getAbsolutePath();

                        FileOutputStream outStream = new FileOutputStream(f);
                        byte[] buff = new byte[5 * 1024];

                        int len;
                        int readBytes = 0;

                        while ((len = inStream.read(buff)) != -1) {
                            outStream.write(buff, 0, len);
                            final int percent = Math.round(100.0f * f.length() / length);
                            if (percent == 100)
                                MediaView.didLoadGif = true;
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
                                        saveGif(f, c);
                                    }
                                });
                            } else if (doOnClick != null) {
                                MediaView.doOnClick = new Runnable() {
                                    @Override
                                    public void run() {
                                        saveGif(f, c);

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


                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            } else {

                final String finalS = s;
                Log.v("Slide", "https://gfycat.com/cajax/checkUrl/" + s);

                Ion.with(c).load("https://gfycat.com/cajax/checkUrl/" + s).asJsonObject().setCallback(new FutureCallback<JsonObject>() {
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

                                                                                                                                  CacheUtil.makeRoom(c, length);

                                                                                                                                  f.createNewFile();
                                                                                                                                  MediaView.fileLoc = f.getAbsolutePath();

                                                                                                                                  FileOutputStream outStream = new FileOutputStream(f);
                                                                                                                                  byte[] buff = new byte[5 * 1024];

                                                                                                                                  int len;
                                                                                                                                  int readBytes = 0;

                                                                                                                                  while ((len = inStream.read(buff)) != -1) {
                                                                                                                                      outStream.write(buff, 0, len);
                                                                                                                                      final int percent = Math.round(100.0f * f.length() / length);
                                                                                                                                      if (percent == 100)
                                                                                                                                          MediaView.didLoadGif = true;
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
                                                                                                                                                  saveGif(f, c);
                                                                                                                                              }
                                                                                                                                          });
                                                                                                                                      } else if (doOnClick != null) {
                                                                                                                                          MediaView.doOnClick = new Runnable() {
                                                                                                                                              @Override
                                                                                                                                              public void run() {
                                                                                                                                                  saveGif(f, c);

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
                                                                                                                          } catch (Exception ex) {
                                                                                                                              ex.printStackTrace();
                                                                                                                          }
                                                                                                                          return null;
                                                                                                                      }

                                                                                                                  }.execute();
                                                                                                              } else {
                                                                                                                  Log.v(LogUtil.getTag(), "https://upload.gfycat.com/transcode?fetchUrl=" + finalS);
                                                                                                                  if (progressBar != null)
                                                                                                                      progressBar.setIndeterminate(true);
                                                                                                                  Ion.with(c)
                                                                                                                          .load("http://upload.gfycat.com/transcode?fetchUrl=" + finalS)
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

                                                                                                                                                               if (closeIfNull)
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

                                                                                                                                                           } else {
                                                                                                                                                               final URL url = new URL(result.get("mp4Url").getAsString()); //wont exist on server yet, just load the full version
                                                                                                                                                               URLConnection ucon = url.openConnection();
                                                                                                                                                               ucon.setReadTimeout(5000);
                                                                                                                                                               ucon.setConnectTimeout(10000);
                                                                                                                                                               InputStream is = ucon.getInputStream();
                                                                                                                                                               BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 5);

                                                                                                                                                               int length = ucon.getContentLength();

                                                                                                                                                               CacheUtil.makeRoom(c, length);

                                                                                                                                                               final File f = new File(ImageLoaderUtils.getCacheDirectory(c).getAbsolutePath() + File.separator + url.toString().replaceAll("[^a-zA-Z0-9]", "") + ".mp4");

                                                                                                                                                               f.createNewFile();
                                                                                                                                                               MediaView.fileLoc = f.getAbsolutePath();

                                                                                                                                                               FileOutputStream outStream = new FileOutputStream(f);
                                                                                                                                                               byte[] buff = new byte[5 * 1024];

                                                                                                                                                               int len;
                                                                                                                                                               while ((len = inStream.read(buff)) != -1) {
                                                                                                                                                                   outStream.write(buff, 0, len);
                                                                                                                                                                   final int percent = Math.round(100.0f * f.length() / length);
                                                                                                                                                                   if (percent == 100)
                                                                                                                                                                       MediaView.didLoadGif = true;
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

                                                                                                                                                               c.runOnUiThread(new Runnable() {
                                                                                                                                                                   @Override
                                                                                                                                                                   public void run() {
                                                                                                                                                                       video.setVideoPath(f.getAbsolutePath());
                                                                                                                                                                       //videoView.set

                                                                                                                                                                       if (placeholder != null && !hideControls && !(c instanceof Shadowbox)) {
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
                                                                                                                                                                                   saveGif(f, c);
                                                                                                                                                                               }
                                                                                                                                                                           });
                                                                                                                                                                       } else if (doOnClick != null) {
                                                                                                                                                                           MediaView.doOnClick = new Runnable() {
                                                                                                                                                                               @Override
                                                                                                                                                                               public void run() {
                                                                                                                                                                                   saveGif(f, c);

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
            }
            return null;
        }
    }

    public static void saveGifToCache(final Activity c, String... sub) {
        String s = sub[0];

        if (s.contains("webm") && s.contains("imgur")) {
            s = s.replace("webm", "gifv");
        }
        if (s.contains("mp4") && s.contains("imgur")) {
            s = s.replace("mp4", "gifv");
        }

        if (s.endsWith("v")) {
            s = s.substring(0, s.length() - 1);
        } else if (s.contains("gfycat")) {
            s = s.substring(3, s.length());
        }
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
                                    if (result != null && result.get("gfyItem") != null && !result.getAsJsonObject("gfyItem").get("mp4Url").isJsonNull()) {
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

                                            CacheUtil.makeRoom(c, length);

                                            f.createNewFile();
                                            MediaView.fileLoc = f.getAbsolutePath();

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
                                    } catch (Exception e2) {
                                        e2.printStackTrace();
                                    }
                                    return null;
                                }
                            }.execute();
                        }
                    });
        } else {
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

                                        CacheUtil.makeRoom(c, length);

                                        f.createNewFile();
                                        MediaView.fileLoc = f.getAbsolutePath();

                                        FileOutputStream outStream = new FileOutputStream(f);
                                        byte[] buff = new byte[5 * 1024];

                                        int len;
                                        int readBytes = 0;

                                        while ((len = inStream.read(buff)) != -1) {
                                            outStream.write(buff, 0, len);
                                            final int percent = Math.round(100.0f * f.length() / length);
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
                        }.execute();
                    } else {
                        Ion.with(c)
                                .load("http://upload.gfycat.com/transcode?fetchUrl=" + finalS)
                                .asJsonObject()
                                .setCallback(new FutureCallback<JsonObject>() {
                                    @Override
                                    public void onCompleted(Exception e, final JsonObject result) {
                                        new AsyncTask<Void, Void, Void>() {

                                            @Override
                                            protected Void doInBackground(Void... params) {
                                                try {

                                                    if (result != null && result.get("mp4Url") != null && !result.get("mp4Url").isJsonNull()) {
                                                        final URL url = new URL(getSmallerGfy(result.get("mp4Url").getAsString()));
                                                        URLConnection ucon = url.openConnection();
                                                        ucon.setReadTimeout(5000);
                                                        ucon.setConnectTimeout(10000);
                                                        InputStream is = ucon.getInputStream();
                                                        BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 5);

                                                        int length = ucon.getContentLength();

                                                        CacheUtil.makeRoom(c, length);

                                                        final File f = new File(ImageLoaderUtils.getCacheDirectory(c).getAbsolutePath() + File.separator + url.toString().replaceAll("[^a-zA-Z0-9]", "") + ".mp4");

                                                        f.createNewFile();
                                                        MediaView.fileLoc = f.getAbsolutePath();

                                                        FileOutputStream outStream = new FileOutputStream(f);
                                                        byte[] buff = new byte[5 * 1024];

                                                        int len;
                                                        while ((len = inStream.read(buff)) != -1) {
                                                            outStream.write(buff, 0, len);
                                                            int percent = Math.round(100.0f * f.length() / length);
                                                        }
                                                        outStream.flush();
                                                        outStream.close();
                                                        inStream.close();
                                                    }
                                                } catch (Exception e3) {
                                                    e3.printStackTrace();
                                                }
                                                return null;
                                            }
                                        }.execute();
                                    }
                                });
                    }
                }
            });
        }
    }

    public static void showErrorDialog(final Activity a) {
        new AlertDialogWrapper.Builder(a)
                .setTitle("Uh oh, something went wrong.")
                .setMessage("Slide couldn't save to the selected directory. Would you like to choose a new save location?")
                .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new FolderChooserDialog.Builder(a instanceof GifView ? (GifView) a : (MediaView) a)
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
                .setTitle("Set gif save location")
                .setMessage("Slide's gif save location has not been set yet. Would you like to set this now?")
                .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new FolderChooserDialog.Builder(a instanceof GifView ? (GifView) a : (MediaView) a)
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
        Intent mediaScanIntent = new Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.parse("file://" + s);
        mediaScanIntent.setData(contentUri);
        c.sendBroadcast(mediaScanIntent);


        final Intent shareIntent = new Intent(Intent.ACTION_VIEW);
        shareIntent.setDataAndType(Uri.parse(s), "video/*");
        PendingIntent contentIntent = PendingIntent.getActivity(c, 0, shareIntent, PendingIntent.FLAG_CANCEL_CURRENT);


        Notification notif = new NotificationCompat.Builder(c)
                .setContentTitle("Gif saved")
                .setSmallIcon(R.drawable.notif)
                .setContentIntent(contentIntent)
                .build();

        NotificationManager mNotificationManager =
                (NotificationManager) c.getSystemService(Activity.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, notif);
    }
}