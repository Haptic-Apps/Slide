package me.ccrama.redditslide;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import net.dean.jraw.models.Submission;

import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Adapters.SubredditPosts;

/**
 * Created by ccrama on 10/2/2015.
 */
public class StackWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
    }

}

class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private List<Submission> submissions = new ArrayList<Submission>();
    private Context mContext;
    private int mAppWidgetId;

    public StackRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    public void onCreate() {

    }

    public void onDestroy() {
        submissions.clear();
    }

    public int getCount() {
        return submissions.size();
    }

    public RemoteViews getViewAt(int position) {
        final RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.submission_widget);

        if (position <= getCount()) {

            final Submission submission = submissions.get(position);

            String url = "";
            ContentType.ImageType type = ContentType.getImageType(submission);
            if (type == ContentType.ImageType.IMAGE) {
                url = ContentType.getFixedUrl(submission.getUrl());
            } else if (submission.getDataNode().has("preview") && submission.getDataNode().get("preview").get("images").get(0).get("source").has("height") && submission.getDataNode().get("preview").get("images").get(0).get("source").get("height").asInt() > 200) {

                url = submission.getDataNode().get("preview").get("images").get(0).get("source").get("url").asText();

            } else if (submission.getThumbnail() != null && (submission.getThumbnailType() == Submission.ThumbnailType.URL || submission.getThumbnailType() == Submission.ThumbnailType.NSFW)) {
                url = submission.getThumbnail();
            }
            try {
                final String finalUrl = url;

               //todo rv.setImageViewBitmap(R.id.thumbnail, Glide.with(mContext).load(url).asBitmap().);
                rv.setTextViewText(R.id.title, Html.fromHtml(submission.getTitle()));


            } catch (Exception e) {
                Log.v("Slide", e.toString());
            }


            rv.setTextViewText(R.id.title, Html.fromHtml(submission.getTitle()));

            rv.setTextViewText(R.id.subreddit, submission.getSubredditName());
            rv.setTextViewText(R.id.info, submission.getAuthor() + " " + TimeUtils.getTimeAgo(submission.getCreatedUtc().getTime(), mContext));

            Bundle extras = new Bundle();
            extras.putString("url", submission.getUrl());
            Intent fillInIntent = new Intent();
            fillInIntent.putExtras(extras);
            rv.setOnClickFillInIntent(R.id.card, fillInIntent);
        }

        return rv;
    }

    public RemoteViews getLoadingView() {
        return null;
    }

    public int getViewTypeCount() {
        return 1;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }

    SubredditPosts posts;

    public void onDataSetChanged() {
        if (posts == null) {
            posts = new SubredditPosts("all");
            Log.v("Slide", "MAKING POSTS");
        }
        submissions = posts.getPosts();
        Log.v("Slide", "POSTS IS SIZE " + submissions.size());


    }
}