package me.ccrama.redditslide;

import java.util.Locale;

/**
 * Created by ccrama on 9/17/2015.
 */
public class ContentGrabber {


    /*Inbox Data*/
    public enum InboxValue {
        INBOX(R.string.mail_tab_inbox), UNREAD(R.string.mail_tab_unread),
        MESSAGES(R.string.mail_tab_messages), SENT(R.string.mail_tab_sent),
        MENTIONS(R.string.mail_tab_mentions);

        private final int displayName;

        InboxValue(int resource) {
            this.displayName = resource;
        }

        public int getDisplayName() {
            return displayName;
        }

        public String getWhereName() {
            return this.name().toLowerCase(Locale.ENGLISH);
        }

    }

    public enum ModValue {
        NODMAIL("Mod Mail"), MODQUEUE("Modqueue"), REPORTS("Reports"), UNMODERATED("Unmoderated"),
        SPAM("Spam"), EDITED("Edited");
        final String displayName;

        ModValue(String s) {
            this.displayName = s;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getWhereName() {
            return displayName.toLowerCase(Locale.ENGLISH);
        }

    }
}
