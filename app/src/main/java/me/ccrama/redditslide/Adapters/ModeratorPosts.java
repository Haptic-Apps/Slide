package me.ccrama.redditslide.Adapters;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import net.dean.jraw.models.PublicContribution;
import net.dean.jraw.paginators.ModeratorPaginator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import me.ccrama.redditslide.Authentication;

/**
 * Created by ccrama on 9/17/2015.
 */
public class ModeratorPosts {
    public ArrayList<PublicContribution> posts;
    public boolean loading;
    private SwipeRefreshLayout refreshLayout;
    private String where;

    private String subreddit;
    private ModeratorAdapter adapter;
    private ModeratorPaginator paginator;
    public ModeratorPosts(ArrayList<PublicContribution> firstData, ModeratorPaginator paginator) {
        posts = firstData;
        this.paginator = paginator;
    }

    public ModeratorPosts(String where, String subreddit) {
        this.where = where;
        this.subreddit = subreddit;
    }

    public void bindAdapter(ModeratorAdapter a, SwipeRefreshLayout layout) throws ExecutionException, InterruptedException {
        this.adapter = a;
        this.refreshLayout = layout;
        loadMore(a, where, subreddit);
    }

    public void loadMore(ModeratorAdapter adapter, String where, String subreddit) {
        this.subreddit = subreddit;

            new LoadData(true).execute(where);



    }

    public void addData(List<PublicContribution> data) {
        posts.addAll(data);
    }

    public class LoadData extends AsyncTask<String, Void, ArrayList<PublicContribution>> {
        final boolean reset;

        public LoadData(boolean reset) {
            this.reset = reset;
        }

        @Override
        public void onPostExecute(ArrayList<PublicContribution> subs) {
            if (subs != null) {

                loading = false;
                refreshLayout.setRefreshing(false);
                adapter.dataSet = subs;
                adapter.notifyDataSetChanged();
            } else {
                adapter.setError(true);
                refreshLayout.setRefreshing(false);

            }
        }

        @Override
        protected ArrayList<PublicContribution> doInBackground(String... subredditPaginators) {
            try {
                if (reset || paginator == null) {
                    paginator = new ModeratorPaginator(Authentication.reddit, subreddit, where);
                }
                paginator.setIncludeComments(true);
                paginator.setIncludeSubmissions(true);

                if (paginator.hasNext()) {
                    ArrayList<PublicContribution> done = new ArrayList<>(paginator.next());

                    Log.v("Slide", done.size() + "SIZE");
                    return done;

                }
                return null;
            } catch (Exception e) {
                return null;
            }
        }
    }
}
