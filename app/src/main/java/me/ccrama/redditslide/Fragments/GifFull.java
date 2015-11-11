package me.ccrama.redditslide.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import net.dean.jraw.models.Submission;

import me.ccrama.redditslide.Activities.CommentsScreen;
import me.ccrama.redditslide.Activities.CommentsScreenPopup;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Views.MediaVideoView;


/**
 * Created by ccrama on 6/2/2015.
 */
public class GifFull extends Fragment {

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.submission_gifcard, container, false);

        TextView title = (TextView) rootView.findViewById(R.id.title);
        TextView desc = (TextView) rootView.findViewById(R.id.desc);

        title.setText(s.getTitle());
        desc.setText(s.getAuthor() + " " + TimeUtils.getTimeAgo(s.getCreatedUtc().getTime(), getContext()));
        ContentType.ImageType type = ContentType.getImageType(s);

        placeholder = rootView.findViewById(R.id.placeholder);
        gif = rootView.findViewById(R.id.gif);


        gif.setVisibility(View.VISIBLE);
        final MediaVideoView v = (MediaVideoView) gif;
        v.clearFocus();


        String dat = s.getUrl();


        if (dat.contains("gfy")) {
            new AsyncGyfcat().execute(dat.substring(3, dat.length()));
        } else {
            if (dat.endsWith("v")) {
                dat = dat.substring(0, dat.length() - 1);
            }
            new AsyncImageLoader().execute(dat);
        }


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
                    (getActivity()).startActivity(i2);
                }
            }
        });
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        i = bundle.getInt("page", 0);
        s = DataShare.sharedSubreddit.get(bundle.getInt("page", 0));

    }

    private class AsyncImageLoader extends AsyncTask<String, Void, Void> {


        @Override
        protected Void doInBackground(String... sub) {

            String s = sub[0];
            if (s.endsWith("v")) {
                s = s.substring(0, s.length() - 1);
            }
            s = s.trim();

            final String finalS = s;
            Log.v("Slide", "http://gfycat.com/cajax/checkUrl/" + s);
            if (getContext() != null) {
                Ion.with(getActivity()).load("http://gfycat.com/cajax/checkUrl/" + s).asJsonObject().setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, final JsonObject result) {
                        if (result != null && result.get("urlKnown").getAsBoolean()) {
                            final MediaVideoView videoView =
                                    (MediaVideoView) gif;

                            videoView.setVideoPath(
                                    result.get("mp4Url").getAsString());
                            //videoView.set


                            videoView.start();
                            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    placeholder.setVisibility(View.GONE);
                                    mp.setLooping(true);

                                }
                            });

                        } else {

                            Ion.with(getActivity())
                                    .load("http://upload.gfycat.com/transcode?fetchUrl=" + finalS)
                                    .asJsonObject()
                                    .setCallback(new FutureCallback<JsonObject>() {
                                        @Override
                                        public void onCompleted(Exception e, final JsonObject result) {

                                            final MediaVideoView videoView =
                                                    (MediaVideoView) gif;

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
                                                videoView.setVideoPath(
                                                        result.get("mp4Url").getAsString());


                                                videoView.start();
                                                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                                    @Override
                                                    public void onPrepared(MediaPlayer mp) {

                                                        placeholder.setVisibility(View.GONE);
                                                        mp.setLooping(true);


                                                    }
                                                });
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


    private class AsyncGyfcat extends AsyncTask<String, Void, Void> {


        @Override
        protected Void doInBackground(String... sub) {
            String s = sub[0].substring(sub[0].lastIndexOf("/"), sub[0].length());


            Log.v("Slide", "http://gfycat.com/cajax/get" + s);
            Ion.with(getActivity())
                    .load("http://gfycat.com/cajax/get" + s)
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, final JsonObject result) {

                            final MediaVideoView v = (MediaVideoView) gif;
                            String obj = "";
                            if (result == null || result.get("gfyItem") == null || result.getAsJsonObject("gfyItem").get("mp4Url").isJsonNull()) {

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
                                obj = result.getAsJsonObject("gfyItem").get("mp4Url").getAsString();

                            }

                            try {
                                MediaController mediacontroller = new MediaController(getActivity());
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

                            v.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    mp.setLooping(true);
                                    placeholder.setVisibility(View.GONE);


                                }
                            });


                        }
                    });
            return null;

        }


    }
}
