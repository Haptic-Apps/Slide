package me.ccrama.redditslide.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import me.ccrama.redditslide.SpoilerRobotoTextView;

import static org.junit.Assert.fail;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SpoilerRobotoTextView.class)
public class SpoilerTextTest {
    private final Pattern htmlSpoilerPattern = Whitebox.getInternalState(SpoilerRobotoTextView.class, "htmlSpoilerPattern");
    private final Pattern nativeSpoilerPattern = Whitebox.getInternalState(SpoilerRobotoTextView.class, "nativeSpoilerPattern");

    private final List<Object[]> htmlSpoilerTests = new ArrayList<Object[]>() {{
        add(new Object[]{"<a href=\"#spoiler\">test</a>", true});
        add(new Object[]{"<a href=\"#sp\">test</a>", true});
        add(new Object[]{"<a href=\"#s\">test</a>", true});
        add(new Object[]{"<a href=\"#not-a-spoiler\">test</a>", false});
    }};

    private final List<Object[]> nativeSpoilerTests = new ArrayList<Object[]>() {{
        add(new Object[]{"<span class=\"md-spoiler-text\">test</span>", true});
        add(new Object[]{"<span class=\"md-bold-text md-spoiler-text md-italic-text\">test</span>", true});
        add(new Object[]{"<span class=\"not-a-spoiler\">test</span>", false});
    }};

    @Test
    public void htmlSpoilerTest() {
        spoilerTest(htmlSpoilerTests, htmlSpoilerPattern, "HTML spoiler test");
    }

    @Test
    public void nativeSpoilerTest() {
        spoilerTest(nativeSpoilerTests, nativeSpoilerPattern, "Native spoiler test");
    }

    private void spoilerTest(List<Object[]> tests, Pattern pattern, String name) {
        for (Object[] test : tests) {
            if (pattern.matcher((String) test[0]).matches() == (Boolean) test[1]) {
                System.out.println(name + ": " + (String) test[0] + " PASSED");
            } else {
                System.out.println(name + ": " + (String) test[0] + " FAILED");
                fail();
            }
        }
    }
}
