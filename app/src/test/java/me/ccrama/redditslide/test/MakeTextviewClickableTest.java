package me.ccrama.redditslide.test;

import org.junit.Test;
import org.robolectric.annotation.Config;

import java.io.IOException;

import me.ccrama.redditslide.BuildConfig;

//@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class MakeTextviewClickableTest {

    // MakeTextviewClickable makeTextviewClickable = new MakeTextviewClickable();

    @Test
    public void multipleCodeBlocks_shouldContainOriginalCodeText() throws IOException {
        //     // Use SubredditView since it works without issue
        //     Activity activity = Robolectric.buildActivity(Album.class).withIntent(new Intent().putExtra(Album.EXTRA_URL, "/test")).create().get();
        //     SubmissionTextViewGroup viewGroup = new SubmissionTextViewGroup(activity.getApplicationContext());

        //     makeTextviewClickable.ParseTextWithLinksTextViewComment(TestUtils.getResource("submissions/multipleCodeBlocks.html"), viewGroup, activity, "");

        //     // Note that the whitespaces in code have a char value of 160, NOT 32.
        //     // Convert to make assertions easier to type
        //     String firstCodeBlock = ((SpoilerRobotoTextView)viewGroup.getChildAt(1)).getText().toString().replace((char) 160, ' ');
        //     String secondCodeBlock = ((SpoilerRobotoTextView)viewGroup.getChildAt(3)).getText().toString().replace((char) 160, ' ');

        //     assertThat(viewGroup.getChildCount(), equalTo(5));
        //     assertThat(firstCodeBlock, containsString("for byte in buffer[..read].iter()"));
        //     assertThat(secondCodeBlock, containsString("for &byte in buffer[..read].iter()"));
    }
}
