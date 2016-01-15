package me.ccrama.redditslide.Adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.SubredditPaginator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Cache;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.util.NetworkUtil;

/**
 * Created by ccrama on 9/17/2015.
 */
public class SubredditPosts {
    public ArrayList<Submission> posts;
    public String subreddit;
    public boolean nomore = false;
    public boolean stillShow;
    public boolean offline;
    public boolean forced;
    public boolean loading;
    private SubredditPaginator paginator;
    public OfflineSubreddit cached;
    boolean doneOnce;

    public SubredditPosts(String subreddit) {
        this.subreddit = subreddit;
    }

    public void loadMore(Context context, SubmissionDisplay displayer, boolean reset) {
        try {
            List<Submission> submissions = new LoadData(context, displayer, reset).execute(subreddit).get();
            displayer.update(submissions, reset, offline, subreddit);
        } catch (InterruptedException e) {
            // TODO
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO
            e.printStackTrace();
        }
    }

    public void loadMore(Context context, SubmissionDisplay displayer, boolean reset, String subreddit) {
        this.subreddit = subreddit;
        loadMore(context, displayer, reset);
    }

    public class LoadData extends AsyncTask<String, Void, ArrayList<Submission>> {
        final boolean reset;
        final Context context;
        final SubmissionDisplay displayer;

        public LoadData(Context context, SubmissionDisplay displayer, boolean reset) {
            this.context = context;
            this.displayer = displayer;
            this.reset = reset;
        }

        @Override
        public void onPostExecute(ArrayList<Submission> subs) {
            loading = false;

        }

        @Override
        protected ArrayList<Submission> doInBackground(String... subredditPaginators) {
            ArrayList<Submission> things = new ArrayList<>();

            Log.v("Slide", "DOING FOR " + subredditPaginators[0]);

            if (posts == null) {
                posts = new ArrayList<>();
            }

            if (NetworkUtil.isConnected(context)) {
                stillShow = true;
                if (Reddit.cacheDefault && reset && !forced && Cache.hasSub(subredditPaginators[0]) && !doneOnce) {
                    offline = true;
                    doneOnce = true;
                    return null;
                }

                offline = false;

                if (reset || paginator == null) {

                    offline = false;
                    if (subredditPaginators[0].toLowerCase().equals("frontpage")) {
                        paginator = new SubredditPaginator(Authentication.reddit);
                    } else {
                        paginator = new SubredditPaginator(Authentication.reddit, subredditPaginators[0]);

                    }
                    paginator.setSorting(Reddit.defaultSorting);
                    paginator.setTimePeriod(Reddit.timePeriod);
                    paginator.setLimit(25);
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

                if (Reddit.cache)
                    Cache.writeSubreddit(things, subredditPaginators[0]);
                return things;


            } else {
                offline = true;
            }
            return null;

        }
    }
}