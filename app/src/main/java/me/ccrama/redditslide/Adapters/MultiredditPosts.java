package me.ccrama.redditslide.Adapters;

import android.content.Context;
import android.os.AsyncTask;

import net.dean.jraw.models.MultiReddit;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.MultiRedditPaginator;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Constants;
import me.ccrama.redditslide.HasSeen;
import me.ccrama.redditslide.LastComments;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.PhotoLoader;
import me.ccrama.redditslide.PostLoader;
import me.ccrama.redditslide.PostMatch;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SubmissionCache;
import me.ccrama.redditslide.Synccit.MySynccitReadTask;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.NetworkUtil;

/**
 * This class is reponsible for loading subreddit specific submissions
 * {@link loadMore(Context, SubmissionDisplay, boolean, String)} is implemented
 * asynchronously.
 * <p>
 * Created by ccrama on 9/17/2015.
 */
public class MultiredditPosts implements PostLoader {
    public List<Submission> posts;
    public boolean nomore = false;
    public boolean stillShow;
    public boolean offline;
    public boolean loading;
    public String profile;
    private MultiRedditPaginator paginator;
    Context c;
    MultiredditAdapter adapter;

    public MultiredditPosts(String multireddit) {
        this(multireddit, "");
    }

    public MultiredditPosts(String multireddit, String profile) {
        posts = new ArrayList<>();
        LogUtil.e("MJWHITTA: Profile is " + profile + ".");
        LogUtil.e("MJWHITTA: Multireddit is " + multireddit + ".");
        if (profile.isEmpty()) {
            this.multiReddit = UserSubscriptions.getMultiredditByDisplayName(multireddit);
        } else {
            this.multiReddit = UserSubscriptions.getPublicMultiredditByDisplayName(profile, multireddit);
        }
        this.profile = profile;
    }

    @Override
    public void loadMore(Context context, SubmissionDisplay displayer, boolean reset) {
        this.c = context;
        new LoadData(context, displayer, reset).execute(multiReddit);
    }

    public void loadMore(Context context, SubmissionDisplay displayer, boolean reset, MultiredditAdapter adapter) {
        this.adapter = adapter;
        this.c = context;
        loadMore(context, displayer, reset);
    }

    @Override
    public List<Submission> getPosts() {
        return posts;
    }

    public MultiReddit multiReddit;

    @Override
    public boolean hasMore() {
        return !nomore;
    }

    public boolean skipOne;
    boolean usedOffline;

    /**
     * Asynchronous task for loading data
     */
    private class LoadData extends AsyncTask<MultiReddit, Void, List<Submission>> {
        final boolean reset;
        Context context;
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

                if (reset || offline || posts == null) {
                    posts = new ArrayList<>(new LinkedHashSet(submissions));
                    start = -1;
                } else {
                    posts.addAll(submissions);
                    posts = new ArrayList<>(new LinkedHashSet(posts));
                    offline = false;
                }
                if (!usedOffline)
                    OfflineSubreddit.getSubreddit("multi" + multiReddit.getDisplayName().toLowerCase(Locale.ENGLISH), false, context).overwriteSubmissions(posts).writeToMemory(c);

                String[] ids = new String[submissions.size()];
                int i = 0;
                for (Submission s : submissions) {
                    ids[i] = s.getId();
                    i++;
                }
                if (!SettingValues.synccitName.isEmpty() && !offline) {
                    new MySynccitReadTask().execute(ids);
                }
                final int finalStart = start;

                // update online
                displayer.updateSuccess(posts, finalStart);

            } else if (submissions != null) {
                // end of submissions
                nomore = true;
            } else if (!OfflineSubreddit.getSubreddit("multi" + multiReddit.getDisplayName().toLowerCase(
                    Locale.ENGLISH), false, context).submissions.isEmpty() && !nomore && SettingValues.cache) {
                offline = true;
                final OfflineSubreddit cached = OfflineSubreddit.getSubreddit("multi" + multiReddit.getDisplayName().toLowerCase(Locale.ENGLISH), true, context);

                List<Submission> finalSubs = new ArrayList<>();
                for (Submission s : cached.submissions) {
                    if (!PostMatch.doesMatch(s, "multi" + multiReddit.getDisplayName().toLowerCase(Locale.ENGLISH), false)) {
                        finalSubs.add(s);
                    }
                }

                posts = finalSubs;

                if (!cached.submissions.isEmpty()) {
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
        protected List<Submission> doInBackground(MultiReddit... subredditPaginators) {
            if (!NetworkUtil.isConnected(context)) {
                offline = true;
                return null;
            } else {
                offline = false;
            }

            stillShow = true;

            if (reset || paginator == null) {
                offline = false;
                paginator = new MultiRedditPaginator(Authentication.reddit, subredditPaginators[0]);
                paginator.setSorting(SettingValues.getSubmissionSort(
                        "multi" + subredditPaginators[0].getDisplayName().toLowerCase(Locale.ENGLISH)));
                paginator.setTimePeriod(SettingValues.getSubmissionTimePeriod("multi" + subredditPaginators[0].getDisplayName().toLowerCase(Locale.ENGLISH)));
                paginator.setLimit(Constants.PAGINATOR_POST_LIMIT);
            }

            List<Submission> things = new ArrayList<>();

            try {
                if (paginator != null && paginator.hasNext()) {
                    things.addAll(paginator.next());
                } else {
                    nomore = true;
                }

            } catch (Exception e) {
                e.printStackTrace();
                if (e.getMessage().contains("Forbidden")) {
                    Reddit.authentication.updateToken(context);
                }

            }

            List<Submission> filteredSubmissions = new ArrayList<>();
            for (Submission s : things) {
                if (!PostMatch.doesMatch(s, paginator.getMultiReddit().getDisplayName(), false)) {
                    filteredSubmissions.add(s);
                }
            }

            HasSeen.setHasSeenSubmission(filteredSubmissions);
            SubmissionCache.cacheSubmissions(filteredSubmissions, context, paginator.getMultiReddit().getDisplayName());

            if (!(SettingValues.noImages && ((!NetworkUtil.isConnectedWifi(c) && SettingValues.lowResMobile) || SettingValues.lowResAlways)))
                PhotoLoader.loadPhotos(c, filteredSubmissions);

            if (SettingValues.storeHistory) LastComments.setCommentsSince(filteredSubmissions);

            return filteredSubmissions;
        }
    }
}
