package me.ccrama.redditslide;

import android.util.Log;

import net.dean.jraw.models.Submission;

import me.ccrama.redditslide.util.LogUtil;

/**
 * Created by ccrama on 5/26/2015.
 */
public class ContentType {

    public static boolean isGif(String s) {
        return (s.endsWith(".gif") || s.contains("gfycat.com") || s.endsWith(".webm") || s.endsWith(".mp4") || s.endsWith(".gifv"));
    }

    public static boolean isImage(String s) {
        return (s.endsWith(".png") || s.endsWith(".jpg") || s.endsWith(".jpeg") );
    }

    public static boolean isAlbum(String s) {
        return (s.contains("imgur") && (s.contains("/a/")) || (s.contains("imgur") && (s.contains("gallery") || s.contains("/g/")) ));
    }

    public static boolean isRedditLink(String url) {
        return (url.contains("reddit.com") || url.contains("redd.it")) && !url.contains("/wiki") && !url.contains("reddit.com/live");
    }

    public static boolean isImgurLink(String url) {
        return (url.contains("imgur") && !isImage(url) && !isGif(url) && !isAlbum(url));
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
            Log.v(LogUtil.getTag(), f);
            return f;
        } else {
            if (s2.lastIndexOf("/") < s2.length()) {
                String f = s2.substring(s2.lastIndexOf("/") + 1, s2.length());
                Log.v(LogUtil.getTag(), f);
                return f;

            } else {
                return null;

            }
        }
    }

    /**
     * Gets the ImageType from the Submission
     * @param s Submission to get ImageType from
     * @return ImageType of this Submission
     */
    public static ImageType getImageType(Submission s) {
        try {
            Submission.ThumbnailType t = s.getThumbnailType();
            String url = s.getUrl();
            if (s.isSelfPost()) {
                return ImageType.SELF;
            } else if (isRedditLink(url)) {
                return ImageType.REDDIT;
            }
            if (url.startsWith("/") && !url.startsWith("//")) {
                url = "reddit.com" + url;
            } else {
                url = url.replace("//", "https://");
            }
            if (s.getUrl().contains("reddit.com") || s.getUrl().contains("redd.it")) {
                return ImageType.REDDIT;
            }
            if (!s.getUrl().contains("youtube.co") && !s.getUrl().contains("youtu.be") && s.getDataNode().has("media_embed") && s.getDataNode().get("media_embed").has("content") && !isAlbum(url) && !isImage(url) && !isGif(url)) {
                return ImageType.EMBEDDED;
            }
            if (s.getUrl().contains("youtube.co") || s.getUrl().contains("youtu.be")) {
                return ImageType.VIDEO;
            }
            if (s.getUrl().contains("streamable.com")) {
                return ImageType.STREAMABLE;
            }

            if (isAlbum(url)) {
                return ImageType.ALBUM;
            }
            if (!s.isNsfw()) {

                if (isImgurLink(url)) {
                    if (url.contains("?") && (url.contains(".png") || url.contains(".gif") || url.contains(".jpg"))) {
                        url = url.substring(0, url.lastIndexOf("?"));
                        return getImageType(url);
                    }
                    return ImageType.IMGUR;
                }


                switch (t) {
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
            } else {
                //if the submission is NSFW
                if (isImage(url) && !url.contains("gif")) {
                    return ImageType.NSFW_IMAGE;
                } else if (isGif(url)) {
                    if (url.contains("gfy"))
                        return ImageType.NSFW_GFY;
                    return ImageType.NSFW_GIF;
                } else {
                    return ImageType.NSFW_LINK;
                }
            }
        } catch (Exception e) {
            return ImageType.NONE;
        }
    }

    public static String getSubmissionFromUrl(String s2) {
        String s = s2;
        int lastIndex = s.lastIndexOf("comments") + 9;
        if (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        Log.v(LogUtil.getTag(), "URL" + s);
        String f = s.substring(lastIndex);
        Log.v(LogUtil.getTag(), "URLnew" + f);

        if (f.length() > 6) {
            f = f.substring(0, s.indexOf("/") + 1);
        }
        Log.v(LogUtil.getTag(), f);
        return f;

    }

    public static ImageType getImageType(String url) {
        if (url.equals("#s") || url.equals("/s") ||url.equals("/spoiler") || url.equals("/spoiler") || url.equals("/sp") || url.equals("/c") || url.equals("/f")) {
            return ImageType.SPOILER;
        }
        if (url.startsWith("/") && !url.startsWith("//")) {
            url = "reddit.com" + url;
        } else {
            url = url.replace("//", "https://");
        }
        if (isRedditLink(url)) {
            return ImageType.REDDIT;
        } else if (url.length() >= 20 && url.substring(0, 20).contains("youtube.com") || url.length() >= 15 &&url.substring(0, 15).contains("youtu.be")) {
            return ImageType.VIDEO;
        }
        if(url.contains("streamable.com")){
            return ImageType.STREAMABLE;
        }
        if(isImgurLink(url)){
            if(url.contains("?") && (url.contains(".png") ||url.contains(".gif") || url.contains(".jpg"))){
                url = url.substring(0, url.lastIndexOf("?"));
                return getImageType(url);
            }
            return ImageType.IMGUR;
        }
        if (isAlbum(url)) {
            return ImageType.ALBUM;
        }
        if (isImage(url) && !url.contains(".gif")) {
            return ImageType.IMAGE;
        } else if (isGif(url)) {
            if (url.contains("gfycat"))
                return ImageType.GFY;
            return ImageType.GIF;
        } else if (!url.isEmpty()){
            return ImageType.LINK;
        } else {
            return ImageType.NONE;
        }
    }

    public enum ImageType {
        NSFW_IMAGE,
        NSFW_GIF,
        STREAMABLE,
        NSFW_GFY,
        REDDIT,
        EMBEDDED,
        LINK,
        IMAGE_LINK,
        NSFW_LINK,
        SELF,
        GFY,
        ALBUM,
        IMAGE,
        IMGUR,
        GIF,
        NONE_GFY,
        NONE_GIF,
        NONE,
        NONE_IMAGE,
        VIDEO,
        NONE_URL,
        SPOILER
    }
}