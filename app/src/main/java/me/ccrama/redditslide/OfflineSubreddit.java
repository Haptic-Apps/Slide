package me.ccrama.redditslide;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.dean.jraw.models.CommentSort;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.meta.SubmissionSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by carlo_000 on 11/19/2015.
 */
public class OfflineSubreddit {
    public long time;
    public ArrayList<Submission> submissions;

    public String subreddit;

    public OfflineSubreddit overwriteSubmissions(List<Submission> data) {
        submissions = new ArrayList<>(data);
        return this;
    }

    public void addSubmissions(List<Submission> data) {
        submissions.addAll(data);
    }

    public void writeToMemory() {
        StringBuilder s = new StringBuilder();
        s.append(System.currentTimeMillis()).append("<SEPARATOR>");
        for (Submission sub : submissions) {
            s.append(sub.getDataNode().toString());
            s.append("<SEPARATOR>");
        }
        String finals = s.toString();
        finals = finals.substring(0, finals.length() - 11);
        Reddit.cachedData.edit().putString(subreddit.toLowerCase(), finals).commit();
    }

    public OfflineSubreddit setComment(String fullname, Submission submission){
        for(Submission s : submissions){
            if(s.getFullName().equals(fullname)){
                int index = submissions.indexOf(s);
                submissions.remove(s);
                submissions.add(index, submission);
                break;
            }
        }
        return this;
    }

    public OfflineSubreddit(String subreddit) {
        this.subreddit = subreddit;
        String[] split = Reddit.cachedData.getString(subreddit.toLowerCase(), "").split("<SEPARATOR>");
        if (split.length > 1) {
            time = Long.valueOf(split[0]);
            submissions = new ArrayList<>();
            for (int i = 1; i < split.length; i++) {
                try {
                    if (split[i].startsWith("[")) {
                        submissions.add(SubmissionSerializer.withComments(new ObjectMapper().readTree(split[i]), CommentSort.CONFIDENCE));
                    } else {
                        submissions.add(new Submission(new ObjectMapper().readTree(split[i])));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            submissions = new ArrayList<>();
        }
    }

    public OfflineSubreddit overwriteSubmissions(ArrayList<JsonNode> newSubmissions) {
        StringBuilder s = new StringBuilder();
        s.append(System.currentTimeMillis()).append("<SEPARATOR>");
        for (JsonNode sub : newSubmissions) {
            s.append(sub.toString());
            s.append("<SEPARATOR>");
        }
        String finals = s.toString();
        finals = finals.substring(0, finals.length() - 11);
        Reddit.appRestart.edit().putString(subreddit.toLowerCase() , finals).commit();
      return this;
    }
}
