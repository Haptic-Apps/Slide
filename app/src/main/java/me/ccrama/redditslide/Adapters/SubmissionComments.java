package me.ccrama.redditslide.Adapters;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;

import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.SubmissionRequest;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.CommentSort;
import net.dean.jraw.models.MoreChildren;
import net.dean.jraw.models.Submission;

import java.util.ArrayList;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Fragments.CommentPage;

/**
 * Created by ccrama on 9/17/2015.
 */
public class SubmissionComments {
    public final SwipeRefreshLayout refreshLayout;
    private final String fullName;
    private final CommentPage page;
    public ArrayList<CommentObject> comments;
    public Submission submission;
    private String context;
    private CommentSort defaultSorting = CommentSort.CONFIDENCE;
    private CommentAdapter adapter;
    public LoadData mLoadData;

    public SubmissionComments(String fullName, CommentPage commentPage, SwipeRefreshLayout layout, Submission s) {
        this.fullName = fullName;
        this.page = commentPage;

        this.refreshLayout = layout;

        if(s.getComments() != null){
            submission = s;
            CommentNode baseComment = s.getComments();
            comments = new ArrayList<>();

            int toPut = -1;
            MoreChildren toDo = null;
            CommentNode toDoComment = null;
            for (CommentNode n : baseComment.walkTree()) {
                CommentObject obj = new CommentObject(n);
                if (n.getDepth() <= toPut && toDo != null) {
                   obj.setMoreChildren(toDo, toDoComment);
                    toPut = -1;
                    toDo = null;
                }
                comments.add(obj);


                if (n.hasMoreComments()) {
                    toPut = n.getDepth();
                    toDo = n.getMoreChildren();
                    toDoComment = n;
                }
            }
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }

            refreshLayout.setRefreshing(false);
            refreshLayout.setEnabled(false);
        }
    }

    public SubmissionComments(String fullName, CommentPage commentPage, SwipeRefreshLayout layout) {
        this.fullName = fullName;
        this.page = commentPage;

        this.refreshLayout = layout;
    }

    public SubmissionComments(String fullName, CommentPage commentPage, SwipeRefreshLayout layout, String context) {
        this.fullName = fullName;
        this.page = commentPage;
        this.context = context;
        this.refreshLayout = layout;
    }

    public void cancelLoad() {
        if (mLoadData != null) {
            mLoadData.cancel(true);
        }
    }

    public void setSorting(CommentSort sort) {
        defaultSorting = sort;

        mLoadData = new LoadData(false);
        mLoadData.execute(fullName);
    }

    public void loadMore(CommentAdapter adapter, String subreddit) {
        this.adapter = adapter;

        mLoadData = new LoadData(true);
        mLoadData.execute(fullName);
    }

    public class LoadData extends AsyncTask<String, Void, ArrayList<CommentObject>> {
        final boolean reset;

        public LoadData(boolean reset) {
            this.reset = reset;
        }

        @Override
        public void onPostExecute(ArrayList<CommentObject> subs) {
            page.doData(reset);
            refreshLayout.setRefreshing(false);
        }

        @Override
        protected ArrayList<CommentObject> doInBackground(String... subredditPaginators) {
            SubmissionRequest.Builder builder;
            if (context == null) {
                builder = new SubmissionRequest.Builder(fullName).sort(defaultSorting);
            } else {
                builder = new SubmissionRequest.Builder(fullName).sort(defaultSorting).focus(context).context(3);
            }
            try {
                submission = Authentication.reddit.getSubmission(builder.build());
                CommentNode baseComment = submission.getComments();
                comments = new ArrayList<>();

                int i = 0;
                int toPut = -1;
                MoreChildren toDo = null;
                CommentNode toDoComment = null;
                for (CommentNode n : baseComment.walkTree()) {

                    CommentObject obj = new CommentObject(n);
                    if (n.getDepth() <= toPut && toDo != null) {
                        comments.get(comments.size() - 1).setMoreChildren(toDo, toDoComment);
                        toPut = -1;
                        toDo = null;
                    }
                    comments.add(obj);


                    if (n.hasMoreComments()) {
                        toPut = n.getDepth();
                        toDo = n.getMoreChildren();
                        toDoComment = n;
                    }
                    i++;

                }
                return comments;
            } catch (NetworkException e) {
                //Todo reauthenticate
            }
            return null;
        }
    }


}
