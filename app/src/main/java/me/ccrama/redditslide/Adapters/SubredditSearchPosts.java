package me.ccrama.redditslide.Adapters;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import net.dean.jraw.http.NetworkException;
import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.Paginator;
import net.dean.jraw.paginators.SubmissionSearchPaginator;
import net.dean.jraw.paginators.SubmissionSearchPaginatorMultireddit;
import net.dean.jraw.paginators.TimePeriod;

import java.net.UnknownHostException;
import java.util.ArrayList;

import me.ccrama.redditslide.Activities.MultiredditOverview;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.PostMatch;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.util.SortingUtil;

/**
 * Created by ccrama on 9/17/2015.
 */
public class SubredditSearchPosts extends GeneralPosts {
    private String term;
    private String subreddit = "";
    public  boolean               loading;
    private Paginator<Submission> paginator;
    public  SwipeRefreshLayout    refreshLayout;
    private ContributionAdapter   adapter;

    public Activity parent;

    public SubredditSearchPosts(String subreddit, String term, Activity parent, boolean multireddit) {
        if (subreddit != null) {
            this.subreddit = subreddit;
        }
        this.parent = parent;
        this.term = term;
        this.multireddit = multireddit;
    }

    public void bindAdapter(ContributionAdapter a, SwipeRefreshLayout layout) {
        this.adapter = a;
        this.refreshLayout = layout;
        loadMore(a, subreddit, term, true);
    }

    public void loadMore(ContributionAdapter a, String subreddit, String where, boolean reset) {
        this.adapter = a;
        this.subreddit = subreddit;
        this.term = where;
        new LoadData(reset).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void loadMore(ContributionAdapter a, String subreddit, String where, boolean reset,
            boolean multi, TimePeriod time) {
        this.adapter = a;
        this.subreddit = subreddit;
        this.term = where;
        this.multireddit = multi;
        this.time = time;
        new LoadData(reset).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    boolean multireddit;
    TimePeriod time = TimePeriod.ALL;

    public void reset(TimePeriod time) {
        this.time = time;
        new LoadData(true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public class LoadData extends AsyncTask<String, Void, ArrayList<Contribution>> {
        final boolean reset;

        public LoadData(boolean reset) {
            this.reset = reset;
        }

        @Override
        public void onPostExecute(ArrayList<Contribution> submissions) {
            loading = false;

            if(error != null){
                if(error instanceof NetworkException){
                    NetworkException e = (NetworkException)error;
                    Toast.makeText(adapter.mContext,"Loading failed, " + e.getResponse().getStatusCode() + ": " + ((NetworkException) error).getResponse().getStatusMessage(), Toast.LENGTH_LONG).show();
                }
                if(error.getCause() instanceof UnknownHostException){
                    Toast.makeText(adapter.mContext,"Loading failed, please check your internet connection", Toast.LENGTH_LONG).show();
                }
            }

            if (submissions != null && !submissions.isEmpty()) {
                // new submissions found

                int start = 0;
                if (posts != null) {
                    start = posts.size() + 1;
                }

                ArrayList<Contribution> filteredSubmissions = new ArrayList<>();
                for (Contribution c : submissions) {
                    if (c instanceof Submission) {
                        if (!PostMatch.doesMatch((Submission) c)) {
                            filteredSubmissions.add(c);
                        }
                    } else {
                        filteredSubmissions.add(c);
                    }
                }

                if (reset || posts == null) {
                    posts = filteredSubmissions;
                    start = -1;
                } else {
                    posts.addAll(filteredSubmissions);
                }

                final int finalStart = start;
                // update online
                if (refreshLayout != null) {
                    refreshLayout.setRefreshing(false);
                }

                if (finalStart != -1) {
                    adapter.notifyItemRangeInserted(finalStart + 1, posts.size());
                } else {
                    adapter.notifyDataSetChanged();
                }

            } else if (submissions != null) {
                // end of submissions
                nomore = true;
                adapter.notifyDataSetChanged();
                if (reset) {
                    Toast.makeText(adapter.mContext, R.string.no_posts_found, Toast.LENGTH_LONG).show();
                }
            } else if (!nomore) {
                // error
                adapter.setError(true);
            }
            refreshLayout.setRefreshing(false);
        }

        @Override
        protected ArrayList<Contribution> doInBackground(String... subredditPaginators) {
            ArrayList<Contribution> newSubmissions = new ArrayList<>();
            try {
                if (reset || paginator == null) {
                    if (multireddit) {
                        paginator = new SubmissionSearchPaginatorMultireddit(Authentication.reddit,
                                term);
                        ((SubmissionSearchPaginatorMultireddit) paginator).setMultiReddit(
                                MultiredditOverview.searchMulti);
                        ((SubmissionSearchPaginatorMultireddit) paginator).setSearchSorting(
                                SubmissionSearchPaginatorMultireddit.SearchSort.valueOf(
                                        SortingUtil.search.toString()));
                        ((SubmissionSearchPaginatorMultireddit) paginator).setSyntax(
                                SubmissionSearchPaginatorMultireddit.SearchSyntax.LUCENE);

                    } else {
                        paginator = new SubmissionSearchPaginator(Authentication.reddit, term);
                        if (!subreddit.isEmpty()) {
                            ((SubmissionSearchPaginator) paginator).setSubreddit(subreddit);
                        }
                        ((SubmissionSearchPaginator) paginator).setSearchSorting(
                                SortingUtil.search);
                        ((SubmissionSearchPaginator) paginator).setSyntax(
                                SubmissionSearchPaginator.SearchSyntax.LUCENE);

                    }
                    paginator.setTimePeriod((time));
                }

                if (!paginator.hasNext()) {
                    nomore = true;
                    return newSubmissions;
                }
                newSubmissions.addAll(paginator.next());

                return newSubmissions;
            } catch (Exception e) {
              error = e;
                e.printStackTrace();
                return null;
            }
        }
        Exception error;

    }

}
