package me.ccrama.redditslide.test;

import org.junit.Test;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.ccrama.redditslide.SubredditStorage;
import me.ccrama.redditslide.UserSubscriptions;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;



/**
 * Created by Alex Macleod on 28/03/2016.
 */
public class SubredditStorageTest {
    private ArrayList<String> subreddits = new ArrayList<String>(Arrays.asList(
            "xyy", "xyz", "frontpage", "mod", "friends", "random", "aaa"
    ));

    @Test
    public void sortsSubreddits() {
        assertThat(UserSubscriptions.sort(subreddits), is(new ArrayList<String>(Arrays.asList(
                "frontpage", "all", "random", "friends", "mod", "aaa", "xyy", "xyz"
        ))));
    }

    @Test
    public void sortsSubredditsNoExtras() {
        assertThat(UserSubscriptions.sortNoExtras(subreddits), is(new ArrayList<String>(Arrays.asList(
                "frontpage", "random", "friends", "mod", "aaa", "xyy", "xyz"
        ))));
    }
}
