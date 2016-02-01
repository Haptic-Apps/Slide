package me.ccrama.redditslide.Adapters;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.UserContributionPaginator;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.PostMatch;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.util.LogUtil;

/**
 * Created by ccrama on 9/17/2015.
 */
public class ContributionPosts extends GeneralPosts {
    private final String where;
    private final String subreddit;
    public boolean loading;
    private UserContributionPaginator paginator;
    private SwipeRefreshLayout refreshLayout;
    private ContributionAdapter adapter;

    public ContributionPosts(String subreddit, String where) {
        this.subreddit = subreddit;
        this.where = where;
    }

    public boolean nomore;

    public void bindAdapter(ContributionAdapter a, SwipeRefreshLayout layout) throws ExecutionException, InterruptedException {
        this.adapter = a;
        this.refreshLayout = layout;
        loadMore(a, subreddit, true);
    }

    public void loadMore(ContributionAdapter adapter, String subreddit, boolean reset) {


        new LoadData(reset).execute(subreddit);


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
                Log.v(LogUtil.getTag(), "SIZE IS " + filteredSubmissions.size());

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
                    adapter.notifyItemRangeInserted(finalStart, posts.size());
                } else {
                    adapter.notifyDataSetChanged();
                }

            } else if (submissions != null) {
                // end of submissions
                nomore = true;
            } else if (!nomore) {
                // error
                adapter.setError(true);
            }
            refreshLayout.setRefreshing(false);
        }

        @Override
        protected ArrayList<Contribution> doInBackground(String... subredditPaginators) {
            ArrayList<Contribution> newData = new ArrayList<>();
            try {
                if (reset || paginator == null) {
                    paginator = new UserContributionPaginator(Authentication.reddit, where, subreddit);

                    paginator.setSorting(Reddit.getSorting(subreddit));
                    paginator.setTimePeriod(Reddit.timePeriod);
                }

                if (!paginator.hasNext()) {
                    nomore = true;
                    return new ArrayList<>();
                }
                for (Contribution c : paginator.next()) {
                    if (c instanceof Submission) {
                        Submission s = (Submission) c;
                            newData.add(s);

                    } else {
                        newData.add(c);
                    }
                }

                return newData;
            } catch (Exception e) {
                return null;
            }
        }

    }

}
