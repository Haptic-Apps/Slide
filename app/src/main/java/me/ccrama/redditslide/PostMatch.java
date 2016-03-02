package me.ccrama.redditslide;

import net.dean.jraw.models.Submission;

/**
 * Created by carlo_000 on 1/13/2016.
 */
public class PostMatch {
    public static boolean contains(String target, String[] strings, boolean totalMatch) {
        for (String s : strings) {
            if (totalMatch ? target.equals(s) : target.contains(s)) {
                return true;
            }
        }
        return false;
    }

    public static String[] titles;
    public static String[] texts;
    public static String[] domains;
    public static String[] subreddits;


    public static boolean doesMatch(Submission s) {
        String title = s.getTitle();
        String body = s.getSelftext();
        String domain = s.getUrl();
        String subreddit = s.getSubredditName();

        boolean titlec;
        boolean bodyc;
        boolean domainc;
        boolean subredditc;

        if (titles == null) titles = SettingValues.titleFilters.replaceAll("^[,\\s]+", "").split("[,\\s]+");
        if (texts == null) texts = SettingValues.textFilters.replaceAll("^[,\\s]+", "").split("[,\\s]+");
        if (domains == null) domains = SettingValues.domainFilters.replaceAll("^[,\\s]+", "").split("[,\\s]+");
        if (subreddits == null) subreddits = SettingValues.subredditFilters.replaceAll("^[,\\s]+", "").split("[,\\s]+");

        titlec = !SettingValues.titleFilters.isEmpty() && contains(title.toLowerCase(), titles, false);

        bodyc = !SettingValues.textFilters.isEmpty() && contains(body.toLowerCase(), texts, false);

        domainc = !SettingValues.domainFilters.isEmpty() && contains(domain.toLowerCase(), domains, false);

        subredditc = !SettingValues.subredditFilters.isEmpty() && contains(subreddit.toLowerCase(), subreddits, true);

        return (titlec || bodyc || domainc || subredditc);
    }
}
