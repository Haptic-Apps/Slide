package me.ccrama.redditslide;

import android.util.Log;

import net.dean.jraw.models.Submission;

import java.net.URI;
import java.net.URISyntaxException;

import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.StreamableUtil;

/**
 * Created by ccrama on 5/26/2015.
 */
public class ContentType {

    private static boolean isGif(URI uri) {
        final String host = uri.getHost();
        final String path = uri.getPath();

        return path.endsWith(".gif")
                || path.endsWith(".gifv")
                || path.endsWith(".webm")
                || path.endsWith(".mp4")
                || host.endsWith("gfycat.com");
    }

    private static boolean isImage(URI uri) {
        final String host = uri.getHost();
        final String path = uri.getPath();

        return host.equals("i.reddituploads.com")
                || path.endsWith(".png")
                || path.endsWith(".jpg")
                || path.endsWith(".jpeg");
    }

    private static boolean isAlbum(URI uri) {
        final String host = uri.getHost();
        final String path = uri.getPath();

        return host.endsWith("imgur.com")
                && (path.startsWith("/a/")
                || path.startsWith("/gallery/")
                || path.startsWith("/g/")
                || path.contains(","));
    }

    private static boolean isRedditLink(URI uri) {
        final String host = uri.getHost();
        final String path = uri.getPath();

        return (host.endsWith("reddit.com") || host.endsWith("redd.it")) && !path.startsWith("/live");
    }
    
    public static boolean isImgurLink(String url) {
        try {
            final URI uri = new URI(url);
            final String host = uri.getHost();

            return host.endsWith("imgur.com")
                    && !isAlbum(uri)
                    && !isGif(uri)
                    && !isImage(uri);
        } catch (URISyntaxException e) {
            return false;
        }
    }

    /**
     * Gets the ContentType from a URL
     *
     * @param url URL to get ContentType from
     * @return ContentType of the URL
     */
    public static ImageType getImageType(String url) {
        if (!url.startsWith("//") && ((url.startsWith("/") && url.length() < 4)
                || url.startsWith("#spoil")
                || url.startsWith("/spoil")
                || url.equals("#s")
                || url.equals("#b")
                || url.equals("#sp"))) {
            return ImageType.SPOILER;
        }

        if (url.startsWith("//")) url = "https:" + url;
        if (url.startsWith("/")) url = "reddit.com" + url;
        if (!url.contains("://")) url = "http://" + url;

        try {
            final URI uri = new URI(url);
            final String host = uri.getHost();
            final String scheme = uri.getScheme();

            if (!scheme.equals("http") && !scheme.equals("https")) {
                return ImageType.EXTERNAL;
            }
            if (isGif(uri)) {
                return ImageType.GIF;
            }
            if (isImage(uri)) {
                return ImageType.IMAGE;
            }
            if (isAlbum(uri)) {
                return ImageType.ALBUM;
            }
            if (host.endsWith("imgur.com")) {
                return ImageType.IMGUR;
            }
            if (isRedditLink(uri)) {
                return ImageType.REDDIT;
            }
            if (host.endsWith("vid.me")) {
                return ImageType.VID_ME;
            }
            if (host.endsWith("deviantart.com")) {
                return ImageType.DEVIANTART;
            }
            if (host.endsWith("streamable.com")) {
                return ImageType.STREAMABLE;
            }

            return ImageType.LINK;
        } catch (URISyntaxException e) {
            return ImageType.NONE;
        }
    }

    /**
     * Gets the ContentType from a Submission
     * @param submission Submission to get the ContentType from
     * @return ContentType of the Submission
     */
    public static ImageType getImageType(Submission submission) {
        final String url = submission.getUrl();
        final ImageType basicType = getImageType(url);

        if (submission.isSelfPost()) {
            return ImageType.SELF;
        }
        // TODO: Decide whether internal youtube links should be EMBEDDED or LINK
        if (basicType.equals(ImageType.LINK)
                && submission.getDataNode().has("media_embed")
                && submission.getDataNode().get("media_embed").has("content")) {
            return ImageType.EMBEDDED;
        }

        return basicType;
    }

    /**
     * Returns a string identifier for a submission e.g. Link, GIF, NSFW Image
     *
     * @param submission Submission to get the description for
     * @return the String identifier
     */
    public static int getContentDescription(Submission submission) {
        final ImageType contentType = getImageType(submission);

        if (submission.isNsfw()) {
            switch (contentType) {
                case GIF:
                    return R.string.type_nsfw_gif;
                case IMAGE:
                    return R.string.type_nsfw_img;
                case LINK:
                    return R.string.type_nsfw_link;
            }
        } else {
            switch (contentType) {
                case ALBUM:
                    return R.string.type_album;
                case DEVIANTART:
                    return R.string.type_deviantart;
                case EMBEDDED:
                    return R.string.type_emb;
                case EXTERNAL:
                    return R.string.type_link;
                case GIF:
                    return R.string.type_gif;
                case IMAGE:
                    return R.string.type_img;
                case IMGUR:
                    return R.string.type_imgur;
                case LINK:
                    return R.string.type_link;
                case NONE:
                    return R.string.type_title_only;
                case REDDIT:
                    return R.string.type_reddit;
                case SELF:
                    return R.string.type_selftext;
                case STREAMABLE:
                    return R.string.type_streamable;
                case VIDEO:
                case VID_ME:
                    return R.string.type_vid;
            }
        }
        return R.string.type_link;
    }

    public enum ImageType {
        ALBUM,
        DEVIANTART,
        EMBEDDED,
        EXTERNAL,
        GIF,
        IMAGE,
        IMGUR,
        LINK,
        NONE,
        REDDIT,
        SELF,
        SPOILER,
        STREAMABLE,
        VIDEO,
        VID_ME
    }
}