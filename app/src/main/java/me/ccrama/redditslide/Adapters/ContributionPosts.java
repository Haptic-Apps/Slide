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
import me.ccrama.redditslide.Hidden;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;

/**
 * Created by ccrama on 9/17/2015.
 */
public class ContributionPosts {
    public ArrayList<Contribution> posts;
    private UserContributionPaginator paginator;
    private SwipeRefreshLayout refreshLayout;

    private final String where;

    private final String subreddit;

    public ContributionPosts(String subreddit, String where) {
        this.subreddit = subreddit;
        this.where = where;
    }

    private ContributionAdapter adapter;

    public void bindAdapter(ContributionAdapter a, SwipeRefreshLayout layout) throws ExecutionException, InterruptedException {
        this.adapter = a;
        this.refreshLayout=layout;
        loadMore(a, subreddit, where);
    }

    public void loadMore(ContributionAdapter adapter, String subreddit, String where) {

        if(Reddit.online) {

            new LoadData(true).execute(subreddit);

        } else {
            adapter.setError(true);
            refreshLayout.setRefreshing(false);
        }

    }

    public class LoadData extends AsyncTask<String, Void, ArrayList<Contribution>> {
        final boolean reset;

        public LoadData(boolean reset) {
            this.reset = reset;
        }

        @Override
        public void onPostExecute(ArrayList<Contribution> subs) {
            if(subs == null){
                adapter.setError(true);
            } else {
                if (reset) {
                    posts = subs;
                    ((Activity) adapter.mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshLayout.setRefreshing(false);

                            adapter.dataSet = posts;

                            adapter.notifyItemRangeInserted(0, posts.size());

                        }
                    });
                } else {
                    final int start = posts.size();
                    posts.addAll(subs);
                    ((Activity) adapter.mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshLayout.setRefreshing(false);

                            adapter.dataSet = posts;

                            adapter.notifyItemRangeInserted(start, start + posts.size());

                        }
                    });
                }

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
                            if (Hidden.isHidden(s)) {
                                if (SettingValues.NSFWPosts && s.isNsfw()) {
                                    posts.add(s);
                                } else if (!s.isNsfw()) {
                                    posts.add(s);
                                }
                            }
                        } else {
                            posts.add(c);
                        }
                    }
                } else {
                    for (Contribution c : paginator.next()) {
                        if (c instanceof Submission) {
                            Submission s = (Submission) c;
                            if (Hidden.isHidden(s)) {
                                if (SettingValues.NSFWPosts && s.isNsfw()) {
                                    posts.add(s);
                                } else if (!s.isNsfw()) {
                                    posts.add(s);
                                }
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
