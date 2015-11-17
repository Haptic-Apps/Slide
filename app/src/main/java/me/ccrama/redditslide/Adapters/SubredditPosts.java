package me.ccrama.redditslide.Adapters;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.SubredditPaginator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import me.ccrama.redditslide.Activities.CommentsScreen;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;

/**
 * Created by ccrama on 9/17/2015.
 */
public class SubredditPosts {
    public ArrayList<Submission> posts;
    public boolean loading;
    public String subreddit;
    public boolean nomore = false;
    CommentsScreen pagerad;
    private SubredditPaginator paginator;
    private SwipeRefreshLayout refreshLayout;
    private SubmissionAdapter adapter;

    public SubredditPosts(ArrayList<Submission> firstData, SubredditPaginator paginator) {
        posts = firstData;
        this.paginator = paginator;
    }

    public SubredditPosts(String subreddit) {
        this.subreddit = subreddit;
    }

    public ArrayList<Submission> getPosts() {
        try {
            return new LoadData(true).execute(subreddit).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public void bindAdapter(SubmissionAdapter a, SwipeRefreshLayout layout) throws ExecutionException, InterruptedException {
        this.adapter = a;
        this.refreshLayout = layout;

        loadMore(a, true, subreddit);
    }

    public void loadMore(SubmissionAdapter adapter, boolean reset, String subreddit) {
        this.adapter = adapter;
        new LoadData(reset).execute(subreddit);


    }

    public void loadMore(CommentsScreen adapter, boolean reset) {
        this.pagerad = adapter;
        new LoadData(reset).execute(subreddit);

    }

    public void addData(List<Submission> data) {
        posts.addAll(data);
    }

    public class LoadData extends AsyncTask<String, Void, ArrayList<Submission>> {
        final boolean reset;

        public LoadData(boolean reset) {
            this.reset = reset;
        }

        @Override
        public void onPostExecute(ArrayList<Submission> subs) {
            loading = false;

            if (subs != null && subs.size() > 0) {

                Log.v("Slide", "DONE LOADING, SIZE IS NOW " + posts.size());

                (adapter.mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (refreshLayout != null)

                            refreshLayout.setRefreshing(false);


                        adapter.notifyDataSetChanged();

                    }
                });
            } else if (subs != null) {
                nomore = true;
            } else {
                adapter.setError(true);

            }
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
            if (paginator != null && paginator.hasNext()) {
                if (reset) {
                    posts = new ArrayList<>();
                    try {
                        for (Submission c : paginator.next()) {
                            Submission s = c;
                                if (SettingValues.NSFWPosts && s.isNsfw()) {
                                    posts.add(s);
                                } else if (!s.isNsfw()) {
                                    posts.add(s);
                                }


                        }
                    } catch (Exception ignored) {
                        //gets caught above
                    }
                } else {
                    if (posts == null)
                        posts = new ArrayList<>();

                    try {
                        for (Submission c : paginator.next()) {
                            Submission s = c;

                                if (SettingValues.NSFWPosts && s.isNsfw()) {
                                    posts.add(s);
                                } else if (!s.isNsfw()) {
                                    posts.add(s);

                            }

                        }
                    } catch (Exception ignored) {
                        //gets caught above
                    }
                }
            } else {
                nomore = true;
            }

            return posts;
        }
    }
}