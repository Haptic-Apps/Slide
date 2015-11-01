package me.ccrama.redditslide.Adapters;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;

import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.SubredditPaginator;

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
public class SubredditPosts {
    public ArrayList<Submission> posts = null;
    private SubredditPaginator paginator;
    private SwipeRefreshLayout refreshLayout;

    public boolean loading;

    public SubredditPosts(ArrayList<Submission> firstData, SubredditPaginator paginator) {
        posts = firstData;
        this.paginator = paginator;
    }

    public ArrayList<Submission> getPosts() {
        try {
            return new LoadData(true).execute(subreddit).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public String subreddit;

    public SubredditPosts(String subreddit) {
        this.subreddit = subreddit;
    }

    private SubmissionAdapter adapter;

    public void bindAdapter(SubmissionAdapter a, SwipeRefreshLayout layout) {
        this.adapter = a;
        this.refreshLayout = layout;
        loadMore(a, true, subreddit);
    }

    public void loadMore(SubmissionAdapter adapter, boolean reset, String subreddit) {
        if (Reddit.online) {

            new LoadData(reset).execute(subreddit);

        } else {
            adapter.setError(true);
            refreshLayout.setRefreshing(false);
        }


    }

    public class LoadData extends AsyncTask<String, Void, ArrayList<Submission>> {
        final boolean reset;

        public LoadData(boolean reset) {
            this.reset = reset;
        }

        @Override
        public void onPostExecute(ArrayList<Submission> subs) {

            if (subs == null || subs.isEmpty()) {
                adapter.setError(true);
                refreshLayout.setRefreshing(false);
            } else {
                adapter.undoSetError();
                if (reset) {
                    posts = subs;
                    ((Activity) adapter.mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshLayout.setRefreshing(false);

                            adapter.dataSet = posts;

                            adapter.notifyDataSetChanged();

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

                            adapter.notifyDataSetChanged();

                        }
                    });
                }
            }
        }

        @Override
        protected ArrayList<Submission> doInBackground(String... subredditPaginators) {
            try {
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


                }

                return null;
            } catch (Exception e) {
                return null;
            }
        }
    }

    public void addData(List<Submission> data) {
        posts.addAll(data);
    }
}
