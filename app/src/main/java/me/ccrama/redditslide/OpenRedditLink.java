package me.ccrama.redditslide;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Arrays;

import me.ccrama.redditslide.Activities.CommentsScreenSingle;
import me.ccrama.redditslide.Activities.MainActivity;
import me.ccrama.redditslide.Activities.Profile;
import me.ccrama.redditslide.Activities.SubredditView;
import me.ccrama.redditslide.Activities.Wiki;

public class OpenRedditLink {
    private final static String TAG = "OpenRedditLink";

    public OpenRedditLink(Context context, String url) {
        boolean np = url.matches("(https?://)?np.reddit.com.*");

        url = url.replace("np.", "");
        url = url.replace("www.", "");
        url = url.replace("http://", "");
        url = url.replace("https://", "");
        url = url.replace("m.", "");
        url = url.replace("i.", "");

        if (url.startsWith("/")) url = "reddit.com" + url;
        if (url.endsWith("/")) url = url.substring(0, url.length() - 1);

        String[] parts = url.split("/");
        if (parts[parts.length - 1].startsWith("?"))
            parts = Arrays.copyOf(parts, parts.length - 1);

        Log.v(TAG, "Opening URL " + url);

        if (url.matches("(?i)redd\\.it.*")) {
            // Redd.it link. Format: redd.it/post_id
            Intent i = new Intent(context, CommentsScreenSingle.class);
            i.putExtra("subreddit", "NOTHING");
            i.putExtra("context", "NOTHING");
            i.putExtra("np", np);
            i.putExtra("submission", parts[1]);
            context.startActivity(i);
        } else if (url.matches("(?i)reddit\\.com/r/\\w+/wiki(/.*)?")) {
            // Wiki link. Format: reddit.com/r/$subreddit/wiki/$page [optional]
            Intent i = new Intent(context, Wiki.class);
            i.putExtra("subreddit", parts[2]);
            context.startActivity(i);
        } else if (url.matches("(?i)reddit\\.com/r/\\w+/comments/\\w+(/\\w+)?")) {
            // Post comments. Format: reddit.com/r/$subreddit/comments/$post_id/$post_title [optional]
            Intent i = new Intent(context, CommentsScreenSingle.class);
            i.putExtra("subreddit", parts[2]);
            i.putExtra("context", "NOTHING");
            i.putExtra("np", np);
            i.putExtra("submission", parts[4]);
            context.startActivity(i);
        } else if (url.matches("(?i)reddit\\.com/r/\\w+/comments/\\w+/\\w*/\\w+")) {
            // Permalink. Format: reddit.com/r/$subreddit/comments/$post_id/$post_title [can be empty]/$comment_id
        Intent i = new Intent(context, CommentsScreenSingle.class);
            i.putExtra("subreddit", parts[2]);
            i.putExtra("submission", parts[4]);
            i.putExtra("np", np);
            i.putExtra("loadmore", true);
            String end = parts[6];
            if (end.contains("?")) end = end.substring(0, end.indexOf("?"));
            i.putExtra("context", end);
            context.startActivity(i);
        } else if (url.matches("(?i)reddit\\.com/r/\\w+(/.*)?")) {
            // Subreddit. Format: reddit.com/r/$subreddit/$sort [optional]
            Intent intent = new Intent(context, SubredditView.class);
            intent.putExtra("subreddit", parts[2]);
            context.startActivity(intent);
        } else if (url.matches("(?i)reddit\\.com/u(ser)?(/.*)?")) {
            // User. Format: reddit.com/u [or user]/$username/$page [optional]
            String name = parts[2];
            if (name.equals("me")) name = Authentication.name;
            Intent myIntent = new Intent(context, Profile.class);
            myIntent.putExtra("profile", name);
            context.startActivity(myIntent);
        } else {
            Intent i = new Intent(context, MainActivity.class);
            context.startActivity(i);
        }
    }

    public OpenRedditLink(Context c, String submission, String subreddit, String id) {
        Intent i = new Intent(c, CommentsScreenSingle.class);
        i.putExtra("subreddit", subreddit);
        i.putExtra("context", id);
        i.putExtra("submission", submission);
        c.startActivity(i);
    }
}
