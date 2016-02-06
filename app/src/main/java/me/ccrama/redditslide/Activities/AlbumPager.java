package me.ccrama.redditslide.Activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.ImageLoaderUtils;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.Views.MakeTextviewClickable;
import me.ccrama.redditslide.Views.MediaVideoView;
import me.ccrama.redditslide.Views.TitleTextView;
import me.ccrama.redditslide.Views.ToolbarColorizeHelper;


/**
 * Created by ccrama on 1/25/2016.
 *
 * This is an extension of Album.java which utilizes a ViewPager for Imgur content
 * instead of a RecyclerView (horizontal vs vertical). It also supports gifs and progress
 * bars which Album.java doesn't.
 *
 */
public class AlbumPager extends FullScreenActivity {
    boolean gallery = false;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    public void onCreate(Bundle savedInstanceState) {
        overrideSwipeFromAnywhere();

        super.onCreate(savedInstanceState);
        getTheme().applyStyle(new ColorPreferences(this).getFontStyle().getBaseId(), true);
        applyColorTheme();
        setContentView(R.layout.album_pager);

        final Toolbar b = (Toolbar) findViewById(R.id.toolbar);
        b.setTitle(R.string.album_loading);
        ToolbarColorizeHelper.colorizeToolbar(b, Color.WHITE, this);
        setSupportActionBar(b);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        String rawDat = cutEnds(getIntent().getExtras().getString("url", ""));
        if (rawDat.contains("gallery")) {
            gallery = true;
        }
        if (rawDat.endsWith("/")) {
            rawDat = rawDat.substring(0, rawDat.length() - 1);
        }
        String rawdat2 = rawDat;
        if (rawdat2.substring(rawDat.lastIndexOf("/"), rawdat2.length()).length() < 4) {
            rawDat = rawDat.replace(rawDat.substring(rawDat.lastIndexOf("/"), rawdat2.length()), "");
        }
        if (rawDat.isEmpty()) {
            finish();
        } else {

            new AsyncImageLoader().execute(getHash(rawDat));

        }

    }

    private String getHash(String s) {
        String next = s.substring(s.lastIndexOf("/"), s.length());
        if (next.length() < 5) {
            return getHash(s.replace(next, ""));
        } else {
            return next;
        }

    }

    boolean slider;

    private String cutEnds(String s) {
        if (s.endsWith("/")) {
            return s.substring(0, s.length() - 1);
        } else {
            return s;
        }
    }

    public ArrayList<JsonElement> images;

    public class AlbumViewPager extends FragmentStatePagerAdapter {


        public AlbumViewPager(FragmentManager m) {
            super(m);
        }


        @Override
        public Fragment getItem(int i) {

            String url;
            if (gallery) {
                url = ("https://imgur.com/" + images.get(i).getAsJsonObject().get("hash").getAsString() + ".png");

            } else {
                url = (images.get(i).getAsJsonObject().getAsJsonObject("links").get("original").getAsString());

            }

            if (url.contains("gif")) {
                //do gif stuff
                Fragment f = new Gif();
                Bundle args = new Bundle();
                args.putInt("page", i);
                f.setArguments(args);

                return f;
            } else {
                Fragment f = new ImageFullNoSubmission();
                Bundle args = new Bundle();
                args.putInt("page", i);
                f.setArguments(args);

                return f;
            }


        }


        @Override
        public int getCount() {
            if (images == null) {
                return 0;
            }
            return images.size();
        }
    }

    public class Gif extends Fragment {

        private int i = 0;
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
                    R.layout.submission_gifcard_album, container, false);
            loader = (ProgressBar) rootView.findViewById(R.id.gifprogress);

            TitleTextView title = (TitleTextView) rootView.findViewById(R.id.title);
            SpoilerRobotoTextView desc = (SpoilerRobotoTextView) rootView.findViewById(R.id.desc);

            title.setVisibility(View.VISIBLE);
            desc.setVisibility(View.VISIBLE);
            if (user.getAsJsonObject().has("image")) {
                {
                    if (!user.getAsJsonObject().getAsJsonObject("image").get("title").isJsonNull()) {

                        new MakeTextviewClickable().ParseTextWithLinksTextViewComment(user.getAsJsonObject().getAsJsonObject("image").get("title").getAsString(), desc, (Activity) inflater.getContext(), "");
                        if (desc.getText().toString().isEmpty()) {
                            desc.setVisibility(View.GONE);
                        }

                    } else {
                        desc.setVisibility(View.GONE);

                    }
                }
                {
                    if (!user.getAsJsonObject().getAsJsonObject("image").get("caption").isJsonNull()) {
                        title.setText(user.getAsJsonObject().getAsJsonObject("image").get("caption").getAsString());
                        new MakeTextviewClickable().ParseTextWithLinksTextViewComment(user.getAsJsonObject().getAsJsonObject("image").get("caption").getAsString(), title, (Activity) inflater.getContext(), "");

                        if (title.getText().toString().isEmpty()) {
                            title.setVisibility(View.GONE);
                        }
                    } else {
                        title.setVisibility(View.GONE);

                    }
                }
            } else {
                if (user.getAsJsonObject().has("title")) {
                    new MakeTextviewClickable().ParseTextWithLinksTextViewComment(user.getAsJsonObject().get("title").getAsString(), desc, (Activity) inflater.getContext(), "");
                    if (desc.getText().toString().isEmpty()) {
                        desc.setVisibility(View.GONE);
                    }

                } else {

                    desc.setVisibility(View.GONE);

                }
                if (user.getAsJsonObject().has("description")) {
                    new MakeTextviewClickable().ParseTextWithLinksTextViewComment(user.getAsJsonObject().get("description").getAsString(), title, (Activity) inflater.getContext(), "");
                    if (title.getText().toString().isEmpty()) {
                        title.setVisibility(View.GONE);
                    }
                } else {
                    title.setVisibility(View.GONE);

                }


            }
            gif = rootView.findViewById(R.id.gif);


            gif.setVisibility(View.VISIBLE);
            final MediaVideoView v = (MediaVideoView) gif;
            v.clearFocus();



            String dat;
            if (gallery) {
                dat = ("https://imgur.com/" + images.get(i).getAsJsonObject().get("hash").getAsString() + ".png");

            } else {
                dat = (images.get(i).getAsJsonObject().getAsJsonObject("links").get("original").getAsString());

            }


            if (dat.contains("webm") && dat.contains("imgur")) {
                dat = dat.replace("webm", "gifv");
            }
            if (dat.contains("mp4") && dat.contains("imgur")) {
                dat = dat.replace("mp4", "gifv");
            }

            if (dat.endsWith("v")) {
                dat = dat.substring(0, dat.length() - 1);
            } else if (dat.contains("gfycat")) {
                dat = dat.substring(3, dat.length());
            }
            new AsyncImageLoader().execute(dat);

            return rootView;
        }

        public String getSmallerGfy(String gfyUrl) {
            gfyUrl = gfyUrl.replaceAll("fat|zippy|giant", "thumbs");
            if (!gfyUrl.endsWith("-mobile.mp4"))
                gfyUrl = gfyUrl.replaceAll("\\.mp4", "-mobile.mp4");
            return gfyUrl;
        }

        JsonElement user;
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle bundle = this.getArguments();
            i = bundle.getInt("page", 0);
            user = images.get(bundle.getInt("page", 0));

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
                                        }

                                        ;


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

                    if (getActivity() != null)
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
                                                        mediaController.setAnchorView(videoView);
                                                        videoView.setMediaController(mediaController);

                                                        loader.setIndeterminate(false);


                                                        videoView.start();
                                                        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                                            @Override
                                                            public void onPrepared(MediaPlayer mp) {
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

    public class ImageFullNoSubmission extends Fragment {

        private int i = 0;
        private JsonElement user;


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final ViewGroup rootView = (ViewGroup) inflater.inflate(
                    R.layout.submission_imagecard_album, container, false);

            String url;

            if (gallery) {
                url = ("https://imgur.com/" + user.getAsJsonObject().get("hash").getAsString() + ".png");

            } else {
                url = (user.getAsJsonObject().getAsJsonObject("links").get("original").getAsString());

            }

            final SubsamplingScaleImageView image = (SubsamplingScaleImageView) rootView.findViewById(R.id.image);
            TitleTextView title = (TitleTextView) rootView.findViewById(R.id.title);
            SpoilerRobotoTextView desc = (SpoilerRobotoTextView) rootView.findViewById(R.id.desc);
            ImageView fakeImage = new ImageView(getActivity());
            fakeImage.setLayoutParams(new LinearLayout.LayoutParams(image.getWidth(), image.getHeight()));
            fakeImage.setScaleType(ImageView.ScaleType.CENTER_CROP);


            ((Reddit) getActivity().getApplication()).getImageLoader()
                    .displayImage(url, new ImageViewAware(fakeImage), ImageLoaderUtils.options, new ImageLoadingListener() {
                        private View mView;

                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            mView = view;
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                            Log.v("Slide", "LOADING FAILED");

                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            image.setImage(ImageSource.bitmap(loadedImage));
                            (rootView.findViewById(R.id.progress)).setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadingCancelled(String imageUri, View view) {
                            Log.v("Slide", "LOADING CANCELLED");

                        }
                    }, new ImageLoadingProgressListener() {
                        @Override
                        public void onProgressUpdate(String imageUri, View view, int current, int total) {
                            ((ProgressBar) rootView.findViewById(R.id.progress)).setProgress(Math.round(100.0f * current / total));
                        }
                    });
            title.setVisibility(View.VISIBLE);
            desc.setVisibility(View.VISIBLE);
            if (user.getAsJsonObject().has("image")) {
                {
                    if (!user.getAsJsonObject().getAsJsonObject("image").get("title").isJsonNull()) {

                        new MakeTextviewClickable().ParseTextWithLinksTextViewComment(user.getAsJsonObject().getAsJsonObject("image").get("title").getAsString(), desc, (Activity) inflater.getContext(), "");
                        if (desc.getText().toString().isEmpty()) {
                            desc.setVisibility(View.GONE);
                        }

                    } else {
                        desc.setVisibility(View.GONE);

                    }
                }
                {
                    if (!user.getAsJsonObject().getAsJsonObject("image").get("caption").isJsonNull()) {
                        title.setText(user.getAsJsonObject().getAsJsonObject("image").get("caption").getAsString());
                        new MakeTextviewClickable().ParseTextWithLinksTextViewComment(user.getAsJsonObject().getAsJsonObject("image").get("caption").getAsString(), title, (Activity) inflater.getContext(), "");

                        if (title.getText().toString().isEmpty()) {
                            title.setVisibility(View.GONE);
                        }
                    } else {
                        title.setVisibility(View.GONE);

                    }
                }
            } else {
                if (user.getAsJsonObject().has("title")) {
                    new MakeTextviewClickable().ParseTextWithLinksTextViewComment(user.getAsJsonObject().get("title").getAsString(), desc, (Activity) inflater.getContext(), "");
                    if (desc.getText().toString().isEmpty()) {
                        desc.setVisibility(View.GONE);
                    }

                } else {

                    desc.setVisibility(View.GONE);

                }
                if (user.getAsJsonObject().has("description")) {
                    new MakeTextviewClickable().ParseTextWithLinksTextViewComment(user.getAsJsonObject().get("description").getAsString(), title, (Activity) inflater.getContext(), "");
                    if (title.getText().toString().isEmpty()) {
                        title.setVisibility(View.GONE);
                    }
                } else {
                    title.setVisibility(View.GONE);

                }


            }


            return rootView;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle bundle = this.getArguments();
            i = bundle.getInt("page", 0);
            user = images.get(i);

        }

    }


    private class AsyncImageLoader extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(final String... sub) {
            if (gallery) {
                Ion.with(AlbumPager.this)
                        .load("https://imgur.com/gallery/" + sub[0] + ".json")
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                if (result != null && result.has("data")) {
                                    Log.v("Slide", result.toString());


                                    final ArrayList<JsonElement> jsons = new ArrayList<>();


                                    if (!result.getAsJsonObject("data").getAsJsonObject("image").get("is_album").getAsBoolean()) {
                                        if (result.getAsJsonObject("data").getAsJsonObject("image").get("mimetype").getAsString().contains("gif")) {
                                            Intent i = new Intent(AlbumPager.this, GifView.class);
                                            i.putExtra("url", "http://imgur.com/" + result.getAsJsonObject("data").getAsJsonObject("image").get("hash").getAsString() + ".gif"); //could be a gif
                                            startActivity(i);
                                        } else {
                                            Intent i = new Intent(AlbumPager.this, FullscreenImage.class);
                                            i.putExtra("url", "http://imgur.com/" + result.getAsJsonObject("data").getAsJsonObject("image").get("hash").getAsString() + ".png"); //could be a gif
                                            startActivity(i);
                                        }
                                        finish();

                                    } else {
                                        JsonArray obj = result.getAsJsonObject("data").getAsJsonObject("image").getAsJsonObject("album_images").get("images").getAsJsonArray();
                                        if (obj != null && !obj.isJsonNull() && obj.size() > 0) {

                                            for (JsonElement o : obj) {
                                                jsons.add(o);
                                            }

                                            getSupportActionBar().setTitle(getString(R.string.album_title_count, jsons.size()));


                                            ViewPager p = (ViewPager) findViewById(R.id.images_horizontal);
                                            images = jsons;
                                            getSupportActionBar().setSubtitle(1 + "/" + images.size());

                                            p.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                                                @Override
                                                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                                                    getSupportActionBar().setSubtitle((position + 1)+ "/" + images.size());
                                                }

                                                @Override
                                                public void onPageSelected(int position) {

                                                }

                                                @Override
                                                public void onPageScrollStateChanged(int state) {

                                                }
                                            });
                                            AlbumViewPager adapter = new AlbumViewPager(getSupportFragmentManager());
                                            p.setAdapter(adapter);
                                            adapter.notifyDataSetChanged();

                                            slider = true;


                                        }
                                    }
                                } else {

                                    Intent i = new Intent(AlbumPager.this, Website.class);
                                    i.putExtra("url", "http://imgur.com/gallery/" + sub[0]);

                                    startActivity(i);
                                    finish();
                                    //Catch failed api call
                                }
                            }

                        });
            } else {
                Log.v("Slide", "http://api.imgur.com/2/album" + sub[0] + ".json");
                Ion.with(AlbumPager.this)
                        .load("http://api.imgur.com/2/album" + sub[0] + ".json")
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                Dialog dialog = new AlertDialogWrapper.Builder(AlbumPager.this)
                                        .setTitle(R.string.album_err_not_found)
                                        .setMessage(R.string.album_err_msg_not_found)
                                        .setCancelable(false)
                                        .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                finish();
                                            }
                                        }).create();

                                if (result != null) {
                                    Log.v("Slide", result.toString());

                                    final ArrayList<JsonElement> jsons = new ArrayList<>();

                                    if (result.has("album")) {
                                        if (result.get("album").getAsJsonObject().has("title") && !result.get("album").isJsonNull() && !result.get("album").getAsJsonObject().get("title").isJsonNull()) {
                                            getSupportActionBar().setTitle(result.get("album").getAsJsonObject().get("title").getAsString());
                                        } else {
                                            getSupportActionBar().setTitle("Album");

                                        }
                                        JsonObject obj = result.getAsJsonObject("album");
                                        if (obj != null && !obj.isJsonNull() && obj.has("images")) {

                                            final JsonArray jsonAuthorsArray = obj.get("images").getAsJsonArray();

                                            for (JsonElement o : jsonAuthorsArray) {
                                                jsons.add(o);
                                            }

                                            images = jsons;

                                            ViewPager p = (ViewPager) findViewById(R.id.images_horizontal);

                                            getSupportActionBar().setSubtitle(1 + "/" + images.size());

                                            AlbumViewPager adapter = new AlbumViewPager(getSupportFragmentManager());
                                            p.setAdapter(adapter);
                                            p.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                                                @Override
                                                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                                                    getSupportActionBar().setSubtitle((position + 1)+ "/" + images.size());
                                                }

                                                @Override
                                                public void onPageSelected(int position) {

                                                }

                                                @Override
                                                public void onPageScrollStateChanged(int state) {

                                                }
                                            });
                                            adapter.notifyDataSetChanged();

                                        } else {

                                            new AlertDialogWrapper.Builder(AlbumPager.this)
                                                    .setTitle(R.string.album_err_not_found)
                                                    .setMessage(R.string.album_err_msg_not_found)
                                                    .setCancelable(false)
                                                    .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            finish();
                                                        }
                                                    }).create().show();
                                        }
                                    } else {
                                        dialog.show();
                                    }
                                } else {
                                    dialog.show();
                                }
                            }

                        });
            }

            return null;

        }


    }


}