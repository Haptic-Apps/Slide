package me.ccrama.redditslide;

import net.dean.jraw.models.Submission;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by carlo_000 on 11/18/2015.
 */
public final class Cache {
    private Cache() {}

    public static void writeSubreddit( ArrayList<Submission> objects, String subreddit) throws IOException {
        StringBuilder s = new StringBuilder();
        for(Submission sub : objects){
            s.append(sub.getDataNode().toString());
            s.append("<SEPARATOR>");
        }
        String finals = s.toString();
        finals = finals.substring(0, finals.length() - 11);
       Reddit.appRestart.edit().putString(subreddit + ":" + System.currentTimeMillis(), finals).apply();

    }


}