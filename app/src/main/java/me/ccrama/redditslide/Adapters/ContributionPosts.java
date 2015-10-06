package me.ccrama.redditslide.Adapters;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;

import net.dean.jraw.http.NetworkException;
import net.dean.jraw.models.Contribution;
import net.dean.jraw.paginators.UserContributionPaginator;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Reddit;

/**
 * Created by ccrama on 9/17/2015.
 */
public class ContributionPosts {
    public ArrayList<Contribution> posts;
    public UserContributionPaginator paginator;
    public SwipeRefreshLayout refreshLayout;

    public String where;

    public String subreddit;

    public ContributionPosts(String subreddit, String where) {
        this.subreddit = subreddit;
        this.where = where;
    }

    public ContributionAdapter adapter;

    public void bindAdapter(ContributionAdapter a, SwipeRefreshLayout layout) throws ExecutionException, InterruptedException {
        this.adapter = a;
        this.refreshLayout=layout;
        loadMore(a, true, subreddit, where);
    }

    public void loadMore(ContributionAdapter adapter, boolean reset, String subreddit, String where) throws ExecutionException, InterruptedException {

        new LoadData(reset).execute(subreddit);


    }

    public class LoadData extends AsyncTask<String, Void, ArrayList<Contribution>> {
        boolean reset;

        public LoadData(boolean reset) {
            this.reset = reset;
        }

        @Override
        public void onPostExecute(ArrayList<Contribution> subs) {
            if (reset) {
                posts = subs;
            } else {
                posts.addAll(subs);
            }
            ((Activity) adapter.mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refreshLayout.setRefreshing(false);

                    adapter.dataSet = posts;

                    adapter.notifyDataSetChanged();

                }
            });
        }

        @Override
        protected ArrayList<Contribution> doInBackground(String... subredditPaginators) {
            if (reset || paginator == null) {
                    paginator = new UserContributionPaginator(Authentication.reddit, where, subreddit);

                paginator.setSorting(Reddit.defaultSorting);
                paginator.setTimePeriod(Reddit.timePeriod);
            }
            if (paginator.hasNext()) {
                try {
                    return new ArrayList<>(paginator.next());
                } catch (NetworkException e){

                }
            }
            return null;
        }
    }

}
