package me.ccrama.redditslide;

import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.PrivateMessage;
import net.dean.jraw.models.Submission;

import java.util.ArrayList;

public class DataShare {
    public static ArrayList<Submission> sharedSubreddit;
    public static Submission sharedSubmission;
    public static Submission notifs;
    public static PrivateMessage sharedMessage;
    public static ArrayList<CommentNode> sharedComments;
    public static String subAuthor;
}
