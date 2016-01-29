package me.ccrama.redditslide;

import net.dean.jraw.models.Submission;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by carlo_000 on 1/13/2016.
 */
public class PostMatch {
    public static boolean contains(String target, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(target.toLowerCase());
        return m.find();
    }
    public static boolean doesMatch(Submission s) {
        String title = s.getTitle();
        String body = s.getSelftext();
        String domain = s.getUrl();

        boolean titlec;
        boolean bodyc;
        boolean domainc;

        titlec = !Reddit.titleFilters.isEmpty() && contains(title.toLowerCase(), Reddit.titleFiltersRegex);

        bodyc = !Reddit.textFilters.isEmpty() && contains(body.toLowerCase(), Reddit.textFiltersRegex);

        domainc = !Reddit.domainFilters.isEmpty() && contains(domain.toLowerCase(), Reddit.domainFiltersRegex);

        return (titlec || bodyc || domainc);
    }
}
