package me.ccrama.redditslide;

import android.content.SharedPreferences;

import net.dean.jraw.models.Submission;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Set;

/**
 * Created by carlo_000 on 1/13/2016.
 */
public class PostMatch {
    /**
     * Checks if a string is totally or partially contained in a set of strings
     *
     * @param target     string to check
     * @param strings    set of strings to check in
     * @param totalMatch only allow total match, no partial matches
     * @return if the string is contained in the set of strings
     */
    public static boolean contains(String target, Set<String> strings, boolean totalMatch) {
        // filters are always stored lowercase
        if (totalMatch) {
            return strings.contains(target.toLowerCase(Locale.ENGLISH).trim());
        } else if (strings.contains(target.toLowerCase(Locale.ENGLISH).trim())) {
            return true;
        } else {
            for (String s : strings) {
                if (target.toLowerCase(Locale.ENGLISH).trim().contains(s)) {
                    return true;
                }
            }
            return false;
        }
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
    public static boolean isDomain(String target, Set<String> strings) throws MalformedURLException {
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
        try {
            return isDomain(url.toLowerCase(Locale.ENGLISH), SettingValues.alwaysExternal);
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public static SharedPreferences filters;

    public static boolean doesMatch(Submission s, String baseSubreddit, boolean ignore18) {
        if (Hidden.id.contains(s.getFullName())) return true; // if it's hidden we're not going to show it regardless

        String title = s.getTitle();
        String body = s.getSelftext();
        String domain = s.getUrl();
        String subreddit = s.getSubredditName();
        String flair = s.getSubmissionFlair().getText() != null ? s.getSubmissionFlair().getText() : "";

        if (contains(title, SettingValues.titleFilters, false)) return true;

        if (contains(body, SettingValues.textFilters, false)) return true;

        if (contains(s.getAuthor(), SettingValues.userFilters, false)) return true;

        try {
            if (isDomain(domain.toLowerCase(Locale.ENGLISH), SettingValues.domainFilters)) return true;
        } catch (MalformedURLException ignored) {
        }

        if (!subreddit.equalsIgnoreCase(baseSubreddit) && contains(subreddit, SettingValues.subredditFilters, true)) {
            return true;
        }

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
            case STREAMABLE:
            case VIDEO:
                if (videos) {
                    contentMatch = true;
                }
                break;
        }

        if (!flair.isEmpty())
            for (String flairText : SettingValues.flairFilters) {
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

        return contentMatch;
    }

    public static boolean doesMatch(Submission s) {
        String title = s.getTitle();
        String body = s.getSelftext();
        String domain = s.getUrl();
        String subreddit = s.getSubredditName();

        boolean domainc = false;

        boolean titlec = contains(title, SettingValues.titleFilters, false);

        boolean bodyc = contains(body, SettingValues.textFilters, false);

        try {
            domainc = isDomain(domain.toLowerCase(Locale.ENGLISH), SettingValues.domainFilters);
        } catch (MalformedURLException ignored) {
        }

        boolean subredditc = subreddit != null && !subreddit.isEmpty() && contains(subreddit, SettingValues.subredditFilters, true);

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
