package me.ccrama.redditslide;

import android.content.SharedPreferences;

import net.dean.jraw.models.Submission;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

/**
 * Created by carlo_000 on 1/13/2016.
 */
public class PostMatch {
    public static boolean contains(String target, String[] strings, boolean totalMatch) {
        for (String s : strings) {
            s = s.toLowerCase(Locale.ENGLISH).trim();
            if (!s.isEmpty() && !s.equals("\n") && totalMatch ? target.equals(s)
                    : target.contains(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a domain should be filtered or not: returns true if the target domain ends with the
     * comparison domain and if supplied, target path begins with the comparison path
     *
     * @param target  URL to check
     * @param strings The URLs to check against
     * @return If the target is covered by any strings
     * @throws MalformedURLException
     */
    public static boolean isDomain(String target, String[] strings) throws MalformedURLException {
        URL domain = new URL(target);
        for (String s : strings) {
            if (!s.contains("/")) {
                if (ContentType.hostContains(domain.getHost(), s)) {
                    return true;
                } else {
                    continue;
                }
            }

            if (!s.contains("://")) {
                s = "http://" + s;
            }

            try {
                URL comparison = new URL(s.toLowerCase(Locale.ENGLISH));

                if (ContentType.hostContains(domain.getHost(), comparison.getHost())
                        && domain.getPath().startsWith(comparison.getPath())) {
                    return true;
                }
            } catch (MalformedURLException ignored) {
            }
        }
        return false;
    }

    public static boolean openExternal(String url) {
        if (externalDomain == null) {
            externalDomain =
                    SettingValues.alwaysExternal.replaceAll("^[,\\s]+", "").split("[,\\s]+");
        }
        try {
            return !SettingValues.alwaysExternal.isEmpty() && isDomain(
                    url.toLowerCase(Locale.ENGLISH), externalDomain);
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public static SharedPreferences filters;

    public static String[] titles         = null;
    public static String[] texts          = null;
    public static String[] domains        = null;
    public static String[] subreddits     = null;
    public static String[] externalDomain = null;
    public static String[] flairs         = null;
    public static String[] users          = null;


    public static boolean doesMatch(Submission s, String baseSubreddit, boolean ignore18) {
        String title = s.getTitle();
        String body = s.getSelftext();
        String domain = s.getUrl();
        String subreddit = s.getSubredditName();
        String flair =
                s.getSubmissionFlair().getText() != null ? s.getSubmissionFlair().getText() : "";

        boolean titlec;
        boolean bodyc;
        boolean domainc;
        boolean subredditc;
        boolean userc;

        if (titles == null) {
            titles = SettingValues.titleFilters.replaceAll("^[,\\s]+", "").split("[,\\s]+");
        }
        if (texts == null) {
            texts = SettingValues.textFilters.replaceAll("^[,\\s]+", "").split("[,\\s]+");
        }
        if (domains == null) {
            domains = SettingValues.domainFilters.replaceAll("^[,\\s]+", "").split("[,\\s]+");
        }
        if (subreddits == null) {
            subreddits = SettingValues.subredditFilters.replaceAll("^[,\\s]+", "").split("[,\\s]+");
        }
        if (flairs == null) {
            flairs = SettingValues.flairFilters.replaceAll("^[,]+", "").split("[,]+");
        }
        if (users == null) {
            users = SettingValues.userFilters.replaceAll("^[,\\s]+", "").split("[,\\s]+");
        }

        titlec =
                !SettingValues.titleFilters.isEmpty() && contains(title.toLowerCase(Locale.ENGLISH),
                        titles, false);

        bodyc = !SettingValues.textFilters.isEmpty() && contains(body.toLowerCase(Locale.ENGLISH),
                texts, false);

        userc = !SettingValues.userFilters.isEmpty() && contains(
                s.getAuthor().toLowerCase(Locale.ENGLISH), users, false);

        try {
            domainc = !SettingValues.domainFilters.isEmpty() && isDomain(
                    domain.toLowerCase(Locale.ENGLISH), domains);
        } catch (MalformedURLException e) {
            domainc = false;
        }

        subredditc = !subreddit.equalsIgnoreCase(baseSubreddit)
                && !SettingValues.subredditFilters.isEmpty()
                && contains(subreddit.toLowerCase(Locale.ENGLISH), subreddits, true);

        boolean contentMatch = false;

        if (baseSubreddit == null || baseSubreddit.isEmpty()) {
            baseSubreddit = "frontpage";
        }

        baseSubreddit = baseSubreddit.toLowerCase(Locale.ENGLISH);
        boolean gifs = isGif(baseSubreddit);
        boolean images = isImage(baseSubreddit);
        boolean nsfw = isNsfw(baseSubreddit);
        boolean albums = isAlbums(baseSubreddit);
        boolean urls = isUrls(baseSubreddit);
        boolean selftext = isSelftext(baseSubreddit);
        boolean videos = isVideo(baseSubreddit);


        if (s.isNsfw()) {
            if (!SettingValues.showNSFWContent) {
                contentMatch = true;
            }
            if (ignore18) {
                contentMatch = false;
            }
            if (nsfw) {
                contentMatch = true;
            }
        }
        switch (ContentType.getContentType(s)) {
            case REDDIT:
            case EMBEDDED:
            case LINK:
                if (urls) {
                    contentMatch = true;
                }
                break;
            case SELF:
            case NONE:
                if (selftext) {
                    contentMatch = true;
                }
                break;
            case ALBUM:
                if (albums) {
                    contentMatch = true;
                }
                break;
            case IMAGE:
            case DEVIANTART:
            case IMGUR:
            case XKCD:
                if (images) {
                    contentMatch = true;
                }
                break;
            case GIF:
                if (gifs) {
                    contentMatch = true;
                }
                break;
            case VID_ME:
            case STREAMABLE:
            case VIDEO:
                if (videos) {
                    contentMatch = true;
                }
                break;
        }

        if (!flair.isEmpty()) {
            for (String flairText : flairs) {
                if (flairText.toLowerCase(Locale.ENGLISH).startsWith(baseSubreddit)) {
                    String[] split = flairText.split(":");
                    if (split[0].equalsIgnoreCase(baseSubreddit)) {
                        if (flair.equalsIgnoreCase(split[1].trim())) {
                            contentMatch = true;
                            break;
                        }
                    }
                }
            }
        }

        return (titlec
                || bodyc
                || userc
                || domainc
                || subredditc
                || contentMatch
                || Hidden.id.contains(s.getFullName()));
    }

    public static boolean doesMatch(Submission s) {
        String title = s.getTitle();
        String body = s.getSelftext();
        String domain = s.getUrl();
        String subreddit = s.getSubredditName();

        boolean titlec;
        boolean bodyc;
        boolean domainc;
        boolean subredditc;

        if (titles == null) {
            titles = SettingValues.titleFilters.replaceAll("^[,\\s]+", "").split("[,\\s]+");
        }
        if (texts == null) {
            texts = SettingValues.textFilters.replaceAll("^[,\\s]+", "").split("[,\\s]+");
        }
        if (domains == null) {
            domains = SettingValues.domainFilters.replaceAll("^[,\\s]+", "").split("[,\\s]+");
        }
        if (subreddits == null) {
            subreddits = SettingValues.subredditFilters.replaceAll("^[,\\s]+", "").split("[,\\s]+");
        }

        titlec =
                !SettingValues.titleFilters.isEmpty() && contains(title.toLowerCase(Locale.ENGLISH),
                        titles, false);

        bodyc = !SettingValues.textFilters.isEmpty() && contains(body.toLowerCase(Locale.ENGLISH),
                texts, false);

        domainc = !SettingValues.domainFilters.isEmpty() && contains(
                domain.toLowerCase(Locale.ENGLISH), domains, false);

        subredditc = subreddit != null
                && !subreddit.isEmpty()
                && !SettingValues.subredditFilters.isEmpty()
                && contains(subreddit.toLowerCase(Locale.ENGLISH), subreddits, true);

        return (titlec || bodyc || domainc || subredditc);
    }

    public static void setChosen(boolean[] values, String subreddit) {
        subreddit = subreddit.toLowerCase(Locale.ENGLISH);
        SharedPreferences.Editor e = filters.edit();
        e.putBoolean(subreddit + "_gifsFilter", values[2]);
        e.putBoolean(subreddit + "_albumsFilter", values[1]);
        e.putBoolean(subreddit + "_imagesFilter", values[0]);
        e.putBoolean(subreddit + "_nsfwFilter", values[6]);
        e.putBoolean(subreddit + "_selftextFilter", values[5]);
        e.putBoolean(subreddit + "_urlsFilter", values[4]);
        e.putBoolean(subreddit + "_videoFilter", values[3]);
        e.apply();
    }

    public static boolean isGif(String baseSubreddit) {
        return filters.getBoolean(baseSubreddit + "_gifsFilter", false);
    }

    public static boolean isImage(String baseSubreddit) {
        return filters.getBoolean(baseSubreddit + "_imagesFilter", false);
    }

    public static boolean isAlbums(String baseSubreddit) {
        return filters.getBoolean(baseSubreddit + "_albumsFilter", false);
    }

    public static boolean isNsfw(String baseSubreddit) {
        return filters.getBoolean(baseSubreddit + "_nsfwFilter", false);
    }

    public static boolean isSelftext(String baseSubreddit) {
        return filters.getBoolean(baseSubreddit + "_selftextFilter", false);
    }

    public static boolean isUrls(String baseSubreddit) {
        return filters.getBoolean(baseSubreddit + "_urlsFilter", false);
    }

    public static boolean isVideo(String baseSubreddit) {
        return filters.getBoolean(baseSubreddit + "_videoFilter", false);
    }
}
