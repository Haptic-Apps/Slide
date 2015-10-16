package me.ccrama.redditslide.Adapters;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;

import net.dean.jraw.http.NetworkException;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.SubredditPaginator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Reddit;

public class SubredditPosts {
    public ArrayList<Submission> posts;
    public SubredditPaginator paginator;
    public SwipeRefreshLayout refreshLayout;

    public boolean loading;
    public SubredditPosts(ArrayList<Submission> firstData, SubredditPaginator paginator) {
        posts = firstData;
        this.paginator = paginator;
    }

    public ArrayList<Submission> getPosts(){
        try {
            return new LoadData(true).execute(subreddit).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    public String subreddit;

    public SubredditPosts(String subreddit) {
        this.subreddit = subreddit;
    }

    public SubmissionAdapter adapter;

    public void bindAdapter(SubmissionAdapter a, SwipeRefreshLayout layout) throws ExecutionException, InterruptedException {
        this.adapter = a;
        this.refreshLayout=layout;
        loadMore(a, true, subreddit);
    }

    public void loadMore(SubmissionAdapter adapter, boolean reset, String subreddit) throws ExecutionException, InterruptedException {
        new LoadData(reset).execute(subreddit);


    }

    public class LoadData extends AsyncTask<String, Void, ArrayList<Submission>> {
        boolean reset;

        public LoadData(boolean reset) {
            this.reset = reset;
        }

        @Override
        public void onPostExecute(ArrayList<Submission> subs) {

            loading = true;
            if(refreshLayout != null)
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
        protected ArrayList<Submission> doInBackground(String... subredditPaginators) {
            if (reset || paginator == null) {
                if (subredditPaginators[0].toLowerCase().equals("frontpage")) {
                    paginator = new SubredditPaginator(Authentication.reddit);
                } else {
                    paginator = new SubredditPaginator(Authentication.reddit, subredditPaginators[0]);

                }
                paginator.setSorting(Reddit.defaultSorting);
                paginator.setTimePeriod(Reddit.timePeriod);
            }
            if (paginator.hasNext()) {
                try {
                    if (reset) {
                        posts = new ArrayList<>(paginator.next());
                    } else {
                        posts.addAll(paginator.next());
                    }
                    return posts ;
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
