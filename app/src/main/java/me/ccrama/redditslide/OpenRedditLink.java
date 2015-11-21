package me.ccrama.redditslide;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Arrays;

import me.ccrama.redditslide.Activities.CommentsScreenSingle;
import me.ccrama.redditslide.Activities.Profile;
import me.ccrama.redditslide.Activities.SubredditView;
import me.ccrama.redditslide.Activities.Wiki;

public class OpenRedditLink {
    private final static String TAG = "OpenRedditLink";

    public OpenRedditLink(Context context, String url) {
        boolean np = url.contains("np.");
        url = url.replace("np.", "");
        url = url.replace("www.", "");
        url = url.replace("http://", "");
        url = url.replace("https://", "");

        if (url.startsWith("/")) {
            url = "reddit.com" + url;
        }
        Log.v(TAG, url);
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);

        }
        url = url.replace("//", "/");

        String[] parts = url.split("/");
        if (parts[parts.length - 1].startsWith("?")) {
            parts = Arrays.copyOf(parts, parts.length - 1);


        }

        if (!url.contains("reddit.com") && !url.contains("redd.it")) { // not a valid link
            Log.v(TAG, "Invalid link");
            return;
        }

        if (url.matches("redd\\.it.*")) { // redd.it post link
            Intent i = new Intent(context, CommentsScreenSingle.class);
            i.putExtra("subreddit", "NOTHING");
            i.putExtra("context", "NOTHING");
            i.putExtra("np", np);
            i.putExtra("submission", parts[1]);
            context.startActivity(i);
        } else if (url.matches("reddit\\.com/r/\\w*/wiki/.*")) { // wiki link
            Intent i = new Intent(context, Wiki.class);
            i.putExtra("subreddit", parts[2]);
            context.startActivity(i);
        } else if (url.matches("reddit\\.com/r/\\w*")) { // subreddit link
            Intent intent = new Intent(context, SubredditView.class);
            intent.putExtra("subreddit", parts[2]);
            context.startActivity(intent);
        } else if (url.matches("reddit\\.com/u(ser)?/.*")) { // user link
            String name = parts[2];
            if (name.equals("me")) name = Authentication.name;
            Intent myIntent = new Intent(context, Profile.class);
            myIntent.putExtra("profile", name);
            context.startActivity(myIntent);
        } else if (parts.length == 7) { // post link with context
            Intent i = new Intent(context, CommentsScreenSingle.class);
            i.putExtra("subreddit", parts[2]);
            i.putExtra("submission", parts[4]);
            i.putExtra("np", np);
            i.putExtra("loadmore", true);
            String end = parts[6];
            if (end.contains("?")) end = end.substring(0, end.indexOf("?"));
            i.putExtra("context", end);
            context.startActivity(i);
        } else { // post link without context
            Intent i = new Intent(context, CommentsScreenSingle.class);
            i.putExtra("subreddit", parts[2]);
            i.putExtra("context", "NOTHING");
            i.putExtra("np", np);
            i.putExtra("submission", parts[4]);
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
