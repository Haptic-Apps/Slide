package me.ccrama.redditslide.test;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import me.ccrama.redditslide.CaseInsensitiveArrayList;
import me.ccrama.redditslide.UserSubscriptions;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;



/**
 * Created by Alex Macleod on 28/03/2016.
 */
public class UserSubscriptionsTest {
    private final CaseInsensitiveArrayList subreddits = new CaseInsensitiveArrayList(Arrays.asList(
            "xyy", "xyz", "frontpage", "mod", "friends", "random", "aaa"
    ));

    @Test
    public void sortsSubreddits() {
        assertThat(UserSubscriptions.sort(subreddits), is(new ArrayList<>(Arrays.asList(
                "frontpage", "all", "random", "friends", "mod", "aaa", "xyy", "xyz"
        ))));
    }

    @Test
    public void sortsSubredditsNoExtras() {
        assertThat(UserSubscriptions.sortNoExtras(subreddits, false), is(new ArrayList<>(Arrays.asList(
                "frontpage", "random", "friends", "mod", "aaa", "xyy", "xyz"
        ))));
    }
}
