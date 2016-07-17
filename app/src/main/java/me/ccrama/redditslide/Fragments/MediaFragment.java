package me.ccrama.redditslide.Fragments;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import net.dean.jraw.models.Submission;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import me.ccrama.redditslide.Activities.Album;
import me.ccrama.redditslide.Activities.AlbumPager;
import me.ccrama.redditslide.Activities.CommentsScreen;
import me.ccrama.redditslide.Activities.FullscreenVideo;
import me.ccrama.redditslide.Activities.MediaView;
import me.ccrama.redditslide.Activities.Shadowbox;
import me.ccrama.redditslide.Activities.Website;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SecretConstants;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SubmissionViews.PopulateShadowboxInfo;
import me.ccrama.redditslide.SubmissionViews.PopulateSubmissionViewHolder;
import me.ccrama.redditslide.Views.ImageSource;
import me.ccrama.redditslide.Views.MediaVideoView;
import me.ccrama.redditslide.Views.SubsamplingScaleImageView;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.CustomTabUtil;
import me.ccrama.redditslide.util.GifUtils;
import me.ccrama.redditslide.util.HttpUtil;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.NetworkUtil;
import okhttp3.OkHttpClient;


/**
 * Created by ccrama on 6/2/2015.
 */
public class MediaFragment extends Fragment {

    public String firstUrl;
    public String contentUrl;
    public String sub;
    public String actuallyLoaded;
    public int i;
    private ViewGroup rootView;
    private MediaVideoView videoView;
    private boolean imageShown;
    private float previous;
    private boolean hidden;
    private int stopPosition;
    public boolean isGif;
    private GifUtils.AsyncLoadGif gif;
    private Submission s;
    private OkHttpClient client;
    private Gson gson;
    private String mashapeKey;

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
            videoView.start();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (videoView != null) {
            videoView.seekTo(stopPosition);
            videoView.start();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (videoView != null) {
            stopPosition = videoView.getCurrentPosition();
            videoView.pause();
            ((SlidingUpPanelLayout) rootView.findViewById(R.id.sliding_layout)).setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            outState.putInt("position", stopPosition);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(
                R.layout.submission_mediacard, container, false);
        if (savedInstanceState != null && savedInstanceState.containsKey("position"))
            stopPosition = savedInstanceState.getInt("position");
        if (!firstUrl.isEmpty()) {
            displayImage(firstUrl);
        }


        PopulateShadowboxInfo.doActionbar(s, rootView, getActivity(), true);

        (rootView.findViewById(R.id.thumbimage2)).setVisibility(View.GONE);


        ContentType.Type type = ContentType.getContentType(s);


        if (!ContentType.fullImage(type)) {
            if (!s.getDataNode().has("preview") || !s.getDataNode().get("preview").get("images").get(0).get("source").has("height")) {
                (rootView.findViewById(R.id.thumbimage2)).setVisibility(View.VISIBLE);
                ((ImageView) rootView.findViewById(R.id.thumbimage2)).setImageResource(R.drawable.web);
                addClickFunctions((rootView.findViewById(R.id.thumbimage2)), rootView, type, getActivity(), s);
                (rootView.findViewById(R.id.progress)).setVisibility(View.GONE);
            } else {
                addClickFunctions((rootView.findViewById(R.id.submission_image)), rootView, type, getActivity(), s);
            }
        } else {
            (rootView.findViewById(R.id.thumbimage2)).setVisibility(View.GONE);
            addClickFunctions((rootView.findViewById(R.id.submission_image)), rootView, type, getActivity(), s);
        }
        doLoad(contentUrl);


        rootView.findViewById(R.id.base).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i2 = new Intent(getActivity(), CommentsScreen.class);
                i2.putExtra(CommentsScreen.EXTRA_PAGE, i);
                i2.putExtra(CommentsScreen.EXTRA_SUBREDDIT, sub);
                getActivity().startActivity(i2);

            }
        });
        final View.OnClickListener openClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SlidingUpPanelLayout) rootView.findViewById(R.id.sliding_layout)).setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            }
        };
        rootView.findViewById(R.id.base).setOnClickListener(openClick);
        final View title = rootView.findViewById(R.id.title);
        title.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ((SlidingUpPanelLayout) rootView.findViewById(R.id.sliding_layout)).setPanelHeight(title.getMeasuredHeight());
                title.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        ((SlidingUpPanelLayout) rootView.findViewById(R.id.sliding_layout)).addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    rootView.findViewById(R.id.base).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i2 = new Intent(getActivity(), CommentsScreen.class);
                            i2.putExtra(CommentsScreen.EXTRA_PAGE, i);
                            i2.putExtra(CommentsScreen.EXTRA_SUBREDDIT, sub);
                            getActivity().startActivity(i2);
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
        firstUrl = bundle.getString("firstUrl");
        sub = ((Shadowbox) getActivity()).subreddit;
        i = bundle.getInt("page");
        if (((Shadowbox) getActivity()).subredditPosts.getPosts().size() != 0)
            s = ((Shadowbox) getActivity()).subredditPosts.getPosts().get(i);
        else
            getActivity().finish();
        contentUrl = bundle.getString("contentUrl");

        client = new OkHttpClient();
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
            case VID_ME:
            case STREAMABLE:
            case GIF:
                doLoadGif(contentUrl);
                break;
        }
    }

    private static void addClickFunctions(final View base, final View clickingArea, ContentType.Type type, final Activity contextActivity, final Submission submission) {
        switch (type) {
            case VID_ME:
            case STREAMABLE:
                base.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (SettingValues.video) {
                            Intent myIntent = new Intent(contextActivity, MediaView.class);
                            myIntent.putExtra(MediaView.EXTRA_URL, submission.getUrl());
                            contextActivity.startActivity(myIntent);

                        } else {
                            Reddit.defaultShare(submission.getUrl(), contextActivity);
                        }
                    }
                });
            case EMBEDDED:
                base.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v2) {
                        if (SettingValues.video) {
                            Reddit.defaultShare(submission.getUrl(), contextActivity);
                            String data = submission.getDataNode().get("media_embed").get("content").asText();
                            {
                                Intent i = new Intent(contextActivity, FullscreenVideo.class);
                                i.putExtra(FullscreenVideo.EXTRA_HTML, data);
                                contextActivity.startActivity(i);
                            }
                        } else {
                            Reddit.defaultShare(submission.getUrl(), contextActivity);
                        }
                    }

                });
                break;
            case REDDIT:
                base.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v2) {
                        PopulateSubmissionViewHolder.openRedditContent(submission.getUrl(), contextActivity);
                    }
                });
                break;
            case LINK:
                base.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v2) {
                        CustomTabUtil.openUrl(
                                submission.getUrl(), Palette.getColor(submission.getSubredditName()), contextActivity);
                    }
                });
                break;
            case SELF:

                break;
            case ALBUM:
                base.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v2) {

                        if (SettingValues.album) {
                            if (SettingValues.albumSwipe) {
                                Intent i = new Intent(contextActivity, AlbumPager.class);
                                i.putExtra(Album.EXTRA_URL, submission.getUrl());
                                contextActivity.startActivity(i);
                                contextActivity.overridePendingTransition(R.anim.slideright, R.anim.fade_out);
                            } else {
                                Intent i = new Intent(contextActivity, Album.class);
                                i.putExtra(Album.EXTRA_URL, submission.getUrl());
                                contextActivity.startActivity(i);
                                contextActivity.overridePendingTransition(R.anim.slideright, R.anim.fade_out);
                            }


                        } else {
                            Reddit.defaultShare(submission.getUrl(), contextActivity);
                        }

                    }
                });
                break;
            case DEVIANTART:
            case IMAGE:
                base.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v2) {
                        PopulateSubmissionViewHolder.openImage(contextActivity, submission, null, -1);
                    }
                });
                break;
            case GIF:
                base.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v2) {
                        PopulateSubmissionViewHolder.openGif(contextActivity, submission, -1);
                    }
                });
                break;
            case NONE:

                break;
            case VIDEO:
                base.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (Reddit.videoPlugin) {
                            try {
                                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                                sharingIntent.setClassName("ccrama.me.slideyoutubeplugin",
                                        "ccrama.me.slideyoutubeplugin.YouTubeView");
                                sharingIntent.putExtra("url", submission.getUrl());
                                contextActivity.startActivity(sharingIntent);

                            } catch (Exception e) {
                                Reddit.defaultShare(submission.getUrl(), contextActivity);
                            }
                        } else {
                            Reddit.defaultShare(submission.getUrl(), contextActivity);
                        }
                    }
                });
        }

    }

    public void doLoadGif(final String dat) {
        isGif = true;
        videoView = (MediaVideoView) rootView.findViewById(R.id.gif);
        videoView.clearFocus();
        videoView.setZOrderOnTop(true);
        rootView.findViewById(R.id.gifarea).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.submission_image).setVisibility(View.GONE);
        final ProgressBar loader = (ProgressBar) rootView.findViewById(R.id.gifprogress);
        rootView.findViewById(R.id.progress).setVisibility(View.GONE);
        gif = new GifUtils.AsyncLoadGif(getActivity(), (MediaVideoView) rootView.findViewById(R.id.gif), loader, rootView.findViewById(R.id.placeholder), false, false, false);
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
                    i.putExtra(Website.EXTRA_URL, contentUrl);
                    getActivity().startActivity(i);
                }
            }
        }.execute();
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
                            Intent i = new Intent(getActivity(), Website.class);
                            i.putExtra(Website.EXTRA_URL, finalUrl);
                            getActivity().startActivity(i);
                        }
                    }
                }
            }.execute();
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
                                    i.putExtra(Website.EXTRA_URL, finalUrl2);
                                    getActivity().startActivity(i);
                                }
                            }
                        });

                    } catch (IOException e) {
                        LogUtil.e(e, "Error loading image finalUrl2 = [" + finalUrl2 + "]");
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    rootView.findViewById(R.id.progress).setVisibility(View.GONE);
                }
            }.execute();

        } else {
            displayImage(contentUrl);
        }


        actuallyLoaded = contentUrl;
    }

    public void displayImage(final String url) {
        if (!imageShown) {
            actuallyLoaded = url;
            final SubsamplingScaleImageView i = (SubsamplingScaleImageView) rootView.findViewById(R.id.submission_image);

            i.setMinimumDpi(70);
            i.setMinimumTileDpi(240);
            final ProgressBar bar = (ProgressBar) rootView.findViewById(R.id.progress);
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

            File f = ((Reddit) getActivity().getApplicationContext()).getImageLoader().getDiscCache().get(url);
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
                i.setOnZoomChangedListener(new SubsamplingScaleImageView.OnZoomChangedListener() {
                    @Override
                    public void onZoomLevelChanged(float zoom) {
                        if (zoom > previous && !hidden && zoom > base) {
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
                        } else if (zoom <= previous && hidden) {
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
                        previous = zoom;
                    }
                });
            } else {
                ((Reddit) getActivity().getApplicationContext()).getImageLoader()
                        .displayImage(url, new ImageViewAware(fakeImage), new DisplayImageOptions.Builder()
                                .resetViewBeforeLoading(true)
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
                            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                                Log.v(LogUtil.getTag(), "LOADING FAILED");

                            }

                            @Override
                            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                imageShown = true;
                                File f = null;
                                if (getActivity() != null)
                                    f = ((Reddit) getActivity().getApplicationContext()).getImageLoader().getDiscCache().get(url);
                                if (f != null && f.exists()) {
                                    i.setImage(ImageSource.uri(f.getAbsolutePath()));
                                } else {
                                    i.setImage(ImageSource.bitmap(loadedImage));
                                }
                                (rootView.findViewById(R.id.progress)).setVisibility(View.GONE);
                                handler.removeCallbacks(progressBarDelayRunner);

                                previous = i.scale;
                                final float base = i.scale;
                                i.setOnZoomChangedListener(new SubsamplingScaleImageView.OnZoomChangedListener() {
                                    @Override
                                    public void onZoomLevelChanged(float zoom) {
                                        if (zoom > previous && !hidden && zoom > base) {
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
                                        } else if (zoom <= previous && hidden) {
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
                                        previous = zoom;
                                    }
                                });
                            }

                            @Override
                            public void onLoadingCancelled(String imageUri, View view) {
                                Log.v(LogUtil.getTag(), "LOADING CANCELLED");

                            }
                        }, new ImageLoadingProgressListener() {
                            @Override
                            public void onProgressUpdate(String imageUri, View view, int current, int total) {
                                ((ProgressBar) rootView.findViewById(R.id.progress)).setProgress(Math.round(100.0f * current / total));
                            }
                        });
            }

            rootView.findViewById(R.id.submission_image).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v2) {
                    getActivity().finish();
                }
            });
        }
    }
}
