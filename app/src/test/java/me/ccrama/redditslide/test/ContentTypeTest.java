package me.ccrama.redditslide.test;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.ContentType.Type;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class ContentTypeTest {

    @BeforeClass
    public static void setUp() {
        SettingValues.alwaysExternal = new HashSet<>(Arrays.asList(
                "twitter.com",
                "github.com",
                "t.co",
                "example.com/path"
        ));
    }

    @Test
    public void comparesHosts() {
        assertTrue(ContentType.hostContains("www.example.com", "example.com"));
        assertTrue(ContentType.hostContains("www.example.com", "www.example.com"));
        assertTrue(ContentType.hostContains("www.example.com", "no-match", "example.com"));
        assertTrue(ContentType.hostContains("www.example.com", "", null, "example.com"));
        assertTrue(ContentType.hostContains("www.example.com", "example.com", "no-match"));
        assertTrue(ContentType.hostContains("example.com.www.example.com", "example.com"));

        assertFalse(ContentType.hostContains("www.example.com", "www.example.com.au"));
        assertFalse(ContentType.hostContains("www.example.com", "www.example"));
        assertFalse(ContentType.hostContains("www.example.com", "notexample.com"));
        assertFalse(ContentType.hostContains("www.example.com", ""));
    }

    @Test
    public void detectsAlbum() {
        assertThat(ContentType.getContentType("http://www.imgur.com/a/duARTe"), is(Type.ALBUM));
        assertThat(ContentType.getContentType("https://imgur.com/gallery/DmXJ4"), is(Type.ALBUM));
        assertThat(ContentType.getContentType("http://imgur.com/82UIrJk,dIjBFjv"), is(Type.ALBUM));
    }

    @Test
    public void detectsExternal() {
        assertThat(ContentType.getContentType("https://twitter.com/jaffathecake/status/718071903378735105?s=09"), is(Type.EXTERNAL));
        assertThat(ContentType.getContentType("https://github.com/ccrama/Slide"), is(Type.EXTERNAL));
        assertThat(ContentType.getContentType("http://example.com/path/that/matches"), is(Type.EXTERNAL));
        assertThat(ContentType.getContentType("http://example.com/path"), is(Type.EXTERNAL));
        assertThat(ContentType.getContentType("http://subdomain.example.com/path"), is(Type.EXTERNAL));
        assertThat(ContentType.getContentType("http://subdomain.twitter.com"), is(Type.EXTERNAL));

        // t.co NOT t.com
        assertThat(ContentType.getContentType("https://t.com"), is(not(Type.EXTERNAL)));
        assertThat(ContentType.getContentType("example.com/differentpath"), is(not(Type.EXTERNAL)));
        assertThat(ContentType.getContentType("https://example.com"), is(not(Type.EXTERNAL)));
    }

    @Test
    public void detectsGif() {
        assertThat(ContentType.getContentType("https://i.imgur.com/33YIg0B.gifv"), is(Type.GIF));
        assertThat(ContentType.getContentType("https://i.imgur.com/33YIg0B.gif"), is(Type.GIF));
        assertThat(ContentType.getContentType("i.imgur.com/33YIg0B.gif?args=should&not=matter"), is(Type.GIF));
        assertThat(ContentType.getContentType("https://i.imgur.com/33YIg0B.gifnot"), is(not(Type.GIF)));

        assertThat(ContentType.getContentType("https://fat.gfycat.com/EcstaticLegitimateAnemone.webm"), is(Type.GIF));
        assertThat(ContentType.getContentType("https://thumbs.gfycat.com/EcstaticLegitimateAnemone-mobile.mp4"), is(Type.GIF));
        assertThat(ContentType.getContentType("https://gfycat.com/BogusAmpleArmednylonshrimp"), is(Type.GIF));
    }

    @Test
    public void detectsImage() {
        assertThat(ContentType.getContentType("https://i.imgur.com/FGtUo6c.jpg"), is(Type.IMAGE));
        assertThat(ContentType.getContentType("https://i.imgur.com/FGtUo6c.png"), is(Type.IMAGE));
        assertThat(ContentType.getContentType("https://i.imgur.com/FGtUo6c.png?moo=1"), is(Type.IMAGE));
        assertThat(ContentType.getContentType("https://i.reddituploads.com/289b451dc4bf4306878852f83b5cf6f9?fit=max&h=1536&w=1536&s=103e17990aa7084727ea43cda02c318b"), is(Type.IMAGE));
    }

    @Test
    public void detectsImgur() {
        assertThat(ContentType.getContentType("https://i.imgur.com/33YIg0B"), is(Type.IMGUR));
    }

    @Test
    public void detectsSpoiler() {
        assertThat(ContentType.getContentType("/s"), is(Type.SPOILER));
        assertThat(ContentType.getContentType("/sp"), is(Type.SPOILER));
        assertThat(ContentType.getContentType("/spoiler"), is(Type.SPOILER));
        assertThat(ContentType.getContentType("#s"), is(Type.SPOILER));
    }

    @Test
    public void detectsReddit() {
        assertThat(ContentType.getContentType("https://www.reddit.com/r/todayilearned/comments/42wgbg/til_the_tshirt_was_invented_in_1904_and_marketed/"), is(Type.REDDIT));
        assertThat(ContentType.getContentType("https://www.reddit.com/42wgbg/"), is(Type.REDDIT));
        assertThat(ContentType.getContentType("https://www.reddit.com/r/live/"), is(Type.REDDIT));
        assertThat(ContentType.getContentType("https://www.reddit.com"), is(Type.REDDIT));
        assertThat(ContentType.getContentType("redd.it/eorhm"), is(Type.REDDIT));
        assertThat(ContentType.getContentType("/r/Android"), is(Type.REDDIT));
        assertThat(ContentType.getContentType("https://www.reddit.com/r/Android/wiki/index"), is(Type.REDDIT));
        assertThat(ContentType.getContentType("https://www.reddit.com/r/Android/help"), is(Type.REDDIT));
        assertThat(ContentType.getContentType("https://www.reddit.com/live/wbjbjba8zrl6"), is(Type.REDDIT));
    }

    @Test
    public void detectsWithoutScheme() {
        // Capitalised
        assertThat(ContentType.getContentType("Https://google.com"), is(not(Type.NONE)));
        // Missing
        assertThat(ContentType.getContentType("google.com"), is(not(Type.NONE)));
        // Protocol relative
        assertThat(ContentType.getContentType("//google.com"), is(not(Type.NONE)));
    }

    @Test
    public void detectsVideo() {
        Reddit.videoPlugin = true;
        assertThat(ContentType.getContentType("https://www.youtube.com/watch?v=lX_pF03vCSU"), is(Type.VIDEO));
        assertThat(ContentType.getContentType("https://youtu.be/lX_pF03vCSU"), is(Type.VIDEO));

        assertThat(ContentType.getContentType("https://www.gifyoutube.com/"), is(not(Type.VIDEO)));

        Reddit.videoPlugin = false;
        assertThat(ContentType.getContentType("https://www.youtube.com/watch?v=lX_pF03vCSU"), is(not(Type.VIDEO)));
        assertThat(ContentType.getContentType("https://youtu.be/lX_pF03vCSU"), is(not(Type.VIDEO)));
    }

    @Test
    public void detectsStreamable() {
        assertThat(ContentType.getContentType("https://streamable.com/l41f"), is(Type.STREAMABLE));
    }

    @Test
    public void detectsDeviantart() {
        assertThat(ContentType.getContentType("http://manweri.deviantart.com/art/A-centaur-in-disguise-179507382"), is(Type.DEVIANTART));
    }

    @Test
    public void detectsLink() {
        assertThat(ContentType.getContentType("https://stackoverflow.com/"), is(Type.LINK));
    }

    @Test
    public void detectsNone() {
        assertThat(ContentType.getContentType(""), is(Type.NONE));
    }
}