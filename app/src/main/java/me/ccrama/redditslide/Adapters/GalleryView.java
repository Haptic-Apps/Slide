package me.ccrama.redditslide.Adapters;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cocosw.bottomsheet.BottomSheet;

import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Thumbnails;

import java.util.ArrayList;

import me.ccrama.redditslide.Activities.Album;
import me.ccrama.redditslide.Activities.AlbumPager;
import me.ccrama.redditslide.Activities.CommentsScreen;
import me.ccrama.redditslide.Activities.FullscreenVideo;
import me.ccrama.redditslide.Activities.Gallery;
import me.ccrama.redditslide.Activities.MediaView;
import me.ccrama.redditslide.Activities.Tumblr;
import me.ccrama.redditslide.Activities.TumblrPager;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.PostMatch;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SubmissionViews.PopulateSubmissionViewHolder;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.LinkUtil;

public class GalleryView extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Gallery               main;
    public        boolean               paddingBottom;
    public final  ArrayList<Submission> posts;
    public final  String                subreddit;

    public GalleryView(final Gallery context, ArrayList<Submission> displayer, String subreddit) {
        main = context;
        this.posts = displayer;
        this.subreddit = subreddit;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gallery_image, parent, false);
        return new AlbumViewHolder(v);
    }

    public double getHeightFromAspectRatio(int imageHeight, int imageWidth, int viewWidth) {
        double ratio = (double) imageHeight / (double) imageWidth;
        return (viewWidth * ratio);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder2, final int i) {
        if (holder2 instanceof AlbumViewHolder) {

            final AlbumViewHolder holder = (AlbumViewHolder) holder2;

            final Submission submission = posts.get(i);

            if (submission.getThumbnails() != null
                    && submission.getThumbnails().getSource() != null) {
                ((Reddit) main.getApplicationContext()).getImageLoader()
                        .displayImage(submission.getThumbnails().getSource().getUrl(), holder.image,
                                ImageGridAdapter.options);
            } else {
                ((Reddit) main.getApplicationContext()).getImageLoader()
                        .displayImage(submission.getUrl(), holder.image, ImageGridAdapter.options);

            }
            double h = 0;
            int height = 0;
            if (submission.getThumbnails() != null) {
                Thumbnails.Image source = submission.getThumbnails().getSource();
                if (source != null) {
                    h = getHeightFromAspectRatio(source.getHeight(), source.getWidth(),
                            holder.image.getWidth());
                    height = source.getHeight();
                }
            }

            holder.type.setVisibility(View.VISIBLE);
            switch (ContentType.getContentType(submission)) {
                case ALBUM:
                    holder.type.setImageResource(R.drawable.album);
                    break;
                case EXTERNAL:
                case LINK:
                case REDDIT:
                    holder.type.setImageResource(R.drawable.world);
                    break;
                case SELF:
                    holder.type.setImageResource(R.drawable.fontsizedarker);
                    break;
                case EMBEDDED:
                case GIF:
                case STREAMABLE:
                case VIDEO:
                case VID_ME:
                    holder.type.setImageResource(R.drawable.play);
                    break;
                default:
                    holder.type.setVisibility(View.GONE);
                    break;
            }

            if (h != 0) {
                if (h > 3200) {
                    holder.image.setLayoutParams(new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT, 3200));
                } else {
                    holder.image.setLayoutParams(new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT, (int) h));
                }
            } else {
                if (height > 3200) {
                    holder.image.setLayoutParams(new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT, 3200));
                } else {
                    holder.image.setLayoutParams(new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT));
                }
            }

            holder.comments.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    Intent i2 = new Intent(main, CommentsScreen.class);
                    i2.putExtra(CommentsScreen.EXTRA_PAGE,
                            main.subredditPosts.getPosts().indexOf(submission));
                    i2.putExtra(CommentsScreen.EXTRA_SUBREDDIT, subreddit);
                    i2.putExtra("fullname", submission.getFullName());
                    main.startActivity(i2);
                }
            });

            holder.image.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    if (main != null) {
                        BottomSheet.Builder b =
                                new BottomSheet.Builder(main).title(submission.getUrl()).grid();
                        int[] attrs = new int[]{R.attr.tintColor};
                        TypedArray ta = main.obtainStyledAttributes(attrs);

                        int color = ta.getColor(0, Color.WHITE);
                        Drawable open =
                                main.getResources().getDrawable(R.drawable.ic_open_in_browser);
                        open.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                        Drawable share = main.getResources().getDrawable(R.drawable.ic_share);
                        share.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                        Drawable copy = main.getResources().getDrawable(R.drawable.ic_content_copy);
                        copy.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

                        ta.recycle();

                        b.sheet(R.id.open_link, open,
                                main.getResources().getString(R.string.submission_link_extern));
                        b.sheet(R.id.share_link, share,
                                main.getResources().getString(R.string.share_link));
                        b.sheet(R.id.copy_link, copy,
                                main.getResources().getString(R.string.submission_link_copy));

                        b.listener(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case R.id.open_link:
                                        LinkUtil.openExternally(submission.getUrl());
                                        break;
                                    case R.id.share_link:
                                        Reddit.defaultShareText("", submission.getUrl(), main);
                                        break;
                                    case R.id.copy_link:
                                        LinkUtil.copyUrl(submission.getUrl(), main);
                                        break;
                                }
                            }
                        }).show();
                        return true;
                    }
                    return true;
                }
            });

            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContentType.Type type = ContentType.getContentType(submission);
                    if (!PostMatch.openExternal(submission.getUrl())
                            || type == ContentType.Type.VIDEO) {
                        switch (type) {
                            case VID_ME:
                            case STREAMABLE:
                                if (SettingValues.video) {
                                    Intent myIntent = new Intent(main, MediaView.class);
                                    myIntent.putExtra(MediaView.SUBREDDIT, subreddit);
                                    myIntent.putExtra(MediaView.EXTRA_URL, submission.getUrl());
                                    main.startActivity(myIntent);
                                } else {
                                    LinkUtil.openExternally(submission.getUrl());
                                }
                                break;
                            case IMGUR:
                                PopulateSubmissionViewHolder.openImage(type, main, submission, null,
                                        holder.getAdapterPosition());
                                break;
                            case EMBEDDED:
                                if (SettingValues.video) {
                                    String data = Html.fromHtml(submission.getDataNode()
                                            .get("media_embed")
                                            .get("content")
                                            .asText()).toString();
                                    {
                                        Intent i = new Intent(main, FullscreenVideo.class);
                                        i.putExtra(FullscreenVideo.EXTRA_HTML, data);
                                        main.startActivity(i);
                                    }
                                } else {
                                    LinkUtil.openExternally(submission.getUrl());
                                }
                                break;
                            case REDDIT:
                                PopulateSubmissionViewHolder.openRedditContent(submission.getUrl(),
                                        main);
                                break;
                            case LINK:
                                LinkUtil.openUrl(submission.getUrl(),
                                        Palette.getColor(submission.getSubredditName()), main);
                                break;
                            case ALBUM:
                                if (SettingValues.album) {
                                    if (SettingValues.albumSwipe) {
                                        Intent i = new Intent(main, AlbumPager.class);
                                        i.putExtra(AlbumPager.SUBREDDIT, subreddit);
                                        i.putExtra(Album.EXTRA_URL, submission.getUrl());
                                        main.startActivity(i);
                                    } else {
                                        Intent i = new Intent(main, Album.class);
                                        i.putExtra(Album.SUBREDDIT, subreddit);
                                        i.putExtra(Album.EXTRA_URL, submission.getUrl());
                                        main.startActivity(i);
                                    }
                                } else {
                                    LinkUtil.openExternally(submission.getUrl());

                                }
                                break;
                            case TUMBLR:
                                if (SettingValues.image) {
                                    if (SettingValues.albumSwipe) {
                                        Intent i = new Intent(main, TumblrPager.class);
                                        i.putExtra(TumblrPager.SUBREDDIT, subreddit);
                                        i.putExtra(Album.EXTRA_URL, submission.getUrl());
                                        main.startActivity(i);
                                    } else {
                                        Intent i = new Intent(main, Tumblr.class);
                                        i.putExtra(Tumblr.SUBREDDIT, subreddit);
                                        i.putExtra(Album.EXTRA_URL, submission.getUrl());
                                        main.startActivity(i);
                                    }
                                } else {
                                    LinkUtil.openExternally(submission.getUrl());

                                }
                                break;
                            case DEVIANTART:
                            case XKCD:
                            case IMAGE:
                                PopulateSubmissionViewHolder.openImage(type, main, submission, null,
                                        holder.getAdapterPosition());
                                break;
                            case GIF:
                                PopulateSubmissionViewHolder.openGif(main, submission,
                                        holder.getAdapterPosition());
                                break;
                            case NONE:
                            case SELF:
                                holder.comments.callOnClick();
                                break;
                            case VIDEO:
                                if (!LinkUtil.tryOpenWithVideoPlugin(submission.getUrl())) {
                                    LinkUtil.openUrl(submission.getUrl(),
                                            Palette.getStatusBarColor(), main);
                                }
                                break;
                        }
                    } else {
                        LinkUtil.openExternally(submission.getUrl());
                    }
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return posts == null ? 0 : posts.size();
    }

    public class SpacerViewHolder extends RecyclerView.ViewHolder {
        public SpacerViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class AlbumViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final ImageView type;
        final View      comments;

        public AlbumViewHolder(View itemView) {
            super(itemView);
            comments = itemView.findViewById(R.id.comments);
            image = itemView.findViewById(R.id.image);
            type = itemView.findViewById(R.id.type);
        }
    }

}
