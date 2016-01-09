package me.ccrama.redditslide;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.dean.jraw.models.CommentSort;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.meta.SubmissionSerializer;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by carlo_000 on 11/19/2015.
 */
public class OfflineSubreddit {
    public long time;
    public ArrayList<Submission> submissions;

    public OfflineSubreddit(String s){
        String[] split = s.split("<SEPARATOR>");
        time = Long.valueOf(split[0]);
        submissions = new ArrayList<>();
        for(int i = 1; i < split.length; i++){
            try {
                if(split[i].startsWith("[")) {//has comment
                    submissions.add(SubmissionSerializer.withComments(new ObjectMapper().readTree(split[i]), CommentSort.CONFIDENCE));
                } else {
                    submissions.add(new Submission(new ObjectMapper().readTree(split[i])));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
