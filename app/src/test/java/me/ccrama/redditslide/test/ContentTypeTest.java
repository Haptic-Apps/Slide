package me.ccrama.redditslide.test;

import org.junit.Test;

import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.ContentType.Type;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


public class ContentTypeTest {

    @Test
    public void detectsAlbum() {
        assertThat(ContentType.getContentType("http://www.imgur.com/a/duARTe"), is(Type.ALBUM));
        assertThat(ContentType.getContentType("https://imgur.com/gallery/DmXJ4"), is(Type.ALBUM));
        assertThat(ContentType.getContentType("http://imgur.com/82UIrJk,dIjBFjv"), is(Type.ALBUM));
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


        assertThat(ContentType.getContentType("https://www.reddit.com/live/wbjbjba8zrl6"), is(not(Type.REDDIT)));
    }

    @Test
    public void detectsStreamable() {
        assertThat(ContentType.getContentType("https://streamable.com/l41f"), is(Type.STREAMABLE));
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