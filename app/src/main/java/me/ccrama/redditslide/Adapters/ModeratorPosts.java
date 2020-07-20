package me.ccrama.redditslide.Adapters;

import android.os.AsyncTask;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import net.dean.jraw.models.PublicContribution;
import net.dean.jraw.paginators.ModeratorPaginator;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

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

    public void bindAdapter(ModeratorAdapter a, SwipeRefreshLayout layout) {
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

                if(reset || posts == null){
                    posts = new ArrayList<>(new LinkedHashSet(subs));
                } else {
                    posts.addAll(subs);
                    posts = new ArrayList<>(new LinkedHashSet(posts));
                }
                loading = false;
                refreshLayout.setRefreshing(false);
                adapter.dataSet = ModeratorPosts.this;
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

                    return new ArrayList<>(paginator.next());

                }
                return null;
            } catch (Exception e) {
                return null;
            }
        }
    }
}
