package me.ccrama.redditslide.Fragments;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import net.dean.jraw.models.Comment;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import me.ccrama.redditslide.Activities.MediaView;
import me.ccrama.redditslide.Activities.ShadowboxComments;
import me.ccrama.redditslide.Activities.Website;
import me.ccrama.redditslide.Adapters.CommentUrlObject;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SecretConstants;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SubmissionViews.PopulateShadowboxInfo;
import me.ccrama.redditslide.Views.ExoVideoView;
import me.ccrama.redditslide.Views.ImageSource;
import me.ccrama.redditslide.Views.SubsamplingScaleImageView;
import me.ccrama.redditslide.util.GifUtils;
import me.ccrama.redditslide.util.HttpUtil;
import me.ccrama.redditslide.util.LinkUtil;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.NetworkUtil;
import okhttp3.OkHttpClient;


/**
 * Created by ccrama on 6/2/2015.
 */
public class MediaFragmentComment extends Fragment {

    public  String                contentUrl;
    public  String                sub;
    public  String                actuallyLoaded;
    public  int                   i;
    private ViewGroup             rootView;
    private ExoVideoView          videoView;
    private boolean               imageShown;
    private float                 previous;
    private boolean               hidden;
    private long                   stopPosition;
    public  boolean               isGif;
    private GifUtils.AsyncLoadGif gif;
    private CommentUrlObject      s;
    private OkHttpClient          client;
    private Gson                  gson;
    private String                mashapeKey;

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.v("Destroying");
        ((SubsamplingScaleImageView) rootView.findViewById(R.id.submission_image)).recycle();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && videoView != null) {
            videoView.seekTo(0);
            videoView.play();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (videoView != null) {
            videoView.seekTo((int) stopPosition);
            videoView.play();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (videoView != null) {
            stopPosition = videoView.getCurrentPosition();
            videoView.pause();
            ((SlidingUpPanelLayout) rootView.findViewById(R.id.sliding_layout)).setPanelState(
                    SlidingUpPanelLayout.PanelState.COLLAPSED);
            outState.putLong("position", stopPosition);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.submission_mediacard, container, false);
        if (savedInstanceState != null && savedInstanceState.containsKey("position")) {
            stopPosition = savedInstanceState.getLong("position");
        }
        final SlidingUpPanelLayout slideLayout = rootView.findViewById(R.id.sliding_layout);

        PopulateShadowboxInfo.doActionbar(s.comment, rootView, getActivity(), true);
        (rootView.findViewById(R.id.thumbimage2)).setVisibility(View.GONE);

        ContentType.Type type = ContentType.getContentType(contentUrl);

        if (ContentType.fullImage(type)) {
            (rootView.findViewById(R.id.thumbimage2)).setVisibility(View.GONE);
        }
        addClickFunctions((rootView.findViewById(R.id.submission_image)), slideLayout, rootView,
                type, getActivity(), s);
        doLoad(contentUrl);

        final View.OnClickListener openClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SlidingUpPanelLayout) rootView.findViewById(R.id.sliding_layout)).setPanelState(
                        SlidingUpPanelLayout.PanelState.EXPANDED);
            }
        };
        rootView.findViewById(R.id.base).setOnClickListener(openClick);
        final View title = rootView.findViewById(R.id.title);
        title.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        ((SlidingUpPanelLayout) rootView.findViewById(
                                R.id.sliding_layout)).setPanelHeight(title.getMeasuredHeight());
                        title.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
        ((SlidingUpPanelLayout) rootView.findViewById(R.id.sliding_layout)).addPanelSlideListener(
                new SlidingUpPanelLayout.PanelSlideListener() {
                    @Override
                    public void onPanelSlide(View panel, float slideOffset) {

                    }

                    @Override
                    public void onPanelStateChanged(View panel,
                            SlidingUpPanelLayout.PanelState previousState,
                            SlidingUpPanelLayout.PanelState newState) {
                        if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                            final Comment c = s.comment.getComment();
                            rootView.findViewById(R.id.base)
                                    .setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            String url = "https://reddit.com"
                                                    + "/r/"
                                                    + c.getSubredditName()
                                                    + "/comments/"
                                                    + c.getDataNode()
                                                    .get("link_id")
                                                    .asText()
                                                    .substring(3, c.getDataNode()
                                                            .get("link_id")
                                                            .asText()
                                                            .length())
                                                    + "/nothing/"
                                                    + c.getId()
                                                    + "?context=3";
                                            new OpenRedditLink(getActivity(), url);
                                        }
                                    });
                        } else {
                            rootView.findViewById(R.id.base).setOnClickListener(openClick);
                        }
                    }
                });
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        i = bundle.getInt("page");
        s = ((ShadowboxComments) getActivity()).comments.get(i);
        sub = s.comment.getComment().getSubredditName();
        contentUrl = bundle.getString("contentUrl");
        client = Reddit.client;
        gson = new Gson();
        mashapeKey = SecretConstants.getImgurApiKey(getContext());
    }

    public void doLoad(final String contentUrl) {
        switch (ContentType.getContentType(contentUrl)) {
            case DEVIANTART:
                doLoadDeviantArt(contentUrl);
                break;
            case IMAGE:
                doLoadImage(contentUrl);
                break;
            case IMGUR:
                doLoadImgur(contentUrl);
                break;
            case XKCD:
                doLoadXKCD(contentUrl);
                break;
            case STREAMABLE:
            case GIF:
                doLoadGif(contentUrl);
                break;
        }
    }

    public void doLoadXKCD(String url) {
        if (!url.endsWith("/")) {
            url = url + "/";
        }

        if (NetworkUtil.isConnected(getContext())) {
            final String apiUrl = url + "info.0.json";
            LogUtil.v(apiUrl);

            final String finalUrl = url;
            new AsyncTask<Void, Void, JsonObject>() {
                @Override
                protected JsonObject doInBackground(Void... params) {
                    return HttpUtil.getJsonObject(client, gson, apiUrl);
                }

                @Override
                protected void onPostExecute(final JsonObject result) {
                    if (result != null && !result.isJsonNull() && result.has("error")) {
                        LogUtil.v("Error loading content");
                    } else {
                        try {
                            if (result != null && !result.isJsonNull() && result.has("img")) {
                                doLoadImage(result.get("img").getAsString());
                                rootView.findViewById(R.id.submission_image)
                                        .setOnLongClickListener(new View.OnLongClickListener() {
                                            @Override
                                            public boolean onLongClick(View v) {
                                                try {
                                                    new AlertDialogWrapper.Builder(
                                                            getContext()).setTitle(
                                                            result.get("safe_title").getAsString())
                                                            .setMessage(
                                                                    result.get("alt").getAsString())
                                                            .show();
                                                } catch (Exception ignored) {

                                                }
                                                return true;
                                            }
                                        });
                            } else {
                                Intent i = new Intent(getContext(), Website.class);
                                i.putExtra(LinkUtil.EXTRA_URL, finalUrl);
                                getContext().startActivity(i);
                            }
                        } catch (Exception e2) {
                            e2.printStackTrace();
                            Intent i = new Intent(getContext(), Website.class);
                            i.putExtra(LinkUtil.EXTRA_URL, finalUrl);
                            getContext().startActivity(i);
                        }
                    }

                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }


    private static void addClickFunctions(final View base, final SlidingUpPanelLayout slidingPanel,
            final View clickingArea, final ContentType.Type type, final Activity contextActivity,
            final CommentUrlObject submission) {
        base.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (slidingPanel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                } else {
                    if (type == ContentType.Type.IMAGE) {
                        if (SettingValues.image) {
                            Intent myIntent = new Intent(contextActivity, MediaView.class);
                            String url;
                            url = submission.getUrl();
                            myIntent.putExtra(MediaView.EXTRA_DISPLAY_URL, submission.getUrl());
                            myIntent.putExtra(MediaView.EXTRA_URL, url);
                            myIntent.putExtra(MediaView.SUBREDDIT, submission.getSubredditName());
                            myIntent.putExtra(MediaView.EXTRA_SHARE_URL, submission.getUrl());

                            contextActivity.startActivity(myIntent);
                        } else {
                            LinkUtil.openExternally(submission.getUrl());
                        }
                    }
                }
            }
        });
    }


    public void doLoadGif(final String dat) {
        isGif = true;
        videoView = rootView.findViewById(R.id.gif);
        videoView.clearFocus();
        rootView.findViewById(R.id.gifarea).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.submission_image).setVisibility(View.GONE);
        final ProgressBar loader = rootView.findViewById(R.id.gifprogress);
        rootView.findViewById(R.id.progress).setVisibility(View.GONE);
        gif = new GifUtils.AsyncLoadGif(getActivity(), videoView, loader,
                rootView.findViewById(R.id.placeholder), false, true, sub);
        gif.execute(dat);
    }

    public void doLoadDeviantArt(String url) {
        final String apiUrl = "http://backend.deviantart.com/oembed?url=" + url;
        LogUtil.v(apiUrl);
        new AsyncTask<Void, Void, JsonObject>() {
            @Override
            protected JsonObject doInBackground(Void... params) {
                return HttpUtil.getJsonObject(client, gson, apiUrl);
            }

            @Override
            protected void onPostExecute(JsonObject result) {
                LogUtil.v("doLoad onPostExecute() called with: " + "result = [" + result + "]");
                if (result != null && !result.isJsonNull() && (result.has("fullsize_url")
                        || result.has("url"))) {

                    String url;
                    if (result.has("fullsize_url")) {
                        url = result.get("fullsize_url").getAsString();
                    } else {
                        url = result.get("url").getAsString();
                    }
                    doLoadImage(url);
                } else {
                    Intent i = new Intent(getActivity(), Website.class);
                    i.putExtra(LinkUtil.EXTRA_URL, contentUrl);
                    getActivity().startActivity(i);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void doLoadImgur(String url) {
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        final String finalUrl = url;
        String hash = url.substring(url.lastIndexOf("/"), url.length());

        if (NetworkUtil.isConnected(getActivity())) {

            if (hash.startsWith("/")) hash = hash.substring(1, hash.length());
            final String apiUrl = "https://imgur-apiv3.p.mashape.com/3/image/" + hash + ".json";
            LogUtil.v(apiUrl);

            new AsyncTask<Void, Void, JsonObject>() {
                @Override
                protected JsonObject doInBackground(Void... params) {
                    return HttpUtil.getImgurMashapeJsonObject(client, gson, apiUrl, mashapeKey);
                }

                @Override
                protected void onPostExecute(JsonObject result) {
                    if (result != null && !result.isJsonNull() && result.has("error")) {
                        LogUtil.v("Error loading content");
                        (getActivity()).finish();
                    } else {
                        try {
                            if (result != null && !result.isJsonNull() && result.has("image")) {
                                String type = result.get("image")
                                        .getAsJsonObject()
                                        .get("image")
                                        .getAsJsonObject()
                                        .get("type")
                                        .getAsString();
                                String urls = result.get("image")
                                        .getAsJsonObject()
                                        .get("links")
                                        .getAsJsonObject()
                                        .get("original")
                                        .getAsString();

                                if (type.contains("gif")) {
                                    doLoadGif(urls);
                                } else if (!imageShown) { //only load if there is no image
                                    doLoadImage(urls);
                                }
                            } else if (result != null && result.has("data")) {
                                String type = result.get("data")
                                        .getAsJsonObject()
                                        .get("type")
                                        .getAsString();
                                String urls = result.get("data")
                                        .getAsJsonObject()
                                        .get("link")
                                        .getAsString();
                                String mp4 = "";
                                if (result.get("data").getAsJsonObject().has("mp4")) {
                                    mp4 = result.get("data")
                                            .getAsJsonObject()
                                            .get("mp4")
                                            .getAsString();
                                }

                                if (type.contains("gif")) {
                                    doLoadGif(((mp4 == null || mp4.isEmpty()) ? urls : mp4));
                                } else if (!imageShown) { //only load if there is no image
                                    doLoadImage(urls);
                                }
                            } else {
                                if (!imageShown) doLoadImage(finalUrl);
                            }
                        } catch (Exception e) {
                            LogUtil.e(e, "Error loading Imgur image finalUrl = ["
                                    + finalUrl
                                    + "], apiUrl = ["
                                    + apiUrl
                                    + "]");
                            //todo open it?
                        }
                    }
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void doLoadImage(String contentUrl) {
        if (contentUrl != null && contentUrl.contains("bildgur.de")) {
            contentUrl = contentUrl.replace("b.bildgur.de", "i.imgur.com");
        }
        if (contentUrl != null && ContentType.isImgurLink(contentUrl)) {
            contentUrl = contentUrl + ".png";
        }

        rootView.findViewById(R.id.gifprogress).setVisibility(View.GONE);

        if (contentUrl != null && contentUrl.contains("m.imgur.com")) {
            contentUrl = contentUrl.replace("m.imgur.com", "i.imgur.com");
        }

        if ((contentUrl != null
                && !contentUrl.startsWith("https://i.redditmedia.com")
                && !contentUrl.startsWith("https://i.reddituploads.com")
                && !contentUrl.contains(
                "imgur.com"))) { //we can assume redditmedia and imgur links are to direct images and not websites
            rootView.findViewById(R.id.progress).setVisibility(View.VISIBLE);
            ((ProgressBar) rootView.findViewById(R.id.progress)).setIndeterminate(true);

            final String finalUrl2 = contentUrl;
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        URL obj = new URL(finalUrl2);
                        URLConnection conn = obj.openConnection();
                        final String type = conn.getHeaderField("Content-Type");
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (!imageShown
                                            && type != null
                                            && !type.isEmpty()
                                            && type.startsWith("image/")) {
                                        //is image
                                        if (type.contains("gif")) {
                                            doLoadGif(finalUrl2.replace(".jpg", ".gif")
                                                    .replace(".png", ".gif"));
                                        } else if (!imageShown) {
                                            displayImage(finalUrl2);
                                        }
                                        actuallyLoaded = finalUrl2;
                                    } else if (!imageShown) {
                                        Intent i = new Intent(getActivity(), Website.class);
                                        i.putExtra(LinkUtil.EXTRA_URL, finalUrl2);
                                        getActivity().startActivity(i);
                                    }
                                }
                            });
                        }

                    } catch (IOException e) {
                        LogUtil.e(e, "Error loading image finalUrl2 = [" + finalUrl2 + "]");
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    rootView.findViewById(R.id.progress).setVisibility(View.GONE);
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        } else {
            displayImage(contentUrl);
        }


        actuallyLoaded = contentUrl;
    }

    public void displayImage(final String url) {
        if (!imageShown) {
            actuallyLoaded = url;
            final SubsamplingScaleImageView i = rootView.findViewById(R.id.submission_image);

            i.setMinimumDpi(70);
            i.setMinimumTileDpi(240);
            final ProgressBar bar = rootView.findViewById(R.id.progress);
            bar.setIndeterminate(false);
            bar.setProgress(0);

            final Handler handler = new Handler();
            final Runnable progressBarDelayRunner = new Runnable() {
                public void run() {
                    bar.setVisibility(View.VISIBLE);
                }
            };
            handler.postDelayed(progressBarDelayRunner, 500);

            ImageView fakeImage = new ImageView(getActivity());
            fakeImage.setLayoutParams(new LinearLayout.LayoutParams(i.getWidth(), i.getHeight()));
            fakeImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

            File f = ((Reddit) getActivity().getApplicationContext()).getImageLoader()
                    .getDiskCache()
                    .get(url);
            if (f != null && f.exists()) {
                imageShown = true;

                try {
                    i.setImage(ImageSource.uri(f.getAbsolutePath()));
                } catch (Exception e) {
                    //todo  i.setImage(ImageSource.bitmap(loadedImage));
                }
                (rootView.findViewById(R.id.progress)).setVisibility(View.GONE);
                handler.removeCallbacks(progressBarDelayRunner);

                previous = i.scale;
                final float base = i.scale;
                i.setOnStateChangedListener(new SubsamplingScaleImageView.OnStateChangedListener() {
                    @Override
                    public void onScaleChanged(float newScale, int origin) {
                        if (newScale > previous && !hidden && newScale > base) {
                            hidden = true;
                            final View base = rootView.findViewById(R.id.base);

                            ValueAnimator va = ValueAnimator.ofFloat(1.0f, 0.2f);
                            int mDuration = 250; //in millis
                            va.setDuration(mDuration);
                            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                public void onAnimationUpdate(ValueAnimator animation) {
                                    Float value = (Float) animation.getAnimatedValue();
                                    base.setAlpha(value);
                                }
                            });
                            va.start();
                            //hide
                        } else if (newScale <= previous && hidden) {
                            hidden = false;
                            final View base = rootView.findViewById(R.id.base);

                            ValueAnimator va = ValueAnimator.ofFloat(0.2f, 1.0f);
                            int mDuration = 250; //in millis
                            va.setDuration(mDuration);
                            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                public void onAnimationUpdate(ValueAnimator animation) {
                                    Float value = (Float) animation.getAnimatedValue();
                                    base.setAlpha(value);
                                }
                            });
                            va.start();
                            //unhide
                        }
                        previous = newScale;
                    }

                    @Override
                    public void onCenterChanged(PointF newCenter, int origin) {

                    }
                });
            } else {
                ((Reddit) getActivity().getApplicationContext()).getImageLoader()
                        .displayImage(url, new ImageViewAware(fakeImage),
                                new DisplayImageOptions.Builder().resetViewBeforeLoading(true)
                                        .cacheOnDisk(true)
                                        .imageScaleType(ImageScaleType.NONE)
                                        .cacheInMemory(false)
                                        .build(), new ImageLoadingListener() {
                                    private View mView;

                                    @Override
                                    public void onLoadingStarted(String imageUri, View view) {
                                        imageShown = true;
                                        mView = view;
                                    }

                                    @Override
                                    public void onLoadingFailed(String imageUri, View view,
                                            FailReason failReason) {
                                        Log.v(LogUtil.getTag(), "LOADING FAILED");

                                    }

                                    @Override
                                    public void onLoadingComplete(String imageUri, View view,
                                            Bitmap loadedImage) {
                                        imageShown = true;
                                        File f = null;
                                        if (getActivity() != null) {
                                            f =
                                                    ((Reddit) getActivity().getApplicationContext())
                                                            .getImageLoader()
                                                            .getDiskCache()
                                                            .get(url);
                                        }
                                        if (f != null && f.exists()) {
                                            i.setImage(ImageSource.uri(f.getAbsolutePath()));
                                        } else {
                                            i.setImage(ImageSource.bitmap(loadedImage));
                                        }
                                        (rootView.findViewById(R.id.progress)).setVisibility(
                                                View.GONE);
                                        handler.removeCallbacks(progressBarDelayRunner);

                                        previous = i.scale;
                                        final float base = i.scale;
                                        i.setOnStateChangedListener(
                                                new SubsamplingScaleImageView.OnStateChangedListener() {
                                                    @Override
                                                    public void onScaleChanged(float newScale, int origin) {
                                                        if (newScale > previous
                                                                && !hidden
                                                                && newScale > base) {
                                                            hidden = true;
                                                            final View base = rootView.findViewById(
                                                                    R.id.base);

                                                            ValueAnimator va =
                                                                    ValueAnimator.ofFloat(1.0f,
                                                                            0.2f);
                                                            int mDuration = 250; //in millis
                                                            va.setDuration(mDuration);
                                                            va.addUpdateListener(
                                                                    new ValueAnimator.AnimatorUpdateListener() {
                                                                        public void onAnimationUpdate(
                                                                                ValueAnimator animation) {
                                                                            Float value =
                                                                                    (Float) animation
                                                                                            .getAnimatedValue();
                                                                            base.setAlpha(value);
                                                                        }
                                                                    });
                                                            va.start();
                                                            //hide
                                                        } else if (newScale <= previous && hidden) {
                                                            hidden = false;
                                                            final View base = rootView.findViewById(
                                                                    R.id.base);

                                                            ValueAnimator va =
                                                                    ValueAnimator.ofFloat(0.2f,
                                                                            1.0f);
                                                            int mDuration = 250; //in millis
                                                            va.setDuration(mDuration);
                                                            va.addUpdateListener(
                                                                    new ValueAnimator.AnimatorUpdateListener() {
                                                                        public void onAnimationUpdate(
                                                                                ValueAnimator animation) {
                                                                            Float value =
                                                                                    (Float) animation
                                                                                            .getAnimatedValue();
                                                                            base.setAlpha(value);
                                                                        }
                                                                    });
                                                            va.start();
                                                            //unhide
                                                        }
                                                        previous = newScale;
                                                    }

                                                    @Override
                                                    public void onCenterChanged(PointF newCenter, int origin) {

                                                    }
                                                });
                                    }

                                    @Override
                                    public void onLoadingCancelled(String imageUri, View view) {
                                        Log.v(LogUtil.getTag(), "LOADING CANCELLED");

                                    }
                                }, new ImageLoadingProgressListener() {
                                    @Override
                                    public void onProgressUpdate(String imageUri, View view,
                                            int current, int total) {
                                        ((ProgressBar) rootView.findViewById(
                                                R.id.progress)).setProgress(
                                                Math.round(100.0f * current / total));
                                    }
                                });
            }
        }
    }
}
