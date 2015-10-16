package me.ccrama.redditslide;

/**
 * Created by ccrama on 9/17/2015.
 */
public class ContentGrabber {


    /*Inbox Data*/
    public enum InboxValue{
        INBOX("Inbox"), UNREAD("Unread"), MESSAGES("Messages"), SENT("Sent"), MENTIONS("Mentions");
        String displayName;
         InboxValue(String s){
            this.displayName = s;
        }
        public String getDisplayName(){
            return displayName;
        }
        public String getWhereName(){
            return displayName.toLowerCase();
        }

    }

}
