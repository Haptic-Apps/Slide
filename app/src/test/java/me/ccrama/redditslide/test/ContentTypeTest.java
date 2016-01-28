package me.ccrama.redditslide.test;

import org.junit.Test;

import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.ContentType.ImageType;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;


public class ContentTypeTest {

    @Test
    public void testContentType_Album() {
        assertThat(ContentType.getImageType("http://www.imgur.com/a/duARTe"), is(ImageType.ALBUM));
        assertThat(ContentType.getImageType("https://imgur.com/gallery/DmXJ4"), is(ImageType.ALBUM));
    }

    @Test
    public void testContentType_Gif() {
        assertThat(ContentType.getImageType("https://i.imgur.com/33YIg0B.gifv"), is(ImageType.GIF));
        assertThat(ContentType.getImageType("https://i.imgur.com/33YIg0B.gif"), is(ImageType.GIF));
        assertThat(ContentType.getImageType("https://i.imgur.com/33YIg0B.gifnot"), is(not(ImageType.GIF)));
    }

    @Test
    public void testContentType_Gfy() {
        assertThat(ContentType.getImageType("https://fat.gfycat.com/EcstaticLegitimateAnemone.webm"), is(ImageType.GFY));
        assertThat(ContentType.getImageType("https://thumbs.gfycat.com/EcstaticLegitimateAnemone-mobile.mp4"), is(ImageType.GFY));
    }

    @Test
    public void testContentType_Image() {
        assertThat(ContentType.getImageType("https://i.imgur.com/FGtUo6c.jpg"), is(ImageType.IMAGE));
        assertThat(ContentType.getImageType("https://i.imgur.com/FGtUo6c.png"), is(ImageType.IMAGE));
    }

    @Test
    public void testContentType_Imgur() {
        assertThat(ContentType.getImageType("https://i.imgur.com/33YIg0B"), is(ImageType.IMGUR));
    }

    @Test
    public void testContentType_Reddit() {
        assertThat(ContentType.getImageType("https://www.reddit.com/r/todayilearned/comments/42wgbg/til_the_tshirt_was_invented_in_1904_and_marketed/"), is(ImageType.REDDIT));
        assertThat(ContentType.getImageType("https://www.reddit.com/42wgbg/"), is(ImageType.REDDIT));
        assertThat(ContentType.getImageType("https://www.reddit.com/r/live/"), is(ImageType.REDDIT));
        assertThat(ContentType.getImageType("redd.it/eorhm"), is(ImageType.REDDIT));


        assertThat(ContentType.getImageType("https://www.reddit.com/live/wbjbjba8zrl6"), is(not(ImageType.REDDIT)));
        assertThat(ContentType.getImageType("https://www.reddit.com/r/Android/wiki/index"), is(not(ImageType.REDDIT)));
    }

    @Test
    public void testContentType_Link() {
        assertThat(ContentType.getImageType("https://stackoverflow.com/"), is(ImageType.LINK));
    }

    @Test
    public void testContentType_None() {
        assertThat(ContentType.getImageType(""), is(ImageType.NONE));
    }
}