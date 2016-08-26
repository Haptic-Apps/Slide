package me.ccrama.redditslide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import java.util.Arrays;

import me.ccrama.redditslide.Activities.CommentsScreenSingle;
import me.ccrama.redditslide.Activities.LiveThread;
import me.ccrama.redditslide.Activities.Profile;
import me.ccrama.redditslide.Activities.Search;
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

    //Returns true if link was in fact handled by this method. If false, further action should be taken
    public static boolean openUrl(Context context, String url, boolean openIfOther) {
        String oldUrl = url;
        boolean np = false;

        LogUtil.v("Link is " + url);
        url = formatRedditUrl(url);
        if (url.isEmpty()) {
            LinkUtil.openExternally(oldUrl, context, false);
            return false;
        } else if (url.startsWith("np")) {
            np = true;
            url = url.substring(2);
        }

        RedditLinkType type = getRedditLinkType(url);

        String[] parts = url.split("/");
        if (parts[parts.length - 1].startsWith("?")) parts = Arrays.copyOf(parts, parts.length - 1);

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
            case LIVE: {
                Intent i = new Intent(context, LiveThread.class);
                i.putExtra(LiveThread.EXTRA_LIVEURL, parts[2]);
                context.startActivity(i);
            }
            break;
            case WIKI: {
                Intent i = new Intent(context, Wiki.class);
                i.putExtra(Wiki.EXTRA_SUBREDDIT, parts[2]);
                String page;
                if (parts.length >= 5) {
                    page = parts[4];
                    if (page.contains("#")) {
                        page = page.substring(0, page.indexOf("#"));
                    }
                    i.putExtra(Wiki.EXTRA_PAGE, page);
                }
                context.startActivity(i);
                break;
            }
            case SEARCH: {
                Intent i = new Intent(context, Search.class);
                String end = parts[parts.length - 1];
                end = end.replace(":", "%3A");
                boolean restrictSub = end.contains("restrict_sr=on");
                if (restrictSub) {
                    i.putExtra(Search.EXTRA_SUBREDDIT, parts[2]);
                } else {
                    i.putExtra(Search.EXTRA_SUBREDDIT, "all");
                }
                if (end.contains("q=")) {
                    String querry;
                    int index = end.indexOf("q=");
                    if (end.contains("&") && end.contains("+")) {
                        querry = end.substring(index + 2,
                                end.contains("+") ? Math.max(end.lastIndexOf("+", index),
                                        end.indexOf("&", index)) : end.indexOf("&", index));
                    } else if (end.contains("&")) {
                        querry = end.substring(index + 2, end.indexOf("&", index));
                    } else {
                        querry = end.substring(index + 2, end.length());
                    }
                    i.putExtra(Search.EXTRA_TERM, querry);
                }
                if (end.contains("author:")) {
                    String author;
                    int index = end.indexOf("author:");
                    if (end.contains("&") && end.contains("+")) {
                        author = end.substring(index + 7,
                                end.contains("+") ? Math.max(end.indexOf("+", index),
                                        end.indexOf("&", index)) : end.indexOf("&", index));
                    } else if (end.contains("&")) {
                        author = end.substring(index + 7, end.indexOf("&", index));
                    } else {
                        author = end.substring(index + 7, end.length());
                    }
                    i.putExtra(Search.EXTRA_AUTHOR, author);
                }
                if (end.contains("nsfw:")) {
                    boolean nsfw;
                    int index = end.indexOf("nsfw:");
                    if (end.contains("&") && end.contains("+")) {
                        nsfw = end.substring(index + 5,
                                end.contains("+") ? Math.max(end.indexOf("+", index),
                                        end.indexOf("&", index)) : end.indexOf("&", index))
                                .equals("yes");
                    } else if (end.contains("&")) {
                        nsfw = end.substring(index + 5, end.indexOf("&", index)).equals("yes");
                    } else {
                        nsfw = end.substring(index + 5, end.length()).equals("yes");
                    }
                    i.putExtra(Search.EXTRA_NSFW, nsfw);
                }
                if (end.contains("self:")) {
                    boolean self;
                    int index = end.indexOf("self:");
                    if (end.contains("&") && end.contains("+")) {
                        self = end.substring(index + 5,
                                end.contains("+") ? Math.max(end.indexOf("+", index),
                                        end.indexOf("&", index)) : end.indexOf("&", index))
                                .equals("yes");
                    } else if (end.contains("&")) {
                        self = end.substring(index + 5, end.indexOf("&", index)).equals("yes");
                    } else {
                        self = end.substring(index + 5, end.length()).equals("yes");
                    }
                    i.putExtra(Search.EXTRA_SELF, self);
                }
                if (end.contains("selftext:")) {
                    boolean selftext;
                    int index = end.indexOf("selftext:");
                    if (end.contains("&") && end.contains("+")) {
                        selftext = end.substring(index + 5,
                                end.contains("+") ? Math.max(end.indexOf("+", index),
                                        end.indexOf("&", index)) : end.indexOf("&", index))
                                .equals("yes");
                    } else if (end.contains("&")) {
                        selftext = end.substring(index + 5, end.indexOf("&", index)).equals("yes");
                    } else {
                        selftext = end.substring(index + 5, end.length()).equals("yes");
                    }
                    i.putExtra(Search.EXTRA_SELF, selftext);
                }
                if (end.contains("url:")) {
                    String s_url;
                    int index = end.indexOf("url:");
                    if (end.contains("&") && end.contains("+")) {
                        s_url = end.substring(index + 4,
                                end.contains("+") ? Math.max(end.indexOf("+", index),
                                        end.indexOf("&", index)) : end.indexOf("&", index));
                    } else if (end.contains("&")) {
                        s_url = end.substring(index + 4, end.indexOf("&", index));
                    } else {
                        s_url = end.substring(index + 4, end.length());
                    }
                    i.putExtra(Search.EXTRA_URL, s_url);
                }
                if (end.contains("site:")) {
                    String site;
                    int index = end.indexOf("site:");
                    if (end.contains("&") && end.contains("+")) {
                        site = end.substring(index + 5,
                                end.contains("+") ? Math.max(end.indexOf("+", index),
                                        end.indexOf("&", index)) : end.indexOf("&", index));
                    } else if (end.contains("&")) {
                        site = end.substring(index + 5, end.indexOf("&", index));
                    } else {
                        site = end.substring(index + 5, end.length());
                    }
                    i.putExtra(Search.EXTRA_SITE, site);
                }
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

                    if (end.length() >= 3) i.putExtra(CommentsScreenSingle.EXTRA_CONTEXT, end);

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
                if (openIfOther) {
                    if (context instanceof Activity) {
                        LinkUtil.openUrl(oldUrl, Palette.getStatusBarColor(), (Activity) context);
                    } else {
                        Intent i = new Intent(context, Website.class);
                        i.putExtra(Website.EXTRA_URL, oldUrl);
                        context.startActivity(i);
                    }
                } else {
                    return false;
                }
                break;
            }
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
     * Takes an reddit.com url and formats it for easier use
     *
     * @param url The url to format
     * @return Formatted url without subdomains, language tags & other unused prefixes
     */
    public static String formatRedditUrl(String url) {

        // Strip unused prefixes that don't require special handling
        url = url.replaceFirst("(?i)^(https?://)?(www\\.)?((ssl|pay)\\.)?", "");

        if (url.matches("(?i)[a-z0-9-_]+\\.reddit\\.com.*")) { // tests for subdomain
            String subdomain = url.split("\\.", 2)[0];
            String domainRegex = "(?i)" + subdomain + "\\.reddit\\.com";
            if (subdomain.equalsIgnoreCase("np")) {
                // no participation link: https://www.reddit.com/r/NoParticipation/wiki/index
                url = url.replaceFirst(domainRegex, "reddit.com");
                url = "np" + url;
            } else if (subdomain.matches("beta|blog|code|mod|out|store")) {
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

        // Converts links such as reddit.com/help to reddit.com/r/reddit.com/wiki
        if (url.matches("(?i)[^/]++/(?>wiki|help)(?>$|/.*)")) {
            url = url.replaceFirst("(?i)/(?>wiki|help)", "/r/reddit.com/wiki");
        }

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
        } else if (url.matches("(?i)reddit\\.com/live/[^/]*")) {
            return RedditLinkType.LIVE;
        } else if (url.matches("(?i)reddit\\.com(?:/r/[a-z0-9-_.]+)?/(?:wiki|help).*")) {
            // Wiki link. Format: reddit.com/r/$subreddit/wiki/$page [optional]
            return RedditLinkType.WIKI;
        } else if (url.matches("(?i)reddit\\.com/r/[a-z0-9-_.]+/about.*")) {
            // Unhandled link. Format: reddit.com/r/$subreddit/about/$page [optional]
            return RedditLinkType.OTHER;
        } else if (url.matches("(?i)reddit\\.com/r/[a-z0-9-_.]+/search.*")) {
            // Wiki link. Format: reddit.com/r/$subreddit/search?q= [optional]
            return RedditLinkType.SEARCH;
        } else if (url.matches("(?i)reddit\\.com/r/[a-z0-9-_.]+/comments/\\w+/\\w*/.*")) {
            // Permalink to comments. Format: reddit.com/r/$subreddit/comments/$post_id/$post_title [can be empty]/$comment_id
            return RedditLinkType.COMMENT_PERMALINK;
        } else if (url.matches("(?i)reddit\\.com/r/[a-z0-9-_.]+/comments/\\w+.*")) {
            // Submission. Format: reddit.com/r/$subreddit/comments/$post_id/$post_title [optional]
            return RedditLinkType.SUBMISSION;
        } else if (url.matches("(?i)reddit\\.com/comments/\\w+.*")) {
            // Submission without a given subreddit. Format: reddit.com/comments/$post_id/$post_title [optional]
            return RedditLinkType.SUBMISSION_WITHOUT_SUB;
        } else if (url.matches("(?i)reddit\\.com/r/[a-z0-9-_.]+.*")) {
            // Subreddit. Format: reddit.com/r/$subreddit/$sort [optional]
            return RedditLinkType.SUBREDDIT;
        } else if (url.matches("(?i)reddit\\.com/u(?:ser)?/[a-z0-9-_]+.*")) {
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
        SEARCH,
        LIVE,
        OTHER
    }


}
