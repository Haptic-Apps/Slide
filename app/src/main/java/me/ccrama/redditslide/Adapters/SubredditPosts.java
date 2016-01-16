package me.ccrama.redditslide.Adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.SubredditPaginator;

import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Cache;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.PostLoader;
import me.ccrama.redditslide.PostMatch;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.util.NetworkUtil;

/**
 * Created by ccrama on 9/17/2015.
 */
public class SubredditPosts implements PostLoader {
    public List<Submission> posts;
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
        posts = new ArrayList<>();
        this.subreddit = subreddit;
    }

    @Override
    public void loadMore(Context context, SubmissionDisplay displayer, boolean reset) {
        new LoadData(context, displayer, reset).execute(subreddit);
    }

    public void loadMore(Context context, SubmissionDisplay displayer, boolean reset, String subreddit) {
        this.subreddit = subreddit;
        loadMore(context, displayer, reset);
    }

    @Override
    public List<Submission> getPosts() {
        return posts;
    }

    @Override
    public boolean hasMore() {
        return !nomore;
    }

    private class LoadData extends AsyncTask<String, Void, ArrayList<Submission>> {
        final boolean reset;
        final Context context;
        final SubmissionDisplay displayer;

        public LoadData(Context context, SubmissionDisplay displayer, boolean reset) {
            this.context = context;
            this.displayer = displayer;
            this.reset = reset;
        }

        @Override
        public void onPostExecute(ArrayList<Submission> submissions) {
            loading = false;

            if (submissions != null && !submissions.isEmpty()) {
                // new submissions found
                int start = 0;
                if (posts != null) {
                    start = posts.size() + 1;
                }

                List<Submission> filteredSubmissions = new ArrayList<>();
                for (Submission s : submissions) {
                    if (!PostMatch.doesMatch(s)) {
                        filteredSubmissions.add(s);
                    }
                }

                if (reset || offline || posts == null) {
                    posts = filteredSubmissions;
                    start = -1;
                } else {
                    posts.addAll(filteredSubmissions);
                    offline = false;
                }

                final int finalStart = start;
                // update online
                displayer.updateSuccess(posts, finalStart);
            } else if (submissions != null) {
                // end of submissions
                nomore = true;
            } else if (Cache.hasSub(subreddit.toLowerCase()) && !nomore && Reddit.cache) {
                // is offline
                Log.v("Slide", "GETTING SUB " + subreddit.toLowerCase());
                offline = true;
                final OfflineSubreddit cached = Cache.getSubreddit(subreddit.toLowerCase());

                List<Submission> finalSubs = new ArrayList<>();
                for (Submission s : cached.submissions) {
                    if (!PostMatch.doesMatch(s)) {
                        finalSubs.add(s);
                    }
                }

                posts = finalSubs;
                if (cached.submissions.size() > 0) {
                    stillShow = true;
                } else {
                    displayer.updateOfflineError();
                }
                // update offline
                displayer.updateOffline(submissions, cached.time);
            } else if (!nomore) {
                // error
                displayer.updateError();
            }
        }

        @Override
        protected ArrayList<Submission> doInBackground(String... subredditPaginators) {
            Log.v("Slide", "DOING FOR " + subredditPaginators[0]);

            if (NetworkUtil.isConnected(context)) {
                stillShow = true;
                if (Reddit.cacheDefault && reset && !forced && Cache.hasSub(subredditPaginators[0]) && !doneOnce && Reddit.cache) {
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

                ArrayList<Submission> things = new ArrayList<>();

                if (paginator != null && paginator.hasNext()) {
                    for (Submission submission : paginator.next()) {
                        if (SettingValues.NSFWPosts || !submission.isNsfw()) {
                            things.add(submission);
                        }
                    }
                } else {
                    nomore = true;
                }

                if (Reddit.cache) {
                    Cache.writeSubreddit(things, subredditPaginators[0]);
                }

                DataShare.sharedSubreddit = things; // set this since it gets out of sync at CommentPage

                return things;
            } else {
                offline = true;
            }
            return null;
        }
    }
}