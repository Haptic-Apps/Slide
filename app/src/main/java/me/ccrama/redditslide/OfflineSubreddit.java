package me.ccrama.redditslide;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.dean.jraw.models.CommentSort;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.meta.SubmissionSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by carlo_000 on 11/19/2015.
 */
public class OfflineSubreddit {

    public static HashMap<String, OfflineSubreddit> subredditBackups = new HashMap<>();
    public long time;
    public ArrayList<Submission> submissions;
    public ArrayList<String> dataNodes;

    public String subreddit;

    public OfflineSubreddit overwriteSubmissions(List<Submission> data) {
        submissions = new ArrayList<>(data);
        subredditBackups.put(subreddit.toLowerCase(), this);

        return this;
    }

    public void addSubmissions(List<Submission> data) {
        submissions.addAll(data);
        subredditBackups.put(subreddit.toLowerCase(), this);

    }

    public void writeToMemory() {
        if(subreddit!= null) {
            if (dataNodes == null) {
                StringBuilder s = new StringBuilder();
                s.append(System.currentTimeMillis()).append("<SEPARATOR>");
                for (Submission sub : submissions) {
                    s.append(sub.getDataNode().toString());
                    s.append("<SEPARATOR>");
                }
                String finals = s.toString();
                finals = finals.substring(0, finals.length() - 11);
                Reddit.cachedData.edit().putString(subreddit.toLowerCase(), finals).commit();

            } else {
                StringBuilder s = new StringBuilder();
                s.append(System.currentTimeMillis()).append("<SEPARATOR>");
                for (String sub : dataNodes) {
                    s.append(sub);
                    s.append("<SEPARATOR>");
                }
                String finals = s.toString();
                finals = finals.substring(0, finals.length() - 11);
                Reddit.cachedData.edit().putString(subreddit.toLowerCase(), finals).commit();


                dataNodes = null;

            }
        }
        subredditBackups.put(subreddit.toLowerCase(), this);

    }

    public OfflineSubreddit setCommentAndWrite(String fullname, JsonNode submission, Submission finalSub){
        if(dataNodes == null) {
            dataNodes = new ArrayList<>();
            int index = 0;
            for (Submission s : submissions) {
                if (s.getFullName().equals(fullname)) {
                    dataNodes.add(submission.toString());
                 index = submissions.indexOf(s);

                } else {
                    dataNodes.add(s.getDataNode().toString());
                }
            }
            submissions.remove(index);
            submissions.add(index, finalSub);
        } else {
            int index = 0;
            for (Submission s : submissions) {
                if (s.getFullName().equals(fullname)) {
                    index = submissions.indexOf(s);

                    dataNodes.remove(index);
                    dataNodes.add(index, submission.toString());

                }
            }

            submissions.remove(index);
            submissions.add(index, finalSub);
        }
        return this;
    }

    public static OfflineSubreddit getSubreddit(String subreddit) {

        if(subredditBackups.containsKey(subreddit)){
            return subredditBackups.get(subreddit);
        } else {
            OfflineSubreddit o = new OfflineSubreddit();
            o.subreddit = subreddit.toLowerCase();

            String[] split = Reddit.cachedData.getString(subreddit.toLowerCase(), "").split("<SEPARATOR>");
            if (split.length > 1) {
                o.time = Long.valueOf(split[0]);
                o.submissions = new ArrayList<>();
                for (int i = 1; i < split.length; i++) {
                    try {
                        if (split[i].startsWith("[")) {
                            o.submissions.add(SubmissionSerializer.withComments(new ObjectMapper().readTree(split[i]), CommentSort.CONFIDENCE));
                        } else {
                            o.submissions.add(new Submission(new ObjectMapper().readTree(split[i])));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                subredditBackups.put(subreddit.toLowerCase(), o);

            } else {
                o.submissions = new ArrayList<>();
            }
            return o;
        }
    }
    public void clearSeenPosts(boolean forever) {
        if (submissions != null) {

            for (int i = submissions.size(); i > -1; i--) {
                try {
                    if (HasSeen.getSeen(submissions.get(i).getFullName())) {
                        if (forever) {
                            Hidden.setHidden(submissions.get(i));
                        }
                        submissions.remove(i);
                    }
                } catch (IndexOutOfBoundsException e) {
                    //Let the loop reset itself
                }
            }

        }
        writeToMemory();
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
        Reddit.cachedData.edit().putString(subreddit.toLowerCase(), finals).apply();
        subredditBackups.put(subreddit.toLowerCase(), this);

        return this;
    }
}
