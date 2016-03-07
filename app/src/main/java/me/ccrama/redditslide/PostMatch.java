package me.ccrama.redditslide;

import android.content.SharedPreferences;

import net.dean.jraw.models.Submission;

/**
 * Created by carlo_000 on 1/13/2016.
 */
public class PostMatch {
    public static boolean contains(String target, String[] strings, boolean totalMatch) {
        for (String s : strings) {
            s = s.toLowerCase();
            if (totalMatch ? target.equals(s) : target.contains(s)) {
                return true;
            }
        }
        return false;
    }

    public static SharedPreferences filters;

    public static String[] titles = null;
    public static String[] texts = null;
    public static String[] domains = null;
    public static String[] subreddits = null;


    public static boolean doesMatch(Submission s, String baseSubreddit) {
        String title = s.getTitle();
        String body = s.getSelftext();
        String domain = s.getUrl();
        String subreddit = s.getSubredditName();

        boolean titlec;
        boolean bodyc;
        boolean domainc;
        boolean subredditc;

        if (titles == null)
            titles = SettingValues.titleFilters.replaceAll("^[,\\s]+", "").split("[,\\s]+");
        if (texts == null)
            texts = SettingValues.textFilters.replaceAll("^[,\\s]+", "").split("[,\\s]+");
        if (domains == null)
            domains = SettingValues.domainFilters.replaceAll("^[,\\s]+", "").split("[,\\s]+");
        if (subreddits == null)
            subreddits = SettingValues.subredditFilters.replaceAll("^[,\\s]+", "").split("[,\\s]+");

        titlec = !SettingValues.titleFilters.isEmpty() && contains(title.toLowerCase(), titles, false);

        bodyc = !SettingValues.textFilters.isEmpty() && contains(body.toLowerCase(), texts, false);

        domainc = !SettingValues.domainFilters.isEmpty() && contains(domain.toLowerCase(), domains, false);

        subredditc = !SettingValues.subredditFilters.isEmpty() && contains(subreddit.toLowerCase(), subreddits, true);
        boolean contentMatch = false;

        if (baseSubreddit == null || baseSubreddit.isEmpty()) baseSubreddit = "frontpage";
        baseSubreddit = baseSubreddit.toLowerCase();
        boolean gifs = isGif(baseSubreddit);
        boolean images = isImage(baseSubreddit);
        boolean nsfw = isNsfw(baseSubreddit);
        boolean albums = isAlbums(baseSubreddit);
        boolean urls = isUrls(baseSubreddit);
        boolean selftext = isSelftext(baseSubreddit);


        switch (ContentType.getImageType(s)) {

            case NSFW_IMAGE:
            case NSFW_GIF:
            case NSFW_GFY:
            case NSFW_LINK:
                if (!nsfw) contentMatch = true;
                break;
            case REDDIT:
            case EMBEDDED:
            case LINK:
            case IMAGE_LINK:
            case NONE_URL:
            case VIDEO:
                if (!urls) contentMatch = true;
                break;
            case SELF:
            case NONE:
                if (!selftext) contentMatch = true;
                break;
            case ALBUM:
                if (!albums) contentMatch = true;
                break;
            case IMAGE:
            case IMGUR:
            case NONE_IMAGE:
                if (!images) contentMatch = true;
                break;
            case GFY:
            case GIF:
            case NONE_GFY:
            case NONE_GIF:
                if (!gifs) contentMatch = true;
                break;

        }

        return (titlec || bodyc || domainc || subredditc || contentMatch);
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

        if (titles == null)
            titles = SettingValues.titleFilters.replaceAll("^[,\\s]+", "").split("[,\\s]+");
        if (texts == null)
            texts = SettingValues.textFilters.replaceAll("^[,\\s]+", "").split("[,\\s]+");
        if (domains == null)
            domains = SettingValues.domainFilters.replaceAll("^[,\\s]+", "").split("[,\\s]+");
        if (subreddits == null)
            subreddits = SettingValues.subredditFilters.replaceAll("^[,\\s]+", "").split("[,\\s]+");

        titlec = !SettingValues.titleFilters.isEmpty() && contains(title.toLowerCase(), titles, false);

        bodyc = !SettingValues.textFilters.isEmpty() && contains(body.toLowerCase(), texts, false);

        domainc = !SettingValues.domainFilters.isEmpty() && contains(domain.toLowerCase(), domains, false);

        subredditc = !SettingValues.subredditFilters.isEmpty() && contains(subreddit.toLowerCase(), subreddits, true);

        return (titlec || bodyc || domainc || subredditc);
    }

    public static void setChosen(boolean[] values, String subreddit) {
        subreddit = subreddit.toLowerCase();
        SharedPreferences.Editor e = filters.edit();
        e.putBoolean(subreddit + "_gifs", values[0]);
        e.putBoolean(subreddit + "_albums", values[1]);
        e.putBoolean(subreddit + "_images", values[2]);
        e.putBoolean(subreddit + "_nsfw", values[3]);
        e.putBoolean(subreddit + "_selftext", values[4]);
        e.putBoolean(subreddit + "_urls", values[5]);
        e.apply();

    }

    public static boolean isGif(String baseSubreddit) {
        return filters.getBoolean(baseSubreddit + "_gifs", true);
    }

    public static boolean isImage(String baseSubreddit) {
        return filters.getBoolean(baseSubreddit + "_images", true);
    }

    public static boolean isAlbums(String baseSubreddit) {
        return filters.getBoolean(baseSubreddit + "_albums", true);
    }

    public static boolean isNsfw(String baseSubreddit) {
        return filters.getBoolean(baseSubreddit + "_nsfw", true);
    }

    public static boolean isSelftext(String baseSubreddit) {
        return filters.getBoolean(baseSubreddit + "_selftext", true);
    }

    public static boolean isUrls(String baseSubreddit) {
        return filters.getBoolean(baseSubreddit + "_urls", true);
    }
}
