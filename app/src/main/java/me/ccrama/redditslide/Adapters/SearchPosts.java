package me.ccrama.redditslide.Adapters;

import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.SubmissionSearchPaginator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ccrama on 9/17/2015.
 */
class SearchPosts {
    private final ArrayList<Submission> posts;

    public SearchPosts(ArrayList<Submission> firstData, SubmissionSearchPaginator paginator){
        posts = firstData;
        SubmissionSearchPaginator paginator1 = paginator;
    }
    public void addData(List<Submission> data){
        posts.addAll(data);
    }
}
