package me.ccrama.redditslide.Adapters;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;

import net.dean.jraw.http.NetworkException;
import net.dean.jraw.models.MultiReddit;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.MultiRedditPaginator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Reddit;

public class MultiredditPosts {
    public ArrayList<Submission> posts;
    public MultiRedditPaginator paginator;
    public SwipeRefreshLayout refreshLayout;

    public MultiredditPosts(ArrayList<Submission> firstData, MultiRedditPaginator paginator) {
        posts = firstData;
        this.paginator = paginator;
    }

    public MultiReddit subreddit;

    public MultiredditPosts(MultiReddit subreddit) {
        this.subreddit = subreddit;
    }

    public MultiredditAdapter adapter;

    public void bindAdapter(MultiredditAdapter a, SwipeRefreshLayout layout) throws ExecutionException, InterruptedException {
        this.adapter = a;
        this.refreshLayout=layout;
        loadMore(a, true, subreddit);
    }

    public void loadMore(MultiredditAdapter adapter, boolean reset, MultiReddit subreddit) throws ExecutionException, InterruptedException {
        new LoadData(reset).execute(subreddit);


    }

    public class LoadData extends AsyncTask<MultiReddit, Void, ArrayList<Submission>> {
        boolean reset;

        public LoadData(boolean reset) {
            this.reset = reset;
        }

        @Override
        public void onPostExecute(ArrayList<Submission> subs) {
            if (reset) {
                posts = subs;
            } else {
                posts.addAll(subs);
            }
            ((Activity) adapter.mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refreshLayout.setRefreshing(false);

                    adapter.notifyDataSetChanged();

                }
            });
        }

        @Override
        protected ArrayList<Submission> doInBackground(MultiReddit... subredditPaginators) {
            if (reset || paginator == null) {

                    paginator = new MultiRedditPaginator(Authentication.reddit, subredditPaginators[0]);


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

    public void addData(List<Submission> data) {
        posts.addAll(data);
    }
}
