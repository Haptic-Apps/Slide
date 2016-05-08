package me.ccrama.redditslide.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Thumbnails;

import me.ccrama.redditslide.Activities.Album;
import me.ccrama.redditslide.Activities.AlbumPager;
import me.ccrama.redditslide.Activities.CommentsScreen;
import me.ccrama.redditslide.Activities.FullscreenVideo;
import me.ccrama.redditslide.Activities.MediaView;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.PostLoader;
import me.ccrama.redditslide.PostMatch;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SubmissionViews.PopulateSubmissionViewHolder;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.CustomTabUtil;

public class GalleryView extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Activity main;
    public boolean paddingBottom;
    public PostLoader displayer;
    public String subreddit;

    public GalleryView(final Activity context, PostLoader displayer, String subreddit) {
        main = context;
        this.displayer = displayer;
        this.subreddit = subreddit;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_image, parent, false);
        return new AlbumViewHolder(v);
    }

    public double getHeightFromAspectRatio(int imageHeight, int imageWidth, int viewWidth) {
        double ratio = (double) imageHeight / (double) imageWidth;
        return (viewWidth * ratio);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder2, final int i) {
        if (holder2 instanceof AlbumViewHolder) {

            final AlbumViewHolder holder = (AlbumViewHolder) holder2;

            final Submission submission = displayer.getPosts().get(i);

            if (submission.getThumbnails() != null && submission.getThumbnails().getSource() != null) {
                ((Reddit) main.getApplicationContext()).getImageLoader().displayImage(submission.getThumbnails().getSource().getUrl(), holder.image, ImageGridAdapter.options);
            }
            double h = 0;
            int height = 0;
            if (submission.getThumbnails() != null) {
                Thumbnails.Image source = submission.getThumbnails().getSource();
                if (source != null) {
                    h = getHeightFromAspectRatio(source.getHeight(), source.getWidth(), holder.image.getWidth());
                    height = source.getHeight();
                }
            }

            if (h != 0) {
                if (h > 3200) {
                    holder.image.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 3200));
                } else {
                    holder.image.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, (int) h));
                }
            } else {
                if (height > 3200) {
                    holder.image.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 3200));
                } else {
                    holder.image.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                }
            }

            holder.comments.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i2 = new Intent(main, CommentsScreen.class);
                    i2.putExtra(CommentsScreen.EXTRA_PAGE, i);
                    i2.putExtra(CommentsScreen.EXTRA_SUBREDDIT, subreddit);
                    main.startActivity(i2);
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContentType.Type type = ContentType.getContentType(submission);
                    if (!PostMatch.openExternal(submission.getUrl()) || type == ContentType.Type.VIDEO) {
                        switch (type) {
                            case VID_ME:
                            case STREAMABLE:
                                if (SettingValues.video) {
                                    Intent myIntent = new Intent(main, MediaView.class);
                                    myIntent.putExtra(MediaView.EXTRA_URL, submission.getUrl());
                                    main.startActivity(myIntent);
                                } else {
                                    Reddit.defaultShare(submission.getUrl(), main);
                                }
                                break;
                            case IMGUR:
                                PopulateSubmissionViewHolder.openImage(main, submission, null);
                                break;
                            case EMBEDDED:
                                if (SettingValues.video) {
                                    String data = Html.fromHtml(submission.getDataNode().get("media_embed").get("content").asText()).toString();
                                    {
                                        Intent i = new Intent(main, FullscreenVideo.class);
                                        i.putExtra(FullscreenVideo.EXTRA_HTML, data);
                                        main.startActivity(i);
                                    }
                                } else {
                                    Reddit.defaultShare(submission.getUrl(), main);
                                }
                                break;
                            case REDDIT:
                                PopulateSubmissionViewHolder.openRedditContent(submission.getUrl(), main);
                                break;
                            case LINK:
                                CustomTabUtil.openUrl(submission.getUrl(), Palette.getColor(submission.getSubredditName()), main);
                                break;
                            case ALBUM:
                                if (SettingValues.album) {
                                    if (SettingValues.albumSwipe) {
                                        Intent i = new Intent(main, AlbumPager.class);
                                        i.putExtra(Album.EXTRA_URL, submission.getUrl());
                                        main.startActivity(i);
                                        main.overridePendingTransition(R.anim.slideright, R.anim.fade_out);
                                    } else {
                                        Intent i = new Intent(main, Album.class);
                                        i.putExtra(Album.EXTRA_URL, submission.getUrl());
                                        main.startActivity(i);
                                        main.overridePendingTransition(R.anim.slideright, R.anim.fade_out);
                                    }
                                } else {
                                    Reddit.defaultShare(submission.getUrl(), main);

                                }
                                break;
                            case DEVIANTART:
                            case IMAGE:
                                PopulateSubmissionViewHolder.openImage(main, submission, null);
                                break;
                            case GIF:
                                PopulateSubmissionViewHolder.openGif(main, submission);
                                break;
                            case NONE:
                            case SELF:
                                holder.comments.callOnClick();
                                break;
                            case VIDEO:
                                if (Reddit.videoPlugin) {
                                    try {
                                        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                                        sharingIntent.setClassName("ccrama.me.slideyoutubeplugin",
                                                "ccrama.me.slideyoutubeplugin.YouTubeView");
                                        sharingIntent.putExtra("url", submission.getUrl());
                                        main.startActivity(sharingIntent);

                                    } catch (Exception e) {
                                        Reddit.defaultShare(submission.getUrl(), main);
                                    }
                                } else {
                                    Reddit.defaultShare(submission.getUrl(), main);
                                }
                                break;
                        }
                    } else {
                        Reddit.defaultShare(submission.getUrl(), main);
                    }
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return displayer.getPosts() == null ? 0 : displayer.getPosts().size();
    }

    public class SpacerViewHolder extends RecyclerView.ViewHolder {
        public SpacerViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class AlbumViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final View comments;

        public AlbumViewHolder(View itemView) {
            super(itemView);
            comments = itemView.findViewById(R.id.comments);
            image = (ImageView) itemView.findViewById(R.id.image);


        }
    }

}
