package me.ccrama.redditslide.Adapters;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;

import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.SubmissionRequest;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.CommentSort;
import net.dean.jraw.models.Submission;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Fragments.CommentPage;

/**
 * Created by ccrama on 9/17/2015.
 */
public class SubmissionComments {
    public ArrayList<CommentNode> comments;
    public CommentNode baseComment;
    public SwipeRefreshLayout refreshLayout;

    public String context;

    public String fullName;
    public Submission submission;
    public CommentPage page;

    public CommentSort defaultSorting = CommentSort.CONFIDENCE;
    public SubmissionComments(String fullName, CommentPage commentPage, SwipeRefreshLayout layout) {
        this.fullName = fullName;
        this.page = commentPage;

        this.refreshLayout = layout;
        new LoadData(true).execute(fullName);
    }
    public SubmissionComments(String fullName, CommentPage commentPage, SwipeRefreshLayout layout, String context) {
        this.fullName = fullName;
        this.page = commentPage;
        this.context = context;
        this.refreshLayout = layout;
        new LoadData(true).execute(fullName);
    }
    public void setSorting(CommentSort sort){
        defaultSorting = sort;
        new LoadData(false).execute(fullName);

    }
    public CommentAdapter adapter;


    public void loadMore(CommentAdapter adapter, boolean reset, String subreddit) throws ExecutionException, InterruptedException {
        this.adapter = adapter;
        new LoadData(reset).execute(subreddit);

    }

    public class LoadData extends AsyncTask<String, Void, ArrayList<Submission>> {
        boolean reset;

        public LoadData(boolean reset) {
            this.reset = reset;
        }

        @Override
        public void onPostExecute(ArrayList<Submission> subs) {


            if(adapter != null){
                adapter.notifyDataSetChanged();
            }
                    page.doData(reset);

                    refreshLayout.setRefreshing(false);
        }

        @Override
        protected ArrayList<Submission> doInBackground(String... subredditPaginators) {
            SubmissionRequest.Builder builder = null;
            if(context == null) {
                builder = new SubmissionRequest.Builder(fullName).sort(defaultSorting);
            } else {
                builder = new SubmissionRequest.Builder(fullName).sort(defaultSorting).focus(context).context(3);
            }
            try {
                submission = Authentication.reddit.getSubmission(builder.build());
                baseComment = submission.getComments();
                comments = new ArrayList<>();
                for (CommentNode n : baseComment.walkTree()) {

                    comments.add(n);



                }
            } catch (NetworkException e ){
                //Todo reauthenticate
            }
            return null;
        }
    }


}
