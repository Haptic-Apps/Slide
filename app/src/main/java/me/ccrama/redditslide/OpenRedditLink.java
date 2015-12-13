package me.ccrama.redditslide;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.ccrama.redditslide.Activities.CommentsScreenSingle;
import me.ccrama.redditslide.Activities.MainActivity;
import me.ccrama.redditslide.Activities.Profile;
import me.ccrama.redditslide.Activities.SubredditView;
import me.ccrama.redditslide.Activities.Wiki;

public class OpenRedditLink {
    private final static String TAG = "OpenRedditLink";

    public OpenRedditLink(Context context, String url) {
        // Strip unused prefixes that don't require special handling
        url = url.replaceFirst("(?i)^(https?://)?(www\\.)?((ssl|pay)\\.)?", "");

        boolean np = false;
        if (url.matches("(?i)[a-z0-9-_]+\\.reddit\\.com[a-z0-9-_/?=]*")) { // tests for subdomain
            String subdomain = url.split("\\.", 2)[0];
            String domainRegex = "(?i)" + subdomain + "\\.reddit\\.com";
            if (subdomain.equalsIgnoreCase("np")) {
                // no participation link: https://www.reddit.com/r/NoParticipation/wiki/index
                np = true;
                url = url.replaceFirst(domainRegex, "reddit.com");
            } else if (subdomain.matches("blog|store|beta")) {
                customIntentChooser("http://www." + url, context);
                return;
            } else if (subdomain.matches("(?i)([_a-z0-9]{2}-)?[_a-z0-9]{1,2}")) {
                /*
                    Either the subdomain is a language tag (with optional region) or
                    a single letter domain, which for simplicity are ignored.
                 */
                url = url.replaceFirst(domainRegex, "reddit.com");
            } else {
                // subdomain is a subreddit, change subreddit.reddit.com to reddit.com/r/subreddit
                url = url.replaceFirst(domainRegex, "reddit.com/r/" + subdomain);
            }
        }

        if (url.startsWith("/")) url = "reddit.com" + url;
        if (url.endsWith("/")) url = url.substring(0, url.length() - 1);

        String[] parts = url.split("/");
        if (parts[parts.length - 1].startsWith("?"))
            parts = Arrays.copyOf(parts, parts.length - 1);

        Log.v(TAG, "Opening URL " + url);

        if (url.matches("(?i)redd\\.it/\\w+")) {
            // Redd.it link. Format: redd.it/post_id
            Intent i = new Intent(context, CommentsScreenSingle.class);
            i.putExtra("subreddit", "NOTHING");
            i.putExtra("context", "NOTHING");
            i.putExtra("np", np);
            i.putExtra("submission", parts[1]);
            context.startActivity(i);
        } else if (url.matches("(?i)reddit\\.com/r/[a-z0-9-_]+/wiki.*")) {
            // Wiki link. Format: reddit.com/r/$subreddit/wiki/$page [optional]
            Intent i = new Intent(context, Wiki.class);
            i.putExtra("subreddit", parts[2]);
            context.startActivity(i);
        } else if (url.matches("(?i)reddit\\.com/r/[a-z0-9-_]+/comments/\\w+/\\w*/.*")) {
            // Permalink. Format: reddit.com/r/$subreddit/comments/$post_id/$post_title [can be empty]/$comment_id
            Intent i = new Intent(context, CommentsScreenSingle.class);
            i.putExtra("subreddit", parts[2]);
            i.putExtra("submission", parts[4]);
            i.putExtra("np", np);
            if (parts.length >= 7) {
                i.putExtra("loadmore", true);
                String end = parts[6];
                if (end.contains("?")) end = end.substring(0, end.indexOf("?"));
                i.putExtra("context", end);

                Log.v("Slide", "CONTEXT " + end);
            }
            context.startActivity(i);
        } else if (url.matches("(?i)reddit\\.com/r/[a-z0-9-_]+/comments/\\w+.*")) {
            // Post comments. Format: reddit.com/r/$subreddit/comments/$post_id/$post_title [optional]
            Intent i = new Intent(context, CommentsScreenSingle.class);
            i.putExtra("subreddit", parts[2]);
            i.putExtra("context", "NOTHING");
            i.putExtra("np", np);
            i.putExtra("submission", parts[4]);
            context.startActivity(i);
        } else if (url.matches("(?i)reddit\\.com/r/[a-z0-9-_]+.*")) {
            // Subreddit. Format: reddit.com/r/$subreddit/$sort [optional]
            Intent intent = new Intent(context, SubredditView.class);
            intent.putExtra("subreddit", parts[2]);
            context.startActivity(intent);
        } else if (url.matches("(?i)reddit\\.com/u(ser)?/[a-z0-9-_]+.*")) {
            // User. Format: reddit.com/u [or user]/$username/$page [optional]
            String name = parts[2];
            if (name.equals("me")) name = Authentication.name;
            Intent myIntent = new Intent(context, Profile.class);
            myIntent.putExtra("profile", name);
            context.startActivity(myIntent);
        } else if (url.matches("(?i)reddit\\.com/prefs")) {
            customIntentChooser("https://www.reddit.com/prefs", context);
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

    /* Exclude a package (this app) from opening an intent.
    Source: http://stackoverflow.com/a/23268821/4026792
    */

    public static void customIntentChooser(String url, Context c) {
        String packageNameToIgnore = BuildConfig.APPLICATION_ID;
        Uri uri = Uri.parse(url);
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setData(uri);
        PackageManager packageManager = c.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        ArrayList<Intent> targetIntents = new ArrayList<>();
        for (ResolveInfo currentInfo : activities) {
            String packageName = currentInfo.activityInfo.packageName;
            if (!packageNameToIgnore.equals(packageName)) {
                Intent targetIntent = new Intent(android.content.Intent.ACTION_VIEW);
                targetIntent.setData(uri);
                targetIntent.setPackage(packageName);
                targetIntents.add(targetIntent);
            }
        }
        if (targetIntents.size() > 0) {
            Intent chooserIntent = Intent.createChooser(targetIntents.remove(0), "Open URL with");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetIntents.toArray(new Parcelable[]{}));
            c.startActivity(chooserIntent);
        } else
            Reddit.defaultShare(url, c);

    }
}
