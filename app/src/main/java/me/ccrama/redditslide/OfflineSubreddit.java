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
    public ArrayList<JsonNode> baseNodes;
    public String subreddit;
    public boolean base;


    public OfflineSubreddit overwriteSubmissionsString(List<JsonNode> data) {
        baseNodes = new ArrayList<>(data);
        return this;
    }
    public OfflineSubreddit overwriteSubmissions(List<Submission> data) {
        submissions = new ArrayList<>(data);
        return this;
    }

    public void writeToMemory() {
        if (subreddit != null) {
            String title = subreddit.toLowerCase() + "," + (base ? 0 : time);
            String fullNames = "";
            if(baseNodes ==null) {
                for (Submission sub : submissions) {
                    fullNames += sub.getFullName() + ",";
                    Reddit.cachedData.edit().putString(sub.getFullName(), sub.getDataNode().toString()).apply();
                }
            } else {
                for (JsonNode sub : baseNodes) {
                    if(sub.has("name")) {
                        fullNames += sub.get("name").asText() + ",";
                        Reddit.cachedData.edit().putString(sub.get("name").asText(), sub.toString()).apply();
                    }
                }
            }
            Reddit.cachedData.edit().putString(title, fullNames.substring(0, fullNames.length() - 1)).apply();
        }
    }

    public static OfflineSubreddit getSubreddit(String subreddit) {
        return getSubreddit(subreddit, 0);
    }

    public static OfflineSubreddit getSubreddit(String subreddit, int time) {
        subreddit = subreddit.toLowerCase();

        OfflineSubreddit o = new OfflineSubreddit();
        o.subreddit = subreddit.toLowerCase();

        if (time == 0) {
            o.base = true;
        }

        o.time = time;

        String[] split = Reddit.cachedData.getString(subreddit.toLowerCase() + time, "").split(",");
        if (split.length > 1) {
            o.time = time;
            o.submissions = new ArrayList<>();
            for (String s : split) {
                String gotten = Reddit.cachedData.getString(s, "");
                if (!gotten.isEmpty()) {
                    try {
                        if (gotten.startsWith("[")) {
                            o.submissions.add(SubmissionSerializer.withComments(new ObjectMapper().readTree(gotten), CommentSort.CONFIDENCE));
                        } else {
                            o.submissions.add(new Submission(new ObjectMapper().readTree(gotten)));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        } else {
            o.submissions = new ArrayList<>();
        }
        return o;

    }

    public static OfflineSubreddit newSubreddit(String subreddit) {
        subreddit = subreddit.toLowerCase();

        OfflineSubreddit o = new OfflineSubreddit();
        o.subreddit = subreddit.toLowerCase();
        o.base = false;

        o.time = System.currentTimeMillis();

        o.submissions = new ArrayList<>();

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
            if (toRemove != null) {
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
            if (save) {
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
}
