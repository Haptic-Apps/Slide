package me.ccrama.redditslide;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;

import net.dean.jraw.models.Submission;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

/**
 * Created by ccrama on 5/26/2015.
 */
public class ContentType {

    public static boolean isGif(URI uri) {
        try {
            final String host = uri.getHost().toLowerCase();
            final String path = uri.getPath().toLowerCase();

            return host.endsWith("gfycat.com")
                    || path.endsWith(".gif")
                    || path.endsWith(".gifv")
                    || path.endsWith(".webm")
                    || path.endsWith(".mp4");

        } catch (NullPointerException e) {
            return false;
        }
    }

    public static boolean isImage(URI uri) {
        try {
            final String host = uri.getHost().toLowerCase();
            final String path = uri.getPath().toLowerCase();

            return host.equals("i.reddituploads.com")
                    || path.endsWith(".png")
                    || path.endsWith(".jpg")
                    || path.endsWith(".jpeg");

        } catch (NullPointerException e) {
            return false;
        }
    }

    public static boolean isAlbum(URI uri) {
        try {
            final String host = uri.getHost().toLowerCase();
            final String path = uri.getPath().toLowerCase();

            return host.endsWith("imgur.com")
                    && (path.startsWith("/a/")
                    || path.startsWith("/gallery/")
                    || path.startsWith("/g/")
                    || path.contains(","));

        } catch (NullPointerException e) {
            return false;
        }
    }

    public static boolean isRedditLink(URI uri) {
        try {
            final String host = uri.getHost().toLowerCase();

            return host.endsWith("reddit.com")
                    || host.endsWith("redd.it");

        } catch (NullPointerException e) {
            return false;
        }
    }

    public static boolean isImgurLink(String url) {
        try {
            final URI uri = new URI(url);
            final String host = uri.getHost().toLowerCase();

            return host.endsWith("imgur.com")
                    && !isAlbum(uri)
                    && !isGif(uri)
                    && !isImage(uri);

        } catch (URISyntaxException | NullPointerException e) {
            return false;
        }
    }

    /**
     * Attempt to determine the content type of a link from the URL
     *
     * @param url URL to get ContentType from
     * @return ContentType of the URL
     */
    public static Type getContentType(String url) {
        if (!url.startsWith("//") && ((url.startsWith("/") && url.length() < 4)
                || url.startsWith("#spoil")
                || url.startsWith("/spoil")
                || url.startsWith("#s-")
                || url.equals("#s")
                || url.equals("#ln")
                || url.equals("#b")
                || url.equals("#sp"))) {
            return Type.SPOILER;
        }

        if (url.startsWith("//")) url = "https:" + url;
        if (url.startsWith("/")) url = "reddit.com" + url;
        if (!url.contains("://")) url = "http://" + url;

        try {
            final URI uri = new URI(url);
            final String host = uri.getHost().toLowerCase();
            final String scheme = uri.getScheme().toLowerCase();

            if (!scheme.equals("http") && !scheme.equals("https")) {
                return Type.EXTERNAL;
            }
            if (Reddit.videoPlugin && ((host.contains("youtu.be") || host.contains("youtube.co")))) {
                return Type.VIDEO;
            } else if (PostMatch.openExternal(url)) {
                return Type.EXTERNAL;
            }
            if (isGif(uri)) {
                return Type.GIF;
            }
            if (isImage(uri)) {
                return Type.IMAGE;
            }
            if (isAlbum(uri)) {
                return Type.ALBUM;
            }
            if (host.endsWith("imgur.com")) {
                return Type.IMGUR;
            }
            if (isRedditLink(uri)) {
                return Type.REDDIT;
            }
            if (host.endsWith("vid.me")) {
                return Type.VID_ME;
            }
            if (host.endsWith("deviantart.com")) {
                return Type.DEVIANTART;
            }
            if (host.endsWith("streamable.com")) {
                return Type.STREAMABLE;
            }

            return Type.LINK;

        } catch (URISyntaxException | NullPointerException e) {
            return Type.NONE;
        }
    }

    /**
     * Attempts to determine the content of a submission, mostly based on the URL
     *
     * @param submission Submission to get the content type from
     * @return Content type of the Submission
     * @see #getContentType(String)
     */
    public static Type getContentType(Submission submission) {
        final String url = submission.getUrl();
        final Type basicType = getContentType(url);

        if (submission.isSelfPost()) {
            return Type.SELF;
        }
        // TODO: Decide whether internal youtube links should be EMBEDDED or LINK
        if (basicType.equals(Type.LINK)
                && submission.getDataNode().has("media_embed")
                && submission.getDataNode().get("media_embed").has("content")) {
            return Type.EMBEDDED;
        }

        return basicType;
    }

    public static boolean fullImage(Type t) {
        switch (t) {

            case ALBUM:
            case DEVIANTART:
            case GIF:
            case IMAGE:
            case IMGUR:
            case STREAMABLE:
            case VIDEO:
            case SELF:
            case VID_ME:
                return true;

            case EMBEDDED:
            case EXTERNAL:
            case LINK:
            case NONE:
            case REDDIT:
            case SPOILER:
            default:
                return false;

        }
    }

    /**
     * Returns a string identifier for a submission e.g. Link, GIF, NSFW Image
     *
     * @param submission Submission to get the description for
     * @return the String identifier
     */
    private static int getContentID(Submission submission) {
        final Type contentType = getContentType(submission);

        if (submission.isNsfw()) {
            switch (contentType) {
                case ALBUM:
                    return R.string.type_nsfw_album;
                case EMBEDDED:
                    return R.string.type_nsfw_emb;
                case EXTERNAL:
                    return R.string.type_nsfw_link;
                case GIF:
                    return R.string.type_nsfw_gif;
                case IMAGE:
                    return R.string.type_nsfw_img;
                case IMGUR:
                    return R.string.type_nsfw_imgur;
                case LINK:
                    return R.string.type_nsfw_link;
                case VIDEO:
                case VID_ME:
                    return R.string.type_nsfw_video;
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
                    return R.string.type_external;
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
                    return R.string.type_youtube;
                case VID_ME:
                    return R.string.type_vidme;
            }
        }
        return R.string.type_link;
    }

    static HashMap<String, String> contentDescriptions = new HashMap<>();

    /**
     * Returns a description of the submission, for example "Link", "NSFW link", if the link is set
     * to open externally it returns the package name of the app that opens it, or "External"
     *
     * @param submission The submission to describe
     * @param context    Current context
     * @return The content description
     */
    public static String getContentDescription(Submission submission, Context context) {
        final int generic = getContentID(submission);
        final Resources res = context.getResources();
        final String domain = submission.getDomain();

        if (generic != R.string.type_external) {
            return res.getString(generic);
        }

        if (contentDescriptions.containsKey(domain)) {
            return contentDescriptions.get(domain);
        }

        try {
            final PackageManager pm = context.getPackageManager();
            final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(submission.getUrl()));
            final String packageName = pm.resolveActivity(intent, 0).activityInfo.packageName;
            String description;

            if (!packageName.equals("android")) {
                description = pm.getApplicationLabel(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)).toString();
            } else {
                description = res.getString(generic);
            }

            // Looking up a package name takes a long time (3~10ms), memoize it
            contentDescriptions.put(domain, description);
            return description;
        } catch (PackageManager.NameNotFoundException | NullPointerException e) {
            contentDescriptions.put(domain, res.getString(generic));
            return res.getString(generic);
        }
    }

    public enum Type {
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