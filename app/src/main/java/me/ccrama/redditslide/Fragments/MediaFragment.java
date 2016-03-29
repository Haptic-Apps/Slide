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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;

import net.dean.jraw.models.Submission;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import me.ccrama.redditslide.Activities.Album;
import me.ccrama.redditslide.Activities.AlbumPager;
import me.ccrama.redditslide.Activities.CommentsScreen;
import me.ccrama.redditslide.Activities.FullscreenVideo;
import me.ccrama.redditslide.Activities.GifView;
import me.ccrama.redditslide.Activities.Website;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.PostMatch;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SubmissionViews.PopulateShadowboxInfo;
import me.ccrama.redditslide.SubmissionViews.PopulateSubmissionViewHolder;
import me.ccrama.redditslide.Views.ImageSource;
import me.ccrama.redditslide.Views.MediaVideoView;
import me.ccrama.redditslide.Views.SubsamplingScaleImageView;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.CustomTabUtil;
import me.ccrama.redditslide.util.GifUtils;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.NetworkUtil;


/**
 * Created by ccrama on 6/2/2015.
 */
public class MediaFragment extends Fragment {


    ViewGroup rootView;
    boolean imageShown;
    private float previous;
    private boolean hidden;
    Submission s;
    @Override
    public void onResume(){
        super.onResume();
        if(videoView != null){
            videoView.seekTo(stopPosition);
            videoView.start();
        }
    }
    int stopPosition;
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(videoView != null) {
            stopPosition = videoView.getCurrentPosition();
            videoView.pause();
            outState.putInt("position", stopPosition);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(
                R.layout.submission_mediacard, container, false);
        if (!firstUrl.isEmpty()) {
            imageShown = true;
            LogUtil.v("Displaying first image");
            displayImage(firstUrl);
        }

        if(savedInstanceState != null && savedInstanceState.containsKey("position")) stopPosition = savedInstanceState.getInt("position");


        doLoad(contentUrl);
        PopulateShadowboxInfo.doActionbar(s, rootView, getActivity());

        (rootView.findViewById(R.id.thumbimage2)).setVisibility(View.GONE);


        ContentType.ImageType type = ContentType.getImageType(s);


        if (!type.toString().toLowerCase().contains("image") && type != ContentType.ImageType.DEVIANTART && !type.toString().toLowerCase().contains("gfy") && !type.toString().toLowerCase().contains("gif") && !type.toString().toLowerCase().contains("imgur") || type.toString().toLowerCase().contains("link")) {
            if (!s.getDataNode().has("preview") || !s.getDataNode().get("preview").get("images").get(0).get("source").has("height") || s.getDataNode().get("preview").get("images").get(0).get("source").get("height").asInt() <= 200) {
                (rootView.findViewById(R.id.thumbimage2)).setVisibility(View.VISIBLE);
                ((ImageView) rootView.findViewById(R.id.thumbimage2)).setImageResource(R.drawable.web);
                addClickFunctions((rootView.findViewById(R.id.thumbimage2)), rootView, type, getActivity(), s);
                (rootView.findViewById(R.id.progress)).setVisibility(View.GONE);
            } else {
                addClickFunctions((rootView.findViewById(R.id.submission_image)), rootView, type, getActivity(), s);
            }
        } else {
            (rootView.findViewById(R.id.thumbimage2)).setVisibility(View.GONE);
        }


        rootView.findViewById(R.id.base).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i2 = new Intent(getActivity(), CommentsScreen.class);
                i2.putExtra(CommentsScreen.EXTRA_PAGE, i);
                i2.putExtra(CommentsScreen.EXTRA_SUBREDDIT, sub);
                (getActivity()).startActivity(i2);

            }
        });
        return rootView;
    }

    public String firstUrl;
    public String contentUrl;
    public String sub;
    public int i;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        firstUrl = bundle.getString("firstUrl");
        sub = bundle.getString("sub");
        i = bundle.getInt("page");
        s = OfflineSubreddit.getSubreddit(sub).submissions.get(i);
        contentUrl = bundle.getString("contentUrl");
    }

    public void doLoad(String contentUrl) {
        switch (ContentType.getImageType(contentUrl)) {
            case NSFW_IMAGE:
                doLoadImage(contentUrl);
                break;
            case NSFW_GIF:
                doLoadGif(contentUrl);
                break;
            case NSFW_GFY:
                doLoadGif(contentUrl);
                break;
            case IMAGE_LINK:
                doLoadImage(contentUrl);
                break;
            case DEVIANTART:
                Ion.with(this).load("http://backend.deviantart.com/oembed?url=" + contentUrl).asJsonObject().setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (result != null && !result.isJsonNull() && (result.has("fullsize_url") || result.has("url"))) {

                            String url;
                            if (result.has("fullsize_url")) {
                                url = result.get("fullsize_url").getAsString();
                            } else {
                                url = result.get("url").getAsString();
                            }
                            doLoadImage(url);
                        } else {
                            if (!s.getDataNode().has("preview") || !s.getDataNode().get("preview").get("images").get(0).get("source").has("height") || s.getDataNode().get("preview").get("images").get(0).get("source").get("height").asInt() <= 200) {
                                (rootView.findViewById(R.id.thumbimage2)).setVisibility(View.VISIBLE);
                                ((ImageView) rootView.findViewById(R.id.thumbimage2)).setImageResource(R.drawable.web);
                                addClickFunctions((rootView.findViewById(R.id.thumbimage2)), rootView, ContentType.ImageType.IMAGE_LINK, getActivity(), s);
                                (rootView.findViewById(R.id.progress)).setVisibility(View.GONE);
                            } else {
                                addClickFunctions((rootView.findViewById(R.id.submission_image)), rootView, ContentType.ImageType.IMAGE_LINK, getActivity(), s);
                            }
                        }
                    }
                });
                break;
            case GFY:
                doLoadGif(contentUrl);
                break;
            case IMAGE:
                doLoadImage(contentUrl);
                break;
            case IMGUR:
                doLoadImgur(contentUrl);
                break;
            case GIF:
                doLoadGif(contentUrl);
                break;
            case NONE_GFY:
                doLoadGif(contentUrl);
                break;
            case NONE_GIF:
                doLoadGif(contentUrl);
                break;
            case NONE_IMAGE:
                doLoadImage(contentUrl);
                break;
        }
    }
    MediaVideoView videoView;
    public void doLoadGif(final String dat) {
        videoView = (MediaVideoView) rootView.findViewById(R.id.gif);
        videoView.clearFocus();
        rootView.findViewById(R.id.gifarea).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.submission_image).setVisibility(View.GONE);
        final ProgressBar loader = (ProgressBar) rootView.findViewById(R.id.gifprogress);
        rootView.findViewById(R.id.progress).setVisibility(View.GONE);
        new GifUtils.AsyncLoadGif(getActivity(), (MediaVideoView) rootView.findViewById(R.id.gif), loader, rootView.findViewById(R.id.placeholder), rootView.findViewById(R.id.save), true, false).execute(dat);
    }

    public void doLoadImgur(String url) {
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        String hash = url.substring(url.lastIndexOf("/"), url.length());

        if (NetworkUtil.isConnected(getActivity())) {
            final String finalUrl = url;
            final String finalUrl1 = url;
            LogUtil.v("Loading" + "https://api.imgur.com/2/image/" + hash + ".json");
            Ion.with(this).load("https://api.imgur.com/2/image/" + hash + ".json")
                    .asJsonObject().setCallback(new FutureCallback<JsonObject>() {
                                                    @Override
                                                    public void onCompleted(Exception e, JsonObject obj) {
                                                        if (obj != null && !obj.isJsonNull() && obj.has("error")) {
                                                            LogUtil.v("Error loading content");
                                                            (getActivity()).finish();
                                                        } else {
                                                            try {
                                                                if (!obj.isJsonNull() && obj.has("image")) {
                                                                    String type = obj.get("image").getAsJsonObject().get("image").getAsJsonObject().get("type").getAsString();
                                                                    String urls = obj.get("image").getAsJsonObject().get("links").getAsJsonObject().get("original").getAsString();

                                                                    if (type.contains("gif")) {
                                                                        doLoadGif(urls);
                                                                    } else if (!imageShown) { //only load if there is no image
                                                                        doLoadImage(urls);
                                                                    }
                                                                } else {
                                                                    if (!imageShown)
                                                                        doLoadImage(finalUrl1);
                                                                }
                                                            } catch (Exception e2) {
                                                                Intent i = new Intent(getActivity(), Website.class);
                                                                i.putExtra(Website.EXTRA_URL, finalUrl);
                                                                getActivity().startActivity(i);
                                                            }
                                                        }
                                                    }
                                                }

            );
        }
    }

    public void doLoadImage(String contentUrl) {
        if (contentUrl != null && ContentType.isImgurLink(contentUrl)) {
            contentUrl = contentUrl + ".png";
        }
        rootView.findViewById(R.id.gifprogress).setVisibility(View.GONE);
        LogUtil.v(contentUrl);
        if ((contentUrl != null && !contentUrl.startsWith("https://i.redditmedia.com") && !contentUrl.contains("imgur.com")) || contentUrl != null && contentUrl.contains(".jpg") && !contentUrl.contains("i.redditmedia.com") && Authentication.didOnline) { //we can assume redditmedia and imgur links are to direct images and not websites
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
                                if (type != null && !type.isEmpty() && type.startsWith("image/")) {
                                    //is image
                                    if (type.contains("gif")) {
                                        doLoadGif(finalUrl2.replace(".jpg", ".gif"));
                                    } else if (!imageShown) {
                                        displayImage(finalUrl2);
                                    }
                                } else {
                                    Intent i = new Intent(getActivity(), Website.class);
                                    i.putExtra(Website.EXTRA_URL, finalUrl2);
                                    getActivity().startActivity(i);
                                }
                            }
                        });

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute();

        } else if (contentUrl != null && !imageShown) {
            displayImage(contentUrl);
        }
    }


    public void displayImage(String url) {
        final SubsamplingScaleImageView i = (SubsamplingScaleImageView) rootView.findViewById(R.id.submission_image);

        i.setMinimumDpi(100);
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


        ((Reddit) getActivity().getApplication()).getImageLoader()
                .displayImage(url, new ImageViewAware(fakeImage), new DisplayImageOptions.Builder()
                        .resetViewBeforeLoading(true)
                        .cacheOnDisk(true)
                        .imageScaleType(ImageScaleType.NONE)
                        .cacheInMemory(false)
                        .build(), new ImageLoadingListener() {
                    private View mView;

                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        mView = view;
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        Log.v(LogUtil.getTag(), "LOADING FAILED");

                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

                        i.setImage(ImageSource.cachedBitmap(loadedImage));


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

        i.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v2) {
                //todo open
            }
        });


    }
    private static void addClickFunctions(final View base, final View clickingArea, ContentType.ImageType type, final Activity contextActivity, final Submission submission) {
        if(!PostMatch.openExternal(submission.getUrl())) {

            switch (type) {
                case NSFW_IMAGE:
                    base.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v2) {
                            PopulateSubmissionViewHolder.openImage(contextActivity, submission);

                        }
                    });
                    break;
                case STREAMABLE:
                    base.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (SettingValues.video) {
                                Intent myIntent = new Intent(contextActivity, GifView.class);

                                myIntent.putExtra(GifView.EXTRA_STREAMABLE, submission.getUrl());
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
                case NSFW_GIF:

                    base.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v2) {
                            PopulateSubmissionViewHolder.openGif(false, contextActivity, submission);

                        }
                    });
                    break;
                case NSFW_GFY:

                    base.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v2) {
                            PopulateSubmissionViewHolder.openGif(true, contextActivity, submission);

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
                case IMAGE_LINK:
                    base.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v2) {
                            CustomTabUtil.openUrl(
                                    submission.getUrl(), Palette.getColor(submission.getSubredditName()), contextActivity);
                        }
                    });
                    break;
                case NSFW_LINK:
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
                case GFY:
                    base.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v2) {
                            PopulateSubmissionViewHolder.openGif(true, contextActivity, submission);

                        }
                    });
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
                            PopulateSubmissionViewHolder.openImage(contextActivity, submission);

                        }
                    });
                    break;
                case GIF:
                    base.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v2) {
                            PopulateSubmissionViewHolder.openGif(false, contextActivity, submission);

                        }
                    });
                    break;
                case NONE_GFY:
                    base.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v2) {
                            PopulateSubmissionViewHolder.openGif(true, contextActivity, submission);

                        }
                    });
                    break;
                case NONE_GIF:
                    base.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v2) {
                            PopulateSubmissionViewHolder.openGif(false, contextActivity, submission);

                        }
                    });
                    break;

                case NONE:

                    break;
                case NONE_IMAGE:
                    base.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v2) {
                            PopulateSubmissionViewHolder.openImage(contextActivity, submission);


                        }
                    });
                    break;
                case NONE_URL:
                    base.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v2) {
                            CustomTabUtil.openUrl(
                                    submission.getUrl(), Palette.getColor(submission.getSubredditName()), contextActivity);
                        }
                    });
                    break;
                case VIDEO:
                    base.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Reddit.defaultShare(submission.getUrl(), contextActivity);
                        }
                    });
            }
        } else {
            Reddit.defaultShare(submission.getUrl(), contextActivity);
        }
    }


}
