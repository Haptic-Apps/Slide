package me.ccrama.redditslide.Fragments;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;

import net.dean.jraw.models.Submission;

import me.ccrama.redditslide.Activities.Album;
import me.ccrama.redditslide.Activities.AlbumPager;
import me.ccrama.redditslide.Activities.CommentsScreen;
import me.ccrama.redditslide.Activities.FullscreenVideo;
import me.ccrama.redditslide.Activities.GifView;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.ImageLoaderUtils;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.PostMatch;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SubmissionViews.PopulateShadowboxInfo;
import me.ccrama.redditslide.SubmissionViews.PopulateSubmissionViewHolder;
import me.ccrama.redditslide.Views.ImageSource;
import me.ccrama.redditslide.Views.SubsamplingScaleImageView;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.CustomTabUtil;
import me.ccrama.redditslide.util.LogUtil;


/**
 * Created by ccrama on 6/2/2015.
 */
public class ImageFull extends Fragment {

    private int i = 0;
    private Submission s;
    private ViewGroup rootView;
    private SubsamplingScaleImageView image;


    private static void addClickFunctions(final View base, final View clickingArea, ContentType.ImageType type, final Activity contextActivity, final Submission submission) {
        if(!PostMatch.openExternal(submission.getUrl())) {

            switch (type) {
                case VID_ME:
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
                            PopulateSubmissionViewHolder.openImage(contextActivity, submission);

                        }
                    });
                    break;
                case GIF:
                    base.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v2) {
                            PopulateSubmissionViewHolder.openGif(contextActivity, submission);

                        }
                    });
                    break;
                case NONE:
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

    ContentType.ImageType type;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(
                R.layout.submission_imagecard, container, false);
        image = (SubsamplingScaleImageView) rootView.findViewById(R.id.image);

        PopulateShadowboxInfo.doActionbar(s, rootView, getActivity());

        (rootView.findViewById(R.id.thumbimage2)).setVisibility(View.GONE);


        type = ContentType.getImageType(s);

        // TODO: Check old IMAGE_LINK affect on things
        if (type.equals(ContentType.ImageType.IMAGE)) {
            addClickFunctions(image, rootView, type, getActivity(), s);
            loadImage(s.getUrl());
        } else if (s.getDataNode().has("preview") && s.getDataNode().get("preview").get("images").get(0).get("source").has("height") && s.getDataNode().get("preview").get("images").get(0).get("source").get("height").asInt() > 200) {
            loadImage(s.getDataNode().get("preview").get("images").get(0).get("source").get("url").asText());
        } else {
            image.recycle();
            (rootView.findViewById(R.id.thumbimage2)).setVisibility(View.VISIBLE);
            ((ImageView) rootView.findViewById(R.id.thumbimage2)).setImageResource(R.drawable.web);
            addClickFunctions((rootView.findViewById(R.id.thumbimage2)), rootView, type, getActivity(), s);

            (rootView.findViewById(R.id.progress)).setVisibility(View.GONE);

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

    boolean hidden;
    float previous;

    private void loadImage(String url) {
        final ProgressBar bar = (ProgressBar) rootView.findViewById(R.id.progress);
        bar.setIndeterminate(false);
        bar.setProgress(0);
        if (url != null && ContentType.isImgurLink(url)) {
            url = url + ".png";
        }
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
                        Log.v(LogUtil.getTag(), "LOADING FAILED");

                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        image.setImage(ImageSource.bitmap(loadedImage));
                        (rootView.findViewById(R.id.progress)).setVisibility(View.GONE);

                        if (type == ContentType.ImageType.IMAGE || type == ContentType.ImageType.IMGUR) {
                            image.setMinimumDpi(30);
                            previous = image.scale;
                            final float base = image.scale;
                            image.setOnZoomChangedListener(new SubsamplingScaleImageView.OnZoomChangedListener() {
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

    public String sub;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        i = bundle.getInt("page", 0);
        sub = bundle.getString("sub");
        s = OfflineSubreddit.getSubreddit(sub).submissions.get(bundle.getInt("page", 0));

    }

}
