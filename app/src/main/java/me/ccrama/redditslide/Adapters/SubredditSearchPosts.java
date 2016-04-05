package me.ccrama.redditslide.Adapters;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;

import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.SubmissionSearchPaginator;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import me.ccrama.redditslide.Activities.Search;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.PostMatch;
import me.ccrama.redditslide.Reddit;

/**
 * Created by ccrama on 9/17/2015.
 */
public class SubredditSearchPosts extends GeneralPosts {
    private  String term;
    private  String subreddit = "";
    public boolean loading;
    private SubmissionSearchPaginator paginator;
    public SwipeRefreshLayout refreshLayout;
    private ContributionAdapter adapter;

    public Search parent;

    public SubredditSearchPosts(String subreddit, String term, Search parent) {
        if(subreddit != null) {
            this.subreddit = subreddit;
        }
        this.parent = parent;
        this.term = term;
    }

    public void bindAdapter(ContributionAdapter a, SwipeRefreshLayout layout) throws ExecutionException, InterruptedException {
        this.adapter = a;
        this.refreshLayout = layout;
        loadMore(a, subreddit, term, true);
    }

    public void loadMore(ContributionAdapter a, String subreddit, String where, boolean reset) {


        this.adapter = a;
        this.subreddit = subreddit;
        this.term = where;


        new LoadData(reset).execute();


    }
    public void reset() {
        new LoadData(true).execute();
    }

    public class LoadData extends AsyncTask<String, Void, ArrayList<Contribution>> {
        final boolean reset;

        public LoadData(boolean reset) {
            this.reset = reset;
        }

        @Override
        public void onPostExecute(ArrayList<Contribution> submissions) {
            loading = false;

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
                    paginator = new SubmissionSearchPaginator(Authentication.reddit, term);
                    if(!subreddit.isEmpty())
                    paginator.setSubreddit(subreddit);

                    paginator.setSearchSorting(Reddit.search);
                    paginator.setTimePeriod((parent.time));
                }

                if(!paginator.hasNext()){
                    nomore = true;
                    return new ArrayList<>();
                }
                if (reset) {
                    nomore = false;
                    for (Submission s : paginator.next()) {

                                    newSubmissions.add(s);


                        }
                    if(newSubmissions.size() == 0){
                        nomore = true;
                    }

                } else  if(!nomore){
                    for (Submission s : paginator.next()) {

                            newSubmissions.add(s);

                    }
                } else {
                    adapter.notifyDataSetChanged();
                }
                return newSubmissions;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

    }

}
