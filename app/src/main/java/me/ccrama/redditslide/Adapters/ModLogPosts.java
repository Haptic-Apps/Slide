package me.ccrama.redditslide.Adapters;

import android.os.AsyncTask;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import net.dean.jraw.models.ModAction;
import net.dean.jraw.paginators.ModLogPaginator;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import me.ccrama.redditslide.Authentication;

/**
 * Created by ccrama on 9/17/2015.
 */
public class ModLogPosts {
    public  ArrayList<ModAction> posts;
    public  boolean              loading;
    private SwipeRefreshLayout   refreshLayout;
    private ModLogAdapter        adapter;
    private ModLogPaginator      paginator;

    public ModLogPosts(ArrayList<ModAction> firstData, ModLogPaginator paginator) {
        posts = firstData;
        this.paginator = paginator;
    }

    public ModLogPosts() {
    }

    public void bindAdapter(ModLogAdapter a, SwipeRefreshLayout layout) {
        this.adapter = a;
        this.refreshLayout = layout;
        loadMore(a);
    }

    public void loadMore(ModLogAdapter adapter) {
        new ModLogPosts.LoadData(true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void addData(List<ModAction> data) {
        posts.addAll(data);
    }

    public class LoadData extends AsyncTask<String, Void, ArrayList<ModAction>> {
        final boolean reset;

        public LoadData(boolean reset) {
            this.reset = reset;
        }

        @Override
        public void onPostExecute(ArrayList<ModAction> subs) {
            if (subs != null) {

                if (reset || posts == null) {
                    posts = new ArrayList<>(new LinkedHashSet(subs));
                } else {
                    posts.addAll(subs);
                    posts = new ArrayList<>(new LinkedHashSet(posts));
                }
                loading = false;
                refreshLayout.setRefreshing(false);
                adapter.dataSet = ModLogPosts.this;
                adapter.notifyDataSetChanged();
            } else {
                adapter.setError(true);
                refreshLayout.setRefreshing(false);

            }
        }

        @Override
        protected ArrayList<ModAction> doInBackground(String... subredditPaginators) {
            try {
                if (reset || paginator == null) {
                    paginator = new ModLogPaginator(Authentication.reddit, "mod");
                }

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
