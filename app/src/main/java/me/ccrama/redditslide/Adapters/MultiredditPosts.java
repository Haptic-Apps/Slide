package me.ccrama.redditslide.Adapters;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;

import net.dean.jraw.models.MultiReddit;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.MultiRedditPaginator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Hidden;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;

/**
 * Created by ccrama on 9/17/2015.
 */
public class MultiredditPosts {
    public ArrayList<Submission> posts;
    private MultiRedditPaginator paginator;
    private SwipeRefreshLayout refreshLayout;

    public MultiredditPosts(ArrayList<Submission> firstData, MultiRedditPaginator paginator) {
        posts = firstData;
        this.paginator = paginator;
    }

    private MultiReddit subreddit;

    public MultiredditPosts(MultiReddit subreddit) {
        this.subreddit = subreddit;
    }

    private MultiredditAdapter adapter;

    public void bindAdapter(MultiredditAdapter a, SwipeRefreshLayout layout) throws ExecutionException, InterruptedException {
        this.adapter = a;
        this.refreshLayout=layout;
        loadMore(a, subreddit);
    }

    public void loadMore(MultiredditAdapter adapter, MultiReddit subreddit) {
        if(Reddit.online) {

            new LoadData(true).execute(subreddit);

        } else {
            adapter.setError(true);
            refreshLayout.setRefreshing(false);
        }

    }

    public class LoadData extends AsyncTask<MultiReddit, Void, ArrayList<Submission>> {
        final boolean reset;

        public LoadData(boolean reset) {
            this.reset = reset;
        }

        @Override
        public void onPostExecute(ArrayList<Submission> subs) {
            if(subs ==null) {
                adapter.setError(true);
            } else {
                if (reset) {
                    posts = subs;
                    ((Activity) adapter.mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshLayout.setRefreshing(false);

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

                            adapter.notifyItemRangeInserted(start, start + posts.size());

                        }
                    });
                }
            }
        }

        @Override
        protected ArrayList<Submission> doInBackground(MultiReddit... subredditPaginators) {
            try {
                if (reset || paginator == null) {

                    paginator = new MultiRedditPaginator(Authentication.reddit, subredditPaginators[0]);
                    paginator.setSorting(Reddit.defaultSorting);
                    paginator.setTimePeriod(Reddit.timePeriod);
                }
                if (reset) {
                    posts = new ArrayList<>();
                    for (Submission s : paginator.next()) {
                        if (Hidden.isHidden(s)) {
                            if (SettingValues.NSFWPosts && s.isNsfw()) {
                                posts.add(s);
                            } else if (!s.isNsfw()) {
                                posts.add(s);
                            }
                        }
                    }
                } else {
                    for (Submission s : paginator.next()) {
                        if (SettingValues.NSFWPosts && s.isNsfw()) {
                            posts.add(s);
                        } else if (!s.isNsfw()) {
                            posts.add(s);
                        }
                    }
                }
                return posts;
            }catch (Exception e){
                return null;
            }

        }
    }

    public void addData(List<Submission> data) {
        posts.addAll(data);
    }
}
