package me.ccrama.redditslide.Adapters;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;

import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.UserContributionPaginator;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;

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

    public void bindAdapter(ContributionAdapter a, SwipeRefreshLayout layout) throws ExecutionException, InterruptedException {
        this.adapter = a;
        this.refreshLayout = layout;
        loadMore(a, subreddit, where);
    }

    public void loadMore(ContributionAdapter adapter, String subreddit, String where) {


            new LoadData(true).execute(subreddit);


    }

    public class LoadData extends AsyncTask<String, Void, ArrayList<Contribution>> {
        final boolean reset;

        public LoadData(boolean reset) {
            this.reset = reset;
        }

        @Override
        public void onPostExecute(ArrayList<Contribution> subs) {
            if (subs != null) {

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
                refreshLayout.setRefreshing(false);

            }
        }

        @Override
        protected ArrayList<Contribution> doInBackground(String... subredditPaginators) {
            try {
                if (reset || paginator == null) {
                    paginator = new UserContributionPaginator(Authentication.reddit, where, subreddit);

                    paginator.setSorting(Reddit.defaultSorting);
                    paginator.setTimePeriod(Reddit.timePeriod);
                }
                if (reset) {
                    posts = new ArrayList<>();
                    for (Contribution c : paginator.next()) {
                        if (c instanceof Submission) {
                            Submission s = (Submission) c;
                                if (SettingValues.NSFWPosts && s.isNsfw()) {
                                    posts.add(s);
                                } else if (!s.isNsfw()) {
                                    posts.add(s);

                            }
                        } else {
                            posts.add(c);
                        }
                    }
                } else {
                    for (Contribution c : paginator.next()) {
                        if (c instanceof Submission) {
                            Submission s = (Submission) c;
                                if (SettingValues.NSFWPosts && s.isNsfw()) {
                                    posts.add(s);
                                } else if (!s.isNsfw()) {
                                    posts.add(s);

                            }
                        } else {
                            posts.add(c);
                        }
                    }
                }
                return posts;
            } catch (Exception e) {
                return null;
            }
        }

    }

}
