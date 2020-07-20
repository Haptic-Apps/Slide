package me.ccrama.redditslide.Toolbox;

import com.google.gson.annotations.SerializedName;

import org.apache.commons.text.StringEscapeUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

public class RemovalReasons {
    @SerializedName("pmsubject") private String pmSubject;
    private String header;
    private String footer;
    @SerializedName("logsub") private String logSub;
    @SerializedName("logtitle") private String logTitle;
    @SerializedName("logreason") private String logReason;
    @SerializedName("bantitle") private String banTitle; // Is this even used by Toolbox? For mod button bans maybe (not a removal reason thing...)?

    private List<RemovalReason> reasons;

    public RemovalReasons() {

    }

    public String getPmSubject() {
        if (pmSubject.isEmpty()) {
            return "Your {kind} was removed from /r/{subreddit}";
        }
        return pmSubject;
    }

    public String getHeader() {
        try {
            return URLDecoder.decode(header, "UTF-8"); // header is url encoded
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public String getFooter() {
        try {
            return URLDecoder.decode(footer, "UTF-8"); // footer is url encoded
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public String getLogSub() {
        return logSub;
    }

    public String getLogTitle() {
        if (logTitle.isEmpty()) {
            return "Removed: {kind} by /u/{author} to /r/{subreddit}";
        }
        return logTitle;
    }

    public String getLogReason() {
        return logReason;
    }

    public List<RemovalReason> getReasons() {
        return reasons;
    }

    /**
     * Class defining an individual removal reason
     */
    public static class RemovalReason {
        private String title;
        private String text;
        private String flairText;
        private String flairCSS;

        public RemovalReason() {

        }

        public String getTitle() {
            return title;
        }

        public String getText() {
            try {
                // text is url encoded but uses non-standard %uXXXX char sequences
                return URLDecoder.decode(StringEscapeUtils.unescapeJava(text.replace("%u", "\\u")), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                return null;
            }
        }

        public String getFlairText() {
            return flairText;
        }

        public String getFlairCSS() {
            return flairCSS;
        }
    }
}
