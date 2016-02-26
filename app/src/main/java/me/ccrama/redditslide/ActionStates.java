package me.ccrama.redditslide;

import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import java.util.ArrayList;

/**
 * Created by carlo_000 on 2/26/2016.
 */
public class ActionStates {
    public static ArrayList<String> upVotedFullnames = new ArrayList<>();
    public static ArrayList<String> downVotedFullnames = new ArrayList<>();

    public static ArrayList<String> unvotedFullnames = new ArrayList<>();
    public ArrayList<String> savedFullnames;

    public static VoteDirection getVoteDirection(Submission s) {
        if (upVotedFullnames.contains(s.getFullName())) {
            return VoteDirection.UPVOTE;
        } else if (downVotedFullnames.contains(s.getFullName())) {
            return VoteDirection.DOWNVOTE;
        } else if (unvotedFullnames.contains(s.getFullName())) {
            return VoteDirection.NO_VOTE;
        } else {
            return s.getVote();
        }
    }

    public static void setVoteDirection(Submission s, VoteDirection direction){
        String fullname = s.getFullName();
        upVotedFullnames.remove(fullname);
        downVotedFullnames.remove(fullname);
        unvotedFullnames.remove(fullname);
        switch(direction){

            case UPVOTE:
                upVotedFullnames.add(fullname);
                break;
            case DOWNVOTE:
                downVotedFullnames.add(fullname);
                break;
            case NO_VOTE:
                unvotedFullnames.add(fullname);
                break;
        }
    }
}
