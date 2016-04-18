package me.ccrama.redditslide;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import net.dean.jraw.models.CommentSort;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.meta.SubmissionSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.util.LogUtil;

/**
 * Created by carlo_000 on 11/19/2015.
 */
public class OfflineSubreddit {

    public static Long currentid = 0l;
    public long time;
    public ArrayList<Submission> submissions;
    public String subreddit;
    public boolean base;

    public static void writeSubmission(JsonNode node, Submission s) {
        Reddit.cachedData.edit().putString(s.getFullName(), node.toString()).apply();
    }


    public OfflineSubreddit overwriteSubmissions(List<Submission> data) {
        submissions = new ArrayList<>(data);
        return this;
    }

    public void writeToMemory() {
        if (subreddit != null) {
            String title = subreddit.toLowerCase() + "," + (base ? 0 : time);
            String fullNames = "";
            for (Submission sub : submissions) {
                fullNames += sub.getFullName() + ",";
                if (!Reddit.cachedData.contains(sub.getFullName()))
                    Reddit.cachedData.edit().putString(sub.getFullName(), sub.getDataNode().toString()).apply();
            }
            if (fullNames.length() > 0)
                Reddit.cachedData.edit().putString(title, fullNames.substring(0, fullNames.length() - 1)).apply();
        }
    }

    public void writeToMemory(ArrayList<String> names) {
        if (subreddit != null && !names.isEmpty()) {
            String title = subreddit.toLowerCase() + "," + (base ? 0 : time);
            String fullNames = "";
            for (String sub : names) {
                fullNames += sub + ",";
            }
            Reddit.cachedData.edit().putString(title, fullNames.substring(0, fullNames.length() - 1)).apply();
        }
    }

    public static OfflineSubreddit getSubreddit(String subreddit, boolean offline) {
        return getSubreddit(subreddit, 0l, offline);
    }

    public static OfflineSubreddit getSubNoLoad(String s) {
        s = s.toLowerCase();

        OfflineSubreddit o = new OfflineSubreddit();
        o.subreddit = s.toLowerCase();
        o.base = true;
        o.time = 0;
        o.submissions = new ArrayList<>();
        return o;
    }

    public static OfflineSubreddit getSubreddit(String subreddit, Long time, boolean offline) {
        subreddit = subreddit.toLowerCase();

        OfflineSubreddit o = new OfflineSubreddit();
        o.subreddit = subreddit.toLowerCase();

        if (time == 0) {
            o.base = true;
        }

        o.time = time;

        String[] split = Reddit.cachedData.getString(subreddit.toLowerCase() + "," + time, "").split(",");
        if (split.length > 1) {
            o.time = time;
            o.submissions = new ArrayList<>();
            ObjectMapper mapperBase = new ObjectMapper();
            ObjectReader reader = mapperBase.reader();

            for (String s : split) {
                LogUtil.v("Time is " + System.currentTimeMillis());
                String gotten = Reddit.cachedData.getString(s, "");
                if (!gotten.isEmpty()) {

                    try {
                        if (gotten.startsWith("[") && offline) {
                            o.submissions.add(SubmissionSerializer.withComments(reader.readTree(gotten), CommentSort.CONFIDENCE));
                        } else if (gotten.startsWith("[")) {
                            JsonNode elem = reader.readTree(gotten);
                            o.submissions.add(new Submission(elem.get(0).get("data").get("children").get(0).get("data")));
                        } else {
                            o.submissions.add(new Submission(reader.readTree(gotten)));
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

    public static ArrayList<String> getAll(String subreddit) {
        subreddit = subreddit.toLowerCase();
        ArrayList<String> keys = new ArrayList<>();
        for (String s : Reddit.cachedData.getAll().keySet()) {
            if (s.startsWith(subreddit) && s.contains(",")) {
                keys.add(s);
            }
        }
        return keys;
    }


}
