package me.ccrama.redditslide.Activities;

import android.content.SharedPreferences;

import net.dean.jraw.models.Submission;

import java.util.List;

import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.Reddit;

/**
 * Created by carlo_000 on 1/30/2016.
 */
public class SubredditCache {
    public SharedPreferences subredditCache;

    public void writeSubreddit(List<Submission> objects, String subreddit) {
        StringBuilder s = new StringBuilder();
        s.append(System.currentTimeMillis()).append("<SEPARATOR>");
        for (Submission sub : objects) {
            s.append(sub.getDataNode().toString());
            s.append("<SEPARATOR>");
        }
        String finals = s.toString();
        finals = finals.substring(0, finals.length() - 11);
        subredditCache.edit().putString(subreddit.toLowerCase(), finals).apply();

    }

    public List<Submission> getSubmissions(String s){
        if (Reddit.appRestart.contains(s.toLowerCase())) {
            return new OfflineSubreddit(Reddit.appRestart.getString(s.toLowerCase(), "")).submissions;
        } else {
            return null;
        }
    }

    public void addSubmissions(String s, List<Submission> objects){

    }
}
