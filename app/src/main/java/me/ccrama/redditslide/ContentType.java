package me.ccrama.redditslide;

import android.util.Log;

import net.dean.jraw.models.Submission;

/**
 * Created by ccrama on 5/26/2015.
 */
public class ContentType {

    private static boolean isGif(String s) {
        return (s.contains("gif") || s.contains("gfy") || s.contains("webm") || s.contains("mp4"));
    }

    private static boolean isImage(String s) {
        return (s.contains("png") || s.contains("jpg") || s.contains("jpeg") || s.contains("imgur"));
    }

    private static boolean isAlbum(String s) {
        return s.contains("imgur") && (s.contains("/a/") || s.contains("gallery") || s.contains("/g/"));
    }

    public static String getFixedUrl(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        if (s.contains("imgur") && !s.contains(".jpg") && !s.contains("gif") && !s.contains("png") && !isAlbum(s)) {
            String hash = s.substring(s.lastIndexOf("/") + 1, s.length());
            s = "http://i.imgur.com/" + hash + ".jpg";
        }
        return s;
    }

    public static String getFixedUrlThumb(String s2) {
        String s = s2;
        if (s == null || s.isEmpty()) {
            return "";
        }
        if (s.contains("imgur") && !s.contains(".jpg")) {
            String hash = s.substring(s.lastIndexOf("/"), s.length());
            s = "http://i.imgur.com/" + hash + "m.jpg";
        }
        return s;
    }

    private static String getLastPartofUrl(String s2) {
        if (s2.endsWith("/")) {
            return getLastPartofUrl(s2.substring(0, s2.length() - 1));
        }
        if (s2.contains("?")) {
            String f = s2.substring(s2.lastIndexOf("/") + 1, s2.lastIndexOf("?"));
            Log.v("Slide", f);
            return f;
        } else {
            if (s2.lastIndexOf("/") < s2.length()) {
                String f = s2.substring(s2.lastIndexOf("/") + 1, s2.length());
                Log.v("Slide", f);
                return f;

            } else {
                return null;

            }
        }
    }


    public static ImageType getImageType(Submission s) {
        Submission.ThumbnailType t = s.getThumbnailType();
        String url = s.getUrl();
        if (url.startsWith("/")) {
            url = "reddit.com" + url;
        }
        if (s.isSelfPost()) {
            return ImageType.SELF;
        } else if ((url.contains("reddit.com") || url.contains("redd.it")) && !url.contains("/wiki") && !url.contains("/live")) {
            return ImageType.REDDIT;
        }

        if (s.getDataNode().has("media_embed") && s.getDataNode().get("media_embed").has("content") && !isAlbum(url) && !isImage(url) && !isGif(url)) {
            return ImageType.EMBEDDED;
        }
        if (s.getUrl().contains("reddit.com") || s.getUrl().contains("redd.it")) {
            return ImageType.REDDIT;
        }
        switch (t) {
            case NSFW:
                if (isImage(url) && !url.contains("gif")) {
                    return ImageType.NSFW_IMAGE;
                } else if (isGif(url)) {
                    if (url.contains("gfy"))
                        return ImageType.NSFW_GFY;
                    return ImageType.NSFW_GIF;

                } else {
                    return ImageType.NSFW_LINK;
                }
            case DEFAULT:
                if (isAlbum(url)) {
                    return ImageType.ALBUM;
                }
                if (isImage(url) && !url.contains("gif")) {
                    return ImageType.IMAGE;
                } else if (isGif(url)) {
                    if (url.contains("gfy"))
                        return ImageType.GFY;
                    return ImageType.GIF;

                } else {
                    return ImageType.IMAGE_LINK;
                }
            case SELF:
                return ImageType.SELF;
            case NONE:
                if (isAlbum(url)) {
                    return ImageType.ALBUM;
                }
                if (isImage(url) && !url.contains("gif")) {
                    return ImageType.NONE_IMAGE;
                } else if (isGif(url)) {
                    if (url.contains("gfy"))
                        return ImageType.NONE_GFY;
                    return ImageType.NONE_GIF;
                } else if ((url.contains("reddit.com") || url.contains("redd.it")) && !url.contains("/wiki") && !url.contains("/live")) {
                    return ImageType.REDDIT;
                } else if (!url.isEmpty()) {
                    return ImageType.LINK;

                } else {
                    return ImageType.NONE;
                }
            case URL:
                if (isAlbum(url)) {
                    return ImageType.ALBUM;
                }
                if (isImage(url) && !url.contains("gif")) {
                    return ImageType.IMAGE;
                } else if (isGif(url)) {
                    if (url.contains("gfy"))
                        return ImageType.GFY;
                    return ImageType.GIF;
                } else if (!url.isEmpty()) {
                    return ImageType.LINK;

                } else {
                    return ImageType.NONE;
                }
            default:
                return ImageType.NONE;
        }
    }

    public static String getSubmissionFromUrl(String s2) {
        String s = s2;
        int lastIndex = s.lastIndexOf("comments") + 9;
        if (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        Log.v("Slide", "URL" + s);
        String f = s.substring(lastIndex);
        Log.v("Slide", "URLnew" + f);

        if (f.length() > 6) {
            f = f.substring(0, s.indexOf("/") + 1);
        }
        Log.v("Slide", f);
        return f;

    }

    public static ImageType getImageType(String url) {

        if (url.startsWith("/")) {
            url = "reddit.com" + url;
        }
        if ((url.contains("reddit.com") || url.contains("redd.it")) && !url.contains("/wiki") && !url.contains("/live")) {
            return ImageType.REDDIT;
        } else if (url.contains("youtu")) {
            return ImageType.VIDEO;

        }

        if (isAlbum(url)) {
            return ImageType.ALBUM;
        }
        if (isImage(url) && !url.contains("gif")) {
            return ImageType.IMAGE;
        } else if (isGif(url)) {
            if (url.contains("gfy"))
                return ImageType.GFY;
            return ImageType.GIF;
        } else {
            return ImageType.LINK;

        }

    }

    public enum ImageType {
        NSFW_IMAGE, NSFW_GIF, NSFW_GFY, REDDIT, EMBEDDED, LINK, IMAGE_LINK, NSFW_LINK, SELF, GFY, ALBUM, IMAGE, GIF, NONE_GFY, NONE_GIF, NONE, NONE_IMAGE, VIDEO, NONE_URL
    }
}
