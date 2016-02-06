package me.ccrama.redditslide.test;

import android.app.Activity;
import android.content.Intent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;

import me.ccrama.redditslide.Activities.SubredditView;
import me.ccrama.redditslide.BuildConfig;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.Views.MakeTextviewClickable;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class MakeTextviewClickableTest {

    MakeTextviewClickable makeTextviewClickable = new MakeTextviewClickable();

    @Test
    public void multipleCodeBlocks_shouldContainOriginalCodeText() throws IOException {
        // Use SubredditView since it works without issue
        Activity activity = Robolectric.buildActivity(SubredditView.class).withIntent(new Intent().putExtra("subreddit", "")).get();
        SpoilerRobotoTextView textView = new SpoilerRobotoTextView(activity.getApplicationContext());

        makeTextviewClickable.ParseTextWithLinksTextViewComment(TestUtils.getResource("submissions/submissionWithMultipleCodeBlocks.html"), textView, activity, "");

        // Note that the whitespaces in code have a char value of 160, NOT 32.
        // Convert to make assertions easier to type
        String text = textView.getText().toString().replace((char)160, ' ');

        assertThat(text, containsString("for byte in buffer[..read].iter()"));
        assertThat(text, containsString("for &byte in buffer[..read].iter()"));
    }
}
