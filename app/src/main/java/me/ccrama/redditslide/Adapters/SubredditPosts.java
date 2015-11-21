package me.ccrama.redditslide.Adapters;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.widget.Toast;

import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.SubredditPaginator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import me.ccrama.redditslide.Activities.CommentsScreen;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Cache;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.util.NetworkUtil;

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
    public boolean stillShow;
    public boolean offline;

    public void loadMore(CommentsScreen adapter, boolean reset) {
        this.pagerad = adapter;
        new LoadData(reset).execute(subreddit);

    }
    public OfflineSubreddit cached;

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
                if(reset || offline){
                    posts = subs;
                } else {
                    posts.addAll(subs);
                }


                Log.v("Slide", "DONE LOADING, SIZE IS NOW " + posts.size());

                (adapter.mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (refreshLayout != null) {

                            refreshLayout.setRefreshing(false);

                        }


                        adapter.notifyDataSetChanged();

                    }
                });
            } else if (subs != null) {
                nomore = true;
            } else if(Cache.hasSub(subreddit) && !nomore && Reddit.cache) {
                offline = true;
                cached = Cache.getSubreddit(subreddit);
                posts = cached.submissions;
                (adapter.mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (refreshLayout != null) {

                            refreshLayout.setRefreshing(false);
                            Toast.makeText(refreshLayout.getContext(), "Last updated " + TimeUtils.getTimeAgo(cached.time, refreshLayout.getContext()), Toast.LENGTH_SHORT).show();

                        }


                        adapter.notifyDataSetChanged();

                    }
                });
            } else if(!nomore){
                if (refreshLayout != null)

                    refreshLayout.setRefreshing(false);
                adapter.setError(true);

            }


        }

        @Override
        protected ArrayList<Submission> doInBackground(String... subredditPaginators) {
            ArrayList<Submission> things = new ArrayList<>();

            if(posts == null){
                posts = new ArrayList<>();
            }

            if(NetworkUtil.getConnectivityStatus(refreshLayout.getContext())) { //is online'
                stillShow = true;
                if(Reddit.cacheDefault  && reset && !offline){
                    offline = true;
                    return null;
                }
                if (reset || paginator == null) {

                    offline = false;
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
                            try {
                                for (Submission c : paginator.next()) {
                                    Submission s = c;
                                    if (SettingValues.NSFWPosts && s.isNsfw()) {
                                        things.add(s);
                                    } else if (!s.isNsfw()) {
                                        things.add(s);
                                    }


                                }
                            } catch (Exception ignored) {
                                //gets caught above
                            }
                        } else {


                            try {
                                for (Submission c : paginator.next()) {
                                    Submission s = c;

                                    if (SettingValues.NSFWPosts && s.isNsfw()) {
                                        things.add(s);
                                    } else if (!s.isNsfw()) {
                                        things.add(s);

                                    }

                                }
                            } catch (Exception ignored) {
                                //gets caught above
                            }
                        }
                    } else {
                        nomore = true;
                    }

                    if(Reddit.cache)
                    Cache.writeSubreddit(things, subredditPaginators[0]);
                    return things;



            }
            return null;

        }
    }
}