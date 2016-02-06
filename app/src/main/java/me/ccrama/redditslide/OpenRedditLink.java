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
import me.ccrama.redditslide.Activities.Profile;
import me.ccrama.redditslide.Activities.SubredditView;
import me.ccrama.redditslide.Activities.Wiki;
import me.ccrama.redditslide.util.LogUtil;

public class OpenRedditLink {

    public OpenRedditLink(Context context, String url) {
        String oldUrl = url;
        boolean np = false;

        url = formatRedditUrl(url);
        if (url.isEmpty()) {
            customIntentChooser(oldUrl, context);
            return;
        } else if (url.startsWith("np")) {
            np = true;
            url = url.substring(2);
        }
        Log.v(LogUtil.getTag(), "Opening URL " + url);

        RedditLinkType type = getRedditLinkType(url);

        String[] parts = url.split("/");
        if (parts[parts.length - 1].startsWith("?"))
            parts = Arrays.copyOf(parts, parts.length - 1);

        switch (type) {
            case SHORTENED: {
                Intent i = new Intent(context, CommentsScreenSingle.class);
                i.putExtra(CommentsScreenSingle.EXTRA_SUBREDDIT, Reddit.EMPTY_STRING);
                i.putExtra(CommentsScreenSingle.EXTRA_CONTEXT, Reddit.EMPTY_STRING);
                i.putExtra(CommentsScreenSingle.EXTRA_NP, np);
                i.putExtra(CommentsScreenSingle.EXTRA_SUBMISSION, parts[1]);
                context.startActivity(i);
                break;
            }
            case WIKI: {
                Intent i = new Intent(context, Wiki.class);
                i.putExtra(Wiki.EXTRA_SUBREDDIT, parts[2]);
                context.startActivity(i);
                break;
            }
            case COMMENT_PERMALINK: {
                Intent i = new Intent(context, CommentsScreenSingle.class);
                i.putExtra(CommentsScreenSingle.EXTRA_SUBREDDIT, parts[2]);
                i.putExtra(CommentsScreenSingle.EXTRA_SUBMISSION, parts[4]);
                i.putExtra(CommentsScreenSingle.EXTRA_NP, np);
                if (parts.length >= 7) {
                    i.putExtra(CommentsScreenSingle.EXTRA_LOADMORE, true);
                    String end = parts[6];
                    if (end.contains("?")) end = end.substring(0, end.indexOf("?"));
                    i.putExtra(CommentsScreenSingle.EXTRA_CONTEXT, end);

                    Log.v(LogUtil.getTag(), "CONTEXT " + end);
                }
                context.startActivity(i);
                break;
            }
            case SUBMISSION: {
                Intent i = new Intent(context, CommentsScreenSingle.class);
                i.putExtra(CommentsScreenSingle.EXTRA_SUBREDDIT, parts[2]);
                i.putExtra(CommentsScreenSingle.EXTRA_CONTEXT, Reddit.EMPTY_STRING);
                i.putExtra(CommentsScreenSingle.EXTRA_NP, np);
                i.putExtra(CommentsScreenSingle.EXTRA_SUBMISSION, parts[4]);
                context.startActivity(i);
                break;
            }
            case SUBMISSION_WITHOUT_SUB: {
                Intent i = new Intent(context, CommentsScreenSingle.class);
                i.putExtra(CommentsScreenSingle.EXTRA_SUBREDDIT, Reddit.EMPTY_STRING);
                i.putExtra(CommentsScreenSingle.EXTRA_CONTEXT, Reddit.EMPTY_STRING);
                i.putExtra(CommentsScreenSingle.EXTRA_NP, np);
                i.putExtra(CommentsScreenSingle.EXTRA_SUBMISSION, parts[2]);
                context.startActivity(i);
                break;
            }
            case SUBREDDIT: {
                Intent intent = new Intent(context, SubredditView.class);
                intent.putExtra(SubredditView.EXTRA_SUBREDDIT, parts[2]);
                context.startActivity(intent);
                break;
            }
            case USER: {
                String name = parts[2];
                if (name.equals("me") && Authentication.isLoggedIn) name = Authentication.name;
                Intent myIntent = new Intent(context, Profile.class);
                myIntent.putExtra(Profile.EXTRA_PROFILE, name);
                context.startActivity(myIntent);
                break;
            }
            case OTHER: {
                customIntentChooser(oldUrl, context);
                break;
            }
        }
    }


    public OpenRedditLink(Context c, String submission, String subreddit, String id) {
        Intent i = new Intent(c, CommentsScreenSingle.class);
        i.putExtra(CommentsScreenSingle.EXTRA_SUBREDDIT, subreddit);
        i.putExtra(CommentsScreenSingle.EXTRA_CONTEXT, id);
        i.putExtra(CommentsScreenSingle.EXTRA_SUBMISSION, submission);
        c.startActivity(i);
    }

    /**
     * Show an intent chooser ("Open link with...") and exclude this app from the chooser.
     * Source: http://stackoverflow.com/a/23268821/4026792
     *
     * @param url The url as a String
     * @param c   Context for opening the intent
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
            Intent chooserIntent = Intent.createChooser(targetIntents.remove(0), c.getString(R.string.misc_link_chooser));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetIntents.toArray(new Parcelable[]{}));
            c.startActivity(chooserIntent);
        } else
            Reddit.defaultShare(url, c);
    }

    /**
     * Takes an reddit.com url and formats it for easier use
     *
     * @param url The url to format
     * @return Formatted url without subdomains, language tags & other unused prefixes
     */
    public static String formatRedditUrl(String url) {

        // Strip unused prefixes that don't require special handling
        url = url.replaceFirst("(?i)^(https?://)?(www\\.)?((ssl|pay)\\.)?", "");

        if (url.matches("(?i)[a-z0-9-_]+\\.reddit\\.com[a-z0-9-_/?=&]*.*")) { // tests for subdomain
            String subdomain = url.split("\\.", 2)[0];
            String domainRegex = "(?i)" + subdomain + "\\.reddit\\.com";
            if (subdomain.equalsIgnoreCase("np")) {
                // no participation link: https://www.reddit.com/r/NoParticipation/wiki/index
                url = url.replaceFirst(domainRegex, "reddit.com");
                url = "np" + url;
            } else if (subdomain.matches("blog|store|beta")) {
                return "";
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

        return url;
    }

    /**
     * Determines the reddit link type
     *
     * @param url Reddit.com link
     * @return LinkType
     */
    public static RedditLinkType getRedditLinkType(String url) {
        if (url.matches("(?i)redd\\.it/\\w+")) {
            // Redd.it link. Format: redd.it/post_id
            return RedditLinkType.SHORTENED;
        } else if (url.matches("(?i)reddit\\.com/r/[a-z0-9-_]+/wiki.*")) {
            // Wiki link. Format: reddit.com/r/$subreddit/wiki/$page [optional]
            return RedditLinkType.WIKI;
        } else if (url.matches("(?i)reddit\\.com/r/[a-z0-9-_]+/comments/\\w+/\\w*/.*")) {
            // Permalink to comments. Format: reddit.com/r/$subreddit/comments/$post_id/$post_title [can be empty]/$comment_id
            return RedditLinkType.COMMENT_PERMALINK;
        } else if (url.matches("(?i)reddit\\.com/r/[a-z0-9-_]+/comments/\\w+.*")) {
            // Submission. Format: reddit.com/r/$subreddit/comments/$post_id/$post_title [optional]
            return RedditLinkType.SUBMISSION;
        } else if (url.matches("(?i)reddit\\.com/comments/\\w+.*")) {
            // Submission without a given subreddit. Format: reddit.com/comments/$post_id/$post_title [optional]
            return RedditLinkType.SUBMISSION_WITHOUT_SUB;
        } else if (url.matches("(?i)reddit\\.com/r/[a-z0-9-_]+.*")) {
            // Subreddit. Format: reddit.com/r/$subreddit/$sort [optional]
            return RedditLinkType.SUBREDDIT;
        } else if (url.matches("(?i)reddit\\.com/u(ser)?/[a-z0-9-_]+.*")) {
            // User. Format: reddit.com/u [or user]/$username/$page [optional]
            return RedditLinkType.USER;
        } else {
            //Open all links that we can't open in another app
            return RedditLinkType.OTHER;
        }
    }

    public enum RedditLinkType {
        SHORTENED,
        WIKI,
        COMMENT_PERMALINK,
        SUBMISSION,
        SUBMISSION_WITHOUT_SUB,
        SUBREDDIT,
        USER,
        OTHER
    }


}
