package me.ccrama.redditslide.Adapters;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;

import net.dean.jraw.models.PublicContribution;
import net.dean.jraw.paginators.ModeratorPaginator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Reddit;

/**
 * Created by ccrama on 9/17/2015.
 */
public class ModeratorPosts {
    public ArrayList<PublicContribution> posts;
    private SwipeRefreshLayout refreshLayout;

    public ModeratorPosts(ArrayList<PublicContribution> firstData, ModeratorPaginator paginator) {
        posts = firstData;
        this.paginator = paginator;
    }

    private String where;

    private String subreddit;
    public ModeratorPosts(String where, String subreddit) {
        this.where = where; 
        this.subreddit = subreddit;
    }

    private ModeratorAdapter adapter;
    private ModeratorPaginator paginator;

    public void bindAdapter(ModeratorAdapter a, SwipeRefreshLayout layout) throws ExecutionException, InterruptedException {
        this.adapter = a;
        this.refreshLayout=layout;
        loadMore(a, where, subreddit);
    }

    public void loadMore(ModeratorAdapter adapter, String where, String subreddit) {
        this.subreddit = subreddit;
        if(Reddit.online) {

            new LoadData(true).execute(where);

        } else {
            adapter.setError(true);
            refreshLayout.setRefreshing(false);
        }

    }

    public boolean loading;
    public class LoadData extends AsyncTask<String, Void, ArrayList<PublicContribution>> {
        final boolean reset;

        public LoadData(boolean reset) {
            this.reset = reset;
        }

        @Override
        public void onPostExecute(ArrayList<PublicContribution> subs) {
            if(subs != null) {

                loading = false;

                if (refreshLayout != null)
                    ((Activity) adapter.mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshLayout.setRefreshing(false);

                            adapter.dataSet = posts;

                            adapter.notifyDataSetChanged();

                        }
                    });
            } else {
                adapter.setError(true);

            }
        }

        @Override
        protected ArrayList<PublicContribution> doInBackground(String... subredditPaginators) {
            try {
                if (reset || paginator == null) {
                    paginator = new ModeratorPaginator(Authentication.reddit, subreddit, where);
                }
                if (paginator.hasNext()) {
                        ArrayList<PublicContribution> done = new ArrayList<>(paginator.next());

                        return done;

                }
                return null;
            } catch (Exception e){
                return null;
            }
        }
    }

    public void addData(List<PublicContribution> data) {
        posts.addAll(data);
    }
}
