package me.ccrama.redditslide.test;

import org.junit.Test;

import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.ContentType.ImageType;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


public class ContentTypeTest {

    @Test
    public void detectsAlbum() {
        assertThat(ContentType.getImageType("http://www.imgur.com/a/duARTe"), is(ImageType.ALBUM));
        assertThat(ContentType.getImageType("https://imgur.com/gallery/DmXJ4"), is(ImageType.ALBUM));
        assertThat(ContentType.getImageType("http://imgur.com/82UIrJk,dIjBFjv"), is(ImageType.ALBUM));
    }

    @Test
    public void detectsGif() {
        assertThat(ContentType.getImageType("https://i.imgur.com/33YIg0B.gifv"), is(ImageType.GIF));
        assertThat(ContentType.getImageType("https://i.imgur.com/33YIg0B.gif"), is(ImageType.GIF));
        assertThat(ContentType.getImageType("i.imgur.com/33YIg0B.gif?args=should&not=matter"), is(ImageType.GIF));
        assertThat(ContentType.getImageType("https://i.imgur.com/33YIg0B.gifnot"), is(not(ImageType.GIF)));

        assertThat(ContentType.getImageType("https://fat.gfycat.com/EcstaticLegitimateAnemone.webm"), is(ImageType.GIF));
        assertThat(ContentType.getImageType("https://thumbs.gfycat.com/EcstaticLegitimateAnemone-mobile.mp4"), is(ImageType.GIF));
        assertThat(ContentType.getImageType("https://gfycat.com/BogusAmpleArmednylonshrimp"), is(ImageType.GIF));
    }

    @Test
    public void detectsImage() {
        assertThat(ContentType.getImageType("https://i.imgur.com/FGtUo6c.jpg"), is(ImageType.IMAGE));
        assertThat(ContentType.getImageType("https://i.imgur.com/FGtUo6c.png"), is(ImageType.IMAGE));
        assertThat(ContentType.getImageType("https://i.imgur.com/FGtUo6c.png?moo=1"), is(ImageType.IMAGE));
        assertThat(ContentType.getImageType("https://i.reddituploads.com/289b451dc4bf4306878852f83b5cf6f9?fit=max&h=1536&w=1536&s=103e17990aa7084727ea43cda02c318b"), is(ImageType.IMAGE));
    }

    @Test
    public void detectsImgur() {
        assertThat(ContentType.getImageType("https://i.imgur.com/33YIg0B"), is(ImageType.IMGUR));
    }

    @Test
    public void detectsSpoiler() {
        assertThat(ContentType.getImageType("/s"), is(ImageType.SPOILER));
        assertThat(ContentType.getImageType("/sp"), is(ImageType.SPOILER));
        assertThat(ContentType.getImageType("/spoiler"), is(ImageType.SPOILER));
        assertThat(ContentType.getImageType("#s"), is(ImageType.SPOILER));
    }

    @Test
    public void detectsReddit() {
        assertThat(ContentType.getImageType("https://www.reddit.com/r/todayilearned/comments/42wgbg/til_the_tshirt_was_invented_in_1904_and_marketed/"), is(ImageType.REDDIT));
        assertThat(ContentType.getImageType("https://www.reddit.com/42wgbg/"), is(ImageType.REDDIT));
        assertThat(ContentType.getImageType("https://www.reddit.com/r/live/"), is(ImageType.REDDIT));
        assertThat(ContentType.getImageType("https://www.reddit.com"), is(ImageType.REDDIT));
        assertThat(ContentType.getImageType("redd.it/eorhm"), is(ImageType.REDDIT));
        assertThat(ContentType.getImageType("/r/Android"), is(ImageType.REDDIT));
        assertThat(ContentType.getImageType("https://www.reddit.com/r/Android/wiki/index"), is(ImageType.REDDIT));
        assertThat(ContentType.getImageType("https://www.reddit.com/r/Android/help"), is(ImageType.REDDIT));


        assertThat(ContentType.getImageType("https://www.reddit.com/live/wbjbjba8zrl6"), is(not(ImageType.REDDIT)));
    }

    @Test
    public void detectsStreamable() {
        assertThat(ContentType.getImageType("https://streamable.com/l41f"), is(ImageType.STREAMABLE));
    }

    @Test
    public void detectsLink() {
        assertThat(ContentType.getImageType("https://stackoverflow.com/"), is(ImageType.LINK));
    }

    @Test
    public void detectsNone() {
        assertThat(ContentType.getImageType(""), is(ImageType.NONE));
    }
}