package me.ccrama.redditslide.test;

import org.junit.Test;

import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.ContentType.contentTypes;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


public class ContentTypeTest {

    @Test
    public void detectsAlbum() {
        assertThat(ContentType.getContentType("http://www.imgur.com/a/duARTe"), is(contentTypes.ALBUM));
        assertThat(ContentType.getContentType("https://imgur.com/gallery/DmXJ4"), is(contentTypes.ALBUM));
        assertThat(ContentType.getContentType("http://imgur.com/82UIrJk,dIjBFjv"), is(contentTypes.ALBUM));
    }

    @Test
    public void detectsGif() {
        assertThat(ContentType.getContentType("https://i.imgur.com/33YIg0B.gifv"), is(contentTypes.GIF));
        assertThat(ContentType.getContentType("https://i.imgur.com/33YIg0B.gif"), is(contentTypes.GIF));
        assertThat(ContentType.getContentType("i.imgur.com/33YIg0B.gif?args=should&not=matter"), is(contentTypes.GIF));
        assertThat(ContentType.getContentType("https://i.imgur.com/33YIg0B.gifnot"), is(not(contentTypes.GIF)));

        assertThat(ContentType.getContentType("https://fat.gfycat.com/EcstaticLegitimateAnemone.webm"), is(contentTypes.GIF));
        assertThat(ContentType.getContentType("https://thumbs.gfycat.com/EcstaticLegitimateAnemone-mobile.mp4"), is(contentTypes.GIF));
        assertThat(ContentType.getContentType("https://gfycat.com/BogusAmpleArmednylonshrimp"), is(contentTypes.GIF));
    }

    @Test
    public void detectsImage() {
        assertThat(ContentType.getContentType("https://i.imgur.com/FGtUo6c.jpg"), is(contentTypes.IMAGE));
        assertThat(ContentType.getContentType("https://i.imgur.com/FGtUo6c.png"), is(contentTypes.IMAGE));
        assertThat(ContentType.getContentType("https://i.imgur.com/FGtUo6c.png?moo=1"), is(contentTypes.IMAGE));
        assertThat(ContentType.getContentType("https://i.reddituploads.com/289b451dc4bf4306878852f83b5cf6f9?fit=max&h=1536&w=1536&s=103e17990aa7084727ea43cda02c318b"), is(contentTypes.IMAGE));
    }

    @Test
    public void detectsImgur() {
        assertThat(ContentType.getContentType("https://i.imgur.com/33YIg0B"), is(contentTypes.IMGUR));
    }

    @Test
    public void detectsSpoiler() {
        assertThat(ContentType.getContentType("/s"), is(contentTypes.SPOILER));
        assertThat(ContentType.getContentType("/sp"), is(contentTypes.SPOILER));
        assertThat(ContentType.getContentType("/spoiler"), is(contentTypes.SPOILER));
        assertThat(ContentType.getContentType("#s"), is(contentTypes.SPOILER));
    }

    @Test
    public void detectsReddit() {
        assertThat(ContentType.getContentType("https://www.reddit.com/r/todayilearned/comments/42wgbg/til_the_tshirt_was_invented_in_1904_and_marketed/"), is(contentTypes.REDDIT));
        assertThat(ContentType.getContentType("https://www.reddit.com/42wgbg/"), is(contentTypes.REDDIT));
        assertThat(ContentType.getContentType("https://www.reddit.com/r/live/"), is(contentTypes.REDDIT));
        assertThat(ContentType.getContentType("https://www.reddit.com"), is(contentTypes.REDDIT));
        assertThat(ContentType.getContentType("redd.it/eorhm"), is(contentTypes.REDDIT));
        assertThat(ContentType.getContentType("/r/Android"), is(contentTypes.REDDIT));
        assertThat(ContentType.getContentType("https://www.reddit.com/r/Android/wiki/index"), is(contentTypes.REDDIT));
        assertThat(ContentType.getContentType("https://www.reddit.com/r/Android/help"), is(contentTypes.REDDIT));


        assertThat(ContentType.getContentType("https://www.reddit.com/live/wbjbjba8zrl6"), is(not(contentTypes.REDDIT)));
    }

    @Test
    public void detectsStreamable() {
        assertThat(ContentType.getContentType("https://streamable.com/l41f"), is(contentTypes.STREAMABLE));
    }

    @Test
    public void detectsLink() {
        assertThat(ContentType.getContentType("https://stackoverflow.com/"), is(contentTypes.LINK));
    }

    @Test
    public void detectsNone() {
        assertThat(ContentType.getContentType(""), is(contentTypes.NONE));
    }
}