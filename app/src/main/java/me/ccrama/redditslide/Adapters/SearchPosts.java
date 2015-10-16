package me.ccrama.redditslide.Adapters;

import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.SubmissionSearchPaginator;

import java.util.ArrayList;
import java.util.List;

public class SearchPosts {
    public ArrayList<Submission> posts;
    public SubmissionSearchPaginator paginator;
    public SearchPosts(ArrayList<Submission> firstData, SubmissionSearchPaginator paginator){
        posts = firstData;
        this.paginator = paginator;
    }
    public void addData(List<Submission> data){
        posts.addAll(data);
    }
}
