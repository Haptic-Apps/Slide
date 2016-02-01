package me.ccrama.redditslide.Adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.SubredditPaginator;

import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Activities.MainActivity;
import me.ccrama.redditslide.Activities.SubredditView;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.PostLoader;
import me.ccrama.redditslide.PostMatch;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.NetworkUtil;

/**
 * This class is reponsible for loading subreddit specific submissions
 * {@link loadMore(Context, SubmissionDisplay, boolean, String)} is implemented
 * asynchronously.
 *
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
    public void loadMore(Context context, SubmissionDisplay display, boolean reset) {
        new LoadData(context, display, reset).execute(subreddit);
    }

    public void loadMore(Context context, SubmissionDisplay display, boolean reset, String subreddit) {
        this.subreddit = subreddit;
        loadMore(context, display, reset);
    }

    @Override
    public List<Submission> getPosts() {
        return posts;
    }

    @Override
    public boolean hasMore() {
        return !nomore;
    }
   public boolean skipOne;

    /**
     * Asynchronous task for loading data
     */
    private class LoadData extends AsyncTask<String, Void, List<Submission>> {
        final boolean reset;
        final Context context;
        final SubmissionDisplay displayer;

        public LoadData(Context context, SubmissionDisplay displayer, boolean reset) {
            this.context = context;
            this.displayer = displayer;
            this.reset = reset;
        }

        @Override
        public void onPostExecute(List<Submission> submissions) {
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

                new OfflineSubreddit(subreddit).overwriteSubmissions(posts).writeToMemory();

                // update online
                displayer.updateSuccess(posts, finalStart);
            } else if (submissions != null) {
                // end of submissions
                nomore = true;
            } else if (!new OfflineSubreddit(subreddit).submissions.isEmpty()  && !nomore && SettingValues.cache) {
                offline = true;
                final OfflineSubreddit cached = new OfflineSubreddit(subreddit);

                List<Submission> finalSubs = new ArrayList<>();
                for (Submission s : cached.submissions) {
                    if (!PostMatch.doesMatch(s)) {
                        finalSubs.add(s);
                    }
                }

                posts = finalSubs;
                new OfflineSubreddit(subreddit).overwriteSubmissions(posts).writeToMemory();

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
        protected List<Submission> doInBackground(String... subredditPaginators) {
            Log.v(LogUtil.getTag(), "DOING FOR " + subredditPaginators[0]);

            if (!NetworkUtil.isConnected(context)) {
                offline = true;
                return null;
            } else {
                offline = false;
            }

            stillShow = true;
            if (SettingValues.cacheDefault && reset && !forced && !new OfflineSubreddit(subreddit).submissions.isEmpty() && !doneOnce && SettingValues.cache) {
                offline = true;
                doneOnce = true;
                return null;
            }

            if (reset || paginator == null) {
                offline = false;
                if (subredditPaginators[0].toLowerCase().equals("frontpage")) {
                    paginator = new SubredditPaginator(Authentication.reddit);
                } else {
                    paginator = new SubredditPaginator(Authentication.reddit, subredditPaginators[0]);

                }
                paginator.setSorting(Reddit.getSorting(subreddit));
                paginator.setTimePeriod(Reddit.getTime(subreddit));
                paginator.setLimit(25);
                if(skipOne){
                    paginator.next();
                }
            }

            List<Submission> things = new ArrayList<>();

            try {
                if (paginator != null && paginator.hasNext()) {
                    for (Submission submission : paginator.next()) {
                        things.add(submission);

                    }
                } else {
                    nomore = true;
                }

            } catch(Exception e){
                e.printStackTrace();

            }

            return things;
        }
    }
}