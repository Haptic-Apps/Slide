package me.ccrama.redditslide.test;

import org.junit.Test;

import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.OpenRedditLink.RedditLinkType;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class OpenRedditLinkTest {

    //Less characters
    private String shortUrl(String url) {
        return OpenRedditLink.formatRedditUrl(url);
    }

    @Test
    public void testLinkType_Shortened() {
        assertThat(OpenRedditLink.getRedditLinkType(
                shortUrl("https://redd.it/eorhm/")), is(RedditLinkType.SHORTENED));
    }

    @Test
    public void testLinkType_Wiki() {
        assertThat(OpenRedditLink.getRedditLinkType(
                shortUrl("https://www.reddit.com/r/Android/wiki/index")), is(RedditLinkType.WIKI));
        assertThat(OpenRedditLink.getRedditLinkType(
                shortUrl("https://ww.reddit.com/r/Android/help")), is(RedditLinkType.WIKI));
    }

    @Test
    public void testLinkType_Comment() {
        assertThat(OpenRedditLink.getRedditLinkType(
                        shortUrl("https://www.reddit.com/r/announcements/comments/eorhm/reddit_30_less_typing/c19qk6j")),
                is(RedditLinkType.COMMENT_PERMALINK));
    }

    @Test
    public void testLinkType_Submission() {
        assertThat(OpenRedditLink.getRedditLinkType(
                        shortUrl("https://www.reddit.com/r/announcements/comments/eorhm/reddit_30_less_typing/")),
                is(RedditLinkType.SUBMISSION));
    }

    @Test
    public void testLinkType_SubmissionWithoutSub() {
        assertThat(OpenRedditLink.getRedditLinkType(
                        shortUrl("https://www.reddit.com/comments/eorhm/reddit_30_less_typing/")),
                is(RedditLinkType.SUBMISSION_WITHOUT_SUB));
    }

    @Test
    public void testLinkType_Subreddit() {
        assertThat(OpenRedditLink.getRedditLinkType(
                shortUrl("https://www.reddit.com/r/android")), is(RedditLinkType.SUBREDDIT));
    }

    @Test
    public void testLinkType_User() {
        assertThat(OpenRedditLink.getRedditLinkType(
                shortUrl("https://www.reddit.com/u/l3d00m")), is(RedditLinkType.USER));
    }

    @Test
    public void testLinkType_Other() {
        assertThat(OpenRedditLink.getRedditLinkType(
                shortUrl("https://www.reddit.com/live/wbjbjba8zrl6")), is(RedditLinkType.OTHER));
    }


    @Test
    public void testUrlFormatter_Basic() {
        assertThat(shortUrl("https://www.reddit.com/live/wbjbjba8zrl6"), is("reddit.com/live/wbjbjba8zrl6"));
    }

    @Test
    public void testUrlFormatter_Np() {
        assertThat(shortUrl("https://np.reddit.com/live/wbjbjba8zrl6"), is("npreddit.com/live/wbjbjba8zrl6"));
    }

    @Test
    public void testUrlFormatter_Prefix() {
        assertThat(shortUrl("https://blog.reddit.com/"), is(""));
    }

    @Test
    public void testUrlFormatter_Subreddit() {
        assertThat(shortUrl("/r/android"), is("reddit.com/r/android"));
    }

    @Test
    public void testURLFormatter_Wiki() {
        assertThat(shortUrl("https://reddit.com/help"), is("reddit.com/r/reddit.com/wiki"));
        assertThat(shortUrl("https://reddit.com/help/registration"), is("reddit.com/r/reddit.com/wiki/registration"));
        assertThat(shortUrl("https://www.reddit.com/r/android/wiki/index"), is("reddit.com/r/android/wiki/index"));
    }
}