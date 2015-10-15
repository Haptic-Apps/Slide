package me.ccrama.redditslide.Views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.text.ClipboardManager;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.databind.JsonNode;

import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import java.net.URI;
import java.net.URISyntaxException;

import me.ccrama.redditslide.ActiveTextView;
import me.ccrama.redditslide.Activities.Album;
import me.ccrama.redditslide.Activities.FullscreenImage;
import me.ccrama.redditslide.Activities.FullscreenVideo;
import me.ccrama.redditslide.Activities.GifView;
import me.ccrama.redditslide.Adapters.SubmissionViewHolder;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.HasSeen;
import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Visuals.Pallete;
import me.ccrama.redditslide.Vote;

/**
 * Created by ccrama on 9/19/2015.
 */
public class PopulateSubmissionViewHolder {
    boolean upvoted;
    boolean downvoted;

    public void PopulateSubmissionViewHolder(final SubmissionViewHolder holder, final Submission submission, final Context mContext, boolean fullscreen) {
        if (HasSeen.getSeen(submission.getFullName())) {
            holder.itemView.setAlpha(0.7f);
        } else {
            holder.itemView.setAlpha(1.0f);
        }



        holder.title.setText(Html.fromHtml(submission.getTitle()));

        holder.info.setText(submission.getAuthor() + " " + TimeUtils.getTimeAgo(submission.getCreatedUtc().getTime()));


        holder.subreddit.setText(submission.getSubredditName());

        holder.itemView.findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPopup(holder, submission, (Activity) mContext);
            }
        });

        holder.comments.setText(submission.getCommentCount() + " comments");
        if(submission.getSubredditName().equals("androidcirclejerk")) {
            holder.score.setText(submission.getScore() + " upDuARTes");
        } else {
            holder.score.setText(submission.getScore() + " points");

        }
        if (Authentication.isLoggedIn) {
            if (submission.getVote() == VoteDirection.UPVOTE) {
                holder.score.setTextColor(mContext.getResources().getColor(R.color.md_orange_500));
            } else if (submission.getVote() == VoteDirection.DOWNVOTE) {
                holder.score.setTextColor(mContext.getResources().getColor(R.color.md_blue_500));

            } else {
                holder.score.setTextColor(holder.comments.getCurrentTextColor());
            }
        }





        ContentType.ImageType type = ContentType.getImageType(submission);

        String url = "";
        boolean big = SettingValues.largeThumbnails;
        holder.thumbImage.setVisibility(View.VISIBLE);

        boolean bigAtEnd = false;
        String submissionURL = submission.getUrl();
        if (type == ContentType.ImageType.IMAGE) {
            url = ContentType.getFixedUrl(submission.getUrl());
            if (big || fullscreen) {
                Glide.with(mContext).load(url).into(holder.leadImage);
                holder.imageArea.setVisibility(View.VISIBLE);
                holder.previewContent.setVisibility(View.GONE);
                bigAtEnd = true;
            } else {
                Glide.with(mContext).load(url).into(holder.thumbImage);

                holder.imageArea.setVisibility(View.GONE);
                holder.previewContent.setVisibility(View.VISIBLE);
                bigAtEnd = false;
            }
        } else if (submission.getDataNode().has("preview") && submission.getDataNode().get("preview").get("images").get(0).get("source").has("height") && submission.getDataNode().get("preview").get("images").get(0).get("source").get("height").asInt() > 200) {

           boolean blurry = isBlurry(submission.getDataNode(), mContext, submission.getTitle());
            holder.leadImage.setMinimumHeight(submission.getDataNode().get("preview").get("images").get(0).get("source").get("height").asInt());
            url = submission.getDataNode().get("preview").get("images").get(0).get("source").get("url").asText();
            if ((big || fullscreen) && !blurry) {
                Glide.with(mContext).load(url).into(holder.leadImage);

                holder.imageArea.setVisibility(View.VISIBLE);
                holder.previewContent.setVisibility(View.GONE);
                bigAtEnd = true;
            } else {
                Glide.with(mContext).load(url).into(holder.thumbImage);

                holder.imageArea.setVisibility(View.GONE);
                holder.previewContent.setVisibility(View.VISIBLE);
                bigAtEnd = false;
            }
        } else if (submission.getThumbnail() != null && (submission.getThumbnailType() == Submission.ThumbnailType.URL || submission.getThumbnailType() == Submission.ThumbnailType.NSFW)) {
            holder.leadImage.setMinimumHeight(0);

            if ((SettingValues.NSFWPreviews && submission.getThumbnailType() == Submission.ThumbnailType.NSFW) || submission.getThumbnailType() == Submission.ThumbnailType.URL) {
                bigAtEnd = false;
                Glide.with(mContext).load(submission.getThumbnail()).into(holder.thumbImage);

                holder.imageArea.setVisibility(View.GONE);
                holder.previewContent.setVisibility(View.VISIBLE);
            } else {
                bigAtEnd = false;
                holder.thumbImage.setVisibility(View.GONE);
                holder.imageArea.setVisibility(View.GONE);
                holder.previewContent.setVisibility(View.VISIBLE);
            }
        } else {
            bigAtEnd = false;
            holder.thumbImage.setVisibility(View.GONE);
            holder.imageArea.setVisibility(View.GONE);
            holder.previewContent.setVisibility(View.VISIBLE);
        }

        if(!SettingValues.infoBar && !fullscreen){
            holder.previewContent.setVisibility(View.GONE);
        }
        if(bigAtEnd){
            holder.thumbImage.setVisibility(View.GONE);
        }


        View pinned = holder.itemView.findViewById(R.id.pinned);

        if(fullscreen){
            View flair = holder.itemView.findViewById(R.id.flairbubble);

            if(submission.getSubmissionFlair().getText() == null  || submission.getSubmissionFlair() == null || submission.getSubmissionFlair().getText().isEmpty() || submission.getSubmissionFlair().getText() == null){
                flair.setVisibility(View.GONE);
            } else {
                flair.setVisibility(View.VISIBLE);
                Log.v("Slide", "FLAIR IS '" + submission.getSubmissionFlair().getText() + "'");
                ((TextView) flair.findViewById(R.id.text)).setText(submission.getSubmissionFlair().getText());
            }

            ActiveTextView bod = ((ActiveTextView) holder.itemView.findViewById(R.id.body));
            if( !submission.getSelftext().isEmpty()){
                new MakeTextviewClickable().ParseTextWithLinksTextView(submission.getDataNode().get("selftext_html").asText(), bod, (Activity) mContext, submission.getSubredditName());
                holder.itemView.findViewById(R.id.body_area).setVisibility(View.VISIBLE);
            } else {
                holder.itemView.findViewById(R.id.body_area).setVisibility(View.GONE);
            }
        }
        View nsfw = holder.itemView.findViewById(R.id.nsfw);
        if (submission.isStickied()) {
            pinned.setVisibility(View.VISIBLE);
        } else {
            pinned.setVisibility(View.GONE);
        }
        if (submission.isNsfw()) {
            nsfw.setVisibility(View.VISIBLE);
        } else {
            nsfw.setVisibility(View.GONE);

        }

        TextView title;
        TextView info;
        if (bigAtEnd) {
            title = holder.textImage;
            info = holder.subTextImage;
        } else {
            title = holder.contentTitle;
            info = holder.contentURL;
        }
        title.setVisibility(View.VISIBLE);
        info.setVisibility(View.VISIBLE);

        switch (type) {
            case NSFW_IMAGE:
                title.setText("NSFW image");
                break;
            case NSFW_GIF:
                title.setText("NSFW GIF");

                break;
            case NSFW_GFY:
                title.setText("NSFW GIF");

                break;
            case REDDIT:
                title.setText("Reddit link");

                break;
            case LINK:
                title.setText("Link");

                break;
            case IMAGE_LINK:
                title.setText("Link");

                break;
            case NSFW_LINK:
                title.setText("NSFW Link");

                break;
            case SELF:
                title.setVisibility(View.GONE);
                info.setVisibility(View.GONE);
                if(!bigAtEnd)
                    holder.previewContent.setVisibility(View.GONE);
                break;
            case GFY:
                title.setText("GIF");

                break;
            case ALBUM:
                title.setText("Album");


                break;
            case IMAGE:
                title.setVisibility(View.GONE);
                info.setVisibility(View.GONE);
                break;
            case GIF:
                title.setText("GIF");

                break;
            case NONE_GFY:
                title.setText("GIF");

                break;
            case NONE_GIF:
                title.setText("GIF");

                break;
            case NONE:
                title.setText("Title post");

                break;
            case NONE_IMAGE:
                title.setText("Image");

                break;
            case VIDEO:
                title.setText("Video");

                break;
            case EMBEDDED:
                title.setText("Embedded");

                break;
            case NONE_URL:
                title.setText("Link");

                break;
        }

        try {
            info.setText(getDomainName(submission.getUrl()));
        } catch (URISyntaxException e1) {
            e1.printStackTrace();
        }


        try {

            if (submission.getVote() == VoteDirection.UPVOTE) {
                upvoted = true;
            } else if (submission.getVote() == VoteDirection.DOWNVOTE) {
                downvoted = true;
            }

            final ImageView downvotebutton = (ImageView) holder.itemView.findViewById(R.id.downvote);
            final ImageView upvotebutton = (ImageView) holder.itemView.findViewById(R.id.upvote);
            final TextView points = holder.score;
            final TextView comments = holder.comments;
            if (Authentication.isLoggedIn) {
                {
                    downvotebutton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!upvoted && !downvoted) {
                                downvoted = true;
                                points.setTextColor(mContext.getResources().getColor(R.color.md_blue_500));


                                new Vote(false, points, mContext).execute(submission);
                            } else if (!downvoted && upvoted) {
                                new Vote(false, points, mContext).execute(submission);
                                downvoted = true;
                                points.setTextColor(mContext.getResources().getColor(R.color.md_blue_500));
                                upvoted = false;
                            } else if (!upvoted && downvoted) {
                                new Vote(points, mContext).execute(submission);
                                points.setTextColor(comments.getCurrentTextColor());
                                downvoted = false;
                            }
                        }
                    });
                }
                {
                    upvotebutton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!upvoted && !downvoted) {
                                upvoted = true;
                                new Vote(true, points, mContext).execute(submission);
                                points.setTextColor(mContext.getResources().getColor(R.color.md_orange_500));
                            } else if (!upvoted && downvoted) {
                                new Vote(true, points, mContext).execute(submission);
                                points.setTextColor(mContext.getResources().getColor(R.color.md_orange_500));
                                upvoted = true;
                                downvoted = false;
                            } else if (upvoted && !downvoted) {
                                points.setTextColor(comments.getCurrentTextColor());
                                new Vote(points, mContext).execute(submission);
                                upvoted = false;
                            }
                        }
                    });
                }
            } else {
                upvotebutton.setVisibility(View.GONE);
                downvotebutton.setVisibility(View.GONE);

            }
            View baseView;

            if(bigAtEnd){
                baseView = holder.imageArea;
            } else {
                baseView= holder.previewContent;
            }
            addClickFunctions(holder.imageArea, baseView, type, (Activity) mContext, submission);
            addClickFunctions(holder.thumbImage, baseView, type, (Activity) mContext, submission);
            addClickFunctions(holder.leadImage, baseView, type, (Activity) mContext, submission);

            addClickFunctions(holder.previewContent, baseView, type, (Activity) mContext, submission);

        } catch (Exception e) {

        }


    }

    public static String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

    public static void openPopup(SubmissionViewHolder holder, final Submission s, final Activity mContext) {
        if (SettingValues.actionBarVisible) {
            PopupMenu popup = new PopupMenu(mContext, holder.itemView.findViewById(R.id.menu));
            popup.getMenu().add("Copy permalink").setNumericShortcut('1');
            popup.getMenu().add("Author profile").setNumericShortcut('2');
            popup.getMenu().add("Go to subreddit").setNumericShortcut('3');

            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getNumericShortcut()) {
                        case '1': {
                            ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(mContext.CLIPBOARD_SERVICE);
                            clipboard.setText("http://reddit.com" + s.getPermalink());
                            Toast.makeText(mContext, "URL copied to clipboard", Toast.LENGTH_SHORT).show();
                        }
                        break;
                        case '2': {
                            //    Intent i = new Intent(mContext, Profile.class);
                            //   i.putExtra("name", s.getAuthor());
                            //  mContext.startActivity(i);
                        }
                        break;
                        case '3': {
                            //    Intent inte = new Intent(mContext, SingleSubredditViewNew.class);
                            //    inte.putExtra("subreddit", s.getSubredditName());
                            //    mContext.startActivity(inte);

                        }
                        break;

                    }

                    return true;
                }
            });

            popup.show();
        } else {
            if (holder.actionBar.getVisibility() == View.GONE) {
                holder.actionBar.setVisibility(View.VISIBLE);
            } else {
                holder.actionBar.setVisibility(View.GONE);

            }
        }

    }




    private static void addClickFunctions(final View base, final View clickingArea, ContentType.ImageType type, final Activity contextActivity, final Submission submission) {
        switch (type) {
            case NSFW_IMAGE:
                base.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v2) {
                        openImage(contextActivity, submission);

                    }
                });
                break;
            case EMBEDDED:
                base.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v2) {
                        String data = submission.getDataNode().get("media_embed").get("content").asText();
                        {
                            Intent i = new Intent(contextActivity, FullscreenVideo.class);
                            i.putExtra("html", data);
                            contextActivity.startActivity(i);
                        }
                    }
                });
                break;
            case NSFW_GIF:
                base.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v2) {
                        openGif(false, contextActivity, submission);

                    }
                });
                break;
            case NSFW_GFY:

                base.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v2) {
                        openGif(true, contextActivity, submission);

                    }
                });
                break;
            case REDDIT:
                base.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v2) {
                       openRedditContent(submission.getUrl(), true, contextActivity);
                    }
                });
                break;
            case LINK:
                base.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v2) {
                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(Reddit.getSession());
                        builder.setToolbarColor(Pallete.getColor(submission.getSubredditName())).setShowTitle(true);

                        builder.setStartAnimations(contextActivity, R.anim.slideright, R.anim.fading_out_real);
                        builder.setExitAnimations(contextActivity, R.anim.fade_out, R.anim.fade_in_real);
                        CustomTabsIntent customTabsIntent = builder.build();
                        customTabsIntent.launchUrl(contextActivity, Uri.parse(submission.getUrl()));

                    }
                });
                break;
            case IMAGE_LINK:
                base.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v2) {
                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(Reddit.getSession());
                        builder.setToolbarColor(Pallete.getColor(submission.getSubredditName())).setShowTitle(true);

                        builder.setStartAnimations(contextActivity, R.anim.slideright, R.anim.fading_out_real);
                        builder.setExitAnimations(contextActivity, R.anim.fade_out, R.anim.fade_in_real);
                        CustomTabsIntent customTabsIntent = builder.build();
                        customTabsIntent.launchUrl(contextActivity, Uri.parse(submission.getUrl()));
                    }
                });
                break;
            case NSFW_LINK:
                base.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v2) {
                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(Reddit.getSession());
                        builder.setToolbarColor(Pallete.getColor(submission.getSubredditName())).setShowTitle(true);
                        builder.setStartAnimations(contextActivity, R.anim.slideright, R.anim.fading_out_real);
                        builder.setExitAnimations(contextActivity, R.anim.fade_out, R.anim.fade_in_real);
                        CustomTabsIntent customTabsIntent = builder.build();
                        customTabsIntent.launchUrl(contextActivity, Uri.parse(submission.getUrl()));
                    }
                });
                break;
            case SELF:

                break;
            case GFY:
                base.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v2) {
                        openGif( true, contextActivity, submission);

                    }
                });
                break;
            case ALBUM:
                base.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v2) {
                        Intent i = new Intent(contextActivity, Album.class);
                        i.putExtra("url", submission.getUrl());
                        contextActivity.startActivity(i);
                        contextActivity.overridePendingTransition(R.anim.slideright, R.anim.fade_out);


                    }
                });
                break;
            case IMAGE:
                base.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v2) {
                        openImage( contextActivity, submission);

                    }
                });
                break;
            case GIF:
                base.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v2) {
                        openGif( false, contextActivity, submission);

                    }
                });
                break;
            case NONE_GFY:
                base.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v2) {
                        openGif( true, contextActivity, submission);

                    }
                });
                break;
            case NONE_GIF:
                base.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v2) {
                        openGif( false, contextActivity, submission);

                    }
                });
                break;

            case NONE:

                break;
            case NONE_IMAGE:
                base.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v2) {
                        openImage(contextActivity, submission);


                    }
                });
                break;
            case NONE_URL:
                base.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v2) {
                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(Reddit.getSession());
                        builder.setToolbarColor(Pallete.getColor(submission.getSubredditName())).setShowTitle(true);

                        builder.setStartAnimations(contextActivity, R.anim.slideright, R.anim.fading_out_real);
                        builder.setExitAnimations(contextActivity, R.anim.fade_out, R.anim.fade_in_real);
                        CustomTabsIntent customTabsIntent = builder.build();
                        customTabsIntent.launchUrl(contextActivity, Uri.parse(submission.getUrl()));
                    }
                });
                break;
            case VIDEO:
                base.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(contextActivity, FullscreenVideo.class);
                        intent.putExtra("html", submission.getUrl());
                        contextActivity.startActivity(intent);

                    }
                });

        }
    }


    public static void openRedditContent(String url, boolean internal, Context c) {
        new OpenRedditLink(c,url);
    }
    public static boolean isBlurry(JsonNode s, Context mC, String title){
        int pixesl = s.get("preview").get("images").get(0).get("source").get("width").asInt();
        float density = mC.getResources().getDisplayMetrics().density;
        float dp = pixesl / density;
        Configuration configuration = mC.getResources().getConfiguration();
        int screenWidthDp = configuration.screenWidthDp; //The current width of the available screen space, in dp units, corresponding to screen width resource qualifier.

        if(dp < screenWidthDp / 3){
            return true;
        }
        else {
            return false;
        }
    }
    public static void openImage(Activity contextActivity, Submission submission) {
        DataShare.sharedSubmission = submission;
        Intent myIntent = new Intent(contextActivity, FullscreenImage.class);
        myIntent.putExtra("url", ContentType.getFixedUrl(submission.getUrl()));
        contextActivity.startActivity(myIntent);
        contextActivity.overridePendingTransition(R.anim.slideright, R.anim.fade_out);

    }
    public static void openGif(final boolean gfy, Activity contextActivity, Submission submission) {
        DataShare.sharedSubmission = submission;

        Intent myIntent = new Intent(contextActivity, GifView.class);
        if (gfy) {
            myIntent.putExtra("url", "gfy" + submission.getUrl());
        } else {
            myIntent.putExtra("url", "" + submission.getUrl());

        }
        contextActivity.startActivity(myIntent);
        contextActivity.overridePendingTransition(R.anim.slideright, R.anim.fade_out);


    }

}
