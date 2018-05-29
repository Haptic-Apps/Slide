package me.ccrama.redditslide.test;

import org.junit.Test;

import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.OpenRedditLink.RedditLinkType;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class OpenRedditLinkTest {

    // Less characters
    private String formatURL(String url) {
        return OpenRedditLink.formatRedditUrl(url);
    }

    private OpenRedditLink.RedditLinkType getType(String url) {
        return OpenRedditLink.getRedditLinkType(url);
    }

    @Test
    public void detectsShortened() {
        assertThat(getType(formatURL("https://redd.it/eorhm/")), is(RedditLinkType.SHORTENED));
    }

    @Test
    public void detectsWiki() {
        assertThat(getType(formatURL("https://www.reddit.com/r/Android/wiki/index")),
                is(RedditLinkType.WIKI));
        assertThat(getType(formatURL("https://www.reddit.com/r/Android/help")),
                is(RedditLinkType.WIKI));
        assertThat(getType(formatURL("https://reddit.com/help")), is(RedditLinkType.WIKI));
    }

    @Test
    public void detectsComment() {
        assertThat(getType(formatURL(
                "https://www.reddit.com/r/announcements/comments/eorhm/reddit_30_less_typing/c19qk6j")),
                is(RedditLinkType.COMMENT_PERMALINK));
        assertThat(getType(formatURL(
                "https://www.reddit.com/r/announcements/comments/eorhm//c19qk6j")),
                is(RedditLinkType.COMMENT_PERMALINK));
    }

    @Test
    public void detectsSubmission() {
        assertThat(getType(formatURL(
                "https://www.reddit.com/r/announcements/comments/eorhm/reddit_30_less_typing/")),
                is(RedditLinkType.SUBMISSION));
    }

    @Test
    public void detectsSubmissionWithoutSub() {
        assertThat(
                getType(formatURL("https://www.reddit.com/comments/eorhm/reddit_30_less_typing/")),
                is(RedditLinkType.SUBMISSION_WITHOUT_SUB));
    }

    @Test
    public void detectsSubreddit() {
        assertThat(getType(formatURL("https://www.reddit.com/r/android")),
                is(RedditLinkType.SUBREDDIT));
    }

    @Test
    public void detectsSearch() {
//        assertThat(getType(formatURL("https://www.reddit.com/search?q=test")),
//                is(RedditLinkType.SEARCH));
        assertThat(getType(formatURL(
                "https://www.reddit.com/r/Android/search?q=test&restrict_sr=on&sort=relevance&t=all")),
                is(RedditLinkType.SEARCH));
    }

    @Test
    public void detectsUser() {
        assertThat(getType(formatURL("https://www.reddit.com/u/l3d00m")), is(RedditLinkType.USER));
    }

    @Test
    public void detectsHome() {
        assertThat(getType(formatURL("https://www.reddit.com/")), is(RedditLinkType.HOME));
    }

    @Test
    public void detectsOther() {
        assertThat(getType(formatURL("https://www.reddit.com/r/pics/about/moderators")),
                is(RedditLinkType.OTHER));
        assertThat(getType(formatURL("https://www.reddit.com/live/x9gf3donjlkq/discussions")),
                is(RedditLinkType.OTHER));
        assertThat(getType(formatURL("https://www.reddit.com/live/x9gf3donjlkq/contributors")),
                is(RedditLinkType.OTHER));
    }

    @Test
    public void detectsLive() {
        assertThat(getType(formatURL("https://www.reddit.com/live/x9gf3donjlkq")),
                is(RedditLinkType.LIVE));
    }

    @Test
    public void formatsBasic() {
        assertThat(formatURL("https://www.reddit.com/live/wbjbjba8zrl6"),
                is("reddit.com/live/wbjbjba8zrl6"));
    }

    @Test
    public void formatsNp() {
        assertThat(formatURL("https://np.reddit.com/live/wbjbjba8zrl6"),
                is("npreddit.com/live/wbjbjba8zrl6"));
    }

    @Test
    public void formatsSubdomains() {
        assertThat(formatURL("https://beta.reddit.com/"), is(""));
        assertThat(formatURL("https://blog.reddit.com/"), is(""));
        assertThat(formatURL("https://code.reddit.com/"), is(""));
        // https://www.reddit.com/r/modnews/comments/4z2nic/upcoming_change_updates_to_modredditcom/
        assertThat(formatURL("https://mod.reddit.com/"), is(""));
        // https://www.reddit.com/r/changelog/comments/49jjb7/reddit_change_click_events_on_outbound_links/
        assertThat(formatURL("https://out.reddit.com/"), is(""));
        assertThat(formatURL("https://store.reddit.com/"), is(""));
        assertThat(formatURL("https://pay.reddit.com/"), is("reddit.com"));
        assertThat(formatURL("https://ssl.reddit.com/"), is("reddit.com"));
        assertThat(formatURL("https://en-gb.reddit.com/"), is("reddit.com"));
        assertThat(formatURL("https://us.reddit.com/"), is("reddit.com"));
    }

    @Test
    public void formatsSubreddit() {
        assertThat(formatURL("/r/android"), is("reddit.com/r/android"));
        assertThat(formatURL("https://android.reddit.com"), is("reddit.com/r/android"));
    }

    @Test
    public void formatsWiki() {
        assertThat(formatURL("https://reddit.com/help"), is("reddit.com/r/reddit.com/wiki"));
        assertThat(formatURL("https://reddit.com/help/registration"),
                is("reddit.com/r/reddit.com/wiki/registration"));
        assertThat(formatURL("https://www.reddit.com/r/android/wiki/index"),
                is("reddit.com/r/android/wiki/index"));
    }

    @Test
    public void formatsProtocol() {
        assertThat(formatURL("http://reddit.com"), is("reddit.com"));
        assertThat(formatURL("https://reddit.com"), is("reddit.com"));
    }
}