package me.ccrama.redditslide.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
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
import android.widget.TextView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;

import net.dean.jraw.models.Submission;

import me.ccrama.redditslide.Activities.Album;
import me.ccrama.redditslide.Activities.CommentsScreen;
import me.ccrama.redditslide.Activities.CommentsScreenPopup;
import me.ccrama.redditslide.Activities.FullscreenVideo;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.ImageLoaderUtils;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Views.PopulateSubmissionViewHolder;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.CustomTabUtil;


/**
 * Created by ccrama on 6/2/2015.
 */
public class ImageFull extends Fragment {

    private int i = 0;
    private Submission s;

    private static void addClickFunctions(final View base, final View clickingArea, ContentType.ImageType type, final Activity contextActivity, final Submission submission) {
        switch (type) {
            case NSFW_IMAGE:
                base.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v2) {
                        PopulateSubmissionViewHolder.openImage(contextActivity, submission);

                    }
                });
                break;
            case EMBEDDED:
                base.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v2) {
                        if (Reddit.video) {
                            String data = submission.getDataNode().get("media_embed").get("content").asText();
                            {
                                Intent i = new Intent(contextActivity, FullscreenVideo.class);
                                i.putExtra("html", data);
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
                        if (Reddit.album) {
                            Intent i = new Intent(contextActivity, Album.class);
                            i.putExtra("url", submission.getUrl());
                            contextActivity.startActivity(i);
                            contextActivity.overridePendingTransition(R.anim.slideright, R.anim.fade_out);
                        } else {
                            Reddit.defaultShare(submission.getUrl(), contextActivity);
                        }

                    }
                });
                break;
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
                        if (Reddit.video) {
                            Intent intent = new Intent(contextActivity, FullscreenVideo.class);
                            intent.putExtra("html", submission.getUrl());
                            contextActivity.startActivity(intent);
                        } else {
                            Reddit.defaultShare(submission.getUrl(), contextActivity);
                        }

                    }
                });

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.submission_imagecard, container, false);

        final SubsamplingScaleImageView image = (SubsamplingScaleImageView) rootView.findViewById(R.id.image);
        TextView title = (TextView) rootView.findViewById(R.id.title);
        TextView desc = (TextView) rootView.findViewById(R.id.desc);

        title.setText(s.getTitle());
        desc.setText(s.getSubredditName() + getString(R.string.submission_properties_seperator) + s.getAuthor() + " " + TimeUtils.getTimeAgo(s.getCreated().getTime(), getContext()) +
                getString(R.string.submission_properties_seperator) +
                PopulateSubmissionViewHolder.getSubmissionScoreString(s.getScore(), getActivity().getResources(), s)
                + getString(R.string.submission_properties_seperator)
                + getActivity().getResources().getQuantityString(R.plurals.submission_comment_count, s.getCommentCount(), s.getCommentCount()));




        ContentType.ImageType type = ContentType.getImageType(s);

        String url;

        if (type.toString().toLowerCase().contains("image") && type != ContentType.ImageType.IMAGE_LINK) {
            addClickFunctions(image, rootView, type, getActivity(), s);

            url = s.getUrl();
            final ProgressBar bar = (ProgressBar) rootView.findViewById(R.id.progress);
            bar.setIndeterminate(false);
            bar.setProgress(0);
            if (url != null && url.contains("imgur") && (!url.contains(".png") || !url.contains(".jpg") || !url.contains(".jpeg"))) {
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

        } else if (s.getDataNode().has("preview") && s.getDataNode().get("preview").get("images").get(0).get("source").has("height") && s.getDataNode().get("preview").get("images").get(0).get("source").get("height").asInt() > 200) {

            url = s.getDataNode().get("preview").get("images").get(0).get("source").get("url").asText();
            final ProgressBar bar = (ProgressBar) rootView.findViewById(R.id.progress);
            bar.setIndeterminate(false);
            bar.setProgress(0);
            if (url != null && url.contains("imgur") && (!url.contains(".png") || !url.contains(".jpg") || !url.contains(".jpeg"))) {
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

        } else {
            addClickFunctions(image, rootView, type, getActivity(), s);
            image.setImage(ImageSource.resource(R.drawable.web));
            (rootView.findViewById(R.id.progress)).setVisibility(View.GONE);

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
                    i2.putExtra("subreddit", s.getSubredditName());
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
        s = DataShare.sharedSubreddit.get(i);

    }

}
