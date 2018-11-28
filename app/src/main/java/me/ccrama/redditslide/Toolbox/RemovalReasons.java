package me.ccrama.redditslide.Toolbox;

import java.util.List;

public class RemovalReasons {
    private String pmSubject;
    private String header;
    private String footer;
    private String logSub;
    private String logTitle;
    private String logReason;
    private String banTitle; // Is this even used by Toolbox?

    private List<RemovalReason> reasons;

    public RemovalReasons() {

    }

    public String getPmSubject() {
        return pmSubject;
    }

    public String getHeader() {
        return header;
    }

    public String getFooter() {
        return footer;
    }

    public String getLogSub() {
        return logSub;
    }

    public String getLogTitle() {
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
    public class RemovalReason {
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
            return text;
        }

        public String getFlairText() {
            return flairText;
        }

        public String getFlairCSS() {
            return flairCSS;
        }
    }
}
