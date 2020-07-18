package me.ccrama.redditslide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Objects;

import me.ccrama.redditslide.Activities.CommentsScreenSingle;
import me.ccrama.redditslide.Activities.LiveThread;
import me.ccrama.redditslide.Activities.MainActivity;
import me.ccrama.redditslide.Activities.MultiredditOverview;
import me.ccrama.redditslide.Activities.OpenContent;
import me.ccrama.redditslide.Activities.Profile;
import me.ccrama.redditslide.Activities.Search;
import me.ccrama.redditslide.Activities.SendMessage;
import me.ccrama.redditslide.Activities.Submit;
import me.ccrama.redditslide.Activities.SubredditView;
import me.ccrama.redditslide.Activities.Website;
import me.ccrama.redditslide.Activities.Wiki;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.LinkUtil;
import me.ccrama.redditslide.util.LogUtil;

public class OpenRedditLink {

    public OpenRedditLink(Context context, String url) {
        openUrl(context, url, true);
    }

    public OpenRedditLink(Context context, String url, boolean openIfOther) {
        openUrl(context, url, openIfOther);
    }

    /**
     * If the Uri has a query parameter called key, call intent.putExtra with the corresponding
     * query paramter value as a String.
     *
     * @param intent The intent to put the data into
     * @param uri    The uri to look up the query parameter from
     * @param name   The name of the data to add to the intent. e.g. {@link Intent#EXTRA_SUBJECT}
     * @param key    The query parameter key, e.g. selftext in ?selftext=true
     * @see Intent#putExtra(String, String)
     */
    private static void putExtraIfParamExists(Intent intent, Uri uri, String name, String key) {
        String param = uri.getQueryParameter(key);

        if (param != null) {
            intent.putExtra(name, param);
        }
    }

    /**
     * If the Uri has a query parameter called key, and it equals toCompare, call intent.putExtra
     * with true.
     *
     * @param intent    The intent to put the data into
     * @param uri       The uri to look up the query parameter from
     * @param name      The name of the data to add to the intent. e.g. {@link Intent#EXTRA_SUBJECT}
     * @param key       The query parameter key, e.g. selftext in ?selftext=true
     * @param toCompare The value to compare the query parameter value to
     * @see Intent#putExtra(String, boolean)
     */
    private static void putExtraIfParamEquals(Intent intent, Uri uri, String name, String key,
            String toCompare) {
        String param = uri.getQueryParameter(key);

        if (param != null && param.equals(toCompare)) {
            intent.putExtra(name, true);
        }
    }

    //Returns true if link was in fact handled by this method. If false, further action should be taken
    public static boolean openUrl(Context context, String url, boolean openIfOther) {
        boolean np = false;

        LogUtil.v("Link is " + url);
        Uri uri = formatRedditUrl(url);

        if (uri == null) {
            LinkUtil.openExternally(url);
            return false;
        }

        if (uri.getHost().startsWith("np")) {
            np = true;
            uri = uri.buildUpon().authority("reddit.com").build();
        }

        RedditLinkType type = getRedditLinkType(uri);

        List<String> parts = uri.getPathSegments();

        Intent i = null;
        switch (type) {
            case SHORTENED: {
                i = new Intent(context, CommentsScreenSingle.class);
                i.putExtra(CommentsScreenSingle.EXTRA_SUBREDDIT, Reddit.EMPTY_STRING);
                i.putExtra(CommentsScreenSingle.EXTRA_CONTEXT, Reddit.EMPTY_STRING);
                i.putExtra(CommentsScreenSingle.EXTRA_NP, np);
                i.putExtra(CommentsScreenSingle.EXTRA_SUBMISSION, parts.get(0));
                break;
            }
            case LIVE: {
                i = new Intent(context, LiveThread.class);
                i.putExtra(LiveThread.EXTRA_LIVEURL, parts.get(1));
                break;
            }
            case WIKI: {
                i = new Intent(context, Wiki.class);
                i.putExtra(Wiki.EXTRA_SUBREDDIT, parts.get(1));

                if (parts.size() >= 4) {
                    i.putExtra(Wiki.EXTRA_PAGE, parts.get(3));
                }
                break;
            }
            case SEARCH: {
                i = new Intent(context, Search.class);

                String restrictSub = uri.getQueryParameter("restrict_sr");
                if (restrictSub != null && restrictSub.equals("on")) {
                    i.putExtra(Search.EXTRA_SUBREDDIT, parts.get(1));
                } else {
                    i.putExtra(Search.EXTRA_SUBREDDIT, "all");
                }

                putExtraIfParamExists(i, uri, Search.EXTRA_TERM, "q");
                putExtraIfParamExists(i, uri, Search.EXTRA_AUTHOR, "author");
                putExtraIfParamExists(i, uri, Search.EXTRA_URL, "url");
                putExtraIfParamExists(i, uri, Search.EXTRA_SITE, "site");

                putExtraIfParamEquals(i, uri, Search.EXTRA_NSFW, "nsfw", "yes");
                putExtraIfParamEquals(i, uri, Search.EXTRA_SELF, "self", "yes");
                putExtraIfParamEquals(i, uri, Search.EXTRA_SELF, "selftext", "yes");

                break;
            }
            case SUBMIT: {
                i = new Intent(context, Submit.class);
                i.putExtra(Submit.EXTRA_SUBREDDIT, parts.get(1));

                // Submit already uses EXTRA_SUBJECT for title and EXTRA_TEXT for URL for sharing so we use those
                putExtraIfParamExists(i, uri, Intent.EXTRA_SUBJECT, "title");

                // Reddit behavior: If selftext is true or if selftext doesn't exist and text does exist then page
                // defaults to showing self post page. If selftext is false, or doesn't exist and no text then the page
                // defaults to showing the link post page.
                // We say isSelfText=true for the "no selftext, no text, no url" condition because that's slide's
                // default behavior for the submit page, whereas reddit's behavior would say isSelfText=false.
                boolean isSelfText = uri.getBooleanQueryParameter("selftext", false)
                        || uri.getQueryParameter("text") != null
                        || !uri.getBooleanQueryParameter("url", false);

                i.putExtra(Submit.EXTRA_IS_SELF, isSelfText);

                putExtraIfParamExists(i, uri, Submit.EXTRA_BODY, "text");
                putExtraIfParamExists(i, uri, Intent.EXTRA_TEXT, "url");

                break;
            }
            case COMMENT_PERMALINK: {
                i = new Intent(context, CommentsScreenSingle.class);
                if (parts.get(0).equalsIgnoreCase("u") || parts.get(0).equalsIgnoreCase("user")) {
                    // Prepend u_ because user profile posts are made to /r/u_username
                    i.putExtra(CommentsScreenSingle.EXTRA_SUBREDDIT, "u_" + parts.get(2));
                } else {
                    i.putExtra(CommentsScreenSingle.EXTRA_SUBREDDIT, parts.get(1));
                }
                i.putExtra(CommentsScreenSingle.EXTRA_SUBMISSION, parts.get(3));
                i.putExtra(CommentsScreenSingle.EXTRA_NP, np);
                if (parts.size() >= 6) {
                    i.putExtra(CommentsScreenSingle.EXTRA_LOADMORE, true);
                    String end = parts.get(5);

                    if (end.length() >= 3) i.putExtra(CommentsScreenSingle.EXTRA_CONTEXT, end);

                    putExtraIfParamExists(i, uri, CommentsScreenSingle.EXTRA_CONTEXT_NUMBER,"context");

                    try {
                        String contextNumber = uri.getQueryParameter("context");
                        if (contextNumber != null) {
                            i.putExtra(CommentsScreenSingle.EXTRA_CONTEXT_NUMBER, Integer.parseInt(contextNumber));
                        }
                    } catch (NumberFormatException ignored) {

                    }
                }
                break;
            }
            case SUBMISSION: {
                i = new Intent(context, CommentsScreenSingle.class);
                if (parts.get(0).equalsIgnoreCase("u") || parts.get(0).equalsIgnoreCase("user")) {
                    // Prepend u_ because user profile posts are made to /r/u_username
                    i.putExtra(CommentsScreenSingle.EXTRA_SUBREDDIT, "u_" + parts.get(1));
                } else {
                    i.putExtra(CommentsScreenSingle.EXTRA_SUBREDDIT, parts.get(1));
                }
                i.putExtra(CommentsScreenSingle.EXTRA_CONTEXT, Reddit.EMPTY_STRING);
                i.putExtra(CommentsScreenSingle.EXTRA_NP, np);
                i.putExtra(CommentsScreenSingle.EXTRA_SUBMISSION, parts.get(3));
                break;
            }
            case SUBMISSION_WITHOUT_SUB: {
                i = new Intent(context, CommentsScreenSingle.class);
                i.putExtra(CommentsScreenSingle.EXTRA_SUBREDDIT, Reddit.EMPTY_STRING);
                i.putExtra(CommentsScreenSingle.EXTRA_CONTEXT, Reddit.EMPTY_STRING);
                i.putExtra(CommentsScreenSingle.EXTRA_NP, np);
                i.putExtra(CommentsScreenSingle.EXTRA_SUBMISSION, parts.get(1));
                break;
            }
            case SUBREDDIT: {
                i = new Intent(context, SubredditView.class);
                i.putExtra(SubredditView.EXTRA_SUBREDDIT, parts.get(1));
                break;
            }
            case MULTIREDDIT: {
                i = new Intent(context, MultiredditOverview.class);
                i.putExtra(MultiredditOverview.EXTRA_PROFILE, parts.get(1));
                i.putExtra(MultiredditOverview.EXTRA_MULTI, parts.get(3));
                break;
            }
            case MESSAGE: {
                i = new Intent(context, SendMessage.class);

                putExtraIfParamExists(i, uri, SendMessage.EXTRA_NAME, "to");
                putExtraIfParamExists(i, uri, SendMessage.EXTRA_SUBJECT, "subject");
                putExtraIfParamExists(i, uri, SendMessage.EXTRA_MESSAGE, "message");

                break;
            }
            case USER: {
                String name = parts.get(1);
                if (name.equals("me") && Authentication.isLoggedIn) name = Authentication.name;
                i = new Intent(context, Profile.class);
                i.putExtra(Profile.EXTRA_PROFILE, name);
                break;
            }
            case HOME: {
                i = new Intent(context, MainActivity.class);
                break;
            }
            case OTHER: {
                if (openIfOther) {
                    if (context instanceof Activity) {
                        LinkUtil.openUrl(url, Palette.getStatusBarColor(), (Activity) context);
                    } else {
                        i = new Intent(context, Website.class);
                        i.putExtra(LinkUtil.EXTRA_URL, url);
                    }
                } else {
                    return false;
                }
                break;
            }
        }
        if (i != null) {
            if (context instanceof OpenContent) {
                // i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                // i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(i);
        }
        return true;
    }

    public OpenRedditLink(Context c, String submission, String subreddit, String id) {
        Intent i = new Intent(c, CommentsScreenSingle.class);
        i.putExtra(CommentsScreenSingle.EXTRA_SUBREDDIT, subreddit);
        i.putExtra(CommentsScreenSingle.EXTRA_CONTEXT, id);
        i.putExtra(CommentsScreenSingle.EXTRA_SUBMISSION, submission);
        c.startActivity(i);
    }

    /**
     * Append every path segment in segments to a Uri.Builder
     *
     * @param builder Uri builder to append the path segments to
     * @param segments A list of path segments to append to the builder
     *
     * @see Uri#getPathSegments() 
     */
    private static void appendPathSegments(Uri.Builder builder, List<String> segments) {
        for (String segment : segments) {
            builder.appendPath(segment);
        }
    }

    /**
     * Takes a reddit.com url and formats it for easier use
     *
     * @param url The url to format
     * @return Formatted Uri without subdomains, language tags & other unused prefixes
     */
    @Nullable
    public static Uri formatRedditUrl(String url) {
        if (url == null) {
            return null;
        }

        Uri uri = LinkUtil.formatURL(url);

        if (uri.getHost().equals("www.google.com")) {
            String ampURL = uri.getQueryParameter("url");
            if (ampURL != null) {
                Uri ampURI = Uri.parse(ampURL);
                String host = ampURI.getHost();
                if (host != null && host.equals("amp.reddit.com")) {
                    uri = ampURI;
                }
            }

            if (uri.getPath().startsWith("/amp/s/amp.reddit.com")) {
                List<String> segments = uri.getPathSegments();
                Uri.Builder builder = uri.buildUpon().authority("reddit.com").path(null);

                appendPathSegments(builder, segments.subList(3, segments.size()));

                uri = builder.build();
            }

        }

        if (uri.getHost().matches("(?i).+\\.reddit\\.com")) { // tests for subdomain
            Uri.Builder builder = uri.buildUpon();
            String host = uri.getHost();

            String subdomain = host.split("\\.", 2)[0];

            if (subdomain.equalsIgnoreCase("np")) {
                // no participation link: https://www.reddit.com/r/NoParticipation/wiki/index
                host = "npreddit.com";
            } else if (subdomain.matches("www|ssl|pay|amp|old|new|")
                    // country codes (e.g. en-GB, us)
                    || subdomain.matches("(?i)([_a-z0-9]{2}-)?[_a-z0-9]{1,2}")) {
                // Subdomains that don't require special handling
                host = "reddit.com";
            } else if (subdomain.matches("beta|blog|code|mod|out|store")) {
                return null;
            } else {
                // subdomain is a subreddit, change subreddit.reddit.com to reddit.com/r/subreddit
                host = "reddit.com";
                builder.path("r").appendPath(subdomain);
                appendPathSegments(builder, uri.getPathSegments());
            }

            uri = builder.authority(host).build();
        }

        List<String> segments = uri.getPathSegments();
        // Converts links such as reddit.com/help to reddit.com/r/reddit.com/wiki
        if (!segments.isEmpty() && segments.get(0).matches("w|wiki|help")) {
            Uri.Builder builder = uri.buildUpon().path("/r/reddit.com/wiki");

            appendPathSegments(builder, segments.subList(1, segments.size()));

            uri = builder.build();
        }

        return uri;
    }

    /**
     * Determines the reddit link type
     *
     * @param uri Reddit.com link
     * @return LinkType
     */
    public static RedditLinkType getRedditLinkType(@NonNull Uri uri) {
        String host = Objects.requireNonNull(uri.getHost());

        if (host.equals("redd.it")) {
            return RedditLinkType.SHORTENED;
        }

        String path = Objects.requireNonNull(uri.getPath());

        if (path.matches("(?i)/live/[^/]*")) {
            return RedditLinkType.LIVE;
        } else if (path.matches("(?i)/message/compose.*")) {
            return RedditLinkType.MESSAGE;
        } else if (path.matches("(?i)(?:/r/[a-z0-9-_.]+)?/(?:w|wiki|help).*")) {
            // Wiki link. Format: reddit.com/r/$subreddit/w[iki]/$page [optional]
            return RedditLinkType.WIKI;
        } else if (path.matches("(?i)/r/[a-z0-9-_.]+/about.*")) {
            // Unhandled link. Format: reddit.com/r/$subreddit/about/$page [optional]
            return RedditLinkType.OTHER;
        } else if (path.matches("(?i)/r/[a-z0-9-_.]+/search.*")) {
            // Wiki link. Format: reddit.com/r/$subreddit/search?q= [optional]
            return RedditLinkType.SEARCH;
        } else if (path.matches("(?i)/r/[a-z0-9-_.]+/submit.*")) {
            // Submit post link. Format: reddit.com/r/$subreddit/submit
            return RedditLinkType.SUBMIT;
        } else if (path.matches("(?i)/(?:r|u(?:ser)?)/[a-z0-9-_.]+/comments/\\w+/[\\w-]*/.+")) {
            // Permalink to comments. Format: reddit.com/r [or u or user]/$subreddit/comments/$post_id/$post_title [can be empty]/$comment_id
            return RedditLinkType.COMMENT_PERMALINK;
        } else if (path.matches("(?i)/(?:r|u(?:ser)?)/[a-z0-9-_.]+/comments/\\w+.*")) {
            // Submission. Format: reddit.com/r [or u or user]/$subreddit/comments/$post_id/$post_title [optional]
            return RedditLinkType.SUBMISSION;
        } else if (path.matches("(?i)/comments/\\w+.*")) {
            // Submission without a given subreddit. Format: reddit.com/comments/$post_id/$post_title [optional]
            return RedditLinkType.SUBMISSION_WITHOUT_SUB;
        } else if (path.matches("(?i)/r/[a-z0-9-_.]+.*")) {
            // Subreddit. Format: reddit.com/r/$subreddit/$sort [optional]
            return RedditLinkType.SUBREDDIT;
        } else if (path.matches("(?i)/u(?:ser)?/[a-z0-9-_]+.*/m/[a-z0-9_]+.*")) {
            // Multireddit. Format: reddit.com/u [or user]/$username/m/$multireddit/$sort [optional]
            return RedditLinkType.MULTIREDDIT;
        } else if (path.matches("(?i)/u(?:ser)?/[a-z0-9-_]+.*")) {
            // User. Format: reddit.com/u [or user]/$username/$page [optional]
            return RedditLinkType.USER;
        } else if (path.matches("^/?$")) {
            // Reddit home link
            return RedditLinkType.HOME;
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
        SEARCH,
        MESSAGE,
        MULTIREDDIT,
        LIVE,
        SUBMIT,
        HOME,
        OTHER
    }


}
