package me.ccrama.redditslide.Fragments;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import net.dean.jraw.models.Submission;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import me.ccrama.redditslide.Activities.CommentsScreen;
import me.ccrama.redditslide.Activities.CommentsScreenPopup;
import me.ccrama.redditslide.Activities.GifView;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.ImageLoaderUtils;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Views.MediaVideoView;
import me.ccrama.redditslide.Views.PopulateSubmissionViewHolder;


/**
 * Created by ccrama on 6/2/2015.
 */
public class Gif extends Fragment {

    private int i = 0;
    private View placeholder;
    private Submission s;
    private View gif;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (this.isVisible()) {
            if (!isVisibleToUser)   // If we are becoming invisible, then...
            {
                ((MediaVideoView) gif).pause();
                gif.setVisibility(View.GONE);
            }

            if (isVisibleToUser) // If we are becoming visible, then...
            {
                ((MediaVideoView) gif).start();
                gif.setVisibility(View.VISIBLE);

            }
        }
    }
    ViewGroup rootView;
    ProgressBar loader;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       rootView = (ViewGroup) inflater.inflate(
                R.layout.submission_gifcard, container, false);
        loader = (ProgressBar) rootView.findViewById(R.id.gifprogress);

        TextView title = (TextView) rootView.findViewById(R.id.title);
        TextView desc = (TextView) rootView.findViewById(R.id.desc);

        title.setText(s.getTitle());
        desc.setText(s.getSubredditName() + getString(R.string.submission_properties_seperator) + s.getAuthor() + " " + TimeUtils.getTimeAgo(s.getCreated().getTime(), getContext()) +
                getString(R.string.submission_properties_seperator) +
                PopulateSubmissionViewHolder.getSubmissionScoreString(s.getScore(), getActivity().getResources(), s)
                + getString(R.string.submission_properties_seperator)
                + getActivity().getResources().getQuantityString(R.plurals.submission_comment_count, s.getCommentCount(), s.getCommentCount()));        ContentType.ImageType type = ContentType.getImageType(s);

        placeholder = rootView.findViewById(R.id.placeholder);
        gif = rootView.findViewById(R.id.gif);


        gif.setVisibility(View.VISIBLE);
        final MediaVideoView v = (MediaVideoView) gif;
        v.clearFocus();


        String dat = s.getUrl();


        if(dat.contains("webm") && dat.contains("imgur")){
            dat = dat.replace("webm", "gifv");
        }
        if(dat.contains("mp4") && dat.contains("imgur")){
            dat = dat.replace("mp4", "gifv");
        }

        if (dat.endsWith("v")) {
            dat = dat.substring(0, dat.length() - 1);
        } else if (dat.contains("gfycat")) {
            dat = dat.substring(3, dat.length());
        }
        new AsyncImageLoader().execute(dat);



        rootView.findViewById(R.id.base).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Reddit.tabletUI && getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    Intent i2 = new Intent(getActivity(), CommentsScreenPopup.class);
                    i2.putExtra("page", i);
                    (getActivity()).startActivity(i2);

                } else {
                    Intent i2 = new Intent(getActivity(), CommentsScreen.class);
                    i2.putExtra("page", i);
                    i2.putExtra("subreddit", s.getSubredditName());
                    (getActivity()).startActivity(i2);
                }
            }
        });
        return rootView;
    }
    public static String getSmallerGfy(String gfyUrl) {
        gfyUrl = gfyUrl.replaceAll("fat|zippy|giant", "thumbs");
        if (!gfyUrl.endsWith("-mobile.mp4"))
            gfyUrl = gfyUrl.replaceAll("\\.mp4", "-mobile.mp4");
        return gfyUrl;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        i = bundle.getInt("page", 0);
        s = DataShare.sharedSubreddit.get(bundle.getInt("page", 0));

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
                Ion.with(getActivity())
                        .load("http://gfycat.com/cajax/get" + s)
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, final JsonObject result) {
                                new AsyncTask<Void, Void, Void>() {

                                    @Override
                                    protected Void doInBackground(Void... params) {
                                        final MediaVideoView videoView = (MediaVideoView) rootView.findViewById(R.id.gif);
                                        String obj = "";
                                        if (result == null || result.get("gfyItem") == null || result.getAsJsonObject("gfyItem").get("mp4Url").isJsonNull()) {

                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    new AlertDialogWrapper.Builder(getActivity())
                                                            .setTitle(R.string.gif_err_title)
                                                            .setMessage(R.string.gif_err_msg)
                                                            .setCancelable(false)
                                                            .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                }
                                                            }).create().show();
                                                }
                                            });


                                        } else {
                                            obj = result.getAsJsonObject("gfyItem").get("mp4Url").getAsString();

                                        }
                                        try {
                                            final URL url = new URL(obj);
                                            final File f = new File(ImageLoaderUtils.getCacheDirectory(getActivity()).getAbsolutePath() + File.separator + url.toString().replaceAll("[^a-zA-Z0-9]", "") + ".mp4");


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
                                                    getActivity().runOnUiThread(new Runnable() {
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
                                                getActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {

                                                        loader.setVisibility(View.GONE);

                                                    }
                                                });
                                            }

                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    videoView.setVideoPath(f.getAbsolutePath());
                                                    //videoView.set

                                                    MediaController mediaController = new
                                                            MediaController(getActivity());
                                                    mediaController.setAnchorView(rootView.findViewById(R.id.placeholder));
                                                    videoView.setMediaController(mediaController);

                                                    loader.setIndeterminate(false);



                                                    videoView.start();
                                                    videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                                        @Override
                                                        public void onPrepared(MediaPlayer mp) {
                                                            View placeholder = rootView.findViewById(R.id.placeholder);

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

                Ion.with(getActivity()).load("http://gfycat.com/cajax/checkUrl/" + s).asJsonObject().setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, final JsonObject result) {
                        if (result != null && result.has("urlKnown") && result.get("urlKnown").getAsBoolean()) {
                            final MediaVideoView videoView =
                                    (MediaVideoView) rootView.findViewById(R.id.gif);
                            new AsyncTask<Void, Void, Void>() {

                                @Override
                                protected Void doInBackground(Void... params) {
                                    try {

                                        final URL url = new URL(getSmallerGfy(result.get("mp4Url").getAsString()));
                                        final File f = new File(ImageLoaderUtils.getCacheDirectory(getActivity()).getAbsolutePath() + File.separator + url.toString().replaceAll("[^a-zA-Z0-9]", "") + ".mp4");


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
                                                getActivity().runOnUiThread(new Runnable() {
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
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {

                                                    loader.setVisibility(View.GONE);

                                                }
                                            });
                                        }
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                videoView.setVideoPath(f.getAbsolutePath());
                                                //videoView.set

                                                MediaController mediaController = new
                                                        MediaController(getActivity());
                                                mediaController.setAnchorView(getActivity().findViewById(R.id.placeholder));
                                                videoView.setMediaController(mediaController);

                                                loader.setIndeterminate(false);


                                                videoView.start();
                                                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                                    @Override
                                                    public void onPrepared(MediaPlayer mp) {
                                                        View placeholder = rootView.findViewById(R.id.placeholder);

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

                            Ion.with(getActivity())
                                    .load("http://upload.gfycat.com/transcode?fetchUrl=" + finalS)
                                    .asJsonObject()
                                    .setCallback(new FutureCallback<JsonObject>() {
                                        @Override
                                        public void onCompleted(Exception e, final JsonObject result) {

                                            try {
                                                final MediaVideoView videoView =
                                                        (MediaVideoView) rootView.findViewById(R.id.gif);

                                                if (result == null || result.get("mp4Url") == null || result.get("mp4Url").isJsonNull()) {

                                                    new AlertDialogWrapper.Builder(getActivity())
                                                            .setTitle(R.string.gif_err_title)
                                                            .setMessage(R.string.gif_err_msg)
                                                            .setCancelable(false)
                                                            .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
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

                                                    final File f = new File(ImageLoaderUtils.getCacheDirectory(getActivity()).getAbsolutePath() + File.separator + url.toString().replaceAll("[^a-zA-Z0-9]", "") + ".mp4");

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

                                                    getActivity().runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            videoView.setVideoPath(f.getAbsolutePath());
                                                            //videoView.set

                                                            MediaController mediaController = new
                                                                    MediaController(getActivity());
                                                            mediaController.setAnchorView(rootView.findViewById(R.id.placeholder));
                                                            videoView.setMediaController(mediaController);

                                                            loader.setIndeterminate(false);


                                                            videoView.start();
                                                            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                                                @Override
                                                                public void onPrepared(MediaPlayer mp) {
                                                                    View placeholder = rootView.findViewById(R.id.placeholder);

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
