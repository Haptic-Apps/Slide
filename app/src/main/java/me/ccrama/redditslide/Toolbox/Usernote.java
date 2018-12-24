package me.ccrama.redditslide.Toolbox;

import com.google.gson.annotations.SerializedName;

/**
 * Defines a Usernote so GSON can deserialize it
 */
public class Usernote {
    @SerializedName("n") private String noteText;
    @SerializedName("l") private String link;
    @SerializedName("t") private long time;
    @SerializedName("m") private int mod;
    @SerializedName("w") private int warning;

    public Usernote() { // for GSON
    }

    public Usernote(String noteText, String link, long time, int mod, int warning) {
        this.noteText = noteText;
        this.link = link;
        this.time = time;
        this.mod = mod;
        this.warning = warning;
    }

    public String getNoteText() {
        return noteText;
    }

    public long getTime() {
        return time * 1000; // * 1000 so it makes sense as a long
    }

    public int getMod() {
        return mod;
    }

    public String getLink() {
        return link;
    }

    public int getWarning() {
        return warning;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Usernote) {
            return ((Usernote) obj).warning == warning
                    && ((Usernote) obj).mod == mod
                    && ((Usernote) obj).time == time
                    && ((Usernote) obj).noteText.equals(noteText)
                    && ((Usernote) obj).link.equals(link);
        }
        return false;
    }

    /**
     * Identify what type of link a usernote points to, if any
     * @return Type of link
     */
    public UsernoteLinkType getLinkType() {
        if (link.isEmpty()) {
            return null;
        } if (link.startsWith("m,")) {
            return UsernoteLinkType.MODMAIL;
        } else if (link.startsWith("l,") && link.split(",").length == 3) {
            return UsernoteLinkType.COMMENT;
        } else if (link.startsWith("l,")) {
            return UsernoteLinkType.POST;
        } else {
            return null;
        }
    }

    /**
     * Gets the Usernote's link as a URL
     * @return String of usernote's URL.
     */
    public String getLinkAsURL(String subreddit) {
        if (link.isEmpty()) {
            return null;
        }

        if (getLinkType() == UsernoteLinkType.MODMAIL) {
            return "https://www.reddit.com/message/messages/" + link.substring(3);
        } else {
            String[] split = link.split(",");
            return "https://www.reddit.com/r/" + subreddit + "/comments/" + split[1]
                    + (getLinkType() == UsernoteLinkType.COMMENT ? "/_/" + split[2] : "");
        }
    }

    public enum UsernoteLinkType {
        POST, COMMENT, MODMAIL
    }
}
