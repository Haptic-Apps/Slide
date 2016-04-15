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
    public ArrayList<String> dataNodes;

    public String subreddit;

    public OfflineSubreddit overwriteSubmissions(List<Submission> data) {
        submissions = new ArrayList<>(data);
        return this;
    }

    public void addSubmissions(List<Submission> data) {
        submissions.addAll(data);
    }

    public void writeToMemory() {
        if (subreddit != null) {
            System.gc();
            if (dataNodes == null) {
                String s = System.currentTimeMillis() + "<SEPARATOR>";
                for (Submission sub : submissions) {
                    s = s + (sub.getDataNode().toString()) + "<SEPARATOR>";
                }
                s = s.substring(0, s.length() - 11);
                Reddit.cachedData.edit().putString(subreddit.toLowerCase(), s).apply();

            } else {
                String s = System.currentTimeMillis() + "<SEPARATOR>";
                for (String sub : dataNodes) {
                    s = s + (sub + "<SEPARATOR>");
                }
                s = s.substring(0, s.length() - 11);
                Reddit.cachedData.edit().putString(subreddit.toLowerCase(), s).apply();


                dataNodes = null;

            }
        }
    }

    public OfflineSubreddit setCommentAndWrite(String fullname, JsonNode submission, Submission finalSub) {
        if (dataNodes == null) {
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
        subreddit = subreddit.toLowerCase();


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

        } else {
            o.submissions = new ArrayList<>();
        }
        return o;

    }

    public void clearPost(Submission s) {
        if (submissions != null) {
            Submission toRemove = null;
            for (Submission s2 : submissions) {
                if (s.getFullName().equals(s2.getFullName())) {
                    toRemove = s2;
                }
            }
            if(toRemove != null){
                submissions.remove(toRemove);
            }

        }
    }

    int savedIndex;
    Submission savedSubmission;

    public void hide(int index) {
        hide(index, true);
    }
    public void hide(int index, boolean save) {
        if (submissions != null) {
            savedSubmission = submissions.get(index);
            submissions.remove(index);
            if(save) {
                savedIndex = index;
                writeToMemory();
            }
        }
    }
    public void unhideLast() {
        if (submissions != null && savedSubmission != null) {
            submissions.add(savedIndex, savedSubmission);
            writeToMemory();
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
        Reddit.cachedData.edit().putString(subreddit.toLowerCase(), finals).apply();

        return this;
    }

    public void overwriteSubmissions(String newSubmissions) {
        String finals = newSubmissions.substring(0, newSubmissions.length() - 11);
        Reddit.cachedData.edit().putString(subreddit.toLowerCase(), finals).apply();
    }
}
